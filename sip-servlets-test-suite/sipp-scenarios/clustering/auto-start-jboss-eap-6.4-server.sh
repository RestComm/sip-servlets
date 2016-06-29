export JAVA_OPTS="-server -Xms1536m -Xmx1536m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode"
if [ "0" = $3 ]; then
  echo "Port configuration is 0. Using default command. No need for hacks."
  $JBOSS_HOME/bin/standalone.sh -Djboss.server.log.threshold=INFO -c $1 > jboss-$1-$4.out 2>&1 &
else
  $JBOSS_HOME/bin/standalone.sh -Djboss.server.log.threshold=INFO -c $1 -Djboss.server.data.dir=$JBOSS_HOME/standalone/data2 -Djboss.server.log.dir=$JBOSS_HOME/standalone/log2 -Djboss.server.temp.dir=$JBOSS_HOME/standalone/tmp2 > jboss-$1-$4.out 2>&1 &
fi
export jbosspid=$!
echo "Starting app server. The pid for $2 is $jbosspid and other options ports are $3 config is $4"
echo $jbosspid > $2
