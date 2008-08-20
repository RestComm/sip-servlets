export JBOSS_HOME=/home/deruelle/servers/jboss-4.2.2.GA-cluster-network

cp jboss-service-all.xml $JBOSS_HOME/server/all/conf/jboss-service.xml
cp server-jboss-failover-all.xml $JBOSS_HOME/server/all/deploy/jboss-web.deployer/server.xml
cp jboss-context.xml $JBOSS_HOME/server/all/deploy/jboss-web.deployer/context.xml
cp jboss-tomcat-service.xml $JBOSS_HOME/server/all/deploy/jboss-web.deployer/META-INF/jboss-service.xml
cp webserver-xmbean.xml $JBOSS_HOME/server/all/deploy/jboss-web.deployer/META-INF/webserver-xmbean.xml
cp log4j.xml $JBOSS_HOME/server/all/conf/jboss-log4j.xml

mvn clean install -f ../../../sip-servlets-bootstrap/pom.xml -P jboss -Dnode=all

mkdir $JBOSS_HOME/server/all/conf/dars

mvn clean install -f ../../../sip-servlets-examples/simple-sip-servlet-distributable/pom.xml
cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-1.0.war $JBOSS_HOME/server/all/deploy
cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/all/conf/dars
