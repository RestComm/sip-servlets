To build run:
mvn clean install

To deploy:
drop the generated war file in webapps Tomcat directory or Jboss deploy directory.

Alternatively you can add a context entry in the tomcat's server.xml like this 
(this won't work on Jboss and this is not the recommended way to go) :
	<Context 
	className="org.mobicents.servlet.sip.startup.SipStandardContext" 
	configClass="org.mobicents.servlet.sip.startup.SipContextConfig" 
	docBase="E:\workspaces\sip-servlets\sip-servlets-examples\call-blocking\target\call-blocking-1.0" 
	name="call-blocking-context" 
	path="/call-blocking">
		<Manager className="org.mobicents.servlet.sip.core.session.SipStandardManager"/>						
	</Context> 

use this as a DAR file (Service tag/darConfigurationFileLocation attribute in the tomcat's server.xml):
file:///E:/workspaces/sip-servlets/sip-servlets-examples/call-blocking/call-blocking-servlet-dar.properties

To use:
You can now run Tomcat or Jboss.
Start a SIP Phone such as the From Header should be one of the following address :
sip:blocked-sender@sip-servlets.com or sip:blocked-sender@127.0.0.1
   
The SIP phone doesn't have to be registered.
We recommend using 3CX Phone, SJ Phone or WengoPhone. 

Make a call to any address, you should receive a Forbidden.