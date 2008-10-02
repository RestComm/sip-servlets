Building and Deployment
-----------------------

To build run:
mvn clean install

To deploy:
modify the build.xml to change the manage username and password and make sure the CATALINA_HOME variable is set.
call ant to deploy your application, ant undeploy to undeploy it and ant redeploy to redeploy it