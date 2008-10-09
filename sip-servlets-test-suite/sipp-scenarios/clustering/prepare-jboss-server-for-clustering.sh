export JBOSS_HOME=/home/deruelle/servers/jboss-4.2.2.GA-cluster

cp -rf $JBOSS_HOME/server/all $JBOSS_HOME/server/port-1
cp -rf $JBOSS_HOME/server/all $JBOSS_HOME/server/port-2
cp jboss-service-port-1.xml $JBOSS_HOME/server/port-1/conf/jboss-service.xml
cp jboss-service-port-2.xml $JBOSS_HOME/server/port-2/conf/jboss-service.xml
cp server-jboss-port-1.xml $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer/server.xml
cp server-jboss-port-2.xml $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer/server.xml
cp jboss-context.xml $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer/context.xml
cp jboss-context.xml $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer/context.xml
cp jboss-tomcat-service.xml $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer/META-INF/jboss-service.xml
cp jboss-tomcat-service.xml $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer/META-INF/jboss-service.xml
cp webserver-xmbean.xml $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer/META-INF/webserver-xmbean.xml
cp webserver-xmbean.xml $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer/META-INF/webserver-xmbean.xml
cp log4j.xml $JBOSS_HOME/server/port-1/conf/jboss-log4j.xml
cp log4j.xml $JBOSS_HOME/server/port-2/conf/jboss-log4j.xml

cp $M2_REPO/org/mobicents/servlet/sip/sip-servlets-impl/0.7-SNAPSHOT/sip-servlets-impl-0.7-SNAPSHOT.jar $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer
cp $M2_REPO/org/mobicents/servlet/sip/sip-servlets-impl/0.7-SNAPSHOT/sip-servlets-impl-0.7-SNAPSHOT.jar $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer
cp $M2_REPO/org/mobicents/servlet/sip/sip-servlets-application-router/0.7-SNAPSHOT/sip-servlets-application-router-0.7-SNAPSHOT.jar $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer
cp $M2_REPO/org/mobicents/servlet/sip/sip-servlets-application-router/0.7-SNAPSHOT/sip-servlets-application-router-0.7-SNAPSHOT.jar $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer
cp $M2_REPO/org/mobicents/servlet/sip/sip-servlets-spec/1.1.7-SNAPSHOT/sip-servlets-spec-1.1.7-SNAPSHOT.jar $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer
cp $M2_REPO/org/mobicents/servlet/sip/sip-servlets-spec/1.1.7-SNAPSHOT/sip-servlets-spec-1.1.7-SNAPSHOT.jar $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer
cp $M2_REPO/org/mobicents/tools/sip-balancer/1.0-SNAPSHOT/sip-balancer-1.0-SNAPSHOT.jar $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer
cp $M2_REPO/org/mobicents/tools/sip-balancer/1.0-SNAPSHOT/sip-balancer-1.0-SNAPSHOT.jar $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer
cp $M2_REPO/net/java/stun4j/stun4j/1.0.MOBICENTS/stun4j-1.0.MOBICENTS.jar $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer
cp $M2_REPO/net/java/stun4j/stun4j/1.0.MOBICENTS/stun4j-1.0.MOBICENTS.jar $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer
cp $M2_REPO/org/dnsjava/dnsjava/2.0.6/dnsjava-2.0.6.jar $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer
cp $M2_REPO/org/dnsjava/dnsjava/2.0.6/dnsjava-2.0.6.jar $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer
cp $M2_REPO/javax/sip/jain-sip-api/1.2/jain-sip-api-1.2.jar $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer
cp $M2_REPO/javax/sip/jain-sip-api/1.2/jain-sip-api-1.2.jar $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer
cp $M2_REPO/javax/sip/jain-sip-ri/1.2.76/jain-sip-ri-1.2.83.jar $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer
cp $M2_REPO/javax/sip/jain-sip-ri/1.2.76/jain-sip-ri-1.2.83.jar $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer
cp $M2_REPO/concurrent/concurrent/1.3.4/concurrent-1.3.4.jar $JBOSS_HOME/server/port-1/deploy/jboss-web.deployer
cp $M2_REPO/concurrent/concurrent/1.3.4/concurrent-1.3.4.jar $JBOSS_HOME/server/port-2/deploy/jboss-web.deployer

mkdir $JBOSS_HOME/server/port-1/conf/dars
mkdir $JBOSS_HOME/server/port-2/conf/dars

mvn clean install -f ../../../sip-servlets-examples/simple-sip-servlet-distributable/pom.xml
cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-1/deploy
cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-2/deploy
cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-1/conf/dars
cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-2/conf/dars
