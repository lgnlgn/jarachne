package org.jarachne.sentry.master.handler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.core.Module;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.master.MasterModule;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * 
 * @author lgn-mop
 *
 */
public class StatusHandler extends RequestHandler{

	
	public StatusHandler(MasterModule module) {
		super(module, null);
	}

	public String getPath() {
		// TODO Auto-generated method stub
		return "/status";
	}

	public void handle(NettyHttpRequest request, DefaultHttpResponse resp) {
		JSONObject json = new JSONObject();
		JSONArray ja = new JSONArray();
		ja.addAll(((MasterModule)module).yieldSlaves());
		json.put("live_slaves", ja);
		HttpResponseUtil.setResponse(resp, "live nodes", json.toString());
	}

}
