package org.jarachne.sentry.job;

import net.sf.json.JSONObject;

import org.jarachne.sentry.core.JobManager;

public abstract class AbstractJob implements JobManager.Job{
	long createTime ;
	StringBuilder recorder;
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
}
