package org.jarachne.sentry.http;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class ClientHandler extends SimpleChannelHandler{

	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (e.getMessage() instanceof HttpResponse){
			HttpResponse resp = (HttpResponse)e.getMessage();


			System.out.println(resp.getHeader("aaa"));}
		else{
			System.out.println("!!!!!!!!!!!");
		}
		//		ctx.getChannel().close();
	}
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {

		//		if (log.isTraceEnabled())
		//			log.trace("Connection exceptionCaught:{}", e.getCause().toString());
		System.out.println("???");
		e.getChannel().close();
	}
}
