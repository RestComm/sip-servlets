mvn clean install -f ../../../pom.xml -U

cp tomcat-setup/mss-sip-stack.properties $CATALINA_HOME/conf/mss-sip-stack.properties
cp tomcat-setup/server.xml $CATALINA_HOME/conf/server.xml
cp tomcat-setup/log4j.xml $CATALINA_HOME/lib/log4j.xml
cp mss-sip-stack.properties $CATALINA_HOME/conf/mss-sip-stack.properties
mvn clean install -f ../../../sip-servlets-examples/simple-sip-servlet/pom.xml


if [ $# -ne 0 ]; then
	case $1 in	
	    proxy)
	    		echo "Distributed example used is proxy"
	    		mvn clean install -o -f ../../../sip-servlets-examples/location-service/pom.xml
				cp ../../../sip-servlets-examples/location-service/target/location-service-distributable-*.war $CATALINA_HOME/webapps
				cp ../../../sip-servlets-examples/location-service/locationservice-dar.properties $CATALINA_HOME/conf/dars/mobicents-dar.properties
	            ;;
	    b2bua)
	            echo "Distributed example used is b2bua"
	    		mvn clean install -o -f ../../../sip-servlets-examples/call-forwarding/pom.xml
				cp ../../../sip-servlets-examples/call-forwarding/target/call-forwarding-*.war $CATALINA_HOME/webapps
				cp ../../../sip-servlets-examples/call-forwarding/call-forwarding-b2bua-servlet-dar.properties $CATALINA_HOME/conf/dars/mobicents-dar.properties
	            ;;	    	   
	    *)
	            echo "Distributed example used is uas"
	    		mvn clean install -f ../../../sip-servlets-examples/simple-sip-servlet/pom.xml
				cp ../../../sip-servlets-examples/simple-sip-servlet/target/simple-sip-servlet-*.war $CATALINA_HOME/webapps
				cp ../../../sip-servlets-examples/simple-sip-servlet/simple-dar.properties $CATALINA_HOME/conf/dars/mobicents-dar.properties
	            ;;
    esac
fi
