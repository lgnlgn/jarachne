package org.jarachne.sentry.handler.slave;


import org.jarachne.network.http.Handler;
import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class HeartbeatHandler implements Handler{

	public String getPath() {
		return "ping";
	}

	public DefaultHttpResponse handle(MessageEvent me) {
		return new DefaultHttpResponse(((HttpRequest)me.getMessage()).getProtocolVersion(), HttpResponseStatus.OK);
	}



}
