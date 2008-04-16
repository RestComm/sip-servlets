/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.mobicents.seam.actions;

import java.sql.Timestamp;

@javax.ejb.Remote
public interface OrderManager
{  
    public void confirmOrder(long orderId);
    public void cancelOrder(long orderId);
	public void setDeliveryDate(Object attribute, Timestamp timeStamp);    
}
