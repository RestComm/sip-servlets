Building and Deployment
-----------------------

(you can skip this step if you already have click2call deployed)

To build run:
mvn clean compile war:war

To deploy:
drop the generated war file in webapps Tomcat directory or JBoss deploy directory.

Alternatively you can add a context entry in the tomcat's server.xml like this 
(this won't work on JBoss and this is not the recommended way to go) :
	<Context 
	className="org.mobicents.servlet.sip.startup.SipStandardContext" 
	configClass="org.mobicents.servlet.sip.startup.SipContextConfig" 
	docBase="E:\sip-serv\sip-servlets-examples\click-to-call\target\click-to-call-servlet-1.0" 
	name="sip-test-context" 
	path="/click2call">
		<Manager className="org.mobicents.servlet.sip.core.session.SipStandardManager"/>						
	</Context> 
Note that path="/click2call" is important. 


Usage
-----

You can now run Tomcat or JBoss and navigate to http://localhost:8080/click2call/.
If you have no registered SIp clients you will be asked to register at least two.
Configure your SIP clients to use the sip servlets server as a register and proxy.
By default it will accept any password, see below for instrcutions how to enable
security and authentication. After the registration you will see
a table where each cell will initiate a call between the corresponding clients.

You can also navigate to http://localhost:8080/click2call/simplecall.html, which is
a simplified version that doesn't require registered clients.
You will see the index page where you can enter two SIP URIs. Enter the URIs
of two SIP phones and click "Submit". The SIP phones don't have to be registered.
I recommend using 3CX Phone and SJ Phone as the two phones in this demo
(under Windows), WengoPhone works too. After you pick up both phones the RTP session starts.
 
If you want to REGISTER use this as a DAR file:
REGISTER: ("org.mobicents.servlet.sip.example.SimpleSipServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")
OPTIONS: ("org.mobicents.servlet.sip.example.SimpleSipServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")


To enable security in JBoss AS
==============================

To enable security for JBoss Application Server, basically you have to create a security 
domain suitable for digest authentication and then configure your Sip Servlets application
to use this security domain. Here are the steps to make a simple configuration with static
users, passwords and roles:

1. In server/default/conf/login-config.xml add a new security policy with hashed passwords, like this:
    <application-policy name = "sip-servlets">
       <authentication>
          <login-module code="org.jboss.security.auth.spi.UsersRolesLoginModule"
             flag = "required">
           <module-option name="usersProperties">props/sip-servlets-users.properties</module-option>
           <module-option name="rolesProperties">props/sip-servlets-roles.properties</module-option>
	   <module-option name="hashAlgorithm">MD5</module-option>
           <module-option name="hashEncoding">rfc2617</module-option>
           <module-option name="hashUserPassword">false</module-option>
           <module-option name="hashStorePassword">true</module-option>
           <module-option name="passwordIsA1Hash">true</module-option>
           <module-option name="storeDigestCallback">
                org.jboss.security.auth.spi.RFC2617Digest
           </module-option>
          </login-module>
       </authentication>
    </application-policy>

2. In the file props/sip-servlets-users.properties put this:
# A sample users.properties file, this line creates user "admin" with password "admin" for "sip-servlets-realm"
admin=9640e13fedc736bdafa6b9007d965c56

The cryptic string here is the A1 value of the digest authentication algorithm and can be
generated with the following command:
$ java -cp ../server/default/lib/jbosssx.jar \
org.jboss.security.auth.spi.RFC2617Digest username "My Realm" password
RFC2617 A1 hash: 9b47ec6f03603dd49863e7d58c4c49ea

3. In the file props/sip-servlets-roles.properties put this:
# A sample roles.properties file for use with some roles
admin=caller,role1,role2,..

Each line in this file assigns roles to the users defined in sip-servlets-users.properties

4. In jboss-web.xml in your application put this:
<jboss-web>
   <!-- Uncomment the security-domain to enable security. You will
      need to edit the htmladaptor login configuration to setup the
      login modules used to authentication users. -->
      <security-domain>java:/jaas/sip-servlets</security-domain>
  
</jboss-web>

5. In sip.xml in your application add some security constraint and authentication type, for example:
	<security-constraint>
	   <display-name>REGISTER Method Security Constraint</display-name>
	      <resource-collection>
	          <resource-name>SimpleSipServlet</resource-name>
	          <description>Require authenticated REGSITER requests</description>
	          <servlet-name>SimpleSipServlet</servlet-name>
	          <sip-method>REGISTER</sip-method>   
	     </resource-collection>   
	     <auth-constraint>      
	           <role-name>caller</role-name>  
	     </auth-constraint>
	 </security-constraint>
	 
	 <login-config> 
         	<auth-method>DIGEST</auth-method> 
         	<realm-name>sip-servlets-realm</realm-name> 
   	 </login-config> 



To enable security in Tomcat
============================

You have to specify some security realm for the servlet container or it's parents, and add some 
contraints to the application. There are the steps:
1. Open <tomcat_home>/conf/server.xml and uncomment the following line:
<Realm className="org.apache.catalina.realm.MemoryRealm" />

2. Open <tomcat_root>/conf/tomcat-users.xml and add the following <user> sub-element:
<user name="user" password="password" roles="caller" />

3. In sip.xml in your application add some security constraint and authentication type, for example:
	<security-constraint>
	   <display-name>REGISTER Method Security Constraint</display-name>
	      <resource-collection>
	          <resource-name>SimpleSipServlet</resource-name>
	          <description>Require authenticated REGSITER requests</description>
	          <servlet-name>SimpleSipServlet</servlet-name>
	          <sip-method>REGISTER</sip-method>   
	     </resource-collection>   
	     <auth-constraint>      
	           <role-name>caller</role-name>  
	     </auth-constraint>
	 </security-constraint>
	 
	 <login-config> 
         	<auth-method>DIGEST</auth-method> 
         	<realm-name>sip-servlets-realm</realm-name> 
   	 </login-config> 