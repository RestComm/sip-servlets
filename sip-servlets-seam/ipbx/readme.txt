Setting up JBoss Tools or JBoss Devleoper Studio
------------------------------------------------
This is a JBoss Tools Seam Eclipse project without the jar dependencies. If you want to add the dependencies in order to make the project buildable and deployable from JBoss Tools run:
ant dependencies

After that the jars will be copied to WebContent/WEB-INF/lib and the project can be properly loaded into JBoss Tools. You still need to modify .classpath to match your settings for Applications Server and Java runtime names. You will also be asked to specify a new Seam runtime folder and that should be all.


If you don't want to use JBoss Tools
------------------------------------
Just set JBOSS_HOME and execute:
ant all