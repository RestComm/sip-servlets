package org.jboss.mobicents.seam.test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.jboss.mobicents.seam.model.Product;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;



public class BestSellersTest 
    extends SeamTest
{
    @Test
    public void testTopProducts() 
        throws Exception
    {        
        new NonFacesRequest() {         
            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse()
            {
                List<Product> products = (List<Product>) getValue("#{topProducts}");

                assertNotNull("topProducts", products);
                assertEquals("topProducts size",  8, products.size());               

                Product prev = null;
                for (Product p: products) {
                    if (prev != null) {
                        assertTrue("descending order", 
                                p.getInventory().getSales() <= prev.getInventory().getSales());
                    }

                    prev = p;
                }

                // 14.98/29.99/39.95

                assertEquals("price 1", new BigDecimal("14.98"), products.get(0).getPrice());
                assertEquals("price 2", new BigDecimal("29.99"), products.get(1).getPrice());
                assertEquals("price 3", new BigDecimal("39.95"), products.get(2).getPrice());
            }               
        }.run();
    }
}
