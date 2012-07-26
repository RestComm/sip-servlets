mvn clean install -f ../../../pom.xml -P jboss-5

cp jboss-5-setup/mss-sip-stack-jboss.properties $JBOSS_HOME/server/default/conf/mss-sip-stack.properties
cp jboss-5-setup/context-jboss-5.xml $JBOSS_HOME/server/default/deploy/jbossweb.sar/context.xml
cp jboss-5-setup/jboss-beans.xml $JBOSS_HOME/server/default/deploy/jbossweb.sar/META-INF/jboss-beans.xml
cp jboss-5-setup/metadata-deployer-jboss-beans.xml $JBOSS_HOME/server/default/deployers/metadata-deployer-jboss-beans.xml
cp jboss-5-setup/war-deployers-jboss-beans.xml $JBOSS_HOME/server/default/deployers/jbossweb.deployer/META-INF/war-deployers-jboss-beans.xml
cp jboss-5-setup/log4j.xml $JBOSS_HOME/server/default/conf/jboss-log4j.xml
cp jboss-5-setup/server-jboss-5.xml $JBOSS_HOME/server/default/deploy/jbossweb.sar/server.xml

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
