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

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.catalina.Context;
import org.apache.catalina.startup.HostConfig;
import org.apache.catalina.util.ContextName;
import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.catalina.annotations.SipApplicationAnnotationUtils;
import org.mobicents.servlet.sip.core.SipContext;

/**
 * @author Jean Deruelle
 *
 */
public class SipHostConfig extends HostConfig {
	private static final String WAR_EXTENSION = ".war";
	private static final String SAR_EXTENSION = ".sar";
	public static final String SIP_CONTEXT_CLASS = "org.mobicents.servlet.sip.startup.SipStandardContext";
	public static final String SIP_CONTEXT_CONFIG_CLASS = "org.mobicents.servlet.sip.startup.SipContextConfig";
	
	private static final Logger logger = Logger.getLogger(SipHostConfig.class);
	/**
	 * 
	 */
	public SipHostConfig() {
		super();				
	}

	@Override
	protected void deployApps() {		
		super.deployApps();		
	}
		
	//completely overwritten since jboss web in jboss as5 and as 4.2.3 are not based on tomcat 6.0.20 
	//tc 7.0.4 changed this method to getBaseName(String path). also this replace the getConfigFile(String path) method
//	@Override
//    protected String getDocBase(String path) {
//        String basename = null;
//        if (path.equals("")) {
//            basename = "ROOT";
//        } else {
//            basename = path.substring(1).replace('/', '#');
//        }
//        return (basename);
//    }

    
	@Override
	protected void deployApps(String name) {		
		File appBase = appBase();
        File configBase = configBase();

        //        String baseName = getBaseName(name);
        
        ContextName cn = new ContextName(name);
        String baseName = cn.getBaseName();
        
        //No need for docBase, replace by baseName
        //String docBase = getBaseName(name);
        if (deploymentExists(baseName)) {
            return;
        }
        
        // Deploy XML descriptors from configBase
        File xml = new File(configBase, baseName + ".xml");
        if (xml.exists()) {
            deployDescriptor(cn, xml);
            return;
        }
        // Deploy WARs, and loop if additional descriptors are found
        File war = new File(appBase, baseName + ".war");
        if (war.exists()) {
        	boolean isSipServletApplication = isSipServletArchive(war);
            if(isSipServletApplication) {  
            	deploySAR(cn, war);
            } else {
            	deployWAR(cn, war);
            }
            return;
        }
        // Deploy expanded folders
        File dir = new File(appBase, baseName);
        if (dir.exists()) {
            deployDirectory(cn, dir);
        }
		// Deploy SARs, and loop if additional descriptors are found
        File sar = new File(appBase, baseName + SAR_EXTENSION);
        if (sar.exists()) {
            deploySAR(cn, sar);
        }
	}
	
	/**
	 * 
	 * @param NAME
	 * @param sar
	 * @param string
	 */
	private void deploySAR(ContextName cn, File sar) {
		if (deploymentExists(cn.getName()))
            return;
		if(logger.isDebugEnabled()) {
    		logger.debug(SipContext.APPLICATION_SIP_XML + " found in " 
    				+ sar + ". Enabling sip servlet archive deployment");
    	}
		String initialConfigClass = configClass;
		String initialContextClass = contextClass;
		host.setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
		setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
		setContextClass(SIP_CONTEXT_CLASS);
		deployWAR(cn, sar);
		host.setConfigClass(initialConfigClass);
		configClass = initialConfigClass;
        contextClass = initialContextClass;
	}
	
	@Override
	protected void deployWAR(ContextName cn, File dir) {
		if(logger.isTraceEnabled()) {
    		logger.trace("Context class used to deploy the WAR : " + contextClass);
    		logger.trace("Context config class used to deploy the WAR : " + configClass);
    	}
		super.deployWAR(cn, dir);
	}

	@Override
	protected void deployDirectory(ContextName cn, File dir) {
		if (deploymentExists(cn.getName()))
            return;
		
		boolean isSipServletApplication = isSipServletDirectory(dir);
		if(isSipServletApplication) {
			if(logger.isDebugEnabled()) {
        		logger.debug(SipContext.APPLICATION_SIP_XML + " found in " 
        				+ dir + ". Enabling sip servlet archive deployment");
        	}
			String initialConfigClass = configClass;
			String initialContextClass = contextClass;
			host.setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
			setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
			setContextClass(SIP_CONTEXT_CLASS);
			super.deployDirectory(cn, dir);
			host.setConfigClass(initialConfigClass);
			configClass = initialConfigClass;
	        contextClass = initialContextClass;
		} else {
			super.deployDirectory(cn, dir);
		}
	}
	
	@Override
	protected void deployDescriptor(ContextName cn, File contextXml) {
		super.deployDescriptor(cn, contextXml);
	}
	
	@Override
	protected void deployWARs(File appBase, String[] files) {		
		if (files == null)
            return;
        
        for (int i = 0; i < files.length; i++) {
            
            if (files[i].equalsIgnoreCase("META-INF"))
                continue;
            if (files[i].equalsIgnoreCase("WEB-INF"))
                continue;
            File dir = new File(appBase, files[i]);
            boolean isSipServletApplication = isSipServletArchive(dir);
            if(isSipServletApplication) {
            	ContextName cn = new ContextName(files[i]);
                // Calculate the context path and make sure it is unique
//                String contextPath = "/" + files[i];
//                int period = contextPath.lastIndexOf(".");
//                if (period >= 0)
//                    contextPath = contextPath.substring(0, period);
//                if (contextPath.equals("/ROOT"))
//                    contextPath = "";
                
                if (isServiced(cn.getName()))
                    continue;                               
                                
                String initialConfigClass = configClass;
        		String initialContextClass = contextClass;
        		host.setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
        		setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
        		setContextClass(SIP_CONTEXT_CLASS);
                deploySAR(cn, dir);
                host.setConfigClass(initialConfigClass);
                configClass = initialConfigClass;
                contextClass = initialContextClass;        		
            }                        
        }
        super.deployWARs(appBase, files);
	}
	
	/**
	 * Check if the file given in parameter match a sip servlet application, i.e.
	 * if it contains a sip.xml in its WEB-INF directory
	 * @param file the file to check (war or sar)
	 * @return true if the file is a sip servlet application, false otherwise
	 */
	private boolean isSipServletArchive(File file) {
		if (file.getName().toLowerCase().endsWith(SAR_EXTENSION)) {
			return true;
		} else if (file.getName().toLowerCase().endsWith(WAR_EXTENSION)) {
			try{
                JarFile jar = new JarFile(file);                                  
                JarEntry entry = jar.getJarEntry(SipContext.APPLICATION_SIP_XML);
                if(entry != null) {
                        return true;
                }                
	        } catch (IOException e) {
	        	if(logger.isInfoEnabled()) {
	        		logger.info("couldn't find WEB-INF/sip.xml in " + file + " checking for package-info.class");
	        	}
	        }
			return SipApplicationAnnotationUtils.findPackageInfoInArchive(file);
		} 		
		return false;
	}

	/**
	 * Check if the file given in parameter match a sip servlet application, i.e.
	 * if it contains a sip.xml in its WEB-INF directory
	 * @param file the file to check (war or sar)
	 * @return true if the file is a sip servlet application, false otherwise
	 */
	private boolean isSipServletDirectory(File dir) {
		 if(dir.isDirectory()) {
			 //Fix provided by Thomas Leseney for exploded directory deployments
			File sipXmlFile = new File(dir.getAbsoluteFile(), SipContext.APPLICATION_SIP_XML);
			if(sipXmlFile.exists()) {
				return true;
			}
			if(SipApplicationAnnotationUtils.findPackageInfoinDirectory(dir)) return true;
		}		
		return false;
	}

	
	@Override
	public void manageApp(Context arg0) {		
		super.manageApp(arg0);
	}
}
