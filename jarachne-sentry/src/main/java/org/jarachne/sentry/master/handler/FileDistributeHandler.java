package org.jarachne.sentry.master.handler;

import static org.jboss.netty.channel.Channels.pipeline;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import net.sf.json.JSONObject;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.jarachne.common.Constants;
import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.core.HttpRequestCallable;
import org.jarachne.sentry.core.Module;
import org.jarachne.sentry.core.SentryConstants;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.master.MasterModule;
import org.jarachne.util.concurrent.ConcurrentExecutor;
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

/**
 * synchronized file distribute handler, only allow small files
 * URL : http://HOST:PORT/file?filename=
 * 
 * @author lgn
 *
 */
public class FileDistributeHandler extends RequestHandler{
	
	public final static String PATH = "/file";
	public final static String KEY_FILENAME = "filename";
	private String toSlavePath = SentryConstants.Paths.SLAVE_RECIEVE_FILE_PATH;
	
	static ESLogger log = Loggers.getLogger(DataCollectHandler.class);
	
	static class FileSendChannelHandler extends AbstractDistributedChannelHandler{
		MasterModule module;
		@Override
		public String requestSlaveUri() {
			return SentryConstants.Paths.SLAVE_RECIEVE_FILE_PATH;
		}

		@Override
		public AbstractDistributedChannelHandler clone(	Collection<String> currentSlaves) {
			FileSendChannelHandler ch = new FileSendChannelHandler();
			for(String slaveAddress: currentSlaves){
				ch.callbacks.put(slaveAddress, "waiting");
			}
			return ch;
		}
		
	}
	
	public FileDistributeHandler(Module module) {
		super(module, new FileSendChannelHandler());
	}
	
	public String getPath() {
		return PATH;
	}

	public void handle(NettyHttpRequest req, DefaultHttpResponse resp) {
		String file = req.param( KEY_FILENAME);
		if (file == null){
			HttpResponseUtil.setResponse(resp, "FileDistributeHandler", "\"'filepath' not specified!\"", HttpResponseStatus.BAD_REQUEST );
		}else{
			MasterModule m = (MasterModule)module;
			String filePath =  file;
			try {
//				String  resultString = this.sendFileToSlaves(m.getBootstrap(), channel, m.yieldSlaves(), filePath);
				String resultString = this.requestSlaves(m.httpClient, m.yieldSlaves(), filePath, 20000);
				HttpResponseUtil.setResponse(resp, "send " + filePath + " to slaves", resultString);
			} catch (Exception e) {
				HttpResponseUtil.setResponse(resp, "FileDistributeHandler", "\"file send job failed!\"", HttpResponseStatus.INTERNAL_SERVER_ERROR );
				e.printStackTrace();
			}
		}
		
	}
	
	
	private String requestSlaves(HttpClient client, List<String> slaves, String filePath, long timeOut) throws InterruptedException, ExecutionException, TimeoutException, IOException{
		List<Callable<String >> distributedReqs = new ArrayList<Callable<String >> ();
		RandomAccessFile raf = new RandomAccessFile( Constants.DATA_PATH + "/"  + filePath, "r");
		byte[] file = new byte[(int)raf.length()];
		raf.read(file);
		raf.close();
		for(String slaveAddr: slaves){
//			HttpUriRequest req = new HttpGet("http://" + slaveAddr + toSlavePath);
			HttpPost post = new HttpPost("http://" + slaveAddr + toSlavePath);
			post.setEntity(new ByteArrayEntity(file));
			post.addHeader(KEY_FILENAME, filePath);
			distributedReqs.add(new HttpRequestCallable(client, post));
		}
		List<String> results = ConcurrentExecutor.execute(distributedReqs, timeOut);
		return HttpRequestCallable.summarizeResult(distributedReqs, results);
	}
	
	
	@Deprecated
	private String sendFileToSlaves(ClientBootstrap bootstrap, AbstractDistributedChannelHandler toSlaveChannel, List<String> slaves, String filePath) throws IOException, InterruptedException{
		if (slaves.isEmpty()){
			return "no slaves";
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
			req.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
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
