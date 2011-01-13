/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.web.tomcat.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.catalina.Context;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.deployment.MappedReferenceMetaDataResolverDeployer;
import org.jboss.deployment.dependency.ContainerDependencyMetaData;
import org.jboss.deployment.spi.DeploymentEndpointResolver;
import org.jboss.injection.DependsHandler;
import org.jboss.injection.InjectionHandler;
import org.jboss.injection.PersistenceContextHandler;
import org.jboss.injection.PersistenceUnitHandler;
import org.jboss.jpa.resolvers.PersistenceUnitDependencyResolver;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.Environment;
import org.jboss.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.jboss.web.WebApplication;
import org.jboss.web.tomcat.service.injection.ConvergedSipResourceHandler;
import org.jboss.web.tomcat.service.injection.WebEJBHandler;
import org.jboss.web.tomcat.service.injection.WebServiceRefInjectionHandler;

/**
 * Extends the TomcatInjectionContainer to be able to inject SipFactory, TimerService and SipSessionUtils 
 * through @Resource annotation as defined per sip servlet 1.1 specification 
 * 
 * @author jean.deruelle@gmail.com
 * 
 */
public class TomcatConvergedSipInjectionContainer extends
		TomcatInjectionContainer {
	private static final Logger log = Logger.getLogger(TomcatConvergedSipInjectionContainer.class);

	private DeploymentEndpointResolver deploymentEndpointResolver;
	private Map<String, ContainerDependencyMetaData> endpointMap;
	private static final Set<String> dynamicClassLoaders = new HashSet<String>();
	private static final Properties restrictedFilters = new Properties();
	private static final Properties restrictedListeners = new Properties();
	private static final Properties restrictedServlets = new Properties();

	static {
		try {
			InputStream is = TomcatInjectionContainer.class
					.getClassLoader()
					.getResourceAsStream(
							"org/apache/catalina/core/RestrictedServlets.properties");
			if (is != null) {
				restrictedServlets.load(is);
			} else {
				log
						.error("Could not load org/apache/catalina/core/RestrictedServlets.properties");
			}
		} catch (IOException e) {
			log
					.error(
							"Error reading org/apache/catalina/core/RestrictedServlets.properties",
							e);
		}

		try {
			InputStream is = TomcatInjectionContainer.class
					.getClassLoader()
					.getResourceAsStream(
							"org/apache/catalina/core/RestrictedListeners.properties");
			if (is != null) {
				restrictedListeners.load(is);
			} else {
				log
						.error("Could not load org/apache/catalina/core/RestrictedListeners.properties");
			}
		} catch (IOException e) {
			log
					.error(
							"Error reading org/apache/catalina/core/RestrictedListeners.properties",
							e);
		}
		try {
			InputStream is = TomcatInjectionContainer.class
					.getClassLoader()
					.getResourceAsStream(
							"org/apache/catalina/core/RestrictedFilters.properties");
			if (is != null) {
				restrictedFilters.load(is);
			} else {
				log
						.error("Could not load org/apache/catalina/core/RestrictedFilters.properties");
			}
		} catch (IOException e) {
			log
					.error(
							"Error reading org/apache/catalina/core/RestrictedFilters.properties",
							e);
		}

		// 
		dynamicClassLoaders.add("org.apache.jasper.servlet.JasperLoader");
	}

	public TomcatConvergedSipInjectionContainer(WebApplication appInfo,
			DeploymentUnit unit, Context catalinaContext,
			PersistenceUnitDependencyResolver resolver) {
		super(appInfo, unit, catalinaContext, resolver);
		this.webDD = unit.getAttachment(JBossConvergedSipMetaData.class);
	    assert this.webDD != null : "webDD is null (no JBossConvergedSipMetaData attachment in VFSDeploymentUnit)";
		this.deploymentEndpointResolver = unit.getAttachment(DeploymentEndpointResolver.class);
	    this.endpointMap = unit.getTopLevel().getAttachment(MappedReferenceMetaDataResolverDeployer.ENDPOINT_MAP_KEY, Map.class);
	}

	/**
	 * Process the meta data. There is no introspection needed, as the
	 * annotations were already processed. The handlers add the EjbEncInjectors
	 * to encInjectors. Other injectors are added to the encInjections map.
	 * <p/>
	 * This must be called before container is registered with any
	 * microcontainer
	 * 
	 */
	public void processMetadata() {
		// 
		InjectionHandler<Environment> webEjbHandler = new WebEJBHandler<Environment>(
				webDD, deploymentEndpointResolver, endpointMap, unit
						.getRelativePath());

		// todo injection handlers should be pluggable from XML
		handlers = new ArrayList<InjectionHandler<Environment>>();
		handlers.add(webEjbHandler);
		handlers.add(new DependsHandler<Environment>());
		handlers.add(new PersistenceContextHandler<Environment>());
		handlers.add(new PersistenceUnitHandler<Environment>());
		handlers.add(new ConvergedSipResourceHandler<Environment>());
		handlers.add(new WebServiceRefInjectionHandler<Environment>());

		ClassLoader old = Thread.currentThread().getContextClassLoader();
		ClassLoader webLoader = getClassloader();
		Thread.currentThread().setContextClassLoader(webLoader);
		try {
			for (InjectionHandler<Environment> handler : handlers)
				handler.loadXml(webDD.getJndiEnvironmentRefsGroup(), this);
		} finally {
			Thread.currentThread().setContextClassLoader(old);
		}
	}
	
	public Context getCatalinaContext() {
		return catalinaContext;
	}
}
