package com.week2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class BIOMutiThreadHttpServer {
	public static void main(String[] args) {
		startServer();
	}

	public static void startServer() {
		try {
			ServerSocket serverSocket = new ServerSocket();
			SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", 8080);
			serverSocket.bind(socketAddress);
			for (;;) {
				Socket socket = serverSocket.accept();
				new Thread(() -> SocketWriteDataUtil.writeData(socket)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
