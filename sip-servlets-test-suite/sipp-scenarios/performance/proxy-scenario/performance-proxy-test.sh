#../sipp 127.0.0.1:5080 -s perf-receiver -sf simple-flow/performance-uac.xml -trace_err -i 127.0.0.1 -p 5055 -r 40 -m 15000
../sipp 127.0.0.1:5080 -sf performance-proxy-uas.xml -trace_err -i 127.0.0.1 -p 5090 -r 1 -m 1 -bg
../sipp 127.0.0.1:5080 -sf performance-proxy-uac.xml -trace_err -i 127.0.0.1 -p 5055 -r 1 -m 1
sleep 10
