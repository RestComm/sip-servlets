To use perf test, first run the 'download-and-compile-sipp.sh' to download and compile sipp, as the filename says.
This is needed once, because sipp is a C program and needs to be compiled in you system. It takes about 1 minute. 

Start the sip-servlets container with the server.xml provided in the folder (just change the absolute paths), 
running the simple-sip-servlet example, and run the the performance-test script.

use keys + and - to increase or decrease the calls per second rate. 

Note: There are no plans at the moment to port the bash scripts to windows batch files.
