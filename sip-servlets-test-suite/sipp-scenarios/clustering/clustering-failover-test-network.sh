if [ $# -ne 0 ]; then
	case $1 in	
	    proxy)
	    		echo "Distributed example used is proxy";
	    		./sipp 192.168.1.21:5080 -sf proxy/location-service-receiver.xml -i 192.168.1.21 -p 5090 -bg -trace_msg -timeout 20  
	    		./sipp 192.168.1.21:5080 -s receiver-failover -sf proxy/location-service-sender.xml -trace_err -i 192.168.1.21 -p 5050 -r 1 -m 1 -rsa 192.168.1.21:5060 -trace_msg
	            ;;
	    b2bua)
	            echo "Distributed example used is b2bua";
	    		./sipp 192.168.1.21:5080 -s yousendbye -sf uas/clustering-uac.xml -trace_err -i 192.168.1.21 -p 5050 -r 1 -m 1 -rsa 192.168.1.21:5060
	            ;;
	    *)
	            echo "Distributed example used is uas";
	    		./sipp 192.168.1.21:5080 -s yousendbye -sf uas/clustering-uac.xml -trace_err -i 192.168.1.21 -p 5050 -r 1 -m 1 -rsa 192.168.1.21:5060
	            ;;
    esac
fi
#./sipp 192.168.1.21:5080 -s yousendbye -sf clustering-uac.xml -trace_err -i 192.168.1.21 -p 5050 -r 1 -m 1 -rsa 192.168.1.21:5060