/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors as indicated
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
package org.jboss.metadata.annotation.creator.sip;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Collection;

import javax.servlet.sip.annotation.SipApplication;
import javax.servlet.sip.annotation.SipServlet;

import org.jboss.metadata.annotation.creator.AbstractFinderUser;
import org.jboss.metadata.annotation.creator.Processor;
import org.jboss.metadata.annotation.creator.ProcessorUtils;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.javaee.spec.EmptyMetaData;
import org.jboss.metadata.sip.spec.ProxyConfigMetaData;
import org.jboss.metadata.sip.spec.ServletSelectionMetaData;
import org.jboss.metadata.sip.spec.SipMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.metadata.web.spec.ServletsMetaData;
import org.jboss.metadata.web.spec.SessionConfigMetaData;

/**
 * Create the correct meta data for a SipServlet annotation.
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class SipServletProcessor extends
		AbstractFinderUser implements
		Processor<SipMetaData, Class<?>> {

	private String parsedAnnotatedPackage = null;
	
	public SipServletProcessor(AnnotationFinder<AnnotatedElement> finder) {
		super(finder);
	}
	
	public void process(SipMetaData sipMetaData, Class<?> beanClass) {		
		SipServlet servlet = finder.getAnnotation(beanClass, SipServlet.class);
		if (servlet == null)
			return ;
		
		ServletMetaData beanMetaData = create(beanClass, servlet);
		if(beanMetaData == null)
	         return; 						
		
		ServletsMetaData servletsMetaData = sipMetaData.getSipServlets(); 
		if(servletsMetaData == null) {
			servletsMetaData = new ServletsMetaData();
			sipMetaData.setSipServlets(servletsMetaData);			
		}
		servletsMetaData.add(beanMetaData);
		if(sipMetaData.getServletSelection() == null) {
			sipMetaData.setServletSelection(new ServletSelectionMetaData());
		}
		if (sipMetaData.getServletSelection().getMainServlet() == null
				|| sipMetaData.getServletSelection().getMainServlet().equals("")) {
			sipMetaData.getServletSelection().setMainServlet(beanMetaData.getServletName());
		}
		
		//parsing sipapplication annotation
		String applicationName = servlet.applicationName();
		if (applicationName == null
				|| applicationName.equals("")) {
			// String packageName = clazz.getCanonicalName().substring(0,
			// clazz.getCanonicalName().lastIndexOf('.'));
			// Wasted a whole day watching this line...
			Package pack = beanClass.getPackage();
			String packageName = pack.getName();

			SipApplication appData = getApplicationAnnotation(pack);
			if (appData != null) {
				if (this.parsedAnnotatedPackage != null
						&& !this.parsedAnnotatedPackage.equals(packageName)) {
					throw new IllegalStateException(
							"Cant have two different applications in a single context - "
									+ packageName + " and "
									+ this.parsedAnnotatedPackage);
				}
				
				if (this.parsedAnnotatedPackage == null) {
					this.parsedAnnotatedPackage = packageName;
					parseSipApplication(sipMetaData, appData, packageName);
				}
			}
		}
	}
	
	public ServletMetaData create(Class<?> beanClass, SipServlet servlet) {		
		String name = servlet.name();
		if (name == null || name.equals("")) {
			name = beanClass.getSimpleName(); // if no name is specified deduce from the classname
		} 
		
		ServletMetaData servletMetaData = new ServletMetaData();
		servletMetaData.setName(name);		
		servletMetaData.setServletName(name);
		servletMetaData.setServletClass(beanClass.getCanonicalName());
		servletMetaData.setLoadOnStartup(servlet.loadOnStartup());
		
		return servletMetaData;
	}	

	private void parseSipApplication(SipMetaData sipMetaData, SipApplication appData, String packageName) {
		if(appData.mainServlet() != null && appData.mainServlet().length() > 0) {
			sipMetaData.getServletSelection().setMainServlet(appData.mainServlet());
		}
		if(sipMetaData.getProxyConfig() == null) {
			sipMetaData.setProxyConfig(new ProxyConfigMetaData());
		}
		sipMetaData.getProxyConfig().setProxyTimeout(appData.proxyTimeout());
		if(sipMetaData.getSessionConfig() == null) {
			sipMetaData.setSessionConfig(new SessionConfigMetaData());
		}
    	sipMetaData.getSessionConfig().setSessionTimeout(appData.sessionTimeout());
    	
    	if(appData.name() == null || appData.name().equals(""))
    		sipMetaData.setApplicationName(packageName);
    	else
    		sipMetaData.setApplicationName(appData.name());
    	
//    	if(appData.displayName() == null || appData.displayName().equals(""))
//    		sipMetaData.setDisplayName(packageName);
//    	else
//    		sipMetaData.setDisplayName(appData.displayName());
//    	if(logger.isDebugEnabled()) {
//			logger.debug("the following @SipApplication annotation has been found : ");
//			logger.debug("ApplicationName : " + sipMetaData.getApplicationName());
//			logger.debug("MainServlet : " + sipMetaData.getMainServlet());
//		}
//    	sipMetaData.setDescription(appData.description());
//    	sipMetaData.setLargeIcon(appData.largeIcon());
//    	sipMetaData.setSmallIcon(appData.smallIcon());
    	if(appData.distributable()) {
    		sipMetaData.setDistributable(new EmptyMetaData());
    	}
    }
	
	public static SipApplication getApplicationAnnotation(Package pack) {
    	if(pack == null) return null;
    	
    	SipApplication sipApp = (SipApplication) pack.getAnnotation(SipApplication.class);
    	if(sipApp != null) {
    		return sipApp;
    	}
    	return null;
    }
	
	public Collection<Class<? extends Annotation>> getAnnotationTypes() {
		return ProcessorUtils.createAnnotationSet(SipServlet.class);
	}

}
