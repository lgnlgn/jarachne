package org.jarachne.sentry.master.handler.local;

import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.handler.LocalRequestHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;

public class DataDisplayHandler implements LocalRequestHandler{

	public String getPath() {
		// TODO Auto-generated method stub
		return "/local/data";
	}

	public void handle(NettyHttpRequest request, DefaultHttpResponse resp) {
		// TODO Auto-generated method stub
		
	}

}
