export JAVA_OPTS="-Xms1536m -Xmx1536m -XX:PermSize=128M -XX:MaxPermSize=256M -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode"
sh $JBOSS_HOME/bin/run.sh run