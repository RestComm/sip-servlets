/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.mobicents.seam.actions;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.jboss.mobicents.seam.model.OrderLine;
import org.jboss.mobicents.seam.model.Product;

public interface ShoppingCart
{
    public boolean getIsEmpty();

    public void addProduct(Product product, int quantity);
    public List<OrderLine> getCart();
    @SuppressWarnings("unchecked")
    public Map getCartSelection();

    public BigDecimal getSubtotal();
    public BigDecimal getTax();
    public BigDecimal getTotal();

    public void updateCart();
    public void resetCart();

    public void destroy();
}
