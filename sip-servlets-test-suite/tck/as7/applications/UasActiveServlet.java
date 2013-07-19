/*
 * $Id: UasActive.java,v 1.7 2003/11/10 00:17:23 akristensen Exp $
 *
 * Copyright 2006 Cisco Systems, Inc.
 */
/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 * 
 * UasActiveServlet is used to test the specification of  UAS
 */
package com.bea.sipservlet.tck.apps.spec.uas;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.ServletTimer;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletMessage;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.servlet.sip.TimerService;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;

/**

 * Accepts incoming INVITE with 180 and then 200. Waits 2 seconds and then

 * sends an INFO request, waits another 2 seconds and then sends a BYE.

 * 

 * <p>Sets headers Foo (multivalue: bar, baz) and User-Agent: UasActive.

 * Also sets content using different mechanisms in all four messages

 * sent. In all cases the resulting content should be "Active UAS" with

 * Content-Length: 10 and Content-Type: text/plain, possibly with some

 * charset attribute.

 */
@javax.servlet.sip.annotation.SipServlet(name = "UasActive")
public class UasActiveServlet extends BaseServlet {
  private static final long FREQUENCY = 2000;

  private static final String CONTENT = "Active UAS";

  private static final byte[] BA_CONTENT = getBytes_ISO_8859_1(CONTENT);

  @Resource
  SipFactory sipFactory;

  @Resource
  TimerService timerService;

  static UasActiveServlet instance;

  private static Logger logger = Logger.getLogger(UasActiveServlet.class);

  static final byte[] getBytes_ISO_8859_1(String s) {
    try {
      return s.getBytes("ISO-8859-1");
    } catch (UnsupportedEncodingException ex) {
      // can't happen - all platforms MUST support ISO-8859-1
      return null;
    }
  }

  static synchronized UasActiveServlet getInstance() {
    return instance;
  }

  /**

   * Timer callback called from TimerCallback. Is invoked repeatedly.

   * First time we send an INFO in the established dialog and set a

   * flag in the SipSession. Second time we send a BYE and cancel the

   * timer.

   */
  public void timeout(ServletTimer timer, SipSession sipSession) {
    serverEntryLog();
    try {
      SipServletRequest req;
      String headerName;
      if (sipSession.getAttribute("TERMINATE") == null) {
        sipSession.setAttribute("TERMINATE", Boolean.TRUE);
        req = sipSession.createRequest("INFO");
        if ((headerName = testSystemHeaders(req)) != null) {
          logger.error("***Error: Managed to set system header: ***" +
              headerName);
          return;
        }
        addHeaders(req);
        setContent(req, 3);
      } else {
        timer.cancel();
        req = sipSession.createRequest("BYE");
        if ((headerName = testSystemHeaders(req)) != null) {
          logger.error("***Error: Managed to set system header: ***" +
              headerName);
          return;
        }
        addHeaders(req);
        setContent(req, 4);
      }
      req.send();
    } catch (Exception ex) {
      logger.error("***failed to send subsequent request***", ex);
    }
  }

  /** Checks we have a temp dir for this app, Servlet API v2.3, 3.7. */
  public void init() throws ServletException {
    File tempDir = (File) getServletContext().getAttribute(
        "javax.servlet.context.tempdir");
    if (tempDir == null) {
      throw new ServletException(
          "no tempdir under \"javax.servlet.context.tempdir\"");
    }
    if (!tempDir.isDirectory()) {
      throw new ServletException("tempdir not a directory");
    }
    if (sipFactory == null) {
      throw new ServletException("No SipFactory context attribute");
    }
    if (timerService == null) {
      throw new ServletException("No TimerService context attribute");
    }
    // bit of a hack - lets timer get to this servlet
    synchronized (UasActiveServlet.class) {
      if (instance == null) {
        instance = this;
      }
    }
  }

  public void destroy() {
    synchronized (UasActiveServlet.class) {
      if (instance == this) {
        instance = null;
      }
    }
  }

  protected void doInvite(SipServletRequest req) throws ServletException,
      IOException {
    serverEntryLog();
    logger.debug("---doInvite---");
    String headerName;
    if (!req.isInitial())
      throw new IllegalStateException();
    SipServletResponse resp;
    SipSession sipSession = req.getSession();
    timerService.createTimer(req.getApplicationSession(), FREQUENCY, FREQUENCY,
        true, true, UasActiveServlet.class.getName());
    resp = req.createResponse(180, "no particular reason");
    addHeaders(resp);
    setContent(resp, 1);
    resp.send();
    resp = req.createResponse(200);
    addHeaders(resp);
    setContent(resp, 2);
    resp.send();
  }

  /**

   * Add single and multi value headers. Verify that

   * <ul>

   * <li>setting "system" headers fail with IllegalArgumentException

   * <li>setHeader removes existing headers

   * <li>addHeader can be used to set multiple header field values

   * </ul>

   */
  static void addHeaders(SipServletMessage msg) {
    msg.addHeader("Foo", "bar");
    msg.addHeader("Foo", "baz");
    msg.addHeader("User-Agent", "ActiveUas");
    msg.setHeader("User-Agent", "UasActive");
  }

  /**

   * Attempts to set system headers [SSA 6.4.1], and return false if

   * it succeeds in doing so.

   * 

   * @throws Err if successful in setting system header or if an

   *     error occurs

   */
  String testSystemHeaders(SipServletMessage msg)
      throws ServletParseException {
    if (setSystemHeader(msg, "Call-ID", "fh4783r4")) {
      err("Managed to set Call-ID system header");
    }
    if (setSystemHeader(msg, "From", "<sip:alice@example.com>")) {
      err("Managed to set From system header");
    }
    if (setSystemHeader(msg, "To", "<sip:alice@example.com>")) {
      err("Managed to set To system header");
    }
    if (setSystemHeader(msg, "CSeq", "234765 " + msg.getMethod())) {
      err("Managed to set CSeq system header");
    }
    if (setSystemHeader(msg, "Via", "SIP/2.0/TCP 127.0.0.1sip:localhost>")) {
      err("Managed to set Record-Route system header");
    }
    if (setSystemHeader(msg, "Route", "<sip:localhost>")) {
      err("Managed to set Route system header");
    }
    try {
      // this should cause exception:
      msg.addAddressHeader("Contact", sipFactory
          .createAddress("<sip:localhost:5000>"), false);
      err("Managed to set Contact system header");
    } catch (IllegalArgumentException _) {
    }
    return null;
  }

  private static boolean setSystemHeader(SipServletMessage msg, String name,
      String value) {
    try {
      msg.setHeader("Call-ID", "fh4783r4");
      return true;
    } catch (IllegalArgumentException _) {
      return false;
    }
  }

  /**

   * Sets content in four different ways. Actual content is the same

   * in all cases but method of setting differs. UAC tester

   * verifies the content along with Content-Type and Content-Length

   * is set correctly.

   * 

   * <p>XXX: extend to test character sets.

   */
  private void setContent(SipServletMessage msg, int m) throws IOException {
    switch (m) {
      case 1:
        // setContent with String
        try {
          msg.setContent(CONTENT, "text/plain");
        } catch (UnsupportedEncodingException ex) {
          logger.error("***setContent failed***", ex);
          throw new RuntimeException(ex.getMessage());
        }
        break;
      case 2:
      case 3:
      case 4:
        // setContent with byte[]
        try {
          msg.setContent(BA_CONTENT, "text/plain");
        } catch (UnsupportedEncodingException ex) {
          logger.error("***setContent failed***", ex);
          throw new RuntimeException(ex.getMessage());
        }
        break;
      /*

      case 3:

      case 4:

      // write to OutputStream

      OutputStream out = msg.getOutputStream();

      out.write(BA_CONTENT);

      out.close();

      msg.setContentType("text/plain");

      break;

      */
    }
  }

  protected void doProvisionalResponse(SipServletResponse resp)
      throws ServletException {
    serverEntryLog();
    throw new ServletException("got provisional response: " + resp);
  }

  protected void doErrorResponse(SipServletResponse resp)
      throws ServletException {
    serverEntryLog();
    throw new ServletException("got error response: " + resp);
  }

  private static void err(String msg) {
// throw new Err(msg);
  }

  public String getServletInfo() {
    return "Active UAS test servlet; sends INFO and BYE";
  }
}
