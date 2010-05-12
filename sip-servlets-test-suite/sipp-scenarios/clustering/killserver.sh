pid=`cat $1`
sleep $2
sleep 2
./auto-kill-process-tree.sh $pid $1
