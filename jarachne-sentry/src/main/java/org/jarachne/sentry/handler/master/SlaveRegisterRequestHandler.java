package org.jarachne.sentry.handler.master;

import org.jarachne.network.http.Handler;
import org.jarachne.sentry.core.MasterModule;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class SlaveRegisterRequestHandler implements Handler{
	private MasterModule master;

	public SlaveRegisterRequestHandler(MasterModule master){
		this.master = master;
	}
	
	public String getPath() {
		return "register";
	}

	public DefaultHttpResponse handle(MessageEvent me) {
		
		final Channel ch = me.getChannel();
		String remoteAddress = ch.getRemoteAddress().toString();
		master.addSlave(remoteAddress);
		return new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CREATED);
	}
}
