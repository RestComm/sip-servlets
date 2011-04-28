/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
