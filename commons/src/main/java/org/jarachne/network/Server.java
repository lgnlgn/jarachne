package org.jarachne.network;


public interface Server {
	public void init();

	public void start();

	public void stop();

	public String serverName();
}
