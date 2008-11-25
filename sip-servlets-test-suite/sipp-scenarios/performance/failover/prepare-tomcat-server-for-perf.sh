cp ../tomcat-setup/failover-server.xml $CATALINA_HOME/conf/server.xml
cp ../tomcat-setup/log4j.xml $CATALINA_HOME/lib/log4j.xml
mvn clean install -f ../../../../sip-servlets-examples/simple-sip-servlet/pom.xml
cp ../../../../sip-servlets-examples/simple-sip-servlet/target/simple-sip-servlet-*.war $CATALINA_HOME/webapps
cp ../../../../sip-servlets-examples/simple-sip-servlet/simple-dar.properties $CATALINA_HOME/conf/dars/simple-dar.properties
