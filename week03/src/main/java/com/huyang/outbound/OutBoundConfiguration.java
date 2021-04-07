package com.huyang.outbound;

import com.huyang.filter.HttpRequestFilter;
import com.huyang.filter.HttpResponseFilter;
import com.huyang.router.HttpRouter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OutBoundConfiguration {
	private final static String OUT_BOUND_STRATEGY_NETTY = "NETTY";
	private final static String OUT_BOUND_STRATEGY_HTTPCLIENT = "HTTPCLIENT";

	@Value("${gateway.outbound.strategy}")
	private String strategy;

	@Bean
	public OutBoundHandler anettyClientOutBoundHandler(HttpRouter httpRouter, HttpRequestFilter httpRequestFilter, HttpResponseFilter httpResponseFilter) {
		if (OUT_BOUND_STRATEGY_NETTY.equals(strategy)) {
			return new NettyClientOutBoundHandler(httpRouter, httpRequestFilter, httpResponseFilter);
		}
		return new HttpClientOutBoundHandler(httpRouter, httpRequestFilter, httpResponseFilter);
	}
}
