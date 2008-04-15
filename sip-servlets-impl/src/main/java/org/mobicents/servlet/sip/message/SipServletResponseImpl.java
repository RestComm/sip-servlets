package org.mobicents.servlet.sip.message;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import javax.servlet.ServletOutputStream;
import javax.servlet.sip.Proxy;
import javax.servlet.sip.Rel100Exception;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;
import javax.sip.Dialog;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ContactHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.mobicents.servlet.sip.core.session.SipSessionImpl;

public class SipServletResponseImpl extends SipServletMessageImpl implements
		SipServletResponse {
	
	
	
	

	public SipServletResponseImpl (Response response, SipProvider provider, ServerTransaction serverTransaction, SipSession session, Dialog dialog) {
		super(response, provider, serverTransaction, session, dialog);
		
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

	

	public SipServletRequest createAck() {
		// TODO Auto-generated method stub
		return null;
	}

	public SipServletRequest createPrack() {
		// TODO Auto-generated method stub
		return null;
	}

	public ServletOutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Proxy getProxy() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReasonPhrase() {
		// TODO Auto-generated method stub
		return null;
	}

	public SipServletRequest getRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	public PrintWriter getWriter() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendReliably() throws Rel100Exception {
		// TODO Auto-generated method stub

	}

	public void setStatus(int statusCode) {
		// TODO Auto-generated method stub

	}

	public void setStatus(int statusCode, String reasonPhrase) {
		// TODO Auto-generated method stub

	}

	public void flushBuffer() throws IOException {
		// TODO Auto-generated method stub

	}

	public int getBufferSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Locale getLocale() {
		// TODO Auto-generated method stub
		return null;
	}

	public void reset() {
		// TODO Auto-generated method stub

	}

	public void resetBuffer() {
		// TODO Auto-generated method stub

	}

	public void setBufferSize(int arg0) {
		// TODO Auto-generated method stub

	}

	public void setLocale(Locale arg0) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void send()  {
		try {
			ServerTransaction st = (ServerTransaction) transaction;
			st.sendResponse( (Response)this.message );
		} catch (Exception e) {
			//TODO logger
			throw new IllegalStateException(e.getMessage());
		}
	}
}
