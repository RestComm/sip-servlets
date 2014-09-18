/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2013, Telestax Inc and individual contributors
 * by the @authors tag.
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
package org.mobicents.servlet.sip.arquillian.testsuite;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.ProxyBranch;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.mobicents.javax.servlet.sip.ProxyBranchListener;
import org.mobicents.javax.servlet.sip.ResponseType;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 *
 */
@javax.servlet.sip.annotation.SipServlet(loadOnStartup=1, applicationName="SimpleSipServletApplication")
@javax.servlet.sip.annotation.SipListener
public class ProxySipServlet extends SipServlet  implements ProxyBranchListener {

    private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(ProxySipServlet.class);

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        logger.info("the proxy sip servlet has been started");
        super.init(servletConfig);
    }

    @Override
    protected void doRegister(SipServletRequest req) throws ServletException, IOException {
        logger.info("Got REGISTER: \n"+req+"\n");
        req.createResponse(200).send();
    }

    @Override
    protected void doInvite(SipServletRequest request) throws ServletException, IOException {
        URI toURI = request.getTo().getURI();
        Proxy proxy = request.getProxy();
        proxy.setRecordRoute(true);
        proxy.setProxyTimeout(3); // Set the proxy timeout in seconds
        proxy.proxyTo(toURI);
    }

    @Override
    public void onProxyBranchResponseTimeout(ResponseType responseType, ProxyBranch proxyBranch) {
        logger.info("onProxyBranchResponseTimeout callback was called. responseType = " + responseType + " , branch = " + proxyBranch + ", request " + proxyBranch.getRequest() + ", response " + proxyBranch.getResponse());
        //Just clean up here, container will send back 408 Request Timeout
    }
}
