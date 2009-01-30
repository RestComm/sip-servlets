package org.mobicents.ipbx.session.call.logging;

import java.util.HashSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipSession;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.mobicents.ipbx.entity.Contact;
import org.mobicents.ipbx.entity.History;
import org.mobicents.ipbx.entity.User;
import org.mobicents.ipbx.session.DataLoader;
import org.mobicents.ipbx.session.call.model.CallStateManager;
import org.mobicents.ipbx.session.util.DateUtil;

@Name("callHistory")
@Scope(ScopeType.STATELESS)
@Transactional
public class CallHistory {
	@In EntityManagerFactory ipbxEntityManagerFactory;
	@In SipSession sipSession;
	@In DataLoader dataLoader;
	@In CallStateManager callStateManager;
	
	@Observer("RESPONSE")
	public void response(SipServletResponse response) {
		int status = response.getStatus();
		if(status >= 300) {
			String message = "Call to " + response.getTo().getURI() +
			" has failed with status code " + status;
			addHistory(message);
		} else if (status >= 200) {
			String method = response.getRequest().getMethod();
			if(method.equalsIgnoreCase("INVITE")) {
				String message = "Call to " + response.getTo().getURI() +
				" has succeeded with status code " + status;
				addHistory(message);
			} else if(method.equalsIgnoreCase("BYE")) {
				String message = "Call with " + response.getTo().getURI() +
				" has ended with status code " + status;
				addHistory(message);
			}
		}
	}
	
	@Observer("incomingCall")
	public void acceptedCall(SipServletRequest request) {
		addHistory("Incoming call from " + request.getFrom().getURI());
	}
	
	@Observer("rejectedCall")
	public void rejectedCall(String from, String to) {
		// TODO
	}
	
	public void addHistory(String message) {
		try {
			EntityManager em = ipbxEntityManagerFactory.createEntityManager();
			User user = (User) sipSession.getAttribute("user");
			User u = em.find(User.class, user.getId());
			History history = new History();
			history.setMessage(message);
			history.setTimestamp(DateUtil.now());
			history.setUser(u);
			if(u.getHistory() == null) {
				u.setHistory(new HashSet<History>());
			}
			u.getHistory().add(history);
			em.persist(history);
			callStateManager.getCurrentState(user.getName()).makeHistoryDirty();
		} catch (Exception e) {}
	}
	

}
