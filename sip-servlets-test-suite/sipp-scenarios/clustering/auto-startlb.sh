#!/bin/sh

java -DlogConfigFile=$JBOSS_HOME/sip-balancer/lb-log4j.xml -Djava.util.logging.config.file=lb-logging.properties.normal -server -Xms1536m -Xmx1536m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -jar $JBOSS_HOME/tools/sip-balancer/sip-balancer-jar-with-dependencies.jar -mobicents-balancer-config=$JBOSS_HOME/sip-balancer/lb-configuration.properties


