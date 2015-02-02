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

package org.mobicents.servlet.sip.catalina.annotations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;
/**
 * Utils for parwing SipApplication annotation without a classloader
 * 
 * @author Vladimir Ralev
 *
 */
public class SipApplicationAnnotationUtils {
	private static final Logger logger = Logger.getLogger(SipApplicationAnnotationUtils.class);
	
	private final static byte[] SIP_APPLICATION_BYTES = "SipApplication".getBytes();
	private final static byte[] ANNOTATION_BYTES = "annotation".getBytes();
	
	private static boolean contains(byte[] text, byte[] subtext) {
		if(text.length<subtext.length) return false;
		for(int q=0; q<text.length-subtext.length; q++) {
			boolean found = true;
			for(int w=0; w<subtext.length; w++) {
				if(text[q+w] != subtext[w]) {
					found = false; break;
				}
			}
			if(found) return true;
		}
		return false;
	}
	
	public static boolean findPackageInfoInArchive(File archive) {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(archive);
		} catch (Exception e1) {
			logger.error("Unable to open " + archive.getAbsolutePath() + ". No sip applications were parsed.");
			return false;
		}
		
		Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zipFile.entries();;
		while (entries.hasMoreElements())  {
			ZipEntry entry = entries.nextElement();
			if(entry.getName().contains("package-info.class")) {
				try {
					if(findSipApplicationAnnotation(zipFile.getInputStream(entry))) {
						return true;
					}
				} catch (IOException e) {
				}
			}
		}
		return false;
		
	}

	/**
	 * Determine if there is a sip application in this folder.
	 * 
	 * TODO: HACK: FIXME: This method reads raw class file trying to determine if it
	 * uses the SIpApplication annotation. This seems to be reliable and a lot faster
	 * than using a classloader, but can be reviewed in the future especially when
	 * JBoss AS 5.0 is available with the new deployer.
	 */
	public static boolean findPackageInfoinDirectory(File file) {
		if(file.getName().equals("package-info.class")) {
			FileInputStream stream = null;
			try {
				stream = new FileInputStream (file);
				if(findSipApplicationAnnotation(stream)) return true;
			} catch (Exception e) {}
			finally {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
		if(file.isDirectory()) {
			for(File subFile:file.listFiles()) {
				if(findPackageInfoinDirectory(subFile)) return true;
			}
		}
		return false;
	}

	/**
	 * Determine if this stream contains SipApplication annotations
	 * 
	 * TODO: HACK: FIXME: This method reads raw class file trying to determine if it
	 * uses the SIpApplication annotation. This seems to be reliable and a lot faster
	 * than using a classloader, but can be reviewed in the future especially when
	 * JBoss AS 5.0 is available with the new deployer.
	 */
	public static boolean findSipApplicationAnnotation(InputStream stream) {
		try {
			byte[] rawClassBytes;
			rawClassBytes = new byte[stream.available()];
			stream.read(rawClassBytes);
			boolean one = contains(rawClassBytes, SIP_APPLICATION_BYTES);
			boolean two = contains(rawClassBytes, ANNOTATION_BYTES);
			if(one && two) 
				return true;
		} catch (Exception e) {}
		return false;
	}
}
