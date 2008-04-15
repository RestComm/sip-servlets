package org.mobicents.servlet.sip.proxy;

import javax.servlet.sip.SipURI;

public class ProxyParams
{
	public ProxyParams(SipURI destination, SipURI outboundInterface, SipURI routeRecord, SipURI path)
	{
		this.destination = destination;
		this.outboundInterface = outboundInterface;
		this.routeRecord = routeRecord;
		this.path = path;
	}
	public SipURI destination;
	public SipURI outboundInterface;
	public SipURI routeRecord;
	public SipURI path;// for REGISTER path header
}