#Don't forget to kill the sipp uas process after the test run it might still hang 
killall sipp
rm *.log
../sipp 127.0.0.1:5055 -sf performance-proxy-uas.xml -trace_err -trace_msg -i 127.0.0.1 -p 5090 -bg
../sipp 127.0.0.1:5090 -sf performance-proxy-uac.xml -trace_err -trace_msg -i 127.0.0.1 -p 5055 -rsa 127.0.0.1:5080 -r 1 -m 1
sleep 10
