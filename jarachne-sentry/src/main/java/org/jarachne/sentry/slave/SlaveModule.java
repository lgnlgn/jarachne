package org.jarachne.sentry.slave;

import org.apache.zookeeper.KeeperException;
import org.jarachne.common.Constants;
import org.jarachne.util.ZKClient;

public class SlaveModule {
	public SlaveModule() throws KeeperException, InterruptedException{
		ZKClient.get().createIfNotExist(Constants.ZK_SLAVE_PATH);
	}
	
	public void register(String address) throws KeeperException, InterruptedException{
		ZKClient.get().registerEphemeralNode(Constants.ZK_SLAVE_PATH, address);
	}
}
