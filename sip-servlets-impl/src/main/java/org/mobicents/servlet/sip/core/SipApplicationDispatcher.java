package org.mobicents.servlet.sip.core;

import java.util.Map;

import javax.sip.SipListener;

import org.apache.catalina.Wrapper;

public interface SipApplicationDispatcher extends SipListener {

	public void init();
	
	//public void start();
	
	//public void stop();
	
	public void addSipApplication(String sipApplicationName, Map<String, ? extends Wrapper> sipApplicationServlets);
	
	public void removeSipApplication(String sipApplication);
}
