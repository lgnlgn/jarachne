package org.jarachne.sentry.master;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.zookeeper.KeeperException;
import org.jarachne.common.Constants;
import org.jarachne.sentry.core.JobManager;
import org.jarachne.sentry.core.Module;
import org.jarachne.util.ZKClient;
import org.jarachne.util.ZKClient.ChildrenWatcher;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class MasterModule extends Module{
	
	private JobManager masterJobManager;
	private volatile Map<String, String> slaves;
	private String dataDir;
	public ClientBootstrap bootstrap;
	public HttpClient httpClient;
	private ChildrenWatcher cw;
	
	public MasterModule() throws KeeperException, InterruptedException{
		slaves = new ConcurrentHashMap<String, String>();
		dataDir = Constants.DATA_PATH;
		bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		httpClient = new DefaultHttpClient();
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
			}
		};
		ZKClient.get().watchChildren(Constants.ZK_SLAVE_PATH, cw);
	}
	
	public void addMessageToJob(String content){
		this.masterJobManager.addMessageToJob(content);
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
