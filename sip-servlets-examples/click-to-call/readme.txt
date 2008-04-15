To build run:
mvn clean compile war:war

To deploy:
Copy the war file in the TOMCAT_HOME/webapps folder

To use:
Add a context entry in the tomcat's servler.xml like this:
			<Context 
			className="org.mobicents.servlet.sip.startup.SipStandardContext" 
			configClass="org.mobicents.servlet.sip.startup.SipContextConfig" 
			docBase="E:\sip-serv\sip-servlets-examples\click-to-call\target\click-to-call-servlet-1.0-SNAPSHOT" 
			name="sip-test-context" 
			path="/click2call"/>

Note that path="/sip-test" is important. After that you can run Tomcat and 
naviagate to http://localhost:8080/click2call/.
You will see the index page where you can enter two SIP URIs. Enter the URIs
of two SIP phones and click "Submit". The SIP phones don't have to be registered.
Registration servlet is not ready yet. I recommend using 3CX Phone and SJ Phone
as the two phones in this demo (under Windows). After you pick up the RTP session
starts. The BYE still doesn't work.
