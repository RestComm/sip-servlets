To use perf test, first run the 'download-and-compile-sipp.sh' to download and compile sipp, as the filename says in the parent directory.
This is needed once, because sipp is a C program and needs to be compiled in you system. It takes about 1 minute. 

Then :
1. call the script sh prepare-server-for-proxy-perf.sh
2. call the script sh start-server-for-perf.sh
3; When the server is started, call the script sh performance-b2bua-test.sh

use keys + and - to increase or decrease the calls per second rate. 

Note: There are no plans at the moment to port the bash scripts to windows batch files.
