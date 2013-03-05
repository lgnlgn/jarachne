package org.jarachne.sentry.handler;

public abstract class ToSlaveRequestHandler implements RequestHandler{
	String uri;
	AbstractDistributedChannelHandler channelHandler;
	
	
	
	public String getUri() {
		return uri;
	}



	public void setUri(String uri) {
		this.uri = uri;
	}



	public AbstractDistributedChannelHandler getChannelHandler() {
		return channelHandler;
	}



	public void setChannel(AbstractDistributedChannelHandler channel) {
		this.channelHandler = channel;
	}



	abstract public String getPath() ;
}
