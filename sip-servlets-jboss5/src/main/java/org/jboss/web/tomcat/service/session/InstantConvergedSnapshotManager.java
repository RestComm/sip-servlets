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

import org.jboss.web.tomcat.service.session.distributedcache.spi.OutgoingDistributableSessionData;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * 
 */
public class InstantConvergedSnapshotManager extends InstantSnapshotManager implements SnapshotSipManager {

	/**
	 * @param manager
	 * @param path
	 */
	public InstantConvergedSnapshotManager(AbstractJBossManager manager, String path) {
		super(manager, path);
	}

	/**
	 * Instant replication of the modified session
	 */
	public void snapshot(ClusteredSipSession<? extends OutgoingDistributableSessionData> session) {
		if (session != null) {
			try {
				((ClusteredSipManager)getManager()).storeSipSession(session);
			} catch (Exception e) {
				getLog().warn(
						"Failed to replicate session "
								+ session.getId(), e);
			}
		}
	}
	
	/**
	 * Instant replication of the modified session
	 */
	public void snapshot(ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session) {
		if (session != null) {
			try {
				((ClusteredSipManager)getManager()).storeSipApplicationSession(session);
			} catch (Exception e) {
				getLog().warn(
						"Failed to replicate session "
								+ session.getId(), e);
			}
		}
	}

}
