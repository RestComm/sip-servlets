Reproducing TCK certification process for Mobicents SIP Servlets :
Download the TCK from http://www.oracle.com/technology/tech/java/standards/jsr289/tck_download.html and Unzip it in a location that will be referred as TCK_HOME for the remainder of this README.
Download JBOSS AS 4.2.2.GA from http://www.jboss.org/products/jbossas and unzip it in a location that will be referred as JBOSS_HOME for the remainder of this README.
Download maven 2 from http://maven.apache.org/
 
Checkout Mobicents Sip Servlets from http://mobicents.googlecode.com/svn/trunk/ with this command svn checkout http://mobicents.googlecode.com/svn/trunk/ mobicents-sip-servlets (this location will be referred as MSS_HOME)
Follow the instructions from http://www.mobicents.org/installation-jboss.html to make the JBoss AS a converged sip container.
Copy the server.xml located in this directory to $JBOSS_HOME/server/default/deploy/jboss-web.deployer/server.xml

Run the following command to remove our application router jar from the JBoss AS with the following command rm $JBOSS_HOME/server/default/deploy/jboss-web.deployer/sip-servlets-application-router-0.6-SNAPSHOT.jar
Copy the TCK app router jar to JBoss AS with the following command cp $TCK_DIRECTORY/dist/tck-approuter.jar $JBOSS_HOME/server/default/deploy/jboss-web.deployer/  

Copy the files from this directory (excludeList.properties and default.properties) to the $TCK_HOME/tck/conf directory
Change the $TCK_HOME/tck/conf/signature.properties to point to your local version of the sip servlets specification jar (namely $MSS_HOME/sip-servlets-spec/target/sip-servlets-spec-1.1.6-SNAPSHOT.jar)

In the TCK, Change the web.xml of the apitestapp so that the url-pattern tag is equals to /*, rebuild it to have a new apitestapp.sar and copy it to $TCK_HOME/tck/dist
In the TCK, Rebuild the uas app to have a clean uas.sar (otherwise the ContextTest fails, it seems the sar shipped with the TCK is an old version of the servlet) and copy it to $TCK_HOME/tck/dist

Change all the files present in $TCK_HOME/tck/dist that end with the .sar extension to end with .war extension instead (or .sar2) and copy them to $JBOSS_HOME/server/default/deploy/
Start JBoss AS from $JBOSS_HOME/bin with the following command sh run.sh. Wait for it to start completely

Run dos2unix *.sh command in $TCK_HOME/tck/bin
Run the following command sh runAllTests.sh from $TCK_HOME/tck/bin/ 
