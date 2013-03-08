package org.jarachne.sentry.master.handler;

import java.io.File;
import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.core.Module;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.master.MasterModule;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;

public class DataDisplayHandler extends RequestHandler{

	public DataDisplayHandler(Module module) {
		super(module, null);
	}

	public String getPath() {
		// TODO Auto-generated method stub
		return "/local/data";
	}

	public void handle(NettyHttpRequest request, DefaultHttpResponse resp) {

		File[] files = new File(((MasterModule)module).getDataDir()).listFiles();
		ArrayList<String> dataNames = new ArrayList<String>();
		for(File f : files){
			//TODO strictly check! 
			if (f.isDirectory()){
				dataNames.add(f.getName());
			}
		}
		JSONObject jo = new JSONObject();
		JSONArray ja = new JSONArray();
		ja.addAll(dataNames);
		jo.put("dataList", ja);
		HttpResponseUtil.setResponse(resp, "data on master server", jo.toString());
		
	}

}
