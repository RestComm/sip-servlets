/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.mobicents.seam.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="ORDERLINES")
public class OrderLine
    implements Serializable
{
    private static final long serialVersionUID = 207236100660985541L;

    long    lineId;
    int     position;
    Product product;
    int     quantity;
    Date    orderDate;
    Order   order;

    @Id @GeneratedValue
    @Column(name="ORDERLINEID")
    public long getLineId() {
        return lineId;
    }
    public void setLineId(long id) {
        this.lineId = id;
    }

    @Column(name="POS")
    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }
    
    @ManyToOne
    @JoinColumn(name="ORDERID")
    public Order getOrder() {
        return order;
    }
    public void setOrder(Order order) {
        this.order = order;
    }

    @ManyToOne
    @JoinColumn(name="PROD_ID",unique=false,nullable=false)
    public Product getProduct() {
        return product;
    }
    public void setProduct(Product product) {
        this.product=product;
    }

    @Column(name="QUANTITY",nullable=false)
    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    public void addQuantity(int howmany) {
        quantity += howmany;
    }
}
