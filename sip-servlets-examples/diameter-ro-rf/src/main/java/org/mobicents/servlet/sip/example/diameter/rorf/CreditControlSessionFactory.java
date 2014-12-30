/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.mobicents.servlet.sip.example.diameter.rorf;

import java.util.concurrent.ScheduledFuture;

import org.jdiameter.api.Answer;
import org.jdiameter.api.ApplicationId;
import org.jdiameter.api.Message;
import org.jdiameter.api.Request;
import org.jdiameter.api.SessionFactory;
import org.jdiameter.api.app.AppSession;
import org.jdiameter.api.app.StateChangeListener;
import org.jdiameter.api.auth.events.ReAuthAnswer;
import org.jdiameter.api.auth.events.ReAuthRequest;
import org.jdiameter.api.cca.ClientCCASession;
import org.jdiameter.api.cca.ClientCCASessionListener;
import org.jdiameter.api.cca.ServerCCASession;
import org.jdiameter.api.cca.ServerCCASessionListener;
import org.jdiameter.api.cca.events.JCreditControlAnswer;
import org.jdiameter.api.cca.events.JCreditControlRequest;
import org.jdiameter.client.api.ISessionFactory;
import org.jdiameter.client.impl.app.cca.ClientCCASessionDataLocalImpl;
import org.jdiameter.client.impl.app.cca.ClientCCASessionImpl;
import org.jdiameter.common.api.app.IAppSessionFactory;
import org.jdiameter.common.api.app.cca.ICCAMessageFactory;
import org.jdiameter.common.api.app.cca.IClientCCASessionContext;
import org.jdiameter.common.api.app.cca.IServerCCASessionContext;
import org.jdiameter.common.impl.app.auth.ReAuthAnswerImpl;
import org.jdiameter.common.impl.app.auth.ReAuthRequestImpl;
import org.jdiameter.common.impl.app.cca.JCreditControlAnswerImpl;
import org.jdiameter.common.impl.app.cca.JCreditControlRequestImpl;
import org.jdiameter.server.impl.app.cca.ServerCCASessionDataLocalImpl;
import org.jdiameter.server.impl.app.cca.ServerCCASessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CreditControlSessionFactory implements IAppSessionFactory, ClientCCASessionListener, ServerCCASessionListener,
          StateChangeListener, ICCAMessageFactory , IServerCCASessionContext, IClientCCASessionContext {

  protected SessionFactory sessionFactory = null;

  //its milliseconds
  protected long messageTimeout = 5000;

  protected int defaultDirectDebitingFailureHandling = 0;
  protected int defaultCreditControlFailureHandling = 0;

  //its seconds
  protected long defaultValidityTime = 30;
  protected long defaultTxTimerValue = 10;
  protected static final Logger logger = LoggerFactory.getLogger(CreditControlSessionFactory.class);

  public CreditControlSessionFactory(SessionFactory sessionFactory, long messageTimeout) {
    super();
    this.sessionFactory = sessionFactory;
    this.messageTimeout = messageTimeout;
  }

  public CreditControlSessionFactory(SessionFactory sessionFactory, long messageTimeout, int defaultDirectDebitingFailureHandling,
      int defaultCreditControlFailureHandling, long defaultValidityTime, long defaultTxTimerValue) {
    super();
    this.sessionFactory = sessionFactory;

    this.messageTimeout = messageTimeout;
    this.defaultDirectDebitingFailureHandling = defaultDirectDebitingFailureHandling;
    this.defaultCreditControlFailureHandling = defaultCreditControlFailureHandling;
    this.defaultValidityTime = defaultValidityTime;
    this.defaultTxTimerValue = defaultTxTimerValue;
  }

  public AppSession getSession(String sessionId, Class<? extends AppSession> aClass) {
	  return getNewSession(sessionId, aClass, null, null);
  }
  
  public AppSession getNewSession(String sessionId, Class<? extends AppSession> aClass, ApplicationId applicationId, Object[] args) {
    AppSession value = null;
    try {
      if (aClass ==  ClientCCASession.class) {

        ClientCCASessionImpl clientSession = null;
        ClientCCASessionDataLocalImpl data = new ClientCCASessionDataLocalImpl();
        data.setApplicationId(applicationId);
        if(args != null && args.length>1 && args[0] instanceof Request) {
        	Request request = (Request) args[0];
        	data.setSessionId(request.getSessionId());
        	clientSession = new ClientCCASessionImpl(data, this, (ISessionFactory) sessionFactory, this, this, this);
        }
        else {
        	data.setSessionId(sessionId);
        	clientSession = new ClientCCASessionImpl(data, this, (ISessionFactory) sessionFactory, this, this, this);
        }
        clientSession.addStateChangeNotification(this);
        
        value = clientSession;
      }
      else if (aClass ==  ServerCCASession.class) {
        ServerCCASessionImpl serverSession = null;
        ServerCCASessionDataLocalImpl data = new ServerCCASessionDataLocalImpl();
        data.setApplicationId(applicationId);
        if (args !=  null && args.length > 1 && args[0] instanceof Request) {
        	// This shouldn't happen but just in case
        	Request request = (Request) args[0];
        	data.setSessionId(request.getSessionId());
        	serverSession = new ServerCCASessionImpl(data, this, (ISessionFactory) sessionFactory, this, this, this);
        }
        else {
        	data.setSessionId(sessionId);
        	serverSession = new ServerCCASessionImpl(data, this, (ISessionFactory) sessionFactory, this, this, this);
        }
        serverSession.addStateChangeNotification(this);

        value = serverSession;
      }
      else {
        throw new IllegalArgumentException("Wrong session class!![" + aClass + "]. Supported[" + ClientCCASession.class+","+ServerCCASession.class + "]");
      }
    }
    catch (Exception e) {
      logger.error("Failure to obtain new Accounting Session.", e);
    }

    return value;
  }

  public void stateChanged(Enum oldState, Enum newState) {
    if (logger.isInfoEnabled()) {
      logger.info("Diameter CCA SessionFactory :: stateChanged :: oldState[" + oldState + "], newState[" + newState + "]");
    }
  }

  public long[] getApplicationIds() {
    //FIXME: ???
    return new long[]{4};
  }

  public long getDefaultValidityTime() {
    return this.defaultValidityTime;
  }

  public JCreditControlAnswer createCreditControlAnswer(Answer answer) {
    return new JCreditControlAnswerImpl(answer);
  }

  public JCreditControlRequest createCreditControlRequest(Request req) {
    return new JCreditControlRequestImpl(req);
  }

  public ReAuthAnswer createReAuthAnswer(Answer answer) {
    return new ReAuthAnswerImpl(answer);
  }

  public ReAuthRequest createReAuthRequest(Request req) {
    return new ReAuthRequestImpl(req);
  }

  // /////////////////////
  // // CONTEXT METHODS //
  // /////////////////////

  public void sessionSupervisionTimerReStarted(ServerCCASession session, ScheduledFuture future) {
    // TODO Auto-generated method stub
  }

  public void sessionSupervisionTimerStarted(ServerCCASession session, ScheduledFuture future) {
    // TODO Auto-generated method stub
  }

  public void sessionSupervisionTimerStopped(ServerCCASession session, ScheduledFuture future) {
    // TODO Auto-generated method stub
  }

  public void timeoutExpired(Request request) {
    //FIXME ???
  }

  public void denyAccessOnDeliverFailure(ClientCCASession clientCCASessionImpl, Message request) {
    // TODO Auto-generated method stub
  }

  public void denyAccessOnFailureMessage(ClientCCASession clientCCASessionImpl) {
    // TODO Auto-generated method stub
  }

  public int getDefaultCCFHValue() {
    return defaultCreditControlFailureHandling;
  }

  public int getDefaultDDFHValue() {
    return defaultDirectDebitingFailureHandling;
  }

  public long getDefaultTxTimerValue() {
    return defaultTxTimerValue;
  }

  public void grantAccessOnDeliverFailure(ClientCCASession clientCCASessionImpl, Message request) {
    // TODO Auto-generated method stub
  }

  public void grantAccessOnFailureMessage(ClientCCASession clientCCASessionImpl) {
    // TODO Auto-generated method stub
  }

  public void grantAccessOnTxExpire(ClientCCASession clientCCASessionImpl) {
    // TODO Auto-generated method stub
  }

  public void indicateServiceError(ClientCCASession clientCCASessionImpl) {
    // TODO Auto-generated method stub
  }

}
