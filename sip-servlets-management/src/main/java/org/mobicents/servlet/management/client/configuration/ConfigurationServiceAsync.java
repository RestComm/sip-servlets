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


import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ConfigurationServiceAsync {

	void setQueueSize(int queueSize, AsyncCallback<Void> callback);
	void getQueueSize(AsyncCallback<Integer> callback);
	void setMemoryThreshold(int memoryThreshold, AsyncCallback<Void> callback);
	void getMemoryThreshold(AsyncCallback<Integer> callback);
	void getConcurrencyControlMode(AsyncCallback<String> callback);
	void setConcurrencyControlMode(String mode, AsyncCallback<Void> callback);
	void getCongestionControlPolicy(AsyncCallback<String> callback);
	void setCongestionControlPolicy(String policy, AsyncCallback<Void> callback);
	void setCongestionControlCheckingInterval(long interval, AsyncCallback<Void> callback);
	void getCongestionControlCheckingInterval(AsyncCallback<Long> callback);
	void setBaseTimerInterval(int baseTimerInterval, AsyncCallback<Void> callback);
	void getBaseTimerInterval(AsyncCallback<Integer> callback);
	void setT2Interval(int t2Interval, AsyncCallback<Void> callback);
	void getT2Interval(AsyncCallback<Integer> callback);
	void setT4Interval(int t4Interval, AsyncCallback<Void> callback);
	void getT4Interval(AsyncCallback<Integer> callback);
	void setTimerDInterval(int timerDInterval, AsyncCallback<Void> callback);
	void getTimerDInterval(AsyncCallback<Integer> callback);
	void getLoggingMode(AsyncCallback<String> callback);
	void setLoggingMode(String loggingMode, AsyncCallback<Void> callback);
	void listLoggingProfiles(AsyncCallback<String[]> callback);
}
