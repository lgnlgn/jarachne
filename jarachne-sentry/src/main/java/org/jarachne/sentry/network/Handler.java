/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jarachne.sentry.network;

import org.jboss.netty.handler.codec.http.DefaultHttpResponse;

/**

 */
public interface Handler {

	 public String getPath();
	 
	 public void handle(NettyHttpRequest req,DefaultHttpResponse resp);
	 
}
