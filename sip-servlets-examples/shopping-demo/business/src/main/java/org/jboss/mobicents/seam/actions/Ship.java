/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.mobicents.seam.actions;

public interface Ship {
    public String getTrack();
    public void   setTrack(String track);

    public String ship();
    public String viewTask();   

    public void   destroy();
}
