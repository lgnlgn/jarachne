package org.jarachne.sentry.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.json.JSONObject;

import org.jarachne.network.http.NettyHttpRequest;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpMessage;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

/**
 * channel for distributed requests
 * @author lgn-mop
 *
 */
public abstract class AbstractDistributedChannelHandler extends SimpleChannelUpstreamHandler{

	protected Collection<String> slaves;
	protected Map<String, String> collectedResults;

	protected AbstractDistributedChannelHandler(){
		this.slaves = new ArrayList<String>();
		this.collectedResults = new ConcurrentHashMap<String, String>();
	}
	/**
	 * uri path sent request to slaves
	 * <p> need to be the same to the slave.getPath()</p>
	 * @return
	 */
	abstract public String requestSlaveUri();
	
	public String processResult(){
		JSONObject jo = new JSONObject();
		jo.putAll(collectedResults);
		return jo.toString();
	}
	
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)throws Exception {
		HttpMessage message = (HttpMessage)e.getMessage();
		String remoteAddress = e.getRemoteAddress().toString();
		this.collectedResults.put(remoteAddress, new String(message.getContent().array()));
//		e.getFuture().addListener(ChannelFutureListener.CLOSE);

	}
	
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception{
		e.getChannel().close();
	}
	
	abstract public AbstractDistributedChannelHandler clone(Collection<String> currentSlaves) ;
}
