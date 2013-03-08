package org.jarachne.sentry.master.handler.distributed;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class DataCollectChannelHandler extends AbstractDistributedChannelHandler{
	
	public DataCollectChannelHandler() {
		this.slaves = new ArrayList<String>();
		this.collectedResults = new ConcurrentHashMap<String, String>();
		
	}

	@Override
	public AbstractDistributedChannelHandler clone(Collection<String> currentSlaves) {
		DataCollectChannelHandler ch = new DataCollectChannelHandler();
		ch.slaves.addAll(currentSlaves);
		return ch;
	}

	public String getPath() {
		// TODO Auto-generated method stub
		return "/data";
	}

	@Override
	public String requestSlaveUri() {
		// TODO Auto-generated method stub
		return "/data";
	}

}
