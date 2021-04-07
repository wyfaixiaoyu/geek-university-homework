package com.huyang.router;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterConfiguration {
	@Value("${gateway.server.proxy}")
	private String proxyServices;

	@Bean
	public HttpRouter nettyClientOutBoundHandler() {
		String[] proxyServiceList = proxyServices.split(",");
		return new RandomHttpRouter(proxyServiceList);
	}

}
