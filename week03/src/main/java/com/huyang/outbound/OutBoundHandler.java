package com.huyang.outbound;

import com.huyang.filter.HttpRequestFilter;
import com.huyang.filter.HttpResponseFilter;
import com.huyang.router.HttpRouter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;

import java.util.UUID;

public abstract class OutBoundHandler {

	protected HttpRouter httpRouter;
	protected HttpRequestFilter httpRequestFilter;
	protected HttpResponseFilter httpResponseFilter;

	protected OutBoundHandler(HttpRouter httpRouter, HttpRequestFilter httpRequestFilter, HttpResponseFilter httpResponseFilter) {
		this.httpRouter = httpRouter;
		this.httpRequestFilter = httpRequestFilter;
		this.httpResponseFilter = httpResponseFilter;
	}

	public void handle(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullRequest) {
		String traceId = UUID.randomUUID().toString().replace("-", "");
		httpRequestFilter.filter(fullRequest, traceId);
		this.doHandle(httpRouter.route(), fullRequest, channelHandlerContext);
	}

	abstract void doHandle(String url, FullHttpRequest fullRequest, ChannelHandlerContext channelHandlerContext);
}
