package org.jarachne.sentry.slave.handler;

import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;

import org.jarachne.sentry.core.Module;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.slave.SlaveModule;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * correspond to {@link #DataCollectHandler} in master
 * 
 * @author lgn-mop
 *
 */
public class DataReportHandler extends RequestHandler{


	public DataReportHandler(SlaveModule module) {
		super(module, null);
		// TODO Auto-generated constructor stub
	}

	public String getPath() {
		return "/data";
	}

	public void handle(NettyHttpRequest request, DefaultHttpResponse resp) {
		// TODO Auto-generated method stub
		HttpResponseUtil.setHttpResponseWithMessage(resp, HttpResponseStatus.OK, "testing--- no data");
		
	}
	
}
