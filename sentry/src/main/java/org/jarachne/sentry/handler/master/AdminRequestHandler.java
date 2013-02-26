package org.jarachne.sentry.handler.master;



import org.jarachne.network.http.Handler;
import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public class AdminRequestHandler implements Handler{

	public String getPath() {
		// TODO Auto-generated method stub
		return "/admin";
	}

	public DefaultHttpResponse handle(MessageEvent me) {
		// TODO Auto-generated method stub
		final HttpRequest request = (HttpRequest) me.getMessage();
		NettyHttpRequest nhr = new NettyHttpRequest(request);
		
		String action = nhr.param("action", "help");
		DefaultHttpResponse resp = new DefaultHttpResponse(request.getProtocolVersion(), HttpResponseStatus.OK);
		if (action == null){
			HttpResponseUtil.setHttpResponseWithMessage(resp, HttpResponseStatus.BAD_REQUEST, "action must be set:[...]");
			return resp;
		}else{
			if (action.equals("help")){
				HttpResponseUtil.setHttpResponseOkReturn(resp, "cmd:[help, test]");
			}else if (action.equals("test")){
				HttpResponseUtil.setHttpResponseOkReturn(resp, "tttteeeessssstttttt");
			}
			return resp;
		}
	}

}
