To build run:
mvn clean install

To deploy:
Add a context entry in the tomcat's servler.xml like this:
	<Context 
	className="org.mobicents.servlet.sip.startup.SipStandardContext" 
	configClass="org.mobicents.servlet.sip.startup.SipContextConfig" 
	docBase="E:\workspaces\sip-servlets\sip-servlets-examples\location-service\target\location-service-1.0-SNAPSHOT" 
	name="location-service-context" 
	path="/location-service">
		<Manager className="org.mobicents.servlet.sip.core.session.SipStandardManager"/>						
	</Context> 

use this as a DAR file (Service tag/darConfigurationFileLocation attribute in the tomcat's server.xml):
file:///E:/workspaces/sip-servlets/sip-servlets-examples/location-service/locationservice-dar.properties

To use:
You can run Tomcat.
Start two SIP Phones.
One phone should be setup as sip:receiver@sip-servlets.com on ip address 127.0.0.1 and port 5090
The other phone can be registered as anything   
   
The SIP phone doesn't have to be registered.
We recommend using 3CX Phone, SJ Phone or WengoPhone. 

From the second phone, make a call to sip:receiver@sip-servlets.com you should have the other phone ringing.
