package com.huyang;

import com.huyang.inbound.NettyHttpInboundServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NettyGateWayApplication implements ApplicationRunner {
	@Autowired
	private NettyHttpInboundServer nettyHttpInboundServer;

	public static void main(String[] args) {
		SpringApplication.run(NettyGateWayApplication.class);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		System.out.println("NettyHttpInboundServer start ...");
		nettyHttpInboundServer.startServer();
	}
}
