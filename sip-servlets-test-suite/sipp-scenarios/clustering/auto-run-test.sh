#Start tests for the current configuration with 80 sec timeout
./clustering-failover-test.sh $1 80 100 5
#Keep track of the status
export CURRENT_CODE=$?
echo "$1 $CURRENT_CODE" >> $2

./auto-kill-process-tree.sh `cat $config1.pid` $config1
