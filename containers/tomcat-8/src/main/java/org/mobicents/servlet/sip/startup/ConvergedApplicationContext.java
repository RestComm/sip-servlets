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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ApplicationContext;
import org.apache.catalina.core.StandardContext;
import org.mobicents.servlet.sip.catalina.SipServletImpl;
import org.mobicents.servlet.sip.core.session.SipRequestDispatcher;

/**
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public class ConvergedApplicationContext extends ApplicationContext {
	
	protected ConvergedApplicationContextFacade convergedFacade = null;
	
//	/**
//	 * @param basePath
//	 * @param context
//	 */
//	public ConvergedApplicationContext(String basePath, StandardContext context) {
//		super(basePath, context);
//		convergedFacade = new ConvergedApplicationContextFacade(this);
//	}

	/**
	 * @param context
	 */
	
	public ConvergedApplicationContext(StandardContext context) {
		super(context);
		convergedFacade = new ConvergedApplicationContextFacade(this);
	}
	
	@Override
	protected ServletContext getFacade() {
		return convergedFacade;
	}
	
	@Override
	public RequestDispatcher getNamedDispatcher(String name) {
		 // Validate the name argument
        if (name == null)
            return (null);

        // Create and return a corresponding request dispatcher
        Wrapper wrapper = (Wrapper) getContext().findChild(name);
        if (wrapper == null)
            return (null);
        
        if(wrapper instanceof SipServletImpl) {
        	return new SipRequestDispatcher((SipServletImpl) wrapper);
        } else {
        	return super.getNamedDispatcher(name);
        }
	}
}
