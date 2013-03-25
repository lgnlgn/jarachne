package org.jarachne.sentry.master.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jarachne.common.Constants;
import org.jarachne.common.JarachneException;
import org.jarachne.network.http.HttpResponseUtil;
import org.jarachne.network.http.NettyHttpRequest;
import org.jarachne.sentry.core.HttpRequestCallable;
import org.jarachne.sentry.core.Module;
import org.jarachne.sentry.core.SentryConstants;
import org.jarachne.sentry.handler.RequestHandler;
import org.jarachne.sentry.master.MasterModule;
import org.jarachne.sentry.slave.handler.FileReceiveHandler;
import org.jarachne.util.concurrent.ConcurrentExecutor;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;


/**
 * send data blocks to slaves,  
 * URL format: http://HOST:PORT/distributedata?name=
 * @author lgn
 *
 */
public class DataAllocateHandler extends RequestHandler{

	public static String PATH =  "/distributedata";
	final static String KEY_BLOCK = "block";
	public final static String KEY_DATA = "name";
	final static int TRIES = 2;
	public DataAllocateHandler(Module module) {
		super(module, null);
	}

	private int getNumbBlocks(String dataName) throws  IOException{
		File data = new File(Constants.DATA_PATH + "/" + dataName);
		if (!data.isDirectory() ){
			File dataProperties =  new File(Constants.DATA_PATH + "/" + dataName + Constants.DATA_SUFFIXES[0] );
			if (dataProperties.isFile()){
				Properties p = new Properties();
				InputStream in = new FileInputStream(dataProperties);
				p.load(in);
				in.close();
				return  new Integer(p.getProperty(KEY_BLOCK));
			}		
		}
		throw new JarachneException("file does not exists!");
	}

	public String getPath() {
		return PATH;
	}

	public void handle(NettyHttpRequest req, DefaultHttpResponse resp) {
		MasterModule m = ((MasterModule)module);
		String dataName = req.param(KEY_DATA);
		if (dataName == null){
			HttpResponseUtil.setResponse(resp, "distribute data to slaves", "require '" + KEY_DATA + "' parameter");
			resp.setStatus(HttpResponseStatus.BAD_REQUEST);
		}else{
			List<String> slaves =  m.yieldSlaves();
			HttpClient client = m.httpClient;
			client.getParams();
			
			int tries = 0;
			long t = System.currentTimeMillis();
			try {
				int blocks = getNumbBlocks(dataName);
				boolean[] sendOks= new boolean[blocks];
				Array.setBoolean(sendOks, 0, false);
				for(; tries < TRIES && allTrue(sendOks); tries++){
					System.out.println("try : " + tries);
					for(int i = 0 ; i < blocks; i++){
						int toSlavex = i % slaves.size();
						if (sendOks[i] == true)
							continue;
						HttpPost[] postsOfBlock = setHttpPost("http:/" + slaves.get(toSlavex) + "/" + SentryConstants.Paths.SLAVE_RECIEVE_FILE_PATH, dataName, i);
						boolean[] postResults = new boolean[postsOfBlock.length];
						for(int j = 0 ; j < postsOfBlock.length; j ++){
							try{
								String result = this.fetchResult(client, postsOfBlock[j]);
								if (result.equals(FileReceiveHandler.SUCCESS_STRING)){
									postResults[j] = true;
									m.addMessageToJob(postsOfBlock[j].getURI() + "  finished!" );
								}
							}catch(Exception e){
								//TODO
								e.printStackTrace();
							}
						}
						if (allTrue(postResults)){
							sendOks[i] = true;
						}
					}
					
				}
				
				long timeCost = System.currentTimeMillis() - t;
				if (allTrue(sendOks) == false){
					HttpResponseUtil.setResponse(resp, "distribute (" + dataName+ ") to slaves", 
							" #tries reaches (" + TRIES + ") times, cost -> (" + timeCost +") ms.");
					resp.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
				}else{
					HttpResponseUtil.setResponse(resp, "distribute (" + dataName+ ") to slaves", 
							"data distributed! #tries -> (" + TRIES + ") times, cost -> (" + timeCost +") ms.");

				}
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				client.getConnectionManager().shutdown();
			}
		}
	}

	private HttpPost[] setHttpPost(String URL, String dataName, int blockId) throws IOException{
		HttpPost[] httpPosts = new HttpPost[Constants.BLOCK_SUFFIXES.length];
		for(int i = 0 ; i < Constants.BLOCK_SUFFIXES.length; i++){
			HttpPost post = new HttpPost(URL);
			post.addHeader(SentryConstants.HeaderKeys.DATANAME, dataName);
			post.addHeader(SentryConstants.HeaderKeys.FILENAME, "/" + dataName + "/" + blockId + Constants.BLOCK_SUFFIXES[i]);
			HttpParams params = post.getParams();
			HttpConnectionParams.setConnectionTimeout(params,1000);
			RandomAccessFile raf = new RandomAccessFile(String.format("%s/%s/%d%s", Constants.DATA_PATH, dataName, blockId, Constants.BLOCK_SUFFIXES[i]), "r");
			byte[] file = new byte[(int)raf.length()];
			raf.read(file);
			raf.close();

			post.setEntity(new ByteArrayEntity(file));
			httpPosts[i] = post;
		}
		return httpPosts;
	}

	
	private String fetchResult(HttpClient client, HttpUriRequest req ) throws InterruptedException, ExecutionException, TimeoutException{
		List<Callable<String >> distributedReqs = new ArrayList<Callable<String >> (1);
		distributedReqs.add(new HttpRequestCallable(client, req));
		List<String> results = ConcurrentExecutor.execute(distributedReqs, 2000);
		return results.toString();
	}
	
	private boolean allTrue(boolean[] sendOks){
		for(int i = 0; i < sendOks.length; i++){
			if (sendOks[i] == false){
				return true;
			}
		}
		return false;
	}

}
