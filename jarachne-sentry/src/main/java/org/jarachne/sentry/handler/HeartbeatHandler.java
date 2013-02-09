package org.jarachne.sentry.handler;


import org.jarachne.network.http.Handler;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.util.HttpResponseUtil;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;

public class HeartbeatHandler implements Handler{

	public String getPath() {
		return "ping";
	}

	public void handle(NettyHttpRequest req, DefaultHttpResponse resp) {
		HttpResponseUtil.setHttpResponseOkReturn(resp, "ok");
		
	}

}
