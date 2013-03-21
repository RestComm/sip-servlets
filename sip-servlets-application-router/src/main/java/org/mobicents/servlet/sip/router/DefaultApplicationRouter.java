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

package org.mobicents.servlet.sip.router;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.ar.SipApplicationRouter;
import javax.servlet.sip.ar.SipApplicationRouterInfo;
import javax.servlet.sip.ar.SipApplicationRoutingDirective;
import javax.servlet.sip.ar.SipApplicationRoutingRegion;
import javax.servlet.sip.ar.SipRouteModifier;
import javax.servlet.sip.ar.SipTargetedRequestInfo;

import org.apache.log4j.Logger;

/**
 * Implementation of the default application router as defined per JSR 289 Appendix C
 * 
 * <p>
 * As an application router component is essential for the functioning of the
 * container the following application router logic SHOULD be available with
 * every container compliant with this specification. The container
 * implementations MAY chose to provide a much richer application router
 * component. For the purpose of this discussion the application router defined
 * in this appendix is termed as Default Application Router (DAR).
 * </p>
 * 
 * <p>
 * The Application Router and the Container have a simple contract defined by
 * the <code>SipApplicationRouter</code> interface. <br />
 * The DAR MUST implement all the methods of that interface as described in this
 * document.
 * <p>
 * <b>The DAR Configuration File</b><br />
 * 
 * The DAR works off a simple configuration text file which is modeled as a Java
 * properties file:
 * 
 * <ul>
 * <li>The properties file MUST be made available to the DAR and the
 * location/content of this file MUST be accessible from a hierarchical URI
 * which itself is to be supplied as a system property "javax.servlet.sip.dar"
 * </li>
 * <li>The propeties file has a simple format in which the name of the property
 * is the SIP method and the value is a simple comma separated stringified value
 * for the SipRouterInfo object.
 * 
 * <pre>
 *       INVITE: (sip-router-info-1), (sip-router-info-2)..
 *       SUBSCRIBE:  (sip-router-info-3), (sip-router-info-4)..
 * </pre>
 * 
 * </li>
 * <li> The properties file is first read by the DAR when the
 * <code>init()</code> is first called on the DAR. The arguments passed in the
 * <code>init()</code> are ignored. </li>
 * 
 * <li> The properties file is refreshed each time
 * <code>applicationDeployed()</code> or <code>applicationUndeployed()</code>
 * is called, similar to init the argument of these two invocations are ignored,
 * these callbacks act just as a trigger to read the file afresh. </li>
 * </ul>
 * </p>
 * <p>
 * The sip-router-info data that goes in the properties file is a stringified
 * version of the SipRouterInfo object. It consists of the following information :
 * <ul>
 * <li>The name of the application as known to the container</li>
 * <li>The identity of the subscriber that the DAR returns. It can return any
 * header in the SIP request using the DAR directive DAR:<i>SIP_HEADER</i> e.g
 * "DAR:From" would return the sip uri in From header. Or alternatively it can
 * return any string.</li>
 * 
 * <li>The routing region, one of the strings "ORIGINATING", "TERMINATING" or
 * "NEUTRAL"</li>
 * <li>A SIP URI indicating the route as returned by the application route, it
 * can be an empty string. </li>
 * <li>A route modifier which can be any one of the strings "ROUTE",
 * "NO_ROUTE", or "CLEAR_ROUTE" </li>
 * <li>A string representing stateInfo. As stateInfo is for application
 * router's internal use only, what goes in this is up to the individual DAR
 * implementations. As a hint the stateInfo could contain the index into the
 * list of sip-router-info that was returned last. </li>
 * </ul>
 * 
 * </p>
 * <p>
 * Following is an example of the DAR configuration file:
 * 
 * <pre>
 *  INVITE: (&quot;OriginatingCallWaiting&quot;, &quot;DAR:From&quot;, &quot;ORIGINATING&quot;, &quot;&quot;,
 *  &quot;NO_ROUTE&quot;, &quot;0&quot;), (&quot;CallForwarding&quot;, &quot;DAR:To&quot;, &quot;TERMINATING&quot;, &quot;&quot;,
 *  NO_ROUTE&quot;, &quot;1&quot;)
 * </pre>
 * 
 * In this example the DAR is setup to invoke two applications on INVITE request
 * one each in the originating and the terminating half. The applications are
 * identified by their names as defined in the application deployment
 * descriptors and used here. The subscriber identity returned in this case is
 * the URI from the From and To header respectively for the two applications.
 * The DAR does not return any route to the container and maintains the
 * invocation state in the stateInfo as the index of the last application in the
 * list.
 * 
 * <p>
 * <b>The DAR Operation</b><br />
 * The key interaction point between the Container and the Application Router is
 * the method
 * 
 * <pre>
 * SipApplicationRouterInfo getNextApplication(SipServletRequest initialRequest,
 * 		SipApplicationRoutingRegion region,
 * 		SipApplicationRoutingDirective directive, java.lang.String stateInfo);
 * </pre>
 * 
 * This method is invoked when an initial request is received by the container.
 * When this method is invoked on DAR it will make use of the stateInfo and the
 * initialRequest parameters and find out what SIP method is in the request.
 * Next it will create the object <code>
 * SipApplicationRouterInfo</code> from
 * the sip-router-info information in the properties file, starting from the
 * first in the list. The stateInfo could contain the index of the last
 * sip-router-info returned so on next invocation of getNextApplication the DAR
 * proceeds to the next sip-router- info in the list. The order of declaration
 * of sip-router-info becomes the priority order of invocation.
 * 
 * <p>
 * As you would notice this is a minimalist application router with no
 * processing logic besides the declaration of the application order. It is
 * expected that in the real world deployments the application router shall play
 * an extremely important role in application orchestration and composition and
 * shall make use of complex rules and diverse data repositories. The DAR is
 * specified as being a very simple implementation that shall be available as
 * part of the reference implementation of this specification and can be used in
 * place of an application router.
 * 
 * </p>
 */
public class DefaultApplicationRouter implements SipApplicationRouter, ManageableApplicationRouter{	
	private static final String DIRECTION_PARAMETER = "DIRECTION";
	private static final String REGEX_PARAMETER = "REGEX";
	private static final String DIRECTION_OUTBOUND = "OUTBOUND";
	private static final String DIRECTION_INBOUND = "INBOUND";
	//	the logger
	private static Logger log = Logger.getLogger(DefaultApplicationRouter.class);
	//the prefix used in the dar configuration file to specify the subscriber URI to use
	//when reusing the information from one header
	private static final String DAR_SUSCRIBER_PREFIX = "DAR:";
	private static final String FROM = "From";
	private static final String TO = "To";
	private static final int DAR_SUSCRIBER_PREFIX_LENGTH = DAR_SUSCRIBER_PREFIX.length();
	private static final String METHOD_WILDCARD = "ALL";
	//the parser for the properties file
	private DefaultApplicationRouterParser defaultApplicationRouterParser;
	//Applications deployed within the container
	Set<String> containerDeployedApplicationNames = null;
	//List of applications defined in the defautl application router properties file
	Map<String, List<? extends SipApplicationRouterInfo>> defaultSipApplicationRouterInfos;
	
	/**
	 * Default Constructor
	 */
	public DefaultApplicationRouter() {
		containerDeployedApplicationNames = new HashSet<String>();
		defaultApplicationRouterParser = new DefaultApplicationRouterParser();
		defaultSipApplicationRouterInfos = new ConcurrentHashMap<String, List<? extends SipApplicationRouterInfo>>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void applicationDeployed(List<String> newlyDeployedApplicationNames) {
		init();
		synchronized (containerDeployedApplicationNames) {
			containerDeployedApplicationNames.addAll(newlyDeployedApplicationNames);	
		}		
	}

	/**
	 * {@inheritDoc}
	 */
	public void applicationUndeployed(List<String> undeployedApplicationNames) {
		init();
		synchronized (containerDeployedApplicationNames) {
			containerDeployedApplicationNames.removeAll(undeployedApplicationNames);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy() {
		synchronized (containerDeployedApplicationNames) {
			containerDeployedApplicationNames.clear();			
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public SipApplicationRouterInfo getNextApplication(
			SipServletRequest initialRequest,
			SipApplicationRoutingRegion region,
			SipApplicationRoutingDirective directive,
			SipTargetedRequestInfo targetedRequestInfo,
			Serializable stateInfo) {		
		// Minimalist application router implementation with no processing logic 
		// besides the declaration of the application order as specified in JSR 289 - Appendix C
		SipApplicationRouterInfo sipApplicationRouterInfo = null;
		if(initialRequest != null) {	
			if(log.isDebugEnabled()) {
				log.debug(this + " checking for next application for request " + initialRequest 
						+ " , region=" + region + " , directive=" + directive + 
						", targetedRequestInfo="+ targetedRequestInfo + ", stateinfo=" + stateInfo + " with following dar " + defaultApplicationRouterParser.getProperties());
			}
			List<? extends SipApplicationRouterInfo> defaultSipApplicationRouterInfoList = 
				defaultSipApplicationRouterInfos.get(initialRequest.getMethod());		
			sipApplicationRouterInfo = getNextApplication(initialRequest, stateInfo,
						defaultSipApplicationRouterInfoList);			
			if(sipApplicationRouterInfo == null) {
				defaultSipApplicationRouterInfoList = 
					defaultSipApplicationRouterInfos.get(METHOD_WILDCARD);
				sipApplicationRouterInfo = getNextApplication(initialRequest, stateInfo,
						defaultSipApplicationRouterInfoList);	
			}
			if(sipApplicationRouterInfo != null) {
				return sipApplicationRouterInfo;
			}
		}
		return new SipApplicationRouterInfo(null,null,null,null,null,null);
	}
	
	/*
	 * This method is checking if the application that initiated the request is currently configured to be called for this method.
	 * Apps that initiate request may not be in the list.
	 */
	private DefaultSipApplicationRouterInfo getFirstRequestApplicationEntry(List<? extends SipApplicationRouterInfo> defaultSipApplicationRouterInfoList, SipServletRequest initialRequest) {
		SipSession sipSession = initialRequest.getSession(false);
		if(sipSession != null) {
			String appName = sipSession.getApplicationSession().getApplicationName();
			Iterator<? extends SipApplicationRouterInfo> iterator = defaultSipApplicationRouterInfoList.iterator();
			while(iterator.hasNext()) {
				DefaultSipApplicationRouterInfo next = (DefaultSipApplicationRouterInfo)iterator.next();
				if(next.getApplicationName().equals(appName)) return next;
			}
		}
		return null;
	}

	private SipApplicationRouterInfo getNextApplication(
			SipServletRequest initialRequest,
			Serializable stateInfo,
			List<? extends SipApplicationRouterInfo> defaultSipApplicationRouterInfoList) {
		
		if(defaultSipApplicationRouterInfoList != null && defaultSipApplicationRouterInfoList.size() > 0) {
			int previousAppOrder = 0; 
			if(stateInfo != null) {					
				previousAppOrder = (Integer) stateInfo;
				if(log.isDebugEnabled()) {
					log.debug("The previous app order was : " + previousAppOrder);
				}
			}
			ListIterator<? extends SipApplicationRouterInfo> defaultSipApplicationRouterInfoIt = defaultSipApplicationRouterInfoList.listIterator(previousAppOrder++);
			while (defaultSipApplicationRouterInfoIt.hasNext() ) {
				
				/*
				 *  Fix for http://code.google.com/p/mobicents/issues/detail?id=987 Issue 987
				 *  
				 *  INBOUND and OUTBOUND request routing logic is as follows:
				 *  Determine if the request was initiated by the previous application by either see the application missing in the DAR
				 *  or by looking at the DIRECTION hint in the optional parameters. If the request was initiated by the app then we will
				 *  call only applications without INBOUND direction. All applications without hint will be called to keep backward compatibility.
				 */
				DefaultSipApplicationRouterInfo defaultSipApplicationRouterInfo = (DefaultSipApplicationRouterInfo) defaultSipApplicationRouterInfoIt.next();
				
				/*
				 * This method is checking if the application that initiated the request is currently configured to be called for this method.
				 * Apps that initiate request may not be in the list thus params must be assumed for them.
				 */
				DefaultSipApplicationRouterInfo requestSipApplicationRouterInfo = (DefaultSipApplicationRouterInfo) getFirstRequestApplicationEntry(defaultSipApplicationRouterInfoList, initialRequest);
				
				String currentDirection = defaultSipApplicationRouterInfo.getOptionalParameters().get(DIRECTION_PARAMETER);
				String requestDirection = null;
				if(requestSipApplicationRouterInfo != null) {
					requestDirection = requestSipApplicationRouterInfo.getOptionalParameters().get(DIRECTION_PARAMETER);
				} else {
					if(initialRequest.getSession(false) != null) {
						// If this request comes from outside (was not initiated by some app) the session will be null,
						// thus if it's not null we can assume the request already has a session and was initiated by
						// the application...
						requestDirection = DIRECTION_OUTBOUND;
					}
				}
				
				if(DIRECTION_OUTBOUND.equalsIgnoreCase(requestDirection)) { // If the request was initiated by the previous app or the previous app has out marker
					if(DIRECTION_INBOUND.equalsIgnoreCase(currentDirection)) { // but the new application is handling only INBOUND request
						if(log.isDebugEnabled()) {
							log.debug(defaultSipApplicationRouterInfo.getApplicationName() + " will not be called because we are routing the request out and the application has 'DIRECTION=INBOUND' hint.");
						}
						continue; // .. then just don't call the application
					}
				}
				
				String regEx = defaultSipApplicationRouterInfo.getOptionalParameters().get(REGEX_PARAMETER);
				if(regEx != null) {
					Pattern pattern = Pattern.compile(regEx);
					Matcher matcher = pattern.matcher(initialRequest.toString());
					if(matcher.find()) {
						if(log.isDebugEnabled()) {
							log.debug("initialRequest " + initialRequest + " matching regex pattern " + regEx +
			                   "begin index " + matcher.start() + " and ending at index " + matcher.end() + " for application " + defaultSipApplicationRouterInfo.getApplicationName());
						}						
					} else {
						if(log.isDebugEnabled()) {
							log.debug("initialRequest " + initialRequest + " not matching regex pattern " + regEx +
			                   " skipping application " + defaultSipApplicationRouterInfo.getApplicationName());
						}
						continue; // pattern not matching, just don't call the application
					}
				}
				
				boolean isApplicationPresentInContainer = false;
				synchronized (containerDeployedApplicationNames) {
					if(containerDeployedApplicationNames.contains(defaultSipApplicationRouterInfo.getApplicationName())) {
						isApplicationPresentInContainer = true;
						if(log.isDebugEnabled()) {
							log.debug(defaultSipApplicationRouterInfo.getApplicationName() + " is present in the container.");
						}
					}
				}
				if(log.isDebugEnabled()) {
					log.debug("Route Modifier : " + defaultSipApplicationRouterInfo.getRouteModifier());
				}
				//if application is deployed in the container or if the intention is to route outside even if the application is not deployed
				if(isApplicationPresentInContainer || !SipRouteModifier.NO_ROUTE.equals(defaultSipApplicationRouterInfo.getRouteModifier())) {
					//prevents to route to the same application twice in a row
					if(initialRequest.getSession(false) == null || 
									!defaultSipApplicationRouterInfo.getApplicationName().equals(
											initialRequest.getSession(false).getApplicationSession().getApplicationName())) {
						String subscriberIdentity = defaultSipApplicationRouterInfo.getSubscriberIdentity();
						if(subscriberIdentity.indexOf(DAR_SUSCRIBER_PREFIX) != -1) {
							String headerName = subscriberIdentity.substring(
									DAR_SUSCRIBER_PREFIX_LENGTH);
							if(FROM.equalsIgnoreCase(headerName)) {
								subscriberIdentity = initialRequest.getFrom().getURI().toString();
							} else if(TO.equalsIgnoreCase(headerName)) {
								subscriberIdentity = initialRequest.getTo().getURI().toString();
							} else {
								subscriberIdentity = initialRequest.getHeader(headerName);
							}							
						}
						return new SipApplicationRouterInfo(
								defaultSipApplicationRouterInfo.getApplicationName(),
								defaultSipApplicationRouterInfo.getRoutingRegion(), 
								subscriberIdentity,
								defaultSipApplicationRouterInfo.getRoutes(),
								defaultSipApplicationRouterInfo.getRouteModifier(),
								defaultSipApplicationRouterInfo.getOrder());					
					}
				}
			}
		}
		return null;
	}

	/**
	 * load the configuration file as defined in appendix C of JSR289
	 */
	public void init() {		
		defaultApplicationRouterParser.init();		
		try {
			defaultSipApplicationRouterInfos = defaultApplicationRouterParser.parse();
		} catch (ParseException e) {
			log.fatal("Impossible to parse the default application router configuration file",e);
			throw new IllegalArgumentException("Impossible to parse the default application router configuration file",e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ar.SipApplicationRouter#init(java.util.Properties)
	 */
	public void init(Properties properties) {
		init();		
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.router.ManageableApplicationRouter#configure(java.lang.Object)
	 */
	public synchronized void configure(Object configuration) {
		
		if(!(configuration instanceof Properties) && !(configuration.toString() instanceof String)) 
			throw new IllegalArgumentException("Configuration for DAR must be of type Properties or String; " + configuration.getClass().getName() + " was received");
		
		Properties properties = new Properties();
		if(configuration instanceof String) {
			try {
				properties.load(new StringReader((String)configuration));
			} catch (IOException e) {
				throw new IllegalArgumentException(e);
			}
		} else if(configuration instanceof Properties) {
			properties = (Properties) configuration;
		}
		try {
			defaultSipApplicationRouterInfos = 
				this.defaultApplicationRouterParser.parse(properties);
		} catch (ParseException e1) {
			throw new IllegalArgumentException("Failed to parse the new DAR properties", e1);
		}
		
		String configFileLocation = defaultApplicationRouterParser.getDarConfigurationFileLocation();
		
		if(configFileLocation.startsWith("file:/")) {
			int i = 5;
			while(configFileLocation.charAt(i) == '/') i++;
			configFileLocation = configFileLocation.substring(i-1);
		} else {
			log.warn("Can not write persist DAR configuration to " + configFileLocation + ". Make sure you have write permissions and make sure it's a local file. NOTE THAT THE NEW DAR CONFIGURATION IS LOADED AND EFFECTIVE.");
			return;
		}
		
		if(configFileLocation == null || configFileLocation.length()<1) 
			throw new IllegalStateException("Configuration file name is empty.");
		
		File configFile = new File(configFileLocation);
		FileOutputStream fis = null;
		try {
			fis = new FileOutputStream(configFile);
			properties.store(fis, "Application Router Configuration");
		} catch (Exception e) {
			throw new IllegalStateException("Failed to store configuration file.", e);
		} finally {			
			if(fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					log.error("fail to close the following file " + configFile.getAbsolutePath(), e);
				}
			}
		}
		
		log.info("Stored DAR configuration in " + configFile.getAbsolutePath());
	}

	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.router.ManageableApplicationRouter#getCurrentConfiguration()
	 */
	public synchronized Object getCurrentConfiguration() {
		return defaultApplicationRouterParser.getProperties();
	}
	
	/* (non-Javadoc)
	 * @see org.mobicents.servlet.sip.router.ManageableApplicationRouter#getCurrentConfiguration()
	 */
	public synchronized Map<String, List<? extends SipApplicationRouterInfo>> getConfiguration() {
		return defaultSipApplicationRouterInfos;
	}

}
