/*
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
package org.mobicents.slee.service.alerting;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.Sbb;
import javax.slee.SbbContext;

import net.java.slee.resource.smpp.ActivityContextInterfaceFactory;
import net.java.slee.resource.smpp.ClientTransaction;
import net.java.slee.resource.smpp.Dialog;
import net.java.slee.resource.smpp.ShortMessage;
import net.java.slee.resource.smpp.SmppProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mobicents.slee.service.events.SmsAlertingCustomEvent;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public abstract class SmsAlertingSbb implements Sbb { 
	
	// the sbb's sbbContext
	private SbbContext sbbContext;
	
	private Log logger = LogFactory.getLog(SmsAlertingSbb.class);

	private SmppProvider smppProvider;

	private ActivityContextInterfaceFactory smppAcif;
	
	public SmsAlertingSbb() {
		super();
	}

	protected SbbContext getSbbContext() {
		return this.sbbContext;
	}
	/**
	 * implements javax.slee.Sbb Please refer to JSLEE v1.1 Specification, Early
	 * Draft Review Page 54 for further information. <br>
	 * The SLEE invokes this method after a new instance of the SBB abstract
	 * class is created. During this method, an SBB entity has not been assigned
	 * to the SBB object. The SBB object can take advantage of this method to
	 * allocate and initialize state or connect to resources that are to be held
	 * by the SBB object during its lifetime. Such state and resources cannot be
	 * specific to an SBB entity because the SBB object might be reused during
	 * its lifetime to serve multiple SBB entities. <br>
	 * This method indicates a transition from state "DOES NOT EXIST" to
	 * "POOLED" (see page 52)
	 */
	public void setSbbContext(SbbContext sbbContext) {
		this.sbbContext = sbbContext;
		try {
			Context ctx = (Context) new InitialContext()
					.lookup("java:comp/env");

			smppProvider = (SmppProvider) 
				ctx.lookup("slee/resources/smpp/3.4/smppinterface");            
			smppAcif = (ActivityContextInterfaceFactory) 
            	ctx.lookup("slee/resources/smpp/3.4/factoryprovider");
		} catch (NamingException ne) {
			logger.error("Could not set SBB context: " + ne.toString(), ne);
		}
	}

	public void onSendSmsAlert(SmsAlertingCustomEvent event, ActivityContextInterface aci) {
		logger.info("****** org.mobicents.slee.service.alerting.SEND_SMS ******* ");
		logger.info(this
				+ ": received a SEND_SMS event. alertId = "
				+ event.getAlertId() + ". alertText = " + event.getAlertText() + ", tel = " +  event.getTel());

		Dialog dialog = smppProvider.getDialog(event.getTel(), "0020");
        
        ShortMessage sms = dialog.createMessage();        
        sms.setText("alertId = "+ event.getAlertId() + ". alertText = " + event.getAlertText());
        
        ClientTransaction tx = dialog.createSubmitSmTransaction();
        try {
	        ActivityContextInterface ac = smppAcif.getActivityContextInterface(tx);
	        ac.attach(sbbContext.getSbbLocalObject());
	        tx.send(sms);
        } catch (Exception e) {
        	logger.error("couldn't send the alerting sms (alertId = "
				+ event.getAlertId() + ". alertText = " + event.getAlertText() + ", tel = " +  event.getTel() + ")", e);
        }
	}	
	
	/**
	 * implements javax.slee.Sbb Please refer to JSLEE v1.1 Specification, Early
	 * Draft Review Page 54 for further information. <br>
	 * The SLEE invokes this method before terminating the life of the SBB
	 * object. The SBB object can take advantage of this method to free state or
	 * resources that are held by the SBB object. These state and resources
	 * typically had been allocated by the setSbbContext method. <br>
	 * This method indicates a transition from state "POOLED" to "DOES NOT
	 * EXIST" (see page 52)
	 */
	public void unsetSbbContext() {
		logger.info("CommonSbb: " + this + ": unsetSbbContext() called.");
	}

	/**
	 * implements javax.slee.Sbb Please refer to JSLEE v1.1 Specification, Early
	 * Draft Review Page 55 for further information. <br>
	 * The SLEE invokes this method on an SBB object before the SLEE creates a
	 * new SBB entity in response to an initial event or an invocation of the
	 * create method on a ChildRelation object. This method should initialize
	 * the SBB object using the CMP field get and set accessor methods, such
	 * that when this method returns, the persistent representation of the SBB
	 * entity can be created. <br>
	 * This method is the first part of a transition from state "POOLED" to
	 * "READY" (see page 52)
	 */
	public void sbbCreate() throws javax.slee.CreateException {
		logger.info("CommonSbb: " + this + ": sbbCreate() called.");
	}

	/**
	 * implements javax.slee.Sbb Please refer to JSLEE v1.1 Specification, Early
	 * Draft Review Page 55 for further information. <br>
	 * The SLEE invokes this method on an SBB object after the SLEE creates a
	 * new SBB entity. The SLEE invokes this method after the persistent
	 * representation of the SBB entity has been created and the SBB object is
	 * assigned to the created SBB entity. This method gives the SBB object a
	 * chance to initialize additional transient state and acquire additional
	 * resources that it needs while it is in the Ready state. <br>
	 * This method is the second part of a transition from state "POOLED" to
	 * "READY" (see page 52)
	 */
	public void sbbPostCreate() throws CreateException {
		logger.info("CommonSbb: " + this + ": sbbPostCreate() called.");
	}

	/**
	 * implements javax.slee.Sbb Please refer to JSLEE v1.1 Specification, Early
	 * Draft Review Page 55 for further information. <br>
	 * The SLEE invokes this method on an SBB object when the SLEE picks the SBB
	 * object in the pooled state and assigns it to a specific SBB entity. This
	 * method gives the SBB object a chance to initialize additional transient
	 * state and acquire additional resources that it needs while it is in the
	 * Ready state. <br>
	 * This method indicates a transition from state "POOLED" to "READY" (see
	 * page 52)
	 */
	public void sbbActivate() {
		logger.info("CommonSbb: " + this + ": sbbActivate() called.");
	}

	/**
	 * implements javax.slee.Sbb Please refer to JSLEE v1.1 Specification, Early
	 * Draft Review Page 56 for further information. <br>
	 * The SLEE invokes this method on an SBB object when the SLEE decides to
	 * disassociate the SBB object from the SBB entity, and to put the SBB
	 * object back into the pool of available SBB objects. This method gives the
	 * SBB object the chance to release any state or resources that should not
	 * be held while the SBB object is in the pool. These state and resources
	 * typically had been allocated during the sbbActivate method. <br>
	 * This method indicates a transition from state "READY" to "POOLED" (see
	 * page 52)
	 */
	public void sbbPassivate() {
		logger.info("CommonSbb: " + this + ": sbbPassivate() called.");
	}

	/**
	 * implements javax.slee.Sbb Please refer to JSLEE v1.1 Specification, Early
	 * Draft Review Page 56 for further information. <br>
	 * The SLEE invokes the sbbRemove method on an SBB object before the SLEE
	 * removes the SBB entity assigned to the SBB object. <br>
	 * This method indicates a transition from state "READY" to "POOLED" (see
	 * page 52)
	 */
	public void sbbRemove() {
		logger.info("CommonSbb: " + this + ": sbbRemove() called.");
	}

	/**
	 * implements javax.slee.Sbb Please refer to JSLEE v1.1 Specification, Early
	 * Draft Review Page 56 for further information. <br>
	 * The SLEE calls this method to synchronize the state of an SBB object with
	 * its assigned SBB entity�s persistent state. The SBB Developer can assume
	 * that the SBB object�s persistent state has been loaded just before this
	 * method is invoked. <br>
	 * This method indicates a transition from state "READY" to "READY" (see
	 * page 52)
	 */
	public void sbbLoad() {
		logger.info("CommonSbb: " + this + ": sbbLoad() called.");
	}

	/**
	 * implements javax.slee.Sbb Please refer to JSLEE v1.1 Specification, Early
	 * Draft Review Page 57 for further information. <br>
	 * The SLEE calls this method to synchronize the state of the SBB entity�s
	 * persistent state with the state of the SBB object. The SBB Developer
	 * should use this method to update the SBB object using the CMP field
	 * accessor methods before its persistent state is synchronized. <br>
	 * This method indicates a transition from state "READY" to "READY" (see
	 * page 52)
	 */
	public void sbbStore() {
		logger.info("CommonSbb: " + this + ": sbbStore() called.");
	}

	/**
	 * implements javax.slee.Sbb Please refer to JSLEE v1.1 Specification, Early
	 * Draft Review Page 67 for further information. <br>
	 * The SLEE invokes the sbbRolledBack callback method after a transaction
	 * used in a SLEE originated invocation has rolled back.
	 */
	public void sbbRolledBack(javax.slee.RolledBackContext rolledBackContext) {
		logger.info("CommonSbb: " + this + ": sbbRolledBack() called.");
	}

	/**
	 * implements javax.slee.Sbb Please refer to JSLEE v1.1 Specification, Early
	 * Draft Review Page 65 for further information. <br>
	 * The SLEE invokes this method after a SLEE originated invocation of a
	 * transactional method of the SBB object returns by throwing a
	 * RuntimeException.
	 */
	public void sbbExceptionThrown(Exception exception, Object obj,
			javax.slee.ActivityContextInterface activityContextInterface) {
		logger.info("CommonSbb: " + this + ": sbbExceptionThrown() called.");
	}

}
