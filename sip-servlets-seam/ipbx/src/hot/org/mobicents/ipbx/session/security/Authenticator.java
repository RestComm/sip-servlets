package org.mobicents.ipbx.session.security;

import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;
import org.mobicents.ipbx.entity.Role;
import org.mobicents.ipbx.entity.User;


@Name("authenticator")
public class Authenticator
{
    @Logger Log log;
    
    @In Identity identity;
    
    @In EntityManager entityManager;
    
    @Out User user;
    @Out(scope=ScopeType.SESSION, value="sessionUser") User sessionUser;
   
    @SuppressWarnings("deprecation")
	public boolean authenticate()
    {
        log.info("Authenticating #0", identity.getUsername());
        
        //List users = entityManager.createQuery("SELECT user FROM User user").getResultList();
        
        User user = (User) entityManager.createQuery("SELECT user FROM User user WHERE user.name = :userName")
        	.setParameter("userName", identity.getUsername()).getSingleResult();
        
        this.user = user;
        this.sessionUser = user;
        
        if(user == null) {
        	log.info("No such user " + identity.getUsername());
        	return false;
        }
        if(user.getPassword().equals(identity.getPassword())) {
        	identity.addRole("basic");
        	if(user.getRoles() != null) {
        		for(Role role: user.getRoles()) {
        			identity.addRole(role.getRole());
        		}
        	}
        	return true;
        }
        return false;
    }
}
