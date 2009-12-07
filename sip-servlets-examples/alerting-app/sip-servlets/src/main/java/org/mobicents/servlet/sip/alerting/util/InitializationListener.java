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
package org.mobicents.servlet.sip.alerting.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 * 
 */
public class InitializationListener implements ServletContextListener {
	private static Logger logger = Logger.getLogger(InitializationListener.class);
	private static final String AUDIO_DIR = "/audio";
	private static final String FILE_PROTOCOL = "file://";
	private static final String[] AUDIO_FILES = new String[] {};

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */	
	public void contextDestroyed(ServletContextEvent arg0) {
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
		String audioFilePath =FILE_PROTOCOL + tempWriteDir.getAbsolutePath() + File.separatorChar;
		servletContext.setAttribute("audioFilePath", audioFilePath);
		logger.info("Setting audioFilePath param to " + audioFilePath);
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
