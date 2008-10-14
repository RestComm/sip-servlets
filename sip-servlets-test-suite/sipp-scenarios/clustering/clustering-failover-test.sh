if [ $# -ne 0 ]; then
	case $1 in	
	    proxy)
	    		echo "Distributed example used is proxy";
	    		./sipp 127.0.0.1:5080 -sf proxy/location-service-receiver.xml -i 127.0.0.1 -p 5090 -bg -trace_msg -timeout 20  
	    		./sipp 127.0.0.1:5080 -s receiver-failover -sf proxy/location-service-sender.xml -trace_err -i 127.0.0.1 -p 5050 -r 1 -m 1 -rsa 127.0.0.1:5060 -trace_msg
	            ;;
	    b2bua)
	            echo "Distributed example used is b2bua";
	    		./sipp 127.0.0.1:5080 -s yousendbye -sf uas/clustering-uac.xml -trace_err -i 127.0.0.1 -p 5050 -r 1 -m 1 -rsa 127.0.0.1:5060
	            ;;
	    *)
	            echo "Distributed example used is uas";
	    		./sipp 127.0.0.1:5080 -s yousendbye -sf uas/clustering-uac.xml -trace_err -i 127.0.0.1 -p 5050 -r 1 -m 1 -rsa 127.0.0.1:5060
	            ;;
    esac
fi