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
 *  Implementation of the JAIN-SIP SIPTransactionStack .
 *  @see  gov/nist/javax/sip/stack/SIPTransactionStack.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function SIPTransactionStack() {
    if(logger!=undefined) logger.debug("SIPTransactionStack:SIPTransactionStack()");
    this.classname="SIPTransactionStack"; 
    
    this.messageProcessors=new Array();
    this.sipMessageFactory=null;
    this.activeClientTransactionCount = 0;
    this.mergeTable=new Array();
    this.defaultRouter=null;
    this.needsLogging=null;
    this.stackName=null;
    this.router=null;
    this.maxConnections=-1;
    this.useRouterForAll=null;
    this.readTimeout= -1;
    this.outboundProxy=null;
    this.routerPath=null;
    this.isAutomaticDialogSupportEnabled=null;
    this.forkedEvents=new Array();
    this.generateTimeStampHeader=null;
    this.cancelClientTransactionChecked = true;
    this.remoteTagReassignmentAllowed = true;
    this.logStackTraceOnMessageSend = true;
    this.stackDoesCongestionControl = true;
    this.checkBranchId=false;
    this.isAutomaticDialogErrorHandlingEnabled = true;
    this.isDialogTerminatedEventDeliveredForNullDialog = false;
    this.serverTransactionTable=new Array();
    this.clientTransactionTable=new Array();
    this.terminatedServerTransactionsPendingAck=new Array();
    this.forkedClientTransactionTable=new Array();
    this.dialogCreatingMethods=new Array();
    this.dialogTable=new Array();
    this.earlyDialogTable=new Array();
    this.pendingTransactions=new Array();
    this.unlimitedServerTransactionTableSize = true;
    this.unlimitedClientTransactionTableSize = true;
    this.serverTransactionTableHighwaterMark = 5000;
    this.serverTransactionTableLowaterMark = 4000;
    this.clientTransactionTableHiwaterMark = 1000;
    this.clientTransactionTableLowaterMark = 800;
    this.rfc2543Supported=true;
    this.timer=0;
    this.maxForkTime=0;
    this.toExit=false;
    this.isBackToBackUserAgent = false;
    this.maxListenerResponseTime=-1;
    this.non2XXAckPassedToListener=null;
    this.maxMessageSize=null;
    this.addressResolver = new DefaultAddressResolver();
    
    this.dialogCreatingMethods.push("REFER");
    this.dialogCreatingMethods.push("INVITE");
    this.dialogCreatingMethods.push("SUBSCRIBE");
    this.dialogCreatingMethods.push("REGISTER");
}

SIPTransactionStack.prototype.BASE_TIMER_INTERVAL=500;
SIPTransactionStack.prototype.CONNECTION_LINGER_TIME=8;
SIPTransactionStack.prototype.BRANCH_MAGIC_COOKIE_LOWER_CASE="z9hg4bk";
SIPTransactionStack.prototype.TRYING=100;
SIPTransactionStack.prototype.RINGING=180;

SIPTransactionStack.prototype.reInit =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:reInit()");
    this.messageProcessors = new Array();
    this.pendingTransactions = new Array();
    this.clientTransactionTable = new Array();
    this.serverTransactionTable = new Array();
    this.mergeTable = new Array();
    this.dialogTable = new Array();
    this.earlyDialogTable = new Array();
    this.terminatedServerTransactionsPendingAck = new Array();
    this.forkedClientTransactionTable = new Array();
    this.timer = null;
    this.activeClientTransactionCount=0;
}

SIPTransactionStack.prototype.addExtensionMethod =function(extensionMethod){
    if(logger!=undefined) logger.debug("SIPTransactionStack:addExtensionMethod():extensionMethod="+extensionMethod);
    if (extensionMethod!="NOTIFY") {
        var l=null;
        for(var i=0;i<this.dialogCreatingMethods.length;i++)
        {
            if(this.dialogCreatingMethods[i]==extensionMethod.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '').toUpperCase())
            {
                l=i;
            }
        }
        if(l==null)
        {
            this.dialogCreatingMethods.push(extensionMethod.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '').toUpperCase());
        }
    }
}

SIPTransactionStack.prototype.removeDialog =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:removeDialog()");
    if(typeof arguments[0]=="objet")
    {
        var dialog=arguments[0];
        this.removeDialogobjet(dialog);
    }
    else if(typeof arguments[0]=="string")
    {
        var dialogId=arguments[0];
        this.removeDialogstring(dialogId);
    }
}

SIPTransactionStack.prototype.removeDialogstring =function(dialogId){
    if(logger!=undefined) logger.debug("SIPTransactionStack:removeDialogstring():dialogId="+dialogId);
    var l=null;
    for(var i=0;i<this.dialogTable.length;i++)
    {
        if(this.dialogTable[i][0]==dialogId)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        this.dialogTable.splice(l,1);
    }    
}

SIPTransactionStack.prototype.removeDialogobjet =function(dialog){
    if(logger!=undefined) logger.debug("SIPTransactionStack:removeDialogobjet():dialog="+dialog);
    var id = dialog.getDialogId();
    var earlyId = dialog.getEarlyDialogId();
    if (earlyId != null) {
        var l=null;
        for(var i=0;i<this.earlyDialogTable.length;i++)
        {
            if(this.earlyDialogTable[i][0]==earlyId)
            {
                l=i;
            }
        }
        this.earlyDialogTable.splice(l,1);
        for(i=0;i<this.dialogTable.length;i++)
        {
            if(this.dialogTable[i][0]==earlyId)
            {
                l=i;
            }
        }
        if(l!=null)
        {
            this.dialogTable.splice(l,1);
        } 
    }
    if (id != null) {
        var old = null;
        for(i=0;i<this.dialogTable.length;i++)
        {
            if(this.dialogTable[i][0]==id)
            {
                old = this.dialogTable[i][1];
            }
        }
        if (old == dialog) {
            for(i=0;i<this.dialogTable.length;i++)
            {
                if(this.dialogTable[i][0]==id)
                {
                    l=i;
                }
            }
            if(l!=null)
            {
                this.dialogTable.splice(l,1);
            } 
        }
        if (!dialog.testAndSetIsDialogTerminatedEventDelivered()) {
            var event = new DialogTerminatedEvent(dialog.getSipProvider(),dialog);
            dialog.getSipProvider().handleEvent(event, null);
        }
    }
}

SIPTransactionStack.prototype.findSubscribeTransaction =function(notifyMessage,listeningPoint){
    if(logger!=undefined) logger.debug("SIPTransactionStack:findSubscribeTransaction():notifyMessage="+notifyMessage+",listeningPoint="+listeningPoint);
    var retval = null;
    var thisToTag = notifyMessage.getTo().getTag();
    if (thisToTag == null) {
        return retval;
    }
    var eventHdr = notifyMessage.getHeader("Event");
    if (eventHdr == null) {
        return retval;
    }
    for(var i=0;i<this.clientTransactionTable.length;i++)
    {
        var ct = this.clientTransactionTable[i][1];
        if (ct.getMethod()!="SUBSCRIBE") {
            continue;
        }
        var fromTag = ct.from.getTag();
        var hisEvent = ct.event;
        if (hisEvent == null) {
            continue;
        }
        if (fromTag.toLowerCase()==thisToTag.toLowerCase()
            && hisEvent != null
            && eventHdr.match(hisEvent)
            && notifyMessage.getCallId().getCallId().toLowerCase()==ct.callId.getCallId().toLowerCase()) {
            retval = ct;
            return retval;
        }
    }
    return retval;
}

SIPTransactionStack.prototype.removeTransactionPendingAck =function(serverTransaction){
    if(logger!=undefined) logger.debug("SIPTransactionStack:removeTransactionPendingAck():serverTransaction="+serverTransaction);
    var branchId = serverTransaction.getRequest().getTopmostVia().getBranch();
    var l=null;
    for(var i=0;i<this.terminatedServerTransactionsPendingAck.length;i++)
    {
        if(this.terminatedServerTransactionsPendingAck[i][0]==branchId)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        var r=true;
    }
    else
    {
        r=false;
    }
    if (branchId != null && r) {
        l=null;
        for(i=0;i<this.terminatedServerTransactionsPendingAck.length;i++)
        {
            if(this.terminatedServerTransactionsPendingAck[i][0]==branchId)
            {
                l=i;
            }
        }
        if(l!=null)
        {
            this.terminatedServerTransactionsPendingAck.splice(l,1);
        }
        return true;
    } else {
        return false;
    }

}

SIPTransactionStack.prototype.removeTransactionHash =function(sipTransaction){
    if(logger!=undefined) logger.debug("SIPTransactionStack:removeTransactionHash():sipTransaction="+sipTransaction);
    var sipRequest = sipTransaction.getOriginalRequest();
    if (sipRequest == null) {
        return;
    }
    if (sipTransaction instanceof SIPClientTransaction) {
        var key = sipTransaction.getTransactionId();
        var l=null;
        for(var i=0;i<this.clientTransactionTable.length;i++)
        {
            if(this.clientTransactionTable[i][0]==key)
            {
                l=i;
            }
        }
        this.clientTransactionTable.splice(l,1);
    } 
    else if (sipTransaction instanceof SIPServerTransaction) {
        key = sipTransaction.getTransactionId();
        l=null;
        for(i=0;i<this.serverTransactionTable.length;i++)
        {
            if(this.serverTransactionTable[i][0]==key)
            {
                l=i;
            }
        }
        this.serverTransactionTable.splice(l,1);
    }
}

SIPTransactionStack.prototype.removePendingTransaction =function(tr){
    if(logger!=undefined) logger.debug("SIPTransactionStack:removePendingTransaction():tr="+tr);
    var l=null;
    for(var i=0;i<this.pendingTransactions.length;i++)
    {
        if(this.pendingTransactions[i][0]==tr.getTransactionId())
        {
            l=i;
        }
    }
    if(l!=null)
    {
        this.pendingTransactions.splice(l,1);
    }
}

SIPTransactionStack.prototype.isAlive =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:isAlive()");
    if(!this.toExit)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransactionStack.prototype.getTimer =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getTimer()");
    return this.timer;
}

SIPTransactionStack.prototype.findCancelTransaction =function(cancelRequest,isServer){
    if(logger!=undefined) logger.debug("SIPTransactionStack:findCancelTransaction():cancelRequest="+cancelRequest+",isServer="+isServer);
    if (isServer) {
        for(var i=0;i<this.serverTransactionTable.length;i++)
        {
            var transaction = this.serverTransactionTable[i][1];
            var sipServerTransaction = transaction;
            if (sipServerTransaction.doesCancelMatchTransaction(cancelRequest)) {
                return sipServerTransaction;
            }
        }
    } 
    else {
        for(i=0;i<this.clientTransactionTable.length;i++)
        {
            transaction = this.clientTransactionTable[i][1];
            var sipClientTransaction = transaction;
            if (sipClientTransaction.doesCancelMatchTransaction(cancelRequest)) {
                return sipClientTransaction;
            }
        }
    }
    return null;
}

SIPTransactionStack.prototype.getDialog =function(dialogId){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getDialog():dialogId="+dialogId);
    var l=null;
    for(var i=0;i<this.dialogTable.length;i++)
    {
        if(this.dialogTable[i][0]==dialogId)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        var sipDialog = this.dialogTable[l][1];
        return sipDialog;
    }
    else
    {
        return null;
    }
}

SIPTransactionStack.prototype.isDialogCreated =function(method){
    if(logger!=undefined) logger.debug("SIPTransactionStack:isDialogCreated():method="+method);
    var l=null;
    for(var i=0;i<this.dialogCreatingMethods.length;i++)
    {
        if(this.dialogCreatingMethods[i]==method)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        return true;
    }
    else
    {
        return false
    }
}

SIPTransactionStack.prototype.isRfc2543Supported =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:isRfc2543Supported()");
    return this.rfc2543Supported;
}

SIPTransactionStack.prototype.createDialog =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:isRfc2543Supported()");
    if(arguments.length==1)
    {
        var transaction=arguments[0];
        return this.createDialogargu1(transaction);
    }
    else if(arguments.length==2)
    {
        if(arguments[0].classname=="SipProviderImpl")
        {
            var sipProvider=arguments[0];
            sipResponse=arguments[1];
            return new SIPDialog(sipProvider,sipResponse); 
        }
        else
        {
            transaction=arguments[0];
            var sipResponse=arguments[1];
            return this.createDialogargu2(transaction, sipResponse);
        }
    }
}

SIPTransactionStack.prototype.createDialogargu1 =function(transaction){
    if(logger!=undefined) logger.debug("SIPTransactionStack:createDialogargu1():transaction:"+transaction);
    var retval = null;
    if (transaction instanceof SIPClientTransaction) {
        var dialogId = transaction.getRequest().getDialogId(false);
        var l=null;
        for(var i=0;i<this.earlyDialogTable.length;i++)
        {
            if(this.earlyDialogTable[i][0]==dialogId)
            {
                l=i;
            }
        }
        if (l != null) {
            var dialog = this.earlyDialogTable[l][1];
            if (dialog.getState() == null || dialog.getState() == "EARLY") {
                retval = dialog;
            } 
            else {
                retval = new SIPDialog(transaction);
                this.earlyDialogTable[l][1]=retval;
            }
        } 
        else 
        {
            retval = new SIPDialog(transaction);
            var array=new Array();
            array[0]=dialogId;
            array[1]=retval;
            this.earlyDialogTable.push(array);
        }
    } 
    else {
        retval = new SIPDialog(transaction);
    }
    return retval;
}

SIPTransactionStack.prototype.createDialogargu2 =function(transaction,sipResponse){
    if(logger!=undefined) logger.debug("SIPTransactionStack:createDialogargu2():transaction="+transaction+", sipResponse="+sipResponse);
    var dialogId = transaction.getRequest().getDialogId(false);
    var retval = null;
    var l=null;
    for(var i=0;i<this.earlyDialogTable.length;i++)
    {
        if(this.earlyDialogTable[i][0]==dialogId)
        {
            l=i;
        }
    }
    if (l != null) {
        retval = this.earlyDialogTable[l][1];
        if (sipResponse.isFinalResponse()) {
            this.earlyDialogTable.splice(l,1);
        }
    } 
    else {
        retval = new SIPDialog(transaction, sipResponse);
    }
    return retval;
}

SIPTransactionStack.prototype.createRawMessageChannel =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:createRawMessageChannel()");
    var newChannel = null;
    //var l=null;
    for(var i=0;i<this.messageProcessors.length && newChannel == null;i++)
    {
        var processor = this.messageProcessors[i];
        if (processor.getURLWS()==this.wsurl) {
            newChannel = processor.createMessageChannel();
        }
    }
    return newChannel;
}

SIPTransactionStack.prototype.isNon2XXAckPassedToListener =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:isNon2XXAckPassedToListener()");
    if(this.non2XXAckPassedToListener)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransactionStack.prototype.isTransactionPendingAck =function(serverTransaction){
    if(logger!=undefined) logger.debug("SIPTransactionStack:isTransactionPendingAck():serverTransaction="+serverTransaction);
    var branchId = serverTransaction.getRequest().getTopmostVia().getBranch();
    var l=null;
    for(var i=0;i<this.terminatedServerTransactionsPendingAck.length;i++)
    {
        if(this.terminatedServerTransactionsPendingAck[i][0]==branchId)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransactionStack.prototype.setNon2XXAckPassedToListener =function(passToListener){
    if(logger!=undefined) logger.debug("SIPTransactionStack:setNon2XXAckPassedToListener():passToListener="+passToListener);
    this.non2XXAckPassedToListener = passToListener;
}

SIPTransactionStack.prototype.addForkedClientTransaction =function(clientTransaction){
    if(logger!=undefined) logger.debug("SIPTransactionStack:addForkedClientTransaction():clientTransaction="+clientTransaction);
    var l=null;
    for(var i=0;i<this.forkedClientTransactionTable.length;i++)
    {
        if(this.forkedClientTransactionTable[i][0]==clientTransaction.getTransactionId())
        {
            l=i;
            this.forkedClientTransactionTable[i][1]=clientTransaction;
        }
    }
    if(l==null)
    {
        var array=new Array();
        array[0]=clientTransaction.getTransactionId();
        array[1]=clientTransaction;
        this.forkedClientTransactionTable.push(array);
    }
}

SIPTransactionStack.prototype.getForkedTransaction =function(transactionId){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getForkedTransaction():transactionId:"+transactionId);
    var l=null;
    for(var i=0;i<this.forkedClientTransactionTable.length;i++)
    {
        if(this.forkedClientTransactionTable[i][0]==transactionId)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        return this.forkedClientTransactionTable[l][1];
    }
    else
    {
        return null;
    }
}

SIPTransactionStack.prototype.addTransactionPendingAck =function(serverTransaction){
    if(logger!=undefined) logger.debug("SIPTransactionStack:addTransactionPendingAck():serverTransaction="+serverTransaction);
    var branchId = serverTransaction.getRequest().getTopmostVia().getBranch();
    if (branchId != null) {
        var l=null;
        for(var i=0;i<this.terminatedServerTransactionsPendingAck.length;i++)
        {
            if(this.forkedClientTransactionTable[i][0]==branchId)
            {
                l=i;
                this.forkedClientTransactionTable[i][1]=serverTransaction;
            }
        }
        if(l==null)
        {
            var array=new Array();
            array[0]=branchId;
            array[1]=serverTransaction;
            this.forkedClientTransactionTable.push(array);
        }
    }
}

SIPTransactionStack.prototype.findTransactionPendingAck =function(ackMessage){
    if(logger!=undefined) logger.debug("SIPTransactionStack:findTransactionPendingAck():ackMessage="+ackMessage);
    var l=null;
    for(var i=0;i<this.terminatedServerTransactionsPendingAck.length;i++)
    {
        if(this.terminatedServerTransactionsPendingAck[i][0]==ackMessage.getTopmostVia().getBranch())
        {
            l=i;
        }
    }
    if(l==null)
    {
        return null;
    }
    else
    {
        return this.terminatedServerTransactionsPendingAck[l][1];
    }
}

SIPTransactionStack.prototype.putDialog =function(dialog){
    if(logger!=undefined) logger.debug("SIPTransactionStack:putDialog():dialog="+dialog);
    var dialogId = dialog.getDialogId();
    var l=null;
    for(var i=0;i<this.dialogTable.length;i++)
    {
        if(this.dialogTable[i][0]==dialogId)
        {
            l=i;
        }
    }
    if (l!=null) {
        return;
    }
    dialog.setStack(this);
    var array=new Array()
    array[0]=dialogId;
    array[1]=dialog;
    this.dialogTable.push(array);
}

SIPTransactionStack.prototype.findPendingTransaction =function(requestReceived){
    if(logger!=undefined) logger.debug("SIPTransactionStack:findPendingTransaction():requestReceived="+requestReceived);
    var l=null;
    for(var i=0;i<this.pendingTransactions.length;i++)
    {
        if(this.dialogTable[i][0]==requestReceived.getTransactionId())
        {
            l=i;
        }
    }
    if(l==null)
    {
        return null;
    }
    else
    {
        return this.pendingTransactions[l][1];
    }
}

SIPTransactionStack.prototype.putPendingTransaction =function(tr){
    if(logger!=undefined) logger.debug("SIPTransactionStack:putPendingTransaction():tr="+tr);
    var l=null;
    for(var i=0;i<this.pendingTransactions.length;i++)
    {
        if(this.pendingTransactions[i][0]==tr.getTransactionId())
        {
            l=i;
            this.pendingTransactions[i][1]=tr;
        }
    }
    if(l==null)
    {
        var array=new Array();
        array[0]=tr.getTransactionId();
        array[1]=tr;
        this.pendingTransactions.push(array);
    }
}

SIPTransactionStack.prototype.removePendingTransaction =function(tr){
    if(logger!=undefined) logger.debug("SIPTransactionStack:removePendingTransaction():tr="+tr);
    var l=null;
    for(var i=0;i<this.pendingTransactions.length;i++)
    {
        if(this.pendingTransactions[i][0]==tr.getTransactionId())
        {
            l=i;
        }
    }
    this.pendingTransactions.splice(l,1);
}

SIPTransactionStack.prototype.getServerTransactionTableSize =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getServerTransactionTableSize()");
    return this.serverTransactionTable.length;
}

SIPTransactionStack.prototype.getClientTransactionTableSize =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getClientTransactionTableSize()");
    return this.clientTransactionTable.length;
}

SIPTransactionStack.prototype.findTransaction =function(sipMessage,isServer){
    if(logger!=undefined) logger.debug("SIPTransactionStack:findTransaction():sipMessage="+sipMessage+", isServer="+isServer);
    var retval = null;
    if (isServer) {
        var via = sipMessage.getTopmostVia();
        if (via.getBranch() != null) {
            var key = sipMessage.getTransactionId();
            for(var i=0;i<this.serverTransactionTable.length;i++)
            {
                if(this.serverTransactionTable[i][0]==key)
                {
                    retval=this.serverTransactionTable[i][1];
                }
            }
            if (key.substring(0,7).toLowerCase()=="z9hg4bk") {
                return retval;
            }
        }
        for(i=0;i<this.serverTransactionTable.length;i++)
        {
            var sipServerTransaction = this.serverTransactionTable[i][1];
            if (sipServerTransaction.isMessagePartOfTransaction(sipMessage)) {
                retval = sipServerTransaction;
                return retval;
            }
        }
    } else {
        via = sipMessage.getTopmostVia();
        if (via.getBranch() != null) {
            key = sipMessage.getTransactionId();
            for(i=0;i<this.clientTransactionTable.length;i++)
            {
                if(this.clientTransactionTable[i][0]==key)
                {
                    retval=this.clientTransactionTable[i][1];
                }
            }
            if (key.substring(0,7).toLowerCase()=="z9hg4bk") {
                return retval;
            }
        }
        for(i=0;i<this.serverTransactionTable.length;i++)
        {
            var clientTransaction = this.clientTransactionTable[i][1];
            if (clientTransaction.isMessagePartOfTransaction(sipMessage)) {
                retval = clientTransaction;
                return retval;
            }
        }
    }
    return retval;
}

SIPTransactionStack.prototype.removeFromMergeTable =function(tr){
    if(logger!=undefined) logger.debug("SIPTransactionStack:removeFromMergeTable():tr="+tr);
    var key = tr.getRequest().getMergeId();
    var l=null
    if (key != null) {
        for(var i=0;i<this.mergeTable.length;i++)
        {
            if(this.mergeTable[i][0]==key)
            {
                l=i;
            }
        }
        if(l!=null)
        {
            this.mergeTable.splice(l,1);
        }
    }   
}

SIPTransactionStack.prototype.putInMergeTable =function(sipTransaction,sipRequest){
    if(logger!=undefined) logger.debug("SIPTransactionStack:putInMergeTable():sipTransaction="+sipTransaction+", sipRequest="+sipRequest);
    var mergeKey = sipRequest.getMergeId();
    var l=null;
    if (mergeKey != null) {
        for(var i=0;i<this.mergeTable.length;i++)
        {
            if(this.mergeTable[i][0]==mergeKey)
            {
                this.mergeTable[i][1]=sipTransaction;
                l=i
            }
        }
        if(l==null)
        {
            var array=new Array()
            array[0]=mergeKey;
            array[1]=sipTransaction;
            this.mergeTable.push(array);
        }
    }
}

SIPTransactionStack.prototype.addTransactionHash =function(sipTransaction){
    if(logger!=undefined) logger.debug("SIPTransactionStack:addTransactionHash():sipTransaction="+sipTransaction);
    var sipRequest = sipTransaction.getOriginalRequest();
    if (sipTransaction instanceof SIPClientTransaction) {
        this.activeClientTransactionCount++;
        var l=null;
        var key = sipRequest.getTransactionId();
        for(var i=0;i<this.clientTransactionTable.length;i++)
        {
            if(this.clientTransactionTable[i][0]==key)
            {
                l=i;
                this.clientTransactionTable[i][1]=sipTransaction;
            }
        }
        if(l==null)
        {
            var array=new Array();
            array[0]=key;
            array[1]=sipTransaction;
            this.clientTransactionTable.push(array);
        }
    } else {
        l=null;
        key = sipRequest.getTransactionId();
        for(i=0;i<this.serverTransactionTable.length;i++)
        {
            if(this.serverTransactionTable[i][0]==key)
            {
                l=i;
                this.serverTransactionTable[i][1]=sipTransaction;
            }
        }
        if(l==null)
        {
            array=new Array();
            array[0]=key;
            array[1]=sipTransaction;
            this.serverTransactionTable.push(array);
        }
    }
}

SIPTransactionStack.prototype.setMessageFactory =function(messageFactory){
    if(logger!=undefined) logger.debug("SIPTransactionStack:setMessageFactory():messageFactory="+messageFactory);
    this.sipMessageFactory = messageFactory; 
}

SIPTransactionStack.prototype.checkBranchIdFunction =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:checkBranchIdFunction()");
    return this.checkBranchId;    
}
SIPTransactionStack.prototype.addMessageProcessor =function(newMessageProcessor){
    if(logger!=undefined) logger.debug("SIPTransactionStack:addMessageProcessor():newMessageProcessor="+newMessageProcessor);
    var l=null
    for(var i=0;i<this.messageProcessors.length;i++)
    {
        if(this.messageProcessors[i]==newMessageProcessor)
        {
            l=i;
        }
    }
    if(l==null)
    {
        this.messageProcessors.push(newMessageProcessor);
    }
}

SIPTransactionStack.prototype.findMergedTransaction =function(sipRequest){
    if(logger!=undefined) logger.debug("SIPTransactionStack:findMergedTransaction():sipRequest="+sipRequest);
    if (sipRequest.getMethod()!="INVITE") {
        return null;
    }
    var mergeId = sipRequest.getMergeId();
    var mergedTransaction = null;  
    for(var i=0;i<this.mergeTable.length;i++)
    {
        if(this.mergeTable[i][0]==mergeId)
        {
            mergedTransaction = this.mergeTable[i][1];
        }
    }
    if (mergeId == null) {
        return null;
    } 
    else if (mergedTransaction != null && !mergedTransaction.isMessagePartOfTransaction(sipRequest)) {
        return mergedTransaction;
    } 
    else {
        for (i=0;i<this.dialogTable.length;i++) {
            var dialog=this.dialogTable[i][1];
            var sipDialog = dialog;
            if (sipDialog.getFirstTransaction() != null
                && sipDialog.getFirstTransaction() instanceof SIPServerTransaction) {
                var serverTransaction = sipDialog.getFirstTransaction();
                var transactionRequest = sipDialog.getFirstTransaction().getOriginalRequest();
                if ((!serverTransaction.isMessagePartOfTransaction(sipRequest))
                    && sipRequest.getMergeId()==transactionRequest.getMergeId()) {
                    return sipDialog.getFirstTransaction();
                }
            }
        }
        return null;
    }
}

SIPTransactionStack.prototype.mapTransaction =function(transaction){
    if(logger!=undefined) logger.debug("SIPTransactionStack:mapTransaction():transaction"+transaction);
    if (transaction.isMapped) {
        return;
    }
    this.addTransactionHash(transaction);
    transaction.isMapped = true;      
}

SIPTransactionStack.prototype.createTransaction =function(request,mc,nextHop){
    if(logger!=undefined) logger.debug("SIPTransactionStack:createMessageChannel():request="+request+",mc="+","+mc+",nextHop="+nextHop);
    var returnChannel=null;
    if (mc == null) {
        return null;
    }
    returnChannel = this.createClientTransaction(request, mc);
    returnChannel.setViaPort(nextHop.getPort());
    returnChannel.setViaHost(nextHop.getHost());
    this.addTransactionHash(returnChannel);
    return returnChannel;       
}

SIPTransactionStack.prototype.createClientTransaction =function(sipRequest,encapsulatedMessageChannel){
    if(logger!=undefined) logger.debug("SIPTransactionStack:createClientTransaction():sipRequest="+sipRequest+",encapsulatedMessageChannel="+encapsulatedMessageChannel);
    var ct = new SIPClientTransaction(this, encapsulatedMessageChannel);
    ct.setOriginalRequest(sipRequest);
    return ct;       
}

SIPTransactionStack.prototype.addTransaction =function(transaction){
    if(logger!=undefined) logger.debug("SIPTransactionStack:addTransaction():transaction="+transaction);
    if(transaction instanceof SIPServerTransaction)
    {
        transaction.map();
    }
    this.addTransactionHash(transaction);     
}

SIPTransactionStack.prototype.transactionErrorEvent =function(transactionErrorEvent){
    if(logger!=undefined) logger.debug("SIPTransactionStack:transactionErrorEvent():transactionErrorEvent="+transactionErrorEvent);
    var transaction = transactionErrorEvent.getSource();
    if (transactionErrorEvent.getErrorID() == 2) {
        transaction.setState("TERMINATED");
        if (transaction instanceof SIPServerTransaction) {
            transaction.collectionTime = 0;
        }
        transaction.disableTimeoutTimer();
    }       
}

SIPTransactionStack.prototype.dialogErrorEvent =function(dialogErrorEvent){
    if(logger!=undefined) logger.debug("SIPTransactionStack:dialogErrorEvent():dialogErrorEvent="+dialogErrorEvent);
    var sipDialog = dialogErrorEvent.getSource();
    if (sipDialog != null) {
        sipDialog.delet();
    }     
}

SIPTransactionStack.prototype.stopStack =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:stopStack()");
    if (this.timer != null) {
        clearTimeout(this.timer)
    }
    this.timer = null;
    this.pendingTransactions=new Array();
    this.toExit = true;
    var processorList = this.getMessageProcessors();
    for (var processorIndex = 0; processorIndex < processorList.length; processorIndex++) {
        this.removeMessageProcessor(processorList[processorIndex]);
        this.clientTransactionTable=new Array();
        this.serverTransactionTable=new Array();
        this.dialogTable=new Array();
    }
}

SIPTransactionStack.prototype.getMaxMessageSize =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getMaxMessageSize()");
    return this.maxMessageSize;
}
SIPTransactionStack.prototype.getNextHop =function(sipRequest){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getNextHop():sipRequest="+sipRequest);
    if (this.useRouterForAll) {
        if (this.router != null) {
            return this.router.getNextHop(sipRequest);
        } 
        else {
            return null;
        }
    } 
    else {
        if (sipRequest.getRequestURI().isSipURI() || sipRequest.getRouteHeaders() != null) {
            return this.defaultRouter.getNextHop(sipRequest);
        } 
        else if (this.router != null) {
            return this.router.getNextHop(sipRequest);
        } 
        else {
            return null;
        }
    }   
}

SIPTransactionStack.prototype.setStackName =function(stackName){
    if(logger!=undefined) logger.debug("SIPTransactionStack:setStackName():stackName="+stackName);
    this.stackName = stackName;
}

SIPTransactionStack.prototype.setHostAddress =function(stackAddress){
    if(logger!=undefined) logger.debug("SIPTransactionStack:setHostAddress():stackAddress="+stackAddress);
    if (stackAddress.indexOf(':') != stackAddress.lastIndexOf(':')
        && stackAddress.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '').charAt(0) != '[') {
        this.stackAddress = '[' + stackAddress + ']';
    } else {
        this.stackAddress = stackAddress;
    }
    this.stackInetAddress = stackAddress;   
}

SIPTransactionStack.prototype.getHostAddress =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getHostAddress()"+stackAddress);
    return this.stackAddress ;  
}

SIPTransactionStack.prototype.setRouter =function(router){
    if(logger!=undefined) logger.debug("SIPTransactionStack:setRouter():router="+router);
    this.router = router;
}

SIPTransactionStack.prototype.getRouter =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getRouter()");
    if(arguments.length==0)
    {
        this.getRouterargu0();
    }
    else
    {
        var request=arguments[0];
        this.getRouterargu1(request);
    }
}

SIPTransactionStack.prototype.getRouterargu0 =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getRouterargu0()");
    return this.router;
}

SIPTransactionStack.prototype.getRouterargu1 =function(request){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getRouterargu1():request="+request);
    if (request.getRequestLine() == null) {
        return this.defaultRouter;
    } 
    else if (this.useRouterForAll) {
        return this.router;
    }
    else {
        if (request.getRequestURI().getScheme()=="sip"
            || request.getRequestURI().getScheme()=="sips") {
            return this.defaultRouter;
        } else {
            if (this.router != null) {
                return this.router;
            } else {
                return this.defaultRouter;
            }
        }
    }       
}

SIPTransactionStack.prototype.removeMessageProcessor =function(oldMessageProcessor){
    if(logger!=undefined) logger.debug("SIPTransactionStack:removeMessageProcessor():oldMessageProcessor="+oldMessageProcessor);
    var l=null;
    for(var i=0;i<this.messageProcessors.lengt;i++)
    {
        if (this.messageProcessors[i]==oldMessageProcessor) {
            l=i;
        }
    }
    if (l!=null) {
        this.messageProcessors.splice(l,1);
        oldMessageProcessor.stop();
    }
}

SIPTransactionStack.prototype.getMessageProcessors =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getMessageProcessors()");
    return this.messageProcessors;
}
SIPTransactionStack.prototype.isEventForked =function(ename){
    if(logger!=undefined) logger.debug("SIPTransactionStack:isEventForked():ename="+ename);
    var l=null;
    for(var i=0;i<this.forkedEvents.length;i++)
    {
        if(this.forkedEvents[i]==ename)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransactionStack.prototype.getActiveClientTransactionCount =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getActiveClientTransactionCount()");
    return this.activeClientTransactionCount;    
}

SIPTransactionStack.prototype.isCancelClientTransactionChecked =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:isCancelClientTransactionChecked()");
    return this.cancelClientTransactionChecked; 
}

SIPTransactionStack.prototype.isRemoteTagReassignmentAllowed =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:isRemoteTagReassignmentAllowed()");
    return this.remoteTagReassignmentAllowed;
}

SIPTransactionStack.prototype.getDialogs =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getDialogs()");
    if(arguments.length==0)
    {
        return this.getDialogsargu0();
    }
    else
    {
        var state=arguments[0];
        return this.getDialogsargu1(state);
    }
}

SIPTransactionStack.prototype.getDialogsargu0 =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getDialogsargu0()");
    var dialogs = new Array();
    for(var i=0;i<this.dialogTable.length;i++)
    {
        var l=null;
        for(var x=0;x<dialogs.legnth;x++)
        {
            if(dialogs[x]==this.dialogTable[i][1])
            {
                l=i;
            }
        }
        if(l==null)
        {
            dialogs.push(this.dialogTable[i][1]);
        }
    }
    for(i=0;i<this.earlyDialogTable.length;i++)
    {
        l=null;
        for(x=0;x<dialogs.legnth;x++)
        {
            if(dialogs[x]==this.earlyDialogTable[i][1])
            {
                l=i;
            }
        }
        if(l==null)
        {
            dialogs.push(this.earlyDialogTable[i][1]);
        }
    }
    return dialogs;
}

SIPTransactionStack.prototype.getDialogsargu1 =function(state){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getDialogsargu1():state="+state);
    var matchingDialogs = new Array();
    if ("EARLY"==state) {
        for(var i=0;i<this.earlyDialogTable.length;i++)
        {
            var l=null;
            for(var x=0;x<matchingDialogs.legnth;x++)
            {
                if(matchingDialogs[x]==this.earlyDialogTable[i][1])
                {
                    l=i;
                }
            }
            if(l==null)
            {
                matchingDialogs.push(this.earlyDialogTable[i][1]);
            }
        }
    }
    else {
        for(i=0;i<this.dialogTable.length;i++)
        {
            var dialog=this.dialogTable[i][1];
            if (dialog.getState() != null && dialog.getState()==state) {
                l=null;
                for(x=0;x<matchingDialogs.legnth;x++)
                {
                    if(matchingDialogs[x]==dialog)
                    {
                        l=i;
                    }
                }
                if(l==null)
                {
                    matchingDialogs.push(dialog);
                }
            }
        }
    }
    return matchingDialogs;     
}

SIPTransactionStack.prototype.setTimer =function(timer){
    if(logger!=undefined) logger.debug("SIPTransactionStack:setTimer():timer="+timer);
    this.timer = timer;
}

SIPTransactionStack.prototype.setDeliverDialogTerminatedEventForNullDialog =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:setDeliverDialogTerminatedEventForNullDialog()");
    this.isDialogTerminatedEventDeliveredForNullDialog = true;    
}
SIPTransactionStack.prototype.getAddressResolver =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:getAddressResolver()");
    return this.addressResolver;      
}

/*SIPTransactionStack.prototype.obtainLocalAddress =function(){
    if(logger!=undefined) logger.debug("SIPTransactionStack:obtainLocalAddress()");
   
}*/
//pay attention to audit part