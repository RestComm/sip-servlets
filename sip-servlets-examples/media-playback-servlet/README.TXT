Instructions to build and deploy
--------------------------------


1. Install media server in JBoss AS. 
2. mvn clean install war:inplace.

IMPORTANT: Don't deploy the packaged war file. Deploy the exploded directory 
media-playback-servlet/target/media-playback-servlet-1.0 with .sar2 extension.
This is needed because the packaged war file contains jar files in WEB-INF/lib that are
renamed by maven to include version suffix, however this breaks FreeTTS. In the exploded
directory, this is fixed.

To use
------

Just dial the servlet directly, without any proxy. Tested with Ekiga - there is some problem routing some ACK, but it works.

What is included in the demo
----------------------------
Media playback, Text to Speech with FreeTTS and DTMF detection.