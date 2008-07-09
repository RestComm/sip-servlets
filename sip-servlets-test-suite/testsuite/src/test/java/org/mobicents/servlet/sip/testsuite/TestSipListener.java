/*
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
package org.mobicents.servlet.sip.testsuite;

import gov.nist.javax.sip.header.SIPETag;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.TransactionDoesNotExistException;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.EventHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.SIPETagHeader;
import javax.sip.header.SIPIfMatchHeader;
import javax.sip.header.SubscriptionStateHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

/**
 * This class is a UAC template. Shootist is the guy that shoots and shootme is
 * the guy that gets shot.
 * 
 * @author M. Ranganathan
 */

public class TestSipListener implements SipListener {
	private static final String TO_TAG = "5432";

	private static final String PLAIN_UTF8_CONTENT_SUBTYPE = "plain;charset=UTF-8";

	private static final String TEXT_CONTENT_TYPE = "text";
	
	private static final String PIDF_XML_SUBTYPE = "pidf+xml";

	private static final String APPLICATION_CONTENT_TYPE = "application";

	private boolean sendBye;
	
	private SipProvider sipProvider;

	private ProtocolObjects protocolObjects;

	private ContactHeader contactHeader;

	private ListeningPoint listeningPoint;

	private ClientTransaction inviteClientTid;
	
	private ServerTransaction inviteServerTid;

	private Dialog dialog;

	public int myPort;

	private int peerPort;

	private String peerHostPort;

	private int dialogTerminatedCount;

	private int transctionTerminatedCount;

	private int transactionCount;

	private int dialogCount;

	private boolean cancelReceived;
	
	private boolean cancelOkReceived;
	
	private boolean ackSent;
	
	private boolean ackReceived;
	
	private boolean requestTerminatedReceived;
	
	private boolean byeReceived;

	private boolean redirectReceived;

	private boolean okToByeReceived;
	
	private URI requestURI;
	
	private Request inviteRequest;
	
	private boolean cancelSent;

	private boolean waitForCancel;
	
	private String lastMessageContent;
	
	private List<String> allMessagesContent;
	
	private List<String> allSubscriptionStates;

	private boolean finalResponseReceived;
	
	private int finalResponseToSend;
	
	public int ackCount = 0;
	
	public int notifyCount = 0;
	
	private List<Integer> provisionalResponsesToSend;

	private boolean useToURIasRequestUri;

	private boolean sendUpdateOn180;

	private String publishEvent = "reg";
	
	private String sipETag;

	private String publishContentMessage;
	
	private static Logger logger = Logger.getLogger(TestSipListener.class);

	class MyEventSource implements Runnable {
		private TestSipListener notifier;
		private EventHeader eventHeader;

		public MyEventSource(TestSipListener notifier, EventHeader eventHeader ) {
			this.notifier = notifier;
			this.eventHeader = eventHeader;
		}

		public void run() {
			try {
				for (int i = 0; i < 1; i++) {

					Thread.sleep(1000);
					Request request = this.notifier.dialog.createRequest(Request.NOTIFY);
					SubscriptionStateHeader subscriptionState = protocolObjects.headerFactory
							.createSubscriptionStateHeader(SubscriptionStateHeader.ACTIVE);
					request.addHeader(subscriptionState);
					request.addHeader(eventHeader);
										
					allSubscriptionStates.add(subscriptionState.getState().toLowerCase());
					// Lets mark our Contact
//					((SipURI)dialog.getLocalParty().getURI()).setParameter("id","not2");
					
					ClientTransaction ct = sipProvider.getNewClientTransaction(request);
					logger.info("NOTIFY Branch ID " +
						((ViaHeader)request.getHeader(ViaHeader.NAME)).getParameter("branch"));
					this.notifier.dialog.sendRequest(ct);
					logger.info("Dialog " + dialog);
					logger.info("Dialog state after active NOTIFY: " + dialog.getState());
				}
			} catch (Throwable ex) {
				logger.info(ex.getMessage(), ex);
			}
		}
	}

	public void processRequest(RequestEvent requestReceivedEvent) {
		Request request = requestReceivedEvent.getRequest();
		ServerTransaction serverTransactionId = requestReceivedEvent
				.getServerTransaction();

		logger.info("\n\nRequest " + request.getMethod()
				+ " received at " + protocolObjects.sipStack.getStackName()
				+ " with server transaction id " + serverTransactionId);

		if (request.getMethod().equals(Request.INVITE)) {
			processInvite(requestReceivedEvent, serverTransactionId);
		}
				
		if (request.getMethod().equals(Request.BYE)) {
			processBye(request, serverTransactionId);
		}
		
		if (request.getMethod().equals(Request.ACK)) {
			processAck(request, serverTransactionId);
		}

		if (request.getMethod().equals(Request.CANCEL)) {
			processCancel(requestReceivedEvent, serverTransactionId);
		}
		
		if (request.getMethod().equals(Request.MESSAGE)) {
			processMessage(request, serverTransactionId);
		}
		
		if (request.getMethod().equals(Request.NOTIFY)) {
			processNotify(requestReceivedEvent, serverTransactionId);
		}
		
		if (request.getMethod().equals(Request.SUBSCRIBE)) {
			processSubscribe(requestReceivedEvent, serverTransactionId);
		}
		
		if (request.getMethod().equals(Request.UPDATE)) {
			processUpdate(request, serverTransactionId);
		}
		
		if (request.getMethod().equals(Request.PUBLISH)) {
			processPublish(requestReceivedEvent, serverTransactionId);
		}
	}
	
	private void processPublish(RequestEvent requestEvent,
			ServerTransaction serverTransactionId) {
		try {
			SipProvider sipProvider = (SipProvider) requestEvent.getSource();
			Request request = requestEvent.getRequest();
			
			logger.info("shootist:  got a publish . ServerTxId = " + serverTransactionId);
			ServerTransaction st = requestEvent.getServerTransaction();
			if (st == null) {
				st = sipProvider.getNewServerTransaction(request);
			}
			inviteServerTid = st;
			Dialog dialog = st.getDialog();
			
			this.dialogCount ++;
			this.dialog = dialog;
			
			logger.info("Shootme: dialog = " + dialog);
			
			if(request.getRawContent() != null) {
				this.lastMessageContent = new String(request.getRawContent());
				allMessagesContent.add(new String(lastMessageContent));
			}
			SIPIfMatchHeader sipIfMatchHeader = (SIPIfMatchHeader) request.getHeader(SIPIfMatchHeader.NAME);
			boolean sipIfMatchFound = true;
			if(sipIfMatchHeader!= null && sipIfMatchHeader.getETag() != null && !sipIfMatchHeader.getETag().equals(sipETag)) {
				sipIfMatchFound = false;
			}
			if(sipIfMatchFound) {
			
				Response response = protocolObjects.messageFactory.createResponse(
						200, request);
				sipETag = Integer.toString((int) (Math.random()*10000000));
				SIPETagHeader sipTagHeader = protocolObjects.headerFactory.createSIPETagHeader(sipETag);
				response.addHeader(sipTagHeader);
				response.addHeader(request.getHeader(ExpiresHeader.NAME));
				st.sendResponse(response);
				this.transactionCount++;
				logger.info("shootist:  Sending OK.");
			} else {
				Response response = protocolObjects.messageFactory.createResponse(
						500, request);
				serverTransactionId.sendResponse(response);
				this.transactionCount++;
			}
		
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}		
	}

	public void processUpdate(Request request,
			ServerTransaction serverTransactionId) {
		try {
			logger.info("shootist:  got a update. ServerTxId = " + serverTransactionId);
			this.byeReceived  = true;
			if (serverTransactionId == null) {
				logger.info("shootist:  null TID.");
				return;
			}
			
			Dialog dialog = serverTransactionId.getDialog();			
			logger.info("Dialog State = " + dialog.getState());
			Response response = protocolObjects.messageFactory.createResponse(
					200, request);
			serverTransactionId.sendResponse(response);
			this.transactionCount++;
			logger.info("shootist:  Sending OK.");
			logger.info("Dialog State = " + dialog.getState());
		
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	/**
	 * Process the invite request.
	 */
	public void processSubscribe(RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		try {
			logger.info("notifier: got an Subscribe sending OK");
			logger.info("notifier:  " + request);
			logger.info("notifier : dialog = " + requestEvent.getDialog());
			EventHeader eventHeader = (EventHeader) request.getHeader(EventHeader.NAME);
//			this.gotSubscribeRequest = true;
			
			// Always create a ServerTransaction, best as early as possible in the code
			Response response = null;
			ServerTransaction st = requestEvent.getServerTransaction();			
			if (st == null) {
				st = sipProvider.getNewServerTransaction(request);
			}
			
			// Check if it is an initial SUBSCRIBE or a refresh / unsubscribe
			boolean isInitial = requestEvent.getDialog() == null;
			if ( isInitial ) {
				// JvB: need random tags to test forking
				String toTag = Integer.toHexString( (int) (Math.random() * Integer.MAX_VALUE) );
				response = protocolObjects.messageFactory.createResponse(202, request);
				ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
				
				// Sanity check: to header should not ahve a tag. Else the dialog 
				// should have matched
				toHeader.setTag(toTag); // Application is supposed to set.
				
				this.dialog = st.getDialog();
				// subscribe dialogs do not terminate on bye.
				this.dialog.terminateOnBye(false);
			} else {
				response = protocolObjects.messageFactory.createResponse(200, request);
			}

			// Both 2xx response to SUBSCRIBE and NOTIFY need a Contact
			Address address = protocolObjects.addressFactory.createAddress("Notifier <sip:127.0.0.1>");
			((SipURI)address.getURI()).setPort( sipProvider.getListeningPoint(ListeningPoint.UDP).getPort() );				
			ContactHeader contactHeader = protocolObjects.headerFactory.createContactHeader(address);			
			response.addHeader(contactHeader);
			
			// Expires header is mandatory in 2xx responses to SUBSCRIBE
			ExpiresHeader expires = (ExpiresHeader) request.getHeader( ExpiresHeader.NAME );
			if (expires==null) {
				expires = protocolObjects.headerFactory.createExpiresHeader(30);	// rather short
			}
			response.addHeader( expires );
			
			/*
			 * JvB: The SUBSCRIBE MUST be answered first. See RFC3265 3.1.6.2: 
			 * "[...] a NOTIFY message is always sent immediately after any 200-
			 * class response to a SUBSCRIBE request"
			 * 
			 *  Do this before creating the NOTIFY request below
			 */
			st.sendResponse(response);
			//Thread.sleep(1000); // Be kind to implementations
						
			/*
			 * NOTIFY requests MUST contain a "Subscription-State" header with a
			 * value of "active", "pending", or "terminated". The "active" value
			 * indicates that the subscription has been accepted and has been
			 * authorized (in most cases; see section 5.2.). The "pending" value
			 * indicates that the subscription has been received, but that
			 * policy information is insufficient to accept or deny the
			 * subscription at this time. The "terminated" value indicates that
			 * the subscription is not active.
			 */
		
			Request notifyRequest = dialog.createRequest( "NOTIFY" );
			
			
			// Mark the contact header, to check that the remote contact is updated
//			((SipURI)contactHeader.getAddress().getURI()).setParameter("id","not");
			
			// Initial state is pending, second time we assume terminated (Expires==0)		
			SubscriptionStateHeader sstate = protocolObjects.headerFactory.createSubscriptionStateHeader(
					expires.getExpires() != 0 ? SubscriptionStateHeader.PENDING : SubscriptionStateHeader.TERMINATED );
			allSubscriptionStates.add(sstate.getState().toLowerCase());
			
			
			// Need a reason for terminated
			if ( sstate.getState().equalsIgnoreCase("terminated") ) {
				sstate.setReasonCode( "deactivated" );
			}
			
			notifyRequest.addHeader(sstate);
			notifyRequest.setHeader(eventHeader);
			notifyRequest.setHeader(contactHeader);
			// notifyRequest.setHeader(routeHeader);
			ClientTransaction ct = sipProvider.getNewClientTransaction(notifyRequest);

			// Let the other side know that the tx is pending acceptance
			//
			dialog.sendRequest(ct);
			logger.info("NOTIFY Branch ID " +
				((ViaHeader)request.getHeader(ViaHeader.NAME)).getParameter("branch"));
			logger.info("Dialog " + dialog);
			logger.info("Dialog state after pending NOTIFY: " + dialog.getState());
			
			if (expires.getExpires() != 0) {
				Thread myEventSource = new Thread(new MyEventSource(this,eventHeader));
				myEventSource.start();
			}
		} catch (Throwable ex) {
			logger.info(ex.getMessage(), ex);
		}
	}
	
	public void processNotify(RequestEvent requestEvent,
			ServerTransaction serverTransactionId) {
		SipProvider provider = (SipProvider) requestEvent.getSource();
		Request notify = requestEvent.getRequest();
		try {
			logger.info("subscriber:  got a notify count  " + this.notifyCount++ );
			if (serverTransactionId == null) {
				logger.info("subscriber:  null TID.");
				serverTransactionId = provider.getNewServerTransaction(notify);
			}
			Dialog dialog = serverTransactionId.getDialog();
//			if ( dialog != subscriberDialog ) {
//				if (forkedDialog == null) {
//					forkedDialog = dialog;
//				} else  {
//					AbstractSubsnotifyTestCase.assertTrue("Dialog should be either the subscriber dialog ", 
//							forkedDialog  == dialog);
//				}
//			}
//			
//			this.dialogs.add(dialog);
			logger.info("Dialog State = " + dialog.getState());
			
			Response response = protocolObjects.messageFactory.createResponse(200, notify);
			// SHOULD add a Contact
			ContactHeader contact = (ContactHeader) contactHeader.clone();
			((SipURI)contact.getAddress().getURI()).setParameter( "id", "sub" );
			response.addHeader( contact );
			logger.info("Transaction State = " + serverTransactionId.getState());
			serverTransactionId.sendResponse(response);
			logger.info("Dialog State = " + dialog.getState());
			SubscriptionStateHeader subscriptionState = (SubscriptionStateHeader) notify
					.getHeader(SubscriptionStateHeader.NAME);

			// Subscription is terminated?
			String state = subscriptionState.getState();
			allSubscriptionStates.add(state.toLowerCase());
			if (state.equalsIgnoreCase(SubscriptionStateHeader.TERMINATED)) {
				dialog.delete();
			} else if (state.equalsIgnoreCase(SubscriptionStateHeader.ACTIVE)) {
				if("reg".equalsIgnoreCase(((EventHeader)notify.getHeader(EventHeader.NAME)).getEventType())) {
					logger.info("Subscriber: sending unSUBSCRIBE");
					
					// Else we end it ourselves
					Request unsubscribe = dialog.createRequest(Request.SUBSCRIBE);
					
					logger.info( "dialog created:" + unsubscribe );
					// SHOULD add a Contact (done by dialog), lets mark it to test updates
	//				((SipURI) dialog.getLocalParty().getURI()).setParameter( "id", "unsub" );
					ExpiresHeader expires = protocolObjects.headerFactory.createExpiresHeader(0);
					unsubscribe.addHeader(expires);
					// JvB note : stack should do this!
					unsubscribe.addHeader(notify.getHeader(EventHeader.NAME)); // copy
												// event
												// header
					logger.info("Sending Unsubscribe : " + unsubscribe);
					logger.info("unsubscribe dialog  " + dialog);
					ClientTransaction ct = sipProvider.getNewClientTransaction(unsubscribe);
					dialog.sendRequest(ct);
				}
			} else {
				logger.info("Subscriber: state now " + state);// pending
			}

		} catch (Exception ex) {
			logger.error("Unexpected exception",ex);
		}
	}
	
	private void processMessage(Request request,
			ServerTransaction serverTransactionId) {
		
		ServerTransaction serverTransaction = null;

        try {

            serverTransaction = 
            	(serverTransactionId == null? 
            			sipProvider.getNewServerTransaction(request): 
            				serverTransactionId);
        } catch (javax.sip.TransactionAlreadyExistsException ex) {
            ex.printStackTrace();
            return;
        } catch (javax.sip.TransactionUnavailableException ex1) {
            ex1.printStackTrace();
            return;
        }
		
		ContentTypeHeader contentTypeHeader = (ContentTypeHeader) 
		request.getHeader(ContentTypeHeader.NAME);		
		if(TEXT_CONTENT_TYPE.equals(contentTypeHeader.getContentType())) {
			this.lastMessageContent = new String(request.getRawContent());
			allMessagesContent.add(new String(lastMessageContent));
		}
		try {
			Response okResponse = protocolObjects.messageFactory.createResponse(
					Response.OK, request);			
			ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
			if (toHeader.getTag() == null) {
				toHeader.setTag(Integer.toString((int) (Math.random()*10000000)));
			}
//			okResponse.addHeader(contactHeader);
			serverTransaction.sendResponse(okResponse);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("error sending OK response to message", ex);
		}		
	}

	private void processCancel(RequestEvent requestEvent,
			ServerTransaction serverTransactionId) {
		try {
			cancelReceived = true;
			SipProvider sipProvider = (SipProvider) requestEvent.getSource();
			Request request = requestEvent.getRequest();
			Response response = protocolObjects.messageFactory.createResponse(
					Response.OK, request);
			ServerTransaction st = requestEvent.getServerTransaction();
	
			if (st == null) {
				st = sipProvider.getNewServerTransaction(request);
			}
			Dialog dialog = st.getDialog();		
			logger.info("Shootme: dialog = " + dialog);		
			st.sendResponse(response);
			
			response = protocolObjects.messageFactory.createResponse(
					Response.REQUEST_TERMINATED, inviteRequest);
			inviteServerTid.sendResponse(response);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("error sending CANCEL responses", ex);
		}
	}

	/**
	 * Process the invite request.
	 */
	public void processInvite(RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		logger.info("shootme: got an Invite " + request);	
		try {
			ServerTransaction st = requestEvent.getServerTransaction();			
			if (st == null) {
				st = sipProvider.getNewServerTransaction(request);
			}
			inviteServerTid = st;
			Dialog dialog = st.getDialog();
			
			this.dialogCount ++;
			this.dialog = dialog;
			
			logger.info("Shootme: dialog = " + dialog);
			
			this.inviteRequest = request;
				
			for (int provisionalResponseToSend : provisionalResponsesToSend) {
				Response response = protocolObjects.messageFactory.createResponse(
						provisionalResponseToSend, request);
				if(provisionalResponseToSend >= Response.TRYING && provisionalResponseToSend < Response.OK) {
					ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
					if(provisionalResponseToSend != Response.TRYING && toHeader.getTag() == null) {
						toHeader.setTag(TO_TAG); // Application is supposed to set.
					}
					st.sendResponse(response);
					Thread.sleep(1000);
				}
			}								
						
			if(!waitForCancel) {
				Address address = protocolObjects.addressFactory
				.createAddress("Shootme <sip:127.0.0.1:" + myPort
						+";transport="+protocolObjects.transport
						+ ">");
				ContactHeader contactHeader = protocolObjects.headerFactory.createContactHeader(address);						
				Response finalResponse = protocolObjects.messageFactory
						.createResponse(finalResponseToSend, request);
				ToHeader toHeader = (ToHeader) finalResponse.getHeader(ToHeader.NAME);
				if(toHeader.getTag() == null) {
					toHeader.setTag(TO_TAG); // Application is supposed to set.
				}
				finalResponse.addHeader(contactHeader);				
				st.sendResponse(finalResponse);
			} else {
				logger.info("Waiting for CANCEL, stopping the INVITE processing ");
				return ;
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void processBye(Request request,
			ServerTransaction serverTransactionId) {
		try {
			logger.info("shootist:  got a bye . ServerTxId = " + serverTransactionId);
			this.byeReceived  = true;
			if (serverTransactionId == null) {
				logger.info("shootist:  null TID.");
				return;
			}
			
			Dialog dialog = serverTransactionId.getDialog();			
			logger.info("Dialog State = " + dialog.getState());
			Response response = protocolObjects.messageFactory.createResponse(
					200, request);
			serverTransactionId.sendResponse(response);
			this.transactionCount++;
			logger.info("shootist:  Sending OK.");
			logger.info("Dialog State = " + dialog.getState());
		
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	public void processAck(Request request,
			ServerTransaction serverTransactionId) {
		try {
			logger.info("shootist:  got a " + request);
			logger.info("shootist:  got an ACK. ServerTxId = " + serverTransactionId);
			ackReceived = true;
			ackCount ++;
			if(sendBye) {											
				Thread.sleep(1000);
				Request byeRequest = serverTransactionId.getDialog().createRequest(Request.BYE);
				ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
				logger.info("Sending BYE : " + byeRequest);
				serverTransactionId.getDialog().sendRequest(ct);
				logger.info("Dialog State = " + serverTransactionId.getDialog().getState());
			}							
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}
	public void processResponse(ResponseEvent responseReceivedEvent) {
		logger.info("Got a response");
		Response response = (Response) responseReceivedEvent.getResponse();
		ClientTransaction tid = responseReceivedEvent.getClientTransaction();
		CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

		logger.info("Response received : Status Code = "
				+ response.getStatusCode() + " " + cseq);
		if (tid == null) {
			logger.info("Stray response -- dropping ");
			return;
		}
		logger.info("transaction state is " + tid.getState());
		logger.info("Dialog = " + tid.getDialog());
		logger.info("Dialog State is " + tid.getDialog().getState());

		try {			
			if(response.getStatusCode() >= 200 && response.getStatusCode() < 700) {
				finalResponseReceived = true;
			}
			if (response.getStatusCode() == Response.OK) {
				logger.info("response = " + response);
				if (cseq.getMethod().equals(Request.INVITE)) {
					Request ackRequest = dialog.createAck(cseq.getSeqNumber());
					if (useToURIasRequestUri) {
						ackRequest.setRequestURI(requestURI);	
					}
					logger.info("Sending ACK");
					dialog.sendAck(ackRequest);
					ackSent = true;
					Thread.sleep(1000);
					// If the caller is supposed to send the bye
					if(sendBye) {
						sendBye();
					}
				} else if(cseq.getMethod().equals(Request.BYE)) {
					okToByeReceived = true;
				} else if (cseq.getMethod().equals(Request.CANCEL)) {
					this.cancelOkReceived = true;
					if (dialog.getState() == DialogState.CONFIRMED) {
						// oops cancel went in too late. Need to hang up the
						// dialog.
						logger.info("Sending BYE -- cancel went in too late !!");
						Request byeRequest = dialog.createRequest(Request.BYE);
						ClientTransaction ct = sipProvider
								.getNewClientTransaction(byeRequest);
						dialog.sendRequest(ct);
					} 
				} else if (cseq.getMethod().equals(Request.PUBLISH)) {
					SIPETagHeader sipTagHeader = (SIPETagHeader)response.getHeader(SIPETag.NAME);
					sipETag = sipTagHeader.getETag();
				}
			} else if  (response.getStatusCode() == Response.MOVED_TEMPORARILY) {
				// Dialog dies as soon as you get an error response.
				this.redirectReceived = true;
				if (cseq.getMethod().equals(Request.INVITE)) {
					// lookup the contact header
					ContactHeader contHdr = (ContactHeader) response
							.getHeader(ContactHeader.NAME);
					// we can re-use the from header
					FromHeader from = ((FromHeader) response
							.getHeader(FromHeader.NAME));
					// we use the to-address, but without the tag
					ToHeader to = (ToHeader) (response.getHeader(ToHeader.NAME)).clone();
					to.removeParameter("tag");
					// the call-id can be re-used
					CallIdHeader callID = ((CallIdHeader) response
							.getHeader(CallIdHeader.NAME));
					// we take the next cseq
					long seqNo = (((CSeqHeader) response
							.getHeader(CSeqHeader.NAME)).getSeqNumber());
					logger.info("seqNo = " + seqNo);
					CSeqHeader cseqNew = protocolObjects.headerFactory
							.createCSeqHeader(++seqNo, "INVITE");
					// Create ViaHeaders (either use tcp or udp)
					ArrayList viaHeaders = new ArrayList();
					ViaHeader viaHeader = protocolObjects.headerFactory
							.createViaHeader("127.0.0.1", sipProvider
									.getListeningPoint(protocolObjects.transport).getPort(), 
									protocolObjects.transport,
									null);
					// add via headers
					viaHeaders.add(viaHeader);
					// create max forwards
					MaxForwardsHeader maxForwardsHeader = protocolObjects.headerFactory
							.createMaxForwardsHeader(10);
					// create invite Request
					SipURI newUri = (SipURI)this.requestURI.clone();					
					newUri.setParameter("redirection", "true");
					requestURI = newUri;
					Request invRequest = protocolObjects.messageFactory
							.createRequest(newUri,
									"INVITE", callID, cseqNew, from, to,
									viaHeaders, maxForwardsHeader);
					// we set the Request URI to the address given
					SipURI contactURI = 
					protocolObjects.addressFactory.createSipURI(null, this.listeningPoint.getIPAddress());
					
					contactURI.setPort(this.listeningPoint.getPort());
					contactURI.setTransportParam(protocolObjects.transport);
					
					Address address = protocolObjects.addressFactory.createAddress(contactURI);
					ContactHeader contact = protocolObjects.headerFactory.createContactHeader(address);
					invRequest.addHeader(contact);
					
					// the contacat header in the response contains where to redirect
					// the request to -- which in this case happens to be back to the
					// same location.
					ContactHeader chdr = (ContactHeader)response.getHeader(ContactHeader.NAME);
					
					SipURI sipUri = (SipURI)chdr.getAddress().getURI();
//					sipUri.setLrParam();
					RouteHeader routeHeader = 
						protocolObjects.headerFactory.createRouteHeader(chdr.getAddress());
					invRequest.addHeader(routeHeader);
					invRequest.setRequestURI(sipUri);
					
					logger.info("Sending INVITE to "
							+ contHdr.getAddress().getURI().toString());
					inviteClientTid = sipProvider.getNewClientTransaction(invRequest);
					this.transactionCount++;
					
					logger.info("New TID = " + inviteClientTid);
					inviteClientTid.sendRequest();
					logger.info("sendReqeust succeeded " + inviteClientTid);
					Dialog dialog = inviteClientTid.getDialog();
					this.dialogCount ++;
					this.dialog = dialog;
				
		
				}
			} else if (response.getStatusCode() == Response.REQUEST_TERMINATED) {
				if(cseq.getMethod().equals(Request.INVITE)){
					this.requestTerminatedReceived = true;
				}
			} else if(response.getStatusCode() == Response.RINGING && sendUpdateOn180) {
				Request updateRequest = dialog.createRequest(Request.UPDATE);
				ClientTransaction ct = sipProvider
						.getNewClientTransaction(updateRequest);
				dialog.sendRequest(ct);
			}
			/**
			 * end of modified code
			 */
		} catch (Exception ex) {
			logger.error("An unexpected exception occured while processing the response" , ex);
		}

	}

	/**
	 * @throws SipException
	 * @throws TransactionUnavailableException
	 * @throws TransactionDoesNotExistException
	 */
	public void sendBye() throws SipException,
			TransactionUnavailableException, TransactionDoesNotExistException {
		Request byeRequest = this.dialog.createRequest(Request.BYE);
		ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
		dialog.sendRequest(ct);
	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

		logger.info("Transaction Time out");
	}

	

	public SipProvider createProvider() throws Exception {
		logger.info("Shootist: createProvider()");
		listeningPoint = protocolObjects.sipStack.createListeningPoint(
				"127.0.0.1", myPort, protocolObjects.transport);
		this.sipProvider = protocolObjects.sipStack
				.createSipProvider(listeningPoint);
		return sipProvider;

	}

	public void sendSipRequest(String method, URI fromURI, URI toURI, String messageContent, SipURI route, boolean useToURIasRequestUri) throws SipException, ParseException, InvalidArgumentException {
		this.useToURIasRequestUri = useToURIasRequestUri;
		// create >From Header
		Address fromNameAddress = protocolObjects.addressFactory
				.createAddress(fromURI);			
		FromHeader fromHeader = protocolObjects.headerFactory
				.createFromHeader(fromNameAddress, Integer.toString((int) (Math.random()*10000000)));

		// create To Header			
		Address toNameAddress = protocolObjects.addressFactory
				.createAddress(toURI);			
		ToHeader toHeader = protocolObjects.headerFactory.createToHeader(
				toNameAddress, null);

		if(toURI instanceof SipURI) {
			SipURI toSipUri = (SipURI) toURI;
			// create Request URI
			this.requestURI = protocolObjects.addressFactory.createSipURI(
					toSipUri.getUser(), peerHostPort);
			((SipURI)this.requestURI).setPort(peerPort);
		}
		if(useToURIasRequestUri) {
			this.requestURI = toURI;
		}
		
		// Create ViaHeaders

		List<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
		ViaHeader viaHeader = protocolObjects.headerFactory
				.createViaHeader("127.0.0.1", sipProvider
						.getListeningPoint(protocolObjects.transport).getPort(), protocolObjects.transport,
						null);

		// add via headers
		viaHeaders.add(viaHeader);

		// Create ContentTypeHeader
//			ContentTypeHeader contentTypeHeader = protocolObjects.headerFactory
//					.createContentTypeHeader("application", "sdp");

		// Create a new CallId header
		CallIdHeader callIdHeader = sipProvider.getNewCallId();

		// Create a new Cseq header
		CSeqHeader cSeqHeader = protocolObjects.headerFactory
				.createCSeqHeader(1L, method);

		// Create a new MaxForwardsHeader
		MaxForwardsHeader maxForwards = protocolObjects.headerFactory
				.createMaxForwardsHeader(70);

		// Create the request.
		Request request = protocolObjects.messageFactory.createRequest(
				requestURI, method, callIdHeader, cSeqHeader,
				fromHeader, toHeader, viaHeaders, maxForwards);
		// Create contact headers
		String host = "127.0.0.1";

		URI contactUrl = null;
		if(fromURI instanceof SipURI) {
			contactUrl = protocolObjects.addressFactory.createSipURI(
				((SipURI)fromURI).getUser(), host);
			/**
			 * either use tcp or udp
			 */
			((SipURI)contactUrl).setPort(listeningPoint.getPort());
//				contactUrl.setTransportParam(protocolObjects.transport);		
			((SipURI)contactUrl).setLrParam();
		} else {
			contactUrl = fromURI;
		}
		
		// Create the contact name address.	
		Address contactAddress = protocolObjects.addressFactory
				.createAddress(contactUrl);

		// Add the contact address.
//			contactAddress.setDisplayName(fromName);

		contactHeader = protocolObjects.headerFactory
				.createContactHeader(contactAddress);
		request.addHeader(contactHeader);
		
		SipURI uri = protocolObjects.addressFactory.createSipURI(null, "127.0.0.1");
		
		uri.setLrParam();
		uri.setTransportParam(protocolObjects.transport);
		uri.setPort(this.peerPort);
		
		if(route != null) {
			Address address = protocolObjects.addressFactory.createAddress(route);
			RouteHeader routeHeader = protocolObjects.headerFactory.createRouteHeader(address);
			request.addHeader(routeHeader);
		} else {
			Address address = protocolObjects.addressFactory.createAddress(uri);
			RouteHeader routeHeader = protocolObjects.headerFactory.createRouteHeader(address);
			request.addHeader(routeHeader);
		}

		// set the message content
		if(messageContent != null) {
			ContentLengthHeader contentLengthHeader = 
				protocolObjects.headerFactory.createContentLengthHeader(messageContent.length());
			ContentTypeHeader contentTypeHeader = 
				protocolObjects.headerFactory.createContentTypeHeader(TEXT_CONTENT_TYPE,PLAIN_UTF8_CONTENT_SUBTYPE);
			byte[] contents = messageContent.getBytes();
			request.setContent(contents, contentTypeHeader);
			request.setContentLength(contentLengthHeader);
		}
		if(Request.SUBSCRIBE.equals(method) || Request.PUBLISH.equals(method)) {
			// Create an event header for the subscription.
			EventHeader eventHeader = protocolObjects.headerFactory.createEventHeader(publishEvent);				
			request.addHeader(eventHeader);
			ExpiresHeader expires = protocolObjects.headerFactory.createExpiresHeader(200);
			request.addHeader(expires);			
		}
		if(Request.PUBLISH.equals(method)) {
			if(sipETag != null) {
				SIPIfMatchHeader sipIfMatchHeader = protocolObjects.headerFactory.createSIPIfMatchHeader(sipETag);
				request.addHeader(sipIfMatchHeader);
			}
			if(publishContentMessage != null) {
				ContentLengthHeader contentLengthHeader = 
					protocolObjects.headerFactory.createContentLengthHeader(publishContentMessage.length());
				ContentTypeHeader contentTypeHeader = 
					protocolObjects.headerFactory.createContentTypeHeader(APPLICATION_CONTENT_TYPE,PIDF_XML_SUBTYPE);
				request.setContentLength(contentLengthHeader);
				request.setContent(publishContentMessage, contentTypeHeader);
			}
		}
		// Create the client transaction.
		inviteClientTid = sipProvider.getNewClientTransaction(request);
		// send the request out.
		inviteClientTid.sendRequest();

		this.transactionCount ++;
		
		logger.info("client tx = " + inviteClientTid);
		dialog = inviteClientTid.getDialog();
		this.dialogCount++;
	}
	
	public TestSipListener (int myPort, int peerPort, ProtocolObjects protocolObjects, boolean callerSendBye) {
		this.protocolObjects = protocolObjects;		
		this.myPort = myPort;
		if(peerPort > 0) {
			this.peerPort = peerPort;
			this.peerHostPort = "127.0.0.1:"+ peerPort;
		}
		this.sendBye = callerSendBye;
		allMessagesContent = new ArrayList<String>();
		allSubscriptionStates = new ArrayList<String>();
		finalResponseToSend = Response.OK;
		provisionalResponsesToSend = new ArrayList<Integer>();
		provisionalResponsesToSend.add(Response.TRYING);
		provisionalResponsesToSend.add(Response.RINGING);
	}

	public void processIOException(IOExceptionEvent exceptionEvent) {
		logger.info("IOException happened for "
				+ exceptionEvent.getHost() + " port = "
				+ exceptionEvent.getPort());

	}

	public void processTransactionTerminated(
			TransactionTerminatedEvent transactionTerminatedEvent) {
		logger.info("Transaction terminated event recieved for " + 
				transactionTerminatedEvent.getClientTransaction());
		this.transctionTerminatedCount++;
	}

	public void processDialogTerminated(
			DialogTerminatedEvent dialogTerminatedEvent) {
		this.dialogTerminatedCount++;

	}

	public boolean getOkToByeReceived() {
		return okToByeReceived;
	}
	
	public boolean getByeReceived() {
		return byeReceived;
	}

	public void sendCancel() {
		try {
			logger.info("Sending cancel");
			
			Request cancelRequest = inviteClientTid.createCancel();
			ClientTransaction cancelTid = sipProvider
					.getNewClientTransaction(cancelRequest);
			cancelTid.sendRequest();
			cancelSent = true;
		} catch (Exception ex) {
			ex.printStackTrace();						
		}
	}

	/**
	 * @return the cancelReceived
	 */
	public boolean isCancelReceived() {
		return cancelReceived;
	}

	/**
	 * @return the cancelOkReceived
	 */
	public boolean isCancelOkReceived() {
		return cancelOkReceived;
	}

	/**
	 * @return the requestTerminatedReceived
	 */
	public boolean isRequestTerminatedReceived() {
		return requestTerminatedReceived;
	}

	/**
	 * @return the waitForCancel
	 */
	public boolean isWaitForCancel() {
		return waitForCancel;
	}

	/**
	 * @param waitForCancel the waitForCancel to set
	 */
	public void setWaitForCancel(boolean waitForCancel) {
		this.waitForCancel = waitForCancel;
	}

	/**
	 * @return the ackSent
	 */
	public boolean isAckSent() {
		return ackSent;
	}

	/**
	 * @return the ackReceived
	 */
	public boolean isAckReceived() {
		return ackReceived;
	}

	/**
	 * 
	 * @param messageToSend
	 * @throws SipException
	 * @throws InvalidArgumentException
	 * @throws ParseException
	 */ 
	public void sendMessageInDialog(String messageToSend) throws SipException, InvalidArgumentException, ParseException {		
		Request message = dialog.createRequest(Request.MESSAGE);
		ContentLengthHeader contentLengthHeader = 
			protocolObjects.headerFactory.createContentLengthHeader(messageToSend.length());
		ContentTypeHeader contentTypeHeader = 
			protocolObjects.headerFactory.createContentTypeHeader(TEXT_CONTENT_TYPE,PLAIN_UTF8_CONTENT_SUBTYPE);
		message.setContentLength(contentLengthHeader);
		message.setContent(messageToSend, contentTypeHeader);
		ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(message);
		dialog.sendRequest(clientTransaction);
	}
	
	/**
	 * 
	 * @param messageToSend
	 * @throws SipException
	 * @throws InvalidArgumentException
	 * @throws ParseException
	 */ 
	public void sendMessageNoDialog(String messageToSend) throws SipException, InvalidArgumentException, ParseException {		
		Request message = dialog.createRequest(Request.MESSAGE);
		ContentLengthHeader contentLengthHeader = 
			protocolObjects.headerFactory.createContentLengthHeader(messageToSend.length());
		ContentTypeHeader contentTypeHeader = 
			protocolObjects.headerFactory.createContentTypeHeader(TEXT_CONTENT_TYPE,PLAIN_UTF8_CONTENT_SUBTYPE);
		message.setContentLength(contentLengthHeader);
		message.setContent(messageToSend, contentTypeHeader);
		ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(message);
		dialog.sendRequest(clientTransaction);
	}

	/**
	 * @return the lastMessageContent
	 */
	public String getLastMessageContent() {
		String content = null;
		if(lastMessageContent != null) {
			content = new String(lastMessageContent);
			lastMessageContent = null;
		}
		return content;
	}

	/**
	 * @return the allMessagesContent
	 */
	public List<String> getAllMessagesContent() {
		return allMessagesContent;
	}

	/**
	 * @return the finalResponseReceived
	 */
	public boolean isFinalResponseReceived() {
		return finalResponseReceived;
	}

	/**
	 * @return the finalResponseToSend
	 */
	public int getFinalResponseToSend() {
		return finalResponseToSend;
	}

	/**
	 * @param finalResponseToSend the finalResponseToSend to set
	 */
	public void setFinalResponseToSend(int finalResponseToSend) {
		this.finalResponseToSend = finalResponseToSend;
	}

	/**
	 * @return the provisionalResponsesToSend
	 */
	public List<Integer> getProvisionalResponsesToSend() {
		return provisionalResponsesToSend;
	}

	/**
	 * @param provisionalResponsesToSend the provisionalResponsesToSend to set
	 */
	public void setProvisionalResponsesToSend(
			List<Integer> provisionalResponsesToSend) {
		this.provisionalResponsesToSend = provisionalResponsesToSend;
	}

	/**
	 * @return the allSubscriptionState
	 */
	public List<String> getAllSubscriptionState() {
		return allSubscriptionStates;
	}

	/**
	 * @param byeReceived the byeReceived to set
	 */
	public void setByeReceived(boolean byeReceived) {
		this.byeReceived = byeReceived;
	}

	/**
	 * @param okToByeReceived the okToByeReceived to set
	 */
	public void setOkToByeReceived(boolean okToByeReceived) {
		this.okToByeReceived = okToByeReceived;
	}

	public void setSendUpdateOn180(boolean sendUpdateOn180) {
		this.sendUpdateOn180 = sendUpdateOn180;
	}

	public void setPublishEvent(String publishEvent) {
		this.publishEvent  = publishEvent;
	}

	public void setPublishContentMessage(String publishContentMessage) {
		this.publishContentMessage = publishContentMessage;		
	}	
}
