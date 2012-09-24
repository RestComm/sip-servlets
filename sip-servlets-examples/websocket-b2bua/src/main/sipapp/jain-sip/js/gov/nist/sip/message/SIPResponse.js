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
 *  Implementation of the JAIN-SIP SIPResponse .
 *  @see  gov/nist/javax/sip/message/SIPResponse.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function SIPResponse() {
    if(logger!=undefined) logger.debug("SIPResponse:SIPResponse()");
    this.classname="SIPResponse";
    this.statusLine=new StatusLine();
    this.nameTable = new Array();
    this.unrecognizedHeaders= new Array();
    this.headers=new Array();
    this.attachHeader(new ContentLength(0), false);
}

SIPResponse.prototype = new SIPMessage();
SIPResponse.prototype.constructor=SIPResponse;
SIPResponse.prototype.TRYING=100;
SIPResponse.prototype.RINGING=180;
SIPResponse.prototype.CALL_IS_BEING_FORWARDED=181;
SIPResponse.prototype.QUEUED=182;
SIPResponse.prototype.SESSION_PROGRESS=183;
SIPResponse.prototype.OK=200;
SIPResponse.prototype.ACCEPTED=202;
SIPResponse.prototype.MULTIPLE_CHOICES=300;
SIPResponse.prototype.MOVED_PERMANENTLY=301;
SIPResponse.prototype.MOVED_TEMPORARILY=302;
SIPResponse.prototype.USE_PROXY=305;
SIPResponse.prototype.ALTERNATIVE_SERVICE=380;
SIPResponse.prototype.BAD_REQUEST=400;
SIPResponse.prototype.UNAUTHORIZED=401;
SIPResponse.prototype.PAYMENT_REQUIRED=402;
SIPResponse.prototype.FORBIDDEN=403;
SIPResponse.prototype.NOT_FOUND=404;
SIPResponse.prototype.METHOD_NOT_ALLOWED=405;
SIPResponse.prototype.NOT_ACCEPTABLE=406;
SIPResponse.prototype.PROXY_AUTHENTICATION_REQUIRED=407;
SIPResponse.prototype.REQUEST_TIMEOUT=408;
SIPResponse.prototype.GONE=410;
SIPResponse.prototype.TEMPORARILY_UNAVAILABLE=480;
SIPResponse.prototype.REQUEST_ENTITY_TOO_LARGE=413;
SIPResponse.prototype.REQUEST_URI_TOO_LONG=414;
SIPResponse.prototype.UNSUPPORTED_MEDIA_TYPE=415;
SIPResponse.prototype.UNSUPPORTED_URI_SCHEME=416;
SIPResponse.prototype.BAD_EXTENSION=420;
SIPResponse.prototype.EXTENSION_REQUIRED=421;
SIPResponse.prototype.INTERVAL_TOO_BRIEF=423;
SIPResponse.prototype.CALL_OR_TRANSACTION_DOES_NOT_EXIST=481;
SIPResponse.prototype.LOOP_DETECTED=482;
SIPResponse.prototype.TOO_MANY_HOPS=483;
SIPResponse.prototype.ADDRESS_INCOMPLETE=484;
SIPResponse.prototype.AMBIGUOUS=485;
SIPResponse.prototype.BUSY_HERE=486;
SIPResponse.prototype.REQUEST_TERMINATED=487;
SIPResponse.prototype.NOT_ACCEPTABLE_HERE=488;
SIPResponse.prototype.BAD_EVENT=489;
SIPResponse.prototype.REQUEST_PENDING=491;
SIPResponse.prototype.SERVER_INTERNAL_ERROR=500;
SIPResponse.prototype.UNDECIPHERABLE=493;
SIPResponse.prototype.NOT_IMPLEMENTED=501;
SIPResponse.prototype.BAD_GATEWAY=502;
SIPResponse.prototype.SERVICE_UNAVAILABLE=503;
SIPResponse.prototype.SERVER_TIMEOUT=504;
SIPResponse.prototype.VERSION_NOT_SUPPORTED=505;
SIPResponse.prototype.MESSAGE_TOO_LARGE=513;
SIPResponse.prototype.BUSY_EVERYWHERE=600;
SIPResponse.prototype.DECLINE=603;
SIPResponse.prototype.DOES_NOT_EXIST_ANYWHERE=604;
SIPResponse.prototype.SESSION_NOT_ACCEPTABLE=606;
SIPResponse.prototype.CONDITIONAL_REQUEST_FAILED=412;
SIPResponse.prototype.CSeq="CSeq";
SIPResponse.prototype.To="To";
SIPResponse.prototype.From="From";
SIPResponse.prototype.Via="Via";
SIPResponse.prototype.CallID="Call-ID";
SIPResponse.prototype.COLON=":";
SIPResponse.prototype.CANCEL="CANCEL";
SIPResponse.prototype.ACK="ACK";


SIPResponse.prototype.getReasonPhrase=function()
{
    if(arguments.length==1)
    {
        var rc=arguments[0];
        return this.getReasonPhrase_argu1(rc);
    }
    else
    {
        return this.getReasonPhrase_argu0();
    }
}

SIPResponse.prototype.getReasonPhrase_argu1 =function(rc){
    if(logger!=undefined) logger.debug("SIPResponse:getReasonPhrase_argu1():rc="+rc);
    var retval = null;
    switch (rc) {
        case this.TRYING :
            retval = "Trying";
            break;

        case this.RINGING :
            retval = "Ringing";
            break;

        case this.CALL_IS_BEING_FORWARDED :
            retval = "Call is being forwarded";
            break;

        case this.QUEUED :
            retval = "Queued";
            break;

        case this.SESSION_PROGRESS :
            retval = "Session progress";
            break;

        case this.OK :
            retval = "OK";
            break;

        case this.ACCEPTED :
            retval = "Accepted";
            break;

        case this.MULTIPLE_CHOICES :
            retval = "Multiple choices";
            break;

        case this.MOVED_PERMANENTLY :
            retval = "Moved permanently";
            break;

        case this.MOVED_TEMPORARILY :
            retval = "Moved Temporarily";
            break;

        case this.USE_PROXY :
            retval = "Use proxy";
            break;

        case this.ALTERNATIVE_SERVICE :
            retval = "Alternative service";
            break;

        case this.BAD_REQUEST :
            retval = "Bad request";
            break;

        case this.UNAUTHORIZED :
            retval = "Unauthorized";
            break;

        case this.PAYMENT_REQUIRED :
            retval = "Payment required";
            break;

        case this.FORBIDDEN :
            retval = "Forbidden";
            break;

        case this.NOT_FOUND :
            retval = "Not found";
            break;

        case this.METHOD_NOT_ALLOWED :
            retval = "Method not allowed";
            break;

        case this.NOT_ACCEPTABLE :
            retval = "Not acceptable";
            break;

        case this.PROXY_AUTHENTICATION_REQUIRED :
            retval = "Proxy Authentication required";
            break;

        case this.REQUEST_TIMEOUT :
            retval = "Request timeout";
            break;

        case this.GONE :
            retval = "Gone";
            break;

        case this.TEMPORARILY_UNAVAILABLE :
            retval = "Temporarily Unavailable";
            break;

        case this.REQUEST_ENTITY_TOO_LARGE :
            retval = "Request entity too large";
            break;

        case this.REQUEST_URI_TOO_LONG :
            retval = "Request-URI too large";
            break;

        case this.UNSUPPORTED_MEDIA_TYPE :
            retval = "Unsupported media type";
            break;

        case this.UNSUPPORTED_URI_SCHEME :
            retval = "Unsupported URI Scheme";
            break;

        case this.BAD_EXTENSION :
            retval = "Bad extension";
            break;

        case this.EXTENSION_REQUIRED :
            retval = "Etension Required";
            break;

        case this.INTERVAL_TOO_BRIEF :
            retval = "Interval too brief";
            break;

        case this.CALL_OR_TRANSACTION_DOES_NOT_EXIST :
            retval = "Call leg/Transaction does not exist";
            break;

        case this.LOOP_DETECTED :
            retval = "Loop detected";
            break;

        case this.TOO_MANY_HOPS :
            retval = "Too many hops";
            break;

        case this.ADDRESS_INCOMPLETE :
            retval = "Address incomplete";
            break;

        case this.AMBIGUOUS :
            retval = "Ambiguous";
            break;

        case this.BUSY_HERE :
            retval = "Busy here";
            break;

        case this.REQUEST_TERMINATED :
            retval = "Request Terminated";
            break;

        //Issue 168, Typo fix reported by fre on the retval
        case this.NOT_ACCEPTABLE_HERE :
            retval = "Not Acceptable here";
            break;

        case this.BAD_EVENT :
            retval = "Bad Event";
            break;

        case this.REQUEST_PENDING :
            retval = "Request Pending";
            break;

        case this.SERVER_INTERNAL_ERROR :
            retval = "Server Internal Error";
            break;

        case this.UNDECIPHERABLE :
            retval = "Undecipherable";
            break;

        case this.NOT_IMPLEMENTED :
            retval = "Not implemented";
            break;

        case this.BAD_GATEWAY :
            retval = "Bad gateway";
            break;

        case this.SERVICE_UNAVAILABLE :
            retval = "Service unavailable";
            break;

        case this.SERVER_TIMEOUT :
            retval = "Gateway timeout";
            break;

        case this.VERSION_NOT_SUPPORTED :
            retval = "SIP version not supported";
            break;

        case this.MESSAGE_TOO_LARGE :
            retval = "Message Too Large";
            break;

        case this.BUSY_EVERYWHERE :
            retval = "Busy everywhere";
            break;

        case this.DECLINE :
            retval = "Decline";
            break;

        case this.DOES_NOT_EXIST_ANYWHERE :
            retval = "Does not exist anywhere";
            break;

        case this.SESSION_NOT_ACCEPTABLE :
            retval = "Session Not acceptable";
            break;

        case this.CONDITIONAL_REQUEST_FAILED:
            retval = "Conditional request failed";
            break;

        default :
            retval = "Unknown Status";

    }
    return retval;
}

SIPResponse.prototype.setStatusCode =function(statusCode){
    if(logger!=undefined) logger.debug("SIPResponse:setStatusCode():statusCode="+statusCode);
    if (statusCode < 100 || statusCode > 699)
    {
        console.error("SIPResponse:setStatusCode(): bad status code");
        throw "SIPResponse:setStatusCode(): bad status code";
    }
    if (this.statusLine == null)
    {
        this.statusLine = new StatusLine();
    }
    this.statusLine.setStatusCode(statusCode);
}

SIPResponse.prototype.getStatusLine =function(){
    if(logger!=undefined) logger.debug("SIPResponse:getStatusLine()");
    return this.statusLine;
}

SIPResponse.prototype.getStatusCode =function(){
    if(logger!=undefined) logger.debug("SIPResponse:getStatusCode()");
    return this.statusLine.getStatusCode();
}

SIPResponse.prototype.setReasonPhrase =function(reasonPhrase){
    if(logger!=undefined) logger.debug("SIPResponse:setReasonPhrase():reasonPhrase="+reasonPhrase);
    if (reasonPhrase == null)
    {
        console.error("SIPResponse:setReasonPhrase(): bad reason phrase");
        throw "SIPResponse:setReasonPhrase(): bad reason phrase";
    }
    if (this.statusLine == null)
    {
        this.statusLine = new StatusLine();
    }
    this.statusLine.setReasonPhrase(reasonPhrase);
}

SIPResponse.prototype.getReasonPhrase_argu0 =function(){
    if(logger!=undefined) logger.debug("SIPResponse:getReasonPhrase_argu0()");
    if (this.statusLine == null || this.statusLine.getReasonPhrase() == null)
    {
        return "";
    }
    else
    {
        return this.statusLine.getReasonPhrase();
    }
}

SIPResponse.prototype.isFinalResponse =function(rc){
    if(logger!=undefined) logger.debug("SIPResponse:isFinalResponse():rc="+rc);
    if(rc==null)
    {
        rc=this.statusLine.getStatusCode();
    }
    if(rc >= 200 && rc < 700)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPResponse.prototype.setStatusLine =function(sl){
    if(logger!=undefined) logger.debug("SIPResponse:setStatusLine():sl="+sl.classname);
    this.statusLine = sl;
}

SIPResponse.prototype.checkHeaders =function(){
    if(logger!=undefined) logger.debug("SIPResponse:checkHeaders()");
    
    if (this.getCSeq() == null) {
        console.error("SIPResponse:checkHeaders(): "+ this.CSeq+ " is missing ", 0);
        throw "SIPResponse:checkHeaders(): "+ this.CSeq+ " is missing ";
    }
    if (this.getTo() == null) {
        console.error("SIPResponse:checkHeaders(): "+ this.To+ " is missing ", 0);
        throw "SIPResponse:checkHeaders(): "+ this.To+ " is missing ";
    }
    if (this.getFrom() == null) {
        console.error("SIPResponse:checkHeaders(): "+ this.From+ " is missing ", 0);
        throw "SIPResponse:checkHeaders(): "+ this.From+ " is missing ";
    }
    if (this.getViaHeaders() == null) {
        console.error("SIPResponse:checkHeaders(): "+ this.Via+ " is missing ", 0);
        throw "SIPResponse:checkHeaders(): "+ this.Via+ " is missing ";
    }
    if (this.getCallId() == null) {
       console.error("SIPResponse:checkHeaders(): "+ this.CallID+ " is missing ", 0);
        throw "SIPResponse:checkHeaders(): "+ this.CallID+ " is missing ";
    }
    if (this.getStatusCode() > 699) {
        console.error("SIPResponse:checkHeaders(): unknown error code!" + this.getStatusCode(), 0);
        throw "SIPResponse:checkHeaders(): unknown error code!" + this.getStatusCode();
    }
}

SIPResponse.prototype.encode =function(){
    if(logger!=undefined) logger.debug("SIPResponse:encode()");
    var retval;
    if (this.statusLine != null)
    {
        retval = this.statusLine.encode() + this.superencode();
    }
    else
    {
        retval = this.superencode();
    }
    return retval ;
}

SIPResponse.prototype.encodeMessage =function(){
    if(logger!=undefined) logger.debug("SIPResponse:encodeMessage()");
    var retval;
    if (this.statusLine != null)
    {
        retval = this.statusLine.encode() + this.encodeSIPHeaders();
    }
    else
    {
        retval = this.encodeSIPHeaders();
    }
    return retval ;
}

SIPResponse.prototype.superencode =function(){
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

SIPResponse.prototype.getMessageAsEncodedStrings =function(){
    if(logger!=undefined) logger.debug("SIPResponse:getMessageAsEncodedStrings()");
    var retval = Object.getPrototypeOf(this).getMessageAsEncodedStrings();
    if (this.statusLine != null)
    {
        var x=new Array();
        x[0]=this.statusLine.encode();
        for(var i=0;i<this.retval.length;i++)
        {
            x[i+1]=this.retval[i];
        }
        retval=x;   
    }
    return retval;
}

SIPResponse.prototype.encodeAsBytes =function(transport){
    if(logger!=undefined) logger.debug("SIPResponse:encodeAsBytes():transport="+transport);
    var slbytes = new Array();
    if (this.statusLine != null) {
        slbytes = this.getBytes(this.statusLine.encode());
    }
    var superbytes = Object.getPrototypeOf(this).encodeAsBytes(transport);
    var retval = new Array();
    retval=slbytes.concat(superbytes);
    return retval;
}

SIPResponse.prototype.getDialogId =function(){
    if(logger!=undefined) logger.debug("SIPResponse:getDialogId()");
    if(arguments.length==1)
    {
        var isServer = arguments[0];
        return this.getDialogIdargu1(isServer);
    }
    else if(arguments.length==2)
    {
        isServer = arguments[0];
        var toTag = arguments[1];
        return this.getDialogIdargu2(isServer, toTag);
    }
}
SIPResponse.prototype.getDialogIdargu1 =function(isServer){
    if(logger!=undefined) logger.debug("SIPResponse:getDialogIdargu1():isServer="+isServer);
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
    } else {
        if (to.getTag() != null) {
            retval=retval+this.COLON+to.getTag();
        }
        if (from.getTag() != null) {
            retval=retval+this.COLON+from.getTag();
        }
    }
    return retval.toString().toLowerCase();
}

SIPResponse.prototype.getDialogIdargu2 =function(isServer, toTag){
    if(logger!=undefined) logger.debug("SIPResponse:getDialogIdargu2():isServer="+isServer+", toTag="+toTag);
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

SIPResponse.prototype.setBranch =function(via, method){
    if(logger!=undefined) logger.debug("SIPResponse:setBranch():via="+via.classname+", method="+method);
    var branch;
    if (method==this.ACK) {
        if (this.statusLine.getStatusCode() >= 300 ) {
            branch = this.getTopmostVia().getBranch();   // non-2xx ACK uses same branch
        } 
        else {
            var utils=new Utils();
            branch = utils.getInstance().generateBranchId();    // 2xx ACK gets new branch
        }
    } 
    else if (method==this.CANCEL) {
        branch = this.getTopmostVia().getBranch();   // CANCEL uses same branch
    } 
    else {
        return;
    }
    via.setBranch( branch );
}

SIPResponse.prototype.getFirstLine =function(){
    if(logger!=undefined) logger.debug("SIPResponse:getFirstLine()");
    if (this.statusLine == null)
    {
        return null;
    }
    else
    {
        return this.statusLine.encode();
    }
}

SIPResponse.prototype.setSIPVersion =function(sipVersion){
    if(logger!=undefined) logger.debug("SIPResponse:setSIPVersion():sipVersion="+sipVersion);
    this.statusLine.setSipVersion(sipVersion);
}

SIPResponse.prototype.getSIPVersion =function(){
    if(logger!=undefined) logger.debug("SIPResponse:getSIPVersion()");
    return this.statusLine.getSipVersion();
}

SIPResponse.prototype.toString =function(){
    if(logger!=undefined) logger.debug("SIPResponse:toString()");
    if (this.statusLine == null) {
        return  "";
    }
    else {
        return this.statusLine.encode() + Object.getPrototypeOf(this).encode();
    }
}

SIPResponse.prototype.createRequest =function(requestURI, via, cseq, from, to){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():requestURI="+requestURI);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():from="+from);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():to="+to);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():via="+via);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():cseq="+cseq);
    var newRequest = new SIPRequest();
    var method = cseq.getMethod();
    var callid=this.getCallId();
    newRequest.setMethod(method);
    newRequest.setRequestURI(requestURI);
    this.setBranch(via, method);
    newRequest.setHeader(via);
    newRequest.setHeader(from);
    newRequest.setHeader(to);
    newRequest.setHeader(cseq);
    newRequest.setHeader(callid);
    newRequest.attachHeader(new MaxForwards(70), false);
    var headerIterator = newRequest.getHeaders();
    for(var i=0;i<headerIterator.length;i++)
    {
        var nextHeader = headerIterator[i];
        if (this.isResponseHeader(nextHeader)
            || nextHeader instanceof ViaList
            || nextHeader instanceof CSeq
            || nextHeader instanceof ContentType
            || nextHeader instanceof ContentLength
            || nextHeader instanceof RecordRouteList
            //|| nextHeader instanceof RequireList
            || nextHeader instanceof ContactList    // JvB: added
            || nextHeader instanceof ContentLength
            //|| nextHeader instanceof ServerHeader
            || nextHeader instanceof Reason
            //|| nextHeader instanceof SessionExpires
            || nextHeader instanceof ReasonList) {
            continue;
        }
        if (nextHeader instanceof To)
        {
            nextHeader =  to;
        }
        else if (nextHeader instanceof From)
        {
            nextHeader =  from;
        }
        newRequest.attachHeader(nextHeader, false);    
    }
    // JvB: all requests need a Max-Forwards
    var mfimpl=new MessageFactoryImpl();   
    if (mfimpl.getDefaultUserAgentHeader() != null ) {
        newRequest.setHeader(mfimpl.getDefaultUserAgentHeader());
    }
    return newRequest;
}

SIPResponse.prototype.getBytes =function(str){
    if(logger!=undefined) logger.debug("SIPResponse:getBytes():str="+str);
    var array=new Array();
    str=new String(str);
    for(var i=0;i<str.length;i++)
    {
        array[i]=str.charCodeAt(i);
    } 
    return array;
}

SIPResponse.prototype.addContactUseragent =function(requestsent){
    if(logger!=undefined) logger.debug("SIPResponse:addContactUseragent():requestsent:"+requestsent);
    var contact=requestsent.getContactHeader();
    var useragent=requestsent.getHeader("User-Agent");
    this.addHeader(contact);
    this.addHeader(useragent);
}

SIPResponse.prototype.fortest =function(){
    if(logger!=undefined) logger.debug("SIPResponse:fortest()");
    var retval = this.statusLine.encode();
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