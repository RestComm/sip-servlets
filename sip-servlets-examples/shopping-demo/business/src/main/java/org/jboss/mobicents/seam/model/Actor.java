/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.mobicents.seam.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

@Entity
@Table(name="ACTORS")
@Indexed
public class Actor
    implements Serializable
{
    private static final long serialVersionUID = 8176964737283403683L;

    long id;
    String name;

    @Id @GeneratedValue
    @Column(name="ID")
    @DocumentId
    public long getId() {
        return id;
    }                    
    public void setId(long id) {
        this.id = id;
    }     

    @Column(name="NAME", length=50)
    @Field(index = Index.TOKENIZED)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
