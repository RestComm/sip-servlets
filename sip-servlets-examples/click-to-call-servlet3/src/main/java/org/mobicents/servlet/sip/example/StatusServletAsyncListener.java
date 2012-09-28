package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.util.Queue;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

public class StatusServletAsyncListener implements AsyncListener {
	StatusServletNotifier data;
	Queue<AsyncContext> queue;
	AsyncContext ac;
	
	public StatusServletAsyncListener() {}
	
	public StatusServletAsyncListener(AsyncContext ac, Queue<AsyncContext> queue, StatusServletNotifier data) {
		this.ac = ac;
		this.data = data;
		this.queue = queue;
	}
	
	public void onComplete(AsyncEvent event) throws IOException {
		queue.remove(ac);
	}
	public void onTimeout(AsyncEvent event) throws IOException {
		queue.remove(ac);
	}
	public void onError(AsyncEvent event) throws IOException {
		queue.remove(ac);
	}
	public void onStartAsync(AsyncEvent event) throws IOException {
	}
}