package org.jarachne.sentry.handler;

import org.jarachne.network.http.Handler;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.core.Module;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;

/**
 * handler #HttpRequest
 * @author lgn-mop
 *
 */
public abstract class RequestHandler implements Handler{
	
	protected Module module;
	/**
	 *  for distributed request
	 */
	protected AbstractDistributedChannelHandler channel;
	
	public RequestHandler(Module module, AbstractDistributedChannelHandler channel){
		this.module = module;
		this.channel = channel;
	}
}
