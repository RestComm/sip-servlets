/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.mobicents.seam.actions;

import java.util.List;

import javax.ejb.ApplicationException;

import org.jboss.mobicents.seam.model.Product;

@ApplicationException(rollback=true)
public class InsufficientQuantityException
    extends Exception
{
    private static final long serialVersionUID = 7772258944523315127L;
    
    List<Product> products = null;

    public InsufficientQuantityException(List<Product> products) {
        this.products = products;
    }

    public List<Product> getProducts() {
        return products;
    }
}
