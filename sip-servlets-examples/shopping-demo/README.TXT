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
2. Modify the context param audio.files.path yo map to your file system in 
sip-servlets/sip-servlets-examples/shopping-demo/web/src/main/webapp/WEB-INF/sip.xml file.
and the order.approval.waitingtime (the time between the moment the customer confirm 
its order and the next phone cal made to ask for delivery date default to 6 sec) if needed
    
mvn clean install from shopping-demo dir

To deploy:

1. use the shopping-demo-dar.properties located in the same dir as this README file 
2. drop the ear file located in shopping-demo/ear/target into the Jboss deploy directory.

To use
------

1. Start Jboss
2. Go to http://localhost:8080/shopping-demo and follow instructions on how to play with the example at 
http://groups.google.com/group/mobicents-public/web/converged-application-demo

Note : Use DTMF INFO as a way for sending DTMF for now, 
couldn't get media server to work with rfc2833 or inband currently
