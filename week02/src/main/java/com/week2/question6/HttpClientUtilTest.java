package com.week2.question6;

import com.week2.BIOSingleThreadHttpServer;
import com.week2.NettyHttpServer;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpClientUtilTest {

	public static void main(String[] args) {
		new Thread(() -> NettyHttpServer.startServer()).start();
		for (;;) {
			System.out.println(getRequest("http://127.0.0.1:8080"));
		}
	}

	public static String getRequest(String url) {
		try {
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpClient httpRequest = HttpClientBuilder.create().build();
			CloseableHttpResponse httpResponse = httpRequest.execute(httpGet);
			String result = EntityUtils.toString(httpResponse.getEntity());
			httpRequest.close();
			httpResponse.close();
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
