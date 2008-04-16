/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 

package org.jboss.mobicents.seam.actions;

import org.jboss.seam.annotations.Name;

/**
 * An example of a Seam component used to do jBPM
 * assignments. (This is silly, for such a simple
 * case, we would not need a component.)
 * 
 * @author Gavin King
 */
@Name("shipperAssignment")
public class ShipperAssignment
{
    public String[] getPooledActors()
    {
       return new String[] { "shippers", "admins" };
    }
}
