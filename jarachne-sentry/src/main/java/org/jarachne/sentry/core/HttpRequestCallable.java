package org.jarachne.sentry.core;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import net.sf.json.JSONObject;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.jarachne.network.http.HttpResponseUtil;

public class HttpRequestCallable  implements Callable<String>{
	private HttpClient httpClient;
	private HttpUriRequest httpMessage;
	private BasicResponseHandler responseHandler = new BasicResponseHandler();
	
	
	public HttpRequestCallable(HttpClient client, HttpUriRequest httpMessage){
		this.httpClient = client;
		this.httpMessage = httpMessage;
	}
	
	public String call() throws Exception {
		try{
			String result = httpClient.execute(httpMessage, responseHandler);
			return result;
		}catch(IOException e){
			return "error!";
		}
	}
	
	public String toString(){
		return httpMessage.getURI().getHost() + ":" + httpMessage.getURI().getPort();
	}
	
	public static String summarizeResult(List<? extends Callable<String>> calls, List<String> results){
		JSONObject jo = new JSONObject();
		for(int i = 0 ; i < calls.size(); i++){
			JSONObject json = JSONObject.fromObject(results.get(i));
			jo.put(calls.get(i), json.get(HttpResponseUtil.RESPONSE).toString());
		}
		return jo.toString();
	}
	
}
