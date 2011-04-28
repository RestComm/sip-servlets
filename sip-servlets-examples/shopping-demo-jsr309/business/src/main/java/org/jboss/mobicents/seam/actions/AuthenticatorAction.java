/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.jboss.mobicents.seam.actions;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.jboss.mobicents.seam.model.User;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.bpm.Actor;
import org.jboss.seam.security.Identity;

@Stateless
@Name("authenticator")
public class AuthenticatorAction implements Authenticator
{
    @In 
    private EntityManager entityManager;

    @In Actor actor;
    @In Identity identity;

    @Out(required=false, scope=ScopeType.SESSION) 
    User currentUser;

    public boolean authenticate()
    {
        try {
            currentUser = (User) 
                entityManager.createQuery("select u from User u where u.userName = #{identity.username} and u.password = #{identity.password}")       
                             .getSingleResult();
        } catch (PersistenceException e) {
            return false;
        }

        actor.setId(identity.getUsername());

        if (currentUser instanceof Admin) {
            actor.getGroupActorIds().add("shippers");
            actor.getGroupActorIds().add("reviewers");
            identity.addRole("admin");
        }
      
        return true;
    }
}
