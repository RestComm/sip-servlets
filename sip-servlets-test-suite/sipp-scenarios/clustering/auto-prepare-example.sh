#export config=$2
export deployments_dir=$2

#Reconfigure application server , deploy example
rm ./apps_to_deploy
if [ $# -ne 0 ]; then
	case $1 in	
	    proxy)
	    		echo "Distributed example used is proxy"
	    		echo "location-service-distributable" > ./apps_to_deploy
                        DAR_TO_COPY=$EXAMPLES_HOME/location-service-distributable/distributable-location-service-dar.properties
				;;
	    proxy-early)
	    		# TODO: adapt to eap-6.4
	    		echo "Distributed example used is proxy early failover"
	    		#MAVEN_PROJECT_TO_BUILD = $EXAMPLES_HOME/location-service-distributable/pom.xml
				#rm -rf $JBOSS_HOME/server/$config/deploy/location-service-distributable-*.war
				#cp $EXAMPLES_HOME/location-service-distributable/target/location-service-distributable-*.war $JBOSS_HOME/server/$config/deploy
				#cp $EXAMPLES_HOME/location-service-distributable/distributable-location-service-dar.properties $JBOSS_HOME/server/$config/conf/dars/mobicents-dar.properties
				#cp setup/jboss-5/mss-sip-stack-jboss-early-failover.properties $JBOSS_HOME/server/$config/conf/mss-sip-stack.properties				
	            ;;
	    b2bua)
	            echo "Distributed example used is b2bua"
	    		echo "call-forwarding-distributable"  > ./apps_to_deploy
			DAR_TO_COPY=$EXAMPLES_HOME/call-forwarding-distributable/distributable-call-forwarding-dar.properties
				;;
	    b2bua-early)
	            # TODO: adapt to eap-6.4
	            echo "Distributed example used is b2bua early failover"
	    		#MAVEN_PROJECT_TO_BUILD=$EXAMPLES_HOME/call-forwarding-distributable/pom.xml
				#rm -rf $JBOSS_HOME/server/$config/deploy/call-forwarding-distributable-*.war
				#cp $EXAMPLES_HOME/call-forwarding-distributable/target/call-forwarding-distributable-*.war $JBOSS_HOME/server/$config/deploy
				#cp $EXAMPLES_HOME/call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/$config/conf/dars/mobicents-dar.properties
				#cp setup/jboss-5/mss-sip-stack-jboss-early-failover.properties $JBOSS_HOME/server/$config/conf/mss-sip-stack.properties
	            ;;
	    custom-b2bua*)
	    		echo "Distributed example used is custom b2bua"
	    		echo "custom-call-forwarding-distributable" > ./apps_to_deploy
                        DAR_TO_COPY=$EXAMPLES_HOME/custom-call-forwarding-distributable/distributable-call-forwarding-dar.properties
	            ;;
	    custom-b2bua-early)
	            # TODO: adapt to eap-6.4
	            echo "Distributed example used is custom b2bua early failover"
	    		#MAVEN_PROJECT_TO_BUILD=$EXAMPLES_HOME/custom-call-forwarding-distributable/pom.xml
				#rm -rf $JBOSS_HOME/server/$config/deploy/custom-call-forwarding-distributable-*.war
				#cp $EXAMPLES_HOME/custom-call-forwarding-distributable/target/custom-call-forwarding-distributable-*.war $JBOSS_HOME/server/$config/deploy
				#cp $EXAMPLES_HOME/custom-call-forwarding-distributable/distributable-call-forwarding-dar.properties $JBOSS_HOME/server/$config/conf/dars/mobicents-dar.properties
				#cp setup/jboss-5/mss-sip-stack-jboss-early-failover.properties $JBOSS_HOME/server/$config/conf/mss-sip-stack.properties
	            ;;
	    c2c)
	    		# TODO: adapt to eap-6.4
	    		echo "Distributed example used is Click To call"
	    		#MAVEN_PROJECT_TO_BUILD=$EXAMPLES_HOME/click2call-distributable/pom.xml
				#rm -rf $JBOSS_HOME/server/$config/deploy/click2call-distributable*.war
				#cp $EXAMPLES_HOME/click2call-distributable/target/click2call-distributable*.war $JBOSS_HOME/server/$config/deploy/click2call-distributable.war
				#echo "" > $JBOSS_HOME/server/all/conf/dars/mobicents-dar.properties
				;;	    
	    uas)
	            echo "Distributed example used is uas"
                    echo "simple-sip-servlet-distributable" > ./apps_to_deploy
                    DAR_TO_COPY=$EXAMPLES_HOME/simple-sip-servlet-distributable/distributable-simple-dar.properties
	            ;;
	    uas-passivation)
	            # TODO: adapt to eap-6.4
	            #echo "Distributed example used is uas"
	    		#MAVEN_PROJECT_TO_BUILD=$EXAMPLES_HOME/simple-sip-servlet-distributable/pom.xml
				#rm -rf $JBOSS_HOME/server/$config/deploy/simple-sip-servlet-distributable-*.war
				#cp $EXAMPLES_HOME/simple-sip-servlet-distributable/target/simple-sip-servlet-distributable-*.war $JBOSS_HOME/server/$config/deploy
				#cp $EXAMPLES_HOME/simple-sip-servlet-distributable/distributable-simple-dar.properties $JBOSS_HOME/server/$config/conf/dars/mobicents-dar.properties
				#cp ./setup/jboss-5/war-deployers-jboss-beans-passivation-enabled.xml $JBOSS_HOME/server/$config/deployers/jbossweb.deployer/META-INF/war-deployers-jboss-beans.xml				
	            ;; 
	    uac)
	            echo "Distributed example used is uac"
                        MAVEN_CMD_OPTS="$MAVEN_CMD_OPTS -Dsip.method=INVITE"
	    		echo "shootist-sip-servlet-distributable" > ./apps_to_deploy
			DAR_TO_COPY=$EXAMPLES_HOME/shootist-sip-servlet-distributable/distributable-shootist-dar.properties
	            ;;
	    uac-register)
	            echo "Distributed example used is uac REGISTER"
                        MAVEN_CMD_OPTS="$MAVEN_CMD_OPTS -Dsip.method=REGISTER"
	    		echo "shootist-sip-servlet-distributable" > ./apps_to_deploy
			DAR_TO_COPY=$EXAMPLES_HOME/shootist-sip-servlet-distributable/distributable-shootist-dar.properties
	            ;;
        proxy-b2bua-ar)
	            echo "Distributed example used is proxy-b2bua-ar"
	    		echo "custom-call-forwarding-distributable" > ./apps_to_deploy
                        echo "location-service-distributable" >> ./apps_to_deploy
			DAR_TO_COPY=ar/proxy-b2bua-ar-dar.properties
			PROPERTIES_TO_COPY=setup/jboss-7/mss-sip-stack-jboss.properties
    esac
fi

if [ "x$DAR_TO_COPY" != "x" ]; then
    cp $DAR_TO_COPY $JBOSS_HOME/standalone/configuration/dars/mobicents-dar.properties
fi

if [ "x$PROPERTIES_TO_COPY" != "x" ]; then
    cp $PROPERTIES_TO_COPY $JBOSS_HOME/standalone/configuration/mss-sip-stack.properties
fi

while read CURRENT_APP
do
    echo Processing App "$CURRENT_APP"

    mvn -q $MAVEN_CMD_OPTS clean install -f $EXAMPLES_HOME/$CURRENT_APP/pom.xml
    if [ "x$3" = "x" ]; then 
        rm -f $JBOSS_HOME/standalone/$deployments_dir/$CURRENT_APP-*
        cp $EXAMPLES_HOME/$CURRENT_APP/target/$CURRENT_APP-*.war $JBOSS_HOME/standalone/$deployments_dir
    fi

    if [ "x$3" = "xredeployIfFailed" ]; then
        if [ "`echo $JBOSS_HOME/standalone/$deployments_dir/$CURRENT_APP-*.failed`" != "$JBOSS_HOME/standalone/$deployments_dir/$CURRENT_APP-*.failed" ]; then
            echo "Previous deployment failed"
            rm -f $JBOSS_HOME/standalone/$deployments_dir/$CURRENT_APP-*
            cp $EXAMPLES_HOME/$CURRENT_APP/target/$CURRENT_APP-*.war $JBOSS_HOME/standalone/$deployments_dir
        fi
    fi

done < "./apps_to_deploy" 
