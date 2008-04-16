/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.mobicents.seam.actions;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.jboss.mobicents.seam.model.Order;
import org.jboss.seam.annotations.Name;

@Stateless
@Name("stats")
public class StoreManagerBean
    implements StoreManager,
               Serializable
{  
    private static final long serialVersionUID = 7011610947757223263L;

    @PersistenceContext
    EntityManager em;

    public long getNumberOrders() {
        return (Long) em.createQuery("select count(o) from Order o where o.status != :status")
            .setParameter("status", Order.Status.CANCELLED)
            .getSingleResult();
    }

    public BigDecimal getTotalSales() {
        try {
            BigDecimal totalSales = (BigDecimal) em.createQuery("select sum(o.totalAmount) from Order o where o.status != :status")
                .setParameter("status", Order.Status.CANCELLED)
                .getSingleResult();
            return totalSales==null ? BigDecimal.ZERO : totalSales;
        } catch (NoResultException e) {
            return BigDecimal.ZERO;
        }
    }

    public long getUnitsSold() {
        try {
            return (Long) em.createQuery("select sum(i.sales) from Inventory i").getSingleResult();
        } catch (NoResultException e) {
            return 0;
        }
    }

    public long getTotalInventory() {
        try {
            return (Long) em.createQuery("select sum(i.quantity) from Inventory i").getSingleResult();
        } catch (NoResultException e) {
            return 0;
        }
    }

}
