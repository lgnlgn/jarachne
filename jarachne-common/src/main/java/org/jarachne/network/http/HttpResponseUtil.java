package org.jarachne.network.http;

import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class HttpResponseUtil {
	
	final static String basicFormat = "{\"message\":\"%s\"}";
	final static String messageFormat = "{\"message\":\"%s\", \"reason\":\"%s\"}";
	
	public static void setHttpResponseWithStatus(DefaultHttpResponse resp , HttpResponseStatus status){
		resp.setStatus( status );
		resp.setContent(ChannelBuffers.copiedBuffer( String.format(basicFormat, status.toString()), 
				Charset.forName("UTF-8")));
	}
	
	public static void setHttpResponseWithMessage(DefaultHttpResponse resp , HttpResponseStatus status, String content){
		resp.setStatus( status );
		resp.setContent(ChannelBuffers.copiedBuffer( String.format(messageFormat, status.toString(), content), 
				Charset.forName("UTF-8")));
	}
	
	
	/*
	 * Set server error response
	 */
	public static void setServerErrorResp( DefaultHttpResponse resp ){
		setHttpResponseWithStatus(resp, HttpResponseStatus.SERVICE_UNAVAILABLE);
	}
	
	public static void setNotFoundDataResp( DefaultHttpResponse resp ){
		setHttpResponseWithStatus(resp, HttpResponseStatus.NOT_FOUND);
	}
	
	
	/*
	 * Set server error response
	 */
	public static void setParameterErrorResp( DefaultHttpResponse resp ){
		setHttpResponseWithStatus(resp, HttpResponseStatus.BAD_REQUEST);
	}
	
	public static void setMethodNotAllowedResp( DefaultHttpResponse resp ){
		setHttpResponseWithStatus(resp, HttpResponseStatus.METHOD_NOT_ALLOWED);
	}
	
	public static void setHttpResponseOkReturn( DefaultHttpResponse resp, String content){
		resp.setStatus( HttpResponseStatus.OK );
		resp.setContent(ChannelBuffers.copiedBuffer( content, 
				Charset.forName("UTF-8")));
	}
	
	public static void setHttpResponseOkReturn( DefaultHttpResponse resp, byte[] content){
		resp.setStatus( HttpResponseStatus.OK );
		resp.setContent(ChannelBuffers.copiedBuffer( content));
	}
	
	public static void setResponse( DefaultHttpResponse resp, String respHead, String respBody){
		resp.setStatus( HttpResponseStatus.OK );
		StringBuilder builder = new StringBuilder();
		builder.append("{ \"reqest\": \"" + respHead + "\", \"respone\":" ).append(respBody).append("}");
		
		resp.setContent(ChannelBuffers.copiedBuffer( builder.toString().getBytes()));
	}
}
