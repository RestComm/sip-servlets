package org.mobicents.ipbx.session.security;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.sip.SipSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.mobicents.ipbx.entity.Registration;
import org.mobicents.ipbx.entity.User;

@Name("sipAuthenticator")
@Scope(ScopeType.STATELESS)
public class SimpleSipAuthenticator {
	    @In EntityManager entityManager;
	    @In(required=false) SipSession sipSession;
	    @In(required=false) @Out(required=false) User user;
	   
	    public Registration authenticate(String uri) {
	    	List<Registration> registrations = entityManager.createQuery(
	    			"SELECT registration FROM Registration registration WHERE registration.uri = :requestUri")
        		.setParameter("requestUri", uri).getResultList();
	    	
	    	if(registrations.size() <= 0) return null;
	    	
	    	Registration reg = registrations.get(0);
	    	
	    	User user = reg.getUser();
	    	
	    	if(user == null) return null;
	    	
	    	this.user = user;
	    	sipSession.setAttribute("user", user);
	        
	        return reg;
	    }
	    
	    public Registration findRegistration(String uri) {
	    	List<Registration> registrations = entityManager.createQuery(
	    	"SELECT registration FROM Registration registration WHERE registration.uri = :requestUri")
	    	.setParameter("requestUri", uri).getResultList();

	    	if(registrations.size() <= 0) return null;

	    	Registration reg = registrations.get(0);

	    	return reg;
	    }
}
