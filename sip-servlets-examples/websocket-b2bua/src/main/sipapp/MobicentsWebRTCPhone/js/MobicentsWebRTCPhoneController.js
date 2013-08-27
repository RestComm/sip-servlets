/**
 * Class MobicentsWebRTCPhoneController
 * @public 
 */ 

navigator.getUserMedia = navigator.webkitGetUserMedia || navigator.mozGetUserMedia;
window.URL = window.URL || window.webkitURL;
/**
 * Constructor 
 */ 
function MobicentsWebRTCPhoneController(view) {
    console.debug("MobicentsWebRTCPhoneController:MobicentsWebRTCPhoneController()")
    //  WebRTComm client 
    this.view=view;
    this.webRTCommClient=new WebRTCommClient(this); 
    this.webRTCommClientConfiguration=undefined;
    this.localAudioVideoMediaStream=undefined;
    this.webRTCommCall=undefined;
    this.sipContact=MobicentsWebRTCPhoneController.prototype.DEFAULT_SIP_CONTACT;
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
//MobicentsWebRTCPhoneController.prototype.DEFAULT_STUN_SERVER="undefined"; // stun.l.google.com:19302
MobicentsWebRTCPhoneController.prototype.DEFAULT_STUN_SERVER="stun.l.google.com:19302"; 
MobicentsWebRTCPhoneController.prototype.DEFAULT_TURN_SERVER=undefined;
MobicentsWebRTCPhoneController.prototype.DEFAULT_TURN_LOGIN=undefined; 
MobicentsWebRTCPhoneController.prototype.DEFAULT_TURN_PASSWORD=undefined; 
MobicentsWebRTCPhoneController.prototype.DEFAULT_AUDIO_CODECS_FILTER=undefined; // RTCPeerConnection default codec filter
MobicentsWebRTCPhoneController.prototype.DEFAULT_VIDEO_CODECS_FILTER=undefined; // RTCPeerConnection default codec filter
MobicentsWebRTCPhoneController.prototype.DEFAULT_LOCAL_VIDEO_FORMAT="{\"mandatory\": {\"maxWidth\": 500}}"
MobicentsWebRTCPhoneController.prototype.DEFAULT_SIP_URI_CONTACT_PARAMETERS=undefined;
MobicentsWebRTCPhoneController.prototype.DEFAULT_DTLS_SRTP_KEY_AGREEMENT_MODE=true;
MobicentsWebRTCPhoneController.prototype.DEFAULT_FORCE_TURN_MEDIA_RELAY_MODE=false;

/**
 * on load event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onLoadViewEventHandler=function() 
{
    console.debug ("MobicentsWebRTCPhoneController:onLoadViewEventHandler()");
        
    // Setup SIP default Profile
    this.webRTCommClientConfiguration =  { 
        communicationMode:WebRTCommClient.prototype.SIP,
        sip:{
            sipUserAgent:this.DEFAULT_SIP_USER_AGENT,
            sipOutboundProxy:this.DEFAULT_SIP_OUTBOUND_PROXY,
            sipDomain:this.DEFAULT_SIP_DOMAIN,
            sipDisplayName:this.DEFAULT_SIP_DISPLAY_NAME,
            sipUserName:this.DEFAULT_SIP_USER_NAME,
            sipLogin:this.DEFAULT_SIP_LOGIN,
            sipPassword:this.DEFAULT_SIP_PASSWORD,
            sipUriContactParameters:this.DEFAULT_SIP_URI_CONTACT_PARAMETERS,
            sipUserAgentCapabilities:this.DEFAULT_SIP_USER_AGENT_CAPABILITIES,
            sipRegisterMode:this.DEFAULT_SIP_REGISTER_MODE
        },
        RTCPeerConnection:
        {
            stunServer:this.DEFAULT_STUN_SERVER,
            turnServer:this.DEFAULT_TURN_SERVER, 
            turnLogin:this.DEFAULT_TURN_LOGIN,
            turnPassword:this.DEFAULT_TURN_PASSWORD,
            dtlsSrtpKeyAgreement:this.DEFAULT_DTLS_SRTP_KEY_AGREEMENT_MODE,
            forceTurnMediaRelay:this.DEFAULT_FORCE_TURN_MEDIA_RELAY_MODE
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
                this.webRTCommClientConfiguration.sip.sipUserName =argument[1];
                if(this.webRTCommClientConfiguration.sip.sipUserName=="") this.webRTCommClientConfiguration.sip.sipUserName=undefined;
            } 
            else if("sipDomain"==argument[0])
            {
                this.webRTCommClientConfiguration.sip.sipDomain =argument[1];
                if(this.webRTCommClientConfiguration.sip.sipDomain=="") this.webRTCommClientConfiguration.sip.sipDomain=undefined;
            } 
            else if("sipDisplayName"==argument[0])
            {
                this.webRTCommClientConfiguration.sip.sipDisplayName =argument[1];
                if(this.webRTCommClientConfiguration.sip.sipDisplayName=="") this.webRTCommClientConfiguration.sip.sipDisplayName=undefined;
            } 
            else if("sipPassword"==argument[0])
            {
                this.webRTCommClientConfiguration.sip.sipPassword =argument[1];
                if(this.webRTCommClientConfiguration.sip.sipPassword=="") this.webRTCommClientConfiguration.sip.sipPassword=undefined;
            } 
            else if("sipLogin"==argument[0])
            {
                this.webRTCommClientConfiguration.sip.sipLogin =argument[1];
                if(this.webRTCommClientConfiguration.sip.sipLogin=="") this.webRTCommClientConfiguration.sip.sipLogin=undefined;
            }
            else if("sipContact"==argument[0])
            {
                this.sipContact =argument[1];
                if(this.webRTCommClientConfiguration.sip.sipContact=="") this.webRTCommClientConfiguration.sip.sipContact=undefined;
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
    if(this.webRTCommClient != undefined)
    {
        try
        {
            this.webRTCommClient.close();  
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
    //this.view.disableCancelCallButton();
    this.view.disableDisconnectButton();
    this.view.disableConnectButton();
    this.view.stopLocalVideo();
    this.view.hideLocalVideo();
    this.view.stopRemoteVideo();
    this.view.hideRemoteVideo();
    this.view.setStunServerTextInputValue(this.webRTCommClientConfiguration.RTCPeerConnection.stunServer);
    this.view.setTurnServerTextInputValue(this.webRTCommClientConfiguration.RTCPeerConnection.turnServer);
    this.view.setTurnLoginTextInputValue(this.webRTCommClientConfiguration.RTCPeerConnection.turnLogin);
    this.view.setTurnPasswordTextInputValue(this.webRTCommClientConfiguration.RTCPeerConnection.turnPassword);
    this.view.setSipOutboundProxyTextInputValue(this.webRTCommClientConfiguration.sip.sipOutboundProxy);
//    this.view.setSipUserAgentTextInputValue(this.webRTCommClientConfiguration.sip.sipUserAgent);
//    this.view.setSipUriContactParametersTextInputValue(this.webRTCommClientConfiguration.sip.sipUriContactParameters);
//    this.view.setSipUserAgentCapabilitiesTextInputValue(this.webRTCommClientConfiguration.sip.sipUserAgentCapabilities);
    this.view.setSipDomainTextInputValue(this.webRTCommClientConfiguration.sip.sipDomain);
    this.view.setSipDisplayNameTextInputValue(this.webRTCommClientConfiguration.sip.sipDisplayName);
    this.view.setSipUserNameTextInputValue(this.webRTCommClientConfiguration.sip.sipUserName);
    this.view.setSipLoginTextInputValue(this.webRTCommClientConfiguration.sip.sipLogin);
    this.view.setSipPasswordTextInputValue(this.webRTCommClientConfiguration.sip.sipPassword);
    this.view.setSipContactTextInputValue(this.sipContact);
    this.view.setAudioCodecsFilterTextInputValue(MobicentsWebRTCPhoneController.prototype.DEFAULT_AUDIO_CODECS_FILTER);
    this.view.setVideoCodecsFilterTextInputValue(MobicentsWebRTCPhoneController.prototype.DEFAULT_VIDEO_CODECS_FILTER);
    this.view.setLocalVideoFormatTextInputValue(MobicentsWebRTCPhoneController.prototype.DEFAULT_LOCAL_VIDEO_FORMAT)
    
    // Get local user media
    try
    {
        this.getLocalUserMedia(MobicentsWebRTCPhoneController.prototype.DEFAULT_LOCAL_VIDEO_FORMAT)
    }
    catch(exception)
    {
        console.error("MobicentsWebRTCPhoneController:onLoadEventHandler(): catched exception: "+exception);
        modal_alert("MobicentsWebRTCPhoneController:onLoadEventHandler(): catched exception: "+exception);
    }   
}
  
MobicentsWebRTCPhoneController.prototype.getLocalUserMedia=function(videoContraints){
    console.debug ("MobicentsWebRTCPhoneController:getLocalUserMedia():videoContraints="+JSON.stringify(videoContraints));  
    var that = this;
    this.view.stopLocalVideo();
    if(this.localAudioVideoMediaStream) this.localAudioVideoMediaStream.stop();
    if(navigator.getUserMedia)
    {
        // Google Chrome user agent
        navigator.getUserMedia({
            audio:true, 
            video: JSON.parse(videoContraints)
        }, function(localMediaStream) {
            that.onGetUserMediaSuccessEventHandler(localMediaStream);
        }, function(error) {
            that.onGetUserMediaErrorEventHandler(error);
        });
    }
    else
    {
        console.error("MobicentsWebRTCPhoneController:onLoadEventHandler(): navigator doesn't implemement getUserMedia API")
        modal_alert("MobicentsWebRTCPhoneController:onLoadEventHandler(): navigator doesn't implemement getUserMedia API")     
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
        this.localAudioVideoMediaStream.onended = function() {
            alert("this.localAudioVideoMediaStream.onended")
        }
        var audioTracks = undefined;
        if(this.localAudioVideoMediaStream.audioTracks) audioTracks=this.localAudioVideoMediaStream.audioTracks;
        else if(this.localAudioVideoMediaStream.getAudioTracks) audioTracks=this.localAudioVideoMediaStream.getAudioTracks();
        if(audioTracks)
        {
            console.debug("MobicentsWebRTCPhoneController:onWebkitGetUserMediaSuccessEventHandler(): audioTracks="+JSON.stringify(audioTracks));
            for(var i=0; i<audioTracks.length;i++)
            {
                audioTracks[i].onmute = function() {
                    alert("videoTracks[i].onmute")
                };
                audioTracks[i].onunmute = function() {
                    alert("audioTracks[i].onunmute")
                }
                audioTracks[i].onended = function() {
                    alert("audioTracks[i].onended")
                }
            }             
            audioTracks.onmute = function() {
                alert("audioTracks.onmute")
            };
            audioTracks.onunmute = function() {
                alert("audioTracks.onunmute")
            }
            audioTracks.onended = function() {
                alert("audioTracks.onended")
            } 
        }
        else
        {
            alert("MediaStream Track  API not supported");
        }
        
        var videoTracks = undefined;
        if(this.localAudioVideoMediaStream.videoTracks) videoTracks=this.localAudioVideoMediaStream.videoTracks;
        else if(this.localAudioVideoMediaStream.getVideoTracks) videoTracks=this.localAudioVideoMediaStream.getVideoTracks();
        if(videoTracks)
        {
            console.debug("MobicentsWebRTCPhoneController:onWebkitGetUserMediaSuccessEventHandler(): videoTracks="+JSON.stringify(videoTracks));
            for(var i=0; i<videoTracks.length;i++)
            {
                videoTracks[i].onmute = function() {
                    alert("videoTracks[i].onmute")
                };
                videoTracks[i].onunmute = function() {
                    alert("videoTracks[i].onunmute")
                }
                videoTracks[i].onended = function() {
                    alert("videoTracks[i].onended")
                }
            }
            videoTracks.onmute = function() {
                alert("videoTracks.onmute")
            };
            videoTracks.onunmute = function() {
                alert("videoTracks.onunmute")
            }
            videoTracks.onended = function() {
                alert("videoTracks.onended")
            }
        }
        
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
MobicentsWebRTCPhoneController.prototype.onChangeLocalVideoFormatViewEventHandler=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onChangeLocalVideoFormatViewEventHandler()");  
    // Get local user media
    try
    {
        this.getLocalUserMedia(this.view.getLocalVideoFormatTextInputValue());
    }
    catch(exception)
    {
        console.error("MobicentsWebRTCPhoneController:onChangeLocalVideoFormatViewEventHandler(): catched exception: "+exception);
        modal_alert("MobicentsWebRTCPhoneController:onChangeLocalVideoFormatViewEventHandler(): catched exception: "+exception);
    }   
}
/**
 * on connect event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onClickConnectButtonViewEventHandler=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onClickConnectButtonViewEventHandler()"); 
    if(this.webRTCommClient != undefined)
    {
        try
        {
	    if(this.view.getStunServerTextInputValue() != null)
            {
                this.webRTCommClientConfiguration.RTCPeerConnection.stunServer= this.view.getStunServerTextInputValue();
            }
            else 
            {
                this.webRTCommClientConfiguration.RTCPeerConnection.stunServer=undefined;
            }
            if(this.view.getTurnServerTextInputValue() != null)
            {
                this.webRTCommClientConfiguration.RTCPeerConnection.turnServer= this.view.getTurnServerTextInputValue();
                this.webRTCommClientConfiguration.RTCPeerConnection.turnLogin= this.view.getTurnLoginTextInputValue();
                this.webRTCommClientConfiguration.RTCPeerConnection.turnPassword= this.view.getTurnPasswordTextInputValue();
            }
            else  
            { 
                this.webRTCommClientConfiguration.RTCPeerConnection.turnServer= undefined;
                this.webRTCommClientConfiguration.RTCPeerConnection.turnLogin= undefined;
                this.webRTCommClientConfiguration.RTCPeerConnection.turnPassword= undefined;
            }

           //this.webRTCommClientConfiguration.RTCPeerConnection.forceTurnMediaRelay=this.view.getForceTurnMediaRelayValue();
            //this.webRTCommClientConfiguration.RTCPeerConnection.dtlsSrtpKeyAgreement=this.view.getDtlsSrtpKeyAgreementValue();
            this.webRTCommClientConfiguration.sip.sipOutboundProxy = this.view.getSipOutboundProxyTextInputValue();
            //this.webRTCommClientConfiguration.sip.sipUserAgent = this.view.getSipUserAgentTextInputValue(); 
            //this.webRTCommClientConfiguration.sip.sipUriContactParameters = this.view.getSipUriContactParametersTextInputValue();
            //this.webRTCommClientConfiguration.sip.sipUserAgentCapabilities = this.view.getSipUserAgentCapabilitiesTextInputValue();
            this.webRTCommClientConfiguration.sip.sipDomain = this.view.getSipDomainTextInputValue();
            this.webRTCommClientConfiguration.sip.sipDisplayName= this.view.getSipDisplayNameTextInputValue();
            this.webRTCommClientConfiguration.sip.sipUserName = this.view.getSipUserNameTextInputValue();
            this.webRTCommClientConfiguration.sip.sipLogin = this.view.getSipLoginTextInputValue();
            this.webRTCommClientConfiguration.sip.sipPassword = this.view.getSipPasswordTextInputValue();
            //this.webRTCommClientConfiguration.sip.sipRegisterMode = this.view.getSipRegisterModeValue();
            this.webRTCommClient.open(this.webRTCommClientConfiguration); 
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
    if(this.webRTCommClient != undefined)
    {
        try
        {
            this.webRTCommClient.close();  
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
    if(this.webRTCommCall == undefined)
    {
        try
        {
            var callConfiguration = {
                displayName:this.DEFAULT_SIP_DISPLAY_NAME,
                localMediaStream: this.localAudioVideoMediaStream,
                audioMediaFlag:true,
                videoMediaFlag:true,
                messageMediaFlag:false,
                audioCodecsFilter:this.view.getAudioCodecsFilterTextInputValue(),
                videoCodecsFilter:this.view.getVideoCodecsFilterTextInputValue()
            }
            this.webRTCommCall = this.webRTCommClient.call(calleePhoneNumber, callConfiguration);
            this.view.disableCallButton();
            this.view.disableDisconnectButton();
            //this.view.enableCancelCallButton();
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
MobicentsWebRTCPhoneController.prototype.onClickCancelCallButtonViewEventHandler=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onClickCancelCallButtonViewEventHandler()"); 
    if(this.webRTCommCall != undefined)
    {
        try
        {
            this.webRTCommCall.close();
            this.view.disableCancelCallButton();
            this.view.stopRinging();
        }
        catch(exception)
        {
            console.error("MobicentsWebRTCPhoneController:onClickCancelCallButtonViewEventHandler(): catched exception:"+exception)  
        }
    }
    else
    {
        console.error("MobicentsWebRTCPhoneController:onClickCancelCallButtonViewEventHandler(): internal error");      
    }
}

/**
 * on call event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onClickEndCallButtonViewEventHandler=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onClickEndCallButtonViewEventHandler()"); 
    if(this.webRTCommCall)
    {
        try
        {
            this.webRTCommCall.close();
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
    if(this.webRTCommCall)
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
            this.webRTCommCall.accept(callConfiguration);
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
	if(this.webRTCommCall)
    {
        try
        {
            this.webRTCommCall.reject();
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
 * on accept event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onClickSendMessageButtonViewEventHandler=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onClickSendMessageButtonViewEventHandler()"); 
    if(this.webRTCommCall)
    {
        try
        {
            var message = document.getElementById("messageTextArea").value;
            this.webRTCommCall.sendMessage(message);
            document.getElementById("messageTextArea").value="";
        }
        catch(exception)
        {
            console.error("MobicentsWebRTCPhoneController:onClickRejectCallButtonViewEventHandler(): catched exception:"+exception); 
            alert("Send message failed:"+exception)
        }
    }
    else
    {
        console.error("MobicentsWebRTCPhoneController:onClickRejectCallButtonViewEventHandler(): internal error");      
    }
}



/**
 * on local audio mute event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onClickMuteLocalAudioButtonViewEventHandler=function(checked)
{
    console.debug ("MobicentsWebRTCPhoneController:onClickMuteLocalAudioButtonViewEventHandler():checked="+checked);
    if(this.webRTCommCall)
    {
        try
        {
            if(checked) this.webRTCommCall.muteLocalAudioMediaStream();
            else this.webRTCommCall.unmuteLocalAudioMediaStream();
        }
        catch(exception)
        {
            console.error("MobicentsWebRTCPhoneController:onClickMuteLocalAudioButtonViewEventHandler(): catched exception:"+exception)  
        }
    }
    else
    {
        console.error("MobicentsWebRTCPhoneController:onClickMuteLocalAudioButtonViewEventHandler(): internal error");      
    } 
}

/**
 * on local video hide event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onClickHideLocalVideoButtonViewEventHandler=function(checked)
{
    console.debug ("MobicentsWebRTCPhoneController:onClickHideLocalVideoButtonViewEventHandler():checked="+checked);
    if(this.webRTCommCall)
    {
        try
        {
            if(checked) this.webRTCommCall.hideLocalVideoMediaStream();
            else this.webRTCommCall.showLocalVideoMediaStream();
        }
        catch(exception)
        {
            console.error("MobicentsWebRTCPhoneController:onClickHideLocalVideoButtonViewEventHandler(): catched exception:"+exception)  
        }
    }
    else
    {
        console.error("MobicentsWebRTCPhoneController:onClickHideLocalVideoButtonViewEventHandler(): internal error");      
    }   
}

/**
 * on remote audio mute event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onClickMuteRemoteAudioButtonViewEventHandler=function(checked)
{
    console.debug ("MobicentsWebRTCPhoneController:onClickMuteRemoteAudioButtonViewEventHandler():checked="+checked);
    if(this.webRTCommCall)
    {
        try
        {
            if(checked) this.webRTCommCall.muteRemoteAudioMediaStream();
            else this.webRTCommCall.unmuteRemoteAudioMediaStream();
        }
        catch(exception)
        {
            console.error("MobicentsWebRTCPhoneController:onClickMuteRemoteAudioButtonViewEventHandler(): catched exception:"+exception)  
        }
    }
    else
    {
        console.error("MobicentsWebRTCPhoneController:onClickMuteRemoteAudioButtonViewEventHandler(): internal error");      
    } 
}

/**
 * on remote video mute event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onClickHideRemoteVideoButtonViewEventHandler=function(checked)
{
    console.debug ("MobicentsWebRTCPhoneController:onClickHideRemoteVideoButtonViewEventHandler():checked="+checked);
    if(this.webRTCommCall)
    {
        try
        {
            if(checked) this.webRTCommCall.hideRemoteVideoMediaStream();
            else this.webRTCommCall.showRemoteVideoMediaStream();
        }
        catch(exception)
        {
            console.error("MobicentsWebRTCPhoneController:onClickHideRemoteVideoButtonViewEventHandler(): catched exception:"+exception)  
        }
    }
    else
    {
        console.error("MobicentsWebRTCPhoneController:onClickHideRemoteVideoButtonViewEventHandler(): internal error");      
    }   
}

/**
 * on remote video mute event handler
 */ 
MobicentsWebRTCPhoneController.prototype.onClickStopVideoStreamButtonViewEventHandler=function(checked)
{
    console.debug ("MobicentsWebRTCPhoneController:onClickStopVideoStreamButtonViewEventHandler():checked="+checked);
    var videoTracks = undefined;
    if(this.localAudioVideoMediaStream.videoTracks) videoTracks=this.localAudioVideoMediaStream.videoTracks;
    else if(this.localAudioVideoMediaStream.getVideoTracks) videoTracks=this.localAudioVideoMediaStream.getVideoTracks();
    if(videoTracks)
    {
        for(var i=0; i<videoTracks.length;i++)
        {
            this.localAudioVideoMediaStream.removeTrack(videoTracks[i]);
        }                  
    }  
    if(this.webRTCommCall)
    {
        try
        {
            //this.webRTCommCall.stopVideoMediaStream();
            var videoTracks = undefined;
            if(this.localAudioVideoMediaStream.videoTracks) videoTracks=this.localAudioVideoMediaStream.videoTracks;
            else if(this.localAudioVideoMediaStream.getVideoTracks) videoTracks=this.localAudioVideoMediaStream.getVideoTracks();
            if(videoTracks)
            {
                for(var i=0; i<videoTracks.length;i++)
                {
                    this.localAudioVideoMediaStream.removeTrack(videoTracks[i]);
                }                  
            }  
        }
        catch(exception)
        {
            console.error("MobicentsWebRTCPhoneController:onClickStopVideoStreamButtonViewEventHandler(): catched exception:"+exception)  
        }
    }
    else
    {
        console.error("MobicentsWebRTCPhoneController:onClickStopVideoStreamButtonViewEventHandler(): internal error");      
    }   
}

/**
  * Implementation of the WebRtcCommClient listener interface
  */
MobicentsWebRTCPhoneController.prototype.onWebRTCommClientOpenedEvent=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRTCommClientOpenedEvent()");
    //Enabled button DISCONNECT, CALL diable CONNECT and BYE
    this.view.enableDisconnectButton();
    this.view.enableCallButton();
    this.view.disableConnectButton();
    this.view.disableEndCallButton();
	//this.view.disableCancelCallButton();
    //this.view.disableSendMessageButton();
    modal_alert("Online"); 
}
    
MobicentsWebRTCPhoneController.prototype.onWebRTCommClientOpenErrorEvent=function(error)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRTCommClientOpenErrorEvent():error:"+error); 
    this.view.enableConnectButton();
    this.view.disableDisconnectButton();
    this.view.disableCallButton();
    this.view.disableEndCallButton();
    //this.view.disableCancelCallButton();
    //this.view.disableSendMessageButton();
    this.webRTCommCall=undefined;
    modal_alert("Connection has failed, offline"); 
} 
    
/**
 * Implementation of the WebRtcCommClient listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRTCommClientClosedEvent=function()
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRTCommClientClosedEvent()"); 
    //Enabled button CONNECT, disable DISCONECT, CALL, BYE
    this.view.enableConnectButton();
    this.view.disableDisconnectButton();
    this.view.disableCallButton();
    this.view.disableEndCallButton();
    //this.view.disableCancelCallButton();
    //this.view.disableSendMessageButton();
    this.webRTCommCall=undefined;
    modal_alert("Offline"); 
}
    
/**
 * Implementation of the webRTCommCall listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRTCommCallClosedEvent=function(webRTCommCall)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRTCommCallClosedEvent(): webRTCommCall.getId()="+webRTCommCall.getId()); 

    //Enabled button DISCONECT, CALL
    this.view.enableCallButton();
    this.view.enableDisconnectButton();
    this.view.disableEndCallButton();
    this.view.disableConnectButton();
    this.view.stopRemoteVideo();
    this.view.hideRemoteVideo();
    //this.view.disableCancelCallButton();
    //this.view.disableSendMessageButton();
    this.webRTCommCall=undefined;
    modal_alert("Communication closed"); 
    
}
   
   
/**
 * Implementation of the webRTCommCall listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRTCommCallOpenedEvent=function(webRTCommCall)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRTCommCallOpenedEvent(): webRTCommCall.getId()="+webRTCommCall.getId()); 
   
    this.view.stopRinging();
    this.view.disableCallButton();
    this.view.disableDisconnectButton();
    this.view.enableEndCallButton();
    this.view.disableDisconnectButton();
    this.view.disableConnectButton();
    //this.view.disableCancelCallButton();
    //this.view.enableSendMessageButton();
    if(webRTCommCall.getRemoteBundledAudioVideoMediaStream())
    {
        this.view.showRemoteVideo();
        this.view.playRemoteVideo(webRTCommCall.getRemoteBundledAudioVideoMediaStream());
    }
    else
    {
        /*if(webRTCommCall.getRemoteAudioMediaStream())
        {
            this.view.playRemoteAudio(webRTCommCall.getRemoteAudioMediaStream());
        } */
        if(webRTCommCall.getRemoteVideoMediaStream())
        {
            this.view.showRemoteVideo();
            this.view.playRemoteVideo(webRTCommCall.getRemoteVideoMediaStream());
        } 
    }
    
    modal_alert("Communication opened"); 
}

/**
 * Implementation of the webRTCommCall listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRTCommCallInProgressEvent=function(webRTCommCall)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRTCommCallInProgressEvent(): webRTCommCall.getId()="+webRTCommCall.getId()); 

    modal_alert("Communication in progress"); 
}


/**
 * Implementation of the webRTCommCall listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRTCommCallOpenErrorEvent=function(webRTCommCall, error)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRTCommCallOpenErrorEvent(): webRTCommCall.getId()="+webRTCommCall.getId()); 

    //Enabled button DISCONECT, CALL
    this.view.enableCallButton();
    this.view.enableDisconnectButton();
    this.view.disableEndCallButton();
    this.view.disableConnectButton();
    this.view.hideRemoteVideo();
    this.view.stopRemoteVideo();
    this.view.stopRinging();
    //this.view.disableCancelCallButton();
    //this.view.disableSendMessageButton();
    this.webRTCommCall=undefined;
    modal_alert("Communication failed: error:"+error); 
}

/**
 * Implementation of the webRTCommCall listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRTCommCallRingingEvent=function(webRTCommCall)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRTCommCallRingingEvent(): webRTCommCall.getId()="+webRTCommCall.getId()); 
    this.webRTCommCall=webRTCommCall;
    this.view.playRinging();
    this.view.disableCallButton();
    this.view.disableDisconnectButton();
    this.view.disableEndCallButton();
    this.view.disableConnectButton();
    //this.view.disableSendMessageButton();
    //this.view.disableCancelCallButton();
    show_desktop_notification("Incoming Call from " + webRTCommCall.getCallerPhoneNumber());
    $("#call_message").html("<p>Incoming Call from " + webRTCommCall.getCallerPhoneNumber() +"</p>");
     $('#callModal').modal(); 
}

/**
 * Implementation of the webRTCommCall listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRTCommCallRingingBackEvent=function(webRTCommCall)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRTCommCallRingingBackEvent(): webRTCommCall.getId()="+webRTCommCall.getId()); 
    this.view.playRinging();
    this.view.disableCallButton();
    this.view.disableDisconnectButton();
    this.view.disableEndCallButton();
    //this.view.disableSendMessageButton();
    //this.view.enableCancelCallButton();
    this.view.disableConnectButton();
}

/**
 * Implementation of the WebRTCommCall listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRTCommCallHangupEvent=function(webRTCommCall)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRTCommCallHangupEvent(): webRTCommCall.getId()="+webRTCommCall.getId()); 
    //Enabled button DISCONECT, CALL
    this.view.enableCallButton();
    this.view.enableDisconnectButton();
    //this.view.disableRejectCallButton();
    //this.view.disableAcceptCallButton();
    this.view.disableEndCallButton();
    //this.view.disableCancelCallButton();
    this.view.disableConnectButton();
    //this.view.disableSendMessageButton();    
    this.view.stopRemoteVideo();
    //this.view.stopRemoteAudio();
    this.view.stopRinging();
    this.view.hideRemoteVideo();
    this.webRTCommCall=undefined;
    
    if(webRTCommCall.getCallerPhoneNumber())
        modal_alert("Communication closed by "+webRTCommCall.getCallerPhoneNumber());
    else 
        modal_alert("Communication close by "+webRTCommCall.getCalleePhoneNumber());
}

/**
 * Implementation of the WebRTCommCall listener interface
 */
MobicentsWebRTCPhoneController.prototype.onWebRTCommCallMessageEvent=function(webRTCommCall, message)
{
    console.debug ("MobicentsWebRTCPhoneController:onWebRTCommCallMessageEvent(): webRTCommCall.getId()="+webRTCommCall.getId()); 
    if(webRTCommCall.isIncoming()) alert("Message from "+webRTCommCall.getCallerPhoneNumber()+":"+message);
    else alert("Message from "+webRTCommCall.getCalleePhoneNumber()+":"+message);
}


/**
 * Message event
 * @public
 * @param {WebRTCommCall} webRTCommCall source WebRTCommCall object
 */
WebRTCommCallEventListenerInterface.prototype.onWebRTCommCallMessageEvent= function(webRTCommCall, message) {
    throw "WebRTCommCallEventListenerInterface:onWebRTCommCallMessageEvent(): not implemented;";   
}




