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


function ApplicationSipTest(sipWsUrl) {
    console.debug("ApplicationSipTest:ApplicationSipTest(): sipWsUrl="+sipWsUrl);
    // SIP Stack config
    this.sipWsUrl=sipWsUrl;
    this.sipUserAgentName="WebRTC JAIN SIP JS UserAgent";
    this.init();
}

// SIP listener heritage 
ApplicationSipTest.prototype = new SipListener();
ApplicationSipTest.prototype.constructor=ApplicationSipTest;

//  State of REGISTER peerConnectionState machine
ApplicationSipTest.prototype.UNREGISTERED_STATE="UNREGISTERED_STATE";
ApplicationSipTest.prototype.REGISTERING_STATE="REGISTERING_STATE";
ApplicationSipTest.prototype.REGISTER_REFRESHING_STATE="REGISTER_REFRESHING_STATE";
ApplicationSipTest.prototype.REGISTERING_401_STATE="REGISTERING_401_STATE";
ApplicationSipTest.prototype.REGISTERED_STATE="REGISTERED_STATE";
ApplicationSipTest.prototype.UNREGISTERING_401_STATE="UNREGISTERING_401_STATE";
ApplicationSipTest.prototype.UNREGISTERING_STATE="UNREGISTERING_STATE";

//  State of outgoing call peerConnectionState machine
ApplicationSipTest.prototype.INVITING_INITIAL_STATE="INVITING_INITIAL_STATE";
ApplicationSipTest.prototype.INVITING_STATE="INVITING_STATE";
ApplicationSipTest.prototype.INVITING_407_STATE="INVITING_407_STATE";
ApplicationSipTest.prototype.INVITING_ACCEPTED_STATE="INVITING_ACCEPTED_STATE";
ApplicationSipTest.prototype.INVITING_LOCAL_HANGINGUP_STATE="INVITING_LOCAL_HANGINGUP_STATE";
ApplicationSipTest.prototype.INVITING_LOCAL_HANGINGUP_407_STATE="INVITING_LOCAL_HANGINGUP_407_STATE";

//  State of outgoing call peerConnectionState machine
ApplicationSipTest.prototype.INVITED_INITIAL_STATE="INVITED_INITIAL_STATE";
ApplicationSipTest.prototype.INVITED_ACCEPTED_STATE="INVITED_ACCEPTED_STATE";
ApplicationSipTest.prototype.INVITED_LOCAL_HANGINGUP_STATE="INVITED_LOCAL_HANGINGUP_STATE";
ApplicationSipTest.prototype.INVITED_LOCAL_HANGINGUP_407_STATE="INVITED_LOCAL_HANGINGUP_407_STATE";
ApplicationSipTest.prototype.INVITED_HANGUP_STATE="INVITED_HANGUP_STATE";

ApplicationSipTest.prototype.init =function(){
    console.debug ("ApplicationSipTest:init()");  
    
    this.initGUI();
    this.initSipAccount();
    this.initSipRegisterStateMachine();
    this.initSipInvitingStateMachine();
    this.initSipInvitedStateMachine();
    this.initPeerConnectionStateMachine();
    this.initJainSipStack(); 
}

ApplicationSipTest.prototype.initGUI=function(){
    console.debug ("ApplicationSipTest:initGUI()");  
    hideCallButton();
    hideUnRegisterButton();
    showRegisterButton();
    hideByeButton();
}

ApplicationSipTest.prototype.initSipAccount=function(){
    console.debug ("ApplicationSipTest:initSipAccount()");    
    // SIP account config        
    this.sipDomain=null;
    this.sipDisplayName=null;
    this.sipUserName=null;
    this.sipLogin=null;
    this.sipPassword=null;
}

ApplicationSipTest.prototype.initSipRegisterStateMachine=function(){
    console.debug ("ApplicationSipTest:initSipRegisterStateMachine()");  
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

ApplicationSipTest.prototype.initSipInvitingStateMachine=function(){
    console.debug ("ApplicationSipTest:initSipInvitingStateMachine()");  
    // SIP ougoing call (INVITING) state machine 
    this.callee=null;
    this.invitingState=this.INVITING_INITIAL_STATE;
    this.jainSipInvitingSentRequest=null;
    this.jainSipInvitingDialog=null;
    this.jainSipInvitingTransaction=null;
}

ApplicationSipTest.prototype.initSipInvitedStateMachine=function(){
    console.debug ("ApplicationSipTest:initSipInvitedStateMachine()");  
    // SIP ougoing call (INVITED) state machine 
    this.invitedState=this.INVITED_INITIAL_STATE;
    this.jainSipInvitedReceivedRequest=null;
    this.jainSipInvitedDialog=null;
    this.jainSipInvitedTransaction=null;
}
    
ApplicationSipTest.prototype.initJainSipStack=function(){
    console.debug ("ApplicationSipTest:initJainSipStack()");  
   
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
 
ApplicationSipTest.prototype.initPeerConnectionStateMachine=function(){
    console.debug ("ApplicationSipTest:initPeerConnectionStateMachine()");     
    
    // PeerConnection/Media call context
    this.jsepPeerConnectionConstructor = webkitPeerConnection00;
    this.sessionDescriptionConstructor = SessionDescription;
    var stunServer=document.getElementById("stunServer").value;
    if(stunServer!="")
    {
        this.peerConnectionStunServer = stunServer; 
    }
    if(this.peerConnection)
    {
        console.debug ("ApplicationSipTest:initPeerConnectionStateMachine(): force peerConnection close");
        document.getElementById("remoteVideo").pause();
        document.getElementById("remoteVideo").src= null;
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
ApplicationSipTest.prototype.processDialogTerminated =function(dialogTerminatedEvent){
    console.debug ("ApplicationSipTest:processDialogTerminated()");  
}

//SIPListener interface implementation
ApplicationSipTest.prototype.processIOException =function(exceptionEvent){
    console.error("ApplicationSipTest:processIOException()");   
}

//SIPListener interface implementation
ApplicationSipTest.prototype.processTimeout =function(timeoutEvent){
    console.debug("ApplicationSipTest:processTimeout()"); 
}

//SIPListener interface implementation
ApplicationSipTest.prototype.processTransactionTerminated =function(transactionTerminatedEvent){
    console.debug("ApplicationSipTest:processTransactionTerminated()"); 
}
//SIPListener interface implementation
ApplicationSipTest.prototype.processDisconnected =function(){   
    console.error("ApplicationSipTest:processDisconnected()"); 
    alert("disconnected with SIP server");
    this.initGUI();
}

//SIPListener interface implementation
ApplicationSipTest.prototype.processConnectionError =function(error){
    console.error("ApplicationSipTest:processConnectionError():error="+error); 
}

//SIPListener interface implementation
ApplicationSipTest.prototype.processConnected =function(){
    console.debug("ApplicationSipTest:processConnected()");
    this.register(
        document.getElementById("sipDomain").value,
        document.getElementById("sipDisplayName").value,
        document.getElementById("sipUserName").value,
        document.getElementById("sipLogin").value,
        document.getElementById("sipPassword").value);
}

//SIPListener interface implementation
ApplicationSipTest.prototype.processResponse =function(responseEvent){
    console.debug("ApplicationSipTest:processResponse()");  
    var jainSipResponse=responseEvent.getResponse(); 
    if(jainSipResponse.getCSeq().getMethod()=="REGISTER")
    {
        this.handleStateMachineRegisterResponseEvent(responseEvent);
    }
    else if(this.invitingState!=this.INVITING_INITIAL_STATE) this.handleStateMachineInvitingResponseEvent(responseEvent); 
    else if(this.invitedState!=this.INVITED_INITIAL_STATE)  this.handleStateMachineInvitedResponseEvent(responseEvent);
    else
    {
        console.debug("ApplicationSipTest:processResponse(): response ignored");      
    }
}

//SIPListener interface implementation
ApplicationSipTest.prototype.processRequest =function(requestEvent){
    console.debug("ApplicationSipTest:processRequest()");
    var jainSipRequest=requestEvent.getRequest(); 
    var jainSipRequestMethod=jainSipRequest.getMethod();   
    if((jainSipRequestMethod=="BYE")||(jainSipRequestMethod=="ACK")||(jainSipRequestMethod=="CANCEL"))
    {
        // Subscequent request on ongoing dialog
        if(this.invitingState!=this.INVITING_INITIAL_STATE) this.handleStateMachineInvitingRequestEvent(requestEvent); 
        else if(this.invitedState!=this.INVITED_INITIAL_STATE)  this.handleStateMachineInvitedRequestEvent(requestEvent);
        else
        {
            console.debug("ApplicationSipTest:processResponse(): request ignored");      
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
        console.debug("ApplicationSipTest:processResponse(): request ignored");      
    }
}



ApplicationSipTest.prototype.register =function(sipDomain, sipDisplayName, sipUserName, sipLogin, sipPassword){
    console.debug("ApplicationSipTest:register(): sipDomain="+sipDomain);
    console.debug("ApplicationSipTest:register(): sipDisplayName="+sipDisplayName);
    console.debug("ApplicationSipTest:register(): sipUserName="+sipUserName);
    console.debug("ApplicationSipTest:register(): sipLogin="+sipLogin);
    console.debug("ApplicationSipTest:register(): sipPassword="+sipPassword);
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
            var contactUriString=this.sipUserName+"@"+this.localIpAddress;             
            var jainSipCseqHeader=this.headerFactory.createCSeqHeader(1,"REGISTER");
            var jainSipCallIdHeader=this.headerFactory.createCallIdHeader();
            var jainSipExpiresHeader=this.headerFactory.createExpiresHeader(3600);
            var jainSipMaxForwardHeader=this.headerFactory.createMaxForwardsHeader(70);
            var jainSipRequestUri=this.addressFactory.createSipURI_user_host(null,this.sipDomain);
            var jainSipAllowListHeader=this.headerFactory.createHeaders("Allow: INVITE,ACK,CANCEL,OPTION,BYE,REFER,NOTIFY,MESSAGE,SUBSCRIBE,INFO");
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
            console.error("ApplicationSipTest:register(): catched exception:"+exception);
            alert("ApplicationSipTest:register(): catched exception:"+exception);  
        }
    }
    else
    {
        alert("ApplicationSipTest:register(): bad state, action register unauthorized");      
    }  
}


ApplicationSipTest.prototype.keepAliveRegister =function(){
    console.debug("ApplicationSipTest:keepAliveRegister()");
    
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
        throw "ApplicationSipTest:keepAliveRegister(): bad state, action keep alive register unauthorized";            
    }
}

ApplicationSipTest.prototype.unRegister =function(){
    console.debug("ApplicationSipTest:unRegister()");
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
        throw "ApplicationSipTest:unRegister(): bad state, action keep alive register unauthorized";            
    }
    else
    {
        this.unregisterPendingFlag=true;     
    }
}

ApplicationSipTest.prototype.handleStateMachineRegisterResponseEvent =function(responseEvent){
    console.debug("ApplicationSipTest:handleStateMachineRegisterResponseEvent(): this.registerState="+this.registerState);
    var jainSipResponse=responseEvent.getResponse(); 
    var statusCode = parseInt(jainSipResponse.getStatusCode()); 
    if(this.registerState==this.UNREGISTERED_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineRegisterResponseEvent(): bad state, SIP response ignored");  
    }
    else if((this.registerState==this.REGISTERING_STATE) || (this.registerState==this.REGISTER_REFRESHING_STATE))
    {   
        if(statusCode< 200)
        {
            console.debug("ApplicationSipTest:handleStateMachineRegisterResponseEvent(): 1XX response ignored"); 
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
            alert("SIP registration failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine())    
        }
    }                     
    else if(this.registerState==this.REGISTERING_401_STATE)
    {
        if(statusCode < 200)
        {
            console.debug("ApplicationSipTest:handleStateMachineRegisterResponseEvent(): 1XX response ignored"); 
        }
        else if(statusCode==200)
        {
            this.registerState=this.REGISTERED_STATE; 
            if(this.registeredFlag==false)
            {
                console.debug("ApplicationSipTest:handleStateMachineRegisterResponseEvent(): this.registeredFlag=true"); 
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
            alert("SIP registration failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine());
            this.init();
        } 
    }
    else if(this.registerState==this.REGISTERED_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineRegisterResponseEvent(): bad state, SIP response ignored");   
    }
    else if(this.registerState==this.UNREGISTERING_STATE)
    {
        if(statusCode< 200)
        {
            console.debug("ApplicationSipTest:handleStateMachineRegisterResponseEvent(): 1XX response ignored"); 
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
                console.debug("ApplicationSipTest:handleStateMachineRegisterResponseEvent(): this.registeredFlag=false"); 
                hideCallButton();
                hideUnRegisterButton();
                showRegisterButton();
                hideByeButton();
            }
        }
        else
        {
            alert("SIP unregistration failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine());  
            this.init();
        }
    }
    else if(this.registerState==this.UNREGISTERING_401_STATE)
    {
        if(statusCode< 200)
        {
            console.debug("ApplicationSipTest:handleStateMachineRegisterResponseEvent(): 1XX response ignored"); 
        }
        else if(statusCode==200)
        {
            this.registerState=this.UNREGISTERED_STATE;
            if(this.registeredFlag==true)
            {
                console.debug("ApplicationSipTest:handleStateMachineRegisterResponseEvent(): this.registeredFlag=false"); 
                this.registeredFlag=false;
                hideCallButton();
                hideUnRegisterButton();
                showRegisterButton();
                hideByeButton();
            }
        }
        else
        {
            alert("SIP unregistration failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine());
            this.init();
        }
    }
    else if(this.registerState==this.UNREGISTERED_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineRegisterResponseEvent(): bad state, SIP response ignored");  
    }
    else
    {
        console.error("ApplicationSipTest:handleStateMachineRegisterResponseEvent(): bad state, SIP response ignored");    
    }
}


ApplicationSipTest.prototype.call =function(to){
    console.debug("ApplicationSipTest:call():to: "+to);
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
                this.peerConnectionMarkActionNeeded(); 
            }
            catch(exception)
            {
                console.error("ApplicationSipTest:call(): catched exception:"+exception);
                alert("ApplicationSipTest:call(): catched exception:"+exception);  
                this.initPeerConnectionStateMachine();
                this.initSipInvitingStateMachine();
                showCallButton(); 
            }
        }
        else
        {
            alert("ApplicationSipTest:call(): bad state, action call unauthorized");    
        }
    }
    else
    {
        alert("ApplicationSipTest:call(): unregistered, action call unauthorized");           
    }
}


ApplicationSipTest.prototype.sendInviteSipRequest =function(sdpOffer){
    console.debug("ApplicationSipTest:sendInviteSipRequest()"); 
    try{
        var fromSipUriString=this.sipUserName+"@"+this.sipDomain;
        var toSipUriString= this.callee+"@"+this.sipDomain;
        var random=new Date();       
        var jainSipCseqHeader=this.headerFactory.createCSeqHeader(1,"INVITE");
        var jainSipCallIdHeader=this.headerFactory.createCallIdHeader();
        var jainSipMaxForwardHeader=this.headerFactory.createMaxForwardsHeader(70);
        var jainSipRequestUri=this.addressFactory.createSipURI_user_host(null,toSipUriString);
        var jainSipAllowListHeader=this.headerFactory.createHeaders("Allow: INVITE,ACK,CANCEL,OPTION,BYE"+",REFER,NOTIFY,MESSAGE,SUBSCRIBE,INFO");         
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
        console.error("ApplicationSipTest:sendInviteSipRequest(): catched exception:"+exception);
        throw("ApplicationSipTest:sendInviteSipRequest(): catched exception:"+exception);  
        this.initPeerConnectionStateMachine();
        this.initSipInvitingStateMachine();
        showCallButton();   
    }
}

ApplicationSipTest.prototype.send200OKSipResponse =function(sdpOffer){
    console.debug("ApplicationSipTest:send200OKSipResponse()"); 
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
        console.error("ApplicationSipTest:send200OKSipResponse(): catched exception:"+exception);
        throw("ApplicationSipTest:send200OKSipResponse(): catched exception:"+exception);  
   
    }
}



ApplicationSipTest.prototype.bye =function(){
    console.debug("ApplicationSipTest:bye()");
   
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
            console.error("ApplicationSipTest:bye(): catched exception:"+exception);
            alert("ApplicationSipTest:bye(): catched exception:"+exception); 
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
            console.error("ApplicationSipTest:bye(): catched exception:"+exception);
            alert("ApplicationSipTest:bye(): catched exception:"+exception); 
            this.initPeerConnectionStateMachine();
            this.initSipInvitedStateMachine();
            hideByeButton();
            showCallButton();
            showUnRegisterButton();
        }
    }
    else
    {
        alert("ApplicationSipTest:bye(): bad state, action call unauthorized");     
    }
   
}

ApplicationSipTest.prototype.handleStateMachineInvitingResponseEvent =function(responseEvent){
    console.debug("ApplicationSipTest:handleStateMachineInvitingResponseEvent(): this.invitingState="+this.invitingState);
    var jainSipResponse=responseEvent.getResponse(); 
    var statusCode = parseInt(jainSipResponse.getStatusCode()); 
    if(this.invitingState==this.INVITING_STATE)
    {
        if(statusCode< 200)
        {
            console.debug("ApplicationSipTest:handleStateMachineInvitingResponseEvent(): 1XX response ignored"); 
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
            this.jainSipInvitingDialog=responseEvent.getOriginalTransaction().getDialog();
            this.invitingState=this.INVITING_ACCEPTED_STATE;
            showByeButton();
            hideUnRegisterButton();
            this.jainSipInvitingDialog.setRemoteTarget(jainSipResponse.getHeader("Contact"));
            var jainSipMessageACK = responseEvent.getOriginalTransaction().createAck();
            this.jainSipInvitingDialog.sendAck(jainSipMessageACK);
            var sdpAnswerString = jainSipResponse.getContent();
            this.peerConnection.setRemoteDescription(this.peerConnection.SDP_ANSWER,new this.sessionDescriptionConstructor(sdpAnswerString));
            console.log("ApplicationSipTest:peerConnectionOnStableState(): sdpAnswerString="+sdpAnswerString);
            this.peerConnectionState = 'established';  
        }
        else
        {
            alert("SIP INVITE failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine()) 
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();
            showCallButton();
            hideByeButton();
            showUnRegisterButton();

        }     
    } 
    else if(this.invitingState==this.INVITING_407_STATE)
    {
        if(statusCode< 200)
        {
            console.debug("ApplicationSipTest:handleStateMachineInvitingResponseEvent(): 1XX response ignored"); 
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
            alert("SIP INVITE failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine());
            hideByeButton();
            showCallButton();
            showUnRegisterButton();
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();
        }    
    } 
    else if(this.invitingState==this.INVITING_FAILED_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineInvitingResponseEvent(): bad state, SIP response ignored");        
    } 
    else if(this.invitingState==this.INVITING_ACCEPTED_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineInvitingResponseEvent(): bad state, SIP response ignored");        
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
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();  
        }
        else
        {
            alert("SIP BYE failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine());
            hideByeButton();
            showUnRegisterButton();
            showCallButton();
            document.getElementById("remoteVideo").pause();
            document.getElementById("remoteVideo").src= null;
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();  
        }
    } 
    else if(this.invitingState==this.INVITING_LOCAL_HANGINGUP_407_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineInvitingResponseEvent(): bad state, SIP response ignored"); 
        if(statusCode==200)
        {
            hideByeButton();
            showUnRegisterButton();
            showCallButton();
            document.getElementById("remoteVideo").pause();
            document.getElementById("remoteVideo").src= null;
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();  
        }
        else
        {
            alert("SIP BYE failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine());
            hideByeButton();
            showUnRegisterButton();
            showCallButton();
            document.getElementById("remoteVideo").pause();
            document.getElementById("remoteVideo").src= null;
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();  
        }        
    } 
}

ApplicationSipTest.prototype.handleStateMachineInvitingRequestEvent =function(requestEvent){
    console.debug("ApplicationSipTest:handleStateMachineInvitingRequestEvent(): this.invitingState="+this.invitingState);
    var jainSipRequest=requestEvent.getRequest();
    var requestMethod = jainSipRequest.getMethod();
    
    if(this.invitingState==this.INVITING_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineInvitingRequestEvent(): bad state, SIP request ignored");  
    } 
    else if(this.invitingState==this.INVITING_407_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineInvitingRequestEvent(): bad state, SIP request ignored");  
    } 
    else if(this.invitingState==this.INVITING_FAILED_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineInvitingRequestEvent(): bad state, SIP request ignored");        
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
            this.initPeerConnectionStateMachine();
            this.initSipInvitingStateMachine();  
            alert("Contact has hangup"); 
        }
        else
        {
            console.error("ApplicationSipTest:handleStateMachineInvitingRequestEvent(): bad state, SIP request ignored"); 
        }
    } 
    else if(this.invitingState==this.INVITING_LOCAL_HANGINGUP_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineInvitingRequestEvent(): bad state, SIP request ignored");
    } 
    else if(this.invitingState==this.INVITING_LOCAL_HANGINGUP_407_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineInvitingRequestEvent(): bad state, SIP request ignored");        
    } 
}

ApplicationSipTest.prototype.handleStateMachineInvitedResponseEvent =function(responseEvent){
    console.debug("ApplicationSipTest:handleStateMachineInvitedResponseEvent(): this.invitingState="+this.invitingState);
    var jainSipResponse=responseEvent.getResponse(); 
    var statusCode = parseInt(jainSipResponse.getStatusCode()); 
    if(this.invitedState==this.INVITED_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineInvitedResponseEvent(): bad state, SIP response ignored");    
    } 
    else if(this.invitedState==this.INVITED_ACCEPTED_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineInvitedResponseEvent(): bad state, SIP response ignored");        
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
            alert("SIP BYE failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine());
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
            alert("SIP BYE failed:" + jainSipResponse.getStatusCode()+ "  "+ jainSipResponse.getStatusLine());
            hideByeButton();
            showUnRegisterButton();
            showCallButton();
            this.initPeerConnectionStateMachine();
            this.initSipInvitedStateMachine();
        }        
    } 
}

ApplicationSipTest.prototype.handleStateMachineInvitedRequestEvent =function(requestEvent){
    console.debug("ApplicationSipTest:handleStateMachineInvitedRequestEvent(): this.invitedState="+this.invitedState);
    var jainSipRequest=requestEvent.getRequest();
    var requestMethod = jainSipRequest.getMethod();
    var headerFrom = jainSipRequest.getHeader("From");
    if(this.invitedState==this.INVITED_INITIAL_STATE)
    {
        var jainSip180ORingingResponse=jainSipRequest.createResponse(180, "Ringing");
        jainSip180ORingingResponse.addHeader(this.jainSipContactHeader);
        jainSip180ORingingResponse.addHeader(this.jainSipUserAgentHeader);
        requestEvent.getServerTransaction().sendResponse(jainSip180ORingingResponse);
            
        var sipUri = headerFrom.getAddress().getURI();
        console.debug("ApplicationSipTest:handleStateMachineInvitedRequestEvent(): sipUri.getUser()="+sipUri.getUser());
        var result = confirm("Call from "+sipUri.getUser()+ ": Accept or Reject");
        if (result==true)
        {
            // Accepted 
            try
            {
                this.jainSipInvitedReceivedRequest=jainSipRequest;
                this.jainSipInvitedTransaction=requestEvent.getServerTransaction();
                this.jainSipInvitedDialog=requestEvent.getServerTransaction().getDialog();
                this.createPeerConnection();
                this.peerConnection.addStream(this.localAudioVideoMediaStream, {
                    has_audio: true, 
                    has_video: true
                });
                this.lastReceivedSdpOfferString = jainSipRequest.getContent();
                var sdpOffer = new this.sessionDescriptionConstructor(this.lastReceivedSdpOfferString);
                this.peerConnection.setRemoteDescription(this.peerConnection.SDP_OFFER,	sdpOffer);
                this.peerConnectionState = 'offer-received';
                this.peerConnectionMarkActionNeeded();
            }
            catch(exception)
            {
                // Temporarily Unavailable
                var jainSipResponse480=jainSipRequest.createResponse(480,"Temporarily Unavailable");
                jainSipResponse480.addHeader(this.jainSipContactHeader);
                jainSipResponse480.addHeader(this.jainSipUserAgentHeader);
                requestEvent.getServerTransaction().sendResponse(jainSipResponse480);
                hideByeButton();
                showCallButton();
                showUnRegisterButton(); 
                this.initPeerConnectionStateMachine();
                this.initSipInvitedStateMachine();
                console.error("ApplicationSipTest:handleStateMachineInvitedRequestEvent(): catched exception:"+exception);
                alert("ApplicationSipTest:handleStateMachineInvitedRequestEvent(): catched exception:"+exception);  
            }
        }
        else
        {
            // Rejected 
            // Temporarily Unavailable
            var jainSipResponse480=jainSipRequest.createResponse(480,"Temporarily Unavailable");
            jainSipResponse480.addHeader(this.jainSipContactHeader);
            jainSipResponse480.addHeader(this.jainSipUserAgentHeader);
            requestEvent.getServerTransaction().sendResponse(jainSipResponse480);
            hideByeButton();
            showCallButton();
            showUnRegisterButton();
            this.initPeerConnectionStateMachine();
            this.initSipInvitedStateMachine();
        }  
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
            this.peerConnection.close();
            this.peerConnection=null;
            alert("Contact has hangup"); 
        }
        else if(requestMethod=="ACK")  
        {         
            this.jainSipInvitedDialog=requestEvent.getServerTransaction().getDialog();
        }
        else {
            console.error("ApplicationSipTest:handleStateMachineInvitedRequestEvent(): bad state, SIP request ignored"); 
        }
    } 
    else if(this.invitedState==this.INVITED_LOCAL_HANGINGUP_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineInvitedRequestEvent(): bad state, SIP request ignored");
    } 
    else if(this.invitedState==this.INVITED_LOCAL_HANGINGUP_407_STATE)
    {
        console.error("ApplicationSipTest:handleStateMachineInvitedRequestEvent(): bad state, SIP request ignored");        
    } 
}
    
ApplicationSipTest.prototype.createPeerConnection =function(){
    console.debug("ApplicationSipTest:createPeerConnection()");
    var that = this;
    this.peerConnection = new this.jsepPeerConnectionConstructor(
        this.peerConnectionStunServer,
        function(candidate, more) 
        {
            console.debug("ApplicationSipTest:createPeerConnection():candidate="+candidate+" more="+more);
            if (more == false) 
            {
                // At the moment, we do not renegotiate when new candidates
                // show up after the more flag has been false once.
                that.peerConnectionMoreIceComing = false;
                that.peerConnectionMarkActionNeeded();
            }
            that.peerConnectionIceCandidateCount += 1;
        }
        );	
		
    this.peerConnection.onaddstream = function(event) 
    {
        console.debug("ApplicationSipTest:createPeerConnection():onaddstream()");
        that.remoteAudioVideoMediaStream = event.stream;
        var url = webkitURL.createObjectURL(that.remoteAudioVideoMediaStream);
        document.getElementById("remoteVideo").src= url;
        document.getElementById("remoteVideo").play();
    }
		
    this.peerConnection.onremovestream = function(event) 
    {
        console.debug("ApplicationSipTest:createPeerConnection():onremovestream()");
        that.remoteAudioVideoMediaStream = null;
        document.getElementById("remoteVideo").pause();
        document.getElementById("remoteVideo").src= null;

    }
            
    this.peerConnectionMarkActionNeeded();
}
    
ApplicationSipTest.prototype.peerConnectionMarkActionNeeded =function(){
    console.debug("ApplicationSipTest:peerConnectionMarkActionNeeded()");
    this.peerConnectionActionNeeded = true;
    var that = this; // make this available in closure.
    // Post an event to myself so that I get called a while later.
    // (needs more JS/DOM info. Just call the processing function on a delay
    // for now.)
    window.setTimeout(function() {
        that.peerConnectionOnStableState();
    },1);
}

// Internal function for the Connection: Called when a stable peerConnectionState
// is entered by the browser (to allow for multiple AddStream calls or
// other interesting actions).
// This function will generate an offer or answer, as needed, and send
// to the remote party using our onsignalingmessage function.
ApplicationSipTest.prototype.peerConnectionOnStableState =function(timeoutEvent){
    console.debug("ApplicationSipTest:peerConnectionOnStableState(): peerConnectionState="+this.peerConnectionState); 
    console.debug("ApplicationSipTest:peerConnectionOnStableState(): this.peerConnectionActionNeeded="+this.peerConnectionActionNeeded); 
    var mySDP;
    if (this.peerConnectionActionNeeded) 
    {
        if (this.peerConnectionState == 'new' || this.peerConnectionState == 'established') 
        {
            // See if the current offer is the same as what we already sent.
            // If not, no change is needed.
            var newOffer = this.peerConnection.createOffer({
                has_audio:true, 
                has_video:true
            });
				
            // CLM allowing only sending sdp in 'new' peerConnectionState ==> to avoid sending the same SDP we already sent (chrome bug??),
            // as the GW does not support this yet
            if (newOffer.toSdp() != this.peerConnectionLastSdpOffer && this.peerConnectionState == 'new') 
            {
                // Prepare to send an offer.
                this.peerConnection.setLocalDescription(this.peerConnection.SDP_OFFER,newOffer);
                console.log("ApplicationSipTest:peerConnectionOnStableState(): newOffer="+newOffer);
                this.peerConnection.startIce();
                this.peerConnectionState = 'preparing-offer';
                this.peerConnectionMarkActionNeeded();
                return;
            } 
            else
                console.log("ApplicationSipTest:peerConnectionOnStableState(): Not sending a new offer");
        } 
        else if (this.peerConnectionState == 'preparing-offer') 
        {
            // Don't do anything until we have the ICE candidates.
            if (this.peerConnectionMoreIceComing)  return;		
            // Now able to send the offer we've already prepared.
            this.peerConnectionLastSdpOffer = this.peerConnection.localDescription.toSdp();
            this.sendInviteSipRequest(this.peerConnectionLastSdpOffer);
            // Not done: Retransmission on non-response.
            this.peerConnectionState = 'offer-sent';
        } 
        else if (this.peerConnectionState == 'offer-received') 
        {
            mySDP = this.peerConnection.createAnswer(this.lastReceivedSdpOfferString , {
                has_audio:true, 
                has_video:true
            });
            this.peerConnection.setLocalDescription(this.peerConnection.SDP_ANSWER,	mySDP);
            this.peerConnectionState = 'offer-received-preparing-answer';
            if (!this.peerConnectionIceStarted) 
            {
                var now = new Date();
                console.log("ApplicationSipTest:peerConnectionOnStableState():"+ now.getTime() + ": Starting ICE in responder");
                this.peerConnection.startIce();
                this.peerConnectionIceStarted = true;
            } 
            else 
            {
                this.peerConnectionMarkActionNeeded();
                return;
            }
        } 
        else if (this.peerConnectionState == 'offer-received-preparing-answer') 
        {
            if (this.peerConnectionMoreIceComing) 
                return;
            mySDP = this.peerConnection.localDescription;
            this.send200OKSipResponse(mySDP.toSdp())
            this.peerConnectionState = 'established';
        } 
        else 
            console.error("ApplicationSipTest:peerConnectionOnStableState(): dazed and confused in state " + this.peerConnectionState +", stopping here");	
            
        this.peerConnectionActionNeeded = false;
    }
}
