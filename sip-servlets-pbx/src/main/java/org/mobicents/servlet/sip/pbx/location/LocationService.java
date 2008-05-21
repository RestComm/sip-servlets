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
package org.mobicents.servlet.sip.pbx.location;

import java.util.List;

/**
 * @author Thomas Leseney
 */
public interface LocationService {

	void start() throws Exception;
	void stop() throws Exception;
	
	Binding createBinding(String aor, String contact);
	void addBinding(Binding binding);
	List<Binding> getBindings(String aor);
	List<Binding> getAllBindings();
	void updateBinding(Binding binding);
	void removeBinding(Binding binding);
	
	// TODO move to JTA ?
	void beginTransaction();
	void commitTransaction();
	void rollbackTransaction();
}
