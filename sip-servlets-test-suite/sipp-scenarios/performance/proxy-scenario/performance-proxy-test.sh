#Don't forget to kill the sipp uas process after the test run it might still hang 
../sipp 127.0.0.1:5080 -sf performance-proxy-uas.xml -trace_err -i 127.0.0.1 -p 5090 -bg
../sipp 127.0.0.1:5080 -sf performance-proxy-uac.xml -trace_err -i 127.0.0.1 -p 5055 -r 15 -m 10000
sleep 10
