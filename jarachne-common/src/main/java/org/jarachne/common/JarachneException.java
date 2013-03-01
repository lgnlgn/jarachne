package org.jarachne.common;

/**
 * @author liangguoning
 * @version 1.0
 * 
 * 
 *          知道明确的错误原因的异常
 */
public class JarachneException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public int errorCode=502;
	public JarachneException(String message) {
		super(message);
	}

	public JarachneException(String message, Throwable cause) {
		super(message, cause);
	}
	public JarachneException(String message,int errcode) {
		super(message);
		this.errorCode=errcode;
	}

	public JarachneException(String message, Throwable cause,int errcode) {
		super(message, cause);
	    this.errorCode=errcode;
	}
	
}
