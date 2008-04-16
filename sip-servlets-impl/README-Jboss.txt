Install maven 2
Install Jboss 4.2.2.GA

open up the pom.xml from sip-servlets-bootstrap project and change the following property jboss.home at the end of the file
Make it the same as your jboss root installation by example E:\servers\jboss-4.2.2.GA

Run 
mvn clean install -P jboss (-P jboss is to activate the jboss profile) 
from the project root (sip-servlets-bootstrap)

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
check that the following files have been copied during the mvn clean install -P jboss
in jboss_home/server/default/lib directory, there should be a sip-servlets-0.2.jar
in jboss_home/server/default/lib directory, there should be a sip-servlets-spec-1.0.jar
in jboss_home/server/default/lib directory, there should be a jain-sip-api-1.2.jar
in jboss_home/server/default/lib directory, there should be a jain-sip-ri-1.2.jar
in jboss_home/server/default/lib directory, there should be a concurrent-1.3.4.jar
in jboss_home/server/default/lib directory, there should be a log4j-1.2.14.jar
in jboss_home/server/default/lib directory, there should be a commons-logging-1.1.jar

make a backup of your jboss_home/server/default/deploy/jboss-web.deployer/server.xml file
copy the server-jboss.xml located in docs to jboss_home/server/default/deploy/jboss-web.deployer/ and rename it to server.xml
look for service tag in the server.xml file and 
modify the darConfigurationFileLocation attribute to map to your filesystem

Now you need to modify some extra config files so that jboss can deploy sar2/war files 
containing sip servlet application :

In your jboss_home\server\default\deploy\jboss-web.deployer\context.xml :
Add this attribute to the manager xml tag, className="org.mobicents.servlet.sip.core.session.SipStandardManager"

In your jboss_home\server\default\deploy\jboss-web.deployer\META-INF\jboss-service.xml :
modify the code attribute of the mbean tag so that it become code="org.mobicents.servlet.sip.startup.jboss.JBossSip"

In your jboss_home\server\default\deploy\jboss-web.deployer\META-INF\webserver-xmbean.xml :
modify the class xml tag of the mbean tag so that it become 
<class>org.mobicents.servlet.sip.startup.jboss.JBossSip</class>

You're all set !
    	
go to your jboss_home/bin directory
run the following command
run.bat

Run a mvn clean install at the root of simple-sip-servlet project
and drop in the jboss_home\server\default\deploy directory the war file generated in target dir of simple-sip-servlet, you should see the deployment occuring.

To play with real examples check out the different examples located under trunk/sip-servlets-examples on the svn repo.

otherwise this file has not yet been updated with the latest steps or I screwed up somewhere.  
In any case provide some feedback as explained at the bottom of the home page at https://sip-servlets.dev.java.net/
