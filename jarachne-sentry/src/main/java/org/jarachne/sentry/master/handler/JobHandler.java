package org.jarachne.sentry.master.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import org.jarachne.network.http.Handler;
import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.core.Module;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.job.AbstractJob;
import org.jarachne.sentry.job.DataDistributeJob;
import org.jarachne.sentry.job.SleepingJob;
import org.jarachne.sentry.master.MasterModule;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * TODO more
 * 
 * @author lgn
 *
 */
public class JobHandler extends RequestHandler{

	public static final String PATH = "/job";
	
	public JobHandler(Module module) {
		super(module, null);
	}

	final static List<String> jobAllow = new ArrayList<String>();
	static {
		jobAllow.add("data");
		jobAllow.add("algorithm");
		jobAllow.add("sleep");
	}
	
	public String getPath() {
		return PATH;
	}

	public void handle(NettyHttpRequest req, DefaultHttpResponse resp) {
		String jobName = req.param("jobname");
		String killJob = req.param("kill");
		
		if (!jobAllow.contains(jobName)){
			HttpResponseUtil.setResponse(resp, "start job :" + jobName +" found by 'jobname'", " Only allowed:" + jobAllow);
			resp.setStatus(HttpResponseStatus.BAD_REQUEST);
			return;
		}
		MasterModule m = (MasterModule)this.module;
		AbstractJob job = null;
		if (jobName.equals("data")){
			String dataName = req.param("data");
			if (dataName == null){
				HttpResponseUtil.setResponse(resp, "start job : data", " require 'data' ");
				resp.setStatus(HttpResponseStatus.BAD_REQUEST);
				return;
			}
			job = new DataDistributeJob(m.httpClient, m.getModuleAddress(), dataName);
		}else if (jobName.equals("sleep")){
			String ms = req.param("ms", "10000");
			job = new SleepingJob(new Integer(ms));
		}
		
		boolean submission = m.submitJob(job);
		if (submission == true){
			HttpResponseUtil.setResponse(resp, "start job : data", "\"job submited\"");
		}else{
			//TODO detailed 
			HttpResponseUtil.setResponse(resp, "start job : data", "\"submision failed\"");
		}
	}
	
}
