if [ $# -ne 0 ]; then
	case $1 in	
	    proxy)
	    		rm ./proxy/*.log
	    		echo "Distributed example used is proxy";
	    		./sipp 127.0.0.1:5080 -sf proxy/location-service-receiver.xml -i 127.0.0.1 -p 5090 -bg -trace_msg -timeout 30  
	    		./sipp 127.0.0.1:5080 -s receiver-failover -sf proxy/location-service-sender.xml -trace_err -i 127.0.0.1 -p 5050 -r 1 -m 1 -rsa 127.0.0.1:5060 -trace_msg
	            ;;
	    b2bua)
	    		rm ./b2bua/*.log
	            echo "Distributed example used is b2bua";
	            ./sipp 127.0.0.1:5080 -sf b2bua/call-forwarding-receiver.xml -i 127.0.0.1 -p 5090 -bg -trace_msg -timeout 20  
	    		./sipp 127.0.0.1:5080 -s receiver -sf b2bua/call-forwarding-sender.xml -trace_err -i 127.0.0.1 -p 5050 -r 1 -m 1 -rsa 127.0.0.1:5060 -trace_msg
	            ;;
	    b2bua-remote-send-bye)
	    		rm ./b2bua/*.log
	            echo "Distributed example used is b2bua-remote-send-bye";
	            ./sipp 127.0.0.1:5080 -sf b2bua/call-forwarding-receiver-sends-bye.xml -i 127.0.0.1 -p 5090 -bg -trace_msg -timeout 30 
	    		./sipp 127.0.0.1:5080 -s receiver -sf b2bua/call-forwarding-sender-receives-bye.xml -trace_err -i 127.0.0.1 -p 5050 -r 1 -m 1 -rsa 127.0.0.1:5060 -trace_msg -nd
	            ;;
		uac)
				rm ./uac/*.log
	            echo "Distributed example used is uac";
	            ./sipp 127.0.0.1:5080 -sf uac/receiver.xml -i 127.0.0.1 -p 5090 -trace_msg -timeout 30
	            ;;	          
		c2c)
				rm ./converged-click2call/*.log
	            echo "Distributed example used is 3pcc";
	            echo "Now, quickly navigate a browser to this URL: http://localhost/click2call-distributable/call?to=sip:to@127.0.0.1:5090&from=sip:from@127.0.0.1:5091";
	            echo "Then, wait a few secs for the dialog to be established and kill the primary server. The secondary server will take over.";
	            echo "40 secs after you have loaded the URL, one of the SIPP clients will send a BYE to close the dialog.";
	            echo "If nothing happens, check if your apache server with mod_jk installed is running and configured properly.";
	            echo "There are sample config files to run apache and mod_jkon localhost in the converged-click2call directory.";
	            echo "If you need more info about how to configure mod_jk see http://www.jboss.org/community/docs/DOC-12525";
                killall sipp
	            ./sipp 127.0.0.1:5080 -sf converged-click2call/receiver.xml -i 127.0.0.1 -p 5090 -trace_msg -timeout 100 -bg
                ./sipp 127.0.0.1:5080 -sf converged-click2call/receiver-sendbye.xml -i 127.0.0.1 -p 5091 -trace_msg -timeout 100 -bg
                sleep 1
                #wget "http://localhost/click2call-distributable/call?to=sip:to@127.0.0.1:5090&from=sip:from@127.0.0.1:5091"
	            ;;	   
	    *)
	    		rm ./uas/*.log
	            echo "Distributed example used is uas";
	    		./sipp 127.0.0.1:5080 -s isendbye -sf uas/clustering-uac.xml -trace_err -i 127.0.0.1 -p 5050 -r 1 -m 1 -rsa 127.0.0.1:5060 -trace_msg -nd
	            ;;
    esac
fi
