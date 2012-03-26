BUILD_DIR=/tmp/mss-as7
mkdir -p $BUILD_DIR
cd $BUILD_DIR

#
# get as7 release (trunk could be used too, nightly build is not enough as it does not include artifacts
#   needed to built our extension)
## 
## to get nightly
##wget https://ci.jboss.org/jenkins/job/JBoss-AS-7.x-latest/lastSuccessfulBuild/artifact/build/target/jboss-as-7.x.zip
##unzip jboss-as-7.x.zip
## to get trunk and build
##git clone https://github.com/jbossas/jboss-as.git
##cd jboss-as
##mvn clean install -DskipTests
#
# must be in sync with
wget http://download.jboss.org/jbossas/7.1/jboss-as-7.1.1.Final/jboss-as-7.1.1.Final.zip
unzip jboss-as-7.1.1.Final.zip
#
# set AS7_TAG as name of the AS7 release/tag
AS7_TAG=jboss-as-7.1.1.Final

#
# get sipservlets repo and build, including as7 abstraction layer
git clone https://code.google.com/p/sipservlets/
cd sipservlets
git checkout trunk
mvn clean install -Pas7

#
# build sip-servlets-as7-drop-in modules and install in AS7
cd sip-servlets-as7-drop-in
pushd jboss-as-mobicents/
mvn clean install
popd
pushd build-mobicents-modules/
mvn clean package
#
# modules installation
cp -pr target/$AS7_TAG/modules/org/mobicents/ $BUILD_DIR/$AS7_TAG/modules/org/
popd

#
# Create standalone-sip.xml file
cd $BUILD_DIR/$AS7_TAG
cp standalone/configuration/standalone.xml standalone/configuration/standalone-sip.xml
patch -p0 --verbose < $BUILD_DIR/sipservlets/sip-servlets-as7-drop-in/patches/patch.standalone.sip.dropin.xml

#
# Configure jboss-as-web module
cd $BUILD_DIR/$AS7_TAG
patch -p0 --verbose < $BUILD_DIR/sipservlets/sip-servlets-as7-drop-in/patches/patch.jboss-as-web.module.xml

#
# Configure jboss-as-ee module
cd $BUILD_DIR/$AS7_TAG
patch -p0 --verbose < $BUILD_DIR/sipservlets/sip-servlets-as7-drop-in/patches/patch.jboss-as-ee.module.xml

#
# Run AS7
./bin/standalone.sh -c standalone-sip.xml -Djavax.servlet.sip.dar=file:///tmp/mobicents-dar.properties

