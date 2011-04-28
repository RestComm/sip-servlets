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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;

/**
 * This is the SIP Servlet for OpenIMS Integration example.
 * 
 * @author <a href="mailto:brainslog@gmail.com"> Alexandre Mendonca </a>
 *
 */
public class DiameterOpenIMSSipServlet extends SipServlet {

  private static final long serialVersionUID = 1L;

  private static Logger logger = Logger.getLogger(DiameterOpenIMSSipServlet.class);

  DiameterShClient diameterShClient = null;

  private static SipFactory sipFactory;

  public static HashMap<String, Collection<MissedCall>> missedCalls = new HashMap<String, Collection<MissedCall>>();

  /**
   * Default constructor.
   */
  public DiameterOpenIMSSipServlet() {}

  @Override
  public void init(ServletConfig servletConfig) throws ServletException
  {
    logger.info("Diameter OpenIMS SIP Servlet : Initializing ...");

    super.init(servletConfig);

    // Get the SIP Factory
    sipFactory = (SipFactory)servletConfig.getServletContext().getAttribute(SIP_FACTORY);

    // Initialize Diameter Sh Client
    try
    {
      // Get our Diameter Sh Client instance
      this.diameterShClient = new DiameterShClient();

      logger.info("Diameter OpenIMS SIP Servlet : Sh-Client Initialized successfuly!");
    }
    catch ( Exception e ) {
      logger.error( "Diameter OpenIMS SIP Servlet : Sh-Client Failed to Initialize.", e );
    }   
  }

  @Override
  protected void doInvite(SipServletRequest request) throws ServletException, IOException
  {
    logger.info("Proccessing INVITE (" + request.getFrom() + " -> " + request.getTo() +") Request...");   

    if(request.isInitial())
    {
      Proxy proxy = request.getProxy();
      if(request.getSession().getAttribute( "firstInvite") == null)
      {
        request.getSession().setAttribute( "firstInvite", true );
        proxy.setRecordRoute(true);
        proxy.setSupervised(true);
        proxy.proxyTo( request.getRequestURI() );
      }
      else
      {
        proxy.proxyTo( request.getRequestURI() );
      }
    }
  }

  @Override
  protected void doErrorResponse( SipServletResponse response ) throws ServletException, IOException
  {
    logger.info("Proccessing Error Response (" + response.getStatus() + ")...");

    if(response.getStatus() == 404)
    {
      // Let's see from whom to whom
      String to =  response.getTo().getDisplayName() == null ? response.getTo().getURI().toString() : response.getTo().getDisplayName() + " <" + response.getTo().getURI() + ">";
      String from = response.getFrom().getDisplayName() == null ? response.getFrom().getURI().toString() : response.getFrom().getDisplayName() + " <" + response.getFrom().getURI() + ">";

      String toAddress = response.getTo().getURI().toString();

      // Create the MissedCall object
      MissedCall mC = new MissedCall(from, new Date());

      logger.info( "Created Missed Call: From[" + from + "], To[" + to + "]/URI[" + toAddress + "], Date[" + mC.getDate() + "]" );

      Collection<MissedCall> mCs = missedCalls.get(toAddress);

      if(mCs == null)
      {
        mCs = new ArrayList<MissedCall>();
        missedCalls.put( toAddress, mCs );
      }

      if(!mCs.contains( mC ))
      {
        mCs.add( mC );
        logger.info( "Added new missed call to " + toAddress + " list. (current size: " + mCs.size() + ")" );
      }
      
    }
    else
    {
      logger.info( "Got error response (" + response.getStatus() + "). Not processing further." );
    }
  }

  public static void sendSIPMessage(String toAddressString, String message)
  {
    try
    {
      logger.info( "Sending SIP Message [" + message + "] to [" + toAddressString + "]" );

      SipApplicationSession appSession = sipFactory.createApplicationSession();
      Address from = sipFactory.createAddress("Missed Calls <sip:missed-calls@mss.mobicents.org>");
      Address to = sipFactory.createAddress(toAddressString);
      SipServletRequest request = sipFactory.createRequest(appSession, "MESSAGE", from, to);
      request.setContent(message, "text/html");

      request.send(); 
    }
    catch (Exception e) {
      logger.error( "Failure creating/sending SIP Message notification.", e );
    }

  }
}
