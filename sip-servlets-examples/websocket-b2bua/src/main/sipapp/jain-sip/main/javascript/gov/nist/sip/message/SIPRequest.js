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
 *  Implementation of the JAIN-SIP SIPRequest .
 *  @see  gov/nist/javax/sip/message/SIPRequest.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function SIPRequest() {
    if(logger!=undefined) logger.debug("SIPRequest:SIPRequest()");
    this.classname="SIPRequest";
    this.serialVersionUID = "3360720013577322927L";
    this.transactionPointer=null;
    this.requestLine = new RequestLine();
    this.messageChannel=null;
    this.inviteTransaction=null;
    this.targetRefreshMethods = new Array();
    this.nameTable = new Array();
    this.unrecognizedHeaders= new Array();
    this.headers=new Array();
    this.attachHeader(new ContentLength(0), false);
    
    this.targetRefreshMethods.push(this.INVITE);
    this.targetRefreshMethods.push(this.UPDATE);
    this.targetRefreshMethods.push(this.SUBSCRIBE);
    this.targetRefreshMethods.push(this.NOTIFY);
    this.targetRefreshMethods.push(this.REFER);
    
    this.putName(this.INVITE);
    this.putName(this.BYE);
    this.putName(this.CANCEL);
    this.putName(this.ACK);
    this.putName(this.PRACK);
    this.putName(this.INFO);
    this.putName(this.MESSAGE);
    this.putName(this.NOTIFY);
    this.putName(this.OPTIONS);
    this.putName(this.PRACK);
    this.putName(this.PUBLISH);
    this.putName(this.REFER);
    this.putName(this.REGISTER);
    this.putName(this.SUBSCRIBE);
    this.putName(this.UPDATE);
    
}

SIPRequest.prototype = new SIPMessage();
SIPRequest.prototype.constructor=SIPRequest;
SIPRequest.prototype.DEFAULT_USER="ip";
SIPRequest.prototype.DEFAULT_TRANSPORT="tcp";
SIPRequest.prototype.INVITE="INVITE";
SIPRequest.prototype.BYE="BYE";
SIPRequest.prototype.CANCEL="CANCEL";
SIPRequest.prototype.ACK="ACK";
SIPRequest.prototype.PRACK="PRACK";
SIPRequest.prototype.INFO="INFO";
SIPRequest.prototype.MESSAGE="MESSAGE";
SIPRequest.prototype.NOTIFY="NOTIFY";
SIPRequest.prototype.OPTIONS="OPTIONS";
SIPRequest.prototype.PUBLISH="PUBLISH";
SIPRequest.prototype.REFER="REFER";
SIPRequest.prototype.REGISTER="REGISTER";
SIPRequest.prototype.SUBSCRIBE="SUBSCRIBE";
SIPRequest.prototype.UPDATE="UPDATE";
SIPRequest.prototype.CSeqHeader="CSeq";
SIPRequest.prototype.ToHeader="To";
SIPRequest.prototype.CallIdHeader="Call-ID";
SIPRequest.prototype.FromHeader="From";
SIPRequest.prototype.ViaHeader="Via";
SIPRequest.prototype.MaxForwardsHeader="Max-Forwards";
SIPRequest.prototype.EventHeader="Event";
SIPRequest.prototype.ContactHeader="Contact";
SIPRequest.prototype.COLON=":";
//SIPRequest.prototype.UPDATE="UPDATE";

SIPRequest.prototype.putName =function(name){
    if(logger!=undefined) logger.debug("SIPRequest:putName():name="+name);
    var array=new Array();
    array[0]=name;
    array[1]=name;
    this.nameTable.push(array);
}

SIPRequest.prototype.isTargetRefresh =function(ucaseMethod){
    if(logger!=undefined) logger.debug("SIPRequest:isTargetRefresh():ucaseMethod="+ucaseMethod);
    var x=null;
    for(var i=0;i<this.targetRefreshMethods;i++)
    {
        if(this.targetRefreshMethods[i]==ucaseMethod)
        {
            x=1;
        }
    }
    if(x!=null)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPRequest.prototype.isDialogCreating =function(ucaseMethod){
    if(logger!=undefined) logger.debug("SIPRequest:isDialogCreating():ucaseMethod:"+ucaseMethod);
    var siptranstack=new SIPTransactionStack();
    return siptranstack.isDialogCreated(ucaseMethod);
}

SIPRequest.prototype.getCannonicalName =function(method){
    if(logger!=undefined) logger.debug("SIPRequest:getCannonicalName():method:"+method);
    var x=null;
    for(var i=0;i<this.nameTable;i++)
    {
        if(this.nameTable[i][0]==method)
        {
            x=i;
        }
    }
    if(x!=null)
    {
        return this.nameTable[x][1];
    }
    else
    {
        return method;
    }
}

SIPRequest.prototype.getRequestLine =function(){
    if(logger!=undefined) logger.debug("SIPRequest:getRequestLine()");
    return this.requestLine;
}

SIPRequest.prototype.setRequestLine =function(requestLine){
    if(logger!=undefined) logger.debug("SIPRequest:setRequestLine():requestLine="+requestLine.classname);
    this.requestLine = requestLine;
}

SIPRequest.prototype.checkHeaders =function(){
    if(logger!=undefined) logger.debug("SIPRequest:checkHeaders()");
    var prefix = "Missing a required header : ";

    if (this.getCSeq() == null) {
        console.error("SIPRequest:checkHeaders(): "+prefix + this.CSeqHeader);
        throw "SIPRequest:checkHeaders(): "+prefix + this.CSeqHeader;
    }
    if (this.getTo() == null) {
        console.error("SIPRequest:checkHeaders(): "+prefix + this.ToHeader);
        throw "SIPRequest:checkHeaders(): "+prefix + this.ToHeader;
    }
    if (this.callIdHeader == null || this.callIdHeader.getCallId() == null
        || this.callIdHeader.getCallId()=="") {
        console.error("SIPRequest:checkHeaders(): "+prefix + this.CallIdHeader);
        throw "SIPRequest:checkHeaders(): "+prefix + this.CallIdHeader;
    }
    if (this.getFrom() == null) {
        console.error("SIPRequest:checkHeaders(): "+prefix + this.FromHeader);
        throw "SIPRequest:checkHeaders(): "+prefix + this.FromHeader;
    }
    if (this.getViaHeaders() == null) {
        console.error("SIPRequest:checkHeaders(): "+prefix + this.ViaHeader);
        throw "SIPRequest:checkHeaders(): "+prefix + this.ViaHeader;
    }
    if (this.getMaxForwards() == null) {
        console.error("SIPRequest:checkHeaders(): "+prefix + this.MaxForwardsHeader);
        throw "SIPRequest:checkHeaders(): "+prefix + this.MaxForwardsHeader;
    }
    if (this.getTopmostVia() == null){
        console.error("SIPRequest:checkHeaders(): no via header in request!");
        throw "SIPRequest:checkHeaders():  no via header in request!";
    }
    
    if (this.getMethod()==this.NOTIFY) {
        /*if (getHeader(SubscriptionStateHeader.NAME) == null){
            console.error(prefix + SubscriptionStateHeader.NAME, 0);
        }*/
        if (getHeader(this.EventHeader) == null){
            console.error("SIPRequest:checkHeaders(): "+prefix + this.EventHeader);
            throw "SIPRequest:checkHeaders(): "+prefix + this.EventHeader;
        }
    } else if (this.getMethod()==this.PUBLISH) {
        if (this.getHeader(this.EventHeader) == null)
            console.error("SIPRequest:checkHeaders(): "+prefix + this.EventHeader);
            throw "SIPRequest:checkHeaders(): "+prefix + this.EventHeader;
    }
    if (this.requestLine.getMethod()==this.INVITE
        || this.requestLine.getMethod()==this.SUBSCRIBE
        || this.requestLine.getMethod()==this.REFER) {
        if (this.getContactHeader() == null) {
            // Make sure this is not a target refresh. If this is a target
            // refresh its ok not to have a contact header. Otherwise
            // contact header is mandatory.
            if (this.getToTag() == null){
                console.error("SIPRequest:checkHeaders(): "+prefix + this.ContactHeader);
                throw "SIPRequest:checkHeaders(): "+prefix + this.ContactHeader;
            }
        }
        if (this.requestLine.getUri() instanceof SipUri) {
            var scheme = (this.requestLine.getUri()).getScheme();
            if ("sips"==scheme.toLowerCase()) {
                var sipUri = this.getContactHeader().getAddress().getURI();
                if (sipUri.getScheme()!="sips") {
                    console.error("SIPRequest:checkHeaders(): scheme for contact should be sips:" + sipUri);
                    throw "SIPRequest:checkHeaders(): scheme for contact should be sips:" + sipUri;
                }
            }
        }
    }
    
    /*
         * Contact header is mandatory for a SIP INVITE request.
         */
    if (this.getContactHeader() == null&& (this.getMethod()==this.INVITE
        || this.getMethod()==this.REFER || this.getMethod()==this.SUBSCRIBE)) {
        console.error("SIPRequest:checkHeaders(): Contact Header is Mandatory for a SIP INVITE");
        throw "SIPRequest:checkHeaders(): Scheme for contact should be sips:" + sipUri;
    }

    if (this.requestLine != null && this.requestLine.getMethod() != null
        && this.getCSeq().getMethod() != null
        && this.requestLine.getMethod()!= this.getCSeq().getMethod()) {
        console.error("SIPRequest:checkHeaders(): CSEQ method mismatch with  Request-Line");
        throw "SIPRequest:checkHeaders(): CSEQ method mismatch with  Request-Line";
    }
}


SIPRequest.prototype.setDefaults =function(){
    if(logger!=undefined) logger.debug("SIPRequest:setDefaults()");
    if (this.requestLine == null)
    {
        return;
    }
    var method = this.requestLine.getMethod();
    if (method == null)
    {
        return;
    }
    var u = this.requestLine.getUri();
    if (u == null)
    {
        return;
    }
    if (method == this.REGISTER || method == this.INVITE) {
        if (u instanceof SipUri) {
            var sipUri = u;
            sipUri.setUserParam(this.DEFAULT_USER);
            sipUri.setTransportParam(this.DEFAULT_TRANSPORT);
        }
    }
}

SIPRequest.prototype.setRequestLineDefaults =function(){
    if(logger!=undefined) logger.debug("SIPRequest:setRequestLineDefaults()");
    var method = this.requestLine.getMethod();
    if (method == null) {
        var cseq =  this.getCSeq();
        if (cseq != null) {
            method = this.getCannonicalName(cseq.getMethod());
            this.requestLine.setMethod(method);
        }
    }
}

SIPRequest.prototype.getRequestURI =function(){
    if(logger!=undefined) logger.debug("SIPRequest:getRequestURI()");
    if (this.requestLine == null)
    {
        return null;
    }
    else
    {
        return  this.requestLine.getUri();
    }
}

SIPRequest.prototype.setRequestURI =function(uri){
    if(logger!=undefined) logger.debug("SIPRequest:setRequestURI():uri="+uri.encode());
    if ( uri == null ) {
        console.error("SIPRequest:setRequestURI(): null request URI");
        throw "SIPRequest:setRequestURI(): null request URI";
    }
    if (this.requestLine == null) {
        this.requestLine = new RequestLine();
    }
    this.requestLine.setUri(uri);
    this.nullRequest = false;
}

SIPRequest.prototype.setMethod =function(method){
    if(logger!=undefined) logger.debug("SIPRequest:setMethod():method="+method);
    if (method == null)
    {
        console.error("SIPRequest:setMethod(): null method");
        throw "SIPRequest:setMethod(): null method";
    }
    if (this.requestLine == null) {
        this.requestLine = new RequestLine();
    }
    var meth = this.getCannonicalName(method);
    this.requestLine.setMethod(meth);

    if (this.cSeqHeader != null) {
        this.cSeqHeader.setMethod(meth);
    }
}

SIPRequest.prototype.getMethod =function(){
    if(logger!=undefined) logger.debug("SIPRequest:getMethod()");
    if (this.requestLine == null)
    {
        return null;
    }
    else
    {
        return this.requestLine.getMethod();
    }
}

SIPRequest.prototype.encode =function(){
    if(logger!=undefined) logger.debug("SIPRequest:encode()");
    var retval=null;
    if (this.requestLine != null) {
        this.setRequestLineDefaults();
        retval = this.requestLine.encode() + this.superencode();
    } else if (this.isNullRequest()) {
        retval = "\r\n\r\n";
    } else {       
        retval = this.superencode();
    }
    return retval;
}

SIPRequest.prototype.encodeMessage =function(){
    if(logger!=undefined) logger.debug("SIPRequest:encodeMessage()");
    var retval;
    if (this.requestLine != null) 
    {
        this.setRequestLineDefaults();
        retval = this.requestLine.encode() + this.encodeSIPHeaders();
    } 
    else if (this.isNullRequest()) 
    {
        retval = "\r\n\r\n";
    } 
    else
    {
        retval = this.encodeSIPHeaders();
    }
    return retval;
}

SIPRequest.prototype.toString =function(){
    if(logger!=undefined) logger.debug("SIPRequest:toString()");
    return this.encode();
}

SIPRequest.prototype.superencode =function(){
    if(logger!=undefined) logger.debug("SIPRequest:superencode()");
    var encoding = "";
    for(var i=0;i<this.headers.length;i++)
    {
        var siphdr = this.headers[i];
        if (!(siphdr instanceof ContentLength))
        {
            encoding=encoding+siphdr.encode();
        }
    }
    for(i=0;i<this.unrecognizedHeaders.length;i++)
    {
        var unrecognized=this.unrecognizedHeaders[i];
        encoding=encoding+unrecognized+this.NEWLINE;
    }
    encoding=encoding+this.contentLengthHeader.encode()+this.NEWLINE;
    if (this.messageContentObject != null) 
    {
        var mbody = this.getContent().toString();
        encoding=encoding+mbody;
    }
    else if (this.messageContent != null || this.messageContentBytes != null) {
        var content = "";
        if (this.messageContent != null)
        {
            content = this.messageContent;
        }
        else 
        {
            for(i=0;i<this.messageContentBytes.length;i++)
            {
                content=content+this.messageContentBytes[i];
            }
        }
        encoding=encoding+content;
    }
    return encoding.toString();
}

SIPRequest.prototype.getMessageAsEncodedStrings =function(){
    if(logger!=undefined) logger.debug("SIPRequest:getMessageAsEncodedStrings()");
    var retval = Object.getPrototypeOf(this).getMessageAsEncodedStrings();
    if (this.requestLine != null) {
        this.setRequestLineDefaults();
        retval.addFirst(this.requestLine.encode());
    }
    return retval;
}

SIPRequest.prototype.getDialogId =function(){
    if(logger!=undefined) logger.debug("SIPRequest:getDialogId()");
    if(arguments.length==1)
    {
        var isServer=arguments[0];
        return this.getDialogIdargu1(isServer);
    }
    else if(arguments.length==2)
    {
        isServer=arguments[0];
        var toTag=arguments[1];
        return this.getDialogIdargu2(isServer, toTag);
    }
}

SIPRequest.prototype.getDialogIdargu1 =function(isServer){
    if(logger!=undefined) logger.debug("SIPRequest:getDialogId():isServer="+isServer);
    var cid = this.getCallId();
    var retval = cid.getCallId();
    var from = this.getFrom();
    var to = this.getTo();
    if (!isServer) {
        if (from.getTag() != null) {
            retval=retval+this.COLON+from.getTag();
        }
        if (to.getTag() != null) {
            retval=retval+this.COLON+to.getTag();
        }
    } 
    else {
        if (to.getTag() != null) {
            retval=retval+this.COLON+to.getTag();
        }
        if (from.getTag() != null) {
            retval=retval+this.COLON+from.getTag();
        }
    }
    return retval.toString().toLowerCase();
}

SIPRequest.prototype.getDialogIdargu2 =function(isServer, toTag){
    if(logger!=undefined) logger.debug("SIPRequest:getDialogIdargu2():isServer="+isServer+", toTag="+toTag);
    var from =  this.getFrom();
    var cid =  this.getCallId();
    var retval = cid.getCallId();
    if (!isServer) {
        if (from.getTag() != null) {
            retval=retval+this.COLON+from.getTag();
        }
        if (toTag != null) {
            retval=retval+this.COLON+toTag;
        }
    } else {
        if (toTag != null) {
            retval=retval+this.COLON+toTag;
        }
        if (from.getTag() != null) {
            retval=retval+this.COLON+from.getTag();
        }
    }
    return retval.toString().toLowerCase();
}

SIPRequest.prototype.createResponse =function(){
    if(logger!=undefined) logger.debug("SIPRequest:createResponse()");
    if(arguments.length==1)
    {
        var statusCode=arguments[0];
        return this.createResponseargu1(statusCode);
    }
    else if(arguments.length==2)
    {
        statusCode=arguments[0];
        var reasonPhrase=arguments[1];
        return this.createResponseargu2(statusCode, reasonPhrase);
    }
    else if(arguments.length==3)
    {
        statusCode=arguments[0];
        reasonPhrase=arguments[1];
        var requestsent=arguments[2];
        var response=this.createResponseargu2(statusCode, reasonPhrase);
        return this.createResponseargu3(response,requestsent);
    }
}

SIPRequest.prototype.createResponseargu1 =function(statusCode){
    if(logger!=undefined) logger.debug("SIPRequest:createResponseargu1():statusCode="+statusCode);
    var sipresponse=new SIPResponse();
    var reasonPhrase = sipresponse.getReasonPhrase(statusCode);
    return this.createResponseargu2(statusCode, reasonPhrase);
}

SIPRequest.prototype.createResponseargu2 =function(statusCode,reasonPhrase){
    if(logger!=undefined) logger.debug("SIPRequest:createResponseargu2():statusCode="+statusCode+", reasonPhrase"+reasonPhrase);
    var newResponse= new SIPResponse();
    var headerIterator=null;
    var nextHeader=null;
    try {
        newResponse.setStatusCode(statusCode);
    } catch (ex) {
        console.error("SIPRequest:createResponseargu2(): bad code " + statusCode);
        throw "SIPRequest:createResponseargu2(): bad code " + statusCode;
    }
    if (reasonPhrase != null)
    {
        newResponse.setReasonPhrase(reasonPhrase);
    }
    else
    {
        var sipresponse=new SIPResponse();
        newResponse.setReasonPhrase(sipresponse.getReasonPhrase(statusCode));
    }
    headerIterator = this.getHeaders();
    for(var i=0;i<headerIterator.length;i++)
    {
        nextHeader = headerIterator[i];
        if(nextHeader instanceof To)
        {
            if(!nextHeader.hasTag())
            {
                var date=new Date();
                var tag=date.getTime();
                nextHeader.setTag(tag);
            }
        }
        if(nextHeader instanceof ViaList)
        {
            newResponse.addViaHeaderList(nextHeader);
        }
        else if(!(nextHeader instanceof ContactList) && !(nextHeader instanceof UserAgent))
        {
            if(newResponse.getStatusCode()==200)
            {
                newResponse.attachHeader(nextHeader, false);
            }
            else if(!(nextHeader instanceof ContentLength)&&!(nextHeader instanceof ContentType))
            {
                newResponse.attachHeader(nextHeader, false);
            }
        }
    }
    if(this.getMessageContent()!=null&&newResponse.getStatusCode()==200)
    {
        newResponse.setMessageContent(this.getMessageContent());
    }
    var mfimpl=new MessageFactoryImpl();
    if (mfimpl.getDefaultServerHeader() != null) {
        newResponse.setHeader(mfimpl.getDefaultServerHeader());
    }
    if (newResponse.getStatusCode() == 100) {
        newResponse.getTo().removeParameter("tag");
    }
    if(newResponse.getStatusCode()==480&&newResponse.getHeaders("recordroute")!=null)
    {
        newResponse.removeHeader("record-route");
    }
    var server = mfimpl.getDefaultServerHeader();
    if (server != null) {
        newResponse.setHeader(server);
    }
    return newResponse;
}

SIPRequest.prototype.createResponseargu3 =function(response,requestsent){
    if(logger!=undefined) logger.debug("SIPRequest:createResponseargu3():response="+response+", requestsent=" +requestsent);
    response.addContactUseragent(requestsent);
    return response;
}

SIPRequest.prototype.mustCopyRR =function(code){
    if(logger!=undefined) logger.debug("SIPRequest:mustCopyRR():code="+code);
    if ( code>100 && code<300 ) {
        return this.isDialogCreating( this.getMethod() ) && this.getToTag() == null;
    } 
    else {
        return false;
    }
}

SIPRequest.prototype.createCancelRequest =function(){
    if(logger!=undefined) logger.debug("SIPRequest:createCancelRequest()");
    if (this.getMethod()!=this.INVITE)
    {
        console.error("SIPRequest:createCancelRequest(): Attempt to create CANCEL for " + this.getMethod());
        throw "SIPRequest:createCancelRequest(): Attempt to create CANCEL for " + this.getMethod();
    }
    var cancel = new SIPRequest();
    cancel.setRequestLine( this.requestLine);
    cancel.setMethod(this.CANCEL);
    cancel.setHeader(this.callIdHeader);
    cancel.setHeader(this.toHeader);
    cancel.setHeader(this.cSeqHeader);
    cancel.getCSeq().setMethod(this.CANCEL);
    cancel.setHeader( this.fromHeader);
    cancel.addFirst( this.getTopmostVia());
    cancel.setHeader( this.maxForwardsHeader);
    if (this.getRouteHeaders() != null) {
        cancel.setHeader(this.getRouteHeaders());
    }
    var mfimpl=new MessageFactoryImpl();
    if (mfimpl.getDefaultUserAgentHeader() != null) {
        cancel.setHeader(mfimpl.getDefaultUserAgentHeader());
    }
    return cancel;
}

SIPRequest.prototype.createAckRequest =function()
{
    if(arguments.length==1)
    {
        var responseToHeader=arguments[0];
        return this.createAckRequest_argu1(responseToHeader);
    }
    else
    {
        return this.createACKRequest_argu0();
    }
}
SIPRequest.prototype.createAckRequest_argu1 =function(responseToHeader){
    if(logger!=undefined) logger.debug("SIPRequest:createAckRequest_argu1():responseToHeader="+responseToHeader.classname);
    var newRequest = new SIPRequest();
    var nextHeader =null;
    newRequest.setRequestLine(this.requestLine);
    newRequest.setMethod(this.ACK);
    var headerIterator = this.getHeaders();
    for(var i=0;i<headerIterator.length;i++)
    {
        nextHeader=headerIterator[i];
        if (nextHeader instanceof RouteList) {
            continue;
        } else if (nextHeader instanceof ProxyAuthorization) {
            continue;
        } else if (nextHeader instanceof ContentLength) {
            // Adding content is responsibility of user.
            nextHeader.setContentLength(0);
        } else if (nextHeader instanceof ContentType) {
            continue;
        }else if (nextHeader instanceof CSeq) {
            // The CSeq header field in the
            // ACK MUST contain the same value for the
            // sequence number as was present in the
            // original request, but the method parameter
            // MUST be equal to "ACK".
            var cseq = nextHeader;
            cseq.setMethod(this.ACK);
            nextHeader = cseq;
        } else if (nextHeader instanceof To) {
            if (responseToHeader != null) {
                nextHeader = responseToHeader;
            } else {
                nextHeader = nextHeader;
            }
        } else if (nextHeader instanceof ContactList || nextHeader instanceof Expires) {
            continue;
        } else if (nextHeader instanceof ViaList) {
            nextHeader =  nextHeader.getFirst();
        } else {
            nextHeader = nextHeader;
        }
        newRequest.attachHeader(nextHeader, false);
    }
    var mfimpl=new MessageFactoryImpl();
    if (mfimpl.getDefaultUserAgentHeader() != null) {
        newRequest.setHeader(mfimpl.getDefaultUserAgentHeader());
    }
    return newRequest;
}
SIPRequest.prototype.createErrorAck =function(responseToHeader){
    if(logger!=undefined) logger.debug("SIPRequest:createErrorAck():responseToHeader="+responseToHeader.classname);
    var newRequest = new SIPRequest();
    var newrequestline=new RequestLine();
    newrequestline.setMethod(this.ACK);
    newrequestline.setUri(this.requestLine.getUri());
    var newCSeq=new CSeq();
    newCSeq.setMethod(this.ACK);
    newCSeq.setSeqNumber(this.cSeqHeader.getSequenceNumber());
    newRequest.setRequestLine(newrequestline);
    newRequest.setMethod(this.ACK);
    newRequest.setHeader(this.callIdHeader);
    newRequest.setHeader(this.maxForwardsHeader);
    newRequest.setHeader(this.fromHeader);
    newRequest.setHeader(responseToHeader);
    newRequest.addFirst(this.getTopmostVia());
    newRequest.setHeader(newCSeq);
    if (this.getRouteHeaders() != null) {
        newRequest.setHeader(this.getRouteHeaders());
    }
    var mfimpl=new MessageFactoryImpl();
    if (mfimpl.getDefaultUserAgentHeader() != null) {
        newRequest.setHeader(mfimpl.getDefaultUserAgentHeader());
    }
    return newRequest;
}
SIPRequest.prototype.createSIPRequest =function(requestLine, switchHeaders){
    if(logger!=undefined) logger.debug("SIPRequest:createSIPRequest():requestLine="+requestLine.classname+", switchHeaders="+switchHeaders);
    var newRequest = new SIPRequest();
    newRequest.requestLine = requestLine;
    var headerIterator = this.getHeaders();
    var f=null;
    var t=null;
    for(var i=0;i<headerIterator.length;i++)
    {
        var nextHeader = headerIterator[i];
        if (nextHeader instanceof CSeq) {
            var newCseq = nextHeader;
            nextHeader = newCseq;
            newCseq.setMethod(requestLine.getMethod());
        } else if (nextHeader.classname=="ViaList") {
            var via = nextHeader.getFirst();
            //via.removeParameter("branch");
            nextHeader = via;
        } else if (nextHeader instanceof To) {
            var to =  nextHeader;
            if (switchHeaders) {
                nextHeader = new From(to);
            //nextHeader.removeTag();
            } else {
                nextHeader = to;
            //nextHeader.removeTag();
            }
        } else if (nextHeader instanceof From) {
            var from =  nextHeader;
            if (switchHeaders) {
                nextHeader = new To(from);
            //nextHeader.removeTag();
            } else {
                nextHeader = from;
            //nextHeader.removeTag();
            }
        } else if (nextHeader instanceof ContentLength) {
            var cl = nextHeader;
            cl.setContentLength(0);
            nextHeader = cl;
        }
        else if (!(nextHeader instanceof CallID) && !(nextHeader instanceof MaxForwards)
            && !(nextHeader instanceof Expires)&& !(nextHeader instanceof UserAgent)
            &&!(nextHeader instanceof AllowList)&&!(nextHeader instanceof ContactList)) {
            continue;
        }
        newRequest.attachHeader(nextHeader, false);
    }
    var mfimpl=new MessageFactoryImpl();
    if (mfimpl.getDefaultUserAgentHeader() != null) {
        newRequest.setHeader(mfimpl.getDefaultUserAgentHeader());
    }
    return newRequest;
}


SIPRequest.prototype.createBYERequest =function(switchHeaders){
    if(logger!=undefined) logger.debug("SIPRequest:createBYERequest():switchHeaders="+switchHeaders);
    var requestLine =  this.requestLine;
    this.requestLine.setMethod("BYE");
    return this.createSIPRequest(requestLine, switchHeaders);
}

SIPRequest.prototype.createACKRequest_argu0 =function(){
    if(logger!=undefined) logger.debug("SIPRequest:createACKRequest_argu0()");
    var requestLine =  this.requestLine;
    this.requestLine.setMethod(this.ACK);
    return this.createSIPRequest(requestLine, false);
}

SIPRequest.prototype.getViaHost =function(){
    if(logger!=undefined) logger.debug("SIPRequest:getViaHost()");
    var via = this.getViaHeaders().getFirst();
    return via.getHost();
}

SIPRequest.prototype.getViaPort =function(){
    if(logger!=undefined) logger.debug("SIPRequest:getViaPort()");
    var via = this.getViaHeaders().getFirst();
    if (via.hasPort())
    {
        return via.getPort();
    }
    else
    {
        return 5060;
    }
}

SIPRequest.prototype.getFirstLine =function(){
    if(logger!=undefined) logger.debug("SIPRequest:getFirstLine()");
    if (this.requestLine == null)
    {
        return null;
    }
    else
    {
        return this.requestLine.encode();
    }
}

SIPRequest.prototype.setSIPVersion =function(sipVersion){
    if(logger!=undefined) logger.debug("SIPRequest:setSIPVersion():sipVersion="+sipVersion);
    if (sipVersion == null || (sipVersion.toLowerCase()!=("SIP/2.0").toLowerCase()))
    {
        console.error("SIPRequest:setSIPVersion(): bad sipVersion", 0);
        throw "SIPRequest:setSIPVersion(): bad sipVersion";
    }
    this.requestLine.setSipVersion(sipVersion);
}

SIPRequest.prototype.getSIPVersion =function(){
    if(logger!=undefined) logger.debug("SIPRequest:getSIPVersion()");
    return this.requestLine.getSipVersion();
}

SIPRequest.prototype.getTransaction =function(){
    if(logger!=undefined) logger.debug("SIPRequest:getTransaction()");
    return this.transactionPointer;
}

SIPRequest.prototype.setTransaction =function(transaction){
    if(logger!=undefined) logger.debug("SIPRequest:setTransaction():transaction:"+transaction);
    this.transactionPointer = transaction;
}

SIPRequest.prototype.getMessageChannel =function(){
    if(logger!=undefined) logger.debug("SIPRequest:getMessageChannel()");
    return this.messageChannel;
}

SIPRequest.prototype.setMessageChannel =function(messageChannel){
    if(logger!=undefined) logger.debug("SIPRequest:setMessageChannel():messageChannel:"+messageChannel.classname);
    this.messageChannel=messageChannel;
}

SIPRequest.prototype.getMergeId =function(){
    if(logger!=undefined) logger.debug("SIPRequest:getMergeId()");
    /*
         * generate an identifier from the From tag, call-ID, and CSeq
         */
    var fromTag = this.getFromTag();
    var cseq = this.cSeqHeader.toString();
    var callId = this.callIdHeader.getCallId();
    /* NOTE : The RFC does NOT specify you need to include a Request URI 
         * This is added here for the case of Back to Back User Agents.
         */
    var requestUri = this.getRequestURI().toString();

    if (fromTag != null) {
        var buffer="";
        buffer=(buffer+requestUri+":"+fromTag+":"+cseq+":"+callId).toString();
        return buffer;
    } 
    else
    {
        return null;
    }
}

SIPRequest.prototype.setInviteTransaction =function(inviteTransaction){
    if(logger!=undefined) logger.debug("SIPRequest:setInviteTransaction():inviteTransaction="+inviteTransaction.classname);
    this.inviteTransaction=inviteTransaction;
}

SIPRequest.prototype.getInviteTransaction =function(){
    if(logger!=undefined) logger.debug("SIPRequest:getInviteTransaction()");
    return this.inviteTransaction;
}

SIPRequest.prototype.fortest =function(){
    if(logger!=undefined) logger.debug("SIPRequest:fortest()");
    var retval = this.requestLine.encode();
    for(var i=0;i<this.headers.length;i++)
    {
        var siphdr = this.headers[i];
        if (!(siphdr instanceof ContentLength))
        {
            var headertemp=siphdr.encode();
           
            retval=retval+headertemp;
        }
    }
    return retval;
}
