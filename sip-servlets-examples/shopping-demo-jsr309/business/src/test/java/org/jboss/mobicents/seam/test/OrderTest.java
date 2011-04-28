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

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.DataModel;

import org.jboss.mobicents.seam.actions.ShoppingCart;
import org.jboss.mobicents.seam.model.Order;
import org.jboss.mobicents.seam.model.Product;
import org.jboss.mobicents.seam.model.User;
import org.jboss.mobicents.seam.model.Order.Status;
import org.jboss.seam.core.Conversation;
import org.jboss.seam.mock.SeamTest;
import org.jboss.seam.security.NotLoggedInException;
import org.jbpm.jpdl.el.ELException;
import org.testng.annotations.Test;

public class OrderTest
    extends SeamTest
{
    
    @Test 
    public void selectDvd() 
        throws Exception 
    {
        new FacesRequest("/dvd.xhtml") {
            @Override
            protected void  beforeRequest() {
                setParameter("id", "41");
            }

            @Override
            protected void renderResponse() throws Exception {
                Product dvd = (Product) getValue("#{dvd}");
                assert dvd != null;
                assert dvd.getProductId() == 41;                               
            }
        }.run();
    }
        
    @Test
    public void addToCart() 
        throws Exception 
    {
        String id = new FacesRequest("/dvd.xhtml") {
            @Override
            protected void beforeRequest() {
                setParameter("id", "41");
            }
        }.run();
        new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{search.addToCart}");
            }
            
            @Override
            protected void renderResponse() throws Exception {
                ShoppingCart cart = (ShoppingCart) getValue("#{cart}");
                assert cart != null;
                assert cart.getCart().size() == 1;
            }
        }.run();
        
        new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void beforeRequest() {
                setParameter("id", "42");
            }          
        }.run();
        
        new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{search.addToCart}");
            }
            
            @Override
            protected void renderResponse() throws Exception {
                ShoppingCart cart = (ShoppingCart) getValue("#{cart}");
                assert cart != null;
                assert cart.getCart().size() == 2;
            }
        }.run();
        
        new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void beforeRequest() {
                setParameter("id", "41");
            }         
        }.run();
        
        new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{search.addToCart}");
            }
            
            @Override
            protected void renderResponse() throws Exception {
                ShoppingCart cart = (ShoppingCart) getValue("#{cart}");
                assert cart != null;
                assert cart.getCart().size() == 2;
            }
        }.run();
        
        
        new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void beforeRequest() {
                setParameter("id", "43");
            }           
        }.run();
        
        new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{search.addToCart}");
            }
            
            @Override
            protected void renderResponse() throws Exception {
                ShoppingCart cart = (ShoppingCart) getValue("#{cart}");
                assert cart != null;
                assert cart.getCart().size() == 3;
            }
        }.run();
        
    }
    
    @Test
    public void checkoutNotLoggedIn() throws Exception {
        String id = new FacesRequest("/dvd.xhtml") {
            @Override
            protected void beforeRequest() {
                setParameter("id", "41");
            }
        }.run();
        
        id = new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{search.addToCart}");
            }
        }.run();
        
        id = new FacesRequest("/checkout.xhtml", id) {
        }.run();
             
        id = new FacesRequest("/checkout.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{checkout.createOrder}");
            }
            @Override
            protected void renderResponse() throws Exception {
                Order order = (Order) getValue("#{order}");
                assert order != null;
                
            }
        }.run();    
        
        id = new FacesRequest("/checkout.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
//                try {
                    invokeAction("#{checkout.submitOrder}");
                    assert false; // should fail
//                } catch (ELException e) {
//                    assert e.getCause() instanceof NotLoggedInException;
//                }
            }
            @Override
            protected void renderResponse() throws Exception {
                Order order = (Order) getValue("#{order}");
                assert order != null;
            }
        }.run();    
        
        id = new FacesRequest("/checkout.xhtml", id) {
            @Override
            protected void applyRequestValues() throws Exception {
               setValue("#{identity.username}", "user1");
               setValue("#{identity.password}", "password");
            }
            protected void invokeApplication() throws Exception {
                invokeAction("#{identity.login}");
            }
            @Override
            protected void renderResponse() throws Exception {
                assert getValue("#{identity.loggedIn}").equals(Boolean.TRUE);
                User currentUser = (User) getValue("#{currentUser}");
                assert currentUser.getUserName().equals("user1");
            }
        }.run();       
        
    }
    
    public long makeOrder() throws Exception {
        String id = new FacesRequest("/dvd.xhtml") {
            @Override
            protected void beforeRequest() {
                setParameter("id", "41");
            }
        }.run();
        
        id = new FacesRequest("/dvd.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{search.addToCart}");
            }
        }.run();
        
        id = new NonFacesRequest("/checkout.xhtml", id) {
        }.run();
        
        id = new FacesRequest("/checkout.xhtml", id) {
            @Override
            protected void applyRequestValues() throws Exception {
               setValue("#{identity.username}", "user1");
               setValue("#{identity.password}", "password");
            }
            protected void invokeApplication() throws Exception {
                invokeAction("#{identity.login}");
            }
            @Override
            protected void renderResponse() throws Exception {
                assert getValue("#{identity.loggedIn}").equals(Boolean.TRUE);
                User currentUser = (User) getValue("#{currentUser}");
                assert currentUser.getUserName().equals("user1");
            }
        }.run();       

        id = new FacesRequest("/checkout.xhtml", id) {
            @Override
            protected void invokeApplication() throws Exception {             
                invokeAction("#{checkout.createOrder}");
                Order order = (Order) getValue("#{currentOrder}");
                assert order!=null;
            }         
        }.run();                   
        
        id = new NonFacesRequest("/confirm.xhtml", id) {                   
        }.run();    
                
        
        final Wrapper<Long> orderId = new Wrapper<Long>();
        
        id = new FacesRequest("/confirm.xhtml", id) {
            protected void invokeApplication() throws Exception {
                invokeAction("#{checkout.submitOrder}");
            }
            @Override
            protected void renderResponse() throws Exception {
                Order order = (Order) getValue("#{completedOrder}");
                assert order!=null;
                assert order.getCustomer().getUserName().equals("user1");
                assert order.getStatus().equals(Status.OPEN);
                
                orderId.setValue(order.getOrderId());
            }
        }.run();
        
        return orderId.getValue();
    }
    
    @Test
    public void checkout() throws Exception {
        makeOrder();
    }
    
    @Test 
    public void showOrders() throws Exception {
        final long order1 = makeOrder();
        final long order2 = makeOrder();
        final long order3 = makeOrder();
        
        new NonFacesRequest("/showOrders.xhtml") {
            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                DataModel model = (DataModel) getValue("#{orders}");

                List<Long> orders = new ArrayList<Long>();
                for (Order order: (List<Order>) model.getWrappedData()) {
                    orders.add(order.getOrderId());
                }

                assert orders.contains(order1);
                assert orders.contains(order2);
                assert orders.contains(order3);
            }
            
        }.run();        
    }
    
    
    @Test 
    public void cancelOrder() throws Exception {
        final long order1 = makeOrder();      
        
        String id = new NonFacesRequest("/showorders.xhtml") {
            @SuppressWarnings("unchecked")
            @Override
            protected void renderResponse() throws Exception {
                DataModel model = (DataModel) getValue("#{orders}");
                assert model!=null;
                
                assert Conversation.instance().isLongRunning();
            }
            
        }.run();  
        
        id = new FacesRequest("/showorders.xhtml",id) {
            @SuppressWarnings("unchecked")
            @Override
            protected void applyRequestValues() throws Exception {
               DataModel model = (DataModel) getValue("#{orders}");
            
               int index =0;
               for (Order order: (List<Order>) model.getWrappedData()) {
                   if (order.getOrderId() == order1) {
                       model.setRowIndex(index);
                       break;
                   }
                   index++;
               }
            }
         
            @Override
            protected void invokeApplication() throws Exception {
               invokeAction("#{showorders.detailOrder}");
            }
            
            @Override
            protected void renderResponse() throws Exception {
                assert false;
            }
        }.run();
        
        id = new FacesRequest("/showorders.xhtml",id) {           
            @Override
            protected void renderResponse() throws Exception {
                Order order = (Order) getValue("#{myorder}");                
                assert order.getOrderId() == order1;
                assert order.getStatus() == Status.OPEN;

            }
        }.run();
        
        id = new FacesRequest("/showorders.xhtml",id) {  
            @Override
            protected void invokeApplication() throws Exception {
                invokeAction("#{showorders.cancelOrder}");
            }
            @Override
            protected void renderResponse() throws Exception {
                Order order = (Order) getValue("#{myorder}");                
                assert order.getOrderId() == order1;
                assert order.getStatus() == Status.CANCELLED;
                assert false;
            }
        }.run();
    }
    
    
    static class Wrapper<T> {
        T value;
        
        public void setValue(T value) {
            this.value = value;
        }
        
        public T getValue() {
            return value;
        }
    }
}
