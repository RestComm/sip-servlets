#!/bin/sh
# This script is used for the environment variables setup for Linux/UNIX
echo ${JAVA_HOME}
if [ "${JAVA_HOME}" = "" ] ; then 
	echo "JAVA_HOME is not set."
	echo "Please set JAVA_HOME to the directory of your local JDK and the JDK version must be 1.5 or later."  
  	exit
fi
echo ${ANT_HOME}
if [ "${ANT_HOME}" = "" ] ; then 
	echo "ANT_HOME is not set."
	echo "Please set ANT_HOME to the directory of your local ant and the ant version must be 1.6.5 or later." 
	exit
fi
# Get and Set REFERENCE_JAR from conf/signature.properties file
export SIGCONFIG=${TCK_DIRECTORY}/conf/signature.properties
if [ ! -f "$SIGCONFIG" ]; then 
	echo "The signature.properties does not exist in the conf directory"
	exit
fi
# set the location of the referenced JSR289 Jar file for signature test. 
export REFERENCE_JAR=`awk   'BEGIN{FS="="} {print  $2 }'< ${SIGCONFIG}`
if [ ! -f "$REFERENCE_JAR" ]; then 
	echo "The reference jar: $REFERENCE_JAR does not exist"
	exit
fi
# Set CLASSPATH for JSR289 TCK.
export JSR289TCK_HOME=${TCK_DIRECTORY}/..
export TCK_HOME=${JSR289TCK_HOME}/tck
export TCK_LIB=${TCK_HOME}/lib
export SIPUNIT_HOME=${JSR289TCK_HOME}/sipunit
export SIPUNIT_LIB=${SIPUNIT_HOME}/lib
export APIJAR="${TCK_LIB}/servlet-2_5-api.jar:${TCK_LIB}/sipservlet-1_1-api.jar"
export REPORT_HOME=${TCK_HOME}/report
export SIGREPORT_HOME=${REPORT_HOME}/sig_report
export SIGTEST_LIB=${TCK_LIB}/sigtest.jar
export TCKJUNIT_LIB=${TCK_LIB}/tck-junit.jar
export JUNIT_LIB=${SIPUNIT_LIB}/junit.jar
export SIGNATURE_FILE_NAME=${TCK_DIRECTORY}/bin/jsr289_api.sig
export SIGNATURE_REPORT_NAME=jsr289SignatureReport.txt
export CLASSPATH=${JAVA_HOME}/lib/tools.jar:${TCKJUNIT_LIB}:${JUNIT_LIB}:${SIGTEST_LIB}:${TCK_HOME}/conf
export PATH=${JAVA_HOME}/jre/bin:${ANT_HOME}/bin:$PATH

echo Classpath ${CLASSPATH}
echo Path ${PATH}
echo ant_home ${ANT_HOME}