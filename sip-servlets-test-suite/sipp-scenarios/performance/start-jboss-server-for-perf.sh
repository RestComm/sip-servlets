export JAVA_OPTS="-server -Xms2048m -Xmx2048m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode"
sh $JBOSS_HOME/bin/run.sh run
