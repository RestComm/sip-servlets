/*
 * JBoss, Home of Professional Open Source Copyright 2010, Red Hat Inc., and individual contributors as indicated by the
 *
 * @authors tag. See the copyright.txt in the distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.as7;

import static org.mobicents.as7.SipMessages.MESSAGES;

import javax.management.MBeanServer;

import org.apache.catalina.Host;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.core.StandardService;
import org.apache.tomcat.util.modeler.Registry;
import org.jboss.as.controller.services.path.PathManager;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.startup.SipProtocolHandler;
import org.mobicents.servlet.sip.startup.SipStandardEngine;
import org.mobicents.servlet.sip.startup.SipStandardService;

/**
 * Service configuring and starting the web container.
 *
 * @author Emanuel Muckenhuber
 */
class SipServerService implements SipServer, Service<SipServer> {

    // FIXME: josemrecio - settle on using the proper name
    private static final String JBOSS_SIP = "jboss.sip";

    private static final String TEMP_DIR = "jboss.server.temp.dir";

//    private final String defaultHost;
//    private final boolean useNative;
	private static final String FILE_PREFIX_PATH = "file:///";
    private String sipAppRouterFile;
    private String sipStackPropertiesFile;
    final String sipPathName;
    final String sipAppDispatcherClass;
    final int sipCongestionControlInterval;
    final String sipConcurrencyControlMode;
    final boolean usePrettyEncoding;

    private final String instanceId;

//    private Engine engine;
    private StandardServer server;
//    private StandardService service;

    private SipStandardEngine sipEngine;
    private SipStandardService sipService;

    private final InjectedValue<MBeanServer> mbeanServer = new InjectedValue<MBeanServer>();
    private final InjectedValue<PathManager> pathManagerInjector = new InjectedValue<PathManager>();

    public SipServerService(final String sipAppRouterFile, final String sipStackPropertiesFile, final String sipPathName, String sipAppDispatcherClass, int sipCongestionControlInterval, String sipConcurrencyControlMode, boolean usePrettyEncoding, String instanceId) {
//        this.defaultHost = defaultHost;
//        this.useNative = useNative;
    	this.sipAppRouterFile = sipAppRouterFile;
    	this.sipStackPropertiesFile = sipStackPropertiesFile;
    	this.sipPathName = sipPathName;
    	this.sipAppDispatcherClass = sipAppDispatcherClass;
    	this.sipCongestionControlInterval = sipCongestionControlInterval;
    	this.sipConcurrencyControlMode = sipConcurrencyControlMode;
        this.instanceId = instanceId;
        this.usePrettyEncoding = usePrettyEncoding;
    }

    /** {@inheritDoc} */
    public synchronized void start(StartContext context) throws StartException {
        if (org.apache.tomcat.util.Constants.ENABLE_MODELER) {
            // Set the MBeanServer
            final MBeanServer mbeanServer = this.mbeanServer.getOptionalValue();
            if(mbeanServer != null) {
                Registry.getRegistry(null, null).setMBeanServer(mbeanServer);
            }
        }

        System.setProperty("catalina.home", pathManagerInjector.getValue().getPathEntry(TEMP_DIR).resolvePath());
        final StandardServer server = new StandardServer();

//        final StandardService service = new StandardService();
//        service.setName(JBOSS_SIP);
//        service.setServer(server);
//        server.addService(service);

//        final Engine engine = new StandardEngine();
//        engine.setName(JBOSS_SIP);
//        engine.setService(service);
//        engine.setDefaultHost(defaultHost);
//        if (instanceId != null) {
//            engine.setJvmRoute(instanceId);
//        }
//
//        service.setContainer(engine);

//        if (useNative) {
//            final AprLifecycleListener apr = new AprLifecycleListener();
//            apr.setSSLEngine("on");
//            server.addLifecycleListener(apr);
//        }
//        server.addLifecycleListener(new JasperListener());

        final SipStandardService sipService = new SipStandardService();
        if (sipAppDispatcherClass != null) {
        	sipService.setSipApplicationDispatcherClassName(sipAppDispatcherClass);        	
        }
        else {
        	sipService.setSipApplicationDispatcherClassName(SipApplicationDispatcherImpl.class.getName());
        }
        //
		final String configDir = System.getProperty("jboss.server.config.dir");
    	if(sipAppRouterFile != null) {
    		if(!sipAppRouterFile.startsWith(FILE_PREFIX_PATH)) {
    			sipAppRouterFile = FILE_PREFIX_PATH.concat(configDir).concat("/").concat(sipAppRouterFile);
    		}
    		System.setProperty("javax.servlet.sip.dar", sipAppRouterFile);
    	}
    	//
    	sipService.setSipPathName(sipPathName);
    	//
    	if(sipStackPropertiesFile != null) {
    		if(!sipStackPropertiesFile.startsWith(FILE_PREFIX_PATH)) {
    			sipStackPropertiesFile = FILE_PREFIX_PATH.concat(configDir).concat("/").concat(sipStackPropertiesFile);
    		}
    	}
        sipService.setSipStackPropertiesFile(sipStackPropertiesFile);
        //
        if (sipConcurrencyControlMode != null) {
        	sipService.setConcurrencyControlMode(sipConcurrencyControlMode);
        }
        else {
        	sipService.setConcurrencyControlMode("None");
        }
        //
        sipService.setCongestionControlCheckingInterval(sipCongestionControlInterval);
        //
        sipService.setUsePrettyEncoding(usePrettyEncoding);
        //
        sipService.setName(JBOSS_SIP);
        sipService.setServer(server);
        server.addService(sipService);

        final SipStandardEngine sipEngine = new SipStandardEngine();
        sipEngine.setName(JBOSS_SIP);
        sipEngine.setService(sipService);
//        sipEngine.setDefaultHost(defaultHost);
        if (instanceId != null) {
            sipEngine.setJvmRoute(instanceId);
        }
        sipService.setContainer(sipEngine);

        try {
            server.init();
            server.start();
        } catch (Exception e) {
            throw new StartException(MESSAGES.errorStartingSip(), e);
        }
        this.server = server;
//        this.service = service;
//        this.engine = engine;
        this.sipService = sipService;
        this.sipEngine = sipEngine;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void stop(StopContext context) {
        try {
            server.stop();
        } catch (Exception e) {
        }
//        engine = null;
//        service = null;
        server = null;
        sipEngine = null;
        sipService = null;
    }

    /** {@inheritDoc} */
    public synchronized SipServer getValue() throws IllegalStateException {
        return this;
    }

    /** {@inheritDoc} */
    public synchronized void addConnector(Connector connector) {
        if (connector.getProtocolHandler() instanceof SipProtocolHandler) {
            final SipStandardService sipService = this.sipService;
            sipService.addConnector(connector);
        }
    }

    /** {@inheritDoc} */
    public synchronized void removeConnector(Connector connector) {
        if (connector.getProtocolHandler() instanceof SipProtocolHandler) {
            final StandardService service = this.sipService;
            service.removeConnector(connector);
        }
    }

    /** {@inheritDoc} */
    public synchronized void addHost(Host host) {
//        final Engine engine = this.engine;
//        engine.addChild(host);
        final SipStandardEngine sipEngine = this.sipEngine;
        sipEngine.addChild(host);
    }

    /** {@inheritDoc} */
    public synchronized void removeHost(Host host) {
//        final Engine engine = this.engine;
//        engine.removeChild(host);
        final SipStandardEngine sipEngine = this.sipEngine;
        sipEngine.removeChild(host);
    }

    InjectedValue<MBeanServer> getMbeanServer() {
        return mbeanServer;
    }

    InjectedValue<PathManager> getPathManagerInjector() {
        return pathManagerInjector;
    }

    public StandardServer getServer() {
        return server;
    }

    public StandardService getService() {
        return sipService;
    }

    public SipStandardService getSipService() {
        return sipService;
    }

}
