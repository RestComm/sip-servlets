/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package javax.servlet.sip.ar.spi;

import javax.servlet.sip.ar.SipApplicationRouter;

/**
 * <p>
 * This class is used by the SIP Servlet container to load and instanciate the application router.<br/>
 * The application router must be packaged in accordance with the rules specified in the Service Provider document. 
 * Specifically, the jar file containing the application router implementation must include 
 * META-INF/services/javax.servlet.sip.ar.spi.SipApplicationRouterProvider file. 
 * The contents of the file indicate the name of the concrete public subclass of the SipApplicationRouterProvider class. 
 * The concrete subclass must have a no-arg public constructor.<br/>
 * As specified by the Service Provider framework, the providers may be installed by :<br/>
 * 
 * 1. Including the provider jar in the system classpath
 * 2. Including the provider jar in the extension class path
 * 3. Container-specific means
 * 
 * The example below shows an app router provider implementation installed in the system classpath 
 * (i.e. the first approach from the three options discussed above).
 *  public class AcmeAppRouter implements SipApplicationRouter {
 *  [...]
 *  }
 *  
 *  public class AcmeAppRouterProvider extends SipApplicationRouterProvider {
 *  	private final AcmeAppRouter appRouter = new AcmeAppRouter();
 *  
 *  	public AcmeAppRouterProvider() {
 *  	}
 *  
 *  	public SipApplicationRouter getSipApplicationRouter() {
 *      	return appRouter;
 *  	}
 *  }
 *  
 *  The AcmeAppRouter is then packaged in a jar file and prepended to the system class path. 
 *  The SIP servlet container can look up the application router in a manner outlined below.
 *  
 *  SipApplicationRouter getSipApplicationRouter() {
 *  	Iterator ps = Service.providers(SipApplicationRouterProvider.class);
 *  	while (ps.hasNext()) {
 *  		SipApplicationRouterProvider p = (SipApplicationRouterProvider)ps.next();
 *  		return p.getSipApplicationRouter();
 *  	}
 *  	return null;
 *  }
 *  
 *  Since the SIP servlet specification allows for only one application router to be active at any given time, 
 *  the container selects the first provider available in the system classpath.
 *  Instead of relying on classpath order, the specification also defines a system property 
 *  which instructs the container to load a given provider. 
 *  The javax.servlet.sip.ar.spi.SipApplicationRouterProvider system property can be used to override 
 *  loading behavior and force a specific provider implementation to be used. 
 *  
 *  For portability reasons, containers that provide their own deployment mechanism 
 *  for the application router SHOULD obey the system property, if specified by the deployer.
 *  
 *  @since 1.1
 */
public abstract class SipApplicationRouterProvider {

	public SipApplicationRouterProvider() {}
	
	/**
	 * Retrieve an instance of the application router created by this provider 
	 * @return application router instance
	 */
	public abstract SipApplicationRouter getSipApplicationRouter();
}
