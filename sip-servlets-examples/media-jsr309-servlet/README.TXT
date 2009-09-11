Instructions to build and deploy
--------------------------------


1. Install media server as standalone or in JBoss AS. Follow the instruction here TODO
2. set JBOSS_HOME environment variable pointing to your JBoss AS Directory where Sip-Servlets Server is installed
3. Call 'mvn clean install' from parent directory 'media-jsr309-servlet'

To Deploy : 
Drop the generated war file in JBoss deploy directory.
Copy the media-jsr309-dar.properties to your jboss_home/server/default/conf/dars. To use this dar file for this service, specify in the Service xml tag, darConfigurationFileLocation attribute of the jboss_home/server/default/deploy/jbossweb.sar/server.xml , the following : conf/dars/media-jsr309-dar.properties.

You can now run your JBoss container. If MMS is standalone, start it. If its embedded in JBoss AS, it starts with JBoss container.


To use
------

Just dial the servlet directly as explained in below steps, without any proxy. Tested with Twinkle
1. Dial 1010 to test Announcement and DTMF Detection
2. Dial 1011 to test Recording

What is included in the demo
----------------------------
Media playback, DTMF detection and Recording.
