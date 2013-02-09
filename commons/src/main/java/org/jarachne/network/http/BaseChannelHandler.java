package org.jarachne.network.http;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.jarachne.util.Strings;
import org.jarachne.util.logging.ESLogger;
import org.jarachne.util.logging.Loggers;
import org.jboss.netty.buffer.ChannelBuffers;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;




public class BaseChannelHandler extends SimpleChannelHandler {
	
	static ESLogger log = Loggers.getLogger(BaseChannelHandler.class);
	protected final static String CONTENT_TYPE = Strings.CONTENT_TYPE;
	String servicePath = "";


	protected Handlers handlers ;

	
	public BaseChannelHandler(String servicePath,  Handlers handlers ) {
		this.servicePath = servicePath;
		this.handlers = handlers;
	}

	protected boolean isLegalService(String serviceFromPath){
		if (this.servicePath.equals(serviceFromPath)){
			return true;
		}else{
			return false;
		}
	}
	
	
	public void messageReceived(ChannelHandlerContext ctx, final MessageEvent me)
			throws Exception {
		
		final HttpRequest request = (HttpRequest) me.getMessage();

		NettyHttpRequest nhr = new NettyHttpRequest(request);
		nhr.setMessageEvent(me);

		DefaultHttpResponse resp = new DefaultHttpResponse(request
				.getProtocolVersion(), HttpResponseStatus.valueOf(200));

		String[] parts = nhr.path().split("/");
		//--------
		if (!isLegalService(parts[1])){
			resp.setStatus(HttpResponseStatus.NOT_FOUND);
			resp.setContent(ChannelBuffers.copiedBuffer("No Service for  : "+parts[1], 
					Charset.defaultCharset()));
			return ;
		}
		
		Handler h = handlers.getHandler( parts[3] );
		
		if(h!=null){
			h.handle(nhr, resp);
		}else{
			resp.setStatus(HttpResponseStatus.NOT_FOUND);
			resp.setContent(ChannelBuffers.copiedBuffer("No handler for :  " + parts[3], 
					Charset.defaultCharset()));
			
		}
		
		
		resp.setHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE);
		resp.setHeader("Content-Length", resp.getContent().readableBytes());

		boolean close = !HttpHeaders.isKeepAlive(request);

		resp.setHeader(HttpHeaders.Names.CONNECTION,
				close ? HttpHeaders.Values.CLOSE
						: HttpHeaders.Values.KEEP_ALIVE);
		
		ChannelFuture cf = me.getChannel().write(resp);

		if (close) 
			cf.addListener(ChannelFutureListener.CLOSE);
		writeAccessLog(me.getChannel(), nhr, resp);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {

//		if (log.isTraceEnabled())
//			log.trace("Connection exceptionCaught:{}", e.getCause().toString());

		e.getChannel().close();
	}
	
	public void addHandlers(Handlers handlers){
		this.handlers = handlers;
	}
	
	/**
	 * override this method for your log content
	 * @param channel
	 * @param nhr
	 * @param resp
	 * @throws UnsupportedEncodingException
	 */
	protected void writeAccessLog( final Channel channel, NettyHttpRequest nhr, 
			DefaultHttpResponse resp) throws UnsupportedEncodingException{
		String ip = channel.getRemoteAddress().toString();
		if( ip.startsWith("/") ) ip = ip.substring(1);
		
		String url = nhr.getRequest().getUri();
		String responeContent = resp.getContent().toString( CharsetUtil.UTF_8 );
		
		log.info( "{} \"{}\" {}", ip, url, responeContent );
	}
	
	public String toString(){
		return "path=" + servicePath + ", handlers=" + handlers.toString();
	}
}
