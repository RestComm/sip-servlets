$JBOSS_HOME/bin/run.sh -c "$1" "-Djboss.service.binding.set=ports-0$3 -Djboss.messaging.ServerPeerID=0" > "jboss-$1-$4.out"&
export jbosspid=$!
echo "Starting app server. The pid for $2 is $jbosspid"
echo $jbosspid > $2
