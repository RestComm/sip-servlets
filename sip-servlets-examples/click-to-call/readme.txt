To build run:
mvn clean compile war:war

To deploy:
Add a context entry in the tomcat's servler.xml like this:
			<Context 
			className="org.mobicents.servlet.sip.startup.SipStandardContext" 
			configClass="org.mobicents.servlet.sip.startup.SipContextConfig" 
			docBase="E:\sip-serv\sip-servlets-examples\click-to-call\target\click-to-call-servlet-1.0-SNAPSHOT" 
			name="sip-test-context" 
			path="/click2call"/>
Note that path="/click2call" is important. 

To use:
You can run Tomcat and naviagate to http://localhost:8080/click2call/.
You will see the index page where you can enter two SIP URIs. Enter the URIs
of two SIP phones and click "Submit". The SIP phones don't have to be registered.
I recommend using 3CX Phone and SJ Phone as the two phones in this demo
(under Windows), WengoPhone works too. After you pick up both phones the RTP session starts.
 
If you want to REGISTER use this as a DAR file:
INVITE: ("org.mobicents.servlet.sip.example.SimpleSipServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
REGISTER: ("org.mobicents.servlet.sip.example.SimpleSipServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
OPTIONS: ("org.mobicents.servlet.sip.example.SimpleSipServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
