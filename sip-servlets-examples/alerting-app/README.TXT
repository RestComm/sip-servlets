Instructions to build and deploy
--------------------------------

To build:
1. Install Mobicents ALL 1.2.1.GA and makes JBOSS_HOME point to it like
	export MOBICENTS_ALL_HOME=/home/deruelle/test/mobi-ALL-1.2.1.GA 
	export JBOSS_HOME=$MOBICENTS_ALL_HOME/jboss-4.2.3.GA
    
2. mvn clean install from alerting-app dir

the war file located in alerting-app/sip-servlets/target is copied automatically into the Jboss default deploy directory.
Same for JSLEE part of the example : service DU and events DU.

3. Use the content of alerting-app-dar.properties located in the same dir as this README file and copy them over into JBOSS_HOME/server/default/conf/dars/mobicents-dar.properties

4. install the SMPP RA :
	cd $MOBICENTS_ALL_HOME
	cd resources
	cd smpp-ra
	ant

5. Download SMPPSim from http://www.seleniumsoftware.com/downloads.html and install it locally
	Modify the SMPP_SIM/conf/smppsim.props
	In this file, Add 1 to SYSTEM_IDS Property like this SYSTEM_IDS=smppclient1,smppclient2,1
	Same thing for PASSWORDS Property PASSWORDS=password,password,1
	This will allow the SMPP Resource Adaptor to connect to the simulator

6. Run the SMPP Simulator
	sudo sh ./startsmppsim.sh

7. Startup JBoss within Mobicents 1.2.1 : 
	cd $JBOSS_HOME/bin
	sh run.sh

To use
------

Go to http://localhost:8080/alerting-app/send-sms-alert-test.html, fill in the input fields and click submit
An SMS should go to the simulator

Start a SIP Phone.
Go to http://localhost:8080/alerting-app/send-alert-test.html, fill in the input fields (the tel should be the sip address of your SIP Phone like sip:username@127.0.0.1:5060) and click submit
A Call should go to your sip phone notifying you of the alert