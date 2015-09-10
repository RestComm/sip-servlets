/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.mobicents.io.undertow.servlet.spec;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.session.ConvergedSession;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.undertow.SipContextImpl;
import org.mobicents.servlet.sip.undertow.UndertowSipManager;
import org.wildfly.extension.undertow.HttpListenerService;
import org.wildfly.extension.undertow.ListenerService;

/**
 * This class handles the additionnal sip features of a converged session
 * It is a delegate since it is used by many http session implementations classes (Standard and clustered ones)
 *
 *
 * @author Jean Deruelle
 * @author Vladimir Ralev
 *
 *         This class is based on the contents of org.mobicents.servlet.sip.catalina.session.ConvergedSessionDelegate class from sip-servlet-as7 project,
 *         re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 */
public class ConvergedSessionDelegate {
    private static final Logger logger = Logger.getLogger(ConvergedSessionDelegate.class);
    // We are storing the app session id in the http sessions (since they are replicated) under this key
    private static final String APPLICATION_SESSION_ID_ATTRIBUTE_NAME = "org.mobicents.servlet.sip.SipApplicationSessionId";

    protected UndertowSipManager sipManager;
    protected ConvergedSession httpSession;

    /**
     *
     * @param sessionManager
     */
    public ConvergedSessionDelegate(UndertowSipManager manager, ConvergedSession httpSession) {
        this.sipManager = manager;
        this.httpSession = httpSession;
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.sip.ConvergedHttpSession#encodeURL(java.lang.String)
     */
    public String encodeURL(String url) {

        StringBuffer urlEncoded = new StringBuffer();
        int indexOfQuestionMark = url.indexOf("?");
        if(indexOfQuestionMark > 0) {
            //Handles those cases :
            //http://forums.searchenginewatch.com/showthread.php?t=9817
            //http://forums.searchenginewatch.com/showthread.php?p=72232#post72232
            String urlBeforeQuestionMark = url.substring(0, indexOfQuestionMark);
            String urlAfterQuestionMark = url.substring(indexOfQuestionMark);
            urlEncoded = urlEncoded.append(urlBeforeQuestionMark);
            urlEncoded = urlEncoded.append(";jsessionid=");
            urlEncoded = urlEncoded.append(httpSession.getId());
            urlEncoded = urlEncoded.append(urlAfterQuestionMark);
        } else {
            //Handles those cases :
            //http://www.seroundtable.com/archives/003204.html#more
            //http://www.seroundtable.com/archives#more
            int indexOfPoundSign = url.indexOf("#");
            if(indexOfPoundSign > 0) {
                String urlBeforePoundSign = url.substring(0, indexOfPoundSign);
                String urlAfterPoundSign = url.substring(indexOfPoundSign);
                urlEncoded = urlEncoded.append(urlBeforePoundSign);
                urlEncoded = urlEncoded.append(";jsessionid=");
                urlEncoded = urlEncoded.append(httpSession.getId());
                urlEncoded = urlEncoded.append(urlAfterPoundSign);
            } else {
                //Handles the rest
                //http://www.seroundtable.com/archives/003204.html
                //http://www.seroundtable.com/archives
                urlEncoded = urlEncoded.append(url);
                urlEncoded = urlEncoded.append(";jsessionid=");
                urlEncoded = urlEncoded.append(httpSession.getId());
            }
        }

        return urlEncoded.toString();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.sip.ConvergedHttpSession#encodeURL(java.lang.String, java.lang.String)
     */
    public String encodeURL(String relativePath, String scheme) {
        StringBuffer urlEncoded = new StringBuffer();
        //Context
        SipContextImpl context = (SipContextImpl)sipManager.getContainer();
        //ListenersService
        List<ListenerService<?>> webServerListeners = context.getWebServerListeners();
        //Host
        String hostname = context.getDeploymentInfoFacade().getDeploymentInfo().getHostName();
        //kakonyii: get aliases from undrertow's host service!
        Set<String> aliases = context.getHostOfDeployment().getAllAliases();

        // Needed for http://code.google.com/p/sipservlets/issues/detail?id=150
        // to pass TCK test com.bea.sipservlet.tck.agents.api.javax_servlet_sip.ConvergedHttpSessionTest.testEncodeURL002
        if(aliases.size() >= 1) {
            // FIXME would be better to try to match the alias by some path or IP but unaware if this is really needed at this point
            // or how it can be effectively done
            hostname = (String)aliases.toArray()[0];
        }

        //retrieving the port corresponding to the specified scheme
        //TODO ask EG what if the scheme is not supported on the server ?
        //kakonyii: find out the port from undertow's web server listeners
        int port = -1;
        int i = 0;
        while (i < webServerListeners.size() && port < 0) {
            ListenerService<?> listener = webServerListeners.get(i);
            if(scheme != null && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) && listener instanceof HttpListenerService) {
                port = listener.getBinding().getValue().getPort();
            }
            i++;
        }
        urlEncoded = urlEncoded.append(scheme);
        urlEncoded = urlEncoded.append("://");
        urlEncoded = urlEncoded.append(hostname);
        urlEncoded = urlEncoded.append(":");
        urlEncoded = urlEncoded.append(port);
        //check that at SipContextImpl.getPath():
        urlEncoded = urlEncoded.append(context.getPath());
        urlEncoded = urlEncoded.append(encodeURL(relativePath));
        return urlEncoded.toString();
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.sip.ConvergedHttpSession#getApplicationSession()
     */
    public MobicentsSipApplicationSession getApplicationSession(boolean create) {

        //First check if the http session has the app session id in its attributes
        SipApplicationSessionKey key = null;
        // need to check if the session is still valid
        if(httpSession.isValidIntern()) {
            key = (SipApplicationSessionKey) httpSession.getAttribute(APPLICATION_SESSION_ID_ATTRIBUTE_NAME);
        }
        SipContext sipContext = (SipContext)sipManager.getContainer();
        if(key != null) {
            MobicentsSipApplicationSession sipAppSession = sipManager.getSipApplicationSession(key, false);
            if (sipAppSession != null) {
                // make sure to acquire this app session and add it to the set of app sessions we monitor in the context of the application
                // to release them all when we exit application code
                sipContext.enterSipApp(sipAppSession, null, true, true);
                return sipAppSession;
            } else {
                if(logger.isDebugEnabled()) {
                    logger.debug(key + " found in http session " + httpSession.getId() + " but not present in the manager. already invalidated ? creating new sip application session");
                }
            }
        }

        //Otherwise proceed as normally

        //the application session if currently associated is returned,
        MobicentsSipApplicationSession sipApplicationSession =
            sipManager.findSipApplicationSession(httpSession);
        if(sipApplicationSession == null && create) {
            //however if no application session is associated it is created,
            //associated with the HttpSession and returned.

            //not needed anymore since the sipappsesionkey is not a callid anymore but a rnadom uuid
            //ExtendedListeningPoint listeningPoint =
            //sipNetworkInterfaceManager.getExtendedListeningPoints().next();
            SipApplicationSessionKey sipApplicationSessionKey = SessionManagerUtil.getSipApplicationSessionKey(
                    sipContext.getApplicationName(),
                    null,
                    null);

            sipApplicationSession =
                    sipManager.getSipApplicationSession(sipApplicationSessionKey, true);

            // Store the App Session ID in the HTTP sessions to recover it from there when it's transfered to a new node.
            httpSession.setAttribute(APPLICATION_SESSION_ID_ATTRIBUTE_NAME,
                    sipApplicationSession.getKey());
            sipApplicationSession.addHttpSession(httpSession);
        }
        if(sipApplicationSession != null) {
            // make sure to acquire this app session and add it to the set of app sessions we monitor in the context of the application
            // to release them all when we exit application code
            sipContext.enterSipApp(sipApplicationSession, null, true, true);
            return sipApplicationSession.getFacade();
        } else {
            return null;
        }
//      return sipApplicationSession;
    }
}
