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

package org.mobicents.slee.service.interop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.slee.ActivityContextInterface;
import javax.slee.CreateException;
import javax.slee.Sbb;
import javax.slee.SbbContext;
import javax.slee.UnrecognizedActivityException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.mobicents.seam.CallManager;
import org.mobicents.mscontrol.MsConnection;
import org.mobicents.mscontrol.MsConnectionEvent;
import org.mobicents.mscontrol.MsEndpoint;
import org.mobicents.mscontrol.MsLink;
import org.mobicents.mscontrol.MsLinkEvent;
import org.mobicents.mscontrol.MsLinkMode;
import org.mobicents.mscontrol.MsNotifyEvent;
import org.mobicents.mscontrol.MsProvider;
import org.mobicents.mscontrol.MsSession;
import org.mobicents.mscontrol.events.MsEventAction;
import org.mobicents.mscontrol.events.MsEventFactory;
import org.mobicents.mscontrol.events.MsEventIdentifier;
import org.mobicents.mscontrol.events.MsRequestedEvent;
import org.mobicents.mscontrol.events.MsRequestedSignal;
import org.mobicents.mscontrol.events.ann.MsPlayRequestedSignal;
import org.mobicents.mscontrol.events.dtmf.MsDtmfNotifyEvent;
import org.mobicents.mscontrol.events.dtmf.MsDtmfRequestedEvent;
import org.mobicents.mscontrol.events.pkg.DTMF;
import org.mobicents.mscontrol.events.pkg.MsAnnouncement;
import org.mobicents.slee.resource.media.ratype.MediaRaActivityContextInterfaceFactory;
import org.mobicents.slee.resource.tts.ratype.TTSActivityContextInterfaceFactory;
import org.mobicents.slee.resource.tts.ratype.TTSProvider;
import org.mobicents.slee.resource.tts.ratype.TTSSession;
import org.mobicents.slee.service.events.InteropCustomEvent;

/**
 * 
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A> 
 *
 */
public abstract class InteropSbb implements Sbb { 

	private static final String IVR_ENDPOINT_NAME = "media/trunk/IVR/$";
	private static final String PR_ENDPOINT_NAME = "media/trunk/PacketRelay/$";

	private static final String OPENING_ANNOUNCEMENT = "Welcome to JavaOne 2008. Please enter your booth number followed by the pound sign to get some free beers.";
	
	// the sbb's sbbContext
	private SbbContext sbbContext;
	
	private Log logger = LogFactory.getLog(InteropSbb.class);

	String audioFilePath = null;

	String callerSip = null;

	String adminSip = null;

	long waitingTime = 0;

	private MsProvider msProvider;

	private MediaRaActivityContextInterfaceFactory mediaAcif;
	
	private TTSActivityContextInterfaceFactory ttsActivityContextInterfaceFactory;

	private TTSProvider ttsProvider;	


	/** Creates a new instance of SecondBounceSbb */
	public InteropSbb() {
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

			audioFilePath = System.getProperty("jboss.server.data.dir") + "/JavaOne2008.wav";

			msProvider = (MsProvider) ctx
					.lookup("slee/resources/media/1.0/provider");

			mediaAcif = (MediaRaActivityContextInterfaceFactory) ctx
					.lookup("slee/resources/media/1.0/acifactory");
			
			ttsActivityContextInterfaceFactory = (TTSActivityContextInterfaceFactory) ctx
			.lookup("slee/resources/ttsRA/0.1/acifactory");

			ttsProvider = (TTSProvider) ctx
			.lookup("slee/resources/ttsRA/0.1/provider");


		} catch (NamingException ne) {
			logger.error("Could not set SBB context: " + ne.toString(), ne);
		}
	}

	public void onInitMedia(InteropCustomEvent event, ActivityContextInterface ac) {
		logger.info("****** org.mobicents.slee.service.interopdemo.INIT_MEDIA ******* ");
		logger.info("InteropSbb: " + this
				+ ": received an INIT_MEDIA event. username = "
				+ event.getBoothNumber() + ". sdpContent = " + new String(event.getSdpContent()));

		this.setInteropCustomEvent(event);
		MsConnection msConnection = msProvider.createSession().createNetworkConnection(PR_ENDPOINT_NAME);
		try {
			ActivityContextInterface msAci = mediaAcif.getActivityContextInterface(msConnection);
			msAci.attach(this.getSbbContext().getSbbLocalObject());
		} catch (Exception ex) {
           logger.error("Internal server error", ex);          
           return;
       }
		msConnection.modify("$", new String(event.getSdpContent()));			
	}
	
	public void onPlayOpeningAnnouncement(InteropCustomEvent event, ActivityContextInterface ac) {
		logger.info("****** org.mobicents.slee.service.interopdemo.PLAY_ANN ******* ");
		logger.info("InteropSbb: " + this
				+ ": received an PLAY_ANN_OPENING event. username = "
				+ event.getBoothNumber() + ". sdpContent = " + event.getSdpContent());

		this.setInteropCustomEvent(event);
		
		playAnnouncement(OPENING_ANNOUNCEMENT, false, true, false);							
	}
	
	public void onPlayConfirmationAnnouncement(InteropCustomEvent event, ActivityContextInterface ac) {
		logger.info("****** org.mobicents.slee.service.interopdemo.PLAY_ANN ******* ");
		logger.info("InteropSbb: " + this
				+ ": received an PLAY_ANN_CONFIRM event. username = "
				+ event.getBoothNumber() + ". sdpContent = " + event.getSdpContent());

		this.setInteropCustomEvent(event);
		
		String announcement = generateConfirmationAnnouncement(event.getBoothNumber());
		
		playAnnouncement(announcement, true, false, true);
	}

	/**
	 * @param event
	 * @return
	 */
	private static String generateConfirmationAnnouncement(
			String boothNumber) {
		
		StringBuffer stringBuffer = new StringBuffer();
		if(boothNumber !=null && boothNumber.length() > 0) {				
			stringBuffer.append("Free beers are on their way to your booth number ");		
			stringBuffer.append(boothNumber);
			stringBuffer.append(". Feel free to call again. Bye.");
		} else {
			stringBuffer.append("We didn't understand your booth number, sorry. Please call again !");					
		}
		return stringBuffer.toString();
	}
	
	public void onLinkConnected(MsLinkEvent evt, ActivityContextInterface aci) {
		System.out.println("LINK CONNECTED");
	}
	
	private void playAnnouncement(String announcement, boolean attachToGeneratorActivity, boolean listenForDTMF, boolean listenForCompletion) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {}
		
		TTSSession ttsSession = ttsProvider.getNewTTSSession(
				audioFilePath, "kevin");
		
		ttsSession.textToAudioFile(announcement);		
		
		MsEventFactory eventFactory = msProvider
			.getEventFactory();
		
		MsLink link = getLink();
		if(link == null) {
			logger.error("Connection could not be created, closing the call ");
			CallManager callManagerRef = (CallManager)this.getInteropCustomEvent().getCallManagerRef();
			try {
				callManagerRef.endCall(null, false);
			} catch (IOException e) {
				logger.error("Impossible to call back the EJB", e);				
			}
			return;
		}
		if(attachToGeneratorActivity) {
			try {
				ActivityContextInterface generatorActivity = mediaAcif
						.getActivityContextInterface(link.getSession());
				generatorActivity.attach(getSbbContext().getSbbLocalObject());				
			} catch (javax.slee.UnrecognizedActivityException e) {
				logger.error("Impossible to attach to Media Signal Generator activity", e);
			}
		}
		
		String announcementFile = "file:" + audioFilePath;
		// Let us request for Announcement Complete event or Failure
		// in case if it happens
		MsRequestedEvent onCompleted = eventFactory.createRequestedEvent(MsAnnouncement.COMPLETED);
		onCompleted.setEventAction(MsEventAction.NOTIFY);

		MsRequestedEvent onFailed = eventFactory.createRequestedEvent(MsAnnouncement.FAILED);
		onFailed.setEventAction(MsEventAction.NOTIFY);
		
		MsPlayRequestedSignal play = (MsPlayRequestedSignal) eventFactory.createRequestedSignal(MsAnnouncement.PLAY);
		
		play.setURL(announcementFile);
		
		MsDtmfRequestedEvent dtmf = (MsDtmfRequestedEvent) eventFactory.createRequestedEvent(DTMF.TONE);
		
		MsRequestedSignal[] requestedSignals = new MsRequestedSignal[] { play };
        
        List<MsRequestedEvent> eventList = new ArrayList<MsRequestedEvent>();
        eventList.add(onFailed);
        if(listenForCompletion) {
        	eventList.add(onCompleted);
        }
        if(listenForDTMF) {
        	try {
	        	ActivityContextInterface dtmfAci = mediaAcif.getActivityContextInterface(link.getSession());
	        	dtmfAci.attach(getSbbContext().getSbbLocalObject());
	        	eventList.add(dtmf);
        	} catch (UnrecognizedActivityException e) {
    			logger.error("Internal Server Erro", e);
    		}
        }
        MsRequestedEvent[] requestedEvents = eventList.toArray(new MsRequestedEvent[eventList.size()]);
        
        link.getEndpoints()[1].execute(requestedSignals, requestedEvents, link);					
	}
	
	private MsConnection getConnection() {
		ActivityContextInterface[] activities = getSbbContext().getActivities();
		for (int i = 0; i < activities.length; i++) {
			if (activities[i].getActivity() instanceof MsConnection) {
				return (MsConnection) activities[i].getActivity();
			}
		}
		logger.info("Connection is null...");
		return null;
	}
	
	private MsLink getLink() {
		ActivityContextInterface[] activities = getSbbContext().getActivities();
		for (int i = 0; i < activities.length; i++) {
			if (activities[i].getActivity() instanceof MsLink) {
				return (MsLink) activities[i].getActivity();
			}
		}
		logger.info("Link is null...");
		return null;
	}
	
	public void onConnectionHalfOpened(MsConnectionEvent evt,
			ActivityContextInterface aci) {
		logger.info("Connection Created");
		logger.info("user name : " + this.getInteropCustomEvent().getBoothNumber());
		logger.info("initial SDP content : " + new String(this.getInteropCustomEvent().getSdpContent()));
		MsConnection connection = evt.getConnection();
		String sdp = connection.getLocalDescriptor();
		logger.info("connection SDP content : " + sdp);
		CallManager callManagerRef = (CallManager)this.getInteropCustomEvent().getCallManagerRef();
		try {
			callManagerRef.mediaConnectionCreated(sdp);
		} catch (IOException e) {
			logger.error("Impossible to call back the EJB", e);    
		}
	}
	
	public void onConnectionOpened(MsConnectionEvent evt,
			ActivityContextInterface aci) {
		logger.info("Connection Modified");
		logger.info("user name : " + this.getInteropCustomEvent().getBoothNumber());
		logger.info("initial SDP content : " + new String(this.getInteropCustomEvent().getSdpContent()));
		MsConnection connection = evt.getConnection();
		String sdp = connection.getLocalDescriptor();
		logger.info("connection SDP content : " + sdp);
		
		if(connection == null) {
			logger.error("Connection could not be created, closing the call ");
			CallManager callManagerRef = (CallManager)this.getInteropCustomEvent().getCallManagerRef();
			try {
				callManagerRef.endCall(null, false);
			} catch (IOException e) {
				logger.error("Impossible to call back the EJB", e);				
			}
			return;
		}
		MsSession session = connection.getSession();
		MsLink link = session.createLink(MsLinkMode.FULL_DUPLEX);

		ActivityContextInterface linkActivity = null;
		try {
			linkActivity = mediaAcif.getActivityContextInterface(link);
		} catch (UnrecognizedActivityException ex) {
		}

		linkActivity.attach(sbbContext.getSbbLocalObject());
		link.join(evt.getConnection().getEndpoint().getLocalName(), IVR_ENDPOINT_NAME);		
	}


	public void onAnnouncementComplete(MsNotifyEvent evt,
			ActivityContextInterface aci) {
		logger.info("Confirmation Announcement complete");
		CallManager callManagerRef = (CallManager)this.getInteropCustomEvent().getCallManagerRef();
		try {
			callManagerRef.endCall(null, false);
		} catch (IOException e) {
			logger.error("Impossible to call back the EJB", e);    
		}
		getConnection().release();
	}

	
	private void initDtmfDetector(MsLink link, String endpointName) {
		MsEventFactory eventFactory = msProvider
				.getEventFactory();
		try {
			ActivityContextInterface dtmfAci = mediaAcif
					.getActivityContextInterface(link.getSession());
			dtmfAci.attach(getSbbContext().getSbbLocalObject());
			
			MsDtmfRequestedEvent dtmf = (MsDtmfRequestedEvent) eventFactory.createRequestedEvent(DTMF.TONE);
			MsRequestedSignal[] signals = new MsRequestedSignal[] {};
			MsRequestedEvent[] events = new MsRequestedEvent[] { dtmf };

			link.getEndpoints()[1].execute(signals, events, link);
		} catch (UnrecognizedActivityException e) {
			logger.error("Internal Server Erro", e);
		}
	}
	
	public void onDtmf(MsNotifyEvent evt, ActivityContextInterface aci) {
		MsEventIdentifier identifier = evt.getEventID();
        if (identifier.equals(DTMF.TONE)) {
            MsDtmfNotifyEvent event = (MsDtmfNotifyEvent) evt;
            String signal = event.getSequence();
            logger.info("org.mobicents.slee.media.dtmf.DTMF " + signal);            			
		
			int cause = -1;
			
			try {
				cause = Integer.parseInt(signal);
			} catch (java.lang.NumberFormatException e) {
				//user entered a # sign 
				String announcement = generateConfirmationAnnouncement(getBoothNumber());
				playAnnouncement(announcement, true, false, true);
				return ;
			}				
			
			String boothNumber = getBoothNumber();
			if(boothNumber == null) {
				boothNumber = "";
			}
			
			switch (cause) {
				case 0:
					boothNumber = boothNumber + "0";
					break;
				case 1:
					boothNumber = boothNumber + "1";
					break;
				case 2:
					boothNumber = boothNumber + "2";
					break;
				case 3:
					boothNumber = boothNumber + "3";
					break;
				case 4:
					boothNumber = boothNumber + "4";
					break;
				case 5:
					boothNumber = boothNumber + "5";
					break;
				case 6:
					boothNumber = boothNumber + "6";
					break;
				case 7:
					boothNumber = boothNumber + "7";
					break;
				case 8:
					boothNumber = boothNumber + "8";
					break;
				case 9:
					boothNumber = boothNumber + "9";
					break;
				default:
					break;
			}
			
			setBoothNumber(boothNumber);
			
			this.initDtmfDetector(getLink(), IVR_ENDPOINT_NAME);
        }
	}

	public abstract void setInteropCustomEvent(InteropCustomEvent customEvent);

	public abstract InteropCustomEvent getInteropCustomEvent();
	
	public abstract void setBoothNumber(String boothNumber);

	public abstract String getBoothNumber();

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
