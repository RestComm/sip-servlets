#!/bin/sh
export pid=$1
killed="no"
for child in $(ps -o pid,ppid -ax | \
   awk "{ if ( \$2 == $pid ) { print \$1 }}")
do
  echo "Killing script $2 child process $child because parent pid = $pid"
  kill $child
  killed="yes"
done

if [ "yes" = $killed ]; then
  echo "The app server is not dead? We will sleep. We must raise error here, because this server should have been dead."
  sleep 12
fi
