package org.mobicents.ipbx.session;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.remoting.WebRemote;
import org.mobicents.ipbx.entity.History;
import org.mobicents.ipbx.entity.User;

@Name("dataLoader")
@Scope(ScopeType.APPLICATION)
@Startup
@Transactional
public class DataLoader {
	// This is a temporary hack to make the history work
	public static HashMap<String, List> history = new HashMap<String, List>();
	private String text;
	@In(scope=ScopeType.SESSION, required=false) @Out(scope=ScopeType.SESSION, required=false) List registrationCache;
	@In(scope=ScopeType.SESSION, required=false) @Out(scope=ScopeType.SESSION, required=false) List contactCache;
	@In(scope=ScopeType.SESSION, required=false) @Out(scope=ScopeType.SESSION, required=false) List historyCache;
	
	@In EntityManager entityManager;
	@In(required=false) User user;
	
	@WebRemote
	public String work(String name) {
		System.out.println("work..." + name);
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("end of work...");
		text = "We are Here" + name;
		return text;
	}
	
	public List loadContacts() {
		try {
			refreshContacts();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return contactCache;
	}
	
	public void refreshContacts() {
		User u = entityManager.find(User.class, user.getId());
		contactCache = entityManager.createQuery(
			"SELECT contact FROM Contact contact where contact.user=:u")
			.setParameter("u", u).getResultList();
	}
	
	public List loadRegistrations() {
		try {
			refreshRegistrations();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return registrationCache;
 	}
	
	public void refreshRegistrations() {
		User u = entityManager.find(User.class, user.getId());
		registrationCache = entityManager.createQuery(
		   "SELECT registration FROM Registration registration where registration.user=:u")
		   .setParameter("u", u).getResultList();
	}
	
	public List loadHistory() {
		try {
			refreshHistory();
		} catch (Exception e) {
			e.printStackTrace();
		}
        return history.get(user.getName());
	}
	
	public void refreshHistory() {
		if(history.get(user.getName()) == null) history.put(user.getName(), new LinkedList<History>());
		/*
		User u = entityManager.find(User.class, user.getId());
		historyCache = entityManager.createQuery("SELECT history FROM History history where history.user=:u order by history.timestamp desc").setParameter("u", u).getResultList();
		 */
	}
}
