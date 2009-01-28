package org.mobicents.ipbx.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import org.mobicents.ipbx.entity.Registration;

@Name("registrationHome")
public class RegistrationHome extends EntityHome<Registration>
{
    @RequestParameter
    Long registrationId;

    @Override
    public Object getId()
    {
        if (registrationId == null)
        {
            return super.getId();
        }
        else
        {
            return registrationId;
        }
    }

    @Override @Begin
    public void create() {
        super.create();
    }

}
