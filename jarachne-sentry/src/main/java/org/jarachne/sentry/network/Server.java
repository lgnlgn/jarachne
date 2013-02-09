package org.jarachne.sentry.network;


public interface Server {
	public void init();

	public void start();

	public void stop();

	public String serverName();
}
