package org.jarachne.sentry.slave.handler;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.core.Module;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.slave.SlaveModule;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;

public class DataReceiveHandler extends RequestHandler{
	
	public DataReceiveHandler(SlaveModule module) {
		super(module, null);
		// TODO Auto-generated constructor stub
	}

	public String getPath() {
		// TODO Auto-generated method stub
		return "/file";
	}

	public void handle(NettyHttpRequest req, DefaultHttpResponse resp) {
		String filename = req.header("filename");
		byte[] file = req.getRequest().getContent().array();
		System.out.println("filename");
		try{
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(((SlaveModule)module).getDataDir() + "/" + filename));
			bos.write(file);
			bos.close();
			resp.setContent(ChannelBuffers.copiedBuffer("finish".getBytes()));
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
