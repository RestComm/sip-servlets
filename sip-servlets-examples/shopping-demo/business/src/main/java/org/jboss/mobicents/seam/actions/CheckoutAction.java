/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.mobicents.seam.actions;

import java.io.File;
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
import javax.servlet.sip.SipApplicationSession;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipSessionsUtil;
import javax.servlet.sip.TimerService;
import javax.servlet.sip.URI;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import org.jboss.mobicents.seam.listeners.MediaConnectionListener;
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
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsPeer;
import org.mobicents.mscontrol.MsPeerFactory;
import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.MsSession;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;

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

	@Resource(mappedName="java:/sip/shopping-demo/SipFactory") SipFactory sipFactory;
	@Resource(mappedName="java:/sip/shopping-demo/TimerService") TimerService sipTimerService;
	@Resource(mappedName="java:/sip/shopping-demo/SipSessionsUtil") SipSessionsUtil sipSessionsUtil;
	
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
		System.out.println("SIPTIMERSERVICE" + sipTimerService);
		System.out.println("SIPFACTORY" + sipFactory);
		System.out.println("SIPSESSIONUTIL" + sipSessionsUtil);		
		//TODO remove hard coded uri from address header
		try {
			SipApplicationSession sipApplicationSession = sipFactory.createApplicationSession();
			Address fromAddress = sipFactory.createAddress("sip:admin@sip-servlets.com");
			Address toAddress = sipFactory.createAddress(customerPhone);
			SipServletRequest sipServletRequest = 
				sipFactory.createRequest(sipApplicationSession, "INVITE", fromAddress, toAddress);
			URI requestURI = sipFactory.createURI(customerPhone);
			sipServletRequest.setRequestURI(requestURI);
			//TTS file creation		
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(customerName);
			stringBuffer.append(" has placed an order of $");
			stringBuffer.append(ammount);
			stringBuffer.append(". Press 1 to approve and 2 to reject.");				
			
			buildAudio(stringBuffer.toString(), "speech.wav");
			Thread.sleep(300);
			//Media Server Control Creation
			MsPeer peer = MsPeerFactory.getPeer();
			MsProvider provider = peer.getProvider();
			MsSession session = provider.createSession();
			MsConnection connection = session.createNetworkConnection("media/trunk/IVR/1");
			MediaConnectionListener listener = new MediaConnectionListener();
			listener.setInviteRequest(sipServletRequest);
			connection.addConnectionListener(listener);
			connection.modify("$", null);
			sipApplicationSession.setAttribute("customerName", customerName);
			sipApplicationSession.setAttribute("amountOrder", amount);
			sipApplicationSession.setAttribute("connection", connection);			
		} catch (UnsupportedOperationException uoe) {
			// TODO log exception
			uoe.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	private void buildAudio(String text, String filename) throws Exception {
		VoiceManager mgr = VoiceManager.getInstance();
		Voice voice = mgr.getVoice("kevin16");
		voice.allocate();
		File speech = new File(filename);
		SingleFileAudioPlayer player = new SingleFileAudioPlayer(getBasename(speech.getAbsolutePath()), getAudioType(filename));
		voice.setAudioPlayer(player);
		voice.startBatch();
		boolean ok = voice.speak(text);
		voice.endBatch();
		player.close();
		voice.deallocate();
	}
	
	private static String getBasename(String path) {
		int index = path.lastIndexOf(".");
		if (index == -1) {
			return path;
		} else {
			return path.substring(0, index);
		}
	}
	
	private static String getExtension(String path) {
		int index = path.lastIndexOf(".");
		if (index == -1) {
			return null;
		} else {
			return path.substring(index + 1);
		}
	}
	
	private static AudioFileFormat.Type getAudioType(String file) {
		AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
		String extension = getExtension(file);

		for (int i = 0; i < types.length; i++) {
			if (types[i].getExtension().equals(extension)) {
				return types[i];
			}
		}
		return null;
	}
	
	@Remove
	public void destroy() {
	}

}
