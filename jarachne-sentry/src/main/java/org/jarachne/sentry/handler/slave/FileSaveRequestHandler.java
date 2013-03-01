package org.jarachne.sentry.handler.slave;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;


import org.jarachne.network.http.Handler;
import org.jarachne.network.http.NettyHttpRequest;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class FileSaveRequestHandler implements Handler{

	

	public String getPath() {
		// TODO Auto-generated method stub
		return "/file";
	}

	public DefaultHttpResponse handle(MessageEvent me) {
		// TODO Auto-generated method stub
		HttpRequest req = (HttpRequest)me.getMessage();
		DefaultHttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK );
		NettyHttpRequest nhr = new NettyHttpRequest(req);
		String path = nhr.param("path");
		try {
			if (path != null){
			
				path = NettyHttpRequest.decode(path, "UTF-8");
				FileOutputStream fos = new FileOutputStream(new File(path));
				fos.write(req.getContent().array());
				fos.flush();
				fos.close();
			}
			else{
				resp.setContent(ChannelBuffers.copiedBuffer("require 'path'".getBytes()));
				resp.setStatus(HttpResponseStatus.BAD_REQUEST);
				
			}
		} catch (UnsupportedEncodingException e) {
			resp.setContent(ChannelBuffers.copiedBuffer("decode 'path'  error".getBytes()));
			resp.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			resp.setContent(ChannelBuffers.copiedBuffer("open 'path' file error".getBytes()));
			resp.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
		}
		return resp;
	}

}
