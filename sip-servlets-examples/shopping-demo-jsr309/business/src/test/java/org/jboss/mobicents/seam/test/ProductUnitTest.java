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
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;

import org.jboss.mobicents.seam.model.Product;
import org.jboss.seam.mock.SeamTest;
import org.testng.annotations.Test;

public class ProductUnitTest 
    extends SeamTest
{   
    EntityManager em() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("dvdDatabase");
        EntityManager        em  = emf.createEntityManager();
        assertNotNull("entity manager", em);
        assertTrue("entity manager open", em.isOpen());
        return em;
    }


    @Test
    public void testRequiredAttributes()
        throws Exception
    {
        Product p = new Product();

        EntityManager em = em();
        try {
            em.persist(p);
            fail("empty product persisted");
        } catch (PersistenceException e) {
            // good
        } finally {
            em.close();
        }
    }

    @Test 
    public void testCreateDelete() {
        EntityManager em = em();

        Product p = new Product();
        p.setTitle("test");

        em.getTransaction().begin();
        em.persist(p);
        em.getTransaction().commit();

        long id = p.getProductId();
        assertTrue("product id set", id != 0);
        
        p = em.find(Product.class ,id);
        assertNotNull("find by id", p);
        assertEquals("id", id, p.getProductId());
        assertEquals("title", "test", p.getTitle());

        em.getTransaction().begin();
        em.remove(p);
        em.getTransaction().commit();
        
        p = em.find(Product.class, id);
        assertNull("deleted product", p);
    }
    
}
