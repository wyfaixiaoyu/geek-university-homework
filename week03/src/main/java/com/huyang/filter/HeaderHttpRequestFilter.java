package com.huyang.filter;

import io.netty.handler.codec.http.FullHttpRequest;

public class HeaderHttpRequestFilter implements HttpRequestFilter{
	@Override
	public void filter(FullHttpRequest fullRequest, String traceId) {
		fullRequest.headers().set("traceId", traceId);
	}
}
