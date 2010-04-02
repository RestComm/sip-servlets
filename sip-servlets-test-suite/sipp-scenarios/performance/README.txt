modify the JBOSS_HOME variable in prepare-jboss-server-for-perf.sh script to map your own or set the JBOSS_HOME env variable
run sh prepare-jboss-server-for-perf.sh uas (or proxy, b2bua or uac  if you want to test something else) from this directory
run sh start-jboss-server.sh from this directory
run sh performance-test.sh uas (or proxy or b2bua, if you want to test something else) from this directory 
