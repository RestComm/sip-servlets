/*
 * under the terms of the GNU Lesser General Public License as
 * This is free software; you can redistribute it and/or modify it
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

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletException;

import org.apache.catalina.Manager;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

/**
 * This class extends the jboss ClusteredSessionValve (JBoss AS 4.2.2 Tag) to make use of the 
 * ConvergedSessionReplicationContext.
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 * 
 */
public class ConvergedClusteredSessionValve extends ClusteredSessionValve {

	// The info string for this Valve
	private static final String info = "ConvergedClusteredSessionValve/1.0";

	/**
	 * Create a new Valve.
	 */
	public ConvergedClusteredSessionValve(Manager manager) {
		super(manager);
	}
	
	/**
	 * Get information about this Valve.
	 */
	public String getInfo() {
		return info;
	}

	@Override
	/**
	 * Valve-chain handler method. This method gets called when the request goes
	 * through the Valve-chain. Our session replication mechanism replicates the
	 * session after request got through the servlet code.
	 * 
	 * @param request The request object associated with this request.
	 * @param response The response object associated with this request.
	 */
	public void invoke(Request request, Response response) throws IOException,
			ServletException {
		// Initialize the context and store the request and response objects
		// for any clustering code that has no direct access to these objects
		ConvergedSessionReplicationContext.enterWebapp(request, response, true);
		try {
			// Workaround to JBAS-5735. Ensure we get the session from the manager
	         // rather than a cached ref from the Request.
	         String requestedId = request.getRequestedSessionId();
	         if (requestedId != null)
	         {
	            super.manager.findSession(requestedId);
	         }
			// let the servlet invocation go through
			getNext().invoke(request, response);
		} finally // We replicate no matter what
		{
			// --> We are now after the servlet invocation
			try {
				ConvergedSessionReplicationContext ctx = ConvergedSessionReplicationContext
						.exitWebapp();

				if (ctx.getSoleSnapshotManager() != null) {
					ctx.getSoleSnapshotManager().snapshot(ctx.getSoleSession());
				} else {
					// Cross-context request touched multiple sesssions;
					// need to replicate them all
					Map sessions = ctx.getCrossContextSessions();
					if (sessions != null && sessions.size() > 0) {
						for (Iterator iter = sessions.entrySet().iterator(); iter
								.hasNext();) {
							Map.Entry entry = (Map.Entry) iter.next();
							((SnapshotManager) entry.getValue())
									.snapshot((ClusteredSession) entry.getKey());
						}
					}
				}
			} finally {
				ConvergedSessionReplicationContext.finishCacheActivity();
			}

		}
	}
}
