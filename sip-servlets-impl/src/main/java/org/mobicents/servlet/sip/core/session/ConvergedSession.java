/**
 * 
 */
package org.mobicents.servlet.sip.core.session;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.servlet.http.HttpSession;
import javax.servlet.sip.ConvergedHttpSession;
import javax.servlet.sip.SipApplicationSession;

import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Manager;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.security.SecurityUtil;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.message.SipFactoryImpl;
import org.mobicents.servlet.sip.startup.SipContext;

/**
 * Extension of the Tomcat StandardSession class so that applications
 * are able to cast session to javax.servlet.sip.ConvergedHttpSession interface.
 * 
 * @author Jean Deruelle
 *
 */
public class ConvergedSession 
		extends org.apache.catalina.session.StandardSession 
		implements ConvergedHttpSession {

	protected SipFactoryImpl sipFactoryImpl;		
	
	/**
     * The facade associated with this session.  NOTE:  This value is not
     * included in the serialized version of this object.
     */
    protected transient ConvergedSessionFacade facade = null;
	/**
	 * 
	 * @param sessionManager
	 */
	public ConvergedSession(Manager manager, SipFactoryImpl sipFactoryImpl) {
		super(manager);
		this.sipFactoryImpl = sipFactoryImpl;
	}
	
	@Override
	public HttpSession getSession() {
        if (facade == null){
            if (SecurityUtil.isPackageProtectionEnabled()){
                final ConvergedSession fsession = this;
                facade = (ConvergedSessionFacade)AccessController.doPrivileged(new PrivilegedAction(){
                    public Object run(){
                        return new ConvergedSessionFacade(fsession);
                    }
                });
            } else {
                facade = new ConvergedSessionFacade(this);
            }
        }
        return (facade);
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ConvergedHttpSession#encodeURL(java.lang.String)
	 */
	public String encodeURL(String url) {
		StringBuffer urlEncoded = new StringBuffer();
		urlEncoded = urlEncoded.append(url);
		urlEncoded = urlEncoded.append(";jsessionid=");
		urlEncoded = urlEncoded.append(getId());
		return urlEncoded.toString();
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ConvergedHttpSession#encodeURL(java.lang.String, java.lang.String)
	 */
	public String encodeURL(String relativePath, String scheme) {
		StringBuffer urlEncoded = new StringBuffer();
		//Context
		Context context = (Context)manager.getContainer();
		//Host
		Host host = (Host)context.getParent();
		//Service
		Service service = ((Engine)host.getParent()).getService();
		String hostname = host.getName();
		//retrieving the port corresponding to the specified scheme
		//TODO ask EG what if the scheme is not supported on the server ?
		int port = -1;		
		Connector[] connectors = service.findConnectors();
		int i = 0;
		while (i < connectors.length && port < 0) {
			if(scheme != null && scheme.equalsIgnoreCase(connectors[i].getProtocol())) {
				port = connectors[i].getPort();
			}
			i++;
		}
		urlEncoded = urlEncoded.append(scheme);
		urlEncoded = urlEncoded.append("://");
		urlEncoded = urlEncoded.append(hostname);
		urlEncoded = urlEncoded.append(":");
		urlEncoded = urlEncoded.append(port);
		urlEncoded = urlEncoded.append("/");
		urlEncoded = urlEncoded.append(((Context)manager.getContainer()).getPath());
		urlEncoded = urlEncoded.append(relativePath);
		urlEncoded = urlEncoded.append(";jsessionid=");
		urlEncoded = urlEncoded.append(getId());
		return urlEncoded.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.ConvergedHttpSession#getApplicationSession()
	 */
	public SipApplicationSession getApplicationSession() {
		//the application session if currently associated is returned, 
		SipApplicationSessionImpl sipApplicationSession =
			sipFactoryImpl.getSessionManager().findSipApplicationSession(this);
		if(sipApplicationSession == null) {
			//however if no application session is associated it is created, 
			//associated with the HttpSession and returned.
			SipApplicationSessionKey sipApplicationSessionKey = SessionManager.getSipApplicationSessionKey(
					((SipContext)manager.getContainer()).getApplicationName(), 
					JainSipUtils.findMatchingSipProvider(sipFactoryImpl.getSipProviders(), "udp").getNewCallId().getCallId());
			sipApplicationSession = 
				sipFactoryImpl.getSessionManager().getSipApplicationSession(sipApplicationSessionKey, true);
			if(manager.getContainer() instanceof SipContext) {
				sipApplicationSession.setSipContext((SipContext)manager.getContainer());
			}
			sipApplicationSession.addHttpSession(this);
		}
		return sipApplicationSession;
	}

}
