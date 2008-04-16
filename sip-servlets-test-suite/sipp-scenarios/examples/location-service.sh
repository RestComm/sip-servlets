./sipp 127.0.0.1:5080 -sf location-service/location-service-c.xml -i 127.0.0.1 -p 6090 -r 1 -m 1 -bg -trace_msg
./sipp 127.0.0.1:5080 -sf location-service/location-service-b.xml -i 127.0.0.1 -p 5090 -r 1 -m 1 -bg -trace_msg
./sipp 127.0.0.1:5080 -sf location-service/location-service-a.xml -i 127.0.0.1 -p 5055 -r 1 -m 1 -bg -trace_msg
sleep 10
