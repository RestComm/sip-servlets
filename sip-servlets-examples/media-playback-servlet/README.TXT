Instructions to build and deploy
--------------------------------

1. Install media server in JBoss AS. Follow Oleg's instructions from the google repository.
2. Copy lib/mscontrol.jar to JBOSS_HOME/server/default/lib (cant put it in the WEB-INF/lib because of some CL issue)
3. mvn clean install war:inplace (or whatever)
4. Deploy it as a sar2 or war in JBoss
5. Use the ./media-playback-dar.properties as a DAR file

To use
------

Just dial the servlet directly, without any proxy. Tested with Ekiga - there is some problem routing some ACK, but it works.

