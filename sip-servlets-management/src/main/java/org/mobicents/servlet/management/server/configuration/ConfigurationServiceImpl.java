package org.mobicents.servlet.management.server.configuration;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.catalina.mbeans.MBeanUtils;
import org.mobicents.servlet.management.client.configuration.ConfigurationService;
import org.mobicents.servlet.sip.annotation.ConcurrencyControlMode;
import org.mobicents.servlet.sip.core.CongestionControlPolicy;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConfigurationServiceImpl  extends RemoteServiceServlet implements ConfigurationService {
	static final long serialVersionUID = 1L;
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
	
	public int getBaseTimerInterval() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			String size = 
				mserver.getAttribute(
						dispatcherName, "baseTimerInterval").toString();
			return Integer.parseInt(size);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public void setConcurrencyControlMode(String mode) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			mserver.invoke(dispatcherName, "setConcurrencyControlModeByName", new Object[]{mode}, new String[]{"java.lang.String"});
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
	
	public void setBaseTimerInterval(int baseTimerInterval) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			Attribute att = new Attribute("baseTimerInterval", new Integer(baseTimerInterval));
			mserver.setAttribute(dispatcherName, att);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
		
	}
	
	public String getCongestionControlPolicy() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			CongestionControlPolicy policy = 
				(CongestionControlPolicy) mserver.getAttribute(
						dispatcherName, "congestionControlPolicy");
			return policy.toString();
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public int getMemoryThreshold() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			String memoryThreshold = 
				mserver.getAttribute(
						dispatcherName, "memoryThreshold").toString();
			return Integer.parseInt(memoryThreshold);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public void setCongestionControlPolicy(String policy) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();			
			mserver.invoke(dispatcherName, "setCongestionControlPolicyByName", new Object[]{policy}, new String[]{"java.lang.String"});
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public void setMemoryThreshold(int memoryThreshold) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			Attribute att = new Attribute("memoryThreshold", new Integer(memoryThreshold));
			mserver.setAttribute(dispatcherName, att);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
		
	}
	
	public long getCongestionControlCheckingInterval() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			String checkingInterval = 
				mserver.getAttribute(
						dispatcherName, "congestionControlCheckingInterval").toString();
			return Long.parseLong(checkingInterval);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public void setCongestionControlCheckingInterval(long interval) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			Attribute att = new Attribute("congestionControlCheckingInterval", new Long(interval));
			mserver.setAttribute(dispatcherName, att);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
		
	}

}
