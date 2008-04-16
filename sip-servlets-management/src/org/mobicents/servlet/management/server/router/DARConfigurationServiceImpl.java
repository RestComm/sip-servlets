package org.mobicents.servlet.management.server.router;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.mobicents.servlet.management.client.router.DARConfigurationService;
import org.mobicents.servlet.management.client.router.DARRoute;
import org.mobicents.servlet.management.client.router.DARRouteNode;
import org.mobicents.servlet.sip.Constants;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.router.DefaultApplicationRouter;
import org.mobicents.servlet.sip.router.DefaultApplicationRouterParser;
import org.mobicents.servlet.sip.router.DefaultSipApplicationRouterInfo;
import org.mobicents.servlet.sip.startup.SipContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DARConfigurationServiceImpl extends RemoteServiceServlet implements DARConfigurationService {

	public void configure(String config) {
		try {
			System.out.print(config);
			DefaultApplicationRouter router = (DefaultApplicationRouter) 
			getServletContext().getAttribute(Constants.APPLICATION_ROUTER);
			Properties props = new Properties();
			byte bytes[] = config.getBytes();
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
			props.load(byteArrayInputStream);
			router.configure(props);
		} catch (Throwable t) {
			throw new RuntimeException("Error", t);
		}
	}

	public String[] getApplications() {
		Object obj = getServletContext().getAttribute(Constants.APPLICATION_DISPATCHER);
		SipApplicationDispatcherImpl dispatcher = (SipApplicationDispatcherImpl) obj;
		Iterator iterator = dispatcher.findSipApplications();
		ArrayList appList = new ArrayList();
		while(iterator.hasNext()){
			SipContext ctx = (SipContext)iterator.next();
			appList.add(ctx.getApplicationName());
		}
		String[] ret = new String[appList.size()];
		for(int q=0; q<appList.size(); q++) ret[q] = (String) appList.get(q);
		
		return ret;
	}

	public DARRoute[] getConfiguration() {
		DefaultApplicationRouter router = (DefaultApplicationRouter) 
			getServletContext().getAttribute(org.mobicents.servlet.sip.Constants.APPLICATION_ROUTER);
		
		Properties properties = (Properties) router.getCurrentConfiguration();
		DefaultApplicationRouterParser parser = new DefaultApplicationRouterParser();
		try {
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
					clientNode.setRoute(routeNode.getRoute());
					clientNode.setSubscriber(routeNode.getSubscriberIdentity());
					clientNode.setRouteModifier(routeNode.getRouteModifier().toString());
					clientNode.setRoutingRegion(routeNode.getRoutingRegion().toString());
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
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
