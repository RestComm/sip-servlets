/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.mobicents.seam.actions;

public interface Accept {
    public String accept();
    public String reject();

    public String viewTask();

    public void   destroy();
}
