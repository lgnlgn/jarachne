/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jarachne.network.http;

import java.util.Map;

import org.jarachne.util.Strings;

/**
 * 
 * @author lgn
 *
 */
public class Handlers {

	Map<String, Handler> reqHandlers =  new java.util.concurrent.ConcurrentHashMap<String, Handler>();
	Map<String, Handler> respHandlers = new java.util.concurrent.ConcurrentHashMap<String, Handler>();
	
	
	public void addRequestHandler(Handler... h) {
		for (Handler a : h) {
			reqHandlers.put(a.getPath(), a);
		}
	}

	public void addResponseHandler(Handler... h) {
		for (Handler a : h) {
			respHandlers.put(a.getPath(), a);
		}
	}
	
	public Handler getRequestHandler(String path) {
		return  reqHandlers.get(path);
	}
	
	public Handler getResponseHandler(String path) {
		return  respHandlers.get(path);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder("{");
		String[] handlerNames = new String[this.reqHandlers.size()];
		int i = 0;
		for(Handler handler : reqHandlers.values()){
			handlerNames[i++] = "\"" + handler.getClass().getName() + "\"";
		}
		Strings.arrayToAppend(sb, "request", handlerNames);
		handlerNames = new String[this.respHandlers.size()];
		i = 0;
		for(Handler handler : respHandlers.values()){
			handlerNames[i++] = "\"" + handler.getClass().getName() + "\"";
		}
		Strings.arrayToAppend(sb, "response", handlerNames);
		sb.append("}");
		return sb.toString();
	}
}
