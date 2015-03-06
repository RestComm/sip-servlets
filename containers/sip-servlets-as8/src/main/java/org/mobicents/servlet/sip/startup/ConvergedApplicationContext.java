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
package org.mobicents.servlet.sip.startup;

import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.ServletContainer;
import io.undertow.servlet.spec.ServletContextImpl;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.mobicents.servlet.sip.core.session.SipRequestDispatcher;
import org.mobicents.servlet.sip.undertow.SipServletImpl;
import org.mobicents.servlet.sip.undertow.UndertowSipContextDeployment;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class ConvergedApplicationContext extends ServletContextImpl {

    protected ConvergedApplicationContextFacade convergedFacade = null;

    public ConvergedApplicationContext(ServletContainer servletContainer, Deployment deployment) {
        super(servletContainer, deployment);

        convergedFacade = new ConvergedApplicationContextFacade(this, servletContainer, deployment);
    }

    public ServletContext getFacade() {
        return convergedFacade;
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        // Validate the name argument
        if (name == null)
            return (null);

        // Create and return a corresponding request dispatcher
        UndertowSipContextDeployment deployment = (UndertowSipContextDeployment) this.getDeployment();
        Servlet servlet = (Servlet) deployment.findSipServletByName(name);

        if (servlet == null)
            return super.getNamedDispatcher(name);
        // return (null);

        if (servlet instanceof SipServletImpl) {
            return new SipRequestDispatcher((SipServletImpl) servlet);
        } else {
            return super.getNamedDispatcher(name);
        }
    }
}
