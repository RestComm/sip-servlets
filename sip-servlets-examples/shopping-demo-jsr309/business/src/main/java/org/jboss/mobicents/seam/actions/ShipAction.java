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

package org.jboss.mobicents.seam.actions;

import java.io.Serializable;

import javax.ejb.Remove;
import javax.ejb.Stateful;

import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.jboss.mobicents.seam.model.Order;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.bpm.BeginTask;
import org.jboss.seam.annotations.bpm.EndTask;

@Stateful
@Name("ship")
public class ShipAction
    implements Ship,
               Serializable
{
    private static final long serialVersionUID = -5284603520443473953L;
    
    @In 
    Order order;
    
    String track;

    @NotNull
    @Length(min=4,max=10)
    public String getTrack() {
        return track;
    }
    
    public void setTrack(String track) {
        this.track=track;
    }

    @BeginTask
    public String viewTask() {
        return "ship";
    }
    
    @EndTask
    public String ship() {        
        order.ship(track);
        return "admin";
    }

    @Remove
    public void destroy() { }
}
