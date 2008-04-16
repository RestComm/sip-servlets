package org.jboss.mobicents.seam.actions;

import java.math.BigDecimal;

import javax.naming.InitialContext;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;

@Name("beforeOrderProcessed")
public class BeforeOrderProcessedAction {
	
    @In String  customerfullname;
    @In String  cutomerphone;
    @In BigDecimal amount;
    @In Long orderId;	
    
	@Out(value = "adminExternalActivityHandle", scope = ScopeType.BUSINESS_PROCESS, required = false)
//	ExternalActivityHandle handle;  
    
    public void fireBeforeOrderProcessedEvent()
    {
        System.out.println( "***************Fire BEFORE_ORDER_PROCESSED . Custom event to call user to set date ***************************" );
        System.out.println( "Customer Name = " + customerfullname);	
        System.out.println( "Phone = " + cutomerphone);
        System.out.println( "orderId = " + orderId);
        System.out.println( "Amount = " + amount);
        
        // TODO replace by Sip Servlets call
		        
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
//			handle = conn1.createActivityHandle();
//
//			EventTypeID requestType = conn1.getEventTypeID(
//					"org.mobicents.slee.service.dvddemo.BEFORE_ORDER_PROCESSED",
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
