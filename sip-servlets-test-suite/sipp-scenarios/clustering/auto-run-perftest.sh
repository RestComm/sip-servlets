#Start tests for the current configuration with 80 sec timeout
export TERM=vt100
./clustering-failover-test.sh $1 120 $3 $4
#Keep track of the status
export CURRENT_CODE=$?
echo "$1 $CURRENT_CODE" >> $2

cp $JBOSS_HOME/standalone/log/server.log ./$1-$config1-server.log
cp $JBOSS_HOME/standalone/log2/server.log ./$1-$config2-server.log

