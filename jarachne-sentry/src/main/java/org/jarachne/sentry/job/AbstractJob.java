package org.jarachne.sentry.job;

import net.sf.json.JSONObject;

import org.jarachne.common.Job;


public abstract class AbstractJob implements Job{
	long createTime ;
	StringBuilder recorder;
	volatile boolean runningFlag = false;
	public AbstractJob(){
		createTime = System.currentTimeMillis();
		recorder = new StringBuilder();
	}
	
	protected abstract JSONObject generateStatus();
	
	public String getJobStatus(){
		JSONObject json = new JSONObject();
		json.put("timeSpent", System.currentTimeMillis() - createTime);
		json.put("status", this.generateStatus());
		return json.toString();
	}
	
	public void addMessage(String content){
		recorder.append(content);
	}
	
}
