package org.jboss.mobicents.seam.test;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;

import org.jboss.mobicents.seam.actions.StoreManager;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

public class StoreManagerTest 
    extends SeamTest
{
    @Test
    public void testTopProducts() 
        throws Exception
    {
        
        new FacesRequest() {
            StoreManager manager;
            @Override
            protected void updateModelValues()
            {
                manager = (StoreManager) getInstance("stats");
            }
            @Override
            protected void renderResponse()
            {
                // these are from order instances - 
                assertEquals("number orders",   0L,    manager.getNumberOrders());
                assertEquals("total sales",     BigDecimal.ZERO,   manager.getTotalSales());

                // these are from inventory
                assertEquals("units sold",      5734,  manager.getUnitsSold());
                assertEquals("total inventory", 23432, manager.getTotalInventory());
            }               
        }.run();
    }

}
