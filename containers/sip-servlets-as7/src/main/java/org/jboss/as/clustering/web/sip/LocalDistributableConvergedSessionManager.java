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

package org.jboss.as.clustering.web.sip;

import org.jboss.as.clustering.web.DistributableSessionMetadata;
import org.jboss.as.clustering.web.SessionIdFactory;

/**
 * Callback interface to allow the distributed caching layer to invoke upon the
 * local session manager.
 * 
 * @author Brian Stansberry
 * @author posfai.gergely@ext.alerant.hu
 * @version $Revision: $
 */
public interface LocalDistributableConvergedSessionManager extends SessionIdFactory {
	void notifyRemoteSipApplicationSessionInvalidation(String sessId);

	void notifyRemoteSipSessionInvalidation(String sipAppSessionId,
			String sipSessionId);

	void notifySipApplicationSessionLocalAttributeModification(String sessId);

	void notifySipSessionLocalAttributeModification(String sipAppSessionId,
			String sipSessionId);

	boolean sipApplicationSessionChangedInDistributedCache(String realId,
			String owner, int intValue, long longValue,
			DistributableSessionMetadata distributableSessionMetadata);

	boolean sipSessionChangedInDistributedCache(String sipAppSessionId,
			String sipSessionId, String owner, int intValue, long longValue,
			DistributableSessionMetadata distributableSessionMetadata);

	void sipApplicationSessionActivated();

	void sipSessionActivated();
}
