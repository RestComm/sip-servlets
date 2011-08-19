#echo "Script name is		[$0]"
#echo "First Parameter is		[$1]"
#echo "Second Parameter is		[$2]"
#echo "This Process ID is		[$$]"
#echo "This Parameter Count is	[$#]"
#echo "All Parameters		[$@]"
#echo "The FLAGS are			[$-]"

rm -rf $JBOSS_HOME/server/port-1
rm -rf $JBOSS_HOME/server/port-2
rm -rf $JBOSS_HOME/server/port-3

mvn clean install -f ../../../pom.xml -P jboss-5 -Dnode=all

cp setup/jboss-5/mss-sip-stack-jboss.properties $JBOSS_HOME/server/all/conf/mss-sip-stack.properties
cp setup/jboss-5/performance/log4j.xml $JBOSS_HOME/server/all/conf/jboss-log4j.xml
cp setup/jboss-5/log4j.xml $JBOSS_HOME/server/all/conf/jboss-log4j.xml
cp setup/jboss-5/context-jboss-5.xml $JBOSS_HOME/server/all/deploy/jbossweb.sar/context.xml
cp setup/jboss-5/jboss-beans.xml $JBOSS_HOME/server/all/deploy/jbossweb.sar/META-INF/jboss-beans.xml
cp setup/jboss-5/metadata-deployer-jboss-beans.xml $JBOSS_HOME/server/all/deployers/metadata-deployer-jboss-beans.xml
cp setup/jboss-5/war-deployers-jboss-beans.xml $JBOSS_HOME/server/all/deployers/jbossweb.deployer/META-INF/war-deployers-jboss-beans.xml
cp setup/jboss-5/jboss-cache-manager-jboss-beans.xml $JBOSS_HOME/server/all/deploy/cluster/jboss-cache-manager.sar/META-INF/jboss-cache-manager-jboss-beans.xml

cp -rf $JBOSS_HOME/server/all $JBOSS_HOME/server/port-1
cp -rf $JBOSS_HOME/server/all $JBOSS_HOME/server/port-2

cp setup/jboss-5/server-jboss-5-failover-port-1.xml $JBOSS_HOME/server/port-1/deploy/jbossweb.sar/server.xml
cp setup/jboss-5/server-jboss-5-failover-port-2.xml $JBOSS_HOME/server/port-2/deploy/jbossweb.sar/server.xml

mkdir $JBOSS_HOME/server/port-1/conf/dars
mkdir $JBOSS_HOME/server/port-2/conf/dars

if [ $# -ne 0 ]; then
	case $1 in	
	    proxy)
	    		echo "Distributed example used is proxy"
	    		mvn clean install -f ../../../sip-servlets-examples/location-service-distributable/pom.xml
				cp ../../../sip-servlets-examples/location-service-distributable/target/location-service-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/location-service-distributable/target/location-service-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/location-service-distributable/distributable-location-service-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/location-service-distributable/distributable-location-service-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
	            ;;
	    proxy-early)
	    		echo "Distributed example used is proxy with early failure"
	    		mvn clean install -f ../../../sip-servlets-examples/location-service-distributable/pom.xml
				cp ../../../sip-servlets-examples/location-service-distributable/target/location-service-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/location-service-distributable/target/location-service-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/location-service-distributable/distributable-location-service-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/location-service-distributable/distributable-location-service-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
				cp setup/jboss-5/mss-sip-stack-jboss-early-failover.properties $JBOSS_HOME/server/port-1/conf/mss-sip-stack.properties
				cp setup/jboss-5/mss-sip-stack-jboss-early-failover.properties $JBOSS_HOME/server/port-2/conf/mss-sip-stack.properties
	            ;;
	    b2bua)
	            echo "Distributed example used is b2bua"
	    		mvn clean install -f ../../../sip-servlets-examples/call-forwarding-distributable/pom.xml
				cp ../../../sip-servlets-examples/call-forwarding-distributable/target/call-forwarding-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/call-forwarding-distributable/target/call-forwarding-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
	            ;;
	    b2bua-early)
	            echo "Distributed example used is b2bua with early dialog"
	    		mvn clean install -f ../../../sip-servlets-examples/call-forwarding-distributable/pom.xml
				cp ../../../sip-servlets-examples/call-forwarding-distributable/target/call-forwarding-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/call-forwarding-distributable/target/call-forwarding-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
				cp setup/jboss-5/mss-sip-stack-jboss-early-failover.properties $JBOSS_HOME/server/port-1/conf/mss-sip-stack.properties
				cp setup/jboss-5/mss-sip-stack-jboss-early-failover.properties $JBOSS_HOME/server/port-2/conf/mss-sip-stack.properties
	            ;;
	    custom-b2bua)
	            echo "Distributed example used is b2bua"
	    		mvn clean install -o -f ../../../sip-servlets-examples/custom-call-forwarding-distributable/pom.xml
				rm -rf $JBOSS_HOME/server/port-1/deploy/custom-call-forwarding-distributable-*.war
				rm -rf $JBOSS_HOME/server/port-2/deploy/custom-call-forwarding-distributable-*.war
				cp ../../../sip-servlets-examples/custom-call-forwarding-distributable/target/custom-call-forwarding-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/custom-call-forwarding-distributable/target/custom-call-forwarding-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/custom-call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/custom-call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
	            ;;
	    custom-b2bua-early)
	            echo "Distributed example used is b2bua"
	    		mvn clean install -o -f ../../../sip-servlets-examples/custom-call-forwarding-distributable/pom.xml
				rm -rf $JBOSS_HOME/server/port-1/deploy/custom-call-forwarding-distributable-*.war
				rm -rf $JBOSS_HOME/server/port-2/deploy/custom-call-forwarding-distributable-*.war
				cp ../../../sip-servlets-examples/custom-call-forwarding-distributable/target/custom-call-forwarding-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/custom-call-forwarding-distributable/target/custom-call-forwarding-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/custom-call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/custom-call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
				cp setup/jboss-5/mss-sip-stack-jboss-early-failover.properties $JBOSS_HOME/server/port-1/conf/mss-sip-stack.properties
				cp setup/jboss-5/mss-sip-stack-jboss-early-failover.properties $JBOSS_HOME/server/port-2/conf/mss-sip-stack.properties
	            ;;
	    c2c)
	    		echo "Distributed example used is Click To call"
	    		mvn clean install -f ../../../sip-servlets-examples/click2call-distributable/pom.xml
				cp ../../../sip-servlets-examples/click2call-distributable/target/click2call-distributable*.war $JBOSS_HOME/server/port-1/deploy/click2call-distributable.war
				cp ../../../sip-servlets-examples/click2call-distributable/target/click2call-distributable*.war $JBOSS_HOME/server/port-2/deploy/click2call-distributable.war
				echo "" > $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				echo "" > $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
				;;
	    uac)
	            echo "Distributed example used is uac"
	    		mvn clean process-resources -Dsend.on.init=true -Dsip.method=INVITE install -f ../../../sip-servlets-examples/shootist-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/target/shootist-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				mvn clean process-resources -Dsend.on.init=false -Dsip.method=INVITE install -o -f ../../../sip-servlets-examples/shootist-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/target/shootist-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/distributable-shootist-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/distributable-shootist-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
	            ;;
	    uac-register)
	            echo "Distributed example used is uac REGISTER"
	    		mvn clean process-resources -Dsend.on.init=true -Dsip.method=REGISTER install -f ../../../sip-servlets-examples/shootist-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/target/shootist-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				mvn clean process-resources -Dsend.on.init=false -Dsip.method=REGISTER install -o -f ../../../sip-servlets-examples/shootist-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/target/shootist-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/distributable-shootist-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/shootist-sip-servlet-distributable/distributable-shootist-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
	            ;;
	    uas-passivation)
	            echo "Distributed example used is uas"
	    		mvn clean install -f ../../../sip-servlets-examples/simple-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
				cp setup/jboss-5/war-deployers-jboss-beans-passivation-enabled.xml $JBOSS_HOME/server/port-1/deployers/jbossweb.deployer/META-INF/war-deployers-jboss-beans.xml
				cp setup/jboss-5/war-deployers-jboss-beans-passivation-enabled.xml $JBOSS_HOME/server/port-2/deployers/jbossweb.deployer/META-INF/war-deployers-jboss-beans.xml
	            ;;
	    uas-timer-colocation)
	            echo "Distributed example used is uas timer colocation"
	            cp -rf $JBOSS_HOME/server/all $JBOSS_HOME/server/port-3
	            cp setup/jboss-5/server-jboss-5-failover-cc-sas-port-1.xml $JBOSS_HOME/server/port-1/deploy/jbossweb.sar/server.xml
	            cp setup/jboss-5/server-jboss-5-failover-cc-sas-port-2.xml $JBOSS_HOME/server/port-2/deploy/jbossweb.sar/server.xml
	            cp setup/jboss-5/server-jboss-5-failover-cc-sas-port-3.xml $JBOSS_HOME/server/port-3/deploy/jbossweb.sar/server.xml
	    		mvn clean install -f ../../../sip-servlets-examples/simple-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-3/deploy
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-3/conf/dars/distributable-dar.properties
	            ;;
	    *)
	            echo "Distributed example used is uas"
	    		mvn clean install -f ../../../sip-servlets-examples/simple-sip-servlet-distributable/pom.xml
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-1/deploy
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/port-2/deploy
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-1/conf/dars/distributable-dar.properties
				cp ../../../sip-servlets-examples/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/port-2/conf/dars/distributable-dar.properties
	            ;;
    esac
fi
