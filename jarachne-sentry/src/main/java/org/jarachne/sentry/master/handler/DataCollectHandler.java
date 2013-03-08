package org.jarachne.sentry.master.handler;

import java.util.Collection;
import java.util.concurrent.ExecutionException;

import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.master.MasterModule;

import org.jarachne.util.logging.ESLogger;
import org.jarachne.util.logging.Loggers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;

public class DataCollectHandler extends RequestHandler{
	static ESLogger log = Loggers.getLogger(DataCollectHandler.class);
	
	static class DataCollectChannelHandler extends AbstractDistributedChannelHandler{
		@Override
		public String requestSlaveUri() {
			return "/data";
		}

		@Override
		public AbstractDistributedChannelHandler clone(
				Collection<String> currentSlaves) {
			DataCollectChannelHandler ch = new DataCollectChannelHandler();
			ch.slaves.addAll(currentSlaves);
			return ch;
		}
	}
	
	
	public DataCollectHandler(MasterModule module) {
		super(module, new DataCollectChannelHandler());
	}

	public String getPath() {
		// TODO Auto-generated method stub
		return "/data";
	}

	public void handle(NettyHttpRequest req, DefaultHttpResponse resp) {
		MasterModule m = ((MasterModule)module);
		
		try {
			String result = m.requestSlaves(this.channel);
			HttpResponseUtil.setResponse(resp, "collect data on slaves", result);
		} catch (InterruptedException e) {
			log.error("requestSlaves exception ", e);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
		
	}

}