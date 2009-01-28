package org.mobicents.ipbx.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import org.mobicents.ipbx.entity.User;

@Name("userHome")
public class UserHome extends EntityHome<User>
{
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
