package org.mobicents.ipbx.session.configuration;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.*;
import org.jboss.seam.log.Log;
import org.mobicents.ipbx.entity.GlobalProperty;
import org.mobicents.ipbx.entity.PstnGatewayAccount;

@Name("pbxConfiguration")
@Scope(ScopeType.APPLICATION)
@Startup
@Transactional
public class PbxConfiguration {
	@In EntityManager entityManager;
	@Logger Log log;
	
	
	public static List<PstnGatewayAccount> getPstnAccounts() {
		return PbxConfigurationHolder.pstnAccounts;
	}
	
	
	public static HashMap<String,GlobalProperty> getGlobalProperties() {
		return PbxConfigurationHolder.globalProperties;
	}
	
	public static String getProperty(String property) {
		return getGlobalProperties().get(property).getValue();
	}

	@Create
	@Observer("globalSettingsChanged")
	public void updateAll() throws InterruptedException {
		Thread.sleep(300);
		log.info("Global settings updated");
		loadPstnAccounts();
		loadGlobalProperties();
	}
	
	public void loadGlobalProperties() {
        List<GlobalProperty> props = entityManager.createQuery("SELECT global FROM GlobalProperty global").getResultList();
        HashMap<String,GlobalProperty> globalProperties = new HashMap<String, GlobalProperty>();
		for(GlobalProperty p : props) {
			globalProperties.put(p.getName(), p);
		}
		PbxConfigurationHolder.globalProperties = globalProperties;
	}
	
	public void loadPstnAccounts() {
		PbxConfigurationHolder.pstnAccounts = entityManager.createQuery("SELECT pstn FROM PstnGatewayAccount pstn").getResultList();
	}
}
