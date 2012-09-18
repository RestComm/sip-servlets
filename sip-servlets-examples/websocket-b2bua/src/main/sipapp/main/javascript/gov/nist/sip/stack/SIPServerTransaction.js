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
 *  Implementation of the JAIN-SIP SIPServerTransaction .
 *  @see  gov/nist/javax/sip/stack/SIPServerTransaction.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *  
 */
var sipservertransaction=this;
function SIPServerTransaction(sipStack,newChannelToUse) {
    if(logger!=undefined) logger.debug("SIPServerTransaction:SIPServerTransaction()");
    this.classname="SIPServerTransaction"; 
    this.auditTag = 0;
    this.sipStack=sipStack;
    this.originalRequest=null;
    this.encapsulatedChannel=newChannelToUse;
    this.wsurl=this.encapsulatedChannel.wsurl;
    this.disableTimeoutTimer();
    this.eventListeners = new Array();
    this.addEventListener(sipStack);
    this.timert=null;
    this.timer=null;
    
    if (sipStack.maxListenerResponseTime != -1) {
        this.timer=sipStack.getTimer();
        this.timer=setTimeout(this.listenerExecutionMaxTimer(), sipStack.maxListenerResponseTime * 1000);
    }
    
    this.rseqNumber =  (Math.random() * 1000);
    this.requestOf=null;
    this.dialog=null;
    this.pendingReliableResponse=null;
    this.provisionalResponseTask=null;
    this.isAckSeen=null;
    this.pendingSubscribeTransaction=null;
    this.inviteTransaction=null;
    sipservertransaction=this;
}

SIPServerTransaction.prototype = new SIPTransaction();
SIPServerTransaction.prototype.constructor=SIPServerTransaction;
SIPServerTransaction.prototype.TERMINATED_STATE=2;
SIPServerTransaction.prototype.TIMEOUT_ERROR=1;
SIPServerTransaction.prototype.RECEIVED="received";
SIPServerTransaction.prototype.INVITE="INVITE";
SIPServerTransaction.prototype.CANCEL="CANCEL";
SIPServerTransaction.prototype.BRANCH="branch";
SIPServerTransaction.prototype.BRANCH_MAGIC_COOKIE_LOWER_CASE="z9hg4bk";
SIPServerTransaction.prototype.TIMER_H=64;
SIPServerTransaction.prototype.TIMER_J=64;
SIPServerTransaction.prototype.TIMER_I=SIPTransaction.prototype.TIMER_I;
SIPServerTransaction.prototype.CONNECTION_LINGER_TIME=8;
SIPServerTransaction.prototype.BASE_TIMER_INTERVAL=500;
SIPServerTransaction.prototype.ExpiresHeader="Expires";
SIPServerTransaction.prototype.ContactHeader="Contact";

function listenerExecutionMaxTimer(){
    if(logger!=undefined) logger.debug("ListenerExecutionMaxTimer()");
    var serverTransaction = sipservertransaction;
    if (serverTransaction.getState() == null) {
        serverTransaction.terminate();
        var sipStack = serverTransaction.getSIPStack();
        sipStack.removePendingTransaction(serverTransaction);
        sipStack.removeTransaction(serverTransaction);
    }
}

SIPServerTransaction.prototype.setRequestInterface =function(newRequestOf){
    if(logger!=undefined) logger.debug("SIPServerTransaction:setRequestInterface():newRequestOf="+newRequestOf);
    this.requestOf = newRequestOf;
}

SIPServerTransaction.prototype.getResponseChannel =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:getResponseChannel()");
    return this;
}

SIPServerTransaction.prototype.isMessagePartOfTransaction =function(messageToTest){
    if(logger!=undefined) logger.debug("SIPServerTransaction:isMessagePartOfTransaction():messageToTest="+messageToTest);
    var transactionMatches = false;
    var method = messageToTest.getCSeq().getMethod();
    if (method==this.INVITE || !this.isTerminated()) {
        var viaHeaders = messageToTest.getViaHeaders();
        if (viaHeaders != null) {
            var topViaHeader = viaHeaders.getFirst();
            var messageBranch = topViaHeader.getBranch();
            if (messageBranch != null) {
                if (messageBranch.toLowerCase().substring(0,7)!=this.BRANCH_MAGIC_COOKIE_LOWER_CASE) {
                    messageBranch = null;
                }
            }
            if (messageBranch != null && this.getBranch() != null) {
                if (method==this.CANCEL) {
                    if(this.getMethod()==this.CANCEL
                        && this.getBranch().toLowerCase()==messageBranch.toLowerCase()
                        && topViaHeader.getSentBy()==
                        this.getOriginalRequest().getViaHeaders().getFirst().getSentBy())
                        {
                        transactionMatches=true;
                    }
                    else
                    {
                        transactionMatches=false;
                    }
                } 
                else {
                    if(this.getBranch().toLowerCase()==messageBranch.toLowerCase()
                        && topViaHeader.getSentBy()==
                        this.getOriginalRequest().getViaHeaders().getFirst().getSentBy())
                        {
                        transactionMatches=true;
                    }
                    else
                    {
                        transactionMatches=false;
                    }
                }
            } 
            else {
                var originalFromTag = SIPTransaction.prototype.fromTag;
                var thisFromTag = messageToTest.getFrom().getTag();
                if(originalFromTag == null || thisFromTag == null)
                {
                    var skipFrom=true;
                }
                else
                {
                    skipFrom=false;
                }
                var originalToTag = SIPTransaction.prototype.toTag;
                var thisToTag = messageToTest.getTo().getTag();
                if(originalToTag == null || thisToTag == null)
                {
                    var skipTo=true;
                }
                else
                {
                    skipTo=false;
                }
                if(messageToTest instanceof SIPResponse)
                {
                    var isResponse=true;
                }
                else
                {
                    isResponse=false;
                }
                if (messageToTest.getCSeq().getMethod().toLowerCase()==this.CANCEL.toLowerCase()
                    && getOriginalRequest().getCSeq().getMethod().toLowerCase()!=this.CANCEL.toLowerCase()) {
                    transactionMatches = false;
                } 
                else if ((isResponse || this.getOriginalRequest().getRequestURI()==
                    messageToTest.getRequestURI())
                && (skipFrom || originalFromTag != null 
                    && originalFromTag.toLowerCase()==thisFromTag.toLowerCase())
                && (skipTo || originalToTag != null 
                    && originalToTag.toLowerCase()==thisToTag.toLowerCase())
                && this.getOriginalRequest().getCallId().getCallId().toLowerCase()==
                    messageToTest.getCallId().getCallId().toLowerCase()
                    && this.getOriginalRequest().getCSeq().getSeqNumber() == messageToTest
                    .getCSeq().getSeqNumber()
                    && ((messageToTest.getCSeq().getMethod()!=(this.CANCEL)) || this.getOriginalRequest()
                        .getMethod()==messageToTest.getCSeq().getMethod())
                    && topViaHeader==getOriginalRequest().getViaHeaders().getFirst()) {
                    transactionMatches = true;
                }
            }
        }
    }
    return transactionMatches;
}

SIPServerTransaction.prototype.isTransactionMapped =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:isTransactionMapped()");
    return this.isMapped;
}

SIPServerTransaction.prototype.processRequest =function(transactionRequest,sourceChannel){
    if(logger!=undefined) logger.debug("SIPServerTransaction:processRequest():transactionRequest="+transactionRequest+", sourceChannel="+sourceChannel);
    var toTu = false;
    if (this.getRealState() == null) {
        this.setOriginalRequest(transactionRequest);
        this.setState("TRYING");
        toTu = true;
        this.setPassToListener();
    }
    else if (this.isInviteTransaction() && "COMPLETED" == this.getRealState()
        && transactionRequest.getMethod()=="ACK") {
        this.setState("CONFIRMED");
        if (!this.isReliable()) {
            this.enableTimeoutTimer(this.TIMER_I);
        } 
        else {
            this.setState("TERMINATED");
        }
        if (this.sipStack.isNon2XXAckPassedToListener()) {
            this.requestOf.processRequest(transactionRequest, this);
        } 
        return;
    }
    else if (transactionRequest.getMethod()==this.getOriginalRequest().getMethod()) {
        if ("PROCEEDING" == this.getRealState()|| "COMPLETED" == this.getRealState()) {
            if (this.lastResponse != null) {
                SIPTransaction.prototype.sendMessage.call(this,this.lastResponse);
            }
        } else if (transactionRequest.getMethod()=="ACK") {
            if (this.requestOf != null)
            {
                this.requestOf.processRequest(transactionRequest, this);
            }
        }
        return;
    }
    if ("COMPLETED" != this.getRealState()
        && "TERMINATED" != this.getRealState() && this.requestOf != null) {
        if (this.getOriginalRequest().getMethod()==transactionRequest.getMethod()) {
            if (toTu) {
                this.requestOf.processRequest(transactionRequest, this);
            } 
        } 
        else {
            if (this.requestOf != null) {
                this.requestOf.processRequest(transactionRequest, this);
            } 
        }
    }
    else {
        if (this.getSIPStack().isDialogCreated(this.getOriginalRequest().getMethod())
            && this.getRealState() == "TERMINATED"
            && transactionRequest.getMethod()=="ACK"
            && this.requestOf != null) {
            var thisDialog = this.dialog;
            if (thisDialog == null || !thisDialog.ackProcessed) {
                if (thisDialog != null) {
                    thisDialog.ackReceived(transactionRequest);
                    thisDialog.ackProcessed = true;
                }
                this.requestOf.processRequest(transactionRequest, this);
            } 
        } 
        else if (transactionRequest.getMethod()=="CANCEL") {
            this.sendMessage(transactionRequest.createResponse("OK"));
        }
    }
}

SIPServerTransaction.prototype.sendMessage =function(messageToSend){
    if(logger!=undefined) logger.debug("SIPServerTransaction:sendMessage():messageToSend="+messageToSend);
    var transactionResponse = messageToSend;
    var statusCode = transactionResponse.getStatusCode();
    if (this.getOriginalRequest().getTopmostVia().getBranch() != null) {
        transactionResponse.getTopmostVia().setBranch(this.getBranch());
    } 
    else {
        transactionResponse.getTopmostVia().removeParameter(ParameterNames.BRANCH);
    }
    if (!this.getOriginalRequest().getTopmostVia().hasPort()) {
        transactionResponse.getTopmostVia().removePort();
    }
    if (!transactionResponse.getCSeq().getMethod()==this.getOriginalRequest().getMethod()) {
        this.sendResponseSRT(transactionResponse);
        return;
    }
    if (this.getRealState() == "TRYING") {
        if (statusCode <=199 && statusCode>=100) {
            this.setState("PROCEEDING");
        } 
        else if (200 <= statusCode && statusCode <= 699) {
            if (!this.isInviteTransaction()) {
                if (!this.isReliable()) {
                    this.setState("COMPLETED");
                    this.enableTimeoutTimer(this.TIMER_J);
                } 
                else {
                    this.setState("TERMINATED");
                }
            } 
            else {
                if (statusCode <=299 && statusCode>=200) {
                    this.disableTimeoutTimer();
                    this.collectionTime = this.TIMER_J;
                    this.setState("TERMINATED");
                } 
                else {
                    this.setState("COMPLETED");
                    this.enableTimeoutTimer(this.TIMER_H);
                }
            }
        }
    }
    else if (this.getRealState() == "PROCEEDING") {
        if (this.isInviteTransaction()) {
            if (statusCode <=299 && statusCode>=100) {
                this.disableTimeoutTimer();
                this.collectionTime = this.TIMER_J;
                this.setState("TERMINATED");
            } 
            else if (300 <= statusCode && statusCode <= 699) {
                this.setState("COMPLETED");
                this.enableTimeoutTimer(this.TIMER_H);
            }
        }
        else if (200 <= statusCode && statusCode <= 699) {
            this.setState("COMPLETED");
            if (!this.isReliable()) {
                this.enableTimeoutTimer(this.TIMER_J);
            } 
            else {
                this.setState("TERMINATED");
            }
        }
    }
    else if ("COMPLETED" == this.getRealState()) {
        return;
    }
    try {
        this.lastResponse = transactionResponse;
        this.sendResponseSRT(transactionResponse);
    } catch (ex) {
        this.setState("TERMINATED");
        this.collectionTime = 0;
        console.error("SIPServerTransaction:sendMessage(): catched exception:"+ex);
    }
}

SIPServerTransaction.prototype.getViaHost =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:getViaHost()");
    return this.getMessageChannel().getViaHost();
}

SIPServerTransaction.prototype.getViaPort =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:getViaPort()");
    return this.getMessageChannel().getViaPort();
}

SIPServerTransaction.prototype.fireTimeoutTimer =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:fireTimeoutTimer()");
    clearTimeout(this.timer);
    if (this.getMethod()=="INVITE" && this.sipStack.removeTransactionPendingAck(this)) {
        return;
    }
    var dialog = this.dialog;
    if (this.getSIPStack().isDialogCreated(this.getOriginalRequest().getMethod())
        && ("CALLING" == this.getRealState() || "TRYING" == this.getRealState())) {
        dialog.setState(this.TERMINATED_STATE);
    } 
    else if (this.getOriginalRequest().getMethod()=="BYE") {
        if (dialog != null && dialog.isTerminatedOnBye()) {
            dialog.setState(this.TERMINATED_STATE);
        }
    }
    if ("COMPLETED" == this.getRealState() && this.isInviteTransaction()) {
        this.raiseErrorEvent(this.TIMEOUT_ERROR);
        this.setState("TERMINATED");
        this.sipStack.removeTransaction(this);
    } 
    else if ("COMPLETED" == this.getRealState() && !this.isInviteTransaction()) {
        this.setState("TERMINATED");
        this.sipStack.removeTransaction(this);
    } 
    else if ("CONFIRMED" == this.getRealState() && this.isInviteTransaction()) {
        this.setState("TERMINATED");
        this.sipStack.removeTransaction(this);
    } 
    else if (!isInviteTransaction()
        && ("COMPLETED" == this.getRealState() || "CONFIRMED" == this.getRealState())) {
        this.setState("TERMINATED");
    } 
    else if (isInviteTransaction() && "TERMINATED" == this.getRealState()) {
        this.raiseErrorEvent(this.TIMEOUT_ERROR);
        if (dialog != null) {
            dialog.setState(this.TERMINATED_STATE);
        }
    }
}

SIPServerTransaction.prototype.getLastResponse =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:getLastResponse()");
    return this.lastResponse;
}

SIPServerTransaction.prototype.sendResponseSRT =function(transactionResponse){
    if(logger!=undefined) logger.debug("SIPServerTransaction:sendResponseSRT():transactionResponse:"+transactionResponse);
    this.getMessageChannel().sendMessage(transactionResponse);
    this.startTransactionTimer();
}

SIPServerTransaction.prototype.sendResponse =function(response){
    if(logger!=undefined) logger.debug("SIPServerTransaction:sendResponse():response:"+response);
    var sipResponse = response;
    var dialog = this.dialog;
    if (response == null) {
        console.error("SIPServerTransaction:sendResponse(): null response argument");
        throw "SIPServerTransaction:sendResponse(): null response argument";
    }
    try {
        sipResponse.checkHeaders();
    } catch (ex) {
        console.error("SIPServerTransaction:sendMessage(): catched exception:"+ex);
        throw "SIPServerTransaction:sendResponse(): catched exception:"+ex;
    }
    
   
    if (sipResponse.getCSeq().getMethod()!=this.getMethod()) {
        console.error("SIPServerTransaction:sendResponse(): CSeq method does not match Request method of request that created the tx.");
        throw "SIPServerTransaction:sendResponse(): CSeq method does not match Request method of request that created the tx.";
    }
    
    if (this.getMethod()==("SUBSCRIBE") && response.getStatusCode() / 100 == 2) {
        if (response.getHeader(this.ExpiresHeader) == null) {
            console.error("SIPServerTransaction:sendResponse(): Expires header is mandatory in 2xx response of SUBSCRIBE");
            throw "SIPServerTransaction:sendResponse(): Expires header is mandatory in 2xx response of SUBSCRIBE";
        } else {
            var requestExpires = this.getOriginalRequest().getExpires();
            var responseExpires = response.getExpires();
            if (requestExpires != null
                && responseExpires.getExpires() > requestExpires.getExpires()) {
                console.error("SIPServerTransaction:sendResponse(): response Expires time exceeds request Expires time : See RFC 3265 3.1.1");
                throw "SIPServerTransaction:sendResponse():Response Expires time exceeds request Expires time : See RFC 3265 3.1.1";
            }
        }
    }
    
    if (sipResponse.getStatusCode() == 200
        && sipResponse.getCSeq().getMethod()=="INVITE"
        && sipResponse.getHeader(this.ContactHeader) == null) {
        console.error("SIPServerTransaction:sendResponse(): Contact Header is mandatory for the OK to the INVITE");
        throw "SIPServerTransaction:sendResponse(): Contact Header is mandatory for the OK to the INVITE";
    }
    
    if (!this.isMessagePartOfTransaction(response)) {
        console.error("SIPServerTransaction:sendResponse(): response does not belong to this transaction.");
        throw "SIPServerTransaction:sendResponse(): response does not belong to this transaction.";
    }
    
    try {
        if (dialog != null) {
            if (sipResponse.getStatusCode() / 100 == 2
                && this.sipStack.isDialogCreated(sipResponse.getCSeq().getMethod())) {
                if (dialog.getLocalTag() == null && sipResponse.getTo().getTag() == null) {
                    var utils=new Utils();
                    sipResponse.getTo().setTag(utils.generateTag());
                } 
                else if (dialog.getLocalTag() != null && sipResponse.getToTag() == null) {
                    sipResponse.setToTag(dialog.getLocalTag());
                } 
                else if (dialog.getLocalTag() != null && sipResponse.getToTag() != null
                    && dialog.getLocalTag()!=sipResponse.getToTag()) {
                    console.error("SIPServerTransaction:sendResponse(): tag mismatch dialogTag is "   + dialog.getLocalTag() + " responseTag is "+ sipResponse.getToTag());
                    throw "SIPServerTransaction:sendResponse(): tag mismatch dialogTag is "   + dialog.getLocalTag() + " responseTag is "+ sipResponse.getToTag();
                }
            }
            if (sipResponse.getCallId().getCallId()!=dialog.getCallId().getCallId()) {
                console.error("SIPServerTransaction:sendResponse(): dialog mismatch!");
                throw "SIPServerTransaction:sendResponse(): dialog mismatch!";
            }
        }
        
        var fromTag = this.getRequest().getFrom().getTag();
        if (fromTag != null && sipResponse.getFromTag() != null
            && sipResponse.getFromTag()!=fromTag) {
            console.error("SIPServerTransaction:sendResponse(): from tag of request does not match response from tag");
            throw "SIPServerTransaction:sendResponse(): from tag of request does not match response from tag";
        } 
        else if (fromTag != null) {
            sipResponse.getFrom().setTag(fromTag);
        } 
        if (dialog != null && response.getStatusCode() != 100) {
            dialog.setResponseTags(sipResponse);
            var oldState = dialog.getState();
            dialog.setLastResponse(this, response);
            if (oldState == null && dialog.getState() == "TERMINATED") {
                var event = new DialogTerminatedEvent(dialog.getSipProvider(), dialog);
                dialog.getSipProvider().handleEvent(event, this);
            }
        }
        this.sendMessage(response);
    } catch (ex) {
        this.setState("TERMINATED");
        this.raiseErrorEvent(this.TRANSPORT_ERROR);
        console.error("SIPServerTransaction:sendMessage(): catched exception:"+ex);
    }
}

SIPServerTransaction.prototype.getRealState =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:getRealState()");
    return SIPTransaction.prototype.getState.call(this);
}

SIPServerTransaction.prototype.getState =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:getState()");
    if (this.isInviteTransaction() && "TRYING" == SIPTransaction.prototype.getState.call(this)) {
        return "PROCEEDING";
    } 
    else {
        return SIPTransaction.prototype.getState.call(this);
    }
}

SIPServerTransaction.prototype.setState =function(newState){
    if(logger!=undefined) logger.debug("SIPServerTransaction:setState():newState:"+newState);
    if (newState == "TERMINATED" && this.isReliable()) {
        this.collectionTime = this.TIMER_J;
    }
    SIPTransaction.prototype.setState.call(this,newState);
}

SIPServerTransaction.prototype.startTransactionTimer =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:startTransactionTimer()");
    if(this.transactionTimerStarted==false)
    {
        this.transactionTimerStarted=true;
    }
    if (this.transactionTimerStarted) {
        if (this.sipStack.getTimer() != null) {
            this.timer = this.sipStack.getTimer();
            var transaction=this;
            this.timer=setInterval(function(){
                if(!transaction.isTerminated()){
                    transaction.fireTimer();
                }
            }, this.BASE_TIMER_INTERVAL);
        }
    }
}

SIPServerTransaction.prototype.getDialog =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:getDialog()");
    return this.dialog;
}

SIPServerTransaction.prototype.setDialog =function(sipDialog,dialogId){
    if(logger!=undefined) logger.debug("SIPServerTransaction:setDialog():sipDialog,dialogId:"+sipDialog+","+dialogId);
    this.dialog = sipDialog;
    if (dialogId != null) {
        this.dialog.setAssigned();
    }
}

SIPServerTransaction.prototype.terminate =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:terminate()");
    this.setState("TERMINATED");
}

SIPServerTransaction.prototype.setAckSeen =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:setAckSeen()");
    this.isAckSeen = true;
}

SIPServerTransaction.prototype.ackSeen =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:ackSeen()");
    return this.isAckSeen;
}

SIPServerTransaction.prototype.setMapped =function(b){
    if(logger!=undefined) logger.debug("SIPServerTransaction:setMapped():b="+b);
    this.isMapped = true;
}

SIPServerTransaction.prototype.setPendingSubscribe =function(pendingSubscribeClientTx){
    if(logger!=undefined) logger.debug("SIPServerTransaction:setPendingSubscribe():pendingSubscribeClientTx="+pendingSubscribeClientTx);
    this.pendingSubscribeTransaction = pendingSubscribeClientTx;
}

SIPServerTransaction.prototype.setInviteTransaction =function(st){
    if(logger!=undefined) logger.debug("SIPServerTransaction:setInviteTransaction():st:"+st);
    this.inviteTransaction = st;
}

SIPServerTransaction.prototype.getCanceledInviteTransaction =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:getCanceledInviteTransaction()");
    return this.inviteTransaction;
}

SIPServerTransaction.prototype.scheduleAckRemoval =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:scheduleAckRemoval()");
    if (this.getMethod() == null || this.getMethod()!="ACK") {
        console.error("SIPServerTransaction:scheduleAckRemoval():  method is null[" + (this.getMethod() == null)+ "] or method is not ACK[" + this.getMethod() + "]");
        throw "SIPServerTransaction:scheduleAckRemoval():  method is null[" + (this.getMethod() == null)+ "] or method is not ACK[" + this.getMethod() + "]";
    }
    this.startTransactionTimer();
}

SIPServerTransaction.prototype.map =function(){
    if(logger!=undefined) logger.debug("SIPServerTransaction:map()");
    var realState = this.getRealState();
    if (realState == null || realState == "TRYING") {
        this.isMapped = true;
    }
    this.sipStack.removePendingTransaction(this);
}