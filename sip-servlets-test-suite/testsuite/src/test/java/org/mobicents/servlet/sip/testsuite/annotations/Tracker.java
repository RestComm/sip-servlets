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
package org.mobicents.servlet.sip.testsuite.annotations;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
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
import javax.sip.TransactionTerminatedEvent;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class Tracker implements SipListener {

	private MessageFactory messageFactory;
	private SipStack sipStack;
	private static final int myPort = 5058;
	protected ServerTransaction inviteTid;
	private Dialog dialog;
	public static final boolean callerSendsBye = true;
	public boolean receivedInvite = false;

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
	 * Process the invite request. If we receive this invite the security
	 * scenario has completed successfully.
	 */
	public void processInvite(RequestEvent requestEvent,
			ServerTransaction serverTransaction) {
		SipProvider sipProvider = (SipProvider) requestEvent.getSource();
		Request request = requestEvent.getRequest();
		try {
			System.out.println("tracker: got an Invite sending Trying");
			
			
			
			// System.out.println("cutme: " + request);
			Response response = messageFactory.createResponse(Response.BUSY_HERE,
					request);
			ServerTransaction st = requestEvent.getServerTransaction();

			if (st == null) {
				st = sipProvider.getNewServerTransaction(request);
			}
			dialog = st.getDialog();
//			inviteRequest = request;
			st.sendResponse(response);
			this.receivedInvite = true;
			// If we dont send final response this will receive cancel.
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

	public void init() {
		SipFactory sipFactory = null;
		sipStack = null;
		sipFactory = SipFactory.getInstance();
		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "cutme");
		// You need 16 for logging traces. 32 for debug + traces.
		// Your code will limp at 32 but it is best for debugging.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"logs/cutmedebug.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"logs/cutmelog.txt");

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
//			headerFactory = sipFactory.createHeaderFactory();
//			addressFactory = sipFactory.createAddressFactory();
			messageFactory = sipFactory.createMessageFactory();
			ListeningPoint lp = sipStack.createListeningPoint("127.0.0.1",
					myPort, "udp");

			Tracker listener = this;

			SipProvider sipProvider = sipStack.createSipProvider(lp);
			System.out.println("udp provider " + sipProvider);
			sipProvider.addSipListener(listener);

		} catch (Exception ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
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
		HashSet hashSet = new HashSet();

		for (Iterator it = sipStack.getSipProviders(); it.hasNext();) {

			SipProvider sipProvider = (SipProvider) it.next();
			hashSet.add(sipProvider);
		}

		for (Iterator it = hashSet.iterator(); it.hasNext();) {
			SipProvider sipProvider = (SipProvider) it.next();

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
}
