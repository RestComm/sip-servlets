/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */ 
package org.jboss.mobicents.seam.actions;

import java.io.Serializable;

import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.jboss.mobicents.seam.model.Order;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.bpm.BeginTask;
import org.jboss.seam.annotations.bpm.EndTask;


@Stateful
@Name("accept")
public class AcceptAction
    implements Accept,
               Serializable
{
    private static final long serialVersionUID = -4439813828608177846L;

    @In(value="currentUser")
    Admin admin;

    @PersistenceContext(type=PersistenceContextType.EXTENDED)
    EntityManager em;

    @In
    Order order;

    @In
    Long orderId;

    @BeginTask
    public String viewTask() {
        return "accept";
    }

    @EndTask(transition="approve")
    public String accept() {
        order.process();
        return "admin";
    }

    @EndTask(transition="reject")
    public String reject() {
        order.cancel();
        return "admin";
    }

    @Remove
    public void destroy() {}
}
