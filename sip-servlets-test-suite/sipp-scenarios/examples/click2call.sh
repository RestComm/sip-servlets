./sipp 127.0.0.1:5080 -sf click2call/click2call-a-reg.xml -i 127.0.0.1 -p 5055 -r 1 -m 1 -bg
./sipp 127.0.0.1:5080 -sf click2call/click2call-b-reg.xml -i 127.0.0.1 -p 5056 -r 1 -m 1 -bg
sleep 2
./sipp 127.0.0.1:5080 -sf click2call/click2call-a.xml -i 127.0.0.1 -p 5055 -r 1 -m 1 -bg -trace_msg
./sipp 127.0.0.1:5080 -sf click2call/click2call-b.xml -i 127.0.0.1 -p 5056 -r 1 -m 1 -bg -trace_msg
sleep 2
wget -S -nc "http://localhost:8080/click2call/call?to=sip:luis@127.0.0.1:5055&from=sip:barreiro@127.0.0.1:5056" -O /dev/null
sleep 10
