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
import javax.sip.Transaction;
import javax.sip.TransactionState;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.ContactHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.log4j.Logger;

public class Shootme implements SipListener {
	private static transient Logger logger = Logger.getLogger(Shootme.class);
	private static AddressFactory addressFactory;

	private static MessageFactory messageFactory;

	private static HeaderFactory headerFactory;
	
	private static SipProvider sipProvider;

	private SipStack sipStack;

	private static final String myAddress = "127.0.0.1";

	private int myPort = 5057;
	private String toTag;

	protected ServerTransaction inviteTid;

	private Response okResponse;

	private Request inviteRequest;

	private Dialog dialog;

	public boolean ended = false;
	
	public boolean cancelled = false;
	
	public boolean usePrack = false;
	
	public int inviteResponseCode = 0;
	
	public static final boolean callerSendsBye = true;

	class MyTimerTask extends TimerTask {
		Shootme shootme;

		public MyTimerTask(Shootme shootme) {
			this.shootme = shootme;

		}

		public void run() {
			shootme.sendInviteOK();
		}

	}

	protected static final String usageString = "java "
			+ "examples.shootist.Shootist \n"
			+ ">>>> is your class path set to the root?";

	public Shootme(int port) {
		myPort = port;
	}

	private static void usage() {
		System.out.println(usageString);
		System.exit(0);

	}
	
	public void processPublishSubscribe (RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
		try {
			this.okResponse = messageFactory.createResponse(Response.OK,
					requestEvent.getRequest());
			this.okResponse.setHeader(headerFactory.createExpiresHeader(1000));
			if(serverTransaction == null) {
				serverTransaction = sipProvider.getNewServerTransaction(requestEvent.getRequest());
			}
			serverTransaction.sendResponse(this.okResponse);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void processRequest(RequestEvent requestEvent) {
		Request request = requestEvent.getRequest();
		ServerTransaction serverTransactionId = requestEvent
				.getServerTransaction();

		System.out.println("\n\nRequest " + request.getMethod()
				+ " received at " + sipStack.getStackName()
				+ " with server transaction id " + serverTransactionId);

		if (request.getMethod().equals(Request.INVITE)) {
			processInvite(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.ACK)) {
			processAck(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.BYE)) {
			processBye(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.CANCEL)) {
			processCancel(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.PRACK)) {
			processPrack(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.PUBLISH)){
			processPublishSubscribe(requestEvent, serverTransactionId);
		} else if (request.getMethod().equals(Request.SUBSCRIBE)){
			processPublishSubscribe(requestEvent, serverTransactionId);
		} else {
			try {
				serverTransactionId.sendResponse( messageFactory.createResponse( 202, request ) );
				
				// send one back
				SipProvider prov = (SipProvider) requestEvent.getSource();
				Request refer = requestEvent.getDialog().createRequest("REFER");
				requestEvent.getDialog().sendRequest( prov.getNewClientTransaction(refer) );				
				
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

	public void processResponse(ResponseEvent responseEvent) {
	}

	/**
	 * Process the ACK request. Send the bye and complete the call flow.
	 */
	public void processAck(RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
		try {
			System.out.println("shootme: got an ACK! ");
			System.out.println("Dialog State = " + dialog.getState());
			SipProvider provider = (SipProvider) requestEvent.getSource();
			if (!callerSendsBye) {
				Request byeRequest = dialog.createRequest(Request.BYE);
				ClientTransaction ct = provider
						.getNewClientTransaction(byeRequest);
				dialog.sendRequest(ct);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
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
			System.out.println("shootme: got an Invite sending Trying");
			// System.out.println("shootme: " + request);
			Response response = null;
			
			if(serverTransaction == null) {
				serverTransaction = sipProvider.getNewServerTransaction(request);
			}
			if(!usePrack) response = messageFactory.createResponse(Response.RINGING,
					request);
			else response = serverTransaction.getDialog().createReliableProvisionalResponse(180);
			String toTag = Integer.toString((int) (Math.random()*10000000));
			ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
			toHeader.setTag(toTag); // Application is supposed to set.
			this.toTag = toTag;
		
			dialog = serverTransaction.getDialog();

			if(!usePrack)
				serverTransaction.sendResponse(response);
			else
				dialog.sendReliableProvisionalResponse(response);
			
			this.okResponse = messageFactory.createResponse(Response.OK,
					request);
			Address address = addressFactory.createAddress("Shootme <sip:"
					+ myAddress + ":" + myPort + ">");
			ContactHeader contactHeader = headerFactory
					.createContactHeader(address);
			response.addHeader(contactHeader);
			toHeader = (ToHeader) okResponse.getHeader(ToHeader.NAME);
			toHeader.setTag(toTag); // Application is supposed to set.
			okResponse.addHeader(contactHeader);
			this.inviteTid = serverTransaction;
			// Defer sending the OK to simulate the phone ringing.
			// Answered in 1 second ( this guy is fast at taking calls)
			this.inviteRequest = request;
	
			if(!usePrack) {
				if(inviteResponseCode == 0) {
					new Timer().schedule(new MyTimerTask(this), 1000);
				} else {
					Response customResp = messageFactory.createResponse(inviteResponseCode,
							request);
					serverTransaction.sendResponse(customResp);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}
 
	private void sendInviteOK() {
		try {
			if (inviteTid.getState() != TransactionState.COMPLETED) {
				System.out.println("shootme: Dialog state before 200: "
						+ inviteTid.getDialog().getState());
				inviteTid.sendResponse(okResponse);
				System.out.println("shootme: Dialog state after 200: "
						+ inviteTid.getDialog().getState());
			}
		} catch (SipException ex) {
			ex.printStackTrace();
		} catch (InvalidArgumentException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Process the bye request.
	 */
	public void processBye(RequestEvent requestEvent,
			ServerTransaction serverTransactionId) {
		ended = true;		
		Request request = requestEvent.getRequest();
		Dialog dialog = requestEvent.getDialog();
		System.out.println("local party = " + dialog.getLocalParty());
		try {
			System.out.println("shootme:  got a bye sending OK.");
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			System.out.println("Dialog State is "
					+ serverTransactionId.getDialog().getState());

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	public void processCancel(RequestEvent requestEvent,
			ServerTransaction serverTransactionId) {		
		Request request = requestEvent.getRequest();
		try {
			System.out.println("shootme:  got a cancel.");
			if (serverTransactionId == null) {
				System.out.println("shootme:  null tid.");
				return;
			}
			Response response = messageFactory.createResponse(200, request);
			serverTransactionId.sendResponse(response);
			cancelled = true;
			if (dialog.getState() != DialogState.CONFIRMED) {
				response = messageFactory.createResponse(
						Response.REQUEST_TERMINATED, inviteRequest);
				inviteTid.sendResponse(response);
			}

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);

		}
	}

	public void processTimeout(javax.sip.TimeoutEvent timeoutEvent) {
		Transaction transaction;
		if (timeoutEvent.isServerTransaction()) {
			transaction = timeoutEvent.getServerTransaction();
		} else {
			transaction = timeoutEvent.getClientTransaction();
		}
		System.out.println("state = " + transaction.getState());
		System.out.println("dialog = " + transaction.getDialog());
		System.out.println("dialogState = "
				+ transaction.getDialog().getState());
		System.out.println("Transaction Time out");
	}

	public void init(String stackName) {
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", stackName);
		// You need 16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"logs/debug"+stackName+".txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"logs/" + stackName + ".txt");

		try {
			// Create SipStack object
			sipStack = sipFactory.createSipStack(properties);
			System.out.println("sipStack = " + sipStack);
		} catch (PeerUnavailableException e) {
			// could not find
			// gov.nist.jain.protocol.ip.sip.SipStackImpl
			// in the classpath
			e.printStackTrace();
			System.err.println(e.getMessage());
			if (e.getCause() != null)
				e.getCause().printStackTrace();
			System.exit(0);
		}

		try {
			headerFactory = sipFactory.createHeaderFactory();
			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",
					myPort, "udp");

			Shootme listener = this;

			sipProvider = sipStack.createSipProvider(lp);
			System.out.println("udp provider " + sipProvider);
			sipProvider.addSipListener(listener);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			usage();
		}

	}

	public void processIOException(IOExceptionEvent exceptionEvent) {
		System.out.println("IOException");

	}

	public void processTransactionTerminated(
			TransactionTerminatedEvent transactionTerminatedEvent) {
		if (transactionTerminatedEvent.isServerTransaction())
			System.out.println("Transaction terminated event recieved"
					+ transactionTerminatedEvent.getServerTransaction());
		else
			System.out.println("Transaction terminated "
					+ transactionTerminatedEvent.getClientTransaction());

	}

	public void processDialogTerminated(
			DialogTerminatedEvent dialogTerminatedEvent) {
		System.out.println("Dialog terminated event recieved");
		Dialog d = dialogTerminatedEvent.getDialog();
		System.out.println("Local Party = " + d.getLocalParty());

	}

	public void destroy() {
		logger.info("Destroying following sip stack " + sipStack.getStackName());
		HashSet<SipProvider> hashSet = new HashSet<SipProvider>();
		
		for (SipProvider sipProvider : hashSet) {
			hashSet.add(sipProvider);
		}
		try {
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
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	private void processPrack(RequestEvent requestEvent,
			ServerTransaction serverTransactionId) {
		try {
			System.out.println("shootme: got an PRACK! ");
			System.out.println("Dialog State = " + dialog.getState());
			
			/**
			 * JvB: First, send 200 OK for PRACK
			 */
			Request prack = requestEvent.getRequest();
			Response prackOk = messageFactory.createResponse( 200, prack );
			serverTransactionId.sendResponse( prackOk );
			
			/**
			 * Send a 200 OK response to complete the 3 way handshake for the
			 * INIVTE.
			 */			
			Response response = messageFactory.createResponse(200,
					inviteRequest);
			ToHeader to = (ToHeader) response.getHeader(ToHeader.NAME);
			to.setTag(this.toTag);
			Address address = addressFactory.createAddress("Shootme <sip:"
					+ myAddress + ":" + myPort + ">");
			ContactHeader contactHeader = headerFactory
					.createContactHeader(address);
			response.addHeader(contactHeader);
			inviteTid.sendResponse(response);			
		} catch (Exception ex) {
			ex.printStackTrace();

		}

	}
}
