package org.jarachne.sentry.master;

import static org.jboss.netty.channel.Channels.pipeline;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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
import org.jarachne.common.Config;
import org.jarachne.common.Constants;
import org.jarachne.common.HttpRequestCallable;
import org.jarachne.sentry.core.Module;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.util.ZKClient;
import org.jarachne.util.ZKClient.ChildrenWatcher;
import org.jarachne.util.concurrent.ConcurrentExecutor;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
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
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

public class MasterModule extends Module{
	private volatile Map<String, String> slaves;
	private String dataDir;
	public ClientBootstrap bootstrap;
//	private HttpClient httpClient;
	private ChildrenWatcher cw;
	
	public MasterModule() throws KeeperException, InterruptedException{
		slaves = new ConcurrentHashMap<String, String>();
		dataDir = Config.get().get("dataDir", "./data");
		bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
//		httpClient = new DefaultHttpClient();
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
	
	public String getDataDir(){
		return this.dataDir;
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
//				System.out.println(slaves);
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
		return this.requestSlaves(tsReq, 2000);
//		List<String> slaves = yieldSlaves();
//		List<Callable<String >> distributedReqs = new ArrayList<Callable<String >> ();
//		for(String slaveAddr: slaves){
//			distributedReqs.add(new HttpRequestCallable(httpClient, "http://" + slaveAddr + tsReq.requestSlaveUri()));
//		}
//		List<String> results = ConcurrentExecutor.execute(distributedReqs);
//		return results.toString();
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
	
	
	public String sendFileToSlaves(AbstractDistributedChannelHandler tsReq, String filePath) throws InterruptedException, IOException{
		if (slaves.isEmpty()){
			return "";
		}
		List<String> slaves = yieldSlaves();
		final AbstractDistributedChannelHandler channelHandler = tsReq.clone( slaves);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
		{

			public ChannelPipeline getPipeline() throws Exception
			{
				ChannelPipeline pipeline = pipeline();
				pipeline.addLast("decoder", new HttpResponseDecoder());
				pipeline.addLast("encoder", new HttpRequestEncoder());
				pipeline.addLast("handler", channelHandler);
				pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
				return pipeline;
			}

		});
		ArrayList<ChannelFuture> channelFutrueList = new ArrayList<ChannelFuture>(slaves.size());
		for(String slaveAddress : slaves){
			int idx = slaveAddress.indexOf(':');
			InetSocketAddress  isa = new InetSocketAddress(slaveAddress.substring(0, idx), new Integer(slaveAddress.substring(idx + 1)));
			ChannelFuture future = bootstrap.connect(isa);
			channelFutrueList.add(future);
			
		}
		waitFutures(channelFutrueList, 1000);
		ArrayList<ChannelFuture> reqCF = new ArrayList<ChannelFuture>();
		
		
		String[] fileName = filePath.split("/");
		FileChannel fc = new FileInputStream(filePath).getChannel();
		
		RandomAccessFile raf = new RandomAccessFile(filePath, "r");
		byte[] file = new byte[(int)raf.length()];
		raf.read(file);
		raf.close();
		for(ChannelFuture cf : channelFutrueList){
			HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, tsReq.requestSlaveUri());
			req.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			req.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
			req.setHeader("filename", fileName[fileName.length-1]);
			req.setContent(ChannelBuffers.copiedBuffer(file));
			Channel ch = cf.getChannel();
			reqCF.add(ch.write(req));

		}
		return "111";
	}
	
	public String requestSlaves(AbstractDistributedChannelHandler tsReq, long soTimeOut) throws InterruptedException{
		if (slaves.isEmpty()){
			return "";
		}
		List<String> slaves = yieldSlaves();
		final AbstractDistributedChannelHandler channelHandler = tsReq.clone(slaves);
		
		
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
			InetSocketAddress  isa = new InetSocketAddress(slaveAddress.substring(0, idx), new Integer(slaveAddress.substring(idx + 1)));
			ChannelFuture future = bootstrap.connect(isa);
			channelFutrueList.add(future);
			
		}
		waitFutures(channelFutrueList, 1000);

		ArrayList<ChannelFuture> reqCF = new ArrayList<ChannelFuture>();

		for(ChannelFuture cf : channelFutrueList){
			HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, tsReq.requestSlaveUri());
			req.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			req.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
			Channel ch = cf.getChannel();
			reqCF.add(ch.write(req));

		}
		waitFutures(reqCF, 1000);
		return channelHandler.processResult();	
	}

	
}
