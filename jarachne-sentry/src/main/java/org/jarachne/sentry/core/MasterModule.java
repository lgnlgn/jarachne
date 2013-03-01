package org.jarachne.sentry.core;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.bootstrap.ServerBootstrap;

public class MasterModule {
	private Map<String, String> slaves = new ConcurrentHashMap<String, String>();
	private volatile boolean isHeartbeating;
	ClientBootstrap client;
	
	public boolean isHeartbeating() {
		return isHeartbeating;
	}

	public void setHeartbeating(boolean isHeartbeating) {
		this.isHeartbeating = isHeartbeating;
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
	
	
}
