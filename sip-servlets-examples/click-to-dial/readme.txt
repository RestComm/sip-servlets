To build run:
mvn clean compile war:war

To deploy:
Copy the war file in the TOMCAT_HOME/webapps folder

To use:
When you deploy this sample in tomcat you should navigate to http://localhost:8080/<NAME_OF_WAR>/call
For example http://localhost:8080/click-to-call-servlet-1.0-SNAPSHOT/call