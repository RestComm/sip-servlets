#export JBOSS_HOME=/home/deruelle/servers/jboss-5.1.0.CR1
export JAVA_OPTS="-Xms1024m -Xmx1024m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode"
sh $JBOSS_HOME/bin/run.sh -c port-2 -Djboss.service.binding.set=ports-02 -Djboss.messaging.ServerPeerID=1

