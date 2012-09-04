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
package org.mobicents.servlet.sip.pbx.registrar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse; 
import javax.servlet.sip.URI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mobicents.servlet.sip.pbx.ResponseException;
import org.mobicents.servlet.sip.pbx.SipHeaders;
import org.mobicents.servlet.sip.pbx.URIUtil;
import org.mobicents.servlet.sip.pbx.location.Binding;
import org.mobicents.servlet.sip.pbx.location.LocationService;

/**
 * @author Thomas Leseney
 */
public class RegistrarService {

	private static Log logger = LogFactory.getLog(RegistrarService.class);
	
	private LocationService locationService;
	private SipFactory sipFactory;
	
	private int minExpires = 60;
	private int maxExpires = 86400;
	private int defaultExpires = 3600;
	
	private java.text.DateFormat dateFormat;
	 
	public RegistrarService() {
		dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT")); 
	}
	
	public void setLocationService(LocationService locationService) {
		this.locationService = locationService;
	}
	
	public void setSipFactory(SipFactory sipFactory) {
		this.sipFactory = sipFactory;
	}
	
	public void doRegister(SipServletRequest request) throws IOException, ServletException {
		
		boolean success = false;
		try {
			locationService.beginTransaction();
			
			URI uri = request.getRequestURI();
			if (!isLocal(uri)) {
				throw new ResponseException(SipServletResponse.SC_FORBIDDEN, "Not local domain: " + uri);
			}
			
			String aor = URIUtil.toCanonical(request.getTo().getURI());
			
			logger.debug("Handling register for aor: " + aor);
			
			List<Binding> bindings = locationService.getBindings(aor);
			if (bindings == null) {
				bindings = Collections.emptyList();
			}

			Iterator<Address> it = request.getAddressHeaders(SipHeaders.CONTACT);
			
			if (it.hasNext()) {
				List<Address> contacts = new ArrayList<Address>();
				boolean wildcard = false;
				
				while (it.hasNext()) {
					Address contact = it.next();
					if (contact.isWildcard()) {
						wildcard = true;
						if (it.hasNext() || contacts.size() > 0 || request.getExpires() > 0) {
							throw new ResponseException(SipServletResponse.SC_BAD_REQUEST, "invalid wildcard");
						}
					}
					contacts.add(contact);
				}
				String callId = request.getCallId();
				int cseq = getCSeq(request);
				
				if (wildcard) {
					for (Binding binding : bindings) {
						if (callId.equals(binding.getCallId()) && cseq < binding.getCseq()) {
							throw new ResponseException(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "lower cseq");
						}
						locationService.removeBinding(binding);
					}
				} else {
					for (Address contact : contacts) {
						int expires = -1;
						expires = contact.getExpires();
						
						if (expires < 0) {
							expires = request.getExpires();
						}
						
						if (expires != 0) {
							if (expires < 0) {
								expires = defaultExpires;
							}
							if (expires > maxExpires) {
								expires = maxExpires;
							} else if (expires < minExpires) {
								ResponseException e = new ResponseException(SipServletResponse.SC_INTERVAL_TOO_BRIEF);
								e.addHeader(SipHeaders.MIN_EXPIRES, Integer.toString(minExpires));
								throw e;
							}
						}
						boolean exist = false;
						
						for (Binding binding : bindings) {
							URI contactUri = null;
							try {
								contactUri = sipFactory.createURI(binding.getContact());
							} catch (ServletParseException e) {
								throw new ResponseException(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "Invalid URI: " + binding.getContact());
							}
							if (contact.getURI().equals(contactUri)) {
								exist = true;
								if (callId.equals(binding.getCallId()) && cseq < binding.getCseq()) {
									throw new ResponseException(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "lower cseq");
								}
								if (expires == 0) {
									locationService.removeBinding(binding);
									logger.debug("Removed binding: " + binding);
								} else {
									binding.setContact(contact.getURI().toString());
									binding.setCallId(callId);
									binding.setCseq(cseq);
									binding.setExpires(expires);
									locationService.updateBinding(binding);		
									logger.debug("Updated binding: " + binding);
								}
							}
						}
						if (!exist && expires != 0) {
							Binding binding = locationService.createBinding(aor, contact.getURI().toString());
							binding.setCallId(callId);
							binding.setCseq(cseq);
							binding.setExpires(expires);
							locationService.addBinding(binding);
							logger.debug("Added binding: " + binding);
						}
					}
				}
				bindings = locationService.getBindings(aor);
			}
			SipServletResponse ok = request.createResponse(SipServletResponse.SC_OK);
			ok.addHeader(SipHeaders.DATE, dateFormat.format(new Date()));
			if (bindings != null) {
				for (Binding binding : bindings) {
					ok.addHeader(SipHeaders.CONTACT, "<" + binding.getContact() + ">;expires=" + binding.getExpires());
				}
			}
			ok.send();
			success = true;
		} catch (ResponseException e) {
			logger.warn("Error while handling register", e);
			request.createResponse(e.getStatus(), e.getReason()).send();
		} catch (Throwable t) {
			logger.warn("Error while handling register", t);
			request.createResponse(SipServletResponse.SC_SERVER_INTERNAL_ERROR, t.getMessage());
		} finally {
			if (success) {
				locationService.commitTransaction();
			} else {
				locationService.rollbackTransaction();
			}
		}
	}
	
	private boolean isLocal(URI uri) {
		return true;
	}
	
	private int getCSeq(SipServletRequest request) throws ResponseException {
		String s = request.getHeader(SipHeaders.CSEQ);
		try {
			return Integer.parseInt(s.substring(0, s.indexOf(' ')));
		} catch (Exception e) {
			throw new ResponseException(400, "invalid cseq");
		}
	}
}
