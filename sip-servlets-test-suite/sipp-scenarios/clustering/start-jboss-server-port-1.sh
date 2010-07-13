#export JBOSS_HOME=/home/deruelle/servers/jboss-4.2.2.GA-cluster
export JAVA_OPTS="-Xms1536m -Xmx1536m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode"
sh $JBOSS_HOME/bin/run.sh -c port-1
