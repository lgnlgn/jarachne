package org.jarachne.sentry.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.jarachne.util.ZKClient;

public class ZKtest {

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws KeeperException 
	 */
	public static void main(String[] args) throws KeeperException, InterruptedException {
		// TODO Auto-generated method stub
		ZKClient.get().registerEphemeralNode("/aaaa", "bbb");
		Thread.sleep(2000);
	}

}
