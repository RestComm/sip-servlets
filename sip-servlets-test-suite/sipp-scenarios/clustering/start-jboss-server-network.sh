#export JBOSS_HOME=/home/deruelle/servers/jboss-4.2.2.GA-cluster-network
export JAVA_OPTS="-Xms1024m -Xmx1024m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode"
sh $JBOSS_HOME/bin/run.sh -c all -b 192.168.0.10
