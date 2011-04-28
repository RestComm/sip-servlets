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

package org.jboss.metadata.sip.spec;

import javax.xml.bind.annotation.XmlElement;

import org.jboss.metadata.javaee.support.IdMetaDataImpl;
import org.jboss.xb.annotations.JBossXmlNsPrefix;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class SessionConfigMetaData extends IdMetaDataImpl
{
	   private static final long serialVersionUID = 1;
	   private int sessionTimeout;

	   public int getSessionTimeout()
	   {
	      return sessionTimeout;
	   }
	   
	   @XmlElement(name="session-timeout")
	   @JBossXmlNsPrefix(prefix="javaee", schemaTargetIfNotMapped=true)
	   public void setSessionTimeout(int sessionTimeout)
	   {
	      this.sessionTimeout = sessionTimeout;
	   }
}
