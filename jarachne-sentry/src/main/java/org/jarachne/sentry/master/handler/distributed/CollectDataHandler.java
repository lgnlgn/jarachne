package org.jarachne.sentry.master.handler.distributed;

import java.util.ArrayList;
import java.util.Collection;

import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class CollectDataHandler extends AbstractDistributedChannelHandler{

	Collection<String> slaves;
	
	public CollectDataHandler() {
		slaves = new ArrayList<String>();
	}

	@Override
	public String processResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		HttpRequest req = (HttpRequest)e.getMessage();
		DefaultHttpResponse resp = new DefaultHttpResponse(req.getProtocolVersion(), HttpResponseStatus.OK);
		
	}


	@Override
	public AbstractDistributedChannelHandler clone(Collection<String> currentSlaves) {
		CollectDataHandler ch = new CollectDataHandler();
		ch.slaves.addAll(currentSlaves);
		return null;
	}

	public String getPath() {
		// TODO Auto-generated method stub
		return "/data";
	}

}
