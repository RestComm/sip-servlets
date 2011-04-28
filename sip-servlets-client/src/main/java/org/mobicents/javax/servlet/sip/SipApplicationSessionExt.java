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

package org.mobicents.javax.servlet.sip;

/**
 * Interface Extension that adds extra features to the JSR 289 SipApplicationSession interface.</br>
 * It adds the following capabilities : 
 * 
 * <ul> 		
 * 		<li>
 * 			Allows for applications to schedule work asynchronously against a SipApplicationSession in a thread-safe manner if used in conjunction with the Mobicents Concurrency Control Mechanism
 * 		</li>
 * </ul>
 * 
 * Here is some sample code to show how the asynchronous work can be used :
 * 
 * <pre>
 * ((SipApplicationSessionExt)sipApplicationSession).scheduleAsynchronousWork(new SipApplicationSessionAsynchronousWork() {
 * 		private static final long serialVersionUID = 1L;
 * 
 * 		public void doAsynchronousWork(SipApplicationSession sipApplicationSession) {				
 * 			
 * 			String textMessageToSend = (String) sipApplicationSession.getAttribute("textMessageToSend"); 					
 * 			
 *			try {
 *				SipServletRequest sipServletRequest = sipFactory.createRequest(
 *						sipApplicationSession, 
 *						"MESSAGE", 
 *						"sip:sender@sip-servlets.com", 
 *						"sip:receiver@sip-servlets.com");
 *				SipURI sipUri = sipFactory.createSipURI("receiver", "127.0.0.1:5060");
 *				sipServletRequest.setRequestURI(sipUri);
 *				sipServletRequest.setContentLength(content.length());
 *				sipServletRequest.setContent(content, "text/plain;charset=UTF-8");
 *				sipServletRequest.send();
 *			} catch (ServletParseException e) {
 *				logger.error("Exception occured while parsing the addresses",e);
 *			} catch (IOException e) {
 *				logger.error("Exception occured while sending the request",e);			
 *			}
 * 		}
 * 	});
 * </pre>
 * 
 * @author jean.deruelle@gmail.com
 * @since 1.4
 */
public interface SipApplicationSessionExt {	
	/**
	 * This method allows an application to access a SipApplicationSession in an asynchronous manner. 
	 * This method is useful for accessing the SipApplicationSession from Web or EJB modules in a converged application 
	 * or from unmanaged threads started by the application itself.
	 * 
     * When this API is used in conjunction with the Mobicents Concurrency Control in SipApplicationSession mode, 
     * the container guarantees that the business logic contained within the SipApplicationSessionAsynchronousWork
     * will be executed in a thread-safe manner. 
     * 
     * It has to be noted that the work may never execute if the session gets invalidated in the meantime
     * and the work will be executed locally on the node on a cluster. 
     * 
	 * @param work the work to be performed on this SipApplicationSession. 
	 */
    void scheduleAsynchronousWork(SipApplicationSessionAsynchronousWork work);
}
