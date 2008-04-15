package org.mobicents.servlet.sip.proxy;

import javax.servlet.sip.SipURI;

public class ProxyParams
{
	public ProxyParams(SipURI target, SipURI stack, SipURI routeRecord)
	{
		this.target = target;
		this.stack = stack;
		this.routeRecord = routeRecord;
	}
	public SipURI target;
	public SipURI stack;
	public SipURI routeRecord;
}