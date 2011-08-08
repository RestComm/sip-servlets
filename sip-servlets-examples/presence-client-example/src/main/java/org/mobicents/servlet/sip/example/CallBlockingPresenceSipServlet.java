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

package org.mobicents.servlet.sip.example;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.StringTokenizer;

import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletContextEvent;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.annotation.SipListener;

import org.apache.log4j.Logger;
import org.jboss.jmx.adaptor.rmi.RMIAdaptor;
import org.mobicents.xcap.client.XcapClient;
import org.mobicents.xcap.client.XcapResponse;
import org.mobicents.xcap.client.auth.Credentials;
import org.mobicents.xcap.client.impl.XcapClientImpl;
import org.mobicents.xcap.client.uri.DocumentSelectorBuilder;
import org.mobicents.xcap.client.uri.ElementSelectorBuilder;
import org.mobicents.xcap.client.uri.UriBuilder;

/**
 * This example shows Call Blocking and XCAP Capabilities. It makes the sip servlet acts as
 * a UAS.
 * Based on the from header it blocks the call if it founds the address in the XDMS user's blocked list that is provisionned through the HTTP Servlet.
 * 
 * @author Jean Deruelle
 *
 */
@SipListener
@javax.servlet.sip.annotation.SipServlet(loadOnStartup=1)
public class CallBlockingPresenceSipServlet extends SipServlet implements SipServletListener {

	private static Logger logger = Logger.getLogger(CallBlockingPresenceSipServlet.class);
	private XcapClient xcapClient = new XcapClientImpl();
	private Credentials credentials = null;	
	private UriBuilder uriBuilder = null;
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		logger.info("the call blocking presence sip servlet has been started");
		super.init(servletConfig);		
	}

	@Override
	protected void doInvite(SipServletRequest request) throws ServletException,
			IOException {

		logger.info("Got request:\n"
				+ request.toString());
		String fromUri = request.getFrom().getURI().toString();
		String toUri = request.getTo().getURI().toString();
		logger.info("Caller is " + fromUri);
		logger.info("Callee is " + toUri);
	
		String elementSelector = new ElementSelectorBuilder()
	        .appendStepByName("resource-lists")
	        .appendStepByAttr("list","name","blocked")
	        .appendStepByAttr("entry","uri", fromUri)
	        .toPercentEncodedString();
		try {			
			URI elementURI = uriBuilder.setElementSelector(elementSelector).toURI();
			System.out.println("Resource Lists for " + credentials.getUserPrincipal().getName() + ":\n"+ elementURI);
			XcapResponse response = xcapClient.get(elementURI, null, credentials);		
			boolean isBlocked = false;
			System.out.println("Get result for resource lists doc:" + response);
			if(response.getCode() >= 200 && response.getCode() < 300 ) {
				isBlocked = true;
			}
			if(isBlocked) {
				logger.info(fromUri + " has been blocked !");
				SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_FORBIDDEN);
				sipServletResponse.send();		
			} else {
				logger.info(fromUri + " has not been blocked, we proxy the request");
				Proxy proxy = request.getProxy();
				proxy.setRecordRoute(true);
				proxy.proxyTo(request.getRequestURI());
			}
		} catch (URISyntaxException e) {
			logger.error("Couldn't access the XDMS", e);
			request.createResponse(500, "Couldn't access the XDMS").send();
			return;
		}		
	}	

	public void servletInitialized(SipServletContextEvent event) {
		logger.info("the call blocking sip servlet has been initialized");				
		final ServletContext servletContext = event.getServletContext();
		final String username = servletContext.getInitParameter("username");
		final String password =  servletContext.getInitParameter("password");
		
		createUser(username, password, servletContext.getInitParameter("xdms.provider.url"));
				
		final String schemeAndAuth = "http://" + servletContext.getInitParameter("xdms.ip.address") + ":" + servletContext.getInitParameter("xdms.port");
		final String xcapRoot = servletContext.getInitParameter("xdms.xcap.root");
		final String blockedUsers = servletContext.getInitParameter("blocked.users");
		
		try {
			createResourceListContent(schemeAndAuth, xcapRoot, username, password, blockedUsers);
		} catch (Exception e) {
			logger.error("Couldn't provision the XDMS", e);
		}
		
	}
	
	private void createUser(String username, String password, String xdmsProviderUrl) {
		// Provisioning of the user in the XDMS
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.PROVIDER_URL, xdmsProviderUrl);
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                        "org.jnp.interfaces.NamingContextFactory");
        env.put(Context.URL_PKG_PREFIXES, "org.jnp.interfaces");

        try {
	        InitialContext ctx = new InitialContext(env);
	        RMIAdaptor rmiAdaptor = (RMIAdaptor) ctx.lookup("jmx/rmi/RMIAdaptor");
	        ObjectName userProfileMBeanObjectName = new ObjectName("org.mobicents.sippresence:name=UserProfileControl");
	        
	        String users = (String) rmiAdaptor.invoke(userProfileMBeanObjectName, "listUsersAsString", new String[]{}, new String[]{});
	        if(!users.contains(username)) {
	        
		        String sigs[] = { String.class.getName(), String.class.getName() };
		        Object[] args = { username, password };
		        rmiAdaptor.invoke(userProfileMBeanObjectName, "addUser", args, sigs);
		        logger.info("User " + username + " added in the xdms");
	        } else {
	        	logger.info("User " + username + " already present in the xdms");
	        }
        } catch (Exception e) {
        	logger.error("Couldn't provision the user " + username, e);
		}
	}
	
	private void createResourceListContent(String schemeAndAuth, String xcapRoot, String username, String password, String blockedUsers) throws Exception {		
		// create resource lists doc uri
		credentials = xcapClient.getCredentialsFactory().getHttpDigestCredentials(username, password);
		String resourcesListDocumentSelector = DocumentSelectorBuilder.getUserDocumentSelectorBuilder("resource-lists", username, "index").toPercentEncodedString();
		uriBuilder = new UriBuilder()
			.setSchemeAndAuthority(schemeAndAuth)
			.setXcapRoot(xcapRoot)
			.setDocumentSelector(resourcesListDocumentSelector);		
		
		// create resource-list content
		String resourceList =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
			"\n<resource-lists xmlns=\"urn:ietf:params:xml:ns:resource-lists\">" +
				"\n\t<list name=\"blocked\">";
		
		StringTokenizer tokenizer = new StringTokenizer(blockedUsers, ",");
		while (tokenizer.hasMoreTokens()) {
			String blockedUser = tokenizer.nextToken();			
			resourceList += "\n\t\t<entry uri=\""+blockedUser+"\"></entry>";
		}
		resourceList += "\n\t</list>" +
			"\n</resource-lists>";	
		
		// put the doc in the xdms
		System.out.println("Resource Lists doc generated for " + username + ":\n"+resourceList);
		int putResult = xcapClient.put(uriBuilder.toURI(),"application/resource-lists+xml",resourceList,null,credentials).getCode();
		System.out.println("Put result for resource lists doc:"+putResult);
	}
	
	
}