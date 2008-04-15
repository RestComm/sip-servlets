package org.mobicents.servlet.sip.annotations;

import java.io.*;
import java.lang.annotation.Annotation;
import java.util.List;

import javax.servlet.sip.annotation.SipApplication;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.catalina.Wrapper;
import org.mobicents.servlet.sip.core.SipApplicationDispatcher;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.startup.loading.SipServletImpl;

/**
 * This class implement the logic to enumerate all class files in
 * WEB-INF/classes and extract descriptor information. I am using this method because
 * if I look directly in a classloader it would have loaded all libs includeing third
 * party libs in WEB-INF/jar, and system libs. Parsing all these would be slow, so for
 * now we will only look in WEB-INF/classes since it works.
 * 
 * @author Vladimir Ralev
 *
 */
public class ClassFileScanner {
	
	private String docbase;
	
	private SipStandardContext sipContext;
	
	private String parsedAnnotatedPackage = null;
	
	private boolean applicationParsed = false;
	
	public ClassFileScanner(String docbase, SipStandardContext ctx) {
		this.docbase = docbase;
		this.sipContext = ctx;
	}
	
	
	public void scan() {
		_scan(new File(this.docbase));
	}
	
    private void _scan(File folder) {
        File[] files = folder.listFiles();
        for(int j = 0; j < files.length; j++) {
            if(files[j].isDirectory())
                _scan(files[j]);
            else
            	analyzeClass(files[j].getAbsolutePath());
        }
    }
    
    private void analyzeClass(String path) {
    	String classpath = path.substring(docbase.length());
    	classpath = classpath.replace('/', '.').replace('\\', '.');
    	if(classpath.endsWith(".class")) {
    		classpath = classpath.substring(0, classpath.length() - 6);
    		if(classpath.startsWith(".")) classpath = classpath.substring(1);
    		String className = classpath;
    		try {
				Class clazz = Class.forName(className);
				processListenerAnnotation(clazz);
				processServletAnnotation(clazz);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    private void processListenerAnnotation(Class<?> clazz) {
    	SipListener listener = (SipListener) clazz.getAnnotation(SipListener.class);
    	if(listener != null)
    		sipContext.addSipApplicationListener(clazz.getCanonicalName());
    }
    
    private static void copyParsedProperties(SipStandardContext from, SipStandardContext to) {
    	to.setMainServlet(from.getMainServlet());
    	to.setApplicationName(from.getApplicationName());
    	to.setDisplayName(from.getDisplayName());
    	to.setDistributable(from.getDistributable());
    	to.setProxyTimeout(from.getProxyTimeout());
    	to.setSessionTimeout(from.getSessionTimeout());
    	to.setSmallIcon(from.getSmallIcon());
    	to.setLargeIcon(from.getLargeIcon());
    	to.setDescription(from.getDescription());
    }
    
    private void processServletAnnotation(Class<?> clazz) {
    	SipServlet servlet = (SipServlet) clazz.getAnnotation(SipServlet.class);
    	if(servlet == null) return;
    	
    	Wrapper wrapper = sipContext.createWrapper();
    	if(wrapper instanceof SipServletImpl) {
    		SipServletImpl sipServletImpl = (SipServletImpl) wrapper;

    		String appName = null;
    		if(servlet.applicationName() == null || servlet.applicationName().equals("")) {
    			String packageName = clazz.getPackage().getName();
    			SipApplication appData = getApplicationAnnotation(packageName);
    			if(appData != null) {
    				if(this.parsedAnnotatedPackage != null && !this.parsedAnnotatedPackage.equals(packageName)) {
    						throw new IllegalStateException("Cant have two different applications in a single context - "
    								+ packageName + " and " + this.parsedAnnotatedPackage);
    				}

    	    		this.applicationParsed = true;
    				if(this.parsedAnnotatedPackage == null) {
    					this.parsedAnnotatedPackage = packageName;
    					parseSipApplication(sipContext, appData, packageName);
    				}
    				appName = sipContext.getName();
    			}
    		} 
    		
    		if(appName == null) {
    			appName = servlet.applicationName();
    		}

    		String name = null;
    		if(servlet.name() == null || servlet.name().equals("")) {
    			name = clazz.getSimpleName(); // if no name is specified deduce from the classname
    		} else {
    			name = servlet.name();
    		}
    		
    		if(sipContext.getMainServlet() == null || sipContext.getMainServlet().equals("")) {
    			sipContext.setMainServlet(name);
    		}
    		
			sipServletImpl.setName(name);
			sipServletImpl.setServletName(name);
			sipServletImpl.setDisplayName(name);
			sipServletImpl.setDescription(name + "description");
    		sipServletImpl.setServletClass(clazz.getCanonicalName());
    		sipServletImpl.setLoadOnStartup(1);
    		sipServletImpl.setParent(sipContext);
    		sipContext.addChild(sipServletImpl);
    	}
    }
    
    private SipStandardContext parseSipApplication(SipStandardContext context, SipApplication appData, String packageName) {
    	context.setMainServlet(appData.mainServlet());
    	context.setProxyTimeout(appData.proxyTimeout());
    	context.setSessionTimeout(appData.sessionTimeout());
    	
    	if(appData.name() == null || appData.name().equals(""))
    		context.setApplicationName(packageName);
    	else
    		context.setApplicationName(appData.name());
    	
    	if(appData.displayName() == null || appData.displayName().equals(""))
    		context.setDisplayName(packageName);
    	else
    		context.setDisplayName(appData.displayName());
    	
    	context.setDescription(appData.description());
    	context.setLargeIcon(appData.largeIcon());
    	context.setSmallIcon(appData.smallIcon());
    	context.setDistributable(appData.distributable());
    	return context;
    }
    
    private static SipApplication getApplicationAnnotation(String packageName) {
    	Package pack = Package.getPackage(packageName);
    	if(pack == null) return null;
    	
    	SipApplication sipApp = (SipApplication) pack.getAnnotation(SipApplication.class);
    	if(sipApp != null) {
    		return sipApp;
    	}
    	
    	int lastDot = packageName.lastIndexOf('.');
    	if(lastDot <= 0) return null;
    	
    	String parentPackage = packageName.substring(0, lastDot);
    	return getApplicationAnnotation(parentPackage);
    }

    /**
     * Shows if there is SipApplication annotation parsed and thur we dont need to
     * look at sip.xml to seearch descriptor info.
     * 
     * @return
     */
	public boolean isApplicationParsed() {
		return applicationParsed;
	}
    
}
