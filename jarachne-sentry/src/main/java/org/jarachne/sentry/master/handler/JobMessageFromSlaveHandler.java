package org.jarachne.sentry.master.handler;

import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.core.Module;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.sentry.handler.RequestHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;

public class JobMessageFromSlaveHandler extends RequestHandler{
	
	public JobMessageFromSlaveHandler(Module module) {
		super(module, null);
		
	}

	public String getPath() {
		// TODO Auto-generated method stub
		return "/slavereport";
	}

	public void handle(NettyHttpRequest req, DefaultHttpResponse resp) {
		// TODO Auto-generated method stub
		
	}

}
