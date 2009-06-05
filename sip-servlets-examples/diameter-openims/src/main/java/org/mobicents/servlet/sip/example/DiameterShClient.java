package org.mobicents.servlet.sip.example;

import java.io.StringReader;
import java.util.Collection;
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.jdiameter.api.Answer;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpDataException;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.Message;
import org.jdiameter.api.Request;
import org.mobicents.diameter.api.DiameterMessageFactory;
import org.mobicents.diameter.api.DiameterProvider;
import org.mobicents.diameter.dictionary.AvpDictionary;
import org.mobicents.diameter.stack.DiameterListener;
import org.mobicents.diameter.stack.DiameterStackMultiplexer;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * 
 * This class represents a Sh interface client.
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a> 
 */
public class DiameterShClient implements DiameterProvider, DiameterListener
{

  private static final long serialVersionUID = 1L;

  private static Logger logger = Logger.getLogger(DiameterShClient.class);

  private DiameterStackMultiplexer muxMBean = null;
  private DiameterMessageFactory msgFactory = null;
  private DiameterProvider provider = null;

  private String originIP = "127.0.0.1";
  private String originPort = "1812";
  private String originRealm = "mobicents.org";

  private String originHost = null;

  private String destinationIP = "127.0.0.1";
  private String destinationPort = "3868";
  private String destinationRealm = "mobicents.org";

  private String destinationHost = null;

  private final long SH_VENDOR_ID = 10415;
  private final long SH_APPLICATION_ID = 16777217;

  private String PROPERTIES_FILE = "diameter-openims.properties";

  public DiameterShClient() throws InstanceNotFoundException, MBeanException, ReflectionException,  NullPointerException, MalformedObjectNameException
  {
    ObjectName objectName = new ObjectName("diameter.mobicents:service=DiameterStackMultiplexer");

    DiameterListener listener = this;

    // Create the Application-Id for Sh
    ApplicationId[] appIds = new ApplicationId[]{ApplicationId.createByAuthAppId( SH_VENDOR_ID, SH_APPLICATION_ID )};

    Object[] params = new Object[]{};

    String[] signature = new String[]{};

    String operation = "getMultiplexerMBean";

    MBeanServer server = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);

    Object object = server.invoke( objectName, operation, params, signature );

    if(object instanceof DiameterStackMultiplexer)
      muxMBean = (DiameterStackMultiplexer) object;

    logger.info( "muxMBean == " + muxMBean );

    muxMBean.registerListener( listener, appIds );

    msgFactory =  muxMBean.getMessageFactory();

    provider = muxMBean.getProvider();
    
    initializeExample();
  }

  private void initializeExample()
  {
    try
    {
      Properties props = new Properties();
      props.load( this.getClass().getClassLoader().getResourceAsStream("../../META-INF/" + PROPERTIES_FILE) );
      
      this.originIP = props.getProperty( "origin.ip" ) == null ? this.originIP : props.getProperty( "origin.ip" ); 
      this.originPort = props.getProperty( "origin.port" ) == null ? this.originPort : props.getProperty( "origin.port" ); 
      this.originRealm = props.getProperty( "origin.realm" ) == null ? this.originRealm : props.getProperty( "origin.realm" ); 

      this.originHost = props.getProperty( "origin.host" );
      
      this.destinationIP = props.getProperty( "destination.ip" ) == null ? this.destinationIP : props.getProperty( "destination.ip" );
      this.destinationPort = props.getProperty( "destination.port" ) == null ? this.destinationPort : props.getProperty( "destination.port" );
      this.destinationRealm = props.getProperty( "destination.realm" ) == null ? this.destinationRealm : props.getProperty( "destination.realm" ); 
      
      this.destinationHost = props.getProperty( "destination.host" );
      
      String usersStr = props.getProperty( "users" );
      
      if(usersStr != null && usersStr.length() > 0)
      {
        String[] users = usersStr.split( "," );
        
        // We must wait a while until Diameter Connection is properly established
        Thread.sleep( 15000 );
        
        logger.info( "Subscribing to Profile Updates from Users " + users.toString() );
        
        for(String user : users)
        {
          // Create the SNR for the desired user
          Request snr = createSubscribeNotificationsRequest(user.trim());
          
          // And send it!
          Answer ans = (Answer)this.sendMessageSync( snr );
          
          // Check if we succeeded or if we failed
          if( ans != null && ans.getResultCode() != null && ans.getResultCode().getUnsigned32() == 2001 )
          {
            logger.info( "Successfully subscribed to notifications for user '" + user + "'." );
          }
          else
          {
            logger.warn( "Failed subscription to notifications for user '" + user + "'." );
          }
        }
      }
      else
      {
        logger.warn( "No Users are defined for the example. Nothing will happen..." );
      }
    }
    catch (Exception e) {
      logger.error( "Failure reading properties file.", e );
    }
  }

  public String sendMessage( Message message )
  {
    return this.provider.sendMessage( message );
  }

  public Answer processRequest( Request request )
  {
    try
    {
      if(request.getCommandCode() == DiameterShCodes.PUSH_NOTIFICATION_REQUEST)
      {
        logger.info( "Push-Notification-Request received.\r\n" + request );

        AvpSet avps = request.getAvps();

        String userPublicIdentity = avps.getAvp( DiameterShCodes.USER_IDENTITY_AVP ).getGrouped().getAvp( DiameterShCodes.PUBLIC_IDENTITY_AVP ).getUTF8String();

        String userData = avps.getAvp( DiameterShCodes.USER_DATA_AVP ).getOctetString();

        Collection<MissedCall> mCs = DiameterOpenIMSSipServlet.missedCalls.get( userPublicIdentity );

        if(mCs != null && mCs.size() > 0)
        {
          DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
          DocumentBuilder builder = factory.newDocumentBuilder();
          Document doc = (Document) builder.parse(new InputSource(new StringReader(userData)));

          String userState = doc.getElementsByTagName("IMSUserState").item(0).getTextContent();

          if(userState.equals("1"))
          {
            synchronized ( mCs )
            {
              for(MissedCall missedCall : mCs )
              {
                new DiameterOpenIMSSipServlet();
                // Send SIP Message
                DiameterOpenIMSSipServlet.sendSIPMessage( userPublicIdentity, missedCall.getNotification() );
              }
            }

            // Clear the missed calls for this user
            mCs.clear();
          }
        }

        return request.createAnswer( 2001 );
      }
      else
      {
        logger.info( "Not Processing unexpected request (Code[" + request.getCommandCode() + "])");
      }

    }
    catch (Exception e) {
      logger.error( "", e );
    }

    // We don't deal with this...
    return null;

  }

  public void receivedSuccessMessage( Request request, Answer answer )
  {
    logger.info( "Received success message (Result-Code[" + answer.getResultCode() + "]) for Diameter Request with Session-Id [" + request.getSessionId() + "]" );
  }

  public void timeoutExpired( Request request )
  {
    logger.info( "Timeout expired for Diameter Request with Session-Id [" + request.getSessionId() + "]" );
  }

  public Message sendMessageSync( Message message )
  {
    logger.info( printMessage( message ) );

    return this.provider.sendMessageSync( message );
  }

  private String printMessage( Message message )
  {
    String toString = "\r\n" +
    "+----------------------------------- HEADER ----------------------------------+\r\n" +
    "| Version................." + message.getVersion() + "\r\n" +
    "| Command-Flags..........." + "R[" + message.isRequest() + "] P[" + message.isProxiable() + "] " +
    "E[" + message.isError() + "] T[" + message.isReTransmitted() + "]" + "\r\n" +
    "| Command-Code............" + message.getCommandCode() + "\r\n" +
    "| Application-Id.........." + message.getApplicationId() + "\r\n" +
    "| Hop-By-Hop Identifier..." + message.getHopByHopIdentifier() + "\r\n" +
    "| End-To-End Identifier..." + message.getEndToEndIdentifier() + "\r\n" +
    "+------------------------------------ AVPs -----------------------------------+\r\n";

    for( Avp avp : message.getAvps() )
    {
      toString += printAvp( avp, "" );
    }

    toString += "+-----------------------------------------------------------------------------+\r\n";

    return toString;    
  }

  private String printAvp(Avp avp, String indent)
  {
    Object avpValue = null;
    String avpString = "";
    boolean isGrouped = false;

    try
    {
      String avpType = AvpDictionary.INSTANCE.getAvp( avp.getCode(), avp.getVendorId() ).getType();

      if("Integer32".equals(avpType) || "AppId".equals(avpType))
      {
        avpValue = avp.getInteger32();
      }
      else if("Unsigned32".equals(avpType) || "VendorId".equals(avpType))
      {
        avpValue = avp.getUnsigned32();
      }
      else if("Float64".equals(avpType))
      {
        avpValue = avp.getFloat64();
      }
      else if("Integer64".equals(avpType))
      {
        avpValue = avp.getInteger64();
      }
      else if("Time".equals(avpType))
      {
        avpValue = avp.getTime();
      }
      else if("Unsigned64".equals(avpType))
      {
        avpValue = avp.getUnsigned64();
      }
      else if("Grouped".equals(avpType))
      {
        avpValue = "<Grouped>";
        isGrouped = true;
      }
      else
      {
        avpValue = avp.getOctetString().replaceAll( "\r", "" ).replaceAll( "\n", "" );
      }
    }
    catch (Exception ignore) {
      try
      {
        avpValue = avp.getOctetString().replaceAll( "\r", "" ).replaceAll( "\n", "" );
      }
      catch ( AvpDataException e ) {
        avpValue = avp.toString();
      }
    }

    avpString += "| " + indent + "AVP: Code[" + avp.getCode() + "] VendorID[" + avp.getVendorId() + "] Value[" + 
    avpValue + "] Flags[M=" + avp.isMandatory() + ";E=" + avp.isEncrypted() + ";V=" + avp.isVendorId() + "]\r\n";

    if(isGrouped)
    {
      try
      {
        for(Avp subAvp : avp.getGrouped())
        {
          avpString += printAvp( subAvp, indent + "  " );          
        }
      }
      catch ( AvpDataException e )
      {
        // Failed to ungroup... ignore then...
      }
    }

    return avpString;
  }

  public Request createSubscribeNotificationsRequest(String user)
  {
    try 
    {
      // Create the Subscribe-Notifications-Request
      // < Subscribe-Notifications-Request > :: =   < Diameter Header: 308, REQ, PXY, 16777217 >
      Request req = (Request) this.msgFactory.createRequest( DiameterShCodes.SUBSCRIBE_NOTIFICATIONS_REQUEST, SH_APPLICATION_ID );

      // Make it proxiable (just in case... we never know, what will happen)
      req.setProxiable( true );

      // Obtain the AVPs (should be an empty set)
      AvpSet avps = req.getAvps();

      // < Session-Id >
      avps.addAvp( Avp.SESSION_ID, ("123sip-servlets456;" + System.currentTimeMillis()).getBytes()  );

      // { Vendor-Specific-Application-Id }
      AvpSet vsaiAvp = avps.addGroupedAvp( Avp.VENDOR_SPECIFIC_APPLICATION_ID, true, false );
      vsaiAvp.addAvp( Avp.VENDOR_ID, SH_VENDOR_ID, true, false, true );
      vsaiAvp.addAvp( Avp.AUTH_APPLICATION_ID, SH_APPLICATION_ID, true, false, true );

      // { Auth-Session-State }
      // 0 == Idle,1 == Pending, 2 == Open, 3 == Disconnected
      avps.addAvp( Avp.AUTH_SESSION_STATE, 2, true, false );

      // { Origin-Host }
      avps.addAvp( Avp.ORIGIN_HOST, (this.originHost != null ? this.originHost : "aaa://" + this.originIP + ":" + this.originPort).getBytes(), true, false );
      // { Origin-Realm }
      avps.addAvp( Avp.ORIGIN_REALM, this.originRealm.getBytes(), true, false );
      // [ Destination-Host ]
      avps.addAvp( Avp.DESTINATION_HOST, (this.destinationHost != null ? this.destinationHost : "aaa://" + this.destinationIP + ":" + this.destinationPort).getBytes(), true, false );
      // { Destination-Realm }
      avps.addAvp( Avp.DESTINATION_REALM, this.destinationRealm.getBytes(), true, false );
      //        *[ Supported-Features ]

      //        { User-Identity }
      AvpSet ui = avps.addGroupedAvp( DiameterShCodes.USER_IDENTITY_AVP, SH_VENDOR_ID, true, false );

      ui.addAvp( DiameterShCodes.PUBLIC_IDENTITY_AVP, "sip:" + user.replaceFirst( "sip:", "" ), SH_VENDOR_ID, true, true, false );

      //        [ Wildcarded-PSI ]
      //        [ Wildcarded-IMPU ]
      //        *[ Service-Indication ]
      //        [ Send-Data-Indication ]
      //        [ Server-Name ]

      // { Subs-Req-Type }
      // 0 == Subscribe // 1 == Unsubrscribe
      avps.addAvp( DiameterShCodes.SUBS_REQ_TYPE_AVP, 0, SH_VENDOR_ID, true, false );

      // *{ Data-Reference }
      // It's enumerated: 0 == Whole data ... 11 == User-State
      avps.addAvp( DiameterShCodes.DATA_REFERENCE_AVP, 11, SH_VENDOR_ID, true, false );

      //        [ Identity-Set ]
      //        [ Expiry-Time ]

      logger.info( "Created Subscribe-Notifications-Request:\r\n" + printMessage( req ) );

      return req;

    } catch (Exception e) {
      logger.error( "Failure trying to create/send Subscribe-Notifications-Request.", e );

      return null;
    }
  }




}
