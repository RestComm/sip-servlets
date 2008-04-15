/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mobicents.servlet.sip.startup;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.catalina.Context;
import org.apache.catalina.startup.HostConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Jean Deruelle
 *
 */
public class SipHostConfig extends HostConfig {
	private static final String SIP_CONTEXT_CLASS = "org.mobicents.servlet.sip.startup.SipStandardContext";
	private static final String SIP_CONTEXT_CONFIG_CLASS = "org.mobicents.servlet.sip.startup.SipContextConfig";
	private static transient Log logger = LogFactory
		.getLog(SipHostConfig.class);
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
	
	@Override
	protected void deployApps(String name) {		
		super.deployApps(name);
		String docBase = getConfigFile(name);
		// Deploy SARs, and loop if additional descriptors are found
        File sar = new File(appBase, docBase + ".sar");
        if (sar.exists()) {
            deploySAR(name, sar, docBase + ".sar");
        }
	}
	
	/**
	 * 
	 * @param name
	 * @param sar
	 * @param string
	 */
	private void deploySAR(String name, File sar, String string) {
		String initialHostConfigClass = host.getConfigClass();
		host.setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
		deployWAR(name, sar, string);
		host.setConfigClass(initialHostConfigClass);
	}

	@Override
	protected void deployDirectory(String contextPath, File dir, String file) {
		if (deploymentExists(contextPath))
            return;
		
		boolean isSipServletApplication = isSipServletDirectory(dir);
		if(isSipServletApplication) {
			if(logger.isDebugEnabled()) {
        		logger.debug(SipContextConfig.APPLICATION_SIP_XML + " found in " 
        				+ dir + ". Enabling sip servlet archive deployment");
        	}
			String initialConfigClass = configClass;
			String initialContextClass = contextClass;
			host.setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
			setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
			setContextClass(SIP_CONTEXT_CLASS);
			super.deployDirectory(contextPath, dir, file);
			host.setConfigClass(initialConfigClass);
			configClass = initialConfigClass;
	        contextClass = initialContextClass;
		} else {
			super.deployDirectory(contextPath, dir, file);
		}
	}
	
	@Override
	protected void deployDescriptor(String contextPath, File contextXml, String file) {
		super.deployDescriptor(contextPath, contextXml, file);
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
            	if(logger.isDebugEnabled()) {
            		logger.debug(SipContextConfig.APPLICATION_SIP_XML + " found in " 
            				+ dir + ". Enabling sip servlet archive deployment");
            	}
            	String initialConfigClass = configClass;
        		String initialContextClass = contextClass;
        		host.setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
        		setConfigClass(SIP_CONTEXT_CONFIG_CLASS);
        		setContextClass(SIP_CONTEXT_CLASS);
                // Calculate the context path and make sure it is unique
                String contextPath = "/" + files[i];
                int period = contextPath.lastIndexOf(".");
                if (period >= 0)
                    contextPath = contextPath.substring(0, period);
                if (contextPath.equals("/ROOT"))
                    contextPath = "";
                
                if (isServiced(contextPath))
                    continue;
                
                String file = files[i];
                
                deploySAR(contextPath, dir, file);
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
		if (file.getName().toLowerCase().endsWith(".sar")) {
			return true;
		} else if (file.getName().toLowerCase().endsWith(".war")) {
			try{
				JarFile jar = new JarFile(file);			          
				JarEntry entry = jar.getJarEntry(SipContextConfig.APPLICATION_SIP_XML);
				if(entry != null) {
					return true;
				}
			} catch (IOException e) {
				logger.error("An unexpected Exception occured " +
						"while trying to check if a sip.xml file exists in " + file, e);
				return false;
			}
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
			File sipXmlFile = new File(dir.getAbsoluteFile() + SipContextConfig.APPLICATION_SIP_XML);
			if(sipXmlFile.exists()) {
				return true;
			}
		}		
		return false;
	}

	
	@Override
	public void manageApp(Context arg0) {		
		super.manageApp(arg0);
	}
}
