To build run:
mvn clean install

To deploy:
drop the generated war file in webapps Tomcat directory or Jboss deploy directory.

Alternatively you can add a context entry in the tomcat's server.xml like this 
(this won't work on Jboss and this is not the recommended way to go) :
	<Context 
	className="org.mobicents.servlet.sip.startup.SipStandardContext" 
	configClass="org.mobicents.servlet.sip.startup.SipContextConfig" 
	docBase="E:\workspaces\sip-servlets\sip-servlets-examples\speed-dial\target\speed-dial-1.0" 
	name="speed-dial-context" 
	path="/speed-dial">
		<Manager className="org.mobicents.servlet.sip.core.session.SipStandardManager"/>						
	</Context> 

use this as a DAR file (Service tag/darConfigurationFileLocation attribute in the tomcat's server.xml):
file:///E:/workspaces/sip-servlets/sip-servlets-examples/speed-dial/speed-dial-dar.properties

To use:
You can now run Tomcat or Jboss.
Start two SIP Phones.
One phone should be setup as sip:receiver@sip-servlets.com on ip address 127.0.0.1 and port 5090
The other phone can be registered as anything   
   
The SIP phone doesn't have to be registered.
We recommend using 3CX Phone, SJ Phone or WengoPhone. 

From the second phone, make a call to sip:9@sip-servlets.com you should have the other phone ringing.
