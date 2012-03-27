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

package org.mobicents.servlet.management.client.configuration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public interface ConfigurationService extends RemoteService {
	public static class Util {

		public static ConfigurationServiceAsync getInstance() {

			ConfigurationServiceAsync instance = (ConfigurationServiceAsync) GWT
					.create(ConfigurationService.class);
			ServiceDefTarget target = (ServiceDefTarget) instance;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + SERVICE_URI);
			return instance;
		}
		
		public static ConfigurationService getSyncInstance() {

			ConfigurationService instance = (ConfigurationService) GWT
					.create(ConfigurationService.class);
			ServiceDefTarget target = (ServiceDefTarget) instance;
			target.setServiceEntryPoint(GWT.getModuleBaseURL() + SYNC_SERVICE_URI);
			return instance;
		}
	}
	public static final String SERVICE_URI = "/ConfigurationService";
	public static final String SYNC_SERVICE_URI = "/ConfigurationServiceSync";
	
	void setQueueSize(int queueSize);
	int getQueueSize();
	public void setMemoryThreshold(int memoryThreshold);
	public int getMemoryThreshold();
	String getConcurrencyControlMode();
	void setConcurrencyControlMode(String mode);
	public String getCongestionControlPolicy();
	public void setCongestionControlPolicy(String congestionControlPolicy);
	public long getCongestionControlCheckingInterval();
	public void setCongestionControlCheckingInterval(long interval);
	void setBaseTimerInterval(int baseTimerInterval);
	int getBaseTimerInterval();
	void setT2Interval(int t2Interval);
	int getT2Interval();
	void setT4Interval(int t4Interval);
	int getT4Interval();
	void setTimerDInterval(int timerDInterval);
	int getTimerDInterval();
	String getLoggingMode();
	void setLoggingMode(String loggingMode);
	String[] listLoggingProfiles();
}
