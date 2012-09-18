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
 *  Implementation of the JAIN-SIP SIPTransaction .
 *  @see  gov/nist/javax/sip/stack/SIPTransaction.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
var siptransaction;
function SIPTransaction(newParentStack,newEncapsulatedChannel) {
    if(logger!=undefined) logger.debug("SIPTransaction:SIPTransaction()");
    this.classname="SIPTransaction"; 
    this.toListener=null;
    this.applicationData=null;
    this.lastResponse=null;
    this.isMapped=null;
    this.transactionId=null;
    this.auditTag = 0;
    this.sipStack=newParentStack;
    this.originalRequest=null;
    this.encapsulatedChannel=newEncapsulatedChannel;
    if(arguments.length==0)
    {
        this.wsurl=null;
    }
    else
    {
        this.wsurl=this.encapsulatedChannel.wsurl;
        /*if (this.isReliable()) {            
            this.encapsulatedChannel.useCount++;
        }*/
        this.disableTimeoutTimer();
        this.addEventListener(newParentStack);
    }
    this.eventListeners = new Array();
    this.transactionTimerStarted = false;
    this.branch=null;
    this.method=null;
    this.cSeq=null;
    this.currentState=null;
    this.timeoutTimerTicksLeft=null;
    this.from=null;
    this.to=null;
    this.event=null;
    this.callId=null;
    this.collectionTime=null;
    this.toTag=null;
    this.fromTag=null;
    this.terminatedEventDelivered=null;
}

SIPTransaction.prototype = new WSMessageChannel();
SIPTransaction.prototype.constructor=SIPTransaction;
SIPTransaction.prototype.BASE_TIMER_INTERVAL = 500;
SIPTransaction.prototype.T4 = 5000 / SIPTransaction.prototype.BASE_TIMER_INTERVAL;
SIPTransaction.prototype.T2 = 4000 / SIPTransaction.prototype.BASE_TIMER_INTERVAL;
SIPTransaction.prototype.TIMER_I = SIPTransaction.prototype.T4;
SIPTransaction.prototype.TIMER_K = SIPTransaction.prototype.T4;
SIPTransaction.prototype.TIMER_D = 32000 / SIPTransaction.prototype.BASE_TIMER_INTERVAL;
SIPTransaction.prototype.T1 = 1;
SIPTransaction.prototype.TIMER_A = 1;
SIPTransaction.prototype.TIMER_B = 64;
SIPTransaction.prototype.TIMER_J = 64;
SIPTransaction.prototype.TIMER_F = 64;
SIPTransaction.prototype.TIMER_H = 64;
SIPTransaction.prototype.INITIAL_STATE=null;
SIPTransaction.prototype.TRYING_STATE = "TRYING";
SIPTransaction.prototype.CALLING_STATE = "CALLING";
SIPTransaction.prototype.PROCEEDING_STATE = "PROCEEDING";
SIPTransaction.prototype.COMPLETED_STATE = "COMPLETED";
SIPTransaction.prototype.CONFIRMED_STATE = "CONFIRMED";
SIPTransaction.prototype.TERMINATED_STATE = "TERMINATED";
SIPTransaction.prototype.MAXIMUM_RETRANSMISSION_TICK_COUNT = 8;
SIPTransaction.prototype.TIMEOUT_RETRANSMIT = 3;
SIPTransaction.prototype.CONNECTION_LINGER_TIME=8;

function lingerTimer() {
    if(logger!=undefined) logger.debug("lingerTimer()");
    var transaction = siptransaction;
    var sipStack = transaction.getSIPStack();
    if (transaction instanceof SIPClientTransaction) {
        sipStack.removeTransaction(transaction);
    } 
    else if (transaction instanceof ServerTransaction) {
        sipStack.removeTransaction(transaction);
    }
}

SIPTransaction.prototype.getBranchId =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getBranchId()");
    return this.branch;
}

SIPTransaction.prototype.setOriginalRequest =function(newOriginalRequest){
    if(logger!=undefined) logger.debug("SIPTransaction:setOriginalRequest():newOriginalRequest="+newOriginalRequest);
    var newBranch= null;
    if (this.originalRequest != null
        && (this.originalRequest.getTransactionId()!=newOriginalRequest.getTransactionId())) {
        this.sipStack.removeTransactionHash(this);
    }
    this.originalRequest = newOriginalRequest;
    this.method = newOriginalRequest.getMethod();
    this.from =  newOriginalRequest.getFrom();
    this.to =  newOriginalRequest.getTo();
    this.toTag = this.to.getTag();
    this.fromTag = this.from.getTag();
    this.callId =  newOriginalRequest.getCallId();
    this.cSeq = newOriginalRequest.getCSeq().getSeqNumber();
    this.event =  newOriginalRequest.getHeader("Event");
    this.transactionId = newOriginalRequest.getTransactionId();
    this.originalRequest.setTransaction(this);
    newBranch = newOriginalRequest.getViaHeaders().getFirst().getBranch();
    if (newBranch != null) {
        this.setBranch(newBranch);
    } else {
        this.setBranch(newOriginalRequest.getTransactionId());
    }
}

SIPTransaction.prototype.getOriginalRequest =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getOriginalRequest()");
    return this.originalRequest;
}

SIPTransaction.prototype.getRequest =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getRequest()");
    return this.originalRequest;
}

SIPTransaction.prototype.isInviteTransaction =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:isInviteTransaction()");
    if(this.getMethod()=="INVITE")
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransaction.prototype.isCancelTransaction =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:isCancelTransaction()");
    if(this.getMethod()=="CANCEL")
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransaction.prototype.isByeTransaction =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:isByeTransaction()");
    if(this.getMethod()=="BYE")
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransaction.prototype.getMessageChannel =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getMessageChannel()");
    return this.encapsulatedChannel;
}

SIPTransaction.prototype.setBranch =function(newBranch){
    if(logger!=undefined) logger.debug("SIPTransaction:setBranch():newBranch:"+newBranch);
    this.branch = newBranch;
}
SIPTransaction.prototype.getBranch =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getBranch()");
    if (this.branch == null) {
        this.branch = this.getOriginalRequest().getTopmostVia().getBranch();
    }
    return this.branch;
}

SIPTransaction.prototype.getMethod =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getMethod()");
    return this.method;
}

SIPTransaction.prototype.getCSeq =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getCSeq()");
    return this.cSeq;
}

SIPTransaction.prototype.setState =function(newState){
    if(logger!=undefined) logger.debug("SIPTransaction:setState():newState="+newState);
    if (this.currentState == "COMPLETED") {
        if (newState != "TERMINATED" && newState != "CONFIRMED")
            newState = "COMPLETED";
    }
    if (this.currentState == "CONFIRMED") {
        if (newState != "TERMINATED")
        {
            newState = "CONFIRMED";
        }
    }
    if (this.currentState != "TERMINATED")
    {
        this.currentState = newState;
    }
    else
    {
        newState = this.currentState;
    }
}

SIPTransaction.prototype.getState =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getState()");
    return this.currentState;
}

SIPTransaction.prototype.enableTimeoutTimer =function(tickCount){
    if(logger!=undefined) logger.debug("SIPTransaction:enableTimeoutTimer():tickCount="+tickCount);
    this.timeoutTimerTicksLeft = tickCount;
}

SIPTransaction.prototype.disableTimeoutTimer =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:disableTimeoutTimer()");
    this.timeoutTimerTicksLeft = -1;
}

SIPTransaction.prototype.fireTimer =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:fireTimer()");
    if (this.timeoutTimerTicksLeft != -1) {
        if (--this.timeoutTimerTicksLeft == 0) {
            this.fireTimeoutTimer();
        }
    }
}

SIPTransaction.prototype.isTerminated =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:isTerminated()");
    if(this.getState() == "TERMINATED")
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransaction.prototype.getURLWS =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getURLWS()");
    return this.encapsulatedChannel.getURLWS();
}

SIPTransaction.prototype.getKey =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getKey()");
    return this.encapsulatedChannel.getKey();
}

SIPTransaction.prototype.getSIPStack =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getSIPStack()");
    return this.sipStack;
}

SIPTransaction.prototype.getTransport =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getTransport()");
    return this.encapsulatedChannel.getTransport();
}

SIPTransaction.prototype.isReliable =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:isReliable()");
    return true;
}

SIPTransaction.prototype.getViaHeader =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getViaHeader()");
    var channelViaHeader = WSMessageChannel.prototype.getViaHeader.call(this);
    channelViaHeader.setBranch(this.branch);
    return channelViaHeader;
}

SIPTransaction.prototype.sendMessage=function(messageToSend){
    if(logger!=undefined) logger.debug("SIPTransaction:sendMessageargu1():messageToSend="+messageToSend);
    this.encapsulatedChannel.sendMessage(messageToSend);
//this.startTransactionTimer();
}

SIPTransaction.prototype.addEventListener =function(newListener){
    if(logger!=undefined) logger.debug("SIPTransaction:addEventListener():newListener="+newListener);
    var l=null;
    for(var i=0;i<this.eventListeners.length;i++)
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

SIPTransaction.prototype.removeEventListener =function(oldListener){
    if(logger!=undefined) logger.debug("SIPTransaction:removeEventListener():oldListener="+oldListener);
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

SIPTransaction.prototype.raiseErrorEvent =function(errorEventID){
    if(logger!=undefined) logger.debug("SIPTransaction:raiseErrorEvent():errorEventID="+errorEventID);
    var nextListener=null;
    var newErrorEvent = new SIPTransactionErrorEvent(this, errorEventID);
    for(var i=0;i<this.eventListeners.length;i++)
    {
        nextListener = this.eventListeners[i];
        nextListener.transactionErrorEvent(newErrorEvent);
    }
    if (errorEventID != this.TIMEOUT_RETRANSMIT) {
        this.eventListeners=new Array();
        this.setState("TERMINATED");
        if (this instanceof SIPServerTransaction && this.isByeTransaction() && this.getDialog() != null)
        {
            this.getDialog().setState("TERMINATED");
        }
    }
}

SIPTransaction.prototype.isServerTransaction =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:isServerTransaction()");
    if(this instanceof SIPServerTransaction)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransaction.prototype.getDialog =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getDialog()");
}

SIPTransaction.prototype.setDialog =function(sipDialog,dialogId){
    if(logger!=undefined) logger.debug("SIPTransaction:setDialog():sipDialog="+sipDialog+", dialogId="+dialogId);
}

SIPTransaction.prototype.getViaHost =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getViaHost()");
    return this.getViaHeader().getHost();
}

SIPTransaction.prototype.getLastResponse =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getLastResponse()");
    return this.lastResponse;
}

SIPTransaction.prototype.getResponse =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getResponse()");
    return this.lastResponse;
}

SIPTransaction.prototype.getTransactionId =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getTransactionId()");
    return this.transactionId;
}

SIPTransaction.prototype.hashCode =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:hashCode()");
    if (this.transactionId == null)
    {
        return -1;
    }
    else
    { 
        var hash = 0;
        var x=this.transactionId;
        if(!(x == null || x.value == ""))  
        {  
            for (var i = 0; i < x.length; i++)  
            {  
                hash = hash * 31 + x.charCodeAt(i);  
                var MAX_VALUE = 0x7fffffff;  
                var MIN_VALUE = -0x80000000;  
                if(hash > MAX_VALUE || hash < MIN_VALUE)  
                {  
                    hash &= 0xFFFFFFFF;  
                }  
            }  
        }  
        return hash;
    }
}

SIPTransaction.prototype.getViaPort =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getViaPort()");
    return this.getViaHeader().getPort();
}

SIPTransaction.prototype.getPort =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getPort()");
    return this.encapsulatedChannel.getPort();
}

SIPTransaction.prototype.doesCancelMatchTransaction =function(requestToTest){
    if(logger!=undefined) logger.debug("SIPTransaction:doesCancelMatchTransaction():requestToTest="+requestToTest);
    var viaHeaders;
    var topViaHeader;
    var messageBranch;
    var transactionMatches = false;
    if (this.getOriginalRequest() == null || this.getOriginalRequest().getMethod()=="CANCEL")
    {
        return false;
    }
    viaHeaders = requestToTest.getViaHeaders();
    if (viaHeaders != null) {
        topViaHeader = viaHeaders.getFirst();
        messageBranch = topViaHeader.getBranch();
        if (messageBranch != null) {
            if(messageBranch.toLowerCase().substring(0,7)!="z9hg4bk")
            {
                messageBranch = null;
            }
        }
        if (messageBranch != null && this.getBranch() != null) {
            if (this.getBranch().toLowerCase()==messageBranch.toLowerCase()
                && topViaHeader.getSentBy()==
                this.getOriginalRequest().getViaHeaders().getFirst().getSentBy()) {
                transactionMatches = true;
            }
        } else {
            if (this.getOriginalRequest().getRequestURI()==
                requestToTest.getRequestURI()
                && this.getOriginalRequest().getTo()==
                requestToTest.getTo()
                && this.getOriginalRequest().getFrom()==
                requestToTest.getFrom()
                && this.getOriginalRequest().getCallId().getCallId()==
                requestToTest.getCallId().getCallId()
                && this.getOriginalRequest().getCSeq().getSeqNumber() == requestToTest.getCSeq().getSeqNumber()
                && topViaHeader==this.getOriginalRequest().getViaHeaders().getFirst()) {
                transactionMatches = true;
            }
        }
    }
    if (transactionMatches) {
        this.setPassToListener();
    }
    return transactionMatches;
}

SIPTransaction.prototype.close =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:close()");
    this.encapsulatedChannel.close();
}

SIPTransaction.prototype.isSecure =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:isSecure()");
    return this.encapsulatedChannel.isSecure();
}

SIPTransaction.prototype.getMessageProcessor =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getMessageProcessor()");
    return this.encapsulatedChannel.getMessageProcessor();
}

SIPTransaction.prototype.setApplicationData =function(applicationData){
    if(logger!=undefined) logger.debug("SIPTransaction:setApplicationData():applicationData="+applicationData);
    this.applicationData = applicationData;
}

SIPTransaction.prototype.getURLWS =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getURLWS()");
    return this.wsurl;
}

SIPTransaction.prototype.getApplicationData =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getApplicationData()");
    return this.applicationData;
}

SIPTransaction.prototype.setEncapsulatedChannel =function(messageChannel){
    if(logger!=undefined) logger.debug("SIPTransaction:setEncapsulatedChannel():messageChannel="+messageChannel);
    this.encapsulatedChannel = messageChannel;
}

SIPTransaction.prototype.getSipProvider =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:getSipProvider()");
    return this.getMessageProcessor().getListeningPoint().getProvider();
}

SIPTransaction.prototype.raiseIOExceptionEvent =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:raiseIOExceptionEvent()");
    this.setState("TERMINATED");
}

SIPTransaction.prototype.passToListener =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:passToListener()");
    return this.toListener;
}

SIPTransaction.prototype.setPassToListener =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:setPassToListener()");
    this.toListener = true;
}

SIPTransaction.prototype.testAndSetTransactionTerminatedEvent =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:testAndSetTransactionTerminatedEvent()");
    var retval=!this.terminatedEventDelivered;
    this.terminatedEventDelivered = true;
    return retval;
}

SIPTransaction.prototype.startTransactionTimer =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:startTransactionTimer()");
    
}

SIPTransaction.prototype.isMessagePartOfTransaction =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:isMessagePartOfTransaction()");
    
}

SIPTransaction.prototype.fireTimeoutTimer =function(){
    if(logger!=undefined) logger.debug("SIPTransaction:fireTimeoutTimer()");
    
}