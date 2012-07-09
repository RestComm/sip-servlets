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

package org.mobicents.servlet.management.server.router;

import java.io.ByteArrayInputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.mobicents.servlet.management.client.router.DARConfigurationService;
import org.mobicents.servlet.management.client.router.DARRoute;
import org.mobicents.servlet.management.client.router.DARRouteNode;
import org.mobicents.servlet.sip.router.DefaultApplicationRouterParser;
import org.mobicents.servlet.sip.router.DefaultSipApplicationRouterInfo;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DARConfigurationServiceImpl extends RemoteServiceServlet implements DARConfigurationService {
	
	private static MBeanServer mserver;
	static {
		if( MBeanServerFactory.findMBeanServer(null).size() > 0 ) {
            mserver = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);
           
        } else {
            mserver = ManagementFactory.getPlatformMBeanServer();
           
        }
	}
	
	private ObjectName getApplicationDispatcher() {
		try {
			ObjectName dispatcherQuery = new ObjectName("*:type=SipApplicationDispatcher");
			ObjectInstance dispatcherInstance = (ObjectInstance) 
			mserver.queryMBeans(dispatcherQuery, null).iterator().next();
			ObjectName dispatcherName = dispatcherInstance.getObjectName();
			return dispatcherName;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	public void configure(String config) {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			
			Properties props = new Properties();
			byte bytes[] = config.getBytes();
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
			props.load(byteArrayInputStream);
			mserver.invoke(dispatcherName, "updateApplicationRouterConfiguration", new Object[]{props},
					new String[]{Object.class.getName()});
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public String[] getApplications() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			String[] applications = (String[]) mserver.invoke(dispatcherName, "findInstalledSipApplications", new Object[]{},
					new String[]{});
			
			return applications;
			
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public DARRoute[] getConfiguration() {
		try {
			ObjectName dispatcherName = getApplicationDispatcher();
			Object configuration = (Object) mserver.invoke(dispatcherName, "retrieveApplicationRouterConfiguration", new Object[]{},
					new String[]{});

			Properties properties = (Properties) configuration;
			DefaultApplicationRouterParser parser = new DefaultApplicationRouterParser();

			Map<String, List<DefaultSipApplicationRouterInfo>> infos = parser.parse(properties);
			Set<String> requests = infos.keySet();
			ArrayList<DARRoute> allRoutes = new ArrayList<DARRoute>();
			for(String request: requests) {
				List<DefaultSipApplicationRouterInfo> routeInfo = infos.get(request);
				DARRoute clientRoute = new DARRoute();
				clientRoute.setRequest(request);
				ArrayList<DARRouteNode> clientRouteArray = new ArrayList<DARRouteNode>();
				for(DefaultSipApplicationRouterInfo routeNode: routeInfo) {
					DARRouteNode clientNode = new DARRouteNode();
					clientNode.setApplication(routeNode.getApplicationName());
					clientNode.setOrder(new Integer(routeNode.getOrder()).toString());
					clientNode.setRoute(routeNode.getRoutes()[0]);
					clientNode.setSubscriber(routeNode.getSubscriberIdentity());
					clientNode.setRouteModifier(routeNode.getRouteModifier().toString());
					clientNode.setRoutingRegion(routeNode.getRoutingRegion().toString());
					String direction = routeNode.getOptionalParameters().get("DIRECTION");
					if(direction == null) direction = "NONE";
					clientNode.setDirection(direction);
					clientRouteArray.add(clientNode);
				}
				DARRouteNode[] array = new DARRouteNode[clientRouteArray.size()];
				array = clientRouteArray.toArray(array);
				clientRoute.setNodes(array);
				allRoutes.add(clientRoute);
			}
			DARRoute[] result = new DARRoute[allRoutes.size()];
			result = allRoutes.toArray(result);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
