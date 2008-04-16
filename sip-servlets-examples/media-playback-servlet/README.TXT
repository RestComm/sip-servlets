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
    
2. mvn clean install war:inplace.
4. IMPORTANT: Don't deploy the packaged war file. Deploy the exploded directory 
media-playback-servlet/target/media-playback-servlet-1.0-SNAPSHOT with .sar2 extension.
This is needed because the packaged war file contains jar files in WEB-INF/lib that are
renamed by maven to include version suffix, however this breaks FreeTTS. In the exploded
directory, this is fixed.

To use
------

Just dial the servlet directly, without any proxy. Tested with Ekiga - there is some problem routing some ACK, but it works.

What is included in the demo
----------------------------
Media playback, Text to Speech with FreeTTS and DTMF detection.