package org.jarachne.common;

public interface Job extends Runnable{
	
	public int autoExpireTime();
	
	public String getJobName();
	
	public String getJobStatus();
	
	public void addMessage(String content);
	/**
	 * force close
	 */
	public void releaseResource();
	
	public boolean isRunning();
}
