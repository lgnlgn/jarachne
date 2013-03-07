package org.jarachne.sentry.master;

import static org.jboss.netty.channel.Channels.pipeline;


import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.zookeeper.KeeperException;
import org.jarachne.common.Constants;
import org.jarachne.common.HttpRequestCallable;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.util.ZKClient;
import org.jarachne.util.ZKClient.ChildrenWatcher;
import org.jarachne.util.concurrent.ConcurrentExecutor;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class MasterModule {
	private volatile Map<String, String> slaves;

	public ClientBootstrap bootstrap;
	private HttpClient httpClient;
	private ChildrenWatcher cw;
	
	public MasterModule() throws KeeperException, InterruptedException{
		slaves = new ConcurrentHashMap<String, String>();
		bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		httpClient = new DefaultHttpClient();
		ZKClient.get().createIfNotExist(Constants.ZK_MASTER_PATH);
		ZKClient.get().createIfNotExist(Constants.ZK_SLAVE_PATH);
		this.watchZookeeper();
	}

	
	public void addSlave(String hostPort){
		this.slaves.put(hostPort, "");
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
	

	public void watchZookeeper(){
		cw = new ChildrenWatcher() {
			@Override
			public void nodeRemoved(String node) {
				removeSlave(node);
				
			}
			@Override
			public void nodeAdded(String node) {
				addSlave(node);
				System.out.println(slaves);
			}
		};
		ZKClient.get().watchChildren(Constants.ZK_SLAVE_PATH, cw);
	}
	
	
	
	public List<String> yieldSlaves(){
		List<String> a = new ArrayList<String>();
		a.addAll(this.slaves.keySet());
		return a;
	}
	
	public String requestSlaves(AbstractDistributedChannelHandler tsReq) throws InterruptedException, ExecutionException{
//		return this.requestSlaves(tsReq, 2000);
		List<String> slaves = yieldSlaves();
		List<Callable<String >> distributedReqs = new ArrayList<Callable<String >> ();
		for(String slaveAddr: slaves){
			distributedReqs.add(new HttpRequestCallable(httpClient, "http://" + slaveAddr + tsReq.requestSlaveUri()));
		}
		
		List<String> results = ConcurrentExecutor.execute(distributedReqs);
		return results.toString();
	}
	
	
	
	private static boolean waitFutures(List<ChannelFuture> cfs, long timeOut) throws InterruptedException{
		int i = 0 ;
		long t = System.currentTimeMillis();
		while(true){
			for(ChannelFuture cf : cfs){
				if (cf.isDone()){
					i += 1;
				}
			}
			if (i == cfs.size())
				return true;
			else {
				i = 0;
			}
			long t2 = System.currentTimeMillis() -t;
			if (t2 > timeOut)
				return false;
			Thread.sleep(10);
		}
	}
	
	
	
	
	public String requestSlaves(AbstractDistributedChannelHandler tsReq, long soTimeOut) throws InterruptedException{
		if (slaves.isEmpty()){
			return "";
		}
		List<String> slaves = yieldSlaves();
		final AbstractDistributedChannelHandler channelHandler = tsReq.clone(slaves);
		System.out.println("sending");
		
		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
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
			ChannelFuture future = bootstrap.connect(new InetSocketAddress(slaveAddress.substring(0, idx), new Integer(slaveAddress.substring(idx + 1))));
			channelFutrueList.add(future);
			
		}
		boolean ok;
		for(ChannelFuture cf : channelFutrueList){
			ok = cf.awaitUninterruptibly(1000);
			if (!ok)
				throw new RuntimeException("---!!!!!!!!!!---------");
		}

		ArrayList<ChannelFuture> reqCF = new ArrayList<ChannelFuture>();

		for(ChannelFuture cf : channelFutrueList){
			HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, tsReq.requestSlaveUri());
			req.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			req.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
			Channel ch = cf.getChannel();
			reqCF.add(ch.write(req));

		}
		for(ChannelFuture cf : reqCF){
			ok = cf.awaitUninterruptibly(1000);
			if (!ok)
				throw new RuntimeException("---!!!!!!!!!!---------");
		}

		return channelHandler.processResult();	
	}
	
}
