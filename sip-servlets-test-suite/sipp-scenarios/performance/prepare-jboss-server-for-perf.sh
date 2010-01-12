mvn clean install -f ../../../pom.xml -P jboss -U

cp jboss-setup/mss-sip-stack.properties $JBOSS_HOME/server/default/conf/mss-sip-stack.properties
cp jboss-setup/server-jboss.xml $JBOSS_HOME/server/default/deploy/jboss-web.deployer/server.xml
cp jboss-setup/jboss-context.xml $JBOSS_HOME/server/default/deploy/jboss-web.deployer/context.xml
cp jboss-setup/jboss-service.xml $JBOSS_HOME/server/default/deploy/jboss-web.deployer/META-INF/jboss-service.xml
cp jboss-setup/webserver-xmbean.xml $JBOSS_HOME/server/default/deploy/jboss-web.deployer/META-INF/webserver-xmbean.xml
cp jboss-setup/log4j.xml $JBOSS_HOME/server/default/conf/jboss-log4j.xml

if [ $# -ne 0 ]; then
	case $1 in	
	    proxy)
	    		echo "Distributed example used is proxy"
	    		mvn clean install -o -f ../../../sip-servlets-examples/location-service/pom.xml
				cp ../../../sip-servlets-examples/location-service/target/location-service-*.war $JBOSS_HOME/server/default/deploy
				cp ../../../sip-servlets-examples/location-service/locationservice-dar.properties $JBOSS_HOME/server/default/conf/dars/mobicents-dar.properties
	            ;;
	    b2bua)
	            echo "Distributed example used is b2bua"
	    		mvn clean install -o -f ../../../sip-servlets-examples/call-forwarding/pom.xml
				cp ../../../sip-servlets-examples/call-forwarding/target/call-forwarding-*.war $JBOSS_HOME/server/default/deploy
				cp ../../../sip-servlets-examples/call-forwarding/call-forwarding-b2bua-servlet-dar.properties $JBOSS_HOME/server/default/conf/dars/mobicents-dar.properties
	            ;;	    	   
	    *)
	            echo "Distributed example used is uas"
	    		mvn clean install -f ../../../sip-servlets-examples/simple-sip-servlet/pom.xml
				cp ../../../sip-servlets-examples/simple-sip-servlet/target/simple-sip-servlet-*.war $JBOSS_HOME/server/default/deploy
				cp ../../../sip-servlets-examples/simple-sip-servlet/simple-dar.properties $JBOSS_HOME/server/default/conf/dars/mobicents-dar.properties
	            ;;
    esac
fi
