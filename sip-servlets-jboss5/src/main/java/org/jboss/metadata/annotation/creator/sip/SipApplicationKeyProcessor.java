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
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipApplicationKey;

import org.jboss.metadata.annotation.creator.AbstractFinderUser;
import org.jboss.metadata.annotation.creator.Processor;
import org.jboss.metadata.annotation.creator.ProcessorUtils;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.sip.spec.SipMetaData;

/**
 * Create the correct meta data for a SipApplicationKey annotation.
 * 
 * @author jean.deruelle@gmail.com
 *
 */
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
