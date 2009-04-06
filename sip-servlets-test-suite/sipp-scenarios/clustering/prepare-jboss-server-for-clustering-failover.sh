#export JBOSS_HOME=/home/deruelle/servers/jboss-4.2.2.GA-cluster

#echo "Script name is		[$0]"
#echo "First Parameter is		[$1]"
#echo "Second Parameter is		[$2]"
#echo "This Process ID is		[$$]"
#echo "This Parameter Count is	[$#]"
#echo "All Parameters		[$@]"
#echo "The FLAGS are			[$-]"

cp -rf $JBOSS_HOME/server/all $JBOSS_HOME/server/port-1
cp -rf $JBOSS_HOME/server/all $JBOSS_HOME/server/port-2
cp jboss-service-port-1.xml $JBOSS_HOME/server/port-1/conf/jboss-service.xml
cp jboss-service-port-2.xml $JBOSS_HOME/server/port-2/conf/jboss-service.xml
cp mss-sip-stack.properties $JBOSS_HOME/server/port-1/conf/mss-sip-stack.properties
cp mss-sip-stack.properties $JBOSS_HOME/server/port-2/conf/mss-sip-stack.properties
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

mvn clean install -o -f ../../../pom.xml -P jboss -Dnode=port-1
mvn clean install -o -f ../../../pom.xml -P jboss -Dnode=port-2

mkdir $JBOSS_HOME/server/port-1/conf/dars
mkdir $JBOSS_HOME/server/port-2/conf/dars

if [ $# -ne 0 ]; then
	case $1 in	
	    proxy)
	    		echo "Distributed example used is proxy";
	    		mvn clean install -o -f ../../../sip-servlets-examples/location-service-distributable/pom.xml
				cp ../../../sip-servlets-examples/location-service-distributable/target/location-service-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/location-service-distributable/target/location-service-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/location-service-distributable/distributable-location-service-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/location-service-distributable/distributable-location-service-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
	            ;;
	    b2bua)
	            echo "Distributed example used is b2bua";
	    		mvn clean install -o -f ../../../sip-servlets-examples/call-forwarding-distributable/pom.xml
				cp ../../../sip-servlets-examples/call-forwarding-distributable/target/call-forwarding-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/call-forwarding-distributable/target/call-forwarding-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
	            ;;
	    c2c)
	    		echo "Distributed example used is Click To call";
	    		mvn clean install -o -f ../../../sip-servlets-examples/click2call-distributable/pom.xml
				cp ../../../sip-servlets-examples/click2call-distributable/target/click2call-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/click2call-distributable/target/click2call-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				;;
	    uac)
	            echo "Distributed example used is uac";
	    		mvn clean process-resources -Dsend.on.init=true install -o -f ../../../sip-servlets-examples/shootist-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/target/shootist-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				mvn clean process-resources -Dsend.on.init=false install -o -f ../../../sip-servlets-examples/shootist-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/target/shootist-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/distributable-shootist-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/distributable-shootist-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
	            ;;
	    *)
	            echo "Distributed example used is uas";
	    		mvn clean install -o -f ../../../sip-servlets-examples/simple-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
	            ;;
    esac
fi