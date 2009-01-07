package org.jboss.metadata.annotation.creator.sip;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipApplicationKey;
import javax.servlet.sip.annotation.SipServlet;

import org.jboss.metadata.annotation.creator.AbstractFinderUser;
import org.jboss.metadata.annotation.creator.Processor;
import org.jboss.metadata.annotation.creator.ProcessorUtils;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.sip.spec.SipMetaData;

public class SipApplicationKeyProcessor extends AbstractFinderUser implements
		Processor<SipMetaData, Method> {

	public SipApplicationKeyProcessor(AnnotationFinder<AnnotatedElement> finder) {
		super(finder);
	}
	
	public void process(SipMetaData sipMetaData, Method method) {
		SipApplicationKey sipApplicationKey = finder.getAnnotation(method, SipApplicationKey.class);
		if (sipApplicationKey == null)
			return ;
		
		if(!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())) {
			throw new IllegalStateException(
					"A method annotated with the @SipApplicationKey annotation MUST be public and static");
		}
		if(!method.getGenericReturnType().equals(String.class)) {
			throw new IllegalStateException(
					"A method annotated with the @SipApplicationKey annotation MUST return a String");
		}
		Type[] types = method.getGenericParameterTypes();
		if(types.length != 1 || !types[0].equals(SipServletRequest.class)) {
			throw new IllegalStateException(
					"A method annotated with the @SipApplicationKey annotation MUST have a single argument of type SipServletRequest");
		}
		
		if(sipMetaData.getSipApplicationKeyMethod() != null) {
			throw new IllegalStateException(
				"More than one SipApplicationKey annotated method is not allowed.");
		}
		
		sipMetaData.setSipApplicationKeyMethod(method);
		
	}

	public Collection<Class<? extends Annotation>> getAnnotationTypes() {
		return ProcessorUtils.createAnnotationSet(SipApplicationKey.class);
	}

}
