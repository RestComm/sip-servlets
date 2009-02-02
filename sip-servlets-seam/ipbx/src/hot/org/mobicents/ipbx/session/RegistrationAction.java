package org.mobicents.ipbx.session;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Transactional;
import org.mobicents.ipbx.entity.Binding;
import org.mobicents.ipbx.entity.Registration;
import org.mobicents.ipbx.entity.User;
import org.jboss.seam.annotations.Name;


@Name("registrationAction")
@Transactional
public class RegistrationAction {
	@In(required=false) User user;
	@In DataLoader dataLoader;
	
	@In EntityManager entityManager;
	
	private String registrationUri;
	
	public String getRegistrationUri() {
		return registrationUri;
	}

	public void setRegistrationUri(String registrationUri) {
		this.registrationUri = registrationUri;
	}

	public void addRegistration() {
		if(user == null) return;
		try {
			User u = entityManager.find(User.class, user.getId());
			Registration registration = new Registration();
			registration.setUri(registrationUri);
			registration.setUser(u);
			if(u.getRegistrations() == null) {
				u.setRegistrations(new HashSet<Registration>());
			}
			u.getRegistrations().add(registration);
			entityManager.persist(registration);
			//user = entityManager.merge(u);
			dataLoader.refreshRegistrations();
			entityManager.flush();
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Added new registration."));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Error. Try Again!"));
		}
	}
	
	public List<Binding> getBindings(Registration registration) {
		LinkedList<Binding> bindings = new LinkedList<Binding>();
		Iterator<Binding> i = registration.getBindings().iterator();
		while(i.hasNext()) {
			bindings.add(i.next());
		}
		return bindings;
	}
	
	public void select(Registration reg) {
		try {
			entityManager.merge(reg);
			entityManager.flush();
		} catch (Exception e) {}
		//reg.setSelected(!reg.isSelected());
	}
	
	public void remove(Registration reg) {
		try {
			entityManager.remove(reg);
		} catch (Exception e) {}
		//reg.setSelected(!reg.isSelected());
	}

}
