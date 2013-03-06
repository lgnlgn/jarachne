package org.jarachne.sentry.handler;

/**
 * two kinds of handler: 
 * 1. channel handler for asynchronize distributed requests
 * 2. handle methods for #messageRecieced
 * @author lgn-mop
 *
 */
public interface RequestHandler {
	
	/**
	 * <p>path for processing local data</p>
	 * <p>path for distributed requests URI</p> 
	 * so we MUST have <b>path of distributed_requester</b> = <b>path of task executor</b>
	 * <p></p>
	 * @return
	 */
	public String getPath();
}
