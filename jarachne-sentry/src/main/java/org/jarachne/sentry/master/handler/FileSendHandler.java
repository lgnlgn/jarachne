package org.jarachne.sentry.master.handler;

import static org.jboss.netty.channel.Channels.pipeline;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.core.Module;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.master.MasterModule;
import org.jarachne.sentry.master.handler.DataCollectHandler.DataCollectChannelHandler;
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
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.stream.ChunkedWriteHandler;

public class FileSendHandler extends RequestHandler{

	static ESLogger log = Loggers.getLogger(DataCollectHandler.class);
	
	static class FileSendChannelHandler extends AbstractDistributedChannelHandler{
		MasterModule module;
		@Override
		public String requestSlaveUri() {
			// TODO Auto-generated method stub
			return "/file";
		}

		@Override
		public AbstractDistributedChannelHandler clone(	Collection<String> currentSlaves) {
			FileSendChannelHandler ch = new FileSendChannelHandler();
			ch.slaves.addAll(currentSlaves);
			return ch;
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
			MasterModule m = (MasterModule)module;
			String filePath = m.getDataDir() + "/" + file;
			try {
				String  resultString = this.sendFileToSlaves(m.getBootstrap(), channel, m.yieldSlaves(), filePath);
				HttpResponseUtil.setResponse(resp, "send " + filePath + " to slaves", resultString);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				HttpResponseUtil.setHttpResponseWithMessage(resp, HttpResponseStatus.INTERNAL_SERVER_ERROR, "file send job failed!");
				e.printStackTrace();
			}
		}
		
	}
	
	private String sendFileToSlaves(ClientBootstrap bootstrap, AbstractDistributedChannelHandler toSlaveChannel, List<String> slaves, String filePath) throws IOException, InterruptedException{
		if (slaves.isEmpty()){
			return "";
		}
		final AbstractDistributedChannelHandler channelHandler = toSlaveChannel.clone( slaves);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory()
		{

			public ChannelPipeline getPipeline() throws Exception
			{
				ChannelPipeline pipeline = pipeline();
				pipeline.addLast("decoder", new HttpResponseDecoder());
				pipeline.addLast("encoder", new HttpRequestEncoder());
				pipeline.addLast("handler", channelHandler);
				pipeline.addLast("chunkedWriter", new ChunkedWriteHandler());
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
		AbstractDistributedChannelHandler.waitChannelFutures(channelFutrueList, 1000);
		ArrayList<ChannelFuture> reqCF = new ArrayList<ChannelFuture>();
		
		
		String[] fileName = filePath.split("/");
		
		RandomAccessFile raf = new RandomAccessFile(filePath, "r");
		byte[] file = new byte[(int)raf.length()];
		raf.read(file);
		raf.close();
		for(ChannelFuture cf : channelFutrueList){
			HttpRequest req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, toSlaveChannel.requestSlaveUri());
			req.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			req.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
//			req.setChunked(true);
			req.setHeader("filename", fileName[fileName.length-1]);
			req.setContent(ChannelBuffers.copiedBuffer(file));
			Channel ch = cf.getChannel();
			reqCF.add(ch.write(req));

		}
		return "action submit";
	}
}
