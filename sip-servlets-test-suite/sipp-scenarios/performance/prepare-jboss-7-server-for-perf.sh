export JBOSS_CONFIG=standalone
export SIPSERVLETS_DIR=$WORKSPACE/sip-servlets
mvn -q clean install -f $SIPSERVLETS_DIR/pom.xml -P as7

#cp jboss-5-setup/mss-sip-stack-jboss.properties $JBOSS_HOME/$JBOSS_CONFIG/conf/mss-sip-stack.properties
#cp jboss-5-setup/context-jboss-5.xml $JBOSS_HOME/$JBOSS_CONFIG/deploy/jbossweb.sar/context.xml
#cp jboss-5-setup/jboss-beans.xml $JBOSS_HOME/$JBOSS_CONFIG/deploy/jbossweb.sar/META-INF/jboss-beans.xml
#cp jboss-5-setup/metadata-deployer-jboss-beans.xml $JBOSS_HOME/$JBOSS_CONFIG/deployers/metadata-deployer-jboss-beans.xml
#cp jboss-5-setup/war-deployers-jboss-beans.xml $JBOSS_HOME/$JBOSS_CONFIG/deployers/jbossweb.deployer/META-INF/war-deployers-jboss-beans.xml
#cp jboss-5-setup/log4j.xml $JBOSS_HOME/$JBOSS_CONFIG/conf/jboss-log4j.xml
#cp jboss-5-setup/server-jboss-5.xml $JBOSS_HOME/$JBOSS_CONFIG/deploy/jbossweb.sar/server.xml

if [ "x$LOG_LEVEL" = "x" ]; then
    LOG_LEVEL=WARN
fi
echo "Will change log level to $LOG_LEVEL"

sed -i "s/INFO/$LOG_LEVEL/g" $JBOSS_HOME/$JBOSS_CONFIG/configuration/standalone-sip.xml
sed -i "s/ERROR/$LOG_LEVEL/g" $JBOSS_HOME/$JBOSS_CONFIG/configuration/standalone-sip.xml
sed -i "s/DEBUG/$LOG_LEVEL/g" $JBOSS_HOME/$JBOSS_CONFIG/configuration/standalone-sip.xml

if [ $# -ne 0 ]; then
	case $1 in	
	    proxy)
	    		echo "Distributed example used is proxy"
	    		mvn -q clean install -f $SIPSERVLETS_DIR/sip-servlets-examples/location-service/pom.xml
			cp $SIPSERVLETS_DIR/sip-servlets-examples/location-service/target/location-service-*.war $JBOSS_HOME/$JBOSS_CONFIG/deployments
			cp $SIPSERVLETS_DIR/sip-servlets-examples/location-service/locationservice-dar.properties $JBOSS_HOME/$JBOSS_CONFIG/configuration/dars/mobicents-dar.properties
	            ;;
	    b2bua)
	            	echo "Distributed example used is b2bua"
	    		mvn clean install -f $SIPSERVLETS_DIR/sip-servlets-examples/call-forwarding/pom.xml
			cp $SIPSERVLETS_DIR/sip-servlets-examples/call-forwarding/target/call-forwarding-*.war $JBOSS_HOME/$JBOSS_CONFIG/deployments
			cp $SIPSERVLETS_DIR/sip-servlets-examples/call-forwarding/call-forwarding-b2bua-servlet-dar.properties $JBOSS_HOME/$JBOSS_CONFIG/configuration/dars/mobicents-dar.properties
	            ;;	    	   
	    *)
	            	echo "Distributed example used is uas"
	    		mvn clean install -f $SIPSERVLETS_DIR/sip-servlets-examples/simple-sip-servlet/pom.xml
			cp $SIPSERVLETS_DIR/sip-servlets-examples/simple-sip-servlet/target/simple-sip-servlet-*.war $JBOSS_HOME/$JBOSS_CONFIG/deployments
			cp $SIPSERVLETS_DIR/sip-servlets-examples/simple-sip-servlet/simple-dar.properties $JBOSS_HOME/$JBOSS_CONFIG/configuration/dars/mobicents-dar.properties
	            ;;
    esac
fi
