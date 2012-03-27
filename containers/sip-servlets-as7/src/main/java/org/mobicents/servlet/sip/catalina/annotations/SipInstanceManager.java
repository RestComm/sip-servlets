package org.mobicents.servlet.sip.catalina.annotations;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.tomcat.InstanceManager;

public interface SipInstanceManager extends InstanceManager {

	public void processAnnotations(Object instance, Map<String, String> injections)
	      throws IllegalAccessException, InvocationTargetException, NamingException;

	public Map<String, String> getInjectionMap(String name);
	
	public void setContext(Context context);
	public Context getContext();
}
