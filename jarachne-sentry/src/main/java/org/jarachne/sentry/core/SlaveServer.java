package org.jarachne.sentry.core;

import org.apache.zookeeper.KeeperException;
import org.jarachne.common.Constants;
import org.jarachne.network.http.BaseChannelHandler;
import org.jarachne.network.http.BaseNioServer;
import org.jarachne.network.http.Handler;
import org.jarachne.network.http.Handlers;
import org.jarachne.sentry.slave.SlaveModule;
import org.jarachne.sentry.slave.handler.DataReceiveHandler;
import org.jarachne.sentry.slave.handler.DataReportHandler;
import org.jarachne.util.logging.Loggers;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;

public class SlaveServer extends BaseNioServer{
	
	Handlers handlers = new Handlers();
	final BaseChannelHandler channel = new BaseChannelHandler(handlers);
	
	public SlaveServer(SlaveModule module)throws KeeperException, InterruptedException{
//		super();
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
				pipeline.addLast("chunk" , new org.jboss.netty.handler.codec.http.HttpChunkAggregator(88888888));
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
	
	public void addHandler(Handler... handlers){
		this.handlers.addHandler(handlers);
	}
	
	public static void main(String[] args) throws Exception {
		SlaveModule module = new SlaveModule();
		SlaveServer server = new SlaveServer(module);
		
		server.addHandler(new DataReportHandler(module));
		server.addHandler(new DataReceiveHandler(module));
		server.start();
		module.register(Constants.ZK_SLAVE_PATH, server.getServerAddress());
	}
}
