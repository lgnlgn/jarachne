package org.jarachne.sentry.core;

import org.jarachne.util.ZKClient;

/**
 * 
 * @author lgn-mop
 *
 */
public abstract class Module {
	public void register(String path, String address) throws Exception{
		ZKClient.get().registerEphemeralNode(path, address);
	}
}
