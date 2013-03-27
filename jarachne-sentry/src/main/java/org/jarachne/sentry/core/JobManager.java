package org.jarachne.sentry.core;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.jarachne.common.JarachneException;
import org.jarachne.common.Job;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JobManager implements Runnable{
	public final static int REMAIN_STATES = 10;
	private volatile Job running ;
//	private volatile String lastJobState;
	
	private LinkedList<String> lastedJobStates;
	
	private Thread jobRunnerThread;
	private Thread managerThread;
	
	public JobManager(){
		lastedJobStates = new LinkedList<String>();
		this.managerThread = new Thread(this);
		this.managerThread.setDaemon(true);
		this.managerThread.start();
	}

	
	private synchronized void clearJob(){
		jobRunnerThread = null;
//		lastJobState = running.getJobStatus();
		lastedJobStates.addLast(running.getJobStatus());
		if (lastedJobStates.size() > REMAIN_STATES){
			lastedJobStates.pollFirst();
		}
		running.releaseResource();
		running = null;
		System.gc();
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
		}else if (!running.isRunning()){
			clearJob();
			return true;
		}
		return false;
	}
	
	public String getLastJobState(){
		return this.lastedJobStates.peekLast();
	}
	
	public JSONArray getLatestJobStates() {
		JSONArray ja = new JSONArray();
		ja.addAll(this.lastedJobStates);
		return ja;
	}
	
	public String getCurrentJobState(){
		if (!isJobSlotFree()){
			return running.getJobStatus();
		}else{ //free
			return " no job running ";
		}
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

