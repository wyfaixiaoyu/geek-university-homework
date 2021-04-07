package com.huyang.filter;

import io.netty.handler.codec.http.FullHttpResponse;

public class HeaderHttpResponseFilter implements HttpResponseFilter{
	@Override
	public void filter(FullHttpResponse fullHttpResponse, String traceId) {
		fullHttpResponse.headers().set("traceId", traceId);
	}
}
