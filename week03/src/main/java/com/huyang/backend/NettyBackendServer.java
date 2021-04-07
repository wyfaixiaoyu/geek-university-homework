package com.huyang.backend;

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

import java.util.concurrent.CompletableFuture;

public class NettyBackendServer {
	public static void main(String[] args) {
		CompletableFuture.runAsync(() -> {
			System.out.println("NettyBackendServer-1 start ...");
			NettyBackendServer nettyBackendServer1 = new NettyBackendServer(8081, "BackendServer-1");
			nettyBackendServer1.startServer();
		});

		System.out.println("NettyBackendServer-2 start ...");
		NettyBackendServer nettyBackendServer2 = new NettyBackendServer(8082, "BackendServer-2");
		nettyBackendServer2.startServer();
	}

	private int port;
	private String serverName;

	public NettyBackendServer(int port, String serverName) {
		this.port = port;
		this.serverName = serverName;
	}

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
					.childHandler(new HttpInitializer(this.serverName));

			Channel channel = serverBootstrap.bind(this.port).sync().channel();
			channel.closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			bossEventLoopGroup.shutdownGracefully();
			workEventLoopGroup.shutdownGracefully();
		}
	}

	class HttpInitializer extends ChannelInitializer<SocketChannel> {
		private String serverName;

		public HttpInitializer(String serverName) {
			this.serverName = serverName;
		}

		@Override
		protected void initChannel(SocketChannel socketChannel) throws Exception {
			ChannelPipeline channelPipeline = socketChannel.pipeline();
			channelPipeline.addLast(new HttpServerCodec());
			channelPipeline.addLast(new HttpObjectAggregator(1024 * 1024));
			channelPipeline.addLast(new HttpHandler(this.serverName));

		}
	}

	class HttpHandler extends ChannelInboundHandlerAdapter {
		private String serverName;

		public HttpHandler(String serverName) {
			this.serverName = serverName;
		}

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
				System.out.println("NettyBackendServer channelRead traceId:" + fullHttpRequest.headers().get("traceId"));
				byte[] content = ("{\"result\":\"ok\", \"server\":\"" + this.serverName + "\"}").getBytes();
				fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
						HttpResponseStatus.OK,
						Unpooled.wrappedBuffer(content));

				fullHttpResponse.headers().set("Content-Type", "application/json");
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
