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
import java.sql.Timestamp;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.sip.ServletTimer;

import org.jboss.mobicents.seam.model.Order;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.Log;

@Stateless
@Name("OrderManager")
public class OrderManagerBean implements OrderManager, Serializable {
	@Logger private Log log;
	
	@PersistenceContext EntityManager em;
		
	public void cancelOrder(long orderId) {
		Order order = (Order) em.createQuery(
				"select o from Order o where o.orderId = :orderId")
				.setParameter("orderId", orderId).getSingleResult();

		order.setStatus(Order.Status.CANCELLED);
		
		ServletTimer deliveryDateTimer = (ServletTimer) Contexts.getApplicationContext().get("deliveryDateTimer" + orderId);
		ServletTimer adminTimer = (ServletTimer) Contexts.getApplicationContext().get("adminTimer" + orderId);
		if(deliveryDateTimer!=null) {			
			deliveryDateTimer.cancel();
			Contexts.getApplicationContext().remove("deliveryDateTimer" + orderId);
			log.info("Delivery Timer for order "+ orderId +" cancelled");
		} else {
			log.info("No Delivery Timer to cancel");
		}
		
		if(adminTimer!=null) {
			adminTimer.cancel();
			Contexts.getApplicationContext().remove("adminTimer" + orderId);
			log.info("Admin Timer for order "+ orderId +" cancelled");
		} else {
			log.info("No Admin Timer to cancel");
		}				
	}
	
	public void confirmOrder(long orderId) {
		Order order = (Order) em.createQuery(
				"select o from Order o where o.orderId = :orderId")
				.setParameter("orderId", orderId).getSingleResult();

		order.setStatus(Order.Status.PROCESSING);

		Contexts.getApplicationContext().remove("deliveryDateTimer" + orderId);
		Contexts.getApplicationContext().remove("adminTimer" + orderId);						
	}
	
	public void setDeliveryDate(Object orderId, Timestamp deliveryDate) {
		Order order = (Order) em.createQuery(
				"select o from Order o where o.orderId = :orderId")
				.setParameter("orderId", orderId).getSingleResult();

		order.setDeliveryDate(deliveryDate);
	}

}
