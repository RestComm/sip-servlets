/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.jboss.as.web.session.sip;

import org.jboss.logging.Logger;
import org.jboss.as.web.session.SessionManager;
import org.jboss.as.web.session.InstantSnapshotManager;
import org.jboss.as.clustering.web.OutgoingDistributableSessionData;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * @author posfai.gergely@ext.alerant.hu
 * 
 */
public class InstantConvergedSnapshotManager extends InstantSnapshotManager implements SnapshotSipManager {
	
	private static Logger logger = Logger.getLogger(InstantConvergedSnapshotManager.class);
	
	/**
	 * @param manager
	 * @param path
	 */
	public InstantConvergedSnapshotManager(SessionManager manager, String path) {
		super(manager, path);
		if (logger.isDebugEnabled()){
			logger.debug("InstantConvergedSnapshotManager constructor - manager=" + manager + ", path=" + path);
		}
	}

	/**
	 * Instant replication of the modified session
	 */
	public void snapshot(ClusteredSipSession<? extends OutgoingDistributableSessionData> session) {
		if (logger.isDebugEnabled()){
			logger.debug("snapshot(ClusteredSipSession)");
		}
		if (session != null) {
			try {
				((ClusteredSipSessionManager<? extends OutgoingDistributableSessionData>)getManager()).storeSipSession(session);
			} catch (Exception e) {
				getLog().warn(
						"Failed to replicate session "
								+ session.getId(), e);
			}
		} else {
			if(logger.isDebugEnabled()){
				logger.debug("nothing to replicate");
			}
		}
	}
	
	/**
	 * Instant replication of the modified session
	 */
	public void snapshot(ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session) {
		if (logger.isDebugEnabled()){
			logger.debug("snapshot(ClusteredSipApplicationSession)");
		}
		if (session != null) {
			try {
				((ClusteredSipSessionManager<? extends OutgoingDistributableSessionData>)getManager()).storeSipApplicationSession(session);
			} catch (Exception e) {
				getLog().warn(
						"Failed to replicate session "
								+ session.getId(), e);
			}
		} else {
			if(logger.isDebugEnabled()){
				logger.debug("nothing to replicate");
			}
		}
	}

}
