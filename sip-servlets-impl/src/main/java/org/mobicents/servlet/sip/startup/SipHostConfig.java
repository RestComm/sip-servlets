/**
 * 
 */
package org.mobicents.servlet.sip.startup;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.startup.HostConfig;

/**
 * @author Jean Deruelle
 *
 */
public class SipHostConfig extends HostConfig {

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
        if (sar.exists())
            deploySAR(name, sar, docBase + ".sar");
	}
	
	/**
	 * 
	 * @param name
	 * @param sar
	 * @param string
	 */
	private void deploySAR(String name, File sar, String string) {
		String initialHostConfigClass = host.getConfigClass();
		host.setConfigClass("org.mobicents.servlet.sip.startup.SipContextConfig");
		deployWAR(name, sar, string);
		host.setConfigClass(initialHostConfigClass);
	}

	@Override
	protected void deployDescriptor(String arg0, File arg1, String arg2) {
		super.deployDescriptor(arg0, arg1, arg2);
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
            if (files[i].toLowerCase().endsWith(".sar")) {
            	String initialConfigClass = configClass;
        		String initialContextClass = contextClass;
        		configClass = "org.mobicents.servlet.sip.startup.SipContextConfig";
        		contextClass = "org.mobicents.servlet.sip.startup.SipStandardContext";
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
                configClass = initialConfigClass;
                contextClass = initialContextClass;        		
            }
            
        }
        super.deployWARs(appBase, files);
	}
	
	@Override
	public void manageApp(Context arg0) {		
		super.manageApp(arg0);
	}
}
