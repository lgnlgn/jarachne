package org.jarachne.sentry.master;





import org.apache.zookeeper.KeeperException;
import org.jarachne.common.Constants;
import org.jarachne.network.http.BaseChannelHandler;
import org.jarachne.network.http.BaseNioServer;
import org.jarachne.network.http.Handler;
import org.jarachne.network.http.Handlers;
import org.jarachne.sentry.master.handler.DataAllocateHandler;
import org.jarachne.sentry.master.handler.DataCollectHandler;
import org.jarachne.sentry.master.handler.DataDisplayHandler;
import org.jarachne.sentry.master.handler.FileDistributeHandler;
import org.jarachne.sentry.master.handler.JobHandler;

import org.jarachne.sentry.master.handler.StatusHandler;
import org.jarachne.util.logging.Loggers;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;


public class MasterServer extends BaseNioServer{

	Handlers handlers = new Handlers();
	final BaseChannelHandler channel = new MasterChannelHandler(handlers);
	
	public String serverName() {
		// TODO Auto-generated method stub
		return "jarachne.master";
	}

	@Override
	protected ChannelUpstreamHandler finalChannelUpstreamHandler() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public MasterServer() throws KeeperException, InterruptedException{
		super();
		log = Loggers.getLogger(MasterServer.class);
	}
	
	protected int defaultPort()
	{
		return 24111;
	}
	
	public void addHandler(Handler... hander){
		this.handlers.addHandler(hander);
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
	
	
	public static void main(String[] args) throws Exception {
		MasterModule module = new MasterModule();
		MasterServer mserver = new MasterServer();
		mserver.addHandler(new DataDisplayHandler(module));
		mserver.addHandler(new DataCollectHandler(module));
		mserver.addHandler(new StatusHandler(module));
		mserver.addHandler(new FileDistributeHandler(module));
		mserver.addHandler(new DataAllocateHandler(module));
		mserver.addHandler(new JobHandler(module));
		mserver.start();
		module.register(Constants.ZK_MASTER_PATH, mserver.getServerAddress());
	}
}
