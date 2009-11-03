#export JBOSS_HOME=/home/deruelle/servers/jboss-4.2.2.GA-cluster
#export JAVA_OPTS="-Xms1024m -Xmx1024m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode"
export JAVA_OPTS="-Xms2048m -Xmx2048m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode"
export TEST_IP=127.0.0.1
#export TEST_IP=192.168.0.13
sh $JBOSS_HOME/bin/run.sh -c port-1 -b $TEST_IP -Djboss.partition.name=MSSPartition
