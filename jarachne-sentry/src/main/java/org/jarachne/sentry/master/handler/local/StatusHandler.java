package org.jarachne.sentry.master.handler.local;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.handler.LocalRequestHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * 
 * @author lgn-mop
 *
 */
public class StatusHandler implements LocalRequestHandler{

	
	public String getPath() {
		// TODO Auto-generated method stub
		return "/local/status";
	}

	public void handle(NettyHttpRequest request, DefaultHttpResponse resp) {
		JSONObject json = new JSONObject();
		HttpResponseUtil.setHttpResponseWithMessage(resp, HttpResponseStatus.OK, "hi");
	}

}
