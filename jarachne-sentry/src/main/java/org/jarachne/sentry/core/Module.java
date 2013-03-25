package org.jarachne.sentry.core;

import org.jarachne.util.ZKClient;

/**
 * 
 * @author lgn-mop
 *
 */
public abstract class Module {
	
	private String address;
	
	public void register(String path, String address) throws Exception{
		ZKClient.get().registerEphemeralNode(path, address);
		this.address = address;
	}
	
	public String getModuleAddress(){
		return address;
	}
}
