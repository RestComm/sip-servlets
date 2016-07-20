#!/bin/sh

java -DlogConfigFile=/home/posger/alerant/projects/sip/git/forks/sip-servlets/build/release/target/restcomm-sip-servlets-as7/restcomm-sip-servlets--jboss-eap-6.4/sip-balancer/lb-log4j.xml -Djava.util.logging.config.file=lb-logging.properties.normal -server -Xms1536m -Xmx1536m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -jar /home/posger/alerant/projects/sip/git/forks/sip-servlets/build/release/target/restcomm-sip-servlets-as7/restcomm-sip-servlets--jboss-eap-6.4/sip-balancer/sip-balancer-jar-2.1.0-SNAPSHOT-jar-with-dependencies.jar -mobicents-balancer-config=/home/posger/alerant/projects/sip/git/forks/sip-servlets/build/release/target/restcomm-sip-servlets-as7/restcomm-sip-servlets--jboss-eap-6.4/sip-balancer/lb-configuration.properties


