#Don't forget to kill the sipp uas process after the test run it might still hang
killall sipp
../sipp 127.0.0.1:5080 -sf call-forwarding-receiver.xml -trace_err -i 127.0.0.1 -p 5090 -bg
../sipp 127.0.0.1:5080 -s receiver -sf call-forwarding-sender.xml -trace_err -i 127.0.0.1 -p 5050 -r 80 -m 1000000
sleep 10
