cp ../tomcat-setup/server.xml $CATALINA_HOME/conf/server.xml
cp ../tomcat-setup/log4j.xml $CATALINA_HOME/lib/log4j.xml
cp ../mss-sip-stack.properties $CATALINA_HOME/conf/mss-sip-stack.properties
mvn clean install -f ../../../../sip-servlets-examples/call-forwarding/pom.xml
cp ../../../../sip-servlets-examples/call-forwarding/target/call-forwarding-*.war $CATALINA_HOME/webapps
cp ../../../../sip-servlets-examples/call-forwarding/call-forwarding-b2bua-servlet-dar.properties $CATALINA_HOME/conf/dars/mobicents-dar.properties
