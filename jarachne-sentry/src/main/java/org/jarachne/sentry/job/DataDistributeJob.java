package org.jarachne.sentry.job;

import net.sf.json.JSONObject;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.jarachne.common.Constants;

import org.jarachne.sentry.master.handler.DataAllocateHandler;
import org.jarachne.sentry.master.handler.FileDistributeHandler;

/**
 * consist of 2 task: 1. properties distribution  2. blocks allocation
 * run job by httpget 
 * @author lgn
 *
 */
public class DataDistributeJob extends AbstractJob{

//	MasterModule module;
	HttpClient client;
	String masterAddress;
	String dataName;
	public DataDistributeJob(HttpClient client, String masterAddress, String dataName){
		super();
		this.masterAddress = masterAddress;
		this.client = client;
		this.dataName = dataName;
	}
	
	public int autoExpireTime() {
		return -1;
	}

	public String getJobName() {
		return "Distribution of '" + dataName + "'";
	}

	public void releaseResource() {
		this.runningFlag = false;
	}

	public void run() {
		HttpGet distributeFileMessage = new HttpGet(	String.format("http://%s%s?%s=%s", 
				masterAddress ,
				FileDistributeHandler.PATH ,
				FileDistributeHandler.KEY_FILENAME,
				dataName + "/" + dataName + Constants.DATA_SUFFIXES[0]
				));
		
		HttpGet allocateDataMessage = new HttpGet(String.format("http://%s%s?%s=%s",
				masterAddress , 
				DataAllocateHandler.PATH,
				DataAllocateHandler.KEY_DATA,
				dataName));

		try{
			client.execute(distributeFileMessage, new BasicResponseHandler()); //
			this.addMessage( Constants.DATA_SUFFIXES[0] + " file send to slaves\n");
			client.execute(allocateDataMessage, new BasicResponseHandler()); //
		}catch(Exception e){
			this.addMessage( "!!!!!!!!!!!!!!Exception !!!!!!!!!!!!!!");
		}
		this.runningFlag = false;
	}

	protected JSONObject generateStatus() {
		JSONObject jo = new JSONObject();
		jo.put("log", recorder.toString());
		return jo;
	}




}
