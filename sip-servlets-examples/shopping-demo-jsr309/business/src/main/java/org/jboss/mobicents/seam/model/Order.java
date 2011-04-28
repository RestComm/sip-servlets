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
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="ORDERS")
public class Order
    implements Serializable
{
    private static final long serialVersionUID = -5451107485769007079L;

    public enum Status {OPEN,CANCELLED,PROCESSING,SHIPPED}

    public static BigDecimal TAX_RATE = new BigDecimal(".0825");

    long orderId;
    Date orderDate;
    Timestamp deliveryDate;
    Customer customer;
    BigDecimal netAmount = BigDecimal.ZERO;
    BigDecimal tax = BigDecimal.ZERO;
    BigDecimal totalAmount = BigDecimal.ZERO;
    List<OrderLine> orderLines = new ArrayList<OrderLine>();
    Status status = Status.OPEN;
    String trackingNumber;

    @Id @GeneratedValue
    @Column(name="ORDERID")
    public long getOrderId() {
        return orderId;
    }                    
    public void setOrderId(long id) {
        this.orderId = id;
    }     

    @Column(name="ORDERDATE",nullable=false)
    public Date getOrderDate() {
        return orderDate;
    }
    public void setOrderDate(Date date) {
        this.orderDate = date;
    }
    
    @Column(name="DELIVERYDATE",nullable=true)
    public Timestamp getDeliveryDate() {
        return deliveryDate;
    }
    public void setDeliveryDate(Timestamp date) {
        this.deliveryDate = date;
    }    

    @Transient
    public boolean isEmpty() {
        return (orderLines == null) || (orderLines.size()==0);
    }
    
    @OneToMany(mappedBy="order", cascade=CascadeType.ALL)
    public List<OrderLine> getOrderLines() {
        return orderLines;
    }
    public void setOrderLines(List<OrderLine> lines) {
        this.orderLines = lines;
    }

    public void addProduct(Product product, int quantity) {
        for (OrderLine line: orderLines) {
            if (product.getProductId() == line.getProduct().getProductId()) {
                line.addQuantity(quantity);
                return;
            }
        }

        OrderLine line = new OrderLine();
        line.setProduct(product);
        line.setQuantity(quantity);
        line.setOrder(this);

        orderLines.add(line);
    }

    public void removeProduct(Product product) {
        for (OrderLine line: orderLines) {
            if (product.getProductId() == line.getProduct().getProductId()) { 
                orderLines.remove(line);
                return;
            }
        }
    }

    
    @ManyToOne
    @JoinColumn(name="USERID")
    public Customer getCustomer() {
        return customer;
    }
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Column(name="NETAMOUNT",nullable=false,precision=12,scale=2)
    public BigDecimal getNetAmount() {
        return netAmount;
    }
    public void setNetAmount(BigDecimal amount) {
        this.netAmount = amount;
    }

    @Column(name="TAX",nullable=false,precision=12,scale=2)
    public BigDecimal getTax() {
        return tax;
    }
    public void setTax(BigDecimal amount) {
        this.tax = amount;
    }

    @Column(name="TOTALAMOUNT",nullable=false,precision=12,scale=2)
    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(BigDecimal amount) {
        this.totalAmount = amount;
    }

    @Column(name="TRACKING")
    public String getTrackingNumber() { 
       return trackingNumber;
    }
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    @Column(name="STATUS")
    public Status getStatus() {
        return status;
    }
    public void setStatus(Status status) {
        this.status = status;
    }

    @Transient
    public int getStatusCode() {
        return status.ordinal();
    }

    public void calculateTotals() {
        BigDecimal total = BigDecimal.ZERO;
        
        int index = 1;
        for (OrderLine line: orderLines) {
            line.setPosition(index++);
            total = total.add(line.getProduct().getPrice().multiply(new BigDecimal(line.getQuantity())));
        }
        
        setNetAmount(total);
        
        setTax(round(getNetAmount().multiply(TAX_RATE)));
        setTotalAmount(getNetAmount().add(getTax()));
    }

    public void cancel() {
        setStatus(Status.CANCELLED);
    }

    public void process() {
        setStatus(Status.PROCESSING);
    }

    public void ship(String tracking) {
        setStatus(Status.SHIPPED);
        setTrackingNumber(tracking);
    }
    

    /**
     * round a positive big decimal to 2 decimal points
     */
    private BigDecimal round(BigDecimal amount) {
        return new BigDecimal(amount.movePointRight(2).add(new BigDecimal(".5")).toBigInteger()).movePointLeft(2);
    }

    @Transient
    public boolean isOpen() {
       return status == Status.OPEN;
    }

}
