package org.mobicents.ipbx.session;

import java.util.LinkedList;

import javax.persistence.EntityManager;

import org.hibernate.engine.QueryParameters;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.mobicents.ipbx.entity.Registration;
import org.mobicents.ipbx.entity.User;

@Name("registrationList")
public class RegistrationList extends EntityQuery<Registration>
{
    public RegistrationList()
    {
        setEjbql("select registration from Registration registration");
        LinkedList restrictions = new LinkedList();
        restrictions.add("registration.user = #{user}");
        setRestrictionExpressionStrings(restrictions);
    }
}
