package com.huyang.router;

import java.util.Random;

public class RandomHttpRouter implements HttpRouter{

	private String[] endpoints;

	public RandomHttpRouter(String[] endpoints) {
		this.endpoints = endpoints;
	}

	@Override
	public String route() {
		int size = this.endpoints.length;
		Random random = new Random(System.currentTimeMillis());
		return endpoints[random.nextInt(size)];
	}
}
