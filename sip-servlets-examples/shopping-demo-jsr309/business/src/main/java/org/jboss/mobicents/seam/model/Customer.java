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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import org.hibernate.validator.Email;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Pattern;
import org.jboss.seam.annotations.Name;

@Entity
@Name("customer")
@DiscriminatorValue("customer")
public class Customer extends User
    implements Serializable
{
    private static final long serialVersionUID = 5699525147178760355L;

    public static String[] cctypes = {"MasterCard", "Visa", "Discover", "Amex", "Dell Preferred"}; 

    String  address1;
    String  address2;
    String  city;
    String  state;
    String  zip;  

    String  email;
    String  phone;

    Integer creditCardType = 1;
    String  creditCard     = "000-0000-0000";
    int     ccMonth        = 1;
    int     ccYear         = 2005;


    public Customer() {
    }


    @Column(name="ADDRESS1",length=50)
    @NotNull
    public String getAddress1() {
        return address1;
    }
    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    @Column(name="ADDRESS2",length=50)
    @NotNull
    public String getAddress2() {
        return address2;
    }
    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    @Column(name="CITY",length=50)  
    @NotNull
    public String getCity() {
        return city;
    }
    public void setCity(String city) {
        this.city = city;
    }


    @Column(name="STATE",length=2)
    @NotNull
    @Length(min=2,max=2)
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    @Column(name="ZIP", length=10)
    @Length(min=5, max=10)
    @Pattern(regex="[0-9]{5}(-[0-9]{4})?", message="not a valid zipcode") // {validator.zip}
    @NotNull
    public String getZip() {
        return zip;
    }
    public void setZip(String zip) {
        this.zip = zip;
    }

    @Column(name="EMAIL",length=50)
    @Email
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name="PHONE",length=50)
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }


    @Column(name="CREDITCARDTYPE")    
    public Integer getCreditCardType() {
        return creditCardType;
    }
    public void setCreditCardType(Integer type) {
        this.creditCardType = type;
    }

    @Transient public String getCreditCardTypeString() {
        if (creditCardType<1 || creditCardType>cctypes.length) {
            return "";
        }
        return cctypes[creditCardType-1];
    }

    @Column(name="CC_NUM", length=50)
    public String getCreditCard() {
        return creditCard;
    }
    public void setCreditCard(String creditCard) {
        this.creditCard = creditCard;
    }

    @Column(name="CC_MONTH", length=50)
    public int getCreditCardMonth() {
        return ccMonth;
    }
    public void setCreditCardMonth(int ccMonth) {
        this.ccMonth = ccMonth;
    }

    @Column(name="CC_YEAR", length=50)
    public int getCreditCardYear() {
        return ccYear;
    }
    public void setCreditCardYear(int ccYear) {
        this.ccYear = ccYear;
    }

    @Transient
    public String getCreditCardExpiration() {
        return "" + ccMonth + "/" + ccYear;
    }

    @Override
    public String toString() {
        return "Customer#" + getId() + "(" + userName + ")";
    }

}
