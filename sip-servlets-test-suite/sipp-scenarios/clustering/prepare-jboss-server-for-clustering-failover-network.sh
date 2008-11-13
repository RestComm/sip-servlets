#export JBOSS_HOME=/home/deruelle/servers/jboss-4.2.2.GA-cluster-network

cp mss-sip-stack.properties $JBOSS_HOME/server/all/conf/mss-sip-stack.properties
cp jboss-service-all.xml $JBOSS_HOME/server/all/conf/jboss-service.xml
cp server-jboss-failover-all.xml $JBOSS_HOME/server/all/deploy/jboss-web.deployer/server.xml
cp jboss-context.xml $JBOSS_HOME/server/all/deploy/jboss-web.deployer/context.xml
cp jboss-tomcat-service.xml $JBOSS_HOME/server/all/deploy/jboss-web.deployer/META-INF/jboss-service.xml
cp webserver-xmbean.xml $JBOSS_HOME/server/all/deploy/jboss-web.deployer/META-INF/webserver-xmbean.xml
cp log4j.xml $JBOSS_HOME/server/all/conf/jboss-log4j.xml

mvn clean install -f ../../../pom.xml -P jboss -Dnode=all

mkdir $JBOSS_HOME/server/all/conf/dars

if [ $# -ne 0 ]; then
	case $1 in	
	    proxy)
	    		echo "Distributed example used is proxy";
	    		mvn clean install -f ../../../sip-servlets-examples/location-service-distributable/pom.xml
				cp ../../../sip-servlets-examples/location-service-distributable/target/location-service-distributable-*.war $JBOSS_HOME/server/all/deploy
				cp ../../../sip-servlets-examples/location-service-distributable/distributable-location-service-dar.properties $JBOSS_HOME/server/all/conf/dars/distributable-dar.properties
	            ;;
	    b2bua)
	            echo "Distributed example used is b2bua";
	    		mvn clean install -f ../../../sip-servlets-examples/call-forwarding-distributable/pom.xml
				cp ../../../sip-servlets-examples/call-forwarding-distributable/target/call-forwarding-distributable-*.war $JBOSS_HOME/server/all/deploy
				cp ../../../sip-servlets-examples/call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/all/conf/dars/distributable-dar.properties
	            ;;
	    uac)
	            echo "Distributed example used is uac";
	    		mvn clean install -f ../../../sip-servlets-examples/shootist-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/all/deploy
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/distributable-shootist-dar.properties $JBOSS_HOME/server/all/conf/dars/distributable-dar.properties
	            ;;
	    *)
	            echo "Distributed example used is uas";
	    		mvn clean install -f ../../../sip-servlets-examples/simple-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/all/deploy
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/all/conf/dars/distributable-dar.properties
	            ;;
    esac
fi