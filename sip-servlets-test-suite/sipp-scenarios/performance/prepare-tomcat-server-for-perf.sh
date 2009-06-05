cp tomcat-setup/mss-sip-stack.properties $CATALINA_HOME/conf/mss-sip-stack.properties
cp tomcat-setup/server.xml $CATALINA_HOME/conf/server.xml
cp tomcat-setup/log4j.xml $CATALINA_HOME/lib/log4j.xml
cp mss-sip-stack.properties $CATALINA_HOME/conf/mss-sip-stack.properties
mvn clean install -f ../../../sip-servlets-examples/simple-sip-servlet/pom.xml
cp ../../../sip-servlets-examples/simple-sip-servlet/target/simple-sip-servlet-*.war $CATALINA_HOME/webapps
cp ../../../sip-servlets-examples/simple-sip-servlet/simple-dar.properties $CATALINA_HOME/conf/dars/mobicents-dar.properties
