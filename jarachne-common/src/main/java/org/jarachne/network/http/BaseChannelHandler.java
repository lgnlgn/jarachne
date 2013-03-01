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
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;


public class BaseChannelHandler extends SimpleChannelHandler {
	
	static ESLogger log = Loggers.getLogger(BaseChannelHandler.class);
	protected final static String CONTENT_TYPE = Strings.CONTENT_TYPE;


	protected Handlers handlers ;

	
	public BaseChannelHandler( Handlers handlers ) {

		this.handlers = handlers;
	}

	
	
	public void messageReceived(ChannelHandlerContext ctx, final MessageEvent me)
			throws Exception {
		
		// common http request
		if (me.getMessage() instanceof HttpRequest){
			
			final HttpRequest request = (HttpRequest) me.getMessage();
			String uri = request.getUri();
			
			String path = uri.split("/")[2];
			Handler h = handlers.getRequestHandler(path);
			if (h != null){
				DefaultHttpResponse resp = h.handle(me);
				
				resp.setHeader(HttpHeaders.Names.CONTENT_TYPE, CONTENT_TYPE);
				resp.setHeader("Content-Length", resp.getContent().readableBytes());

				boolean close = !HttpHeaders.isKeepAlive(request);

				resp.setHeader(HttpHeaders.Names.CONNECTION,
						close ? HttpHeaders.Values.CLOSE
								: HttpHeaders.Values.KEEP_ALIVE);
				
				ChannelFuture cf = me.getChannel().write(resp);

				if (close) 
					cf.addListener(ChannelFutureListener.CLOSE);
				writeAccessLog(me.getChannel(), request, resp);
			}
			
		}else{
			
			final HttpResponse response = (HttpResponse) me.getMessage();
			String path = response.getHeader("path");
			Handler h = handlers.getResponseHandler(path);
			if (h != null){
				h.handle(me);
			}
			// no need further reply 
		}

	
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
	protected void writeAccessLog( final Channel channel, HttpRequest req, 
			DefaultHttpResponse resp) throws UnsupportedEncodingException{
		String ip = channel.getRemoteAddress().toString();
		if( ip.startsWith("/") ) ip = ip.substring(1);
		
		String url = req.getUri();
		String responeContent = resp.getContent().toString( CharsetUtil.UTF_8 );
		
		log.info( "{} \"{}\" {}", ip, url, responeContent );
	}
	
	public String toString(){
		return " handlers=" + handlers.toString();
	}
}
