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
import java.util.List;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.jboss.mobicents.seam.model.Customer;
import org.jboss.mobicents.seam.model.Order;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.bpm.ResumeProcess;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jbpm.graph.exe.ProcessInstance;

@Stateful
@Name("showorders")
public class ShowOrdersAction
    implements ShowOrders,
               Serializable
{
    private static final long serialVersionUID = -5377038496721657104L;

    @In(value="currentUser",required=false)
    Customer customer;

    @PersistenceContext(type=PersistenceContextType.EXTENDED)
    EntityManager em;

    @DataModel
    List<Order> orders;    

    @DataModelSelection
    @Out(value="myorder", required=false, scope=ScopeType.CONVERSATION)
    Order order;

    @Begin @Factory("orders")
    @SuppressWarnings("unchecked")
    public String findOrders() {
        orders = em.createQuery("select o from Order o where o.customer = :customer")
            .setParameter("customer", customer)
            .getResultList();

        order = null;

        return "showorders";
    }
    
    @In(required=false) 
    ProcessInstance processInstance;

    @ResumeProcess(definition="OrderManagement", processKey="#{orders.rowData.orderId}")
    public String cancelOrder() {
       
        em.refresh(order);
       
        if ( order.getStatus() != Order.Status.OPEN ) {
            return null;
        }

        order.setStatus(Order.Status.CANCELLED);
        
        processInstance.signal("cancel");
        
        return findOrders();
    }

    public String detailOrder() {
        em.refresh(order);
        return "showorders";
    }

    @End
    public String reset() {
        return null;
    }

    @Remove
    public void destroy() {}
    
}
