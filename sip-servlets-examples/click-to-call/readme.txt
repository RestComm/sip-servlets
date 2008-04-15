To build run:
mvn clean compile war:war

To deploy:
drop the generated war file in webapps Tomcat directory or Jboss deploy directory.

Alternatively you can add a context entry in the tomcat's server.xml like this 
(this won't work on Jboss and this is not the recommended way to go) :
	<Context 
	className="org.mobicents.servlet.sip.startup.SipStandardContext" 
	configClass="org.mobicents.servlet.sip.startup.SipContextConfig" 
	docBase="E:\sip-serv\sip-servlets-examples\click-to-call\target\click-to-call-servlet-1.0-SNAPSHOT" 
	name="sip-test-context" 
	path="/click2call">
		<Manager className="org.mobicents.servlet.sip.core.session.SipStandardManager"/>						
	</Context> 
Note that path="/click2call" is important. 

To use:
You can now run Tomcat or Jboss and navigate to http://localhost:8080/click2call/.
If you have no registered SIp clients you will be asked to register at least two.
Configure your SIP clients to use the sip servlets server as a register and proxy.
It will accept any password, there is no autherntication. Afte rthat you will see
a table where each cell will initiate a call between the corresponding clients.

You can also navigate to http://localhost:8080/click2call/simplecall.html, which is
a simplified version that doesn't require registered clients.
You will see the index page where you can enter two SIP URIs. Enter the URIs
of two SIP phones and click "Submit". The SIP phones don't have to be registered.
I recommend using 3CX Phone and SJ Phone as the two phones in this demo
(under Windows), WengoPhone works too. After you pick up both phones the RTP session starts.
 
If you want to REGISTER use this as a DAR file:
REGISTER: ("org.mobicents.servlet.sip.example.SimpleSipServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
OPTIONS: ("org.mobicents.servlet.sip.example.SimpleSipServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
