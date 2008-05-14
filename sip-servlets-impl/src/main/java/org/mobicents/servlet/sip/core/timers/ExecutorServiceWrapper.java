/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.core.timers;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorServiceWrapper {	
	private static final int SCHEDULER_THREAD_POOL_DEFAULT_SIZE = 10;

	//TODO need to rename this class in ScheduledExecutorServiceWrapper
	//
	private static final ExecutorServiceWrapper singletonInstance = new ExecutorServiceWrapper();

	private ScheduledThreadPoolExecutor myThreadPool = null;

	private ExecutorServiceWrapper() {
		initialize();
	}
	
	public static ExecutorServiceWrapper getInstance() {		
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
	
	public ScheduledFuture<?> schedule(Callable<?> command, long delay,
			TimeUnit unit) {
		return myThreadPool.schedule(command, delay, unit);
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
