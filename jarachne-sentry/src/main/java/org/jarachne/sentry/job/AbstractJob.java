package org.jarachne.sentry.job;

import net.sf.json.JSONObject;

import org.jarachne.common.Job;


public abstract class AbstractJob implements Job{
	protected long createTime ;
	protected StringBuilder recorder;
	protected volatile boolean runningFlag = true;
	public AbstractJob(){
		createTime = System.currentTimeMillis();
		recorder = new StringBuilder();
	}
	
	protected abstract JSONObject generateStatus();
	
	public String getJobStatus(){
		JSONObject json = new JSONObject();
		json.put("jobName", this.getClass().getSimpleName());
		json.put("jobCreate", createTime);
		json.put("jobDuration", System.currentTimeMillis() - createTime);
		json.put("jobStatus", this.generateStatus());
		return json.toString();
	}
	
	public void addMessage(String content){
		recorder.append(content);
	}
	
	public boolean isRunning(){
		return this.runningFlag;
	}
	
}
