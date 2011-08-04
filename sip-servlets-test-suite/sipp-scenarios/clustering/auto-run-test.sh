#Start tests for the current configuration with 80 sec timeout
export TERM=vt100
./clustering-failover-test.sh $1 120 120 $3
#Keep track of the status
export CURRENT_CODE=$?
echo "$1 $CURRENT_CODE" >> $2

./auto-kill-process-tree.sh `cat config1.pid` $config1
cp $JBOSS_HOME/server/$config1/log/server.log ./$1-$config1-server.log
cp $JBOSS_HOME/server/$config2/log/server.log ./$1-$config2-server.log
