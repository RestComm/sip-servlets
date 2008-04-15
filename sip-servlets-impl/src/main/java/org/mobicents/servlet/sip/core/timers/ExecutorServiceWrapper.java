/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mobicents.servlet.sip.core.timers;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class ExecutorServiceWrapper {

	
	private static final int SCHEDULER_THREAD_POOL_DEFAULT_SIZE = 10;

	//TODO need to rename this class in ScheduledExecutorServiceWrapper
	private static ExecutorServiceWrapper singletonInstance;

	private ScheduledThreadPoolExecutor myThreadPool = null;

//	private static transient Log log = LogFactory
//			.getLog(SipContainerThreadPool.class.getName());

	public static synchronized ExecutorServiceWrapper getInstance() {
		if(singletonInstance == null) {
			singletonInstance = new ExecutorServiceWrapper();
		}
		singletonInstance.initialize();
		return singletonInstance;
	}

	public boolean isShutdown() {
		return myThreadPool.isShutdown();
	}

	public void shutdown() {
		myThreadPool.shutdown();
	}

	public void initialize(int threadPoolSize) {
		myThreadPool = new ScheduledThreadPoolExecutor(threadPoolSize,
				new ThreadPoolExecutor.CallerRunsPolicy());
		myThreadPool.prestartAllCoreThreads();
		// TODO check if the queue should be set.
	}

	public void initialize() {
		initialize(SCHEDULER_THREAD_POOL_DEFAULT_SIZE);
	}
	
	/**
	 * Executes the Runnable in a thread pool of worker threads
	 */
	public void execute(Runnable r) {
		myThreadPool.execute(new MyRunnable(r));
	}

	public ScheduledFuture<?> schedule(Runnable command, long delay,
			TimeUnit unit) {
		return myThreadPool.schedule(new MyRunnable(command), delay, unit);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
			long initialDelay, long period, TimeUnit unit) {
		return myThreadPool.scheduleAtFixedRate(new MyRunnable(command),
				initialDelay, period, unit);
	}

	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
			long initialDelay, long delay, TimeUnit unit) {
		return myThreadPool.scheduleWithFixedDelay(new MyRunnable(command),
				initialDelay, delay, unit);
	}

	public void purge() {
		myThreadPool.purge();
	}

	private class MyRunnable implements Runnable {
		private Runnable myRunnable;

		MyRunnable(Runnable r) {
			myRunnable = r;
		}

		public void run() {
			try {
				myRunnable.run();

			} catch (Throwable t) {
//				log.fatal("error while executing task:", t);
				throw new RuntimeException(t);
			}
		}
	}
	
}
