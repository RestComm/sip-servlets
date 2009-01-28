package org.mobicents.ipbx.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import org.mobicents.ipbx.entity.Contact;

@Name("contactHome")
public class ContactHome extends EntityHome<Contact>
{
    @RequestParameter
    Long contactId;

    @Override
    public Object getId()
    {
        if (contactId == null)
        {
            return super.getId();
        }
        else
        {
            return contactId;
        }
    }

    @Override @Begin
    public void create() {
        super.create();
    }

}
