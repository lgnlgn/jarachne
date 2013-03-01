package org.jarachne.sentry.http;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class ServerHandler extends SimpleChannelHandler{
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		DefaultHttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		HttpRequest req = (HttpRequest)e.getMessage();
		
		resp.setContent(ChannelBuffers.copiedBuffer((e.getChannel().getRemoteAddress() + "--" + ctx.getChannel().getLocalAddress() + " " + req.getUri()).getBytes()));
		resp.setChunked(false);
		Thread.sleep(2222);
		resp.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/text");
		resp.setHeader("aaa", e.getChannel().getRemoteAddress() + "--" + ctx.getChannel().getLocalAddress() + " " + req.getUri());
		resp.setHeader("Content-Length", resp.getContent().readableBytes());
//		System.out.println(resp);
		ChannelFuture cf = e.getChannel().write(resp);
		boolean close = !HttpHeaders.isKeepAlive(req);
		if (close) 
			cf.addListener(ChannelFutureListener.CLOSE);
		
//		ctx.getChannel().close();
	}
	
	
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {

//		if (log.isTraceEnabled())
//			log.trace("Connection exceptionCaught:{}", e.getCause().toString());

		e.getChannel().close();
	}
}
