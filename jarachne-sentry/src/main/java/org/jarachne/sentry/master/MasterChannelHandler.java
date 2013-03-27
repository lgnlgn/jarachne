package org.jarachne.sentry.master;

import java.util.List;
import java.util.Set;

import net.sf.json.JSONArray;

import org.jarachne.network.http.BaseChannelHandler;
import org.jarachne.network.http.Handler;
import org.jarachne.network.http.Handlers;
import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.master.handler.DataAllocateHandler;
import org.jarachne.sentry.master.handler.JobMessageFromSlaveHandler;
import org.jarachne.util.JsonStringUtil;
import org.jarachne.util.Strings;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class MasterChannelHandler extends BaseChannelHandler{

	public MasterChannelHandler(Handlers handlers) {
		super(handlers);
	}

	public void messageReceived(ChannelHandlerContext ctx, final MessageEvent e)
			throws Exception {
		// common http request
		HttpRequest req = (HttpRequest)e.getMessage();
		NettyHttpRequest nhr = new NettyHttpRequest(req);
		String path = nhr.path();

		Handler handler = handlers.getHandler(path);
		DefaultHttpResponse resp = new DefaultHttpResponse(req.getProtocolVersion(), HttpResponseStatus.OK);
		if (handler == null){
			Set<String> handlerPaths = this.handlers.getHandlerPaths();
			
			handlerPaths.remove(DataAllocateHandler.PATH);
			handlerPaths.remove(JobMessageFromSlaveHandler.PATH);
			JSONArray ja = new JSONArray();
			ja.addAll(handlerPaths);
			HttpResponseUtil.setResponse(resp, "request path :" + path, 
					JsonStringUtil.JSONFormatString("code",404, "available_path", ja),
					HttpResponseStatus.BAD_REQUEST	);
		}else{
			handler.handle(nhr, resp);
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
	
}
