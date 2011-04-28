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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.URI;

import org.apache.log4j.Logger;
import org.jdiameter.api.Answer;
import org.jdiameter.api.Message;
import org.jdiameter.api.Request;

/**
 * This example shows how to do Diameter Event Charging.
 * It is based on the location service example. 
 * @author Alexandre Mendonca
 *
 */
public class DiameterEventChargingSipServlet extends SipServlet {

  private static final long serialVersionUID = 1L;
  
  private static Logger logger = Logger.getLogger(DiameterEventChargingSipServlet.class);
  private static final String CONTACT_HEADER = "Contact";
  Map<String, List<URI>> registeredUsers = null;
  
  DiameterBaseClient diameterBaseClient = null;
  
  private static final String CHARGING_MSDN_ID = "00001000";
  private static final long CHARGING_VALUE = 10L; 
  
  /** Creates a new instance of SpeedDialSipServlet */
  public DiameterEventChargingSipServlet() {}

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    logger.info("the locationb service sip servlet has been started");
    super.init(servletConfig);
    SipFactory sipFactory = (SipFactory)getServletContext().getAttribute(SIP_FACTORY);
    registeredUsers = new HashMap<String, List<URI>>();
    List<URI> uriList  = new ArrayList<URI>();
    uriList.add(sipFactory.createURI("sip:receiver@127.0.0.1:5090"));
    //uriList.add(sipFactory.createURI("sip:receiver@127.0.0.1:6090"));
    registeredUsers.put("sip:receiver@sip-servlets.com", uriList);
    List<URI> perfUriList  = new ArrayList<URI>();
    perfUriList.add(sipFactory.createURI("sip:perf-receiver@127.0.0.1:5090"));    
    registeredUsers.put("sip:perf-receiver@sip-servlets.com", perfUriList);
    ArrayList<URI> failOverUriList  = new ArrayList<URI>();
    failOverUriList.add(sipFactory.createURI("sip:receiver-failover@127.0.0.1:5090"));
    registeredUsers.put("sip:receiver-failover@sip-servlets.com", failOverUriList);
    
    // Initialize diameter client
    try {
      diameterBaseClient = new DiameterBaseClient();
    }
    catch ( Exception e ) {
      logger.error( "Failure initializing Diameter Base Client.", e );
    }   
  }

  @Override
  protected void doInvite(SipServletRequest request) throws ServletException,
      IOException {

    logger.info("Got request:\n" + request.toString());   
    
    List<URI> contactAddresses = registeredUsers.get(request.getRequestURI().toString());
    if(contactAddresses != null && contactAddresses.size() > 0) {
      
      // We'll do the charging here (false means it's a debit, not a refund).
      long resultCode = doDiameterCharging( request.getCallId(), CHARGING_MSDN_ID, CHARGING_VALUE, false );
      
      /*
       * Check the response:
       * 
      -  1xxx (Informational)
      -  2xxx (Success)
      -  3xxx (Protocol Errors)
      -  4xxx (Transient Failures)
      -  5xxx (Permanent Failure)
      */
      if(resultCode >= 1000 & resultCode < 2000 )
      {
        logger.info("Diameter Answer == Informational (" + resultCode + ") ... Unexpected! Aborting!");
        SipServletResponse sipServletResponse = request.createResponse( SipServletResponse.SC_SERVICE_UNAVAILABLE );
        sipServletResponse.send();
        return;
      }
      else if(resultCode >= 2000 & resultCode < 3000 )
      {
        logger.info("Diameter Answer == Success ... Proceeding!");
      }
      else if(resultCode >= 3000 & resultCode < 4000 )
      {
        logger.info("Diameter Answer == Protocol Error (" + resultCode + ") ... Ooops!");
        SipServletResponse sipServletResponse = request.createResponse( SipServletResponse.SC_SERVICE_UNAVAILABLE );
        sipServletResponse.send();
        return;
      }
      else if(resultCode >= 4000 & resultCode < 5000 )
      {
        logger.info("Diameter Answer == Transient Failure (" + resultCode + ") ... Aborting! Let the user try again...");

        SipServletResponse sipServletResponse;
        
        if(resultCode == 4241)
        {
          sipServletResponse = request.createResponse( SipServletResponse.SC_PAYMENT_REQUIRED );
        }
        else
        {
          sipServletResponse = request.createResponse( SipServletResponse.SC_SERVICE_UNAVAILABLE );
        }

        sipServletResponse.send();
        return;
      }
      else if(resultCode >= 5000 )
      {
        logger.info( "Diameter Answer == Permanent Failure (" + resultCode + ") ... Aborting!" );
        SipServletResponse sipServletResponse = request.createResponse( SipServletResponse.SC_SERVICE_UNAVAILABLE );
        sipServletResponse.send();
        return;
      }

      Proxy proxy = request.getProxy();
      proxy.setRecordRoute(true);
      proxy.setParallel(true);
      proxy.setSupervised(true);
      proxy.proxyTo(contactAddresses);    
    } else {
      logger.info(request.getRequestURI().toString() + " is not currently registered");
      SipServletResponse sipServletResponse = 
        request.createResponse(SipServletResponse.SC_MOVED_PERMANENTLY, "Moved Permanently");
      sipServletResponse.send();
    }
  }

  @Override
  protected void doRegister(SipServletRequest req) throws ServletException,
      IOException {
    logger.info("Received register request: " + req.getTo());
    int response = SipServletResponse.SC_OK;
    SipServletResponse resp = req.createResponse(response);
    HashMap<String, String> users = (HashMap<String, String>) getServletContext().getAttribute("registeredUsersMap");
    if(users == null) users = new HashMap<String, String>();
    getServletContext().setAttribute("registeredUsersMap", users);
    
    Address address = req.getAddressHeader(CONTACT_HEADER);
    String fromURI = req.getFrom().getURI().toString();
    
    int expires = address.getExpires();
    if(expires < 0) {
      expires = req.getExpires();
    }
    if(expires == 0) {
      users.remove(fromURI);
      logger.info("User " + fromURI + " unregistered");
    } else {
      resp.setAddressHeader(CONTACT_HEADER, address);
      users.put(fromURI, address.getURI().toString());
      logger.info("User " + fromURI + 
          " registered with an Expire time of " + expires);
    }       
            
    resp.send();
  }

  /**
   * {@inheritDoc}
   */
  protected void doResponse(SipServletResponse response)
      throws ServletException, IOException {

    logger.info("SimpleProxyServlet: Got response:\n" + response);
    if(SipServletResponse.SC_OK == response.getStatus() && "BYE".equalsIgnoreCase(response.getMethod())) {
      SipSession sipSession = response.getSession(false);
      if(sipSession != null) {
        SipApplicationSession sipApplicationSession = sipSession.getApplicationSession();
        sipSession.invalidate();
        sipApplicationSession.invalidate();
      }     
    }
    else if ( response.getStatus() >= 400) {
      doErrorResponse(response);
    }
  }
  
  @Override
  protected void doCancel( SipServletRequest req ) throws ServletException, IOException
  {
    doDiameterCharging( req.getCallId(), CHARGING_MSDN_ID, CHARGING_VALUE, true );
  }
  
  @Override
  protected void doErrorResponse( SipServletResponse resp ) throws ServletException, IOException
  {
    doDiameterCharging( resp.getCallId(), CHARGING_MSDN_ID, CHARGING_VALUE, true );
  }
  
  /**
   * Method for doing the Charging, either it's a debit or a refund.
   * 
   * @param sessionId the Session-Id for the Diameter Message
   * @param userId the User-Id of the client in the Diameter Server
   * @param cost the Cost (or Refund value) of the service 
   * @param refund boolean indicating if it's a refund or not
   * @return a long with the Result-Code AVP from the Answer
   */
  private long doDiameterCharging(String sessionId, String userId, Long cost, boolean refund)
  {
    try
    {
      logger.info( "Creating Diameter Charging " + (refund ? "Refund" : "Debit") + " Request UserId[" + userId + "], Cost[" + cost + "]..." );
      Request req = (Request) diameterBaseClient.createAccountingRequest( sessionId, userId, cost, refund );
      
      logger.info( "Sending Diameter Charging " + (refund ? "Refund" : "Debit") + " Request..." );
      Message msg = diameterBaseClient.sendMessageSync( req );

      long resultCode = -1;
      
      if(msg instanceof Answer)
        resultCode = ((Answer)msg).getResultCode().getUnsigned32();
      
      return resultCode;
    }
    catch ( Exception e )
    {
      logger.error( "Failure communicating with Diameter Charging Server.", e );
      return 5012;
    }   
  }
}
