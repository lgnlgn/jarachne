package org.jarachne.sentry.master.handler;

import static org.jboss.netty.channel.Channels.pipeline;

import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.master.MasterModule;

import org.jarachne.util.logging.ESLogger;
import org.jarachne.util.logging.Loggers;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

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
			String result = this.requestSlaves(m.getBootstrap(), channel, m.yieldSlaves(), 2000);
			HttpResponseUtil.setResponse(resp, "collect data on slaves", result);
		} catch (InterruptedException e) {
			log.error("requestSlaves exception ", e);
		} 
		
	}

	private String requestSlaves(ClientBootstrap bootstrap, AbstractDistributedChannelHandler toSlaveHandler, List<String> slaves, long timeOut) throws InterruptedException{
		if (slaves.isEmpty()){
			return "";
		}
		final AbstractDistributedChannelHandler channelHandler = toSlaveHandler.clone(slaves);
		
		
		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
		{

			public ChannelPipeline getPipeline() throws Exception
			{
				ChannelPipeline pipeline = pipeline();

				pipeline.addLast("decoder", new HttpResponseDecoder());
//				pipeline.addLast("aggregator", new HttpChunkAggregator(6048576));
				pipeline.addLast("encoder", new HttpRequestEncoder());
				pipeline.addLast("handler", channelHandler);

				return pipeline;
			}

		});
		ArrayList<ChannelFuture> channelFutrueList = new ArrayList<ChannelFuture>(slaves.size());
		for(String slaveAddress : slaves){
			int idx = slaveAddress.indexOf(':');
			InetSocketAddress  isa = new InetSocketAddress(slaveAddress.substring(0, idx), new Integer(slaveAddress.substring(idx + 1)));
			ChannelFuture future = bootstrap.connect(isa);
			channelFutrueList.add(future);
			
		}
		AbstractDistributedChannelHandler.waitChannelFutures(channelFutrueList, timeOut/2);

		ArrayList<ChannelFuture> reqCF = new ArrayList<ChannelFuture>();

		for(ChannelFuture cf : channelFutrueList){
			HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, toSlaveHandler.requestSlaveUri());
			req.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			req.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
			Channel ch = cf.getChannel();
			reqCF.add(ch.write(req));

		}
		AbstractDistributedChannelHandler.waitChannelFutures(reqCF, timeOut/2);
		return channelHandler.processResult();	
	}
	
}
