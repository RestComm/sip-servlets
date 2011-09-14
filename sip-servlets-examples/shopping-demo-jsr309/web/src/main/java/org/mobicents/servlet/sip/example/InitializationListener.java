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

package org.mobicents.servlet.sip.example;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.media.mscontrol.MsControlFactory;
import javax.media.mscontrol.spi.Driver;
import javax.media.mscontrol.spi.DriverManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.jboss.mobicents.seam.util.MMSUtil;
import org.mobicents.javax.media.mscontrol.spi.DriverImpl;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 * 
 */
public class InitializationListener implements ServletContextListener {
	private static Logger logger = Logger.getLogger(InitializationListener.class);
	private static final String AUDIO_DIR = "/audio";
	private static final String FILE_PROTOCOL = "file://";
	private static final String[] AUDIO_FILES = new String[] {
			"AdminReConfirm.wav", "OrderApproved.wav", "OrderCancelled.wav", "OrderConfirmed.wav",
			"OrderConfirm.wav", "OrderDeliveryDate.wav", "OrderShipped.wav",
			"ReConfirm.wav" };
	
	Properties properties = null;
	private static final String MS_CONTROL_FACTORY = "MsControlFactory";
	public static final String PR_JNDI_NAME = "media/trunk/PacketRelay/$";
	
	// Property key for the Unique MGCP stack name for this application 
    public static final String MGCP_STACK_NAME = "mgcp.stack.name"; 
    // Property key for the IP address where CA MGCP Stack (SIP Servlet 
    // Container) is bound 
    public static final String MGCP_STACK_IP = "mgcp.server.address"; 
    // Property key for the port where CA MGCP Stack is bound 
    public static final String MGCP_STACK_PORT = "mgcp.local.port"; 
    // Property key for the IP address where MGW MGCP Stack (MMS) is bound 
    public static final String MGCP_PEER_IP = "mgcp.bind.address"; 
    // Property key for the port where MGW MGCP Stack is bound 
    public static final String MGCP_PEER_PORT = "mgcp.server.port"; 
	/**
	 * In this case MGW and CA are on same local host
	 */
	public static final String LOCAL_ADDRESS = System.getProperty(
			"jboss.bind.address", "127.0.0.1");
	protected static final String CA_PORT = "2728";

	public static final String PEER_ADDRESS = System.getProperty(
			"jboss.bind.address", "127.0.0.1");
	protected static final String MGW_PORT = "2427";

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */	
	public void contextDestroyed(ServletContextEvent arg0) {
		Iterator<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasNext()) {
			Driver driver = drivers.next();
			DriverManager.deregisterDriver(driver);
			DriverImpl impl = (DriverImpl) driver;
			impl.shutdown();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */	
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		ServletContext servletContext = servletContextEvent.getServletContext();

		File tempWriteDir = (File) servletContext
				.getAttribute("javax.servlet.context.tempdir");
		servletContext.setAttribute("audioFilePath", FILE_PROTOCOL + tempWriteDir
				.getAbsolutePath() + File.separatorChar);
		for (int i = 0; i < AUDIO_FILES.length; i++) {
			String audioFile = AUDIO_FILES[i];
			logger.info("Writing " + audioFile + " to webapp temp dir : "
					+ tempWriteDir);			
			InputStream is = servletContext.getResourceAsStream(AUDIO_DIR + "/" + audioFile);
			copyToTempDir(is, tempWriteDir, audioFile);
		} 

		Enumeration<String> initParamNames = servletContext
				.getInitParameterNames();
		logger.info("Setting init Params into application context");
		while (initParamNames.hasMoreElements()) {
			String initParamName = (String) initParamNames.nextElement();
			servletContext.setAttribute(initParamName, servletContext
					.getInitParameter(initParamName));
			logger.info("Param key=" + initParamName + ", value = "
					+ servletContext.getInitParameter(initParamName));
		}
		// map acting as a registrar
		servletContext.setAttribute("registeredUsersMap", new HashMap<String, String>());
		
		if(servletContextEvent.getServletContext().getAttribute(MS_CONTROL_FACTORY) == null) {
			DriverImpl d = new DriverImpl();
			properties = new Properties();
			properties.setProperty(MGCP_STACK_NAME, "SipServlets");
			properties.setProperty(MGCP_PEER_IP, PEER_ADDRESS);
			properties.setProperty(MGCP_PEER_PORT, MGW_PORT);
	
			properties.setProperty(MGCP_STACK_IP, LOCAL_ADDRESS);
			properties.setProperty(MGCP_STACK_PORT, CA_PORT);
	
			try {
				// create the Media Session Factory
				final MsControlFactory msControlFactory = new DriverImpl().getFactory(properties); 
				MsControlObjects.msControlFactory = msControlFactory;
				servletContextEvent.getServletContext().setAttribute(MS_CONTROL_FACTORY, msControlFactory);
				
				MMSUtil.msControlFactory = MsControlObjects.msControlFactory;
				String pathToAudioDirectory = (String)servletContextEvent.getServletContext().getAttribute("audioFilePath");
				MMSUtil.audioFilePath = pathToAudioDirectory;
				logger.info("started MGCP Stack on " + LOCAL_ADDRESS + "and port " + CA_PORT + " obj: " + MsControlObjects.msControlFactory);
			} catch (Exception e) {
				logger.error("couldn't start the underlying MGCP Stack", e);
			}
		} else {
			logger.info("MGCP Stack already started on " + LOCAL_ADDRESS + "and port " + CA_PORT);
		}
	}

	private void copyToTempDir(InputStream is, File tempWriteDir,
			String fileName) {
		File file = new File(tempWriteDir, fileName);

		final int bufferSize = 1000;
		BufferedOutputStream fout = null;
		BufferedInputStream fin = null;
		try {
			fout = new BufferedOutputStream(new FileOutputStream(file));
			fin = new BufferedInputStream(is);
			byte[] buffer = new byte[bufferSize];
			int readCount = 0;
			while ((readCount = fin.read(buffer)) != -1) {
				if (readCount < bufferSize) {
					fout.write(buffer, 0, readCount);
				} else {
					fout.write(buffer);
				}
			}
		} catch (IOException e) {
			logger.error("An unexpected exception occured while copying audio files",
							e);
		} finally {
			try {
				if (fout != null) {
					fout.flush();
					fout.close();
				}
			} catch (IOException e) {
				logger.error("An unexpected exception while closing stream",
								e);
			}
			try {
				if (fin != null) {
					fin.close();
				}
			} catch (IOException e) {
				logger.error("An unexpected exception while closing stream",
								e);
			}			
		}
	}

}
