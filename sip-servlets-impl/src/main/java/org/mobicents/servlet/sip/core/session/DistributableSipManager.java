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
package org.mobicents.servlet.sip.core.session;

import org.mobicents.servlet.sip.message.SipFactoryImpl;


/**
 * Interface that should be implemented by all manager allowing applications to work in a distributed environment.
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public interface DistributableSipManager extends SipManager {
	/**
	 * Retrieve a sip application session from its key. If none exists, one can enforce
	 * the creation through the create parameter to true.
	 * @param key the key identifying the sip application session to retrieve 
	 * @param create if set to true, if no session has been found one will be created
	 * @param localOnly if true check only locally and not in the cache, if false check in the cache as well
	 * @return the sip application session matching the key
	 */
	public MobicentsSipApplicationSession getSipApplicationSession(final SipApplicationSessionKey key, final boolean create, final boolean localOnly);

	/**
	 * Retrieve a sip session from its key. If none exists, one can enforce
	 * the creation through the create parameter to true. the sip factory cannot be null
	 * if create is set to true.
	 * @param key the key identifying the sip session to retrieve 
	 * @param create if set to true, if no session has been found one will be created
	 * @param sipFactoryImpl needed only for sip session creation.
	 * @param MobicentsSipApplicationSession to associate the SipSession with if create is set to true, if false it won't be used
	 * @param localOnly if true check only locally and not in the cache, if false check in the cache as well
	 * @return the sip session matching the key
	 * @throws IllegalArgumentException if create is set to true and sip Factory is null
	 */
	public MobicentsSipSession getSipSession(final SipSessionKey key, final boolean create, final SipFactoryImpl sipFactoryImpl, final MobicentsSipApplicationSession MobicentsSipApplicationSession, final boolean localOnly);
}
