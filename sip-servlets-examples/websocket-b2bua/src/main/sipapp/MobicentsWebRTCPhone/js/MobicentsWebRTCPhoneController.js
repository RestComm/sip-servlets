/**
 * Class MobicentsWebRTCPhoneController
 * @public 
 */ 

/**
 * Constructor 
 */ 
function MobicentsWebRTCPhoneController(view) {
    console.debug("MobicentsWebRTCPhoneController:MobicentsWebRTCPhoneController()")
    //  WebRtcComm client 
    this.view=view;
    this.webRtcCommClient=new WebRtcCommClient(this); 
    this.webRtcCommClientConfiguration=undefined;
    this.localAudioVideoMediaStream=undefined;
    this.webRtcCommCall=undefined;
    this.sipContactUri=MobicentsWebRTCPhoneController.prototype.DEFAULT_SIP_CONTACT;
}

MobicentsWebRTCPhoneController.prototype.constructor=MobicentsWebRTCPhoneController;

// Default SIP profile to use
MobicentsWebRTCPhoneController.prototype.DEFAULT_SIP_OUTBOUND_PROXY="ws://" + window.location.hostname + ":5082";
MobicentsWebRTCPhoneController.prototype.DEFAULT_SIP_USER_AGENT="MobicentsWebRTCPhone/0.0.1" 
MobicentsWebRTCPhoneController.prototype.DEFAULT_SIP_USER_AGENT_CAPABILITIES=undefined 
MobicentsWebRTCPhoneController.prototype.DEFAULT_SIP_DOMAIN="webrtc.mobicents.org";
MobicentsWebRTCPhoneController.prototype.DEFAULT_SIP_DISPLAY_NAME="alice";
MobicentsWebRTCPhoneController.prototype.DEFAULT_SIP_USER_NAME="alice";
MobicentsWebRTCPhoneController.prototype.DEFAULT_SIP_LOGIN=undefined;
MobicentsWebRTCPhoneController.prototype.DEFAULT_SIP_PASSWORD=undefined;
MobicentsWebRTCPhoneController.prototype.DEFAULT_SIP_CONTACT="bob";
MobicentsWebRTCPhoneController.prototype.DEFAULT_SIP_REGISTER_MODE=true;
MobicentsWebRTCPhoneController.prototype.DEFAULT_STUN_SERVER="undefined"; // stun.l.google.com:19302

/**
 * on load event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onLoadViewEventHandler=function() 
{
    console.debug ("MobicentsWebRTCPhoneController:onLoadViewEventHandler()");
        
    // Setup SIP default Profile
    this.webRtcCommClientConfiguration =  { 
        communicationMode:WebRtcCommClient.prototype.SIP,
        sip:{
            sipUserAgent:this.DEFAULT_SIP_USER_AGENT,
            sipOutboundProxy:this.DEFAULT_SIP_OUTBOUND_PROXY,
            sipDomain:this.DEFAULT_SIP_DOMAIN,
            sipDisplayName:this.DEFAULT_SIP_DISPLAY_NAME,
            sipUserName:this.DEFAULT_SIP_USER_NAME,
            sipLogin:this.DEFAULT_SIP_LOGIN,
            sipPassword:this.DEFAULT_SIP_PASSWORD,
            sipUserAgentCapabilities:this.DEFAULT_SIP_USER_AGENT_CAPABILITIES,
            sipRegisterMode:this.DEFAULT_SIP_REGISTER_MODE
        },
        RTCPeerConnection:
        {
            stunServer:this.DEFAULT_STUN_SERVER         
        }
    } 
    
    // Setup SIP overloaded profile configuration in request URL       
    if(this.view.location.search.length>0)
    {
        var argumentsString = this.view.location.search.substring(1);
        var arguments = argumentsString.split('&');
        if(arguments.length==0) arguments = [argumentsString];
        for(var i=0;i<arguments.length;i++)
        {   
            var argument = arguments[i].split("=");
            if("sipUserName"==argument[0])
            {
                this.webRtcCommClientConfiguration.sip.sipUserName =argument[1];
                if(this.webRtcCommClientConfiguration.sip.sipUserName=="") this.webRtcCommClientConfiguration.sip.sipUserName=undefined;
            } 
            else if("sipDomain"==argument[0])
            {
                this.webRtcCommClientConfiguration.sip.sipDomain =argument[1];
                if(this.webRtcCommClientConfiguration.sip.sipDomain=="") this.webRtcCommClientConfiguration.sip.sipDomain=undefined;
            } 
            else if("sipDisplayedName"==argument[0])
            {
                this.webRtcCommClientConfiguration.sip.sipDisplayName =argument[1];
                if(this.webRtcCommClientConfiguration.sip.sipDisplayName=="") this.webRtcCommClientConfiguration.sip.sipDisplayedName=undefined;
            } 
            else if("sipPassword"==argument[0])
            {
                this.webRtcCommClientConfiguration.sip.sipPassword =argument[1];
                if(this.webRtcCommClientConfiguration.sip.sipPassword=="") this.webRtcCommClientConfiguration.sip.sipPassword=undefined;
            } 
            else if("sipLogin"==argument[0])
            {
                this.webRtcCommClientConfiguration.sip.sipLogin =argument[1];
                if(this.webRtcCommClientConfiguration.sip.sipLogin=="") this.webRtcCommClientConfiguration.sip.sipLogin=undefined;
            }
            else if("sipContactUri"==argument[0])
            {
                this.sipContactUri =argument[1];
            }
        }
    }  
    this.initView();   
}


/**
 * on unload event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onUnloadViewEventHandler=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onBeforeUnloadEventHandler()"); 
    if(this.webRtcCommClient != undefined)
    {
        try
        {
            this.webRtcCommClient.close();  
        }
        catch(exception)
        {
             console.error("WebRtcCommTestWebAppController:onUnloadViewEventHandler(): catched exception:"+exception);  
        }
    }    
}


MobicentsWebRTCPhoneController.prototype.initView=function(){
    console.debug ("MobicentsWebRTCPhoneController:initView()");  
    this.view.disableCallButton();
    this.view.disableEndCallButton();
    this.view.disableDisconnectButton();
    this.view.disableConnectButton();
    this.view.stopLocalVideo();
    this.view.hideLocalVideo();
    this.view.stopRemoteVideo();
    this.view.hideRemoteVideo();
    this.view.setStunServerTextInputValue(this.webRtcCommClientConfiguration.RTCPeerConnection.stunServer);
    this.view.setSipOutboundProxyTextInputValue(this.webRtcCommClientConfiguration.sip.sipOutboundProxy);
    this.view.setSipDomainTextInputValue(this.webRtcCommClientConfiguration.sip.sipDomain);
    this.view.setSipDisplayNameTextInputValue(this.webRtcCommClientConfiguration.sip.sipDisplayName);
    this.view.setSipUserNameTextInputValue(this.webRtcCommClientConfiguration.sip.sipUserName);
    this.view.setSipLoginTextInputValue(this.webRtcCommClientConfiguration.sip.sipLogin);
    this.view.setSipPasswordTextInputValue(this.webRtcCommClientConfiguration.sip.sipPassword);
    this.view.setSipContactUriTextInputValue(this.sipContactUri);
    
    // Get local user media
    try
    {
        var that = this;
        if(navigator.webkitGetUserMedia)
        {
            // Google Chrome user agent
            navigator.webkitGetUserMedia({
                audio:true, 
                video:true
            }, function(localMediaStream) {
                that.onGetUserMediaSuccessEventHandler(localMediaStream);
            }, function(error) {
                that.onGetUserMediaErrorEventHandler(error);
            });
        }
        else if(navigator.mozGetUserMedia)
        {
            // Mozilla firefox  user agent
            navigator.mozGetUserMedia({
                audio:true,
                video:true
            },function(localMediaStream) {
                that.onGetUserMediaSuccessEventHandler(localMediaStream);
            },function(error) {
                that.onGetUserMediaErrorEventHandler(error);
            });
        }
        else
        {
            console.error("MobicentsWebRTCPhoneController:onLoadEventHandler(): navigator doesn't implemement getUserMedia API")
            modal_alert("MobicentsWebRTCPhoneController:onLoadEventHandler(): navigator doesn't implemement getUserMedia API")     
        }
    }
    catch(exception)
    {
        console.error("MobicentsWebRTCPhoneController:onLoadEventHandler(): catched exception: "+exception);
        modal_alert("MobicentsWebRTCPhoneController:onLoadEventHandler(): catched exception: "+exception);
    }   
}
   
/**
 * get user media success event handler (Google Chrome User agent)
 * @param localAudioVideoMediaStream object
 */ 
MobicentsWebRTCPhoneController.prototype.onGetUserMediaSuccessEventHandler=function(localAudioVideoMediaStream) 
{
    try
    {
        console.debug("MobicentsWebRTCPhoneController:onGetUserMediaSuccessEventHandler(): localAudioVideoMediaStream.id="+localAudioVideoMediaStream.id);
        this.localAudioVideoMediaStream=localAudioVideoMediaStream;
        this.view.playLocalVideo(this.localAudioVideoMediaStream);
        this.view.showLocalVideo();
        this.view.enableConnectButton();          
    }
    catch(exception)
    {
        console.debug("MobicentsWebRTCPhoneController:onGetUserMediaSuccessEventHandler(): catched exception: "+exception);
    }
}           
 
MobicentsWebRTCPhoneController.prototype.onGetUserMediaErrorEventHandler=function(error) 
{
    console.debug("MobicentsWebRTCPhoneController:onGetUserMediaErrorEventHandler(): error="+error);
    modal_alert("Failed to get local user media: error="+error);
}	
  
/**
 * on connect event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onClickConnectButtonViewEventHandler=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onClickConnectButtonViewEventHandler()"); 
    if(this.webRtcCommClient != undefined)
    {
        try
        {
            this.webRtcCommClientConfiguration.RTCPeerConnection.stunServer= this.view.getStunServerTextInputValue();
            this.webRtcCommClientConfiguration.sip.sipOutboundProxy = this.view.getSipOutboundProxyTextInputValue();
            this.webRtcCommClientConfiguration.sip.sipDomain = this.view.getSipDomainTextInputValue();
            this.webRtcCommClientConfiguration.sip.sipDisplayName= this.view.getSipDisplayNameTextInputValue();
            this.webRtcCommClientConfiguration.sip.sipUserName = this.view.getSipUserNameTextInputValue();
            this.webRtcCommClientConfiguration.sip.sipLogin = this.view.getSipLoginTextInputValue();
            this.webRtcCommClientConfiguration.sip.sipPassword = this.view.getSipPasswordTextInputValue();
            this.webRtcCommClient.open(this.webRtcCommClientConfiguration); 
            this.view.disableConnectButton();
        }
        catch(exception)
        {
            modal_alert("Connection has failed, reason:"+exception)  
        }
    }
    else
    {
        console.error("MobicentsWebRTCPhoneController:onClickConnectButtonViewEventHandler(): internal error");      
    }
}


/**
 * on disconnect event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onClickDisconnectButtonViewEventHandler=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onClickDisconnectButtonViewEventHandler()"); 
    if(this.webRtcCommClient != undefined)
    {
        try
        {
            this.webRtcCommClient.close();  
        }
        catch(exception)
        {
            modal_alert("Disconnection has failed, reason:"+exception)  
        }
    }
    else
    {
        console.error("MobicentsWebRTCPhoneController:onClickDisconnectButtonViewEventHandler(): internal error");      
    }
}

/**
 * on call event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onClickCallButtonViewEventHandler=function(calleePhoneNumber)
{
    console.debug ("MobicentsWebRTCPhoneController:onClickCallButtonViewEventHandler()"); 
    if(this.webRtcCommClient != undefined)
    {
        try
        {
            var callConfiguration = {
                displayName:this.DEFAULT_SIP_DISPLAY_NAME,
                localMediaStream: this.localAudioVideoMediaStream,
                audioMediaFlag:true,
                videoMediaFlag:true,
                messageMediaFlag:false
            }
            this.webRtcCommCall = this.webRtcCommClient.call(calleePhoneNumber, callConfiguration);
            this.view.disableCallButton();
            this.view.disableDisconnectButton();
        }
        catch(exception)
        {
            modal_alert("Call has failed, reason:"+exception)  
        }
    }
    else
    {
        console.error("MobicentsWebRTCPhoneController:onClickCallButtonViewEventHandler(): internal error");      
    }
}


/**
 * on call event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onClickEndCallButtonViewEventHandler=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onClickEndCallButtonViewEventHandler()"); 
    if(this.webRtcCommClient != undefined)
    {
        try
        {
            this.webRtcCommCall.close();
        }
        catch(exception)
        {
            modal_alert("End has failed, reason:"+exception)  
        }
    }
    else
    {
        console.error("MobicentsWebRTCPhoneController:onClickEndCallButtonViewEventHandler(): internal error");      
    }
}

/**
 * on accept event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onClickAcceptCallButtonViewEventHandler=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onClickAcceptCallButtonViewEventHandler()"); 
    if(this.webRtcCommClient != undefined)
    {
        try
        {
            var callConfiguration = {
                displayName:this.DEFAULT_SIP_DISPLAY_NAME,
                localMediaStream: this.localAudioVideoMediaStream,
                audioMediaFlag:true,
                videoMediaFlag:true,
                messageMediaFlag:false
            }
            this.webRtcCommCall.accept(callConfiguration);
            this.view.enableEndCallButton();
            this.view.stopRinging();
        }
        catch(exception)
        {
            modal_alert("End has failed, reason:"+exception)  
        }
    }
    else
    {
        console.error("MobicentsWebRTCPhoneController:onClickAcceptCallButtonViewEventHandler(): internal error");      
    }
}

/**
 * on accept event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onClickRejectCallButtonViewEventHandler=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onClickRejectCallButtonViewEventHandler()"); 
    if(this.webRtcCommClient != undefined)
    {
        try
        {
            this.webRtcCommCall.reject();
            this.view.enableCallButton();
            this.view.enableDisconnectButton();
            this.view.stopRinging();
        }
        catch(exception)
        {
            modal_alert("End has failed, reason:"+exception)  
        }
    }
    else
    {
        console.error("MobicentsWebRTCPhoneController:onClickRejectCallButtonViewEventHandler(): internal error");      
    }
}



/**
  * Implementation of the WebRtcCommClient listener interface
  */
MobicentsWebRTCPhoneController.prototype.onWebRtcCommClientOpenedEvent=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRtcCommClientOpenedEvent()");
    //Enabled button DISCONNECT, CALL diable CONNECT and BYE
    this.view.enableDisconnectButton();
    this.view.enableCallButton();
    this.view.disableConnectButton();
    this.view.disableEndCallButton();
    modal_alert("Online"); 
}
    
MobicentsWebRTCPhoneController.prototype.onWebRtcCommClientOpenErrorEvent=function(error)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRtcCommClientOpenErrorEvent():error:"+error); 
    this.view.enableConnectButton();
    this.view.disableDisconnectButton();
    this.view.disableCallButton();
    this.view.disableEndCallButton();
    this.webRtcCommCall=undefined;
    modal_alert("Connection has failed, offline"); 
} 
    
/**
 * Implementation of the WebRtcCommClient listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRtcCommClientClosedEvent=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRtcCommClientClosedEvent()"); 
    //Enabled button CONNECT, disable DISCONECT, CALL, BYE
    this.view.enableConnectButton();
    this.view.disableDisconnectButton();
    this.view.disableCallButton();
    this.view.disableEndCallButton();
    this.webRtcCommCall=undefined;
    modal_alert("Offline"); 
}
    
/**
 * Implementation of the WebRtcCommCall listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRtcCommCallClosedEvent=function(webRtcCommCall)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRtcCommCallClosedEvent(): webRtcCommCall.getId()="+webRtcCommCall.getId()); 

    //Enabled button DISCONECT, CALL
    this.view.enableCallButton();
    this.view.enableDisconnectButton();
    this.view.disableEndCallButton();
    this.view.disableConnectButton();
    this.view.stopRemoteVideo();
    this.view.hideRemoteVideo();
    this.webRtcCommCall=undefined;  
    modal_alert("Communication closed"); 
    
}
   
   
/**
 * Implementation of the WebRtcCommCall listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRtcCommCallOpenedEvent=function(webRtcCommCall)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRtcCommCallOpenedEvent(): webRtcCommCall.getId()="+webRtcCommCall.getId()); 
   
    this.view.stopRinging();
    this.view.disableCallButton();
    this.view.disableDisconnectButton();
    this.view.enableEndCallButton();
    this.view.disableDisconnectButton();
    this.view.disableConnectButton();
    this.view.showRemoteVideo();
    this.view.playRemoteVideo(webRtcCommCall.getRemoteMediaStream());
    
    modal_alert("Communication opened"); 
}

/**
 * Implementation of the WebRtcCommCall listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRtcCommCallInProgressEvent=function(webRtcCommCall)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRtcCommCallInProgressEvent(): webRtcCommCall.getId()="+webRtcCommCall.getId()); 

    modal_alert("Communication in progress"); 
}


/**
 * Implementation of the WebRtcCommCall listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRtcCommCallOpenErrorEvent=function(webRtcCommCall, error)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRtcCommCallOpenErrorEvent(): webRtcCommCall.getId()="+webRtcCommCall.getId()); 

    //Enabled button DISCONECT, CALL
    this.view.enableCallButton();
    this.view.enableDisconnectButton();
    this.view.disableEndCallButton();
    this.view.disableConnectButton();
    this.view.hideRemoteVideo();
    this.view.stopRemoteVideo();
    this.view.stopRinging();
    this.webRtcCommCall=undefined;
    modal_alert("Communication failed: error:"+error); 
}

/**
 * Implementation of the WebRtcCommCall listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRtcCommCallRingingEvent=function(webRtcCommCall)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRtcCommCallRingingEvent(): webRtcCommCall.getId()="+webRtcCommCall.getId()); 
    this.webRtcCommCall=webRtcCommCall;
    this.view.playRinging();
    this.view.disableCallButton();
    this.view.disableDisconnectButton();
    this.view.disableEndCallButton();
    this.view.disableConnectButton();
    show_desktop_notification("Incoming Call from " + webRtcCommCall.getCallerPhoneNumber());
    $("#call_message").html("<p>Incoming Call from " + webRtcCommCall.getCallerPhoneNumber() +"</p>");
     $('#callModal').modal(); 
}

/**
 * Implementation of the WebRtcCommCall listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRtcCommCallRingingBackEvent=function(webRtcCommCall)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRtcCommCallRingingBackEvent(): webRtcCommCall.getId()="+webRtcCommCall.getId()); 
    this.view.playRinging();
    this.view.disableCallButton();
    this.view.disableDisconnectButton();
    this.view.disableEndCallButton();
    this.view.disableConnectButton();
}

    



