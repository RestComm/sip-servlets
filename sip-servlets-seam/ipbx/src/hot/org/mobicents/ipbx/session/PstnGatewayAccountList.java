package org.mobicents.ipbx.session;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.mobicents.ipbx.entity.PstnGatewayAccount;

@Name("pstnGatewayAccountList")
public class PstnGatewayAccountList extends EntityQuery<PstnGatewayAccount>
{
    public PstnGatewayAccountList()
    {
        setEjbql("select pstnGatewayAccount from PstnGatewayAccount pstnGatewayAccount");
    }
    
    public List<PstnGatewaySelectItem> getSelectItemResultList() {
    	List<PstnGatewayAccount> list = this.getResultList();
    	LinkedList<PstnGatewaySelectItem> selectItems = new LinkedList<PstnGatewaySelectItem>();
    	Iterator<PstnGatewayAccount> pacc = list.iterator();
    	while (pacc.hasNext()) {
    		selectItems.add(new PstnGatewaySelectItem(pacc.next()));
    	}
    	return selectItems;
    }
}
