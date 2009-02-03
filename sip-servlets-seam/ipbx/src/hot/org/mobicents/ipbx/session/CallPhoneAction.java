package org.mobicents.ipbx.session;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("callPhoneAction")
public class CallPhoneAction {
	@In(create=true) CallAction callAction;
	
	private String number;
	
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public void call() {
		callAction.call(number);
	}
}
