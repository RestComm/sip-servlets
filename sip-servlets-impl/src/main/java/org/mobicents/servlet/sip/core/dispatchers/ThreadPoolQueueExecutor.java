package org.mobicents.servlet.sip.core.dispatchers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolQueueExecutor extends ThreadPoolExecutor {
	
	private BlockingQueue<Runnable> queue;
	
	public ThreadPoolQueueExecutor(int threads, int maxThreads, BlockingQueue<Runnable> workQueue) {
		super(threads, maxThreads, 90, TimeUnit.SECONDS, workQueue);
		this.queue = workQueue;
	}
	
	public BlockingQueue<Runnable> getQueue() {
		return queue;
	}

}
