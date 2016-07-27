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

import org.jboss.as.clustering.web.OutgoingDistributableSessionData;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * @author posfai.gergely@ext.alerant.hu
 * 
 */
public interface SnapshotSipManager {
	void snapshot(
			ClusteredSipSession<? extends OutgoingDistributableSessionData> session);

	void snapshot(
			ClusteredSipApplicationSession<? extends OutgoingDistributableSessionData> session);

	/**
	 * Start the snapshot manager
	 */
	void start();

	/**
	 * Stop the snapshot manager
	 */
	void stop();
}
