export EXAMPLES_HOME=../../../sip-servlets-examples

export config1="standalone-sip-ha.xml"
export config2="standalone-sip-ha-node2.xml"
export deployments_dir1="deployments"
export deployments_dir2="deployments-node2"
export KILL_PARAMS="-9"

export FULLSTARTSLEEP=200
export HALFSTARTSLEEP=200
export CALLS=5

if [ "x$1" != "x" ]; then
    export FULLSTARTSLEEP=$1
fi

if [ "x$2" != "x" ]; then
    export HALFSTARTSLEEP=$2
fi

if [ "x$3" != "x" ]; then
    export config1=$3
fi

if [ "x$4" != "x" ]; then
    export config2=$4
fi

export EXIT_CODE=0;

rm -rf result.txt

# Start SIP LB
sed 's/load-balancer-TEMPLATE/load-balancer-normal/g' ./lb-logging.properties >lb-logging.properties.normal
echo "#!/bin/sh" > auto-startlb.sh
echo "java -Djava.util.logging.config.file=lb-logging.properties.normal -server -Xms1536m -Xmx1536m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -jar $JBOSS_HOME/sip-balancer/sip-balancer-jar-with-dependencies.jar -mobicents-balancer-config=$JBOSS_HOME/sip-balancer/lb-configuration.properties" >> auto-startlb.sh
chmod +x auto-startlb.sh

sed 's/load-balancer-TEMPLATE/load-balancer-worst/g' ./lb-logging.properties >lb-logging.properties.worst
echo "#!/bin/sh" > auto-startlb-worst.sh
echo "java -Djava.util.logging.config.file=lb-logging.properties.worst -server -Xms1536m -Xmx1536m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -jar $JBOSS_HOME/sip-balancer/sip-balancer-jar-with-dependencies.jar -mobicents-balancer-config=ar/worstcase-affinity-lb-configuration.properties" >> auto-startlb-worst.sh
chmod +x auto-startlb-worst.sh

# Uncomment this if you want to keep the original affinity testing.
# The following code tests worst base affinity per request
#echo "#!/bin/sh" > auto-startlb.sh
#echo "java -server -Xms1536m -Xmx1536m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -jar $JBOSS_HOME/sip-balancer/sip-balancer-jar-with-dependencies.jar -mobicents-balancer-config=ar/worstcase-affinity-lb-configuration.properties" >> auto-startlb.sh
#chmod +x auto-startlb.sh


./auto-startlb.sh > siplb.out &
export SIPLB=$!
echo "SIP LB $SIPLB"

##################################
# Test Custom B2BUA
##################################
echo "Test Custom B2BUA"
echo "================================"
./auto-prepare-example.sh custom-b2bua $deployments_dir1
./auto-prepare-example.sh custom-b2bua $deployments_dir2

./auto-start-jboss-eap-6.4-server.sh $config2 config2.pid 1 custom-b2bua

#Wait to boot
sleep $HALFSTARTSLEEP

./auto-start-jboss-eap-6.4-server.sh $config1 config1.pid 0 custom-b2bua

#Wait to boot
sleep $HALFSTARTSLEEP

./auto-run-test.sh custom-b2bua result.txt $CALLS


##The test killed server 1, so we start it again
#./auto-start-jboss-eap-6.4-server.sh $config1 config1.pid 0 custom-b2bua-udp-tcp
#
#sleep $HALFSTARTSLEEP
#
#./auto-run-test.sh custom-b2bua-udp-tcp result.txt $CALLS
#
#
##The test killed server 1, so we start it again
#./auto-start-jboss-eap-6.4-server.sh $config1 config1.pid 0 custom-b2bua-tcp-tcp
#
#sleep $HALFSTARTSLEEP
#
#./auto-run-test.sh custom-b2bua-tcp-tcp result.txt $CALLS

#Kill the app servers
./auto-kill-process-tree.sh `cat config1.pid` $config1
./auto-kill-process-tree.sh `cat config2.pid` $config2

sleep 10

##################################
# Cleanup
##################################

#Kill all processes
./auto-kill-process-tree.sh `cat config1.pid` $config1
./auto-kill-process-tree.sh `cat config2.pid` $config2
./auto-kill-process-tree.sh $SIPLB siplb


kill $KILL_PARAMS `cat config1.pid`
#echo "Exit status for JBOSS1 $JBOSS1: $?"
kill $KILL_PARAMS `cat config2.pid`
#echo "Exit status for JBOSS2 $JBOSS2: $?"
kill $KILL_PARAMS $SIPLB
#echo "Exit status for SIPLB $SIPLB: $?"

cat result.txt

./auto-generate-junit-test-report.sh result.txt report.xml

echo "exit code $EXIT_CODE"
exit $EXIT_CODE
