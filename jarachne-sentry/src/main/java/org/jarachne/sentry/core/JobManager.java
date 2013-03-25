package org.jarachne.sentry.core;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.jarachne.common.JarachneException;
import org.jarachne.common.Job;

import net.sf.json.JSONObject;

public class JobManager implements Runnable{
	
//	public interface Job extends Runnable{
//		
//		public int autoExpireTime();
//		
//		public String getJobName();
//		
//		public String getJobStatus();
//		
//		
//		/**
//		 * force close
//		 */
//		public void releaseResource();
//		
//		public boolean isRunning();
//	}
	
	private volatile Job running ;
	private volatile String lastJobState;
	
	private Thread jobRunnerThread;
	private Thread managerThread;
	
	public JobManager(){
		this.managerThread = new Thread(this);
		this.managerThread.setDaemon(true);
		this.managerThread.start();
	}

	
	private synchronized void clearJob(){
		jobRunnerThread = null;
		lastJobState = running.getJobStatus();
		running.releaseResource();
		running = null;
	}
	
	private synchronized void startJob(){
		jobRunnerThread = new Thread(running);
		jobRunnerThread.start();
	}
	
	/**
	 * 
	 * @return true if we can submit a new job; else false;
	 */
	public synchronized boolean isJobSlotFree(){
		if (running == null){
			return true;
		}
		if (!running.isRunning()){
			clearJob();
			return true;
		}
		return false;
	}
	
	public String getLastJobState(){
		return this.lastJobState;
	}
	
	/**
	 * 
	 * @param job
	 * @return
	 */
	public synchronized boolean submitJob(final Job job){
		if (isJobSlotFree()){
			this.running = job;
			this.startJob();
			return true;
		}else{
			return false;
		}
	}
	
	public synchronized void killJob(){
		if (this.running == null || isJobSlotFree()){
			;
		}else{
			try {
				jobRunnerThread.join(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally{
				clearJob();
			}
		}
	}
	
	public String toString(){
		//TODO
		return null;
	}
	
	/**
	 * check job running state every 100ms
	 */
	public void run(){
		while(true){
			if (isJobSlotFree()){
				;
			}else{
				
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	public void addMessageToJob(String content) {
		if (running != null){
			if (content.endsWith("\n")){
				running.addMessage(content);
			}else{
				running.addMessage(content + "\n");
			}
		}
	}
}

