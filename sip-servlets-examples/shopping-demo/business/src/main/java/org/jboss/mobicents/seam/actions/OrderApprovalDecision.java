package org.jboss.mobicents.seam.actions;

import java.math.BigDecimal;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;

@Name("orderApproval")
public class OrderApprovalDecision {
    private static BigDecimal CUTOFF = new BigDecimal(100);

    @In BigDecimal amount;
    
    public String getHowLargeIsOrder()
    {
        return (amount.compareTo(CUTOFF) >= 0) ? "large order" : "small order";
     }
}
