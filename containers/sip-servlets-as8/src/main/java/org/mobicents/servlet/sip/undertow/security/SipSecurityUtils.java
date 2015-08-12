/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2015, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package org.mobicents.servlet.sip.undertow.security;

import java.lang.reflect.Field;
import java.security.Principal;
import java.util.concurrent.ConcurrentMap;

import gov.nist.javax.sip.header.ims.PAssertedIdentityHeader;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.idm.PasswordCredential;
import io.undertow.servlet.api.AuthMethodConfig;
import io.undertow.servlet.api.Deployment;
import io.undertow.servlet.api.SecurityConstraint;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.api.WebResourceCollection;

import javax.servlet.sip.SipServletResponse;
import javax.sip.SipStack;
import javax.sip.address.SipURI;
import javax.sip.address.TelURL;

import org.apache.log4j.Logger;
import org.jboss.as.security.plugins.SecurityDomainContext;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityRolesAssociation;
import org.jboss.security.authentication.JBossCachedAuthenticationManager;
import org.jboss.security.authentication.JBossCachedAuthenticationManager.DomainInfo;
import org.mobicents.servlet.sip.core.SipContext;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletRequest;
import org.mobicents.servlet.sip.core.message.MobicentsSipServletResponse;
import org.mobicents.servlet.sip.core.security.MobicentsSipLoginConfig;
import org.mobicents.servlet.sip.core.security.SipPrincipal;
import org.mobicents.servlet.sip.security.SIPSecurityConstants;
import org.mobicents.servlet.sip.security.SecurityActions;
import org.mobicents.servlet.sip.undertow.SipContextImpl;
import org.mobicents.servlet.sip.undertow.SipLoginConfig;
import org.mobicents.servlet.sip.undertow.SipSecurityCollection;
import org.mobicents.servlet.sip.undertow.SipSecurityConstraint;
import org.mobicents.servlet.sip.undertow.security.authentication.SipDigestAuthenticationMechanism;
import org.wildfly.extension.undertow.security.JAASIdentityManagerImpl;

/**
 *
 * This class is based org.mobicents.servlet.sip.catalina.security.SipSecurityUtils class from sip-servlet-as7 project,
 * re-implemented for jboss as8 (wildfly) by:
 *
 * @author kakonyi.istvan@alerant.hu
 *
 */
public class SipSecurityUtils {
    private static final Logger log = Logger.getLogger(SipSecurityUtils.class);
    private SipContext sipStandardContext;

    public SipSecurityUtils(SipContext sipContext) {
        this.sipStandardContext = sipContext;
    }

    public boolean authenticate(MobicentsSipServletRequest request, SipSecurityConstraint sipConstraint,
            ServletInfo servletInfo, SipStack sipStack) {
        boolean authenticated = false;
        SipLoginConfig loginConfig = (SipLoginConfig) sipStandardContext.getSipLoginConfig();
        try {
            if (loginConfig != null) {
                for (AuthMethodConfig authmethodConfig : loginConfig.getAuthMethods()) {
                    String authMethod = authmethodConfig.getName();
                    if (authMethod != null) {
                        // (1) First check for Proxy Asserted Identity
                        String pAssertedIdentitySetting = loginConfig
                                .getIdentitySchemeSettings(MobicentsSipLoginConfig.IDENTITY_SCHEME_P_ASSERTED);
                        if (pAssertedIdentitySetting != null) {
                            if (request.getHeader(PAssertedIdentityHeader.NAME) != null) {
                                String pAssertedHeaderValue = request.getHeader(PAssertedIdentityHeader.NAME);

                                // If P-Identity is required we must send error message immediately
                                if (pAssertedHeaderValue == null
                                        && MobicentsSipLoginConfig.IDENTITY_SCHEME_REQUIRED.equals(pAssertedIdentitySetting)) {
                                    request.createResponse(428, "P-Asserted-Identity header is required!").send();
                                    return false;
                                }
                                javax.sip.address.Address address = sipStandardContext.getSipApplicationDispatcher()
                                        .getSipFactory().getAddressFactory().createAddress(pAssertedHeaderValue);
                                String username = null;
                                if (address.getURI().isSipURI()) {
                                    SipURI sipUri = (SipURI) address.getURI();
                                    username = sipUri.getUser();
                                } else {
                                    TelURL telUri = (TelURL) address.getURI();
                                    username = telUri.getPhoneNumber();
                                }
                                SipPrincipal principal = impersonatePrincipal(username,
                                        ((SipContextImpl) sipStandardContext).getDeployment(), servletInfo,
                                        ((SipContextImpl) sipStandardContext).getSecurityDomain(),
                                        ((SipLoginConfig) sipStandardContext.getSipLoginConfig()).getRealmName());

                                if (principal != null) {
                                    authenticated = true;
                                    request.setUserPrincipal(principal);
                                    request.getSipSession().setUserPrincipal(principal);
                                    log.debug("P-Asserted-Identity authetication successful for user: " + username);
                                }
                            }
                        }
                        // (2) Then if P-Identity has failed and is not required attempt DIGEST auth
                        if (!authenticated && authMethod.equalsIgnoreCase("DIGEST")) {
                            SipDigestAuthenticationMechanism digestAuthenticator = new SipDigestAuthenticationMechanism(
                                    ((SipLoginConfig) sipStandardContext.getSipLoginConfig()).getRealmName(),
                                    sipStandardContext.getSipApplicationDispatcher().getSipFactory().getHeaderFactory());
                            digestAuthenticator.setContext(sipStandardContext);
                            MobicentsSipServletResponse response = createErrorResponse(request, sipConstraint);
                            authenticated = digestAuthenticator.authenticate(request, response, loginConfig,
                                    ((SipContextImpl) sipStandardContext).getSecurityDomain(),
                                    ((SipContextImpl) sipStandardContext).getDeployment(), servletInfo, sipStack);
                            request.setUserPrincipal(digestAuthenticator.getPrincipal());
                        } else if (authMethod.equalsIgnoreCase("BASIC")) {
                            throw new IllegalStateException("Basic authentication not supported in JSR 289");
                        }
                    }
                }
            } else {
                log.debug("No login configuration found in sip.xml. We won't authenticate.");
                return true; // There is no auth config in sip.xml. So don't authenticate.
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return authenticated;
    }

    private static MobicentsSipServletResponse createErrorResponse(MobicentsSipServletRequest request,
            SipSecurityConstraint sipConstraint) {
        SipServletResponse response = null;
        if (sipConstraint.isProxyAuthentication()) {
            response = (MobicentsSipServletResponse) request
                    .createResponse(MobicentsSipServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED);
        } else {
            response = (MobicentsSipServletResponse) request.createResponse(MobicentsSipServletResponse.SC_UNAUTHORIZED);
        }
        return (MobicentsSipServletResponse) response;
    }

    public boolean authorize(MobicentsSipServletRequest request, ServletInfo servletInfo, SipStack sipStack) {
        boolean allConstrainsSatisfied = true;
        Object[] constraints = ((SipContextImpl) sipStandardContext).getDeploymentInfoFacade().getDeploymentInfo()
                .getSecurityConstraints().toArray();

        // If we have no constraints, just authorize the request;
        if (constraints.length == 0) {
            return true;
        }

        for (Object constraint : constraints) {
            if (constraint instanceof SipSecurityConstraint) {
                SipSecurityConstraint sipConstraint = (SipSecurityConstraint) constraint;
                for (WebResourceCollection security : sipConstraint.getWebResourceCollections()) {

                    // For each secured resource see if it's bound to the current
                    // request method and servlet name.
                    SipSecurityCollection sipSecurity = (SipSecurityCollection) security;
                    String servletName = request.getSipSession().getHandler();
                    if (sipSecurity.findMethod(request.getMethod()) && sipSecurity.findServletName(servletName)) {
                        boolean constraintSatisfied = false;
                        // If yes, see if the current user is in a role compatible with the
                        // required roles for the resource.
                        if (authenticate(request, sipConstraint, servletInfo, sipStack)) {
                            UndertowSipPrincipal principal = (UndertowSipPrincipal) request.getUserPrincipal();
                            if (principal == null)
                                return false;

                            for (String assignedRole : ((SecurityConstraint) constraint).getRolesAllowed()) {
                                if (principal.isUserInRole(assignedRole)) {
                                    constraintSatisfied = true;
                                    break;
                                }
                            }
                        }
                        if (!constraintSatisfied) {
                            allConstrainsSatisfied = false;
                            log.error("Constraint \"" + sipConstraint.getDisplayName() + "\" not satifsied");
                        }
                    }
                }
            }
        }
        return allConstrainsSatisfied;
    }

    /*
     * This method attempts to obtain the Principal of a user from an auth cache without having to authenticate with a password
     * or certificate. If cache-entry is set in standalone.xml, this method tries to use reflection to get the data FIXME:
     * implementing security cache flush
     */
    public static SipPrincipal impersonatePrincipal(String username, Deployment deployment, ServletInfo servletInfo,
            String securityDomain, String realmName) {
        if (username == null) {
            return null;
        }

        final IdentityManager identityManager = deployment.getDeploymentInfo().getIdentityManager();

        PasswordCredential credential = new PasswordCredential("".toCharArray());

        // get securityDomainContext from identityManager
        if (identityManager instanceof JAASIdentityManagerImpl) {
            Field securityDomainContextField = null;
            Field domainCacheField = null;
            Field credentialField = null;

            try {
                securityDomainContextField = identityManager.getClass().getDeclaredField("securityDomainContext");
                securityDomainContextField.setAccessible(true);

                SecurityDomainContext securityDomainContext = (SecurityDomainContext) securityDomainContextField
                        .get(identityManager);

                if (securityDomainContext != null) {
                    AuthenticationManager authManager = securityDomainContext.getAuthenticationManager();

                    // gets the cache from the manager:
                    if (authManager != null && authManager instanceof JBossCachedAuthenticationManager) {
                        domainCacheField = authManager.getClass().getDeclaredField("domainCache");
                        domainCacheField.setAccessible(true);

                        ConcurrentMap<Principal, DomainInfo> domainCache = (ConcurrentMap<Principal, DomainInfo>) domainCacheField
                                .get(authManager);
                        if (domainCache != null) {
                            for (Principal p : domainCache.keySet()) {
                                if (username.equalsIgnoreCase(p.getName())) {
                                    DomainInfo d = domainCache.get(p);

                                    // gets the credential from the stored DomainInfo:
                                    credentialField = d.getClass().getDeclaredField("credential");
                                    credentialField.setAccessible(true);

                                    credential = new PasswordCredential((char[]) credentialField.get(d));

                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException | SecurityException e) {
                log.warn("Exception occured, while try to impersonate the security principal, please check secuirty settings!",
                        e);
            } finally {
                if (securityDomainContextField != null) {
                    securityDomainContextField.setAccessible(false);
                }
                if (domainCacheField != null) {
                    domainCacheField.setAccessible(false);
                }
                if (credentialField != null) {
                    credentialField.setAccessible(false);
                }
            }
        }

        // taken from
        // https://github.com/jbossas/jboss-as/blob/7.1.2.Final/web/src/main/java/org/jboss/as/web/security/SecurityContextAssociationValve.java#L86
        SecurityContext sc = SecurityActions.getSecurityContext();

        if (sc == null) {
            if (log.isDebugEnabled()) {
                log.debug("Security Domain " + securityDomain + " for Realm " + realmName);
            }
            if (securityDomain == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Security Domain is null using default security domain "
                            + SIPSecurityConstants.DEFAULT_SIP_APPLICATION_POLICY + " for Realm " + realmName);
                }
                securityDomain = SIPSecurityConstants.DEFAULT_SIP_APPLICATION_POLICY;
            }
            sc = SecurityActions.createSecurityContext(securityDomain);

            SecurityActions.setSecurityContextOnAssociation(sc);
        }

        Account account = null;
        try {
            account = identityManager.verify(username, credential);
        } finally {
            SecurityActions.clearSecurityContext();
            SecurityRolesAssociation.setSecurityRoles(null);
        }

        if (account != null) {
            return new UndertowSipPrincipal(account, deployment, servletInfo);
        } else {
            return null;
        }
    }
}
