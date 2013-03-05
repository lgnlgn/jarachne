package org.jarachne.sentry.handler;

/**
 * two kinds of handler: 
 * 1. channel handler for asynchronize distributed requests
 * 2. handle methods for #messageRecieced
 * @author lgn-mop
 *
 */
public interface RequestHandler {
	public String getPath();
}
