Instructions to build and deploy
--------------------------------


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
    
2. Add mscontrol.jar to JBOSS_HOME/server/default/lib (cant put it in the WEB-INF/lib because of some CL issue)
3. mvn clean install war:inplace (or whatever)
4. Deploy it as a sar2 in JBoss

To use
------

Just dial the servlet directly, without any proxy. Tested with Ekiga - there is some problem routing some ACK, but it works.

