To build run:
mvn clean install

To deploy:
drop the generated war file in webapps Tomcat directory or Jboss deploy directory.

Alternatively you can add a context entry in the tomcat's server.xml like this 
(this won't work on Jboss and this is not the recommended way to go) :
	<Context 
	className="org.mobicents.servlet.sip.startup.SipStandardContext" 
	configClass="org.mobicents.servlet.sip.startup.SipContextConfig" 
	docBase="E:\workspaces\sip-servlets\sip-servlets-examples\tmph1\TeleMedicinePh1\tmph1-1.0" 
	name="tmph1-context" 
	path="/tmph1">
		<Manager className="org.mobicents.servlet.sip.core.session.SipStandardManager"/>						
	</Context> 



To use:
You can now run Tomcat or Jboss.
Start two SIP Phones.
One phone should be setup as Doctor on with username receiver    
   


From the first phone, make a call to sip:doctor you should have the other phone ringing.
