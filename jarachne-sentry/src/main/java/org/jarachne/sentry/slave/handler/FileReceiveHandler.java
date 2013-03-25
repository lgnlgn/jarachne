package org.jarachne.sentry.slave.handler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jarachne.common.Constants;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.core.Module;
import org.jarachne.sentry.core.SentryConstants;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.slave.SlaveModule;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;

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
		String dataname = req.header(SentryConstants.HeaderKeys.DATANAME);
		if (filename == null){
			resp.setContent(ChannelBuffers.copiedBuffer(EXCEPTION_STRING.getBytes()));
			return ;
		}
		if (dataname != null){
			new File(((SlaveModule)module).getDataDir() + "/" + dataname ).mkdir();
 		}
		byte[] file = req.getRequest().getContent().array();
		try{
			BufferedOutputStream bos = new BufferedOutputStream(
					new FileOutputStream(((SlaveModule)module).getDataDir() + "/" + filename));
			bos.write(file);
			bos.close();
			resp.setContent(ChannelBuffers.copiedBuffer(SUCCESS_STRING.getBytes()));
		}catch (IOException e) {
			resp.setContent(ChannelBuffers.copiedBuffer(EXCEPTION_STRING.getBytes()));
			e.printStackTrace();
		}
		
	}

}
