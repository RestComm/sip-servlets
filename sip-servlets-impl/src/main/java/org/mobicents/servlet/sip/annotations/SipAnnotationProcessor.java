package org.mobicents.servlet.sip.annotations;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.xml.ws.WebServiceRef;

import org.apache.catalina.util.DefaultAnnotationProcessor;
import org.mobicents.servlet.sip.startup.SipStandardContext;
/**
 * An annotation processor which tries to inject resources found in the servlet context
 * instead of the naming context of the servlet.
 * 
 * @author Vladimir Ralev
 *
 */
public class SipAnnotationProcessor extends DefaultAnnotationProcessor {

	private SipStandardContext sipContext;
	
	@Override
	public void processAnnotations(Object instance) throws IllegalAccessException, InvocationTargetException, NamingException {
	      if (context == null) {
	            // No resource injection
	            return;
	        }
	        
	        // Initialize fields annotations
	        Field[] fields = instance.getClass().getDeclaredFields();
	        for (int i = 0; i < fields.length; i++) {
	            if (fields[i].isAnnotationPresent(Resource.class)) {
	                Resource annotation = (Resource) fields[i].getAnnotation(Resource.class);
	                if(!lookupResourceInServletContext(instance, fields[i], annotation.name()))
	                	lookupFieldResource(context, instance, fields[i], annotation.name());
	            }
	            if (fields[i].isAnnotationPresent(EJB.class)) {
	                EJB annotation = (EJB) fields[i].getAnnotation(EJB.class);
	                lookupFieldResource(context, instance, fields[i], annotation.name());
	            }
	            if (fields[i].isAnnotationPresent(WebServiceRef.class)) {
	                WebServiceRef annotation = 
	                    (WebServiceRef) fields[i].getAnnotation(WebServiceRef.class);
	                lookupFieldResource(context, instance, fields[i], annotation.name());
	            }
	            if (fields[i].isAnnotationPresent(PersistenceContext.class)) {
	                PersistenceContext annotation = 
	                    (PersistenceContext) fields[i].getAnnotation(PersistenceContext.class);
	                lookupFieldResource(context, instance, fields[i], annotation.name());
	            }
	            if (fields[i].isAnnotationPresent(PersistenceUnit.class)) {
	                PersistenceUnit annotation = 
	                    (PersistenceUnit) fields[i].getAnnotation(PersistenceUnit.class);
	                lookupFieldResource(context, instance, fields[i], annotation.name());
	            }
	        }
	}
	
	protected boolean lookupResourceInServletContext(Object instance, Field field, String annotationName) {
		String typeName = field.getType().getCanonicalName();
		if(annotationName == null || annotationName.equals("")) annotationName = typeName;
		Object objectToInject = sipContext.getServletContext().getAttribute(typeName);
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
	        

	public SipAnnotationProcessor(Context context, SipStandardContext sipContext) {
		super(context);
		this.sipContext = sipContext;
	}

}
