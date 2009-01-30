package org.mobicents.ipbx.session;

import java.io.IOException;

import javax.faces.context.FacesContext;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import org.mobicents.ipbx.entity.User;

@Name("userHome")
public class UserHome extends EntityHome<User>
{
    @Override
	public String persist() {
		String ret = super.persist();
		try {
			FacesContext.getCurrentInstance().getExternalContext().redirect("home.seam");
		} catch (IOException e) {
		} 
		return ret;
	}

	@RequestParameter
    Long userId;

    @Override
    public Object getId()
    {
        if (userId == null)
        {
            return super.getId();
        }
        else
        {
            return userId;
        }
    }

    @Override @Begin(join=true)
    public void create() {
        super.create();
    }

}
