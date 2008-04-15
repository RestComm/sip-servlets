/**
 * 
 */
package org.mobicents.servlet.sip.startup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletContext;

import org.apache.catalina.Host;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.ExpandWar;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Startup event listener for a the <b>SipStandardContext</b> that configures
 * the properties of that Context, and the associated defined servlets.
 * 
 * @author Jean Deruelle
 * 
 */
public class SipContextConfig extends ContextConfig implements
		LifecycleListener {

	private static final String APPLICATION_SIP_XML = "/WEB-INF/sip.xml";

	private static transient Log logger = LogFactory
			.getLog(SipContextConfig.class.getCanonicalName());

	/**
	 * {@inheritDoc}
	 */
	public void lifecycleEvent(LifecycleEvent event) {
		logger.info("got lifecycle event : " + event.getType());
		
		try {
			super.lifecycleEvent(event);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		logger.info("lifecycle event handled");
	}

	@Override
	protected synchronized void start() {
		logger.info("starting sipContextConfig");
		ServletContext servletContext = context.getServletContext();
		// calling start on the parent to initialize web resources of the web
		// app if any
		InputStream webXmlInputStream = servletContext
				.getResourceAsStream(Constants.ApplicationWebXml);
		if (webXmlInputStream != null) {
			super.start();
		}
		InputStream sipXmlInputStream = servletContext
				.getResourceAsStream(APPLICATION_SIP_XML);
		// processing of the sip.xml file
		if (sipXmlInputStream != null) {
			log.info(APPLICATION_SIP_XML + " has been found !");
			try {
		
			} catch (Exception e) {
				log.error("Impossible to parse the sip deployment descriptor",
						e);
				ok = false;
			}
		} else {
			log.info(APPLICATION_SIP_XML + " has not been found !");
			ok = false;
		}
		// Make our application available if no problems were encountered
		if (ok) {
			context.setConfigured(true);
		} else {
			logger.warn("contextConfig.unavailable");
			context.setAvailable(false);
		}
		logger.info("sipContextConfig started");
	}

	@Override
	protected synchronized void stop() {
		logger.info("stopping sipContextConfig");
		super.stop();
		logger.info("sipContextConfig stopped");
	}

	/**
	 * Adjust docBase.
	 */
	protected void fixDocBase() throws IOException {
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
	}
}
