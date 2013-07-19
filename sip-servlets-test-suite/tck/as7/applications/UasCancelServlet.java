/*
 * $Id: UasCancel.java,v 1.6 2003/09/20 01:48:14 akristensen Exp $
 *
 * Copyright 2006 Cisco Systems, Inc.
 */
/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * UasCancelServlet is used to test the specification of  UAS
 */
package com.bea.sipservlet.tck.apps.spec.uas;

import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.TimerService;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;

/**

 * Test of CANCEL at a UAS. The successful message flow is:

 * 

 * <ul>

 * <li>receive initial INVITE

 * <li>receive CANCEL for INVITE

 * <li>send 200 to CANCEL (container, not app)

 * <li>send 487 to INVITE (container, not app)

 * <li>doCancel invoked on app

 * </ul>

 * 

 * <p>SSA spec, section 7.2.3.

 */
@javax.servlet.sip.annotation.SipServlet(name = "UasCancel")
public class UasCancelServlet extends BaseServlet {
  @Resource
  TimerService timerService;

  static UasCancelServlet instance;

  static synchronized UasCancelServlet getInstance() {
    return instance;
  }

  private static Logger logger = Logger.getLogger(UasCancelServlet.class);

  public void init() throws ServletException {
    if (timerService == null) {
      throw new ServletException("No TimerService context attribute");
    }
    // bit of a hack - lets timer get to this servlet
    synchronized (UasCancelServlet.class) {
      if (instance == null) {
        instance = this;
      }
    }
  }

  public void destroy() {
    synchronized (UasCancelServlet.class) {
      if (instance == this) {
        instance = null;
      }
    }
  }

  public void timeout(ServletTimer timer, SipSession sipSession) {
    serverEntryLog();
    String reasonPhrase;
    if (sipSession.getAttribute("cancelled") != null) {
      reasonPhrase = "got cancelled";
    } else {
      logger.error("***ERROR: didn't receive notification of CANCEL***");
      reasonPhrase = "didn't get cancelled";
    }
    try {
      SipServletRequest invite = (SipServletRequest) sipSession
          .getAttribute("invite");
      SipServletResponse resp = invite.createResponse(500, reasonPhrase);
      try {
        resp.send();
      } catch (IOException ex) {
        logger.error("***failed to send subsequent request***", ex);
      }
      logger.error("***ERROR: sending final response after CANCEL didn't "
          + "cause IllegalStateException***");
    } catch (IllegalStateException ex) {
      logger.info("===SUCCESS: sending final response after CANCEL "
          + "cause IllegalStateException===");
    }
  }

  protected void doInvite(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    logger.debug("---doInvite---");
    SipServletResponse resp = req.createResponse(180);
    resp.send();
    // just set timer - we expect a CANCEL to arrive...
    SipSession sipSession = req.getSession();
    sipSession.setAttribute("invite", req);
    timerService.createTimer(req.getApplicationSession(), 5000, true,
        UasCancelServlet.class.getName());
  }

  public void doCancel(SipServletRequest req) throws IOException {
    serverEntryLog();
    logger.debug("---doCancel---");
    SipSession sipSession = req.getSession();
    sipSession.setAttribute("cancelled", Boolean.TRUE);
  }

  protected void doAck(SipServletRequest req) throws ServletException {
    serverEntryLog();
    logger.error("***ERROR: doAck should not be invoked for non-2xx's***");
  }

  public String getServletInfo() {
    return "Test of CANCEL at UAS";
  }
}
