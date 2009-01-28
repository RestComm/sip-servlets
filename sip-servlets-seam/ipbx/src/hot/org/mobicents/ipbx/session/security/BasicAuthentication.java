package org.mobicents.ipbx.session.security;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.sip.*;

import org.mobicents.ipbx.entity.User;

public class BasicAuthentication {
	public SipServletResponse getAuthResponse(SipServletRequest request, String realm) {
		if(authenticate(request))
			return null;
		
		SipServletResponse response = request.createResponse(401);
		response.setHeader("WWW-Authenticate", " Basic realm=\"" + realm + "\"");
		return response;
	}
	
	public boolean authenticate(SipServletRequest request) {
		String auth = request.getHeader("Authorization");
		if(auth != null) {
			String base = auth.replace("Basic", "").trim();
			String decodedBase = new String(Base64.decode(base));
			String[] tokens = decodedBase.split(":");
			String user = tokens[0];
			String pass = tokens[1];
			
			EntityManagerFactory factory = Persistence.createEntityManagerFactory("anpbx");
			EntityManager manager = factory.createEntityManager();
			manager.getTransaction().begin();
			User u = (User) manager.createQuery("select user from User user where user.name=:uname")
				.setParameter("uname", user).getSingleResult();
			manager.getTransaction().commit();
			
			if(pass.equals(u.getPassword()))
				return true;
		}
		return false;
		
	}
}
