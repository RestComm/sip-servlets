#export JAVA_OPTS="-server -Xms1536m -Xmx1536m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode"
pwd
if [ "0" = $3 ]; then
  echo "Port configuration is 0. Using default command. No need for hacks."
  echo "$JBOSS_HOME/bin/standalone.sh -Djboss.server.log.threshold=INFO -c standalone-sip-ha-$1.xml -Djboss.node.name=$1"
##-Djboss.socket.binding.port-offset=1$30 apparently,starting both nodes with offset makes LB malfuntion
  $JBOSS_HOME/bin/standalone.sh -Djboss.server.log.threshold=INFO -c standalone-sip-ha-$1.xml -Djboss.node.name=$1  > jboss-$1-$4.out 2>&1 &
else
  echo "$JBOSS_HOME/bin/standalone.sh -Djboss.server.log.threshold=INFO -c standalone-sip-ha-$1.xml -Djboss.node.name=$1 -Djboss.socket.binding.port-offset=2$30 -Djboss.server.data.dir=$JBOSS_HOME/standalone/data2 -Djboss.server.log.dir=$JBOSS_HOME/standalone/log2 -Djboss.server.temp.dir=$JBOSS_HOME/standalone/tmp2"
  $JBOSS_HOME/bin/standalone.sh -Djboss.server.log.threshold=INFO -c standalone-sip-ha-$1.xml -Djboss.node.name=$1 -Djboss.socket.binding.port-offset=2$30 -Djboss.server.data.dir=$JBOSS_HOME/standalone/data2 -Djboss.server.log.dir=$JBOSS_HOME/standalone/log2 -Djboss.server.temp.dir=$JBOSS_HOME/standalone/tmp2 > jboss-$1-$4.out 2>&1 &
fi
#take java pid for later use, instead of standalone
export jbosspid=$!
echo "Starting app server $1. The pid for $2 is $jbosspid and other options ports are $3 config is $4"
echo $jbosspid > $2
