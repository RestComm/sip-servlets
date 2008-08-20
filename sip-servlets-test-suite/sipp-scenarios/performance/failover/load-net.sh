../sipp 192.168.1.22:5081 -s yousendbye -sf load-balancer.xml -trace_err -i 192.168.1.21 -p 5050 -r 2 -m 1000000 -rsa 192.168.1.21:5060
#../sipp -sf uac.xml -i 127.0.0.1 -p 5050 -r 10 -m 1000 -rsa 127.0.0.1:5060 127.0.0.1:5080 -fd 1 -trace_stat -s yousendbye 
#./sipp 127.0.0.1:5080 -s yousendbye -sf simple-flow/performance-uac.xml -trace_err -i 127.0.0.1 -p 5055 -r 40 -m 100000
