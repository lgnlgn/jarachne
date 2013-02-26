package org.jarachne.sentry.handler;

import java.io.File;
import java.io.FileOutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class FileClientHandler extends SimpleChannelUpstreamHandler {
	private volatile boolean readingChunks;
	private File downloadFile;
	private FileOutputStream fOutputStream = null;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		/*
		 * 按照channel的顺序进行处理
		 * server先发送HttpResponse过来，所以这里先对HttpResponse进行处理，进行文件判断之类
		 * 之后，server发送的都是ChunkedFile了。
		 */

		if (e.getMessage() instanceof HttpResponse) {
			DefaultHttpResponse httpResponse = (DefaultHttpResponse) e
					.getMessage();
			String fileName = httpResponse.getHeader("fileName");
			downloadFile = new File(System.getProperty("user.dir")
					+ File.separator + "recived_" + fileName);
			readingChunks = httpResponse.isChunked();
		} else {
			HttpChunk httpChunk = (HttpChunk) e.getMessage();
			
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
			e.getChannel().close();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		System.out.println(e.getCause());
	}
}
