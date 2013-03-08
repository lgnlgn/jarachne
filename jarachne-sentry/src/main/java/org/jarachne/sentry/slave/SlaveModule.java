package org.jarachne.sentry.slave;

import org.apache.zookeeper.KeeperException;
import org.jarachne.common.Constants;
import org.jarachne.sentry.core.Module;
import org.jarachne.util.ZKClient;

public class SlaveModule extends Module{
	public SlaveModule() throws KeeperException, InterruptedException{
		ZKClient.get().createIfNotExist(Constants.ZK_SLAVE_PATH);
	}
	

}
