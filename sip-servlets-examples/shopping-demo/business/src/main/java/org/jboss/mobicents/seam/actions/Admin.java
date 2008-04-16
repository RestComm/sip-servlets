/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.mobicents.seam.actions;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.jboss.mobicents.seam.model.User;

@Entity
@DiscriminatorValue("admin")
public class Admin
    extends User
    implements Serializable
{

    private static final long serialVersionUID = 2548491885863399995L;

    @Transient
    @Override
    public boolean isAdmin() {
        return true;
    }

}
