package org.jarachne.sentry.http;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jarachne.util.logging.ESLogger;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
import org.jboss.netty.handler.codec.serialization.ObjectEncoder;

public class TServer {
	final ChannelGroup allChannels = new DefaultChannelGroup(
			"nio-server");
	protected ESLogger log;

	protected ServerBootstrap s1;
	protected ServerBootstrap s2;
	protected ChannelFactory channelFactory = null;
	
	protected ChannelPipelineFactory getChannelPipelineFactory(){
		return new ChannelPipelineFactory(){
			
			public ChannelPipeline getPipeline()
				throws Exception{
				
				// Create a default pipeline implementation.
				ChannelPipeline pipeline = Channels.pipeline();

				pipeline.addLast("decoder", new HttpRequestDecoder());

				pipeline.addLast("encoder", new HttpResponseEncoder());

//				pipeline.addLast("channel", new BaseChannelHandler(null, null));
				pipeline.addLast("channel", new ServerHandler());
				return pipeline;
			}
		};
	}
	
	protected ChannelFactory createChannelFactory() {
//		ExecutorService es = Executors.newCachedThreadPool();

		return new NioServerSocketChannelFactory(//es, es
		 Executors.newCachedThreadPool(),
		 Executors.newCachedThreadPool()
		// new MemoryAwareThreadPoolExecutor(4, 0, 100000000)

		);

	}
	
	
	public void start(){
		this.channelFactory = this.createChannelFactory();

		s1 = new ServerBootstrap(channelFactory);
		s2 = new ServerBootstrap(channelFactory);
		s1.setPipelineFactory(getChannelPipelineFactory());
		s2.setPipelineFactory(getChannelPipelineFactory());
		Channel serverChannel1 = s1.bind(new InetSocketAddress("localhost", 8001));
		Channel serverChannel2 = s2.bind(new InetSocketAddress("localhost", 8002));
		allChannels.add(serverChannel1);
		allChannels.add(serverChannel2);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TServer server = new TServer();
		server.start();
	}

}
