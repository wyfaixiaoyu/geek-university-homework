package com.week2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;

public class NettyHttpServer {
	public static void main(String[] args){
		startServer();
	}

	public static void startServer() {
		int port = 8080;
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

	static class HttpInitializer extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel socketChannel) throws Exception {
			ChannelPipeline channelPipeline = socketChannel.pipeline();
			channelPipeline.addLast(new HttpServerCodec());
			channelPipeline.addLast(new HttpObjectAggregator(1024 * 1024));
			channelPipeline.addLast(new HttpHandler());

		}
	}

	static class HttpHandler extends ChannelInboundHandlerAdapter {
		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			FullHttpResponse fullHttpResponse = null;
			FullHttpRequest fullHttpRequest = null;
			try {
				fullHttpRequest = (FullHttpRequest)msg;
				String uri = fullHttpRequest.uri();
				byte[] content = "{\"result\":\"ok\"}".getBytes();
				fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
						HttpResponseStatus.OK,
						Unpooled.wrappedBuffer(content));

				fullHttpResponse.headers().set("Content-Type", content);
				fullHttpResponse.headers().set("Content-Length", content.length);


			} catch (Exception e) {
				e.printStackTrace();
				fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
			} finally {
				if (fullHttpRequest != null) {
					if (!HttpUtil.isKeepAlive(fullHttpRequest)) {
						ctx.write(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
					} else {
						fullHttpResponse.headers().set("Connection", "keep-alive");
						ctx.write(fullHttpResponse);
					}
				}
				ReferenceCountUtil.release(msg);
			}

		}
	}
}
