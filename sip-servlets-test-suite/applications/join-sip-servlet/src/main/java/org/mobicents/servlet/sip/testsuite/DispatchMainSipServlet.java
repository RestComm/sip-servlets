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
package org.mobicents.servlet.sip.testsuite;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;


public class DispatchMainSipServlet extends SipServlet {	
	private static final long serialVersionUID = 1L;

//	private static transient Logger logger = Logger.getLogger(DispatchMainSipServlet.class);
//	@Resource
//	private SipFactory sipFactory;
	
	/** Creates a new instance of DispatchMainSipServlet */
	public DispatchMainSipServlet() {
	}
	
	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		if("join".equalsIgnoreCase(((SipURI)request.getRequestURI()).getUser())) {
			RequestDispatcher dispatcher = 
		          getServletContext().getNamedDispatcher("JoinSenderSipServlet");
		      dispatcher.forward(request,null);
		}
		if("join-receiver".equalsIgnoreCase(((SipURI)request.getRequestURI()).getUser())) {
			RequestDispatcher dispatcher = 
		          getServletContext().getNamedDispatcher("JoinReceiverSipServlet");
		      dispatcher.forward(request,null);
		}
	}
	
	@Override
	protected void doMessage(SipServletRequest request) throws ServletException,
			IOException {
		if("join-receiver".equalsIgnoreCase(((SipURI)request.getRequestURI()).getUser())) {
			RequestDispatcher dispatcher = 
		          getServletContext().getNamedDispatcher("JoinSenderSipServlet");
		      dispatcher.forward(request,null);
		}
	}
	
	@Override
	protected void doSuccessResponse(SipServletResponse response)
			throws ServletException, IOException {
		if("joiner".equalsIgnoreCase(((SipURI)response.getFrom().getURI()).getUser())) {
			RequestDispatcher dispatcher = 
		          getServletContext().getNamedDispatcher("JoinSenderSipServlet");
		      dispatcher.forward(null,response);
		}
	}
}