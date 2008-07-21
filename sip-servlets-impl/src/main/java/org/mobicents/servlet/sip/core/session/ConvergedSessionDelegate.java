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

import javax.servlet.http.HttpSession;
import javax.servlet.sip.SipApplicationSession;
import javax.sip.ListeningPoint;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.mobicents.servlet.sip.core.ExtendedListeningPoint;
import org.mobicents.servlet.sip.core.SipNetworkInterfaceManager;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * This class handles the additionnal sip features of a converged session
 * It is a delegate since it is used by many http session implementations classes (Standard and clustered ones) 
 * 
 * 
 * @author Jean Deruelle
 *
 */
public class ConvergedSessionDelegate {

	protected SipNetworkInterfaceManager sipNetworkInterfaceManager;		
	protected SipManager sipManager;
	protected HttpSession httpSession;
	
	/**
	 * 
	 * @param sessionManager
	 */
	public ConvergedSessionDelegate(SipManager manager, SipNetworkInterfaceManager sipNetworkInterfaceManager, HttpSession httpSession) {
		this.sipNetworkInterfaceManager = sipNetworkInterfaceManager;
		this.sipManager = manager;
		this.httpSession = httpSession;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ConvergedHttpSession#encodeURL(java.lang.String)
	 */
	public String encodeURL(String url) {
		StringBuffer urlEncoded = new StringBuffer();
		urlEncoded = urlEncoded.append(url);
		urlEncoded = urlEncoded.append(";jsessionid=");
		urlEncoded = urlEncoded.append(httpSession.getId());
		return urlEncoded.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ConvergedHttpSession#encodeURL(java.lang.String, java.lang.String)
	 */
	public String encodeURL(String relativePath, String scheme) {
		StringBuffer urlEncoded = new StringBuffer();
		//Context
		Context context = (Context)sipManager.getContainer();
		//Host
		Host host = (Host)context.getParent();
		//Service
		Service service = ((Engine)host.getParent()).getService();
		String hostname = host.getName();
		//retrieving the port corresponding to the specified scheme
		//TODO ask EG what if the scheme is not supported on the server ?
		int port = -1;		
		Connector[] connectors = service.findConnectors();
		int i = 0;
		while (i < connectors.length && port < 0) {
			if(scheme != null && scheme.equalsIgnoreCase(connectors[i].getProtocol())) {
				port = connectors[i].getPort();
			}
			i++;
		}
		urlEncoded = urlEncoded.append(scheme);
		urlEncoded = urlEncoded.append("://");
		urlEncoded = urlEncoded.append(hostname);
		urlEncoded = urlEncoded.append(":");
		urlEncoded = urlEncoded.append(port);
		urlEncoded = urlEncoded.append("/");
		urlEncoded = urlEncoded.append(((Context)sipManager.getContainer()).getPath());
		urlEncoded = urlEncoded.append(relativePath);
		urlEncoded = urlEncoded.append(";jsessionid=");
		urlEncoded = urlEncoded.append(httpSession.getId());
		return urlEncoded.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ConvergedHttpSession#getApplicationSession()
	 */
	public SipApplicationSession getApplicationSession() {		
		//the application session if currently associated is returned, 
		MobicentsSipApplicationSession sipApplicationSession =
			sipManager.findSipApplicationSession(httpSession);
		if(sipApplicationSession == null) {
			//however if no application session is associated it is created, 
			//associated with the HttpSession and returned.
			ExtendedListeningPoint listeningPoint = 
				sipNetworkInterfaceManager.findMatchingListeningPoint(ListeningPoint.UDP, false);			
			
			SipApplicationSessionKey sipApplicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(
					((SipContext)sipManager.getContainer()).getApplicationName(), 
					listeningPoint.getSipProvider().getNewCallId().getCallId(),
					false);
			
			sipApplicationSession = 
				sipManager.getSipApplicationSession(sipApplicationSessionKey, true);			
			sipApplicationSession.addHttpSession(httpSession);
		}
		return sipApplicationSession;
	}

}
