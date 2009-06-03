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

import java.util.Iterator;
import java.util.List;

import org.apache.catalina.core.StandardWrapper;
import org.apache.log4j.Logger;
import org.jboss.metadata.javaee.spec.DescriptionGroupMetaData;
import org.jboss.metadata.javaee.spec.DescriptionImpl;
import org.jboss.metadata.javaee.spec.DescriptionsImpl;
import org.jboss.metadata.javaee.spec.DisplayNameImpl;
import org.jboss.metadata.javaee.spec.DisplayNamesImpl;
import org.jboss.metadata.javaee.spec.IconImpl;
import org.jboss.metadata.javaee.spec.IconsImpl;
import org.jboss.metadata.javaee.spec.SecurityRoleRefMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefsMetaData;
import org.jboss.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.jboss.metadata.sip.jboss.JBossServletsMetaData;
import org.jboss.metadata.sip.spec.AndMetaData;
import org.jboss.metadata.sip.spec.ConditionMetaData;
import org.jboss.metadata.sip.spec.ContainsMetaData;
import org.jboss.metadata.sip.spec.EqualMetaData;
import org.jboss.metadata.sip.spec.ExistsMetaData;
import org.jboss.metadata.sip.spec.ListenerMetaData;
import org.jboss.metadata.sip.spec.NotMetaData;
import org.jboss.metadata.sip.spec.OrMetaData;
import org.jboss.metadata.sip.spec.ParamValueMetaData;
import org.jboss.metadata.sip.spec.PatternMetaData;
import org.jboss.metadata.sip.spec.ServletMetaData;
import org.jboss.metadata.sip.spec.ServletSelectionMetaData;
import org.jboss.metadata.sip.spec.SipLoginConfigMetaData;
import org.jboss.metadata.sip.spec.SipResourceCollectionMetaData;
import org.jboss.metadata.sip.spec.SipResourceCollectionsMetaData;
import org.jboss.metadata.sip.spec.SipSecurityConstraintMetaData;
import org.jboss.metadata.sip.spec.SipServletMappingMetaData;
import org.jboss.metadata.sip.spec.SubdomainOfMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.TransportGuaranteeType;
import org.jboss.web.tomcat.service.deployers.JBossContextConfig;
import org.mobicents.servlet.sip.startup.SipContext;
import org.mobicents.servlet.sip.startup.SipDeploymentException;
import org.mobicents.servlet.sip.startup.SipStandardContext;
import org.mobicents.servlet.sip.startup.loading.SipLoginConfig;
import org.mobicents.servlet.sip.startup.loading.SipSecurityConstraint;
import org.mobicents.servlet.sip.startup.loading.SipServletImpl;
import org.mobicents.servlet.sip.startup.loading.SipServletMapping;
import org.mobicents.servlet.sip.startup.loading.rules.AndRule;
import org.mobicents.servlet.sip.startup.loading.rules.ContainsRule;
import org.mobicents.servlet.sip.startup.loading.rules.EqualsRule;
import org.mobicents.servlet.sip.startup.loading.rules.ExistsRule;
import org.mobicents.servlet.sip.startup.loading.rules.MatchingRule;
import org.mobicents.servlet.sip.startup.loading.rules.NotRule;
import org.mobicents.servlet.sip.startup.loading.rules.OrRule;
import org.mobicents.servlet.sip.startup.loading.rules.SubdomainRule;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Startup event listener for a the <b>SipStandardContext</b> that configures
 * the properties of that Context, and the associated defined servlets. It
 * extends the JbossContextConfig to be able to load sip servlet applications.
 * 
 * @author Jean Deruelle
 * 
 */
public class SipJBossContextConfig extends JBossContextConfig {

	private static transient Logger logger = Logger.getLogger(SipJBossContextConfig.class);

	@Override
	protected void processContextParameters() {		
		super.processContextParameters();
		
		JBossWebMetaData metaData = metaDataLocal.get();
		if(metaData instanceof JBossConvergedSipMetaData && context instanceof SipStandardContext) {			
			processSipContextParameters((JBossConvergedSipMetaData)metaData);
		}
	}
	
	/**
	 * Process the context parameters defined in sip.xml. Let a user application
     * 	override the sharedMetaData values.
     */
	protected void processSipContextParameters(JBossConvergedSipMetaData metaData) {
		if(metaData.getSipContextParams() != null) {
			for (org.jboss.metadata.sip.spec.ParamValueMetaData param : metaData.getSipContextParams()) {
				context.addParameter(param.getParamName(), param.getParamValue());
			}
		}
	}

	@Override
	protected void processWebMetaData(JBossWebMetaData metaData) {
		super.processWebMetaData(metaData);
		if(metaData instanceof JBossConvergedSipMetaData && context instanceof SipContext) {			
			processSipMetaData((JBossConvergedSipMetaData)metaData);
		}				
	}

	/**
	 * @param convergedMetaData
	 */
	protected void processSipMetaData(JBossConvergedSipMetaData convergedMetaData) {
		SipContext convergedContext = (SipContext) context;
		convergedContext.setWrapperClass(SipServletImpl.class.getName());
		/* 
		 * sip sepcific treatment 
		 */
		//description
		DescriptionGroupMetaData descriptionGroupMetaData = convergedMetaData.getDescriptionGroup();
		if(descriptionGroupMetaData != null) {
			DescriptionsImpl descriptionsImpl = (DescriptionsImpl)descriptionGroupMetaData.getDescriptions();
			if(descriptionsImpl != null && !descriptionsImpl.isEmpty()) {
				convergedContext.setDescription(((DescriptionImpl)descriptionsImpl.iterator().next()).getDescription());
			}
			IconsImpl iconsImpl = (IconsImpl)descriptionGroupMetaData.getIcons();
			if(iconsImpl != null && !iconsImpl.isEmpty()) {
				IconImpl iconImpl = (IconImpl)iconsImpl.iterator().next();
				convergedContext.setSmallIcon(iconImpl.getSmallIcon());
				convergedContext.setLargeIcon(iconImpl.getLargeIcon());
			}
			DisplayNamesImpl displayNamesImpl = (DisplayNamesImpl)descriptionGroupMetaData.getDisplayNames();
			if(displayNamesImpl != null && !displayNamesImpl.isEmpty()) {
				convergedContext.setDisplayName(((DisplayNameImpl)displayNamesImpl.iterator().next()).getDisplayName());
			}
		}
		//app name
		if(convergedMetaData.getApplicationName() == null) {
			throw new SipDeploymentException("No app-name present in the sip.xml deployment descriptor or no SipApplication annotation defined");
		}
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
			} 
			
			if(servletSelectionMetaData.getSipServletMappings() != null && servletSelectionMetaData.getSipServletMappings().size() > 0) {
				if(servletSelectionSet) {
					throw new SipDeploymentException("the main servlet and servlet mapping cannot be present at the same time in sip.xml or as annotations !");	
				}
				Iterator<SipServletMappingMetaData> sipServletMapping = servletSelectionMetaData.getSipServletMappings().iterator();
				while (sipServletMapping.hasNext()) {
					SipServletMappingMetaData sipServletMappingMetaData = (SipServletMappingMetaData) sipServletMapping
							.next();
					SipServletMapping sipMapping = new SipServletMapping();
					sipMapping.setServletName(sipServletMappingMetaData.getServletName());
					PatternMetaData  pattern = sipServletMappingMetaData.getPattern();
					MatchingRule matchingRule = buildRule(pattern.getCondition());
					sipMapping.setMatchingRule(matchingRule);
					convergedContext.addSipServletMapping(sipMapping);					
				}
				servletSelectionSet = true;
			} 
			
			if(servletSelectionMetaData.getSipRubyController() != null) {
				convergedContext.setSipRubyController(servletSelectionMetaData.getSipRubyController());
				servletSelectionSet = true;
			}
		}
		//Sip Servlet
		JBossServletsMetaData sipServlets = convergedMetaData.getSipServlets();
		if (sipServlets != null) {
			if(sipServlets.size() > 1 && !servletSelectionSet) {
				throw new SipDeploymentException("the main servlet is not set and there is more than one servlet defined in the sip.xml or as annotations !");
			}
			for (ServletMetaData value : sipServlets) {
				SipServletImpl wrapper = (SipServletImpl)convergedContext.createWrapper();
				wrapper.setName(value.getName());
				// no main servlet defined in the sip.xml we take the name of the only sip servlet present
				if(!servletSelectionSet) {
					convergedContext.setMainServlet(value.getName());
				}
				wrapper.setServletClass(value.getServletClass());
				wrapper.setServletName(value.getServletName());
				if (value.getJspFile() != null) {
					wrapper.setJspFile(value.getJspFile());
				}
				wrapper.setLoadOnStartup(value.getLoadOnStartup());
				if (value.getRunAs() != null) {
					wrapper.setRunAs(value.getRunAs().getRoleName());
				}
				List<? extends ParamValueMetaData> params = value.getInitParam();
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
		convergedContext.setConcurrencyControlMode(convergedMetaData.getConcurrencyControlMode());
		convergedContext.setWrapperClass(StandardWrapper.class.getName());
	}
	
	public static MatchingRule buildRule(ConditionMetaData condition) {
		
		
		if (condition instanceof AndMetaData) {
			AndMetaData andMetaData = (AndMetaData) condition;
			AndRule and = new AndRule();
			List<ConditionMetaData> list = andMetaData.getConditions();
			for(ConditionMetaData newCondition : list) {
				and.addCriterion(buildRule(newCondition));
			}
			return and;
		} else if (condition instanceof EqualMetaData) {
			EqualMetaData equalMetaData = (EqualMetaData) condition;
			String var = equalMetaData.getVar();
			String value = equalMetaData.getValue();
			boolean ignoreCase = equalMetaData.isIgnoreCase();
			return new EqualsRule(var, value, ignoreCase);
		} else if (condition instanceof SubdomainOfMetaData) {
			SubdomainOfMetaData subdomainOfMetaData = (SubdomainOfMetaData) condition;
			String var = subdomainOfMetaData.getVar();
			String value = subdomainOfMetaData.getValue();
			return new SubdomainRule(var, value);
		} else if (condition instanceof OrMetaData) {
			OrMetaData orMetaData = (OrMetaData) condition;
			OrRule or = new OrRule();
			List<ConditionMetaData> list = orMetaData.getConditions();
			for(ConditionMetaData newCondition : list) {
				or.addCriterion(buildRule(newCondition));
			}
			return or;
		} else if (condition instanceof NotMetaData) {
			NotMetaData notMetaData = (NotMetaData) condition;
			NotRule not = new NotRule();
			not.setCriterion(buildRule(notMetaData.getCondition()));
			return not;
		} else if (condition instanceof ContainsMetaData) {
			ContainsMetaData containsMetaData = (ContainsMetaData) condition;
			String var = containsMetaData.getVar();
			String value = containsMetaData.getValue();
			boolean ignoreCase = containsMetaData.isIgnoreCase();
			return new ContainsRule(var, value, ignoreCase);
		} else if (condition instanceof ExistsMetaData) {
			ExistsMetaData existsMetaData = (ExistsMetaData) condition;
			return new ExistsRule(existsMetaData.getVar());
		} else {
			throw new IllegalArgumentException("Unknown rule: " + condition);
		} 
	} 


}
