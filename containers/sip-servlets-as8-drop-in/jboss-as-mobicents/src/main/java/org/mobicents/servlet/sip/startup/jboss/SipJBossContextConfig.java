/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012.
 * and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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

import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import io.undertow.servlet.api.ServletInfo;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.sip.SipServletRequest;

import org.jboss.as.naming.ManagedReference;
import org.jboss.as.naming.ManagedReferenceFactory;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.web.common.ServletContextAttribute;
import org.jboss.metadata.javaee.spec.DescriptionGroupMetaData;
import org.jboss.metadata.javaee.spec.DescriptionImpl;
import org.jboss.metadata.javaee.spec.DescriptionsImpl;
import org.jboss.metadata.javaee.spec.DisplayNameImpl;
import org.jboss.metadata.javaee.spec.DisplayNamesImpl;
import org.jboss.metadata.javaee.spec.IconImpl;
import org.jboss.metadata.javaee.spec.IconsImpl;
import org.jboss.metadata.javaee.spec.ParamValueMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefMetaData;
import org.jboss.metadata.javaee.spec.SecurityRoleRefsMetaData;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.jboss.metadata.web.spec.ServletMetaData;
import org.jboss.metadata.web.spec.TransportGuaranteeType;
import org.mobicents.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.mobicents.metadata.sip.jboss.JBossSipServletsMetaData;
import org.mobicents.metadata.sip.spec.AndMetaData;
import org.mobicents.metadata.sip.spec.ConditionMetaData;
import org.mobicents.metadata.sip.spec.ContainsMetaData;
import org.mobicents.metadata.sip.spec.EqualMetaData;
import org.mobicents.metadata.sip.spec.ExistsMetaData;
import org.mobicents.metadata.sip.spec.NotMetaData;
import org.mobicents.metadata.sip.spec.OrMetaData;
import org.mobicents.metadata.sip.spec.PatternMetaData;
import org.mobicents.metadata.sip.spec.SipApplicationKeyMethodInfo;
import org.mobicents.metadata.sip.spec.SipLoginConfigMetaData;
import org.mobicents.metadata.sip.spec.SipResourceCollectionMetaData;
import org.mobicents.metadata.sip.spec.SipResourceCollectionsMetaData;
import org.mobicents.metadata.sip.spec.SipSecurityConstraintMetaData;
import org.mobicents.metadata.sip.spec.SipServletMappingMetaData;
import org.mobicents.metadata.sip.spec.SipServletSelectionMetaData;
import org.mobicents.metadata.sip.spec.SubdomainOfMetaData;
import org.mobicents.servlet.sip.undertow.SipContextImpl;
import org.mobicents.servlet.sip.undertow.SipDeploymentException;
import org.mobicents.servlet.sip.undertow.SipLoginConfig;
import org.mobicents.servlet.sip.undertow.SipSecurityCollection;
import org.mobicents.servlet.sip.undertow.SipSecurityConstraint;
import org.mobicents.servlet.sip.undertow.SipServletImpl;
import org.mobicents.servlet.sip.undertow.rules.AndRule;
import org.mobicents.servlet.sip.undertow.rules.ContainsRule;
import org.mobicents.servlet.sip.undertow.rules.EqualsRule;
import org.mobicents.servlet.sip.undertow.rules.ExistsRule;
import org.mobicents.servlet.sip.undertow.rules.NotRule;
import org.mobicents.servlet.sip.undertow.rules.OrRule;
import org.mobicents.servlet.sip.undertow.rules.SubdomainRule;
import org.mobicents.servlet.sip.core.descriptor.MatchingRule;
import org.mobicents.servlet.sip.startup.loading.SipServletMapping;
import org.wildfly.extension.undertow.deployment.UndertowDeploymentInfoService;


/**
 * Startup event listener for a the <b>SipStandardContext</b> that configures the properties of that Context, and the associated
 * defined servlets. It extends the JbossContextConfig to be able to load sip servlet applications.
 *
 * @author Jean Deruelle
 *
 * This class is based on org.mobicents.servlet.sip.startup.jboss.SipJBossContextConfig class from jboss-as7-mobicents project, re-implemented for jboss as8 (wildfly) by:
 * @author kakonyi.istvan@alerant.hu
 *
 */
public class SipJBossContextConfig{
    DeploymentUnit deploymentUnit;
    UndertowDeploymentInfoService deploymentInfoservice;

    public SipJBossContextConfig(DeploymentUnit deploymentUnitContext, UndertowDeploymentInfoService deploymentInfoservice) {
        this.deploymentUnit = deploymentUnitContext;
        this.deploymentInfoservice = deploymentInfoservice;
    }

    /**
     * Process the context parameters defined in sip.xml. Let a user application override the sharedMetaData values.
     */
    protected void processSipContextParameters(JBossConvergedSipMetaData metaData) {
        if (metaData.getSipContextParams() != null) {
            for (ParamValueMetaData param : metaData.getSipContextParams()) {
                deploymentUnit.addToAttachmentList(ServletContextAttribute.ATTACHMENT_KEY,
                        new ServletContextAttribute(param.getParamName(), param.getParamValue()));
            }
        }
    }

//    // guess that this method matches with JBoss5 processContextParameters() method?
//    @Override
//    protected void processJBossWebMetaData(JBossWebMetaData metaData) {
//        if (metaData instanceof JBossConvergedSipMetaData && context instanceof SipStandardContext) {
//            processSipContextParameters((JBossConvergedSipMetaData) metaData);
//        }
//        super.processJBossWebMetaData(metaData);
//    }

//    @Override
//    protected void processWebMetaData(JBossWebMetaData metaData) {
//        if (metaData instanceof JBossConvergedSipMetaData && context instanceof SipContext) {
//            processSipMetaData((JBossConvergedSipMetaData) metaData);
//            // Issue 1522 http://code.google.com/p/mobicents/issues/detail?id=1522 :
//            // when converged distributable app deployed is missing distributable in one of the Deployment descriptor
//            // throw a better exception
//            if (metaData.getDistributable() != null && metaData.getReplicationConfig() == null) {
//                throw new SipDeploymentException(
//                        "the <distributable/> element should be present in both web.xml and sip.xml so that the application can be correctly clustered");
//            }
//        }
//        super.processWebMetaData(metaData);
//
//    }

    /**
     * @param convergedMetaData
     * @throws Exception
     */
    public void processSipMetaData(JBossConvergedSipMetaData convergedMetaData, SipContextImpl convergedContext) throws Exception {
        //UndertowSipContextDeployment convergedContext = (CatalinaSipContext) context;
        //convergedContext.setWrapperClass(SipServletImpl.class.getName());
        /*
         * sip specific treatment
         */
        // description
        DescriptionGroupMetaData descriptionGroupMetaData = convergedMetaData.getDescriptionGroup();
//        // FIXME: josemrecio - dirty way to detect we are in defaultWebConfig() phase
//        // if so, as there is no defaultSipConfig() equivalent, we just return
//        if (descriptionGroupMetaData == null) {
//            return;
//        }
        if (descriptionGroupMetaData != null) {
            DescriptionsImpl descriptionsImpl = (DescriptionsImpl) descriptionGroupMetaData.getDescriptions();
            if (descriptionsImpl != null && !descriptionsImpl.isEmpty()) {
                convergedContext.setDescription(((DescriptionImpl) descriptionsImpl.iterator().next()).getDescription());
            }
            IconsImpl iconsImpl = (IconsImpl) descriptionGroupMetaData.getIcons();
            if (iconsImpl != null && !iconsImpl.isEmpty()) {
                IconImpl iconImpl = (IconImpl) iconsImpl.iterator().next();
                convergedContext.setSmallIcon(iconImpl.getSmallIcon());
                convergedContext.setLargeIcon(iconImpl.getLargeIcon());
            }
            DisplayNamesImpl displayNamesImpl = (DisplayNamesImpl) descriptionGroupMetaData.getDisplayNames();
            if (displayNamesImpl != null && !displayNamesImpl.isEmpty()) {
                convergedContext.setDisplayName(((DisplayNameImpl) displayNamesImpl.iterator().next()).getDisplayName());
            }
        }

        // Distributable
        if (convergedMetaData.getDistributable() != null) {
            // TODO
            throw new SipDeploymentException("Distributable not supported yet");
        }

        // sip context params
        this.processSipContextParameters(convergedMetaData);

        List<? extends ParamValueMetaData> sipContextParams = convergedMetaData.getSipContextParams();
        if (sipContextParams != null) {
            for (ParamValueMetaData param : sipContextParams) {
                convergedContext.getDeploymentInfoFacade().getDeploymentInfo().addServletContextAttribute(param.getParamName(), param.getParamValue());
            }
        }

        // app name
        if (convergedMetaData.getApplicationName() == null) {
            throw new SipDeploymentException(
                    "No app-name present in the sip.xml deployment descriptor or no SipApplication annotation defined");
        }
        convergedContext.setApplicationName(convergedMetaData.getApplicationName());
        // sip proxy config
        if (convergedMetaData.getProxyConfig() != null) {
            convergedContext.setProxyTimeout(convergedMetaData.getProxyConfig().getProxyTimeout());
        }
        // sip session config
        // http://code.google.com/p/sipservlets/issues/detail?id=209 : Incorrect merging for session-timeout in sip.xml
        if (convergedMetaData.getSipSessionConfig() != null) {
            convergedContext.setSipApplicationSessionTimeout(convergedMetaData.getSipSessionConfig().getSessionTimeout());
        }

        // http://code.google.com/p/sipservlets/issues/detail?id=158 : Implement Missing SIP Security in AS7
        String securityDomain = convergedMetaData.getSecurityDomain();
        convergedContext.setSecurityDomain(securityDomain);

        // sip security contstraints
        List<SipSecurityConstraintMetaData> sipConstraintMetaDatas = convergedMetaData.getSipSecurityConstraints();
        if (sipConstraintMetaDatas != null) {
            for (SipSecurityConstraintMetaData sipConstraintMetaData : sipConstraintMetaDatas) {
                SipSecurityConstraint sipSecurityConstraint = new SipSecurityConstraint();
                sipSecurityConstraint.setDisplayName(sipConstraintMetaData.getDisplayName());
                if (sipConstraintMetaData.getAuthConstraint() != null) {
                    for (String role : sipConstraintMetaData.getAuthConstraint().getRoleNames()) {
                        sipSecurityConstraint.addRoleAllowed(role);
                    }
                }
                if (sipConstraintMetaData.getProxyAuthentication() != null) {
                    sipSecurityConstraint.setProxyAuthentication(true);
                }

                TransportGuaranteeType tg = sipConstraintMetaData.getTransportGuarantee();
                io.undertow.servlet.api.TransportGuaranteeType undertowTg = null;
                if (tg==TransportGuaranteeType.CONFIDENTIAL){
                    undertowTg = io.undertow.servlet.api.TransportGuaranteeType.CONFIDENTIAL;
                }else if (tg==TransportGuaranteeType.INTEGRAL){
                    undertowTg = io.undertow.servlet.api.TransportGuaranteeType.INTEGRAL;
                }else if (tg==TransportGuaranteeType.NONE){
                    undertowTg = io.undertow.servlet.api.TransportGuaranteeType.NONE;
                }else {
                    undertowTg = io.undertow.servlet.api.TransportGuaranteeType.REJECTED;
                }
                sipSecurityConstraint.setTransportGuaranteeType(undertowTg);

                SipResourceCollectionsMetaData srcs = sipConstraintMetaData.getResourceCollections();
                if (srcs != null) {
                    for (SipResourceCollectionMetaData src : srcs) {
                        SipSecurityCollection securityCollection = new SipSecurityCollection();
                        securityCollection.setName(src.getName());
                        List<String> methods = src.getSipMethods();
                        if (methods != null) {
                            for (String method : src.getSipMethods()) {
                                securityCollection.addSipMethod(method);
                            }
                        }
                        List<String> servletNames = src.getServletNames();
                        if (servletNames != null) {
                            for (String servletName : servletNames) {
                                securityCollection.addServletName(servletName);
                            }
                        }
                        sipSecurityConstraint.addCollection(securityCollection);
                    }
                }
                convergedContext.getDeploymentInfoFacade().getDeploymentInfo().addSecurityConstraint(sipSecurityConstraint);
            }
        }
        // sip login config
        SipLoginConfigMetaData sipLoginConfig = convergedMetaData.getSipLoginConfig();
        if (sipLoginConfig != null) {
            SipLoginConfig sipLoginConfig2 = new SipLoginConfig(sipLoginConfig.getAuthMethod(),sipLoginConfig.getRealmName());

            if (sipLoginConfig.getIdentityAssertion() != null) {
                sipLoginConfig2.addIdentityAssertion(sipLoginConfig.getIdentityAssertion().getIdentityAssertionScheme(),
                        sipLoginConfig.getIdentityAssertion().getIdentityAssertionSupport());
            }
            convergedContext.setSipLoginConfig(sipLoginConfig2);
        }
        // Sip Listeners
        List<ListenerMetaData> sipListeners = convergedMetaData.getSipListeners();
        if (sipListeners != null) {
            for (ListenerMetaData value : sipListeners) {
                convergedContext.addSipApplicationListener(value.getListenerClass());
            }
        }

        // servlet selection
        boolean servletSelectionSet = false;
        SipServletSelectionMetaData servletSelectionMetaData = convergedMetaData.getSipServletSelection();
        if (servletSelectionMetaData != null) {
            String mainServlet = servletSelectionMetaData.getMainServlet();
            if (mainServlet != null && mainServlet.length() > 0) {
                convergedContext.setMainServlet(mainServlet);
                servletSelectionSet = true;
            }

            if (servletSelectionMetaData.getSipServletMappings() != null
                    && servletSelectionMetaData.getSipServletMappings().size() > 0) {
                if (servletSelectionSet) {
                    throw new SipDeploymentException(
                            "the main servlet and servlet mapping cannot be present at the same time in sip.xml or as annotations !");
                }
                Iterator<SipServletMappingMetaData> sipServletMapping = servletSelectionMetaData.getSipServletMappings()
                        .iterator();
                while (sipServletMapping.hasNext()) {
                    SipServletMappingMetaData sipServletMappingMetaData = (SipServletMappingMetaData) sipServletMapping.next();
                    SipServletMapping sipMapping = new SipServletMapping();
                    sipMapping.setServletName(sipServletMappingMetaData.getServletName());
                    PatternMetaData pattern = sipServletMappingMetaData.getPattern();
                    MatchingRule matchingRule = buildRule(pattern.getCondition());
                    sipMapping.setMatchingRule(matchingRule);
                    convergedContext.addSipServletMapping(sipMapping);
                }
                servletSelectionSet = true;
            }

            if (servletSelectionMetaData.getSipRubyController() != null) {
                convergedContext.setSipRubyController(servletSelectionMetaData.getSipRubyController());
                servletSelectionSet = true;
            }
        }
        // Sip Servlet
        JBossSipServletsMetaData sipServlets = convergedMetaData.getSipServlets();

        if (sipServlets != null) {
            if (sipServlets.size() > 1 && !servletSelectionSet) {
                throw new SipDeploymentException(
                        "the main servlet is not set and there is more than one servlet defined in the sip.xml or as annotations !");
            }
            for (ServletMetaData value : sipServlets) {

                Class<? extends Servlet> servletClass = (Class<? extends Servlet>) convergedContext.getSipContextClassLoader().loadClass(value.getServletClass());
                ManagedReferenceFactory creator = deploymentInfoservice.getComponentRegistryInjectedValue().getValue().createInstanceFactory(servletClass);

                ServletInfo servletInfo = null;
                if (creator != null) {
                    InstanceFactory<Servlet> factory = createInstanceFactory(creator);
                    servletInfo = new ServletInfo(value.getName(), servletClass,factory);
                }else{
                    servletInfo = new ServletInfo(value.getName(), servletClass);
                }

                // no main servlet defined in the sip.xml we take the name of the only sip servlet present
                if (!servletSelectionSet) {
                    convergedContext.setMainServlet(value.getName());
                }
                if (value.getJspFile() != null) {
                    servletInfo.setJspFile(value.getJspFile());
                }
                servletInfo.setLoadOnStartup(value.getLoadOnStartupInt());
                if (value.getRunAs() != null) {
                    servletInfo.setRunAs(value.getRunAs().getRoleName());
                }
                List<? extends ParamValueMetaData> params = value.getInitParam();
                if (params != null) {
                    for (ParamValueMetaData param : params) {
                        servletInfo.addInitParam(param.getParamName(), param.getParamValue());
                    }
                }
                SecurityRoleRefsMetaData refs = value.getSecurityRoleRefs();
                if (refs != null) {
                    for (SecurityRoleRefMetaData ref : refs) {
                        servletInfo.addSecurityRoleRef(ref.getRoleName(), ref.getRoleLink());
                    }
                }
                SipServletImpl wrapper = new SipServletImpl(servletInfo, convergedContext.getServletContext());
                wrapper.setupMultipart(convergedContext.getServletContext());
                wrapper.setServletName(value.getServletName());

                convergedContext.getDeploymentInfoFacade().addSipServlets(servletInfo);
                convergedContext.addChild(wrapper);
            }
        }
        final SipApplicationKeyMethodInfo sipApplicationKeyMethodInfo = convergedMetaData.getSipApplicationKeyMethodInfo();
        if(sipApplicationKeyMethodInfo != null) {
            final String sipApplicationKeyClassName = sipApplicationKeyMethodInfo.getClassName();
            final String sipApplicationKeyMethodName = sipApplicationKeyMethodInfo.getMethodName();

            ClassLoader contextCLoader = convergedContext.getSipContextClassLoader();
            Method sipApplicationKeyMethod = null;
            try {
                sipApplicationKeyMethod = Class.forName(sipApplicationKeyClassName, true, contextCLoader).getMethod(sipApplicationKeyMethodName, SipServletRequest.class);
            } catch (Exception e) {
                throw e;
            }
            convergedContext.setSipApplicationKeyMethod(sipApplicationKeyMethod);
        }
        convergedContext.setConcurrencyControlMode(convergedMetaData.getConcurrencyControlMode());
        //FIXME: kakonyii: no wrapperclass in wildfly, do we need this? convergedContext.setWrapperClass(StandardWrapper.class.getName());
    }

    //copied from UndertowDeploymentInfoService
    private static <T> InstanceFactory<T> createInstanceFactory(final ManagedReferenceFactory creator) {
        return new InstanceFactory<T>() {
            @Override
            public InstanceHandle<T> createInstance() throws InstantiationException {
                final ManagedReference instance = creator.getReference();
                return new InstanceHandle<T>() {
                    @Override
                    public T getInstance() {
                        return (T) instance.getInstance();
                    }

                    @Override
                    public void release() {
                        instance.release();
                    }
                };
            }
        };
    }

    public static MatchingRule buildRule(ConditionMetaData condition) {

        if (condition instanceof AndMetaData) {
            AndMetaData andMetaData = (AndMetaData) condition;
            AndRule and = new AndRule();
            List<ConditionMetaData> list = andMetaData.getConditions();
            for (ConditionMetaData newCondition : list) {
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
            for (ConditionMetaData newCondition : list) {
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
