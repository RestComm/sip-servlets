Building and Deployment
-----------------------

To build run:
mvn clean compile war:war

And deploy the generated war file to Tomcat.

Usage
-----

You can now run Tomcat and navigate to http://localhost:8080/Click2CallAsync/.
If you have no registered SIp clients you will be asked to register at least two.
Configure your SIP clients to use the sip servlets server as a register and proxy.
By default it will accept any password. After the registration you will see
a table where each cell will initiate a call between the corresponding clients.

 
If you want to REGISTER use this as a DAR file:
REGISTER:("org.mobicents.servlet.sip.example.SimpleAsyncApplication", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
OPTIONS:("org.mobicents.servlet.sip.example.SimpleAsyncApplication", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
