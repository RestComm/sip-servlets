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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.Resource;
import javax.ejb.Remove;
import javax.ejb.SessionContext;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.validator.InvalidStateException;
import org.hibernate.validator.InvalidValue;
import org.jboss.mobicents.seam.model.Customer;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.bpm.Actor;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.security.Identity;

@Stateful
@Name("editCustomer")
public class EditCustomerAction
    implements EditCustomer
{
    @PersistenceContext
    EntityManager em;
    
    @Resource
    SessionContext ctx;

    @In
    Context sessionContext;

    @In(create=true)
    @Out
    Customer customer;
    
    @In
    FacesMessages facesMessages;
    
    @In Identity identity;

    String password = null;    

    public void setPasswordVerify(String password) {
        this.password = password;
    }
    public String getPasswordVerify() {
        return password;
    }


    @Begin(nested=true, pageflow="newuser") 
    public void startEdit() {
    }

    public boolean isValidNamePassword() {
        boolean ok = true;
        if (!isUniqueName()) {
            facesMessages.add("userName", "This name is already in use");
            ok = false;
        }
        if (!isPasswordsMatch()) {
            facesMessages.add("passwordVerify", "Must match password field");
            ok = false;
        }
        return ok;
    }

    @SuppressWarnings("unchecked")
    private boolean isUniqueName() {
        String name = customer.getUserName();
        if (name == null) return true;

        List<Customer> results = em.createQuery("select c from Customer c where c.userName = :name")
            .setParameter("name", name)
            .getResultList();

        return results.size() == 0;
    }

    private boolean isPasswordsMatch() {
        String customerpass = customer.getPassword();

        return (password != null)
            && (customerpass != null) 
            && (customerpass.equals(password));
    }

    public String saveUser() {
        if (!isValidNamePassword()) {
            facesMessages.add("User name #{customer.userName} is not unique");
            return null;
        }

        try {
            em.persist(customer);
            sessionContext.set("currentUser", customer);
            Actor.instance().setId(customer.getUserName());
            
            identity.setUsername(customer.getUserName());
            identity.setPassword(customer.getPassword());
            identity.login();
            
            facesMessages.addFromResourceBundle("createCustomerSuccess");
            return "success";
        } catch (InvalidStateException e) {
            InvalidValue[] vals = e.getInvalidValues();
            for (InvalidValue val: vals) {
                facesMessages.add(val);
            }

            return null;
        } catch (RuntimeException e) {
            ctx.setRollbackOnly();

            facesMessages.addFromResourceBundle("createCustomerError");

            return null;
        }
    }

    public Map<String,Integer> getCreditCardTypes() {
        Map<String,Integer> map = new TreeMap<String,Integer>();
        for (int i=1; i<=5; i++) {
            map.put(Customer.cctypes[i-1], i);
        }
        return map;
    }

    @Remove
    public void destroy() {}
}
