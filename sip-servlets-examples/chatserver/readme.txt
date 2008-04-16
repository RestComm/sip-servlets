To build run:
mvn clean install

To deploy:
drop the generated war file in webapps Tomcat directory or Jboss deploy directory.

Alternatively you can add a context entry in the tomcat's server.xml like this 
(this won't work on Jboss and this is not the recommended way to go) :
	<Context 
	className="org.mobicents.servlet.sip.startup.SipStandardContext" 
	configClass="org.mobicents.servlet.sip.startup.SipContextConfig" 
	docBase="E:\workspaces\sip-servlets\sip-servlets-examples\chatserver\target\chatserver-1.0" 
	name="chatserver-context" 
	path="/chatserver">
		<Manager className="org.mobicents.servlet.sip.core.session.SipStandardManager"/>						
	</Context> 

use this as a DAR file (Service tag/darConfigurationFileLocation attribute in the tomcat's server.xml):
file:///E:/workspaces/sip-servlets/sip-servlets-examples/chatserver/dar-chatserver.properties

To use:
You can now run Tomcat or Jboss.

Starts Two SIP chat clients. Point them to sip:just4fun@127.0.0.1:5070
Type your message and then click on the send button 
	
You should see the messages in both clients once they are both in the chat server 
(that is when they sent at least one message to it).
 