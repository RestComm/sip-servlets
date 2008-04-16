/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mobicents.seam.actions;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jboss.mobicents.seam.model.Order;
import org.jboss.seam.annotations.Name;

@Stateless
public class OrderManagerBean implements OrderManager, Serializable {

	@PersistenceContext
	EntityManager em;

	@Override
	public void cancelOrder(long orderId) {
		Order order = (Order) em.createQuery(
				"select o from Order o where o.orderId = :orderId")
				.setParameter("orderId", orderId).getSingleResult();

		order.setStatus(Order.Status.CANCELLED);
	}

	@Override
	public void confirmOrder(long orderId) {
		Order order = (Order) em.createQuery(
				"select o from Order o where o.orderId = :orderId")
				.setParameter("orderId", orderId).getSingleResult();

		order.setStatus(Order.Status.OPEN);
	}

	@Override
	public void setDeliveryDate(Object orderId, Timestamp deliveryDate) {
		Order order = (Order) em.createQuery(
				"select o from Order o where o.orderId = :orderId")
				.setParameter("orderId", orderId).getSingleResult();

		order.setDeliveryDate(deliveryDate);
	}

}
