package org.mobicents.ipbx.session;

import java.util.LinkedList;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.mobicents.ipbx.entity.Contact;

@Name("contactList")
public class ContactList extends EntityQuery<Contact>
{
    public ContactList()
    {
        setEjbql("select contact from Contact contact");
        LinkedList restrictions = new LinkedList();
        restrictions.add("contact.user = #{user}");
        setRestrictionExpressionStrings(restrictions);
    }
}
