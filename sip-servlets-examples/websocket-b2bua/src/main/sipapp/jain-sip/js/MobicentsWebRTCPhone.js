/*
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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


function MobicentsWebRTCPhone(sipWsUrl) {
    console.debug("MobicentsWebRTCPhone:MobicentsWebRTCPhone(): sipWsUrl="+sipWsUrl);
    // SIP Stack config
    this.sipWsUrl=sipWsUrl;
    this.sipUserAgentName="MobicentsWebRTCPhone";
    this.init();
}

// SIP listener heritage 
MobicentsWebRTCPhone.prototype = new SipListener();
MobicentsWebRTCPhone.prototype.constructor=MobicentsWebRTCPhone;

//  State of REGISTER peerConnectionState machine
MobicentsWebRTCPhone.prototype.UNREGISTERED_STATE="UNREGISTERED_STATE";
MobicentsWebRTCPhone.prototype.REGISTERING_STATE="REGISTERING_STATE";
MobicentsWebRTCPhone.prototype.REGISTER_REFRESHING_STATE="REGISTER_REFRESHING_STATE";
MobicentsWebRTCPhone.prototype.REGISTERING_401_STATE="REGISTERING_401_STATE";
MobicentsWebRTCPhone.prototype.REGISTERED_STATE="REGISTERED_STATE";
MobicentsWebRTCPhone.prototype.UNREGISTERING_401_STATE="UNREGISTERING_401_STATE";
MobicentsWebRTCPhone.prototype.UNREGISTERING_STATE="UNREGISTERING_STATE";

//  State of outgoing call peerConnectionState machine
MobicentsWebRTCPhone.prototype.INVITING_INITIAL_STATE="INVITING_INITIAL_STATE";
MobicentsWebRTCPhone.prototype.INVITING_STATE="INVITING_STATE";
MobicentsWebRTCPhone.prototype.INVITING_407_STATE="INVITING_407_STATE";
MobicentsWebRTCPhone.prototype.INVITING_ACCEPTED_STATE="INVITING_ACCEPTED_STATE";
MobicentsWebRTCPhone.prototype.INVITING_LOCAL_HANGINGUP_STATE="INVITING_LOCAL_HANGINGUP_STATE";
MobicentsWebRTCPhone.prototype.INVITING_LOCAL_HANGINGUP_407_STATE="INVITING_LOCAL_HANGINGUP_407_STATE";

//  State of outgoing call peerConnectionState machine
MobicentsWebRTCPhone.prototype.INVITED_INITIAL_STATE="INVITED_INITIAL_STATE";
MobicentsWebRTCPhone.prototype.INVITED_ACCEPTED_STATE="INVITED_ACCEPTED_STATE";
MobicentsWebRTCPhone.prototype.INVITED_LOCAL_HANGINGUP_STATE="INVITED_LOCAL_HANGINGUP_STATE";
MobicentsWebRTCPhone.prototype.INVITED_LOCAL_HANGINGUP_407_STATE="INVITED_LOCAL_HANGINGUP_407_STATE";
MobicentsWebRTCPhone.prototype.INVITED_HANGUP_STATE="INVITED_HANGUP_STATE";

MobicentsWebRTCPhone.prototype.init =function(){
    console.debug ("MobicentsWebRTCPhone:init()");  
    
    this.initGUI();
    this.initSipAccount();
    this.initSipRegisterStateMachine();
    this.initSipInvitingStateMachine();
    this.initSipInvitedStateMachine();
    this.initPeerConnectionStateMachine();
    this.initJainSipStack(); 
}

MobicentsWebRTCPhone.prototype.initGUI=function(){
    console.debug ("MobicentsWebRTCPhone:initGUI()");  
    hideCallButton();
    hideUnRegisterButton();
    showRegisterButton();
    hideByeButton();
}

MobicentsWebRTCPhone.prototype.initSipAccount=function(){
    console.debug ("MobicentsWebRTCPhone:initSipAccount()");    
    // SIP account config        
    this.sipDomain=null;
    this.sipDisplayName=null;
    this.sipUserName=null;
    this.sipLogin=null;
    this.sipPassword=null;
}

MobicentsWebRTCPhone.prototype.initSipRegisterStateMachine=function(){
    console.debug ("MobicentsWebRTCPhone:initSipRegisterStateMachine()");  
    // SIP REGISTER machine 
    if(this.refreshRegisterTimer) clearTimeout(this.refreshRegisterTimer);
    this.registerState=this.UNREGISTERED_STATE;
    this.refreshRegisterTimer=null; 
    this.registerAuthenticatedFlag=false;
    this.refreshRegisterFlag=false;
    this.jainSipRegisterSentRequest=null;
    this.registeredFlag=false;
    this.unregisterPendingFlag=false;
    
}

MobicentsWebRTCPhone.prototype.initSipInvitingStateMachine=function(){
    console.debug ("MobicentsWebRTCPhone:initSipInvitingStateMachine()");  
    // SIP ougoing call (INVITING) state machine 
    this.callee=null;
    this.invitingState=this.INVITING_INITIAL_STATE;
    this.jainSipInvitingSentRequest=null;
    this.jainSipInvitingDialog=null;
    this.jainSipInvitingTransaction=null;
}

MobicentsWebRTCPhone.prototype.initSipInvitedStateMachine=function(){
    console.debug ("MobicentsWebRTCPhone:initSipInvitedStateMachine()");  
    // SIP ougoing call (INVITED) state machine 
    this.invitedState=this.INVITED_INITIAL_STATE;
    this.jainSipInvitedReceivedRequest=null;
    this.jainSipInvitedDialog=null;
    this.jainSipInvitedTransaction=null;
}
    
MobicentsWebRTCPhone.prototype.initJainSipStack=function(){
    console.debug ("MobicentsWebRTCPhone:initJainSipStack()");  
   
    // Create JAIN SIP main object
    this.sipFactory=new SipFactory();
    this.sipStack=this.sipFactory.createSipStack(this.sipWsUrl,this.sipUserAgentName);
    this.listeningPoint=this.sipStack.createListeningPoint();
    this.sipProvider=this.sipStack.createSipProvider(this.listeningPoint);
    this.sipProvider.addSipListener(this);
    this.headerFactory=this.sipFactory.createHeaderFactory();
    this.addressFactory=this.sipFactory.createAddressFactory();
    this.messageFactory=this.sipFactory.createMessageFactory(this.listeningPoint); 
    this.sipStack.start();
}
 
MobicentsWebRTCPhone.prototype.initPeerConnectionStateMachine=function(){
    console.debug ("MobicentsWebRTCPhone:initPeerConnectionStateMachine()");     
    
    // PeerConnection/Media call context
    var stunServer=document.getElementById("stunServer").value;
    if(stunServer!="")
    {
        this.peerConnectionStunServer = stunServer; 
    }
    if(this.peerConnection)
    {
        console.debug ("MobicentsWebRTCPhone:initPeerConnectionStateMachine(): force peerConnection close");
        document.getElementById("remoteVideo").pause();
        document.getElementById("remoteVideo").src= null;
        document.getElementById("remoteVideo").style.visibility = "hidden";
        this.peerConnection.close();
    }

    this.peerConnection = null;
    this.peerConnectionMessage=null;
    this.peerConnectionActionNeeded = false;
    this.peerConnectionState = 'new';
    this.peerConnectionIceStarted = false;
    this.peerConnectionMoreIceComing = true;
    this.peerConnectionIceCandidateCount = 0;
    this.remoteAudioVideoMediaStream=null;
    this.lastReceivedSdpOfferString=null;
}
  
  
//SIPListener interface implementation
MobicentsWebRTCPhone.prototype.processDialogTerminated =function(dialogTerminatedEvent){
    console.debug ("MobicentsWebRTCPhone:processDialogTerminated()");  
}

//SIPListener interface implementation
MobicentsWebRTCPhone.prototype.processIOException =function(exceptionEvent){
    console.error("MobicentsWebRTCPhone:processIOException()");   
}

//SIPListener interface implementation
MobicentsWebRTCPhone.prototype.processTimeout =function(timeoutEvent){
    console.debug("MobicentsWebRTCPhone:processTimeout()"); 
}

//SIPListener interface implementation
MobicentsWebRTCPhone.prototype.processTransactionTerminated =function(transactionTerminatedEvent){
    console.debug("MobicentsWebRTCPhone:processTransactionTerminated()"); 
}
//SIPListener interface implementation
MobicentsWebRTCPhone.prototype.processDisconnected =function(){   
    console.error("MobicentsWebRTCPhone:processDisconnected()"); 
    modal_alert("Disconnected with SIP server");

    show_desktop_notification("Disconnected with SIP server");
    this.initGUI();
}

//SIPListener interface implementation
MobicentsWebRTCPhone.prototype.processConnectionError =function(error){
    console.error("MobicentsWebRTCPhone:processConnectionError():error="+error); 
}

//SIPListener interface implementation
MobicentsWebRTCPhone.prototype.processConnected =function(){
    console.debug("MobicentsWebRTCPhone:processConnected()");
    this.register(
        document.getElementById("sipDomain").value,
        document.getElementById("sipDisplayName").value,
        document.getElementById("sipUserName").value,
        document.getElementById("sipLogin").value,
        document.getElementById("sipPassword").value);
}

//SIPListener interface implementation
MobicentsWebRTCPhone.prototype.processResponse =function(responseEvent){
    console.debug("MobicentsWebRTCPhone:processResponse()");  
    var jainSipResponse=responseEvent.getResponse(); 
    if(jainSipResponse.getCSeq().getMethod()=="REGISTER")
    {
        this.handleStateMachineRegisterResponseEvent(responseEvent);
    }
    else if(this.invitingState!=this.INVITING_INITIAL_STATE) this.handleStateMachineInvitingResponseEvent(responseEvent); 
    else if(this.invitedState!=this.INVITED_INITIAL_STATE)  this.handleStateMachineInvitedResponseEvent(responseEvent);
    else
    {
        console.debug("MobicentsWebRTCPhone:processResponse(): response ignored");      
    }
}

//SIPListener interface implementation
MobicentsWebRTCPhone.prototype.processRequest =function(requestEvent){
    console.debug("MobicentsWebRTCPhone:processRequest()");
    var jainSipRequest=requestEvent.getRequest(); 
    var jainSipRequestMethod=jainSipRequest.getMethod();   
    if((jainSipRequestMethod=="BYE")||(jainSipRequestMethod=="ACK")||(jainSipRequestMethod=="CANCEL"))
    {
        stopRinging();
        // Subscequent request on ongoing dialog
        if(this.invitingState!=this.INVITING_INITIAL_STATE) this.handleStateMachineInvitingRequestEvent(requestEvent); 
        else if(this.invitedState!=this.INVITED_INITIAL_STATE)  this.handleStateMachineInvitedRequestEvent(requestEvent);
        else
        {
            console.debug("MobicentsWebRTCPhone:processResponse(): request ignored");      
        }
    }
    else if(jainSipRequestMethod=="INVITE")
    {
        // Incoming call 
        if(this.invitingState!=this.INVITING_INITIAL_STATE)
        {
            // ONLY ONE CALL at the same time authorized 
            // Temporarily Unavailable
            var jainSipResponse480=jainSipRequest.createResponse(480,"Temporarily Unavailable");
            jainSipResponse480.addHeader(this.jainSipRegisterSentRequest.getHeader("User-Agent"));
            requestEvent.getServerTransaction().sendMessage(jainSipResponse480);
        }
        else if(this.invitedState!=this.INVITED_INITIAL_STATE)
        {
            // ONLY ONE CALL at the same time  authorized 
            // Temporarily Unavailable
            jainSipResponse480=jainSipRequest.createResponse(480,"Temporarily Unavailable");
            jainSipResponse480.addHeader(this.jainSipRegisterSentRequest.getHeader("User-Agent"));
            requestEvent.getServerTransaction().sendMessage(jainSipResponse480);
        }
        else
        {
            // Handle Incoming Call
            this.handleStateMachineInvitedRequestEvent(requestEvent);
        }
    }
    else
    {
        console.debug("MobicentsWebRTCPhone:processResponse(): request ignored");      
    }
}



MobicentsWebRTCPhone.prototype.register =function(sipDomain, sipDisplayName, sipUserName, sipLogin, sipPassword){
    console.debug("MobicentsWebRTCPhone:register(): sipDomain="+sipDomain);
    console.debug("MobicentsWebRTCPhone:register(): sipDisplayName="+sipDisplayName);
    console.debug("MobicentsWebRTCPhone:register(): sipUserName="+sipUserName);
    console.debug("MobicentsWebRTCPhone:register(): sipLogin="+sipLogin);
    console.debug("MobicentsWebRTCPhone:register(): sipPassword="+sipPassword);
    if(this.registerState==this.UNREGISTERED_STATE)
    {
        try
        {
            // Save SIP account profile
            this.sipDomain=sipDomain;
            this.sipDisplayName=sipDisplayName;
            this.sipUserName=sipUserName;
            this.sipLogin=sipLogin;
            this.sipPassword=sipPassword;
    
            this.jainSipContactHeader = this.listeningPoint.createContactHeader(sipUserName);
            this.jainSipUserAgentHeader = this.headerFactory.createUserAgentHeader(this.listeningPoint.getUserAgent());
            
            // Send SIP REGISTER request
            var fromSipUriString=this.sipUserName+"@"+this.sipDomain;            
            var jainSipCseqHeader=this.headerFactory.createCSeqHeader(1,"REGISTER");
            var jainSipCallIdHeader=this.headerFactory.createCallIdHeader();
            var jainSipExpiresHeader=this.headerFactory.createExpiresHeader(3600);
            var jainSipMaxForwardHeader=this.headerFactory.createMaxForwardsHeader(70);
            var jainSipRequestUri=this.addressFactory.createSipURI_user_host(null,this.sipDomain);
            var jainSipAllowListHeader=this.headerFactory.createHeaders("Allow: INVITE,UPDATE,ACK,CANCEL,BYE,NOTIFY,OPTIONS,MESSAGE,REFER");
            var jainSipFromUri=this.addressFactory.createSipURI_user_host(null,fromSipUriString);
            var jainSipFromAddress=this.addressFactory.createAddress_name_uri(null,jainSipFromUri);
            var random=new Date();
            var tag=random.getTime();
            var jainSipFromHeader=this.headerFactory.createFromHeader(jainSipFromAddress, tag);
            var jainSipToHeader=this.headerFactory.createToHeader(jainSipFromAddress, null);               
            var jainSipRegisterRequest=this.messageFactory.createRequest(jainSipRequestUri,"REGISTER",jainSipCallIdHeader,jainSipCseqHeader,jainSipFromHeader,jainSipToHeader,jainSipMaxForwardHeader);   
            this.messageFactory.addHeader(jainSipRegisterRequest, jainSipExpiresHeader);
            this.messageFactory.addHeader(jainSipRegisterRequest, this.jainSipUserAgentHeader);
            this.messageFactory.addHeader(jainSipRegisterRequest, jainSipAllowListHeader);
            this.messageFactory.addHeader(jainSipRegisterRequest, this.jainSipContactHeader); 
            this.jainSipRegisterSentRequest=jainSipRegisterRequest;
            var jainSipClientTransaction = this.sipProvider.getNewClientTransaction(jainSipRegisterRequest);
            jainSipRegisterRequest.setTransaction(jainSipClientTransaction);
            jainSipClientTransaction.sendRequest();
            this.registerState=this.REGISTERING_STATE;
        }
        catch(exception)
        {
            this.initGUI();
            this.initSipRegisterStateMachine();
            console.error("MobicentsWebRTCPhone:register(): catched exception:"+exception);
            modal_alert("MobicentsWebRTCPhone:register(): catched exception:"+exception);  
        }
    }
    else
    {
        modal_alert("MobicentsWebRTCPhone:register(): bad state, action register unauthorized");      
    }  
}


MobicentsWebRTCPhone.prototype.keepAliveRegister =function(){
    console.debug("MobicentsWebRTCPhone:keepAliveRegister()");
    
    if(this.registerState==this.REGISTERED_STATE)
    {
        this.refreshRegisterTimer=null;
        this.registerState=this.REGISTER_REFRESHING_STATE;
        var num=new Number(this.jainSipRegisterSentRequest.getCSeq().getSeqNumber());
        this.jainSipRegisterSentRequest.getCSeq().setSeqNumber(num+1);
        this.jainSipRegisterSentRequest = this.messageFactory.setNewViaHeader(this.jainSipRegisterSentRequest);
        var jainSipClientTransaction = this.sipProvider.getNewClientTransaction(this.jainSipRegisterSentRequest);
        this.jainSipRegisterSentRequest.setTransaction(jainSipClientTransaction);
        jainSipClientTransaction.sendRequest();
    }
    else
    {
        throw "MobicentsWebRTCPhone:keepAliveRegister(): bad state, action keep alive register unauthorized";            
    }
}

MobicentsWebRTCPhone.prototype.unRegister =function(){
    console.debug("MobicentsWebRTCPhone:unRegister()");
    if(this.registerState==this.REGISTERED_STATE)
    {
        this.registerState=this.UNREGISTERING_STATE;
        if(this.refreshRegisterTimer!=null)
        {
            // Cancel SIP REGISTER refresh timer
            clearTimeout(this.refreshRegisterTimer);
        }
        var num=new Number(this.jainSipRegisterSentRequest.getCSeq().getSeqNumber());
        this.jainSipRegisterSentRequest.getCSeq().setSeqNumber(num+1);
        this.jainSipRegisterSentRequest.getExpires().setExpires(0);
        this.jainSipRegisterSentRequest = this.jainSipRegisterSentRequest=this.messageFactory.setNewViaHeader(this.jainSipRegisterSentRequest); 
        var jainSipClientTransaction = this.sipProvider.getNewClientTransaction(this.jainSipRegisterSentRequest);
        this.jainSipRegisterSentRequest.setTransaction(jainSipClientTransaction);
        jainSipClientTransaction.sendRequest(); 
    }
    else if(this.registerState==this.UNREGISTERED_STATE)
    {
        console.warn("MobicentsWebRTCPhone:unRegister(): bad state, action keep alive register unauthorized");            
    }
    else
    {
        this.unregisterPendingFlag=true;     
    }
}

MobicentsWebRTCPhone.prototype.handleStateMachineRegisterResponseEvent =function(responseEvent){
    console.debug("MobicentsWebRTCPhone:handleStateMachineRegisterResponseEvent(): this.registerState="+this.registerState);
    var jainSipResponse=responseEvent.getResponse(); 
    var statusCode = parseInt(jainSipResponse.getStatusCode()); 
    if(this.registerState==this.UNREGISTERED_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineRegisterResponseEvent(): bad state, SIP response ignored");  
    }
    else if((this.registerState==this.REGISTERING_STATE) || (this.registerState==this.REGISTER_REFRESHING_STATE))
    {   
        if(statusCode< 200)
        {
            console.debug("MobicentsWebRTCPhone:handleStateMachineRegisterResponseEvent(): 1XX response ignored"); 
        }
        else if(statusCode==401)
        {
            this.registerState=this.REGISTERING_401_STATE;
            this.jainSipRegisterSentRequest.removeHeader("Authorization");
            var num=new Number(this.jainSipRegisterSentRequest.getCSeq().getSeqNumber());
            this.jainSipRegisterSentRequest.getCSeq().setSeqNumber(num+1);
            var jainSipAuthorizationHeader=this.headerFactory.createAuthorizationHeader(jainSipResponse,this.jainSipRegisterSentRequest,this.sipPassword,this.sipLogin);
            this.messageFactory.addHeader(this.jainSipRegisterSentRequest, jainSipAuthorizationHeader); 
            this.jainSipRegisterSentRequest = this.messageFactory.setNewViaHeader(this.jainSipRegisterSentRequest);
            var jainSipClientTransaction = this.sipProvider.getNewClientTransaction(this.jainSipRegisterSentRequest);
            this.jainSipRegisterSentRequest.setTransaction(jainSipClientTransaction);
            jainSipClientTransaction.sendRequest();
        }
        else if(statusCode==200)
        {
            this.registerState=this.REGISTERED_STATE;
            if(this.registeredFlag==false)
            {
                this.registeredFlag=true;
                showCallButton();
                showUnRegisterButton();
                hideRegisterButton();
                hideByeButton();		
            }
            
            if(this.unregisterPendingFlag==true) {
                this.unRegister();
                this.unregisterPendingFlag=false;
            }
            
            // Start SIP REGISTER refresh timeout
            var application=this;
            this.refreshRegisterTimer=setTimeout(function(){
                application.keepAliveRegister();
            },40000);
        }
        else
        {
            modal_alert("Registration Failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine().getReasonPhrase())    
        }
    }                     
    else if(this.registerState==this.REGISTERING_401_STATE)
    {
        if(statusCode < 200)
        {
            console.debug("MobicentsWebRTCPhone:handleStateMachineRegisterResponseEvent(): 1XX response ignored"); 
        }
        else if(statusCode==200)
        {
            this.registerState=this.REGISTERED_STATE; 
            if(this.registeredFlag==false)
            {
                console.debug("MobicentsWebRTCPhone:handleStateMachineRegisterResponseEvent(): this.registeredFlag=true"); 
                this.registeredFlag=true;
                showCallButton();
                showUnRegisterButton();
                hideRegisterButton();
                hideByeButton();
            }
                        
            if(this.unregisterPendingFlag==true) {
                this.unRegister();
                this.unregisterPendingFlag=false;
            }
            
            // Start SIP REGISTER refresh timeout
            var application=this;
            this.refreshRegisterTimer=setTimeout(function(){
                application.keepAliveRegister();
            },40000);
        }
        else
        {
            modal_alert("Registration failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine().getReasonPhrase());
            this.init();
        } 
    }
    else if(this.registerState==this.REGISTERED_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineRegisterResponseEvent(): bad state, SIP response ignored");   
    }
    else if(this.registerState==this.UNREGISTERING_STATE)
    {
        if(statusCode< 200)
        {
            console.debug("MobicentsWebRTCPhone:handleStateMachineRegisterResponseEvent(): 1XX response ignored"); 
        }
        else if(statusCode==401)
        {
            this.registerState=this.UNREGISTERING_401_STATE;
            this.jainSipRegisterSentRequest.removeHeader("Authorization");
            var num=new Number(this.jainSipRegisterSentRequest.getCSeq().getSeqNumber());
            this.jainSipRegisterSentRequest.getCSeq().setSeqNumber(num+1);
            var jainSipAuthorizationHeader=this.headerFactory.createAuthorizationHeader(jainSipResponse,this.jainSipRegisterSentRequest,this.sipPassword,this.sipLogin);
            this.messageFactory.addHeader(this.jainSipRegisterSentRequest, jainSipAuthorizationHeader); 
            this.jainSipRegisterSentRequest = this.messageFactory.setNewViaHeader(this.jainSipRegisterSentRequest);
            var jainSipClientTransaction = this.sipProvider.getNewClientTransaction(this.jainSipRegisterSentRequest);
            this.jainSipRegisterSentRequest.setTransaction(jainSipClientTransaction);
            jainSipClientTransaction.sendRequest();
        }
        else if(statusCode==200)
        {
            this.registerState=this.UNREGISTERED_STATE;
            if(this.registeredFlag==true)
            {
                console.debug("MobicentsWebRTCPhone:handleStateMachineRegisterResponseEvent(): this.registeredFlag=false"); 
                hideCallButton();
                hideUnRegisterButton();
                showRegisterButton();
                hideByeButton();
            }
        }
        else
        {
            modal_alert("UnRegistration Failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine().getReasonPhrase());  
            this.init();
        }
    }
    else if(this.registerState==this.UNREGISTERING_401_STATE)
    {
        if(statusCode< 200)
        {
            console.debug("MobicentsWebRTCPhone:handleStateMachineRegisterResponseEvent(): 1XX response ignored"); 
        }
        else if(statusCode==200)
        {
            this.registerState=this.UNREGISTERED_STATE;
            if(this.registeredFlag==true)
            {
                console.debug("MobicentsWebRTCPhone:handleStateMachineRegisterResponseEvent(): this.registeredFlag=false"); 
                this.registeredFlag=false;
                hideCallButton();
                hideUnRegisterButton();
                showRegisterButton();
                hideByeButton();
            }
        }
        else
        {
            modal_alert("UnRegistration Failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine().getReasonPhrase());
            this.init();
        }
    }
    else if(this.registerState==this.UNREGISTERED_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineRegisterResponseEvent(): bad state, SIP response ignored");  
    }
    else
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineRegisterResponseEvent(): bad state, SIP response ignored");    
    }
}


MobicentsWebRTCPhone.prototype.call =function(to){
    console.debug("MobicentsWebRTCPhone:call():to: "+to);
    if(this.registeredFlag==true)
    {
        if(this.invitingState==this.INVITING_INITIAL_STATE)
        {
            try
            {
                this.callee=to;
                hideCallButton();
                this.createPeerConnection();
                this.peerConnection.addStream(this.localAudioVideoMediaStream, {
                    has_audio: true, 
                    has_video: true
                });
                var application=this;
                this.peerConnection.createOffer(function(offer) {
                    application.onPeerConnectionCreateOfferSuccessCallback(offer);
                }, function(error) {
                    application.onPeerConnectionCreateOfferErrorCallback(error);
                });
            }
            catch(exception)
            {
                console.error("MobicentsWebRTCPhone:call(): catched exception:"+exception);
                modal_alert("MobicentsWebRTCPhone:call(): catched exception:"+exception);  
                this.initPeerConnectionStateMachine();
                this.initSipInvitingStateMachine();
                showCallButton(); 
                stopRinging();
            }
        }
        else
        {
            modal_alert("MobicentsWebRTCPhone:call(): bad state, action call unauthorized");    
        }
    }
    else
    {
        modal_alert("MobicentsWebRTCPhone:call(): unregistered, action call unauthorized");           
    }
}

MobicentsWebRTCPhone.prototype.sendInviteSipRequest =function(sdpOffer){
    console.debug("MobicentsWebRTCPhone:sendInviteSipRequest()"); 
    try{
        var fromSipUriString=this.sipUserName+"@"+this.sipDomain;
        var toSipUriString= this.callee;
        var random=new Date();       
        var jainSipCseqHeader=this.headerFactory.createCSeqHeader(1,"INVITE");
        var jainSipCallIdHeader=this.headerFactory.createCallIdHeader();
        var jainSipMaxForwardHeader=this.headerFactory.createMaxForwardsHeader(70);
        var jainSipRequestUri=this.addressFactory.createSipURI_user_host(null,toSipUriString);
        var jainSipAllowListHeader=this.headerFactory.createHeaders("Allow: INVITE,ACK,CANCEL,BYE");         
        var jainSipFromUri=this.addressFactory.createSipURI_user_host(null,fromSipUriString);
        var jainSipFromAdress=this.addressFactory.createAddress_name_uri(null,jainSipFromUri);
        var tagfrom=random.getTime();
        var jainSipFromHeader=this.headerFactory.createFromHeader(jainSipFromAdress, tagfrom);           
        var jainSiptoUri=this.addressFactory.createSipURI_user_host(null,toSipUriString);
        var jainSipToAddress=this.addressFactory.createAddress_name_uri(null,jainSiptoUri);
        var jainSipToHeader=this.headerFactory.createToHeader(jainSipToAddress, null);           
        var jainSipContentTypeHeader=this.headerFactory.createContentTypeHeader("application","sdp");
        this.jainSipInvitingSentRequest=this.messageFactory.createRequest(jainSipRequestUri,"INVITE",
            jainSipCallIdHeader,
            jainSipCseqHeader,
            jainSipFromHeader,
            jainSipToHeader,
            jainSipMaxForwardHeader,
            jainSipContentTypeHeader,
            sdpOffer); 
                      
        this.messageFactory.addHeader( this.jainSipInvitingSentRequest, this.jainSipUserAgentHeader);
        this.messageFactory.addHeader( this.jainSipInvitingSentRequest, jainSipAllowListHeader);
        this.messageFactory.addHeader( this.jainSipInvitingSentRequest, this.jainSipContactHeader);   
        this.invitingState=this.INVITING_STATE;
        this.jainSipInvitingTransaction = this.sipProvider.getNewClientTransaction(this.jainSipInvitingSentRequest);
        this.jainSipInvitingSentRequest.setTransaction(this.jainSipInvitingTransaction);
        this.jainSipInvitingTransaction.sendRequest();
    }
    catch(exception)
    {
        console.error("MobicentsWebRTCPhone:sendInviteSipRequest(): catched exception:"+exception);
        throw("MobicentsWebRTCPhone:sendInviteSipRequest(): catched exception:"+exception);  
        this.initPeerConnectionStateMachine();
        this.initSipInvitingStateMachine();
        showCallButton();   
    }
}

MobicentsWebRTCPhone.prototype.send200OKSipResponse =function(sdpOffer){
    console.debug("MobicentsWebRTCPhone:send200OKSipResponse()"); 
    try{
        this.invitedState=this.INVITED_ACCEPTED_STATE;
        var jainSip200OKResponse=this.jainSipInvitedReceivedRequest.createResponse(200, "OK");
        jainSip200OKResponse.addHeader(this.jainSipContactHeader);
        jainSip200OKResponse.addHeader(this.jainSipUserAgentHeader);
        jainSip200OKResponse.setMessageContent("application","sdp",sdpOffer);
        this.jainSipInvitedTransaction.sendResponse(jainSip200OKResponse);
        showByeButton();
        hideCallButton();
        hideUnRegisterButton();
    }
    catch(exception)
    {
        this.initPeerConnectionStateMachine();
        this.initSipInvitingStateMachine();
        showCallButton();
        console.error("MobicentsWebRTCPhone:send200OKSipResponse(): catched exception:"+exception);
        throw("MobicentsWebRTCPhone:send200OKSipResponse(): catched exception:"+exception);  
   
    }
}



MobicentsWebRTCPhone.prototype.bye =function(){
    console.debug("MobicentsWebRTCPhone:bye()");
   
    if(this.invitingState==this.INVITING_ACCEPTED_STATE)
    {
        try
        {
            var jainSipByeRequest=this.jainSipInvitingDialog.createRequest("BYE");
            jainSipByeRequest.addHeader(this.jainSipContactHeader);
            jainSipByeRequest.addHeader(this.jainSipUserAgentHeader);
            var clientTransaction  = this.sipProvider.getNewClientTransaction(jainSipByeRequest);
            this.jainSipInvitingDialog.sendRequest(clientTransaction);
            this.invitingState=this.INVITING_LOCAL_HANGINGUP_STATE;
        }
        catch(exception)
        {
            console.error("MobicentsWebRTCPhone:bye(): catched exception:"+exception);
            modal_alert("MobicentsWebRTCPhone:bye(): catched exception:"+exception); 
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();
            hideByeButton();
            showCallButton();
        }
    }
    else if(this.invitedState==this.INVITED_ACCEPTED_STATE)
    {
        try
        {
            var jainSipByeRequest=this.jainSipInvitedDialog.createRequest("BYE");
            jainSipByeRequest.addHeader(this.jainSipContactHeader);
            jainSipByeRequest.addHeader(this.jainSipUserAgentHeader);
            var clientTransaction  = this.sipProvider.getNewClientTransaction(jainSipByeRequest);
            this.jainSipInvitedDialog.sendRequest(clientTransaction);
            this.invitedState=this.INVITED_LOCAL_HANGINGUP_STATE;
        }
        catch(exception)
        {
            console.error("MobicentsWebRTCPhone:bye(): catched exception:"+exception);
            modal_alert("MobicentsWebRTCPhone:bye(): catched exception:"+exception); 
            this.initPeerConnectionStateMachine();
            this.initSipInvitedStateMachine();
            hideByeButton();
            showCallButton();
            showUnRegisterButton();
        }
    }
    else
    {
        modal_alert("MobicentsWebRTCPhone:bye(): bad state, action call unauthorized");     
    }
   
}

MobicentsWebRTCPhone.prototype.handleStateMachineInvitingResponseEvent =function(responseEvent){
    console.debug("MobicentsWebRTCPhone:handleStateMachineInvitingResponseEvent(): this.invitingState="+this.invitingState);
    var jainSipResponse=responseEvent.getResponse(); 
    var statusCode = parseInt(jainSipResponse.getStatusCode()); 
    if(this.invitingState==this.INVITING_STATE)
    {
        if(statusCode< 200)
        {
            startRinging();
            console.debug("MobicentsWebRTCPhone:handleStateMachineInvitingResponseEvent(): 1XX response ignored"); 
        }
        else if(statusCode==407)
        {
            this.invitingState=this.INVITING_407_STATE;
            var num=new Number(this.jainSipInvitingSentRequest.getCSeq().getSeqNumber());
            this.jainSipInvitingSentRequest.getCSeq().setSeqNumber(num+1);
            var jainSipAuthorizationHeader=this.headerFactory.createAuthorizationHeader(jainSipResponse,this.jainSipInvitingSentRequest,this.sipPassword,this.sipLogin);
            this.messageFactory.addHeader(this.jainSipInvitingSentRequest, jainSipAuthorizationHeader); 
            this.jainSipInvitingSentRequest = this.messageFactory.setNewViaHeader(this.jainSipInvitingSentRequest);
            var jainSipClientTransaction = this.sipProvider.getNewClientTransaction(this.jainSipInvitingSentRequest);
            this.jainSipInvitingSentRequest.setTransaction(jainSipClientTransaction);
            jainSipClientTransaction.sendRequest();
        }
        else if(statusCode==200)
        {
            stopRinging();
            this.jainSipInvitingDialog=responseEvent.getOriginalTransaction().getDialog();
            this.invitingState=this.INVITING_ACCEPTED_STATE;
            showByeButton();
            hideUnRegisterButton();
            this.jainSipInvitingDialog.setRemoteTarget(jainSipResponse.getHeader("Contact"));
            var jainSipMessageACK = responseEvent.getOriginalTransaction().createAck();
            this.jainSipInvitingDialog.sendAck(jainSipMessageACK);
            var sdpAnswerString = jainSipResponse.getContent();
            var sdpAnswer = new RTCSessionDescription({
                type: 'answer',
                sdp: sdpAnswerString
            });
            var application=this;
            this.peerConnectionState = 'answer-received';
            this.peerConnection.setRemoteDescription(sdpAnswer, function() {
                application.onPeerConnectionSetRemoteDescriptionSuccessCallback();
            }, function(error) {
                application.onPeerConnectionSetRemoteDescriptionErrorCallback(error);
            });  
        }
        else
        {
            modal_alert("Call Rejected :" + jainSipResponse.getStatusLine().getReasonPhrase()) 
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();
            showCallButton();
            hideByeButton();
            showUnRegisterButton();
            stopRinging();
        }     
    } 
    else if(this.invitingState==this.INVITING_407_STATE)
    {
        if(statusCode< 200)
        {
            console.debug("MobicentsWebRTCPhone:handleStateMachineInvitingResponseEvent(): 1XX response ignored"); 
        }
        else if(statusCode==200)
        {
            this.jainSipInvitingDialog=responseEvent.getOriginalTransaction().getDialog();
            this.invitingState=this.INVITING_ACCEPTED_STATE;
            showByeButton();
            hideUnRegisterButton();
            this.jainSipInvitingDialog.setRemoteTarget(jainSipResponse.getHeader("Contact"));
            var jainSipMessageACK = responseEvent.getOriginalTransaction().createAck();
            this.jainSipInvitingDialog.sendAck(jainSipMessageACK);
        }
        else
        {
            modal_alert("Call Failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine().getReasonPhrase());
            hideByeButton();
            showCallButton();
            showUnRegisterButton();
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();
        }    
    } 
    else if(this.invitingState==this.INVITING_FAILED_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineInvitingResponseEvent(): bad state, SIP response ignored");        
    } 
    else if(this.invitingState==this.INVITING_ACCEPTED_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineInvitingResponseEvent(): bad state, SIP response ignored");        
    } 
    else if(this.invitingState==this.INVITING_LOCAL_HANGINGUP_STATE)
    {
        if(statusCode==407)
        {
            this.invitingState=this.INVITING_HANGINGUP_407_STATE; 
            var jainSipByeRequest=this.jainSipInvitingDialog.createRequest("BYE");
            var clientTransaction  = this.sipProvider.getNewClientTransaction(jainSipByeRequest);
            var jainSipAuthorizationHeader=this.headerFactory.createAuthorizationHeader(jainSipResponse,jainSipByeRequest,this.sipPassword,this.sipLogin);
            this.messageFactory.addHeader(jainSipByeRequest, jainSipAuthorizationHeader); 
            this.jainSipInvitingDialog.sendRequest(clientTransaction);
        }
        else if(statusCode==200)
        {
            hideByeButton();
            showUnRegisterButton();
            showCallButton();
            document.getElementById("remoteVideo").pause();
            document.getElementById("remoteVideo").src= null;
            document.getElementById("remoteVideo").style.visibility = "hidden";
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();  
        }
        else
        {
            modal_alert("Call HangUp Failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine().getReasonPhrase());
            hideByeButton();
            showUnRegisterButton();
            showCallButton();
            document.getElementById("remoteVideo").pause();
            document.getElementById("remoteVideo").src= null;
            document.getElementById("remoteVideo").style.visibility = "hidden";
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();  
        }
    } 
    else if(this.invitingState==this.INVITING_LOCAL_HANGINGUP_407_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineInvitingResponseEvent(): bad state, SIP response ignored"); 
        if(statusCode==200)
        {
            hideByeButton();
            showUnRegisterButton();
            showCallButton();
            document.getElementById("remoteVideo").pause();
            document.getElementById("remoteVideo").src= null;
            document.getElementById("remoteVideo").style.visibility = "hidden";
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();  
        }
        else
        {
            modal_alert("Call HangUp Failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine().getReasonPhrase());
            hideByeButton();
            showUnRegisterButton();
            showCallButton();
            document.getElementById("remoteVideo").pause();
            document.getElementById("remoteVideo").src= null;
            document.getElementById("remoteVideo").style.visibility = "hidden";
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();  
        }        
    } 
}

MobicentsWebRTCPhone.prototype.handleStateMachineInvitingRequestEvent =function(requestEvent){
    console.debug("MobicentsWebRTCPhone:handleStateMachineInvitingRequestEvent(): this.invitingState="+this.invitingState);
    var jainSipRequest=requestEvent.getRequest();
    var requestMethod = jainSipRequest.getMethod();
    
    if(this.invitingState==this.INVITING_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineInvitingRequestEvent(): bad state, SIP request ignored");  
    } 
    else if(this.invitingState==this.INVITING_407_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineInvitingRequestEvent(): bad state, SIP request ignored");  
    } 
    else if(this.invitingState==this.INVITING_FAILED_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineInvitingRequestEvent(): bad state, SIP request ignored");        
    } 
    else if(this.invitingState==this.INVITING_ACCEPTED_STATE)
    {
        if(requestMethod=="BYE")  
        {
            var jainSip200OKResponse=jainSipRequest.createResponse(200, "OK");
            jainSip200OKResponse.addHeader(this.jainSipContactHeader);
            requestEvent.getServerTransaction().sendResponse(jainSip200OKResponse);
            hideByeButton();
            showUnRegisterButton();
            showCallButton();
            document.getElementById("remoteVideo").pause();
            document.getElementById("remoteVideo").src= null;
            document.getElementById("remoteVideo").style.visibility = "hidden";
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();  
            modal_alert("Contact has hangup"); 
        }
        else
        {
            console.error("MobicentsWebRTCPhone:handleStateMachineInvitingRequestEvent(): bad state, SIP request ignored"); 
        }
    } 
    else if(this.invitingState==this.INVITING_LOCAL_HANGINGUP_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineInvitingRequestEvent(): bad state, SIP request ignored");
    } 
    else if(this.invitingState==this.INVITING_LOCAL_HANGINGUP_407_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineInvitingRequestEvent(): bad state, SIP request ignored");        
    } 
}

MobicentsWebRTCPhone.prototype.handleStateMachineInvitedResponseEvent =function(responseEvent){
    console.debug("MobicentsWebRTCPhone:handleStateMachineInvitedResponseEvent(): this.invitingState="+this.invitingState);
    var jainSipResponse=responseEvent.getResponse(); 
    var statusCode = parseInt(jainSipResponse.getStatusCode()); 
    if(this.invitedState==this.INVITED_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineInvitedResponseEvent(): bad state, SIP response ignored");    
    } 
    else if(this.invitedState==this.INVITED_ACCEPTED_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineInvitedResponseEvent(): bad state, SIP response ignored");        
    } 
    else if(this.invitedState==this.INVITED_LOCAL_HANGINGUP_STATE)
    {
        if(statusCode==407)
        {
            this.invitedState=this.INVITED_HANGINGUP_407_STATE; 
            var jainSipByeRequest=this.jainSipInvitedDialog.createRequest("BYE");
            var clientTransaction  = this.sipProvider.getNewClientTransaction(jainSipByeRequest);
            var jainSipAuthorizationHeader=this.headerFactory.createAuthorizationHeader(jainSipResponse,jainSipByeRequest,this.sipPassword,this.sipLogin);
            this.messageFactory.addHeader(jainSipByeRequest, jainSipAuthorizationHeader); 
            this.jainSipInvitedDialog.sendRequest(clientTransaction);
        }
        else if(statusCode==200)
        {
            hideByeButton();
            showUnRegisterButton();
            showCallButton();
            this.initPeerConnectionStateMachine();
            this.initSipInvitedStateMachine();
        }
        else
        {
            modal_alert("Call HangUp Failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine().getReasonPhrase());
            hideByeButton();
            showUnRegisterButton();
            showCallButton();
            this.initPeerConnectionStateMachine();
            this.initSipInvitedStateMachine();
        }
    } 
    else if(this.invitedState==this.INVITED_LOCAL_HANGINGUP_407_STATE)
    {
        if(statusCode==200)
        {
            hideByeButton();
            showUnRegisterButton();
            showCallButton();
            this.initPeerConnectionStateMachine();
            this.initSipInvitedStateMachine();
        }
        else
        {
            modal_alert("Call HangUp Failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine().getReasonPhrase());
            hideByeButton();
            showUnRegisterButton();
            showCallButton();
            this.initPeerConnectionStateMachine();
            this.initSipInvitedStateMachine();
        }        
    } 
}

MobicentsWebRTCPhone.prototype.handleStateMachineInvitedRequestEvent =function(requestEvent){
    console.debug("MobicentsWebRTCPhone:handleStateMachineInvitedRequestEvent(): this.invitedState="+this.invitedState);
    var jainSipRequest=requestEvent.getRequest();
    var requestMethod = jainSipRequest.getMethod();
    var headerFrom = jainSipRequest.getHeader("From");
    if(this.invitedState==this.INVITED_INITIAL_STATE)
    {
        startRinging();
        var jainSip180ORingingResponse=jainSipRequest.createResponse(180, "Ringing");
        jainSip180ORingingResponse.addHeader(this.jainSipContactHeader);
        jainSip180ORingingResponse.addHeader(this.jainSipUserAgentHeader);
        requestEvent.getServerTransaction().sendResponse(jainSip180ORingingResponse);

        this.jainSipInvitedReceivedRequest=jainSipRequest;
        this.jainSipInvitedTransaction=requestEvent.getServerTransaction();
        this.jainSipInvitedDialog=requestEvent.getServerTransaction().getDialog();
            
        var sipUri = headerFrom.getAddress().getURI();
        console.debug("MobicentsWebRTCPhone:handleStateMachineInvitedRequestEvent(): sipUri.getUser()="+sipUri.getUser());
        show_desktop_notification("Incoming Call from " + sipUri.getUser());
        $("#call_message").html("<p>Incoming Call from " + sipUri.getUser() +"</p>");
        $('#callModal').modal(); 
    } 
    else if(this.invitedState==this.INVITED_ACCEPTED_STATE)
    {
        if(requestMethod=="BYE")  
        {
            showCallButton();
            hideByeButton();
            showUnRegisterButton(); 
            var jainSip200OKResponse=jainSipRequest.createResponse(200, "OK");
            jainSip200OKResponse.addHeader(this.jainSipContactHeader);
            jainSip200OKResponse.addHeader(this.jainSipUserAgentHeader);
            requestEvent.getServerTransaction().sendResponse(jainSip200OKResponse);
            this.invitedState=this.INVITED_INITIAL_STATE;
            this.jainSipInvitedReceivedRequest=null;
            this.jainSipInvitedDialog=null;
            document.getElementById("remoteVideo").pause();
            document.getElementById("remoteVideo").src= null;
            document.getElementById("remoteVideo").style.visibility = "hidden";
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();
            modal_alert("Contact has hangup"); 
        }
        else if(requestMethod=="ACK")  
        {         
            this.jainSipInvitedDialog=requestEvent.getServerTransaction().getDialog();
        }
        else {
            console.error("MobicentsWebRTCPhone:handleStateMachineInvitedRequestEvent(): bad state, SIP request ignored"); 
        }
    } 
    else if(this.invitedState==this.INVITED_LOCAL_HANGINGUP_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineInvitedRequestEvent(): bad state, SIP request ignored");
    } 
    else if(this.invitedState==this.INVITED_LOCAL_HANGINGUP_407_STATE)
    {
        console.error("MobicentsWebRTCPhone:handleStateMachineInvitedRequestEvent(): bad state, SIP request ignored");        
    } 
}


 
// RTCPeerConnection  state machine
 
MobicentsWebRTCPhone.prototype.createPeerConnection =function(){
    console.debug("MobicentsWebRTCPhone:createPeerConnection()");
    var application = this;
    this.peerConnection = new webkitRTCPeerConnection(null, null);	
		
    this.peerConnection.onaddstream = function(event) {
        application.onPeerConnectionOnAddStreamCallback(event);
    }  
	
    this.peerConnection.onremovestream = function(event) {
        application.onPeerConnectionOnRemoveStreamCallback(event);
    }   
    
    this.peerConnection.onopen= function(event) {
        application.onPeerConnectionOnOpenCallback(event);
    }
    
    this.peerConnection.onstatechange= function(event) {
        application.onPeerConnectionStateChangeCallback(event);
    }
    
    this.peerConnection.onicecandidate= function(rtcIceCandidateEvent) {
        application.onPeerConnectionIceCandidateCallback(rtcIceCandidateEvent);
    }
    
    this.peerConnection.onnegotationneeded= function(event) {
        application.onPeerConnectionIceNegotationNeededCallback(event);
    }
    
    this.peerConnection.ongatheringchange= function(event) {
        application.onPeerConnectionGatheringChangeCallback(event);
    }

    this.peerConnection.onicechange= function(event) {
        application.onPeerConnectionIceChangeCallback(event);
    } 
    
    this.peerConnection.onidentityresult= function(event) {
        application.onPeerConnectionIdentityResultCallback(event);
    }
}
 

  
MobicentsWebRTCPhone.prototype.onPeerConnectionOnAddStreamCallback =function(event){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionOnAddStreamCallback(): event="+event); 
    
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionOnAddStreamCallback():this.peerConnection.readyState="+this.peerConnection.readyState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionOnAddStreamCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionOnAddStreamCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionOnAddStreamCallback: this.peerConnectionState="+this.peerConnectionState);
    
        this.remoteAudioVideoMediaStream = event.stream;
        var url = webkitURL.createObjectURL(this.remoteAudioVideoMediaStream);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionOnAddStreamCallback():url="+url); 
        document.getElementById("remoteVideo").src= url;
        document.getElementById("remoteVideo").play();
        document.getElementById("remoteVideo").style.visibility = "visible"; 
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionOnAddStreamCallback(): this.peerConnection is null, bug in state machine!, bug in state machine!");        
    }
}

MobicentsWebRTCPhone.prototype.onPeerConnectionOnRemoveStreamCallback =function(event){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionOnRemoveStreamCallback(): event="+event);
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionOnRemoveStreamCallback(): this.peerConnection.readyState="+this.peerConnection.readyState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionOnRemoveStreamCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionOnRemoveStreamCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionOnRemoveStreamCallback: this.peerConnectionState="+this.peerConnectionState);
    
        this.remoteAudioVideoMediaStream = null;
        document.getElementById("remoteVideo").pause();
        document.getElementById("remoteVideo").src= null; 
        document.getElementById("remoteVideo").style.visibility = "hidden";
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionOnRemoveStreamCallback(): this.peerConnection is null, bug in state machine!");        
    }
}


MobicentsWebRTCPhone.prototype.onPeerConnectionOnOpenCallback =function(event){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionOnOpenCallback(): event="+event);
    
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionOnOpenCallback():this.peerConnection.readyState="+this.peerConnection.readyState);   
        console.debug("MobicentsWebRTCPhone:onPeerConnectionOnOpenCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionOnOpenCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionOnOpenCallback: this.peerConnectionState="+this.peerConnectionState);
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionOnOpenCallback(): this.peerConnection is null, bug in state machine!");        
    }
}
 
MobicentsWebRTCPhone.prototype.onPeerConnectionStateChangeCallback =function(event){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionStateChangeCallback(): event="+event); 
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionStateChangeCallback(): this.peerConnection.readyState="+this.peerConnection.readyState);   
        console.debug("MobicentsWebRTCPhone:onPeerConnectionStateChangeCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionStateChangeCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionStateChangeCallback: this.peerConnectionState="+this.peerConnectionState);
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionStateChangeCallback(): this.peerConnection is null, bug in state machine!");        
    }
}

MobicentsWebRTCPhone.prototype.onPeerConnectionIceCandidateCallback =function(rtcIceCandidateEvent){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionIceCandidateCallback(): rtcIceCandidateEvent="+rtcIceCandidateEvent); 
    console.debug("MobicentsWebRTCPhone:onPeerConnectionIceCandidateCallback(): this.peerConnection.readyState="+this.peerConnection.readyState);
    console.debug("MobicentsWebRTCPhone:onPeerConnectionIceCandidateCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
    console.debug("MobicentsWebRTCPhone:onPeerConnectionIceCandidateCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
    console.debug("MobicentsWebRTCPhone:onPeerConnectionIceCandidateCallback: this.peerConnectionState="+this.peerConnectionState);
    
    if(this.peerConnection!=null)
    {
        if(rtcIceCandidateEvent.candidate!=null)
        {
            console.debug("MobicentsWebRTCPhone:onPeerConnectionIceCandidateCallback: RTCIceCandidateEvent.candidate.candidate="+rtcIceCandidateEvent.candidate.candidate);
        }
        else
        {
            console.debug("MobicentsWebRTCPhone:onPeerConnectionIceCandidateCallback: no anymore ICE candidate");
            if(this.peerConnectionState == 'preparing-offer') 
            {
                // Send INVITE
                this.sendInviteSipRequest(this.peerConnection.localDescription.sdp);
                this.peerConnectionState = 'offer-sent';
            } 
            else if (this.peerConnectionState == 'preparing-answer') 
            {
                // Send 200 OK
                this.send200OKSipResponse(this.peerConnection.localDescription.sdp)
                this.peerConnectionState = 'established';
            }
            else if (this.peerConnectionState == 'established') 
            {
            // Why this last ice candidate event
            } 
            else
            {
                console.log("MobicentsWebRTCPhone:onPeerConnectionIceCandidateCallback(): RTCPeerConnection bad state!");
            }
        }
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionIceCandidateCallback(): this.peerConnection is null, bug in state machine!");        
    }
}


MobicentsWebRTCPhone.prototype.onPeerConnectionCreateOfferSuccessCallback =function(offer){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateOfferSuccessCallback(): newOffer="+offer); 
    console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateOfferSuccessCallback(): this.peerConnection.readyState="+this.peerConnection.readyState);
    console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateOfferSuccessCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
    console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateOfferSuccessCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
    console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateOfferSuccessCallback: this.peerConnectionState="+this.peerConnectionState);
 
    if(this.peerConnection!=null)
    {
        if (this.peerConnectionState == 'new') 
        {
            // Preparing offer.
            var application=this;
            this.peerConnectionState = 'preparing-offer';
            this.peerConnection.setLocalDescription(offer, function() {
                application.onPeerConnectionSetLocalDescriptionSuccessCallback();
            }, function(error) {
                application.onPeerConnectionSetLocalDescriptionErrorCallback(error);
            });
        } 
        else
        {
            console.error("MobicentsWebRTCPhone:onPeerConnectionCreateOfferSuccessCallback(): RTCPeerConnection bad state!");
        }
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionCreateOfferSuccessCallback(): this.peerConnection is null, bug in state machine!");        
    }
}


MobicentsWebRTCPhone.prototype.onPeerConnectionCreateOfferErrorCallback =function(error){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateOfferErrorCallback():error="+error); 
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateOfferErrorCallback(): this.peerConnection.readyState="+this.peerConnection.readyState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateOfferErrorCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateOfferErrorCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateOfferErrorCallback: this.peerConnectionState="+this.peerConnectionState);
    // TODO Notify Error to INVITE state machine
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionCreateOfferErrorCallback(): this.peerConnection is null, bug in state machine!");        
    }
    alert("error:"+error);

}

MobicentsWebRTCPhone.prototype.onPeerConnectionSetLocalDescriptionSuccessCallback =function(){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionSetLocalDescriptionSuccessCallback()"); 
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetLocalDescriptionSuccessCallback(): this.peerConnection.readyState="+this.peerConnection.readyState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetLocalDescriptionSuccessCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetLocalDescriptionSuccessCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetLocalDescriptionSuccessCallback: this.peerConnectionState="+this.peerConnectionState);
    // Nothing to do, just waiting end ICE resolution
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionCreateOfferErrorCallback(): this.peerConnection is null, bug in state machine!");        
    } 
}

MobicentsWebRTCPhone.prototype.onPeerConnectionSetLocalDescriptionErrorCallback =function(error){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionSetLocalDescriptionErrorCallback():error="+error); 
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetLocalDescriptionErrorCallback(): this.peerConnection.readyState="+this.peerConnection.readyState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetLocalDescriptionErrorCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetLocalDescriptionErrorCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetLocalDescriptionErrorCallback: this.peerConnectionState="+this.peerConnectionState);
    // TODO Notify Error to INVITE state machine
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionSetLocalDescriptionErrorCallback(): this.peerConnection is null, bug in state machine!");        
    } 
    alert("error:"+error);

}

MobicentsWebRTCPhone.prototype.onPeerConnectionCreateAnswerSuccessCallback =function(answer){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateAnswerSuccessCallback():answer="+answer);
    
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateAnswerSuccessCallback(): this.peerConnection.readyState="+this.peerConnection.readyState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateAnswerSuccessCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateAnswerSuccessCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateAnswerSuccessCallback: this.peerConnectionState="+this.peerConnectionState);

        if (this.peerConnectionState == 'offer-received') 
        {
            // Prepare answer.
            var application=this;
            this.peerConnectionState = 'preparing-answer';
            this.peerConnection.setLocalDescription(answer, function() {
                application.onPeerConnectionSetLocalDescriptionSuccessCallback();
            }, function(error) {
                application.onPeerConnectionSetLocalDescriptionErrorCallback(error);
            });
        } 
        else
        {
            console.log("MobicentsWebRTCPhone:onPeerConnectionCreateAnswerSuccessCallback(): RTCPeerConnection bad state!");
        }
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionCreateAnswerSuccessCallback(): this.peerConnection is null, bug in state machine!");        
    } 
}


MobicentsWebRTCPhone.prototype.onPeerConnectionCreateAnswerErrorCallback =function(error){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateAnswerErrorCallback():error="+error);
    
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateAnswerErrorCallback(): this.peerConnection.readyState="+this.peerConnection.readyState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateAnswerErrorCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateAnswerErrorCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionCreateAnswerErrorCallback: this.peerConnectionState="+this.peerConnectionState);
    // TODO Notify Error to INVITE state machin
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionCreateAnswerErrorCallback(): this.peerConnection is null, bug in state machine!");        
    } 
    alert("error:"+error);
}


MobicentsWebRTCPhone.prototype.onPeerConnectionSetRemoteDescriptionSuccessCallback =function(){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionSetRemoteDescriptionSuccessCallback()");
    
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetRemoteDescriptionSuccessCallback(): this.peerConnection.readyState="+this.peerConnection.readyState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetRemoteDescriptionSuccessCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetRemoteDescriptionSuccessCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetRemoteDescriptionSuccessCallback: this.peerConnectionState="+this.peerConnectionState);

        if (this.peerConnectionState == 'answer-received') 
        {            
            this.peerConnectionState = 'established';
        }
        else if (this.peerConnectionState == 'offer-received') 
        {            
            var application=this;
            this.peerConnection.createAnswer(function(answer) {
                application.onPeerConnectionCreateAnswerSuccessCallback(answer);
            }, function(error) {
                application.onPeerConnectionCreateAnswerErrorCallback(error);
            }); 
        }
        else {
            console.log("MobicentsWebRTCPhone:onPeerConnectionSetRemoteDescriptionSuccessCallback(): RTCPeerConnection bad state!");
        }
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionSetRemoteDescriptionSuccessCallback(): this.peerConnection is null, bug in state machine!");        
    } 
}


MobicentsWebRTCPhone.prototype.onPeerConnectionSetRemoteDescriptionErrorCallback =function(error){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionSetRemoteDescriptionErrorCallback():error="+error);
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetRemoteDescriptionErrorCallback(): this.peerConnection.readyState="+this.peerConnection.readyState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetRemoteDescriptionErrorCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetRemoteDescriptionErrorCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionSetRemoteDescriptionErrorCallback: this.peerConnectionState="+this.peerConnectionState);
    // TODO Notify Error to INVITE state machine
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionSetRemoteDescriptionErrorCallback(): this.peerConnection is null, bug in state machine!");        
    } 
    alert("error:"+error);

}


MobicentsWebRTCPhone.prototype.onPeerConnectionIceNegotationNeededCallback =function(event){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionIceNegotationNeededCallback():event="+event);
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionIceNegotationNeededCallback(): this.peerConnection.readyState="+this.peerConnection.readyState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionIceNegotationNeededCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionIceNegotationNeededCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionIceNegotationNeededCallback: this.peerConnectionState="+this.peerConnectionState);
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionIceNegotationNeededCallback(): this.peerConnection is null, bug in state machine!");        
    } 
}

MobicentsWebRTCPhone.prototype.onPeerConnectionGatheringChangeCallback =function(event){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionGatheringChangeCallback():event="+event);
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionGatheringChangeCallback(): this.peerConnection.readyState="+this.peerConnection.readyState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionGatheringChangeCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionGatheringChangeCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionGatheringChangeCallback: this.peerConnectionState="+this.peerConnectionState);
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionGatheringChangeCallback(): this.peerConnection is null, bug in state machine!");        
    } 
}

MobicentsWebRTCPhone.prototype.onPeerConnectionIceChangeCallback =function(event){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionIceChangeCallback():event="+event);
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionIceChangeCallback(): this.peerConnection.readyState="+this.peerConnection.readyState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionIceChangeCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionIceChangeCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionIceChangeCallback: this.peerConnectionState="+this.peerConnectionState);
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionIceChangeCallback(): this.peerConnection is null, bug in state machine!");        
    } 
}

MobicentsWebRTCPhone.prototype.onPeerConnectionIdentityResultCallback =function(event){
    console.debug("MobicentsWebRTCPhone:onPeerConnectionIdentityResultCallback():event="+event);
    if(this.peerConnection!=null)
    {
        console.debug("MobicentsWebRTCPhone:onPeerConnectionIdentityResultCallback(): this.peerConnection.readyState="+this.peerConnection.readyState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionIdentityResultCallback(): this.peerConnection.iceGatheringState="+this.peerConnection.iceGatheringState);
        console.debug("MobicentsWebRTCPhone:onPeerConnectionIdentityResultCallback(): this.peerConnection.iceState="+this.peerConnection.iceState); 
        console.debug("MobicentsWebRTCPhone:onPeerConnectionIdentityResultCallback: this.peerConnectionState="+this.peerConnectionState);
    }
    else 
    {
        console.warn("SimpleWebRtcSipPhone:onPeerConnectionIdentityResultCallback(): this.peerConnection is null, bug in state machine!");        
    } 
}
 

// Accept modal 
$('#callModal .accept-btn').click(function() {
    try
    {
        stopRinging();
        mobicentsWebRTCPhone.createPeerConnection();
        mobicentsWebRTCPhone.peerConnection.addStream(mobicentsWebRTCPhone.localAudioVideoMediaStream, {
            has_audio: true, 
            has_video: true
        });
        mobicentsWebRTCPhone.lastReceivedSdpOfferString = mobicentsWebRTCPhone.jainSipInvitedReceivedRequest.getContent();
        var sdpOffer = new RTCSessionDescription({
            type: 'offer',
            sdp: mobicentsWebRTCPhone.lastReceivedSdpOfferString
        });
        var application=mobicentsWebRTCPhone;
        mobicentsWebRTCPhone.peerConnectionState = 'offer-received';
        mobicentsWebRTCPhone.peerConnection.setRemoteDescription(sdpOffer, function() {
            application.onPeerConnectionSetRemoteDescriptionSuccessCallback();
        }, function(error) {
            application.onPeerConnectionSetRemoteDescriptionErrorCallback(error);
        });
    }
    catch(exception)
    {
        // Temporarily Unavailable
        var jainSipResponse480=mobicentsWebRTCPhone.jainSipInvitedReceivedRequest.createResponse(480,"Temporarily Unavailable");
        jainSipResponse480.addHeader(mobicentsWebRTCPhone.jainSipContactHeader);
        jainSipResponse480.addHeader(mobicentsWebRTCPhone.jainSipUserAgentHeader);
        mobicentsWebRTCPhone.jainSipInvitedTransaction.sendResponse(jainSipResponse480);
        hideByeButton();
        showCallButton();
        showUnRegisterButton(); 
        stopRinging();
        mobicentsWebRTCPhone.initPeerConnectionStateMachine();
        mobicentsWebRTCPhone.initSipInvitedStateMachine();
        console.error("MobicentsWebRTCPhone:handleStateMachineInvitedRequestEvent(): catched exception:"+exception);
    }

});

// Reject modal
$('#callModal .reject-btn').click(function() {
    // Rejected 
    var jainSipResponse603=mobicentsWebRTCPhone.jainSipInvitedReceivedRequest.createResponse(603,"Declined");
    jainSipResponse603.addHeader(mobicentsWebRTCPhone.jainSipContactHeader);
    jainSipResponse603.addHeader(mobicentsWebRTCPhone.jainSipUserAgentHeader);
    mobicentsWebRTCPhone.jainSipInvitedTransaction.sendResponse(jainSipResponse603);
    hideByeButton();
    showCallButton();
    showUnRegisterButton();
    stopRinging();
    mobicentsWebRTCPhone.initPeerConnectionStateMachine();
    mobicentsWebRTCPhone.initSipInvitedStateMachine();
});

function modal_alert(message) {
    $("#modal_message").html(message);
    $('#messageModal').modal(); 
}

function show_desktop_notification(message) {
    if (window.webkitNotifications && window.webkitNotifications.checkPermission() == 0) {
        var thumb = "img/TeleStax-logo-globe-only-transparent-bg.png";
        var title = "Mobicents HTML5 WebRTC Client";
        var popup = window.webkitNotifications.createNotification(thumb, title, message);
        //Show the popup
        popup.show();
	setTimeout(function(){
	        popup.cancel();
        }, '10000');
    }
}
