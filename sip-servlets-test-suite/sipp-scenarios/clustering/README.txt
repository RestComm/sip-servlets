modify the JBOSS_HOME variable in prepare-jboss-server-for-clustering-failover.sh script to map your own
run sh prepare-jboss-server-for-clustering-failover.sh uas (or proxy or b2bua if you want to test something else) from this directory
run sh start-lb.sh from this directory
run sh start-jboss-server-port-1.sh from this directory
run sh start-jboss-server-port-2.sh from this directory
run sh clustering-failover-test.sh uas (or proxy or b2bua if you want to test something else) from this directory 
When the ACK has been received kill the first node, the second node still handles the BYE and sends the OK to it :-)