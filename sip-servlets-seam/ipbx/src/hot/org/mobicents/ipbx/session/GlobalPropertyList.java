package org.mobicents.ipbx.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.mobicents.ipbx.entity.GlobalProperty;

@Name("globalPropertyList")
public class GlobalPropertyList extends EntityQuery<GlobalProperty>
{
    public GlobalPropertyList()
    {
        setEjbql("select globalProperty from GlobalProperty globalProperty");
    }
}
