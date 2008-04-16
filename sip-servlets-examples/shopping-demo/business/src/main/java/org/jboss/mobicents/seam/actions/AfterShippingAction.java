package org.jboss.mobicents.seam.actions;

import java.math.BigDecimal;

import javax.naming.InitialContext;

import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

/**
 * An example of a Seam component used to handle a jBPM transition event.
 * 
 * @author Amit Bhayani
 */
@Name("afterShipping")
public class AfterShippingAction {
	@In
	String customerfullname;

	@In
	String cutomerphone;

	@In
	BigDecimal amount;

	@In
	Long orderId;

	public void orderShipped() {
		System.out
				.println("*************** Fire ORDER_SHIPPED  ***************************");
		System.out.println("First Name = " + customerfullname);
		System.out.println("Phone = " + cutomerphone);
		System.out.println("orderId = " + orderId);

		//TODO replace by Sip Servlets call
		
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
//					"org.mobicents.slee.service.dvddemo.ORDER_SHIPPED",
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
