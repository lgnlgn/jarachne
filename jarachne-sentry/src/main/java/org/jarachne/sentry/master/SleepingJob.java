package org.jarachne.sentry.master;

import net.sf.json.JSONObject;

import org.jarachne.sentry.job.AbstractJob;

public class SleepingJob extends AbstractJob{

	long t;
	
	public SleepingJob(long sleep){
		this.t = sleep;
	}
	
	
	public int autoExpireTime() {
		return 3000;
	}

	public String getJobName() {
		return this.getClass().getSimpleName();
	}

	public void releaseResource() {
		this.runningFlag = false;
	}

	public void run() {
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.runningFlag = false;
	}

	@Override
	protected JSONObject generateStatus() {
		JSONObject jo = new JSONObject();
		jo.put("sleeping", System.currentTimeMillis() - this.createTime);
		return jo;
	}

}
