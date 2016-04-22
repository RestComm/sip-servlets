/**
 * 
 * (c) 2007-2008 BEA Systems, Inc., or its suppliers, as applicable. 
 * All rights reserved. 
 *
 * Used to declare sip application 
 */
package com.bea.sipservlet.tck.apps.spec.uas;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSessionsUtil;

import org.apache.log4j.Logger;

import com.bea.sipservlet.tck.apps.BaseServlet;

@javax.servlet.sip.annotation.SipServlet(name = "Annotation")
public class AnnotationServlet extends BaseServlet {
  private static final long serialVersionUID = 4431707311714245815L;
  private static Logger logger = Logger.getLogger(UasActiveServlet.class);

  private boolean isPostConstructCalled = false;

  @Resource
  SipSessionsUtil sipSessionsUtil;

  public void init() throws ServletException {
  }
  
  @Override
  protected void doMessage(SipServletRequest req) throws ServletException, IOException {
    serverEntryLog();
    
    String verification = "";
    if(!isPostConstructCalled){
      verification = "Fail to invoke the method annotated by @PostConstruct";
    } else if (!verifySipSessionsUtil(sipSessionsUtil, req)){
      verification = "Fail to get SipSessionsUtil through @Resource";
    }
    
    if(verification.length() > 0){
      req.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, verification).send();
    } else {
      req.createResponse(SipServletResponse.SC_OK).send();
    }
  }
  
  protected boolean verifySipSessionsUtil(SipSessionsUtil util, SipServletRequest req){
    if(util != null){
      SipApplicationSession appSession = req.getSession().getApplicationSession();
      String id = appSession.getId();
      SipApplicationSession appSessionNew = sipSessionsUtil.getApplicationSessionById(id);
      if(appSessionNew != null && id.equals(appSessionNew.getId())) return true;
    }
    
    return false;
  }
  
  @PostConstruct
  private void postConstruct() {
    isPostConstructCalled = true;
  }

}
