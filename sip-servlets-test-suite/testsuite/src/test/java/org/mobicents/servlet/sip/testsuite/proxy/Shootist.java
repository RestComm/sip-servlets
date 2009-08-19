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
package org.mobicents.servlet.sip.testsuite.proxy;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
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

	private static SipStack sipStack;

	private ContactHeader contactHeader;

	private ListeningPoint udpListeningPoint;

	private ClientTransaction inviteTid;

	private Dialog dialog;

	private boolean byeTaskRunning;
	
	public boolean ended = false;
	
	public boolean usePrack = false;
	
	public boolean prackOkReceived = false;
	
	public boolean okToInviteRecevied = false;
	
	int count = 0;
	
	private boolean forkingProxy = false;
	
	private boolean outboundProxy = true;
	
	public String requestMethod;
	
	public Request request;
	
	public Response lastResponse;

	class ByeTask  extends TimerTask {
		Dialog dialog;
		public ByeTask(Dialog dialog)  {
			this.dialog = dialog;
		}
		public void run () {
			try {
			   Request byeRequest = this.dialog.createRequest(Request.BYE);
			   ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
			   dialog.sendRequest(ct);
			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(0);
			}

		}

	}

	private static final String usageString = "java "
			+ "examples.shootist.Shootist \n"
			+ ">>>> is your class path set to the root?";

	
	public Shootist(boolean forkingProxy) {
		this.forkingProxy = forkingProxy;
	}


	private static void usage() {
		System.out.println(usageString);
		System.exit(0);

	}


	public void processRequest(RequestEvent requestReceivedEvent) {
		Request request = requestReceivedEvent.getRequest();
		ServerTransaction serverTransactionId = requestReceivedEvent
				.getServerTransaction();

		System.out.println("\n\nRequest " + request.getMethod()
				+ " received at " + sipStack.getStackName()
				+ " with server transaction id " + serverTransactionId);

		// We are the UAC so the only request we get is the BYE.
		if (request.getMethod().equals(Request.BYE))
			processBye(request, serverTransactionId);
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

	public void processBye(Request request,
			ServerTransaction serverTransactionId) {
		try {
			System.out.println("shootist:  got a bye .");
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
			System.exit(0);

		}
	}

       // Save the created ACK request, to respond to retransmitted 2xx
       private Request ackRequest;

	public void processResponse(ResponseEvent responseReceivedEvent) {
		System.out.println("Got a response");
		Response response = (Response) responseReceivedEvent.getResponse();
		this.lastResponse = response;
		ClientTransaction tid = responseReceivedEvent.getClientTransaction();
		CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
		Dialog dialog = responseReceivedEvent.getDialog();
		
		System.out.println("Response received : Status Code = "
				+ response.getStatusCode() + " " + cseq);
		
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
			byeTaskRunning = true;
			if(!response.getHeader("From").toString().contains("sequential")) {
				new Timer().schedule(new ByeTask(dialog), 4000) ;
			}
		}
		System.out.println("transaction state is " + tid.getState());
		//System.out.println("Dialog = " + tid.getDialog());
		//System.out.println("Dialog State is " + tid.getDialog().getState());

		try {
			if (response.getStatusCode() == Response.OK) {
				if (cseq.getMethod().equals(Request.INVITE)) {
					ackRequest = dialog.createAck(cseq.getSeqNumber());
					if(forkingProxy) {
						logger.info("dialog = " + dialog);
						// Proxy will fork. I will accept the second dialog
						// but not the first. 
						logger.info("count = " + count);
//						if (count == 1) {
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
						
						try {
							 Thread.sleep(2000);
							   Request byeRequest = dialog.createRequest(Request.BYE);
							   ClientTransaction ct = sipProvider.getNewClientTransaction(byeRequest);
							   dialog.sendRequest(ct);
							} catch (Exception ex) {
								ex.printStackTrace();
								System.exit(0);
							}
					}
					okToInviteRecevied = true;
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
			} else if(response.getStatusCode() == 180) {
				if(usePrack) {
					Request prackRequest = dialog.createPrack(response);
					ClientTransaction ct = sipProvider.getNewClientTransaction(prackRequest);
					dialog.sendRequest(ct);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {

		System.out.println("Transaction Time out");
	}

	public void sendCancel() {
		try {
			System.out.println("Sending cancel");
			Request cancelRequest = inviteTid.createCancel();
			ClientTransaction cancelTid = sipProvider
					.getNewClientTransaction(cancelRequest);
			cancelTid.sendRequest();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void init() {
		init("BigGuy", false);
	}
	
	public void init(String fromName, boolean useTelURL) {
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		// If you want to try TCP transport change the following to
		String transport = "udp";
		String peerHostPort = "127.0.0.1:5070";
		if(outboundProxy) {
			properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort + "/"
				+ transport);
		}
		// If you want to use UDP then uncomment this.
		properties.setProperty("javax.sip.STACK_NAME", "shootist");

		// The following properties are specific to nist-sip
		// and are not necessarily part of any other jain-sip
		// implementation.
		// You can set a max message size for tcp transport to
		// guard against denial of service attack.
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"logs/shootistdebug.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"logs/shootistlog.txt");

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
			System.exit(0);
		}

		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			udpListeningPoint = sipStack.createListeningPoint("127.0.0.1", 5058, "udp");
			sipProvider = sipStack.createSipProvider(udpListeningPoint);
			Shootist listener = this;
			sipProvider.addSipListener(listener);
			
			String fromSipAddress = "here.com";
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
			String ipAddress = udpListeningPoint.getIPAddress();
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
			String host = "127.0.0.1";

			SipURI contactUrl = addressFactory.createSipURI(fromName, host);
			contactUrl.setPort(udpListeningPoint.getPort());
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
				Address address = addressFactory.createAddress(peerHostPort);
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
}
