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
package org.mobicents.servlet.sip.annotations;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.xml.ws.WebServiceRef;

import org.apache.catalina.core.DefaultInstanceManager;
import org.mobicents.servlet.sip.catalina.CatalinaSipContext;
import org.mobicents.servlet.sip.catalina.annotations.SipInstanceManager;
import org.mobicents.servlet.sip.core.SipContext;
/**
 * An annotation processor which tries to inject resources found in the servlet context
 * instead of the naming context of the servlet.
 * 
 * @author Vladimir Ralev
 *
 */
public class DefaultSipInstanceManager extends DefaultInstanceManager implements SipInstanceManager {

	private SipContext sipContext;
	private Context context;
	private final Map<String, Map<String, String>> injectionMap;
	
	
	@Override
	public void processAnnotations(Object instance, Map<String, String> injections) 
		throws IllegalAccessException, InvocationTargetException, NamingException {
		if (context == null) {
	            // No resource injection
	            return;
	        }
	        
	        // Initialize fields annotations
	        Field[] fields = instance.getClass().getDeclaredFields();
	        for (int i = 0; i < fields.length; i++) {
	        	//First check against injections
	        	if (injections != null && injections.containsKey(fields[i].getName())) {
                    lookupFieldResource(context, instance, fields[i],
                            injections.get(fields[i].getName()));
                } else if (fields[i].isAnnotationPresent(Resource.class)) {
	                Resource annotation = (Resource) fields[i].getAnnotation(Resource.class);
	                if(!lookupResourceInServletContext(instance, fields[i], annotation.name()))
	                	lookupFieldResource(context, instance, fields[i], annotation.name());
	            } else if (fields[i].isAnnotationPresent(EJB.class)) {
	                EJB annotation = (EJB) fields[i].getAnnotation(EJB.class);
	                lookupFieldResource(context, instance, fields[i], annotation.name());
	            } else if (fields[i].isAnnotationPresent(WebServiceRef.class)) {
	                WebServiceRef annotation = 
	                    (WebServiceRef) fields[i].getAnnotation(WebServiceRef.class);
	                lookupFieldResource(context, instance, fields[i], annotation.name());
	            } else if (fields[i].isAnnotationPresent(PersistenceContext.class)) {
	                PersistenceContext annotation = 
	                    (PersistenceContext) fields[i].getAnnotation(PersistenceContext.class);
	                lookupFieldResource(context, instance, fields[i], annotation.name());
	            } else if (fields[i].isAnnotationPresent(PersistenceUnit.class)) {
	                PersistenceUnit annotation = 
	                    (PersistenceUnit) fields[i].getAnnotation(PersistenceUnit.class);
	                lookupFieldResource(context, instance, fields[i], annotation.name());
	            }
	        }
	}
		
	protected boolean lookupResourceInServletContext(Object instance, Field field, String annotationName) {
		String typeName = field.getType().getCanonicalName();
		if(annotationName == null || annotationName.equals("")) annotationName = typeName;
		Object objectToInject = sipContext.getServletContext().getAttribute(annotationName);
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
        
        String normalizedName = normalize(name);
        
        if ((normalizedName != null) && (normalizedName.length() > 0)) {
            lookedupResource = context.lookup(normalizedName);
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

	public DefaultSipInstanceManager(Context context, Map<String, Map<String, String>> injectionMap, 
			CatalinaSipContext catalinaContext, ClassLoader containerClassLoader) {
		super(context, injectionMap, catalinaContext, containerClassLoader);
		this.context = context;
		this.sipContext = catalinaContext;
		this.injectionMap = injectionMap;
	}

	/**
	 * @param sipContext the sipContext to set
	 */
	public void setSipContext(SipContext sipContext) {
		this.sipContext = sipContext;
	}

	/**
	 * @return the sipContext
	 */
	public SipContext getSipContext() {
		return sipContext;
	}
	
	/**
	 * @param context the context to set
	 */
	public void setContext(Context context) {
		this.context = context;
	}

	/**
	 * @return the context
	 */
	public Context getContext() {
		return context;
	}

	@Override
	public Map<String, String> getInjectionMap(String name){
		if (!(injectionMap==null)){
			return injectionMap.get(name);
		}
		return null;
	}

    private static String normalize(String jndiName){
        if(jndiName != null && jndiName.startsWith("java:comp/env/")){
            return jndiName.substring(14);
        }
        return jndiName;
    }
	
}
