/**
 * @class PrivateJainSipMessageConnector
 * @private
 * @classdesc Private framework class handling  SIP client/user message control 
 * @constructor
 * @param {PrivateJainSipClientConnector} clientConnector clientConnector owner object
 * @param {WebRTCommMessage} webRTCommMessage WebRTCommMessage "connected" object
 * @param {string} sipCallId   SIP Call ID
 * @throw {String} Exception "bad argument"
 * @author Laurent STRULLU (laurent.strullu@orange.com) 
 * @author Jean Deruelle (jean.deruelle@ŧelestax.com)
 */
PrivateJainSipMessageConnector = function(clientConnector, webRTCommMessage, sipCallId)
{
    console.debug("PrivateJainSipMessageConnector:PrivateJainSipMessageConnector()");
    if (clientConnector instanceof PrivateJainSipClientConnector && webRTCommMessage instanceof WebRTCommMessage)
    {
        if (typeof(sipCallId) === 'string')
        {
            this.sipCallId = sipCallId;
        }
        else
        {
            this.sipCallId = new String(new Date().getTime());
        }
        this.clientConnector = clientConnector;
        this.webRTCommMessage = webRTCommMessage;
        this.sipMessageState = undefined;
    } else {
        throw "PrivateJainSipMessageConnector:PrivateJainSipMessageConnector(): bad arguments"
    }
};

/**
 * SIP Message Control state machine constant
 * @private
 * @constant
 */
PrivateJainSipMessageConnector.prototype.SIP_MESSAGE_SENDING_STATE = "SIP_MESSAGE_SENDING_STATE";
PrivateJainSipMessageConnector.prototype.SIP_MESSAGE_407_STATE = "SIP_MESSAGE_407_STATE";
PrivateJainSipMessageConnector.prototype.SIP_MESSAGE_SENT_STATE = "SIP_MESSAGE_SENT_STATE";
PrivateJainSipMessageConnector.prototype.SIP_MESSAGE_SEND_FAILED_STATE = "SIP_MESSAGE_SEND_FAILED_STATE";
PrivateJainSipMessageConnector.prototype.SIP_MESSAGE_RECEIVED_STATE = "SIP_MESSAGE_RECEIVED_STATE";


/**
 * Get message id
 * @public
 * @returns {String} sipCallId  
 */ 
PrivateJainSipMessageConnector.prototype.getId= function() {
    return this.sipCallId;  
};

/**
 * Send Authenticated SIP MESSAGE request
 * @param {Request} jainSipMessageRequest 
 * @param {AuthorizationHeader} jainSipAuthorizationHeader
 * @private
 */
PrivateJainSipMessageConnector.prototype.sendAuthenticatedSipMessageRequest = function(jainSipMessageRequest, jainSipAuthorizationHeader) {
    console.debug("PrivateJainSipMessageConnector:sendAuthenticatedSipMessageRequest()");
    jainSipMessageRequest.removeHeader("Authorization");
    var newJainSipMessageRequest = new SIPRequest();
    newJainSipMessageRequest.setMethod(jainSipMessageRequest.getMethod());
    newJainSipMessageRequest.setRequestURI(jainSipMessageRequest.getRequestURI());
    var headerList = jainSipMessageRequest.getHeaders();
    for (var i = 0; i < headerList.length; i++)
    {
    	newJainSipMessageRequest.addHeader(headerList[i]);
    }

    var num = new Number(jainSipMessageRequest.getCSeq().getSeqNumber());
    newJainSipMessageRequest.getCSeq().setSeqNumber(num + 1);
    newJainSipMessageRequest.setCallId(jainSipMessageRequest.getCallId());
    newJainSipMessageRequest.setVia(this.clientConnector.jainSipListeningPoint.getViaHeader());
    newJainSipMessageRequest.setFrom(jainSipMessageRequest.getFrom());
    newJainSipMessageRequest.setTo(jainSipMessageRequest.getTo());
    newJainSipMessageRequest.setMaxForwards(jainSipMessageRequest.getMaxForwards());
    if (jainSipMessageRequest.getContent() !== null)
    {
        var content = jainSipMessageRequest.getContent();
        var contentType = jainSipMessageRequest.getContentTypeHeader();
        newJainSipMessageRequest.setContent(content, contentType);
    }
    
    this.clientConnector.jainSipMessageFactory.addHeader(newJainSipMessageRequest, jainSipAuthorizationHeader);
    jainSipMessageTransaction = this.clientConnector.jainSipProvider.getNewClientTransaction(newJainSipMessageRequest);
    newJainSipMessageRequest.setTransaction(jainSipMessageTransaction);
    jainSipMessageTransaction.sendRequest();
};

/**
 * PrivateJainSipClientConnector interface implementation: handle SIP Request event
 * @public 
 * @param {RequestEvent} requestEvent 
 */
PrivateJainSipMessageConnector.prototype.onJainSipClientConnectorSipRequestEvent = function(requestEvent) {
    console.debug("PrivateJainSipMessageConnector:onJainSipClientConnectorSipRequestEvent() requestEvent : " + requestEvent);

    this.sipMessageState = this.SIP_MESSAGE_RECEIVED_STATE;

    // Send SIP 200 OK response   
    var jainSipRequest = requestEvent.getRequest();
    var jainSip200OKResponse = jainSipRequest.createResponse(200, "OK");
    jainSip200OKResponse.addHeader(this.clientConnector.jainSipContactHeader);
    jainSip200OKResponse.removeHeader("P-Asserted-Identity");
    jainSip200OKResponse.removeHeader("P-Charging-Vector");
    jainSip200OKResponse.removeHeader("P-Charging-Function-Addresses");
    jainSip200OKResponse.removeHeader("P-Called-Party-ID");
    jainSip200OKResponse.removeContent();
    requestEvent.getServerTransaction().sendResponse(jainSip200OKResponse);

    this.webRTCommMessage.from = requestEvent.getRequest().getHeader("From").getAddress().getURI().getUser();
    this.webRTCommMessage.text = requestEvent.getRequest().getContent();

    if (this.webRTCommMessage.webRTCommCall)
    {
        if (this.webRTCommMessage.webRTCommCall.eventListener.onWebRTCommMessageReceivedEvent)
        {
            var that = this;
            setTimeout(function() {
                try {
                    that.webRTCommMessage.webRTCommCall.eventListener.onWebRTCommMessageReceivedEvent(that.webRTCommMessage);
                }
                catch (exception) {
                    console.error("PrivateJainSipClientConnector:onJainSipClientConnectorSipRequestEvent(): catched exception in event listener:" + exception);
                }
            }, 1);
        }
    }
    else
    {
        // No linked call to the event message, forward the message to the client   
        if (this.webRTCommMessage.webRTCommClient.eventListener.onWebRTCommMessageReceivedEvent)
        {
            var that = this;
            setTimeout(function() {
                try {
                    that.webRTCommMessage.webRTCommClient.eventListener.onWebRTCommMessageReceivedEvent(that.webRTCommMessage);
                }
                catch (exception) {
                    console.error("PrivateJainSipClientConnector:onJainSipClientConnectorSipRequestEvent(): catched exception in event listener:" + exception);
                }
            }, 1);
        }
    }
    
    this.close();
};

/**
 * PrivateJainSipClientConnector interface implementation: handle SIP response event
 * @public 
 * @param {ResponseEvent} responseEvent 
 */
PrivateJainSipMessageConnector.prototype.onJainSipClientConnectorSipResponseEvent = function(responseEvent) {
    console.debug("PrivateJainSipMessageConnector:onJainSipClientConnectorSipResponseEvent() responseEvent : " + responseEvent.getResponse().getStatusLine().getReasonPhrase());
    var jainSipResponse = responseEvent.getResponse();
    var statusCode = parseInt(jainSipResponse.getStatusCode());
    
    if (this.sipMessageState === this.SIP_MESSAGE_SENDING_STATE || this.sipMessageState === this.SIP_MESSAGE_407_STATE)
    {
        if (statusCode >= 100 && statusCode < 300) {
            this.sipMessageState = this.SIP_MESSAGE_SENT_STATE;
            if (this.webRTCommMessage.webRTCommCall)
            {
                if (this.webRTCommMessage.webRTCommCall.eventListener.onWebRTCommMessageSentEvent)
                {
                    var that = this;
                    setTimeout(function() {
                        try {
                            that.webRTCommMessage.webRTCommCall.eventListener.onWebRTCommMessageSentEvent(that.webRTCommMessage);
                        }
                        catch (exception) {
                            console.error("PrivateJainSipClientConnector:onJainSipClientConnectorSipResponseEvent(): catched exception in event listener:" + exception);
                        }
                    }, 1);
                }
            }
            else
            {            	
                // No linked call to the event message, forward the message to the client   
                if (this.webRTCommMessage.webRTCommClient.eventListener.onWebRTCommMessageSentEvent)
                {
                    var that = this;
                    setTimeout(function() {
                        try {
                            that.webRTCommMessage.webRTCommClient.eventListener.onWebRTCommMessageSentEvent(that.webRTCommMessage);
                        }
                        catch (exception) {
                            console.error("PrivateJainSipClientConnector:onJainSipClientConnectorSipResponseEvent(): catched exception in event listener:" + exception);
                        }
                    }, 1);
                }
            }
        } else {
            if (statusCode === 407)
            {
        	this.sipMessageState = this.SIP_MESSAGE_407_STATE;
    		
                // Send Authenticated SIP INVITE
        	var jainSipOriginalMessageRequest = responseEvent.getOriginalTransaction().getOriginalRequest();
                var jainSipAuthorizationHeader = this.clientConnector.jainSipHeaderFactory.createAuthorizationHeader(jainSipResponse, jainSipOriginalMessageRequest, 						   this.clientConnector.configuration.sipPassword, this.clientConnector.configuration.sipLogin);
                this.sendAuthenticatedSipMessageRequest(jainSipOriginalMessageRequest, jainSipAuthorizationHeader);
                return;
            } else {
            	this.sipMessageState = this.SIP_MESSAGE_SEND_FAILED_STATE;
            }
        	
            if (this.webRTCommMessage.webRTCommCall)
            {
            	if (this.webRTCommMessage.webRTCommCall.eventListener.onWebRTCommMessageSendErrorEvent)
                {
                    var that = this;
                    setTimeout(function() {
                        try {
                            that.webRTCommMessage.webRTCommCall.eventListener.onWebRTCommMessageSendErrorEvent(that.webRTCommMessage, jainSipResponse.getStatusLine().getReasonPhrase());
                        }
                        catch (exception) {
                            console.error("PrivateJainSipClientConnector:onJainSipClientConnectorSipResponseEvent(): catched exception in event listener:" + exception);
                        }
                    }, 1);
                }
            }
            else
            {
            	if (this.webRTCommMessage.webRTCommClient.eventListener.onWebRTCommMessageSendErrorEvent)
                // No linked call to the event message, forward the message to the client                  
                {
                    var that = this;
                    setTimeout(function() {
                        try {
                            that.webRTCommMessage.webRTCommClient.eventListener.onWebRTCommMessageSendErrorEvent(that.webRTCommMessage, jainSipResponse.getStatusLine().getReasonPhrase());
                        }
                        catch (exception) {
                            console.error("PrivateJainSipClientConnector:onJainSipClientConnectorSipResponseEvent(): catched exception in event listener:" + exception);
                        }
                    }, 1);
                }
            }
        }
    }
    else
    {
        console.error("PrivateJainSipMessageConnector:onJainSipClientConnectorSipResponseEvent() : bad state : " + this.sipMessageState);
    }    
    this.close();
};

/**
 * PrivateJainSipClientConnector interface implementation: handle SIP timeout event
 * @public 
 * @param {TimeoutEvent} timeoutEvent
 */
PrivateJainSipMessageConnector.prototype.onJainSipClientConnectorSipTimeoutEvent = function(timeoutEvent) {
    console.debug("PrivateJainSipMessageConnector:onJainSipClientConnectorSipTimeoutEvent()");

    if (this.sipMessageState === this.SIP_MESSAGE_SENDING_STATE)
    {
        this.sipMessageState = this.SIP_MESSAGE_SEND_FAILED_STATE;
        if (this.webRTCommMessage.webRTCommCall)
        {
            if (this.webRTCommMessage.webRTCommCall.eventListener.onWebRTCommMessageSendErrorEvent)
            {
                var that = this;
                setTimeout(function() {
                    try {
                        that.webRTCommMessage.webRTCommCall.eventListener.onWebRTCommMessageSendErrorEvent(that.webRTCommMessage, "SIP Timeout");
                    }
                    catch (exception) {
                        console.error("PrivateJainSipClientConnector:onJainSipClientConnectorSipTimeoutEvent(): catched exception in event listener:" + exception);
                    }
                }, 1);
            }
        }
        else
        {
            // No linked call to the event message, forward the message to the client   
            if (this.webRTCommMessage.webRTCommClient.eventListener.onWebRTCommMessageSendErrorEvent)
            {
                var that = this;
                setTimeout(function() {
                    try {
                        that.webRTCommClient.eventListener.onWebRTCommMessageSendErrorEvent(that.webRTCommMessage, "SIP Timeout");
                    }
                    catch (exception) {
                        console.error("PrivateJainSipClientConnector:onJainSipClientConnectorSipTimeoutEvent(): catched exception in event listener:" + exception);
                    }
                }, 1);
            }
        }
    }
    else
    {
        console.error("PrivateJainSipMessageConnector:onJainSipClientConnectorSipTimeoutEvent() : bad state : " + this.sipMessageState);
    }
        
    this.close();
};

/**
 * Asynchronous action : close message connector in each case.
 * @public 
 */ 
PrivateJainSipMessageConnector.prototype.close =function(){	
    console.debug("PrivateJainSipMessageConnector:close(): this.sipCallState="+this.sipMessageState);
    this.clientConnector.removeSessionConnector(this.sipCallId);		
};


/**
 * Send SIP MESSAGE request
 * @public 
 */
PrivateJainSipMessageConnector.prototype.send = function() {
    console.debug("PrivateJainSipMessageConnector:send()");
    if (this.sipMessageState === undefined)
    {
        var toSipUri = this.webRTCommMessage.to;
        if (toSipUri.indexOf("@") === -1)
        {
            //No domain, add caller one 
            toSipUri += "@" + this.clientConnector.configuration.sipDomain;
        }
        var fromSipUriString = this.clientConnector.configuration.sipUserName + "@" + this.clientConnector.configuration.sipDomain;
        var jainSipCseqHeader = this.clientConnector.jainSipHeaderFactory.createCSeqHeader(1, "MESSAGE");
        var jainSipCallIdHeader = this.clientConnector.jainSipHeaderFactory.createCallIdHeader(this.sipCallId);
        var jainSipMaxForwardHeader = this.clientConnector.jainSipHeaderFactory.createMaxForwardsHeader(70);
        var jainSipRequestUri = this.clientConnector.jainSipAddressFactory.createSipURI_user_host(null, toSipUri);
        var jainSipAllowListHeader = this.clientConnector.jainSipHeaderFactory.createHeaders("Allow: INVITE,ACK,CANCEL,BYE,MESSAGE");
        var jainSipFromUri = this.clientConnector.jainSipAddressFactory.createSipURI_user_host(null, fromSipUriString);
        var jainSipFromAdress = this.clientConnector.jainSipAddressFactory.createAddress_name_uri(this.clientConnector.configuration.displayName, jainSipFromUri);

        // Setup display name
        if (this.clientConnector.configuration.displayName)
        {
            jainSipFromAdress.setDisplayName(this.clientConnector.configuration.displayName);
        }
        else if (this.clientConnector.configuration.sipDisplayName)
        {
            jainSipFromAdress.setDisplayName(this.clientConnector.configuration.sipDisplayName);
        }
        var tagFrom = new Date().getTime();
        var jainSipFromHeader = this.clientConnector.jainSipHeaderFactory.createFromHeader(jainSipFromAdress, tagFrom);
        var jainSiptoUri = this.clientConnector.jainSipAddressFactory.createSipURI_user_host(null, toSipUri);
        var jainSipToAddress = this.clientConnector.jainSipAddressFactory.createAddress_name_uri(null, jainSiptoUri);
        var jainSipToHeader = this.clientConnector.jainSipHeaderFactory.createToHeader(jainSipToAddress, null);
        var jainSipViaHeader = this.clientConnector.jainSipListeningPoint.getViaHeader();
        var jainSipContentTypeHeader = this.clientConnector.jainSipHeaderFactory.createContentTypeHeader("text", "plain");

        this.jainSipMessageRequest = this.clientConnector.jainSipMessageFactory.createRequest(
                jainSipRequestUri,
                "MESSAGE",
                jainSipCallIdHeader,
                jainSipCseqHeader,
                jainSipFromHeader,
                jainSipToHeader,
                jainSipViaHeader,
                jainSipMaxForwardHeader,
                jainSipContentTypeHeader,
                this.webRTCommMessage.text);

        this.clientConnector.jainSipMessageFactory.addHeader(this.jainSipMessageRequest, jainSipAllowListHeader);
        this.clientConnector.jainSipMessageFactory.addHeader(this.jainSipMessageRequest, this.clientConnector.jainSipContactHeader);
        var jainSipTransaction = this.clientConnector.jainSipProvider.getNewClientTransaction(this.jainSipMessageRequest);
        this.jainSipMessageRequest.setTransaction(jainSipTransaction);
        jainSipTransaction.sendRequest();
        this.sipMessageState = this.SIP_MESSAGE_SENDING_STATE;
    }
    else
    {
        console.error("PrivateJainSipMessageConnector:send(): bad state, unauthorized action");
        throw "PrivateJainSipMessageConnector:send(): bad state, unauthorized action";
    }
};
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

/** 
 * The JavaScript Framework WebRTComm allow Web Application developers to easily 
 * integrate multimedia communication service (e.g. VoIP) in their web site, thanks 
 * to the W3C WebRTC API. The WebRTComm Framework provides a high level communication 
 * API on top of the opensource JAIN SIP JavaScript Stack (implementing transport of SIP over WebSocket). 
 * By using a convergent HTTP/SIP Application Server (e.g. Mobicents MSS) or directly access a 
 * SIP server (e.g. Asterisk), the web developer can rapidly and easily link his web site to a 
 * telephony infrastructure.<br> 
 * 
 * A simple test web application of the WebRTComm Framework can be found 
 * <a href="https://code.google.com/p/webrtcomm/source/browse/?repo=test%2FWebRTCommTestWebApp">here</a>
 * 
 * @module WebRTComm 
 * @author Laurent STRULLU (laurent.strullu@orange.com) 
 */

/**
 * @class PrivateJainSipCallConnector
 * @private
 * @classdesc Private framework class handling  SIP client/user call control: ringing, ringing back, accept, reject, cancel, bye 
 * @constructor
 * @param {PrivateJainSipClientConnector} clientConnector clientConnector owner object
 * @param {WebRTCommCall} webRTCommCall WebRTCommCall "connected" object
 * @param {string} sipCallId   SIP Call ID
 * @throw {String} Exception "bad argument"
 * @author Laurent STRULLU (laurent.strullu@orange.com) 
 */
PrivateJainSipCallConnector = function(clientConnector, webRTCommCall, sipCallId)
{
    console.debug("PrivateJainSipCallConnector:PrivateJainSipCallConnector()");
    if (clientConnector instanceof PrivateJainSipClientConnector && webRTCommCall instanceof WebRTCommCall)
    {
        if (typeof(sipCallId) === 'string')
        {
            this.sipCallId = sipCallId;
        }
        else
        {
            this.sipCallId = new String(new Date().getTime());
        }
        this.clientConnector = clientConnector;
        this.webRTCommCall = webRTCommCall;
        this.webRTCommCall.id = this.sipCallId;
        this.configuration = undefined;
        this.resetSipContext();
    }
    else
    {
        throw "PrivateJainSipCallConnector:PrivateJainSipCallConnector(): bad arguments"
    }
};

/**
 * SIP Call Control state machine constant
 * @private
 * @constant
 */
PrivateJainSipCallConnector.prototype.SIP_INVITING_INITIAL_STATE = "INVITING_INITIAL_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITING_STATE = "INVITING_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITING_407_STATE = "INVITING_407_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITING_ACCEPTED_STATE = "INVITING_ACCEPTED_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITING_LOCAL_HANGINGUP_STATE = "INVITING_LOCAL_HANGINGUP_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITING_LOCAL_HANGINGUP_407_STATE = "INVITING_LOCAL_HANGINGUP_407_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITING_CANCELLING_STATE = "INVITING_CANCELLING_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITING_ERROR_STATE = "INVITING_ERROR_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITING_HANGUP_STATE = "INVITING_HANGUP_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITING_CANCELLED_STATE = "SIP_INVITING_CANCELLED_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITED_INITIAL_STATE = "INVITED_INITIAL_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITED_ACCEPTED_STATE = "INVITED_ACCEPTED_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITED_LOCAL_HANGINGUP_STATE = "INVITED_LOCAL_HANGINGUP_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITED_LOCAL_HANGINGUP_407_STATE = "INVITED_LOCAL_HANGINGUP_407_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITED_HANGUP_STATE = "INVITED_HANGUP_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITED_ERROR_STATE = "INVITED_ERROR_STATE";
PrivateJainSipCallConnector.prototype.SIP_INVITED_CANCELLED_STATE = "INVITING_HANGUP_STATE";

/**
 * Get SIP communication opened/closed status 
 * @public
 * @returns {boolean} true if opened, false if closed
 */
PrivateJainSipCallConnector.prototype.isOpened = function() {
    return ((this.sipCallState === this.SIP_INVITING_ACCEPTED_STATE) || (this.sipCallState === this.SIP_INVITED_ACCEPTED_STATE));
};

/**
 * Get SIP call ID
 * @public
 * @returns {string} SIP Call ID  
 */
PrivateJainSipCallConnector.prototype.getId = function() {
    return this.sipCallId;
};

/**
 * Open JAIN SIP call/communication, asynchronous action,  opened or error event is notified to WebRtcClientCall eventListener
 * @public 
 * @param {object} configuration  WebRTC communication configuration 
 * <p> Communication configuration sample: <br>
 * { <br>
 * <span style="margin-left: 30px">displayName:alice,<br></span>
 * <span style="margin-left: 30px">localMediaStream: [LocalMediaStream],<br></span>
 * <span style="margin-left: 30px">audioMediaFlag:true,<br></span>
 * <span style="margin-left: 30px">videoMediaFlag:false,<br></span>
 * <span style="margin-left: 30px">dataMediaFlag:false,<br></span>
 * <span style="margin-left: 30px">audioCodecsFilter:PCMA,PCMU,OPUS,<br></span>
 * <span style="margin-left: 30px">videoCodecsFilter:VP8,H264,<br></span>
 * }<br>
 * </p>
 * @public  
 * @throw {String} Exception "bad configuration, missing parameter"
 * @throw {String} Exception "bad state, unauthorized action"
 */
PrivateJainSipCallConnector.prototype.open = function(configuration) {
    console.debug("PrivateJainSipCallConnector:open()");
    if (this.sipCallState === undefined)
    {
        if (typeof(configuration) === 'object')
        {
            // Calling
            if (this.checkConfiguration(configuration) === true)
            {
                this.sipCallState = this.SIP_INVITING_INITIAL_STATE;
                this.configuration = configuration;
            }
            else
            {
                console.error("PrivateJainSipCallConnector:open(): bad configuration");
                throw "PrivateJainSipCallConnector:open(): bad configuration";
            }
        }
        else
        {
            // Called
            this.sipCallState = this.SIP_INVITED_INITIAL_STATE;
        }
    }
    else
    {
        console.error("PrivateJainSipCallConnector:open(): bad state, unauthorized action");
        throw "PrivateJainSipCallConnector:open(): bad state, unauthorized action";
    }
};

/**
 * Close JAIN SIP communication, asynchronous action, closed event are notified to the WebRTCommClient eventListener
 * @public 
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception "internal error,check console logs"
 */
PrivateJainSipCallConnector.prototype.close = function() {
    console.debug("PrivateJainSipCallConnector:close(): this.sipCallState=" + this.sipCallState);
    if (this.sipCallState !== undefined)
    {
        try
        {
            if (this.sipCallState === this.SIP_INITIAL_INVITING_STATE)
            {
                // SIP INVITE has not been sent yet.
                this.resetSipContext();
                this.clientConnector.removeSessionConnector(this.sipCallId);
                // Notify closed event
                this.webRTCommCall.onPrivateCallConnectorCallClosedEvent();
            }
            else if (this.sipCallState === this.SIP_INVITING_STATE || this.sipCallState === this.SIP_INVITING_407_STATE)
            {
                // SIP INIVTE has been sent, need to cancel it
                this.jainSipInvitingCancelRequest = this.jainSipInvitingTransaction.createCancel();
                this.jainSipInvitingCancelRequest.addHeader(this.clientConnector.jainSipContactHeader);
                this.jainSipInvitingCancelTransaction = this.clientConnector.jainSipProvider.getNewClientTransaction(this.jainSipInvitingCancelRequest);
                this.jainSipInvitingCancelTransaction.sendRequest();
                this.sipCallState = this.SIP_INVITING_CANCELLING_STATE;
            }
            else if (this.sipCallState === this.SIP_INVITING_ACCEPTED_STATE)
            {
                // Sent SIP BYE
                var jainSipByeRequest = this.jainSipInvitingDialog.createRequest("BYE");
                jainSipByeRequest.removeHeader("Contact");
                jainSipByeRequest.removeHeader("User-Agent");
                jainSipByeRequest.addHeader(this.clientConnector.jainSipContactHeader);
                var clientTransaction = this.clientConnector.jainSipProvider.getNewClientTransaction(jainSipByeRequest);
                this.jainSipInvitingDialog.sendRequest(clientTransaction);
                this.sipCallState = this.SIP_INVITING_LOCAL_HANGINGUP_STATE;
                // Notify closed event
                this.webRTCommCall.onPrivateCallConnectorCallClosedEvent();
            }
            else if (this.sipCallState === this.SIP_INVITED_INITIAL_STATE)
            {
                // Rejected  480 Temporarily Unavailable
                var jainSipResponse480 = this.jainSipInvitedRequest.createResponse(480, "Temporarily Unavailable");
                jainSipResponse480.addHeader(this.clientConnector.jainSipContactHeader);
                this.jainSipInvitedTransaction.sendResponse(jainSipResponse480);
                this.resetSipContext();
                this.clientConnector.removeSessionConnector(this.sipCallId);
            }
            else if (this.sipCallState === this.SIP_INVITED_ACCEPTED_STATE)
            {
                // Sent SIP BYE
                var jainSipByeRequest = this.jainSipInvitedDialog.createRequest("BYE");
                jainSipByeRequest.removeHeader("Contact");
                jainSipByeRequest.removeHeader("User-Agent");
                jainSipByeRequest.addHeader(this.clientConnector.jainSipContactHeader);
                var clientTransaction = this.clientConnector.jainSipProvider.getNewClientTransaction(jainSipByeRequest);
                this.jainSipInvitedDialog.sendRequest(clientTransaction);
                this.sipCallState = this.SIP_INVITED_LOCAL_HANGINGUP_STATE;
            }
            else
            {
                this.resetSipContext();
                this.clientConnector.removeSessionConnector(this.sipCallId);
                // Notify closed event
                this.webRTCommCall.onPrivateCallConnectorCallClosedEvent();
            }
        }
        catch (exception)
        {
            console.error("PrivateJainSipCallConnector:close(): catched exception:" + exception);
            this.resetSipContext();
            this.clientConnector.removeSessionConnector(this.sipCallId);
            // Notify closed event
            this.webRTCommCall.onPrivateCallConnectorCallClosedEvent();
        }
    }
};

/**
 * Process reject of the SIP incoming communication
 * @public 
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception "internal error,check console logs"
 */
PrivateJainSipCallConnector.prototype.reject = function() {
    console.debug("PrivateJainSipCallConnector:reject()");
    if (this.sipCallState === this.SIP_INVITED_INITIAL_STATE)
    {
        try
        {
            // Rejected  Temporarily Unavailable
            var jainSipResponse486 = this.jainSipInvitedRequest.createResponse(486, "Busy here");
            jainSipResponse486.addHeader(this.clientConnector.jainSipContactHeader);
            this.jainSipInvitedTransaction.sendResponse(jainSipResponse486);
        }
        catch (exception)
        {
            console.error("PrivateJainSipCallConnector:reject(): catched exception:" + exception);
        }
        this.close();
    }
    else
    {
        console.error("PrivateJainSipCallConnector:reject(): bad state, unauthorized action");
        throw "PrivateJainSipCallConnector:reject(): bad state, unauthorized action";
    }
};

/**
 * Check configuration 
 * @param {object} configuration SIP call configuration JSON object
 * @private
 * @return true configuration ok false otherwise
 */
PrivateJainSipCallConnector.prototype.checkConfiguration = function(configuration) {
    console.debug("PrivateJainSipCallConnector:checkConfiguration()");
    var check = true;
    return check;
};

/**
 * Reset SIP context 
 * @private
 */
PrivateJainSipCallConnector.prototype.resetSipContext = function() {
    console.debug("PrivateJainSipCallConnector:resetSipContext()");
    this.sipCallState = undefined;
    this.sdpOffer = undefined;
    this.jainSipInvitingSentRequest = undefined;
    this.jainSipInvitingDialog = undefined;
    this.jainSipInvitingTransaction = undefined;
    this.jainSipInvitedReceivedRequest = undefined;
    this.jainSipInvitedDialog = undefined;
    this.jainSipInvitedTransaction = undefined;
};

/**
 * Process invitation of outgoing SIP communication 
 * @public 
 * @param {String} sdpOffer SDP offer received from RTCPeerConenction
 */
PrivateJainSipCallConnector.prototype.invite = function(sdpOffer) {
    console.debug("PrivateJainSipCallConnector:invite()");
    this.sdpOffer = sdpOffer;
    this.sendSipInviteRequest(sdpOffer);
    this.sipCallState = this.SIP_INVITING_STATE;
};


/**
 * Process acceptation of incoming SIP communication
 * @public 
 * @param {string} sdpAnswer SDP answer received from RTCPeerConnection
 */
PrivateJainSipCallConnector.prototype.accept = function(sdpAnswer) {
    console.debug("PrivateJainSipCallConnector:accept()");
    // Send 200 OK
    var jainSip200OKResponse = this.jainSipInvitedRequest.createResponse(200, "OK");
    jainSip200OKResponse.addHeader(this.clientConnector.jainSipContactHeader);
    jainSip200OKResponse.setMessageContent("application", "sdp", sdpAnswer);
    this.jainSipInvitedTransaction.sendResponse(jainSip200OKResponse);
    this.sipCallState = this.SIP_INVITED_ACCEPTED_STATE;
};


/**
 * PrivateJainSipClientConnector interface implementation: handle SIP Request event
 * @public 
 * @param {RequestEvent} requestEvent 
 */
PrivateJainSipCallConnector.prototype.onJainSipClientConnectorSipRequestEvent = function(requestEvent) {
    console.debug("PrivateJainSipCallConnector:onJainSipClientConnectorSipRequestEvent()");
    if (this.jainSipInvitingDialog !== undefined)
        this.processInvitingSipRequestEvent(requestEvent);
    else if (this.jainSipInvitedDialog !== undefined)
        this.processInvitedSipRequestEvent(requestEvent);
    else
    {
        this.processInvitedSipRequestEvent(requestEvent);
    }
};

/**
 * PrivateJainSipClientConnector interface implementation: handle SIP response event
 * @public 
 * @param {ResponseEvent} responseEvent 
 */
PrivateJainSipCallConnector.prototype.onJainSipClientConnectorSipResponseEvent = function(responseEvent) {
    console.debug("PrivateJainSipCallConnector:onJainSipClientConnectorSipResponseEvent()");
    if (this.jainSipInvitingDialog !== undefined)
        this.processInvitingSipResponseEvent(responseEvent);
    else if (this.jainSipInvitedDialog !== undefined)
        this.processInvitedSipResponseEvent(responseEvent);
    else
    {
        console.warn("PrivateJainSipCallConnector:onJainSipClientConnectorSipResponseEvent(): response ignored");
    }
};

/**
 * PrivateJainSipClientConnector interface implementation: handle SIP timeout event
 * @public 
 * @param {TimeoutEvent} timeoutEvent
 */
PrivateJainSipCallConnector.prototype.onJainSipClientConnectorSipTimeoutEvent = function(timeoutEvent) {
    console.debug("PrivateJainSipCallConnector:onJainSipClientConnectorSipTimeoutEvent()");
    // For the time being force close of the call 
    this.close();
};


/**
 * Handle SIP request event for inviting call
 * @private 
 * @param {RequestEvent} requestEvent 
 */
PrivateJainSipCallConnector.prototype.processInvitingSipRequestEvent = function(requestEvent) {
    console.debug("PrivateJainSipCallConnector:processInvitingSipRequestEvent(): this.sipCallState=" + this.sipCallState);
    var jainSipRequest = requestEvent.getRequest();
    var requestMethod = jainSipRequest.getMethod();
    if (this.sipCallState === this.SIP_INVITING_INITIAL_STATE)
    {
        console.error("PrivateJainSipCallConnector:processInvitingSipRequestEvent(): bad state, SIP request ignored");
    }
    else if (this.sipCallState === this.SIP_INVITING_STATE)
    {
        console.error("PrivateJainSipCallConnector:processInvitingSipRequestEvent(): bad state, SIP request ignored");
    }
    else if (this.sipCallState === this.SIP_INVITING_407_STATE)
    {
        console.error("PrivateJainSipCallConnector:processInvitingSipRequestEvent(): bad state, SIP request ignored");
    }
    else if (this.sipCallState === this.SIP_INVITING_ERROR_STATE)
    {
        console.error("PrivateJainSipCallConnector:processInvitingSipRequestEvent(): bad state, SIP request ignored");
    }
    else if (this.sipCallState === this.SIP_INVITING_ACCEPTED_STATE)
    {
        if (requestMethod === "BYE")
        {
            try
            {
                // Sent 200 OK BYE
                var jainSip200OKResponse = jainSipRequest.createResponse(200, "OK");
                jainSip200OKResponse.addHeader(this.clientConnector.jainSipContactHeader);
                requestEvent.getServerTransaction().sendResponse(jainSip200OKResponse);
                // Update SIP call state
                this.sipCallState = this.SIP_INVITING_HANGUP_STATE;
            }
            catch (exception)
            {
                console.error("PrivateJainSipCallConnector:processInvitingSipRequestEvent(): catched exception exception:" + exception);
            }

            // Notify the hangup event
            this.webRTCommCall.onPrivateCallConnectorCallHangupEvent();

            // Close the call
            this.close();
        }
        else
        {
            console.error("PrivateJainSipCallConnector:processInvitingSipRequestEvent(): bad state, SIP request ignored");
        }
    }
    else
    {
        console.error("PrivateJainSipCallConnector:processInvitingSipRequestEvent(): bad state, SIP request ignored");
    }
};

/**
 * Send SIP INVITE request
 * @private 
 */
PrivateJainSipCallConnector.prototype.sendSipInviteRequest = function() {
    console.debug("PrivateJainSipCallConnector:sendSipInviteRequest()");
    // Send INVITE 
    var calleeSipUri = this.webRTCommCall.getCalleePhoneNumber();
    if (calleeSipUri.indexOf("@") === -1)
    {
        //No domain, add caller one 
        calleeSipUri += "@" + this.clientConnector.configuration.sipDomain;
    }
    var fromSipUriString = this.clientConnector.configuration.sipUserName + "@" + this.clientConnector.configuration.sipDomain;
    var jainSipCseqHeader = this.clientConnector.jainSipHeaderFactory.createCSeqHeader(1, "INVITE");
    var jainSipCallIdHeader = this.clientConnector.jainSipHeaderFactory.createCallIdHeader(this.sipCallId);
    var jainSipMaxForwardHeader = this.clientConnector.jainSipHeaderFactory.createMaxForwardsHeader(70);
    var jainSipRequestUri = this.clientConnector.jainSipAddressFactory.createSipURI_user_host(null, calleeSipUri);
    var jainSipAllowListHeader = this.clientConnector.jainSipHeaderFactory.createHeaders("Allow: INVITE,ACK,CANCEL,BYE");
    var jainSipFromUri = this.clientConnector.jainSipAddressFactory.createSipURI_user_host(null, fromSipUriString);
    var jainSipFromAdress = this.clientConnector.jainSipAddressFactory.createAddress_name_uri(this.configuration.displayName, jainSipFromUri);
    // Setup display name
    if (this.configuration.displayName)
    {
        jainSipFromAdress.setDisplayName(this.configuration.displayName);
    }
    else if (this.clientConnector.configuration.sipDisplayName)
    {
        jainSipFromAdress.setDisplayName(this.clientConnector.configuration.sipDisplayName);
    }
    var tagFrom = new Date().getTime();
    var jainSipFromHeader = this.clientConnector.jainSipHeaderFactory.createFromHeader(jainSipFromAdress, tagFrom);
    var jainSiptoUri = this.clientConnector.jainSipAddressFactory.createSipURI_user_host(null, calleeSipUri);
    var jainSipToAddress = this.clientConnector.jainSipAddressFactory.createAddress_name_uri(null, jainSiptoUri);
    var jainSipToHeader = this.clientConnector.jainSipHeaderFactory.createToHeader(jainSipToAddress, null);
    var jainSipViaHeader = this.clientConnector.jainSipListeningPoint.getViaHeader();
    var jainSipContentTypeHeader = this.clientConnector.jainSipHeaderFactory.createContentTypeHeader("application", "sdp");
    this.jainSipInvitingRequest = this.clientConnector.jainSipMessageFactory.createRequest(jainSipRequestUri, "INVITE",
            jainSipCallIdHeader,
            jainSipCseqHeader,
            jainSipFromHeader,
            jainSipToHeader,
            jainSipViaHeader,
            jainSipMaxForwardHeader,
            jainSipContentTypeHeader,
            this.sdpOffer);

    this.clientConnector.jainSipMessageFactory.addHeader(this.jainSipInvitingRequest, jainSipAllowListHeader);
    this.clientConnector.jainSipMessageFactory.addHeader(this.jainSipInvitingRequest, this.clientConnector.jainSipContactHeader);
    this.jainSipInvitingTransaction = this.clientConnector.jainSipProvider.getNewClientTransaction(this.jainSipInvitingRequest);
    this.jainSipInvitingRequest.setTransaction(this.jainSipInvitingTransaction);
    this.jainSipInvitingDialog = this.jainSipInvitingTransaction.getDialog();
    this.jainSipInvitingTransaction.sendRequest();
};

/**
 * Send SIP INVITE request
 * @private 
 * @param {AuthorizationHeader} jainSipAuthorizationHeader Authorization Header
 */
PrivateJainSipCallConnector.prototype.sendAuthenticatedSipInviteRequest = function(jainSipAuthorizationHeader) {
    console.debug("PrivateJainSipCallConnector:sendAuthenticatedSipInviteRequest()");
    this.jainSipInvitingRequest.removeHeader("Authorization");
    var newJainSipInvitingRequest = new SIPRequest();
    newJainSipInvitingRequest.setMethod(this.jainSipInvitingRequest.getMethod());
    newJainSipInvitingRequest.setRequestURI(this.jainSipInvitingRequest.getRequestURI());
    var headerList = this.jainSipInvitingRequest.getHeaders();
    for (var i = 0; i < headerList.length; i++)
    {
        newJainSipInvitingRequest.addHeader(headerList[i]);
    }

    var num = new Number(this.jainSipInvitingRequest.getCSeq().getSeqNumber());
    newJainSipInvitingRequest.getCSeq().setSeqNumber(num + 1);
    newJainSipInvitingRequest.setCallId(this.jainSipInvitingRequest.getCallId());
    newJainSipInvitingRequest.setVia(this.clientConnector.jainSipListeningPoint.getViaHeader());
    newJainSipInvitingRequest.setFrom(this.jainSipInvitingRequest.getFrom());
    newJainSipInvitingRequest.setTo(this.jainSipInvitingRequest.getTo());
    newJainSipInvitingRequest.setMaxForwards(this.jainSipInvitingRequest.getMaxForwards());

    if (this.jainSipInvitingRequest.getContent() !== null)
    {
        var content = this.jainSipInvitingRequest.getContent();
        var contentType = this.jainSipInvitingRequest.getContentTypeHeader();
        newJainSipInvitingRequest.setContent(content, contentType);
    }
    this.jainSipInvitingRequest = newJainSipInvitingRequest;
    this.clientConnector.jainSipMessageFactory.addHeader(this.jainSipInvitingRequest, jainSipAuthorizationHeader);
    this.jainSipInvitingTransaction = this.clientConnector.jainSipProvider.getNewClientTransaction(this.jainSipInvitingRequest);
    this.jainSipInvitingRequest.setTransaction(this.jainSipInvitingransaction);
    this.jainSipInvitingTransaction.sendRequest();
};

/**
 * Handle SIP response event for inviting call
 * @private 
 * @param {ResponseEvent} responseEvent 
 */
PrivateJainSipCallConnector.prototype.processInvitingSipResponseEvent = function(responseEvent) {
    console.debug("PrivateJainSipCallConnector:processInvitingSipResponseEvent(): this.sipCallState=" + this.sipCallState);
    var jainSipResponse = responseEvent.getResponse();
    var statusCode = parseInt(jainSipResponse.getStatusCode());
    if (this.sipCallState === this.SIP_INVITING_STATE)
    {
        if (statusCode < 200)
        {
            if (statusCode === 180)
            {
                // Notify the ringing back event
                this.webRTCommCall.onPrivateCallConnectorCallRingingBackEvent();
            }
            else if (statusCode === 183)
            {
                // Notify asynchronously the in progress event
                this.webRTCommCall.onPrivateCallConnectorCallInProgressEvent();
            }
            console.debug("PrivateJainSipCallConnector:processInvitingSipResponseEvent(): 1XX response ignored");
        }
        else if (statusCode === 407)
        {
            // Send Authenticated SIP INVITE
            var jainSipAuthorizationHeader = this.clientConnector.jainSipHeaderFactory.createAuthorizationHeader(jainSipResponse, this.jainSipInvitingRequest, this.clientConnector.configuration.sipPassword, this.clientConnector.configuration.sipLogin);
            this.sendAuthenticatedSipInviteRequest(jainSipAuthorizationHeader);
            // Update SIP call state            
            this.sipCallState = this.SIP_INVITING_407_STATE;
        }
        else if (statusCode === 200)
        {
            this.jainSipInvitingDialog = responseEvent.getOriginalTransaction().getDialog();
            try
            {
                // Send SIP 200 OK ACK
                this.jainSipInvitingDialog.setRemoteTarget(jainSipResponse.getHeader("Contact"));
                var jainSipMessageACK = this.jainSipInvitingTransaction.createAck();
                jainSipMessageACK.addHeader(this.clientConnector.jainSipContactHeader);
                this.jainSipInvitingDialog.sendAck(jainSipMessageACK);
                // Update SIP call state    
                this.sipCallState = this.SIP_INVITING_ACCEPTED_STATE;
            }
            catch (exception)
            {
                console.error("PrivateJainSipCallConnector:processInvitingSipRequestEvent(): catched exception, exception:" + exception);
            }

            try
            {
                var sdpAnswerString = jainSipResponse.getContent();
                this.webRTCommCall.onPrivateCallConnectorRemoteSdpAnswerEvent(sdpAnswerString);
            }
            catch (exception)
            {
                console.error("PrivateJainSipCallConnector:processInvitingSipRequestEvent(): catched exception, exception:" + exception);

                // Notify the error event
                this.webRTCommCall.onPrivateCallConnectorCallOpenErrorEvent(exception);

                // Close the call
                this.close();
            }
        }
        else
        {
            console.error("PrivateJainSipCallConnector:processInvitingSipRequestEvent(): SIP INVITE failed:" + jainSipResponse.getStatusCode() + "  " + jainSipResponse.getStatusLine().toString());
            // Update SIP call state    
            this.sipCallState = this.SIP_INVITING_ERROR_STATE;
            // Notify asynchronously the error event
            this.webRTCommCall.onPrivateCallConnectorCallOpenErrorEvent(jainSipResponse.getStatusLine().getReasonPhrase());

            this.close();
        }
    }
    else if (this.sipCallState === this.SIP_INVITING_CANCELLING_STATE)
    {
        // Update SIP call state    
        this.sipCallState = this.SIP_INVITING_CANCELLED_STATE;
        this.close();
    }
    else if (this.sipCallState === this.SIP_INVITING_407_STATE)
    {
        if (statusCode < 200)
        {
            console.debug("PrivateJainSipCallConnector:processInvitingSipResponseEvent(): 1XX response ignored");
        }
        else if (statusCode === 200)
        {
            this.jainSipInvitingDialog = responseEvent.getOriginalTransaction().getDialog();

            try
            {
                // Send SIP 200 OK ACK
                this.jainSipInvitingDialog.setRemoteTarget(jainSipResponse.getHeader("Contact"));
                var jainSipMessageACK = this.jainSipInvitingTransaction.createAck();
                jainSipMessageACK.addHeader(this.clientConnector.jainSipContactHeader);
                this.jainSipInvitingDialog.sendAck(jainSipMessageACK);
                // Update SIP call state
                this.sipCallState = this.SIP_INVITING_ACCEPTED_STATE;
            }
            catch (exception)
            {
                console.error("PrivateJainSipCallConnector:processInvitingSipRequestEvent(): catched exception, exception:" + exception);
            }


            try
            {
                var sdpAnswerString = jainSipResponse.getContent();
                this.webRTCommCall.onPrivateCallConnectorRemoteSdpAnswerEvent(sdpAnswerString);
            }
            catch (exception)
            {
                console.error("PrivateJainSipCallConnector:processInvitingSipRequestEvent(): catched exception, exception:" + exception);

                // Notify the error event
                this.webRTCommCall.onPrivateCallConnectorCallOpenErrorEvent(exception);

                // Close the call
                this.close();
            }
        }
        else
        {
            // Update SIP call state
            this.sipCallState = this.SIP_INVITING_ERROR_STATE;

            // Notify the error event
            this.webRTCommCall.onPrivateCallConnectorCallOpenErrorEvent(jainSipResponse.getStatusLine().getReasonPhrase());

            // Close the call
            this.close();
        }
    }
    else if (this.sipCallState === this.SIP_INVITING_ERROR_STATE)
    {
        console.error("PrivateJainSipCallConnector:processInvitingSipResponseEvent(): bad state, SIP response ignored");
    }
    else if (this.sipCallState === this.SIP_INVITING_ACCEPTED_STATE)
    {
        console.error("PrivateJainSipCallConnector:processInvitingSipResponseEvent(): bad state, SIP response ignored");
    }
    else if (this.sipCallState === this.SIP_INVITING_LOCAL_HANGINGUP_STATE)
    {
        if (statusCode === 407)
        {
            try
            {
                // Send Authenticated BYE request
                var jainSipByeRequest = this.jainSipInvitingDialog.createRequest("BYE");
                var clientTransaction = this.clientConnector.jainSipProvider.getNewClientTransaction(jainSipByeRequest);
                var jainSipAuthorizationHeader = this.clientConnector.jainSipHeaderFactory.createAuthorizationHeader(jainSipResponse, jainSipByeRequest, this.clientConnector.configuration.sipPassword, this.clientConnector.configuration.sipLogin);
                this.clientConnector.jainSipMessageFactory.addHeader(jainSipByeRequest, jainSipAuthorizationHeader);
                this.jainSipInvitingDialog.sendRequest(clientTransaction);
                // Update SIP call state
                this.sipCallState = this.SIP_INVITING_HANGINGUP_407_STATE;
            }
            catch (exception)
            {
                console.error("PrivateJainSipCallConnector:processInvitingSipRequestEvent(): catched exception, exception:" + exception);
                this.close();
            }
        }
        else
        {
            // Force close
            this.close();
        }
    }
    else if (this.sipCallState === this.SIP_INVITING_LOCAL_HANGINGUP_407_STATE)
    {
        // Force close
        this.close();
    }
    else
    {
        console.error("PrivateJainSipCallConnector:processInvitingSipResponseEvent(): bad state, SIP response ignored");
    }
};

/**
 * Handle SIP request event for invited call
 * @private 
 * @param {RequestEvent} requestEvent request event
 */
PrivateJainSipCallConnector.prototype.processInvitedSipRequestEvent = function(requestEvent) {
    console.debug("PrivateJainSipCallConnector:processInvitedSipRequestEvent(): this.sipCallState=" + this.sipCallState);
    var jainSipRequest = requestEvent.getRequest();
    var requestMethod = jainSipRequest.getMethod();
    var headerFrom = jainSipRequest.getHeader("From");
    if (this.sipCallState === this.SIP_INVITED_INITIAL_STATE)
    {
        if (requestMethod === "INVITE")
        {
            try
            {
                // Store SIP context
                this.jainSipInvitedRequest = jainSipRequest;
                this.jainSipInvitedTransaction = requestEvent.getServerTransaction();
                this.jainSipInvitedDialog = requestEvent.getServerTransaction().getDialog();

                // Ringing
                var jainSip180ORingingResponse = jainSipRequest.createResponse(180, "Ringing");
                jainSip180ORingingResponse.addHeader(this.clientConnector.jainSipContactHeader);
                requestEvent.getServerTransaction().sendResponse(jainSip180ORingingResponse);
            }
            catch (exception)
            {
                console.error("PrivateJainSipCallConnector:processInvitedSipRequestEvent(): catched exception, exception:" + exception);
            }

            //  Notify remote SDP offer to WebRTCommCall
            this.webRTCommCall.onPrivateCallConnectorRemoteSdpOfferEvent(this.jainSipInvitedRequest.getContent());

            // Notify incoming communication
            var callerPhoneNumber = headerFrom.getAddress().getURI().getUser();
            var callerDisplayName = headerFrom.getAddress().getDisplayName();
            this.webRTCommCall.onPrivateCallConnectorCallRingingEvent(callerPhoneNumber, callerDisplayName);
        }
        else if (requestMethod === "CANCEL")
        {
            try
            {
                // Send 200OK CANCEL
                var jainSip200OKResponse = jainSipRequest.createResponse(200, "OK");
                jainSip200OKResponse.addHeader(this.clientConnector.jainSipContactHeader);
                requestEvent.getServerTransaction().sendResponse(jainSip200OKResponse);

                // Send 487 (Request Cancelled) for the INVITE
                var jainSipResponse487 = this.jainSipInvitedRequest.createResponse(487, "(Request Cancelled)");
                this.jainSipInvitedTransaction.sendMessage(jainSipResponse487);

                // Update SIP call state
                this.sipCallState = this.SIP_INVITED_CANCELLED_STATE;
            }
            catch (exception)
            {
                console.error("PrivateJainSipCallConnector:processInvitedSipRequestEvent(): catched exception, exception:" + exception);
            }

            // Notify asynchronously the hangup event
            this.webRTCommCall.onPrivateCallConnectorCallHangupEvent();

            // Close the call
            this.close();
        }
        else
        {
            console.error("PrivateJainSipCallConnector:processInvitedSipRequestEvent(): bad state, SIP request ignored");
        }
    }
    else if (this.sipCallState === this.SIP_INVITED_ACCEPTED_STATE)
    {
        if (requestMethod === "BYE")
        {
            try
            {
                // Send 200OK
                var jainSip200OKResponse = jainSipRequest.createResponse(200, "OK");
                jainSip200OKResponse.addHeader(this.clientConnector.jainSipContactHeader);
                requestEvent.getServerTransaction().sendResponse(jainSip200OKResponse);

                // Update SIP call state
                this.sipCallState = this.SIP_INVITED_HANGUP_STATE;
            }
            catch (exception)
            {
                console.error("PrivateJainSipCallConnector:processInvitedSipRequestEvent(): catched exception exception:" + exception);
            }

            // Notify asynchronously the hangup event
            this.webRTCommCall.onPrivateCallConnectorCallHangupEvent();

            // Close the call
            this.close();
        }
        else if (requestMethod === "ACK")
        {
            this.jainSipInvitedDialog = requestEvent.getServerTransaction().getDialog();
        }
        else
        {
            console.error("PrivateJainSipCallConnector:processInvitedSipRequestEvent(): bad state, SIP request ignored");
        }
    }
    else if (this.sipCallState === this.SIP_INVITED_LOCAL_HANGINGUP_STATE)
    {
        console.error("PrivateJainSipCallConnector:processInvitedSipRequestEvent(): bad state, SIP request ignored");
    }
    else if (this.sipCallState === this.SIP_INVITED_LOCAL_HANGINGUP_407_STATE)
    {
        console.error("PrivateJainSipCallConnector:processInvitedSipRequestEvent(): bad state, SIP request ignored");
    }
};

/**
 * Handle SIP response event for invited call
 * @private 
 * @param {ResponseEvent} responseEvent response event
 */
PrivateJainSipCallConnector.prototype.processInvitedSipResponseEvent = function(responseEvent) {
    console.debug("PrivateJainSipCallConnector:processInvitedSipResponseEvent(): this.invitingState=" + this.invitingState);
    var jainSipResponse = responseEvent.getResponse();
    var statusCode = parseInt(jainSipResponse.getStatusCode());
    if (this.sipCallState === this.SIP_INVITED_STATE)
    {
        console.error("PrivateJainSipCallConnector:processInvitedSipResponseEvent(): bad state, SIP response ignored");
    }
    else if (this.sipCallState === this.SIP_INVITED_ACCEPTED_STATE)
    {
        console.error("PrivateJainSipCallConnector:processInvitedSipResponseEvent(): bad state, SIP response ignored");
    }
    else if (this.sipCallState === this.SIP_INVITED_LOCAL_HANGINGUP_STATE)
    {
        if (statusCode === 407)
        {
            try
            {
                // Send Authenticated BYE request
                var jainSipByeRequest = this.jainSipInvitedDialog.createRequest("BYE");
                var clientTransaction = this.jainSipProvider.getNewClientTransaction(jainSipByeRequest);
                var jainSipAuthorizationHeader = this.jainSipHeaderFactory.createAuthorizationHeader(jainSipResponse, jainSipByeRequest, this.configuration.sipPassword, this.configuration.sipLogin);
                this.jainSipMessageFactory.addHeader(jainSipByeRequest, jainSipAuthorizationHeader);
                jainSipByeRequest.addHeader(this.clientConnector.jainSipContactHeader);
                this.jainSipInvitedDialog.sendRequest(clientTransaction);

                // Update SIP call state
                this.sipCallState = this.SIP_INVITED_HANGINGUP_407_STATE;
            }
            catch (exception)
            {
                console.error("PrivateJainSipCallConnector:processInvitedSipResponseEvent(): catched exception, exception:" + exception);
                this.close();
            }
        }
        else
        {
            this.close();
        }
    }
    else if (this.sipCallState === this.SIP_INVITED_LOCAL_HANGINGUP_407_STATE)
    {
        // Force close
        this.close();
    }
    else
    {
        console.error("PrivateJainSipCallConnector:processInvitedSipResponseEvent(): bad state, SIP request ignored");
    }
};







/**
 * @class PrivateJainSipClientConnector
 * @classdesc Private framework class handling  SIP client/user agent control 
 * @constructor 
 * @private
 * @param {WebRTCommClient} webRTCommClient "connected" WebRTCommClient object 
 * @throw {String} Exception "bad argument"
 * @author Laurent STRULLU (laurent.strullu@orange.com) 
 */
PrivateJainSipClientConnector = function(webRTCommClient)
{
    console.debug("PrivateJainSipClientConnector:PrivateJainSipClientConnector()");
    if (webRTCommClient instanceof WebRTCommClient)
    {
        this.webRTCommClient = webRTCommClient;
        this.reset();
    }
    else
    {
        throw "PrivateJainSipClientConnector:PrivateJainSipClientConnector(): bad arguments"
    }
};

// Private webRtc class variable
PrivateJainSipClientConnector.prototype.SIP_ALLOW_HEADER = "Allow: INVITE,ACK,CANCEL,BYE,OPTIONS,MESSAGE";

//  State of SIP REGISTER state machine
PrivateJainSipClientConnector.prototype.SIP_UNREGISTERED_STATE = "SIP_UNREGISTERED_STATE";
PrivateJainSipClientConnector.prototype.SIP_REGISTERING_STATE = "SIP_REGISTERING_STATE";
PrivateJainSipClientConnector.prototype.SIP_REGISTER_REFRESHING_STATE = "SIP_REGISTER_REFRESHING_STATE";
PrivateJainSipClientConnector.prototype.SIP_REGISTERING_401_STATE = "SIP_REGISTERING_401_STATE";
PrivateJainSipClientConnector.prototype.SIP_REGISTERED_STATE = "SIP_REGISTERED_STATE";
PrivateJainSipClientConnector.prototype.SIP_UNREGISTERING_401_STATE = "SIP_UNREGISTERING_401_STATE";
PrivateJainSipClientConnector.prototype.SIP_UNREGISTERING_STATE = "SIP_UNREGISTERING_STATE";
PrivateJainSipClientConnector.prototype.SIP_SESSION_EXPIRATION_TIMER = 3600;

/**
 * Get SIP client/user agent opened/closed status 
 * @public
 * @returns {boolean} true if opened, false if closed
 */
PrivateJainSipClientConnector.prototype.isOpened = function() {
    return this.openedFlag;
};


/**
 * Open SIP client/user agent, asynchronous action, opened or error event is notified to WebRtcClientComm
 * @public 
 * @param {object} configuration   SIP client/user agent configuration <br>
 * <p> Client configuration sample: <br>
 * { <br>
 * <span style="margin-left: 60px">sipUriContactParameters:undefined,<br></span>
 * <span style="margin-left: 30px">sipUserAgent:"WebRtcCommTestWebApp/0.0.1",<br></span>
 * <span style="margin-left: 30px">sipUserAgentCapabilities:"+g.oma.sip-im",<br></span>
 * <span style="margin-left: 30px">sipOutboundProxy:"ws://localhost:5082",<br></span>
 * <span style="margin-left: 30px">sipDomain:"sip.net",<br></span>
 * <span style="margin-left: 30px">sipUserName:"alice",<br></span>
 * <span style="margin-left: 30px">sipLogin:"alice@sip.net,<br></span>
 * <span style="margin-left: 30px">sipPassword:"1234567890",<br></span>
 * <span style="margin-left: 30px">sipRegisterMode:true,<br></span>
 * }<br>
 *  </p>
 * @throw {String} Exception "bad argument"
 * @throw {String} Exception "bad configuration, missing parameter"
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception [internal error]
 */
PrivateJainSipClientConnector.prototype.open = function(configuration) {
    console.debug("PrivateJainSipClientConnector:open()");
    try
    {
        if (typeof(configuration) === 'object')
        {
            if (this.openedFlag === false)
            {
                if (this.checkConfiguration(configuration) === true)
                {
                    this.configuration = configuration;

                    // Create JAIN SIP main objects
                    this.jainSipFactory = new SipFactory();
                    this.jainSipStack = this.jainSipFactory.createSipStack(this.configuration.sipUserAgent);
                    this.jainSipListeningPoint = this.jainSipStack.createListeningPoint(this.configuration.sipOutboundProxy);
                    this.jainSipProvider = this.jainSipStack.createSipProvider(this.jainSipListeningPoint);
                    this.jainSipProvider.addSipListener(this);
                    this.jainSipHeaderFactory = this.jainSipFactory.createHeaderFactory();
                    this.jainSipAddressFactory = this.jainSipFactory.createAddressFactory();
                    this.jainSipMessageFactory = this.jainSipFactory.createMessageFactory();
                    this.jainSipContactHeader = this.jainSipListeningPoint.createContactHeader(this.configuration.sipUserName);
                    if (this.configuration.sipUserAgentCapabilities)
                    {
                        this.jainSipContactHeader.setParameter(this.configuration.sipUserAgentCapabilities, null);
                    }
                    if (this.configuration.sipUriContactParameters)
                    {
                        try
                        {
                            var sipUri = this.jainSipContactHeader.getAddress().getURI();
                            var parameters = this.configuration.sipUriContactParameters.split(";");
                            for (var i = 0; i < parameters.length; i++)
                            {
                                var nameValue = parameters[i].split("=");
                                sipUri.uriParms.set_nv(new NameValue(nameValue[0], nameValue[1]));
                            }
                        }
                        catch (exception)
                        {
                            console.error("PrivateJainSipClientConnector:open(): catched exception:" + exception);
                        }
                    }

                    this.jainSipMessageFactory.setDefaultUserAgentHeader(this.jainSipHeaderFactory.createUserAgentHeader(this.jainSipStack.getUserAgent()));
                    this.jainSipStack.start();
                }
                else
                {
                    console.error("PrivateJainSipClientConnector:open(): bad configuration");
                    throw "PrivateJainSipClientConnector:open(): bad configuration";
                }
            }
            else
            {
                console.error("PrivateJainSipClientConnector:open(): bad state, unauthorized action");
                throw "PrivateJainSipClientConnector:open(): bad state, unauthorized action";
            }
        }
        else
        {
            console.error("PrivateJainSipClientConnector:open(): bad argument, check API documentation");
            throw "PrivateJainSipClientConnector:open(): bad argument, check API documentation"
        }
    }
    catch (exception) {
        this.reset();
        console.error("PrivateJainSipClientConnector:open(): catched exception:" + exception);
        throw exception;
    }
};

/**
 * Close SIP client/User Agent, asynchronous action,closed event is notified to WebRtcClientComm
 * Open SIP Call/communication are closed
 * @public 
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception [internal error]
 */
PrivateJainSipClientConnector.prototype.close = function() {
    console.debug("PrivateJainSipClientConnector:close()");
    try
    {
        if (this.openedFlag === true)
        {
            //Force close of open SIP communication
            for (var sipSessionId in this.sessionConnectors) {

                var sessionConnector = this.sessionConnectors[sipSessionId];
                if (sessionConnector instanceof PrivateJainSipCallConnector)
                {
                    if (sessionConnector.isOpened())
                    {
                        sessionConnector.close();
                    }
                }
            }
            if (this.configuration.sipRegisterMode === true)
            {
                if (this.sipRegisterState === this.SIP_REGISTERED_STATE)
                {
                    this.sipUnregisterPendingFlag = false;
                    this.sipRegisterState = this.SIP_UNREGISTERING_STATE;
                    if (this.sipRegisterRefreshTimer)
                    {
                        // Cancel SIP REGISTER refresh timer
                        clearTimeout(this.sipRegisterRefreshTimer);
                    }
                    this.sendNewSipRegisterRequest(0);
                }
                else
                {
                    // Refresh SIP REGISTER ongoing, wait the end and excute SIP unregistration
                    this.sipUnregisterPendingFlag = true;
                }
            }
            else
            {
                this.reset();
                this.webRTCommClient.onPrivateClientConnectorClosedEvent();
            }
        }
        else
        {
            console.error("PrivateJainSipClientConnector:close(): bad state, unauthorized action");
            throw "PrivateJainSipClientConnector:close(): bad state, unauthorized action";
        }
    }
    catch (exception) {
        console.error("PrivateJainSipClientConnector:close(): catched exception:" + exception);
        throw exception;
    }
};


/**
 * Create new CallConnector object
 * @public 
 * @param {WebRTCommCall|WebRTCommMessage} webRTCommSession connected "object"
 * @param {string} sipSessionId SIP CALL ID
 * @throw {String} Exception "bad argument, check API documentation"
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception [internal error]
 */
PrivateJainSipClientConnector.prototype.createPrivateSessionConnector = function(webRTCommSession, sipSessionId) {
    console.debug("PrivateJainSipClientConnector:createPrivateSessionConnector()");
    try
    {
        if (this.openedFlag === true)
        {
            if (webRTCommSession instanceof WebRTCommCall)
            {
                var sessionConnector = new PrivateJainSipCallConnector(this, webRTCommSession, sipSessionId);
                console.debug("PrivateJainSipClientConnector:createPrivateSessionConnector():sessionConnector.sipCallId="+sessionConnector.sipCallId);
                this.sessionConnectors[sessionConnector.sipCallId] = sessionConnector;
                return sessionConnector;

            }
            else if (webRTCommSession instanceof WebRTCommMessage)
            {
                var sessionConnector = new PrivateJainSipMessageConnector(this, webRTCommSession, sipSessionId);
                console.debug("PrivateJainSipClientConnector:createPrivateSessionConnector():sessionConnector.sipCallId="+sessionConnector.sipCallId);
                this.sessionConnectors[sessionConnector.sipCallId] = sessionConnector;
                return sessionConnector;
            }
            else
            {
                console.error("PrivateJainSipClientConnector:createPrivateSessionConnector(): bad argument, check API documentation");
                throw "PrivateJainSipClientConnector:createPrivateSessionConnector(): bad argument, check API documentation"
            }
        }
        console.error("PrivateJainSipClientConnector:createPrivateSessionConnector(): bad state, unauthorized action");
        throw "PrivateJainSipClientConnector:createPrivateSessionConnector(): bad state, unauthorized action";
    }
    catch (exception) {
        console.error("PrivateJainSipClientConnector:createPrivateSessionConnector(): catched exception:" + exception);
        throw exception;
    }
};


/**
 * Remove a PrivateJainSipClientConnector object  in the call table
 * @private
 * @param {string} sipSessionId SIP CALL ID 
 */
PrivateJainSipClientConnector.prototype.removeSessionConnector = function(sipSessionId) {
    console.debug("PrivateJainSipClientConnector:removeSessionConnector(): sipSessionId=" + sipSessionId);
    delete this.sessionConnectors[sipSessionId];
};

/**
 * Reset client context
 * @private
 */
PrivateJainSipClientConnector.prototype.reset = function() {
    console.debug("PrivateJainSipClientConnector:reset()");
    this.openedFlag = false;
    this.configuration = undefined;
    this.resetSipRegisterContext();
    this.sessionConnectors = {};
};

/**
 * Reset SIP register context
 * @private
 */
PrivateJainSipClientConnector.prototype.resetSipRegisterContext = function() {
    console.debug("PrivateJainSipClientConnector:resetSipRegisterContext()");
    if (this.sipRegisterRefreshTimer !== undefined)
        clearTimeout(this.sipRegisterRefreshTimer);
    this.sipRegisterState = this.SIP_UNREGISTERED_STATE;
    this.sipRegisterRefreshTimer = undefined;
    this.sipRegisterAuthenticatedFlag = false;
    this.jainSipRegisterRequest = undefined;
    this.jainSipRegisterTransaction = undefined;
    this.jainSipRegisterDialog = undefined;
    this.sipUnregisterPendingFlag = false;
};

/**
 * Check configuration 
 * @private
 * @param {object} configuration SIP user agent configuration
 * * <p> Client configuration sample: <br>
 * { <br>
 * <span style="margin-left: 30px">sipUserAgent:"WebRtcCommTestWebApp/0.0.1",<br></span>
 * <span style="margin-left: 30px">sipUserAgentCapabilities:"+g.oma.sip-im",<br></span>
 * <span style="margin-left: 30px">sipOutboundProxy:"ws://localhost:5082",<br></span>
 * <span style="margin-left: 30px">sipDomain:"sip.net",<br></span>
 * <span style="margin-left: 30px">sipUserName:"alice",<br></span>
 * <span style="margin-left: 30px">sipLogin:"alice@sip.net,<br></span>
 * <span style="margin-left: 30px">sipPassword:"1234567890",<br></span>
 * <span style="margin-left: 30px">sipUserAgentCapabilities,<br></span>
 * <span style="margin-left: 30px">sipRegisterMode:true,<br></span>
 * }<br>
 *  </p>
 * @return true configuration ok false otherwise
 */
PrivateJainSipClientConnector.prototype.checkConfiguration = function(configuration) {
    console.debug("PrivateJainSipClientConnector:checkConfiguration()");
    try
    {
        var check = true;
        // sipLogin, sipPassword, sipUserAgentCapabilities not mandatory
        if (configuration.sipUserAgent === undefined || configuration.sipUserAgent.length === 0)
        {
            check = false;
            console.error("PrivateJainSipClientConnector:checkConfiguration(): missing configuration parameter sipUserAgent");
        }

        // stunServer, sipLogin, sipPassword, sipApplicationprofile not mandatory
        if (configuration.sipOutboundProxy === undefined || configuration.sipOutboundProxy.length === 0)
        {
            check = false;
            console.error("PrivateJainSipClientConnector:checkConfiguration(): missing configuration parameter sipOutboundProxy");
        }

        if (configuration.sipDomain === undefined || configuration.sipDomain.length === 0)
        {
            check = false;
            console.error("PrivateJainSipClientConnector:checkConfiguration(): missing configuration parameter sipDomain");
        }

        if (configuration.sipUserName === undefined || configuration.sipUserName.length === 0)
        {
            check = false;
            console.error("PrivateJainSipClientConnector:checkConfiguration(): missing configuration parameter sipUserName");
        }

        if (configuration.sipRegisterMode === undefined || configuration.sipRegisterMode.length === 0)
        {
            check = false;
            console.error("PrivateJainSipClientConnector:checkConfiguration(): missing configuration parameter sipRegisterMode");
        }

        if (configuration.sipLogin !== undefined && configuration.sipLogin === "")
        {
            configuration.sipLogin = undefined;
        }

        if (configuration.sipPassword !== undefined && configuration.sipPassword === "")
        {
            configuration.sipPassword = undefined;
        }

        if (configuration.sipUserAgentCapabilities !== undefined && configuration.sipUserAgentCapabilities === "")
        {
            configuration.sipUserAgentCapabilities = undefined;
        }

        console.debug("PrivateJainSipClientConnector:checkConfiguration(): configuration.sipUserAgent:" + configuration.sipUserAgent);
        console.debug("PrivateJainSipClientConnector:checkConfiguration(): configuration.sipUserAgentCapabilities:" + configuration.sipUserAgentCapabilities);
        console.debug("PrivateJainSipClientConnector:checkConfiguration(): configuration.sipOutboundProxy:" + configuration.sipOutboundProxy);
        console.debug("PrivateJainSipClientConnector:checkConfiguration(): configuration.sipDomain:" + configuration.sipDomain);
        console.debug("PrivateJainSipClientConnector:checkConfiguration(): configuration.sipUserName:" + configuration.sipUserName);
        console.debug("PrivateJainSipClientConnector:checkConfiguration(): configuration.sipLogin:" + configuration.sipLogin);
        console.debug("PrivateJainSipClientConnector:checkConfiguration(): configuration.sipPassword: " + configuration.sipPassword);
        console.debug("PrivateJainSipClientConnector:checkConfiguration(): configuration.sipRegisterMode:" + configuration.sipRegisterMode);
        return check;
    }
    catch (exception) {
        console.error("PrivateJainSipClientConnector:checkConfiguration(): catched exception:" + exception);
        return false;
    }
};

/**
 * Implementation of JAIN SIP stack event listener interface: process WebSocket connection event
 * @public 
 */
PrivateJainSipClientConnector.prototype.processConnected = function() {
    console.debug("PrivateJainSipClientConnector:processConnected()");
    try
    {
        // Start SIP REGISTER process
        if (this.openedFlag === false)
        {
            if (this.configuration.sipRegisterMode === true)
            {
                this.resetSipRegisterContext();
                // Send SIP REGISTER request
                this.sendNewSipRegisterRequest(this.SIP_SESSION_EXPIRATION_TIMER);
                this.sipRegisterState = this.SIP_REGISTERING_STATE;
                return;
            }
            else
            {
                this.openedFlag = true;
                this.webRTCommClient.onPrivateClientConnectorOpenedEvent();
                return;
            }
        }
        else
        {
            console.error("PrivateJainSipClientConnector:processConnected(): this.openedFlag==true !");
        }

        // Open failed
        this.reset();
        this.webRTCommClient.onPrivateClientConnectorOpenErrorEvent();
    }
    catch (exception) {
        this.reset();
        this.webRTCommClient.onPrivateClientConnectorOpenErrorEvent();
        console.error("PrivateJainSipClientConnector:processConnected(): catched exception:" + exception);
    }
};


/**
 * Send SIP REGISTER request 
 * @param {int} expiration
 * @private
 */
PrivateJainSipClientConnector.prototype.sendNewSipRegisterRequest = function(expiration) {
    console.debug("PrivateJainSipClientConnector:sendNewSipRegisterRequest()");
    var fromSipUriString = this.configuration.sipUserName + "@" + this.configuration.sipDomain;
    var jainSipCseqHeader = this.jainSipHeaderFactory.createCSeqHeader(1, "REGISTER");
    var jainSipCallIdHeader = this.jainSipHeaderFactory.createCallIdHeader(new String(new Date().getTime()));
    var jainSipExpiresHeader = this.jainSipHeaderFactory.createExpiresHeader(expiration);
    var jainSipMaxForwardHeader = this.jainSipHeaderFactory.createMaxForwardsHeader(70);
    var jainSipRequestUri = this.jainSipAddressFactory.createSipURI_user_host(null, this.configuration.sipDomain);
    var jainSipAllowListHeader = this.jainSipHeaderFactory.createHeaders(PrivateJainSipClientConnector.prototype.SIP_ALLOW_HEADER);
    var jainSipFromUri = this.jainSipAddressFactory.createSipURI_user_host(null, fromSipUriString);
    var jainSipFromAddress = this.jainSipAddressFactory.createAddress_name_uri(null, jainSipFromUri);
    var random = new Date();
    var tag = random.getTime();
    var jainSipFromHeader = this.jainSipHeaderFactory.createFromHeader(jainSipFromAddress, tag);
    var jainSipToHeader = this.jainSipHeaderFactory.createToHeader(jainSipFromAddress, null);
    var jainSipViaHeader = this.jainSipListeningPoint.getViaHeader();
    this.jainSipRegisterRequest = this.jainSipMessageFactory.createRequest(jainSipRequestUri, "REGISTER", jainSipCallIdHeader, jainSipCseqHeader, jainSipFromHeader, jainSipToHeader, jainSipViaHeader, jainSipMaxForwardHeader);
    this.jainSipMessageFactory.addHeader(this.jainSipRegisterRequest, jainSipExpiresHeader);
    this.jainSipMessageFactory.addHeader(this.jainSipRegisterRequest, jainSipAllowListHeader);
    this.jainSipMessageFactory.addHeader(this.jainSipRegisterRequest, this.jainSipContactHeader);

    this.jainSipRegisterTransaction = this.jainSipProvider.getNewClientTransaction(this.jainSipRegisterRequest);
    this.jainSipRegisterDialog = this.jainSipRegisterTransaction.getDialog();
    this.jainSipRegisterRequest.setTransaction(this.jainSipRegisterTransaction);
    this.jainSipRegisterTransaction.sendRequest();
};

/**
 * Send Authentitated SIP REGISTER request 
 * @param {AuthorizationHeader} jainSipAuthorizationHeader
 * @private
 */
PrivateJainSipClientConnector.prototype.sendAuthenticatedSipRegisterRequest = function(jainSipAuthorizationHeader) {
    console.debug("PrivateJainSipClientConnector:sendAuthenticatedSipRegisterRequest()");
    this.jainSipRegisterRequest.removeHeader("Authorization");
    var newJainSipRegisterRequest = new SIPRequest();
    newJainSipRegisterRequest.setMethod(this.jainSipRegisterRequest.getMethod());
    newJainSipRegisterRequest.setRequestURI(this.jainSipRegisterRequest.getRequestURI());
    var headerList = this.jainSipRegisterRequest.getHeaders();
    for (var i = 0; i < headerList.length; i++)
    {
        newJainSipRegisterRequest.addHeader(headerList[i]);
    }

    var num = new Number(this.jainSipRegisterRequest.getCSeq().getSeqNumber());
    newJainSipRegisterRequest.getCSeq().setSeqNumber(num + 1);
    newJainSipRegisterRequest.setCallId(this.jainSipRegisterRequest.getCallId());
    newJainSipRegisterRequest.setVia(this.jainSipListeningPoint.getViaHeader());
    newJainSipRegisterRequest.setFrom(this.jainSipRegisterRequest.getFrom());
    newJainSipRegisterRequest.setTo(this.jainSipRegisterRequest.getTo());
    newJainSipRegisterRequest.setMaxForwards(this.jainSipRegisterRequest.getMaxForwards());

    this.jainSipRegisterRequest = newJainSipRegisterRequest;
    this.jainSipMessageFactory.addHeader(this.jainSipRegisterRequest, jainSipAuthorizationHeader);
    this.jainSipRegisterTransaction = this.jainSipProvider.getNewClientTransaction(this.jainSipRegisterRequest);
    this.jainSipRegisterRequest.setTransaction(this.jainSipRegisterTransaction);
    this.jainSipRegisterTransaction.sendRequest();
};

/**
 * Implementation of JAIN SIP stack event listener interface: process WebSocket disconnection/close event
 * @public
 */
PrivateJainSipClientConnector.prototype.processDisconnected = function() {
    console.debug("PrivateJainSipClientConnector:processDisconnected(): SIP connectivity has been lost");
    try
    {
        this.reset();
        this.webRTCommClient.onPrivateClientConnectorClosedEvent();
    }
    catch (exception) {
        console.error("PrivateJainSipClientConnector:processDisconnected(): catched exception:" + exception);
    }
};

/**
 * Implementation of JAIN SIP stack event listener interface: process WebSocket connection error event
 * @public 
 * @param {string} error WebSocket connection error
 */
PrivateJainSipClientConnector.prototype.processConnectionError = function(error) {
    console.warn("PrivateJainSipClientConnector:processConnectionError(): SIP connection has failed, error:" + error);
    try
    {
        this.reset();
        this.webRTCommClient.onPrivateClientConnectorOpenErrorEvent();
    }
    catch (exception) {
        console.error("PrivateJainSipClientConnector:processConnectionError(): catched exception:" + exception);
    }
};

/**
 * Implementation of JAIN SIP stack event listener interface: process SIP request event
 * @public 
 * @param {RequestEvent} requestEvent JAIN SIP request event
 */
PrivateJainSipClientConnector.prototype.processRequest = function(requestEvent) {
    console.debug("PrivateJainSipClientConnector:processRequest()");
    try
    {
        var jainSipRequest = requestEvent.getRequest();
        console.debug("PrivateJainSipClientConnector:processRequest():jainSipRequest.getCallId().getCallId()="+jainSipRequest.getCallId().getCallId());
        var jainSipRequestMethod = jainSipRequest.getMethod();
        if (jainSipRequestMethod === "OPTIONS")
        {
            this.processSipOptionRequest(requestEvent);
        }
        else
        {
            // Find related PrivateJainSipCallConnector (subsequent request)
            var sipSessionId = jainSipRequest.getCallId().getCallId();
            var sessionConnector = this.sessionConnectors[sipSessionId];
            if (sessionConnector)
            {
                sessionConnector.onJainSipClientConnectorSipRequestEvent(requestEvent);
            }
            else
            {
                if (jainSipRequestMethod === "INVITE")
                {
                    // Incoming SIP INVITE
                    var newWebRTCommCall = new WebRTCommCall(this.webRTCommClient);
                    newWebRTCommCall.incomingCallFlag = true;
                    newWebRTCommCall.connector = this.createPrivateSessionConnector(newWebRTCommCall, sipSessionId);
                    newWebRTCommCall.id = newWebRTCommCall.connector.getId();
                    newWebRTCommCall.connector.sipCallState = PrivateJainSipCallConnector.prototype.SIP_INVITED_INITIAL_STATE;
                    newWebRTCommCall.connector.onJainSipClientConnectorSipRequestEvent(requestEvent);
                }
                else if (jainSipRequestMethod === "MESSAGE")
                {
                    // Incoming SIP MESSAGE
                    // Find WebRTCommCall linked with the message (if exist)
                    var targetedWebRTCommCall = undefined;
                    var from = requestEvent.getRequest().getHeader("From").getAddress().getURI().getUser();
                    for (var sipCallId in this.sessionConnectors)
                    {
                        var sessionConnector = this.sessionConnectors[sipCallId];
                        if (sessionConnector instanceof PrivateJainSipCallConnector)
                        {
                            if (sessionConnector.isOpened())
                            {
                                if (sessionConnector.webRTCommCall.isIncoming() && sessionConnector.webRTCommCall.callerPhoneNumber === from)
                                {
                                    targetedWebRTCommCall = sessionConnector.webRTCommCall;
                                    break;
                                }
                                else if (sessionConnector.webRTCommCall.calleePhoneNumber === from)
                                {
                                    targetedWebRTCommCall = sessionConnector.webRTCommCall;
                                    break;
                                }
                            }
                        }
                    }

                    // Build WebRTCommMessage
                    var newWebRTCommMessage = new WebRTCommMessage(this.webRTCommClient, targetedWebRTCommCall);
                    newWebRTCommMessage.connector.onJainSipClientConnectorSipRequestEvent(requestEvent);
                }
                else
                {
                    console.warn("PrivateJainSipClientConnector:processRequest(): SIP request ignored");
                    //@todo Should send SIP response 404 NOT FOUND or 501 NOT_IMPLEMENTED 				 
                }

            }
        }
    }
    catch (exception) {
        console.error("PrivateJainSipClientConnector:processRequest(): catched exception:" + exception);
    }
};


/**
 * Implementation of JAIN SIP stack event listener interface: process SIP response event
 * @public 
 * @param {ResponseEvent} responseEvent JAIN SIP response event
 */
PrivateJainSipClientConnector.prototype.processResponse = function(responseEvent) {
    console.debug("PrivateJainSipClientConnector:processResponse()");
    try
    {
        var jainSipResponse = responseEvent.getResponse();
        if (jainSipResponse.getCSeq().getMethod() === "REGISTER")
        {
            this.processSipRegisterResponse(responseEvent);
        }
        else
        {
            // Find related PrivateJainSipCallConnector
            var sipSessionId = jainSipResponse.getCallId().getCallId();
            var sessionConnector = this.sessionConnectors[sipSessionId];
            if (sessionConnector)
            {
                sessionConnector.onJainSipClientConnectorSipResponseEvent(responseEvent);
            }
            else
            {
                console.warn("PrivateJainSipClientConnector:processResponse(): PrivateJainSipCallConnector not found, SIP response ignored");
            }
        }
    }
    catch (exception) {
        console.error("PrivateJainSipClientConnector:processResponse(): catched exception:" + exception);
    }
};

/**
 * Implementation of JAIN SIP stack event listener interface: process SIP transaction terminated event
 * @public 
 */
PrivateJainSipClientConnector.prototype.processTransactionTerminated = function() {
    console.debug("PrivateJainSipClientConnector:processTransactionTerminated()");
};

/**
 * Implementation of JAIN SIP stack event listener interface: process SIP dialog terminated event
 * @public 
 */
PrivateJainSipClientConnector.prototype.processDialogTerminated = function() {
    console.debug("PrivateJainSipClientConnector:processDialogTerminated()");
};

/**
 * Implementation of JAIN SIP stack event listener interface: process I/O websocket  error event
 * @public 
 * @param {ExceptionEvent} exceptionEvent JAIN SIP exception event 
 */
PrivateJainSipClientConnector.prototype.processIOException = function(exceptionEvent) {
    console.error("PrivateJainSipClientConnector:processIOException(): exceptionEvent=" + exceptionEvent.message);
};

/**
 * Implementation of JAIN SIP stack event listener interface: process SIP Dialog Timeout event
 * @public 
 * @param {TimeoutEvent} timeoutEvent JAIN SIP timeout event
 */
PrivateJainSipClientConnector.prototype.processTimeout = function(timeoutEvent) {
    console.debug("PrivateJainSipClientConnector:processTimeout():timeoutEvent=" + timeoutEvent);
    try
    {
        var sipClientTransaction = timeoutEvent.getClientTransaction();
        // Find related PrivateJainSipCallConnector
        var sipCallId = sipClientTransaction.getDialog().getCallId().getCallId();
        var sessionConnector = this.sessionConnectors[sipCallId];
        if (sessionConnector)
        {
            sessionConnector.onJainSipClientConnectorSipTimeoutEvent(timeoutEvent);
        }
        else if (this.jainSipRegisterRequest.getCallId().getCallId() === sipCallId)
        {
            console.error("PrivateJainSipClientConnector:processTimeout(): SIP registration failed, request timeout, no response from SIP server");
            this.reset();
            this.webRTCommClient.onPrivateClientConnectorOpenErrorEvent("Request Timeout");
        }
        else
        {
            console.warn("PrivateJainSipClientConnector:processTimeout(): no dialog found, SIP timeout ignored");
        }
    }
    catch (exception) {
        console.error("PrivateJainSipClientConnector:processTimeout(): catched exception:" + exception);
    }
};

/**
 * SIP REGISTER refresh timeout
 * @private 
 */
PrivateJainSipClientConnector.prototype.onSipRegisterTimeout = function() {
    console.debug("PrivateJainSipClientConnector:onSipRegisterTimeout()");
    try
    {
        if (this.sipRegisterState === this.SIP_REGISTERED_STATE)
        {
            this.sipRegisterRefreshTimer = undefined;
            this.sipRegisterState = this.SIP_REGISTER_REFRESHING_STATE;
            // Send SIP REGISTER request
            this.sendNewSipRegisterRequest(this.SIP_SESSION_EXPIRATION_TIMER);
        }
        else
        {
            console.warn("PrivateJainSipClientConnector:onSipRegisterTimeout(): SIP REGISTER refresh stopped");
        }
    }
    catch (exception) {
        console.error("PrivateJainSipClientConnector:onSipRegisterTimeout(): catched exception:" + exception);
    }
};


/**
 * SIP REGISTER state machine
 * @private 
 * @param {ResponseEvent} responseEvent JAIN SIP response to process
 */
PrivateJainSipClientConnector.prototype.processSipRegisterResponse = function(responseEvent) {
    console.debug("PrivateJainSipClientConnector:processSipRegisterResponse(): this.sipRegisterState=" + this.sipRegisterState);

    var jainSipResponse = responseEvent.getResponse();
    var statusCode = parseInt(jainSipResponse.getStatusCode());
    if (this.sipRegisterState === this.SIP_UNREGISTERED_STATE)
    {
        console.error("PrivateJainSipClientConnector:processSipRegisterResponse(): bad state, SIP response ignored");
    }
    else if ((this.sipRegisterState === this.SIP_REGISTERING_STATE) || (this.sipRegisterState === this.SIP_REGISTER_REFRESHING_STATE))
    {
        if (statusCode < 200)
        {
            console.debug("PrivateJainSipClientConnector:processSipRegisterResponse(): 1XX response ignored");
        }
        else if(statusCode === 401 || statusCode === 407)
        {
            if (this.configuration.sipPassword !== undefined && this.configuration.sipLogin !== undefined)
            {
                this.sipRegisterState = this.SIP_REGISTERING_401_STATE;
                var jainSipAuthorizationHeader = this.jainSipHeaderFactory.createAuthorizationHeader(jainSipResponse, this.jainSipRegisterRequest, this.configuration.sipPassword, this.configuration.sipLogin);
                // Send authenticated SIP REGISTER request
                this.sendAuthenticatedSipRegisterRequest(jainSipAuthorizationHeader);
            }
            else
            {
                // Authentification required but not SIP credentials in SIP profile
                console.error("PrivateJainSipClientConnector:processSipRegisterResponse(): SIP registration failed:" + jainSipResponse.getStatusCode() + "  " + jainSipResponse.getStatusLine());
                this.reset();
                this.webRTCommClient.onPrivateClientConnectorOpenErrorEvent();
            }
        }
        else if (statusCode === 200)
        {
            this.sipRegisterState = this.SIP_REGISTERED_STATE;
            if (this.openedFlag === false)
            {
                this.openedFlag = true;
                this.webRTCommClient.onPrivateClientConnectorOpenedEvent();
            }

            if (this.sipUnregisterPendingFlag === true) {
                this.sipUnregisterPendingFlag = false;
                this.sipRegisterState = this.SIP_UNREGISTERING_STATE;
                if (this.sipRegisterRefreshTimer)
                {
                    // Cancel SIP REGISTER refresh timer
                    clearTimeout(this.sipRegisterRefreshTimer);
                }
                this.sendNewSipRegisterRequest(0);
            }
            else
            {
                // Start SIP REGISTER refresh timeout
                var that = this;
                if (this.sipRegisterRefreshTimer)
                    clearTimeout(this.sipRegisterRefreshTimer);
                this.sipRegisterRefreshTimer = setTimeout(function() {
                    that.onSipRegisterTimeout();
                }, 40000);
            }
        }
        else
        {
            console.error("PrivateJainSipClientConnector:processSipRegisterResponse(): SIP registration failed:" + jainSipResponse.getStatusCode() + "  " + jainSipResponse.getStatusLine());
            this.reset();
            this.webRTCommClient.onPrivateClientConnectorOpenErrorEvent();
        }
    }
    else if (this.sipRegisterState === this.SIP_REGISTERING_401_STATE)
    {
        if (statusCode < 200)
        {
            //  No temporary response for SIP REGISTER request 
        }
        else if (statusCode === 200)
        {
            this.sipRegisterState = this.SIP_REGISTERED_STATE;
            if (this.openedFlag === false)
            {
                console.debug("PrivateJainSipClientConnector:processSipRegisterResponse(): this.openedFlag=true");
                this.openedFlag = true;
                this.webRTCommClient.onPrivateClientConnectorOpenedEvent();
            }

            if (this.sipUnregisterPendingFlag === true) {
                this.sipUnregisterPendingFlag = false;
                this.sipRegisterState = this.SIP_UNREGISTERING_STATE;
                if (this.sipRegisterRefreshTimer)
                {
                    // Cancel SIP REGISTER refresh timer
                    clearTimeout(this.sipRegisterRefreshTimer);
                }
                this.sendNewSipRegisterRequest(0);
            }
            else
            {
                // Start SIP REGISTER refresh timeout
                var that = this;
                if (this.sipRegisterRefreshTimer)
                    clearTimeout(this.sipRegisterRefreshTimer);
                this.sipRegisterRefreshTimer = setTimeout(function() {
                    that.onSipRegisterTimeout();
                }, 40000);
            }
        }
        else
        {
            console.error("PrivateJainSipClientConnector:processSipRegisterResponse(): SIP registration failed:" + jainSipResponse.getStatusCode() + "  " + jainSipResponse.getStatusLine());
            this.reset();
            this.webRTCommClient.onPrivateClientConnectorOpenErrorEvent();
        }
    }
    else if (this.sipRegisterState === this.SIP_REGISTERED_STATE)
    {
        console.error("PrivateJainSipClientConnector:processSipRegisterResponse(): bad state, SIP response ignored");
    }
    else if (this.sipRegisterState === this.SIP_UNREGISTERING_STATE)
    {
        if (statusCode < 200)
        {
            //  Not temporary response for SIP REGISTER request  
        }
        else if(statusCode === 401 || statusCode === 407)
        {
            this.sipRegisterState = this.SIP_UNREGISTERING_401_STATE;
            jainSipAuthorizationHeader = this.jainSipHeaderFactory.createAuthorizationHeader(jainSipResponse, this.jainSipRegisterRequest, this.configuration.sipPassword, this.configuration.sipLogin);
            this.sendAuthenticatedSipRegisterRequest(jainSipAuthorizationHeader);
        }
        else if (statusCode === 200)
        {
            this.reset();
            this.webRTCommClient.onPrivateClientConnectorClosedEvent();
        }
        else
        {
            console.error("PrivateJainSipClientConnector:processSipRegisterResponse(): SIP unregistration failed:" + jainSipResponse.getStatusCode() + "  " + jainSipResponse.getStatusLine());
            this.reset();
            this.webRTCommClient.onPrivateClientConnectorClosedEvent();
        }
    }
    else if (this.sipRegisterState === this.SIP_UNREGISTERING_401_STATE)
    {
        if (statusCode < 200)
        {
            //  Not temporary response for SIP REGISTER request 
        }
        else if (statusCode === 200)
        {
            this.reset();
            this.webRTCommClient.onPrivateClientConnectorClosedEvent();
        }
        else
        {
            console.error("PrivateJainSipClientConnector:processSipRegisterResponse(): SIP unregistration failed:" + jainSipResponse.getStatusCode() + "  " + jainSipResponse.getStatusLine());
            this.reset();
            this.webRTCommClient.onPrivateClientConnectorClosedEvent();
        }
    }
    else if (this.sipRegisterState === this.SIP_UNREGISTERED_STATE)
    {
        console.error("PrivateJainSipClientConnector:processSipRegisterResponse(): bad state, SIP response ignored");
    }
    else
    {
        console.error("PrivateJainSipClientConnector:processSipRegisterResponse(): bad state, SIP response ignored");
    }
};



/**
 * Handle SIP OPTIONS RESPONSE (default behaviour: send 200 OK response)                  
 * @param {RequestEvent} requestEvent JAIN SIP request event to process
 * @private 
 */
PrivateJainSipClientConnector.prototype.processSipOptionRequest = function(requestEvent) {
    console.debug("PrivateJainSipClientConnector:processSipOptionRequest()");
    // Build SIP OPTIONS 200 OK response   
    var jainSipRequest = requestEvent.getRequest();
    var jainSip200OKResponse = jainSipRequest.createResponse(200, "OK");
    jainSip200OKResponse.addHeader(this.jainSipContactHeader);
    jainSip200OKResponse.removeHeader("P-Asserted-Identity");
    jainSip200OKResponse.removeHeader("P-Charging-Vector");
    jainSip200OKResponse.removeHeader("P-Charging-Function-Addresses");
    jainSip200OKResponse.removeHeader("P-Called-Party-ID");
    requestEvent.getServerTransaction().sendResponse(jainSip200OKResponse);
};

/**
 * @class WebRTCommCall
 * @classdesc Main class of the WebRTComm Framework providing high level communication management: 
 *            ringing, ringing back, accept, reject, cancel, bye 
 * @constructor
 * @public
 * @param  {WebRTCommClient} webRTCommClient client owner 
 * @author Laurent STRULLU (laurent.strullu@orange.com) 
 * @author Jean Deruelle (jean.deruelle@telestax.com) 
 */
WebRTCommCall = function(webRTCommClient)
{
    if (webRTCommClient instanceof WebRTCommClient)
    {
        console.debug("WebRTCommCall:WebRTCommCall()");
        this.id = undefined;
        this.webRTCommClient = webRTCommClient;
        this.calleePhoneNumber = undefined;
        this.callerPhoneNumber = undefined;
        this.callerDisplayName = undefined;
        this.incomingCallFlag = false;
        this.configuration = undefined;
        this.connector = undefined;
        this.peerConnection = undefined;
        this.peerConnectionState = undefined;
        this.remoteBundledAudioVideoMediaStream = undefined;
        this.remoteAudioMediaStream = undefined;
        this.remoteVideoMediaStream = undefined;
        this.remoteSdpOffer = undefined;
        this.messageChannel = undefined;
	this.dtmfSender = undefined;
        // Set default listener to client listener
        this.eventListener = webRTCommClient.eventListener;
    }
    else
    {
        throw "WebRTCommCall:WebRTCommCall(): bad arguments"
    }
};

/**
 * Audio Codec Name 
 * @private
 * @constant
 */
WebRTCommCall.prototype.codecNames = {
    0: "PCMU",
    8: "PCMA"
};

/**
 * Get opened/closed status 
 * @public
 * @returns {boolean} true if opened, false if closed
 */
WebRTCommCall.prototype.isOpened = function() {
    if (this.connector)
        return this.connector.isOpened();
    else
        return false;
};

/**
 * Get incoming call status 
 * @public
 * @returns {boolean} true if incoming, false if outgoing
 */
WebRTCommCall.prototype.isIncoming = function() {
    if (this.isOpened())
    {
        return this.incomingCallFlag;
    }
    else
    {
        console.error("WebRTCommCall:isIncoming(): bad state, unauthorized action");
        throw "WebRTCommCall:isIncoming(): bad state, unauthorized action";
    }
};



/**
 * Get call ID
 * @public
 * @returns {String} id  
 */
WebRTCommCall.prototype.getId = function() {
    return this.id;
};

/**
 * Get caller phone number
 * @public
 * @returns {String} callerPhoneNumber or undefined
 */
WebRTCommCall.prototype.getCallerPhoneNumber = function() {
    return this.callerPhoneNumber;
};

/**
 * Get caller display Name
 * @public
 * @returns {String} callerDisplayName or undefined
 */
WebRTCommCall.prototype.getCallerDisplayName = function() {
    return this.callerDisplayName;
};

/**
 * Get client configuration
 * @public
 * @returns {object} configuration or undefined
 */
WebRTCommCall.prototype.getConfiguration = function() {
    return this.configuration;
};


/**
 * Get callee phone number
 * @public
 * @return  {String} calleePhoneNumber or undefined
 */
WebRTCommCall.prototype.getCalleePhoneNumber = function() {
    return this.calleePhoneNumber;
};

/**
 * get bundled audio & video remote media stream
 * @public
 * @return {MediaStream} remoteBundledAudioVideoMediaStream or undefined
 */
WebRTCommCall.prototype.getRemoteBundledAudioVideoMediaStream = function() {
    return this.remoteBundledAudioVideoMediaStream;
};

/**
 * get remote audio media stream
 * @public
 * @return {MediaStream} remoteAudioMediaStream or undefined
 */
WebRTCommCall.prototype.getRemoteAudioMediaStream = function() {
    return this.remoteAudioMediaStream;
};

/**
 * get remote audio media stream
 * @public
 * @return {MediaStream} remoteAudioMediaStream or undefined
 */
WebRTCommCall.prototype.getRemoteVideoMediaStream = function() {
    return this.remoteVideoMediaStream;
};


/**
 * set webRTCommCall listener
 * @param {objet} eventListener implementing WebRTCommCallEventListener interface
 */
WebRTCommCall.prototype.setEventListener = function(eventListener) {
    this.eventListener = eventListener;
};

/**
 * Open WebRTC communication,  asynchronous action, opened or error event are notified to the WebRTCommClient eventListener
 * @public 
 * @param {String} calleePhoneNumber callee phone number (bob@sip.net)
 * @param {object} configuration communication configuration JSON object
 * <p> Communication configuration sample: <br>
 * { <br>
 * <span style="margin-left: 30px">displayName:alice,<br></span>
 * <span style="margin-left: 30px">localMediaStream: [LocalMediaStream],<br></span>
 * <span style="margin-left: 30px">audioMediaFlag:true,<br></span>
 * <span style="margin-left: 30px">videoMediaFlag:false,<br></span>
 * <span style="margin-left: 30px">messageMediaFlag:false,<br></span>
 * <span style="margin-left: 30px">audioCodecsFilter:PCMA,PCMU,OPUS,<br></span>
 * <span style="margin-left: 30px">videoCodecsFilter:VP8,H264,<br></span>
 * <span style="margin-left: 30px">opusFmtpCodecsParameters:maxaveragebitrate=128000,<br></span>
 * }<br>
 * </p>
 * @throw {String} Exception "bad argument, check API documentation"
 * @throw {String} Exception "bad configuration, missing parameter"
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception internal error
 */
WebRTCommCall.prototype.open = function(calleePhoneNumber, configuration) {
    console.debug("WebRTCommCall:open():calleePhoneNumber=" + calleePhoneNumber);
    console.debug("WebRTCommCall:open():configuration=" + JSON.stringify(configuration));
    if (typeof(configuration) === 'object')
    {
        if (this.webRTCommClient.isOpened())
        {
            if (this.checkConfiguration(configuration))
            {
                if (this.isOpened() === false)
                {
                    try
                    {
                        var that = this;
                        this.callerPhoneNumber = this.webRTCommClient.configuration.sip.sipUserName;
                        this.calleePhoneNumber = calleePhoneNumber;
                        this.configuration = configuration;
                        this.connector.open(configuration);

                        // Setup RTCPeerConnection first
                        this.createRTCPeerConnection();
                        this.peerConnection.addStream(this.configuration.localMediaStream);
                        if (this.configuration.messageMediaFlag)
                        {
                            if (this.peerConnection.createDataChannel)
                            {
                                try
                                {
                                    this.messageChannel = this.peerConnection.createDataChannel("mymessageChannel", {
                                        reliable: false
                                    });
                                    console.debug("WebRTCommCall:open(): this.messageChannel.label=" + this.messageChannel.label);
                                    console.debug("WebRTCommCall:open(): this.messageChannel.reliable=" + this.messageChannel.reliable);
                                    console.debug("WebRTCommCall:open(): this.messageChannel.binaryType=" + this.messageChannel.binaryType);
                                    this.messageChannel.onopen = function(event) {
                                        that.onRtcPeerConnectionMessageChannelOnOpenEvent(event);
                                    };
                                    this.messageChannel.onclose = function(event) {
                                        that.onRtcPeerConnectionMessageChannelOnClose(event);
                                    };
                                    this.messageChannel.onerror = function(event) {
                                        that.onRtcPeerConnectionMessageChannelOnErrorEvent(event);
                                    };
                                    this.messageChannel.onmessage = function(event) {
                                        that.onRtcPeerConnectionMessageChannelOnMessageEvent(event);
                                    };
                                }
                                catch (exception)
                                {
                                    alert("WebRTCommCall:open():DataChannel not supported");
                                }
                            }
                        }

                        if (window.webkitRTCPeerConnection)
                        {
                            var sdpConstraints = {
                                mandatory:
                                        {
                                            OfferToReceiveAudio: this.configuration.audioMediaFlag,
                                            OfferToReceiveVideo: this.configuration.videoMediaFlag
                                        },
                                optional: []
                            };

                            console.debug("WebRTCommCall:open():sdpConstraints=" + JSON.stringify(sdpConstraints));
                            this.peerConnection.createOffer(function(offer) {
                                that.onRtcPeerConnectionCreateOfferSuccessEvent(offer);
                            }, function(error) {
                                that.onRtcPeerConnectionCreateOfferErrorEvent(error);
                            }, sdpConstraints);
                        }
                        else if (window.mozRTCPeerConnection)
                        {
                            var sdpConstraints = {
                                    offerToReceiveAudio: this.configuration.audioMediaFlag,
                                    offerToReceiveVideo: this.configuration.videoMediaFlag,
                                    mozDontOfferDataChannel: !this.configuration.messageMediaFlag
                            };

                            console.debug("WebRTCommCall:open():sdpConstraints=" + JSON.stringify(sdpConstraints));
                            this.peerConnection.createOffer(function(offer) {
                                that.onRtcPeerConnectionCreateOfferSuccessEvent(offer);
                            }, function(error) {
                                that.onRtcPeerConnectionCreateOfferErrorEvent(error);
                            }, sdpConstraints);
                        }
                        console.debug("WebRTCommCall:open():sdpConstraints=" + JSON.stringify(sdpConstraints));
                    }
                    catch (exception) {
                        console.error("WebRTCommCall:open(): catched exception:" + exception);
                        setTimeout(function() {
                            try {
                                that.eventListener.onWebRTCommCallOpenErrorEvent(that, exception);
                            }
                            catch (exception)
                            {
                                console.error("WebRTCommCall:open(): catched exception in listener:" + exception);
                            }
                        }, 1);
                        // Close properly the communication
                        try {

                            this.close();
                        } catch (e) {
                        }
                        throw exception;
                    }
                }
                else
                {
                    console.error("WebRTCommCall:open(): bad state, unauthorized action");
                    throw "WebRTCommCall:open(): bad state, unauthorized action";
                }
            }
            else
            {
                console.error("WebRTCommCall:open(): bad configuration");
                throw "WebRTCommCall:open(): bad configuration";
            }
        }
        else
        {
            console.error("WebRTCommCall:open(): bad state, unauthorized action");
            throw "WebRTCommCall:open(): bad state, unauthorized action";
        }
    }
    else
    {
        console.error("WebRTCommCall:open(): bad argument, check API documentation");
        throw "WebRTCommCall:open(): bad argument, check API documentation"
    }
};


/**
 * Close WebRTC communication, asynchronous action, closed event are notified to the WebRTCommClient eventListener
 * @public 
 * @throw {String} Exception "bad state, unauthorized action"
 */
WebRTCommCall.prototype.close = function() {
    console.debug("WebRTCommCall:close()");
    if (this.webRTCommClient.isOpened())
    {
        try
        {
            // Close private Call Connector
            if (this.connector)
            {
                this.connector.close();
            }

            // Close RTCPeerConnection
            if (this.peerConnection && this.peerConnection.signalingState !== 'closed')
            {
                if (this.messageChannel)
                    this.messageChannel.close();
                this.peerConnection.close();
                this.peerConnection = undefined;
		this.dtmfSender = undefined;
                // Notify asynchronously the closed event
                var that = this;
                setTimeout(function() {
                    that.eventListener.onWebRTCommCallClosedEvent(that);
                }, 1);
            }
        }
        catch (exception) {
            console.error("WebRTCommCall:close(): catched exception:" + exception);
        }
    }
    else
    {
        console.error("WebRTCommCall:close(): bad state, unauthorized action");
        throw "WebRTCommCall:close(): bad state, unauthorized action";
    }
};

/**
 * Accept incoming WebRTC communication
 * @public 
 * @param {object} configuration communication configuration JSON object
 * <p> Communication configuration sample: <br>
 * { <br>
 * <span style="margin-left: 30px">displayName:alice,<br></span>
 * <span style="margin-left: 30px">localMediaStream: [LocalMediaStream],<br></span>
 * <span style="margin-left: 30px">audioMediaFlag:true,<br></span>
 * <span style="margin-left: 30px">videoMediaFlag:false,<br></span>
 * <span style="margin-left: 30px">messageMediaFlag:false,<br></span>
 * <span style="margin-left: 30px">audioCodecsFilter:PCMA,PCMU,OPUS,<br></span>
 * <span style="margin-left: 30px">videoCodecsFilter:VP8,H264,<br></span>
 * }<br>
 * </p>
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception "internal error,check console logs"
 */
WebRTCommCall.prototype.accept = function(configuration) {
    console.debug("WebRTCommCall:accept():configuration=" + JSON.stringify(configuration));
    if (typeof(configuration) === 'object')
    {
        if (this.webRTCommClient.isOpened())
        {
            if (this.checkConfiguration(configuration))
            {
                this.configuration = configuration;
                if (this.isOpened() === false)
                {
                    try
                    {
                        this.createRTCPeerConnection();
                        this.peerConnection.addStream(this.configuration.localMediaStream);
                        var sdpOffer = undefined;
                        if (window.webkitRTCPeerConnection)
                        {
                            sdpOffer = new RTCSessionDescription({
                                type: 'offer',
                                sdp: this.remoteSdpOffer
                            });
                        }
                        else if (window.mozRTCPeerConnection)
                        {
                            sdpOffer = new mozRTCSessionDescription({
                                type: 'offer',
                                sdp: this.remoteSdpOffer
                            });
                        }
                        var that = this;
                        this.peerConnectionState = 'offer-received';
                        this.peerConnection.setRemoteDescription(sdpOffer, function() {
                            that.onRtcPeerConnectionSetRemoteDescriptionSuccessEvent();
                        }, function(error) {
                            that.onRtcPeerConnectionSetRemoteDescriptionErrorEvent(error);
                        });
                    }
                    catch (exception) {
                        console.error("WebRTCommCall:accept(): catched exception:" + exception);
                        // Close properly the communication
                        try {
                            this.close();
                        } catch (e) {
                        }
                        throw exception;
                    }
                }
                else
                {
                    console.error("WebRTCommCall:accept(): bad state, unauthorized action");
                    throw "WebRTCommCall:accept(): bad state, unauthorized action";
                }
            }
            else
            {
                console.error("WebRTCommCall:accept(): bad configuration");
                throw "WebRTCommCall:accept(): bad configuration";
            }
        }
        else
        {
            console.error("WebRTCommCall:accept(): bad state, unauthorized action");
            throw "WebRTCommCall:accept(): bad state, unauthorized action";
        }
    }
    else
    {
        // Client closed
        console.error("WebRTCommCall:accept(): bad argument, check API documentation");
        throw "WebRTCommCall:accept(): bad argument, check API documentation"
    }
};

/**
 * Reject/refuse incoming WebRTC communication
 * @public 
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception "internal error,check console logs"
 */
WebRTCommCall.prototype.reject = function() {
    console.debug("WebRTCommCall:reject()");
    if (this.webRTCommClient.isOpened())
    {
        try
        {
            this.connector.reject();
        }
        catch (exception)
        {
            console.error("WebRTCommCall:reject(): catched exception:" + exception);
            // Close properly the communication
            try {
                this.close();
            } catch (e) {
            }
            throw exception;
        }
    }
    else
    {
        console.error("WebRTCommCall:reject(): bad state, unauthorized action");
        throw "WebRTCommCall:reject(): bad state, unauthorized action";
    }
};

/**
 * Send DTMF Tone to WebRTC communication peer over the peerconnection
 * @public 
 * @param {String} dtmfEvent to send (1,2,3...)
 */
WebRTCommCall.prototype.sendDTMF = function(dtmfEvent) {
	var duration = 500;
	var gap = 50;
	if (this.dtmfSender) {
	    console.debug('Sending Tones, duration, gap: ', dtmfEvent, duration, gap);
	    this.dtmfSender.insertDTMF(dtmfEvent, duration, gap);
	} else {
	    console.debug('DTMFSender not initialized so not Sending Tones, duration, gap: ', dtmfEvent, duration, gap);	
	}
}


/**
 * Send Short message to WebRTC communication peer
 * Use WebRTC datachannel if open otherwise use transport (e.g SIP) implemented by the connector
 * @public 
 * @param {String} text message to send
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception "internal error,check console logs"
 * @returns {WebRTCommMessage} new created WebRTCommMessage object
 */
WebRTCommCall.prototype.sendMessage = function(text) {
    console.debug("WebRTCommCall:sendMessage()");
    if (this.webRTCommClient.isOpened())
    {
        if (this.isOpened())
        {
            var newWebRTCommMessage = new WebRTCommMessage(this.webRTCommClient, this);
            newWebRTCommMessage.text = text;
            if (this.isIncoming())
            {
                newWebRTCommMessage.to = this.callerPhoneNumber;
            }
            else
            {
                newWebRTCommMessage.to = this.calleePhoneNumber;
            }

            try
            {
                newWebRTCommMessage.connector.send();
            }
            catch (exception)
            {
                console.error("WebRTCommCall:sendMessage(): catched exception:" + exception);
                throw "WebRTCommCall:sendMessage(): catched exception:" + exception;
            }
            return newWebRTCommMessage;
        }
        else
        {
            console.error("WebRTCommCall:sendMessage(): bad state, unauthorized action");
            throw "WebRTCommCall:sendMessage(): bad state, unauthorized action";
        }
    }
    else
    {
        console.error("WebRTCommCall:sendMessage(): bad state, unauthorized action");
        throw "WebRTCommCall:sendMessage(): bad state, unauthorized action";
    }
};

/**
 * Send Short message to WebRTC communication peer
 * Use WebRTC datachannel if open otherwise use transport (e.g SIP) implemented by the connector
 * @public 
 * @param {String} text message to send
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception "internal error,check console logs"
 * @returns {WebRTCommMessage} new created WebRTCommMessage object
 */
WebRTCommCall.prototype.sendDataMessage = function(text) {
    console.debug("WebRTCommCall:sendDataMessage()");
    if (this.webRTCommClient.isOpened())
    {
        if (this.isOpened())
        {
            var newWebRTCommDataMessage = new WebRTCommDataMessage(this.webRTCommClient, this);
            newWebRTCommDataMessage.content = text;
            if (this.isIncoming())
            {
                newWebRTCommDataMessage.to = this.callerPhoneNumber;
            }
            else
            {
                newWebRTCommDataMessage.to = this.calleePhoneNumber;
            }

            if (this.messageChannel && this.messageChannel.readyState === "open")
            {
                try
                {
                    this.messageChannel.send(newWebRTCommDataMessage.content);
                    if (this.eventListener.onWebRTCommDataMessageSentEvent)
                    {
                        var that = this;
                        setTimeout(function() {
                            try {
                                that.eventListener.onWebRTCommDataMessageSentEvent(newWebRTCommDataMessage);
                            }
                            catch (exception) {
                                console.error("WebRTCommCall:sendDataMessage(): catched exception in event listener:" + exception);
                            }
                        }, 1);
                    }
                }
                catch (exception)
                {
                    console.error("WebRTCommCall:sendDataMessage(): catched exception:" + exception);
                    throw "WebRTCommCall:sendDataMessage(): catched exception:" + exception;
                }
            }

            return newWebRTCommDataMessage;
        }
        else
        {
            console.error("WebRTCommCall:sendDataMessage(): bad state, unauthorized action");
            throw "WebRTCommCall:sendDataMessage(): bad state, unauthorized action";
        }
    }
    else
    {
        console.error("WebRTCommCall:sendDataMessage(): bad state, unauthorized action");
        throw "WebRTCommCall:sendDataMessage(): bad state, unauthorized action";
    }
};

/**
 * Mute local audio media stream
 * @public 
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception "not implemented by navigator"
 */
WebRTCommCall.prototype.muteLocalAudioMediaStream = function() {
    console.debug("WebRTCommCall:muteLocalAudioMediaStream()");
    if (this.configuration.localMediaStream && this.configuration.localMediaStream.signalingState === this.configuration.localMediaStream.LIVE)
    {
        var audioTracks = undefined;
        if (this.configuration.localMediaStream.audioTracks)
            audioTracks = this.configuration.localMediaStream.audioTracks;
        else if (this.configuration.localMediaStream.getAudioTracks)
            audioTracks = this.configuration.localMediaStream.getAudioTracks();
        if (audioTracks)
        {
            for (var i = 0; i < audioTracks.length; i++)
            {
                audioTracks[i].enabled = false;
            }
        }
        else
        {
            console.error("WebRTCommCall:muteLocalAudioMediaStream(): not implemented by navigator");
            throw "WebRTCommCall:muteLocalAudioMediaStream(): not implemented by navigator";
        }
    }
    else
    {
        console.error("WebRTCommCall:muteLocalAudioMediaStream(): bad state, unauthorized action");
        throw "WebRTCommCall:muteLocalAudioMediaStream(): bad state, unauthorized action";
    }
};

/**
 * Unmute local audio media stream
 * @public 
 */
WebRTCommCall.prototype.unmuteLocalAudioMediaStream = function() {
    console.debug("WebRTCommCall:unmuteLocalAudioMediaStream()");
    if (this.configuration.localMediaStream && this.configuration.localMediaStream.signalingState === this.configuration.localMediaStream.LIVE)
    {
        var audioTracks = undefined;
        if (this.configuration.localMediaStream.audioTracks)
            audioTracks = this.configuration.localMediaStream.audioTracks;
        else if (this.configuration.localMediaStream.getAudioTracks)
            audioTracks = this.configuration.localMediaStream.getAudioTracks();
        if (audioTracks)
        {
            for (var i = 0; i < audioTracks.length; i++)
            {
                audioTracks[i].enabled = true;
            }
        }
        else
        {
            console.error("WebRTCommCall:unmuteLocalAudioMediaStream(): not implemented by navigator");
            throw "WebRTCommCall:unmuteLocalAudioMediaStream(): not implemented by navigator";
        }
    }
    else
    {
        console.error("WebRTCommCall:unmuteLocalAudioMediaStream(): bad state, unauthorized action");
        throw "WebRTCommCall:unmuteLocalAudioMediaStream(): bad state, unauthorized action";
    }
};

/**
 * Mute remote audio media stream
 * @public 
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception "not implemented by navigator"
 */
WebRTCommCall.prototype.muteRemoteAudioMediaStream = function() {
    console.debug("WebRTCommCall:muteRemoteAudioMediaStream()");
    if (this.remoteBundledAudioVideoMediaStream && this.remoteBundledAudioVideoMediaStream.signalingState === this.remoteBundledAudioVideoMediaStream.LIVE)
    {
        var audioTracks = undefined;
        if (this.remoteBundledAudioVideoMediaStream.audioTracks)
            audioTracks = this.remoteBundledAudioVideoMediaStream.audioTracks;
        else if (this.remoteBundledAudioVideoMediaStream.getAudioTracks)
            audioTracks = this.remoteBundledAudioVideoMediaStream.getAudioTracks();
        if (audioTracks)
        {
            for (var i = 0; i < audioTracks.length; i++)
            {
                audioTracks[i].enabled = false;
            }
        }
        else
        {
            console.error("WebRTCommCall:muteRemoteAudioMediaStream(): not implemented by navigator");
            throw "WebRTCommCall:muteRemoteAudioMediaStream(): not implemented by navigator";
        }
    }
    else
    {
        console.error("WebRTCommCall:muteRemoteAudioMediaStream(): bad state, unauthorized action");
        throw "WebRTCommCall:muteRemoteAudioMediaStream(): bad state, unauthorized action";
    }
};

/**
 * Unmute remote audio media stream
 * @public 
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception "not implemented by navigator"
 */
WebRTCommCall.prototype.unmuteRemoteAudioMediaStream = function() {
    console.debug("WebRTCommCall:unmuteRemoteAudioMediaStream()");
    if (this.remoteBundledAudioVideoMediaStream && this.remoteBundledAudioVideoMediaStream.signalingState === this.remoteBundledAudioVideoMediaStream.LIVE)
    {
        var audioTracks = undefined;
        if (this.remoteBundledAudioVideoMediaStream.audioTracks)
            audioTracks = this.remoteBundledAudioVideoMediaStream.audioTracks;
        else if (this.remoteBundledAudioVideoMediaStream.getAudioTracks)
            audioTracks = this.remoteBundledAudioVideoMediaStream.getAudioTracks();
        if (audioTracks)
        {
            for (var i = 0; i < audioTracks.length; i++)
            {
                audioTracks[i].enabled = true;
            }
        }
        else
        {
            console.error("WebRTCommCall:unmuteRemoteAudioMediaStream(): not implemented by navigator");
            throw "WebRTCommCall:unmuteRemoteAudioMediaStream(): not implemented by navigator";
        }
    }
    else
    {
        console.error("WebRTCommCall:unmuteRemoteAudioMediaStream(): bad state, unauthorized action");
        throw "WebRTCommCall:unmuteRemoteAudioMediaStream(): bad state, unauthorized action";
    }
};

/**
 * Hide local video media stream
 * @public 
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception "not implemented by navigator"
 */
WebRTCommCall.prototype.hideLocalVideoMediaStream = function() {
    console.debug("WebRTCommCall:hideLocalVideoMediaStream()");
    if (this.configuration.localMediaStream && this.configuration.localMediaStream.signalingState === this.configuration.localMediaStream.LIVE)
    {
        var videoTracks = undefined;
        if (this.configuration.localMediaStream.videoTracks)
            videoTracks = this.configuration.localMediaStream.videoTracks;
        else if (this.configuration.localMediaStream.getVideoTracks)
            videoTracks = this.configuration.localMediaStream.getVideoTracks();
        if (videoTracks)
        {
            videoTracks.enabled = !videoTracks.enabled;
            for (var i = 0; i < videoTracks.length; i++)
            {
                videoTracks[i].enabled = false;
            }
        }
        else
        {
            console.error("WebRTCommCall:hideLocalVideoMediaStream(): not implemented by navigator");
            throw "WebRTCommCall:hideLocalVideoMediaStream(): not implemented by navigator";
        }
    }
    else
    {
        console.error("WebRTCommCall:hideLocalVideoMediaStream(): bad state, unauthorized action");
        throw "WebRTCommCall:hideLocalVideoMediaStream(): bad state, unauthorized action";
    }
};

/**
 * Show local video media stream
 * @public 
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception "not implemented by navigator"
 */
WebRTCommCall.prototype.showLocalVideoMediaStream = function() {
    console.debug("WebRTCommCall:showLocalVideoMediaStream()");
    if (this.configuration.localMediaStream && this.configuration.localMediaStream.signalingState === this.configuration.localMediaStream.LIVE)
    {
        var videoTracks = undefined;
        if (this.configuration.localMediaStream.videoTracks)
            videoTracks = this.configuration.localMediaStream.videoTracks;
        else if (this.configuration.localMediaStream.getVideoTracks)
            videoTracks = this.configuration.localMediaStream.getVideoTracks();
        if (videoTracks)
        {
            videoTracks.enabled = !videoTracks.enabled;
            for (var i = 0; i < videoTracks.length; i++)
            {
                videoTracks[i].enabled = true;
            }
        }
        else
        {
            console.error("WebRTCommCall:showLocalVideoMediaStream(): not implemented by navigator");
            throw "WebRTCommCall:showLocalVideoMediaStream(): not implemented by navigator";
        }
    }
    else
    {
        console.error("WebRTCommCall:showLocalVideoMediaStream(): bad state, unauthorized action");
        throw "WebRTCommCall:showLocalVideoMediaStream(): bad state, unauthorized action";
    }
};


/**
 * Hide remote video media stream
 * @public 
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception "not implemented by navigator"
 */
WebRTCommCall.prototype.hideRemoteVideoMediaStream = function() {
    console.debug("WebRTCommCall:hideRemoteVideoMediaStream()");
    if (this.remoteBundledAudioVideoMediaStream && this.remoteBundledAudioVideoMediaStream.signalingState === this.remoteBundledAudioVideoMediaStream.LIVE)
    {
        var videoTracks = undefined;
        if (this.remoteBundledAudioVideoMediaStream.videoTracks)
            videoTracks = this.remoteBundledAudioVideoMediaStream.videoTracks;
        else if (this.remoteBundledAudioVideoMediaStream.getVideoTracks)
            videoTracks = this.remoteBundledAudioVideoMediaStream.getVideoTracks();
        if (videoTracks)
        {
            videoTracks.enabled = !videoTracks.enabled;
            for (var i = 0; i < videoTracks.length; i++)
            {
                videoTracks[i].enabled = false;
            }
        }
        else
        {
            console.error("WebRTCommCall:hideRemoteVideoMediaStream(): not implemented by navigator");
            throw "WebRTCommCall:hideRemoteVideoMediaStream(): not implemented by navigator";
        }
    }
    else
    {
        console.error("WebRTCommCall:hideRemoteVideoMediaStream(): bad state, unauthorized action");
        throw "WebRTCommCall:hideRemoteVideoMediaStream(): bad state, unauthorized action";
    }
};

/**
 * Show remote video media stream
 * @public 
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception "not implemented by navigator"
 */
WebRTCommCall.prototype.showRemoteVideoMediaStream = function() {
    console.debug("WebRTCommCall:showRemoteVideoMediaStream()");
    if (this.remoteBundledAudioVideoMediaStream && this.remoteBundledAudioVideoMediaStream.signalingState === this.remoteBundledAudioVideoMediaStream.LIVE)
    {
        var videoTracks = undefined;
        if (this.remoteBundledAudioVideoMediaStream.videoTracks)
            videoTracks = this.remoteBundledAudioVideoMediaStream.videoTracks;
        else if (this.remoteBundledAudioVideoMediaStream.getVideoTracks)
            videoTracks = this.remoteBundledAudioVideoMediaStream.getVideoTracks();
        if (videoTracks)
        {
            videoTracks.enabled = !videoTracks.enabled;
            for (var i = 0; i < videoTracks.length; i++)
            {
                videoTracks[i].enabled = true;
            }
        }
        else
        {
            console.error("WebRTCommCall:showRemoteVideoMediaStream(): not implemented by navigator");
            throw "WebRTCommCall:showRemoteVideoMediaStream(): not implemented by navigator";
        }
    }
    else
    {
        console.error("WebRTCommCall:showRemoteVideoMediaStream(): bad state, unauthorized action");
        throw "WebRTCommCall:showRemoteVideoMediaStream(): bad state, unauthorized action";
    }
};


/**
 * Check configuration 
 * @private
 * @param {object}  configuration call configuration
 * @return true configuration ok false otherwise
 */
WebRTCommCall.prototype.checkConfiguration = function(configuration) {
    console.debug("WebRTCommCall:checkConfiguration()");

    var check = true;
    // displayName, audioCodecsFilter, videoCodecsFilter NOT mandatoty in configuration

    if (configuration.localMediaStream === undefined)
    {
        check = false;
        console.error("WebRTCommCall:checkConfiguration(): missing localMediaStream");
    }

    if (configuration.audioMediaFlag === undefined || (typeof(configuration.audioMediaFlag) !== 'boolean'))
    {
        check = false;
        console.error("WebRTCommCall:checkConfiguration(): missing audio media flag");
    }

    if (configuration.videoMediaFlag === undefined || (typeof(configuration.videoMediaFlag) !== 'boolean'))
    {
        check = false;
        console.error("WebRTCommCall:checkConfiguration(): missing video media flag");
    }

    if (configuration.messageMediaFlag === undefined || (typeof(configuration.messageMediaFlag) !== 'boolean'))
    {
        check = false;
        console.error("WebRTCommCall:checkConfiguration(): missing message media flag");
    }
    return check;
};

/**
 * Create RTCPeerConnection 
 * @private
 * @return true configuration ok false otherwise
 */
WebRTCommCall.prototype.createRTCPeerConnection = function() {
    console.debug("WebRTCommCall:createPeerConnection()");
    var rtcPeerConnectionConfiguration = {
        iceServers: []
    };

    this.peerConnectionState = 'new';
    var that = this;
    /* https://code.google.com/p/webrtcomm/issues/detail?id=14 */
    if(this.webRTCommClient.configuration.RTCPeerConnection.iceServers) {
    	rtcPeerConnectionConfiguration = this.webRTCommClient.configuration.RTCPeerConnection.iceServers;
    } else {
	    if (this.webRTCommClient.configuration.RTCPeerConnection.stunServer)
	    {
		rtcPeerConnectionConfiguration.iceServers.push({
		    url: "stun:" + this.webRTCommClient.configuration.RTCPeerConnection.stunServer
		});
	    }
	    if (this.webRTCommClient.configuration.RTCPeerConnection.turnServer
		    && this.webRTCommClient.configuration.RTCPeerConnection.turnLogin
		    && this.webRTCommClient.configuration.RTCPeerConnection.turnPassword)
	    {
		rtcPeerConnectionConfiguration.iceServers.push({
		    url: "turn:" + this.webRTCommClient.configuration.RTCPeerConnection.turnServer,
		    username: this.webRTCommClient.configuration.RTCPeerConnection.turnLogin,
		    credential: this.webRTCommClient.configuration.RTCPeerConnection.turnPassword
		});
	    }
    }


    console.debug("WebRTCommCall:createPeerConnection():rtcPeerConnectionConfiguration=" + JSON.stringify(rtcPeerConnectionConfiguration));
    console.debug("WebRTCommCall:createPeerConnection():peerConnectionConstraints=" + JSON.stringify(peerConnectionConstraints));

    if (window.webkitRTCPeerConnection)
    {
        // Google implementation
        var iceTransports = "all";
        if (this.webRTCommClient.configuration.RTCPeerConnection.forceTurnMediaRelay)
        {
            iceTransports = "relay";
        }

        var peerConnectionConstraints = {
            mandatory:
                    {
                        IceTransports: iceTransports
                    },
            optional: []
		//{
		    // SCTP Channels available in Chrome 31
                    //RtpDataChannels: true
                //}, {
		    // DTLS Mandatory and available in Chrome 35
                    //DtlsSrtpKeyAgreement: this.webRTCommClient.configuration.RTCPeerConnection.dtlsSrtpKeyAgreement
              //  }]
        };

        this.peerConnection = new window.webkitRTCPeerConnection(rtcPeerConnectionConfiguration, peerConnectionConstraints);
    }
    else if (window.mozRTCPeerConnection)
    {
        // Mozilla implementation
        this.peerConnection = new window.mozRTCPeerConnection(rtcPeerConnectionConfiguration, peerConnectionConstraints);
    }

    this.peerConnection.onaddstream = function(event) {
        that.onRtcPeerConnectionOnAddStreamEvent(event);
    };

    this.peerConnection.onremovestream = function(event) {
        that.onRtcPeerConnectionOnRemoveStreamEvent(event);
    };

    this.peerConnection.onstatechange = function(event) {
        that.onRtcPeerConnectionStateChangeEvent(event);
    };

    if (window.webkitRTCPeerConnection)
    {
        // Google implementation only for the time being
        this.peerConnection.onsignalingstatechange = function(event) {
            console.warn("RTCPeerConnection API update");
            that.onRtcPeerConnectionStateChangeEvent(event);
        };
    }

    this.peerConnection.onicecandidate = function(rtcIceCandidateEvent) {
        that.onRtcPeerConnectionIceCandidateEvent(rtcIceCandidateEvent);
    };

    this.peerConnection.ongatheringchange = function(event) {
        that.onRtcPeerConnectionGatheringChangeEvent(event);
    };

    this.peerConnection.onicechange = function(event) {
        that.onRtcPeerConnectionIceChangeEvent(event);
    };

    if (window.webkitRTCPeerConnection)
    {
        // Google implementation only for the time being
        this.peerConnection.oniceconnectionstatechange = function(event) {
            that.onRtcPeerConnectionIceChangeEvent(event);
        };
    }

    this.peerConnection.onopen = function(event) {
        that.onRtcPeerConnectionOnOpenEvent(event);
    };

    if (window.webkitRTCPeerConnection)
    {
        // Google implementation only for the time being
        this.peerConnection.onidentityresult = function(event) {
            that.onRtcPeerConnectionIdentityResultEvent(event);
        };
    }

    /* Obsolete
     this.peerConnection.onnegotiationneeded= function(event) {
     that.onRtcPeerConnectionIceNegotiationNeededEvent(event);
     }*/

    this.peerConnection.ondatachannel = function(event) {
        that.onRtcPeerConnectionOnMessageChannelEvent(event);
    };

    console.debug("WebRTCommCall:createPeerConnection(): this.peerConnection=" + JSON.stringify(this.peerConnection));
};

/**
 * Implementation of the PrivateCallConnector listener interface: process remote SDP offer event
 * @private 
 * @param {string} remoteSdpOffer Remote peer SDP offer
 */
WebRTCommCall.prototype.onPrivateCallConnectorRemoteSdpOfferEvent = function(remoteSdpOffer) {
    console.debug("WebRTCommCall:onPrivateCallConnectorSdpOfferEvent()");
    this.remoteSdpOffer = remoteSdpOffer;
};

/**
 * Implementation of the PrivateCallConnector listener interface: process remote SDP answer event
 * @private 
 * @param {string} remoteSdpAnswer
 * @throw exception internal error
 */
WebRTCommCall.prototype.onPrivateCallConnectorRemoteSdpAnswerEvent = function(remoteSdpAnswer) {
    console.debug("WebRTCommCall:onPrivateCallConnectorRemoteSdpAnswerEvent()");
    try
    {
        var sdpAnswer = undefined;
        if (window.webkitRTCPeerConnection)
        {
            sdpAnswer = new RTCSessionDescription({
                type: 'answer',
                sdp: remoteSdpAnswer
            });
        }
        else if (window.mozRTCPeerConnection)
        {
            sdpAnswer = new mozRTCSessionDescription({
                type: 'answer',
                sdp: remoteSdpAnswer
            });
        }

        var that = this;
        this.peerConnectionState = 'answer-received';
        this.peerConnection.setRemoteDescription(sdpAnswer, function() {
            that.onRtcPeerConnectionSetRemoteDescriptionSuccessEvent();
        }, function(error) {
            that.onRtcPeerConnectionSetRemoteDescriptionErrorEvent(error);
        });
    }
    catch (exception)
    {
        console.error("WebRTCommCall:onPrivateCallConnectorRemoteSdpAnswerEvent(): catched exception:" + exception);
        throw exception;
    }
};

/**
 * Implementation of the PrivateCallConnector listener interface: process call opened event
 * @private 
 */
WebRTCommCall.prototype.onPrivateCallConnectorCallOpenedEvent = function()
{
    console.debug("WebRTCommCall:onPrivateCallConnectorCallOpenedEvent()");
    // Notify event to the listener
    if (this.eventListener.onWebRTCommCallOpenEvent)
    {
        var that = this;
        setTimeout(function() {
            try {
                that.eventListener.onWebRTCommCallOpenEvent(that);
            }
            catch (exception)
            {
                console.error("WebRTCommCall:onPrivateCallConnectorCallOpenedEvent(): catched exception in listener:" + exception);
            }
        }, 1);
    }
};

/**
 * Implementation of the PrivateCallConnector listener interface: process call in progress event
 * @private 
 */
WebRTCommCall.prototype.onPrivateCallConnectorCallInProgressEvent = function()
{
    console.debug("WebRTCommCall:onPrivateCallConnectorCallInProgressEvent()");
    // Notify event to the listener
    if (this.eventListener.onWebRTCommCallInProgressEvent)
    {
        var that = this;
        setTimeout(function() {
            try {
                that.eventListener.onWebRTCommCallInProgressEvent(that);
            }
            catch (exception)
            {
                console.error("WebRTCommCall:onPrivateCallConnectorCallInProgressEvent(): catched exception in listener:" + exception);
            }
        }, 1);
    }
};

/**
 * Implementation of the PrivateCallConnector listener interface: process call error event
 * @private 
 * @param {string} error call control error
 */
WebRTCommCall.prototype.onPrivateCallConnectorCallOpenErrorEvent = function(error)
{
    console.debug("WebRTCommCall:onPrivateCallConnectorCallOpenErrorEvent():error=" + error);
    // Notify event to the listener
    if (this.eventListener.onWebRTCommCallOpenErrorEvent)
    {
        var that = this;
        setTimeout(function() {
            try {
                that.eventListener.onWebRTCommCallOpenErrorEvent(that, error);
            }
            catch (exception)
            {
                console.error("WebRTCommCall:onPrivateCallConnectorCallOpenErrorEvent(): catched exception in listener:" + exception);
            }
        }, 1);
    }
};

/**
 * Implementation of the PrivateCallConnector listener interface: process call ringing event
 * @private 
 * @param {string} callerPhoneNumber  caller contact identifier (e.g. bob@sip.net)
 * @param {string} callerDisplayName  caller contact identifier (e.g. bob@sip.net)
 */
WebRTCommCall.prototype.onPrivateCallConnectorCallRingingEvent = function(callerPhoneNumber, callerDisplayName)
{
    console.debug("WebRTCommCall:onPrivateCallConnectorCallRingingEvent():callerPhoneNumber=" + callerPhoneNumber);
    console.debug("WebRTCommCall:onPrivateCallConnectorCallRingingEvent():callerDisplayName=" + callerDisplayName);
    // Notify the closed event to the listener
    this.callerPhoneNumber = callerPhoneNumber;
    this.callerDisplayName = callerDisplayName;
    if (this.eventListener.onWebRTCommCallRingingEvent)
    {
        var that = this;
        setTimeout(function() {
            try {
                that.eventListener.onWebRTCommCallRingingEvent(that);
            }
            catch (exception)
            {
                console.error("WebRTCommCall:onPrivateCallConnectorCallRingingEvent(): catched exception in listener:" + exception);
            }
        }, 1);
    }
};

/**
 * Implementation of the PrivateCallConnector listener interface: process call ringing back event
 * @private 
 */
WebRTCommCall.prototype.onPrivateCallConnectorCallRingingBackEvent = function()
{
    console.debug("WebRTCommCall:onPrivateCallConnectorCallRingingBackEvent()");
    // Notify the closed event to the listener
    if (this.eventListener.onWebRTCommCallRingingBackEvent)
    {
        var that = this;
        setTimeout(function() {
            try {
                that.eventListener.onWebRTCommCallRingingBackEvent(that);
            }
            catch (exception)
            {
                console.error("WebRTCommCall:onPrivateCallConnectorCallRingingBackEvent(): catched exception in listener:" + exception);
            }
        }, 1);
    }
};


/**
 * Implementation of the PrivateCallConnector listener interface: process call closed event 
 * @private 
 */
WebRTCommCall.prototype.onPrivateCallConnectorCallClosedEvent = function()
{
    console.debug("WebRTCommCall:onPrivateCallConnectorCallClosedEvent()");
    this.connector = undefined;
    // Force communication close 
    try {
        this.close();
    } catch (exception) {
    }
};


/**
 * Implementation of the PrivateCallConnector listener interface: process call hangup event  
 * @private 
 */
WebRTCommCall.prototype.onPrivateCallConnectorCallHangupEvent = function()
{
    console.debug("WebRTCommCall:onPrivateCallConnectorCallHangupEvent()");
    // Notify the closed event to the listener
    if (this.eventListener.onWebRTCommCallHangupEvent)
    {
        var that = this;
        setTimeout(function() {
            try {
                that.eventListener.onWebRTCommCallHangupEvent(that);
            }
            catch (exception)
            {
                console.error("WebRTCommCall:onPrivateCallConnectorCallHangupEvent(): catched exception in listener:" + exception);
            }
        }, 1);
    }
};

/**
 * Implementation of the RTCPeerConnection listener interface: process RTCPeerConnection error event
 * @private 
 * @param {string} error internal error
 */
WebRTCommCall.prototype.onRtcPeerConnectionErrorEvent = function(error) {
    console.debug("WebRTCommCall:onRtcPeerConnectionErrorEvent(): error=" + error);
    // Critical issue, notify the error and close properly the call
    // Notify the error event to the listener
    if (this.eventListener.onWebRTCommCallOpenErrorEvent)
    {
        var that = this;
        setTimeout(function() {
            try {
                that.eventListener.onWebRTCommCallOpenErrorEvent(that, error);
            }
            catch (exception)
            {
                console.error("WebRTCommCall:onRtcPeerConnectionErrorEvent(): catched exception in listener:" + exception);
            }
        }, 1);
    }

    try {
        this.close();
    } catch (exception) {
    }
};


/**
 * Implementation of the RTCPeerConnection listener interface: handle RTCPeerConnection state machine
 * @private
 * @param {MediaStreamEvent} event  RTCPeerConnection Event
 */
WebRTCommCall.prototype.onRtcPeerConnectionOnAddStreamEvent = function(event) {
    try
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionOnAddStreamEvent(): event=" + event);
        console.debug("WebRTCommCall:onRtcPeerConnectionOnAddStreamEvent(): event.type=" + event.type);
        if (this.peerConnection)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionOnAddStreamEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
            console.debug("WebRTCommCall:onRtcPeerConnectionOnAddStreamEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
            console.debug("WebRTCommCall:onRtcPeerConnectionOnAddStreamEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
            console.debug("WebRTCommCall:onRtcPeerConnectionOnAddStreamEvent(): this.peerConnectionState=" + this.peerConnectionState);
	    this.remoteBundledAudioVideoMediaStream = event.stream;
	    // https://code.google.com/p/webrtcomm/issues/detail?id=22 Make sure to call WebRTCommCall on add stream event
            if (this.eventListener.onWebRTCommCallOpenedEvent)
            {
                var that = this;
                setTimeout(function() {
                    try {
		        console.debug("WebRTCommCall:calling onWebRTCommCallOpenedEvent(): event=" + event);
                        that.eventListener.onWebRTCommCallOpenedEvent(that);
		        console.debug("WebRTCommCall:onRtcPeerConnectionOnAddStreamEvent(): creating DTMF Sender");
			if (that.peerConnection.createDTMFSender) {
	    		    if (that.configuration.localMediaStream !== null) {
			        var localAudioTrack = that.configuration.localMediaStream.getAudioTracks()[0];
			        that.dtmfSender = that.peerConnection.createDTMFSender(localAudioTrack);
			        //that.dtmfSender.ontonechange = dtmfOnToneChange;
			        console.debug('Created DTMFSender');
			    } else {
			        console.debug('No local stream to create DTMF Sender');
			    }
	  	        } else {
	    		    console.warn('RTCPeerConnection method createDTMFSender() is not support by this browser.');
	  	        }
                    }
                    catch (exception)
                    {
                        console.error("WebRTCommCall:onRtcPeerConnectionOnAddStreamEvent(): catched exception in listener:" + exception);
                    }
                }, 1);
            }
        }
        else
        {
            console.warn("WebRTCommCall:onRtcPeerConnectionOnAddStreamEvent(): event ignored");
        }
    }
    catch (exception)
    {
        console.error("WebRTCommCall:onRtcPeerConnectionOnAddStreamEvent(): catched exception, exception:" + exception);
        this.onRtcPeerConnectionErrorEvent();
    }
};

/**
 * Implementation of the RTCPeerConnection listener interface: handle RTCPeerConnection state machine
 * @private
 * @param {MediaStreamEvent} event  RTCPeerConnection Event
 */
WebRTCommCall.prototype.onRtcPeerConnectionOnRemoveStreamEvent = function(event) {
    try
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionOnRemoveStreamEvent(): event=" + event);
        if (this.peerConnection)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionOnRemoveStreamEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
            console.debug("WebRTCommCall:onRtcPeerConnectionOnRemoveStreamEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
            console.debug("WebRTCommCall:onRtcPeerConnectionOnRemoveStreamEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
            console.debug("WebRTCommCall:onRtcPeerConnectionOnRemoveStreamEvent(): this.peerConnectionState=" + this.peerConnectionState);
            this.remoteBundledAudioVideoMediaStream = undefined;
        }
        else
        {
            console.warn("WebRTCommCall:onRtcPeerConnectionOnRemoveStreamEvent(): event ignored");
        }
    }
    catch (exception)
    {
        console.error("WebRTCommCall:onRtcPeerConnectionOnRemoveStreamEvent(): catched exception, exception:" + exception);
        this.onRtcPeerConnectionErrorEvent();
    }
};

/**
 * Implementation of the RTCPeerConnection listener interface: handle RTCPeerConnection state machine
 * @private
 * @param {RTCPeerConnectionIceEvent} rtcIceCandidateEvent  RTCPeerConnection Event
 */
WebRTCommCall.prototype.onRtcPeerConnectionIceCandidateEvent = function(rtcIceCandidateEvent) {
    try
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionIceCandidateEvent(): rtcIceCandidateEvent=" + JSON.stringify(rtcIceCandidateEvent.candidate));
        if (this.peerConnection)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionIceCandidateEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
            console.debug("WebRTCommCall:onRtcPeerConnectionIceCandidateEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
            console.debug("WebRTCommCall:onRtcPeerConnectionIceCandidateEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
            console.debug("WebRTCommCall:onRtcPeerConnectionIceCandidateEvent(): this.peerConnectionState=" + this.peerConnectionState);
            if (this.peerConnection.signalingState !== 'closed')
            {
                if (this.peerConnection.iceGatheringState === 'complete')
                {
                        if (this.peerConnectionState === 'preparing-offer')
                        {
                            var sdpOfferString = this.peerConnection.localDescription.sdp;
                            var parsedSdpOffer = this.setRtcPeerConnectionLocalDescription(this.peerConnection.localDescription);

                            // Apply modified SDP Offer
                            this.connector.invite(parsedSdpOffer);
                            this.peerConnectionState = 'offer-sent';
                        }
                        else if (this.peerConnectionState === 'preparing-answer')
                        {
                            var sdpAnswerString = this.peerConnection.localDescription.sdp;
                            var parsedSdpAnswer = this.setRtcPeerConnectionLocalDescription(this.peerConnection.localDescription);

                            this.connector.accept(parsedSdpAnswer);
                            this.peerConnectionState = 'established';
                            // Notify opened event to listener
                            if (this.eventListener.onWebRTCommCallOpenedEvent)
                            {
                                var that = this;
                                setTimeout(function() {
                                    try {
                                        that.eventListener.onWebRTCommCallOpenedEvent(that);
                                    }
                                    catch (exception)
                                    {
                                        console.error("WebRTCommCall:onRtcPeerConnectionIceCandidateEvent(): catched exception in listener:" + exception);
                                    }
                                }, 1);
                            }
                        }
                        else if (this.peerConnectionState === 'established')
                        {
                            // Why this last ice candidate event?
                        }
                        else
                        {
                            console.error("WebRTCommCall:onRtcPeerConnectionIceCandidateEvent(): RTCPeerConnection bad state!" + this.peerConnectionState);
                        }
                }
            }
            else
            {
                console.error("WebRTCommCall:onRtcPeerConnectionIceCandidateEvent(): RTCPeerConnection closed!");
            }
        }
        else
        {
            console.warn("WebRTCommCall:onRtcPeerConnectionIceCandidateEvent(): event ignored");
        }
    }
    catch (exception)
    {
        console.error("WebRTCommCall:onRtcPeerConnectionIceCandidateEvent(): catched exception, exception:" + exception);
        this.onRtcPeerConnectionErrorEvent(exception);
    }
};

/**
 * Implementation of the RTCPeerConnection listener interface: handle RTCPeerConnection state machine
 * @private
 * @param {RTCSessionDescription} sdpOffer  RTCPeerConnection SDP offer event
 */
WebRTCommCall.prototype.onRtcPeerConnectionCreateOfferSuccessEvent = function(sdpOffer) {
    try
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionCreateOfferSuccessEvent(): sdpOffer=" + JSON.stringify(sdpOffer));
        if (this.peerConnection)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateOfferSuccessEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateOfferSuccessEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateOfferSuccessEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateOfferSuccessEvent(): this.peerConnectionState=" + this.peerConnectionState);

            if (this.peerConnectionState === 'new')
            {
                // Preparing offer.
                var that = this;
                this.peerConnectionState = 'preparing-offer';
		if (window.webkitRTCPeerConnection) {
			this.setRtcPeerConnectionLocalDescription(sdpOffer);
		}
                
                this.peerConnection.setLocalDescription(sdpOffer, function() {
                    that.onRtcPeerConnectionSetLocalDescriptionSuccessEvent();
                }, function(error) {
                    that.onRtcPeerConnectionSetLocalDescriptionErrorEvent(error);
                });
            }
            else
            {
                console.error("WebRTCommCall:onRtcPeerConnectionCreateOfferSuccessEvent(): RTCPeerConnection bad state!");
            }
        }
        else
        {
            console.warn("WebRTCommCall:onRtcPeerConnectionCreateOfferSuccessEvent(): event ignored");
        }
    }
    catch (exception)
    {
        console.error("WebRTCommCall:onRtcPeerConnectionCreateOfferSuccessEvent(): catched exception, exception:" + exception);
        this.onRtcPeerConnectionErrorEvent();
    }
};

WebRTCommCall.prototype.setRtcPeerConnectionLocalDescription = function(sdpOffer) {
	var sdpOfferString = sdpOffer.sdp;
	var sdpParser = new SDPParser();
	var parsedSdpOffer = sdpParser.parse(sdpOfferString);

	// Check if offer is ok with the requested media constraints
	if (window.webkitRTCPeerConnection) {
		if (this.configuration.videoMediaFlag === false)
		{
		    this.removeMediaDescription(parsedSdpOffer, "video");
		}

		if (this.configuration.audioMediaFlag === false)
		{
		    this.removeMediaDescription(parsedSdpOffer, "audio");
		}
	}

	if (this.configuration.audioCodecsFilter || this.configuration.videoCodecsFilter || this.configuration.opusFmtpCodecsParameters)
	{
	    try
	    {
		// Apply audio/video codecs filter to RTCPeerConnection SDP offer to
		this.applyConfiguredCodecFilterOnSessionDescription(parsedSdpOffer);
	    }
	    catch (exception)
	    {
		console.error("WebRTCommCall:onRtcPeerConnectionCreateOfferSuccessEvent(): configured codec filtering has failded, use inital RTCPeerConnection SDP offer");
	    }
	}

	// Check if offer is ok with the requested RTCPeerConnection constraints
	if (this.webRTCommClient.configuration.RTCPeerConnection.forceTurnMediaRelay === true)
	{
	    this.forceTurnMediaRelay(parsedSdpOffer);
	}
	// Allow patching of chrome ice-options for interconnect with Mobicents Media Server, commented for now but to be made configurable
	// this.patchChromeIce(parsedSdpOffer, "ice-options");
	console.debug("WebRTCommCall:onRtcPeerConnectionCreateOfferSuccessEvent(): parsedSdpOffer=" + parsedSdpOffer);

	// Apply modified SDP Offer
	sdpOffer.sdp = parsedSdpOffer;
	this.peerConnectionLocalDescription = sdpOffer;

	return parsedSdpOffer;
}

/**
 * Implementation of the RTCPeerConnection listener interface: handle RTCPeerConnection state machine
 * @private
 * @param {object} error  RTCPeerConnection SDP offer error event
 */
WebRTCommCall.prototype.onRtcPeerConnectionCreateOfferErrorEvent = function(error) {
    try
    {
        console.error("WebRTCommCall:onRtcPeerConnectionCreateOfferErrorEvent():error=" + JSON.stringify(error));
        if (this.peerConnection)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateOfferErrorEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateOfferErrorEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateOfferErrorEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateOfferErrorEvent(): this.peerConnectionState=" + this.peerConnectionState);
            throw "WebRTCommCall:onRtcPeerConnectionCreateOfferErrorEvent():error=" + error;
        }
        else
        {
            console.warn("WebRTCommCall:onRtcPeerConnectionCreateOfferErrorEvent(): event ignored");
        }
    }
    catch (exception)
    {
        console.error("WebRTCommCall:onRtcPeerConnectionCreateOfferErrorEvent(): catched exception, exception:" + exception);
        this.onRtcPeerConnectionErrorEvent();
    }
};

/**
 * Implementation of the RTCPeerConnection listener interface: handle RTCPeerConnection state machine
 * @private
 */
WebRTCommCall.prototype.onRtcPeerConnectionSetLocalDescriptionSuccessEvent = function() {
    try
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionSuccessEvent():" + JSON.stringify(this.peerConnection));
        if (this.peerConnection)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionSuccessEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
            console.debug("WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionSuccessEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
            console.debug("WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionSuccessEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
            console.debug("WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionSuccessEvent(): this.peerConnectionState=" + this.peerConnectionState);
        }
        else
        {
            console.warn("WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionSuccessEvent(): event ignored");
        }
    }
    catch (exception)
    {
        console.error("WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionSuccessEvent(): catched exception, exception:" + exception);
        this.onRtcPeerConnectionErrorEvent();
    }
};

/**
 * Implementation of the RTCPeerConnection listener interface: handle RTCPeerConnection state machine
 * @private
 * @param {object} error  RTCPeerConnection SDP offer error event
 */
WebRTCommCall.prototype.onRtcPeerConnectionSetLocalDescriptionErrorEvent = function(error) {
    try
    {
        console.error("WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionErrorEvent():error=" + JSON.stringify(error));
        if (this.peerConnection)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionErrorEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
            console.debug("WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionErrorEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
            console.debug("WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionErrorEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
            console.debug("WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionErrorEvent(): this.peerConnectionState=" + this.peerConnectionState);
            throw "WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionErrorEvent():error=" + error;
        }
        else
        {
            console.warn("WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionErrorEvent(): event ignored");
        }
    }
    catch (exception)
    {
        console.error("WebRTCommCall:onRtcPeerConnectionSetLocalDescriptionErrorEvent(): catched exception, exception:" + exception);
        this.onRtcPeerConnectionErrorEvent();
    }
};

/**
 * Implementation of the RTCPeerConnection listener interface: handle RTCPeerConnection state machine
 * @private
 * @param {RTCSessionDescription} answer  RTCPeerConnection SDP answer event
 */
WebRTCommCall.prototype.onRtcPeerConnectionCreateAnswerSuccessEvent = function(sdpAnswser) {
    try
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionCreateAnswerSuccessEvent():answer=" + JSON.stringify(sdpAnswser));
        if (this.peerConnection)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateAnswerSuccessEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateAnswerSuccessEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateAnswerSuccessEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateAnswerSuccessEvent(): this.peerConnectionState=" + this.peerConnectionState);

            if (this.peerConnectionState === 'offer-received')
            {
                // Prepare answer.
                var that = this;
                this.peerConnectionState = 'preparing-answer';             
                var sdpAnswerString = sdpAnswser.sdp;
                var sdpParser = new SDPParser();
                var parsedSdpAnswer = sdpParser.parse(sdpAnswerString);

                // Check if offer is ok with the requested media constraints
                // Can not remove/add SDP m lines
                
                if (this.configuration.audioCodecsFilter || this.configuration.videoCodecsFilter || this.configuration.opusFmtpCodecsParameters)
                {
                    try
                    {
                        // Apply audio/video codecs filter to RTCPeerConnection SDP offer to
                        this.applyConfiguredCodecFilterOnSessionDescription(parsedSdpAnswer);
                    }
                    catch (exception)
                    {
                        console.error("WebRTCommCall:onRtcPeerConnectionCreateAnswerSuccessEvent(): configured codec filtering has failded, use inital RTCPeerConnection SDP offer");
                    }
                }
            	// Allow patching of chrome ice-options for interconnect with Mobicents Media Server, commented for now but to be made configurable
		// this.patchChromeIce(parsedSdpOffer, "ice-options");

                sdpAnswser.sdp = parsedSdpAnswer;
                this.peerConnectionLocalDescription = parsedSdpAnswer;
                this.peerConnection.setLocalDescription(sdpAnswser, function() {
                    that.onRtcPeerConnectionSetLocalDescriptionSuccessEvent();
                }, function(error) {
                    that.onRtcPeerConnectionSetLocalDescriptionErrorEvent(error);
                });
            }
            else
            {
                console.error("WebRTCommCall:onRtcPeerConnectionCreateAnswerSuccessEvent(): RTCPeerConnection bad state!");
            }
        }
        else
        {
            console.warn("WebRTCommCall:onRtcPeerConnectionCreateAnswerSuccessEvent(): event ignored");
        }
    }
    catch (exception)
    {
        console.error("WebRTCommCall:onRtcPeerConnectionCreateAnswerSuccessEvent(): catched exception, exception:" + exception);
        this.onRtcPeerConnectionErrorEvent();
    }
};

/**
 * Implementation of the RTCPeerConnection listener interface: handle RTCPeerConnection state machine
 * @private
 * @param {String} error  SDP error
 */
WebRTCommCall.prototype.onRtcPeerConnectionCreateAnswerErrorEvent = function(error) {
    console.error("WebRTCommCall:onRtcPeerConnectionCreateAnswerErrorEvent():error=" + JSON.stringify(error));
    try
    {
        if (this.peerConnection)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateAnswerErrorEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateAnswerErrorEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateAnswerErrorEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
            console.debug("WebRTCommCall:onRtcPeerConnectionCreateAnswerErrorEvent(): this.peerConnectionState=" + this.peerConnectionState);
        }
        else
        {
            console.warn("WebRTCommCall:onRtcPeerConnectionCreateAnswerErrorEvent(): event ignored");
        }
    }
    catch (exception)
    {
        console.error("WebRTCommCall:onRtcPeerConnectionCreateAnswerErrorEvent(): catched exception, exception:" + exception);
        this.onRtcPeerConnectionErrorEvent();
    }
};

/**
 * RTCPeerConnection listener implementation
 * @private
 */
WebRTCommCall.prototype.onRtcPeerConnectionSetRemoteDescriptionSuccessEvent = function() {
    try
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionSuccessEvent()");
        if (this.peerConnection)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionSuccessEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
            console.debug("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionSuccessEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
            console.debug("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionSuccessEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
            console.debug("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionSuccessEvent(): this.peerConnectionState=" + this.peerConnectionState);

            if (this.peerConnectionState === 'answer-received')
            {
                this.peerConnectionState = 'established';
		console.debug("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionSuccessEvent(): this.peerConnectionState=" + this.peerConnectionState);
                // Notify closed event to listener
                if (this.eventListener.onWebRTCommCallOpenedEvent)
                {
                    var that = this;
                    setTimeout(function() {
                        try {
                            that.eventListener.onWebRTCommCallOpenedEvent(that);
                        }
                        catch (exception)
                        {
                            console.error("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionSuccessEvent(): catched exception in listener:" + exception);
                        }
                    }, 1);
                }
            }
            else if (this.peerConnectionState === 'offer-received')
            {
                var that = this;
                if (window.webkitRTCPeerConnection)
                {
                    var sdpConstraints = {
                        mandatory:
                                {
                                    OfferToReceiveAudio: this.configuration.audioMediaFlag,
                                    OfferToReceiveVideo: this.configuration.videoMediaFlag
                                },
                        optional: []
                    };
		    console.debug("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionSuccessEvent():sdpConstraints=" + JSON.stringify(sdpConstraints));
                    this.peerConnection.createAnswer(function(answer) {
                        that.onRtcPeerConnectionCreateAnswerSuccessEvent(answer);
                    }, function(error) {
                        that.onRtcPeerConnectionCreateAnswerErrorEvent(error);
                    }, sdpConstraints);
                }
                else if (window.mozRTCPeerConnection)
                {
                    var sdpConstraints = {
                            offerToReceiveAudio: this.configuration.audioMediaFlag,
                            offerToReceiveVideo: this.configuration.videoMediaFlag,
                            mozDontOfferDataChannel: !this.configuration.messageMediaFlag
                    };
		    console.debug("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionSuccessEvent():sdpConstraints=" + JSON.stringify(sdpConstraints));
                    this.peerConnection.createAnswer(function(answer) {
                        that.onRtcPeerConnectionCreateAnswerSuccessEvent(answer);
                    }, function(error) {
                        that.onRtcPeerConnectionCreateAnswerErrorEvent(error);
                    }, sdpConstraints);
                }
            }
            else {
                console.error("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionSuccessEvent(): RTCPeerConnection bad state!");
            }
        }
        else
        {
            console.warn("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionSuccessEvent(): event ignored");
        }
    }
    catch (exception)
    {
        console.error("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionSuccessEvent(): catched exception, exception:" + exception);
        this.onRtcPeerConnectionErrorEvent();
    }
};

/**
 * RTCPeerConnection listener implementation
 * @private
 * @param {String} error  SDP error
 */
WebRTCommCall.prototype.onRtcPeerConnectionSetRemoteDescriptionErrorEvent = function(error) {
    try
    {
        console.error("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionErrorEvent():error=" + JSON.stringify(error));
        if (this.peerConnection)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionErrorEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
            console.debug("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionErrorEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
            console.debug("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionErrorEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
            console.debug("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionErrorEvent(): this.peerConnectionState=" + this.peerConnectionState);
            throw "WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionErrorEvent():error=" + error;
        }
        else
        {
            console.warn("WebRTCommCall:onRtcPeerConnectionSetRemoteDescriptionErrorEvent(): event ignored");
        }
    }
    catch (exception)
    {
        this.onRtcPeerConnectionErrorEvent(error);
    }
};

/**
 * RTCPeerConnection listener implementation
 * @private
 * @param {Event} event  RTCPeerConnection open event
 */
WebRTCommCall.prototype.onRtcPeerConnectionOnOpenEvent = function(event) {
    console.debug("WebRTCommCall:onRtcPeerConnectionOnOpenEvent(): event=" + event);
    if (this.peerConnection)
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionOnOpenEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
        console.debug("WebRTCommCall:onRtcPeerConnectionOnOpenEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
        console.debug("WebRTCommCall:onRtcPeerConnectionOnOpenEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
        console.debug("WebRTCommCall:onRtcPeerConnectionOnOpenEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
        console.debug("WebRTCommCall:onRtcPeerConnectionOnOpenEvent(): this.peerConnectionState=" + this.peerConnectionState);
    }
    else
    {
        console.warn("WebRTCommCall:onRtcPeerConnectionOnOpenEvent(): event ignored");
    }
};

/**
 * RTCPeerConnection listener implementation
 * @private
 * @param {Event} event  RTCPeerConnection open event
 */
WebRTCommCall.prototype.onRtcPeerConnectionStateChangeEvent = function(event) {
    console.debug("WebRTCommCall:onRtcPeerConnectionStateChangeEvent(): event=" + event);
    if (this.peerConnection)
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionStateChangeEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
        console.debug("WebRTCommCall:onRtcPeerConnectionStateChangeEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
        console.debug("WebRTCommCall:onRtcPeerConnectionStateChangeEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
        console.debug("WebRTCommCall:onRtcPeerConnectionStateChangeEvent(): this.peerConnectionState=" + this.peerConnectionState);
        if (this.peerConnection && this.peerConnection.signalingState === 'closed')
            this.peerConnection = null;
    }
    else
    {
        console.warn("WebRTCommCall:onRtcPeerConnectionStateChangeEvent(): event ignored");
    }
};

/**
 * RTCPeerConnection listener implementation
 * @private
 * @param {Event} event  RTCPeerConnection ICE negociation Needed event
 */
WebRTCommCall.prototype.onRtcPeerConnectionIceNegotiationNeededEvent = function(event) {
    console.debug("WebRTCommCall:onRtcPeerConnectionIceNegotiationNeededEvent():event=" + event);
    if (this.peerConnection)
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionIceNegotiationNeededEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
        console.debug("WebRTCommCall:onRtcPeerConnectionIceNegotiationNeededEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
        console.debug("WebRTCommCall:onRtcPeerConnectionIceNegotiationNeededEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
        console.debug("WebRTCommCall:onRtcPeerConnectionIceNegotiationNeededEvent(): this.peerConnectionState=" + this.peerConnectionState);
    }
    else
    {
        console.warn("WebRTCommCall:onRtcPeerConnectionIceNegotiationNeededEvent(): event ignored");
    }
};

/**
 * RTCPeerConnection listener implementation
 * @private
 * @param {Event} event  RTCPeerConnection ICE change event
 */
WebRTCommCall.prototype.onRtcPeerConnectionGatheringChangeEvent = function(event) {
    console.debug("WebRTCommCall:onRtcPeerConnectionGatheringChangeEvent():event=" + event);
    if (this.peerConnection)
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionGatheringChangeEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
        console.debug("WebRTCommCall:onRtcPeerConnectionGatheringChangeEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
        console.debug("WebRTCommCall:onRtcPeerConnectionGatheringChangeEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
        console.debug("WebRTCommCall:onRtcPeerConnectionGatheringChangeEvent(): this.peerConnectionState=" + this.peerConnectionState);

        if (this.peerConnection.signalingState !== 'closed')
        {
            if (this.peerConnection.iceGatheringState === "complete")
            {
                    if (this.peerConnectionState === 'preparing-offer')
                    {
                        var sdpOfferString = this.peerConnection.localDescription.sdp;
                        var parsedSdpOffer = this.setRtcPeerConnectionLocalDescription(this.peerConnection.localDescription);

                        // Apply modified SDP Offer
                        this.connector.invite(parsedSdpOffer);
                        this.peerConnectionState = 'offer-sent';
                    }
                    else if (this.peerConnectionState === 'preparing-answer')
                    {
                        var sdpAnswerString = this.peerConnection.localDescription.sdp;
                        var parsedSdpAnswer = this.setRtcPeerConnectionLocalDescription(this.peerConnection.localDescription);

                        this.connector.accept(parsedSdpAnswer);
                        this.peerConnectionState = 'established';
                        // Notify opened event to listener
                        if (this.eventListener.onWebRTCommCallOpenedEvent)
                        {
                            var that = this;
                            setTimeout(function() {
                                try {
                                    that.eventListener.onWebRTCommCallOpenedEvent(that);
                                }
                                catch (exception)
                                {
                                    console.error("WebRTCommCall:onRtcPeerConnectionGatheringChangeEvent(): catched exception in listener:" + exception);
                                }
                            }, 1);
                        }
                    }
                    else if (this.peerConnectionState === 'established')
                    {
                        // Why this last ice candidate event?
                    }
                    else
                    {
                        console.error("WebRTCommCall:onRtcPeerConnectionGatheringChangeEvent(): RTCPeerConnection bad state!");
                    }
            }
        }
        else
        {
            console.error("WebRTCommCall:onRtcPeerConnectionGatheringChangeEvent(): RTCPeerConnection closed!");
        }
    }
    else
    {
        console.warn("WebRTCommCall:onRtcPeerConnectionGatheringChangeEvent(): event ignored");
    }
};

/**
 * RTCPeerConnection listener implementation
 * @private
 * @param {Event} event  RTCPeerConnection open event
 */
WebRTCommCall.prototype.onRtcPeerConnectionIceChangeEvent = function(event) {
    console.debug("WebRTCommCall:onRtcPeerConnectionIceChangeEvent():event=" + event);
    if (this.peerConnection)
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionIceChangeEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
        console.debug("WebRTCommCall:onRtcPeerConnectionIceChangeEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
        console.debug("WebRTCommCall:onRtcPeerConnectionIceChangeEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
        console.debug("WebRTCommCall:onRtcPeerConnectionIceChangeEvent(): this.peerConnectionState=" + this.peerConnectionState);
    }
    else
    {
        console.warn("WebRTCommCall:onRtcPeerConnectionIceChangeEvent(): event ignored");
    }
};

/**
 * RTCPeerConnection listener implementation
 * @private
 * @param {Event} event  RTCPeerConnection identity event
 */
WebRTCommCall.prototype.onRtcPeerConnectionIdentityResultEvent = function(event) {
    console.debug("WebRTCommCall:onRtcPeerConnectionIdentityResultEvent():event=" + event);
    if (this.peerConnection)
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionIdentityResultEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
        console.debug("WebRTCommCall:onRtcPeerConnectionIdentityResultEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
        console.debug("WebRTCommCall:onRtcPeerConnectionIdentityResultEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
        console.debug("WebRTCommCall:onRtcPeerConnectionIdentityResultEvent(): this.peerConnectionState=" + this.peerConnectionState);
    }
    else
    {
        console.warn("WebRTCommCall:onRtcPeerConnectionIdentityResultEvent(): event ignored");
    }
};

/**
 * RTCPeerConnection listener implementation
 * @private
 * @param {Event} event  RTCPeerConnection data channel event
 */
WebRTCommCall.prototype.onRtcPeerConnectionOnMessageChannelEvent = function(event) {
    console.debug("WebRTCommCall:onRtcPeerConnectionOnMessageChannelEvent():event=" + JSON.stringify(event));
    if (this.peerConnection)
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionOnMessageChannelEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
        console.debug("WebRTCommCall:onRtcPeerConnectionOnMessageChannelEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
        console.debug("WebRTCommCall:onRtcPeerConnectionOnMessageChannelEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
        console.debug("WebRTCommCall:onRtcPeerConnectionOnMessageChannelEvent(): this.peerConnectionState=" + this.peerConnectionState);
        this.messageChannel = event.channel;
        console.debug("WebRTCommCall:onRtcPeerConnectionOnMessageChannelEvent(): this.messageChannel.label=" + this.messageChannel.label);
        console.debug("WebRTCommCall:onRtcPeerConnectionOnMessageChannelEvent(): this.messageChannel.reliable=" + this.messageChannel.reliable);
        console.debug("WebRTCommCall:onRtcPeerConnectionOnMessageChannelEvent(): this.messageChannel.binaryType=" + this.messageChannel.binaryType);
        var that = this;
        this.messageChannel.onopen = function(event) {
            that.onRtcPeerConnectionMessageChannelOnOpenEvent(event);
        };
        this.messageChannel.onclose = function(event) {
            that.onRtcPeerConnectionMessageChannelOnClose(event);
        };
        this.messageChannel.onerror = function(event) {
            that.onRtcPeerConnectionMessageChannelOnErrorEvent(event);
        };
        this.messageChannel.onmessage = function(event) {
            that.onRtcPeerConnectionMessageChannelOnMessageEvent(event);
        };
    }
    else
    {
        console.warn("WebRTCommCall:onRtcPeerConnectionOnMessageChannelEvent(): event ignored");
    }
};

WebRTCommCall.prototype.onRtcPeerConnectionMessageChannelOnOpenEvent = function(event) {
    console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnOpenEvent():event=" + event);
    if (this.peerConnection)
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnOpenEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnOpenEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnOpenEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnOpenEvent(): this.peerConnectionState=" + this.peerConnectionState);
        if (this.messageChannel)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnOpenEvent(): this.messageChannel.readyState=" + this.messageChannel.readyState);
            console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnOpenEvent(): this.messageChannel.binaryType=" + this.messageChannel.bufferedAmmount);
        }
    }
    else
    {
        console.warn("WebRTCommCall:onRtcPeerConnectionMessageChannelOnOpenEvent(): event ignored");
    }
};

WebRTCommCall.prototype.onRtcPeerConnectionMessageChannelOnClose = function(event) {
    console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnClose():event=" + event);
    if (this.peerConnection)
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnClose(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnClose(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnClose(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnClose(): this.peerConnectionState=" + this.peerConnectionState);
        if (this.messageChannel)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnClose(): this.messageChannel.readyState=" + this.messageChannel.readyState);
            console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnClose(): this.messageChannel.binaryType=" + this.messageChannel.bufferedAmmount);
        }
    }
    else
    {
        console.warn("WebRTCommCall:onRtcPeerConnectionMessageChannelOnClose(): event ignored");
    }
};

WebRTCommCall.prototype.onRtcPeerConnectionMessageChannelOnErrorEvent = function(event) {
    console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnErrorEvent():event=" + event);
    if (this.peerConnection)
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnErrorEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnErrorEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnErrorEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnErrorEvent(): this.peerConnectionState=" + this.peerConnectionState);
        if (this.messageChannel)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnErrorEvent(): this.messageChannel.readyState=" + this.messageChannel.readyState);
            console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnErrorEvent(): this.messageChannel.binaryType=" + this.messageChannel.bufferedAmmount);
        }
    }
    else
    {
        console.warn("WebRTCommCall:onRtcPeerConnectionMessageChannelOnErrorEvent(): event ignored");
    }
};

WebRTCommCall.prototype.onRtcPeerConnectionMessageChannelOnMessageEvent = function(event) {
    console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnMessageEvent():event=" + event);
    if (this.peerConnection)
    {
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnMessageEvent(): this.peerConnection.signalingState=" + this.peerConnection.signalingState);
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnMessageEvent(): this.peerConnection.iceGatheringState=" + this.peerConnection.iceGatheringState);
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnMessageEvent(): this.peerConnection.iceConnectionState=" + this.peerConnection.iceConnectionState);
        console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnMessageEvent(): this.peerConnectionState=" + this.peerConnectionState);
        if (this.messageChannel)
        {
            console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnMessageEvent(): this.messageChannel.readyState=" + this.messageChannel.readyState);
            console.debug("WebRTCommCall:onRtcPeerConnectionMessageChannelOnMessageEvent(): this.messageChannel.binaryType=" + this.messageChannel.bufferedAmmount);
            if (this.eventListener.onWebRTCommDataMessageReceivedEvent)
            {
                // Build WebRTCommMessage
                var newWebRTCommDataMessage = new WebRTCommDataMessage(this.webRTCommClient, this);
                newWebRTCommDataMessage.content=event.data;
                var that = this;
                setTimeout(function() {
                    try {
                        that.eventListener.onWebRTCommDataMessageReceivedEvent(newWebRTCommDataMessage);
                    }
                    catch (exception)
                    {
                        console.error("WebRTCommCall:onRtcPeerConnectionMessageChannelOnMessageEvent(): catched exception in listener:" + exception);
                    }
                }, 1);
            }
        }
    }
    else
    {
        console.warn("WebRTCommCall:onRtcPeerConnectionMessageChannelOnMessageEvent(): event ignored");
    }
};

/**
 * Modifiy SDP based on configured codec filter
 * @private
 * @param {SessionDescription} sessionDescription  JAIN (gov.nist.sdp) SDP offer object 
 */
WebRTCommCall.prototype.applyConfiguredCodecFilterOnSessionDescription = function(sessionDescription) {
    if (sessionDescription instanceof SessionDescription)
    {
        try
        {
            console.debug("WebRTCommCall:applyConfiguredCodecFilterOnSessionDescription(): sessionDescription=" + sessionDescription);
            // Deep copy the media descriptions
            var mediaDescriptions = sessionDescription.getMediaDescriptions(false);
            for (var i = 0; i < mediaDescriptions.length; i++)
            {
                var mediaDescription = mediaDescriptions[i];
                var mediaField = mediaDescription.getMedia();
                var mediaType = mediaField.getType();
                if (mediaType === "audio")
                {
                    if (this.configuration.audioCodecsFilter)
                    {
                        var offeredAudioCodecs = this.getOfferedCodecsInMediaDescription(mediaDescription);
                        // Filter offered codec first
                        var splitAudioCodecsFilters = (this.configuration.audioCodecsFilter).split(",");
                        this.applyCodecFiltersOnOfferedCodecs(offeredAudioCodecs, splitAudioCodecsFilters);
                        // Apply modification on audio media description
                        this.updateMediaDescription(mediaDescription, offeredAudioCodecs, splitAudioCodecsFilters);
                    }

                    // Add OPUS parameter if required
                    if (this.configuration.opusFmtpCodecsParameters)
                    {
                        this.updateOpusMediaDescription(mediaDescription, this.configuration.opusFmtpCodecsParameters);
                    }
                }
                else if (mediaType === "video" && this.configuration.videoCodecsFilter)
                {
                    var offeredVideoCodecs = this.getOfferedCodecsInMediaDescription(mediaDescription);
                    // Filter offered codec
                    var splitVideoCodecFilter = (this.configuration.videoCodecsFilter).split(",");
                    this.applyCodecFiltersOnOfferedCodecs(offeredVideoCodecs, splitVideoCodecFilter);
                    // Apply modification on video media description
                    this.updateMediaDescription(mediaDescription, offeredVideoCodecs, splitVideoCodecFilter);
                }
            }
        }
        catch (exception)
        {
            console.error("WebRTCommCall:applyConfiguredCodecFilterOnSessionDescription(): catched exception, exception:" + exception);
            throw exception;
        }
    }
    else
    {
        throw "WebRTCommCall:applyConfiguredCodecFilterOnSessionDescription(): bad arguments"
    }
};

/**
 * Get offered codecs in media description
 * @private
 * @param {MediaDescription} mediaDescription  JAIN (gov.nist.sdp) MediaDescription object 
 * @return offeredCodec JSON object { "0":"PCMU", "111":"OPUS", .....} 
 */
WebRTCommCall.prototype.getOfferedCodecsInMediaDescription = function(mediaDescription) {
    console.debug("WebRTCommCall:getOfferedCodecsInMediaDescription()");
    if (mediaDescription instanceof MediaDescription)
    {
        var mediaFormats = mediaDescription.getMedia().getFormats(false);
        var foundCodecs = {};

        // Set static payload type and codec name
        for (var j = 0; j < mediaFormats.length; j++)
        {
            var payloadType = mediaFormats[j];
            console.debug("WebRTCommCall:getOfferedCodecsInMediaDescription(): payloadType=" + payloadType);
            console.debug("WebRTCommCall:getOfferedCodecsInMediaDescription(): this.codecNames[payloadType]=" + this.codecNames[payloadType]);
            foundCodecs[payloadType] = this.codecNames[payloadType];
        }

        // Set dynamic payload type and codec name 
        var attributFields = mediaDescription.getAttributes();
        for (var k = 0; k < attributFields.length; k++)
        {
            var attributField = attributFields[k];
            if (attributField.getName() === "rtpmap")
            {
                try
                {
                    var rtpmapValue = attributField.getValue();
                    var splitRtpmapValue = rtpmapValue.split(" ");
                    var payloadType = splitRtpmapValue[0];
                    var codecInfo = splitRtpmapValue[1];
                    var splittedCodecInfo = codecInfo.split("/");
                    var codecName = splittedCodecInfo[0];
                    foundCodecs[payloadType] = codecName.toUpperCase();
                    console.debug("WebRTCommCall:getOfferedCodecsInMediaDescription(): payloadType=" + payloadType);
                    console.debug("WebRTCommCall:getOfferedCodecsInMediaDescription(): codecName=" + codecName);
                }
                catch (exception)
                {
                    console.error("WebRTCommCall:getOfferedCodecsInMediaDescription(): rtpmap/fmtp format not supported");
                }
            }
        }
        return foundCodecs;
    }
    else
    {
        throw "WebRTCommCall:getOfferedCodecsInMediaDescription(): bad arguments"
    }
};

/**
 * Get offered codec list
 * @private
 * @param {JSON object} foundCodecs  
 * @param {Array} codecFilters  
 */
WebRTCommCall.prototype.applyCodecFiltersOnOfferedCodecs = function(foundCodecs, codecFilters) {
    console.debug("WebRTCommCall:applyCodecFiltersOnOfferedCodecs()");
    if (typeof(foundCodecs) === 'object' && codecFilters instanceof Array)
    {
        for (var offeredMediaCodecPayloadType in foundCodecs) {
            var filteredFlag = false;
            for (var i = 0; i < codecFilters.length; i++)
            {
                if (foundCodecs[offeredMediaCodecPayloadType] === codecFilters[i]) {
                    filteredFlag = true;
                    break;
                }
            }
            if (filteredFlag === false)
            {
                delete(foundCodecs[offeredMediaCodecPayloadType]);
            }
        }
    }
    else
    {
        throw "WebRTCommCall:applyCodecFiltersOnOfferedCodecs(): bad arguments"
    }
};

/**
 * Update offered media description avec configured filters
 * @private
 * @param {MediaDescription} mediaDescription  JAIN (gov.nist.sdp) MediaDescription object 
 * @param {JSON object} filteredCodecs 
 * @param {Array} codecFilters  
 */
WebRTCommCall.prototype.updateMediaDescription = function(mediaDescription, filteredCodecs, codecFilters) {
    console.debug("WebRTCommCall:updateMediaDescription()");
    if (mediaDescription instanceof MediaDescription && typeof(filteredCodecs) === 'object' && codecFilters instanceof Array)
    {
        // Build new media field format lis
        var newFormatListArray = new Array();
        for (var i = 0; i < codecFilters.length; i++)
        {
            for (var offeredCodecPayloadType in filteredCodecs)
            {
                if (filteredCodecs[offeredCodecPayloadType] === codecFilters[i]) {
                    newFormatListArray.push(offeredCodecPayloadType);
                    break;
                }
            }
        }
        mediaDescription.getMedia().setFormats(newFormatListArray);
        // Remove obsolte rtpmap attributs 
        var newAttributeFieldArray = new Array();
        var attributFields = mediaDescription.getAttributes();
        for (var k = 0; k < attributFields.length; k++)
        {
            var attributField = attributFields[k];
            if (attributField.getName() === "rtpmap" || attributField.getName() === "fmtp")
            {
                try
                {
                    var rtpmapValue = attributField.getValue();
                    var splitedRtpmapValue = rtpmapValue.split(" ");
                    var payloadType = splitedRtpmapValue[0];
                    if (filteredCodecs[payloadType] !== undefined)
                        newAttributeFieldArray.push(attributField);
                }
                catch (exception)
                {
                    console.error("WebRTCommCall:updateMediaDescription(): rtpmap/fmtp format not supported");
                }
            }
            else
                newAttributeFieldArray.push(attributField);
        }
        mediaDescription.setAttributes(newAttributeFieldArray);
    }
    else
    {
        throw "WebRTCommCall:updateMediaDescription(): bad arguments"
    }
};

/**
 * Update offered OPUS media description avec required FMTP parameters
 * @private
 * @param {MediaDescription} mediaDescription  JAIN (gov.nist.sdp) MediaDescription object 
 * @param {string} opusMediaFmtpParameters FMTP OPUS parameters
 */
WebRTCommCall.prototype.updateOpusMediaDescription = function(mediaDescription, opusMediaFmtpParameters) {
    console.debug("WebRTCommCall:updateOpusMediaDescription()");
    if (mediaDescription instanceof MediaDescription && typeof(opusMediaFmtpParameters) === 'string')
    {
        // Find OPUS payload Type 
        var opusPayloadType = undefined;
        var attributFields = mediaDescription.getAttributes();
        for (var i = 0; i < attributFields.length; i++)
        {
            var attributField = attributFields[i];
            if (attributField.getName() === "rtpmap")
            {
                try
                {
                    var rtpmapValue = attributField.getValue().toLowerCase();
                    if (rtpmapValue.indexOf("opus") >= 0)
                    {
                        var splitedRtpmapValue = rtpmapValue.split(" ");
                        opusPayloadType = splitedRtpmapValue[0];
                        break;
                    }
                }
                catch (exception)
                {
                    console.error("WebRTCommCall:updateMediaDescription(): rtpmap/fmtp format not supported");
                }
            }
        }

        if (opusPayloadType)
        {
            console.debug("WebRTCommCall:updateOpusMediaDescription():opusPayloadType=" + opusPayloadType);
            // Update FMTP OPUS SDP parameter  
            for (var j = 0; j < attributFields.length; j++)
            {
                var attributField = attributFields[j];
                if (attributField.getName() === "fmtp")
                {
                    try
                    {
                        var fmtpValue = attributField.getValue();
                        var splitedFmtpValue = rtpmapValue.split(" ");
                        var payloadType = splitedFmtpValue[0];
                        if (opusPayloadType === payloadType)
                        {
                            attributField.setValue(fmtpValue + " " + opusMediaFmtpParameters);
                            console.debug("WebRTCommCall:updateOpusMediaDescription():fmtp=" + attributField.getValue());
                        }
                    }
                    catch (exception)
                    {
                        console.error("WebRTCommCall:updateMediaDescription(): rtpmap/fmtp format not supported");
                    }
                }
            }
        }
    }
    else
    {
        throw "WebRTCommCall:updateMediaDescription(): bad arguments"
    }
};

/**
 * Modifiy SDP based on configured codec filter
 * @private
 * @param {SessionDescription} sessionDescription  JAIN (gov.nist.sdp) SDP offer object 
 * @param {String} mediaTypeToRemove  audi/video 
 */
WebRTCommCall.prototype.patchChromeIce = function(sessionDescription, attributeToCheck) {
    console.debug("WebRTCommCall:patchChromeIce()");
    if (sessionDescription instanceof SessionDescription)
    {
        try
        {
	    var otherAttributes = sessionDescription.getAttributes(false);
	    if (otherAttributes != null) {
		for (var i = 0; i <  otherAttributes.length; i++) 
		{
		    var attributField = otherAttributes[i];
		    if (attributField.getName() === attributeToCheck)
		    {
			console.debug("WebRTCommCall:patchChromeIce(), found ice-options session attribute trying to patch");
		        try
		        {
		            var rtpmapValue = attributField.getValue().toLowerCase();
		            if (rtpmapValue.indexOf("google-ice") >= 0)
		            {
				console.debug("WebRTCommCall:patchChromeIce(), found google-ice session attribute trying to patch");+				
		                //attributField.setValue("trickle");
				attributFields.remove(i);
	                        break;
		            }
		        }
		        catch (exception)
		        {
		            console.error("WebRTCommCall:updateMediaDescription(): rtpmap/fmtp format not supported");
		        }
		    }
		}	
	    }	    
            var mediaDescriptions = sessionDescription.getMediaDescriptions(false);
            for (var i = 0; i < mediaDescriptions.length; i++)
            {
                var attributFields = mediaDescriptions[i].getAttributes();
		for (var j = 0; j < attributFields.length; j++)
		{
		    var attributField = attributFields[j];
		    if (attributField.getName() === attributeToCheck)
		    {
			console.debug("WebRTCommCall:patchChromeIce(), found ice-options media attribute trying to patch");
		        try
		        {
		            var rtpmapValue = attributField.getValue().toLowerCase();
		            if (rtpmapValue.indexOf("google-ice") >= 0)
		            {
				console.debug("WebRTCommCall:patchChromeIce(), found google-ice mediajattribute trying to patch");+				
		                //attributField.setValue("trickle");
				attributFields.remove(j);
	                        break;
		            }
		        }
		        catch (exception)
		        {
		            console.error("WebRTCommCall:updateMediaDescription(): rtpmap/fmtp format not supported");
		        }
		    }
		}
            }
        }
        catch (exception)
        {
            console.error("WebRTCommCall:patchChromeIce(): catched exception, exception:" + exception);
            throw exception;
        }
    }
    else
    {
        throw "WebRTCommCall:patchChromeIce(): bad arguments"
    }
};

/**
 * Modifiy SDP based on configured codec filter
 * @private
 * @param {SessionDescription} sessionDescription  JAIN (gov.nist.sdp) SDP offer object 
 * @param {String} mediaTypeToRemove  audi/video 
 */
WebRTCommCall.prototype.removeMediaDescription = function(sessionDescription, mediaTypeToRemove) {
    console.debug("WebRTCommCall:removeMediaDescription()");
    if (sessionDescription instanceof SessionDescription)
    {
        try
        {
            var mediaDescriptions = sessionDescription.getMediaDescriptions(false);
            for (var i = 0; i < mediaDescriptions.length; i++)
            {
                var mediaDescription = mediaDescriptions[i];
                var mediaField = mediaDescription.getMedia();
                var mediaType = mediaField.getType();
                if (mediaType === mediaTypeToRemove)
                {
                    mediaDescriptions.remove(i);
                    break;
                }
            }

	    if (window.mozRTCPeerConnection) {
		    var attributes = sessionDescription.getAttributes(false);
		    for (var i = 0; i < attributes.length; i++)
		    {
			var attribute = attributes[i];
			var attributeValue = attribute.getValue();
			if("BUNDLE sdparta_0 sdparta_1" === attributeValue) {
				if ("video" === mediaTypeToRemove)
				{
				    attribute.setValue("BUNDLE sdparta_0");
				    break;
				}
				if ("audio" === mediaTypeToRemove)
				{
				    attribute.setValue("BUNDLE sdparta_1");
				    break;
				}
			}
		    }
	    }
        }
        catch (exception)
        {
            console.error("WebRTCommCall:removeMediaDescription(): catched exception, exception:" + exception);
            throw exception;
        }
    }
    else
    {
        throw "WebRTCommCall:removeMediaDescription(): bad arguments"
    }
};

/**
 * Modifiy SDP, remove non "relay" ICE candidates
 * @private
 * @param {SessionDescription} sessionDescription  JAIN (gov.nist.sdp) SDP offer object 
 */
WebRTCommCall.prototype.forceTurnMediaRelay = function(sessionDescription) {
    console.debug("WebRTCommCall:forceTurnMediaRelay()");
    if (sessionDescription instanceof SessionDescription)
    {
        try
        {
            var mediaDescriptions = sessionDescription.getMediaDescriptions(false);
            for (var i = 0; i < mediaDescriptions.length; i++)
            {
                var mediaDescription = mediaDescriptions[i];
                var newAttributeFieldArray = new Array();
                var attributFields = mediaDescription.getAttributes();
                for (var k = 0; k < attributFields.length; k++)
                {
                    var attributField = attributFields[k];
                    if (attributField.getName() === "candidate")
                    {
                        var candidateValue = attributField.getValue();
                        var isRelayCandidate = candidateValue.indexOf("typ relay") > 0;
                        if (isRelayCandidate)
                        {
                            newAttributeFieldArray.push(attributField);
                        }
                    }
                    else
                        newAttributeFieldArray.push(attributField);
                }
                mediaDescription.setAttributes(newAttributeFieldArray);
            }
        }
        catch (exception)
        {
            console.error("WebRTCommCall:forceTurnMediaRelay(): catched exception, exception:" + exception);
            throw exception;
        }
    }
    else
    {
        throw "WebRTCommCall:forceTurnMediaRelay(): bad arguments"
    }
};




/**
 * @class WebRTCommMessage
 * @classdesc Implements WebRTComm message  
 * @constructor
 * @public
 * @param  {WebRTCommClient} webRTCommClient WebRTComm client owner 
 * @param  {WebRTCommCall} webRTCommCall WebRTComm call owner 
 * @author Laurent STRULLU (laurent.strullu@orange.com) 
 */ 
WebRTCommMessage = function(webRTCommClient, webRTCommCall)
{
    console.debug("WebRTCommMessage:WebRTCommMessage()");
    if((webRTCommClient instanceof WebRTCommClient) || (webRTCommCall instanceof WebRTCommCall))
    {
        this.id=undefined;
        this.webRTCommClient=webRTCommClient;
        this.webRTCommCall=webRTCommCall;
        this.connector= this.webRTCommClient.connector.createPrivateSessionConnector(this);
        this.text=undefined;
        this.from=undefined;
        this.to=undefined;
    }
    else 
    {
        throw "WebRTCommMessage:WebRTCommMessage(): bad arguments"      
    }
};


/**
 * Get message id
 * @public
 * @returns {String} id  
 */ 
WebRTCommMessage.prototype.getId= function() {
    return this.connector.getId();  
};

/**
 * Get message sender identity
 * @public
 * @returns {String} from  
 */ 
WebRTCommMessage.prototype.getFrom= function() {
    return this.from;  
};

/**
 * Get message recever identity
 * @public
 * @returns {String} to  
 */ 
WebRTCommMessage.prototype.getTo= function() {
    return this.to;  
};

/**
 * Get message 
 * @public
 * @returns {String} message  
 */ 
WebRTCommMessage.prototype.getText= function() {
    return this.text;  
};

/**
 * Get related WebRTCommCall  
 * @public
 * @returns {WebRTCommCall} WebRTCommCall 
 */ 
WebRTCommMessage.prototype.getLinkedWebRTCommCall= function() {
        return this.webRTCommCall;
};
/**
 * @class WebRTCommClient
 * @classdesc Main class of the WebRTComm Framework providing high level communication service: call and be call
 * @constructor
 * @public
 * @param  {object} eventListener event listener object implementing WebRTCommClient and WebRTCommCall listener interface
 */
WebRTCommClient = function(eventListener)
{
    if (typeof eventListener === 'object')
    {
        this.id = "WebRTCommClient" + Math.floor(Math.random() * 2147483648);
        console.debug("WebRTCommClient:WebRTCommClient():this.id=" + this.id);
        this.eventListener = eventListener;
        this.configuration = undefined;
        this.connector = undefined;
        this.closePendingFlag = false;
    }
    else
    {
        throw "WebRTCommClient:WebRTCommClient(): bad arguments"
    }
};

/**
 * SIP call control protocol mode 
 * @public
 * @constant
 */
WebRTCommClient.prototype.SIP = "SIP";


/**
 * Get opened/closed status 
 * @public
 * @returns {boolean} true if opened, false if closed
 */
WebRTCommClient.prototype.isOpened = function() {
    if (this.connector)
        return this.connector.isOpened();
    else
        return false;
};

/**
 * Get client configuration
 * @public
 * @returns {object} configuration
 */
WebRTCommClient.prototype.getConfiguration = function() {
    return this.configuration;
};

/**
 * Open the WebRTC communication client, asynchronous action, opened or error event are notified to the eventListener
 * @public 
 * @param {object} configuration  WebRTC communication client configuration <br>
 * <p> Client configuration sample: <br>
 * { <br>
 * <span style="margin-left: 30px">communicationMode:WebRTCommClient.prototype.SIP,<br></span>
 * <span style="margin-left: 30px">sip: {,<br></span>
 * <span style="margin-left: 60px">sipUriContactParameters:undefined,<br></span>
 * <span style="margin-left: 60px">sipUserAgent:"WebRTCommTestWebApp/0.0.1",<br></span>
 * <span style="margin-left: 60px">sipUserAgentCapabilities=undefined,<br></span>
 * <span style="margin-left: 60px">sipOutboundProxy:"ws://localhost:5082",<br></span>
 * <span style="margin-left: 60px">sipDomain:"sip.net",<br></span>
 * <span style="margin-left: 60px">sipUserName:"alice",<br></span>
 * <span style="margin-left: 60px">sipLogin:"alice@sip.net,<br></span>
 * <span style="margin-left: 60px">sipPassword:"1234567890",<br></span>
 * <span style="margin-left: 60px">sipRegisterMode:true,<br></span>
 * <span style="margin-left: 30px">}<br></span>
 * <span style="margin-left: 30px">RTCPeerConnection: {,<br></span>
 * <span style="margin-left: 60px"stunServer:undefined,<br></span>
 * <span style="margin-left: 30px">}<br></span>
 * }<br>
 *  </p>
 * @throw {String} Exception "bad argument, check API documentation"
 * @throw {String} Exception "bad configuration, missing parameter"
 * @throw {String} Exception "bad state, unauthorized action"
 * @throw {String} Exception [internal error]
 */
WebRTCommClient.prototype.open = function(configuration) {
    console.debug("WebRTCommClient:open(): configuration=" + JSON.stringify(configuration));
    if (typeof(configuration) === 'object')
    {
        if (this.isOpened() === false)
        {
            if (this.checkConfiguration(configuration) === true)
            {
                this.configuration = configuration;
                if (configuration.communicationMode === WebRTCommClient.prototype.SIP)
                {
                    this.connector = new PrivateJainSipClientConnector(this);
                    this.connector.open(this.configuration.sip);
                }
            }
            else
            {
                console.error("WebRTCommClient:open(): bad configuration");
                throw "WebRTCommClient:open(): bad configuration";
            }
        }
        else
        {
            console.error("WebRTCommClient:open(): bad state, unauthorized action");
            throw "WebRTCommClient:open(): bad state, unauthorized action";
        }
    }
    else
    {
        console.error("WebRTCommClient:open(): bad argument, check API documentation");
        throw "WebRTCommClient:open(): bad argument, check API documentation"
    }
};

/**
 * Close the WebRTC communication client, asynchronous action, closed event is notified to the eventListener
 * @public 
 * @throw {String} Exception "bad argument, check API documentation"
 * @throw {String} Exception "bad configuration, missing parameter"
 * @throw {String} Exception "bad state, unauthorized action"
 */
WebRTCommClient.prototype.close = function() {
    console.debug("WebRTCommClient:close()");
    if (this.isOpened())
    {
        try
        {
            this.closePendingFlag = true;
            this.connector.close();
        }
        catch (exception) {
            console.error("WebRTCommClient:close(): catched exception:" + exception);
            // Force notification of closed event to listener
            this.closePendingFlag = false;
            this.connector = undefined;
            if (this.eventListener.onWebRTCommClientClosedEvent !== undefined)
            {
                var that = this;
                setTimeout(function() {
                    try {
                        that.eventListener.onWebRTCommClientClosedEvent(that);
                    }
                    catch (exception)
                    {
                        console.error("WebRTCommClient:onWebRTCommClientClosed(): catched exception in event listener:" + exception);
                    }
                }, 1);
            }
        }
    }
};



/**
 * Send a short text message using transport (e.g SIP)  implemented by the connector
 * @public 
 * @param {String} to destination identifier (Tel URI, SIP URI: sip:bob@sip.net)
 * @param {String} text Message to send <br>
 * @throw {String} Exception "bad argument, check API documentation"
 * @throw {String} Exception "bad configuration, missing parameter"
 * @throw {String} Exception "bad state, unauthorized action"
 * @returns {WebRTCommMessage} new created WebRTCommMessage object
 */
WebRTCommClient.prototype.sendMessage = function(to, text)
{
    try
    {
        console.debug("WebRTCommClient:sendMessage(): to=" + to);
        console.debug("WebRTCommClient:sendMessage(): text=" + text);
        if (this.isOpened())
        {
            var newWebRTCommMessage = new WebRTCommMessage(this,undefined);
            newWebRTCommMessage.to = to;
            newWebRTCommMessage.text = text;
            newWebRTCommMessage.connector.send();
            return newWebRTCommMessage;
        }
        else
        {
            console.error("WebRTCommClient:sendMessage(): bad state, unauthorized action");
            throw "WebRTCommClient:sendMessage(): bad state, unauthorized action";
        }
    }
    catch (exception)
    {
        console.error("WebRTCommClient:sendMessage(): catched exception:" + exception);
        throw "WebRTCommClient:sendMessage(): catched exception:" + exception;
    }
};

/**
 * Request a WebRTC communication, asynchronous action, call events are notified to the eventListener 
 * @public 
 * @param {string} calleePhoneNumber Callee contact identifier (Tel URI, SIP URI: sip:bob@sip.net)
 * @param {object} callConfiguration Communication configuration <br>
 * <p> Communication configuration sample: <br>
 * { <br>
 * <span style="margin-left: 30px">displayName:alice,<br></span>
 * <span style="margin-left: 30px">localMediaStream: [LocalMediaStream],<br></span>
 * <span style="margin-left: 30px">audioMediaFlag:true,<br></span>
 * <span style="margin-left: 30px">videoMediaFlag:false,<br></span>
 * <span style="margin-left: 30px">dataMediaFlag:false,<br></span>
 * <span style="margin-left: 30px">audioCodecsFilter:PCMA,PCMU,OPUS,<br></span>
 * <span style="margin-left: 30px">videoCodecsFilter:VP8,H264,<br></span>
 * <span style="margin-left: 30px">opusFmtpCodecsParameters:maxaveragebitrate=128000,<br></span>
 * }<br>
 * </p>
 * @returns {WebRTCommCall} new created WebRTCommCall object
 * @throw {String} Exception "bad argument, check API documentation"
 * @throw {String} Exception "bad configuration, missing parameter"
 * @throw {String} Exception "bad state, unauthorized action"
 */
WebRTCommClient.prototype.call = function(calleePhoneNumber, callConfiguration) {
    console.debug("WebRTCommClient:call():calleePhoneNumber=" + calleePhoneNumber);
    console.debug("WebRTCommClient:call():callConfiguration=" + JSON.stringify(callConfiguration));
    try
    {
        if (typeof(calleePhoneNumber) === 'string' && typeof(callConfiguration) === 'object')
        {
            if (this.isOpened())
            {
                var newWebRTCommCall = new WebRTCommCall(this);
                newWebRTCommCall.connector = this.connector.createPrivateSessionConnector(newWebRTCommCall);
                newWebRTCommCall.open(calleePhoneNumber, callConfiguration);
                return newWebRTCommCall;
            }
            else
            {
                console.error("WebRTCommClient:call(): bad state, unauthorized action");
                throw "WebRTCommClient:call(): bad state, unauthorized action";
            }
        }
        else
        {
            console.error("WebRTCommClient:call(): bad argument, check API documentation");
            throw "WebRTCommClient:call(): bad argument, check API documentation"
        }
    }
    catch (exception) {
        console.error("WebRTCommClient:call(): catched exception:" + exception);
        throw exception;
    }
};


/**
 * Check validity of the client configuration 
 * @private
 * @param {object} configuration client configuration
 *  * <p> Client configuration sample: <br>
 * { <br>
 * <span style="margin-left: 30px">communicationMode:WebRTCommClient.prototype.SIP,<br></span>
 * <span style="margin-left: 30px">sip: {,<br></span>
 * <span style="margin-left: 60px">sipUriContactParameters:undefined,<br></span>
 * <span style="margin-left: 60px">sipUserAgent:"WebRTCommTestWebApp/0.0.1",<br></span>
 * <span style="margin-left: 60px">sipUserAgentCapabilities=undefined,<br></span>
 * <span style="margin-left: 60px">sipOutboundProxy:"ws://localhost:5082",<br></span>
 * <span style="margin-left: 60px">sipDomain:"sip.net",<br></span>
 * <span style="margin-left: 60px">sipUserName:"alice",<br></span>
 * <span style="margin-left: 60px">sipLogin:"alice@sip.net,<br></span>
 * <span style="margin-left: 60px">sipPassword:"1234567890",<br></span>
 * <span style="margin-left: 60px">sipRegisterMode:true,<br></span>
 * <span style="margin-left: 30px">}<br></span>
 * <span style="margin-left: 30px">RTCPeerConnection: {,<br></span>
 * <span style="margin-left: 60px"stunServer:undefined,<br></span>
 * <span style="margin-left: 30px">}<br></span>
 * }<br>
 *  </p>
 * @returns {boolean} true valid false unvalid
 */
WebRTCommClient.prototype.checkConfiguration = function(configuration) {

    console.debug("WebRTCommClient:checkConfiguration(): configuration=" + JSON.stringify(configuration));
    var check = true;
    if (configuration.communicationMode !== undefined)
    {
        if (configuration.communicationMode === WebRTCommClient.prototype.SIP)
        {
        }
        else
        {
            check = false;
            console.error("WebRTCommClient:checkConfiguration(): unsupported communicationMode");
        }
    }
    else
    {
        check = false;
        console.error("WebRTCommClient:checkConfiguration(): missing configuration parameter communicationMode");
    }
    return check;
};

/**
 * Implements PrivateClientConnector opened event listener interface
 * @private
 */
WebRTCommClient.prototype.onPrivateClientConnectorOpenedEvent = function()
{
    console.debug("WebRTCommClient:onPrivateClientConnectorOpenedEvent()");
    if (this.eventListener.onWebRTCommClientOpenedEvent !== undefined)
    {
        var that = this;
        setTimeout(function() {
            try {
                that.eventListener.onWebRTCommClientOpenedEvent();
            }
            catch (exception) {
                console.error("WebRTCommClient:onPrivateClientConnectorOpenedEvent(): catched exception in event listener:" + exception);
            }
        }, 1);
    }
};

/**
 * Implements PrivateClientConnector error event listener interface
 * @private
 * @param {string} error Error message
 */
WebRTCommClient.prototype.onPrivateClientConnectorOpenErrorEvent = function(error)
{
    console.debug("WebRTCommClient:onPrivateClientConnectorOpenErrorEvent():error:" + error);
    // Force closing of the client
    try {
        this.close();
    } catch (exception) {
    }

    if (this.eventListener.onWebRTCommClientOpenErrorEvent !== undefined)
    {
        var that = this;
        setTimeout(function() {
            try {
                that.eventListener.onWebRTCommClientOpenErrorEvent(error);
            }
            catch (exception) {
                console.error("WebRTCommClient:onPrivateClientConnectorOpenErrorEvent(): catched exception in event listener:" + exception);
            }
        }, 1);
    }
};

/**
 * Implements PrivateClientConnector closed event listener interface
 * @callback PrivatePrivateClientConnector interface
 * @private
 */

WebRTCommClient.prototype.onPrivateClientConnectorClosedEvent = function()
{
    console.debug("WebRTCommClient:onPrivateClientConnectorClosedEvent()");
    var wasOpenedFlag = this.isOpened() || this.closePendingFlag;

    // Close properly the client
    try {
        if (this.closePendingFlag === false)
            this.close();
        else
            this.connector = undefined;
    } catch (exception) {
    }

    if (wasOpenedFlag && (this.eventListener.onWebRTCommClientClosedEvent !== undefined))
    {
        var that = this;
        setTimeout(function() {
            try {
                that.eventListener.onWebRTCommClientClosedEvent();
            }
            catch (exception) {
                console.error("WebRTCommClient:onPrivateClientConnectorClosedEvent(): catched exception in event listener:" + exception);
            }
        }, 1);
    }
    else if (!wasOpenedFlag && (this.eventListener.onWebRTCommClientOpenErrorEvent !== undefined))
    {
        var that = this;
        setTimeout(function() {
            try {
                that.eventListener.onWebRTCommClientOpenErrorEvent("Connection to WebRTCommServer has failed");
            }
            catch (exception) {
                console.error("WebRTCommClient:onWebRTCommClientOpenErrorEvent(): catched exception in event listener:" + exception);
            }
        }, 1);
    }
};
/**
 * @class WebRTCommClientEventListenerInterface
 * @classdesc Abstract class describing  WebRTCommClient event listener interface 
 *            required to be implented by the webapp 
 * @constructor
 * @public
 */ 
 WebRTCommClientEventListenerInterface = function(){
};
 
/**
 * Open event
 * @public
 */ 
WebRTCommClientEventListenerInterface.prototype.onWebRTCommClientOpenedEvent= function() {
    throw "WebRTCommClientEventListenerInterface:onWebRTCommClientOpenedEvent(): not implemented;"; 
};

/**
 * Open error event 
 * @public
 * @param {String} error open error message
 */
WebRTCommClientEventListenerInterface.prototype.onWebRTCommClientOpenErrorEvent= function(error) {
    throw "WebRTCommClientEventListenerInterface:onWebRTCommClientOpenErrorEvent(): not implemented;"; 
};


/**
 * Close event 
 * @public
 */
WebRTCommClientEventListenerInterface.prototype.onWebRTCommClientClosedEvent= function() {
    throw "WebRTCommClientEventListenerInterface:onWebRTCommClientClosedEvent(): not implemented;"; 
};
/**
 * @class WebRTCommCallEventListenerInterface
 * @classdesc Abstract class describing  WebRTCommClient event listener interface 
 *            required to be implented by the webapp 
 * @constructor
 * @public
 */
WebRTCommCallEventListenerInterface = function() {
};

/**
 * Open event
 * @public
 * @param {WebRTCommCall} webRTCommCall source WebRTCommCall object
 */
WebRTCommCallEventListenerInterface.prototype.onWebRTCommCallOpenedEvent = function(webRTCommCall) {
    throw "WebRTCommCallEventListenerInterface:onWebRTCommCallOpenedEvent(): not implemented;";
};


/**
 * In progress event 
 * @public
 * @param {WebRTCommCall} webRTCommCall source WebRTCommCall object
 */
WebRTCommCallEventListenerInterface.prototype.onWebRTCommCallInProgressEvent = function(webRTCommCall) {
    throw "WebRTCommCallEventListenerInterface:onWebRTCommCallInProgressEvent(): not implemented;";
};

/**
 * Open error  event
 * @public
 * @param {WebRTCommCall} webRTCommCall source WebRTCommCall object
 * @param {String} error error message
 */
WebRTCommCallEventListenerInterface.prototype.onWebRTCommCallOpenErrorEvent = function(webRTCommCall, error) {
    throw "WebRTCommCallEventListenerInterface:onWebRTCommCallOpenErrorEvent(): not implemented;";
};

/**
 * Open error  event
 * @public
 * @param {WebRTCommCall} webRTCommCall source WebRTCommCall object
 */
WebRTCommCallEventListenerInterface.prototype.onWebRTCommCallRingingEvent = function(webRTCommCall) {
    throw "WebRTCommCallEventListenerInterface:onWebRTCommCallRingingEvent(): not implemented;";
};

/**
 * Open error  event
 * @public
 * @param {WebRTCommCall} webRTCommCall source WebRTCommCall object
 */
WebRTCommCallEventListenerInterface.prototype.onWebRTCommCallRingingBackEvent = function(webRTCommCall) {
    throw "WebRTCommCallEventListenerInterface:onWebRTCommCallRingingBackEvent(): not implemented;";
};

/**
 * Open error  event
 * @public
 * @param {WebRTCommCall} webRTCommCall source WebRTCommCall object
 */
WebRTCommCallEventListenerInterface.prototype.onWebRTCommCallHangupEvent = function(webRTCommCall) {
    throw "WebRTCommCallEventListenerInterface:onWebRTCommCallHangupEvent(): not implemented;";
};
/**
 * @class WebRTCommMessageEventListenerInterface
 * @classdesc Abstract class describing  WebRTCommMessage event listener interface 
 *            required to be implented by the webapp 
 * @constructor
 * @public
 */ 
 WebRTCommMessageEventListenerInterface = function(){
};
 

/**
 * Received message event
 * @public
 * @param {WebRTCommMessage} message object
 */
WebRTCommMessageEventListenerInterface.prototype.onWebRTCommMessageReceivedEvent = function(message) {
    throw "WebRTCommCallEventListenerInterface:onWebRTCommMessageReceivedEvent(): not implemented;";
};

/**
 * Received message event
 * @public
 * @param {WebRTCommMessage} message object
 */
WebRTCommMessageEventListenerInterface.prototype.onWebRTCommMessageSentEvent = function(message) {
    throw "WebRTCommCallEventListenerInterface:onWebRTCommMessageSentEvent(): not implemented;";
};

/**
 * Send message error event
 * @public
 * @param {WebRTCommMessage} message object
 * @param {String} error code
 */
WebRTCommMessageEventListenerInterface.prototype.onWebRTCommMessageSendErrorEvent = function(message, error) {
    throw "WebRTCommCallEventListenerInterface:onWebRTCommMessageSendErrorEvent(): not implemented;";
};
