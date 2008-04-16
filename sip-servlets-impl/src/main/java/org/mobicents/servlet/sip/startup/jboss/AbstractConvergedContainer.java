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
package org.mobicents.servlet.sip.startup.jboss;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.SubDeployer;
import org.jboss.metadata.WebMetaData;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.mx.loading.LoaderRepositoryFactory.LoaderRepositoryConfig;
import org.jboss.mx.util.ObjectNameFactory;
import org.jboss.util.file.JarUtils;
import org.jboss.web.AbstractWebContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This class extends the Abstract Web Container so that the .war hard coded value
 * is not mandatory and that .sar2 can be used instead
 * 
 * @author Jean Deruelle
 * 
 */
public abstract class AbstractConvergedContainer extends AbstractWebContainer {
	   	
	@Override
	public synchronized void init(DeploymentInfo di) throws DeploymentException {

		log.debug("Begin init");
		this.server = di.getServer();
		try {
			if (di.url.getPath().endsWith("/")) {
				// the URL is a unpacked collection, watch the deployment
				// descriptor
				di.watch = new URL(di.url, "WEB-INF/web.xml");
			} else {
				// just watch the original URL
				di.watch = di.url;
			}

			// We need to unpack the WAR if it has webservices, because we need
			// to manipulate th web.xml before deploying to the web container
			boolean unpackWebservice = di.localCl
					.findResource("WEB-INF/webservices.xml") != null;
			// With JSR-181 annotated JSE endpoints we need to do it as well
			// even if there is no webservices.xml
			unpackWebservice |= server.isRegistered(ObjectNameFactory
					.create("jboss.ws:service=ServiceEndpointManager"));

			// Make sure the war is unpacked if unpackWars is true
			File warFile = new File(di.localUrl.getFile());
			if (warFile.isDirectory() == false
					&& (unpackWars || unpackWebservice)) {
				// After findResource we cannot rename the WAR anymore, because
				// some systems keep an open reference to the file :(
				String prefix = warFile.getCanonicalPath();
				int prefixIndex = prefix.lastIndexOf(".");
				String extension = "war";
				if(prefixIndex < 0) {
					prefix = prefix.substring(0, prefixIndex);
					extension = prefix.substring(prefixIndex + 1, prefix.length());					
				}				
				File expWarFile = new File(prefix + "-exp." + extension);
				
				if (expWarFile.mkdir() == false)
					throw new DeploymentException("Was unable to mkdir: "
							+ expWarFile);
				log.debug("Unpacking " + extension + " to: " + expWarFile);
				FileInputStream fis = new FileInputStream(warFile);
				JarUtils.unjar(fis, expWarFile);
				fis.close();
				log.debug("Replaced " + extension + " with unpacked contents");
				if (warFile.delete() == false)
					log.debug("Was unable to delete " + extension + " file");
				else
					log.debug("Deleted " + extension + " archive");
				// Reset the localUrl to end in a '/'
				di.localUrl = expWarFile.toURL();
				// Reset the localCl to point to the file
				URL[] localCl = new URL[] { di.localUrl };
				di.localCl = new URLClassLoader(localCl);
			}

			WebMetaData metaData = new WebMetaData();
			metaData.setResourceClassLoader(di.localCl);
			metaData
					.setJava2ClassLoadingCompliance(this.java2ClassLoadingCompliance);
			di.metaData = metaData;

			String webContext = di.webContext;
			if (webContext != null) {
				if (webContext.length() > 0 && webContext.charAt(0) != '/')
					webContext = "/" + webContext;
			}
			// Get the war URL
			URL warURL = di.localUrl != null ? di.localUrl : di.url;
			log.debug("webContext: " + webContext);
			log.debug("warURL: " + warURL);

			// Parse the web.xml and jboss-web.xml descriptors
			parseMetaData(webContext, warURL, di.shortName, metaData);

			// Check for a loader-repository
			LoaderRepositoryConfig config = metaData.getLoaderConfig();
			if (config != null)
				di.setRepositoryInfo(config);

			// Generate an event for the initialization
			processNestedDeployments(di);

			emitNotification(SubDeployer.INIT_NOTIFICATION, di);
		} catch (DeploymentException e) {
			log.debug("Problem in init ", e);
			throw e;
		} catch (Exception e) {
			log.error("Problem in init ", e);
			throw new DeploymentException(e);
		}

		log.debug("End init");
	}

	/**
	 * This method creates a context-root string from either the
	 * WEB-INF/jboss-web.xml context-root element is one exists, or the filename
	 * portion of the warURL. It is called if the DeploymentInfo webContext
	 * value is null which indicates a standalone war deployment. A war name of
	 * ROOT.war is handled as a special case of a war that should be installed
	 * as the default web context.
	 */
	protected void parseMetaData(String ctxPath, URL warURL, String warName,
			WebMetaData metaData) throws DeploymentException {
		InputStream jbossWebIS = null;
		InputStream webIS = null;

		// Parse the war deployment descriptors, web.xml and jboss-web.xml
		try {
			// See if the warUrl is a directory
			File warDir = new File(warURL.getFile());
			if (warURL.getProtocol().equals("file")
					&& warDir.isDirectory() == true) {
				File webDD = new File(warDir, "WEB-INF/web.xml");
				if (webDD.exists() == true)
					webIS = new FileInputStream(webDD);
				File jbossWebDD = new File(warDir, "WEB-INF/jboss-web.xml");
				if (jbossWebDD.exists() == true)
					jbossWebIS = new FileInputStream(jbossWebDD);
			} else {
				// First check for a WEB-INF/web.xml and a WEB-INF/jboss-web.xml
				InputStream warIS = warURL.openStream();
				java.util.zip.ZipInputStream zipIS = new java.util.zip.ZipInputStream(
						warIS);
				java.util.zip.ZipEntry entry;
				byte[] buffer = new byte[512];
				int bytes;
				while ((entry = zipIS.getNextEntry()) != null) {
					if (entry.getName().equals("WEB-INF/web.xml")) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						while ((bytes = zipIS.read(buffer)) > 0) {
							baos.write(buffer, 0, bytes);
						}
						webIS = new ByteArrayInputStream(baos.toByteArray());
					} else if (entry.getName().equals("WEB-INF/jboss-web.xml")) {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						while ((bytes = zipIS.read(buffer)) > 0) {
							baos.write(buffer, 0, bytes);
						}
						jbossWebIS = new ByteArrayInputStream(baos
								.toByteArray());
					}
				}
				zipIS.close();
			}

			XmlFileLoader xmlLoader = new XmlFileLoader();
			String warURI = warURL.toExternalForm();
			try {
				if (webIS != null) {
					Document webDoc = xmlLoader.getDocument(webIS, warURI
							+ "/WEB-INF/web.xml");
					Element web = webDoc.getDocumentElement();
					metaData.importXml(web);
				}
			} catch (Exception e) {
				throw new DeploymentException(
						"Failed to parse WEB-INF/web.xml", e);
			}
			try {
				if (jbossWebIS != null) {
					Document jbossWebDoc = xmlLoader.getDocument(jbossWebIS,
							warURI + "/WEB-INF/jboss-web.xml");
					Element jbossWeb = jbossWebDoc.getDocumentElement();
					metaData.importXml(jbossWeb);
				}
			} catch (Exception e) {
				throw new DeploymentException(
						"Failed to parse WEB-INF/jboss-web.xml", e);
			}

		} catch (DeploymentException e) {
			throw e;
		} catch (Exception e) {
			log.warn("Failed to parse descriptors for war(" + warURL + ")", e);
		}

		// Build a war root context from the war name if one was not specified
		String webContext = ctxPath;
		if (webContext == null)
			webContext = metaData.getContextRoot();
		if (webContext == null) {
			// Build the context from the war name, strip the .war suffix
			webContext = warName;
			webContext = webContext.replace('\\', '/');
			if (webContext.endsWith("/"))
				webContext = webContext.substring(0, webContext.length() - 1);
			int prefix = webContext.lastIndexOf('/');
			if (prefix > 0)
				webContext = webContext.substring(prefix + 1);
			int suffix = webContext.lastIndexOf(".");
			if (suffix > 0)
				webContext = webContext.substring(0, suffix);			
			// Strip any '<int-value>.' prefix
			int index = 0;
			for (; index < webContext.length(); index++) {
				char c = webContext.charAt(index);
				if (Character.isDigit(c) == false && c != '.')
					break;
			}
			webContext = webContext.substring(index);
		}

		// Servlet containers are anal about the web context starting with '/'
		if (webContext.length() > 0 && webContext.charAt(0) != '/')
			webContext = "/" + webContext;
		// And also the default root context must be an empty string, not '/'
		else if (webContext.equals("/"))
			webContext = "";
		metaData.setContextRoot(webContext);
	}
}
