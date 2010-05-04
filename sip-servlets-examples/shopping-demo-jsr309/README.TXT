IMPORTANT NOTE : make JBoss a converged sip servlets AS before following those steps. 
Checkout the instructions at https://sip-servlets.dev.java.net/installation-jboss.html

Instructions to build and deploy
--------------------------------

To build:
1.Install Mobicents Media Server 1.0.0.BETA2 from http://www.mobicents.org/mms-downloads.html and drop the .sar file in
your <jboss_home>/server/default/deploy 

2. Modify the context params in sip-servlets/sip-servlets-examples/shopping-demo/web/src/main/webapp/WEB-INF/sip.xml file. 		
		order.approval.waitingtime : the time between the moment the customer confirm 
			its order and the next phone call made to ask for delivery date or for admin to confirm if order is > 100$. 
			You can let the default
    	admin.sip : the sip address of the admin to be called for confirmation on orders > 100$
    
mvn clean install -Dmaven.skip.test=true from shopping-demo dir (we are skipping the tests because haven't figured out something with hibernate in the tests )

To deploy:

1. use the shopping-demo-dar.properties located in the same dir as this README file 
2. drop the ear file located in shopping-demo/ear/target into the Jboss deploy directory.

To use
------

1. Start Jboss
2. Go to http://localhost:8080/shopping-demo and follow instructions on how to play with the example at 
https://sip-servlets.dev.java.net/examples/shopping-demo.html groups.google.com/group/mobicents-public/web/converged-application-demo or http://groups.google.com/group/mobicents-public/web/converged-application-demo

working nicely on linphone on linux for SIP info
working nicely with wengo for inband
