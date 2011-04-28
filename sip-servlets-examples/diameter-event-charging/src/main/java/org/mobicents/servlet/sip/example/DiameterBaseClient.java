/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.mobicents.servlet.sip.example;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

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

public class DiameterBaseClient implements DiameterProvider, DiameterListener
{

  private static final long serialVersionUID = 1L;
 
  private static Logger logger = Logger.getLogger(DiameterBaseClient.class);

  private DiameterStackMultiplexer muxMBean = null;
  private DiameterMessageFactory msgFactory = null;
  private DiameterProvider provider = null;
  
  public DiameterBaseClient() throws InstanceNotFoundException, MBeanException, ReflectionException,  NullPointerException, MalformedObjectNameException
  {
    ObjectName objectName = new ObjectName("diameter.mobicents:service=DiameterStackMultiplexer");
    
    DiameterListener listener = this;

    ApplicationId[] appIds = new ApplicationId[]{ApplicationId.createByAccAppId( 193, 19302L )};
    
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
    
  }
  
  public String sendMessage( Message message )
  {
    return this.provider.sendMessage( message );
  }

  public Answer processRequest( Request request )
  {
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

  public Message createAccountingRequest(String sessionId, String uid, Long value, boolean refund)
  {
    //<command name="ACR">
    Request req = (Request) this.msgFactory.createRequest( 271, 19302L );
    
    req.setProxiable( true );
    
    AvpSet avps = req.getAvps();

    //<avp name="Origin-Host" code="264" vendor="0" value="localhost" />
    avps.addAvp( Avp.ORIGIN_HOST, "aaa://127.0.0.1:1812".getBytes(), true, false );
    //<avp name="Origin-Realm" code="296" vendor="0" value="mobicents.org" />
    avps.addAvp( Avp.ORIGIN_REALM, "mobicents.org".getBytes(), true, false );
    
    //<avp name="Session-Id" code="263" vendor="0" value="12345" />
    avps.addAvp( Avp.SESSION_ID, sessionId.getBytes(), true, false );
    
    //<avp name="Vendor-Specific-Application-Id" code="260" vendor="0">
    //  <avp name="Vendor-Id" code="266" vendor="0" value="193" />
    //  <avp name="Acct-Application-Id" code="259" vendor="0" value="19302" />
    //</avp>
    AvpSet vsai = avps.addGroupedAvp( Avp.VENDOR_SPECIFIC_APPLICATION_ID, true, false );
    vsai.addAvp( 266, 193, true, false, true );
    vsai.addAvp( 259, 19302, true, false, true );
    
    //<avp name="Destination-Realm" code="283" vendor="0" value="mobicents.org" />
    avps.addAvp( Avp.DESTINATION_REALM, "mobicents.org".getBytes(), true, false );
    
    //<avp name="Destination-Host" code="293" vendor="0" value="localhost" />
    avps.addAvp( Avp.DESTINATION_HOST, "aaa://127.0.0.1:21812".getBytes(), true, false );
    
    //<avp name="SCAP-Subscription-Id" code="553" vendor="193">
    //  <avp name="SCAP-Subscription-Id-Type" code="555" vendor="193" value="0" />
    //  <avp name="SCAP-Subscription-Id-Data" code="554" vendor="193" value="00001000" />
    //</avp>
    AvpSet ssi = avps.addGroupedAvp( 553, 193, true, false );
    ssi.addAvp( 555, 0, 193, true, false );
    ssi.addAvp( 554, uid.getBytes(), 193, true, false );

    //<avp name="SCAP-Requested-Service-Unit" code="606" vendor="193">
    //  <avp name="SCAP-Unit-Type" code="611" vendor="193" value="2" />
    //  <avp name="SCAP-Unit-Value" code="612" vendor="193">
    //    <avp name="SCAP-Value-Digits" code="617" vendor="193" value="10" />
    //  </avp>
    //  <avp name="SCAP-Currency-Code" code="544" vendor="193" value="978" />
    //</avp>
    AvpSet srsu = avps.addGroupedAvp( 606, 193, true, false );
    srsu.addAvp( 611, 3, 193, true, false );
    AvpSet suv = srsu.addGroupedAvp( 612, 193, true, false );
    suv.addAvp( 617, value, 193, true, false );
    srsu.addAvp( 544, 978, 193, true, false );

    //<avp name="Accounting-Record-Number" code="485" vendor="0" value="0" />
    avps.addAvp( Avp.ACC_RECORD_NUMBER, 0, true, false );
    
    //<avp name="Accounting-Record-Type" code="480" vendor="0" value="1" />
    avps.addAvp( Avp.ACC_RECORD_TYPE, 1, true, false );
    
    //<avp name="SCAP-Requested-Action" code="615" vendor="193" value="0" />
    avps.addAvp( 615, refund ? 1 : 0, 193, true, false );
    
    //<avp name="SCAP-Service-Parameter-Info" code="607" vendor="193">
    //  <avp name="SCAP-Service-Parameter-Type" code="608" vendor="193" value="0" />
    //  <avp name="SCAP-Service-Parameter-Value" code="609" vendor="193" value="510" />
    //</avp>
    AvpSet sspi1 = avps.addGroupedAvp( 607, 193, true, false );
    sspi1.addAvp( 608, 0, 193, true, false );
    sspi1.addAvp( 609, "510".getBytes(), 193, true, false );

    //<avp name="SCAP-Service-Parameter-Info" code="607" vendor="193">
    //  <avp name="SCAP-Service-Parameter-Type" code="608" vendor="193" value="14" />
    //  <avp name="SCAP-Service-Parameter-Value" code="609" vendor="193" value="20" />
    //</avp>
    AvpSet sspi2 = avps.addGroupedAvp( 607, 193, true, false );
    sspi2.addAvp( 608, 14, 193, true, false );
    sspi2.addAvp( 609, "20".getBytes(), 193, true, false );
    
    return req;
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
  
}
