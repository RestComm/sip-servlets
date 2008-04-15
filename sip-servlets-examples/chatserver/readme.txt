To build run:
mvn clean install

To deploy:
drop the generated war file in webapps Tomcat directory or Jboss deploy directory.

Alternatively you can add a context entry in the tomcat's server.xml like this 
(this won't work on Jboss and this is not the recommended way to go) :
	<Context 
	className="org.mobicents.servlet.sip.startup.SipStandardContext" 
	configClass="org.mobicents.servlet.sip.startup.SipContextConfig" 
	docBase="E:\workspaces\sip-servlets\sip-servlets-examples\chatserver\target\speed-dial-1.0-SNAPSHOT" 
	name="speed-dial-context" 
	path="/speed-dial">
		<Manager className="org.mobicents.servlet.sip.core.session.SipStandardManager"/>						
	</Context> 

use this as a DAR file (Service tag/darConfigurationFileLocation attribute in the tomcat's server.xml):
file:///E:/workspaces/sip-servlets/sip-servlets-examples/chatserver/dar-chatserver.properties

To use:
You can now run Tomcat or Jboss.