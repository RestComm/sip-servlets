modify the JBOSS_HOME variable in prepare-jboss-server-for-clustering-failover.sh script to map your own
run sh prepare-jboss-5-server-for-clustering-failover.sh uas (or proxy, b2bua or uac  if you want to test something else) from this directory
run sh start-lb.sh from this directory
run sh start-jboss-5-server-node1.sh from this directory
When server is fully started
run sh start-jboss-5-server-node2.sh from this directory
When server is fully started
run sh clustering-failover-test.sh uas (or proxy, b2bua, b2bua-remote-send-bye, uac if you want to test something else) from this directory 
When the ACK has been received kill the first node, the second node still handles the BYE and sends the OK to it :-)

Beware in case you test uac, because the shootist application that is used is compiled 2 times.
First time with a parameter saying that the application sends the INVITE when it starts (this one is deployed on jboss server port 1) and 
the second time with the parameter saying that the application doesn't send the INVITE when it starts (this one is deployed on jboss server port 2)
The second time is used only for failover when the first node will crash to get the subsequent requests.  
So you need to first start the jboss server port 2, then start the jboss server port 1 but before it is fully started (When you see SIP Load Balancer Found ! printed)
you need to run sh clustering-failover-test.sh uac so that it listen for the incoming INVITE from the application     