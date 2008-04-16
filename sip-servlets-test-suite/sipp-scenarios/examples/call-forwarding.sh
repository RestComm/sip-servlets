./sipp 127.0.0.1:5080 -sf call-forwarding/call-forwarding-b.xml -i 127.0.0.1 -p 5090 -r 1 -m 1 -bg -trace_msg
./sipp 127.0.0.1:5080 -sf call-forwarding/call-forwarding-a.xml -i 127.0.0.1 -p 5055 -r 1 -m 1 -bg -trace_msg
sleep 10
