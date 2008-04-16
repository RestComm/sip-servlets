Instructions to build and deploy
--------------------------------

0. Install media server in JBoss AS. Follow Oleg's instructions from the google repository.
1. Edit pom.xml to point to mscontrol.jar from media server control (sorry for this, couldnt upload)
2. Add mscontrol.jar to JBOSS_HOME/server/default/lib (cant put it in the WEB-INF/lib because of some CL issue)
3. mvn clean install war:inplace (or whatever)
4. Deploy it as a sar2 in JBoss
5. Use something like this in the DAR file:
INVITE: ("org.mobicents.servlet.sip.example.MediaPlaybackServlet", "DAR:From", "ORIGINATING", "", "NO_ROUTE", "0")

To use
------

Just dial the servlet directly, without any proxy. Tested with Ekiga - there is some problem routing some ACK, but it works.

