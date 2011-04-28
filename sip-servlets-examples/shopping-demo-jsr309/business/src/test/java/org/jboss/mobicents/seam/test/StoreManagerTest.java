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
