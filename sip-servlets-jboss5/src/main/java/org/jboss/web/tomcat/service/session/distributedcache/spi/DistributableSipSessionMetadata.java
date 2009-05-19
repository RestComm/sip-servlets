/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.web.tomcat.service.session.distributedcache.spi;

import gov.nist.javax.sip.stack.SIPDialog;

import javax.servlet.sip.ar.SipApplicationRoutingRegion;

import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipSessionKey;
import org.mobicents.servlet.sip.message.B2buaHelperImpl;
import org.mobicents.servlet.sip.proxy.ProxyImpl;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class DistributableSipSessionMetadata extends
		DistributableSessionMetadata {
	private SipApplicationSessionKey sipApplicationSessionKey;
	private SipSessionKey sipSessionKey;
	private SipApplicationRoutingRegion routingRegion;
	private String handlerServlet;
	private String subscriberURI;
	private SIPDialog sipDialog;
	private boolean invalidateWhenReady;
	private boolean readyToInvalidate;
	private ProxyImpl proxy;
	private B2buaHelperImpl b2buaHelper;
	/**
	 * @param sipApplicationSessionKey the sipApplicationSessionKey to set
	 */
	public void setSipApplicationSessionKey(SipApplicationSessionKey sipApplicationSessionKey) {
		this.sipApplicationSessionKey = sipApplicationSessionKey;
	}

	/**
	 * @return the sipApplicationSessionKey
	 */
	public SipApplicationSessionKey getSipApplicationSessionKey() {
		return sipApplicationSessionKey;
	}

	/**
	 * @param sipSessionKey the sipSessionKey to set
	 */
	public void setSipSessionKey(SipSessionKey sipSessionKey) {
		this.sipSessionKey = sipSessionKey;
	}

	/**
	 * @return the sipSessionKey
	 */
	public SipSessionKey getSipSessionKey() {
		return sipSessionKey;
	}

	/**
	 * @return the routingRegion
	 */
	public SipApplicationRoutingRegion getRoutingRegion() {
		return routingRegion;
	}

	/**
	 * @param routingRegion the routingRegion to set
	 */
	public void setRoutingRegion(SipApplicationRoutingRegion routingRegion) {
		this.routingRegion = routingRegion;
	}

	/**
	 * @return the handlerServlet
	 */
	public String getHandlerServlet() {
		return handlerServlet;
	}

	/**
	 * @param handlerServlet the handlerServlet to set
	 */
	public void setHandlerServlet(String handlerServlet) {
		this.handlerServlet = handlerServlet;
	}

	/**
	 * @return the subscriberURI
	 */
	public String getSubscriberURI() {
		return subscriberURI;
	}

	/**
	 * @param subscriberURI the subscriberURI to set
	 */
	public void setSubscriberURI(String subscriberURI) {
		this.subscriberURI = subscriberURI;
	}

	/**
	 * @return the sipDialog
	 */
	public SIPDialog getSipDialog() {
		return sipDialog;
	}

	/**
	 * @param sipDialog the sipDialog to set
	 */
	public void setSipDialog(SIPDialog sipDialog) {
		this.sipDialog = sipDialog;
	}

	/**
	 * @return the invalidateWhenReady
	 */
	public boolean isInvalidateWhenReady() {
		return invalidateWhenReady;
	}

	/**
	 * @param invalidateWhenReady the invalidateWhenReady to set
	 */
	public void setInvalidateWhenReady(boolean invalidateWhenReady) {
		this.invalidateWhenReady = invalidateWhenReady;
	}

	/**
	 * @return the readyToInvalidate
	 */
	public boolean isReadyToInvalidate() {
		return readyToInvalidate;
	}

	/**
	 * @param readyToInvalidate the readyToInvalidate to set
	 */
	public void setReadyToInvalidate(boolean readyToInvalidate) {
		this.readyToInvalidate = readyToInvalidate;
	}

	/**
	 * @return the proxy
	 */
	public ProxyImpl getProxy() {
		return proxy;
	}

	/**
	 * @param proxy the proxy to set
	 */
	public void setProxy(ProxyImpl proxy) {
		this.proxy = proxy;
	}

	/**
	 * @return the b2buaHelper
	 */
	public B2buaHelperImpl getB2buaHelper() {
		return b2buaHelper;
	}

	/**
	 * @param helper the b2buaHelper to set
	 */
	public void setB2buaHelper(B2buaHelperImpl helper) {
		b2buaHelper = helper;
	}	
}
