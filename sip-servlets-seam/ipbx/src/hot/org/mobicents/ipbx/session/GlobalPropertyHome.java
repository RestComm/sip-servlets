package org.mobicents.ipbx.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.core.Events;
import org.jboss.seam.framework.EntityHome;

import org.mobicents.ipbx.entity.GlobalProperty;

@Name("globalPropertyHome")
public class GlobalPropertyHome extends EntityHome<GlobalProperty>
{
    @Override
	public String persist() {
		String ret = super.persist();
		Events.instance().raiseEvent("globalSettingsChanged");
		return ret;
	}

	@Override
	public String update() {
		String ret = super.update();
		Events.instance().raiseEvent("globalSettingsChanged");
		return ret;
	}

	@RequestParameter
    Long globalPropertyId;

    @Override
    public Object getId()
    {
        if (globalPropertyId == null)
        {
            return super.getId();
        }
        else
        {
            return globalPropertyId;
        }
    }

    @Override @Begin(join=true)
    public void create() {
        super.create();
    }

}
