package com.week2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BIOThreadPoolHttpServer {
	public static void main(String[] args) {
		startServer();
	}

	public static void startServer() {
		try {
			ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);

			ServerSocket serverSocket = new ServerSocket();
			SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 8080);
			serverSocket.bind(socketAddress);
			for (;;) {
				Socket socket = serverSocket.accept();
				executorService.execute(() -> SocketWriteDataUtil.writeData(socket));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
