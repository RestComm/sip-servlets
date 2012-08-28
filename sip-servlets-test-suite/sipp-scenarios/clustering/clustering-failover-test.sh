killall sipp
export TEST_IP=127.0.0.1

export MSS_IP=$TEST_IP
export LB_IP=$TEST_IP
export SIPP_IP=$TEST_IP
#export SIPP_OPTIONS="-t t1"
export ACTIVE_TIMEOUT=$2
#export BACKGROUNDMETHOD="-bg"

export RATE=$3
export CALLS=$4

echo Using $MSS_IP as the MSS test IP
echo Using $LB_IP as the Load Balancer IP
echo Using $SIPP_IP as the SIPp IP

if [ $# -eq 4 ]; then
	case $1 in	
	    proxy)
	    		rm ./proxy/*.log
	    		echo "Distributed example used is proxy";
	    		echo""| sipp $MSS_IP:5080 -sf proxy/location-service-receiver.xml -i $SIPP_IP -p 5090 -nd >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver-failover -sf proxy/location-service-sender.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -timeout $ACTIVE_TIMEOUT -timeout_error -nd
	            ;;
	    proxy-early)
	    		rm ./proxy/*.log
	    		echo "Distributed example used is proxy early dialog failover";
	    		echo""| sipp $MSS_IP:5080 -sf proxy/location-service-receiver-early.xml -i $SIPP_IP -p 5090 -nd >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver-failover -sf proxy/location-service-sender-early.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -timeout $ACTIVE_TIMEOUT -timeout_error -nd
	            ;;
	    proxy-remote-send-bye)
	    		rm ./proxy/*.log
	    		echo "Distributed example used is proxy remote send bye";
	    		echo""| sipp $MSS_IP:5080 -sf proxy/location-service-receiver-sends-bye.xml -i $SIPP_IP -p 5090 -nd >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver-failover -sf proxy/location-service-sender-receives-bye.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	            ;;    
	    proxy-indialog-info)
	    		rm ./proxy/*.log
	    		echo "Distributed example used is proxy indialog info";
	    		echo""| sipp $MSS_IP:5080 -sf proxy/location-service-indialog-info-receiver.xml -i $SIPP_IP -p 5090 -nd >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver-failover -sf proxy/location-service-indialog-info-sender.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	    		;;
	    proxy-termination)
	    		rm ./proxy/*.log
	    		echo "Distributed example used is proxy termination";
	    		echo""| sipp $MSS_IP:5080 -sf proxy/location-service-termination-receiver.xml -i $SIPP_IP -p 5090 -nd >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver-failover -sf proxy/location-service-termination-sender.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	    		;;
	    custom-b2bua)
	    		rm ./b2bua/*.log
	            echo "Distributed example used is custom b2bua";
	            echo""| sipp $MSS_IP:5080 -sf b2bua/custom-call-forwarding-receiver.xml -trace_err -trace_msg -nd -i $SIPP_IP -p 5090 >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver -sf b2bua/custom-call-forwarding-sender.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -timeout $ACTIVE_TIMEOUT -timeout_error -nd
	            ;;
	    custom-b2bua-early)
	    		rm ./b2bua/*.log
	            echo "Distributed example used is custom b2bua early dialog failover";
	            echo""| sipp $MSS_IP:5080 -sf b2bua/custom-call-forwarding-receiver-early.xml -i $SIPP_IP -nd -p 5090 >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver -sf b2bua/custom-call-forwarding-sender-early.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -timeout $ACTIVE_TIMEOUT -timeout_error -nd
	            ;;
	    custom-b2bua-udp-tcp)
	    		rm ./b2bua/*.log
	            echo "Distributed example used is custom b2bua udp tcp";
	            echo""| sipp $MSS_IP:5080 -sf b2bua/custom-call-forwarding-receiver.xml -nd -i $SIPP_IP -p 5090 -t t1 >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver-tcp -sf b2bua/custom-call-forwarding-sender.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -timeout $ACTIVE_TIMEOUT -timeout_error -nd
	            ;;
	    custom-b2bua-tcp-tcp)
	    		rm ./b2bua/*.log
	            echo "Distributed example used is custom b2bua tcp tcp";
	            echo""| sipp $MSS_IP:5080 -nd -sf b2bua/custom-call-forwarding-receiver.xml -i $SIPP_IP -p 5090 -t t1 >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver-tcp -nd -sf b2bua/custom-call-forwarding-sender.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -timeout $ACTIVE_TIMEOUT -t t1 -timeout_error
	            ;;
	    b2bua)
	    		rm ./b2bua/*.log
	            echo "Distributed example used is b2bua";
	            echo""| sipp $MSS_IP:5080 -sf b2bua/call-forwarding-receiver.xml -i $SIPP_IP -p 5090 -nd >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver -sf b2bua/call-forwarding-sender.xml -trace_err -i $SIPP_IP -p 5050 -nd -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -timeout $ACTIVE_TIMEOUT $SIPP_OPTIONS -timeout_error
	            ;;
	    b2bua-info)
	    		rm ./b2bua/*.log
	            echo "Distributed example used is b2bua info";
	            echo""| sipp $MSS_IP:5080 -sf b2bua/call-forwarding-receiver-info.xml -i $SIPP_IP -p 5090 -nd >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver -sf b2bua/call-forwarding-sender-info.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT $SIPP_OPTIONS -timeout_error
	            ;;       	    
	    b2bua-early)
	    		rm ./b2bua/*.log
	            echo "Distributed example used is b2bua early dialog failover";
	            echo""| sipp $MSS_IP:5080 -sf b2bua/call-forwarding-receiver-early.xml -i $SIPP_IP -p 5090 -nd >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver -sf b2bua/call-forwarding-sender-early.xml -trace_err -nd -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -timeout $ACTIVE_TIMEOUT $SIPP_OPTIONS -timeout_error
	            ;;
	    b2bua-early-linked)
	    		rm ./b2bua/*.log
	            echo "Distributed example used is b2bua early dialog failover with linked requests";
	            echo""| sipp $MSS_IP:5080 -sf b2bua/call-forwarding-receiver-early.xml -i $SIPP_IP -p 5090 -nd >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver-linked -sf b2bua/call-forwarding-sender-early.xml -nd -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -timeout $ACTIVE_TIMEOUT $SIPP_OPTIONS -timeout_error
	            ;;
	    b2bua-early-fwd-ack)
	    		rm ./b2bua/*.log
	            echo "Distributed example used is b2bua early dialog failover";
	            echo""| sipp $MSS_IP:5080 -sf b2bua/call-forwarding-receiver-early.xml -i $SIPP_IP -p 5090 -nd >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver-fwd-ack -sf b2bua/call-forwarding-sender-early.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT $SIPP_OPTIONS -timeout_error
	            ;;
	    b2bua-remote-send-bye)
	    		rm ./b2bua/*.log
	            echo "Distributed example used is b2bua-remote-send-bye";
	            echo""| sipp $MSS_IP:5080 -sf b2bua/call-forwarding-receiver-sends-bye.xml -i $SIPP_IP -p 5090 -nd >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver -sf b2bua/call-forwarding-sender-receives-bye.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	            ;;
	    b2bua-remote-send-bye-no-ring)
	    		rm ./b2bua/*.log
	            echo "Distributed example used is b2bua-remote-send-bye-no-ring";
	            echo""| sipp $MSS_IP:5080 -sf b2bua/call-forwarding-receiver-sends-bye-no-ring.xml -i $SIPP_IP -p 5090 -nd >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5080 -s receiver -sf b2bua/call-forwarding-sender-receives-bye.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	            ;;
		uac)
				rm ./uac/*.log
	            echo "Distributed example used is uac";
	            sipp $MSS_IP:5080 -sf uac/receiver.xml -i $SIPP_IP -p 5090 -trace_msg -timeout $ACTIVE_TIMEOUT -timeout_error -nd
	            ;;
	    uac-register)
				rm ./uac/*.log
	            echo "Distributed example used is uac register";
	            sipp $MSS_IP:5080 -sf uac/register-receiver.xml -i $SIPP_IP -p 5090 -trace_msg -timeout $ACTIVE_TIMEOUT -timeout_error -nd
	            ;;
		c2c)
				rm ./converged-click2call/*.log
	            echo "Distributed example used is 3pcc";
	            echo "Now, quickly navigate a browser to this URL: http://"$MSS_IP"/click2call-distributable/call?to=sip:to@"$SIPP_IP":5090&from=sip:from@"$SIPP_IP":5091";
	            echo "Then, wait a few secs for the dialog to be established and kill the primary server. The secondary server will take over.";
	            echo "40 secs after you have loaded the URL, one of the SIPP clients will send a BYE to close the dialog.";
	            echo "If nothing happens, check if your apache server with mod_jk installed is running and configured properly.";
	            echo "There are sample config files to run apache and mod_jkon localhost in the converged-click2call directory.";
	            echo "If you need more info about how to configure mod_jk see http://www.jboss.org/community/docs/DOC-12525";
                killall sipp
	            sipp $MSS_IP:5080 -sf converged-click2call/receiver.xml -i $SIPP_IP -p 5090 -trace_msg -timeout 100 -bg
                sipp $MSS_IP:5080 -sf converged-click2call/receiver-sendbye.xml -i $SIPP_IP -p 5091 -trace_msg -timeout 100 -bg
                sleep 1
                #wget "http://$MSS_IP/click2call-distributable/call?to=sip:to@$SIPP_IP:5090&from=sip:from@$SIPP_IP:5091"
	            ;;
	    uas-reinvite)
	    		rm ./uas/*.log
	            echo "Distributed example used is uas reinvite";
	    		sipp $MSS_IP:5080 -s reinvite -sf uas/clustering-reinvite-uac.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	    		;;
	    uas-no-attributes)
	    		rm ./uas/*.log
	            echo "Distributed example used is uas no attributes";
	    		sipp $MSS_IP:5080 -s NoAttributes -sf uas/clustering-uac-no-attrs.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	    		;;
	    uas-remove-attributes)
	    		rm ./uas/*.log
	            echo "Distributed example used is uas remove attributes";
	    		sipp $MSS_IP:5080 -s reinvite-RemoveAttributes -sf uas/clustering-reinvite-uac.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	    		;;
	    uas-reinvite-passivation)
	    		rm ./uas/*.log
	            echo "Distributed example used is uas reinvite passivation make sure to make at least 2 calls to the same node so that it gets activated";
	    		sipp $MSS_IP:5080 -s reinvite -sf uas/clustering-reinvite-uac-passivation.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	            ;;			    
	    uas-timer)
	    		rm ./uas/*.log
	            echo "Distributed example used is uas timer";
	    		sipp $MSS_IP:5080 -s yousendbye -sf uas/clustering-uac-timer.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	            ;;
	    uas-injected-timer)
	    		rm ./uas/*.log
	            echo "Distributed example used is uas injected timer";
	    		sipp $MSS_IP:5080 -s yousendbye-injectedtimer -sf uas/clustering-uac-timer.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	            ;;
	    uas-cancel-timer)
	    		rm ./uas/*.log
	            echo "Distributed example used is uas cancel timer";
	    		sipp $MSS_IP:5080 -s cancelservlettimer -sf uas/clustering-uac.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	            ;;
	    uas-timer-passivation)
	    		rm ./uas/*.log
	            echo "Distributed example used is uas timer passivation";
	    		sipp $MSS_IP:5080 -s yousendbye -sf uas/clustering-uac-timer-passivation.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	            ;;
	    uas-sas-timer)
	    		# kill first node after the ACK
	    		rm ./uas/*.log
	            echo "Distributed example used is uas sas timer";
	    		sipp $MSS_IP:5080 -s sastimersendbye -sf uas/clustering-uac-timer.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error  		
	            ;;
	    uas-sas-timer-passivation)
	    		# kill first node after the ACK
	    		rm ./uas/*.log
	            echo "Distributed example used is uas sas timer passivation";
	    		sipp $MSS_IP:5080 -s sastimersendbye -sf uas/clustering-uac-timer-passivation.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error  		
	            ;;
		uas-reinvite-sas-timer)
	    		# kill first node after the first ACK
	    		rm ./uas/*.log
	            echo "Distributed example used is uas reinvite sas timer";
	    		sipp $MSS_IP:5080 -s sastimersendbye -sf uas/clustering-reinvite-sas-timer.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	            ;;
	    uas-activation)
	    		rm ./uas/*.log
	            echo "Distributed example used is uas activation";
	    		sipp $MSS_IP:5080 -s isendbye-test-activation -sf uas/clustering-uac.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	            ;;
	    uas)
	    		rm ./uas/*.log
	            echo "Distributed example used is uas";
	    		sipp $MSS_IP:5080 -s isendbye -sf uas/clustering-uac.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	            ;;
	    uas-0.0.0.0)
	    		rm ./uas/*.log
	            echo "Distributed example used is uas-0.0.0.0";
	    		sipp $MSS_IP:5080 -s isendbye -sf uas/clustering-uac.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -nd -timeout $ACTIVE_TIMEOUT -timeout_error
	            ;;
	    proxy-b2bua-ar)
	    		rm ./ar/*.log
	    		echo "Distributed example used is proxy-b2bua-ar";
	    		echo""| sipp $MSS_IP:5060 -sf ar/proxy-b2bua-ar-receiver.xml -i $SIPP_IP -p 5090 -nd >sipp-uas-log-$1.txt 2>&1 &
	    		sipp $MSS_IP:5060 -s receiver-failover -sf ar/proxy-b2bua-ar-sender.xml -trace_err -i $SIPP_IP -p 5050 -r $RATE -m $CALLS -rsa $LB_IP:5060 -trace_msg -timeout $ACTIVE_TIMEOUT -timeout_error
	            ;;
    esac
else
echo "Syntax is command SCENARIO TIMEOUT CALL_RATE NUMBER_OF_CALLS"
fi
