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
package org.mobicents.servlet.sip.security;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.jboss.security.RunAs;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextFactory;

import static org.jboss.as.web.WebMessages.MESSAGES;

/**
 * taken from https://github.com/jbossas/jboss-as/blob/7.1.2.Final/web/src/main/java/org/jboss/as/web/security/SecurityActions.java
 * @author jean.deruelle@gmail.com
 *
 */
public class SecurityActions {
	/**
     * Create a JBoss Security Context with the given security domain name
     *
     * @param domain the security domain name (such as "other" )
     * @return an instanceof {@code SecurityContext}
     */
    public static SecurityContext createSecurityContext(final String domain) {
        return AccessController.doPrivileged(new PrivilegedAction<SecurityContext>() {

            @Override
            public SecurityContext run() {
                try {
                    return SecurityContextFactory.createSecurityContext(domain);
                } catch (Exception e) {
                    throw MESSAGES.failToCreateSecurityContext(e);
                }
            }
        });
    }

    /**
     * Set the {@code SecurityContext} on the {@code SecurityContextAssociation}
     *
     * @param sc the security context
     */
    public static void setSecurityContextOnAssociation(final SecurityContext sc) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            @Override
            public Void run() {
                SecurityContextAssociation.setSecurityContext(sc);
                return null;
            }
        });
    }

    /**
     * Get the current {@code SecurityContext}
     *
     * @return an instance of {@code SecurityContext}
     */
    public static SecurityContext getSecurityContext() {
        return AccessController.doPrivileged(new PrivilegedAction<SecurityContext>() {
            public SecurityContext run() {
                return SecurityContextAssociation.getSecurityContext();
            }
        });
    }

    /**
     * Clears current {@code SecurityContext}
     */
    public static void clearSecurityContext() {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                SecurityContextAssociation.clearSecurityContext();
                return null;
            }
        });
    }

    /**
     * Sets the run as identity
     *
     * @param principal the identity
     */
    public static void pushRunAsIdentity(final RunAsIdentity principal) {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

            @Override
            public Void run() {
                SecurityContext sc = getSecurityContext();
                if (sc == null)
                    throw MESSAGES.noSecurityContext();
                sc.setOutgoingRunAs(principal);
                return null;
            }
        });
    }

    /**
     * Removes the run as identity
     *
     * @return the identity removed
     */
    public static RunAs popRunAsIdentity() {
        return AccessController.doPrivileged(new PrivilegedAction<RunAs>() {

            @Override
            public RunAs run() {
                SecurityContext sc = getSecurityContext();
                if (sc == null)
                    throw MESSAGES.noSecurityContext();
                RunAs principal = sc.getOutgoingRunAs();
                sc.setOutgoingRunAs(null);
                return principal;
            }
        });
    }

    public static final String AUTH_EXCEPTION_KEY = "org.jboss.security.exception";

    public static void clearAuthException() {
        if (System.getSecurityManager() != null) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {

                @Override
                public Void run() {
                    SecurityContext sc = getSecurityContext();
                    if (sc != null)
                        sc.getData().put(AUTH_EXCEPTION_KEY, null);
                    return null;
                }
            });
        } else {
            SecurityContext sc = getSecurityContext();
            if (sc != null)
                sc.getData().put(AUTH_EXCEPTION_KEY, null);
        }
    }

    public static Throwable getAuthException() {
        if (System.getSecurityManager() != null) {
            return AccessController.doPrivileged(new PrivilegedAction<Throwable>() {

                @Override
                public Throwable run() {
                    SecurityContext sc = getSecurityContext();
                    Throwable exception = null;
                    if (sc != null)
                        exception = (Throwable) sc.getData().get(AUTH_EXCEPTION_KEY);
                    return exception;
                }
            });
        } else {
            SecurityContext sc = getSecurityContext();
            Throwable exception = null;
            if (sc != null)
                exception = (Throwable) sc.getData().get(AUTH_EXCEPTION_KEY);
            return exception;
        }
    }
}
