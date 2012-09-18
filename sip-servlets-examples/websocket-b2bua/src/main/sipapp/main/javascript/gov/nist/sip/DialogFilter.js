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
 *  Implementation of the JAIN-SIP DialogFilter .
 *  @see  gov/nist/javax/sip/DialogFilter.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function DialogFilter(sipStack) {
    if(logger!=undefined) logger.debug("DialogFilter:DialogFilter(): sipStack="+sipStack);
    this.classname="DialogFilter"; 
    this.sipStack=sipStack;
    this.transactionChannel=null;
    this.listeningPoint=null;
}

DialogFilter.prototype.processResponse =function(){
    if(logger!=undefined) logger.debug("DialogFilter:processResponse()");
    if(arguments.length==2)
    {
        var sipResponse=arguments[0];
        var incomingChannel=arguments[1];
        this.processResponseargu2(sipResponse, incomingChannel);
    }
    else 
    {
        var response=arguments[0];
        var incomingMessageChannel=arguments[1];
        var dialog=arguments[2];
        this.processResponseargu3(response, incomingMessageChannel, dialog);
    }
}

DialogFilter.prototype.processResponseargu2 =function(sipResponse,incomingChannel){
    if(logger!=undefined) logger.debug("DialogFilter:processResponseargu2():sipResponse="+sipResponse);
    if(logger!=undefined) logger.debug("DialogFilter:processResponseargu2():incomingChannel="+incomingChannel);
    var dialogID = sipResponse.getDialogId(false);
    var sipDialog = this.sipStack.getDialog(dialogID);
    var method = sipResponse.getCSeq().getMethod();
    var utils=new Utils();
    if (this.sipStack.checkBranchId && !utils.responseBelongsToUs(sipResponse)) {
        return;
    }
    if (this.listeningPoint == null) {
        return;
    }
    var sipProvider = this.listeningPoint.getProvider();
    if (sipProvider == null) {
        return;
    }
    if (sipProvider.getSipListener() == null) {
        return;
    }
    var transaction = this.transactionChannel;
    if (sipDialog == null && transaction != null) {
        sipDialog = transaction.getDialog(dialogID);
        if (sipDialog != null && sipDialog.getState() == "TERMINATED")
            sipDialog = null;
    }
    if (this.transactionChannel != null) {
        var originalFrom = this.transactionChannel.getRequest().getFromTag();
        if (originalFrom == null ^ sipResponse.getFrom().getTag() == null) {
            return;
        }
        if (originalFrom != null
            && originalFrom.toLowerCase()!=sipResponse.getFrom().getTag().toLowerCase()) {
            return;
        }
    }
    if (this.sipStack.isDialogCreated(method) && sipResponse.getStatusCode() != 100
        && sipResponse.getFrom().getTag() != null && sipResponse.getTo().getTag() != null
        && sipDialog == null) {
        if (sipProvider.isAutomaticDialogSupportEnabled()) {
            if (this.transactionChannel != null) {
                if (sipDialog == null) {
                    sipDialog = this.sipStack.createDialog(this.transactionChannel, sipResponse);
                    this.transactionChannel.setDialog(sipDialog, sipResponse.getDialogId(false));
                }
            } else {
                sipDialog = this.sipStack.createDialog(sipProvider, sipResponse);
            }
        }
    } else {
        if (sipDialog != null && transaction == null
            && sipDialog.getState() != "TERMINATED") {
            if (sipDialog.getState() == "TERMINATED" && 
                (sipResponse.getStatusCode()>= 200||sipResponse.getStatusCode()<=299)) {
                if ((sipResponse.getStatusCode()>= 200||sipResponse.getStatusCode()<=299)
                    && sipResponse.getCSeq().getMethod()=="INVITE") {
                    var ackRequest = sipDialog.createAck(sipResponse.getCSeq().getSeqNumber());
                    sipDialog.sendAck(ackRequest);
                }
                return;
            } 
            else {
                var ackAlreadySent = false;
                if (sipDialog.isAckSeen() && sipDialog.getLastAckSent() != null) {
                    if (sipDialog.getLastAckSent().getCSeq().getSeqNumber() == sipResponse.getCSeq().getSeqNumber()
                        && sipResponse.getDialogId(false)==sipDialog.getLastAckSent().getDialogId(false)) {
                        ackAlreadySent = true;
                    }
                }
                if (ackAlreadySent && sipResponse.getCSeq().getMethod()==sipDialog.getMethod()) {
                    sipDialog.resendAck();
                    return;
                }
            }
        }

    }
    if (sipDialog != null && sipResponse.getStatusCode() != 100
        && sipResponse.getTo().getTag() != null) {
        sipDialog.setLastResponse(transaction, sipResponse);
    }
    var responseEvent = new ResponseEventExt(sipProvider,transaction,sipDialog,sipResponse);
    if (sipResponse.getCSeq().getMethod()=="INVITE") {
        var originalTx = this.sipStack.getForkedTransaction(sipResponse.getTransactionId());
        responseEvent.setOriginalTransaction(originalTx);
    }
    sipProvider.handleEvent(responseEvent, transaction);
}

DialogFilter.prototype.processResponseargu3 =function(response,incomingMessageChannel,dialog){
    if(logger!=undefined) logger.debug("DialogFilter:processResponseargu3():response="+response);
    if(logger!=undefined) logger.debug("DialogFilter:processResponseargu3(): incomingMessageChannel="+incomingMessageChannel);
    if(logger!=undefined) logger.debug("DialogFilter:processResponseargu3(): ,dialog="+dialog);
    if (this.listeningPoint == null) {
        return;
    }
    var utils=new Utils();
    if (this.sipStack.checkBranchId && !utils.responseBelongsToUs(response)) {
        return;
    }
    var sipProvider = this.listeningPoint.getProvider();
    if (sipProvider == null) {
        return;
    }
    if (sipProvider.getSipListener() == null) {
        return;
    }
    var transaction = this.transactionChannel;
    if (transaction == null) {
        if (dialog != null) {
            if (response.getStatusCode()<200||response.getStatusCode()>299) {
                return;
            } else if (dialog.getState() == "TERMINATED") {
                return;
            } else {
                var ackAlreadySent = false;
                if (dialog.isAckSeen() && dialog.getLastAckSent() != null) {
                    if (dialog.getLastAckSent().getCSeq().getSeqNumber() == response.getCSeq().getSeqNumber()) {
                        ackAlreadySent = true;
                    }
                }
                if (ackAlreadySent
                    && response.getCSeq().getMethod()==dialog.getMethod()) {
                    dialog.resendAck();
                    return;
                }
            }
        }
        var sipEvent = new ResponseEventExt(sipProvider,transaction,dialog,response);
        if (response.getCSeqHeader().getMethod()=="INVITE") {
            var forked = this.sipStack.getForkedTransaction(response.getTransactionId());
            sipEvent.setOriginalTransaction(forked);
        }
        sipProvider.handleEvent(sipEvent, transaction);
        return;
    }
    var responseEvent = null;
    responseEvent = new ResponseEventExt(sipProvider,transaction,dialog,response);
    if (response.getCSeqHeader().getMethod()=="INVITE") {
        responseEvent.setOriginalTransaction(transaction);
    }
    if (dialog != null && response.getStatusCode() != 100) {
        dialog.setLastResponse(transaction, response);
        transaction.setDialog(dialog, dialog.getDialogId());
    }
    sipProvider.handleEvent(responseEvent, transaction);
}

DialogFilter.prototype.getSipStack =function(){
    if(logger!=undefined) logger.debug("DialogFilter:getSipStack()");
    return this.sipStack;
}

DialogFilter.prototype.sendBadRequestResponse =function(sipRequest,transaction,reasonPhrase){
    if(logger!=undefined) logger.debug("DialogFilter:sendBadRequestResponse():sipRequest="+sipRequest);
        if(logger!=undefined) logger.debug("DialogFilter:sendBadRequestResponse(): transaction="+transaction);
        if(logger!=undefined) logger.debug("DialogFilter:sendBadRequestResponse(): reasonPhrase"+reasonPhrase);
    var sipResponse = sipRequest.createResponse(400);
    if (reasonPhrase != null)
    {
        sipResponse.setReasonPhrase(reasonPhrase);
    }
    var mfi=new MessageFactoryImpl();
    var serverHeader = mfi.getDefaultServerHeader();
    if (serverHeader != null) {
        sipResponse.setHeader(serverHeader);
    }
    if (sipRequest.getMethod()=="INVITE") {
        this.sipStack.addTransactionPendingAck(transaction);
    }
    transaction.sendResponse(sipResponse);
}


DialogFilter.prototype.sendCallOrTransactionDoesNotExistResponse =function(sipRequest,transaction){
    if(logger!=undefined) logger.debug("DialogFilter:sendCallOrTransactionDoesNotExistResponse():sipRequest="+sipRequest);
    if(logger!=undefined) logger.debug("DialogFilter:sendCallOrTransactionDoesNotExistResponse(): transaction="+transaction);
    var sipResponse = sipRequest.createResponse(481);
    var mfi=new MessageFactoryImpl();
    var serverHeader = mfi.getDefaultServerHeader();
    if (serverHeader != null) {
        sipResponse.setHeader(serverHeader);
    }
    if (sipRequest.getMethod()=="INVITE") {
        this.sipStack.addTransactionPendingAck(transaction);
    }
    transaction.sendResponse(sipResponse);
}

DialogFilter.prototype.sendLoopDetectedResponse =function(sipRequest,transaction){
    if(logger!=undefined) logger.debug("DialogFilter:sendLoopDetectedResponse():sipRequest="+sipRequest);
    if(logger!=undefined) logger.debug("DialogFilter:sendLoopDetectedResponse(): transaction="+transaction);
    var sipResponse = sipRequest.createResponse(482);
    var mfi=new MessageFactoryImpl();
    var serverHeader = mfi.getDefaultServerHeader();
    if (serverHeader != null) {
        sipResponse.setHeader(serverHeader);
    }
    this.sipStack.addTransactionPendingAck(transaction);
    transaction.sendResponse(sipResponse);
}

DialogFilter.prototype.processRequest =function(sipRequest,incomingMessageChannel){
    if(logger!=undefined) logger.debug("DialogFilter:processRequest(): sipRequest="+sipRequest);
    if(logger!=undefined) logger.debug("DialogFilter:processRequest(): incomingMessageChannel="+incomingMessageChannel);
    if (this.listeningPoint == null) {
        return;
    }
    var sipStack = this.transactionChannel.getSIPStack();
    var sipProvider = this.listeningPoint.getProvider();
    if (sipProvider == null) {
        return;
    }
    var transaction = this.transactionChannel;
    var dialogId = sipRequest.getDialogId(true);
    var dialog = sipStack.getDialog(dialogId);
    if (dialog != null && sipProvider != dialog.getSipProvider()) {
        var contact = dialog.getMyContactHeader();
        if (contact != null) {
            var contactUri = contact.getAddress().getURI();
            var ipAddress = contactUri.getHost();
            var contactPort = contactUri.getPort();
            if (contactPort == -1) {
                contactPort = 5060;
            }
            if (ipAddress != null
                && (ipAddress!=this.listeningPoint.getHostAddress() || contactPort != this.listeningPoint.getPort())) {
                dialog = null;
            }
        }
    }
    if (sipProvider.isAutomaticDialogSupportEnabled()
        && sipProvider.isDialogErrorsAutomaticallyHandled()
        && sipRequest.getToTag() == null) {
        var sipServerTransaction = sipStack.findMergedTransaction(sipRequest);
        if (sipServerTransaction != null) {
            this.sendLoopDetectedResponse(sipRequest, transaction);
            return;
        }
    }
    if (sipRequest.getMethod()=="ACK") {
        if (dialog == null) {
            var ackTransaction = sipStack.findTransactionPendingAck(sipRequest);
            if (ackTransaction != null) {
                ackTransaction.setAckSeen();
                sipStack.removeTransaction(ackTransaction);
                sipStack.removeTransactionPendingAck(ackTransaction);
                return;
            }
        } 
        else {
            if (!dialog.handleAck(transaction)) {
                ackTransaction = sipStack.findTransactionPendingAck(sipRequest);
                if (ackTransaction != null) {
                    ackTransaction.setAckSeen();
                    sipStack.removeTransaction(ackTransaction);
                    sipStack.removeTransactionPendingAck(ackTransaction);
                }
                return;
            } 
            else {
                transaction.passToListener();
                dialog.addTransaction(transaction);
                dialog.addRoute(sipRequest);
                transaction.setDialog(dialog, dialogId);
                if (sipRequest.getMethod()=="INVITE"
                    && sipProvider.isDialogErrorsAutomaticallyHandled()) {
                    sipStack.putInMergeTable(transaction, sipRequest);
                }
                if (sipStack.deliverTerminatedEventForAck) {
                    sipStack.addTransaction(transaction);
                    transaction.scheduleAckRemoval();
                } else {
                    transaction.setMapped(true);
                }
            }
        }
    }
    else if (sipRequest.getMethod()=="BYE") {
        if (dialog != null && !dialog.isRequestConsumable(sipRequest)) {
            if (dialog.getRemoteSeqNumber() >= sipRequest.getCSeq().getSeqNumber()
                && transaction.getState() == "TRYING") {
                this.sendServerInternalErrorResponse(sipRequest, transaction);
            }
            if (transaction != null)
            {
                sipStack.removeTransaction(transaction);
            }
            return;
        } 
        else if (dialog == null && sipProvider.isAutomaticDialogSupportEnabled()) {
            var response = sipRequest.createResponse(481);
            response.setReasonPhrase("Dialog Not Found");
            transaction.sendResponse(response);
            if (transaction != null) {
                sipStack.removeTransaction(transaction);
                transaction = null;
            }
            return;
        }
        if (transaction != null && dialog != null) {
            if (sipProvider == dialog.getSipProvider()) {
                sipStack.addTransaction(transaction);
                dialog.addTransaction(transaction);
                transaction.setDialog(dialog, dialogId);
            }
        }
    } 
    else if (sipRequest.getMethod()=="CANCEL") {
        var st = sipStack.findCancelTransaction(sipRequest, true);
        if (sipRequest.getMethod()=="CANCEL") {
            if (st != null && st.getState() == "TERMINATED") {
                transaction.sendResponse(sipRequest.createResponse("OK"));
                return;
            }
        }
        if (transaction != null && st != null && st.getDialog() != null) {
            transaction.setDialog(st.getDialog(), dialogId);
            dialog = st.getDialog();
        } 
        else if (st == null && sipProvider.isAutomaticDialogSupportEnabled()
            && transaction != null) {
            response = sipRequest.createResponse(481);
            sipProvider.sendResponse(response);
            if (transaction != null) {
                sipStack.removeTransaction(transaction);
            }
            return;
        }
        if (st != null) {
            if (transaction != null) {
                sipStack.addTransaction(transaction);
                transaction.setPassToListener();
                transaction.setInviteTransaction(st);
            }
        }
    }
    else if (sipRequest.getMethod()=="INVITE") {
        var lastTransaction = dialog == null ? null : dialog.getInviteTransaction();
        if (dialog != null && transaction != null && lastTransaction != null
            && sipRequest.getCSeq().getSeqNumber() > dialog.getRemoteSeqNumber()
            && lastTransaction instanceof SIPServerTransaction
            && sipProvider.isDialogErrorsAutomaticallyHandled()
            && dialog.isSequnceNumberValidation()
            && lastTransaction.isInviteTransaction()
            && lastTransaction.getState() != "COMPLETED"
            && lastTransaction.getState() != "TERMINATED"
            && lastTransaction.getState() != "CONFIRMED") {
            this.sendServerInternalErrorResponse(sipRequest, transaction);
            return;
        }
        lastTransaction = (dialog == null ? null : dialog.getLastTransaction());
        if (dialog != null
            && sipProvider.isDialogErrorsAutomaticallyHandled()
            && lastTransaction != null
            && lastTransaction.isInviteTransaction()
            && lastTransaction instanceof SIPClientTransaction
            && lastTransaction.getLastResponse() != null
            && lastTransaction.getLastResponse().getStatusCode() == 200
            && !dialog.isAckSent(lastTransaction.getLastResponse().getCSeq()
                .getSeqNumber())) {
            this.sendRequestPendingResponse(sipRequest, transaction);
            return;
        }
        if (dialog != null && lastTransaction != null
            && sipProvider.isDialogErrorsAutomaticallyHandled()
            && lastTransaction.isInviteTransaction()
            && lastTransaction instanceof ServerTransaction && !dialog.isAckSeen()) {
            this.sendRequestPendingResponse(sipRequest, transaction);
            return;
        }
    }
    if (dialog != null && transaction != null && sipRequest.getMethod()!="BYE"
        && sipRequest.getMethod()!="CANCEL"
        && sipRequest.getMethod()!="ACK") {
        if (!dialog.isRequestConsumable(sipRequest)) {
            if (dialog.getRemoteSeqNumber() >= sipRequest.getCSeq().getSeqNumber()
                && sipProvider.isDialogErrorsAutomaticallyHandled()
                && (transaction.getState() == "TRYING" || transaction.getState() == "PROCEEDING")) {
                this.sendServerInternalErrorResponse(sipRequest, transaction);
            }
            return;
        }
        try {
            if (sipProvider == dialog.getSipProvider()) {
                sipStack.addTransaction(transaction);
                dialog.addTransaction(transaction);
                dialog.addRoute(sipRequest);
                transaction.setDialog(dialog, dialogId);
            }
        } catch (ex) {
            console.error("DialogFilter:processRequest(): catched exception:"+ex);
            sipStack.removeTransaction(transaction);
            return;
        }

    }
    var sipEvent;
    if (transaction != null) {
        sipEvent = new RequestEvent(sipProvider, transaction, dialog, sipRequest);
    } 
    else {
        sipEvent = new RequestEvent(sipProvider, null, dialog, sipRequest);
    }
    sipProvider.handleEvent(sipEvent, transaction);
}

DialogFilter.prototype.getProcessingInfo =function(){
    if(logger!=undefined) logger.debug("DialogFilter:getProcessingInfo()");
    return null;
}
