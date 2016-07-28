#!/bin/sh -e
BUILD_DIR=./build/release/target/restcomm-sip-servlets-as7
CHECKOUT_DIR=./build/release/target/dependencies
#
# set AS7_TAG as name of the AS7 release/tag
AS7_FINAL_NAME=jboss-as-7.2.0.Final
AS7_FINAL_NAME_EAP=jboss-eap-6.4
#AS7_NAME=jboss-as-7.2.0.Final
#AS7_TAG=7.2.0.Final	
VERSION=$1
MSS_FINAL_NAME=restcomm-sip-servlets-$VERSION-$AS7_FINAL_NAME_EAP

rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR
rm -rf $CHECKOUT_DIR
mkdir -p $CHECKOUT_DIR

## get tag and build to have maven deps installed in the local maven repo
#git clone https://github.com/jbossas/jboss-as.git $CHECKOUT_DIR/jboss-as
#cd $CHECKOUT_DIR/jboss-as
#git checkout $AS7_TAG
#mvn clean install -DskipTests

# get JBoss AS 7.2.0 release 
cd $CHECKOUT_DIR
#wget https://app.box.com/shared/static/xaw5io4mj1dd7yzsoxlg.zip
#unzip xaw5io4mj1dd7yzsoxlg.zip
cp /home/posger/alerant/projects/sip/downloads/jboss-eap-6.4.0.zip .
unzip jboss-eap-6.4.0.zip

cd ../../../..
mv $CHECKOUT_DIR/$AS7_FINAL_NAME_EAP $BUILD_DIR
mv $BUILD_DIR/$AS7_FINAL_NAME_EAP $BUILD_DIR/$MSS_FINAL_NAME

# build sip-servlets-as7-drop-in modules and install in AS7
mvn clean install -Pas7

# modules installation
cp -vpr ./containers/sip-servlets-as7-drop-in/build-restcomm-modules/target/$AS7_FINAL_NAME/modules/system/layers/base/org/mobicents $BUILD_DIR/$MSS_FINAL_NAME/modules/system/layers/base/org/mobicents
mvn clean install war:inplace -f ./sip-servlets-examples/click-to-call/pom.xml
cp -pr ./sip-servlets-examples/click-to-call/target/click-to-call-servlet-*.war $BUILD_DIR/$MSS_FINAL_NAME/standalone/deployments/click2call.war
mvn clean install war:inplace -f ./sip-servlets-examples/websocket-b2bua/pom.xml
cp -pr ./sip-servlets-examples/websocket-b2bua/target/websockets-sip-servlet-*.war $BUILD_DIR/$MSS_FINAL_NAME/standalone/deployments/websockets-sip-servlet.war
mvn clean install war:inplace -f ./sip-servlets-examples/media-jsr309-servlet/pom.xml
cp -pr ./sip-servlets-examples/media-jsr309-servlet/target/media-jsr309-servlet.war $BUILD_DIR/$MSS_FINAL_NAME/standalone/deployments/media-jsr309-servlet.war
mvn clean install war:inplace -f ./management/sip-servlets-management/pom.xml
cp -pr ./management/sip-servlets-management/target/sip-servlets-management.war $BUILD_DIR/$MSS_FINAL_NAME/standalone/deployments/sip-servlets-management.war
wget -nc http://labs.consol.de/maven/repository/org/jolokia/jolokia-war/1.1.0/jolokia-war-1.1.0.war -O  $BUILD_DIR/$MSS_FINAL_NAME/standalone/deployments/jolokia.war
mkdir -p $BUILD_DIR/$MSS_FINAL_NAME/standalone/configuration/dars
mkdir -p $BUILD_DIR/$MSS_FINAL_NAME/domain/configuration/dars
cp $BUILD_DIR/../../../../sip-servlets-examples/websocket-b2bua/websocket-dar.properties $BUILD_DIR/$MSS_FINAL_NAME/standalone/configuration/dars/mobicents-dar.properties
cp $BUILD_DIR/../../../../sip-servlets-examples/websocket-b2bua/websocket-dar.properties $BUILD_DIR/$MSS_FINAL_NAME/domain/configuration/dars/mobicents-dar.properties
cp $BUILD_DIR/../../mss-sip-stack.properties $BUILD_DIR/$MSS_FINAL_NAME/standalone/configuration/
cp $BUILD_DIR/../../mss-sip-stack.properties $BUILD_DIR/$MSS_FINAL_NAME/domain/configuration/mss-sip-stack.properties

# move a class file from the mobicents module jar to the jboss module jar in order to access package private class; necessary because: https://issues.jboss.org/browse/AS7-3305 
mkdir tmp1
unzip $BUILD_DIR/$MSS_FINAL_NAME/modules/system/layers/base/org/mobicents/libs/main/sip-servlets-as7-*.jar -d tmp1/
zip -d $BUILD_DIR/$MSS_FINAL_NAME/modules/system/layers/base/org/mobicents/libs/main/sip-servlets-as7-*.jar org/jboss/as/web/session/ExposedSessionBasedClusteredSession.class

mkdir tmp2
unzip $BUILD_DIR/$MSS_FINAL_NAME/modules/system/layers/base/org/jboss/as/web/main/jboss-as-web-*.jar -d tmp2/

cp -f tmp1/org/jboss/as/web/session/ExposedSessionBasedClusteredSession.class tmp2/org/jboss/as/web/session/

DESTINATION_JAR_NAME="$(basename `ls $BUILD_DIR/$MSS_FINAL_NAME/modules/system/layers/base/org/jboss/as/web/main/jboss-as-web-*.jar`)"
cd tmp2/
zip -r ../$DESTINATION_JAR_NAME ./*
cd ..

cp -f $DESTINATION_JAR_NAME $BUILD_DIR/$MSS_FINAL_NAME/modules/system/layers/base/org/jboss/as/web/main/$DESTINATION_JAR_NAME

rm $DESTINATION_JAR_NAME
rm -rf tmp1
rm -rf tmp2

# extend the dependencies of the org.jboss.as.clustering.common:main module with the org.mobicents.libs:main module, to prevent ClassNotFound exceptions during runtime
sed -i '/<\/dependencies>/i\\t<module name="org.mobicents.libs"/>' $BUILD_DIR/$MSS_FINAL_NAME/modules/system/layers/base/org/jboss/as/clustering/common/main/module.xml

#Copy conf settings for standalone and domain profiles
cp -vpr $BUILD_DIR/../../as7-standalone-conf $BUILD_DIR/$MSS_FINAL_NAME/bin/standalone.conf
cp -vpr $BUILD_DIR/../../as7-domain-conf $BUILD_DIR/$MSS_FINAL_NAME/bin/domain.conf
# Copy load balancer
cp -vpr ./build/release/sip-balancer $BUILD_DIR/$MSS_FINAL_NAME/

cd $BUILD_DIR/$MSS_FINAL_NAME

# Create standalone-sip.xml file
cp ./standalone/configuration/standalone.xml ./standalone/configuration/standalone-sip.xml

patch -p0 --verbose < ../../../../../containers/sip-servlets-as7-drop-in/patches/patch.7.2.0.Final.standalone.sip.dropin.xml
#patch -p0 --verbose < ../../../../../containers/sip-servlets-as7-drop-in/patches/patch.domain.sip.dropin.xml

#
# Configure jboss-as-web module
patch -p0 --verbose < ../../../../../containers/sip-servlets-as7-drop-in/patches/patch.7.2.0.Final.jboss-as-web.module.xml

#
# Configure jboss-as-ee module
patch -p0 --verbose < ../../../../../containers/sip-servlets-as7-drop-in/patches/patch.7.2.0.Final.jboss-as-ee.module.xml

### TODO: replace the cp commands with the commented patch commands, (similarly to how the other xml files are constructed). To do this, the appropriate patch files have to be generated.
# Create standalone-sip-ha.xml and standalone-sip-ha-node2.xml files for clustered mode
# Node 1
# patch -p0 --verbose -o ./standalone/configuration/standalone-sip-ha.xml < ../../../../../containers/sip-servlets-as7-drop-in/patches/patch.7.2.0.Final.standalone.sip.ha.dropin.xml
# Node 2
# patch -p0 --verbose -o ./standalone/configuration/standalone-sip-ha-node2.xml < ../../../../../containers/sip-servlets-as7-drop-in/patches/patch.7.2.0.Final.standalone.sip.ha.node2.dropin.xml
cp ../../../../../containers/sip-servlets-as7-drop-in/jboss-as-mobicents/standalone-sip-ha.xml ./standalone/configuration/standalone-sip-ha.xml
cp ../../../../../containers/sip-servlets-as7-drop-in/jboss-as-mobicents/standalone-sip-ha-node2.xml ./standalone/configuration/standalone-sip-ha-node2.xml

cd ..


ant extract-mms -f ../../build.xml -Dmss.home=./target/restcomm-sip-servlets-as7/$MSS_FINAL_NAME
#ant build-mobicents-sip-load-balancer -f ../../build.xml -Dmss.home=./target/mss-as7/$MSS_FINAL_NAME

zip -r $MSS_FINAL_NAME.zip $MSS_FINAL_NAME

#
# Run AS7
#export JBOSS_HOME=$BUILD_DIR/$AS7_TAG
#./bin/standalone.sh -c standalone-sip.xml -Djavax.servlet.sip.dar=file:///tmp/mobicents-dar.properties
