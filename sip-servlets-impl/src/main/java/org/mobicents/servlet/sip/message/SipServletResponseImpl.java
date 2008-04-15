package org.mobicents.servlet.sip.message;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.Rel100Exception;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.Transaction;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.RecordRouteHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.servlet.sip.JainSipUtils;
import org.mobicents.servlet.sip.SipFactories;
import org.mobicents.servlet.sip.core.RoutingState;
import org.mobicents.servlet.sip.core.SipApplicationDispatcherImpl;
import org.mobicents.servlet.sip.core.session.SipSessionImpl;

/**
 * Implementation of the sip servlet response interface
 *
 */
public class SipServletResponseImpl extends SipServletMessageImpl implements
		SipServletResponse {
	private static Log logger =  LogFactory.getLog(SipServletResponseImpl.class);
	
	Response response;
	SipServletRequestImpl originalRequest;

	/**
	 * Constructor
	 * @param response
	 * @param sipFactoryImpl
	 * @param transaction
	 * @param session
	 * @param dialog
	 * @param originalRequest
	 */
	public SipServletResponseImpl (
			Response response, 
			SipFactoryImpl sipFactoryImpl, 
			Transaction transaction, 
			SipSession session, 
			Dialog dialog,
			SipServletRequestImpl originalRequest) {
		
		super(response, sipFactoryImpl, transaction, session, dialog);
		this.response = (Response) response;
		this.originalRequest = originalRequest;
	}
	
	
	@Override
	public boolean isSystemHeader(String headerName) {
		String hName = getFullHeaderName(headerName);

		/*
		 * Contact is a system header field in messages other than REGISTER
		 * requests and responses, 3xx and 485 responses, and 200/OPTIONS
		 * responses.
		 */

		// This doesnt contain contact!!!!
		boolean isSystemHeader = systemHeaders.contains(hName);

		if (isSystemHeader)
			return isSystemHeader;

		boolean isContactSystem = false;
		Response response = (Response) this.message;

		String method = ((CSeqHeader) response.getHeader(CSeqHeader.NAME))
				.getMethod();
		//Killer condition, see comment above for meaning
		if (method.equals(Request.REGISTER)
				|| (299 < response.getStatusCode() && response.getStatusCode() < 400)
				|| response.getStatusCode() == 485
				|| (response.getStatusCode() == 200 && method.equals(Request.OPTIONS))) {
			isContactSystem = false;
		} else {
			isContactSystem = true;
		}

		if (isContactSystem && hName.equals(ContactHeader.NAME)) {
			isSystemHeader = true;
		} else {
			isSystemHeader = false;
		}

		return isSystemHeader;
	}
	
	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#createAck()
	 */
	public SipServletRequest createAck() {
		Dialog dialog = super.session.getSessionCreatingDialog();
		CSeqHeader cSeqHeader = (CSeqHeader)response.getHeader(CSeqHeader.NAME);
		SipServletRequestImpl sipServletAckRequest = null; 
		try {
			Request ackRequest = dialog.createAck(cSeqHeader.getSeqNumber());
			sipServletAckRequest = new SipServletRequestImpl(
					ackRequest,this.sipFactoryImpl, this.getSipSession(), this.getTransaction(), dialog, false); 
		} catch (InvalidArgumentException e) {
			logger.error("Impossible to create the ACK",e);
		} catch (SipException e) {
			logger.error("Impossible to create the ACK",e);
		}		
		return sipServletAckRequest;
	}

	public SipServletRequest createPrack() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		// Always return null
		return null;
	}

	public Proxy getProxy() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#getReasonPhrase()
	 */
	public String getReasonPhrase() {
		return response.getReasonPhrase();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#getRequest()
	 */
	public SipServletRequest getRequest() {		
		return originalRequest;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#getStatus()
	 */
	public int getStatus() {
		return response.getStatusCode();
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.sip.SipServletResponse#getWriter()
	 */
	public PrintWriter getWriter() throws IOException {
		// Always returns null.
		return null;
	}

	public void sendReliably() throws Rel100Exception {
		// TODO Auto-generated method stub

	}
	
	public void setStatus(int statusCode) {
		// FIXME
		try {
			response.setStatusCode(statusCode);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void setStatus(int statusCode, String reasonPhrase) {
		// FIXME
		try {
			response.setStatusCode(statusCode);
			response.setReasonPhrase(reasonPhrase);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize() {		
		return 0;
	}

	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(int arg0) {
		// Do nothing
	}

	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void send()  {
		try {			
			//if this is a final response
			if(response.getStatusCode() >= Response.OK && 
					response.getStatusCode() <= Response.SESSION_NOT_ACCEPTABLE) {
				javax.sip.address.SipURI sipURI = JainSipUtils.createRecordRouteURI(
						sipFactoryImpl.getSipProviders(), 
						JainSipUtils.findTransport((Request)originalRequest.getMessage()));
				sipURI.setParameter(SipApplicationDispatcherImpl.RR_PARAM_APPLICATION_NAME, originalRequest.getCurrentApplicationName());
				sipURI.setParameter(SipApplicationDispatcherImpl.RR_PARAM_HANDLER_NAME, session.getHandler());
				sipURI.setLrParam();
				javax.sip.address.Address recordRouteAddress = 
					SipFactories.addressFactory.createAddress(sipURI);
				RecordRouteHeader recordRouteHeader = 
					SipFactories.headerFactory.createRecordRouteHeader(recordRouteAddress);
				response.addHeader(recordRouteHeader);
			}
			// Update Session state
			session.updateStateOnResponse(this);
			
			ServerTransaction st = (ServerTransaction) getTransaction();
			
			st.sendResponse( (Response)this.message );
			//specify that a final response has been sent for the request
			//so that the application dispatcher knows it has to stop
			//processing the request
			if(response.getStatusCode() >= Response.OK && 
					response.getStatusCode() <= Response.SESSION_NOT_ACCEPTABLE) {
				originalRequest.setRoutingState(RoutingState.FINAL_RESPONSE_SENT);
			}
		} catch (Exception e) {			
			logger.error(e);
			throw new IllegalStateException(e);
		}
	}
}
