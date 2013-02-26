package org.jarachne.sentry.handler.slave;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jarachne.network.http.Handler;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class FileSaveResponseHandler implements Handler{
	private volatile boolean isReading;
	private volatile boolean readingChunks;
	private File downloadFile;
	private FileOutputStream fOutputStream = null;

	public String getPath() {
		return null;
	}


	private void getInfoFromResp(DefaultHttpResponse httpResponse){
		String fileName = httpResponse.getHeader("fileName");
		downloadFile = new File(System.getProperty("user.dir")
				+ File.separator + "recived_" + fileName);
		readingChunks = httpResponse.isChunked();
	}

	public DefaultHttpResponse handle(MessageEvent me) {
		if (isReading == false){
			try{
				if (me.getMessage() instanceof HttpResponse) {
					DefaultHttpResponse httpResponse = (DefaultHttpResponse) me
							.getMessage();
					getInfoFromResp(httpResponse);
					isReading = true;
				} else {
					HttpChunk httpChunk = (HttpChunk) me.getMessage();
					
					if (!httpChunk.isLast()) {
						ChannelBuffer buffer = httpChunk.getContent();
						if (fOutputStream == null) {
							fOutputStream = new FileOutputStream(downloadFile);
						}
						while (buffer.readable()) {
							byte[] dst = new byte[buffer.readableBytes()];
							buffer.readBytes(dst);
							fOutputStream.write(dst);
						}
					} else {
						readingChunks = false;
					}
					fOutputStream.flush();
				}
				if (!readingChunks) {
					fOutputStream.close();
					me.getChannel().close();
					isReading = false;
				}
			}catch (IOException e) {
				
			}finally{
				;
			}
			return new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		}else{
			return null;

		}
	}

}
