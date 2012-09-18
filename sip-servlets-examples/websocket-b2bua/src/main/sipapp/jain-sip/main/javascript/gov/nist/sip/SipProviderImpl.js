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
 *  Implementation of the JAIN-SIP SipProviderImpl .
 *  @see  gov/nist/javax/sip/SipProviderImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function SipProviderImpl() {
    if(logger!=undefined) logger.debug("SipProviderImpl:SipProviderImpl()");
    this.classname="SipProviderImpl"; 
    this.sipListener=null;
    this.sipStack=null;
    this.listeningPoints=new Array();
    this.eventScanner=null;
    this.address=null;
    this.port=null;
    this.automaticDialogSupportEnabled=null; 
    this.dialogErrorsAutomaticallyHandled = true;
    if(arguments.length!=0)
    {
        var sipStack=arguments[0];
        this.sipStack=sipStack;
        this.eventScanner = sipStack.getEventScanner(); // for quick access.
        this.eventScanner.incrementRefcount();
        this.listeningPoints = new Array();
        this.automaticDialogSupportEnabled = this.sipStack.isAutomaticDialogSupportEnabledFunction();
        this.dialogErrorsAutomaticallyHandled = this.sipStack.isAutomaticDialogErrorHandlingEnabledFunction();
    }
}

SipProviderImpl.prototype.getListeningPoint =function(){
    if(logger!=undefined) logger.debug("SipProviderImpl:getListeningPoint()");
    if (this.listeningPoints.length > 0)
    {
        return this.listeningPoints[0][1];
    }
    else
    {
        return null;
    }
}

SipProviderImpl.prototype.isAutomaticDialogSupportEnabled =function(){
    if(logger!=undefined) logger.debug("SipProviderImpl:isAutomaticDialogSupportEnabled()");
    return this.automaticDialogSupportEnabled;
}
SipProviderImpl.prototype.handleEvent =function(sipEvent,transaction){
    if(logger!=undefined) logger.debug("SipProviderImpl:handleEvent():sipEvent="+sipEvent);
    if(logger!=undefined) logger.debug("SipProviderImpl:handleEvent():transaction="+transaction);
    var eventWrapper = new EventWrapper(sipEvent, transaction);
    if (!this.sipStack.reEntrantListener) 
    {
        this.eventScanner.addEvent(eventWrapper);
    } 
    else 
    {
        this.eventScanner.deliverEvent(eventWrapper);
    }
}

SipProviderImpl.prototype.addSipListener =function(sipListener){
    if(logger!=undefined) logger.debug("SipProviderImpl:addSipListener():sipListener="+sipListener);
    if (this.sipStack.sipListener == null) {
        this.sipStack.sipListener = sipListener;
    }
    else if (this.sipStack.sipListener != sipListener) {
        console.error("SipProviderImpl:addSipListener(): stack already has a listener. Only one listener per stack allowed");
        throw "SipProviderImpl:addSipListener(): stack already has a listener. Only one listener per stack allowed";
    }
    this.sipListener = sipListener;
}

SipProviderImpl.prototype.setListeningPoint =function(listeningPoint){
    if(logger!=undefined) logger.debug("SipProviderImpl:setListeningPoint():listeningPoint:"+listeningPoint);
    if (listeningPoint == null)
    {
        console.error("SipProviderImpl:setListeningPoint(): null listeningPoint argument");
        throw "SipProviderImpl:setListeningPoint(): null listeningPoint argument ";
    }
    var lp = listeningPoint;
    lp.sipProvider = this;
    var transport = lp.getTransport().toUpperCase();
    this.address = listeningPoint.getHostAddress();
    this.port = listeningPoint.getPort();
    this.listeningPoints=new Array();
    var array=new Array();
    array[0]=transport;
    array[1]=listeningPoint;
    this.listeningPoints.push(array);
}

SipProviderImpl.prototype.getSipListener =function(){
    if(logger!=undefined) logger.debug("SipProviderImpl:getSipListener()");
    return this.sipListener;
}

SipProviderImpl.prototype.stop =function(){
    if(logger!=undefined) logger.debug("SipProviderImpl:stop()");
    for(var i=0;i<this.listeningPoints.length;i++)
    {
        var listeningPoint = this.listeningPoints[i][1];
        listeningPoint.removeSipProvider();
    }
    this.eventScanner.stop();
}

SipProviderImpl.prototype.getNewCallId =function(){
    if(logger!=undefined) logger.debug("SipProviderImpl:getNewCallId()");
    var utils=new Utils();
    var callId = utils.generateCallIdentifier(this.getListeningPoint().getHostAddress());
    var callid = new CallID();
    callid.setCallId(callId);
    return callid;
}

SipProviderImpl.prototype.getNewClientTransaction =function(request){
    if(logger!=undefined) logger.debug("SipProviderImpl:getNewClientTransaction():request="+request);
    if (request == null)
    {
        console.error("SipProviderImpl:getNewClientTransaction(): null request argument");
        throw "SipProviderImpl:getNewClientTransaction(): null request argument ";
    }
    if (!this.sipStack.isAlive())
    {
        console.error("SipProviderImpl:getNewClientTransaction(): stack is stopped");
        throw "SipProviderImpl:getNewClientTransaction(): stack is stopped";
    }
    var sipRequest = request;
    if (sipRequest.getTransaction() != null)
    {
        console.error("SipProviderImpl:getNewClientTransaction(): transaction already assigned to request");
        throw "SipProviderImpl:getNewClientTransaction(): transaction already assigned to request";
    }
    if (sipRequest.getMethod()=="ACK") {
        console.error("SipProviderImpl:getNewClientTransaction(): cannot create client transaction for  ACK");
        throw "SipProviderImpl:getNewClientTransaction(): cannot create client transaction for  ACK";
    }
    if (sipRequest.getTopmostVia() == null) {
        var lp = this.getListeningPoint();//i ust tcp for the test. in the end we should change it to ws
        var via = lp.getViaHeader();
        request.setHeader(via);
    }
    try {
        sipRequest.checkHeaders();
    } catch (ex) {
        console.error("SipProviderImpl:getNewClientTransaction(): catched exception: "+ ex);
        throw "SipProviderImpl:getNewClientTransaction(): catched exception: "+ ex;
    }
    var bool=null;
    if(sipRequest.getTopmostVia().getBranch().substring(0, 7)=="z9hG4bK")
    {
        bool=true;
    }
    else
    {
        bool=false;
    }
    if (sipRequest.getTopmostVia().getBranch() != null
        && bool && this.sipStack.findTransaction(request, false) != null) {
        console.error("SipProviderImpl:getNewClientTransaction(): transaction already exists!");
        throw "SipProviderImpl:getNewClientTransaction(): transaction already exists!";
    }
    if (request.getMethod().toUpperCase()=="CANCEL") {
        var ct = this.sipStack.findCancelTransaction(request, false);
        if (ct != null) {
            var retval = this.sipStack.createClientTransaction(request, ct.getMessageChannel());
            retval.addEventListener(this);
            this.sipStack.addTransaction(retval);
            if (ct.getDialog() != null) {
                retval.setDialog(ct.getDialog(), sipRequest.getDialogId(false));
            }
            return retval;
        }
    }
    var hop = null;
    hop = this.sipStack.getNextHop(request);
    if (hop == null)
    {
        console.error("SipProviderImpl:getNewClientTransaction(): cannot resolve next hop -- transaction unavailable");
        throw "SipProviderImpl:getNewClientTransaction(): cannot resolve next hop -- transaction unavailable";
    }
    var transport = hop.getTransport();
    //var listeningPoint = this.getListeningPoint();
    var dialogId = sipRequest.getDialogId(false);
    var dialog = this.sipStack.getDialog(dialogId);
    if (dialog != null && dialog.getState() == "TERMINATED") {
        this.sipStack.removeDialog(dialog);
    }
    var branchId = null;
    bool=null;
    if(sipRequest.getTopmostVia().getBranch().substring(0, 7)!="z9hG4bK")
    {
        bool=true;
    }
    else
    {
        bool=false;
    }
    if (sipRequest.getTopmostVia().getBranch() == null
        || bool || this.sipStack.checkBranchIdFunction() ) {
        var utils=new Utils();
        branchId = utils.generateBranchId();
        sipRequest.getTopmostVia().setBranch(branchId);
    }
    var topmostVia = sipRequest.getTopmostVia();
    if(topmostVia.getTransport() == null)
    {
        topmostVia.setTransport(transport);
    }
    branchId = sipRequest.getTopmostVia().getBranch();
    ct = this.sipStack.createTransaction(sipRequest,this.sipStack.getChannel(),hop);
    if (ct == null)
    {
        console.error("SipProviderImpl:getNewClientTransaction(): cannot create transaction");
        throw "SipProviderImpl:getNewClientTransaction(): cannot create transaction";
    }
    ct.setNextHop(hop);
    ct.setOriginalRequest(sipRequest);
    ct.setBranch(branchId);
    if (this.sipStack.isDialogCreated(request.getMethod())) {
        if (dialog != null)
        {
            ct.setDialog(dialog, sipRequest.getDialogId(false));
        }
        else if (this.isAutomaticDialogSupportEnabled()) {
            var sipDialog = this.sipStack.createDialog(ct);
            ct.setDialog(sipDialog, sipRequest.getDialogId(false));
        }
    } 
    else {
        if (dialog != null) {
            ct.setDialog(dialog, sipRequest.getDialogId(false));
        }
    }
    ct.addEventListener(this);
    return ct;
}

SipProviderImpl.prototype.getNewServerTransaction =function(request){
    if(logger!=undefined) logger.debug("SipProviderImpl:getNewServerTransaction():request="+request);
    if (!this.sipStack.isAlive())
    {
        console.error("SipProviderImpl:getNewServerTransaction(): stack is stopped");
        throw "SipProviderImpl:getNewServerTransaction(): stack is stopped";
    }
    var transaction = null;
    var sipRequest = request;
    try {
        sipRequest.checkHeaders();
    } catch (ex) {
        console.error("SipProviderImpl:getNewServerTransaction(): catched exception:"+ex);
        throw "SipProviderImpl:getNewServerTransaction(): catched exception:"+ex;
    }
    if ( request.getMethod()=="ACK") {
        console.error("SipProviderImpl:getNewServerTransaction(): cannot create Server transaction for  ACK");
        throw "SipProviderImpl:getNewServerTransaction(): cannot Server client transaction for  ACK";
    }
    if (sipRequest.getMethod()=="NOTIFY"
        && sipRequest.getFromTag() != null && sipRequest.getToTag() == null) {
        var ct = this.sipStack.findSubscribeTransaction(sipRequest, this.getListeningPoint());
        if (ct == null && !this.sipStack.deliverUnsolicitedNotify) {
            console.error("SipProviderImpl:getNewServerTransaction(): cannot find matching Subscription (and gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY not set)");
            throw "SipProviderImpl:getNewServerTransaction(): cannot find matching Subscription (and gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY not set)"; 
        }
    }
    if (this.sipStack.isDialogCreated(sipRequest.getMethod())) {
        if (this.sipStack.findTransaction(request, true) != null)
        {
            console.error("SipProviderImpl:getNewServerTransaction(): server transaction already exists!");
            throw "SipProviderImpl:getNewServerTransaction(): server transaction already exists!)"; 
        }
        transaction =  request.getTransaction();
        if (transaction == null)
        {
            console.error("SipProviderImpl:getNewServerTransaction(): transaction not available");
            throw "SipProviderImpl:getNewServerTransaction(): transaction not available"; 
        }
        if (transaction.getOriginalRequest() == null)
        {
            transaction.setOriginalRequest(sipRequest);
        }
        try {
            this.sipStack.addTransaction(transaction);
        } catch (ex) {
            console.error("SipProviderImpl:getNewServerTransaction(): catched exception:"+ex);
            throw "SipProviderImpl:getNewServerTransaction(): catched exception:"+ex; 
        }
        transaction.addEventListener(this);
        if (this.isAutomaticDialogSupportEnabled()) {
            var dialogId = sipRequest.getDialogId(true);
            var dialog = this.sipStack.getDialog(dialogId);
            if (dialog == null) {
                dialog = this.sipStack.createDialog(transaction);
            }
            transaction.setDialog(dialog, sipRequest.getDialogId(true));
            if (sipRequest.getMethod()=="INVITE" && this.isDialogErrorsAutomaticallyHandled()) {
                this.sipStack.putInMergeTable(transaction, sipRequest);
            }
            if (dialog.getRemoteTag() != null && dialog.getLocalTag() != null) {
                this.sipStack.putDialog(dialog);
            }
            dialog.setInviteTransaction(transaction);
            dialog.setState(dialog.EARLY_STATE);
        }
    }
    else {
        if (this.isAutomaticDialogSupportEnabled()) {
            transaction = this.sipStack.findTransaction(request, true);
            if (transaction != null)
            {
                console.error("SipProviderImpl:getNewServerTransaction(): transaction exists!");
                throw "SipProviderImpl:getNewServerTransaction(): transaction exists!"; 
            }
            transaction = request.getTransaction();
            if (transaction == null)
            {
                console.error("SipProviderImpl:getNewServerTransaction(): transaction not available");
                throw "SipProviderImpl:getNewServerTransaction():transaction not available"; 
            }
            if (transaction.getOriginalRequest() == null)
            {
                transaction.setOriginalRequest(sipRequest);
            }
            try {
                this.sipStack.addTransaction(transaction);
            } catch (ex) {
                console.error("SipProviderImpl:getNewServerTransaction(): catched exception:"+ex);
                throw "SipProviderImpl:getNewServerTransaction():  catched exception:"+ex; 
            }
            dialogId = sipRequest.getDialogId(true);
            dialog = this.sipStack.getDialog(dialogId);
            if (dialog != null) {
                dialog.addTransaction(transaction);
                dialog.addRoute(sipRequest);
                transaction.setDialog(dialog, sipRequest.getDialogId(true));
            }
        } 
        else {
            transaction = this.sipStack.findTransaction(request, true);
            if (transaction != null)
            {
                console.error("SipProviderImpl:getNewServerTransaction(): transaction exists!");
                throw "SipProviderImpl:getNewServerTransaction(): transaction exists!"; 
            }
            transaction =  request.getTransaction();
            if (transaction != null) {
                if (transaction.getOriginalRequest() == null)
                    transaction.setOriginalRequest(sipRequest);
                this.sipStack.mapTransaction(transaction);
                dialogId = sipRequest.getDialogId(true);
                dialog = this.sipStack.getDialog(dialogId);
                if (dialog != null) {
                    dialog.addTransaction(transaction);
                    dialog.addRoute(sipRequest);
                    transaction.setDialog(dialog, sipRequest.getDialogId(true));
                }
                return transaction;
            } 
            else {
                var mc = sipRequest.getMessageChannel();
                transaction = this.sipStack.createServerTransaction(mc);
                if (transaction == null)
                {
                    console.error("SipProviderImpl:getNewServerTransaction(): transaction unavailable -- too many servrer transactions");
                    throw "SipProviderImpl:getNewServerTransaction(): transaction unavailable -- too many servrer transactions"; 
                }
                transaction.setOriginalRequest(sipRequest);
                this.sipStack.mapTransaction(transaction);
                dialogId = sipRequest.getDialogId(true);
                dialog = this.sipStack.getDialog(dialogId);
                if (dialog != null) {
                    dialog.addTransaction(transaction);
                    dialog.addRoute(sipRequest);
                    transaction.setDialog(dialog, sipRequest.getDialogId(true));
                }
                return transaction;
            }
        }
    } 
    return transaction;
}

SipProviderImpl.prototype.getSipStack =function(){
    if(logger!=undefined) logger.debug("SipProviderImpl:getSipStack()");
    return this.sipStack;
}

SipProviderImpl.prototype.removeSipListener =function(sipListener){
    if(logger!=undefined) logger.debug("SipProviderImpl:removeSipListener():sipListener="+sipListener);
    if (sipListener == this.getSipListener()) {
        this.sipListener = null;
    }
    var found = false;
    var list=this.sipStack.getSipProviders();
    for(var i=0;i<list.length;i++)
    {
        var nextProvider = list[i];
        if (nextProvider.getSipListener() != null)
        {
            found = true;
        }
    }
    if (!found) {
        this.sipStack.sipListener = null;
    }
}

SipProviderImpl.prototype.sendRequest =function(request){
    if(logger!=undefined) logger.debug("SipProviderImpl:sendRequest():request="+request);
    if (!this.sipStack.isAlive())
    {
        console.error("SipProviderImpl:sendRequest(): stack is stopped");
        throw "SipProviderImpl:sendRequest(): stack is stopped";
    }
    var hop = this.sipStack.getRouter(request).getNextHop(request);
    if (hop == null)
    {
        console.error("SipProviderImpl:sendRequest(): could not determine next hop!");
        throw "SipProviderImpl:sendRequest():  could not determine next hop!";
    }
    var sipRequest = request;
    if ((!sipRequest.isNullRequest()) && sipRequest.getTopmostVia() == null)
    {
        console.error("SipProviderImpl:sendRequest(): invalid SipRequest -- no via header!");
        throw "SipProviderImpl:sendRequest(): invalid SipRequest -- no via header!";
    }
    if (!sipRequest.isNullRequest()) {
        var via = sipRequest.getTopmostVia();
        var branch = via.getBranch();
        if (branch == null || branch.length() == 0) {
            via.setBranch(sipRequest.getTransactionId());
        }
    }
    var messageChannel = null;
    var bool=null;
    for(var i=0;i<this.listeningPoints.length;i++)
    {
        if(hop.getTransport().toUpperCase()==this.listeningPoints[i][0])
        {
            bool=i;
        }
    }
    if(bool!=null)
    {
        bool=true;
    }
    else
    {
        bool=false;
    }
    if (bool)
    {
        messageChannel = this.sipStack.createRawMessageChannel();
    }
    if (messageChannel != null) {
        messageChannel.sendMessage(sipRequest);
    } else {
        console.error("SipProviderImpl:sendRequest(): could not create a message channel for "+ hop.toString());
        throw "SipProviderImpl:sendRequest(): could not create a message channel for "+ hop.toString();
    }
}

SipProviderImpl.prototype.sendResponse =function(response){
    if(logger!=undefined) logger.debug("SipProviderImpl:sendResponse():response="+response);
    if (!this.sipStack.isAlive())
    {
       console.error("SipProviderImpl:sendResponse(): stack is stopped");
       throw "SipProviderImpl:sendResponse(): stack is stopped";
    }
    var sipResponse = response;
    var via = sipResponse.getTopmostVia();
    if (via == null)
    {
        console.error("SipProviderImpl:sendResponse(): no via header in response!");
        throw "SipProviderImpl:sendResponse(): no via header in response!";
    }
    var st = this.sipStack.findTransaction(response, true);
    if ( st != null   && st.getState() != "TERMINATED" && this.isAutomaticDialogSupportEnabled()) {
        console.error("SipProviderImpl:sendResponse(): transaction exists -- cannot send response statelessly");
        throw "SipProviderImpl:sendResponse(): transaction exists -- cannot send response statelessly";
    }
    try {
        var listeningPoint = this.getListeningPoint();
        if (listeningPoint == null)
        {
           console.error("SipProviderImpl:sendResponse(): whoopsa daisy! no listening point found for transport "+transport);
           throw "SipProviderImpl:sendResponse(): whoopsa daisy! no listening point found for transport "+transport;
        }
        var messageChannel = this.sipStack.getChannel();
        messageChannel.sendMessage(sipResponse);
    } catch (ex) {
        console.error("SipProviderImpl:sendResponse(): catched exception: "+ ex);
        throw "SipProviderImpl:sendResponse(): catched exception:"+ ex;
    }
}

SipProviderImpl.prototype.getNewDialog =function(transaction){
    if(logger!=undefined) logger.debug("SipProviderImpl:getNewDialog():transaction="+transaction);
    if (transaction == null)
    {
        console.error("SipProviderImpl:sendResponse(): null transaction!");
        throw "SipProviderImpl:sendResponse():  null transaction!";
    }

    if (!this.sipStack.isAlive())
    {
       console.error("SipProviderImpl:sendResponse(): stack is stopped");
       throw "SipProviderImpl:sendResponse(): stack is stopped";
    }

    if (this.isAutomaticDialogSupportEnabled())
    {
       console.error("SipProviderImpl:sendResponse(): error - AUTOMATIC_DIALOG_SUPPORT is on");
       throw "SipProviderImpl:sendResponse(): error - AUTOMATIC_DIALOG_SUPPORT is on";
    }

    if (!this.sipStack.isDialogCreated(transaction.getRequest().getMethod()))
    {
        console.error("SipProviderImpl:sendResponse(): dialog cannot be created for this method "+ transaction.getRequest().getMethod());
        throw "SipProviderImpl:sendResponse(): dialog cannot be created for this method "+ transaction.getRequest().getMethod();
    }
    
    var dialog = null;
    var sipTransaction = transaction;
    if (transaction instanceof SIPServerTransaction) {
        var st = transaction;
        var response = st.getLastResponse();
        if (response != null) {
            if (response.getStatusCode() != 100)
            {
                console.error("SipProviderImpl:sendResponse(): cannot set dialog after response has been sent");
                throw "SipProviderImpl:sendResponse(): cannot set dialog after response has been sent";
            }
        }
        var sipRequest = transaction.getRequest();
        var dialogId = sipRequest.getDialogId(true);
        dialog = this.sipStack.getDialog(dialogId);
        if (dialog == null) {
            dialog = this.sipStack.createDialog(transaction);
            dialog.addTransaction(sipTransaction);
            dialog.addRoute(sipRequest);
            sipTransaction.setDialog(dialog, null);
        } else {
            sipTransaction.setDialog(dialog, sipRequest.getDialogId(true));
        }
        if (sipRequest.getMethod()=="INVITE" && this.isDialogErrorsAutomaticallyHandled()) {
            this.sipStack.putInMergeTable(st, sipRequest);
        }
    }
    else {
        var sipClientTx = transaction;
        response = sipClientTx.getLastResponse();
        if (response == null) {
            var request = sipClientTx.getRequest();
            dialogId = request.getDialogId(false);
            dialog = this.sipStack.getDialog(dialogId);
            if (dialog != null) {
                console.error("SipProviderImpl:sendResponse(): dialog already exists!");
                throw "SipProviderImpl:sendResponse(): dialog already exists!";
            } 
            else {
                dialog = this.sipStack.createDialog(sipTransaction);
            }
            sipClientTx.setDialog(dialog, null);
        } 
        else {
            console.error("SipProviderImpl:sendResponse(): cannot call this method after response is received!");
            throw "SipProviderImpl:sendResponse(): cannot call this method after response is received!";
        }
    }
    dialog.addEventListener(this);
    return dialog;
}



SipProviderImpl.prototype.transactionErrorEvent =function(transactionErrorEvent){
    if(logger!=undefined) logger.debug("SipProviderImpl:transactionErrorEvent():transactionErrorEvent="+transactionErrorEvent);
    var transaction = transactionErrorEvent.getSource();
    if (transactionErrorEvent.getErrorID() == 2) {
        var errorObject = transactionErrorEvent.getSource();
        var timeout = "TRANSACTION";
        var ev = null;

        if (errorObject instanceof SIPServerTransaction) {
            ev = new TimeoutEvent(this, errorObject,timeout);
        } else {
            var clientTx = errorObject;
            var hop = clientTx.getNextHop();
            ev = new TimeoutEvent(this,errorObject,timeout);
        }
        this.handleEvent(ev,errorObject);
    }
    else if (transactionErrorEvent.getErrorID() == 1) {
        errorObject = transactionErrorEvent.getSource();
        timeout = "TRANSACTION";
        ev = null;
        if (errorObject instanceof SIPServerTransaction) {
            ev = new TimeoutEvent(this, errorObject, timeout);
        }
        else {
            clientTx = errorObject;
            hop = clientTx.getNextHop();
            ev = new TimeoutEvent(this,errorObject,timeout);
        }
        this.handleEvent(ev,errorObject);
    }
    else if (transactionErrorEvent.getErrorID() == 3) {
        errorObject = transactionErrorEvent.getSource();
        var tx = errorObject;
        timeout = Timeout.RETRANSMIT;
        ev = null;
        if (errorObject instanceof SIPServerTransaction) {
            ev = new TimeoutEvent(this, errorObject,timeout);
        } else {
            ev = new TimeoutEvent(this, errorObject, timeout);
        }
        this.handleEvent(ev, errorObject);
    }
}


SipProviderImpl.prototype.dialogErrorEvent =function(dialogErrorEvent){
    if(logger!=undefined) logger.debug("SipProviderImpl:dialogErrorEvent():dialogErrorEvent="+dialogErrorEvent);
    var sipDialog = dialogErrorEvent.getSource();
    var reason = "AckNotReceived";
    if (dialogErrorEvent.getErrorID() == 2) {
        reason= "AckNotSent";
    } 
    else if (dialogErrorEvent.getErrorID() == 3) {
        reason = "ReInviteTimeout";
    }
    var ev = new DialogTimeoutEvent(this, sipDialog, reason);
    this.handleEvent(ev, null);
}


SipProviderImpl.prototype.getListeningPoints =function(){
    if(logger!=undefined) logger.debug("SipProviderImpl:getListeningPoints()");
    var retval = new Array();
    for(var i=0;i<this.listeningPoints.length;i++)
    {
        retval[i]=this.listeningPoints[i][1];
    }
    return retval;
}

SipProviderImpl.prototype.addListeningPoint =function(listeningPoint){
    if(logger!=undefined) logger.debug("SipProviderImpl:addListeningPoint():listeningPoint="+listeningPoint);
    var lp = listeningPoint;
    if (lp.sipProvider != null && lp.sipProvider != this)
    {
        console.error("SipProviderImpl:addListeningPoint(): listening point assigned to another provider");
        throw "SipProviderImpl:addListeningPoint(): listening point assigned to another provider";
    }
    var transport = lp.getTransport().toUpperCase();
    if (this.listeningPoints.length==0) {
        this.address = listeningPoint.getHostAddress();
        this.port = listeningPoint.getPort();
    } 
    else {
        if ((this.address!=listeningPoint.getHostAddress())
            || this.port != listeningPoint.getPort())
            {
            console.error("SipProviderImpl:addListeningPoint(): provider already has different IP Address associated");
            throw "SipProviderImpl:addListeningPoint(): provider already has different IP Address associated";          
        }
    }
    var bool=null;
    var l=null
    for(var i=0;i<this.listeningPoints.length;i++)
    {
        if(transport==this.listeningPoints[i][0])
        {
            bool=i;
            l=i;
        }
    }
    if(bool!=null)
    {
        bool=true;
    }
    else
    {
        bool=false;
    }
    if(l!=null)
    {
        var value=this.listeningPoints[l][1]
    }
    if (bool && value != listeningPoint)
    {
        console.error("SipProviderImpl:addListeningPoint(): listening point already assigned for transport!");
        throw "SipProviderImpl:addListeningPoint(): listening point already assigned for transport!";       
    }
    lp.sipProvider = this;
    var array=new Array();
    array[0]=transport;
    array[1]=lp;
    this.listeningPoints.push(array);
}


SipProviderImpl.prototype.removeListeningPoint =function(listeningPoint){
    if(logger!=undefined) logger.debug("SipProviderImpl:removeListeningPoint():listeningPoint="+listeningPoint);
    var lp = listeningPoint;
    /*if (lp.messageProcessor.inUse())
    {
        console.error("Object is in use");
    }*/
    var l=null;
    for(var i=0;i<this.listeningPoints.length;i++)
    {
        if(lp.getTransport().toUpperCase()==this.listeningPoints[i][0])
        {
            l=i;
        }
    }
    this.listeningPoints.splice(l,1);
}


SipProviderImpl.prototype.removeListeningPoints =function(){
    if(logger!=undefined) logger.debug("SipProviderImpl:removeListeningPoints()");
    for(var i=0;i<this.listeningPoints.length;i++)
    {
        var lp = this.listeningPoints[i][1];
        lp.messageProcessor.stop();
    }
    this.listeningPoints=new Array();
}


SipProviderImpl.prototype.setAutomaticDialogSupportEnabled =function(automaticDialogSupportEnabled){
    if(logger!=undefined) logger.debug("SipProviderImpl:setAutomaticDialogSupportEnabled():automaticDialogSupportEnabled"
        +automaticDialogSupportEnabled);
    this.automaticDialogSupportEnabled = automaticDialogSupportEnabled;
    if ( this.automaticDialogSupportEnabled ) {
        this.dialogErrorsAutomaticallyHandled = true;
    }
}


SipProviderImpl.prototype.setDialogErrorsAutomaticallyHandled =function(){
    if(logger!=undefined) logger.debug("SipProviderImpl:setDialogErrorsAutomaticallyHandled()");
    this.dialogErrorsAutomaticallyHandled = true;
}

SipProviderImpl.prototype.isDialogErrorsAutomaticallyHandled =function(){
    if(logger!=undefined) logger.debug("SipProviderImpl:isDialogErrorsAutomaticallyHandled()");
    return this.dialogErrorsAutomaticallyHandled;
}
