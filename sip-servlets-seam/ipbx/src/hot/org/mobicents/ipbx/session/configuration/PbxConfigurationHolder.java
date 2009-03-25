package org.mobicents.ipbx.session.configuration;

import java.util.HashMap;
import java.util.List;

import org.mobicents.ipbx.entity.GlobalProperty;
import org.mobicents.ipbx.entity.PstnGatewayAccount;

public class PbxConfigurationHolder {
	static List<PstnGatewayAccount> pstnAccounts;
	static HashMap<String,GlobalProperty> globalProperties;
}
