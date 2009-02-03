To Build:

You should first run "mvn clean install" on MOBICENTS_HOME/servers/diameter/core/ in order to install the Diameter Stack and Multiplexer into JBoss.
Then, you should do "mvn clean install" at this example location.

--

To Deploy:

Drop the generated war file (diameter-openims-<version>.war) in JBoss deploy directory.
copy the diameteropenims-dar.properties to $JBOSS_HOME/server/default/conf/dars and modify your server.xml to point to this dar file.

--

To Configure:

A configuration file can be found for this example at src/main/sipapp/META-INF/diameter-openims.properties for the source, or at the root inside diameter-openims-<version>.war. Parameters are:

origin.ip - The IP address of the MSS server. Used for creating ORIGIN_HOST AVP;
origin.port - The Diameter listening port of the MSS server Diameter Mux. Used for creating ORIGIN_HOST AVP;
origin.realm - The Diameter Realm of the MSS server;

origin.host - If defined, will override generation of ORIGIN_HOST AVP for Diameter Messages (recommended for this example!);

destination.ip - The IP address of the OpenIMS HSS. Used for creating DESTINATION_HOST AVP;
destination.port - The listening port of the OpenIMS HSS. Used for creating DESTINATION_HOST AVP;
destination.realm - The Diameter Realm of the OpenIMS HSS;

destination.host - If defined, will override generation of DESTINATION_HOST AVP for Diameter Messages;

users - The users to subscribe Profile notifications (separate by comma)

--

To Use:

This example must be run under JBoss.
You will also need OpenIMS Core (http://www.openimscore.org) installed and setup to use MSS as Your Application Server (don't forget to setup Sh Interface permissions and qualify Trigger Point it with Session-Case: Origin-Session).

After OpenIMS being correctly configured and running, start MSS with this example deployed.

Start one SIP Phone with some account (doesn't have to be registered, but it's OK if it is).

Make a call to one of the users configured for this service (alice@open-ims.test or bob@open-ims.test by default), that isn't registered. CSCF will answer with a 404 NOT FOUND and the example will use that info for collecting a missed call.

Now, when the called user registers itself (you can do it with the same SIP phone or a different one), it will receive a message stating that "User XXX tried to call at dd/mm/yyyy at hh:mm:ss".
