package org.jarachne.sentry.handler;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.json.JSONObject;

import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpMessage;

/**
 * channel for distributed requests
 * @author lgn-mop
 *
 */
public abstract class AbstractDistributedChannelHandler extends SimpleChannelUpstreamHandler{
	
	
	
	protected Collection<String> slaves;
	protected Map<String, String> callbacks; 

	protected AbstractDistributedChannelHandler(){
		this.slaves = new ArrayList<String>();
		this.callbacks = new ConcurrentHashMap<String, String>();
	}
	/**
	 * uri path sent request to slaves
	 * <p> need to be the same to the slave.getPath()</p>
	 * @return
	 */
	abstract public String requestSlaveUri();
	
	public String processResult(){
		JSONObject jo = new JSONObject();
		jo.putAll(callbacks);
		return jo.toString();
	}
	
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)throws Exception {
		HttpMessage message = (HttpMessage)e.getMessage();
		String remoteAddress = e.getRemoteAddress().toString().substring(1); // trim first '/'
		this.callbacks.put(remoteAddress, new String(message.getContent().array()));

	}
	
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception{
		e.getChannel().close();
	}
	
	/**
	 * create a ChannelHandler with slave addressed for ClientBootstrap's pipeline
	 * @param currentSlaves
	 * @return
	 */
	public abstract AbstractDistributedChannelHandler clone(Collection<String> currentSlaves) ;

	public static boolean waitChannelFutures(List<ChannelFuture> futures, long timeOut) throws InterruptedException{
		int i = 0 ;
		long t = System.currentTimeMillis();
		while(true){
			for(ChannelFuture cf : futures){
				if (cf.isDone()){
					i += 1;
				}
			}
			if (i == futures.size())
				return true;
			else {
				i = 0;
			}
			long t2 = System.currentTimeMillis() -t;
			if (t2 > timeOut)
				return false;
			Thread.sleep(5);
		}
	}
}
