/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.mobicents.seam.actions;

public interface Help
{
	public String getPhoneNumber();

	public void setPhoneNumber(String phoneNumber);
	
    public void call();
    public void destroy();
}
