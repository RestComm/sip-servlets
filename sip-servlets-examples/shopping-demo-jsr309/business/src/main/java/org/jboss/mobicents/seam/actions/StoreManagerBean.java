/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
