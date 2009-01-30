package org.mobicents.ipbx.session;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.web.RequestParameter;
import org.jboss.seam.framework.EntityHome;

import org.mobicents.ipbx.entity.PstnGatewayAccount;

@Name("pstnGatewayAccountHome")
public class PstnGatewayAccountHome extends EntityHome<PstnGatewayAccount>
{
    @RequestParameter
    Long pstnGatewayAccountId;

    @Override
    public Object getId()
    {
        if (pstnGatewayAccountId == null)
        {
            return super.getId();
        }
        else
        {
            return pstnGatewayAccountId;
        }
    }

    @Override @Begin(join=true)
    public void create() {
        super.create();
    }

}
