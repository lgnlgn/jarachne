package org.jarachne.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.jarachne.common.Config;
import org.jarachne.common.Constants;

public class ZKServer implements Runnable{
	Properties startupProperties ;
	public ZKServer(String path) throws IOException{
		startupProperties = new Properties();
		InputStream in = new FileInputStream(path);
		startupProperties.load(in);
		in.close();
	}
	
	public ZKServer(){
		startupProperties = new Properties();
		startupProperties.put("tickTime", Config.get().get("zk.tickTime", "2000"));
		startupProperties.put("initLimit", Config.get().get("zk.initLimit", "10"));
		startupProperties.put("syncLimit", Config.get().get("zk.syncLimit", "5"));
		startupProperties.put("dataDir", Config.get().get("zk.dataDir", Constants.DATA_PATH));
		startupProperties.put("clientPort", Config.get().get("zk.clientPort", "2181"));
	}
	
	public void run() {
		QuorumPeerConfig quorumConfiguration = new QuorumPeerConfig();
		try {
			quorumConfiguration.parseProperties(startupProperties);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		final ZooKeeperServerMain zooKeeperServer = new ZooKeeperServerMain();
		final ServerConfig configuration = new ServerConfig();
		configuration.readFrom(quorumConfiguration);
		try {
			zooKeeperServer.runFromConfig(configuration);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
