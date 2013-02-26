/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jarachne.network.http;

import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;

/**

 */
public interface Handler {

	 public String getPath();
	 
	 public DefaultHttpResponse handle(MessageEvent me);
	 
}
