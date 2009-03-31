Setting up JBoss Tools or JBoss Devleoper Studio
------------------------------------------------
This is a JBoss Tools Seam Eclipse project without the jar dependencies. If you want to add the dependencies in order to make the project buildable and deployable from JBoss Tools run:
ant dependencies

After that the jars will be copied to WebContent/WEB-INF/lib and the project can be properly loaded into JBoss Tools. You still need to modify the classpath if some jar is missing. You will also be asked to specify a new Seam runtime folder. If you have problems with JBoss AS dependencies, you should go in the project properties (right click on the project and do "Properties"), then select "Project Facets" and click on the "Runtimes" tab - remove the existing runtime (in any) and add your own (it has to be Mobicents Sip Servlets with JBoss). 

It is highlt recommented to use JBDS 2.1 or JBoss Tools 3 or later with official Seam 2.1 support.

If you don't want to use JBoss Tools
------------------------------------
Just set JBOSS_HOME and execute:
ant all