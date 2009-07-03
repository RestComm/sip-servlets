Getting Started
---------------

Start the sip servlets container from bin directory with ./catalina.bat run or ./catalina.sh run command. 
You should see a bunch of stuff on the console saying that some sip servlet applications have been added.	
Now that you can start it, you can play with the examples bundled with the release. For more information, check this link out http://www.mobicents.org/examples.html

There is an Application Router Management Console at this location http://localhost:8080/sip-servlets-management.
This application will help you to more easily configure the default application router.

The default port for SIP is 5080. There is one predeployed sample application - click2call. You can navigate to http://localhost:8080/click2call in order to see it in action.
    
What is different from Tomcat standard version
----------------------------------------------

	* the webapps directory contains both http and sip servlet applications (war and sar)
	* the server.xml has been modified to provide extended classes to common container classes to allow for sip applications to be loaded and sip stack to be started
	* some jars have been added to allow sip servlets to be used
	** in the lib directory, there should be a sip-servlets-impl-*.jar
	** in lib directory, there should be a sip-servlets-spec-*.jar
 	** in lib directory, there should be a sip-balancer-*.jar
	** in lib directory, there should be a jain-sip-api-1.2.jar
	** in lib directory, there should be a jain-sip-ri-1.2.*.jar
	** in lib directory, there should be a concurrent-1.3.4.jar
	** in lib directory, there should be a log4j-1.2.14.jar
	** in lib directory, there should be a stun4j.jar
	** in lib directory, there should be a dnsjava-2.0.6.jar
	* a dars directory containing all the default applications router properties 
	files for using the sip servlets applications bundled with the release, 
	has been added to the conf directory	
