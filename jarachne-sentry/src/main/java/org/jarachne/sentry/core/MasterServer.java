package org.jarachne.sentry.core;





import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.zookeeper.KeeperException;
import org.jarachne.common.Constants;
import org.jarachne.network.http.BaseNioServer;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.master.MasterModule;
import org.jarachne.sentry.master.handler.MasterChannelHandler;
import org.jarachne.sentry.master.handler.distributed.DataCollectHandler;
import org.jarachne.sentry.master.handler.local.DataDisplayHandler;
import org.jarachne.util.ZKClient;
import org.jarachne.util.logging.Loggers;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;


public class MasterServer extends BaseNioServer{


	Map<String, RequestHandler> handlers = new ConcurrentHashMap<String, RequestHandler>();
	final MasterChannelHandler channel ;
	
	public String serverName() {
		// TODO Auto-generated method stub
		return "jarachne.master";
	}

	@Override
	protected ChannelUpstreamHandler finalChannelUpstreamHandler() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public MasterServer(MasterModule module) throws KeeperException, InterruptedException{
		super();
		channel = new MasterChannelHandler(module, handlers);
		log = Loggers.getLogger(MasterServer.class);
	}
	
	protected int defaultPort()
	{
		return 24111;
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
	
	public void addReqHandler(RequestHandler... handlers){
		this.channel.addRequestHandlers(handlers);
	}
	
	public static void main(String[] args) throws KeeperException, InterruptedException {
		MasterModule module = new MasterModule();
		MasterServer mserver = new MasterServer(module);
		ZKClient.get().setData(Constants.ZK_MASTER_PATH, mserver.getServerAddress().getBytes());
		mserver.addReqHandler(new DataDisplayHandler());
		mserver.addReqHandler(new DataCollectHandler());
		mserver.start();
	}
}
