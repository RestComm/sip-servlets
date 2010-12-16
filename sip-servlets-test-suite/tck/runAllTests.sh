#!/bin/sh

# Starts the JSR289 TCK Tests which include Signature test,API tests and SPEC tests.
# Setup the environments before run JSR289 TCK.

. ${TCK_DIRECTORY}/bin/setupENV.sh

# Start JSR289 Signatre Test.
java -cp "${TCK_LIB}/servlet-2_5-api.jar:${REFERENCE_JAR}:${SIGTEST_LIB}" com.sun.tdk.signaturetest.SignatureTest -FileName ${SIGNATURE_FILE_NAME} -apiVersion 2.0 -out ${SIGNATURE_REPORT_NAME} -package javax.servlet.sip -debug -verbose

# Copy the Signature test report to signature report directory.        
if [ ! -d "$SIGREPORT_HOME" ]; then
	mkdir -p ${SIGREPORT_HOME}
fi

cp ${SIGNATURE_REPORT_NAME} ${SIGREPORT_HOME}
rm ${SIGNATURE_REPORT_NAME}
 
cd ${TCK_HOME}
echo ant home ${ANT_HOME}
#${ANT_HOME}/bin/ant -no_config run-tck
export ANT_LIB=${ANT_HOME}/lib
export LOCALCLASSPATH=$LOCALCLASSPATH:$JAVA_HOME/lib/tools.jar
export LOCALCLASSPATH=$ANT_LIB/ant-launcher.jar:$LOCALCLASSPATH
echo LOCALCLASSPATH $LOCALCLASSPATH
export CLASSPATH=$ANT_LIB/ant-launcher.jar:$CLASSPATH
echo CLASSPATH $CLASSPATH


java $ANT_OPTS -classpath $LOCALCLASSPATH -Dant.home=$ANT_HOME -Dant.library.dir=$ANT_LIB $ant_sys_opts org.apache.tools.ant.launch.Launcher $ANT_ARGS -cp $CLASSPATH run-tck
cd ./bin
