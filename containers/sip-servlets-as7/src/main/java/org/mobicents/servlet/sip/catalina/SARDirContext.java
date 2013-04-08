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

package org.mobicents.servlet.sip.catalina;

import static org.jboss.web.NamingMessages.MESSAGES;

import java.io.File;
import java.util.Hashtable;
import java.util.zip.ZipFile;

import org.apache.naming.resources.WARDirContext;
/**
 * SAR Directory Context implementation. 
 * It extends the Tomcat WARDirContext since it can contains converged apps 
 * @author Jean Deruelle
 * @author josemrecio@gmail.com
 */
public class SARDirContext extends WARDirContext {

	/**
	 * 
	 */
	public SARDirContext() {
		super();
	}

	/**
	 * @param env
	 */
	public SARDirContext(Hashtable env) {
		super(env);		
	}

	/**
	 * @param base
	 * @param entries
	 */
	public SARDirContext(ZipFile base, Entry entries) {
		super(base, entries);		
	}

	@Override
	public void setDocBase(String docBase) {		
		// Validate the format of the proposed document root
		if (docBase == null)
            throw MESSAGES.invalidNullDocumentBase();
		if (!(docBase.endsWith(".sar")))
            throw MESSAGES.docBaseNotWar(docBase);

		// Calculate a File object referencing this document base directory
		File base = new File(docBase);

		// Validate that the document base is an existing directory
		if (!base.exists() || !base.canRead() || base.isDirectory())
            throw MESSAGES.docBaseNotWar(docBase);
		try {
			this.base = new ZipFile(base);
		} catch (Exception e) {
            throw MESSAGES.failedOpeningWar(e.getMessage());
		}
		// Change the document root property
        this.docBase = docBase;

		loadEntries();		
	}
}
