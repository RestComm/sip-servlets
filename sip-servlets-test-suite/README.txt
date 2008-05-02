First of all, please modify the properties located in 
	sip-servlets-test-suite\testsuite\src\test\resources\org\mobicents\servlet\sip\testsuite\testsuite.properties


To add an application to the test suite :

	Copy one of the sample app loacted in sip-servlets-test-suite\applications and rename it to yourappname.
	Modify it's pom.xml and add yourappname in the modules section of the parent pom.xml located in 
	sip-servlets-test-suite\applications (This allow to build all the applications before running the test suite)  
	We will now make it available in eclipse...
	Open up eclipse and go to the sip-servlets-test-suite project, click on the right button then properties.
	On the properties popup that just came up go to Java Build Path and then to source tab.
	Add a new folder in selecting sip-servlets-test-suite\applications\yourappname\src\main\java
	Edit the output folder of the new folder you just created to make it go to 
	sip-servlets-test-suite\applications\yourappname\src\main\sipapp
	Modify your application to cover your use case and don't forget to modify the sip.xml.


Add a test case for the newly created application :

	Add a test case for your app in testsuite/src/test/java/org/mobicents/servlet/sip/testsuite/yourpackagename/yourtestcase
	and the corresponding dar configuration file (otherwise your application will never get picked up by the application router)
	in testsuite/src/test/resources/org/mobicents/servlet/sip/testsuite/yourpackagename/
	Make sure the locations in your test case specified in the following methods are correct deployApplication() and
	getDarConfigurationFile().
	Select your test case in eclipse and run it as a junit test. 
	
	
To run the test suite :
	go to sip-servlets-test-suite\applications and run mvn clean install war:inplace
	go to sip-servlets-test-suite\testsuite and run mvn clean install	