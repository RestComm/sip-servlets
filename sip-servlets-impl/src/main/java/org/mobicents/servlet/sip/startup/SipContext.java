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
import javax.servlet.sip.TimerService;

import org.apache.AnnotationProcessor;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.core.SipContextEvent;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipSession;
import org.mobicents.servlet.sip.core.session.SipListenersHolder;
import org.mobicents.servlet.sip.core.session.SipManager;
import org.mobicents.servlet.sip.core.session.SipSessionsUtilImpl;
import org.mobicents.servlet.sip.message.SipFactoryFacade;
import org.mobicents.servlet.sip.message.SipServletRequestImpl;
import org.mobicents.servlet.sip.message.SipServletResponseImpl;
import org.mobicents.servlet.sip.ruby.SipRubyController;
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
	String getApplicationNameHashed();

	boolean hasDistributableManager();
	
	void setApplicationName(String applicationName);

	String getDescription();
	
	void setDescription(String description);
	
	String getLargeIcon();

	void setLargeIcon(String largeIcon);

	SipListenersHolder getListeners();

	void setListeners(SipListenersHolder listeners);

	boolean isMainServlet();
	
	String getMainServlet();

	void setMainServlet(String mainServlet);
	
	void setServletHandler(String servletHandler);
	String getServletHandler();

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
	public Container findChildrenByName(String name);	
	Container findChildrenByClassName(String className);
	
	void addChild(SipServletImpl child);
	
	void removeChild(SipServletImpl child);
	
	SipManager getSipManager();
	
	SipApplicationDispatcher getSipApplicationDispatcher();

	AnnotationProcessor getAnnotationProcessor();

	String getEngineName();

	String getBasePath();
	
	boolean notifySipContextListeners(SipContextEvent event);
	
//	void enterSipApp(SipServletRequestImpl request, SipServletResponseImpl response);
//	void exitSipApp(SipServletRequestImpl request, SipServletResponseImpl response);
	
	void enterSipApp(MobicentsSipApplicationSession sipApplicationSession, MobicentsSipSession sipSession);
	void exitSipApp(MobicentsSipApplicationSession sipApplicationSession, MobicentsSipSession sipSession);
	
//	void enterSipAppHa(MobicentsSipApplicationSession sipApplicationSession, boolean startCacheActivity, boolean bindSessions);
	void enterSipAppHa(boolean startCacheActivity);
//	void enterSipAppHa(SipServletRequestImpl request, SipServletResponseImpl response, boolean startCacheActivity, boolean bindSessions);
	void exitSipAppHa(SipServletRequestImpl request, SipServletResponseImpl response);
	
	SipFactoryFacade getSipFactoryFacade();
	
	SipSessionsUtilImpl getSipSessionsUtil();
	
	TimerService getTimerService();
	
	SipApplicationSessionTimerService getSipApplicationSessionTimerService();

	void setConcurrencyControlMode(ConcurrencyControlMode mode);
	ConcurrencyControlMode getConcurrencyControlMode();

	void setSipRubyController(SipRubyController rubyController);
	SipRubyController getSipRubyController();
}