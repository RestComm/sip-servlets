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
package org.jboss.metadata.sip.spec;

import org.jboss.metadata.sip.spec.MessageDestinationMetaData;
import org.jboss.metadata.javaee.support.AbstractMappedMetaData;
import org.jboss.metadata.javaee.support.JavaEEMetaDataUtil;
import org.jboss.metadata.merge.MergeUtil;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class MessageDestinationsMetaData extends AbstractMappedMetaData<MessageDestinationMetaData>
{
	   /** The serialVersionUID */
	   private static final long serialVersionUID = -6198704374773701253L;

	   /**
	   /**
	    * Merge the contents of override with original into a new MessageDestinationsMetaData.
	    * @param override - metadata augmenting overriden
	    * @param overriden - the base metadata
	    * @param overridenFile - the source of the override destinations
	    * @param overrideFile- the source of the overriden destinations
	    * @return a new merged MessageDestinationsMetaData if either
	    * override and overriden is not null, null otherwise.
	    */
	   public static MessageDestinationsMetaData merge(MessageDestinationsMetaData override,
	         MessageDestinationsMetaData overriden,
	         String overridenFile, String overrideFile)
	   {
	      if (override == null && overriden == null)
	         return null;
	      
	      if (override == null)
	         return overriden;
	      
	      MessageDestinationsMetaData merged = new MessageDestinationsMetaData();
	      // mustOverride is false because legacy jboss descriptors not having a message-destination
	      return JavaEEMetaDataUtil.merge(merged, overriden, override, "message-destination", overridenFile, overrideFile, false);
	   }

	   /**
	    * Create a new MessageDestinationsMetaData.
	    */
	   public MessageDestinationsMetaData()
	   {
	      super("message destination name");
	   }

	   public void merge(MessageDestinationsMetaData override, MessageDestinationsMetaData original)
	   {
	      // TODO: duplicated merge methods?
	      super.merge(override, original);
	      MergeUtil.merge(this, override, original);
	   }
}