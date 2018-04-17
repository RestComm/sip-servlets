#Start tests for the current configuration with 80 sec timeout
export TERM=vt100
sudo tcpdump -ni lo -s 0 -w ./$1.pcap &
./clustering-failover-test.sh $1 120 120 $3
#Keep track of the status
export CURRENT_CODE=$?
echo "$1 $CURRENT_CODE" >> $2

sudo killall tcpdump
./auto-kill-process-tree.sh `cat config1.pid` $config1
cp $JBOSS_HOME/standalone/log/server-$config1.log ./$1-$config1-server.log
cp $JBOSS_HOME/standalone/log/server-$config2.log ./$1-$config2-server.log
