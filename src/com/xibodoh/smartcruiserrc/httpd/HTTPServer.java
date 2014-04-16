/*
  ~ Copyright (c) 2014 Aleksei Semenov (aka xibodoh)
  ~ All rights reserved. This program and the accompanying materials
  ~ are made available under the terms of the GNU Public License v2.0
  ~ which accompanies this distribution, and is available at
  ~ http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
*/
package com.xibodoh.smartcruiserrc.httpd;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import fi.iki.elonen.NanoHTTPD;

public class HTTPServer extends NanoHTTPD {

	public interface RequestHandler {
		/**
		 * Handles request and returns {@link Response} instance
		 * or return null if request can not be handled.
		 *  
		 * @param session
		 * @return
		 */
		public Response serve(IHTTPSession session);
		
		public void start();
		
		public void stop();
		
	}
	
	private static class AsyncRunnerImpl  implements AsyncRunner {
		
		private final ThreadPoolExecutor threadPool;
		
		public AsyncRunnerImpl(int threadCount){
			threadPool = new ThreadPoolExecutor(5, threadCount, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
			threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
			threadPool.prestartAllCoreThreads();
		}
						
		@Override
		public void exec(Runnable code) {
			threadPool.execute(code);
		}
		
		public void shutdown(){
			threadPool.shutdown();
		}
		
	};
	
	private final ConcurrentLinkedQueue<RequestHandler> handlers = new ConcurrentLinkedQueue<RequestHandler>();
	//private final AsyncRunnerImpl asyncRunner;
	
	public HTTPServer(int port) {
		this(null, port);
	}

	public HTTPServer(String hostname, int port) {
		super(hostname, port);
		// TODO setAsyncRunner()
		//asyncRunner = new AsyncRunnerImpl(5);
		//setAsyncRunner(asyncRunner);
	}

	@Override
	public Response serve(IHTTPSession session) {
		//Log.d("Perf", Thread.currentThread()+"new request");
		for (RequestHandler handler: handlers){
			Response response = handler.serve(session);
			if (response!=null){
				return response;
			}
		}		
		return super.serve(session); // will return "not found" response
	}

	public void addRequestHandler(RequestHandler handler){
		handlers.add(handler);
	}
	
	public void removeRequestHandler(RequestHandler handler){
		handlers.remove(handler);
	}

	@Override
	public void stop() {
		//asyncRunner.shutdown();
		super.stop();
		for (RequestHandler handler: handlers){
			handler.stop();
		}
	}

	@Override
	public void start() throws IOException {
		for (RequestHandler handler: handlers){
			handler.start();
		}
		super.start();
	}
	
}
