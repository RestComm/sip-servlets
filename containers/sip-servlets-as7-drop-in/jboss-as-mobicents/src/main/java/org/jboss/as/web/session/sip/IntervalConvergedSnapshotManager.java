/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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
package org.jboss.as.web.session.sip;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.logging.Logger;
import org.jboss.as.web.session.SessionManager;
import org.jboss.as.web.session.IntervalSnapshotManager;
import org.jboss.as.clustering.web.OutgoingDistributableSessionData;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * 
 */
public class IntervalConvergedSnapshotManager extends IntervalSnapshotManager implements SnapshotSipManager {
	protected static Logger logger = Logger.getLogger(IntervalConvergedSnapshotManager.class);
	// the modified sessions
	protected Set<ClusteredSipSession<? extends OutgoingDistributableSessionData>> sipSessions = new LinkedHashSet<ClusteredSipSession<? extends OutgoingDistributableSessionData>>();
	protected Set<ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>> sipApplicationSessions = new LinkedHashSet<ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>>();

	// the interval in ms
	private int interval = 1000;

	// the distribute thread
	private Thread thread = null;

	// Is session processing allowed?
	private boolean processingAllowed = false;
	   
	// has the thread finished?
	private boolean threadDone = false;

	/**
	 * @param manager
	 * @param path
	 */
	public IntervalConvergedSnapshotManager(SessionManager manager, String path) {
		super(manager, path);
	}

	/**
	 * @param manager
	 * @param path
	 * @param interval
	 */
	public IntervalConvergedSnapshotManager(SessionManager manager,
			String path, int interval) {
		super(manager, path, interval);
	}

	/**
	 * Store the modified session in a hashmap for the distributor thread
	 */
	public void snapshot(ClusteredSipSession<? extends OutgoingDistributableSessionData> session) {
		try {
			// Don't hold a ref to the session for a long time
			synchronized (sipSessions) {
				sipSessions.add(session);
			}
			if(logger.isDebugEnabled()){
				logger.debug("queued sip session " + session.getKey() + " for replication");
			}
		} catch (Exception e) {
			logger.error(
					"Failed to queue sip session " + session + " for replication",
					e);
		}
	}

	/**
	 * Store the modified session in a hashmap for the distributor thread
	 */
	public void snapshot(ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session) {
		try {
			// Don't hold a ref to the session for a long time
			synchronized (sipApplicationSessions) {
				sipApplicationSessions.add(session);
			}
			if(logger.isDebugEnabled()){
				logger.debug("queued sip app session " + session.getKey() + " for replication");
			}
		} catch (Exception e) {
			logger.error(
					"Failed to queue sip app session " + session + " for replication",
					e);
		}
	}

	/**
	 * Distribute all modified sessions
	 */
	protected void processSipSessions() {
		Set<ClusteredSipSession<? extends OutgoingDistributableSessionData>> toProcess = null;
		synchronized (sipSessions) {
			toProcess = new HashSet<ClusteredSipSession<? extends OutgoingDistributableSessionData>>(sipSessions);			
			sipSessions.clear();
		}

		ClusteredSipSessionManager<OutgoingDistributableSessionData> mgr = (ClusteredSipSessionManager) getManager();
		for (ClusteredSipSession<? extends OutgoingDistributableSessionData> session : toProcess) {
			// Confirm we haven't been stopped
			if (!processingAllowed)
				break;

			try {
				mgr.storeSipSession(session);
			} catch (Exception e) {
				getLog().error(
						"Caught exception processing session "
								+ session.getKey(), e);
			}
		}
	}

	/**
	 * Distribute all modified sessions
	 */
	protected void processSipApplicationSessions() {
		Set<ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>> toProcess = null;
		// Naoki : Fix for Issue 1620 : Invalid synchronization in IntervalConvergedSnapshotManager		
		synchronized (sipApplicationSessions) {
			toProcess = new HashSet<ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData>>(sipApplicationSessions);			
			sipApplicationSessions.clear();
		}

		ClusteredSipSessionManager<OutgoingDistributableSessionData> mgr = (ClusteredSipSessionManager) getManager();
		for (ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session : toProcess) {
			// Confirm we haven't been stopped
			if (!processingAllowed)
				break;

			try {
				mgr.storeSipApplicationSession(session);
			} catch (Exception e) {
				getLog().error(
						"Caught exception processing session "
								+ session.getKey(), e);
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
