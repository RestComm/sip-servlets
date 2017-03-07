export EXAMPLES_HOME=../../../sip-servlets-examples

export config1="standalone-sip-ha.xml"
export config2="standalone-sip-ha-node2.xml"
export deployments_dir1="deployments"
export deployments_dir2="deployments-node2"
export KILL_PARAMS="-9"

export FULLSTARTSLEEP=200
export HALFSTARTSLEEP=200
export CALLS=5

if [ "x$1" != "x" ]; then
    export FULLSTARTSLEEP=$1
fi

if [ "x$2" != "x" ]; then
    export HALFSTARTSLEEP=$2
fi

if [ "x$3" != "x" ]; then
    export config1=$3
fi

if [ "x$4" != "x" ]; then
    export config2=$4
fi

export EXIT_CODE=0;

##################################
# Test Custom B2BUA
##################################
#echo "Test Custom B2BUA"
#echo "================================"
./auto-prepare-example.sh custom-b2bua $deployments_dir1
./auto-prepare-example.sh custom-b2bua $deployments_dir2

##################################
# Test b2bua
##################################
#echo "Test b2bua"
#echo "================================"
#./auto-prepare-example.sh b2bua $deployments_dir1
#./auto-prepare-example.sh b2bua $deployments_dir2

##################################
# Test proxy
##################################
#echo "Test proxy"
#echo "================================"
#./auto-prepare-example.sh proxy $deployments_dir1
#./auto-prepare-example.sh proxy $deployments_dir2

##################################
# Test UAS
##################################
#echo "Test UAS"
#echo "================================"
#./auto-prepare-example.sh uas $deployments_dir1
#./auto-prepare-example.sh uas $deployments_dir2

##################################
# Test UAC (should always be the last test or it may messed up the other tests) 
##################################
#echo "Test UAC"
#echo "================================"
#./auto-prepare-example.sh uac $deployments_dir1 -Dsend.on.init=true
#./auto-prepare-example.sh uac $deployments_dir2 -Dsend.on.init=false


##################################
# Test UAC REGISTER (should always be the last test or it may messed up the other tests) 
##################################
#echo "Test UAC REGISTER"
#echo "================================"
#./auto-prepare-example.sh uac-register $deployments_dir1 -Dsend.on.init=true
#./auto-prepare-example.sh uac-register $deployments_dir2 -Dsend.on.init=false

