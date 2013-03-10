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
import org.jarachne.common.JobManager;
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
	
	private JobManager masterJobManager;
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
		this.masterJobManager = new JobManager();
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
	
//	public String requestSlaves(AbstractDistributedChannelHandler tsReq) throws InterruptedException, ExecutionException{
//		return this.requestSlaves(tsReq, 2000);
//		List<String> slaves = yieldSlaves();
//		List<Callable<String >> distributedReqs = new ArrayList<Callable<String >> ();
//		for(String slaveAddr: slaves){
//			distributedReqs.add(new HttpRequestCallable(httpClient, "http://" + slaveAddr + tsReq.requestSlaveUri()));
//		}
//		List<String> results = ConcurrentExecutor.execute(distributedReqs);
//		return results.toString();
//	}
	

	
	public ClientBootstrap getBootstrap(){
		return this.bootstrap;
	}
	

	
}
