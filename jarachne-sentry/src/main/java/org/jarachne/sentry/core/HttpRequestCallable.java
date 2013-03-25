package org.jarachne.sentry.core;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;

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

}
