package org.jarachne.sentry.slave;

import java.io.File;

import org.apache.zookeeper.KeeperException;
import org.jarachne.common.Config;
import org.jarachne.common.Constants;
import org.jarachne.sentry.core.Module;
import org.jarachne.util.ZKClient;

public class SlaveModule extends Module{
	
	private String dataDir;
	
	public String getDataDir(){
		return dataDir;
	}
	
	public SlaveModule() throws KeeperException, InterruptedException{
		ZKClient.get().createIfNotExist(Constants.ZK_SLAVE_PATH);
		dataDir = Config.get().get("slavedatadir");
		new File(dataDir).mkdir();
	}
	

}
