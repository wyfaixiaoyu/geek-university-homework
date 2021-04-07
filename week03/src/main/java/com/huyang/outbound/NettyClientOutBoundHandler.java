package com.huyang.outbound;

import com.huyang.filter.HttpRequestFilter;
import com.huyang.filter.HttpResponseFilter;
import com.huyang.router.HttpRouter;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;

public class NettyClientOutBoundHandler extends OutBoundHandler{

	public NettyClientOutBoundHandler(HttpRouter httpRouter, HttpRequestFilter httpRequestFilter, HttpResponseFilter httpResponseFilter) {
		super(httpRouter, httpRequestFilter, httpResponseFilter);
	}

	@Override
	void doHandle(String url, FullHttpRequest fullRequest, ChannelHandlerContext channelHandlerContext) {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			String[] address = url.split(":");
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group)
					.channel(NioSocketChannel.class)
					.remoteAddress(new InetSocketAddress(address[0], Integer.valueOf(address[1])))
					.handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel socketChannel) throws Exception {
							socketChannel.pipeline().addLast(new ClientHandler(fullRequest, channelHandlerContext, httpResponseFilter));
						}
					});

			ChannelFuture channelFuture = bootstrap.connect().sync();
			channelFuture.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			group.shutdownGracefully();
		}
	}

	class ClientHandler extends ChannelInboundHandlerAdapter {
		private FullHttpRequest fullHttpRequest;
		private  ChannelHandlerContext channelHandlerContext;
		private  HttpResponseFilter httpResponseFilter;

		public ClientHandler(FullHttpRequest fullHttpRequest, ChannelHandlerContext channelHandlerContext, HttpResponseFilter httpResponseFilter) {
			this.fullHttpRequest = fullHttpRequest;
			this.channelHandlerContext = channelHandlerContext;
			this.httpResponseFilter = httpResponseFilter;
		}

		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
			FullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, this.fullHttpRequest.uri());

			HttpHeaders headers = this.fullHttpRequest.headers();
			Iterator<Map.Entry<String, String>> headerIterator = headers.iteratorAsString();
			Map.Entry<String, String> header;
			while (headerIterator.hasNext()) {
				header = headerIterator.next();
				request.headers().add(header.getKey(), header.getValue());
			}
			ctx.writeAndFlush(request);
		}

		@Override
		public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
			ctx.flush();
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
			System.out.println("channelRead " + msg.toString());
			if (msg instanceof FullHttpResponse) {
				FullHttpResponse response = (FullHttpResponse) msg;
				FullHttpResponse fullHttpResponse = null;
				try {
					ByteBuf byteBuf = response.content();
					fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, byteBuf);
					fullHttpResponse.headers().set("Content-Length", byteBuf.array().length);
					if (this.httpResponseFilter != null && this.fullHttpRequest != null) {
						httpResponseFilter.filter(fullHttpResponse, this.fullHttpRequest.headers().get("traceId"));
					}
				} catch (Exception e) {
					e.printStackTrace();
					fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
				} finally {
					if (this.fullHttpRequest != null) {
						if (!HttpUtil.isKeepAlive(this.fullHttpRequest)) {
							this.channelHandlerContext.write(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
						} else {
							fullHttpResponse.headers().set("Connection", "keep-alive");
							this.channelHandlerContext.write(fullHttpResponse);
						}
					}
				}
			}
		}


		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
			cause.printStackTrace();
			ctx.close();
		}
	}
}
