package org.mobicents.servlet.sip.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Version {
	private static Logger logger = Logger.getLogger(Version.class);
	public static void printVersion() {
		if(logger.isInfoEnabled()) {
			Properties releaseProperties = new Properties();
			try {
				InputStream in = SipApplicationDispatcherImpl.class.getResourceAsStream("release.properties");
				if(in != null) {
					releaseProperties.load(in);
					in.close();
					String releaseVersion = releaseProperties.getProperty("release.version");
					String releaseName = releaseProperties.getProperty("release.name");
					String releaseDate = releaseProperties.getProperty("release.date");
					String releaseRevision = releaseProperties.getProperty("release.revision");
					if(releaseVersion != null) {
						// Follow the EAP Convention 
						// Release ID: JBoss [EAP] 5.0.1 (build: SVNTag=JBPAPP_5_0_1 date=201003301050)
						logger.info("Release ID: (" + releaseName + ") Sip Servlets " + releaseVersion + " (build: SVNTag=" + releaseRevision + " date=" + releaseDate + ")");
						logger.info(releaseName + " Sip Servlets " + releaseVersion + " (build: revision=" + releaseRevision + " date=" + releaseDate + ") Started.");
					} else {
						logger.warn("Unable to extract the version of Mobicents Sip Servlets currently running");
					}
				} else {
					logger.warn("Unable to extract the version of Mobicents Sip Servlets currently running");
				}
			} catch (IOException e) {
				logger.warn("Unable to extract the version of Mobicents Sip Servlets currently running", e);
			}		
		}
	}
	
	public static String getVersion() {
		Properties releaseProperties = new Properties();
		try {
			InputStream in = SipApplicationDispatcherImpl.class.getResourceAsStream("release.properties");
			if(in != null) {
				releaseProperties.load(in);
				in.close();
				String releaseVersion = releaseProperties.getProperty("release.version");
				String releaseName = releaseProperties.getProperty("release.name");
				String releaseDate = releaseProperties.getProperty("release.date");
				String releaseRevision = releaseProperties.getProperty("release.revision");

				return "Release ID: (" + releaseName + ") Sip Servlets " + releaseVersion + " (build: SVNTag=" + releaseRevision + " date=" + releaseDate + ")";
			}
		} catch (Exception e) {
			logger.warn("Unable to extract the version of Mobicents Sip Servlets currently running", e);
		}	
		return null;
	}
}
