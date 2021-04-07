package com.huyang.inbound;

import com.huyang.outbound.OutBoundHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NettyHttpInboundServer {
	@Value("${server.port}")
	private int port;

	@Autowired
	private OutBoundHandler outBoundHandler;

	public void startServer() {
		EventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(2);
		EventLoopGroup workEventLoopGroup = new NioEventLoopGroup(16);
		try {

			ServerBootstrap serverBootstrap = new ServerBootstrap();

			serverBootstrap.option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.TCP_NODELAY, true)
					.childOption(ChannelOption.SO_KEEPALIVE, true)
					.childOption(ChannelOption.SO_REUSEADDR, true)
					.childOption(ChannelOption.SO_RCVBUF, 32 * 1024)
					.childOption(ChannelOption.SO_SNDBUF, 32 * 1024)
					.childOption(EpollChannelOption.SO_REUSEPORT, true)
					.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

			serverBootstrap.group(bossEventLoopGroup, workEventLoopGroup).channel(NioServerSocketChannel.class)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(new HttpInitializer());

			Channel channel = serverBootstrap.bind(port).sync().channel();
			channel.closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bossEventLoopGroup.shutdownGracefully();
			workEventLoopGroup.shutdownGracefully();
		}
	}

	class HttpInitializer extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel socketChannel) throws Exception {
			ChannelPipeline channelPipeline = socketChannel.pipeline();
			channelPipeline.addLast(new HttpServerCodec());
			channelPipeline.addLast(new HttpObjectAggregator(1024 * 1024));
			channelPipeline.addLast(new HttpHandler());
		}
	}

	class HttpHandler extends ChannelInboundHandlerAdapter {
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
		}

		@Override
		public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) {
			try {
				outBoundHandler.handle(channelHandlerContext, (FullHttpRequest)msg);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				ReferenceCountUtil.release(msg);
			}
		}
	}
}
