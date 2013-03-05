package org.jarachne.sentry.master;

import static org.jboss.netty.channel.Channels.pipeline;


import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.sentry.handler.ToSlaveRequestHandler;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class MasterModule {
	private volatile Map<String, String> slaves;

	public ClientBootstrap client;
	
	public MasterModule(){
		slaves = new ConcurrentHashMap<String, String>();
		client = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
	}

	
	public void addSlave(String hostPort){
		slaves.put(hostPort, null);
	}
	
	public String removeSlave(String hostPort){
		return this.slaves.remove(hostPort);
	}
	
	public Map<String, String> copySlave(){
		Map<String, String> slaves = new ConcurrentHashMap<String, String>();
		for(String slaveAddress : this.slaves.keySet()){
			slaves.put(slaveAddress, null);
		}
		return slaves;
	}
	
	public List<String> yieldSlaves(){
		List<String> a = new ArrayList<String>();
		a.addAll(this.slaves.keySet());
		return a;
	}
	
	public String requestSlaves(ToSlaveRequestHandler tsReq){
		return this.requestSlaves(tsReq, 2000);
	}
	
	
	public String requestSlaves(ToSlaveRequestHandler tsReq, long soTimeOut){
		if (slaves.isEmpty()){
			return "";
		}
		List<String> slaves = yieldSlaves();
		final AbstractDistributedChannelHandler channelHandler = tsReq.getChannelHandler().clone(slaves);
		
		
		client.setPipelineFactory(new ChannelPipelineFactory()
		{

			public ChannelPipeline getPipeline() throws Exception
			{
				ChannelPipeline pipeline = pipeline();

				pipeline.addLast("decoder", new HttpResponseDecoder());
//				pipeline.addLast("aggregator", new HttpChunkAggregator(6048576));
				pipeline.addLast("encoder", new HttpRequestEncoder());
				pipeline.addLast("handler", channelHandler);

				return pipeline;
			}

		});
		ArrayList<ChannelFuture> channelFutrueList = new ArrayList<ChannelFuture>(slaves.size());
		for(String slaveAddress : slaves){
			int idx = slaveAddress.indexOf(':');
			ChannelFuture future = client.connect(new InetSocketAddress(slaveAddress.substring(0, idx), new Integer(slaveAddress.substring(idx + 1))));
			channelFutrueList.add(future);
			
		}
		for(ChannelFuture cf : channelFutrueList){
			if (!cf.awaitUninterruptibly(500)){
				return "!bad connection";
			}
		}
		for(ChannelFuture cf : channelFutrueList){
			HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, tsReq.getUri());
			cf.getChannel().write(req);
		}
		for(ChannelFuture cf : channelFutrueList){
			cf.awaitUninterruptibly(soTimeOut);
		}
		return tsReq.getChannelHandler().processResult();	
	}
	
}
