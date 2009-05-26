#echo "Script name is		[$0]"
#echo "First Parameter is		[$1]"
#echo "Second Parameter is		[$2]"
#echo "This Process ID is		[$$]"
#echo "This Parameter Count is	[$#]"
#echo "All Parameters		[$@]"
#echo "The FLAGS are			[$-]"

#cp -rf $JBOSS_HOME/server/all $JBOSS_HOME/server/port-1
#cp -rf $JBOSS_HOME/server/all $JBOSS_HOME/server/port-2
# we need to copy those jars otherwise we get an IllegalAccessError (might be due to a classloading issue => post a message on jboss forum)
cp $JBOSS_HOME/server/all/deploy/jbossweb.sar/jboss-web-service.jar $JBOSS_HOME/server/port-1/deployers/jbossweb.deployer/jboss-web-service.jar
cp $JBOSS_HOME/server/all/deploy/jbossweb.sar/jboss-web-service.jar $JBOSS_HOME/server/port-2/deployers/jbossweb.deployer/jboss-web-service.jar
cp $JBOSS_HOME/common/lib/jboss-ha-server-cache-jbc.jar $JBOSS_HOME/server/port-1/deployers/jbossweb.deployer/jboss-ha-server-cache-jbc.jar
cp $JBOSS_HOME/common/lib/jboss-ha-server-cache-jbc.jar $JBOSS_HOME/server/port-2/deployers/jbossweb.deployer/jboss-ha-server-cache-jbc.jar
mv $JBOSS_HOME/common/lib/jboss-ha-server-cache-jbc.jar $JBOSS_HOME/common/lib/jboss-ha-server-cache-jbc.jar.bak
# end of jars dirty copy
cp setup/jboss-5/mss-sip-stack-jboss.properties $JBOSS_HOME/server/port-1/conf/mss-sip-stack.properties
cp setup/jboss-5/mss-sip-stack-jboss.properties $JBOSS_HOME/server/port-2/conf/mss-sip-stack.properties
cp setup/jboss-5/server-jboss-5-failover-port-1.xml $JBOSS_HOME/server/port-1/deploy/jbossweb.sar/server.xml
cp setup/jboss-5/server-jboss-5-failover-port-2.xml $JBOSS_HOME/server/port-2/deploy/jbossweb.sar/server.xml
cp setup/jboss-5/context-jboss-5.xml $JBOSS_HOME/server/port-1/deploy/jbossweb.sar/context.xml
cp setup/jboss-5/context-jboss-5.xml $JBOSS_HOME/server/port-2/deploy/jbossweb.sar/context.xml
cp setup/jboss-5/jboss-beans.xml $JBOSS_HOME/server/port-1/deploy/jbossweb.sar/META-INF/jboss-beans.xml
cp setup/jboss-5/jboss-beans.xml $JBOSS_HOME/server/port-2/deploy/jbossweb.sar/META-INF/jboss-beans.xml
cp setup/jboss-5/metadata-deployer-jboss-beans.xml $JBOSS_HOME/server/port-1/deployers/metadata-deployer-jboss-beans.xml
cp setup/jboss-5/metadata-deployer-jboss-beans.xml $JBOSS_HOME/server/port-2/deployers/metadata-deployer-jboss-beans.xml
cp setup/jboss-5/war-deployers-jboss-beans.xml $JBOSS_HOME/server/port-1/deployers/jbossweb.deployer/META-INF/war-deployers-jboss-beans.xml
cp setup/jboss-5/war-deployers-jboss-beans.xml $JBOSS_HOME/server/port-2/deployers/jbossweb.deployer/META-INF/war-deployers-jboss-beans.xml
cp setup/jboss-5/log4j.xml $JBOSS_HOME/server/port-1/conf/jboss-log4j.xml
cp setup/jboss-5/log4j.xml $JBOSS_HOME/server/port-2/conf/jboss-log4j.xml

mvn clean install -f ../../../pom.xml -P jboss-5 -Dnode=port-1
mvn clean install -f ../../../pom.xml -P jboss-5 -Dnode=port-2

#mkdir $JBOSS_HOME/server/port-1/conf/dars
#mkdir $JBOSS_HOME/server/port-2/conf/dars

if [ $# -ne 0 ]; then
	case $1 in	
	    proxy)
	    		echo "Distributed example used is proxy";
	    		mvn clean install -f ../../../sip-servlets-examples/location-service-distributable/pom.xml
				cp ../../../sip-servlets-examples/location-service-distributable/target/location-service-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/location-service-distributable/target/location-service-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/location-service-distributable/distributable-location-service-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/location-service-distributable/distributable-location-service-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
	            ;;
	    b2bua)
	            echo "Distributed example used is b2bua";
	    		mvn clean install -f ../../../sip-servlets-examples/call-forwarding-distributable/pom.xml
				cp ../../../sip-servlets-examples/call-forwarding-distributable/target/call-forwarding-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/call-forwarding-distributable/target/call-forwarding-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
	            ;;
	    c2c)
	    		echo "Distributed example used is Click To call";
	    		mvn clean install -f ../../../sip-servlets-examples/click2call-distributable/pom.xml
				cp ../../../sip-servlets-examples/click2call-distributable/target/click2call-distributable*.war $JBOSS_HOME/server/port-1/deploy/click2call-distributable.war
				cp ../../../sip-servlets-examples/click2call-distributable/target/click2call-distributable*.war $JBOSS_HOME/server/port-2/deploy/click2call-distributable.war
				echo "" > $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				echo "" > $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
				;;
	    uac)
	            echo "Distributed example used is uac";
	    		mvn clean process-resources -Dsend.on.init=true install -f ../../../sip-servlets-examples/shootist-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/target/shootist-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				mvn clean process-resources -Dsend.on.init=false install -o -f ../../../sip-servlets-examples/shootist-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/target/shootist-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/distributable-shootist-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/distributable-shootist-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
	            ;;
	    *)
	            echo "Distributed example used is uas";
	    		mvn clean install -f ../../../sip-servlets-examples/simple-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
	            ;;
    esac
fi
