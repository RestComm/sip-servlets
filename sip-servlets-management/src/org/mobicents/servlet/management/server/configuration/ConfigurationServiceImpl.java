package org.mobicents.servlet.management.server.configuration;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.catalina.mbeans.MBeanUtils;
import org.mobicents.servlet.management.client.configuration.*;
import org.mobicents.servlet.sip.core.ConcurrencyControlMode;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConfigurationServiceImpl  extends RemoteServiceServlet implements ConfigurationService {
	private static MBeanServer mserver = MBeanUtils.createServer();
	
	private ObjectName getApplicationDispatcher() {
		try {
			ObjectName dispatcherQuery = new ObjectName("*:type=SipApplicationDispatcher");
			ObjectInstance dispatcherInstance = (ObjectInstance) 
			mserver.queryMBeans(dispatcherQuery, null).iterator().next();
			ObjectName dispatcherName = dispatcherInstance.getObjectName();
			return dispatcherName;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	public String getConcurrencyControlMode() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			ConcurrencyControlMode mode = 
				(ConcurrencyControlMode) mserver.getAttribute(
						dispatcherName, "concurrencyControlMode");
			return mode.toString();
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public int getQueueSize() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			String size = 
				mserver.getAttribute(
						dispatcherName, "queueSize").toString();
			return Integer.parseInt(size);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public void setConcurrencyControlMode(String mode) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			Attribute att = new Attribute("concurrencyControlMode", ConcurrencyControlMode.valueOf(mode));
			mserver.setAttribute(dispatcherName, att);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public void setQueueSize(int queueSize) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			Attribute att = new Attribute("queueSize", new Integer(queueSize));
			mserver.setAttribute(dispatcherName, att);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
		
	}

}
