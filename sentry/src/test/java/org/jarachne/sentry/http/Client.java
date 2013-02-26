package org.jarachne.sentry.http;

import static org.jboss.netty.channel.Channels.pipeline;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jarachne.sentry.handler.FileClientHandler;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

public class Client {

	public static void main(String[] args) throws ClientProtocolException, IOException, InterruptedException
	{

//		HttpClient client = new DefaultHttpClient();
//		HttpParams httpParams = client.getParams();
//		HttpConnectionParams.setConnectionTimeout(httpParams, 5000);// 连接超时时间3秒
//		HttpConnectionParams.setSoTimeout(httpParams, 5000);// 获取数据超时时间
//		HttpGet get = new HttpGet("http://localhost:8001/1");
//		client.execute(get);
//		String result = client.execute(new HttpGet("http://localhost:8001/1"), new BasicResponseHandler());
//		System.out.println(result);
//		result = client.execute(new HttpGet("http://localhost:8002/2222"), new BasicResponseHandler());
//		System.out.println(result);
//		result = client.execute(new HttpGet("http://localhost:8001/22322"), new BasicResponseHandler());
//		System.out.println(result);
//		System.exit(0);
		ClientBootstrap bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
		{

			public ChannelPipeline getPipeline() throws Exception
			{
				ChannelPipeline pipeline = pipeline();

				pipeline.addLast("decoder", new HttpResponseDecoder());

				/*
				 * 不能添加这个，对传输文件 进行了大小的限制。。。。。
				 */
//				              pipeline.addLast("aggregator", new HttpChunkAggregator(6048576));
				pipeline.addLast("encoder", new HttpRequestEncoder());
				pipeline.addLast("handler", new ClientHandler());

				return pipeline;
			}

		});
		
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(	"localhost", 8001));
//		future.awaitUninterruptibly();
		Channel channel = future.getChannel();
		HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "aaa");
		channel.write(req);
//		channel.getCloseFuture();
		
		channel.write(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "agggaa"));
		channel.write(new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "agggeeeaa"));
		System.out.println("1");
		
		//		channel.getCloseFuture().awaitUninterruptibly();
		
		channel.close();
		bootstrap.releaseExternalResources();
//		future.awaitUninterruptibly();
//		channel.getCloseFuture().awaitUninterruptibly();
	}
}
