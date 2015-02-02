/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
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

package org.mobicents.servlet.sip.core;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.TimerService;

import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.descriptor.MobicentsSipServletMapping;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletResponse;
import org.mobicents.servlet.sip.core.security.MobicentsSipLoginConfig;
import org.mobicents.servlet.sip.core.security.SipDigestAuthenticator;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSessionsUtil;
import org.mobicents.servlet.sip.core.timers.ProxyTimerService;
import org.mobicents.servlet.sip.core.timers.SipApplicationSessionTimerService;
import org.mobicents.servlet.sip.ruby.SipRubyController;

/**
 * A <b>SipContext</b> is a Container that represents a sip/converged servlet context, and
 * therefore an individual sip/converged application, in the Catalina servlet engine.
 *
 * <p>
 * This is used to by example extends Tomcat Context interface to allow sip capabilities to be used on Tomcat deployed applications.
 * <p>
 *
 * @author Jean Deruelle
 */
public interface SipContext {

	public static final String APPLICATION_SIP_XML = "WEB-INF/sip.xml";
	
	public static final String LOAD_BALANCER = "org.mobicents.servlet.sip.LoadBalancer";	
	
	String getApplicationName();
	String getApplicationNameHashed();

	boolean hasDistributableManager();
	
	void setApplicationName(String applicationName);

	String getDescription();
	
	void setDescription(String description);
	
	String getLargeIcon();

	void setLargeIcon(String largeIcon);

	SipListeners getListeners();

	void setListeners(SipListeners listeners);

	boolean isMainServlet();
	
	String getMainServlet();

	void setMainServlet(String mainServlet);
	
	void setServletHandler(String servletHandler);
	String getServletHandler();

	int getProxyTimeout();
	
	void setProxyTimeout(int proxyTimeout);
	
	int getSipApplicationSessionTimeout();
	
	void setSipApplicationSessionTimeout(int proxyTimeout);	
	
	String getSmallIcon();

	void setSmallIcon(String smallIcon);

	void addSipApplicationListener(String listener);
	
	void removeSipApplicationListener(String listener);
	
	String[] findSipApplicationListeners();
	
	Method getSipApplicationKeyMethod();
	
	void setSipApplicationKeyMethod(Method sipApplicationKeyMethod);
	
	void setSipLoginConfig(MobicentsSipLoginConfig config);
	
	MobicentsSipLoginConfig getSipLoginConfig();
	
	void addSipServletMapping(MobicentsSipServletMapping sipServletMapping);
	
	void removeSipServletMapping(MobicentsSipServletMapping sipServletMapping);
	
	List<MobicentsSipServletMapping> findSipServletMappings();
	
	MobicentsSipServletMapping findSipServletMappings(SipServletRequest sipServletRequest);
		
	SipManager getSipManager();
	
	SipApplicationDispatcher getSipApplicationDispatcher();	

	String getEngineName();

	boolean notifySipContextListeners(SipContextEvent event);
	
	/**
	 * notify the application that we are going to access it with the sipapplicationsession and sip session in parameter and tells whether or not to check
	 * if we are in a managed thread to check if we should lock the session or not
	 * @param sipApplicationSession the sip application session that is accessing the application, it can be null
	 * @param sipSession the sip session that is accessing the application, it can be null
	 * @param checkIsManagedThread need to check if the access is done within a managed Thread or not to lock or not the session depending on the concurrency control 
	 */
	void enterSipApp(MobicentsSipApplicationSession sipApplicationSession, MobicentsSipSession sipSession, boolean checkIsManagedThread, boolean isContainerManaged);
	/**
	 * notify the application that we are going to exit it with the sipapplicationsession and sip session in parameter 
	 * @param sipApplicationSession the sip application session that is exiting the application, it can be null
	 * @param sipSession the sip session that is exiting the application, it can be null
	 * 
	 */
	void exitSipApp(MobicentsSipApplicationSession sipApplicationSession, MobicentsSipSession sipSession);
	
	//Issue http://code.google.com/p/mobicents/issues/detail?id=2452
	// Returning boolean vlaue and new parameter batchStarted to decide whether or not to end the batch
	boolean enterSipAppHa(boolean startCacheActivity);
	void exitSipAppHa(MobicentsSipServletRequest request, MobicentsSipServletResponse response, boolean batchStarted);
	
	SipFactory getSipFactoryFacade();
	
	MobicentsSipSessionsUtil getSipSessionsUtil();
	
	TimerService getTimerService();	
	ProxyTimerService getProxyTimerService();
	SipApplicationSessionTimerService getSipApplicationSessionTimerService();

	void setConcurrencyControlMode(ConcurrencyControlMode mode);
	ConcurrencyControlMode getConcurrencyControlMode();

	void setSipRubyController(SipRubyController rubyController);
	SipRubyController getSipRubyController();
	
	ServletContext getServletContext();
	String getPath();
	MobicentsSipServlet findSipServletByClassName(String canonicalName);
	MobicentsSipServlet findSipServletByName(String name);
	
	ClassLoader getSipContextClassLoader();
	Map<String, MobicentsSipServlet> getChildrenMap();
	
	boolean isPackageProtectionEnabled();
	
	boolean authorize(MobicentsSipServletRequest request);
	SipDigestAuthenticator getDigestAuthenticator();

	// http://code.google.com/p/sipservlets/issues/detail?id=135
	void enterSipContext();
	void exitSipContext(ClassLoader oldClassLoader);
}
