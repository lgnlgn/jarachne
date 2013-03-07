package org.jarachne.sentry.slave.handler.local;

import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.handler.LocalRequestHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * correspond to {@link #DataCollectHandler} in master
 * 
 * @author lgn-mop
 *
 */
public class DataReportHandler implements LocalRequestHandler{


	public String getPath() {
		return "/data";
	}

	public void handle(NettyHttpRequest request, DefaultHttpResponse resp) {
		// TODO Auto-generated method stub
		HttpResponseUtil.setHttpResponseWithMessage(resp, HttpResponseStatus.OK, "no data");
		
	}
	
}
