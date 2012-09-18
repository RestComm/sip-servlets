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
 *  Implementation of the JAIN-SIP SIPDialog .
 *  @see  gov/nist/javax/sip/stack/SIPDialog.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function SIPDialog() {
    if(logger!=undefined) logger.debug("SIPDialog:SIPDialog()");
    this.classname="SIPDialog"; 
    
    this.serialVersionUID = "-1429794423085204069L";
    this.dialogTerminatedEventDelivered=null; 
    this.stackTrace=null;
    this.method=null;
    this.isAssigned=null;
    this.reInviteFlag=null;
    this.applicationData=null; 
    this.originalRequest=null;
    this.lastResponse=null;
    this.firstTransaction=null;
    this.lastTransaction=null;
    this.dialogId=null;
    this.earlyDialogId=null;
    this.localSequenceNumber=0;
    this.remoteSequenceNumber=-1;
    this.myTag=null;
    this.hisTag=null;
    this.routeList=new RouteList();
    this.sipStack=null;
    this.dialogState=this.NULL_STATE;
    this.ackSeen=false;
    this.lastAckSent=null;
    this.lastAckReceived=null;
    this.ackProcessed=false;
    this.timerTask=null;
    this.nextSeqno=null;
    this.retransmissionTicksLeft=null;
    this.prevRetransmissionTicks=null;
    this.originalLocalSequenceNumber=null;
    this.ackLine=null;
    this.auditTag = 0;
    this.localParty=null;
    this.remoteParty=null;
    this.callIdHeader=null;
    this.serverTransactionFlag=null;
    this.sipProvider=null;
    this.terminateOnBye=true;
    this.byeSent=null; // Flag set when BYE is sent, to disallow new
    this.remoteTarget=null;
    this.eventHeader=null; // for Subscribe notify
    this.lastInviteOkReceived=null;
    this.reInviteWaitTime = 100;
    this.dialogDeleteTask=null;
    this.timerdelete=null;
    this.dialogDeleteIfNoAckSentTask=null;
    this.isAcknowledged=null;
    this.highestSequenceNumberAcknowledged = -1;
    this.isBackToBackUserAgent=null;
    this.sequenceNumberValidation = true;
    this.eventListeners=new Array();
    this.firstTransactionSecure=null;
    this.firstTransactionSeen=null;
    this.firstTransactionMethod=null;
    this.firstTransactionId=null;
    this.firstTransactionIsServerTransaction=null;
    this.firstTransactionPort = 5060;
    this.contactHeader=null;
    this.timer=null;
    this.inviteTransaction=null;
    if(arguments[0].classname=="SipProviderImpl")
    {
        this.terminateOnBye = true;
        this.routeList = new RouteList();
        this.dialogState = this.NULL_STATE; // not yet initialized.
        this.localSequenceNumber = 0;
        this.remoteSequenceNumber = -1;
        this.sipProvider = arguments[0];  
        if(arguments.length==2)
        {
            sipResponse=arguments[1];
            this.sipStack = this.sipProvider.getSipStack();
            this.setLastResponse(null, sipResponse);
            this.localSequenceNumber = sipResponse.getCSeq().getSeqNumber();
            this.originalLocalSequenceNumber = this.localSequenceNumber;
            this.myTag = sipResponse.getFrom().getTag();
            this.hisTag = sipResponse.getTo().getTag();
            this.localParty = sipResponse.getFrom().getAddress();
            this.remoteParty = sipResponse.getTo().getAddress();
            this.method = sipResponse.getCSeq().getMethod();
            this.callIdHeader = sipResponse.getCallId();
            this.serverTransactionFlag = false;
            this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;
            this.addEventListener(this.sipStack); 
        }
    }
    else if(arguments[0].classname!="SipProviderImpl")
    {
        var transaction=arguments[0];
        this.sipProvider = transaction.getSipProvider();
        this.firstTransactionSeen = false;
        this.terminateOnBye = true;
        this.routeList = new RouteList();
        this.dialogState = this.NULL_STATE; // not yet initialized.
        this.localSequenceNumber = 0;
        this.remoteSequenceNumber = -1;
        var sipRequest = transaction.getRequest();
        this.callIdHeader = sipRequest.getCallId();
        this.earlyDialogId = sipRequest.getDialogId(false);
        if (transaction == null) {
            console.error("SIPDialog:SIPDialog(): null transcation argument");
            throw "SIPDialog:SIPDialog(): null transcation argument";
        }
        this.sipStack = transaction.sipStack;
        if (this.sipProvider == null) {
            console.error("SIPDialog:SIPDialog(): null sip provider argument");
            throw "SIPDialog:SIPDialog(): null sip provider argument";
        }
        this.addTransaction(transaction);
        if(arguments.length==2)
        {
            var sipResponse=arguments[1];
            if (sipResponse == null) {
                console.error("SIPDialog:SIPDialog(): null sip response argument");
                throw "SIPDialog:SIPDialog(): null sip response argument";
            }
            this.setLastResponse(transaction, sipResponse);
            this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;      
        }
        this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;
        this.addEventListener(this.sipStack);
    }
}

SIPDialog.prototype.NULL_STATE=-1;
SIPDialog.prototype.EARLY_STATE=0;
SIPDialog.prototype.CONFIRMED_STATE=1;
SIPDialog.prototype.TERMINATED_STATE=2;
SIPDialog.prototype.DIALOG_LINGER_TIME=8;
SIPDialog.prototype.TimeStampHeader="Timestamp";
SIPDialog.prototype.TIMER_H=64;
SIPDialog.prototype.TIMER_J=64;
SIPDialog.prototype.BASE_TIMER_INTERVAL=500;

var timer=this.timer;
var sipdialog=null;
var variabletransaction=null;
var variabledialog=null;
var variablereinvite=null;
function lingerTimerDialog(){
    if(logger!=undefined) logger.debug("lingerTimerDialog()");
    var dialog = sipdialog;
    if (this.eventListeners != null) {
        this.eventListeners.clear();
    }
    this.timerTaskLock = null;
    dialog.sipStack.removeDialog(dialog);
}
function DialogDeleteTask(){
    if(logger!=undefined) logger.debug("DialogDeleteTask()");
    sipdialog.dialogDeleteTask=null;
}
function DialogTimerTask(){
    if(logger!=undefined) logger.debug("DialogTimerTask()");
    this.transaction=null;
    if(variabletransaction!=null)
    {
        this.transaction = variabletransaction;
    }
    var dialog = sipdialog;
    var transaction = sipdialog.transaction;
    if ((!dialog.ackSeen) && (transaction != null)) {
        var response = transaction.getLastResponse();
        if (response.getStatusCode() == 200) {
            transaction.fireTimer();
        }
    }
    if (dialog.isAckSeen() || dialog.dialogState == 2) {
        sipdialog.transaction = null;
        clearTimeout(sipdialog.timer);
    }
}

function DialogDeleteIfNoAckSentTask(seqno){
    if(logger!=undefined) logger.debug("DialogDeleteIfNoAckSentTask():seqno="+seqno);
    this.seqno = seqno;
    var dialog = sipdialog;
    if (dialog.highestSequenceNumberAcknowledged < seqno) {
        dialog.dialogDeleteIfNoAckSentTask = null;
        if (!dialog.isBackToBackUserAgent) {
            if (dialog.sipProvider.getSipListener() instanceof SipListener) {
                this.raiseErrorEvent(SIPDialogErrorEvent.DIALOG_ACK_NOT_SENT_TIMEOUT);
            } else {
                this.delet();
            }
        } else {
            if (dialog.sipProvider.getSipListener() instanceof SipListener) {
                this.raiseErrorEvent(SIPDialogErrorEvent.DIALOG_ACK_NOT_SENT_TIMEOUT);
            } 
            else {
                try {
                    var byeRequest = dialog.createRequest("BYE");
                    var mfi=new MessageFactoryImpl();
                    if (mfi.getDefaultUserAgentHeader() != null) {
                        byeRequest.addHeader(mfi.getDefaultUserAgentHeader());
                    }
                    var reasonHeader = new Reason();
                    reasonHeader.setProtocol("SIP");
                    reasonHeader.setCause(1025);
                    reasonHeader.setText("Timed out waiting to send ACK");
                    byeRequest.addHeader(reasonHeader);
                    var byeCtx = dialog.getSipProvider().getNewClientTransaction(byeRequest);
                    dialog.sendRequest(byeCtx);
                    return;
                } catch (ex) {
                    console.error("SIPDialog:DialogDeleteIfNoAckSentTask(): catched exception:"+ex);
                    dialog.delet();
                }
            }
        }
    }
}

SIPDialog.prototype.ackReceived =function(sipRequest){
    if(logger!=undefined) logger.debug("SIPDialog:ackReceived():sipRequest="+sipRequest);
    if (this.ackSeen) {
        return;
    }
    var tr = this.getInviteTransaction();
    if (tr != null) {
        if (tr.getCSeq() == sipRequest.getCSeq().getSeqNumber()) {
            this.ackSeen = true;
            this.setLastAckReceived(sipRequest);
            this.setRemoteTag(sipRequest.getFromTag());
            this.setLocalTag(sipRequest.getToTag());
            this.setDialogId(sipRequest.getDialogId(true));
            this.addRoute(sipRequest)
            this.setState(this.CONFIRMED_STATE);
            this.sipStack.putDialog(this);
        }
    }
}

SIPDialog.prototype.isTerminatedOnBye =function(){
    if(logger!=undefined) logger.debug("SIPDialog:isTerminatedOnBye()");
    if(this.terminateOnBye)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPDialog.prototype.setState =function(state){
    if(logger!=undefined) logger.debug("SIPDialog:setState():state="+state);
    this.dialogState = state;
    if (state == this.TERMINATED_STATE) {
        if (this.sipStack.getTimer() != null) { 
            this.timer=this.sipStack.getTimer();
            sipdialog=this;
            this.timer=setTimeout("lingerTimerDialog()", this.DIALOG_LINGER_TIME * 1000);
        }
    }
}

SIPDialog.prototype.getLocalTag =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getLocalTag()");
    return this.myTag;
}

SIPDialog.prototype.isAckSeen =function(){
    if(logger!=undefined) logger.debug("SIPDialog:isAckSeen()");
    return this.ackSeen;
}

SIPDialog.prototype.getLastAckSent =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getLastAckSent()");
    return this.lastAckSent;
}

SIPDialog.prototype.resendAck =function(){
    if(logger!=undefined) logger.debug("SIPDialog:resendAck()");
    if (this.getLastAckSent() != null) {
        if (this.getLastAckSent().getHeader(this.TimeStampHeader) != null
            && this.sipStack.generateTimeStampHeader) {
            var ts = new TimeStamp();
            var d=new Date();
            ts.setTimeStamp(d.getTime());
            this.getLastAckSent().setHeader(ts);
        }
        this.sendAck(this.getLastAckSent(), false);
    }
}

SIPDialog.prototype.getMethod =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getMethod()");
    return this.method;
}

SIPDialog.prototype.isBackToBackUserAgent =function(){
    if(logger!=undefined) logger.debug("SIPDialog:isBackToBackUserAgent()");
    return this.isBackToBackUserAgent;
}

SIPDialog.prototype.getState =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getState()");
    var x=null;
    if (this.dialogState == this.NULL_STATE) {
        x=null; // not yet initialized
    }
    if(this.dialogState==0)
    {
        x="EARLY";
    }
    else if(this.dialogState==1)
    {
        x="CONFIRMED";
    }
    else if(this.dialogState==2)
    {
        x="TERMINATED";
    }
    return x;
}

SIPDialog.prototype.delet =function(){
    if(logger!=undefined) logger.debug("SIPDialog:delet()");
    this.setState(this.TERMINATED_STATE);
}

SIPDialog.prototype.isTerminatedOnBye =function(){
    if(logger!=undefined) logger.debug("SIPDialog:isTerminatedOnBye()");
    return this.terminateOnBye;
}

SIPDialog.prototype.getLastResponse =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getLastResponse()");
    return this.lastResponse;
}

SIPDialog.prototype.setLastResponse =function(transaction,sipResponse){
    if(logger!=undefined) logger.debug("SIPDialog:setLastResponse():transaction="+transaction+", sipResponse="+sipResponse);
    this.callIdHeader = sipResponse.getCallId();
    var statusCode = sipResponse.getStatusCode();
    if (statusCode == 100) {
        return;
    }
    this.lastResponse = sipResponse;
    this.setAssigned();
    if (this.getState() == "TERMINATED") {
        if (sipResponse.getCSeq().getMethod()=="INVITE" && statusCode == 200) {
            this.lastInviteOkReceived = Math.max(sipResponse.getCSeq().getSeqNumber(),
                this.lastInviteOkReceived);
        }
        return;
    }
    var cseqMethod = sipResponse.getCSeq().getMethod();
    if (transaction == null || transaction instanceof SIPClientTransaction) {
        if (this.sipStack.isDialogCreated(cseqMethod)) {
            if (this.getState() == null && (100 <= statusCode && statusCode <= 199)) {
                this.setState(this.EARLY_STATE);
                if ((sipResponse.getToTag() != null || this.sipStack.rfc2543Supported)
                    && this.getRemoteTag() == null) {
                    this.setRemoteTag(sipResponse.getToTag());
                    this.setDialogId(sipResponse.getDialogId(false));
                    this.sipStack.putDialog(this);
                    this.addRoute(sipResponse);
                }
            } else if (this.getState() != null && this.getState()=="EARLY" && 100 <= statusCode && statusCode <= 199) {
                if (cseqMethod==this.getMethod() && transaction != null
                    && (sipResponse.getToTag() != null || this.sipStack.rfc2543Supported)) {
                    this.setRemoteTag(sipResponse.getToTag());
                    this.setDialogId(sipResponse.getDialogId(false));
                    this.sipStack.putDialog(this);
                    this.addRoute(sipResponse);
                }
            } else if (200 <= statusCode && statusCode <= 299) {
                if (cseqMethod==this.getMethod()
                    && (sipResponse.getToTag() != null || this.sipStack.rfc2543Supported)
                    && this.getState() != "CONFIRMED") {
                    this.setRemoteTag(sipResponse.getToTag());
                    this.setDialogId(sipResponse.getDialogId(false));
                    this.sipStack.putDialog(this);
                    this.addRoute(sipResponse);
                    this.setState(this.CONFIRMED_STATE);
                }
                if (cseqMethod=="INVITE") {
                    this.lastInviteOkReceived = Math.max(sipResponse.getCSeq().getSeqNumber(),
                        this.lastInviteOkReceived);
                }
            } 
            else if (statusCode >= 300&& statusCode <= 699
                && (this.getState() == null || 
                    (cseqMethod==this.getMethod() && this.getState()== "EARLY"))) {
                this.setState(this.TERMINATED_STATE);
            }
            if (this.getState() != "CONFIRMED" && this.getState() != "TERMINATED") {
                if (this.originalRequest != null) {
                    var rrList = this.originalRequest.getRecordRouteHeaders();
                    if (rrList != null) {
                        for(var i=rrList.length;i>=0;i--)
                        {
                            var rr = rrList[i];
                            var route = this.routeList.getFirst();
                            if (route != null && rr.getAddress()==route.getAddress()) {
                                this.routeList.removeFirst();
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        } else if (cseqMethod=="NOTIFY"
            && (this.getMethod()=="SUBSCRIBE" || this.getMethod()=="REFER") &&
            sipResponse.getStatusCode() / 100 == 2
            && this.getState() == null) {

            this.setDialogId(sipResponse.getDialogId(true));
            this.sipStack.putDialog(this);
            this.setState(this.CONFIRMED_STATE);
        } else if (cseqMethod=="BYE" && statusCode / 100 == 2
            && this.isTerminatedOnBye()) {
            this.setState(this.TERMINATED_STATE);
        }
    } 
    else {
        if (cseqMethod=="BYE" && statusCode / 100 == 2
            && this.isTerminatedOnBye()) {
            this.setState(this.TERMINATED_STATE);
        } else {
            var doPutDialog = false;
            if (this.getLocalTag() == null && sipResponse.getTo().getTag() != null
                && this.sipStack.isDialogCreated(cseqMethod) && cseqMethod==this.getMethod()) {
                this.setLocalTag(sipResponse.getTo().getTag());
                doPutDialog = true;
            }
            if (statusCode / 100 != 2) {
                if (statusCode / 100 == 1) {
                    if (doPutDialog) {
                        this.setState(this.EARLY_STATE);
                        this.setDialogId(sipResponse.getDialogId(true));
                        this.sipStack.putDialog(this);
                    }
                } 
                else {
                    if (!this.isReInvite() && this.getState() != this.CONFIRMED) {
                        this.setState(SIPDialog.TERMINATED_STATE);
                    }
                }
            } else {
                if (this.dialogState <= this.EARLY_STATE && 
                    (cseqMethod=="INVITE"|| cseqMethod=="SUBSCRIBE" || cseqMethod=="REFER")) {
                    this.setState(this.CONFIRMED_STATE);
                }
                if (doPutDialog) {
                    this.setDialogId(sipResponse.getDialogId(true));
                    this.sipStack.putDialog(this);
                }
            }
        }
    }
}

SIPDialog.prototype.getDialogId =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getDialogId()");
    if (this.dialogId == null && this.lastResponse != null) {
        this.dialogId = this.lastResponse.getDialogId(this.isServer());
    }
    return this.dialogId;
}

SIPDialog.prototype.isAssignedFunction =function(){
    if(logger!=undefined) logger.debug("SIPDialog:isAssigned()");
    return this.isAssigned;
}

SIPDialog.prototype.setResponseTags =function(sipResponse){
    if(logger!=undefined) logger.debug("SIPDialog:setResponseTags():sipResponse="+sipResponse);
    if (this.getLocalTag() != null || this.getRemoteTag() != null) {
        return;
    }
    var responseFromTag = sipResponse.getFromTag();
    if (responseFromTag != null) {
        if (responseFromTag==this.getLocalTag()) {
            sipResponse.setToTag(this.getRemoteTag());
        } 
        else if (responseFromTag==this.getRemoteTag()) {
            sipResponse.setToTag(this.getLocalTag());
        }
    } 
}

SIPDialog.prototype.getSipProvider =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getSipProvider()");
    return this.sipProvider;
}

SIPDialog.prototype.sendAck =function(request){
    if(logger!=undefined) logger.debug("SIPDialog:sendAck():request="+request);
    var ackRequest = request;
    if (!ackRequest.getMethod()=="ACK") {
        console.error("SIPDialog:sendAck(): bad request method -- should be ACK");
        throw "SIPDialog:sendAck(): bad request method -- should be ACK";
    }
    if (this.getState() == null || this.getState() == "EARLY") {
        console.error("SIPDialog:sendAck(): bad dialog state " + this.getState())
        throw "SIPDialog:sendAck(): bad dialog state " + this.getState();
    }
    
    if (this.getCallId().getCallId()!=request.getCallId().getCallId()) {
        console.error("SIPDialog:sendAck(): bad call ID in request");
        throw "SIPDialog:sendAck(): bad call ID in request";
    }
    try {
        if (this.getLocalTag() != null) {
            ackRequest.getFrom().setTag(this.getLocalTag());
        }
        if (this.getRemoteTag() != null) {
            ackRequest.getTo().setTag(this.getRemoteTag());
        }
    } catch (ex) {
        console.error("SIPDialog:sendAck(): catched exception:"+ex);
        throw "SIPDialog:sendAck(): catched exception:"+ex;
    }
    
    var hop = this.sipStack.getNextHop(ackRequest);
    if (hop == null) {
        console.error("SIPDialog:sendAck(): no route!");
        throw "SIPDialog:sendAck(): no route!";
    }
    var lp = this.sipProvider.getListeningPoint();
    if (lp == null) {
        console.error("SIPDialog:sendAck(): no listening point for this provider registered at " + hop);
        throw "SIPDialog:sendAck(): no listening point for this provider registered at " + hop;
    }
    var messageChannel = lp.getMessageProcessor().getIncomingwsMessageChannels();
    this.setLastAckSent(ackRequest);
    messageChannel.sendMessage(ackRequest);
    this.isAcknowledged = true;
    this.highestSequenceNumberAcknowledged = Math.max(this.highestSequenceNumberAcknowledged,
        ackRequest.getCSeq().getSeqNumber());
    if (this.dialogDeleteTask != null) {
    //this.dialogDeleteTask.cancel();
    //this.dialogDeleteTask = null;
    }
    this.ackSeen = true;  
    var encodedSipMessage = request.encode();
    console.info("SIP message sent: "+encodedSipMessage); 
}

SIPDialog.prototype.getRemoteTag =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getRemoteTag()");
    return this.hisTag;
}

SIPDialog.prototype.isServer =function(){
    if(logger!=undefined) logger.debug("SIPDialog:isServer()");
    if (this.firstTransactionSeen == false) {
        return this.serverTransactionFlag;
    } 
    else {
        return this.firstTransactionIsServerTransaction;
    }
}

SIPDialog.prototype.addTransaction =function(transaction){
    if(logger!=undefined) logger.debug("SIPDialog:addTransaction():transaction="+transaction);
    var sipRequest = transaction.getOriginalRequest();
    if (this.firstTransactionSeen && this.firstTransactionId!=(transaction.getBranchId())
        && transaction.getMethod()==this.firstTransactionMethod) {
        this.reInviteFlag = true;
    }
    
    if (this.firstTransactionSeen == false) {
        this.storeFirstTransactionInfo(this, transaction);
        if (sipRequest.getMethod()=="SUBSCRIBE") {
            this.eventHeader = sipRequest.getHeader("Event");
        }
        this.setLocalParty(sipRequest);
        this.setRemoteParty(sipRequest);
        this.setCallId(sipRequest);
        if (this.originalRequest == null) {
            this.originalRequest = sipRequest;
        }
        if (this.method == null) {
            this.method = sipRequest.getMethod();
        }
        if (transaction instanceof SIPServerTransaction) {
            this.hisTag = sipRequest.getFrom().getTag();
        } else {
            this.setLocalSequenceNumber(sipRequest.getCSeq().getSeqNumber());
            this.originalLocalSequenceNumber = this.localSequenceNumber;
            this.myTag = sipRequest.getFrom().getTag();
        }
    } 
    else if (transaction.getMethod()==this.firstTransactionMethod
        && this.firstTransactionIsServerTransaction != transaction.isServerTransaction()) {
        this.storeFirstTransactionInfo(this, transaction);
        this.setLocalParty(sipRequest);
        this.setRemoteParty(sipRequest);
        this.setCallId(sipRequest);
        this.originalRequest = sipRequest;
        this.method = sipRequest.getMethod();
    }
    if (transaction instanceof SIPServerTransaction) {
        this.setRemoteSequenceNumber(sipRequest.getCSeq().getSeqNumber());
    }
    this.lastTransaction = transaction;
}

SIPDialog.prototype.storeFirstTransactionInfo =function(dialog,transaction){
    if(logger!=undefined) logger.debug("SIPDialog:storeFirstTransactionInfo():dialog="+dialog+",transaction="+transaction);
    dialog.firstTransaction = transaction;
    dialog.firstTransactionSeen = true;
    dialog.firstTransactionIsServerTransaction = transaction.isServerTransaction();
    dialog.firstTransactionSecure = true;
    //dialog.firstTransactionPort = transaction.getPort();
    dialog.firstTransactionId = transaction.getBranchId();
    dialog.firstTransactionMethod = transaction.getMethod();
    if (dialog.isServer()) {
        var st = transaction;
        var response = st.getLastResponse();
        dialog.contactHeader = response != null ? response.getContactHeader() : null;
    } else {
        var ct = transaction;
        if (ct != null) {
            var sipRequest = ct.getOriginalRequest();
            dialog.contactHeader = sipRequest.getContactHeader();
        }
    }
}

SIPDialog.prototype.setLocalParty =function(sipMessage){
    if(logger!=undefined) logger.debug("SIPDialog:setLocalParty():sipMessage="+sipMessage);
    if (!this.isServer()) {
        this.localParty = sipMessage.getFrom().getAddress();
    } else {
        this.localParty = sipMessage.getTo().getAddress();
    }
}

SIPDialog.prototype.setRemoteParty =function(sipMessage){
    if(logger!=undefined) logger.debug("SIPDialog:setRemoteParty():sipMessage="+sipMessage);
    if (!this.isServer()) {
        this.remoteParty = sipMessage.getTo().getAddress();
    } else {
        this.remoteParty = sipMessage.getFrom().getAddress();
    }
}

SIPDialog.prototype.setCallId =function(sipRequest){
    if(logger!=undefined) logger.debug("SIPDialog:setCallId():sipRequest="+sipRequest);
    this.callIdHeader = sipRequest.getCallId();
}

SIPDialog.prototype.setLocalSequenceNumber =function(lCseq){
    if(logger!=undefined) logger.debug("SIPDialog:setLocalSequenceNumber():lCseq="+lCseq);
    this.localSequenceNumber = lCseq;
}

SIPDialog.prototype.getLocalParty =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getLocalParty()");
    return this.localParty;
}

SIPDialog.prototype.getRemoteParty =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getRemoteParty()");
    return this.remoteParty;
}

SIPDialog.prototype.addEventListener =function(newListener){
    if(logger!=undefined) logger.debug("SIPDialog:addEventListener():newListener="+newListener);
    var l=null;
    for(var i=0;i<this.eventListeners;i++)
    {
        if(this.eventListeners[i]==newListener)
        {
            l=i;
        }
    }
    if(l==null)
    {
        this.eventListeners.push(newListener);
    }
}

SIPDialog.prototype.setAssigned =function(){
    if(logger!=undefined) logger.debug("SIPDialog:setAssigned()");
    this.isAssigned = true;
}

SIPDialog.prototype.testAndSetIsDialogTerminatedEventDelivered =function(){
    if(logger!=undefined) logger.debug("SIPDialog:testAndSetIsDialogTerminatedEventDelivered()");
    var retval = this.dialogTerminatedEventDelivered;
    this.dialogTerminatedEventDelivered = true;
    return retval;
}

SIPDialog.prototype.getFirstTransaction =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getFirstTransaction()");
    return this.firstTransaction;
}

SIPDialog.prototype.isClientDialog =function(){
    if(logger!=undefined) logger.debug("SIPDialog:isClientDialog()");
    var transaction = this.getFirstTransaction();
    if(transaction instanceof SIPClientTransaction)
    {
        return true
    }
    else
    {
        return false;
    }
}

SIPDialog.prototype.addRoute =function(){
    if(logger!=undefined) logger.debug("SIPDialog:addRoute()");
    if(arguments[0] instanceof SIPResponse)
    {
        var sipResponse=arguments[0];
        this.addRouteResponse(sipResponse);
    }
    else if(arguments[0] instanceof SIPRequest)
    {
        var sipRequest=arguments[0];
        this.addRouteRequest(sipRequest);
    }
    else
    {
        var recordRouteList=arguments[0];
        this.addRouteList(recordRouteList);
    }
}

SIPDialog.prototype.addRouteResponse =function(sipResponse){
    if(logger!=undefined) logger.debug("SIPDialog:addRouteResponse():sipResponse="+sipResponse);
    if (sipResponse.getStatusCode() == 100) {
        return;
    } 
    else if (this.dialogState == 2) {
        return;
    } 
    else if (this.dialogState == 1) {
        if (200<=sipResponse.getStatusCode() && sipResponse.getStatusCode()<=299 && !this.isServer()) {
            var contactList = sipResponse.getContactHeaders();
            var request=new SIPRequest();
            if (contactList != null && request.isTargetRefresh(sipResponse.getCSeq().getMethod())) {
                this.setRemoteTarget(contactList.getFirst());
            }
        }
        return;
    }
    if (!this.isServer()) {
        if (this.getState() != "CONFIRMED"&& this.getState() != "TERMINATED") {
            var rrlist = sipResponse.getRecordRouteHeaders();
            if (rrlist != null) {
                this.addRoute(rrlist);
            } 
            else {
                this.routeList = new RouteList();
            }
        }
        //        contactList = sipResponse.getContactHeaders();
        if (contactList != null) {
            this.setRemoteTarget(contactList.getFirst());
        }
    }
}

SIPDialog.prototype.addRouteRequest =function(sipRequest){
    if(logger!=undefined) logger.debug("SIPDialog:addRouteRequest():sipRequest="+sipRequest);
    var siprequest=new SIPRequest();
    if (this.dialogState == "CONFIRMED"&& siprequest.isTargetRefresh(sipRequest.getMethod())) {
        this.doTargetRefresh(sipRequest);
    }
    if (this.dialogState == "CONFIRMED" || this.dialogState == "TERMINATED") {
        return;
    }
    if (sipRequest.getToTag() != null) {
        return;
    }
    var rrlist = sipRequest.getRecordRouteHeaders();
    if (rrlist != null) {
        this.addRoute(rrlist);
    } 
    else {
        this.routeList = new RouteList();
    }
    var contactList = sipRequest.getContactHeaders();
    if (contactList != null) {
        this.setRemoteTarget(contactList.getFirst());
    }
}

SIPDialog.prototype.addRouteList =function(recordRouteList){
    if(logger!=undefined) logger.debug("SIPDialog:addRouteList():recordRouteList="+recordRouteList);
    if (this.isClientDialog()) {
        this.routeList = new RouteList();
        for(var i=recordRouteList.getHeaderList().length-1;i>=0;i--)
        {
            var rr = recordRouteList.getHeaderList()[i];
            var route = new Route();
            var address =  rr.getAddress();

            route.setAddress(address);
            route.setParameters(rr.getParameters());
            this.routeList.add(route);
        }
    } 
    else {
        this.routeList = new RouteList();
        for(i=0;i<recordRouteList.length;i--)
        {
            rr = recordRouteList[i];
            route = new Route();
            address =  rr.getAddress();

            route.setAddress(address);
            route.setParameters(rr.getParameters());

            this.routeList.add(route);
        }
    }
}

SIPDialog.prototype.setRemoteTarget =function(contact){
    if(logger!=undefined) logger.debug("SIPDialog:setRemoteTarget():contact:"+contact);
    this.remoteTarget = contact.getAddress();
}

SIPDialog.prototype.getRouteList =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getRouteList()");
    return this.routeList;
}

SIPDialog.prototype.setRouteList =function(routeList){
    if(logger!=undefined) logger.debug("SIPDialog:setRouteList():routeList:"+routeList);
    this.routeList = routeList;
}

SIPDialog.prototype.setStack =function(sipStack){
    if(logger!=undefined) logger.debug("SIPDialog:setStack():sipStack:"+sipStack);
    this.sipStack = sipStack;
}

SIPDialog.prototype.getStack =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getStack()");
    return this.sipStack;
}

SIPDialog.prototype.removeEventListener =function(oldListener){
    if(logger!=undefined) logger.debug("SIPDialog:removeEventListener():oldListener:"+oldListener);
    var l=null;
    for(var i=0;i<this.eventListeners.length;i++)
    {
        if(this.eventListeners[i]==oldListener)
        {
            l=i;
        }
    }
    this.eventListeners.splice(l,1);
}

SIPDialog.prototype.setApplicationData =function(applicationData){
    if(logger!=undefined) logger.debug("SIPDialog:setApplicationData():applicationData:"+applicationData);
    this.applicationData = applicationData;
}

SIPDialog.prototype.getApplicationData =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getApplicationData()");
    return this.applicationData;
}

SIPDialog.prototype.requestConsumed =function(){
    if(logger!=undefined) logger.debug("SIPDialog:requestConsumed()");
    this.nextSeqno = this.getRemoteSeqNumber() + 1;
}

SIPDialog.prototype.isRequestConsumable =function(dialogRequest){
    if(logger!=undefined) logger.debug("SIPDialog:isRequestConsumable():dialogRequest:"+dialogRequest);
    if (dialogRequest.getMethod()=="ACK") {
        console.error("SIPDialog:isRequestConsumable(): Illegal method");
        throw "SIPDialog:isRequestConsumable(): Illegal method";
    }
    if (!this.isSequnceNumberValidation()) {
        return true;
    }
    if(this.remoteSequenceNumber <= dialogRequest.getCSeq().getSeqNumber())
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPDialog.prototype.doDeferredDelete =function(){
    if(logger!=undefined) logger.debug("SIPDialog:doDeferredDelete()");
    if (this.sipStack.getTimer() == null) {
        this.setState(this.TERMINATED_STATE);
    } else {
        this.timerdelete=this.sipStack.getTimer();
        sipdialog=this;
        this.timerdelete=setTimeout(function(){
            sipdialog.dialogDeleteTask = new DialogDeleteTask();
        },this.TIMER_H * this.BASE_TIMER_INTERVAL);
    }
}

SIPDialog.prototype.isAckSent =function(cseqNo){
    if(logger!=undefined) logger.debug("SIPDialog:isAckSent():cseqNo="+cseqNo);
    if (this.getLastTransaction() == null) {
        return true;
    }
    if (this.getLastTransaction() instanceof SIPClientTransaction) {
        if (this.getLastAckSent() == null) {
            return false;
        } 
        else {
            return cseqNo <= this.getLastAckSent().getCSeq().getSeqNumber();
        }
    }
    else {
        return true;
    }
}

SIPDialog.prototype.getRouteSet =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getRouteSet()");
    if (this.routeList == null) {
        this.routeList=new Array();
        return this.routeList;
    } 
    else {
        return this.getRouteList();
    }
}

SIPDialog.prototype.setDialogId =function(dialogId){
    if(logger!=undefined) logger.debug("SIPDialog:setDialogId():dialogId="+dialogId);
    this.dialogId = dialogId;
}

SIPDialog.prototype.createFromNOTIFY =function(subscribeTx,notifyST){
    if(logger!=undefined) logger.debug("SIPDialog:createFromNOTIFY(): subscribeTx="+subscribeTx);
    if(logger!=undefined) logger.debug("SIPDialog:createFromNOTIFY(): notifyST="+notifyST);
    var d = new SIPDialog(notifyST);
    d.serverTransactionFlag = false;
    d.lastTransaction = subscribeTx;
    storeFirstTransactionInfo(d, subscribeTx);
    d.terminateOnBye = false;
    d.localSequenceNumber = subscribeTx.getCSeq();
    var not = notifyST.getRequest();
    d.remoteSequenceNumber = not.getCSeq().getSeqNumber();
    d.setDialogId(not.getDialogId(true));
    d.setLocalTag(not.getToTag());
    d.setRemoteTag(not.getFromTag());
    d.setLastResponse(subscribeTx, subscribeTx.getLastResponse());
    d.localParty = not.getTo().getAddress();
    d.remoteParty = not.getFrom().getAddress();
    d.addRoute(not);
    d.setState(this.CONFIRMED_STATE); 
    return d;
}

SIPDialog.prototype.isReInvite =function(){
    if(logger!=undefined) logger.debug("SIPDialog:isReInvite()");
    return this.reInviteFlag;
}

SIPDialog.prototype.setRemoteTag =function(hisTag){
    if(logger!=undefined) logger.debug("SIPDialog:setRemoteTag():hisTag:"+hisTag);
    if (this.hisTag != null && hisTag != null && hisTag!=this.hisTag) {
        if (this.getState() != "EARLY") {
            return;
        } 
        else if (this.sipStack.isRemoteTagReassignmentAllowed()) {
            var removed = false;
            if (this.sipStack.getDialog(this.dialogId) == this) {
                this.sipStack.removeDialog(this.dialogId);
                removed = true;
            }
            this.dialogId = null;
            this.hisTag = hisTag;
            if (removed) {
                this.sipStack.putDialog(this);
            }
        }
    }
    else {
        if (hisTag != null) {
            this.hisTag = hisTag;
        } 
    }
}

SIPDialog.prototype.getLastTransaction =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getLastTransaction()");
    return this.lastTransaction;
}

SIPDialog.prototype.getInviteTransaction =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getInviteTransaction()");
    return this.inviteTransaction;
}

SIPDialog.prototype.setInviteTransaction =function(transaction){
    if(logger!=undefined) logger.debug("SIPDialog:getInviteTransaction():transaction="+transaction);
    this.inviteTransaction=transaction;
}

SIPDialog.prototype.setLocalSequenceNumber =function(lCseq){
    if(logger!=undefined) logger.debug("SIPDialog:setLocalSequenceNumber():lCseq="+lCseq);
    if (lCseq <= this.localSequenceNumber) {
        console.error("SIPDialog:setLocalSequenceNumber(): sequence number should not decrease !");
        throw "SIPDialog:setLocalSequenceNumber(): sequence number should not decrease !";
    }
    this.localSequenceNumber = lCseq;
}

SIPDialog.prototype.setRemoteSequenceNumber =function(rCseq){
    if(logger!=undefined) logger.debug("SIPDialog:setRemoteSequenceNumber():rCseq="+rCseq);
    this.remoteSequenceNumber = rCseq;
}

SIPDialog.prototype.incrementLocalSequenceNumber =function(){
    if(logger!=undefined) logger.debug("SIPDialog:incrementLocalSequenceNumber()");
    ++this.localSequenceNumber;
}
SIPDialog.prototype.getOriginalLocalSequenceNumber =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getOriginalLocalSequenceNumber()");
    return this.originalLocalSequenceNumber;
}
SIPDialog.prototype.getLocalSeqNumber =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getLocalSeqNumber()");
    return this.localSequenceNumber;
}
SIPDialog.prototype.getRemoteSeqNumber =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getRemoteSeqNumber()");
    return this.remoteSequenceNumber;
}
SIPDialog.prototype.setLocalTag =function(mytag){
    if(logger!=undefined) logger.debug("SIPDialog:setLocalTag():mytag:"+mytag);
    this.myTag = mytag;
}
SIPDialog.prototype.getCallId =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getCallId()");
    return this.callIdHeader;
}
SIPDialog.prototype.getRemoteTarget =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getRemoteTarget()");
    return this.remoteTarget;
}
SIPDialog.prototype.isSecure =function(){
    if(logger!=undefined) logger.debug("SIPDialog:isSecure()");
    return this.firstTransactionSecure;
}
SIPDialog.prototype.createRequest =function(){
    if(logger!=undefined) logger.debug("SIPDialog:createRequest()");
    if(arguments.length==1)
    {
        var method=arguments[0];
        return this.createRequestargu1(method);
    }
    else
    {
        method=arguments[0];
        var sipResponse=arguments[1];
        return this.createRequestargu2(method, sipResponse);
    }
}
SIPDialog.prototype.createRequestargu1 =function(method){
    if(logger!=undefined) logger.debug("SIPDialog:createRequestargu1():method:"+method);
    if (method=="ACK") {
        console.error("SIPDialog:createRequestargu1(): invalid method specified for createRequest:" + method);
        throw "SIPDialog:createRequestargu1(): invalid method specified for createRequest:" + method;
    }
    if (this.lastResponse != null) {
        return this.createRequest(method, this.lastResponse);
    } 
    else {
        console.error("SIPDialog:createRequestargu1(): dialog not yet established -- no response!");
        throw "SIPDialog:createRequestargu1(): dialog not yet established -- no response!";
    }
}

SIPDialog.prototype.createRequestargu2 =function(method,sipResponse){
    if(logger!=undefined) logger.debug("SIPDialog:createRequestargu2():method,sipResponse:"+method+","+sipResponse);
    if (method == null || sipResponse == null) {
        console.error("SIPDialog:createRequestargu2(): null argument");
        throw "SIPDialog:createRequestargu2(): null argument";
    }
    if (method=="CANCEL") {
        console.error("SIPDialog:createRequestargu2(): invalid request");
        throw "SIPDialog:createRequestargu2(): invalid request";
    }
    if (this.getState() == null
        || (this.getState() == "TERMINATED" && method.toUpperCase()!="BYE")
        || (this.isServer() && this.getState() == "EARLY" && method.toUpperCase()=="BYE")) {
        console.error("SIPDialog:createRequestargu2(): dialog  " + getDialogId()+" not yet established or terminated " + this.getState());
        throw "SIPDialog:createRequestargu2(): dialog  " + getDialogId()+" not yet established or terminated " + this.getState();
    }
    var sipUri = null;
    if (this.getRemoteTarget() != null) {
        sipUri = this.getRemoteTarget().getURI();
    } 
    else {
        sipUri = this.getRemoteParty().getURI();
        sipUri.clearUriParms();
    }
    if(this.isServer())
    {
        var contactHeader=this.getInviteTransaction().getOriginalRequest().getContactHeader();
        sipUri=contactHeader.getAddress().getURI();
    }
    else
    {
        sipUri=sipResponse.getContactHeader().getAddress().getURI();
    }
    var cseq = new CSeq();
    cseq.setMethod(method);
    //this.getLocalSeqNumber()+1
    cseq.setSeqNumber(this.getLocalSeqNumber()+1);
    if (method=="SUBSCRIBE") {
        if (this.eventHeader != null) {
            sipRequest.addHeader(this.eventHeader);
        }
    }
    var lp = this.sipProvider.getListeningPoint();
    if (lp == null) {
        
        console.error("SIPDialog:createRequestargu2(): cannot find listening point for transport " + sipResponse.getTopmostVia().getTransport());
        throw "SIPDialog:createRequestargu2(): cannot find listening point for transport " + sipResponse.getTopmostVia().getTransport();
    }
    var via = lp.getViaHeader();
    var from = new From();
    from.setAddress(this.localParty);
    var to = new To();
    to.setAddress(this.remoteParty);
    if (this.getLocalTag() != null) {
        from.setTag(this.getLocalTag());
    } else {
        from.removeTag();
    }
    if (this.getRemoteTag() != null) {
        to.setTag(this.getRemoteTag());
    } else {
        to.removeTag();
    }
    var sipRequest = sipResponse.createRequest(sipUri, via, cseq, from, to);
    
    var siprq=new SIPRequest();
    if (siprq.isTargetRefresh(method)) {
        var contactHeader = this.sipProvider.getListeningPoint().createContactHeader();
        contactHeader.getAddress().getURI().setSecure(this.isSecure());
        sipRequest.setHeader(contactHeader);
    }
    this.updateRequest(sipRequest);
    return sipRequest;
}

SIPDialog.prototype.sendRequest =function(clientTransactionId){
    if(logger!=undefined) logger.debug("SIPDialog:sendRequestargu2():clientTransactionId="+clientTransactionId);
    var dialogRequest =  clientTransactionId.getOriginalRequest();
    if (clientTransactionId == null) {
        console.error("SIPDialog:sendRequest(): null parameter");
        throw "SIPDialog:sendRequest(): null parameter";
    }
    
    if (dialogRequest.getMethod()=="ACK" || dialogRequest.getMethod()=="CANCEL") {
        console.error("SIPDialog:sendRequest(): bad request method. " + dialogRequest.getMethod());
       throw "SIPDialog:sendRequest(): bad request method. " + dialogRequest.getMethod();
    }
    
    if (this.byeSent && this.isTerminatedOnBye() && dialogRequest.getMethod()!="BYE") {
        console.error("SIPDialog:sendRequest(): cannot send request; BYE already sent");
        throw "SIPDialog:sendRequest(): cannot send request; BYE already sent";
    }
    if (dialogRequest.getTopmostVia() == null) {
        var via =  clientTransactionId.getOutgoingViaHeader();
        dialogRequest.addHeader(via);
    }
    
    if (this.getCallId().getCallId().toLowerCase()!=dialogRequest.getCallId().getCallId().toLowerCase()) {    
        console.error("SIPDialog:sendRequest(): bad call ID in request");
        throw "SIPDialog:sendRequest(): bad call ID in request";
    }
    clientTransactionId.setDialog(this, this.dialogId);
    this.addTransaction(clientTransactionId);
    clientTransactionId.isMapped = true;
    var from = dialogRequest.getFrom();
    var to = dialogRequest.getTo();
    if (this.getLocalTag() != null && from.getTag() != null && from.getTag()!=this.getLocalTag()) {
        console.error("SIPDialog:sendRequest(): from tag mismatch expecting  " + this.getLocalTag());
        throw "SIPDialog:sendRequest(): from tag mismatch expecting  " + this.getLocalTag();
    }
    
    if (this.getLocalTag() == null && dialogRequest.getMethod()=="NOTIFY") {
        if (this.getMethod()!="SUBSCRIBE") {
            console.error("SIPDialog:sendRequest(): trying to send NOTIFY without SUBSCRIBE Dialog!");
            throw "SIPDialog:sendRequest(): trying to send NOTIFY without SUBSCRIBE Dialog!";
        }
        this.setLocalTag(from.getTag());
    }
    if (this.getLocalTag() != null) {
        from.setTag(this.getLocalTag());
    }
    if (this.getRemoteTag() != null) {
        to.setTag(this.getRemoteTag());
    }
    var messageChannel = this.sipStack.getChannel();
    if (messageChannel == null) {
        var outboundProxy = this.sipStack.getRouter(dialogRequest).getOutboundProxy();
        if (outboundProxy == null) {   
            console.error("SIPDialog:sendRequest(): no route found!");
            throw "SIPDialog:sendRequest(): no route found!"; 
        }
        messageChannel = this.sipStack.createRawMessageChannel(this.getSipProvider().
            getListeningPoint(outboundProxy.getTransport()).getHostAddress(),this.firstTransactionPort, outboundProxy);
        if (messageChannel != null) {
            clientTransactionId.setEncapsulatedChannel(messageChannel);
        }
    } 
    else {
        clientTransactionId.setEncapsulatedChannel(messageChannel);
    }
    if (messageChannel != null) {
        messageChannel.useCount++;
    }
    this.localSequenceNumber++;
    dialogRequest.getCSeq().setSeqNumber(this.getLocalSeqNumber());
    try {
        clientTransactionId.sendMessage(dialogRequest);
        if (dialogRequest.getMethod()=="BYE") {
            this.byeSent = true;
            if (this.isTerminatedOnBye()) {
                this.setState(this.TERMINATED_STATE);
            }
        }
    } catch (ex) {  
        console.error("SIPDialog:sendRequest():  catched execption, error sending message",ex);
        throw "SIPDialog:sendRequest():  catched execption, error sending message";
    }
}

SIPDialog.prototype.startTimer =function(transaction){
    if(logger!=undefined) logger.debug("SIPDialog:startTimer():transaction="+transaction);
    if (this.timerTask != null && this.timerTask.transaction == transaction) {
        return;
    }
    this.ackSeen = false;
    if (this.timerTask != null) {
        this.timerTask.transaction = transaction;
    } else {
        variabletransaction=transaction;
        this.timer=this.sipStack.getTimer();
        sipdialog=this;
        this.timer=setTimeout(function(){
            sipdialog.timer=setInterval(function(){
                sipdialog.timerTask = new DialogTimerTask(variabletransaction);
            }, sipdialog.BASE_TIMER_INTERVAL);
        }, this.BASE_TIMER_INTERVAL);
    } 
}

SIPDialog.prototype.stopTimer =function(){
    if(logger!=undefined) logger.debug("SIPDialog:stopTimer()");
    if (this.timerTask != null) {
        clearTimeout(this.timer);
        this.timerTask = null;
    }
}

SIPDialog.prototype.updateRequest =function(sipRequest){
    if(logger!=undefined) logger.debug("SIPDialog:updateRequest():sipRequest="+sipRequest);
    var rl = this.getRouteList();
    if (!rl.isEmpty()) {
        sipRequest.setHeader(rl);
    } else {
        sipRequest.removeHeader("Route");
    }
    var mfi=new MessageFactoryImpl();
    if (mfi.getDefaultUserAgentHeader() != null) {
        sipRequest.setHeader(mfi.getDefaultUserAgentHeader());
    }
}

SIPDialog.prototype.createAck =function(cseqno){
    if(logger!=undefined) logger.debug("SIPDialog:createAck():cseqno="+cseqno);
    if (this.method!="INVITE") {
        console.error("SIPDialog:createAck(): dialog was not created with an INVITE" + this.method);
        throw "SIPDialog:createAck(): dialog was not created with an INVITE" + this.method;
    }
    if (cseqno <= 0) {
        console.error("SIPDialog:createAck(): bad cseq <= 0");
        throw "SIPDialog:createAck(): bad cseq <= 0";
    }
    if (this.remoteTarget == null) {
        console.error("SIPDialog:createAck(): cannot create ACK - no remote Target!");
         throw "SIPDialog:createAck(): cannot create ACK - no remote Target!";
    }
    if (this.lastInviteOkReceived < cseqno) {
        console.error("SIPDialog:createAck(): dialog not yet established -- no OK response!");
         throw "SIPDialog:createAck(): dialog not yet established -- no OK response!";
    }
    
    try {
        var uri4transport = null;
        if (this.routeList != null && !this.routeList.isEmpty()) {
            var r = this.routeList.getFirst();
            uri4transport = r.getAddress().getURI();
        } else {
            uri4transport = this.remoteTarget.getURI();
        }
        var transport = uri4transport.getTransportParam();
        if (transport == null) {
            transport = "WS";
        }
        var lp = this.sipProvider.getListeningPoint(transport);
        if (lp == null) {
            console.error("SIPDialog:createAck(): cannot create ACK - no ListeningPoint for transport towards next hop found:"+ transport);
            throw "SIPDialog:createAck(): cannot create ACK - no ListeningPoint for transport towards next hop found:"+ transport;
        }
        var sipRequest = new SIPRequest();
        sipRequest.setMethod("ACK");
        sipRequest.setRequestURI(getRemoteTarget().getURI());
        sipRequest.setCallId(this.callIdHeader);
        sipRequest.setCSeq(new CSeq(cseqno, "ACK"));
        var vias = new Array();
        var via = this.lastResponse.getTopmostVia();
        via.removeParameters();
        if (this.originalRequest != null && this.originalRequest.getTopmostVia() != null) {
            var originalRequestParameters = this.originalRequest.getTopmostVia().getParameters();
            if (originalRequestParameters != null && originalRequestParameters.size() > 0) {
                via.setParameters(originalRequestParameters.clone());
            }
        }
        var utils=new Utils();
        via.setBranch(utils.generateBranchId()); // new branch
        vias.add(via);
        sipRequest.setVia(vias);
        var from = new From();
        from.setAddress(this.localParty);
        from.setTag(this.myTag);
        sipRequest.setFrom(from);
        var to = new To();
        to.setAddress(this.remoteParty);
        if (this.hisTag != null) {
            to.setTag(this.hisTag);
        }
        sipRequest.setTo(to);
        sipRequest.setMaxForwards(new MaxForwards(70));

        if (this.originalRequest != null) {
            var authorization = this.originalRequest.getAuthorization();
            if (authorization != null) {
                sipRequest.setHeader(authorization);
            }
        }
        this.updateRequest(sipRequest);
        return sipRequest;
    } catch (ex) {
        console.error("SIPDialog:createAck(): catched unexpected exception ", ex);
        throw "SIPDialog:createAck(): catched unexpected exception";
    }
}

SIPDialog.prototype.setSipProvider =function(sipProvider){
    if(logger!=undefined) logger.debug("SIPDialog:setSipProvider():sipProvider="+sipProvider);
    this.sipProvider = sipProvider;
}

SIPDialog.prototype.doTargetRefresh =function(sipMessage){
    if(logger!=undefined) logger.debug("SIPDialog:doTargetRefresh():sipMessage="+sipMessage);
    var contactList = sipMessage.getContactHeaders();
    if (contactList != null) {
        var contact = contactList.getFirst();
        this.setRemoteTarget(contact);
    }
}

SIPDialog.prototype.createReliableProvisionalResponse =function(statusCode){
    if(logger!=undefined) logger.debug("SIPDialog:createReliableProvisionalResponse():statusCode="+statusCode);
    if (!(this.firstTransactionIsServerTransaction)) {
        console.error("SIPDialog:createReliableProvisionalResponse(): not a Server Dialog!");
        throw "SIPDialog:createReliableProvisionalResponse(): not a Server Dialog!";
    }
    if (statusCode <= 100 || statusCode > 199) {
        console.error("SIPDialog:createReliableProvisionalResponse(): bad status code ");
        throw "SIPDialog:createReliableProvisionalResponse(): bad status code ";
    }
    var request = this.originalRequest;
    if (request.getMethod()!="INVITE") {
        console.error("SIPDialog:createReliableProvisionalResponse(): bad method");
        throw "SIPDialog:createReliableProvisionalResponse(): bad method";
    }
    var list = request.getHeaders("Supported");
    if (list == null&&!optionPresent(list, "100rel")) {
        list = request.getHeaders("Require");
        if (list == null&&!optionPresent(list, "100rel")) {
            console.error("SIPDialog:createReliableProvisionalResponse(): no Supported/Require 100rel header in the request");
            throw "SIPDialog:createReliableProvisionalResponse(): no Supported/Require 100rel header in the request";
        }
    }
    var response = request.createResponse(statusCode);
    var require = new Require();
    require.setOptionTag("100rel");
    response.addHeader(require);
    var rseq = new RSeq();
    rseq.setSeqNumber("1L");
    var rrl = request.getRecordRouteHeaders();
    if (rrl != null) {
        var rrlclone = rrl;
        response.setHeader(rrlclone);
    }
    return response;
}

SIPDialog.prototype.sendReliableProvisionalResponse =function(relResponse){
    if(logger!=undefined) logger.debug("SIPDialog:sendReliableProvisionalResponse():relResponse="+relResponse);
    if (!this.isServer()) {
        console.error("SIPDialog:sendReliableProvisionalResponse(): not a Server Dialog!");
        throw "SIPDialog:sendReliableProvisionalResponse(): not a Server Dialog!";
    }
    var sipResponse = relResponse;
    if (relResponse.getStatusCode() == 100) {
        console.error("SIPDialog:sendReliableProvisionalResponse(): cannot send 100 as a reliable provisional response");
        throw "SIPDialog:sendReliableProvisionalResponse(): cannot send 100 as a reliable provisional response";
    }
    if (relResponse.getStatusCode() / 100 > 2) {
        console.error("SIPDialog:sendReliableProvisionalResponse(): response code is not a 1xx response - should be in the range 101 to 199 ");
        throw "SIPDialog:sendReliableProvisionalResponse(): response code is not a 1xx response - should be in the range 101 to 199 ";
    }
    if (sipResponse.getToTag() == null) {
        console.error("SIPDialog:sendReliableProvisionalResponse(): badly formatted response -- To tag mandatory for Reliable Provisional Response");
        throw "SIPDialog:sendReliableProvisionalResponse(): badly formatted response -- To tag mandatory for Reliable Provisional Response";
    }
    var requireList = relResponse.getHeaders("Require");
    var found = false;
    if (requireList != null) {
        for(var i=0;i<requireList.length && !found;i++)
        {
            var rh = requireList[i];
            if (rh.getOptionTag().toLowerCase()=="100rel") {
                found = true;
            }
        }
    }
    if (!found) {
        var require = new Require("100rel");
        relResponse.addHeader(require);

    }
    var serverTransaction = this.getFirstTransaction();
    this.setLastResponse(serverTransaction, sipResponse);
    this.setDialogId(sipResponse.getDialogId(true));
    serverTransaction.sendReliableProvisionalResponse(relResponse);
}

SIPDialog.prototype.terminateOnBye =function(terminateFlag){
    if(logger!=undefined) logger.debug("SIPDialog:terminateOnBye():terminateFlag:"+terminateFlag);
    this.terminateOnBye = terminateFlag;
}

SIPDialog.prototype.getMyContactHeader =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getMyContactHeader()");
    return this.contactHeader;
}

SIPDialog.prototype.handleAck =function(){
    if(logger!=undefined) logger.debug("SIPDialog:handleAck()");
    return true;
}

SIPDialog.prototype.setEarlyDialogId =function(earlyDialogId){
    if(logger!=undefined) logger.debug("SIPDialog:setEarlyDialogId():earlyDialogId:"+earlyDialogId);
    this.earlyDialogId = earlyDialogId;
}

SIPDialog.prototype.getEarlyDialogId =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getEarlyDialogId()");
    return this.earlyDialogId;
}

SIPDialog.prototype.optionPresent =function(l,option){
    if(logger!=undefined) logger.debug("SIPDialog:optionPresent():l,option:"+l+","+option);
    for(var i=0;i<l.length;i++)
    {
        var opt =  l[i];
        if (opt != null && option.toLowerCase()==opt.getOptionTag().toLowerCase()) {
            return true;
        }
    }
    return false;
}

SIPDialog.prototype.setLastAckReceived =function(lastAckReceived){
    if(logger!=undefined) logger.debug("SIPDialog:setLastAckReceived():lastAckReceived:"+lastAckReceived);
    this.lastAckReceived = lastAckReceived;
}

SIPDialog.prototype.getLastAckReceived =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getLastAckReceived()");
    return this.lastAckReceived;
}

SIPDialog.prototype.setLastAckSent =function(lastAckSent){
    if(logger!=undefined) logger.debug("SIPDialog:setLastAckSent():lastAckSent:"+lastAckSent);
    this.lastAckSent = lastAckSent;
}

SIPDialog.prototype.isAtleastOneAckSent =function(){
    if(logger!=undefined) logger.debug("SIPDialog:isAtleastOneAckSent()");
    return this.isAcknowledged;
}

SIPDialog.prototype.isBackToBackUserAgent =function(){
    if(logger!=undefined) logger.debug("SIPDialog:isBackToBackUserAgent()");
    return this.isBackToBackUserAgent;
}

SIPDialog.prototype.doDeferredDeleteIfNoAckSent =function(seqno){
    if(logger!=undefined) logger.debug("SIPDialog:doDeferredDeleteIfNoAckSent():seqno:"+seqno);
    if (this.sipStack.getTimer() == null) {
        this.setState(this.TERMINATED_STATE);
    } 
    else if (this.dialogDeleteIfNoAckSentTask == null) {
        variabledialog=seqno;
        var timer=this.sipStack.getTimer();
        sipdialog=this;
        timer=setTimeout(function(){
            sipdialog.dialogDeleteIfNoAckSentTask = new DialogDeleteIfNoAckSentTask(variabledialog);
        },this.TIMER_J* this.BASE_TIMER_INTERVAL);
    }
}

SIPDialog.prototype.setBackToBackUserAgent =function(){
    if(logger!=undefined) logger.debug("SIPDialog:setBackToBackUserAgent()");
    this.isBackToBackUserAgent = true;
}

SIPDialog.prototype.getEventHeader =function(){
    if(logger!=undefined) logger.debug("SIPDialog:getEventHeader()");
    return eventHeader;
}

SIPDialog.prototype.setEventHeader =function(eventHeader){
    if(logger!=undefined) logger.debug("SIPDialog:setEventHeader():eventHeader:"+eventHeader);
    this.eventHeader = eventHeader;
}

SIPDialog.prototype.setServerTransactionFlag =function(serverTransactionFlag){
    if(logger!=undefined) logger.debug("SIPDialog:setServerTransactionFlag():serverTransactionFlag="+serverTransactionFlag);
    this.serverTransactionFlag = serverTransactionFlag;
}

SIPDialog.prototype.setReInviteFlag =function(reInviteFlag){
    if(logger!=undefined) logger.debug("SIPDialog:setReInviteFlag():reInviteFlag="+reInviteFlag);
    this.reInviteFlag = reInviteFlag
}

SIPDialog.prototype.isSequnceNumberValidation =function(){
    if(logger!=undefined) logger.debug("SIPDialog:isSequnceNumberValidation()");
    return this.sequenceNumberValidation;
}

SIPDialog.prototype.disableSequenceNumberValidation =function(){
    if(logger!=undefined) logger.debug("SIPDialog:disableSequenceNumberValidation()");
    this.sequenceNumberValidation = false;
}

SIPDialog.prototype.raiseErrorEvent =function(dialogTimeoutError){
    if(logger!=undefined) logger.debug("SIPDialog:raiseErrorEvent():dialogTimeoutError="+dialogTimeoutError);
    var nextListener=null;
    var newErrorEvent = new SIPDialogErrorEvent(this, dialogTimeoutError);
    for(var i=0;i<this.eventListeners.length;i++)
    {
        nextListener = this.eventListeners[i];
        nextListener.dialogErrorEvent(newErrorEvent);
    }
    this.eventListeners=new Array();
    if (dialogTimeoutError != SIPDialogErrorEvent.DIALOG_ACK_NOT_SENT_TIMEOUT
        && dialogTimeoutError != SIPDialogErrorEvent.DIALOG_ACK_NOT_RECEIVED_TIMEOUT
        && dialogTimeoutError != SIPDialogErrorEvent.DIALOG_REINVITE_TIMEOUT) {
        this.delet();
    }
    this.stopTimer();
}

