package org.jarachne.sentry.master.handler;

import java.io.IOException;
import java.util.Collection;

import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.core.Module;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.master.MasterModule;
import org.jarachne.util.logging.ESLogger;
import org.jarachne.util.logging.Loggers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class FileSendHandler extends RequestHandler{


	static ESLogger log = Loggers.getLogger(DataCollectHandler.class);
	
	static class FileSendChannelHandler extends AbstractDistributedChannelHandler{

		@Override
		public String requestSlaveUri() {
			// TODO Auto-generated method stub
			return "/file";
		}

		@Override
		public AbstractDistributedChannelHandler clone(
				Collection<String> currentSlaves) {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public FileSendHandler(Module module) {
		super(module, new FileSendChannelHandler());
	}
	
	public String getPath() {
		return "/file";
	}

	public void handle(NettyHttpRequest req, DefaultHttpResponse resp) {
		String file = req.param("filename");
		if (file == null){
			HttpResponseUtil.setHttpResponseWithMessage(resp, HttpResponseStatus.BAD_REQUEST, "'filepath' not specified!");
		}else{
			String filePath = ((MasterModule)module).getDataDir() + "/" + file;
			try {
				((MasterModule)module).sendFileToSlaves(channel, filePath);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				HttpResponseUtil.setHttpResponseWithMessage(resp, HttpResponseStatus.INTERNAL_SERVER_ERROR, "file send job failed!");
				e.printStackTrace();
			}
		}
		
	}
	
	
}
