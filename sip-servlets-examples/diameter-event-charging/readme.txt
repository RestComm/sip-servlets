To build run:
You should first run "mvn clean install" on MOBICENTS_HOME/servers/diameter/core/ in order to install the Diameter Stack and Multiplexer into JBoss.
Then, you should do "mvn clean install" at this example location.

To deploy:
Drop the generated war file (diameter-event-charging-<version>.war) in JBoss deploy directory.
copy the diametereventcharging-dar.properties to $JBOSS_HOME/server/default/conf/dars and modify your server.xml to point to this dar file.

To use:
This example must be run under JBoss.
You will also need Ericsson SDK Charging Emulator v1.0 (http://www.ericsson.com/mobilityworld/developerszonedown/downloads/tools/charging_solutions/ChargingSDK-1_0_D31E.zip).

Run the Charging Emulator (PPSDiamEmul.jar) and configure it like this:
  Peer Id: aaa://127.0.0.1:21812
  Realm: mobicents.org
  Host IP: 127.0.0.1

Start two SIP Phones.
One phone should be setup as sip:receiver@sip-servlets.com on ip address 127.0.0.1 and port 5090
The other phone can be setup as anything.
The SIP phone doesn't have to be registered.
We recommend using 3CX Phone, SJ Phone or WengoPhone. 

From the second phone, make a call to sip:receiver@sip-servlets.com you should have the other phone ringing.

You should see there is one first request, right after the invite and before the other party accept/reject the call, sent to the Charging Emulator, that's when the debit is made.
In case the call is rejected, or the caller gives up, there's a new Diameter Request sent which will be the refund of the unused value. In case the call is accepted, nothing else will happen, related to Diameter.

You can see the user balance in the emulator: in the menu Config > Account > Click on "00001000-Klas Svensson" and watch the balance. Stretch the window down to see the history.

Please note that this is not the most correct way to do charging, as Diameter provides other means such as unit reservation, but only as purpose of a demo, where you can see debit and refund working.
Also, this is a fixed price call, no matter what the duration. You can also have it using time-based charging.