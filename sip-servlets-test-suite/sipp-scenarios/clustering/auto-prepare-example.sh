export config=$2

#Reconfigure application server , deploy example

if [ $# -ne 0 ]; then
	case $1 in	
	    proxy)
	    		echo "Distributed example used is proxy"
	    		mvn clean install -f $EXAMPLES_HOME/location-service-distributable/pom.xml
			rm -rf $JBOSS_HOME/server/$config/deploy/location-service-distributable-*.war
				cp $EXAMPLES_HOME/location-service-distributable/target/location-service-distributable-*.war $JBOSS_HOME/server/$config/deploy
				cp $EXAMPLES_HOME/location-service-distributable/distributable-location-service-dar.properties $JBOSS_HOME/server/$config/conf/dars/mobicents-dar.properties
	            ;;
	    b2bua)
	            echo "Distributed example used is b2bua"
	    		mvn clean install -f $EXAMPLES_HOME/call-forwarding-distributable/pom.xml
			rm -rf $JBOSS_HOME/server/$config/deploy/call-forwarding-distributable-*.war
				cp $EXAMPLES_HOME/call-forwarding-distributable/target/call-forwarding-distributable-*.war $JBOSS_HOME/server/$config/deploy
				cp $EXAMPLES_HOME/call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/$config/conf/dars/mobicents-dar.properties
	            ;;
	    custom-b2bua)
	            echo "Distributed example used is b2bua"
	    		mvn clean install -f $EXAMPLES_HOME/custom-call-forwarding-distributable/pom.xml
			rm -rf $JBOSS_HOME/server/$config/deploy/custom-call-forwarding-distributable-*.war
				cp $EXAMPLES_HOME/custom-call-forwarding-distributable/target/custom-call-forwarding-distributable-*.war $JBOSS_HOME/server/$config/deploy
				cp $EXAMPLES_HOME/custom-call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/$config/conf/dars/mobicents-dar.properties
	            ;;
	    c2c)
	    		echo "Distributed example used is Click To call"
	    		mvn clean install -f .$EXAMPLES_HOME/click2call-distributable/pom.xml
			rm -rf $JBOSS_HOME/server/$config/deploy/click2call-distributable*.war
				cp $EXAMPLES_HOME/click2call-distributable/target/click2call-distributable*.war $JBOSS_HOME/server/$config/deploy/click2call-distributable.war
				echo "" > $JBOSS_HOME/server/all/conf/dars/mobicents-dar.properties
				;;	    
	    uas)
	            echo "Distributed example used is uas"
	    		mvn clean install -f $EXAMPLES_HOME/simple-sip-servlet-distributable/pom.xml
			rm -rf $JBOSS_HOME/server/$config/deploy/simple-sip-servlet-distributable-*.war
				cp $EXAMPLES_HOME/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/$config/deploy
				cp $EXAMPLES_HOME/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/$config/conf/dars/mobicents-dar.properties
	            ;;
	    uac)
	            echo "Distributed example used is uas"
	    		mvn clean install $3 -f $EXAMPLES_HOME/shootist-sip-servlet-distributable/pom.xml
			rm -rf $JBOSS_HOME/server/$config/deploy/shootist-sip-servlet-distributable-*.war
				cp $EXAMPLES_HOME/shootist-sip-servlet-distributable/target/shootist-sip-servlet-distributable-*.war $JBOSS_HOME/server/$config/deploy
				cp $EXAMPLES_HOME/shootist-sip-servlet-distributable/distributable-shootist-dar.properties $JBOSS_HOME/server/$config/conf/dars/mobicents-dar.properties
	            ;;
    esac
fi
