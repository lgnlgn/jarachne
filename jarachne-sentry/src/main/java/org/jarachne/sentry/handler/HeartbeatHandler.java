package org.jarachne.sentry.handler;

import org.jarachne.sentry.network.Handler;
import org.jarachne.sentry.network.NettyHttpRequest;
import org.jarachne.sentry.util.HttpResponseUtil;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;

public class HeartbeatHandler implements Handler{

	public String getPath() {
		return "ping";
	}

	public void handle(NettyHttpRequest req, DefaultHttpResponse resp) {
		HttpResponseUtil.setHttpResponseOkReturn(resp, "ok");
		
	}

}
