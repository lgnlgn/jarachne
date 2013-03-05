package org.jarachne.sentry.master.handler;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.handler.AbstractDistributedChannelHandler;
import org.jarachne.sentry.handler.LocalRequestHandler;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.handler.ToSlaveRequestHandler;
import org.jarachne.sentry.master.MasterModule;
import org.jarachne.util.Strings;
import org.jarachne.util.logging.ESLogger;
import org.jarachne.util.logging.Loggers;
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

public class MasterChannelHandler extends SimpleChannelHandler{

	static ESLogger log = Loggers.getLogger(MasterChannelHandler.class);
	
	Map<String, RequestHandler> handlers;
	MasterModule module ; //
	
	
	public MasterChannelHandler(Map<String, RequestHandler> handlers){
		this.handlers = handlers;
	}
	
	public MasterChannelHandler(RequestHandler... handlers){
		this.handlers = new ConcurrentHashMap<String, RequestHandler>();
		this.addRequestHandlers(handlers);
	}
	
	public void addRequestHandlers(RequestHandler... handlers){
		for(RequestHandler rh : handlers){
			this.handlers.put(rh.getPath(), rh);
		}
	}
	
	
	//TODO
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception{
		HttpRequest req = (HttpRequest)e.getMessage();
		NettyHttpRequest nhr = new NettyHttpRequest(req);
		String path = nhr.path();
		
		RequestHandler handler = handlers.get(path);
		DefaultHttpResponse resp = new DefaultHttpResponse(req.getProtocolVersion(), HttpResponseStatus.OK);
		if (handler == null){
			HttpResponseUtil.setHttpResponseWithMessage(resp, HttpResponseStatus.BAD_REQUEST, 
					"path not found! current path : " + Strings.display(this.handlers.keySet()));
		}else if (handler instanceof LocalRequestHandler){
			((LocalRequestHandler) handler).handle(nhr, resp);
		}else if (handler instanceof AbstractDistributedChannelHandler){
			//TODO further check
			String result = module.requestSlaves((ToSlaveRequestHandler) handler);
			HttpResponseUtil.setHttpResponseWithMessage(resp, HttpResponseStatus.OK, result);
		}
		
		resp.setHeader(HttpHeaders.Names.CONTENT_TYPE, Strings.CONTENT_TYPE);
		resp.setHeader("Content-Length", resp.getContent().readableBytes());

		boolean close = !HttpHeaders.isKeepAlive(req);

		resp.setHeader(HttpHeaders.Names.CONNECTION,
				close ? HttpHeaders.Values.CLOSE
						: HttpHeaders.Values.KEEP_ALIVE);
		
		ChannelFuture cf = e.getChannel().write(resp);

		if (close) 
			cf.addListener(ChannelFutureListener.CLOSE);
		writeAccessLog(e.getChannel(), req, resp);
	}
	
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception{
		e.getChannel().close();
	}
	
	protected void writeAccessLog( final Channel channel, HttpRequest req, 
			DefaultHttpResponse resp) throws UnsupportedEncodingException{
		String ip = channel.getRemoteAddress().toString();
		if( ip.startsWith("/") ) ip = ip.substring(1);
		
		String url = req.getUri();
		String responeContent = resp.getContent().toString( CharsetUtil.UTF_8 );
		
		log.info( "{} \"{}\" {}", ip, url, responeContent );
	}
	

	
}
