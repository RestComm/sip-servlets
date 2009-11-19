rm -rf chat-server/*.log
./sipp 127.0.0.1:5080 -sf chat-server/chat-server-a.xml -i 127.0.0.1 -p 5055 -r 1 -m 1 -bg -trace_msg
sleep 2
./sipp 127.0.0.1:5080 -sf chat-server/chat-server-b.xml -i 127.0.0.1 -p 5055 -r 1 -m 1 -bg -trace_msg
sleep 2
./sipp 127.0.0.1:5080 -sf chat-server/chat-server-b.xml -i 127.0.0.1 -p 5055 -r 1 -m 1 -bg -trace_msg
sleep 2
./sipp 127.0.0.1:5080 -sf chat-server/chat-server-a.xml -i 127.0.0.1 -p 5055 -r 1 -m 1 -bg -trace_msg
sleep 2
./sipp 127.0.0.1:5080 -sf chat-server/chat-server-b.xml -i 127.0.0.1 -p 5055 -r 1 -m 1 -bg -trace_msg
sleep 5
