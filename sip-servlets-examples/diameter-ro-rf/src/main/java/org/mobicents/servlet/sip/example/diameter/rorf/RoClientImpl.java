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

package org.mobicents.servlet.sip.example.diameter.rorf;

import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.jdiameter.api.Answer;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.Avp;
import org.jdiameter.api.AvpSet;
import org.jdiameter.api.EventListener;
import org.jdiameter.api.IllegalDiameterStateException;
import org.jdiameter.api.InternalException;
import org.jdiameter.api.Network;
import org.jdiameter.api.NetworkReqListener;
import org.jdiameter.api.OverloadException;
import org.jdiameter.api.Request;
import org.jdiameter.api.RouteException;
import org.jdiameter.api.Stack;
import org.jdiameter.api.app.AppAnswerEvent;
import org.jdiameter.api.app.AppRequestEvent;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.auth.events.ReAuthAnswer;
import org.jdiameter.api.auth.events.ReAuthRequest;
import org.jdiameter.api.cca.ClientCCASession;
import org.jdiameter.api.cca.ServerCCASession;
import org.jdiameter.api.cca.events.JCreditControlAnswer;
import org.jdiameter.api.cca.events.JCreditControlRequest;
import org.jdiameter.client.api.ISessionFactory;
import org.mobicents.diameter.stack.DiameterStackMultiplexer;
import org.mobicents.servlet.sip.example.diameter.utils.DiameterUtilities;

public class RoClientImpl extends CreditControlSessionFactory implements NetworkReqListener, EventListener<Request, Answer>, RoClient {

  private static Logger logger = Logger.getLogger(RoClient.class); 

  // Internal Client State Machine --------------------------------------------
  private static final int IDLE                     = 0;
  private static final int SENT_CCR_INITIAL         = 2;
  private static final int RECEIVED_CCA_INITIAL     = 4;
  private static final int SENT_CCR_UPDATE          = 6;
  private static final int RECEIVED_CCA_UPDATE      = 8;
  private static final int SENT_CCR_TERMINATION     = 10;
  private static final int RECEIVED_CCA_TERMINATION = 12;

  // CC-Request-Type Values ---------------------------------------------------
  private static final int CC_REQUEST_TYPE_INITIAL     = 1;
  private static final int CC_REQUEST_TYPE_UPDATE      = 2;
  private static final int CC_REQUEST_TYPE_TERMINATION = 3;
  private static final int CC_REQUEST_TYPE_EVENT       = 4;

  private int currentState = IDLE;

  // Configuration Values -----------------------------------------------------

  private static String clientHost = "127.0.0.1";
  private static String clientPort = "13868";
  private static String clientURI = "aaa://" + clientHost + ":" + clientPort;

  private static String serverHost = "127.0.0.1";
  private static String serverPort = "3868";
  private static String serverURI = "aaa://" + serverHost + ":" + serverPort;

  private static String realmName = "mobicents.org";

  private ApplicationId roAppId = ApplicationId.createByAuthAppId(10415L, 4L);

  // Constants ----------------------------------------------------------------

  // Service Context Domain
  private static final String SERVICE_CONTEXT_DOMAIN = "@mss.mobicents.org";

  // Ten seconds charging periods
  private final int CHARGING_UNITS_TIME = 10; 

  // Stack, Sessions & Listeners ----------------------------------------------

  private DiameterStackMultiplexer muxMBean;
  private RoClientListener listener;

  private HashMap<String, ClientCCASession> roSessions = new HashMap<String, ClientCCASession>();

  private boolean areFinalUnits;
  private int reservedUnits;

  // Charging control variables ----------------------------------------------- 
  private int ccRequestNumber = 0;
  private int totalCallDurationCounter = 0;
  private int partialCallDurationCounter = 0;

  // Timers -------------------------------------------------------------------
  private Timer callDurationTimer = null;
  private Timer sendUpdatetimer = null;
  private Timer sendTerminateTimer = null;

  public RoClientImpl(RoClientListener listener) {
    super(null, 5000);

    this.listener = listener;

    try {
      ObjectName objectName = new ObjectName("diameter.mobicents:service=DiameterStackMultiplexer");
      Object[] params = new Object[]{};
      String[] signature = new String[]{};
      String operation = "getMultiplexerMBean";

      MBeanServer server = (MBeanServer) MBeanServerFactory.findMBeanServer(null).get(0);

      Object object = server.invoke( objectName, operation, params, signature );

      if(object instanceof DiameterStackMultiplexer) {
        muxMBean = (DiameterStackMultiplexer) object;
      }

      Stack stack = muxMBean.getStack();

      super.sessionFactory = stack.getSessionFactory();

      Network network = stack.unwrap(Network.class);
      network.addNetworkReqListener(this, roAppId);

      ((ISessionFactory) sessionFactory).registerAppFacory(ServerCCASession.class, this);
      ((ISessionFactory) sessionFactory).registerAppFacory(ClientCCASession.class, this);
    }
    catch (Exception e) {
      logger.error("Failed to initialize Ro Client.", e);
    }
  }

  /**
   * Creates a new Ro Client Session
   * 
   * @return
   * @throws InternalException
   */
  private ClientCCASession getRoSession() throws InternalException {
    return ((ISessionFactory) super.sessionFactory).getNewAppSession(null, roAppId, ClientCCASession.class, null);
  }

  public void reserveInitialUnits(String subscriptionId, String serviceContextId) throws Exception {
    // Fetch existing Ro session ...
    ClientCCASession roSession = roSessions.get(serviceContextId);
    // ...it shouldn't be present, so create a new one and store it.
    if(roSession == null) {
      roSession = getRoSession();
      roSessions.put(serviceContextId, roSession);
    }

    // Initialize call related control variables
    this.ccRequestNumber = 0;
    this.totalCallDurationCounter = 0;
    this.partialCallDurationCounter = 0;

    // Create and send CCR (INITIAL) 
    JCreditControlRequest initialCCR = createCCR(CC_REQUEST_TYPE_INITIAL, subscriptionId, serviceContextId);
    roSession.sendCreditControlRequest(initialCCR);

    switchStateMachine(SENT_CCR_INITIAL);
  }

  /* (non-Javadoc)
   * @see org.mobicents.diameter.simulator.clients.RoClient#startAccounting(java.lang.String)
   */
  public void startCharging(String subscriptionId, String serviceContextId) throws Exception {
    // Initialize timers
    sendUpdatetimer =  new Timer();
    sendTerminateTimer =  new Timer();
    callDurationTimer =  new Timer();

    if(logger.isInfoEnabled()) {
      logger.info("(((o))) SERVICE HAS BEEN ESTABLISHED (((o)))");
    }

    // Start call time counter
    setCountCallTime(serviceContextId, 1000);

    // Check whether we should do new charging or terminate on final units.
    if(areFinalUnits) {
      setTerminateTimer(subscriptionId, serviceContextId, reservedUnits * 1000);
    }
    else {
      setUpdateTimer(subscriptionId, serviceContextId, reservedUnits * 1000);
    }
  }

  /*
   * (non-Javadoc)
   * @see org.mobicents.servlet.sip.example.diameter.rorf.RoClient#updateCharging(java.lang.String, long)
   */
  public void updateCharging(String subscriptionId, String serviceContextId, long units) throws Exception {
    // Fetch existing Ro session
    ClientCCASession roSession = roSessions.get(serviceContextId);

    // Create and Send CCR (UPDATE)
    JCreditControlRequest updateCCR = createCCR(CC_REQUEST_TYPE_UPDATE, subscriptionId, serviceContextId);
    roSession.sendCreditControlRequest(updateCCR);

    switchStateMachine(SENT_CCR_UPDATE);
  }

  /* (non-Javadoc)
   * @see org.mobicents.diameter.simulator.clients.RoClient#stopAccounting(java.lang.String)
   */
  public void stopCharging(String subscriptionId, String serviceContextId) throws Exception {
    // Stop existing timers
    sendUpdatetimer.cancel();
    callDurationTimer.cancel();

    try {
      // Call listener method
      this.listener.creditTerminated();
    }
    catch (Exception e) {
      logger.error("Failure in Listener handling 'creditTerminated' callback.");
    }

    if(logger.isInfoEnabled()) {
      logger.info("(((o))) SERVICE HAS BEEN TERMINATED! (((o)))");
    }

    // Fetch existing Ro session
    ClientCCASession roSession = roSessions.get(serviceContextId);

    // Create and Send CCR (TERMINATION)
    JCreditControlRequest terminateCCR = createCCR(CC_REQUEST_TYPE_TERMINATION, subscriptionId, serviceContextId);
    roSession.sendCreditControlRequest(terminateCCR);

    switchStateMachine(SENT_CCR_TERMINATION);
  }

  /**
   * Create a Ro CCR message, with the selected Request Type and Service Context ID
   * 
   * @param ccRequestType
   * @param serviceContextId
   * @return
   * @throws Exception
   */
  private JCreditControlRequest createCCR(int ccRequestType, String subscriptionId, String serviceContextId) throws Exception {
    ClientCCASession roSession = roSessions.get(serviceContextId);

    // Create Credit-Control-Request
    JCreditControlRequest ccr = createCreditControlRequest(roSession.getSessions().get(0).createRequest(JCreditControlRequest.code, roAppId, realmName, serverHost));

    // AVPs present by default: Origin-Host, Origin-Realm, Session-Id, Vendor-Specific-Application-Id, Destination-Realm
    AvpSet ccrAvps = ccr.getMessage().getAvps();

    // Add remaining AVPs ... from RFC 4006:
    //<CCR> ::= < Diameter Header: 272, REQ, PXY >
    // < Session-Id > 
    // ccrAvps.addAvp(Avp.SESSION_ID, s.getSessionId());

    // { Origin-Host }
    ccrAvps.removeAvp(Avp.ORIGIN_HOST);
    ccrAvps.addAvp(Avp.ORIGIN_HOST, clientURI, true);

    // { Origin-Realm }
    // ccrAvps.addAvp(Avp.ORIGIN_REALM, realmName, true);

    // { Destination-Realm } 
    // ccrAvps.addAvp(Avp.DESTINATION_REALM, realmName, true);

    // { Auth-Application-Id }
    ccrAvps.addAvp(Avp.AUTH_APPLICATION_ID, 4);
    AvpSet vsaid = ccrAvps.addGroupedAvp(Avp.VENDOR_SPECIFIC_APPLICATION_ID);
    vsaid.addAvp(Avp.VENDOR_ID, 10415);
    vsaid.addAvp(Avp.AUTH_APPLICATION_ID, 4);

    // { Service-Context-Id }
    // 8.42.  Service-Context-Id AVP
    //
    // The Service-Context-Id AVP is of type UTF8String (AVP Code 461) and
    // contains a unique identifier of the Diameter credit-control service
    // specific document that applies to the request (as defined in section
    // 4.1.2).  This is an identifier allocated by the service provider, by
    // the service element manufacturer, or by a standardization body, and
    // MUST uniquely identify a given Diameter credit-control service
    // specific document.  The format of the Service-Context-Id is:
    // 
    // "service-context" "@" "domain"
    // 
    // service-context = Token
    // 
    // The Token is an arbitrary string of characters and digits.
    // 
    // 'domain' represents the entity that allocated the Service-Context-Id.
    // It can be ietf.org, 3gpp.org, etc., if the identifier is allocated by
    // a standardization body, or it can be the FQDN of the service provider
    // (e.g., provider.example.com) or of the vendor (e.g.,
    // vendor.example.com) if the identifier is allocated by a private
    // entity.
    // 
    // This AVP SHOULD be placed as close to the Diameter header as
    // possible.
    // 
    // Service-specific documents that are for private use only (i.e., to
    // one provider's own use, where no interoperability is deemed useful)
    // may define private identifiers without need of coordination.
    // However, when interoperability is wanted, coordination of the
    // identifiers via, for example, publication of an informational RFC is
    // RECOMMENDED in order to make Service-Context-Id globally available.
    ccrAvps.addAvp(461, serviceContextId + SERVICE_CONTEXT_DOMAIN , false);

    // { CC-Request-Type }
    // 8.3.  CC-Request-Type AVP
    //
    // The CC-Request-Type AVP (AVP Code 416) is of type Enumerated and
    // contains the reason for sending the credit-control request message.
    // It MUST be present in all Credit-Control-Request messages.  The
    // following values are defined for the CC-Request-Type AVP:
    // 
    // INITIAL_REQUEST                 1
    //    An Initial request is used to initiate a credit-control session,
    //    and contains credit control information that is relevant to the
    //    initiation.
    // 
    // UPDATE_REQUEST                  2
    //    An Update request contains credit-control information for an
    //    existing credit-control session.  Update credit-control requests
    //    SHOULD be sent every time a credit-control re-authorization is
    //    needed at the expiry of the allocated quota or validity time.
    //    Further, additional service-specific events MAY trigger a
    //    spontaneous Update request.
    // 
    // TERMINATION_REQUEST             3
    //    A Termination request is sent to terminate a credit-control
    //    session and contains credit-control information relevant to the
    //    existing session.
    // 
    // EVENT_REQUEST                   4
    //    An Event request is used when there is no need to maintain any
    //    credit-control session state in the credit-control server.  This
    //    request contains all information relevant to the service, and is
    //    the only request of the service.  The reason for the Event request
    //    is further detailed in the Requested-Action AVP.  The Requested-
    //    Action AVP MUST be included in the Credit-Control-Request message
    //    when CC-Request-Type is set to EVENT_REQUEST.
    ccrAvps.addAvp(416, ccRequestType);

    // { CC-Request-Number }
    // 8.2.  CC-Request-Number AVP
    //
    // The CC-Request-Number AVP (AVP Code 415) is of type Unsigned32 and
    // identifies this request within one session.  As Session-Id AVPs are
    // globally unique, the combination of Session-Id and CC-Request-Number
    // AVPs is also globally unique and can be used in matching credit-
    // control messages with confirmations.  An easy way to produce unique
    // numbers is to set the value to 0 for a credit-control request of type
    // INITIAL_REQUEST and EVENT_REQUEST and to set the value to 1 for the
    // first UPDATE_REQUEST, to 2 for the second, and so on until the value
    // for TERMINATION_REQUEST is one more than for the last UPDATE_REQUEST.
    ccrAvps.addAvp(415, this.ccRequestNumber++);

    // [ Destination-Host ]
    ccrAvps.removeAvp(Avp.DESTINATION_HOST);
    ccrAvps.addAvp(Avp.DESTINATION_HOST, serverURI, false);

    // [ User-Name ] 
    // [ CC-Sub-Session-Id ] 
    // [ Acct-Multi-Session-Id ] 
    // [ Origin-State-Id ] 
    // [ Event-Timestamp ] 

    //*[ Subscription-Id ]
    // 8.46.  Subscription-Id AVP
    // 
    // The Subscription-Id AVP (AVP Code 443) is used to identify the end
    // user's subscription and is of type Grouped.  The Subscription-Id AVP
    // includes a Subscription-Id-Data AVP that holds the identifier and a
    // Subscription-Id-Type AVP that defines the identifier type.
    // 
    // It is defined as follows (per the grouped-avp-def of RFC 3588
    // [DIAMBASE]):
    // 
    // Subscription-Id ::= < AVP Header: 443 >
    //                     { Subscription-Id-Type }
    //                     { Subscription-Id-Data }
    AvpSet subscriptionIdAvp = ccrAvps.addGroupedAvp(443);

    // 8.47.  Subscription-Id-Type AVP
    // 
    // The Subscription-Id-Type AVP (AVP Code 450) is of type Enumerated,
    // and it is used to determine which type of identifier is carried by
    // the Subscription-Id AVP.
    // 
    // This specification defines the following subscription identifiers.
    // However, new Subscription-Id-Type values can be assigned by an IANA
    // designated expert, as defined in section 12.  A server MUST implement
    // all the Subscription-Id-Types required to perform credit
    // authorization for the services it supports, including possible future
    // values.  Unknown or unsupported Subscription-Id-Types MUST be treated
    // according to the 'M' flag rule, as defined in [DIAMBASE].
    // 
    // END_USER_E164                   0
    //   The identifier is in international E.164 format (e.g., MSISDN),
    // according to the ITU-T E.164 numbering plan defined in [E164] and
    // [CE164].
    // 
    // END_USER_IMSI                   1
    //   The identifier is in international IMSI format, according to the
    // ITU-T E.212 numbering plan as defined in [E212] and [CE212].
    // 
    // END_USER_SIP_URI                2
    //   The identifier is in the form of a SIP URI, as defined in [SIP].
    // 
    // END_USER_NAI                    3
    //   The identifier is in the form of a Network Access Identifier, as
    // defined in [NAI].
    // 
    // END_USER_PRIVATE                4
    //   The Identifier is a credit-control server private identifier.
    subscriptionIdAvp.addAvp(450, 2);

    // 8.48.  Subscription-Id-Data AVP
    // 
    // The Subscription-Id-Data AVP (AVP Code 444) is used to identify the
    // end user and is of type UTF8String.  The Subscription-Id-Type AVP
    // defines which type of identifier is used.
    subscriptionIdAvp.addAvp(444, subscriptionId, false);

    // [ Service-Identifier ]  
    // [ Termination-Cause ]

    // [ Requested-Service-Unit ] 
    // 8.18.  Requested-Service-Unit AVP
    //
    // The Requested-Service-Unit AVP (AVP Code 437) is of type Grouped and
    // contains the amount of requested units specified by the Diameter
    // credit-control client.  A server is not required to implement all the
    // unit types, and it must treat unknown or unsupported unit types as
    // invalid AVPs.
    // 
    // The Requested-Service-Unit AVP is defined as follows (per the
    // grouped-avp-def of RFC 3588 [DIAMBASE]):
    //
    // Requested-Service-Unit ::= < AVP Header: 437 >
    //                            [ CC-Time ]
    //                            [ CC-Money ]
    //                            [ CC-Total-Octets ]
    //                            [ CC-Input-Octets ]
    //                            [ CC-Output-Octets ]
    //                            [ CC-Service-Specific-Units ]
    //                           *[ AVP ]
    AvpSet rsuAvp = ccrAvps.addGroupedAvp(437);

    // 8.21.  CC-Time AVP
    //
    // The CC-Time AVP (AVP Code 420) is of type Unsigned32 and indicates
    // the length of the requested, granted, or used time in seconds.
    rsuAvp.addAvp(420, CHARGING_UNITS_TIME);

    // [ Requested-Action ] 
    //*[ Used-Service-Unit ] 
    // 8.19.  Used-Service-Unit AVP
    // 
    // The Used-Service-Unit AVP is of type Grouped (AVP Code 446) and
    // contains the amount of used units measured from the point when the
    // service became active or, if interim interrogations are used during
    // the session, from the point when the previous measurement ended.
    // 
    // The Used-Service-Unit AVP is defined as follows (per the grouped-
    // avp-def of RFC 3588 [DIAMBASE]):
    // 
    //   Used-Service-Unit ::= < AVP Header: 446 >
    //                         [ Tariff-Change-Usage ]
    //                         [ CC-Time ]
    //                         [ CC-Money ]
    //                         [ CC-Total-Octets ]
    //                         [ CC-Input-Octets ]
    //                         [ CC-Output-Octets ]
    //                         [ CC-Service-Specific-Units ]
    //                        *[ AVP ]
    if(ccRequestNumber >= 1) {
      AvpSet usedServiceUnit = ccrAvps.addGroupedAvp(446);
      usedServiceUnit.addAvp(420, this.partialCallDurationCounter);
    }
    // [ AoC-Request-Type ] 
    // [ Multiple-Services-Indicator ] 
    //*[ Multiple-Services-Credit-Control ]  
    //*[ Service-Parameter-Info ]  
    // [ CC-Correlation-Id ] 
    // [ User-Equipment-Info ] 
    //*[ Proxy-Info ]
    //*[ Route-Record ]
    // [ Service-Information ]
    //*[ AVP ]

    DiameterUtilities.printMessage(ccr.getMessage());

    return ccr;
  }

  public void doCreditControlAnswer(ClientCCASession session, JCreditControlRequest request, JCreditControlAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    AvpSet ccrAvps = request.getMessage().getAvps();
    AvpSet ccaAvps = answer.getMessage().getAvps();
    long resultCode = 5000;
    try {
      resultCode = answer.getResultCodeAvp().getUnsigned32();
    }
    catch (Exception e) {
      logger.error("Failed to retrieve Result-Code AVP value.", e);
    }

    switch (currentState) {
    case IDLE:
      logger.error("Unexpected CCA message at IDLE state.");
      break;
    case SENT_CCR_INITIAL:
      try {
        if(resultCode >= 2000 && resultCode < 3000) {
          // Fetch Granted-Service-Unit / CC-Time AVP value
          this.reservedUnits = ccaAvps.getAvp(431).getGrouped().getAvp(420).getInteger32();

          // Check if these are Final Units
          this.areFinalUnits = (answer.getMessage().getAvps().getAvp(430) != null);

          if(logger.isInfoEnabled()) {
            logger.info("(( $ )) Requested [" + CHARGING_UNITS_TIME + "] units, granted [" + reservedUnits + "] units. (( $ ))");
          }

          try {
            this.listener.creditGranted(reservedUnits, areFinalUnits);
          }
          catch (Exception e) {
            logger.error("Failure in Listener handling 'creditGranted' callback.");
          }
        }
        else {
          try {
            this.listener.creditDenied((int)resultCode);
          }
          catch (Exception e) {
            logger.error("Failure in Listener handling 'creditDenied' callback.");
          }
          logger.info("(((o))) UNABLE TO ESTABLISH SERVICE, CREDIT DENIED (" + resultCode + ") (((o)))");
        }
      }
      catch (Exception e) {
        logger.error("(( $ )) Failure handling CCA at SENT_CCR_INITIAL state. (( $ ))", e);
      }
      switchStateMachine(RECEIVED_CCA_INITIAL);
      break;
    case RECEIVED_CCA_INITIAL:
      logger.error("Unexpected CCA message at RECEIVED_CCA_INITIAL state. Duplicate?");
      break;
    case SENT_CCR_UPDATE:
      try {
        if(resultCode >= 2000 && resultCode < 3000) {
          // Fetch Granted-Service-Unit / CC-Time AVP value
          long reservedUnits  = ccaAvps.getAvp(431).getGrouped().getAvp(420).getInteger32();

          // Fetch Service-Context-Id AVP value
          String serviceContextId = ccrAvps.getAvp(461).getUTF8String().replaceAll(SERVICE_CONTEXT_DOMAIN, "");

          if(logger.isInfoEnabled()) {
            logger.info("(( $ )) Requested [" + CHARGING_UNITS_TIME + "] units, granted [" + reservedUnits + "] units. (( $ ))");
          }

          // Reset for calculating Used-Service-Units AVP
          partialCallDurationCounter = 0;

          String subscriptionId = ccrAvps.getAvp(443).getGrouped().getAvp(444).getUTF8String();

          // Check if Final
          Avp finalUnitIndication = answer.getMessage().getAvps().getAvp(430);
          if(finalUnitIndication == null) {
            setUpdateTimer(subscriptionId, serviceContextId, reservedUnits * 1000);
          }
          else {
            setTerminateTimer(subscriptionId, serviceContextId, reservedUnits * 1000);
          }
        }
        else {
          callDurationTimer.cancel();
          logger.error("(( $ )) Failure trying to obtain credit (" + resultCode + "). (( $ ))");
          logger.error("(((o))) SERVICE HAS BEEN TERMINATED! (((o)))");
        }
      }
      catch (Exception e) {
        logger.error("(( $ )) Failure handling CCA at SENT_CCR_UPDATE state. (( $ ))", e);
      }
      switchStateMachine(RECEIVED_CCA_INITIAL);
      break;
    case RECEIVED_CCA_UPDATE:
      logger.error("Unexpected CCA message at RECEIVED_CCA_UPDATE state.");
      break;
    case SENT_CCR_TERMINATION:
      if(resultCode >= 2000 && resultCode < 3000) {
        logger.info("(( $ )) Successfully terminated transaction at Online Charging Server (( $ ))");
      }
      else {
        logger.error("(( $ )) Failure '" + resultCode + "' terminating transaction at Online Charging Server (( $ ))");
      }
      try {
        // Fetch Service-Context-Id AVP value
        String serviceContextId = ccrAvps.getAvp(461).getUTF8String().replaceAll(SERVICE_CONTEXT_DOMAIN, "");
        ClientCCASession roSession = this.roSessions.remove(serviceContextId);
        roSession.release();
        roSession = null;
      }
      catch (Exception e) {
        logger.error("(( $ )) Failure handling CCA at SENT_CCR_TERMINATION state. (( $ ))", e);
      }
      break;
    case RECEIVED_CCA_TERMINATION:
      logger.error("Unexpected CCA message at RECEIVED_CCA_TERMINATION state.");
      break;
    default:
      logger.error("Unexpected CCA message at UNKNOWN state.");
      break;
    }
  }

  public void doOtherEvent(AppSession session, AppRequestEvent request, AppAnswerEvent answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    // Nothing to do here.
  }

  public void doReAuthRequest(ClientCCASession session, ReAuthRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    // Nothing to do here.
  }

  public void doCreditControlRequest(ServerCCASession session, JCreditControlRequest request) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    // Nothing to do here.
  }

  public void doReAuthAnswer(ServerCCASession session, ReAuthRequest request, ReAuthAnswer answer) throws InternalException, IllegalDiameterStateException, RouteException, OverloadException {
    // Nothing to do here.
  }

  public void sessionSupervisionTimerExpired(ServerCCASession session) {
    // Nothing to do here.
  }

  public void denyAccessOnTxExpire(ClientCCASession clientCCASessionImpl) {
    // Nothing to do here.
  }

  public void txTimerExpired(ClientCCASession session) {
    // Nothing to do here.
  }

  public Answer processRequest(Request request) {
    // Nothing to do here.
    return null;
  }

  public void receivedSuccessMessage(Request request, Answer answer) {
    // Nothing to do here.
  }

  // Timers -------------------------------------------------------------------

  private void setUpdateTimer(final String subscriptionId, final String serviceContextId, long ms) {
    Date timeToRun = new Date(System.currentTimeMillis() + ms);
    sendUpdatetimer.schedule(new TimerTask() {
      public void run() {
        try {
          updateCharging(subscriptionId, serviceContextId, partialCallDurationCounter);
        }
        catch (Exception e) {
          logger.error("(( $ )) Failure trying to create/send CCR (UPDATE) message. (( $ ))", e);
        }
      }
    }, timeToRun);
  }

  private void setTerminateTimer(final String subscriptionId, final String serviceContextId, long ms) {
    Date timeToRun = new Date(System.currentTimeMillis() + ms);
    sendTerminateTimer.schedule(new TimerTask() {
      public void run() {
        try {
          stopCharging(subscriptionId, serviceContextId);
        }
        catch (Exception e) {
          logger.error("(( $ )) Failure trying to create/send CCR (UPDATE) message. (( $ ))", e);
        }
      }
    }, timeToRun);
  }

  private void setCountCallTime(final String serviceContextId, long ms) {
    Date timeToRun = new Date(System.currentTimeMillis() + ms);
    callDurationTimer.schedule(new TimerTask() {
      public void run() {
        try {
          totalCallDurationCounter++;
          partialCallDurationCounter++;

          Integer minutes = totalCallDurationCounter / 60;
          Integer seconds = totalCallDurationCounter % 60;

          String minutesStr = minutes > 9 ? String.valueOf(minutes) : "0" + String.valueOf(minutes);
          String secondsStr = seconds > 9 ? String.valueOf(seconds) : "0" + String.valueOf(seconds);
          if(logger.isInfoEnabled()) {
            logger.info("   o))) " + minutesStr + ":" + secondsStr + " (((o   ");
          }
        }
        catch (Exception e) {
          logger.error("(((o))) Failure keeping track of service time. (((o)))", e);
        }
      }
    }, timeToRun, 998);
  }

  private void switchStateMachine(int newState) {
    this.currentState = newState;
  }

  public void stateChanged(Object source, Enum oldState, Enum newState) {
    stateChanged(oldState, newState);
  }
}
