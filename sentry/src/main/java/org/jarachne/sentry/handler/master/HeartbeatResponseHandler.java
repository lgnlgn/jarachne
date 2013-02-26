package org.jarachne.sentry.handler.master;

import java.util.Map;

import org.jarachne.network.http.Handler;
import org.jarachne.sentry.core.MasterModule;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;

/**
 * for callback of distributed requests  
 * only need to remove current slaves from checking list
 * @author lgn
 *
 */
public class HeartbeatResponseHandler implements Handler {

	final Map<String, String> checkingList ;
	
	public HeartbeatResponseHandler(Map<String, String> init){
		this.checkingList = init;
	}
	
	
	public String getPath() {
		// TODO Auto-generated method stub
		return "ping";
	}

	public DefaultHttpResponse handle(MessageEvent me) {
		final Channel ch = me.getChannel();
		String remoteAddress = ch.getRemoteAddress().toString();
		checkingList.remove(remoteAddress);
		return null;
	}

}
