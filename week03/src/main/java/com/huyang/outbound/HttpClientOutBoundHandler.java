package com.huyang.outbound;

import com.huyang.filter.HttpRequestFilter;
import com.huyang.filter.HttpResponseFilter;
import com.huyang.router.HttpRouter;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.*;


public class HttpClientOutBoundHandler extends OutBoundHandler{
	ExecutorService httpClientOutBoundHandlerService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Runtime.getRuntime().availableProcessors(),
			1000, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(2048),
                new NamedThreadFactory("HttpClientOutBoundHandler"), new ThreadPoolExecutor.CallerRunsPolicy());

	public HttpClientOutBoundHandler(HttpRouter httpRouter, HttpRequestFilter httpRequestFilter, HttpResponseFilter httpResponseFilter) {
		super(httpRouter, httpRequestFilter, httpResponseFilter);
	}

	@Override
	void doHandle(String url, FullHttpRequest fullRequest, ChannelHandlerContext channelHandlerContext) {
		executeRequest(url, fullRequest, channelHandlerContext);

		//can't work in threadPool
		//CompletableFuture.runAsync(() -> executeRequest(url, fullRequest, channelHandlerContext), httpClientOutBoundHandlerService);
	}

	private void executeRequest(String url, FullHttpRequest fullRequest, ChannelHandlerContext channelHandlerContext) {
		HttpGet httpGet = new HttpGet("http://" + url);
		CloseableHttpClient httpRequest = HttpClientBuilder.create().build();
		CloseableHttpResponse httpResponse;
		FullHttpResponse fullHttpResponse = null;
		try {
			httpGet.addHeader("traceId", fullRequest.headers().get("traceId"));
			httpResponse = httpRequest.execute(httpGet);
			HttpEntity entity = httpResponse.getEntity();
			String result = EntityUtils.toString(entity);
			httpRequest.close();
			httpResponse.close();

			fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(result, CharsetUtil.UTF_8));
			fullHttpResponse.headers().set("Content-Length", entity.getContentLength());
			fullHttpResponse.headers().set("Content-Type", httpResponse.getFirstHeader("Content-Type"));

			httpResponseFilter.filter(fullHttpResponse, fullRequest.headers().get("traceId"));
		} catch (IOException e) {
			e.printStackTrace();
			fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NO_CONTENT);
		} finally {
			if (fullRequest != null) {
				if (!HttpUtil.isKeepAlive(fullRequest)) {
					channelHandlerContext.write(fullHttpResponse).addListener(ChannelFutureListener.CLOSE);
				} else {
					fullHttpResponse.headers().set("Connection", "keep-alive");
					channelHandlerContext.write(fullHttpResponse);
				}
			}
		}
	}
}
