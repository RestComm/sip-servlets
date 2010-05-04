/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.mobicents.seam.model;

import java.io.Serializable;

import javax.persistence.*;
import org.hibernate.validator.*;

@Entity
@Table(name="USERS")
public abstract class User
    implements Serializable
{
    long    id;

    String  userName;
    String  password;

    String  firstName;
    String  lastName;

    @Id @GeneratedValue
    @Column(name="USERID")
    public long getId() {
        return id;
    }                    
    public void setId(long id) {
        this.id = id;
    }     

    @Column(name="USERNAME",unique=true,nullable=false,length=50)
    @NotNull 
    @Length(min=4,max=16)
    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name="PASSWORD",nullable=false,length=50)
    @NotNull
    @Length(min=6,max=50)
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name="FIRSTNAME",length=50)
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    @Column(name="LASTNAME",length=50)    
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    @Transient
    public boolean isAdmin() {
       return false;
    }
}
