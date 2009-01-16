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
package org.mobicents.servlet.sip.startup;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.TimerService;

import org.apache.AnnotationProcessor;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.session.SipListenersHolder;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.message.SipFactoryFacade;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.startup.loading.SipLoginConfig;
import org.mobicents.servlet.sip.startup.loading.SipServletImpl;
import org.mobicents.servlet.sip.startup.loading.SipServletMapping;

/**
 * A <b>SipContext</b> is a Container that represents a sip/converged servlet context, and
 * therefore an individual sip/converged application, in the Catalina servlet engine.
 *
 * <p>
 * This extends Tomcat Context interface to allow sip capabilities to be used on Tomcat deployed applictions.
 * <p>
 *
 * @author Jean Deruelle
 */
public interface SipContext extends Context {

	public static final String APPLICATION_SIP_XML = "WEB-INF/sip.xml";
	
	public static final String LOAD_BALANCER = "org.mobicents.servlet.sip.LoadBalancer";	
	
	String getApplicationName();

	void setApplicationName(String applicationName);

	String getDescription();
	
	void setDescription(String description);
	
	String getLargeIcon();

	void setLargeIcon(String largeIcon);

	SipListenersHolder getListeners();

	void setListeners(SipListenersHolder listeners);

	String getMainServlet();

	void setMainServlet(String mainServlet);

	int getProxyTimeout();
	
	void setProxyTimeout(int proxyTimeout);
	
	int getSipApplicationSessionTimeout();
	
	void setSipApplicationSessionTimeout(int proxyTimeout);

	void addConstraint(org.apache.catalina.deploy.SecurityConstraint securityConstraint);
	
	void removeConstraint(org.apache.catalina.deploy.SecurityConstraint securityConstraint);
	
	String getSmallIcon();

	void setSmallIcon(String smallIcon);

	void addSipApplicationListener(String listener);
	
	void removeSipApplicationListener(String listener);
	
	String[] findSipApplicationListeners();
	
	Method getSipApplicationKeyMethod();
	
	void setSipApplicationKeyMethod(Method sipApplicationKeyMethod);
	
	void setSipLoginConfig(SipLoginConfig config);
	
	SipLoginConfig getSipLoginConfig();
	
	void addSipServletMapping(SipServletMapping sipServletMapping);
	
	void removeSipServletMapping(SipServletMapping sipServletMapping);
	
	List<SipServletMapping> findSipServletMappings();
	
	SipServletMapping findSipServletMappings(SipServletRequest sipServletRequest);
	
	Map<String, Container> getChildrenMap();
	
	void addChild(SipServletImpl child);
	
	void removeChild(SipServletImpl child);
	
	SipManager getSipManager();
	
	SipApplicationDispatcher getSipApplicationDispatcher();

	AnnotationProcessor getAnnotationProcessor();

	String getEngineName();

	String getBasePath();
	
	boolean notifySipServletsListeners();
	
	void enterSipApp(SipServletRequestImpl request, SipServletResponseImpl response, SipManager manager, boolean startCacheActivity, boolean bindSessions);
	
	void exitSipApp();
	
	SipFactoryFacade getSipFactoryFacade();
	
	SipSessionsUtil getSipSessionsUtil();
	
	TimerService getTimerService();

	void setConcurrencyControlMode(ConcurrencyControlMode mode);
	ConcurrencyControlMode getConcurrencyControlMode();
}