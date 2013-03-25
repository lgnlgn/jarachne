package org.jarachne.sentry.master;

import net.sf.json.JSONObject;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.jarachne.common.Constants;

import org.jarachne.sentry.job.AbstractJob;
import org.jarachne.sentry.master.handler.DataAllocateHandler;
import org.jarachne.sentry.master.handler.FileDistributeHandler;


public class DataDistributeJob extends AbstractJob{

	MasterModule module;
	String dataName;
	public DataDistributeJob(MasterModule module, String dataName){
		super();
		this.module = module;
		this.dataName = dataName;
	}
	
	public int autoExpireTime() {
		return -1;
	}

	public String getJobName() {
		return this.getClass().getSimpleName();
	}

	public void releaseResource() {
		
	}

	public boolean isRunning() {
		return false;
	}

	public void run() {
		HttpClient client = module.httpClient;
		HttpGet distributeFileMessage = new HttpGet(	String.format("http://%s/%s?%s=%s", 
				module.getModuleAddress() ,
				FileDistributeHandler.PATH ,
				FileDistributeHandler.KEY_FILENAME,
				dataName + Constants.DATA_SUFFIXES[0]
				));
		
		HttpGet allocateDataMessage = new HttpGet(String.format("http://%s/%s?%s=%s",
				module.getModuleAddress() , 
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
		
	}

	@Override
	protected JSONObject generateStatus() {
		// TODO Auto-generated method stub
		return null;
	}



}
