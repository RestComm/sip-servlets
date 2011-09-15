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

package org.mobicents.servlet.sip.testsuite.proxy;
import gov.nist.javax.sip.ClientTransactionExt;
import gov.nist.javax.sip.ResponseEventExt;
import gov.nist.javax.sip.SipStackImpl;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogState;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.header.RequireHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;


public class Shootist implements SipListener {
	private static transient Logger logger = Logger.getLogger(Shootist.class);
	
	private static SipProvider sipProvider;

	private static AddressFactory addressFactory;

	private static MessageFactory messageFactory;

	private static HeaderFactory headerFactory;

	private SipStack sipStack;

	private ContactHeader contactHeader;

	private ListeningPoint listeningPoint;

	private ClientTransaction inviteTid;

	private Dialog dialog;

	private boolean byeTaskRunning;
	
	public boolean ended = false;
	
	public boolean usePrack = false;
	
	public boolean prackOkReceived = false;
	
	public boolean okToInviteRecevied = false;
	
	public int pauseBeforeBye = 10000;
	public int pauseBeforeAck = 0;
	
	int count = 0;
	
	private boolean forkingProxy = false;
	
	private boolean outboundProxy = true;
	
	public String requestMethod;
	
	public Request request;
	
	public Response lastResponse;
	
	private static final String TEXT_CONTENT_TYPE = "text";
	
	private String lastMessageContent;
	
	private List<String> allMessagesContent = new ArrayList<String>();
	
	private int numberOfTryingReceived = 0;
	
	private boolean sendCancelOn180;

	private String remotePort = "5070";
	
	private String fromHost;
	
	// Save the created ACK request, to respond to retransmitted 2xx
    private Request ackRequest;

	private boolean forkedResponseReceived;

	private boolean isRequestTerminatedReceived;

	public boolean moveRouteParamsToRequestURI;
	

	class ByeTask  extends TimerTask {
		Dialog dialog;
		public ByeTask(Dialog dialog)  {
			this.dialog = dialog;
		}
		public void run () {
			try {
			   Request byeRequest = this.dialog.createRequest(Request.BYE);
			   if(moveRouteParamsToRequestURI) {
				   RouteHeader routeHeader = (RouteHeader) byeRequest.getHeader(RouteHeader.NAME);
				   SipURI sipURI = (SipURI) routeHeader.getAddress().getURI();
				   Iterator<String> parameterNames = sipURI.getParameterNames();
				   while (parameterNames.hasNext()) {
					   String parameterName = parameterNames.next();					
					   if(!parameterName.equals("transport") && !parameterName.equals("lr")) {
						   ((SipURI)byeRequest.getRequestURI()).setParameter(parameterName, sipURI.getParameter(parameterName));
						   sipURI.removeParameter(parameterName);
						   parameterNames = sipURI.getParameterNames();
					   }
				   }
				   ((SipURI)byeRequest.getRequestURI()).setPort(Integer.parseInt(remotePort));
			   }			  
			   System.out.println("Sending BYE " + byeRequest);
			   ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);			   
			   dialog.sendRequest(ct);
			} catch (Exception ex) {
				ex.printStackTrace();				
			}

		}

	}

	private static final String usageString = "java "
			+ "examples.shootist.Shootist \n"
			+ ">>>> is your class path set to the root?";

	
	public Shootist(boolean forkingProxy, String remotePort) {
		this.forkingProxy = forkingProxy;
		if(remotePort != null) {
			this.remotePort = remotePort;
		}
	}


	private static void usage() {
		System.out.println(usageString);		
	}


	public void processRequest(RequestEvent requestReceivedEvent) {
		Request request = requestReceivedEvent.getRequest();
		ServerTransaction serverTransactionId = requestReceivedEvent
				.getServerTransaction();

		System.out.println("\n\nRequest " + request.getMethod()
				+ " received at " + sipStack.getStackName()
				+ " with server transaction id " + serverTransactionId);

		// We are the UAC so the only request we get is the BYE.
		if (request.getMethod().equals(Request.BYE)) {
			processBye(request, serverTransactionId);
		} else if (request.getMethod().equals(Request.MESSAGE)) {
			processMessage(request, serverTransactionId);
		}
		else {
			try {
				serverTransactionId.sendResponse( messageFactory.createResponse(202,request) );
			} catch (SipException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		if(contentTypeHeader != null) {
			this.lastMessageContent = new String(request.getRawContent());
			allMessagesContent.add(new String(lastMessageContent));				
		} 
		try {
			Response okResponse = messageFactory.createResponse(
					Response.OK, request);			
			ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
			if (toHeader.getTag() == null) {
				toHeader.setTag(Integer.toString(new Random().nextInt(10000000)));
			}
//			okResponse.addHeader(contactHeader);
			serverTransaction.sendResponse(okResponse);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error("error sending OK response to message", ex);
		}			
	}
	
	public void processBye(Request request,
			ServerTransaction serverTransactionId) {
		try {
			System.out.println("shootist:  got a bye .");
			ended = true;
			if (serverTransactionId == null) {
				System.out.println("shootist:  null TID.");
				return;
			}
			Dialog dialog = serverTransactionId.getDialog();
			System.out.println("Dialog State = " + dialog.getState());
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			System.out.println("shootist:  Sending OK.");
			System.out.println("Dialog State = " + dialog.getState());

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void processResponse(ResponseEvent responseReceivedEvent) {		
		ResponseEventExt responseEventExt = (ResponseEventExt) responseReceivedEvent;
		Response response = (Response) responseReceivedEvent.getResponse();
		((SipStackImpl)sipStack).getStackLogger().logInfo("Got a response " + response);
		if(responseEventExt.isForkedResponse()) {
			forkedResponseReceived = true;
		}
		ClientTransaction clientTransaction = responseEventExt.getClientTransaction();		
		final Dialog dialog = responseEventExt.getDialog();
		final boolean isForkedResponse = responseEventExt.isForkedResponse();
		final boolean isRetransmission = responseEventExt.isRetransmission();
		final ClientTransactionExt originalTransaction = responseEventExt.getOriginalTransaction();
		((SipStackImpl)sipStack).getStackLogger().logInfo("is Forked Response " + isForkedResponse);
		((SipStackImpl)sipStack).getStackLogger().logInfo("is Retransmission " + isRetransmission);
		((SipStackImpl)sipStack).getStackLogger().logInfo("Client Transaction " + clientTransaction);
		((SipStackImpl)sipStack).getStackLogger().logInfo("Original Transaction " + originalTransaction);
		((SipStackImpl)sipStack).getStackLogger().logInfo("Dialog " + dialog);
		this.lastResponse = response;
		ClientTransaction tid = responseReceivedEvent.getClientTransaction();
		CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
			
		if(response.getStatusCode() == 100) {
			numberOfTryingReceived++;
		}
		if (response.getStatusCode() == Response.REQUEST_TERMINATED) {
			isRequestTerminatedReceived = true;
			return;
		}
		
		SipURI fromUri = (SipURI)((FromHeader)response.getHeader(FromHeader.NAME)).getAddress().getURI();
		RecordRouteHeader recordRouteHeader = (RecordRouteHeader)response.getHeader(RecordRouteHeader.NAME);
		if(response.getStatusCode() > 100 && fromUri.getUser().equals("nonRecordRouting") && recordRouteHeader != null) {
			logger.error("the proxy is non record routing and we received a record route header in the response !");
			return;
		}
		
		if (tid == null) {
			
			// RFC3261: MUST respond to every 2xx
			if (ackRequest!=null && dialog!=null) {
			   System.out.println("re-sending ACK");
			   try {
			      dialog.sendAck(ackRequest);
			   } catch (SipException se) {
			      se.printStackTrace(); 
			   }
			}	
			return;
		}
		
		// If the caller is supposed to send the bye
		if ( !byeTaskRunning && dialog != null) {			
			if(!response.getHeader("From").toString().contains("sequential")) {
				byeTaskRunning = true;
				new Timer().schedule(new ByeTask(dialog), 4000) ;
			}
		}
		System.out.println("transaction state is " + tid.getState());
		//System.out.println("Dialog = " + tid.getDialog());
		//System.out.println("Dialog State is " + tid.getDialog().getState());

		try {			
			if (response.getStatusCode() == Response.OK) {
				if (cseq.getMethod().equals(Request.INVITE)) {
					okToInviteRecevied = true;
					Thread.sleep(pauseBeforeAck);
					ackRequest = dialog.createAck(cseq.getSeqNumber());
					if(forkingProxy) {
						logger.info("dialog = " + dialog);
						// Proxy will fork. I will accept the second dialog
						// but not the first. 
						logger.info("count = " + count);
//						if (count == 0) {
							//assertTrue(dialog != this.dialog);
							logger.info("Sending ACK");
							dialog.sendAck(ackRequest);	
							
//						} else {
//							// Kill the first dialog by sending a bye.
//							//assertTrue (dialog == this.dialog);
//							count++;
//							logger.info("count = " + count);
//							dialog.sendAck(ackRequest);
//							SipProvider sipProvider = (SipProvider) responseReceivedEvent.getSource();
//							Request byeRequest = dialog.createRequest(Request.BYE);
//							ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
//							dialog.sendRequest(ct);	
//						}
					} else {
						System.out.println("Dialog after 200 OK  " + dialog);
						System.out.println("Dialog State after 200 OK  " + dialog.getState());
						System.out.println("Sending ACK");
						dialog.sendAck(ackRequest);
						
						if(!byeTaskRunning) {
							try {							
								Thread.sleep(pauseBeforeBye);
								Request byeRequest = dialog.createRequest(Request.BYE);
								ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
								dialog.sendRequest(ct);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
					
				} else if (cseq.getMethod().equals(Request.CANCEL)) {
					if (dialog.getState() == DialogState.CONFIRMED) {
						// oops cancel went in too late. Need to hang up the
						// dialog.
						System.out
								.println("Sending BYE -- cancel went in too late !!");
						Request byeRequest = dialog.createRequest(Request.BYE);
						ClientTransaction ct = sipProvider
								.getNewClientTransaction(byeRequest);
						dialog.sendRequest(ct);
					}

				} else if(cseq.getMethod().equals(Request.BYE)) {
					ended = true;
				} else if(cseq.getMethod().equals(Request.PRACK)) {
					prackOkReceived = true;
				}
			} else if(response.getStatusCode() == 180 && !((ResponseEventExt)responseReceivedEvent).isRetransmission()) {
				if(usePrack) {
					Request prackRequest = dialog.createPrack(response);
					ClientTransaction ct = sipProvider.getNewClientTransaction(prackRequest);
					dialog.sendRequest(ct);
				}
				if(sendCancelOn180) {
					System.out.println("received 180 sending cancel");
					sendCancel();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

		System.out.println("Transaction Time out");
	}

	public void sendCancel() {
		try {			
			Request cancelRequest = inviteTid.createCancel();
			System.out.println("Sending cancel " + cancelRequest);
			ClientTransaction cancelTid = sipProvider
					.getNewClientTransaction(cancelRequest);
			cancelTid.sendRequest();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void init() {
		init("BigGuy", false, null);
	}
	
	public void init(String fromName, boolean useTelURL, String transport) {
		if(transport == null) {
			transport = ListeningPoint.UDP;
		}
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();		
		String peerHostPort = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + ":" + remotePort;
		if(outboundProxy) {
			properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
				+ transport);
		}
		// If you want to use UDP then uncomment this.
		properties.setProperty("javax.sip.STACK_NAME", "shootist");
		
		properties.setProperty("gov.nist.javax.sip.MAX_FORK_TIME_SECONDS", "10");
		properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", "4");
		properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", "true");
		
		// The following properties are specific to nist-sip
		// and are not necessarily part of any other jain-sip
		// implementation.
		// You can set a max message size for tcp transport to
		// guard against denial of service attack.
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"logs/shootistdebug.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"logs/shootistlog.xml");

		// Drop the client connection after we are done with the transaction.
		//properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS",
		//		"false");
		// Set to 0 (or NONE) in your production code for max speed.
		// You need 16 (or TRACE) for logging traces. 32 (or DEBUG) for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "DEBUG");

		try {
			// Create SipStack object
			sipStack = sipFactory.createSipStack(properties);
			System.out.println("createSipStack " + sipStack);
		} catch (PeerUnavailableException e) {
			// could not find
			// gov.nist.jain.protocol.ip.sip.SipStackImpl
			// in the classpath
			e.printStackTrace();
			System.err.println(e.getMessage());
		}

		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			listeningPoint = sipStack.createListeningPoint("" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "", 5058, transport);
			sipProvider = sipStack.createSipProvider(listeningPoint);
			Shootist listener = this;
			sipProvider.addSipListener(listener);
			
			String fromSipAddress = "here.com";
			if(fromHost != null) {
				fromSipAddress = fromHost;
			}
			String fromDisplayName = "The Master Blaster";

			String toSipAddress = "there.com";
			String toUser = "LittleGuy";
			String toDisplayName = "The Little Blister";

			// create >From Header
			SipURI fromAddress = addressFactory.createSipURI(fromName,
					fromSipAddress);

			Address fromNameAddress = addressFactory.createAddress(fromAddress);
			fromNameAddress.setDisplayName(fromDisplayName);
			FromHeader fromHeader = headerFactory.createFromHeader(
					fromNameAddress, "12345");

			// create To Header
			URI toAddress = null;
			if(useTelURL) {
				toAddress = addressFactory.createTelURL("+358-555-1234567");
			} else {
				toAddress = addressFactory
				.createSipURI(toUser, toSipAddress);	
			}			
			Address toNameAddress = addressFactory.createAddress(toAddress);
			toNameAddress.setDisplayName(toDisplayName);
			ToHeader toHeader = headerFactory.createToHeader(toNameAddress,
					null);

			// create Request URI
			SipURI requestURI = addressFactory.createSipURI(toUser,
					peerHostPort);

			// Create ViaHeaders

			ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
			String ipAddress = listeningPoint.getIPAddress();
			ViaHeader viaHeader = headerFactory.createViaHeader(ipAddress,
					sipProvider.getListeningPoint(transport).getPort(),
					transport, null);

			// add via headers
			viaHeaders.add(viaHeader);

			// Create ContentTypeHeader
			ContentTypeHeader contentTypeHeader = headerFactory
					.createContentTypeHeader("application", "sdp");

			// Create a new CallId header
			CallIdHeader callIdHeader = sipProvider.getNewCallId();
			
			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = headerFactory
					.createMaxForwardsHeader(70);

			// Create the request.
			if(requestMethod == null) requestMethod = Request.INVITE;
			
			// Create a new Cseq header
			CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L,
					requestMethod);

			Request request = messageFactory.createRequest(requestURI,
					requestMethod, callIdHeader, cSeqHeader, fromHeader,
					toHeader, viaHeaders, maxForwards);
			
			if(requestMethod.equals(Request.PUBLISH)) {
				request.setHeader(headerFactory.createEventHeader("presence"));
				
			}
			// Create contact headers
			String host = "" + System.getProperty("org.mobicents.testsuite.testhostaddr") + "";

			SipURI contactUrl = addressFactory.createSipURI(fromName, host);
			contactUrl.setPort(listeningPoint.getPort());
			contactUrl.setLrParam();

			// Create the contact name address.
			SipURI contactURI = addressFactory.createSipURI(fromName, host);
			contactURI.setPort(sipProvider.getListeningPoint(transport)
					.getPort());

			Address contactAddress = addressFactory.createAddress(contactURI);

			// Add the contact address.
			contactAddress.setDisplayName(fromName);

			contactHeader = headerFactory.createContactHeader(contactAddress);
			request.addHeader(contactHeader);

			// You can add extension headers of your own making
			// to the outgoing SIP request.
			// Add the extension header.
			Header extensionHeader = headerFactory.createHeader("My-Header",
					"my header value");
			request.addHeader(extensionHeader);

			String sdpData = "v=0\r\n"
					+ "o=4855 13760799956958020 13760799956958020"
					+ " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
					+ "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
					+ "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
					+ "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
					+ "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
			byte[] contents = sdpData.getBytes();

			request.setContent(contents, contentTypeHeader);
			// You can add as many extension headers as you
			// want.

			extensionHeader = headerFactory.createHeader("My-Other-Header",
					"my new header value ");
			request.addHeader(extensionHeader);

			Header callInfoHeader = headerFactory.createHeader("Call-Info",
					"<http://www.antd.nist.gov>");
			request.addHeader(callInfoHeader);
			
			if(usePrack) {
				RequireHeader requireHeader = headerFactory
				.createRequireHeader("100rel");
				request.addHeader(requireHeader);
			}
			Request subSeq = (Request) request.clone();
			this.request = request;

			// Create the client transaction.
			inviteTid = sipProvider.getNewClientTransaction(request);

			// send the request out.
			inviteTid.sendRequest();
			
			dialog = inviteTid.getDialog();
			
			if(!outboundProxy) {
				Address address = addressFactory.createAddress("sip:" + peerHostPort);
				RouteHeader routeHeader = headerFactory.createRouteHeader(address);
				request.addHeader(routeHeader);
			} 

			
			// Send a subsequent request after 2 secs
			if(requestMethod.equals(Request.PUBLISH) ||
					requestMethod.equals(Request.SUBSCRIBE)) {
				Thread.sleep(2000);
				
				CSeqHeader subSeqcSeqHeader = headerFactory.createCSeqHeader(2L,
						requestMethod);
				subSeq.setHeader(subSeqcSeqHeader);
				subSeq.setHeader(lastResponse.getHeader(FromHeader.NAME));
				ClientTransaction subseqCt = sipProvider.getNewClientTransaction(subSeq);
				subseqCt.sendRequest();
			}

			

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}
	}

	public void processIOException(IOExceptionEvent exceptionEvent) {
		System.out.println("IOException happened for "
				+ exceptionEvent.getHost() + " port = "
				+ exceptionEvent.getPort());

	}

	public void processTransactionTerminated(
			TransactionTerminatedEvent transactionTerminatedEvent) {
		System.out.println("Transaction terminated event recieved");
	}

	public void processDialogTerminated(
			DialogTerminatedEvent dialogTerminatedEvent) {
		System.out.println("dialogTerminatedEvent");

	}
	
	public void destroy() {
		logger.info("Destroying following sip stack " + sipStack.getStackName());
		HashSet<SipProvider> hashSet = new HashSet<SipProvider>();

		for (SipProvider sipProvider : hashSet) {
			hashSet.add(sipProvider);
		}

		for (SipProvider sipProvider : hashSet) {
			for (int j = 0; j < 5; j++) {
				try {
					sipStack.deleteSipProvider(sipProvider);
				} catch (ObjectInUseException ex) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}

				}
			}
		}

		sipStack.stop();
	}


	/**
	 * @param outboundProxy the outboundProxy to set
	 */
	public void setOutboundProxy(boolean outboundProxy) {
		this.outboundProxy = outboundProxy;
	}


	/**
	 * @return the outboundProxy
	 */
	public boolean isOutboundProxy() {
		return outboundProxy;
	}
	
	/**
	 * @return the lastMessageContent
	 */
	public String getLastMessageContent() {		
		return lastMessageContent;
	}

	/**
	 * @return the allMessagesContent
	 */
	public List<String> getAllMessagesContent() {
		return allMessagesContent;
	}

	/**
	 * @return the numberOfTryingReceived
	 */
	public int getNumberOfTryingReceived() {
		return numberOfTryingReceived;
	}


	/**
	 * @param sendCancelOn180 the sendCancelOn180 to set
	 */
	public void setSendCancelOn180(boolean sendCancelOn180) {
		this.sendCancelOn180 = sendCancelOn180;
	}


	/**
	 * @return the sendCancelOn180
	 */
	public boolean isSendCancelOn180() {
		return sendCancelOn180;
	}


	/**
	 * @param fromHost the fromHost to set
	 */
	public void setFromHost(String fromHost) {
		this.fromHost = fromHost;
	}


	/**
	 * @return the fromHost
	 */
	public String getFromHost() {
		return fromHost;
	}


	/**
	 * @param forkedResponseReceived the forkedResponseReceived to set
	 */
	public void setForkedResponseReceived(boolean forkedResponseReceived) {
		this.forkedResponseReceived = forkedResponseReceived;
	}


	/**
	 * @return the forkedResponseReceived
	 */
	public boolean isForkedResponseReceived() {
		return forkedResponseReceived;
	}


	/**
	 * @return the isRequestTerminatedReceived
	 */
	public boolean isRequestTerminatedReceived() {
		return isRequestTerminatedReceived;
	}

}
