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

package org.jboss.mobicents.seam.model;

import java.io.Serializable;

import javax.persistence.*;

@Entity
@Table(name="INVENTORY")
public class Inventory 
    implements Serializable
{
    private static final long serialVersionUID = 6114190195644971985L;

    int     quantity;
    int     sales;
    long    inventoryId;
    Product product;

    @Id @GeneratedValue
    @Column(name="INV_ID")
    public long getInventoryId() {
        return inventoryId;
    }
    public void setInventoryId(long id) {
        this.inventoryId = id;
    }

    @OneToOne(optional=false)
    @JoinColumn(name="PROD_ID")
    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
        this.product = product;
    }

    @Column(name="QUAN_IN_STOCK",nullable=false)
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Column(name="SALES",nullable=false)
    public int getSales() {
        return sales;
    }
    public void setSales(int sales) {
        this.sales = sales;
    }


    public boolean order(int howmany) {
        if (howmany > quantity) {
            return false;
        }

        quantity -= howmany;
        sales += howmany;

        return true;
    }
}
