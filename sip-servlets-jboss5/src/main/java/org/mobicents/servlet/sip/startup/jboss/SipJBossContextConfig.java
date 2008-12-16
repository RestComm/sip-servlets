/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.startup.jboss;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.jboss.metadata.sip.spec.SipLoginConfigMetaData;
import org.jboss.metadata.sip.spec.SipResourceCollectionMetaData;
import org.jboss.metadata.sip.spec.SipResourceCollectionsMetaData;
import org.jboss.metadata.sip.spec.SipSecurityConstraintMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.jboss.metadata.web.spec.TransportGuaranteeType;
import org.jboss.web.tomcat.service.deployers.JBossContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.startup.loading.SipLoginConfig;
import org.mobicents.servlet.sip.startup.loading.SipSecurityConstraint;

/**
 * Startup event listener for a the <b>SipStandardContext</b> that configures
 * the properties of that Context, and the associated defined servlets. It
 * extends the JbossContextConfig to be able to load sip servlet applications.
 * 
 * @author Jean Deruelle
 * 
 */
public class SipJBossContextConfig extends JBossContextConfig {

	private static transient Log logger = LogFactory
			.getLog(SipJBossContextConfig.class);

	protected void processWebMetaData(JBossWebMetaData metaData) {
		super.processWebMetaData(metaData);
		if(metaData instanceof JBossConvergedSipMetaData && context instanceof SipStandardContext) {			
			JBossConvergedSipMetaData convergedMetaData = (JBossConvergedSipMetaData) metaData;
			SipStandardContext convergedContext = (SipStandardContext) context;
			
			/* 
			 * sip sepcific treatment 
			 */
			//app name
			convergedContext.setApplicationName(convergedMetaData.getApplicationName());
			//servlet selection
			String mainServlet = convergedMetaData.getServletSelection().getMainServlet();
			if(mainServlet != null && mainServlet.length() > 0) {
				convergedContext.setMainServlet(mainServlet);
			} else if(convergedMetaData.getServletSelection().getSipServletMappings().size() > 0) {
				
			}
			//sip proxy config
			convergedContext.setProxyTimeout(convergedMetaData.getProxyConfig().getProxyTimeout());
			//sip session config
			convergedContext.setSipApplicationSessionTimeout(convergedMetaData.getSessionConfig().getSessionTimeout());

			//sip security contstraints
			for(SipSecurityConstraintMetaData sipConstraintMetaData : convergedMetaData.getSipSecurityContraints()) {
				SipSecurityConstraint sipSecurityConstraint = new SipSecurityConstraint();
				sipSecurityConstraint.setDisplayName(sipConstraintMetaData.getDisplayName());
				for(String role : sipConstraintMetaData.getAuthConstraint().getRoleNames()) {
					sipSecurityConstraint.addAuthRole(role);
				}
				if(sipConstraintMetaData.getProxyAuthentication() != null) {
					sipSecurityConstraint.setProxyAuthentication(true);
				}
				TransportGuaranteeType tg = sipConstraintMetaData.getTransportGuarantee();
				sipSecurityConstraint.setUserConstraint(tg.name());
				
				SipResourceCollectionsMetaData srcs = sipConstraintMetaData.getResourceCollections();
				if (srcs != null) {
					for (SipResourceCollectionMetaData src : srcs) {
						org.apache.catalina.deploy.SecurityCollection securityCollection = new org.apache.catalina.deploy.SecurityCollection();
						securityCollection.setName(src.getName());
						List<String> methods = src.getSipMethods();
						if (methods != null) {
							for (String method : src.getSipMethods()) {
								securityCollection.addMethod(method);
							}
						}
						List<String> servletNames = src.getServletNames();
						if (servletNames != null) {
							for (String servletName : servletNames) {
								securityCollection.addPattern(servletName);
							}
						}
						sipSecurityConstraint.addCollection(securityCollection);
					}
				}
			}
			//sip login config
			SipLoginConfigMetaData sipLoginConfig = convergedMetaData.getSipLoginConfig();
			if (sipLoginConfig != null) {
				SipLoginConfig sipLoginConfig2 = new SipLoginConfig();
				sipLoginConfig2.setAuthMethod(sipLoginConfig.getAuthMethod());
				sipLoginConfig2.setRealmName(sipLoginConfig.getRealmName());
				sipLoginConfig2.addIdentityAssertion(sipLoginConfig.getIdentityAssertion().getIdentityAssertionScheme(), sipLoginConfig.getIdentityAssertion().getIdentityAssertionSupport());				
				convergedContext.setSipLoginConfig(sipLoginConfig2);
			}
			//Sip Listeners
			List<ListenerMetaData> sipListeners = convergedMetaData.getSipListeners();
			if (sipListeners != null) {
				for (ListenerMetaData value : sipListeners) {
					convergedContext.addSipApplicationListener(value.getListenerClass());
				}
			}
		}				
	}

}
