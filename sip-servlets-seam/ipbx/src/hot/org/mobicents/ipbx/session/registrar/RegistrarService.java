/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.mobicents.ipbx.session.registrar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.URI;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.log.Log;
import org.mobicents.ipbx.entity.Binding;
import org.mobicents.ipbx.entity.Registration;
import org.mobicents.ipbx.session.DataLoader;
import org.mobicents.ipbx.session.call.model.CallStateManager;
import org.mobicents.ipbx.session.security.SimpleSipAuthenticator;
import org.mobicents.ipbx.session.util.URIUtil;

/**
 * The registrar service as defined per RFC 3261, Section 10.3
 * 
 * TODO have a background timer task checking for unused bindings
 * 
 * @author jean.deruelle@gmail.com
 * @author Thomas Leseney from Nexcom Systems
 */
@Name("registrarService")
@Scope(ScopeType.STATELESS)
@Transactional
public class RegistrarService {
	public static final String CONTACT_HEADER = "Contact";
	public static final String MIN_EXPIRES_HEADER 	= "Min-Expires";
	public static final String CSEQ_HEADER 	= "CSeq";
	public static final String DATE_HEADER 	= "Date";
	
	private int minExpires = 60;
	private int maxExpires = 86400;
	private int defaultExpires = 3600;
	
	@Logger Log log;
	@In(create=true) SimpleSipAuthenticator sipAuthenticator;
	@In EntityManager entityManager;
	@In DataLoader dataLoader;
	@In SipFactory sipFactory;
	
	private java.text.DateFormat dateFormat;
	 
	public RegistrarService() {
		dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z", Locale.US);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT")); 
	}
	
	@Observer("REGISTER")
	public void doRegister(SipServletRequest request) throws ServletException,
			IOException {
		if(log.isDebugEnabled()) {
			log.debug("Registrar received register request: " + request);
		}
		int response = SipServletResponse.SC_OK;
		SipServletResponse resp = request.createResponse(response);
		
		// TODO RFC3261, Section 10.3 : A registrar has to know the set 
		// of domain(s) for which it maintains bindings : get the set of domains we maintain the registration
		// from the overall pbx configuration
		
		// TODO RFC 3261, Section 10.3.1 : The registrar inspects the Request-URI to determine whether it
        // has access to bindings for the domain identified in the Request-URI.  If not, and if the server 
		// also acts as a proxy server, the server SHOULD forward the request to the addressed domain, 
		// following the general behavior for proxying messages described in Section 16
		
		// TODO RFC3261, Section 10.3.2 : To guarantee that the registrar supports any necessary
        // extensions, the registrar MUST process the Require header field values as described for UASs in Section 8.2.2.
		
		// TODO RFC3261, Section 10.3.3 : A registrar SHOULD authenticate the UAC.  Mechanisms for the
        // authentication of SIP user agents are described in Section 22. Registration behavior in no way overrides the generic
        // authentication framework for SIP.  If no authentication mechanism is available, the registrar MAY take the From address
        // as the asserted identity of the originator of the request.
		
		// TODO RFC3261, Section 10.3.4 : The registrar SHOULD determine if the authenticated user is
        // authorized to modify registrations for this address-of-record. For example, a registrar might consult an authorization
        // database that maps user names to a list of addresses-of-record for which that user has authorization to modify bindings.  If
        // the authenticated user is not authorized to modify bindings, the registrar MUST return a 403 (Forbidden) and skip the
        // remaining steps.
		try {
			Registration registration = processAddressOfRecord(request);
			
			// RC3261, Section 10.3.6 : The registrar checks whether the request contains the Contact header field.  
			// If not, it skips to the last step. 
			Iterator<Address> it = request.getAddressHeaders(CONTACT_HEADER);
			if(it != null) {			
				if (it.hasNext()) {
					List<Address> contacts = new ArrayList<Address>();
					boolean wildcard = false;
					// RC3261, Section 10.3.6 : If the Contact header field is present, the registrar checks if there
			        // is one Contact field value that contains the special value "*" and an Expires field.  
					// If the request has additional Contact fields or an expiration time other than zero, the request is
			        // invalid, and the server MUST return a 400 (Invalid Request) and skip the remaining steps.  				
					while (it.hasNext()) {
						Address contact = it.next();
						if (contact.isWildcard()) {
							wildcard = true;
							if (it.hasNext() || contacts.size() > 0 || request.getExpires() > 0) {
								throw new BadRegistrationException(SipServletResponse.SC_BAD_REQUEST, "invalid wildcard");
							}
						}
						contacts.add(contact);
					}								
					processContacts(request, wildcard, registration, contacts);
				}						
			} 			
			// RFC3261, Section 10.3.8 : The registrar returns a 200 (OK) response.  
			// The response MUST contain Contact header field values enumerating all current bindings.  
			// Each Contact value MUST feature an "expires" parameter indicating its expiration interval chosen by the registrar.  
			// The response SHOULD include a Date header field.
			Set<Binding> bindings = registration.getBindings();
			if (bindings != null) {
				for (Binding binding : bindings) {
					resp.addHeader(CONTACT_HEADER, "<" + binding.getContactAddress() + ">;expires=" + binding.getExpires());
				}
			}					
			resp.addHeader(DATE_HEADER, dateFormat.format(new Date()));
			resp.send();
		} catch (BadRegistrationException e) {
			log.error("the registration concerning the request " + request + " is wrong, cause : " + e.getReason());
			resp = request.createResponse(e.getStatus(), e.getReason());
			resp.send();
		} 
	}

	// The binding updates MUST be committed (that is, made visible to the proxy or redirect server) if and only if all binding
    // updates and additions succeed.  If any one of them fails (for example, because the back-end database commit failed), the
    // request MUST fail with a 500 (Server Error) response and all tentative binding updates MUST be removed.	
	private void processContacts(SipServletRequest request, boolean wildcard, Registration registration, List<Address> contacts) throws BadRegistrationException {
		String callId = request.getCallId();
		String cseqHeaderValue = request.getHeader(CSEQ_HEADER);
		int cseq = Integer.parseInt(cseqHeaderValue.substring(0, 1));
		
		Set<Binding> bindings = registration.getBindings();
		if (wildcard) {
			for (Binding binding : bindings) {
				if (callId.equals(binding.getCallId()) && cseq < binding.getCSeq()) {
					throw new BadRegistrationException(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "lower cseq");					
				}
				registration.removeBinding(binding);
			}
		} else {
			for (Address contact : contacts) {
				int expires = getContactExpiresValue(request, contact);
										
				Binding binding = findBinding(bindings, contact);						
				if(binding != null) {
					updateOrRemoveExistingBinding(request, registration, callId, cseq, contact, expires, binding);					
				} else if (expires != 0) {
					createBinding(registration, callId, cseq, contact,
							expires);
				}
			}
		}
	}	

	/**
	 * If the binding does exist, the registrar checks the Call-ID value. 
	 * This algorithm ensures that out-of-order requests from the same UA are ignored.
	 * Each binding record records the Call-ID and CSeq values from the request.
	 * 
	 * If the Call-ID value in the existing binding differs from the Call-ID value in the request, the binding MUST be removed if
	 * the expiration time is zero and updated otherwise.
	 * 
	 * If they are the same, the registrar compares the CSeq value.  If the value
	 * is higher than that of the existing binding, it MUST update or remove the binding as above.  If not, the update MUST be
	 * aborted and the request fails.
	 * 
	 * @param request the request used to construct the response if update must be aborted
	 * @param registration the registration on which the binding update or removal should be performed
	 * @param callId the new callId 
	 * @param cseq the new cseq
	 * @param contact the new contact address
	 * @param expires the expires
	 * @param binding the binding to update or remove
	 * @throws BadRegistrationException if an update should be aborted
	 */
	private void updateOrRemoveExistingBinding(
			SipServletRequest request, Registration registration, String callId, int cseq,
			Address contact, int expires, Binding binding) throws BadRegistrationException {
		if (!callId.equals(binding.getCallId())) {
		    // If the Call-ID value in the existing binding differs from the Call-ID value in the request, the binding MUST be removed if
		    // the expiration time is zero and updated otherwise.
			if (expires == 0) {
				removeBinding(registration, binding);
			} else {
				updateBinding(registration, callId, cseq, contact, expires,
						binding);
			}
		} 
		// If they are the same, the registrar compares the CSeq value.  If the value
		// is higher than that of the existing binding, it MUST update or remove the binding as above.  If not, the update MUST be
		// aborted and the request fails.
		else if (cseq > binding.getCSeq()) {
			if (expires == 0) {				
				removeBinding(registration, binding);
			} else {
				updateBinding(registration, callId, cseq, contact, expires,
						binding);
			}
		} else if (cseq < binding.getCSeq()) {
			throw new BadRegistrationException(SipServletResponse.SC_SERVER_INTERNAL_ERROR, "lower cseq");
		}
		CallStateManager.getUserState(registration.getUser().getName()).makeRegistrationsDirty();
	}

	/**
	 * Create and add a binding
	 * @param registration the registration where the new binding should be added
	 * @param callId the callId
	 * @param cseq the cseq
	 * @param contact the contact address
	 * @param expires the expires for the given contact
	 */
	private void createBinding(Registration registration, String callId,
			int cseq, Address contact, int expires) {
		Binding newBinding = new Binding();
		newBinding.setContactAddress(contact.getURI().toString());
		newBinding.setCallId(callId);
		newBinding.setCSeq(cseq);
		newBinding.setExpires(expires);
		newBinding.setRegistration(registration);
		registration.addBinding(newBinding);
		entityManager.persist(newBinding);
		//user = entityManager.merge(u);
		dataLoader.refreshRegistrations();
		entityManager.flush();
		if(log.isDebugEnabled()) {
			log.debug("Added binding: " + newBinding);
		}
		CallStateManager.getUserState(registration.getUser().getName()).makeRegistrationsDirty();
	}
	
	/**
	 * @param registration
	 * @param callId
	 * @param cseq
	 * @param contact
	 * @param expires
	 * @param binding
	 */
	private void updateBinding(Registration registration, String callId,
			int cseq, Address contact, int expires, Binding binding) {
		binding.setContactAddress(contact.getURI().toString());
		binding.setCallId(callId);
		binding.setCSeq(cseq);
		binding.setExpires(expires);
		registration.updateBinding(binding);
		entityManager.persist(binding);
		//user = entityManager.merge(u);
		dataLoader.refreshRegistrations();
		entityManager.flush();	
		if(log.isInfoEnabled()) {
			log.info("Updated binding: " + binding);
		}
	}

	/**
	 * @param registration
	 * @param binding
	 */
	private void removeBinding(Registration registration, Binding binding) {
		registration.removeBinding(binding);
		entityManager.remove(binding);
		//user = entityManager.merge(u);
		dataLoader.refreshRegistrations();
		entityManager.flush();								
		if(log.isInfoEnabled()) {
			log.info("Removed binding: " + binding);
		}
	}

	/**
	 * For each address, the registrar then searches the list of current bindings using the URI comparison rules.
	 * If the binding does not exist, it is tentatively added. 
	 * @param bindings the bindings to look into for the contact 
	 * @param contact the contact to check against the current bindings
	 * @return the binding found, null otherwise
	 */
	private Binding findBinding(Set<Binding> bindings, Address contact) {
		for (Binding binding : bindings) {
			URI contactUri = null;
			try {
				contactUri = sipFactory.createURI(binding.getContactAddress());
			} catch (ServletParseException e) {
				log.error("Invalid URI present in the registrar: " + binding.getContactAddress(), e);
				// TODO shall we remove the binding in this case ?
				return null;
			}
			if (contact.getURI().equals(contactUri)) {
				return binding;
			}
		}
		return null;
	}

	/**
	 * RFC3261, Section 10.3.5 : The registrar extracts the address-of-record from the To header
	 * field of the request.  If the address-of-record is not valid for the domain in the Request-URI, 
	 * the registrar MUST send a 404 (Not Found) response and skip the remaining steps.  The URI
	 * MUST then be converted to a canonical form.  To do that, all URI parameters MUST be removed (including the user-param), and
	 * any escaped characters MUST be converted to their unescaped form.  
	 *  The result serves as an index into the list of bindings.
	 * @param request the REGISTER request used to extract the address of record and to construct the 404 Not Found
	 * @return the registration if it has been found, null otherwise
	 * @throws IOException if the 404 response could not be sent
	 */
	private Registration processAddressOfRecord(SipServletRequest request)
			throws BadRegistrationException {
		URI toURI = request.getTo().getURI();
		String addressOfRecord = URIUtil.toCanonical(toURI);
		Registration registration = sipAuthenticator.authenticate(addressOfRecord);
		if(registration == null) {
			throw new BadRegistrationException(SipServletResponse.SC_NOT_FOUND, "Address of Record not found");			
		}
		return registration;
	}
	
	/**
	 * RFC 3261, Section 10.3.7 : The registrar determines the expiration interval as follows:
	 * -  If the field value has an "expires" parameter, that value MUST be taken as the requested expiration.
	 * -  If there is no such parameter, but the request has an Expires header field, that value MUST be taken as the requested expiration.
	 * -  If there is neither, a locally-configured default value MUST be taken as the requested expiration.
	 * @param request the request used to create the response
	 * @param contact the contact address to check
	 * @return -1 if no expires was found, otherwise the value
	 * @throws BadRegistrationException if the 423 Interval too Brief response should be sent
	 */
	private int getContactExpiresValue(SipServletRequest request, Address contact) throws BadRegistrationException {
		int expires = contact.getExpires();
		
		if (expires < 0) {
			expires = request.getExpires();
		}
		// The registrar MAY choose an expiration less than the requested
        // expiration interval.  If and only if the requested expiration
        // interval is greater than zero AND smaller than one hour AND
        // less than a registrar-configured minimum, the registrar MAY
        // reject the registration with a response of 423 (Interval Too
        // Brief).  This response MUST contain a Min-Expires header field
        // that states the minimum expiration interval the registrar is
        // willing to honor.  It then skips the remaining steps.
		if (expires != 0) {
			if (expires < 0) {
				expires = defaultExpires;
			}
			if (expires > maxExpires) {
				expires = maxExpires;
			} else if (expires < minExpires) {
				throw new BadRegistrationException(SipServletResponse.SC_INTERVAL_TOO_BRIEF);
			}
		}
		return expires;
	}
}
