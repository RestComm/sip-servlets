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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.sip.annotation.SipListener;

import org.jboss.metadata.annotation.creator.AbstractFinderUser;
import org.jboss.metadata.annotation.creator.Processor;
import org.jboss.metadata.annotation.creator.ProcessorUtils;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.sip.spec.SipMetaData;
import org.jboss.metadata.sip.spec.ListenerMetaData;

/**
 * Create the correct meta data for a SipListener annotation.
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class SipListenerProcessor extends AbstractFinderUser
	implements Processor<SipMetaData, Class<?>> {	

	public SipListenerProcessor(AnnotationFinder<AnnotatedElement> finder) {
		super(finder);
	}
	
	public void process(SipMetaData sipMetaData, Class<?> beanClass) {
		ListenerMetaData beanMetaData = create(beanClass);
		if(beanMetaData == null)
	         return; 
		List<ListenerMetaData> listenersMetaData = sipMetaData.getListeners(); 
		if(listenersMetaData == null) {
			listenersMetaData = new ArrayList<ListenerMetaData>();
			sipMetaData.setListeners(listenersMetaData);			
		}
		listenersMetaData.add(beanMetaData);
	}
	
	public ListenerMetaData create(Class<?> beanClass) {
		SipListener annotation = finder.getAnnotation(beanClass, SipListener.class);
		if (annotation == null)
			return null;

		ListenerMetaData listenerMetaData = new ListenerMetaData();
		listenerMetaData.setListenerClass(beanClass.getCanonicalName());
		
		return listenerMetaData;
	}	

	public Collection<Class<? extends Annotation>> getAnnotationTypes() {
		return ProcessorUtils.createAnnotationSet(SipListener.class);
	}


}
