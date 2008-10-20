IMPORTANT NOTE : make JBoss a converged sip servlets AS before following those steps. 
Checkout the instructions at https://sip-servlets.dev.java.net/installation-jboss.html

Instructions to build and deploy
--------------------------------

To build:
1. Install media server and Mobicents Media Server MSC Controller (Media RA) in JBoss AS
2. Install Mobicents, Mobicents TTS RA in JBoss AS
    
mvn clean install from jslee-sips dir

the ear file located in jslee-sips/sips/ear/target is copied automatically into the Jboss default deploy directory.
Same for service DU.

To deploy:

Use the jslee-sips-interop-dar.properties located in the same dir as this README file 


To use
------

1. Start Jboss
2. Call your server (you can use a sip address of the foolowing form sip:javaone@127.0.0.1:5080) 

working nicely on linphone and twinkle on linux for SIP info
working nicely with wengo for inband
