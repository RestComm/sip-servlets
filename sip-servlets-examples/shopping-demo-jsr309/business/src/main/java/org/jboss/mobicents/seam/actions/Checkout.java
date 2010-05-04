/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.mobicents.seam.actions;

public interface Checkout
{
    public void createOrder();
    public void submitOrder();
    
    public void destroy();
}
