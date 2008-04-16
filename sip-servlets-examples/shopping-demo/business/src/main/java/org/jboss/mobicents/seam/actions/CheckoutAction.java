/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mobicents.seam.actions;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.servlet.sip.Address;
import javax.servlet.sip.ServletParseException;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.URI;

import org.jboss.mobicents.seam.model.Customer;
import org.jboss.mobicents.seam.model.Inventory;
import org.jboss.mobicents.seam.model.Order;
import org.jboss.mobicents.seam.model.OrderLine;
import org.jboss.mobicents.seam.model.Product;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.bpm.CreateProcess;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;

@Stateful
@Name("checkout")
public class CheckoutAction implements Checkout, Serializable {
	private static final long serialVersionUID = -4651884454184474207L;

	@PersistenceContext(type = PersistenceContextType.EXTENDED)
	EntityManager em;

	@In(value = "currentUser", required = false)
	Customer customer;

	@In(create = true)
	ShoppingCart cart;

	@Out(scope = ScopeType.CONVERSATION, required = false)
	Order currentOrder;

	@Out(scope = ScopeType.CONVERSATION, required = false)
	Order completedOrder;

	@Out(scope = ScopeType.BUSINESS_PROCESS, required = false)
	long orderId;

	@Out(scope = ScopeType.BUSINESS_PROCESS, required = false)
	BigDecimal amount = BigDecimal.ZERO;

	@Out(value = "customer", scope = ScopeType.BUSINESS_PROCESS, required = false)
	String customerName;

	@Out(value = "customerfullname", scope = ScopeType.BUSINESS_PROCESS, required = false)
	String customerFullName;

	@Out(value = "cutomerphone", scope = ScopeType.BUSINESS_PROCESS, required = false)
	String customerPhone;

	@Resource(mappedName="java:/sip/SipFactory") SipFactory sipFactory;
	@Resource(mappedName="java:/sip/TimerService") TimerService sipTimerService;
	
	@Begin(nested = true, pageflow = "checkout")
	public void createOrder() {
		currentOrder = new Order();

		for (OrderLine line : cart.getCart()) {
			currentOrder.addProduct(em.find(Product.class, line.getProduct()
					.getProductId()), line.getQuantity());
		}

		currentOrder.calculateTotals();
		cart.resetCart();
	}

	@End
	@CreateProcess(definition = "OrderManagement", processKey = "#{completedOrder.orderId}")
	@Restrict("#{identity.loggedIn}")
	public void submitOrder() {
		try {
			completedOrder = purchase(customer, currentOrder);

			orderId = completedOrder.getOrderId();
			amount = completedOrder.getNetAmount();
			customerName = completedOrder.getCustomer().getUserName();

			customerFullName = completedOrder.getCustomer().getFirstName()
					+ " " + completedOrder.getCustomer().getLastName();
			customerPhone = completedOrder.getCustomer().getPhone();

			fireEvent(orderId, amount, customerFullName, customerPhone);

		} catch (InsufficientQuantityException e) {
			for (Product product : e.getProducts()) {
				Contexts.getEventContext().set("prod", product);
				FacesMessages.instance().addFromResourceBundle(
						"checkoutInsufficientQuantity");
			}
		}
	}

	private Order purchase(Customer customer, Order order)
			throws InsufficientQuantityException {
		order.setCustomer(customer);
		order.setOrderDate(new Date());

		List<Product> errorProducts = new ArrayList<Product>();
		for (OrderLine line : order.getOrderLines()) {
			Inventory inv = line.getProduct().getInventory();
			if (!inv.order(line.getQuantity())) {
				errorProducts.add(line.getProduct());
			}
		}

		if (errorProducts.size() > 0) {
			throw new InsufficientQuantityException(errorProducts);
		}

		order.calculateTotals();
		em.persist(order);

		return order;
	}

	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	private void fireEvent(long orderId, BigDecimal ammount,
			String customerName, String customerPhone) {
		//TODO replace by Sip Servlets call
		System.out.println("SIPTIMERSERVICE" + sipTimerService);
		System.out.println("SIPFACTORY" + sipFactory);
		SipApplicationSession sipApplicationSession = sipFactory.createApplicationSessionByAppName("shopping-demo");
		//TODO remove hard coded uri from address header
		try {
			Address fromAddress = sipFactory.createAddress("sip:admin@sip-servlets.com");
			Address toAddress = sipFactory.createAddress(customerPhone);
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "INVITE", fromAddress, toAddress);
			URI requestURI = sipFactory.createURI(customerPhone);
			sipServletRequest.setRequestURI(requestURI);
			sipServletRequest.send();			
		} catch (ServletParseException spe) {
			// TODO: log exception
			spe.printStackTrace();
		} catch (IOException ioe) {
			// TODO log exception
			ioe.printStackTrace();
		}		
		
//		try {
//
//			InitialContext ic = new InitialContext();
//
//			SipFactory factory = (SipFactory) ic
//					.lookup("java:/sip-servlets");
//
//			SleeConnection conn1 = null;
//			conn1 = factory.getConnection();
//
//			ExternalActivityHandle handle = conn1.createActivityHandle();
//
//			EventTypeID requestType = conn1.getEventTypeID(
//					"org.mobicents.slee.service.dvddemo.ORDER_PLACED",
//					"org.mobicents", "1.0");
//			CustomEvent customEvent = new CustomEvent(orderId, ammount,
//					customerName, customerPhone);
//
//			conn1.fireEvent(customEvent, requestType, handle, null);
//			conn1.close();
//
//		} catch (Exception e) {
//
//			e.printStackTrace();
//
//		}
	}

	@Remove
	public void destroy() {
	}

}
