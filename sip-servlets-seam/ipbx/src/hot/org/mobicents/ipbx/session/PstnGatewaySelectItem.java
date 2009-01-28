package org.mobicents.ipbx.session;

import javax.faces.model.SelectItem;

import org.mobicents.ipbx.entity.PstnGatewayAccount;

public class PstnGatewaySelectItem extends SelectItem {
	@Override
	public String getLabel() {
		return pstnAcc.getName();
	}
	@Override
	public Object getValue() {
		return pstnAcc;
	}
	private PstnGatewayAccount pstnAcc;
	public PstnGatewaySelectItem(PstnGatewayAccount account) {
		this.pstnAcc = account;
	}

}
