package org.jboss.mobicents.seam.actions;

import java.math.BigDecimal;

import javax.naming.InitialContext;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

/**
 * Used for setting date and time for delivery
 * 
 * @author amit.bhayani
 * 
 */

@Name("afterOrderProcessed")
public class AfterOrderProcessedAction {
	@In
	String customerfullname;

	@In
	String cutomerphone;

	@In
	BigDecimal amount;

	@In
	Long orderId;

	public void fireOrderProcessedEvent() {
		System.out
				.println("*************** Fire ORDER_PROCESSED  ***************************");
		System.out.println("First Name = " + customerfullname);
		System.out.println("Phone = " + cutomerphone);
		System.out.println("orderId = " + orderId);

		//TODO : replace by Sip Servlets Call
		
//		try {			
//
//			InitialContext ic = new InitialContext();
//			
//			SleeConnectionFactory factory = (SleeConnectionFactory) ic
//					.lookup("java:/MobicentsConnectionFactory");
//
//			SleeConnection conn1 = null;
//			conn1 = factory.getConnection();
//
//			ExternalActivityHandle handle = conn1.createActivityHandle();
//
//			EventTypeID requestType = conn1.getEventTypeID(
//					"org.mobicents.slee.service.dvddemo.ORDER_PROCESSED",
//					"org.mobicents", "1.0");
//			CustomEvent customEvent = new CustomEvent(orderId, amount,
//					customerfullname, cutomerphone);
//
//			conn1.fireEvent(customEvent, requestType, handle, null);
//			conn1.close();
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}


}
