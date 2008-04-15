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
package org.mobicents.servlet.sip.core.session;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import javax.servlet.ServletContextListener;
import javax.servlet.sip.SipApplicationSessionActivationListener;
import javax.servlet.sip.SipApplicationSessionAttributeListener;
import javax.servlet.sip.SipApplicationSessionBindingListener;
import javax.servlet.sip.SipApplicationSessionListener;
import javax.servlet.sip.SipErrorListener;
import javax.servlet.sip.SipServletListener;
import javax.servlet.sip.SipSessionActivationListener;
import javax.servlet.sip.SipSessionAttributeListener;
import javax.servlet.sip.SipSessionBindingListener;
import javax.servlet.sip.SipSessionListener;
import javax.servlet.sip.TimerListener;

import org.apache.log4j.Logger;


public class SipListenersHolder {
	private static Logger logger = Logger.getLogger(SipListenersHolder.class);
	
	private List<SipApplicationSessionAttributeListener> sipApplicationSessionAttributeListeners;
	private List<SipApplicationSessionBindingListener> sipApplicationSessionBindingListeners;
	private List<SipApplicationSessionListener> sipApplicationSessionListeners;
	private List<SipApplicationSessionActivationListener> sipApplicationSessionActivationListeners;
	private List<SipSessionActivationListener> sipSessionActivationListeners;
	private List<SipSessionAttributeListener> sipSessionAttributeListeners;
	private List<SipSessionBindingListener> sipSessionBindingListeners;
	private List<SipSessionListener> sipSessionListeners;
	private List<SipServletListener> sipServletsListeners;
	private List<SipErrorListener> sipErrorListeners;
	private List<ServletContextListener> servletContextListeners;
	// There may be at most one TimerListener defined.
	private TimerListener timerListener;	

	/**
	 * Default Constructor
	 */
	public SipListenersHolder() {
		this.sipApplicationSessionAttributeListeners = new ArrayList<SipApplicationSessionAttributeListener>();
		this.sipApplicationSessionBindingListeners = new ArrayList<SipApplicationSessionBindingListener>();
		this.sipApplicationSessionListeners = new ArrayList<SipApplicationSessionListener>();
		this.sipApplicationSessionActivationListeners = new ArrayList<SipApplicationSessionActivationListener>();
		this.sipSessionActivationListeners = new ArrayList<SipSessionActivationListener>();
		this.sipSessionAttributeListeners = new ArrayList<SipSessionAttributeListener>();
		this.sipSessionBindingListeners = new ArrayList<SipSessionBindingListener>();
		this.sipSessionListeners = new ArrayList<SipSessionListener>();
		this.sipServletsListeners = new ArrayList<SipServletListener>();
		this.sipErrorListeners = new ArrayList<SipErrorListener>();
		this.servletContextListeners = new ArrayList<ServletContextListener>();		
	}
	
	/**
	 * Load Listeners the given in parameter with the given classloader  
	 * @param listenerList the list of listeners to load 
	 * @param classLoader the classloader to load the listeners with
	 * @return true if all the listeners have been successfully loaded, false otherwise
	 */
	public boolean loadListeners(String[] listeners,
			ClassLoader classLoader) {				
		// Instantiate all the listeners
		for (String className : listeners) {			
			try {
				EventListener listener = (EventListener) classLoader.loadClass(
						className).newInstance();
				//TODO Annotation processing                
				addListenerToBunch(listener);

			} catch (Exception e) {
				logger.fatal("Cannot instantiate listener class " + className,
						e);
				return false;
			}
		}
		return true;
	}

	private void addListenerToBunch(EventListener listener) {
		boolean added = false;
		if (listener instanceof SipApplicationSessionAttributeListener) {
			this.addListener((SipApplicationSessionAttributeListener) listener);
			added = true;
		}

		if (listener instanceof SipApplicationSessionBindingListener) {
			this.addListener((SipApplicationSessionBindingListener) listener);
			added = true;
		}

		if (listener instanceof SipApplicationSessionActivationListener) {
			this.addListener((SipApplicationSessionActivationListener) listener);
			added = true;
		}
		
		if (listener instanceof SipApplicationSessionListener) {
			this.addListener((SipApplicationSessionListener) listener);
			added = true;
		}

		if (listener instanceof SipSessionActivationListener) {
			this.addListener((SipSessionActivationListener) listener);
			added = true;
		}

		if (listener instanceof SipSessionAttributeListener) {
			this.addListener((SipSessionAttributeListener) listener);
			added = true;
		}

		if (listener instanceof SipSessionBindingListener) {
			this.addListener((SipSessionBindingListener) listener);
			added = true;
		}

		if (listener instanceof SipSessionListener) {
			this.addListener((SipSessionListener) listener);
			added = true;
		}

		if (listener instanceof SipServletListener) {
			this.addListener((SipServletListener) listener);
			added = true;
		}

		if (listener instanceof SipErrorListener) {
			this.addListener((SipErrorListener) listener);
			added = true;
		}

		if (listener instanceof ServletContextListener) {
			this.addListener((ServletContextListener) listener);
			added = true;
		}

		if (listener instanceof TimerListener) {
			this.setTimerListener((TimerListener) listener);
			added = true;
		}

		if(!added) {
			throw new IllegalArgumentException("Wrong type of LISTENER!!!["
				+ listener + "]");
		}
	}

	// this.sipApplicationSessionAttributeListeners.clear();
	public void addListener(SipApplicationSessionAttributeListener listener) {
		this.sipApplicationSessionAttributeListeners.add(listener);
	}

	// this.sipApplicationSessionBindingListeners.clear();
	public void addListener(SipApplicationSessionBindingListener listener) {
		this.sipApplicationSessionBindingListeners.add(listener);
	}

	public void addListener(SipApplicationSessionActivationListener listener) {
		this.sipApplicationSessionActivationListeners.add(listener);
	}
	
	// this.sipApplicationSessionListeners.clear();
	public void addListener(SipApplicationSessionListener listener) {
		this.sipApplicationSessionListeners.add(listener);
	}

	// this.sipSessionActivationListeners.clear();
	public void addListener(SipSessionActivationListener listener) {
		this.sipSessionActivationListeners.add(listener);
	}

	// this.sipSessionAttributeListeners.clear();
	public void addListener(SipSessionAttributeListener listener) {
		this.sipSessionAttributeListeners.add(listener);
	}

	// this.sipSessionBindingListeners.clear();
	public void addListener(SipSessionBindingListener listener) {
		this.sipSessionBindingListeners.add(listener);
	}

	// this.sipSessionListeners.clear();
	public void addListener(SipSessionListener listener) {
		this.sipSessionListeners.add(listener);
	}

	// this.sipSessionListeners.clear();
	public void addListener(SipServletListener listener) {
		this.sipServletsListeners.add(listener);
	}

	// this.sipErrorListeners.clear();
	public void addListener(SipErrorListener listener) {
		this.sipErrorListeners.add(listener);
	}

	// this.servletContextListeners.clear();
	public void addListener(ServletContextListener listener) {
		this.servletContextListeners.add(listener);
	}

	public void setTimerListener(TimerListener listener) {
		if(timerListener != null) {
			throw new IllegalArgumentException(
					"the time listener has already been set ("+timerListener.getClass().getName() +
					"), There may be at most one TimerListener defined !");
		}			 
		this.timerListener = listener;
	}

	public List<SipApplicationSessionAttributeListener> getSipApplicationSessionAttributeListeners() {
		return sipApplicationSessionAttributeListeners;
	}

	public List<SipApplicationSessionBindingListener> getSipApplicationSessionBindingListeners() {
		return sipApplicationSessionBindingListeners;
	}

	public List<SipApplicationSessionActivationListener> getSipApplicationSessionActivationListeners() {
		return sipApplicationSessionActivationListeners;
	}
	
	public List<SipApplicationSessionListener> getSipApplicationSessionListeners() {
		return sipApplicationSessionListeners;
	}

	public List<SipSessionActivationListener> getSipSessionActivationListeners() {
		return sipSessionActivationListeners;
	}

	public List<SipSessionAttributeListener> getSipSessionAttributeListeners() {
		return sipSessionAttributeListeners;
	}

	public List<SipSessionBindingListener> getSipSessionBindingListeners() {
		return sipSessionBindingListeners;
	}

	public List<SipSessionListener> getSipSessionListeners() {
		return sipSessionListeners;
	}

	public List<SipServletListener> getSipServletsListeners() {
		return sipServletsListeners;
	}

	public List<SipErrorListener> getSipErrorListeners() {
		return sipErrorListeners;
	}

	public List<ServletContextListener> getServletContextListeners() {
		return servletContextListeners;
	}

	public TimerListener getTimerListener() {
		return timerListener;
	}

	/**
	 * Empty vectors to allow garbage collection
	 */
	public void clean() {
		// TODO: This will be different since we propably will need to remove
		// also
		// all other listeners from Session for instance
		this.sipApplicationSessionAttributeListeners.clear();
		this.sipApplicationSessionBindingListeners.clear();
		this.sipApplicationSessionActivationListeners.clear();
		this.sipApplicationSessionListeners.clear();
		this.sipSessionActivationListeners.clear();
		this.sipSessionAttributeListeners.clear();
		this.sipSessionBindingListeners.clear();
		this.sipSessionListeners.clear();
		this.sipServletsListeners.clear();
		this.sipErrorListeners.clear();
		this.servletContextListeners.clear();
		this.timerListener = null;
	}

}
