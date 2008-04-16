Install maven 2
Install Tomcat 6.0.14

open up the pom.xml from sip-servlets-bootstrap project and change the following property tomcat.home at the end of the file
Make it the same as your tomcat root installation by example E:\servers\apache-tomcat-6.0.14

Run mvn clean install from the project root (sip-servlets-bootstrap)

Open the project with Eclipse,
You need to add the M2_REPO variable so that eclipse can locate the jar files.
to do that right click on the project name, click on properties,
go to 'java build path'
Click on Libraries Tab, then on 'Add Variable' button
On the dialog popup, click on 'Configure variables' button
on the new popup, click on 'New' button
on the new popup(what a popup mess :-)), 
enter M2_REPO for the name input field 
and <your user directory>/.m2/repository for the value.
on windows <your user directory> shall be replaced by C:/Documents and Settings/Ranga by example.
Then OK to all and close all remaining popups. This should compile without errors
if it behaves otherwise mail me to jean.deruelle@gmail.com

To run the sip servlet container :
check that the following files have been copied during the mvn clean install
in tomcat_home/lib directory, there should be a sip-servlets-0.2.jar
in tomcat_home/lib directory, there should be a sip-servlets-spec-1.0.jar
in tomcat_home/lib directory, there should be a jain-sip-api-1.2.jar
in tomcat_home/lib directory, there should be a jain-sip-ri-1.2.jar
in tomcat_home/lib directory, there should be a concurrent-1.3.4.jar
in tomcat_home/lib directory, there should be a log4j-1.2.14.jar
in tomcat_home/lib directory, there should be a commons-logging-1.1.jar

make a backup of your tomcat_home/conf/server.xml file
copy the server-tomcat-6.xml located in docs to tomcat_home/conf and rename it to server.xml
look for service tag in the server.xml file and 
modify the darConfigurationFileLocation attribute to map to your filesystem
    	
go to your tomcat_home/bin directory
run the following command
catalina.bat run

Run a mvn clean install at the root of simple-sip-servlet project
and drop in the tomcat_home\webapps directory the war file generated in target dir of simple-sip-servlet, you should see the deployment occuring.

you should see the following logs on the standard output
18 juil. 2007 08:09:46 org.mobicents.servlet.sip.startup.SipContextConfig start
INFO: /WEB-INF/sip.xml has been found !
18 juil. 2007 08:09:46 org.mobicents.servlet.sip.startup.SipContextConfig start
INFO: /WEB-INF/sip.xml has been parsed !
log4j:WARN No appenders could be found for logger (SipContainer).
log4j:WARN Please initialize the log4j system properly.
the simple sip servlet has been started
18 juil. 2007 08:09:48 org.apache.coyote.http11.Http11BaseProtocol start
INFO: D�marrage de Coyote HTTP/1.1 sur http-8080
18 juil. 2007 08:09:48 org.apache.jk.common.ChannelSocket init
INFO: JK: ajp13 listening on /0.0.0.0:8009
18 juil. 2007 08:09:48 org.apache.jk.server.JkMain start
INFO: Jk running ID=0 time=0/15  config=null
18 juil. 2007 08:09:48 org.apache.catalina.storeconfig.StoreLoader load
INFO: Find registry server-registry.xml at classpath resource
18 juil. 2007 08:09:48 org.apache.catalina.startup.Catalina start
INFO: Server startup in 1984 ms

otherwise this file has not yet been updated with the latest steps or I screwed up somewhere. 
In any case provide some feedback as explained at the bottom of the home page at https://sip-servlets.dev.java.net/

To play with real examples check out the different examples located under trunk/sip-servlets-examples on the svn repo.
