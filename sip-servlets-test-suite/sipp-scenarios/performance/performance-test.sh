killall sipp
rm *.log
#export TEST_IP=192.168.0.12
export TEST_IP=127.0.0.1
echo "IP Address for the test is $TEST_IP"

if [ $# -ne 0 ]; then
	case $1 in	
	    proxy)
	    		rm ./proxy/*.log
	    		echo "Distributed example used is proxy";
	    		./sipp $TEST_IP:5055 -sf proxy-scenario/performance-proxy-uas.xml -trace_err -i $TEST_IP -p 5090 -bg -nd
				./sipp $TEST_IP:5090 -sf proxy-scenario/performance-proxy-uac.xml -trace_err -i $TEST_IP -p 5055 -rsa $TEST_IP:5080 -r 50 -m 250000 -nd
				sleep 10
	            ;;
	    b2bua)
	    		rm ./b2bua/*.log
	            echo "Distributed example used is b2bua";
	            ./sipp $TEST_IP:5080 -sf b2bua/call-forwarding-receiver.xml -trace_err -i $TEST_IP -p 5090 -bg
				./sipp $TEST_IP:5080 -s receiver -sf b2bua/call-forwarding-sender.xml -trace_err -i $TEST_IP -p 5050 -r 100 -m 250000
				sleep 10
	            ;;
	    *)
	    		rm ./uas/*.log
	 			echo "Distributed example used is uas";	    		
	    		./sipp $TEST_IP:5080 -s yousendbye -sf simple-flow/performance-uac.xml -trace_err -i $TEST_IP -p 5055 -r 200 -m 250000
	            ;;
    esac
fi
