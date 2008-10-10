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
package org.mobicents.servlet.sip.core.dispatchers;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This helper class simply exposes the queue of ThreadPoolExecutor class in order to be able
 * to monitor stats from it. It can be used as a SingleThreadExecutor (executing tasks in a
 * single thread sequentially) by passing threads=1 and maxThreads=1, otherwise it behaves like
 * a chache thread pool.
 * 
 * @author Vladimir Ralev
 *
 */
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
