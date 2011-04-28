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
import java.util.HashSet;
import java.util.Set;

import javax.ejb.MessageDriven;
import javax.ejb.Stateful;
import javax.ejb.Stateless;

import org.jboss.ejb3.annotation.Consumer;
import org.jboss.ejb3.annotation.Service;
import org.jboss.logging.Logger;
import org.jboss.metadata.annotation.creator.AbstractCreator;
import org.jboss.metadata.annotation.creator.Creator;
import org.jboss.metadata.annotation.finder.AnnotationFinder;
import org.jboss.metadata.sip.spec.Sip11MetaData;
import org.jboss.metadata.sip.spec.SipMetaData;

/**
 * Create a Sip11MetaData instance from the class annotations
 * based on the Web25MetaDataCreator from JBoss 5
 * 
 * @author jean.deruelle@gmail.com
 */
public class Sip11MetaDataCreator extends AbstractCreator<SipMetaData>
      implements Creator<Collection<Class<?>>, Sip11MetaData>
{
 
   /** The ignore type annotations */
   private static final Set<Class<? extends Annotation>> ignoreTypeAnnotations;
   
   /** The Logger. */
   private static final Logger log = Logger.getLogger(Sip11MetaDataCreator.class);
   
   static
   {
      // Ignoring classes with the the following type annotations
      ignoreTypeAnnotations = new HashSet<Class<? extends Annotation>>();
      ignoreTypeAnnotations.add(Stateful.class);
      ignoreTypeAnnotations.add(Stateless.class);
      ignoreTypeAnnotations.add(MessageDriven.class);
      ignoreTypeAnnotations.add(Service.class);
      ignoreTypeAnnotations.add(Consumer.class);
   }
   
   public Sip11MetaDataCreator(AnnotationFinder<AnnotatedElement> finder)
   {
      super(finder);
      addProcessor(new SipComponentProcessor(finder));      
   }

   public Sip11MetaData create(Collection<Class<?>> classes)
   {
      // Don't create meta data for a empty collection
      if(classes == null || classes.isEmpty())
         return null;
      
      // Create meta data
      Sip11MetaData metaData = create();

      processMetaData(classes, metaData);
      
      return metaData;
   }
   
   protected Sip11MetaData create()
   {
      Sip11MetaData metaData = new Sip11MetaData();
      metaData.setVersion("1.1");
      return metaData;
   }
   
   protected boolean validateClass(Class<?> clazz)
   {
      boolean trace = log.isTraceEnabled();
      for(Class<? extends Annotation> annotation : ignoreTypeAnnotations)
      {
         if(finder.getAnnotation(clazz, annotation) != null)
         {
            if(trace)
               log.trace("won't process class: " + clazz + ", because of the type annotation: "+ annotation);
            return false;
         }
      }
      return true;
   }

}
