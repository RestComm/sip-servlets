package org.mobicents.ipbx.session;

import java.io.IOException;
import java.util.HashSet;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.mobicents.ipbx.entity.Contact;
import org.mobicents.ipbx.entity.User;

@Name("contactAction")
@Scope(ScopeType.EVENT)
@Transactional
public class ContactAction {
	
	@In(required=false) User user;
	
	@In EntityManager entityManager;
	@In DataLoader dataLoader;
	
	private String contactUri;
	
	public void addContact() {
		if(user == null) return;
		try {
			User u = entityManager.find(User.class, user.getId());
			Contact contact = new Contact();
			contact.setUri(contactUri);
			contact.setUser(u);
			if(u.getContacts() == null) {
				u.setContacts(new HashSet<Contact>());
			}
			u.getContacts().add(contact);
			entityManager.persist(contact);
			//user = entityManager.merge(u);
			entityManager.flush();
			dataLoader.refreshContacts();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Added new contact."));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Error. " +e.getMessage()));
		}
	}

	public String getContactUri() {
		return contactUri;
	}
	public void setContactUri(String contactUri) {
		this.contactUri = contactUri;
	}
}
