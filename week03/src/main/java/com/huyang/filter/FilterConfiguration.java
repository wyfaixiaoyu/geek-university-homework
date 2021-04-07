package com.huyang.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfiguration {
	@Bean
	public HttpRequestFilter headerHttpRequestFilter() {
		return new HeaderHttpRequestFilter();
	}

	@Bean
	public HttpResponseFilter headerHttpResponseFilter() {
		return new HeaderHttpResponseFilter();
	}
}
