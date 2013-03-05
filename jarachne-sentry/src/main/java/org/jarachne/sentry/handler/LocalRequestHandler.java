package org.jarachne.sentry.handler;

import org.jarachne.network.http.NettyHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;

/**
 * handler #HttpRequest
 * @author lgn-mop
 *
 */
public interface LocalRequestHandler extends RequestHandler{
	
	public void handle(NettyHttpRequest request, DefaultHttpResponse resp);
}
