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

/*
 *  Implementation of the JAIN-SIP SIPClientTransaction .
 *  @see  gov/nist/javax/sip/stack/SIPClientTransaction.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function SIPClientTransaction(newSIPStack,newChannelToUse) {
    if(logger!=undefined) logger.debug("SIPClientTransaction:SIPClientTransaction():");
    this.classname="SIPClientTransaction"; 
    this.encapsulatedChannel=newChannelToUse;
    this.wsurl=this.encapsulatedChannel.wsurl;
    /*if (this.isReliable()) {            
        this.encapsulatedChannel.useCount++;
    }*/
    this.disableTimeoutTimer();
    this.sipStack=newSIPStack;
    this.infoApp=newSIPStack.infoApp;
    this.addEventListener(newSIPStack);
    this.originalRequest=null;
    this.eventListeners = new Array();
    
    var utils=new Utils();
    this.setBranch(utils.generateBranchId());
    this.notifyOnRetransmit = false;
    this.timeoutIfStillInCallingState = false;
    this.setEncapsulatedChannel(newChannelToUse);
    this.messageProcessor = newChannelToUse.messageProcessor;
    this.sipDialogs=new Array();
    this.lastRequest=null;
    this.viaPort=null;
    this.viaHost=null;
    this.respondTo=null;
    this.defaultDialog=null;
    this.nextHop=null;
    this.callingStateTimeoutCount=null;
    this.timer=null;
    this.oldmessage=null;
}

SIPClientTransaction.prototype = new SIPTransaction();
SIPClientTransaction.prototype.constructor=SIPClientTransaction;
SIPClientTransaction.prototype.BRANCH_MAGIC_COOKIE_LOWER_CASE="z9hg4bk";
SIPClientTransaction.prototype.MAXIMUM_RETRANSMISSION_TICK_COUNT=8;
SIPClientTransaction.prototype.COMPLETED="COMPLETED";
SIPClientTransaction.prototype.PROCEEDING="PROCEEDING";
SIPClientTransaction.prototype.CALLING="CALLING";
SIPClientTransaction.prototype.TERMINATED="TERMINATED";
SIPClientTransaction.prototype.ACK="ACK";
SIPClientTransaction.prototype.INVITE="INVITE";
SIPClientTransaction.prototype.TRYING="TRYING";
SIPClientTransaction.prototype.CANCEL="CANCEL";
SIPClientTransaction.prototype.BYE="BYE";
SIPClientTransaction.prototype.SUBSCRIBE="SUBSCRIBE";
SIPClientTransaction.prototype.NOTIFY="NOTIFY";
SIPClientTransaction.prototype.TIMER_B=64;
SIPClientTransaction.prototype.TIMER_D=SIPTransaction.prototype.TIMER_D;
SIPClientTransaction.prototype.TIMER_F=64;
SIPClientTransaction.prototype.TIMER_K=SIPTransaction.prototype.T4;
SIPClientTransaction.prototype.TIMER_J=64;
SIPClientTransaction.prototype.TimeStampHeader="Timestamp";
SIPClientTransaction.prototype.RouteHeader="Route";
SIPClientTransaction.prototype.RETRANSMIT="RETRANSMIT";
SIPClientTransaction.prototype.TRANSPORT_ERROR=2;
SIPClientTransaction.prototype.TIMEOUT_ERROR=1;
SIPClientTransaction.prototype.EARLY="EARLY";
SIPClientTransaction.prototype.CONNECTION_LINGER_TIME=8;
SIPClientTransaction.prototype.BASE_TIMER_INTERVAL=500;

SIPClientTransaction.prototype.setResponseInterface =function(newRespondTo){
    if(logger!=undefined) logger.debug("SIPClientTransaction:setResponseInterface():newRespondTo="+newRespondTo);
    this.respondTo = newRespondTo;
}

SIPClientTransaction.prototype.getRequestChannel =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:getRequestChannel()");
    return this;
}

SIPClientTransaction.prototype.isMessagePartOfTransaction =function(messageToTest){
    if(logger!=undefined) logger.debug("SIPClientTransaction:isMessagePartOfTransaction():messageToTest="+messageToTest);
    var viaHeaders = messageToTest.getViaHeaders();
    var transactionMatches= false;
    var messageBranch =  viaHeaders.getFirst().getBranch();
    if(this.getBranch() != null && messageBranch != null
        && this.getBranch().toLowerCase().substring(0,7)==(this.BRANCH_MAGIC_COOKIE_LOWER_CASE)
        && messageBranch.toLowerCase().substring(0,7)==(this.BRANCH_MAGIC_COOKIE_LOWER_CASE))
        {
        var rfc3261Compliant = true;
    }
    else
    {
        rfc3261Compliant = false;
    }
    if (this.COMPLETED == this.getState()) {
        if (rfc3261Compliant) {
            if(this.getBranch()().toLowerCase()==viaHeaders.getFirst().getBranch().toLowerCase()
                && this.getMethod()==messageToTest.getCSeq().getMethod())
                {
                transactionMatches = true;
            }
            else
            {
                transactionMatches=false;
            }
        } 
        else {
            if(this.getBranch()==messageToTest.getTransactionId())
            {
                transactionMatches = true;
            }
            else
            {
                transactionMatches=false;
            }
        }
    } 
    else if (!this.isTerminated()) {
        if (rfc3261Compliant) {
            if (viaHeaders != null) {
                if (this.getBranch().toLowerCase()==viaHeaders.getFirst().getBranch().toLowerCase()) {
                    if(this.getOriginalRequest().getCSeq().getMethod()==messageToTest.getCSeq().getMethod())
                    {
                        transactionMatches = true;
                    }
                    else
                    {
                        transactionMatches=false;
                    }
                }
            }
        } 
        else {
            if (this.getBranch() != null) {
                if(this.getBranch().toLowerCase()==messageToTest.getTransactionId().toLowerCase())
                {
                    transactionMatches = true;
                }
                else
                {
                    transactionMatches=false;
                }
            } else {
                if(this.getOriginalRequest().getTransactionId().toLowerCase()==messageToTest.getTransactionId().toLowerCase())
                {
                    transactionMatches = true;
                }
                else
                {
                    transactionMatches=false;
                }
            }
        }
    }
    return transactionMatches;
}

SIPClientTransaction.prototype.sendMessage =function(messageToSend){
    if(logger!=undefined) logger.debug("SIPClientTransaction:sendMessage():messageToSend="+messageToSend);
    var transactionRequest = messageToSend;
    var topVia =  transactionRequest.getViaHeaders().getFirst();
    topVia.setBranch(this.getBranch());
    if (this.PROCEEDING == this.getState()|| this.CALLING == this.getState()) {
        if (transactionRequest.getMethod()==this.ACK) {
            if (this.isReliable()) {
                this.setState(this.TERMINATED);
            } else {
                this.setState(this.COMPLETED);
            }
            SIPTransaction.prototype.sendMessage.call(this,transactionRequest);
            return;
        }
    }
    try {
        this.lastRequest = transactionRequest;
        if (this.getState() == null) {
            this.setOriginalRequest(transactionRequest);
            if (transactionRequest.getMethod()==this.INVITE) {
                this.setState(this.CALLING);
            } 
            else if (transactionRequest.getMethod()==this.ACK) {
                this.setState(this.TERMINATED);
            } 
            else {
                this.setState(this.TRYING);
            }
            if (this.isInviteTransaction()) {
                this.enableTimeoutTimer(this.TIMER_B);
            } 
            else {
                this.enableTimeoutTimer(this.TIMER_F);
            }
        }
        SIPTransaction.prototype.sendMessage.call(this,transactionRequest);  
    } catch (ex) {
        console.error("SIPClientTransaction:sendMessage(): catched exception:"+ex);
        this.setState(this.TERMINATED);
    }
    this.isMapped = true;
    this.startTransactionTimer();
}

SIPClientTransaction.prototype.processResponse =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:processResponse()");
    if(arguments.length==2)
    {
        var sipResponse=arguments[0];
        var incomingChannel=arguments[1];
        this.processResponseargu2(sipResponse, incomingChannel);
    }
    else
    {
        var transactionResponse=arguments[0];
        var sourceChannel=arguments[1];
        var dialog=arguments[2];
        this.processResponseargu3(transactionResponse, sourceChannel, dialog);
    }
}

SIPClientTransaction.prototype.processResponseargu2 =function(sipResponse,incomingChannel){
    if(logger!=undefined) logger.debug("SIPClientTransaction:processResponseargu2(): sipResponse="+sipResponse);
    if(logger!=undefined) logger.debug("SIPClientTransaction:processResponseargu2(): incomingChannel="+incomingChannel);
    var dialog = null;
    var method = sipResponse.getCSeq().getMethod();
    var dialogId = sipResponse.getDialogId(false);
    if (method==this.CANCEL && this.lastRequest != null) {
        var ict = this.lastRequest.getInviteTransaction();
        if (ict != null) {
            dialog = ict.defaultDialog;
        }
    } else {
        dialog = this.getDialog(dialogId);
    }
    if (dialog == null) {
        var code = sipResponse.getStatusCode();
        if ((code > 100 && code < 300)
            && (sipResponse.getToTag() != null || this.sipStack.isRfc2543Supported())
            && this.sipStack.isDialogCreated(method)) {
            if (this.defaultDialog != null) {
                if (sipResponse.getFromTag() != null) {
                    var dialogResponse = this.defaultDialog.getLastResponse();
                    var defaultDialogId = this.defaultDialog.getDialogId();
                    if (dialogResponse == null|| method==this.SUBSCRIBE && defaultDialogId==dialogId
                        && dialogResponse.getCSeq().getMethod()==this.NOTIFY) {
                        this.defaultDialog.setLastResponse(this, sipResponse);
                        dialog = this.defaultDialog;
                    } else {
                        dialog = this.sipStack.getDialog(dialogId);
                        if (dialog == null) {
                            if (this.defaultDialog.isAssignedFunction()) {
                                dialog = this.sipStack.createDialog(this, sipResponse);
                            }
                        }
                    }
                    if ( dialog != null ) {
                        this.setDialog(dialog, dialog.getDialogId());
                    } 
                } else {
                    console.error("SIPClientTransaction:processResponseargu2(): response without from-tag");
                    throw "SIPClientTransaction:processResponseargu2(): response without from-tag";
                }
            } else {
                if (this.sipStack.isAutomaticDialogSupportEnabled) {
                    dialog = this.sipStack.createDialog(this, sipResponse);
                    this.setDialog(dialog, dialog.getDialogId());
                }
            }
        } else {
            dialog = this.defaultDialog;
        }
    } else {    
        dialog.setLastResponse(this, sipResponse);
    }
    this.processResponse(sipResponse, incomingChannel, dialog);
}

SIPClientTransaction.prototype.processResponseargu3 =function(transactionResponse,sourceChannel,dialog){
    if(logger!=undefined) logger.debug("SIPClientTransaction:inviteClientTransaction():transactionResponse="+transactionResponse);
    if(logger!=undefined) logger.debug("SIPClientTransaction:inviteClientTransaction():sourceChannel="+sourceChannel);
    if(logger!=undefined) logger.debug("SIPClientTransaction:inviteClientTransaction():sipDialog="+dialog);

    if (this.getState() == null)
    {
        return;
    }
    if ((this.COMPLETED == this.getState() || this.TERMINATED == this.getState())
        && transactionResponse.getStatusCode() / 100 == 1) {
        return;
    }
    this.lastResponse = transactionResponse;
    try {
        if (this.isInviteTransaction())
        {
            this.inviteClientTransaction(transactionResponse, sourceChannel, dialog);
        }
        else
        {
            this.nonInviteClientTransaction(transactionResponse, sourceChannel, dialog);
        }   
    } catch (ex) {
        console.error("SIPClientTransaction:processResponseargu3(): catched exception:"+ex);
        this.setState(this.TERMINATED);
    }
}

SIPClientTransaction.prototype.nonInviteClientTransaction =function(transactionResponse,sourceChannel,sipDialog){
    if(logger!=undefined) logger.debug("SIPClientTransaction:inviteClientTransaction():transactionResponse="+transactionResponse);
    if(logger!=undefined) logger.debug("SIPClientTransaction:inviteClientTransaction():sourceChannel="+sourceChannel);
    if(logger!=undefined) logger.debug("SIPClientTransaction:inviteClientTransaction():sipDialog="+sipDialog);

    var statusCode = transactionResponse.getStatusCode();
    if (this.TRYING == this.getState()) {
        if (100 <= statusCode && statusCode <= 199) {
            this.setState(this.PROCEEDING);
            this.enableTimeoutTimer(this.TIMER_F);
            if (this.respondTo != null) {
                this.respondTo.processResponse(transactionResponse, this, sipDialog);
            }
        } 
        else if (200 <= statusCode && statusCode <= 699) {
            if (!this.isReliable()) {
                this.setState(this.COMPLETED);
                this.enableTimeoutTimer(this.TIMER_K);
            }
            else {
                this.setState(this.TERMINATED);
                this.sipStack.removeTransaction(this);
                clearTimeout(this.timer);
            }
            if (this.respondTo != null) {
                this.respondTo.processResponse(transactionResponse, this, sipDialog);
            } 
        }
    }
    else if (this.PROCEEDING == this.getState()) {
        if (100 <= statusCode && statusCode <= 199) {
            if (this.respondTo != null) {
                this.respondTo.processResponse(transactionResponse, this, sipDialog);
            } 
        } else if (200 <= statusCode && statusCode <= 699) {
            if (this.respondTo != null) {
                this.respondTo.processResponse(transactionResponse, this, sipDialog);
            } 
            this.disableTimeoutTimer();
            if (!this.isReliable()) {
                this.setState(this.COMPLETED);
                this.enableTimeoutTimer(this.TIMER_K);
            } else {
                this.setState(this.TERMINATED);
            }
        }
    } 
}

SIPClientTransaction.prototype.inviteClientTransaction =function(transactionResponse,sourceChannel,dialog){
    if(logger!=undefined) logger.debug("SIPClientTransaction:inviteClientTransaction():transactionResponse="+transactionResponse);
    if(logger!=undefined) logger.debug("SIPClientTransaction:inviteClientTransaction():sourceChannel="+sourceChannel);
    if(logger!=undefined) logger.debug("SIPClientTransaction:inviteClientTransaction():dialog="+dialog);
    var statusCode = transactionResponse.getStatusCode();
    if (this.TERMINATED == this.getState()) {
        var ackAlreadySent = false;
        if (dialog != null && dialog.isAckSeen() && dialog.getLastAckSent() != null) {
            if (dialog.getLastAckSent().getCSeq().getSeqNumber() == transactionResponse.getCSeq().getSeqNumber()
                && transactionResponse.getFromTag()==dialog.getLastAckSent().getFromTag()) {
                ackAlreadySent = true;
            }
        }
        if (dialog!= null && !ackAlreadySent
            && transactionResponse.getCSeq().getMethod()==dialog.getMethod()) {
            dialog.resendAck();
        }
        this.sipStack.removeTransaction(this);
        clearTimeout(this.timer);
        return;
    }
    else if (this.CALLING == this.getState()) {
        if (200 <= statusCode && statusCode <= 299) {
            this.disableTimeoutTimer();
            this.setState(this.TERMINATED);
            if (this.respondTo != null)
            {
                this.respondTo.processResponse(transactionResponse, this, dialog);
            }
        } 
        else if (100 <= statusCode && statusCode <= 199) {
            this.disableTimeoutTimer();
            this.setState(this.PROCEEDING);
            if (this.respondTo != null)
            {
                this.respondTo.processResponse(transactionResponse, this, dialog);
            }
        } 
        else if (300 <= statusCode && statusCode <= 699) {
            this.sendMessage(this.createErrorAck());
            if (this.respondTo != null) 
            {    
                this.respondTo.processResponse(transactionResponse, this, dialog);
            } 
            if (!this.isReliable()) {
                this.setState(this.COMPLETED);
                this.enableTimeoutTimer(this.TIMER_D);
            } 
            else {
                this.setState(this.TERMINATED);
            }
        }
    }
    else if (this.PROCEEDING == this.getState()) {
        if (100 <= statusCode && statusCode <= 199) 
        {
            if (this.respondTo != null) {
                this.respondTo.processResponse(transactionResponse, this, dialog);
            } 
        } 
        else if (200 <= statusCode && statusCode <= 299) 
        {
            this.setState(this.TERMINATED);
            this.sipStack.removeTransaction(this);
            clearTimeout(this.timer);
            if (this.respondTo != null) {
                this.respondTo.processResponse(transactionResponse, this, dialog);
            } 
        } 
        else if (300 <= statusCode && statusCode <= 699) 
        {
            this.sendMessage(this.createErrorAck());
            if (!this.isReliable()) {
                this.setState(this.COMPLETED);
                this.enableTimeoutTimer(this.TIMER_D);
            } 
            else 
            {
                this.setState(this.TERMINATED);
                this.sipStack.removeTransaction(this);
                clearTimeout(this.timer);
            }
            if (this.respondTo != null)
            {
                this.respondTo.processResponse(transactionResponse, this, dialog);
            }
        }
    }
    else if (this.COMPLETED == this.getState()) {
        this.setState(this.TERMINATED);
        this.sipStack.removeTransaction(this);
        clearTimeout(this.timer);
        if (300 <= statusCode && statusCode <= 699) {
            this.sendMessage(this.createErrorAck());
        }
    }
}

SIPClientTransaction.prototype.sendRequest =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:sendRequest()");
    var sipRequest = this.getOriginalRequest();
    if (this.getState() != null)
    {
        console.error("SIPClientTransaction:sendRequest(): request already sent");
        throw "SIPClientTransaction:sendRequest(): request already sent";
    }
    try {
        sipRequest.checkHeaders();
    } catch (ex) {
        console.error("SIPClientTransaction:sendRequest(): "+ ex);
        throw "SIPClientTransaction:sendRequest(): "+ex;
    }
    try {
        if (this.getOriginalRequest().getMethod()==this.CANCEL
            && this.sipStack.isCancelClientTransactionChecked()) {
            var ct = this.sipStack.findCancelTransaction(this.getOriginalRequest(), false);
            if (ct == null) {
                console.error("SIPClientTransaction:sendRequest(): could not find original tx to cancel. RFC 3261 9.1");
                throw "SIPClientTransaction:sendRequest(): could not find original tx to cancel. RFC 3261 9.1";
            } 
            else if (ct.getState() == null) {
                console.error("SIPClientTransaction:sendRequest(): state is null no provisional response yet -- cannot cancel RFC 3261 9.1");
                throw "SIPClientTransaction:sendRequest(): state is null no provisional response yet -- cannot cancel RFC 3261 9.1";
            } 
            else if (ct.getMethod()!=this.INVITE) {
                console.error("SIPClientTransaction:sendRequest(): cannot cancel non-invite requests RFC 3261 9.1");
                throw "SIPClientTransaction:sendRequest(): cannot cancel non-invite requests RFC 3261 9.1";
            }
        } 
        else if (this.getOriginalRequest().getMethod()==this.BYE
            ||this.getOriginalRequest().getMethod()==this.NOTIFY) {
            var dialog = this.sipStack.getDialog(this.getOriginalRequest().getDialogId(false));
            if (this.getSipProvider().isAutomaticDialogSupportEnabled() && dialog != null) {
                console.error("SIPClientTransaction:sendRequest(): Dialog is present and AutomaticDialogSupport is enabled for the provider -- Send the Request using the Dialog.sendRequest(transaction)");
                throw "SIPClientTransaction:sendRequest(): Dialog is present and AutomaticDialogSupport is enabled for the provider -- Send the Request using the Dialog.sendRequest(transaction)";
            }
        }
        if (this.getOriginalRequest().getMethod()==this.INVITE) {
            dialog = this.getDefaultDialog();
        }
        this.isMapped = true;
        this.sendMessage(sipRequest);
    } catch (ex) {
        this.setState(this.TERMINATED);
        console.error("SIPClientTransaction:sendRequest(): catched exception:"+ ex);
        throw "SIPClientTransaction:sendRequest(): catched exception:"+ ex;
    }
}

SIPClientTransaction.prototype.fireTimeoutTimer =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:fireTimeoutTimer()");
    clearTimeout(this.timer);
    var dialog = this.getDialog();
    if (this.CALLING == this.getState()|| this.TRYING == this.getState()
        || this.PROCEEDING == this.getState()) {
        if (dialog != null&& (dialog.getState() == null || dialog.getState() == this.EARLY)) {
            if (this.getSIPStack().isDialogCreated(this.getOriginalRequest().getMethod())) {
                dialog.delet();
            }
        } 
        else if (dialog != null) {
            if (this.getOriginalRequest().getMethod().toLowerCase()==this.BYE.toLowerCase()
                && dialog.isTerminatedOnBye()) {
                dialog.delet();
            }
        }
    }
    if (this.COMPLETED != this.getState()) {
        this.raiseErrorEvent(this.TIMEOUT_ERROR);
        if (this.getOriginalRequest().getMethod().toLowerCase()==this.CANCEL.toLowerCase()) {
            var inviteTx = this.getOriginalRequest().getInviteTransaction();
            if (inviteTx != null&& inviteTx.getDialog() != null
                && (inviteTx.getState() == this.CALLING || inviteTx.getState() == this.PROCEEDING)) 
                {
                inviteTx.setState(this.TERMINATED);
            }
        }
    } 
    else {
        this.setState(this.TERMINATED);
    }
}

SIPClientTransaction.prototype.createCancel =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:createCancel()");
    var originalRequest = this.getOriginalRequest();
    if (originalRequest == null)
    {
        console.error("SIPClientTransaction:createCancel(): bad state " + this.getState());
        throw "SIPClientTransaction:createCancel(): bad state " + this.getState();
    }
    if (originalRequest.getMethod()!=this.INVITE)
    {
        console.error("SIPClientTransaction:createCancel(): only INIVTE may be cancelled");
        throw "SIPClientTransaction:createCancel(): only INIVTE may be cancelled";
    }
    if (originalRequest.getMethod().toLowerCase()==this.ACK.toLowerCase())
    {
        console.error("SIPClientTransaction:createCancel(): cannot Cancel ACK!");
        throw "SIPClientTransaction:createCancel(): cannot Cancel ACK!";
    }
    else {
        var cancelRequest = originalRequest.createCancelRequest();
        cancelRequest.setInviteTransaction(this);
        return cancelRequest;
    }
}

SIPClientTransaction.prototype.createAck =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:createAck()");
    var originalRequest = this.getOriginalRequest();
    if (originalRequest == null)
    {
        console.error("SIPClientTransaction:createAck(): bad state " + getState());
        throw "SIPClientTransaction:createAck(): bad state " + getState();
    }
    
    if (this.getMethod().toLowerCase()==this.ACK.toLowerCase()) {
        console.error("SIPClientTransaction:createAck(): cannot ACK an ACK!");
        throw "SIPClientTransaction:createAck(): cannot ACK an ACK!";
    } else if (this.lastResponse == null) {
        console.error("SIPClientTransaction:createAck(): bad Transaction state");
        throw "SIPClientTransaction:createAck(): bad Transaction state";
    } else if (this.lastResponse.getStatusCode() < 200) {
        console.error("SIPClientTransaction:createAck() : cannot ACK a provisional response!");
        throw "SIPClientTransaction:createAck(): cannot ACK a provisional response!";
    }
    var ackRequest = originalRequest.createAckRequest(this.lastResponse.getTo());
    var recordRouteList = this.lastResponse.getRecordRouteHeaders();
    if (recordRouteList == null) {
        if (this.lastResponse.getContactHeaders() != null
            && this.lastResponse.getStatusCode() / 100 != 3) {
            var contact = this.lastResponse.getContactHeaders().getFirst();
            var uri =  contact.getAddress().getURI();
            ackRequest.setRequestURI(uri);
        }
        return ackRequest;
    }
    ackRequest.removeHeader(this.RouteHeader);
    var routeList = new RouteList();
    for(var i=recordRouteList.length-1;i>=0;i--)
    {
        var rr =  recordRouteList[i];
        var route = new Route();
        route.setAddressrr.getAddress();
        route.setParameters(rr.getParameters());
        routeList.add(route);
    }
    contact = null;
    if (this.lastResponse.getContactHeaders() != null) {
        contact = this.lastResponse.getContactHeaders().getFirst();
    }
    if (!routeList.getFirst().getAddress().getURI().hasLrParam()) {
        route = null;
        if (contact != null) {
            route = new Route();
            route.setAddress(contact.getAddress());
        }
        var firstRoute = routeList.getFirst();
        routeList.removeFirst();
        uri = firstRoute.getAddress().getURI();
        ackRequest.setRequestURI(uri);
        if (route != null)
            routeList.add(route);
        ackRequest.addHeader(routeList);
    } 
    else {
        if (contact != null) {
            uri =  contact.getAddress().getURI();
            ackRequest.setRequestURI(uri);
            ackRequest.addHeader(routeList);
        }
    }
    return ackRequest;
}

SIPClientTransaction.prototype.createErrorAck =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:createErrorAck()");
    var originalRequest = this.getOriginalRequest();
    if (originalRequest == null)
    {
        console.error("SIPClientTransaction:createErrorAck(): bad state " + getState());
        throw "SIPClientTransaction:createErrorAck(): bad state " + getState();
    }
    if (this.getMethod()!=this.INVITE) 
    {
        console.error("SIPClientTransaction:createErrorAck(): can only ACK an INVITE!");
        throw "SIPClientTransaction:createErrorAck(): can only ACK an INVITE!";
    } 
    else if (this.lastResponse == null) 
    {
        console.error("SIPClientTransaction:createErrorAck(): bad Transaction state");
        throw "SIPClientTransaction:createErrorAck():  bad Transaction state";
    } 
    else if (this.lastResponse.getStatusCode() < 200) 
    {
        console.error("SIPClientTransaction:createErrorAck(): cannot ACK a provisional response!");
        throw "SIPClientTransaction:createErrorAck(): cannot ACK a provisional response!";
    }
    return originalRequest.createErrorAck(this.lastResponse.getTo());
}

SIPClientTransaction.prototype.setViaPort =function(port){
    if(logger!=undefined) logger.debug("SIPClientTransaction:setViaPort():port="+port);
    this.viaPort = port;
}

SIPClientTransaction.prototype.setViaHost =function(host){
    if(logger!=undefined) logger.debug("SIPClientTransaction:setViaHost():host="+host);
    this.viaHost = host;
}

SIPClientTransaction.prototype.getViaPort =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:getViaPort()");
    return this.viaPort;
}

SIPClientTransaction.prototype.getViaHost =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:getViaHost()");
    return this.viaHost;
}

SIPClientTransaction.prototype.getOutgoingViaHeader =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:getOutgoingViaHeader()");
    return this.getMessageProcessor().getViaHeader();
}

SIPClientTransaction.prototype.clearState =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:clearState()");
    
}

SIPClientTransaction.prototype.setState =function(newState){
    if(logger!=undefined) logger.debug("SIPClientTransaction:setState():newState="+newState);
    if (newState == this.TERMINATED && this.isReliable()) {
        this.collectionTime = this.TIMER_J;
    }
    /*if (SIPTransaction.prototype.getState.call(this) != this.COMPLETED
        && (newState == this.COMPLETED || newState == this.TERMINATED)) {
        this.sipStack.decrementActiveClientTransactionCount();
    }*/
    SIPTransaction.prototype.setState.call(this,newState);
}

SIPClientTransaction.prototype.startTransactionTimer =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:startTransactionTimer()");
    if (this.transactionTimerStarted==false) {
        this.transactionTimerStarted=true;
        if (this.sipStack.getTimer() != null ) {
            var transaction=this;
            this.timer=setInterval(function(){
                if(transaction.isTerminated())
                {
                    var sipStack=transaction.getSIPStack();
                    sipStack.removeTransaction(transaction);
                }
                else
                {
                    transaction.fireTimer();
                }
            },this.BASE_TIMER_INTERVAL);
        }
    }
}

SIPClientTransaction.prototype.terminate =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:terminate()");
    this.setState(this.TERMINATED);
}

SIPClientTransaction.prototype.checkFromTag =function(sipResponse){
    if(logger!=undefined) logger.debug("SIPClientTransaction:checkFromTag():sipResponse="+sipResponse);
    var originalFromTag = this.getRequest().getFromTag();
    if (this.defaultDialog != null) {
        if (originalFromTag == null ^ sipResponse.getFrom().getTag() == null) {
            return false;
        }
        if (originalFromTag.toLowerCase()!=sipResponse.getFrom().getTag().toLowerCase()
            && originalFromTag != null) {
            return false;
        }
    }
    return true;
}

SIPClientTransaction.prototype.getDialog =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:getDialog()");
    if(arguments.length==0)
    {
        return this.getDialogargu0();
    }
    else if(arguments.length==1)
    {
        var dialogId=arguments[0];
        return this.getDialogargu1(dialogId);
    }
}

SIPClientTransaction.prototype.getDialogargu0 =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:getDialogargu0()");
    var retval = null;
    if (this.lastResponse != null && this.lastResponse.getFromTag() != null
        && this.lastResponse.getToTag() != null
        && this.lastResponse.getStatusCode() != 100) {
        var dialogId = this.lastResponse.getDialogId(false);
        retval = this.getDialog(dialogId);
    }
    if (retval == null) {
        retval = this.defaultDialog;
    }
    return retval;
}

SIPClientTransaction.prototype.getDialogargu1 =function(dialogId){
    if(logger!=undefined) logger.debug("SIPClientTransaction:getDialogargu1():dialogId="+dialogId);
    var retval=null;
    for(var i=0;i<this.sipDialogs.length;i++)
    {
        if(this.sipDialogs[i][0]==dialogId)
        {
            retval = this.sipDialogs[i][1];
        }
    }
    return retval;
}

SIPClientTransaction.prototype.setDialog =function(sipDialog,dialogId){
    if(logger!=undefined) logger.debug("SIPClientTransaction:setDialog():sipDialog="+sipDialog);
    if(logger!=undefined) logger.debug("SIPClientTransaction:setDialog():dialogId="+dialogId);
    if (sipDialog == null) {
        console.error("SIPClientTransaction:setDialog(): bad dialog argument");
        throw "SIPClientTransaction:setDialog(): bad dialog argument";
    }
    if (this.defaultDialog == null) {
        this.defaultDialog = sipDialog;
    }
    if (dialogId != null && sipDialog.getDialogId() != null) {
        var l=null
        for(var i=0;i<this.sipDialogs.length;i++)
        {
            if(this.sipDialogs[i][0]==dialogId)
            {
                l=i;
            }
        }
        if(l!=null)
        {
            this.sipDialogs[l][1]=sipDialog;
        }
        else
        {
            var array=new Array();
            array[0]=dialogId;
            array[1]=sipDialog;
            this.sipDialogs.push(array);
        }
    }
}

SIPClientTransaction.prototype.getDefaultDialog =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:getDefaultDialog()");
    return this.defaultDialog;
}

SIPClientTransaction.prototype.setNextHop =function(hop){
    if(logger!=undefined) logger.debug("SIPClientTransaction:setNextHop():hop="+hop);
    this.nextHop = hop;
}

SIPClientTransaction.prototype.getNextHop =function(){
    if(logger!=undefined) logger.debug("SIPClientTransaction:getNextHop()");
    return this.nextHop;
}

SIPClientTransaction.prototype.alertIfStillInCallingStateBy =function(count){
    if(logger!=undefined) logger.debug("SIPClientTransaction:alertIfStillInCallingStateBy():count="+count);
    this.timeoutIfStillInCallingState = true;
    this.callingStateTimeoutCount = count;
}