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
 *  Implementation of the JAIN-SIP MessageFactoryImpl .
 *  @see  gov/nist/javax/sip/message/MessageFactoryImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function MessageFactoryImpl() {
    if(logger!=undefined) logger.debug("MessageFactoryImpl:MessageFactoryImpl()");
    this.classname="MessageFactoryImpl";
    this.testing = false;
    this.strict  = true;
    this.defaultContentEncodingCharset = "UTF-8"
    this.userAgent=null;
    this.server=null;
    if(arguments.length!=0)
    {
        this.listeningpoint=arguments[0];
        this.viaheader=this.listeningpoint.getViaHeader();
    }
}

MessageFactoryImpl.prototype.CONTENTTYPEHEADER="Content-Type";

MessageFactoryImpl.prototype.setStrict =function(){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:setStrict()");
    
}

MessageFactoryImpl.prototype.setTest =function(){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:setTest()");
    
}

MessageFactoryImpl.prototype.createRequest =function(){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequest()");
    if(arguments.length==1)
    {
        var requestString = arguments[0];
        return this.createRequestPrototype5(requestString);
    }
    else if(arguments.length==7)
    {
        var requestURI = arguments[0];
        var method = arguments[1];
        var callId = arguments[2];
        var cSeq = arguments[3];
        var from = arguments[4];
        var to = arguments[5];
        var maxForwards = arguments[6];
        var via=this.viaheader;
        return this.createRequestPrototype3(requestURI, method, callId, cSeq, from, to, via, maxForwards);
    }
    else if(arguments.length==9)
    {
        if(arguments[7].classname!="ContentType")
        {
            requestURI = arguments[0];
            method = arguments[1];
            callId = arguments[2];
            cSeq = arguments[3];
            from = arguments[4];
            to = arguments[5];
            via=this.viaheader;
            maxForwards = arguments[6];
            var content = arguments[7];
            var contentType = arguments[8];
            return this.createRequestPrototype2(requestURI, method, callId, cSeq, from, to, via, maxForwards, content, contentType);
        }
        else if(arguments[8].constructor!=Array)
        {
            requestURI = arguments[0];
            method = arguments[1];
            callId = arguments[2];
            cSeq = arguments[3];
            from = arguments[4];
            to = arguments[5];
            via=this.viaheader;
            maxForwards = arguments[6];
            contentType = arguments[7];
            content = arguments[8];
            return this.createRequestPrototype4(requestURI, method, callId, cSeq, from, to, via, maxForwards, contentType, content);
        }
        else if(arguments[8].constructor==Array)
        {
            requestURI = arguments[0];
            method = arguments[1];
            callId = arguments[2];
            cSeq = arguments[3];
            from = arguments[4];
            to = arguments[5];
            via=this.viaheader;
            maxForwards = arguments[6];
            contentType = arguments[7];
            content = arguments[8];
            return this.createRequestPrototype1(requestURI, method, callId, cSeq, from, to, via, maxForwards, contentType, content);
        }
    }
}


MessageFactoryImpl.prototype.createRequestPrototype1 =function(requestURI,method,callId,cSeq,from,to,via,maxForwards,contentType,content){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():requestURI="+requestURI);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():method="+method);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():callId="+callId);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():cSeq="+cSeq);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():from="+from);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():to="+to);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():via="+via);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():maxForwards="+maxForwards);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():contentType="+contentType);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype1():content="+content);
    
    if (requestURI == null || method == null || callId == null
        || cSeq == null || from == null || to == null || via == null
        || maxForwards == null || content == null
        || contentType == null)
        {
        console.error("MessageFactoryImpl:createRequestPrototype1(): some parameters are missing, unable to create the request");
        throw "MessageFactoryImpl:createRequestPrototype1(): some parameters are missing, unable to create the request";
    }
    
    var sipRequest = new SIPRequest();
    sipRequest.setRequestURI(requestURI);
    sipRequest.setMethod(method);
    sipRequest.setCallId(callId);
    sipRequest.setCSeq(cSeq);
    sipRequest.setFrom(from);
    sipRequest.setTo(to);
    sipRequest.setVia(via);
    sipRequest.setMaxForwards(maxForwards);
    sipRequest.setContent(content, contentType);
    if (this.userAgent != null ) {
        sipRequest.setHeader(this.userAgent);
    }
    return sipRequest;
}


MessageFactoryImpl.prototype.createRequestPrototype2 =function(requestURI,method,callId,cSeq,from,to,via,maxForwards,content,contentType){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():requestURI="+requestURI);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():method="+method);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():callId="+callId);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():cSeq="+cSeq);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():from="+from);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():to="+to);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():via="+via);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():maxForwards="+maxForwards);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():contentType="+contentType);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():content="+content);
    
    if (requestURI == null || method == null || callId == null
        || cSeq == null || from == null || to == null || via == null
        || maxForwards == null || content == null
        || contentType == null)
        {
        console.error("MessageFactoryImpl:createRequestPrototype2(): some parameters are missing, unable to create the request");
        throw "MessageFactoryImpl:createRequestPrototype2(): some parameters are missing, unable to create the request";
    }

    var sipRequest = new SIPRequest();
    sipRequest.setRequestURI(requestURI);
    sipRequest.setMethod(method);
    sipRequest.setCallId(callId);
    sipRequest.setCSeq(cSeq);
    sipRequest.setFrom(from);
    sipRequest.setTo(to);
    sipRequest.setVia(via);
    sipRequest.setMaxForwards(maxForwards);
    sipRequest.setHeader(contentType);
    sipRequest.setMessageContent(content);
    if (this.userAgent != null ) {
        sipRequest.setHeader(this.userAgent);
    }
    return sipRequest;
}

MessageFactoryImpl.prototype.createRequestPrototype3 =function(requestURI,method,callId,cSeq,from,to,via,maxForwards){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():requestURI="+requestURI);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():method="+method);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():callId="+callId);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():cSeq="+cSeq);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():from="+from);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():to="+to);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():via="+via);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():maxForwards="+maxForwards);
    
    if (requestURI == null || method == null || callId == null
        || cSeq == null || from == null || to == null || via == null
        || maxForwards == null)
        {
        console.error("MessageFactoryImpl:createRequestPrototype3(): some parameters are missing, unable to create the request");
        throw "MessageFactoryImpl:createRequestPrototype3(): some parameters are missing, unable to create the request";
    }
    var sipRequest = new SIPRequest();
    sipRequest.setRequestURI(requestURI);
    sipRequest.setMethod(method);
    sipRequest.setCallId(callId);
    sipRequest.setCSeq(cSeq);
    sipRequest.setFrom(from);
    sipRequest.setTo(to);
    sipRequest.setVia(via);
    sipRequest.setMaxForwards(maxForwards);
    if (this.userAgent != null) {
        sipRequest.setHeader(this.userAgent);
    }
    return sipRequest;
}

MessageFactoryImpl.prototype.createRequestPrototype4 =function(requestURI,method,callId,cSeq,from,to,via,maxForwards,contentType,content){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():requestURI="+requestURI);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():method="+method);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():callId="+callId);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():cSeq="+cSeq);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():from="+from);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():to="+to);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():via="+via);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():maxForwards="+maxForwards);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():contentType="+contentType);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype2():content="+content);
    
    if (requestURI == null || method == null || callId == null
        || cSeq == null || from == null || to == null || via == null
        || maxForwards == null || content == null
        || contentType == null)
        {
        console.error("MessageFactoryImpl:createRequestPrototype4(): some parameters are missing, unable to create the request");
        throw "MessageFactoryImpl:createRequestPrototype4(): some parameters are missing, unable to create the request";
    }
    
    var sipRequest = new SIPRequest();
    sipRequest.setRequestURI(requestURI);
    sipRequest.setMethod(method);
    sipRequest.setCallId(callId);
    sipRequest.setCSeq(cSeq);
    sipRequest.setFrom(from);
    sipRequest.setTo(to);
    sipRequest.setVia(via);
    sipRequest.setMaxForwards(maxForwards);
    sipRequest.setContent(content, contentType);
    if (this.userAgent != null) {
        sipRequest.setHeader(this.userAgent);
    }
    return sipRequest;
}


MessageFactoryImpl.prototype.createRequestPrototype5 =function(requestString){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createRequestPrototype5():requestString="+requestString);
    if (requestString == null || requestString.equals("")) {
        var retval = new SIPRequest();
        retval.setNullRequest();
        return retval;
    }
    var smp = new StringMsgParser();
    smp.setStrict(this.strict);
    var sipMessage = smp.parseSIPMessage(requestString);
    if (!(sipMessage instanceof SIPRequest))
    {
        console.error("MessageFactoryImpl:createRequestPrototype5(): parsing error");
        throw "MessageFactoryImpl:createRequestPrototype5(): parsing error";
    }
    return  sipMessage;
}


MessageFactoryImpl.prototype.createResponse =function(){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createResponse()");
    if(arguments.length==1)
    {
        var responseString=arguments[0];
        return this.createReponsePrototype9(responseString);
    }
    else if(arguments.length==2)
    {
        var statusCode=arguments[0];
        var request=arguments[1];
        return this.createReponsePrototype6(statusCode, request);
    }
    else if(arguments.length==4)
    {
        if(arguments[3].constructor!=Array)
        {
            statusCode=arguments[0];
            request=arguments[1];
            var contentType=arguments[2];
            var content=arguments[3];
            return this.createReponsePrototype4(statusCode, request, contentType, content);
        }
        else if(arguments[3].constructor==Array)
        {
            statusCode=arguments[0];
            request=arguments[1];
            contentType=arguments[2];
            content=arguments[3];
            return this.createReponsePrototype5(statusCode, request, contentType, content);
        }
    }
    else if(arguments.length==7)
    {
        statusCode=arguments[0];
        var callId=arguments[1];
        var cSeq=arguments[2];
        var from=arguments[3];
        var to=arguments[4];
        var via=arguments[5];
        var maxForwards=arguments[6];
        return this.createReponsePrototype3(statusCode, callId, cSeq, from, to, via, maxForwards)
    }
    else if(arguments.length==9)
    {
        if(arguments[8].classname=="ContentType")
        {
            if(arguments[7].constructor!=Array)
            {
                statusCode=arguments[0];
                callId=arguments[1];
                cSeq=arguments[2];
                from=arguments[3];
                to=arguments[4];
                via=arguments[5];
                maxForwards=arguments[6];
                content=arguments[7];
                contentType=arguments[8];
                return this.createReponsePrototype1(statusCode, callId, cSeq, from, to, via, maxForwards, content, contentType)
            }
            else if(arguments[7].constructor==Array)
            {
                statusCode=arguments[0];
                callId=arguments[1];
                cSeq=arguments[2];
                from=arguments[3];
                to=arguments[4];
                via=arguments[5];
                maxForwards=arguments[6];
                content=arguments[7];
                contentType=arguments[8];
                return this.createReponsePrototype2(statusCode, callId, cSeq, from, to, via, maxForwards, content, contentType)
            }
        }
        else if(arguments[8].classname!="ContentType")
        {
            if(arguments[7].constructor!=Array)
            {
                statusCode=arguments[0];
                callId=arguments[1];
                cSeq=arguments[2];
                from=arguments[3];
                to=arguments[4];
                via=arguments[5];
                maxForwards=arguments[6];
                contentType=arguments[7];
                content=arguments[8];
                return this.createReponsePrototype7(statusCode, callId, cSeq, from, to, via, maxForwards, contentType, content)
            }
            else if(arguments[7].constructor==Array)
            {
                statusCode=arguments[0];
                callId=arguments[1];
                cSeq=arguments[2];
                from=arguments[3];
                to=arguments[4];
                via=arguments[5];
                maxForwards=arguments[6];
                contentType=arguments[7];
                content=arguments[8];
                return this.createReponsePrototype8(statusCode, callId, cSeq, from, to, via, maxForwards, contentType, content)
            }
        }
    }
}

MessageFactoryImpl.prototype.createReponsePrototype1 =function(statusCode, callId, cSeq, from, to, via, maxForwards, content, contentType){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype1():statusCode="+statusCode);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype1():callId="+callId);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype1():cSeq="+cSeq);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype1():from="+from);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype1():to="+to);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype1():via="+via);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype1():maxForwards="+maxForwards);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype1():contentType="+contentType);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype1():content="+content);
    
    if (callId == null || cSeq == null || from == null || to == null
        || via == null || maxForwards == null || content == null
        || contentType == null)
        {
        console.error("MessageFactoryImpl:createReponsePrototype1(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype1(): some parameters are missing, unable to create the response";
    }
    
    var sipResponse = new SIPResponse();
    var statusLine = new StatusLine();
    statusLine.setStatusCode(statusCode);
    var reasonPhrase = sipResponse.getReasonPhrase(statusCode);
    //if (reasonPhrase == null)
    //  throw new ParseException(statusCode + " Unkown  ", 0);
    statusLine.setReasonPhrase(reasonPhrase);
    sipResponse.setStatusLine(statusLine);
    sipResponse.setCallId(callId);
    sipResponse.setCSeq(cSeq);
    sipResponse.setFrom(from);
    sipResponse.setTo(to);
    sipResponse.setVia(via);
    sipResponse.setMaxForwards(maxForwards);
    sipResponse.setContent(content, contentType);
    if (this.userAgent != null) {
        sipResponse.setHeader(this.userAgent);
    }
    return sipResponse;
}

MessageFactoryImpl.prototype.createReponsePrototype2 =function(statusCode, callId, cSeq, from, to, via, maxForwards, content, contentType){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype2():statusCode="+statusCode);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype2():callId="+callId);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype2():cSeq="+cSeq);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype2():from="+from);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype2():to="+to);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype2():via="+via);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype2():maxForwards="+maxForwards);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype2():contentType="+contentType);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype2():content="+content);
    if (callId == null || cSeq == null || from == null || to == null
        || via == null || maxForwards == null || content == null
        || contentType == null)
        {
        console.error("MessageFactoryImpl:createReponsePrototype2(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype2(): some parameters are missing, unable to create the response";
    }
    //i don't know why there is no reason phrase in this function
    var sipResponse = new SIPResponse();
    sipResponse.setStatusCode(statusCode);
    sipResponse.setCallId(callId);
    sipResponse.setCSeq(cSeq);
    sipResponse.setFrom(from);
    sipResponse.setTo(to);
    sipResponse.setVia(via);
    sipResponse.setMaxForwards(maxForwards);
    sipResponse.setHeader(contentType);
    sipResponse.setMessageContent(content);
    if (this.userAgent != null) {
        sipResponse.setHeader(this.userAgent);
    }
    return sipResponse;
}

MessageFactoryImpl.prototype.createReponsePrototype3 =function(statusCode,callId,cSeq,from,to,via,maxForwards){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype3():statusCode="+statusCode);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype3():callId="+callId);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype3():cSeq="+cSeq);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype3():from="+from);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype3():to="+to);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype3():via="+via);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype3():maxForwards="+maxForwards);
    if (callId == null || cSeq == null || from == null || to == null
        || via == null || maxForwards == null)
        {
        console.error("MessageFactoryImpl:createReponsePrototype3(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype3(): some parameters are missing, unable to create the response";
    }
    var sipResponse = new SIPResponse();
    sipResponse.setStatusCode(statusCode);
    sipResponse.setCallId(callId);
    sipResponse.setCSeq(cSeq);
    sipResponse.setFrom(from);
    sipResponse.setTo(to);
    sipResponse.setVia(via);
    sipResponse.setMaxForwards(maxForwards);
    if (this.userAgent != null) {
        sipResponse.setHeader(this.userAgent);
    }
    return sipResponse;
}


MessageFactoryImpl.prototype.createReponsePrototype4 =function(statusCode,request,contentType,content){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype4():statusCode="+statusCode);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype4():request="+request);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype4():contentType="+contentType);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype4():content="+content);
    if (request == null || content == null || contentType == null)
    {
        console.error("MessageFactoryImpl:createReponsePrototype4(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype4(): some parameters are missing, unable to create the response";
    }
    var sipRequest =  request;
    var sipResponse = sipRequest.createResponse(statusCode);
    sipResponse.setContent(content, contentType);
    if (this.server != null) {
        sipResponse.setHeader(this.server);
    }
    return sipResponse;
}


MessageFactoryImpl.prototype.createReponsePrototype5 =function(statusCode,request,contentType,content){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype5():statusCode="+statusCode);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype5():request="+request);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype5():contentType="+contentType);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype5():content="+content);
    if (request == null || content == null || contentType == null)
    {
        console.error("MessageFactoryImpl:createReponsePrototype5(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype5(): some parameters are missing, unable to create the response";
    }
    var sipRequest =  request;
    var sipResponse = sipRequest.createResponse(statusCode);
    sipResponse.setHeader(contentType);
    sipResponse.setMessageContent(content);
    if (this.server != null) {
        sipResponse.setHeader(this.server);
    }
    return sipResponse;
}


MessageFactoryImpl.prototype.createReponsePrototype6 =function(statusCode,request){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype6():statusCode="+statusCode);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype6():request="+request);
    if (request == null || content == null || contentType == null)
    {
        console.error("MessageFactoryImpl:createReponsePrototype6(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype6(): ome parameters are missing, unable to create the response";
    }
    var sipRequest = request;
    var sipResponse = sipRequest.createResponse(statusCode);
    sipResponse.removeContent();
    sipResponse.removeHeader(this.CONTENTTYPEHEADER);
    if (this.server != null) {
        sipResponse.setHeader(this.server);
    }
    return sipResponse;
}


MessageFactoryImpl.prototype.createReponsePrototype7 =function(statusCode, callId, cSeq, from, to, via, maxForwards, contentType, content){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype7():statusCode="+statusCode);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype7():callId="+callId);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype7():cSeq="+cSeq);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype7():from="+from);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype7():to="+to);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype7():via="+via);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype7():maxForwards="+maxForwards);
     if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype7():contentType="+contentType);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype7():content="+content);
    if (callId == null || cSeq == null || from == null || to == null
        || via == null || maxForwards == null || content == null
        || contentType == null)
       {
        console.error("MessageFactoryImpl:createReponsePrototype7(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype7(): some parameters are missing, unable to create the response";
    }
    var sipResponse = new SIPResponse();
    var statusLine = new StatusLine();
    statusLine.setStatusCode(statusCode);
    var reason = sipResponse.getReasonPhrase(statusCode);
    if (reason == null)
    {
        console.error("MessageFactoryImpl:createReponsePrototype7(): unknown statusCode "+ statusCode);
        throw "MessageFactoryImpl:createReponsePrototype7(): unknown statusCode "+ statusCode;
    }
    statusLine.setReasonPhrase(reason);
    sipResponse.setStatusLine(statusLine);
    sipResponse.setCallId(callId);
    sipResponse.setCSeq(cSeq);
    sipResponse.setFrom(from);
    sipResponse.setTo(to);
    sipResponse.setVia(via);
    sipResponse.setContent(content, contentType);
    if (this.userAgent != null) {
        sipResponse.setHeader(this.userAgent);
    }
    return sipResponse;
}


MessageFactoryImpl.prototype.createReponsePrototype8 =function(statusCode, callId, cSeq, from, to, via, maxForwards, contentType, content){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype8():statusCode="+statusCode);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype8():callId="+callId);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype8():cSeq="+cSeq);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype8():from="+from);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype8():to="+to);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype8():via="+via);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype8():maxForwards="+maxForwards);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype8():contentType="+contentType);
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype8():content="+content);

    if (callId == null || cSeq == null || from == null || to == null
        || via == null || maxForwards == null || content == null
        || contentType == null)
        {
         console.error("MessageFactoryImpl:createReponsePrototype8(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype8(): some parameters are missing, unable to create the response";
    }
    var sipResponse = new SIPResponse();
    var statusLine = new StatusLine();
    statusLine.setStatusCode(statusCode);
    var reason = sipResponse.getReasonPhrase(statusCode);
    if (reason == null)
    {
        console.error("MessageFactoryImpl:createReponsePrototype8(): unknown statusCode "+ statusCode);
        throw "MessageFactoryImpl:createReponsePrototype8(): unknown statusCode "+ statusCode;
    }
    statusLine.setReasonPhrase(reason);
    sipResponse.setStatusLine(statusLine);
    sipResponse.setCallId(callId);
    sipResponse.setCSeq(cSeq);
    sipResponse.setFrom(from);
    sipResponse.setTo(to);
    sipResponse.setVia(via);
    sipResponse.setContent(content, contentType);
    if (this.userAgent != null) {
        sipResponse.setHeader(this.userAgent);
    }
    return sipResponse;
}

MessageFactoryImpl.prototype.createReponsePrototype9 =function(responseString){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createReponsePrototype9():responseString="+responseString);
    if (responseString == null)
    {
        return new SIPResponse();
    }
    var smp = new StringMsgParser();
    var sipMessage = smp.parseSIPMessage(responseString);
    if (!(sipMessage instanceof SIPResponse))
    {
        console.error("MessageFactoryImpl:createReponsePrototype9(): parse error");
        throw "MessageFactoryImpl:createReponsePrototype9(): parse error";
    }
    return sipMessage;
}

MessageFactoryImpl.prototype.setDefaultUserAgentHeader =function(userAgent){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:setDefaultUserAgentHeader():userAgent="+userAgent);
    this.userAgent = userAgent;
}

MessageFactoryImpl.prototype.setDefaultServerHeader =function(server){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:setDefaultServerHeader():server="+server);
    this.server = server;
}

MessageFactoryImpl.prototype.getDefaultUserAgentHeader =function(){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:getDefaultUserAgentHeader()");
    return this.userAgent;
}

MessageFactoryImpl.prototype.getDefaultServerHeader =function(){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:getDefaultServerHeader()");
    return this.server;
}

MessageFactoryImpl.prototype.setDefaultContentEncodingCharset =function(charset){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:setDefaultContentEncodingCharset():charset:"+charset);
    if (charset == null ) {
        throw new NullPointerException ("Null argument!");
    }
    this.defaultContentEncodingCharset = charset;
}

MessageFactoryImpl.prototype.getDefaultContentEncodingCharset =function(){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:getDefaultContentEncodingCharset()");
    return this.defaultContentEncodingCharset;
}

MessageFactoryImpl.prototype.createMultipartMimeContent =function(multipartMimeCth,contentType,
    contentSubtype,contentBody){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:createMultipartMimeContent()");
    var boundary = multipartMimeCth.getParameter("boundary");
    var retval = new MultipartMimeContentImpl(multipartMimeCth);
    for (var i = 0 ;  i < contentType.length; i++ ) {
        var cth = new ContentType(contentType[i],contentSubtype[i]);
        var contentImpl  = new ContentImpl(contentBody[i],boundary);
        contentImpl.setContentTypeHeader(cth);
        retval.add(contentImpl);
    }
    return retval;
}

MessageFactoryImpl.prototype.getBytes =function(str){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:getBytes():str="+str);
    var array=new Array();
    str=new String(str);
    for(var i=0;i<str.length;i++)
    {
        array[i]=str.charCodeAt(i);
    }
    return array;
}

MessageFactoryImpl.prototype.addHeader =function(sipmessage,header){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:addHeader():sipmessage="+sipmessage+",header="+header);
    sipmessage.addHeader(header);
    return sipmessage;
}

MessageFactoryImpl.prototype.setNewViaHeader =function(sipmessage){
    if(logger!=undefined) logger.debug("MessageFactoryImpl:setNewViaHeader():sipmessage="+sipmessage);
    this.viaheader=this.listeningpoint.getViaHeader();
    if(sipmessage instanceof SIPRequest)
    {
        var newmessage = new SIPRequest();
        newmessage.setMethod(sipmessage.getMethod());
        newmessage.setRequestURI(sipmessage.getRequestURI());
    }
    else
    {
        newmessage = new SIPResponse();
    }
    
    var headerlist=sipmessage.getHeaders();
    for(var i=0;i<headerlist.length;i++)
    {
        newmessage.addHeader(headerlist[i]);
    }
    
    if(sipmessage.getCSeq()!=null && sipmessage.getCallId()!=null && sipmessage.getViaHeaders()!=null
        && sipmessage.getFrom()!=null && sipmessage.getTo()!=null && sipmessage.getMaxForwards()!=null)
        {
        newmessage.setCSeq(sipmessage.getCSeq());
        newmessage.setCallId(sipmessage.getCallId());
        newmessage.setVia(this.viaheader);
        newmessage.setFrom(sipmessage.getFrom());
        newmessage.setTo(sipmessage.getTo());
        newmessage.setMaxForwards(sipmessage.getMaxForwards());
        if(sipmessage.getContent()!=null)
        {
            var content=sipmessage.getContent();
            var contentType=sipmessage.getContentTypeHeader();
            newmessage.setContent(content, contentType);
        }
    }
    
    return newmessage;
}