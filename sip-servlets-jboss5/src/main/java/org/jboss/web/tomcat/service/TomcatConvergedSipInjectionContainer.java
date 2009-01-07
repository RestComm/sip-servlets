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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.annotation.Resource;
import javax.naming.NamingException;
import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.apache.catalina.Context;
import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.jpa.resolvers.PersistenceUnitDependencyResolver;
import org.jboss.logging.Logger;
import org.jboss.web.WebApplication;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class TomcatConvergedSipInjectionContainer extends
		TomcatInjectionContainer {
	private static final Logger log = Logger.getLogger(TomcatConvergedSipInjectionContainer.class);

//	private DeploymentEndpointResolver deploymentEndpointResolver;
//	private Map<String, ContainerDependencyMetaData> endpointMap;
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
			VFSDeploymentUnit unit, Context catalinaContext,
			PersistenceUnitDependencyResolver resolver) {
		super(appInfo, unit, catalinaContext, resolver);
//		this.deploymentEndpointResolver = unit.getAttachment(DeploymentEndpointResolver.class);
//	    this.endpointMap = unit.getTopLevel().getAttachment(MappedReferenceMetaDataResolverDeployer.ENDPOINT_MAP_KEY, Map.class);
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
//	public void processMetadata() {
//		// 
//		InjectionHandler<Environment> webEjbHandler = new WebEJBHandler<Environment>(
//				webDD, deploymentEndpointResolver, endpointMap, unit
//						.getRelativePath());
//
//		// todo injection handlers should be pluggable from XML
//		handlers = new ArrayList<InjectionHandler<Environment>>();
//		handlers.add(webEjbHandler);
//		handlers.add(new DependsHandler<Environment>());
//		handlers.add(new PersistenceContextHandler<Environment>());
//		handlers.add(new PersistenceUnitHandler<Environment>());
//		handlers.add(new ConvergedSipResourceHandler<Environment>());
//		handlers.add(new WebServiceRefInjectionHandler<Environment>());
//
//		ClassLoader old = Thread.currentThread().getContextClassLoader();
//		ClassLoader webLoader = getClassloader();
//		Thread.currentThread().setContextClassLoader(webLoader);
//		try {
//			for (InjectionHandler<Environment> handler : handlers)
//				handler.loadXml(webDD.getJndiEnvironmentRefsGroup(), this);
//		} finally {
//			Thread.currentThread().setContextClassLoader(old);
//		}
//	}
	
	public Object newInstance(String className) throws IllegalAccessException,
			InvocationTargetException, NamingException, InstantiationException,
			ClassNotFoundException {
		ClassLoader loader = catalinaContext.getLoader().getClassLoader();
		Class<?> clazz = loader.loadClass(className);
		checkAccess(clazz);
		Object instance = clazz.newInstance();
		processInjectors(instance);
		if (!catalinaContext.getIgnoreAnnotations()) {
			processDynamicBeanAnnotations(instance);
			postConstruct(instance);
			processSipResources(instance);
		}
		return instance;
	}

	public Object newInstance(String className, ClassLoader classLoader)
			throws IllegalAccessException, InvocationTargetException,
			NamingException, InstantiationException, ClassNotFoundException {
		Class<?> clazz = classLoader.loadClass(className);
		checkAccess(clazz);
		Object instance = clazz.newInstance();
		processInjectors(instance);
		if (!catalinaContext.getIgnoreAnnotations()) {
			processDynamicBeanAnnotations(instance);
			postConstruct(instance);
			processSipResources(instance);
		}
		return instance;
	}

	public void newInstance(Object instance) throws IllegalAccessException,
			InvocationTargetException, NamingException {
		processInjectors(instance);
		if (!catalinaContext.getIgnoreAnnotations()) {
			processDynamicBeanAnnotations(instance);
			postConstruct(instance);
			processSipResources(instance);
		}
	}
	
	protected void processSipResources(Object instance) throws NamingException, IllegalAccessException {
		// Initialize fields annotations
        Field[] fields = instance.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            if (fields[i].isAnnotationPresent(Resource.class)) {
                Resource annotation = (Resource) fields[i].getAnnotation(Resource.class);
                if(!lookupResourceInServletContext(instance, fields[i], annotation.name()))
                	lookupFieldResource(getEnc(), instance, fields[i], annotation.name());
            }
        }
	}
	
	protected boolean lookupResourceInServletContext(Object instance, Field field, String annotationName) {
		String typeName = field.getType().getCanonicalName();
		if(annotationName == null || annotationName.equals("")) annotationName = typeName;
		Object objectToInject = catalinaContext.getServletContext().getAttribute(annotationName);
		if(objectToInject != null &&
				field.getType().isAssignableFrom(objectToInject.getClass())) {
			boolean accessibility = false;
			accessibility = field.isAccessible();
			field.setAccessible(true);
			try {
				field.set(instance, objectToInject);
			} catch (IllegalArgumentException e) {
				throw e;
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			field.setAccessible(accessibility);
			return true;
		}
		return false;
	}
	   
	/**
     * Inject resources in specified field.
     */
    protected static void lookupFieldResource(javax.naming.Context context, 
            Object instance, Field field, String name)
        throws NamingException, IllegalAccessException {
    
        Object lookedupResource = null;
        boolean accessibility = false;
        
        if ((name != null) &&
                (name.length() > 0)) {
            lookedupResource = context.lookup(name);
        } else {
        	if(field.getClass().getName().startsWith("javax.servlet.sip")) {
        		lookedupResource = context.lookup("sip/" + instance.getClass().getName() + "/" + field.getName());
        	} else {
        		lookedupResource = context.lookup(instance.getClass().getName() + "/" + field.getName());
        	}
        }
        
        accessibility = field.isAccessible();
        field.setAccessible(true);
        field.set(instance, lookedupResource);
        field.setAccessible(accessibility);
    }

	private void checkAccess(Class<?> clazz) {
		if (catalinaContext.getPrivileged())
			return;
		if (Filter.class.isAssignableFrom(clazz)) {
			checkAccess(clazz, restrictedFilters);
		} else if (Servlet.class.isAssignableFrom(clazz)) {
			checkAccess(clazz, restrictedServlets);
		} else {
			checkAccess(clazz, restrictedListeners);
		}
	}

	private void checkAccess(Class<?> clazz, Properties restricted) {
		while (clazz != null) {
			if ("restricted".equals(restricted.getProperty(clazz.getName()))) {
				throw new SecurityException("Restricted class: "
						+ clazz.getName());
			}
			clazz = clazz.getSuperclass();
		}
	}
}
