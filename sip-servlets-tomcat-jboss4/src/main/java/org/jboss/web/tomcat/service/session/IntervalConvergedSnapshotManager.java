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
package org.jboss.web.tomcat.service.session;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * 
 */
public class IntervalConvergedSnapshotManager extends IntervalSnapshotManager implements SnapshotSipManager {
	// the modified sessions
	protected Set sipSessions = new LinkedHashSet();
	protected Set sipApplicationSessions = new LinkedHashSet();

	/**
	 * @param manager
	 * @param path
	 */
	public IntervalConvergedSnapshotManager(AbstractJBossManager manager, String path) {
		super(manager, path);
	}

	/**
	 * @param manager
	 * @param path
	 * @param interval
	 */
	public IntervalConvergedSnapshotManager(AbstractJBossManager manager,
			String path, int interval) {
		super(manager, path, interval);
	}

	/**
	 * Store the modified session in a hashmap for the distributor thread
	 */
	public void snapshot(ClusteredSipSession session) {
		try {
			// Don't hold a ref to the session for a long time
			synchronized (sipSessions) {
				sipSessions.add(session);
			}
		} catch (Exception e) {
			log.error(
					"Failed to queue session " + session + " for replication",
					e);
		}
	}

	/**
	 * Store the modified session in a hashmap for the distributor thread
	 */
	public void snapshot(ClusteredSipApplicationSession session) {
		try {
			// Don't hold a ref to the session for a long time
			synchronized (sipApplicationSessions) {
				sipApplicationSessions.add(session);
			}
		} catch (Exception e) {
			log.error(
					"Failed to queue session " + session + " for replication",
					e);
		}
	}

	/**
	 * Distribute all modified sessions
	 */
	protected void processSipSessions() {
		ClusteredSipSession[] toProcess = null;
		synchronized (sipSessions) {
			toProcess = new ClusteredSipSession[sipSessions.size()];
			toProcess = (ClusteredSipSession[]) sipSessions.toArray(toProcess);
			sipSessions.clear();
		}

		JBossCacheSipManager mgr = (JBossCacheSipManager) getManager();
		for (int i = 0; i < toProcess.length; i++) {
			// Confirm we haven't been stopped
			if (!processingAllowed)
				break;

			try {
				mgr.storeSipSession(toProcess[i]);
			} catch (Exception e) {
				getLog().error(
						"Caught exception processing session "
								+ toProcess[i].getId(), e);
			}
		}
	}

	/**
	 * Distribute all modified sessions
	 */
	protected void processSipApplicationSessions() {
		ClusteredSipApplicationSession[] toProcess = null;
		synchronized (sipApplicationSessions) {
			toProcess = new ClusteredSipApplicationSession[sipApplicationSessions
					.size()];
			toProcess = (ClusteredSipApplicationSession[]) sipApplicationSessions
					.toArray(toProcess);
			sipApplicationSessions.clear();
		}

		JBossCacheSipManager mgr = (JBossCacheSipManager) getManager();
		for (int i = 0; i < toProcess.length; i++) {
			// Confirm we haven't been stopped
			if (!processingAllowed)
				break;

			try {
				mgr.storeSipApplicationSession(toProcess[i]);
			} catch (Exception e) {
				getLog().error(
						"Caught exception processing session "
								+ toProcess[i].getId(), e);
			}
		}
	}

	/**
	 * Start the snapshot manager
	 */
	public void start() {
		processingAllowed = true;
		startThread();
	}

	/**
	 * Stop the snapshot manager
	 */
	public void stop() {
		processingAllowed = false;
		stopThread();
		synchronized (sessions) {
			sessions.clear();
		}
		synchronized (sipSessions) {
			sipSessions.clear();
		}
		synchronized (sipApplicationSessions) {
			sipApplicationSessions.clear();
		}
	}

	/**
	 * Start the distributor thread
	 */
	protected void startThread() {
		if (thread != null) {
			return;
		}

		thread = new Thread(this, "ClusteredConvergedSessionDistributor["
				+ getContextPath() + "]");
		thread.setDaemon(true);
		thread.setContextClassLoader(getManager().getContainer().getLoader()
				.getClassLoader());
		threadDone = false;
		thread.start();
	}

	/**
	 * Stop the distributor thread
	 */
	protected void stopThread() {
		if (thread == null) {
			return;
		}
		threadDone = true;
		thread.interrupt();
		try {
			thread.join();
		} catch (InterruptedException e) {
		}
		thread = null;
	}

	/**
	 * Thread-loop
	 */
	public void run() {
		while (!threadDone) {
			try {
				Thread.sleep(interval);
				processSessions();
				processSipApplicationSessions();
				processSipSessions();
			} catch (InterruptedException ie) {
				if (!threadDone)
					getLog().error("Caught exception processing sessions", ie);
			} catch (Exception e) {
				getLog().error("Caught exception processing sessions", e);
			}
		}
	}
}
