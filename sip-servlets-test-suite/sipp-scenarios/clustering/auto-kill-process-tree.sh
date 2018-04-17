#!/bin/sh
export pid=$1
export killed="no"

echo "Trying Killing script $2 child process with parent pid = $pid"

for child in $(ps -o pid,ppid ax | \
   awk "{ if ( \$2 == $pid ) { print \$1 }}")
do
  echo "Killing script $2 child process $child because parent pid = $pid"
  kill $child # clean kill with shutdown instruction
  sleep .4 # give some time for the clean shutdown to reach out
  echo "Hard Killing script $2 child process $child because parent pid = $pid"
  kill -9 $child # kill it immediately without wasting more time
  kill -9 $pid # kill it immediately without wasting more time
  ps -aef | grep jboss
  export killed="yes"
  echo $killed
done

if [ "no" = $killed ]; then
  echo "The app server is not dead? We will sleep. We must raise error here, because this server should have been dead."
  ps -aef | grep jboss
  sleep 1
fi
