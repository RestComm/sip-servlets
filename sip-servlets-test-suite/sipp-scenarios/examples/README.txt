In this folder there are several subfolders, each one containing sipp scenarios to test the different examples.
There is also one bash script for each example, that start sipp instances with the scenarios for that example.

To use it, first run the 'download-and-compile-sipp.sh' to download and compile sipp, as the filename says.
This is needed once, because sipp is a C program and needs to be compiled in you system. It takes about 1 minute. 

Start the sip-servlets container, running the example you want, and run the appropriate bash script.
You should see a results.txt file with the results of the test, and a log file with the messages in the scenarios folder.


Note: There are no plans at the moment to port the bash scripts to windows batch files.
