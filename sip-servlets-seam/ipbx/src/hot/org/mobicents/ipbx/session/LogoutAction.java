package org.mobicents.ipbx.session;

import java.io.IOException;

import javax.faces.context.FacesContext;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.Identity;

@Name("logoutAction")
public class LogoutAction {
	@In Identity identity;
	public void logout() throws IOException {
		identity.logout();
		FacesContext.getCurrentInstance().getExternalContext().redirect("home.seam"); 
	}
}
