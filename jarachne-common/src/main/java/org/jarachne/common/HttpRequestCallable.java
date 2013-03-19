package org.jarachne.common;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;

/**
 * 
 * @author lgn-mop
 *
 */
@Deprecated
public class HttpRequestCallable implements Callable<String>{
	final static int TIMEOUT = 2000;
	private HttpClient httpClient;
	private String url ;
	private BasicResponseHandler responseHandler = new BasicResponseHandler();
	public HttpRequestCallable(HttpClient client, String url){
		this.httpClient = client;
		this.url = url;
	}

	
	public String call() throws Exception {
		try{
			HttpGet get = new HttpGet(url);
			String result = httpClient.execute(get, responseHandler);
			String[] urlparts = url.split("/");
			return urlparts[2] + "=>" +result;
		}catch(IOException e){
			return null;
		}
		
	}

}
