/*
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
package org.mobicents.servlet.sip.annotations;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.annotation.SipApplication;
import javax.servlet.sip.annotation.SipApplicationKey;
import javax.servlet.sip.annotation.SipListener;
import javax.servlet.sip.annotation.SipServlet;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.annotation.ConcurrencyControl;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.loading.SipServletImpl;

/**
 * This class implement the logic to enumerate all class files in
 * WEB-INF/classes and extract descriptor information. I am using this method because
 * if I look directly in a classloader it would have loaded all libs includeing third
 * party libs in WEB-INF/jar, and system libs. Parsing all these would be slow, so for
 * now we will only look in WEB-INF/classes since it works.
 * 
 * General TODO: Validation
 * 
 * @author Vladimir Ralev
 *
 */
public class ClassFileScanner {

	private static transient final Logger logger = Logger.getLogger(ClassFileScanner.class);
			
	private String docbase;
	
	private SipContext sipContext;
	
	private String parsedAnnotatedPackage = null;
	
	private boolean applicationParsed = false;
	
	private Method sipAppKey = null;
	
	private AnnotationsClassLoader classLoader;
	
	public ClassFileScanner(String docbase, SipContext ctx) {
		this.docbase = docbase;
		this.sipContext = ctx;
	}
	
	/**
	 * Scan the application for annotations with the contextconfig classloader.
	 * It scans in the following locations :
	 * WEB-INF/classes
	 * WEB-INF/lib
	 * ../APP-INF/lib
	 * ../APP-INF/classes
	 * 
	 * @throws AnnotationVerificationException thrown if some annotations doesn't follow the restrictions given by the annotation contract
	 */
	public void scan() throws AnnotationVerificationException {
		ClassLoader cl = this.sipContext.getClass().getClassLoader();
		this.classLoader = new AnnotationsClassLoader(
				cl);
		this.classLoader.setResources(this.sipContext.getResources());
		this.classLoader.setAntiJARLocking(true);
		
		if(logger.isDebugEnabled()) {
			logger.debug("Annotations docBase : " + this.docbase);
		}
		
		this.classLoader.setWorkDir(new File(this.docbase + "/tmp"));
		
		// Add this SAR/WAR's binary file from WEB-INF/classes and WEB-INF/lib		
		this.classLoader.addRepository("/WEB-INF/classes/", new File(this.docbase + "/WEB-INF/classes/"));
		this.classLoader.addJarDir(this.docbase + "/WEB-INF/lib/");
		//Add those only for EAR files
		if(docbase.indexOf(".ear")!=-1) {
			//Adding root dir to include jars located here like ejb modules and so on...
			//Ideally we may want to parse the application.xml and get the jars that are defined in it...?
			this.classLoader.addJarDir(this.docbase + "/../");
			
			// Try to add the EAR binaries as repositories
			File earJarDir = new File(this.docbase + "/../APP-INF/lib");
			File earClassesDir = new File(this.docbase + "/../APP-INF/classes");
			if(earJarDir.exists())
				this.classLoader.addJarDir(this.docbase + "/../APP-INF/lib");
			if(earClassesDir.exists())
				this.classLoader.addRepository(this.docbase + "/../APP-INF/classes");
		}
		// TODO: Add META-INF classpath
			
		_scan(new File(this.docbase));
	}
	
	protected void _scan(File folder) throws AnnotationVerificationException {    	
        File[] files = folder.listFiles();
        if(files != null) {
	        for(int j = 0; j < files.length; j++) {
	            if(files[j].isDirectory()) {
	                _scan(files[j]);
	            } else if(files[j].getAbsolutePath().endsWith(".jar")) {
	            	scanJar(files[j].getAbsolutePath());
	            } else {
	            	analyzeClass(files[j].getAbsolutePath());
	            }
	        }
        }
    }
	
	private void scanJar(String path) throws AnnotationVerificationException {
		if(logger.isDebugEnabled()) {
    		logger.debug("scanning jar " + path + " for annotations");
    	}
		try {
			URL[] urls = { new URL("jar:file:" + path + "!/") };
			URLClassLoader urlClassLoader = URLClassLoader.newInstance(urls, this.classLoader);

			JarFile jar =new JarFile(path);
			Enumeration<JarEntry> jarEntries = jar.entries();
			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = jarEntries.nextElement();
				String entryName = jarEntry.getName();
								
				if(entryName.endsWith(".class")) {
					String className =  entryName.substring(0, entryName.indexOf(".class"));
					className = className.replace('/', '.');
					className = className.replace('\\', '.');
					Class<?> clazz = urlClassLoader.loadClass(className);				
					processAnnotations(clazz);
				}
			}
		} catch (IOException e) {
			throw new AnnotationVerificationException("couldn't read the following jar file for parsing annotations " + path, e);
		} catch (ClassNotFoundException e) {
			throw new AnnotationVerificationException("couldn't read the following jar file for parsing annotations " + path, e);
		}
		
	}
    
    protected void analyzeClass(String path) throws AnnotationVerificationException {
    	if(logger.isDebugEnabled()) {
    		logger.debug("analyzing class " + path + " for annotations");
    	}
    	// TODO: must check if there are extra /// or \\\ or /./ in the path after classes/
    	int classesIndex = path.toLowerCase().lastIndexOf("classes/");
    	if(classesIndex < 0) classesIndex = path.toLowerCase().lastIndexOf("classes\\");
    	classesIndex += "classes/".length();
    	String classpath = path.substring(classesIndex);
    	classpath = classpath.replace('/', '.').replace('\\', '.');
    	if(classpath.endsWith(".class")) {
    		classpath = classpath.substring(0, classpath.length() - 6);
    		if(classpath.startsWith(".")) classpath = classpath.substring(1);
    		String className = classpath;
    		try {
    	    	Class clazz = Class.forName(className, false, this.classLoader);
    	    	processAnnotations(clazz);
    		} catch (Throwable e) {
    			logger.warn("Failed to parse annotations for class " + className);
    			if(logger.isDebugEnabled()) {
    				logger.debug("Failed to parse annotations for class " + className, e);
    			}
    		}
    	}
    }
    
    protected void processAnnotations(Class clazz) throws AnnotationVerificationException {
    	if(logger.isDebugEnabled()) {
    		logger.debug("analyzing class " + clazz + " for annotations");
    	}    	
    	processListenerAnnotation(clazz);
		processServletAnnotation(clazz);
		processSipApplicationKeyAnnotation(clazz);
		processConcurrencyAnnotation(clazz);    	
	}

	protected void processListenerAnnotation(Class<?> clazz) {
    	if(logger.isDebugEnabled()) {
    		logger.debug("scanning " + clazz.getCanonicalName() + " for listener annotations");
    	}
    	SipListener listener = (SipListener) clazz.getAnnotation(SipListener.class);
    	if(listener != null) {
    		if(logger.isDebugEnabled()) {
    			logger.debug("the following listener has been found as an annotation " + clazz.getCanonicalName());
    		}
    		sipContext.addSipApplicationListener(clazz.getCanonicalName());
    	}
    }
    
	protected void processSipApplicationKeyAnnotation(Class<?> clazz) throws AnnotationVerificationException {
    	if(logger.isDebugEnabled()) {
    		logger.debug("scanning " + clazz.getCanonicalName() + " for sip application key annotation");
    	}
		Method[] methods = clazz.getMethods();
		for(Method method:methods) {
			if(method.getAnnotation(SipApplicationKey.class)!=null) {
				if(!Modifier.isStatic(method.getModifiers()) || !Modifier.isPublic(method.getModifiers())) {
					throw new AnnotationVerificationException(
							"A method annotated with the @SipApplicationKey annotation MUST be public and static");
				}
				if(!method.getGenericReturnType().equals(String.class)) {
					throw new AnnotationVerificationException(
							"A method annotated with the @SipApplicationKey annotation MUST return a String");
				}
				Type[] types = method.getGenericParameterTypes();
				if(types.length != 1 || !types[0].equals(SipServletRequest.class)) {
					throw new AnnotationVerificationException(
							"A method annotated with the @SipApplicationKey annotation MUST have a single argument of type SipServletRequest");
				}
				if(this.sipAppKey != null) {
					throw new IllegalStateException(
						"More than one SipApplicationKey annotated method is not allowed.");
				}
				this.sipAppKey = method;
				if(logger.isDebugEnabled()) {
	    			logger.debug("the following @SipApplicationKey annotated method has been found " + method.toString());
	    		}
				sipContext.setSipApplicationKeyMethod(method);
			}
		}    	
    }
    
    
    protected void processServletAnnotation(Class<?> clazz) {
    	if(logger.isDebugEnabled()) {
    		logger.debug("scanning " + clazz.getCanonicalName() + " for servlet annotations");
    	}
		SipServlet servlet = (SipServlet) clazz.getAnnotation(SipServlet.class);
		if (servlet == null)
			return;
		
		SipServletImpl parsedServletData = (SipServletImpl) sipContext.createWrapper();

		String appName = null;
		if (servlet.applicationName() == null
				|| servlet.applicationName().equals("")) {
			// String packageName = clazz.getCanonicalName().substring(0,
			// clazz.getCanonicalName().lastIndexOf('.'));
			// Wasted a whole day watching this line...
			Package pack = clazz.getPackage();
			String packageName = pack.getName();

			SipApplication appData = getApplicationAnnotation(pack);
			if (appData != null) {
				if (this.parsedAnnotatedPackage != null
						&& !this.parsedAnnotatedPackage.equals(packageName)) {
					throw new IllegalStateException(
							"Cant have two different applications in a single context - "
									+ packageName + " and "
									+ this.parsedAnnotatedPackage);
				}
				
				if (this.parsedAnnotatedPackage == null) {
					this.parsedAnnotatedPackage = packageName;
					parseSipApplication(appData, packageName);
				}
				appName = sipContext.getName();
			}
		}

		if (appName == null) {
			appName = servlet.applicationName();
		}

		String name = null;
		if (servlet.name() == null || servlet.name().equals("")) {
			name = clazz.getSimpleName(); // if no name is specified deduce
											// from the classname
		} else {
			name = servlet.name();
		}

		if (sipContext.getMainServlet() == null
				|| sipContext.getMainServlet().equals("")) {
			sipContext.setMainServlet(name);
		}
		if(logger.isDebugEnabled()) {
			logger.debug("the following @SipServlet annotation has been found : ");
			logger.debug("Name,ServletName,DisplayName : " + name);
			logger.debug("Description : " + servlet.description());
			logger.debug("servletClass : " + clazz.getCanonicalName());
		}
		parsedServletData.setName(name);
		parsedServletData.setServletName(name);
		parsedServletData.setDisplayName(name);
		parsedServletData.setDescription(servlet.description());
		parsedServletData.setServletClass(clazz.getCanonicalName());
		parsedServletData.setLoadOnStartup(1);
		parsedServletData.setParent(sipContext);
		sipContext.addChild(parsedServletData);
		this.applicationParsed = true;
	}
    
    protected void parseSipApplication(SipApplication appData, String packageName) {
    	sipContext.setMainServlet(appData.mainServlet());
    	sipContext.setProxyTimeout(appData.proxyTimeout());
    	sipContext.setSessionTimeout(appData.sessionTimeout());
    	
    	if(appData.name() == null || appData.name().equals(""))
    		sipContext.setApplicationName(packageName);
    	else
    		sipContext.setApplicationName(appData.name());
    	
    	if(appData.displayName() == null || appData.displayName().equals(""))
    		sipContext.setDisplayName(packageName);
    	else
    		sipContext.setDisplayName(appData.displayName());
    	if(logger.isDebugEnabled()) {
			logger.debug("the following @SipApplication annotation has been found : ");
			logger.debug("ApplicationName : " + sipContext.getApplicationName());
			logger.debug("MainServlet : " + sipContext.getMainServlet());
		}
    	sipContext.setDescription(appData.description());
    	sipContext.setLargeIcon(appData.largeIcon());
    	sipContext.setSmallIcon(appData.smallIcon());
    	sipContext.setDistributable(appData.distributable());
    }
    
    protected SipApplication getApplicationAnnotation(Package pack) {
    	if(pack == null) return null;
    	
    	SipApplication sipApp = (SipApplication) pack.getAnnotation(SipApplication.class);
    	if(sipApp != null) {
    		return sipApp;
    	}
    	return null;
    }

    /**
     * Check if the @ConcurrencyControl annotation is present in the package and if so process it 
     * @param clazz the clazz to check for @ConcurrencyControl annotation - only its package will be checked
     */
    protected void processConcurrencyAnnotation(Class clazz) {
    	if(sipContext.getConcurrencyControlMode() == null) {
	    	Package pack = clazz.getPackage();
	    	if(pack != null) {
				ConcurrencyControl concurrencyControl = pack.getAnnotation(ConcurrencyControl.class);
				if(concurrencyControl != null) {
					if(logger.isDebugEnabled()) {
						logger.debug("Concurrency control annotation found " + concurrencyControl.mode());
					}
					sipContext.setConcurrencyControlMode(concurrencyControl.mode());
				}
	    	}

    	}
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
