/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mobicents.seam.actions;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.servlet.sip.Address;
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipURI;
import javax.servlet.sip.URI;

import org.jboss.mobicents.seam.listeners.MediaConnectionListener;
import org.jboss.mobicents.seam.model.Customer;
import org.jboss.mobicents.seam.model.Inventory;
import org.jboss.mobicents.seam.model.Order;
import org.jboss.mobicents.seam.model.OrderLine;
import org.jboss.mobicents.seam.model.Product;
import org.jboss.mobicents.seam.util.TTSUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.bpm.CreateProcess;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsPeer;
import org.mobicents.mscontrol.MsPeerFactory;
import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.MsSession;

@Stateful
@Name("checkout")
public class CheckoutAction implements Checkout, Serializable {
	@Logger private Log log;
	
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

	//jboss 5, compliant with sip spec 1.1
	//@Resource(mappedName="java:comp/env/sip/shopping-demo/SipFactory") SipFactory sipFactory;

    //jboss 4
    @Resource(mappedName="java:/sip/shopping-demo/SipFactory") SipFactory sipFactory;

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
			
		try {
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();			
			String callerAddress = (String)Contexts.getApplicationContext().get("caller.sip");
			String callerDomain = (String)Contexts.getApplicationContext().get("caller.domain");
			SipURI fromURI = sipFactory.createSipURI(callerAddress, callerDomain);
			Address fromAddress = sipFactory.createAddress(fromURI);
			Address toAddress = sipFactory.createAddress(customerPhone);
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "INVITE", fromAddress, toAddress);
			// getting the contact address for the registered customer sip address
			String userContact= ((Map<String, String>)Contexts.getApplicationContext().get("registeredUsersMap")).get(customerPhone);
			if(userContact != null && userContact.length() > 0) {
				// for customers using the registrar
				URI requestURI = sipFactory.createURI(userContact);
				sipServletRequest.setRequestURI(requestURI);
			} else {
				// for customers not using the registrar and registered directly their contact location
				URI requestURI = sipFactory.createURI(customerPhone);
				sipServletRequest.setRequestURI(requestURI);
			}
			//TTS file creation		
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("Welcome ");
			stringBuffer.append(customerName);
			stringBuffer.append(". You have placed an order of $");
			stringBuffer.append(ammount); 
			stringBuffer.append(". Press 1 to confirm and 2 to decline.");				
			
			TTSUtils.buildAudio(stringBuffer.toString(), "speech.wav");
			Thread.sleep(300);
			//Media Server Control Creation
			MsPeer peer = MsPeerFactory.getPeer("org.mobicents.mscontrol.impl.MsPeerImpl");
			MsProvider provider = peer.getProvider();
			MsSession session = provider.createSession();
			MsConnection connection = session.createNetworkConnection(MediaConnectionListener.PR_JNDI_NAME);
			MediaConnectionListener listener = new MediaConnectionListener();
			listener.setInviteRequest(sipServletRequest);
//			provider.addConnectionListener(listener);
			connection.addConnectionListener(listener);
			log.info("waiting to get the SDP from Media Server before sending the INVITE to " + callerAddress + "@" + callerDomain);
			connection.modify("$", null);
			sipServletRequest.getSession().setAttribute("customerName", customerName);
			sipServletRequest.getSession().setAttribute("customerPhone", customerPhone);
			sipServletRequest.getSession().setAttribute("amountOrder", amount);
			sipServletRequest.getSession().setAttribute("orderId", orderId);
			sipServletRequest.getSession().setAttribute("connection", connection);
			sipServletRequest.getSession().setAttribute("orderApproval", true);
			sipServletRequest.getSession().setAttribute("caller", (String)Contexts.getApplicationContext().get("caller.sip"));
			sipServletRequest.getSession().setAttribute("callerDomain", (String)Contexts.getApplicationContext().get("caller.domain"));
			sipServletRequest.getSession().setAttribute("callerPassword", (String)Contexts.getApplicationContext().get("caller.password"));
		} catch (UnsupportedOperationException uoe) {
			log.error("An unexpected exception occurred while trying to create the request for checkout confirmation", uoe);
		} catch (Exception e) {
			log.error("An unexpected exception occurred while trying to create the request for checkout confirmation", e);
		}				
	}	
	
	@Remove
	public void destroy() {
	}

}
