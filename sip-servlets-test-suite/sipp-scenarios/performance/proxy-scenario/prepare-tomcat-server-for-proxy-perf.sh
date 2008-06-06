cp ../tomcat-setup/server.xml $CATALINA_HOME/conf/server.xml
cp ../tomcat-setup/log4j.xml $CATALINA_HOME/lib/log4j.xml
mvn clean install -f ../../../../sip-servlets-examples/simple-sip-servlet/pom.xml
cp ../../../../sip-servlets-examples/location-service/target/location-service-1.0.war $CATALINA_HOME/webapps
cp ../../../../sip-servlets-examples/location-service/locationservice-dar.properties $CATALINA_HOME/conf/dars/locationservice-dar.properties
