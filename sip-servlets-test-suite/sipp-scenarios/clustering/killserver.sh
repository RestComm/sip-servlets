pid=`cat $1`
echo "About to kill pid to cause failover:$pid"
sleep $2
sleep 2
./auto-kill-process-tree.sh $pid $1
