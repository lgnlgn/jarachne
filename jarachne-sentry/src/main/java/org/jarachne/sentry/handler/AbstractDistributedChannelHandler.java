package org.jarachne.sentry.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

/**
 * channel for distributed requests
 * @author lgn-mop
 *
 */
public abstract class AbstractDistributedChannelHandler extends SimpleChannelUpstreamHandler implements RequestHandler{

	Collection<String> slaves;
	
	public AbstractDistributedChannelHandler(){
		this.slaves = new ArrayList<String>();
	}
	
	abstract public String processResult();
	
	abstract public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception;
	
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception{
		e.getChannel().close();
	}
	
	abstract public AbstractDistributedChannelHandler clone(Collection<String> currentSlaves) ;
}
