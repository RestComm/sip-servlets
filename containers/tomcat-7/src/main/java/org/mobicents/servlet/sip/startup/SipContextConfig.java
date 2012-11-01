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
package org.mobicents.servlet.sip.startup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletContext;

import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.DigesterFactory;
import org.apache.catalina.startup.ExpandWar;
import org.apache.catalina.util.ContextName;
import org.apache.log4j.Logger;
import org.apache.naming.resources.DirContextURLConnection;
import org.apache.naming.resources.FileDirContext;
import org.apache.naming.resources.ResourceAttributes;
import org.apache.tomcat.util.ExceptionUtils;
import org.apache.tomcat.util.bcel.classfile.AnnotationEntry;
import org.apache.tomcat.util.bcel.classfile.ClassFormatException;
import org.apache.tomcat.util.bcel.classfile.ClassParser;
import org.apache.tomcat.util.bcel.classfile.JavaClass;
import org.apache.tomcat.util.digester.Digester;
import org.mobicents.servlet.sip.annotations.AnnotationVerificationException;
import org.mobicents.servlet.sip.annotations.ClassFileScanner;
import org.mobicents.servlet.sip.catalina.CatalinaSipContext;
import org.mobicents.servlet.sip.catalina.SipDeploymentException;
import org.mobicents.servlet.sip.catalina.SipEntityResolver;
import org.mobicents.servlet.sip.catalina.SipRuleSet;
import org.mobicents.servlet.sip.core.SipContext;
import org.xml.sax.EntityResolver;

/**
 * Startup event listener for a the <b>SipStandardContext</b> that configures
 * the properties of that Context, and the associated defined servlets.
 * it extends the regular tomcat context config to be able to load sip
 * servlet applications.
 * 
 * @author Jean Deruelle
 * 
 */
public class SipContextConfig extends ContextConfig {	

	private static transient final Logger logger = Logger.getLogger(SipContextConfig.class);
	//Issue 77: http://code.google.com/p/sipservlets/issues/detail?id=77
	private Boolean hasWebAnnotations = false;


	/**
	 * {@inheritDoc}
	 */
	public void lifecycleEvent(LifecycleEvent event) {
		try {
			super.lifecycleEvent(event);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@Override
	protected synchronized void configureStart() {		
		if(context instanceof SipContext) {
			if(logger.isDebugEnabled()) {
				logger.debug("starting sipContextConfig");
			}
			ServletContext servletContext = context.getServletContext();
			// calling start on the parent to initialize web resources of the web
			// app if any. That mean that this is a converged application.
			InputStream webXmlInputStream = servletContext
				.getResourceAsStream(Constants.ApplicationWebXml);
			context.setWrapperClass(StandardWrapper.class.getName());						
			if (webXmlInputStream != null) {
				if(logger.isDebugEnabled()) {
					logger.debug(Constants.ApplicationWebXml + " has been found, calling super.start() !");
				}
				super.configureStart();
			} else {
				//Java Servlets 3.0 makes web.xml optional if the servlet is using annotations.
				//So here we check for web servlet annotations.
				checkWebAnnotations();
			}

			if(hasWebAnnotations){
				super.configureStart();
			}

			context.setWrapperClass(org.mobicents.servlet.sip.catalina.SipServletImpl.class.getName());

			//annotations scanning
			ClassFileScanner scanner = new ClassFileScanner(((SipContext)context).getBasePath(), (CatalinaSipContext)context);
			try {
				scanner.scan();
			} catch (AnnotationVerificationException ave) {
				logger.error("An annotation didn't follow its annotation contract",
						ave);
				ok = false;
			}					

			InputStream sipXmlInputStream = servletContext
			.getResourceAsStream(SipContext.APPLICATION_SIP_XML);
			// processing of the sip.xml file
			if (sipXmlInputStream != null) {
				if(logger.isDebugEnabled()) {
					logger.debug(SipContext.APPLICATION_SIP_XML + " has been found !");
				}								

				Digester sipDigester =  DigesterFactory.newDigester(context.getXmlValidation(),
						context.getXmlNamespaceAware(),
						new SipRuleSet());
				EntityResolver entityResolver = new SipEntityResolver();
				sipDigester.setValidating(false);		
				sipDigester.setEntityResolver(entityResolver);				
				//push the context to the digester
				sipDigester.push(context);
				sipDigester.setClassLoader(context.getClass().getClassLoader());
				//parse the sip.xml and populate the context with it
				try {
					sipDigester.resolveEntity(null, null);
					sipDigester.parse(sipXmlInputStream);
				} catch (Throwable e) {
					logger.warn("Impossible to parse the sip.xml deployment descriptor");
					ok = false;
				}
			} else {
				if(logger.isInfoEnabled()) {
					logger.info(SipContext.APPLICATION_SIP_XML + " has not been found !");
				}
				ok = false;
			}	

			// Use description from the annotations no matter if sip.xml parsing failed.
			if(scanner.isApplicationParsed()) { 
				ok = true;
			}

			checkSipDeploymentRequirements(context);

			if(!scanner.isApplicationParsed() && sipXmlInputStream != null) {
				context.setWrapperClass(StandardWrapper.class.getName());
			}

			// Make our application available if no problems were encountered
			if (ok) {
				if(logger.isDebugEnabled()) {
					logger.debug("sipContextConfig started");
				}
				context.setConfigured(true);						
			} else {
				logger.warn("sipContextConfig didn't start properly");
				context.setConfigured(false);
			}			
		} else {
			super.configureStart();
		}				
	}

	private void checkSipDeploymentRequirements(Context context) {
		if(((SipContext) context).getApplicationName() == null) {
			ok = false;
			context.setConfigured(false);				
			throw new SipDeploymentException("No app-name present in the sip.xml deployment descriptor or no SipApplication annotation defined");
		}

		boolean servletSelectionSet = false;
		String mainServlet = ((SipContext) context).getMainServlet();
		if(mainServlet != null && mainServlet.length() > 0) {
			servletSelectionSet = true;
		} else if(((SipContext) context).findSipServletMappings() != null && ((SipContext) context).findSipServletMappings().size() > 0) {
			servletSelectionSet = true;
		} else if(((SipContext) context).getSipRubyController() != null) {
			servletSelectionSet = true;
		}

		if(((SipContext) context).getChildrenMap().keySet().size() > 1 && !servletSelectionSet) {
			ok = false;
			context.setConfigured(false);
			throw new SipDeploymentException("the main servlet is not set and there is more than one servlet defined in the sip.xml or as annotations !");
		}
	}


	@Override
	protected synchronized void configureStop() {
		if(logger.isDebugEnabled()) {
			logger.debug("stopping sipContextConfig");
		}
		super.configureStop();
		if(logger.isDebugEnabled()) {
			logger.debug("sipContextConfig stopped");
		}
	}

	/**
	 * Adjust docBase.
	 */
	protected void fixDocBase() throws IOException {
		if(context instanceof SipContext) {
			Host host = (Host) context.getParent();
			String appBase = host.getAppBase();
			boolean unpackWARs = true;
			if (host instanceof StandardHost) {
				unpackWARs = ((StandardHost) host).isUnpackWARs()
				&& ((StandardContext) context).getUnpackWAR();
			}
			File canonicalAppBase = new File(appBase);
			if (canonicalAppBase.isAbsolute()) {
				canonicalAppBase = canonicalAppBase.getCanonicalFile();
			} else {
				canonicalAppBase = new File(System.getProperty("catalina.base"),
						appBase).getCanonicalFile();
			}
			String docBase = context.getDocBase();
			if (docBase == null) {
				// Trying to guess the docBase according to the path
				String path = context.getPath();
				if (path == null) {
					return;
				}

				ContextName cn = new ContextName(path, context.getWebappVersion());
				docBase = cn.getBaseName();

				//				if (path.equals("")) {
				//					docBase = "ROOT";
				//				} else {
				//					if (path.startsWith("/")) {
				//	                    docBase = path.substring(1).replace('/', '#');
				//	                } else {
				//	                    docBase = path.replace('/', '#');
				//	                }
				//				}
			}
			File file = new File(docBase);
			if (!file.isAbsolute()) {
				docBase = (new File(canonicalAppBase, docBase)).getPath();
			} else {
				docBase = file.getCanonicalPath();
			}
			file = new File(docBase);
			//			String origDocBase = docBase;
			if ((docBase.toLowerCase().endsWith(".sar") || docBase.toLowerCase()
					.endsWith(".war"))
					&& !file.isDirectory() && unpackWARs) {
				URL war = new URL("jar:" + (new File(docBase)).toURI().toURL() + "!/");
		        //Issue:175: http://code.google.com/p/sipservlets/issues/detail?id=175
				ContextName cn = new ContextName(context.getPath(),
		                context.getWebappVersion());
		        String contextPath = cn.getBaseName();

				//				if (contextPath.equals("")) {
				//					contextPath = "ROOT";
				//				} else {
				//		            if (contextPath.lastIndexOf('/') > 0) {
				//		                contextPath = "/" + contextPath.substring(1).replace('/','#');
				//		            }
				//		        }

				docBase = ExpandWar.expand(host, war, contextPath);
				file = new File(docBase);
				docBase = file.getCanonicalPath();
				if (context instanceof CatalinaSipContext) {
					FileDirContext fileDirContext =new FileDirContext();
					fileDirContext.setDocBase(docBase);
					((CatalinaSipContext) context).setResources(fileDirContext );
				}
			} else {
				File docDir = new File(docBase);
				if (!docDir.exists()) {
					String[] extensions = new String[] { ".sar", ".war" };
					for (String extension : extensions) {
						File archiveFile = new File(docBase + extension);
						if (archiveFile.exists()) {
							if (unpackWARs) {
								URL war = new URL("jar:" + archiveFile.toURI().toURL()
										+ "!/");
								docBase = ExpandWar.expand(host, war, context
										.getPath());
								file = new File(docBase);
								docBase = file.getCanonicalPath();
							} else {
								docBase = archiveFile.getCanonicalPath();
							}

							break;
						}
					}
					if (context instanceof CatalinaSipContext) {
						FileDirContext fileDirContext =new FileDirContext();
						fileDirContext.setDocBase(docBase);
						((CatalinaSipContext) context).setResources(fileDirContext );
					}
				}
			}
			if (docBase.startsWith(canonicalAppBase.getPath() + File.separatorChar)) {
				docBase = docBase.substring(canonicalAppBase.getPath().length());
				docBase = docBase.replace(File.separatorChar, '/');
				if (docBase.startsWith("/")) {
					docBase = docBase.substring(1);
				}
			} else {
				docBase = docBase.replace(File.separatorChar, '/');
			}
			context.setDocBase(docBase);
		} else {
			super.fixDocBase();
		}
	}

	
	
	// Check for web servlet annotation in the /WEB-INF/classes	
	protected void checkWebAnnotations(){
		URL webinfClasses = null;
		try{
			webinfClasses = context.getServletContext().getResource("/WEB-INF/classes");
		} catch (MalformedURLException e){
			logger.error(sm.getString("contextConfig.webinfClassesUrl"),e);
		}

		if (webinfClasses == null) {
			// Nothing to do.
			return;
		} else if ("jar".equals(webinfClasses.getProtocol())) {
			processAnnotationsJar(webinfClasses);
		} else if ("jndi".equals(webinfClasses.getProtocol())) {
			processAnnotationsJndi(webinfClasses);
		} else if ("file".equals(webinfClasses.getProtocol())) {
			try {
				processAnnotationsFile(new File(webinfClasses.toURI()));
			} catch (URISyntaxException e) {
				logger.error(sm.getString("contextConfig.fileUrl", webinfClasses), e);
			} 
		} else {
			logger.error(sm.getString("contextConfig.unknownUrlProtocol",
					webinfClasses.getProtocol(), webinfClasses));
		}
	}

	protected void processAnnotationsFile(File file){

		if (file.isDirectory()) {
			String[] dirs = file.list();
			for (String dir : dirs) {
				processAnnotationsFile(new File(file,dir));
			}
		} else if (file.canRead() && file.getName().endsWith(".class")) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(file);
				processAnnotationsStream(fis);
			} catch (IOException e) {
				logger.error(sm.getString("contextConfig.inputStreamFile",
						file.getAbsolutePath()),e);
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (Throwable t) {
						ExceptionUtils.handleThrowable(t);
					}
				}
			}
		}
	}

	protected void processAnnotationsJar(URL url) {

		JarFile jarFile = null;

		try {
			URLConnection urlConn = url.openConnection();
			JarURLConnection jarUrlConn;
			if (!(urlConn instanceof JarURLConnection)) {
				// This should never happen
				sm.getString("contextConfig.jarUrl", url);
				return;
			}

			jarUrlConn = (JarURLConnection) urlConn;
			jarUrlConn.setUseCaches(false);
			jarFile = jarUrlConn.getJarFile();

			Enumeration<JarEntry> jarEntries = jarFile.entries();
			while (jarEntries.hasMoreElements()) {
				JarEntry jarEntry = jarEntries.nextElement();
				String entryName = jarEntry.getName();
				if (entryName.endsWith(".class")) {
					InputStream is = null;
					try {
						is = jarFile.getInputStream(jarEntry);
						processAnnotationsStream(is);
					} catch (IOException e) {
						logger.error(sm.getString("contextConfig.inputStreamJar",
								entryName, url),e);
					} finally {
						if (is != null) {
							try {
								is.close();
							} catch (Throwable t) {
								ExceptionUtils.handleThrowable(t);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error(sm.getString("contextConfig.jarFile", url), e);
		} finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (Throwable t) {
					ExceptionUtils.handleThrowable(t);
				}
			}
		}
	}

	protected void processAnnotationsJndi(URL url) {
		try {
			URLConnection urlConn = url.openConnection();
			DirContextURLConnection dcUrlConn;
			if (!(urlConn instanceof DirContextURLConnection)) {
				// This should never happen
				sm.getString("contextConfig.jndiUrlNotDirContextConn", url);
				return;
			}

			dcUrlConn = (DirContextURLConnection) urlConn;
			dcUrlConn.setUseCaches(false);

			String type = dcUrlConn.getHeaderField(ResourceAttributes.TYPE);
			if (ResourceAttributes.COLLECTION_TYPE.equals(type)) {
				// Collection
				Enumeration<String> dirs = dcUrlConn.list();
				while (dirs.hasMoreElements()) {
					String dir = dirs.nextElement();
					URL dirUrl = new URL(url.toString() + '/' + dir);
					processAnnotationsJndi(dirUrl);
				}

			} else {
				// Single file
				if (url.getPath().endsWith(".class")) {
					InputStream is = null;
					try {
						is = dcUrlConn.getInputStream();
						processAnnotationsStream(is);
					} catch (IOException e) {
						logger.error(sm.getString("contextConfig.inputStreamJndi",
								url),e);
					} finally {
						if (is != null) {
							try {
								is.close();
							} catch (Throwable t) {
								ExceptionUtils.handleThrowable(t);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			logger.error(sm.getString("contextConfig.jndiUrl", url), e);
		} 
	}

	protected void processAnnotationsStream(InputStream is)
	throws ClassFormatException, IOException {

		ClassParser parser = new ClassParser(is, null);
		JavaClass clazz = parser.parse();

		AnnotationEntry[] annotationsEntries = clazz.getAnnotationEntries();

		for (AnnotationEntry ae : annotationsEntries) {
			String type = ae.getAnnotationType();
			if ("Ljavax/servlet/annotation/WebServlet;".equals(type)) {
				hasWebAnnotations=true;
			}else if ("Ljavax/servlet/annotation/WebFilter;".equals(type)) {
				hasWebAnnotations=true;
			}else if ("Ljavax/servlet/annotation/WebListener;".equals(type)) {
				hasWebAnnotations=true;
			}else if ("Ljavax/servlet/annotation/WebInitParam;".equals(type)) {
				hasWebAnnotations=true;
			}else if ("Ljavax/servlet/annotation/MultipartConfig;".equals(type)) {
				hasWebAnnotations=true;
			} else {
				// Unknown annotation - ignore
			}
		}
	}

}
