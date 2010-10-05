$JBOSS_HOME/bin/run.sh -Djboss.server.log.threshold=DEBUG -b 0.0.0.0 -c "$1" "-Djboss.service.binding.set=$3 -Djboss.messaging.ServerPeerID=$4" > "jboss-$1-$5.out"&
export jbosspid=$!
echo "Starting app server. The pid for $2 is $jbosspid"
echo $jbosspid > $2
