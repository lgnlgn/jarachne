/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jarachne.network.http;

import java.util.Map;

/**

 */
public class Handlers {

	Map<String, Handler> handlers = new java.util.concurrent.ConcurrentHashMap<String, Handler>();

	public void addHandler(Handler... h) {
		for (Handler a : h) {
			handlers.put(a.getPath(), a);
		}
	}

	public Handler getHandler(String path) {

		return  handlers.get(path);
	}
	
	public String toString(){
		return handlers.toString();
	}
}
