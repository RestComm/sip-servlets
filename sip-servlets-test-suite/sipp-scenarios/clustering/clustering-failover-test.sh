killall sipp
export TEST_IP=127.0.0.1

export MSS_IP=$TEST_IP
export LB_IP=$TEST_IP
export SIPP_IP=$TEST_IP



if [ "x$SIPP_TIMEOUT" = "x" ]; then
    export ACTIVE_TIMEOUT=$2
else
    export ACTIVE_TIMEOUT=$SIPP_TIMEOUT
fi

if [ "x$CALL_RATE" = "x" ]; then
    export RATE=$3
else 
    export RATE=$CALL_RATE
fi
if [ "x$CALLS" = "x" ]; then
    export CALLS=$4
fi

echo Using $MSS_IP as the MSS test IP
echo Using $LB_IP as the Load Balancer IP
echo Using $SIPP_IP as the SIPp IP

if [ "x$RESULTS_DIR" = "x" ]; then
    CSV_FILE=$1.csv
else
    CSV_FILE=$RESULTS_DIR/$1.csv
fi

if [ "x$SIPP_OPTIONS" = "x" ]; then
    SIPP_OPTIONS="-i $SIPP_IP -trace_err -trace_msg -nd "
fi
if [ "x$SIPP_RECEIVER_OPTIONS" = "x" ]; then
    SIPP_RECEIVER_OPTIONS="-p 5090 $SIPP_OPTIONS"
fi
if [ "x$SIPP_SENDER_OPTIONS" = "x" ]; then
    SIPP_SENDER_OPTIONS="$SIPP_OPTIONS -p 5050 -rsa $LB_IP:5060 -timeout $ACTIVE_TIMEOUT -timeout_error -trace_stat  -stf $CSV_FILE -r $RATE -m $CALLS"
fi

echo "SIPP_RECEIVER_OPTIONS: $SIPP_RECEIVER_OPTIONS"
echo "SIPP_SENDER_OPTIONS: $SIPP_SENDER_OPTIONS"


if [ $# -eq 4 ]; then
	case $1 in	
	    proxy)
	    		rm -f ./proxy/*.log
	    		echo "Distributed example used is proxy";
	    		echo""| $PRECOMPILED_SIPP $MSS_IP:5080 -sf proxy/location-service-receiver.xml $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver-failover -sf proxy/location-service-sender.xml $SIPP_SENDER_OPTIONS
	            ;;
	    proxy-early)
	    		rm -f ./proxy/*.log
	    		echo "Distributed example used is proxy early dialog failover";
	    		echo""| sipp $MSS_IP:5080 -sf proxy/location-service-receiver-early.xml $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver-failover -sf proxy/location-service-sender-early.xml $SIPP_SENDER_OPTIONS
	            ;;
	    proxy-remote-send-bye)
	    		rm -f  ./proxy/*.log
	    		echo "Distributed example used is proxy remote send bye";
	    		echo""| $PRECOMPILED_SIPP $MSS_IP:5080 -sf proxy/location-service-receiver-sends-bye.xml $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver-failover -sf proxy/location-service-sender-receives-bye.xml $SIPP_SENDER_OPTIONS
	            ;;    
	    proxy-indialog-info)
	    		rm -f  ./proxy/*.log
	    		echo "Distributed example used is proxy indialog info";
	    		echo""| $PRECOMPILED_SIPP $MSS_IP:5080 -sf proxy/location-service-indialog-info-receiver.xml $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver-failover -sf proxy/location-service-indialog-info-sender.xml $SIPP_SENDER_OPTIONS
	    		;;
	    proxy-termination)
	    		rm -f  ./proxy/*.log
	    		echo "Distributed example used is proxy termination";
	    		echo""| $PRECOMPILED_SIPP $MSS_IP:5080 -sf proxy/location-service-termination-receiver.xml $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver-failover -sf proxy/location-service-termination-sender.xml $SIPP_SENDER_OPTIONS 
	    		;;
	    custom-b2bua)
	    		rm -f  ./b2bua/*.log
	            echo "Distributed example used is custom b2bua";
	            echo""| $PRECOMPILED_SIPP $MSS_IP:5080 -sf b2bua/custom-call-forwarding-receiver.xml $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver -sf b2bua/custom-call-forwarding-sender.xml $SIPP_SENDER_OPTIONS
	            ;;
	    custom-b2bua-early)
	    		rm -f  ./b2bua/*.log
	            echo "Distributed example used is custom b2bua early dialog failover";
	            echo""| $PRECOMPILED_SIPP $MSS_IP:5080 -sf b2bua/custom-call-forwarding-receiver-early.xml  $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver -sf b2bua/custom-call-forwarding-sender-early.xml  $SIPP_SENDER_OPTIONS
	            ;; 
	    custom-b2bua-udp-tcp)
	    		rm -f  ./b2bua/*.log
	            echo "Distributed example used is custom b2bua udp tcp";
	            echo""| $PRECOMPILED_SIPP $MSS_IP:5080 -sf b2bua/custom-call-forwarding-receiver.xml  -t t1 $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver-tcp -sf b2bua/custom-call-forwarding-sender.xml  $SIPP_SENDER_OPTIONS
	            ;;
	    custom-b2bua-tcp-tcp)
	    		rm -f  ./b2bua/*.log
	            echo "Distributed example used is custom b2bua tcp tcp";
	            echo""| $PRECOMPILED_SIPP $MSS_IP:5080  -sf b2bua/custom-call-forwarding-receiver.xml  -t t1 $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver-tcp  -sf b2bua/custom-call-forwarding-sender.xml  $SIPP_SENDER_OPTIONS 
	            ;;
	    b2bua)
	    		rm -f  ./b2bua/*.log
	            echo "Distributed example used is b2bua";
	            echo""| $PRECOMPILED_SIPP $MSS_IP:5080 -sf b2bua/call-forwarding-receiver.xml   $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver -sf b2bua/call-forwarding-sender.xml   $SIPP_SENDER_OPTIONS 
	            ;;
	    b2bua-info)
	    		rm -f  ./b2bua/*.log
	            echo "Distributed example used is b2bua info";
	            echo""| sipp $MSS_IP:5080 -sf b2bua/call-forwarding-receiver-info.xml   $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver -sf b2bua/call-forwarding-sender-info.xml  $SIPP_SENDER_OPTIONS 
	            ;;       	    
	    b2bua-early)
	    		rm -f  ./b2bua/*.log
	            echo "Distributed example used is b2bua early dialog failover";
	            echo""| $PRECOMPILED_SIPP $MSS_IP:5080 -sf b2bua/call-forwarding-receiver-early.xml  $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver -sf b2bua/call-forwarding-sender-early.xml   $SIPP_SENDER_OPTIONS 
	            ;;
	    b2bua-early-linked)
	    		rm -f  ./b2bua/*.log
	            echo "Distributed example used is b2bua early dialog failover with linked requests";
	            echo""| $PRECOMPILED_SIPP $MSS_IP:5080 -sf b2bua/call-forwarding-receiver-early.xml  $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		PRECOMPILED_SIPP $MSS_IP:5080 -s receiver-linked -sf b2bua/call-forwarding-sender-early.xml   $SIPP_SENDER_OPTIONS  
	            ;;
	    b2bua-early-fwd-ack)
	    		rm -f  ./b2bua/*.log
	            echo "Distributed example used is b2bua early dialog failover";
	            echo""| $PRECOMPILED_SIPP $MSS_IP:5080 -sf b2bua/call-forwarding-receiver-early.xml  $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver-fwd-ack -sf b2bua/call-forwarding-sender-early.xml  $SIPP_SENDER_OPTIONS 
	            ;;
	    b2bua-remote-send-bye)
	    		rm -f  ./b2bua/*.log
	            echo "Distributed example used is b2bua-remote-send-bye";
	            echo""| $PRECOMPILED_SIPP $MSS_IP:5080 -sf b2bua/call-forwarding-receiver-sends-bye.xml   $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver -sf b2bua/call-forwarding-sender-receives-bye.xml  $SIPP_SENDER_OPTIONS 
	            ;;
	    b2bua-remote-send-bye-no-ring)
	    		rm -f  ./b2bua/*.log
	            echo "Distributed example used is b2bua-remote-send-bye-no-ring";
	            echo""| $PRECOMPILED_SIPP $MSS_IP:5080 -sf b2bua/call-forwarding-receiver-sends-bye-no-ring.xml   $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s receiver -sf b2bua/call-forwarding-sender-receives-bye.xml  $SIPP_SENDER_OPTIONS 
	            ;;
		uac)
				rm -f  ./uac/*.log
	            echo "Distributed example used is uac";
	            $PRECOMPILED_SIPP $MSS_IP:5080 -sf uac/receiver.xml -p 5090
	            ;;
	    uac-register)
				rm -f  ./uac/*.log
	            echo "Distributed example used is uac register";
	            $PRECOMPILED_SIPP $MSS_IP:5080 -sf uac/register-receiver.xml -p 5090
	            ;;
		c2c)
				rm -f  ./converged-click2call/*.log
	            echo "Distributed example used is 3pcc";
	            echo "Now, quickly navigate a browser to this URL: http://"$MSS_IP"/click2call-distributable/call?to=sip:to@"$SIPP_IP":5090&from=sip:from@"$SIPP_IP":5091";
	            echo "Then, wait a few secs for the dialog to be established and kill the primary server. The secondary server will take over.";
	            echo "40 secs after you have loaded the URL, one of the SIPP clients will send a BYE to close the dialog.";
	            echo "If nothing happens, check if your apache server with mod_jk installed is running and configured properly.";
	            echo "There are sample config files to run apache and mod_jkon localhost in the converged-click2call directory.";
	            echo "If you need more info about how to configure mod_jk see http://www.jboss.org/community/docs/DOC-12525";
                killall sipp
	            $PRECOMPILED_SIPP $MSS_IP:5080 -sf converged-click2call/receiver.xml -timeout 100 -bg
                $PRECOMPILED_SIPP $MSS_IP:5080 -sf converged-click2call/receiver-sendbye.xml -p 5091 -timeout 100 -bg
                sleep 1
                #wget "http://$MSS_IP/click2call-distributable/call?to=sip:to@$SIPP_IP:5090&from=sip:from@$SIPP_IP:5091"
	            ;;
	    uas-reinvite)
	    		rm -f  ./uas/*.log
	            echo "Distributed example used is uas reinvite";
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s reinvite -sf uas/clustering-reinvite-uac.xml  $SIPP_SENDER_OPTIONS 
	    		;;
	    uas-no-attributes)
	    		rm -f  ./uas/*.log
	            echo "Distributed example used is uas no attributes";
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s NoAttributes -sf uas/clustering-uac-no-attrs.xml  $SIPP_SENDER_OPTIONS 
	    		;;
	    uas-remove-attributes)
	    		rm -f  ./uas/*.log
	            echo "Distributed example used is uas remove attributes";
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s reinvite-RemoveAttributes -sf uas/clustering-reinvite-uac.xml  $SIPP_SENDER_OPTIONS 
	    		;;
	    uas-reinvite-passivation)
	    		rm -f  ./uas/*.log
	            echo "Distributed example used is uas reinvite passivation make sure to make at least 2 calls to the same node so that it gets activated";
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s reinvite -sf uas/clustering-reinvite-uac-passivation.xml  $SIPP_SENDER_OPTIONS 
	            ;;			    
	    uas-timer)
	    		rm -f  ./uas/*.log
	            echo "Distributed example used is uas timer";
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s yousendbye -sf uas/clustering-uac-timer.xml  $SIPP_SENDER_OPTIONS 
	            ;;
	    uas-injected-timer)
	    		rm -f  ./uas/*.log
	            echo "Distributed example used is uas injected timer";
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s yousendbye-injectedtimer -sf uas/clustering-uac-timer.xml  $SIPP_SENDER_OPTIONS 
	            ;;
	    uas-cancel-timer)
	    		rm -f  ./uas/*.log
	            echo "Distributed example used is uas cancel timer";
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s cancelservlettimer -sf uas/clustering-uac.xml  $SIPP_SENDER_OPTIONS 
	            ;;
	    uas-timer-passivation)
	    		rm -f  ./uas/*.log
	            echo "Distributed example used is uas timer passivation";
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s yousendbye -sf uas/clustering-uac-timer-passivation.xml  $SIPP_SENDER_OPTIONS 
	            ;;
	    uas-sas-timer)
	    		# kill first node after the ACK
	    		rm -f  ./uas/*.log
	            echo "Distributed example used is uas sas timer";
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s sastimersendbye -sf uas/clustering-uac-timer.xml  $SIPP_SENDER_OPTIONS	
	            ;;
	    uas-sas-timer-passivation)
	    		# kill first node after the ACK
	    		rm -f  ./uas/*.log
	            echo "Distributed example used is uas sas timer passivation";
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s sastimersendbye -sf uas/clustering-uac-timer-passivation.xml  $SIPP_SENDER_OPTIONS	
	            ;;
		uas-reinvite-sas-timer)
	    		# kill first node after the first ACK
	    		rm -f  ./uas/*.log
	            echo "Distributed example used is uas reinvite sas timer";
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s sastimersendbye -sf uas/clustering-reinvite-sas-timer.xml  $SIPP_SENDER_OPTIONS  
	            ;;
	    uas-activation)
	    		rm -f  ./uas/*.log
	            echo "Distributed example used is uas activation";
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s isendbye-test-activation -sf uas/clustering-uac.xml  $SIPP_SENDER_OPTIONS 
	            ;;
	    uas)
	    		rm -f  ./uas/*.log
	            echo "Distributed example used is uas";
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s isendbye -sf uas/clustering-uac.xml  $SIPP_SENDER_OPTIONS 
	            ;;
	    uas-0.0.0.0)
	    		rm -f  ./uas/*.log
	            echo "Distributed example used is uas-0.0.0.0";
	    		$PRECOMPILED_SIPP $MSS_IP:5080 -s isendbye -sf uas/clustering-uac.xml  $SIPP_SENDER_OPTIONS 
	            ;;
	    proxy-b2bua-ar)
	    		rm -f  ./ar/*.log
	    		echo "Distributed example used is proxy-b2bua-ar";
	    		echo""| $PRECOMPILED_SIPP $MSS_IP:5060 -sf ar/proxy-b2bua-ar-receiver.xml  $SIPP_RECEIVER_OPTIONS >sipp-uas-log-$1.txt 2>&1 &
	    		$PRECOMPILED_SIPP $MSS_IP:5060 -s receiver-failover -sf ar/proxy-b2bua-ar-sender.xml  $SIPP_SENDER_OPTIONS
	            ;;
    esac
else
echo "Syntax is command SCENARIO TIMEOUT CALL_RATE NUMBER_OF_CALLS"
fi
