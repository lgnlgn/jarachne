package org.jarachne.network.http;


public interface Server {
	public void init();

	public void start();

	public void stop();

	public String serverName();
}
