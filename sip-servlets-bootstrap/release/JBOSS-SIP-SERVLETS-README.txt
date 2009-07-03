Get started
-----------

Start the sip servlets container from bin directory with ./run.bat run or ./run.sh command.
You should see a bunch of stuff on the console saying that some sip servlet applications have been added.
Now that you can start it, you can play with the examples bundled with the release. For more information,
check this link out http://www.mobicents.org/examples.html

There is an Application Router Management Console at this location http://localhost:8080/sip-servlets-management.
This application will help you to more easily configure the default application router.

This package includes Mobicents Media Server CR3 to allow developers to create complete SIP/JEE/Web/Media
converged applications. For more information about Media Server visit http://groups.google.com/group/mobicents-public/web/mobicents-media-server.
You can find a real example demonstrating Sip Servlets, JEE, JBoss Seam, Media Server, Text to Speech and
jBPM working together here http://www.mobicents.org/shopping-demo.html.

The default port for SIP is 5080. There is two predeployed sample application :
- click2call : You can navigate to http://localhost:8080/click2call in order to see it in action. See http://www.mobicents.org/click2call.html for more information
- diameter-event-charging : a Diameter Event Charging service based on Location Service that performs call charging at a fixed-rate (event charging). See http://www.mobicents.org/diameter_event_charging.html for more information

Clustering for JBoss
--------------------

This distribution includes a pre-configured cluster-enabled server. It is located in the 'all' configuration.
Be aware that the clustering and failover support in Mobicents Sip Servlets are still a technology preview and may have some limitations.
Before starting the cluster-enabled server you must have the Sip Load Balancer from the sip-balancer 
directory running on your machine or on the network. For more information read the following documents:

http://www.mobicents.org/load-balancer.html
http://www.mobicents.org/clustering.html
http://www.mobicents.org/failover.html

Note that most of the configuration is already done in the 'all' configuration from this distribution. You should
just change the binding addresses and the ports specific to your set up.

To run the 'all' configuration you you should execute:
run -c all


What is different from JBoss standard version
---------------------------------------------

	* the server/default/deploy directory contains both http and sip servlet applications (war and sar)
	* the server/default/deploy/jboss-web.depoyer unit has been modified to provide extended classes to common container classes to allow for sip applications to be loaded and sip stack to be started
	* some jars have been added to allow sip servlets to be used
	** in server/default/deploy/jboss-web.deployer/ directory, there should be a sip-servlets-impl-*.jar
	** in server/default/deploy/jboss-web.deployer/ directory, there should be a sip-servlets-spec-*.jar
	** in server/default/deploy/jboss-web.deployer/ directory, there should be a sip-balancer-*.jar
	** in server/default/deploy/jboss-web.deployer/ directory, there should be a jain-sip-api-1.2.jar
	** in server/default/deploy/jboss-web.deployer/ directory, there should be a jain-sip-ri-1.2*.jar
	** in server/default/deploy/jboss-web.deployer/ directory, there should be a concurrent-1.3.4.jar
	** in server/default/deploy/jboss-web.deployer/ directory, there should be a log4j-1.2.14.jar
	** in server/default/deploy/jboss-web.deployer/ directory, there should be a stun4j-1.0-MOBICENTS.jar
	** in server/default/deploy/jboss-web.deployer/ directory, there should be a dnsjava-2.0.6.jar
	* a dars directory containing all the default applications router properties 
	files for using the sip servlets applications bundled with the release, 
	has been added to the server/default/conf
