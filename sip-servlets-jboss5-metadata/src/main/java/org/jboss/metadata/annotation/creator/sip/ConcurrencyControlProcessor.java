/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

import javax.servlet.sip.annotation.SipServlet;

import org.jboss.metadata.annotation.creator.AbstractFinderUser;
import org.jboss.metadata.annotation.creator.Processor;
import org.jboss.metadata.annotation.creator.ProcessorUtils;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.sip.spec.SipMetaData;
import org.mobicents.servlet.sip.annotation.ConcurrencyControl;

/**
 * Create the correct meta data for a ConcurrencyControl annotation.
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class ConcurrencyControlProcessor extends
		AbstractFinderUser implements
		Processor<SipMetaData, Class<?>> {

	
	public ConcurrencyControlProcessor(AnnotationFinder<AnnotatedElement> finder) {
		super(finder);
	}
	
	/**
	 * Check if the @ConcurrencyControl annotation is present in the package and if so process it 
	 * @param clazz the clazz to check for @ConcurrencyControl annotation - only its package will be checked
	 */
	public void process(SipMetaData sipMetaData, Class<?> beanClass) {		
		if(sipMetaData.getConcurrencyControlMode() == null) {
	    	Package pack = beanClass.getPackage();
	    	if(pack != null) {
				ConcurrencyControl concurrencyControl = pack.getAnnotation(ConcurrencyControl.class);
				if(concurrencyControl != null) {			
					sipMetaData.setConcurrencyControlMode(concurrencyControl.mode());
				}
	    	}
		}
	}
	
	
	public Collection<Class<? extends Annotation>> getAnnotationTypes() {
		return ProcessorUtils.createAnnotationSet(SipServlet.class);
	}

}
