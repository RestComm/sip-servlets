export JBOSS_HOME=/home/deruelle/servers/jboss-4.2.2.GA-cluster

cp -rf $JBOSS_HOME/server/all $JBOSS_HOME/server/port-1
cp -rf $JBOSS_HOME/server/all $JBOSS_HOME/server/port-2
cp jboss-service-port-1.xml $JBOSS_HOME/server/port-1/conf/jboss-service.xml
cp jboss-service-port-2.xml $JBOSS_HOME/server/port-2/conf/jboss-service.xml
cp server-jboss-failover-port-1.xml $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer/server.xml
cp server-jboss-failover-port-2.xml $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer/server.xml
cp jboss-context.xml $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer/context.xml
cp jboss-context.xml $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer/context.xml
cp jboss-tomcat-service.xml $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer/META-INF/jboss-service.xml
cp jboss-tomcat-service.xml $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer/META-INF/jboss-service.xml
cp webserver-xmbean.xml $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer/META-INF/webserver-xmbean.xml
cp webserver-xmbean.xml $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer/META-INF/webserver-xmbean.xml
cp log4j.xml $JBOSS_HOME/server/port-1/conf/jboss-log4j.xml
cp log4j.xml $JBOSS_HOME/server/port-2/conf/jboss-log4j.xml

mvn clean install -f ../../../pom.xml -P jboss -Dnode=port-1
mvn clean install -f ../../../pom.xml -P jboss -Dnode=port-2

mkdir $JBOSS_HOME/server/port-1/conf/dars
mkdir $JBOSS_HOME/server/port-2/conf/dars

mvn clean install -f ../../../sip-servlets-examples/simple-sip-servlet-distributable/pom.xml
cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-1/deploy
cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-2/deploy
cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-1/conf/dars
cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-2/conf/dars
