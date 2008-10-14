modify the JBOSS_HOME variable in prepare-jboss-server-for-clustering-failover-network.sh script to map your own
modify the hard coded ip address (192.168.1.21) in the different files used by the scripts to map your own IP
 
run sh prepare-jboss-server-for-clustering-failover-network.sh uas (or proxy or b2bua if you want to test something else) from this directory on both machines
run sh start-lb-network.sh from this directory
run sh start-jboss-server-all.sh from this directory on both machines
run sh clustering-failover-test-network.sh uas (or proxy or b2bua if you want to test something else) from this directory 
When the ACK has been received kill the first node, the second node still handles the BYE and sends the OK to it :-)