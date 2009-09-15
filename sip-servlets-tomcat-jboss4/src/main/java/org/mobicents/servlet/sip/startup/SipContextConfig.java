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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
import org.apache.log4j.Logger;
import org.apache.naming.resources.FileDirContext;
import org.apache.tomcat.util.digester.Digester;
import org.mobicents.servlet.sip.annotations.AnnotationVerificationException;
import org.mobicents.servlet.sip.annotations.ClassFileScanner;
import org.mobicents.servlet.sip.startup.loading.SipServletImpl;
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
	protected synchronized void start() {		
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
				super.start();
			}						
			
			context.setWrapperClass(SipServletImpl.class.getName());
			
			//annotations scanning
			ClassFileScanner scanner = new ClassFileScanner(((SipContext)context).getBasePath(), (SipContext)context);
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
				
				Digester sipDigester =  DigesterFactory.newDigester(xmlValidation,
	                    xmlNamespaceAware,
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
			super.start();
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
	protected synchronized void stop() {
		if(logger.isDebugEnabled()) {
			logger.debug("stopping sipContextConfig");
		}
		super.stop();
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
				if (path.equals("")) {
					docBase = "ROOT";
				} else {
					if (path.startsWith("/")) {
						docBase = path.substring(1);
					} else {
						docBase = path;
					}
				}
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
				URL war = new URL("jar:" + (new File(docBase)).toURL() + "!/");
				String contextPath = context.getPath();
				if (contextPath.equals("")) {
					contextPath = "ROOT";
				}
				docBase = ExpandWar.expand(host, war, contextPath);
				file = new File(docBase);
				docBase = file.getCanonicalPath();
				if (context instanceof SipContext) {
					FileDirContext fileDirContext =new FileDirContext();
					fileDirContext.setDocBase(docBase);
	                ((SipContext) context).setResources(fileDirContext );
	            }
			} else {
				File docDir = new File(docBase);
				if (!docDir.exists()) {
					String[] extensions = new String[] { ".sar", ".war" };
					for (String extension : extensions) {
						File archiveFile = new File(docBase + extension);
						if (archiveFile.exists()) {
							if (unpackWARs) {
								URL war = new URL("jar:" + archiveFile.toURL()
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
					if (context instanceof SipContext) {
						FileDirContext fileDirContext =new FileDirContext();
						fileDirContext.setDocBase(docBase);
		                ((SipContext) context).setResources(fileDirContext );
	                }
				}
			}
			if (docBase.startsWith(canonicalAppBase.getPath())) {
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
}
