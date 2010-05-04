/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.mobicents.seam.actions;

import java.util.Map;

import javax.ejb.Local;

@Local
public interface EditCustomer
{
    public void startEdit();

    public Map<String,Integer> getCreditCardTypes();

    public void   setPasswordVerify(String password);
    public String getPasswordVerify();

    public boolean isValidNamePassword();

    public String saveUser();

    public void destroy();
}
