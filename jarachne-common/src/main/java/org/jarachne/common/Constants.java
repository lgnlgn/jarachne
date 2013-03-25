package org.jarachne.common;

public class Constants {
	
	public final static String DATA_PATH = Config.get().get("dataDir", "./data");
	
	
	public final static String ZK_SLAVE_PATH = "/jarachne/slaves";
	public final static String ZK_MASTER_PATH = "/jarachne/master";


	public final static String[] BLOCK_SUFFIXES = new String[]{	".data", ".sta", };
	public final static String[] DATA_SUFFIXES = new String[]{".global"};
	
}
