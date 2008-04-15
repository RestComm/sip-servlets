To build run:
mvn clean install

To deploy:
Add a context entry in the tomcat's servler.xml like this:
	<Context 
	className="org.mobicents.servlet.sip.startup.SipStandardContext" 
	configClass="org.mobicents.servlet.sip.startup.SipContextConfig" 
	docBase="E:\workspaces\sip-servlets\sip-servlets-examples\call-forwarding\target\call-forwarding-1.0-SNAPSHOT" 
	name="call-forwarding-context" 
	path="/call-forwarding">
		<Manager className="org.mobicents.servlet.sip.core.session.SipStandardManager"/>						
	</Context> 

use this as a DAR file (Service tag/darConfigurationFileLocation attribute in the tomcat's server.xml):
file:///E:/workspaces/sip-servlets/sip-servlets-examples/call-forwarding/call-forwarding-b2bua-servlet-dar.properties

To use:
You can run Tomcat.
Start two SIP Phones.
One should be setup such as the From Header should be one of the following address :
sip:forward-sender@sip-servlets.com or sip:forward-sender@127.0.0.1
The other one should be setup as forward-receiver on ip address 127.0.0.1 and port 5090   
   
The SIP phone doesn't have to be registered.
We recommend using 3CX Phone, SJ Phone or WengoPhone. 

From the first phone, make a call to sip:receiver@sip-servlets.com you should have the other phone ringing.
