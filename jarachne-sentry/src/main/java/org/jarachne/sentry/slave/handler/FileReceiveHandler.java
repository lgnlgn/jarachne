package org.jarachne.sentry.slave.handler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jarachne.common.Constants;
import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.core.Module;
import org.jarachne.sentry.core.SentryConstants;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.slave.SlaveModule;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

/**
 * 
 * @author lgn
 *
 */
public class FileReceiveHandler extends RequestHandler{
	
	public final static String SUCCESS_STRING = "SUCCESS!";
	public final static String EXCEPTION_STRING = "EXCEPTION!";
	
	public FileReceiveHandler(SlaveModule module) {
		super(module, null);
	}

	public String getPath() {
		return SentryConstants.Paths.SLAVE_RECIEVE_FILE_PATH;
	}

	public void handle(NettyHttpRequest req, DefaultHttpResponse resp) {
		String filename = req.header(SentryConstants.HeaderKeys.FILENAME);
		if (filename == null){
			resp.setContent(ChannelBuffers.copiedBuffer(EXCEPTION_STRING.getBytes()));
			return ;
		}
		int didx = filename.lastIndexOf("/");
		if (didx > 0){
			String dir = ((SlaveModule)module).getDataDir() + "/" + filename.substring(0, didx);
			new File( dir ).mkdirs();
			System.out.println(dir);
		}
		byte[] file = req.getRequest().getContent().array();
		try{
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(((SlaveModule)module).getDataDir() + "/" + filename));
			bos.write(file);
			bos.close();
			HttpResponseUtil.setResponse(resp, "FileReceiveHandler", SUCCESS_STRING);
		}catch (IOException e) {
			HttpResponseUtil.setResponse(resp, "FileReceiveHandler", EXCEPTION_STRING, HttpResponseStatus.INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		
	}

}
