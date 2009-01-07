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

import org.apache.catalina.core.StandardWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefsMetaData;
import org.jboss.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.jboss.metadata.sip.spec.ServletSelectionMetaData;
import org.jboss.metadata.sip.spec.SipLoginConfigMetaData;
import org.jboss.metadata.sip.spec.SipResourceCollectionMetaData;
import org.jboss.metadata.sip.spec.SipResourceCollectionsMetaData;
import org.jboss.metadata.sip.spec.SipSecurityConstraintMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.metadata.web.spec.ServletsMetaData;
import org.jboss.metadata.web.spec.TransportGuaranteeType;
import org.jboss.web.tomcat.service.deployers.JBossContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.startup.loading.SipLoginConfig;
import org.mobicents.servlet.sip.startup.loading.SipSecurityConstraint;
import org.mobicents.servlet.sip.startup.loading.SipServletImpl;

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

	@Override
	protected void processWebMetaData(JBossWebMetaData metaData) {
		super.processWebMetaData(metaData);
		if(metaData instanceof JBossConvergedSipMetaData && context instanceof SipStandardContext) {			
			processSipMetaData((JBossConvergedSipMetaData)metaData);
		}				
	}

	/**
	 * @param convergedMetaData
	 */
	protected void processSipMetaData(JBossConvergedSipMetaData convergedMetaData) {
		SipStandardContext convergedContext = (SipStandardContext) context;
		convergedContext.setWrapperClass(SipServletImpl.class.getName());
		/* 
		 * sip sepcific treatment 
		 */
		//app name
		convergedContext.setApplicationName(convergedMetaData.getApplicationName());		
		//sip proxy config
		if(convergedMetaData.getProxyConfig() != null) {
			convergedContext.setProxyTimeout(convergedMetaData.getProxyConfig().getProxyTimeout());
		}
		//sip session config
		if(convergedMetaData.getSessionConfig() != null) {
			convergedContext.setSipApplicationSessionTimeout(convergedMetaData.getSessionConfig().getSessionTimeout());
		}

		//sip security contstraints
		List<SipSecurityConstraintMetaData> sipConstraintMetaDatas = convergedMetaData.getSipSecurityContraints();
		if(sipConstraintMetaDatas != null) {
			for(SipSecurityConstraintMetaData sipConstraintMetaData : sipConstraintMetaDatas) {
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
		
		//servlet selection
		boolean servletSelectionSet = false;
		ServletSelectionMetaData servletSelectionMetaData = convergedMetaData.getServletSelection();
		if(servletSelectionMetaData != null) {
			String mainServlet = servletSelectionMetaData.getMainServlet();
			if(mainServlet != null && mainServlet.length() > 0) {
				convergedContext.setMainServlet(mainServlet);
				servletSelectionSet = true;
			} else if(servletSelectionMetaData.getSipServletMappings() != null || servletSelectionMetaData.getSipServletMappings().size() > 0) {
				servletSelectionSet = true;
			}
		}
		//Sip Servlet
		ServletsMetaData servlets = convergedMetaData.getSipServlets();
		if (servlets != null) {
			if(servlets.size() > 1 && !servletSelectionSet) {
				throw new IllegalArgumentException("the main servlet is not set and there is more than one servlet defined in the sip.xml or as annotations !");
			}
			for (ServletMetaData value : servlets) {
				org.apache.catalina.Wrapper wrapper = convergedContext.createWrapper();
				wrapper.setName(value.getName());
				// no main servlet defined in the sip.xml we take the name of the only sip servlet present
				if(!servletSelectionSet) {
					convergedContext.setMainServlet(value.getName());
				}
				wrapper.setServletClass(value.getServletClass());
				if (value.getJspFile() != null) {
					wrapper.setJspFile(value.getJspFile());
				}
				wrapper.setLoadOnStartup(value.getLoadOnStartup());
				if (value.getRunAs() != null) {
					wrapper.setRunAs(value.getRunAs().getRoleName());
				}
				List<ParamValueMetaData> params = value.getInitParam();
				if (params != null) {
					for (ParamValueMetaData param : params) {
						wrapper.addInitParameter(param.getParamName(), param
								.getParamValue());
					}
				}
				SecurityRoleRefsMetaData refs = value.getSecurityRoleRefs();
				if (refs != null) {
					for (SecurityRoleRefMetaData ref : refs) {
						wrapper.addSecurityReference(ref.getRoleName(), ref
								.getRoleLink());
					}
				}
				convergedContext.addChild((SipServletImpl)wrapper);
			}
		}
		convergedContext.setSipApplicationKeyMethod(convergedMetaData.getSipApplicationKeyMethod());
		convergedContext.setWrapperClass(StandardWrapper.class.getName());
	}

}
