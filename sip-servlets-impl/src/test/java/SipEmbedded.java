import java.net.URL;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.router.DefaultApplicationRouter;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipHostConfig;
import org.mobicents.servlet.sip.startup.SipProtocolHandler;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.startup.SipStandardService;

/**
 * @author Jean Deruelle
 *
 */
public class SipEmbedded extends SipStandardService {

	
	private String path = null;

	private SipStandardService embedded = null;

	private StandardHost host = null;

	/**
	 * Default Constructor
	 * 
	 */
	public SipEmbedded() {

	}

	/**
	 * Basic Accessor setting the value of the context path
	 * 
	 * @param path -
	 *            the path
	 */
	public void setPath(String path) {

		this.path = path;
	}

	/**
	 * Basic Accessor returning the value of the context path
	 * 
	 * @return - the context path
	 */
	public String getPath() {

		return path;
	}

	/**
	 * This method Starts the Tomcat server.
	 */
	public void startTomcat() throws Exception {

		Engine engine = null;
		// Set the home directory
		System.setProperty("catalina.home", getPath());
		//logging configuration
		BasicConfigurator.configure();
		PropertyConfigurator.configure("E:\\servers\\apache-tomcat-5.5.20\\common\\classes\\logging.properties");

		// Create an embedded server
		embedded = new SipStandardService();
		/*
		 * <Service className="org.mobicents.servlet.sip.startup.SipStandardService"
		 * darConfigurationFileLocation="file:///E:/sip-serv/sip-servlets-impl/docs/dar.properties"
		 * name="Catalina"
		 * sipApplicationDispatcherClassName="org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl"
		 * sipApplicationRouterClassName="org.mobicents.servlet.sip.router.DefaultApplicationRouter">
		 */
		SipApplicationDispatcherImpl sipApplicationDispatcher = new SipApplicationDispatcherImpl();		
//		SipStandardService service = new SipStandardService();		
		embedded.setName("Catalina");
		embedded.setSipApplicationDispatcher(sipApplicationDispatcher);
		embedded.setSipApplicationDispatcherClassName(SipApplicationDispatcherImpl.class.getName());
		embedded.setSipApplicationRouterClassName(DefaultApplicationRouter.class.getName());		
		embedded.setDarConfigurationFileLocation("file:///E:/workspaces/sip-servlets/sip-servlets-impl/docs/dar.properties");
		// Create an engine		
//		engine = embedded.createEngine();
		engine = new StandardEngine();
		engine.setName("Catalina");
		engine.setDefaultHost("localhost");
		engine.setService(embedded);		
		// Create a default virtual host
//		host = (StandardHost) embedded.createHost("localhost", getPath() + "/webapps");
		host = new StandardHost();
        host.setAppBase(getPath() + "/webapps");
        host.setName("localhost");
		host.setConfigClass(StandardContext.class.getName());		
		host.setAppBase("webapps");
		host.addLifecycleListener(new SipHostConfig());
		host.setAutoDeploy(false);
		host.setDeployOnStartup(false);
		engine.addChild(host);

		// Install the assembled container hierarchy
		embedded.setContainer(engine);

		/*
		 * <Connector debugLog="../logs/debuglog.txt" ipAddress="0.0.0.0"
		 * logLevel="DEBUG" port="5070"
		 * protocol="org.mobicents.servlet.sip.startup.SipProtocolHandler"
		 * serverLog="../logs/serverlog.txt" signalingTransport="udp"
		 * sipPathName="gov.nist" sipStackName="SIP-Servlet-Tomcat-Server"/>
		 */
		Connector connector = new Connector(
				SipProtocolHandler.class.getName());
		SipProtocolHandler ph = (SipProtocolHandler) connector
				.getProtocolHandler();
		ph.setPort(5070);
		ph.setDebugLog("../logs/debuglog.txt");
		ph.setIpAddress("127.0.0.1");
		ph.setLogLevel("DEBUG");
		ph.setServerLog("../logs/serverlog.txt");
		ph.setSignalingTransport("udp");
		ph.setSipPathName("gov.nist");
		ph.setSipStackName("SIP-Servlet-Tomcat-Server");

		embedded.addConnector(connector);
		// Start the embedded server
		embedded.start();				
	}

	/**
	 * This method Stops the Tomcat server.
	 */
	public void stopTomcat() throws Exception {
		// Stop the embedded server
		embedded.stop();
	}

	/**
	 * Registers a WAR with the container.
	 * 
	 * @param contextPath -
	 *            the context path under which the application will be
	 *            registered
	 * @param warFile -
	 *            the URL of the WAR to be registered.
	 */
	public void registerWAR(String contextPath, URL warFile) throws Exception {

		if (contextPath == null) {

			throw new Exception("Invalid Path : " + contextPath);
		}
		if (contextPath.equals("/")) {

			contextPath = "";
		}
		if (warFile == null) {

			throw new Exception("Invalid WAR : " + warFile);
		}
		StandardHost sh = (StandardHost) host;
		
		// sh.
		/*
		 * Deployer deployer = (Deployer)host; Context context =
		 * deployer.findDeployedApp(contextPath);
		 * 
		 * if (context != null) {
		 * 
		 * throw new Exception("Context " + contextPath + " Already Exists!"); }
		 * deployer.install(contextPath, warFile);
		 */

	}

	/**
	 * Unregisters a WAR from the web server.
	 * 
	 * @param contextPath -
	 *            the context path to be removed
	 */
	public void unregisterWAR(String contextPath) throws Exception {

		Context context = host.map(contextPath);
		if (context != null) {

//			embedded.removeContext(context);
			context.getParent().removeChild(context);
		} else {

			throw new Exception("Context does not exist for named path : "
					+ contextPath);
		}
	}

	/**
	 * Deploy a context to the embedded tomcat container
	 * @param contextPath the context Path of the context to deploy
	 */
	public Container deployContext(String docBase, String name, String path) {
		SipStandardContext context = new SipStandardContext();
		context.setDocBase(docBase);
		context.setName(name);
		context.setPath(path);
		context.setParent(host);
		context.addLifecycleListener(new SipContextConfig());		 
		host.addChild(context);
		return context;
	}
	
	public void undeployContext(Container context) {
		host.removeChild(context);
	}	
}
