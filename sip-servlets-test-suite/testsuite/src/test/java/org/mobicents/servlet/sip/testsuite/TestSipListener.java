package org.mobicents.servlet.sip.testsuite;

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
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
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
	private static final String CONTENT_TYPE_TEXT = "text";
	
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
	
	private SipURI requestURI;
	
	private Request inviteRequest;
	
	private boolean cancelSent;

	private boolean waitForCancel;
	
	private String lastMessageContent;
	
	private static Logger logger = Logger.getLogger(TestSipListener.class);
	

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
	}
	
	private void processMessage(Request request,
			ServerTransaction serverTransactionId) {
		
		ContentTypeHeader contentTypeHeader = (ContentTypeHeader) 
		request.getHeader(ContentTypeHeader.NAME);		
		if(CONTENT_TYPE_TEXT.equals(contentTypeHeader.getContentType())) {
			this.lastMessageContent = new String(request.getRawContent());			
		}
		try {
			Response okResponse = protocolObjects.messageFactory.createResponse(
					Response.OK, request);			
			ToHeader toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
			if (toHeader.getTag() == null) {
				toHeader.setTag(Integer.toString((int) (Math.random()*10000000)));
			}
			okResponse.addHeader(contactHeader);
			serverTransactionId.sendResponse(okResponse);
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
		try {
			logger.info("shootme: got an Invite " + request); 
			Response response = protocolObjects.messageFactory.createResponse(
					Response.TRYING, request);
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			Address address = protocolObjects.addressFactory
					.createAddress("Shootme <sip:127.0.0.1:" + myPort
							+";transport="+protocolObjects.transport
							+ ">");
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
			
			st.sendResponse(response);			
				
			Response ringing = protocolObjects.messageFactory
				.createResponse(Response.RINGING, request);
				toHeader = (ToHeader) ringing.getHeader(ToHeader.NAME);
				toHeader.setTag("5432"); // Application is supposed to set.
				st.sendResponse(ringing);			
			
			Thread.sleep(1000);
			if(!waitForCancel) {
				ContactHeader contactHeader = protocolObjects.headerFactory.createContactHeader(address);						
				Response okResponse = protocolObjects.messageFactory
						.createResponse(Response.OK, request);
				toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
				toHeader.setTag("5432"); // Application is supposed to set.
				okResponse.addHeader(contactHeader);			
					
				st.sendResponse(okResponse);
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
			if (response.getStatusCode() == Response.OK) {
				logger.info("response = " + response);
				if (cseq.getMethod().equals(Request.INVITE)) {
					Request ackRequest = dialog.createAck(cseq.getSeqNumber());
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
			}
			/**
			 * end of modified code
			 */
		} catch (Exception ex) {
			ex.printStackTrace();
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

	public void sendInvite(SipURI fromURI, SipURI toURI) throws SipException, ParseException, InvalidArgumentException {
			// create >From Header
			Address fromNameAddress = protocolObjects.addressFactory
					.createAddress(fromURI);			
			FromHeader fromHeader = protocolObjects.headerFactory
					.createFromHeader(fromNameAddress, "12345");

			// create To Header			
			Address toNameAddress = protocolObjects.addressFactory
					.createAddress(toURI);			
			ToHeader toHeader = protocolObjects.headerFactory.createToHeader(
					toNameAddress, null);

			// create Request URI
			this.requestURI = protocolObjects.addressFactory.createSipURI(
					toURI.getUser(), peerHostPort);
			this.requestURI.setPort(peerPort);
			
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
					.createCSeqHeader(1L, Request.INVITE);

			// Create a new MaxForwardsHeader
			MaxForwardsHeader maxForwards = protocolObjects.headerFactory
					.createMaxForwardsHeader(70);

			// Create the request.
			Request request = protocolObjects.messageFactory.createRequest(
					requestURI, Request.INVITE, callIdHeader, cSeqHeader,
					fromHeader, toHeader, viaHeaders, maxForwards);
			// Create contact headers
			String host = "127.0.0.1";

			SipURI contactUrl = protocolObjects.addressFactory.createSipURI(
					fromURI.getUser(), host);
			/**
			 * either use tcp or udp
			 */
			contactUrl.setPort(listeningPoint.getPort());
//			contactUrl.setTransportParam(protocolObjects.transport);
		
			// Create the contact name address.
			
			Address contactAddress = protocolObjects.addressFactory
					.createAddress(contactUrl);
			contactUrl.setLrParam();

			// Add the contact address.
//			contactAddress.setDisplayName(fromName);

			contactHeader = protocolObjects.headerFactory
					.createContactHeader(contactAddress);
			request.addHeader(contactHeader);
			
			SipURI uri = protocolObjects.addressFactory.createSipURI(null, "127.0.0.1");
			
			uri.setLrParam();
			uri.setTransportParam(protocolObjects.transport);
			uri.setPort(this.peerPort);
			

			Address address = protocolObjects.addressFactory.createAddress(uri);
			RouteHeader routeHeader = protocolObjects.headerFactory.createRouteHeader(address);
			request.addHeader(routeHeader);
			// Add the extension header.
//			Header extensionHeader = protocolObjects.headerFactory
//					.createHeader("My-Header", "my header value");
//			request.addHeader(extensionHeader);
//
//			String sdpData = "v=0\r\n"
//					+ "o=4855 13760799956958020 13760799956958020"
//					+ " IN IP4  129.6.55.78\r\n" + "s=mysession session\r\n"
//					+ "p=+46 8 52018010\r\n" + "c=IN IP4  129.6.55.78\r\n"
//					+ "t=0 0\r\n" + "m=audio 6022 RTP/AVP 0 4 18\r\n"
//					+ "a=rtpmap:0 PCMU/8000\r\n" + "a=rtpmap:4 G723/8000\r\n"
//					+ "a=rtpmap:18 G729A/8000\r\n" + "a=ptime:20\r\n";
//			byte[] contents = sdpData.getBytes();

//			request.setContent(contents, contentTypeHeader);

//			extensionHeader = protocolObjects.headerFactory.createHeader(
//					"My-Other-Header", "my new header value ");
//			request.addHeader(extensionHeader);

//			Header callInfoHeader = protocolObjects.headerFactory.createHeader(
//					"Call-Info", "<http://www.antd.nist.gov>");
//			request.addHeader(callInfoHeader);

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
			protocolObjects.headerFactory.createContentTypeHeader("text","plain;charset=UTF-8");
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
			protocolObjects.headerFactory.createContentTypeHeader("text","plain;charset=UTF-8");
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
}
