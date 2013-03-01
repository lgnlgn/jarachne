package org.jarachne.sentry.core;

import io.netty.handler.codec.http.HttpChunkAggregator;



import org.jarachne.network.http.BaseChannelHandler;
import org.jarachne.network.http.BaseNioServer;
import org.jarachne.network.http.Handler;
import org.jarachne.network.http.Handlers;
import org.jarachne.sentry.handler.master.AdminRequestHandler;
import org.jarachne.sentry.handler.slave.FileSaveRequestHandler;

import org.jarachne.util.logging.Loggers;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;


public class MasterServer extends BaseNioServer{

	Handlers handlers = new Handlers();
	final BaseChannelHandler channel = new BaseChannelHandler( handlers);
	
	public String serverName() {
		// TODO Auto-generated method stub
		return "jarachne.master";
	}

	@Override
	protected ChannelUpstreamHandler finalChannelUpstreamHandler() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public MasterServer(){
		super();
		log = Loggers.getLogger(MasterServer.class);
	}
	
	protected int defaultPort()
	{
		return 12111;
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
	
	public void addReqHandler(Handler... handlers){
		for(Handler h : handlers){
			this.handlers.addRequestHandler(h);
		}
	}
	
	public static void main(String[] args) {
		MasterServer mserver = new MasterServer();
		mserver.addReqHandler(new FileSaveRequestHandler());
		mserver.addReqHandler(new AdminRequestHandler());
		mserver.start();
	}
}
