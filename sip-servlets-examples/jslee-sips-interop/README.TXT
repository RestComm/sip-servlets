IMPORTANT NOTE : make JBoss a converged sip servlets AS before following those steps. 
Checkout the instructions at https://sip-servlets.dev.java.net/installation-jboss.html

Instructions to build and deploy
--------------------------------

To build:
0. Install media server in JBoss AS. Follow Oleg's instructions from the google repository.
1. If the media server's jboss-service.xml doesnt have an IVR endpoint add one like this:
   <mbean code="org.mobicents.media.server.impl.ivr.IVREndpointManagement" 
           name="media.mobicents:endpoint=ivr"> 
        <attribute name="JndiName">media/trunk/IVR/1</attribute>
        <attribute name="BindAddress">${jboss.bind.address}</attribute>
        <attribute name="Jitter">60</attribute>
        <attribute name="PacketizationPeriod">20</attribute>
        <attribute name="PortRange">1024-65535</attribute>
        <attribute name="PCMA">true</attribute>
    </mbean>
2. Install Mobicents, Mobicents TTS RA and Mobicents Media RA
    
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
