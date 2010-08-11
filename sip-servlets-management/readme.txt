Mobicents Sip Servlets Management Console
-----------------------------------------

This web console allows to graphically edit the DAR configuration file used to route requests 
withing the sip servlets container. It uses GWT + GWT-EXT + GWT-DND and works on Firefox and IE.

Building
--------
1. Make sure you have all sip servlets maven artifacts in your local maven repository.
2. Edit build.properties and set the M2_REPO property to point to your local maven repository root.
3. Get GWT 1.4 from http://code.google.com/webtoolkit/ and set GWT_HOME=the_dir_where_you_install_gwt.
4. Get JBoss AS 4.2, install sip servlets there and set JBOSS_HOME=your_jboss_installation_dir.
5. Run ant in this folder - it will deploy the console to JBOSS_HOME.

6. Optional - you can use the Cypal Eclipse plugin for GWT apps to ease GUI testing.

Using
-----
Just add apps, drag and drop apps or groups of apps. You can view the DAR configuration text 
with "View source" and you can save the configuration with "Save". The configuration will be 
persisted to the default location of your dar file if possible (if the file is accessible for
writing).

Security
--------
To secure the console you must read WEB-INF/web.xml and Uncomment the security constraints declaration.
After that you can use the same username and password you use with JMX console.