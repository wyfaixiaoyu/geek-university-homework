package com.week2;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class SocketWriteDataUtil {
	public static void writeData(Socket socket) {
		try(OutputStream outputStream = socket.getOutputStream()) {
			String bodyContent = "{\"result\":\"ok\"}";
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("HTTP/1.1 200 OK\n");
			stringBuilder.append("Content-Type: application/json; charset=utf-8\n");
			stringBuilder.append("Content-Length: " + bodyContent.getBytes().length + "\n");
			stringBuilder.append("\n");
			stringBuilder.append(bodyContent);
			outputStream.write(stringBuilder.toString().getBytes());
			outputStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
