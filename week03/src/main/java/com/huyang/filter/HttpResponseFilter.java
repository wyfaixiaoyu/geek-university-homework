package com.huyang.filter;

import io.netty.handler.codec.http.FullHttpResponse;

public interface HttpResponseFilter {
	void filter(FullHttpResponse fullHttpResponse, String traceId);
}
