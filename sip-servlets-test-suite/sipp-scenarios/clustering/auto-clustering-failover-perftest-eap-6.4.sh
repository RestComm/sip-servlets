export WORKSPACE=/home/posger/alerant/projects/sip/git/forks/sip-servlets/sip-servlets-test-suite/sipp-scenarios/clustering
export EXAMPLES_HOME=../../../sip-servlets-examples
export config1="standalone-sip-ha.xml"
export config2="standalone-sip-ha-node2.xml"
export deployments_dir1="deployments"
export deployments_dir2="deployments-node2"
export KILL_PARAMS="-9"
export EXIT_CODE=0;
#export PC_NETWORK_CAPTURE=1

##################################
echo "Prepare input vars"
##################################
if [[ -z $GOALS_FILE ]]; then
  export GOALS_FILE=$WORKSPACE/perftest/mss-cluster-goals.xsl
fi
if [[ -z $TEST_OPTS ]]; then
  export TEST_OPTS="-Xms6144m -Xmx6144m -Xmn2048m"
fi
if [[ -z $TEST_DURATION ]]; then
  TEST_DURATION=2
fi
if [[ -z $CALL_RATE ]]; then
  CALL_RATE=10
fi
if [[ -z $CALL_LENGTH ]]; then
  CALL_LENGTH=1
fi
if [[ -z $FULLSTARTSLEEP ]]; then
  FULLSTARTSLEEP=50
fi
if [[ -z $HALFSTARTSLEEP ]]; then
  HALFSTARTSLEEP=50
fi

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

echo "JAVA_HOME:$JAVA_HOME"
echo "GOALS_FILE:$GOALS_FILE"
echo "TEST_OPTS:$TEST_OPTS"
echo "TEST_DURATION:$TEST_DURATION"
echo "CALL_RATE:$CALL_RATE"
echo "CALL_LENGTH:$CALL_LENGTH"
echo "FULLSTARTSLEEP:$FULLSTARTSLEEP"
echo "HALFSTARTSLEEP:$HALFSTARTSLEEP"

##################################
echo "Calculate PATH vars"
##################################
export TEST_HOME=$WORKSPACE/perftest/mss-jboss
export RESULTS_DIR=$WORKSPACE/perftest/results
export DASHBOARD_DIR=$WORKSPACE/perftest/dashboard
export TOOLS_DIR=$WORKSPACE/perftest/report-tools
export SIPP_CSV=$WORKSPACE/perftest/performance-clustered-*_.csv # TODO: where is this csv supposed to coming from?
export SIPP_RTT_CSV=$WORKSPACE/perftest/performance-clustered*_rtt.csv  # TODO: where is this csv supposed to coming from?
#export SIPP_Performance_UAC=$WORKSPACE/perftest/jain-sip/src/performance/uas/performance-uac.xml
#export DATA_COLLECTION_DIR=$WORKSPACE/perftest/results/traces

##################################
echo "Prepare directories, clean and create"
##################################
rm -fr $TEST_HOME
mkdir -p $TEST_HOME
rm -fr $TOOLS_DIR
mkdir -p $TOOLS_DIR
rm -fr $RESULTS_DIR
mkdir -p $RESULTS_DIR
rm -fr $DASHBOARD_DIR
mkdir -p $DASHBOARD_DIR
#rm -fr jain-sip
#rm -f $WORKSPACE/perf*.zip
#rm -fr $DATA_COLLECTION_DIR
#mkdir -p $DATA_COLLECTION_DIR
rm -rf result.txt

##################################
echo "Calculate Call/Sipp vars"
##################################
CALLS=$(( $CALL_RATE * $CALL_LENGTH * $TEST_DURATION ))
WAIT_TIME=$(( $CALLS / $CALL_RATE + $CALL_LENGTH * 2 ))
SIPP_TIMEOUT=$(( $WAIT_TIME + 10 ))
CONCURRENT_CALLS=$(($CALL_RATE * $CALL_LENGTH * 2 ))
echo "calls:$CALLS"
echo "call rate:$CALL_RATE"
echo "call length:$CALL_LENGTH"
echo "wait time:$WAIT_TIME"
echo "sipp timeout:$SIPP_TIMEOUT"
echo "concurrent calls:$CONCURRENT_CALLS"
echo "FULLSTARTSLEEP:$FULLSTARTSLEEP"
echo "HALFSTARTSLEEP:$HALFSTARTSLEEP"

##################################
echo "Prepare monitoring tool"
##################################
cd perftest
rm -rf sipp-report-tool
git clone https://github.com/RestComm/PerfCorder.git sipp-report-tool
cd sipp-report-tool
mvn -q clean install
cp -r target/classes/* $TOOLS_DIR/
chmod 777 $TOOLS_DIR/*.sh
cp target/sipp-report-*-with-dependencies.jar $TOOLS_DIR/
cd $WORKSPACE


##################################
echo "Start SIP LB"
##################################
# sed 's/load-balancer-TEMPLATE/load-balancer-normal/g' ./lb-logging.properties >lb-logging.properties.normal
# echo "#!/bin/sh" > auto-startlb.sh
# echo "java -Djava.util.logging.config.file=lb-logging.properties.normal -server -Xms1536m -Xmx1536m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -jar $JBOSS_HOME/sip-balancer/sip-balancer-jar-2.1.0-SNAPSHOT-jar-with-dependencies.jar -mobicents-balancer-config=$JBOSS_HOME/sip-balancer/lb-configuration.properties" >> auto-startlb.sh
# chmod +x auto-startlb.sh

# sed 's/load-balancer-TEMPLATE/load-balancer-worst/g' ./lb-logging.properties >lb-logging.properties.worst
# echo "#!/bin/sh" > auto-startlb-worst.sh
# echo "java -Djava.util.logging.config.file=lb-logging.properties.worst -server -Xms1536m -Xmx1536m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -jar $JBOSS_HOME/sip-balancer/sip-balancer-jar-2.1.0-SNAPSHOT-jar-with-dependencies.jar -mobicents-balancer-config=ar/worstcase-affinity-lb-configuration.properties" >> auto-startlb-worst.sh
# chmod +x auto-startlb-worst.sh

# Uncomment this if you want to keep the original affinity testing.
# The following code tests worst base affinity per request
#echo "#!/bin/sh" > auto-startlb.sh
#echo "java -server -Xms1536m -Xmx1536m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -jar $JBOSS_HOME/sip-balancer/sip-balancer-jar-with-dependencies.jar -mobicents-balancer-config=ar/worstcase-affinity-lb-configuration.properties" >> auto-startlb.sh
#chmod +x auto-startlb.sh

# Start LB
./auto-startlb.sh > siplb.out &
export SIPLB=$!
echo "SIP LB $SIPLB"

##################################
echo "Prepare test - test used for perf test: Custom B2BUA"
##################################
echo "Test Custom B2BUA"
echo "================================"
./auto-prepare-example.sh custom-b2bua $deployments_dir1
./auto-prepare-example.sh custom-b2bua $deployments_dir2

# Start node 1
./auto-start-jboss-eap-6.4-server-perftest.sh $config1 config1.pid 0 custom-b2bua
# Wait to boot
sleep $HALFSTARTSLEEP

# Start node 2
./auto-start-jboss-eap-6.4-server-perftest.sh $config2 config2.pid 1 custom-b2bua
# Wait to boot
sleep $HALFSTARTSLEEP


##################################
echo "Start data collection for process PID=`cat $WORKSPACE/config1.pid`"
##################################
cd $TOOLS_DIR
./pc_start_collect.sh `cat $WORKSPACE/config1.pid`
./pc_start_collect.sh `cat $WORKSPACE/config2.pid`
cd $WORKSPACE

##################################
echo "Start test"
##################################
killall sipp
./auto-run-perftest.sh custom-b2bua result.txt $CALL_RATE $CALLS
echo "Finishing test..."
sleep 10

##################################
echo "Collect perftest results and clean"
##################################
cp $SIPP_CSV $TOOLS_DIR/target/data/periodic/sip/sipp.csv
cp $SIPP_RTT_CSV $TOOLS_DIR/target/data/periodic/sip/sipp_rtt.csv
cd $TOOLS_DIR
./pc_stop_collect.sh `cat $WORKSPACE/config1.pid`
cp -f $JBOSS_HOME/standalone/log/server.log $RESULTS_DIR/server-node1.log
cp -f $JBOSS_HOME/standalone/log2/server.log $RESULTS_DIR/server-node2.log
cp -f $TOOLS_DIR/perf*.zip $RESULTS_DIR/

##################################
echo "Check for performance regression"
##################################
./pc_analyse.sh $TOOLS_DIR/perf*.zip 1 > $RESULTS_DIR/PerfCorderAnalysis.xml 2> $RESULTS_DIR/analysis.log
cat $RESULTS_DIR/PerfCorderAnalysis.xml | ./pc_test.sh  $GOALS_FILE > $RESULTS_DIR/TEST-PerfCorderAnalysisTest.xml 2> $RESULTS_DIR/test.log
cat $RESULTS_DIR/PerfCorderAnalysis.xml | ./pc_html_gen.sh > $RESULTS_DIR/PerfCorderAnalysis.html 2> $RESULTS_DIR/htmlgen.log
cd $WORKSPACE

##################################
echo "Cleanup"
##################################
# Kill all processes
echo "Kill remaining sipp processes if any"
killall sipp
echo "Kill JBoss nodes and LB if still live"
./auto-kill-process-tree.sh `cat config1.pid` $config1
./auto-kill-process-tree.sh `cat config2.pid` $config2
./auto-kill-process-tree.sh $SIPLB siplb


kill $KILL_PARAMS `cat config1.pid`
kill $KILL_PARAMS `cat config2.pid`
kill $KILL_PARAMS $SIPLB

echo "exit code $EXIT_CODE"
exit $EXIT_CODE

