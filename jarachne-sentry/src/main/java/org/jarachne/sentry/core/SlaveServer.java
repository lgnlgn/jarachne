package org.jarachne.sentry.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.zookeeper.KeeperException;
import org.jarachne.network.http.BaseNioServer;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.slave.SlaveModule;
import org.jarachne.sentry.slave.handler.SlaveChannelHandler;
import org.jarachne.sentry.slave.handler.local.DataReportHandler;
import org.jarachne.util.logging.Loggers;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

public class SlaveServer extends BaseNioServer{
	Map<String, RequestHandler> handlers = new ConcurrentHashMap<String, RequestHandler>();
	final SlaveChannelHandler channel;
	
	public SlaveServer(SlaveModule module)throws KeeperException, InterruptedException{
		super();
		channel = new SlaveChannelHandler(handlers);
		log = Loggers.getLogger(SlaveServer.class);
	}
	
	
	public String serverName() {
		return "jarachne.slave";
	}

	@Override
	protected ChannelUpstreamHandler finalChannelUpstreamHandler() {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected ChannelPipelineFactory getChannelPipelineFactory(){
		return new ChannelPipelineFactory(){
			
			public ChannelPipeline getPipeline()
				throws Exception{
				
				// Create a default pipeline implementation.
				ChannelPipeline pipeline = Channels.pipeline();


				pipeline.addLast("decoder", new HttpRequestDecoder());
				pipeline.addLast("chunk" , new org.jboss.netty.handler.codec.http.HttpChunkAggregator(8888888));
				pipeline.addLast("encoder", new HttpResponseEncoder());
				pipeline.addLast("channel", channel);

				return pipeline;
			}
		};
	}
	
	protected int defaultPort()
	{
		return 25111;
	}
	
	public void addReqHandler(RequestHandler... handlers){
		this.channel.addRequestHandlers(handlers);
	}
	
	public static void main(String[] args) throws KeeperException, InterruptedException {
		SlaveModule module = new SlaveModule();
		SlaveServer server = new SlaveServer(module);
		module.register(server.getServerAddress());
		server.addReqHandler(new DataReportHandler());
		server.start();
	}
}
