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
 *  Implementation of the JAIN-SIP HeaderFactoryImpl .
 *  @see  gov/nist/javax/sip/header/HeaderFactoryImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function HeaderFactoryImpl() {
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:HeaderFactoryImpl()");
    this.classname="HeaderFactoryImpl";
    this.stripAddressScopeZones = false;
}

HeaderFactoryImpl.prototype = new SIPHeaderList();
HeaderFactoryImpl.prototype.constructor=HeaderFactoryImpl;
HeaderFactoryImpl.prototype.NAME="Authorization";

HeaderFactoryImpl.prototype.setPrettyEncoding =function(flag){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:setPrettyEncoding():flag="+flag);
    var sipheaderlist=new SIPHeaderList();
    sipheaderlist.setPrettyEncode(flag);
}

HeaderFactoryImpl.prototype.createAllowEventsHeader =function(eventType){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createAllowEventsHeader():eventType="+eventType);
    if (eventType == null)
    {
        console.error("HeaderFactoryImpl:createAllowEventsHeader(): null arg eventType");
        throw "HeaderFactoryImpl:createAllowEventsHeader(): null arg eventType";
    }
    var allowEvents = new AllowEvents();
    allowEvents.setEventType(eventType);
    return allowEvents;
}

HeaderFactoryImpl.prototype.createAllowHeader =function(method){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createAllowHeader():method="+method);
    if (method == null)
    {
        console.error("HeaderFactoryImpl:createAllowHeader(): null arg method");
        throw "HeaderFactoryImpl:createAllowHeader(): null arg method"; 
    }
    var allow = new Allow();
    allow.setMethod(method);
    return allow;
}

HeaderFactoryImpl.prototype.createCSeqHeader =function(sequenceNumber,method){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createCSeqHeader(): sequenceNumber="+sequenceNumber+", method="+sequenceNumber);
    if (sequenceNumber < 0)
    {
        console.error("HeaderFactoryImpl:createCSeqHeader(): bad arg " + sequenceNumber);
        throw "HeaderFactoryImpl:createCSeqHeader(): bad arg " + sequenceNumber;
    }
    if (method == null)
    {
        console.error("HeaderFactoryImpl:createCSeqHeader(): null arg method");
        throw "HeaderFactoryImpl:createCSeqHeader(): null arg method";      
    }
    var cseq = new CSeq();
    cseq.setMethod(method);
    cseq.setSeqNumber(sequenceNumber);
    return cseq;
}

HeaderFactoryImpl.prototype.createCallIdHeader =function(callId){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createCallIdHeader():callId="+callId);
    if (callId == null)
    {
        var chain=new Date();
        callId=new String(chain.getTime());
    }
    var c = new CallID();
    c.setCallId(callId);
    return c;
}

HeaderFactoryImpl.prototype.createContactHeader =function(){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createContactHeader()");
    if(arguments.length==0)
    {
        var contact = new Contact();
        contact.setWildCardFlag(true);
        contact.setExpires(0);
        return contact;
    }
    else
    {
        var address=arguments[0];
        if (address == null)
        {
            console.error("HeaderFactoryImpl:createContactHeader(): null arg address");
            throw "HeaderFactoryImpl:createContactHeader(): null arg address";      
        }
        contact = new Contact();
        contact.setAddress(address);
        return contact;
    }  
}

HeaderFactoryImpl.prototype.createContentDispositionHeader =function(contentDisposition){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createContentLengthHeader():contentDisposition="+contentDisposition);
    if (contentDisposition < 0)
    {
        console.error("HeaderFactoryImpl:createContentDispositionHeader(): null arg contentDisposition");
        throw "HeaderFactoryImpl:createContentDispositionHeader(): null arg contentDisposition";
    }
    var c = new ContentDisposition();
    c.setDispositionType(contentDisposition);
    return c;
}

HeaderFactoryImpl.prototype.createContentLengthHeader =function(contentLength){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createContentLengthHeader():contentLength="+contentLength);
    if (contentLength < 0)
    {
        console.error("HeaderFactoryImpl:createContentLengthHeader(): bad contentLength");
        throw "HeaderFactoryImpl:createContentLengthHeader(): bad contentLength"; 
    }
    var c = new ContentLength();
    c.setContentLength(contentLength);
    return c;
}

HeaderFactoryImpl.prototype.createContentTypeHeader =function(contentType,contentSubType){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createContentTypeHeader():contentType="+contentType+",contentSubType="+contentSubType);
    if (contentType == null || contentSubType == null)
    {
        console.error("HeaderFactoryImpl:createContentTypeHeader(): null contentType or subType");
        throw "HeaderFactoryImpl:createContentTypeHeader(): null contentType or subType"; 
    }
    var c = new ContentType();
    c.setContentType(contentType);
    c.setContentSubType(contentSubType);
    return c;
}

HeaderFactoryImpl.prototype.createEventHeader =function(eventType){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createEventHeader():eventType="+eventType);
    if (eventType == null)
    {
        console.error("HeaderFactoryImpl:createEventHeader(): null eventType");
        throw "HeaderFactoryImpl:createEventHeader(): null eventType"; 
    }
    var event = new Event();
    event.setEventType(eventType);
    return event;
}

HeaderFactoryImpl.prototype.createExpiresHeader =function(expires){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createExpiresHeader(): expiers=");
    if (expires < 0)
    {
        console.error("HeaderFactoryImpl:createExpiresHeader(): bad value " + expires);
        throw "HeaderFactoryImpl:createExpiresHeader(): bad value " + expires;
    }
    var e = new Expires();
    e.setExpires(expires);
    return e;
}


HeaderFactoryImpl.prototype.createExtensionHeader =function(name,value){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createExtensionHeader():name="+name+",value:"+value);
    if (name == null)
    {
        console.error("HeaderFactoryImpl:createExtensionHeader(): bad name");
        throw "HeaderFactoryImpl:createExtensionHeader(): bad name";
    }
    var ext = new ExtensionHeaderImpl();
    ext.setName(name);
    ext.setValue(value);
    return ext;
}

HeaderFactoryImpl.prototype.createFromHeader =function(address,tag){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createFromHeader():address="+address+", tag="+tag);
    if (address == null)
    {
        console.error("HeaderFactoryImpl:createFromHeader(): null address arg");
        throw "HeaderFactoryImpl:createFromHeader(): null address arg";
    }
    var from = new From();
    from.setAddress(address);
    if (tag != null)
    {
        from.setTag(tag);
    }
    return from;
}

HeaderFactoryImpl.prototype.createMaxForwardsHeader =function(maxForwards){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createMaxForwardsHeader(): maxForwards="+maxForwards);
    if (maxForwards < 0 || maxForwards > 255)
    {
        console.error("HeaderFactoryImpl:createMaxForwardsHeader(): bad maxForwards arg " + maxForwards);
        throw "HeaderFactoryImpl:createMaxForwardsHeader(): bad maxForwards arg " + maxForwards;
    }
    var m = new MaxForwards();
    m.setMaxForwards(maxForwards);
    return m;
}

HeaderFactoryImpl.prototype.createProxyAuthenticateHeader =function(scheme){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createProxyAuthenticateHeader():scheme="+scheme);
    if (scheme == null)
    {
        console.error("HeaderFactoryImpl:createProxyAuthenticateHeader(): null scheme arg");
        throw "HeaderFactoryImpl:createProxyAuthenticateHeader(): null scheme arg";
    }
    var p = new ProxyAuthenticate();
    p.setScheme(scheme);
    return p;
}


HeaderFactoryImpl.prototype.createProxyAuthorizationHeader =function(scheme){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createProxyAuthorizationHeader():scheme="+scheme);
    if (scheme == null)
    {
        console.error("HeaderFactoryImpl:createProxyAuthorizationHeader(): null scheme arg");
        throw "HeaderFactoryImpl:createProxyAuthorizationHeader():null scheme arg";      
    }
    var p = new ProxyAuthorization();
    p.setScheme(scheme);
    return p;
}

HeaderFactoryImpl.prototype.createReasonHeader =function(protocol,cause,text){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createReasonHeader():protocol="+protocol+", cause="+cause+",text="+text);
    if (protocol == null)
    {
        console.error("HeaderFactoryImpl:createReasonHeader(): null protocol arg");
        throw "HeaderFactoryImpl:createReasonHeader(): null protocol arg";    
    }
    if (cause < 0)
    {
        console.error("HeaderFactoryImpl:createReasonHeader(): bad cause");
        throw "HeaderFactoryImpl:createReasonHeader():bad cause";   
    }
    var reason = new Reason();
    reason.setProtocol(protocol);
    reason.setCause(cause);
    reason.setText(text);
    return reason;
}

HeaderFactoryImpl.prototype.createRecordRouteHeader =function(address){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createRecordRouteHeader():address="+address);
    if ( address == null) 
    {
        console.error("HeaderFactoryImpl:createRecordRouteHeader(): null address arg");
        throw "HeaderFactoryImpl:createRecordRouteHeader(): null address arg";  
    }
    var recordRoute = new RecordRoute();
    recordRoute.setAddress(address);
    return recordRoute;
}

HeaderFactoryImpl.prototype.createRouteHeader =function(address){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createRouteHeader():address="+address);
    if (address == null)
    {
        console.error("HeaderFactoryImpl:createRouteHeader(): null address arg");
        throw "HeaderFactoryImpl:createRouteHeader(): null address arg";
    }
    var route = new Route();
    route.setAddress(address);
    return route;
}

HeaderFactoryImpl.prototype.createSubjectHeader =function(subject){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createSubjectHeader():subject="+subject);
    if (subject == null)
    {
        console.error("HeaderFactoryImpl:createSubjectHeader(): null subject arg");
        throw "HeaderFactoryImpl:createSubjectHeader():  null subject arg";
    }
    var s = new Subject();
    s.setSubject(subject);
    return s;
}

HeaderFactoryImpl.prototype.createSupportedHeader =function(optionTag){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createSupportedHeader():optionTag="+optionTag);
    if (optionTag == null)
    {
        console.error("HeaderFactoryImpl:createSupportedHeader(): null optionTag arg");
        throw "HeaderFactoryImpl:createSupportedHeader(): null optionTag arg";
    }
    var supported = new Supported();
    supported.setOptionTag(optionTag);
    return supported;
}

HeaderFactoryImpl.prototype.createTimeStampHeader =function(timeStamp){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createTimeStampHeader():timeStamp="+timeStamp);
    if (timeStamp < 0)
    {
        console.error("HeaderFactoryImpl:createTimeStampHeader(): illegal timeStamp");
        throw "HeaderFactoryImpl:createTimeStampHeader(): illegal timeStamp";
    }
    var t = new TimeStamp();
    t.setTimeStamp(timeStamp);
    return t;
}

HeaderFactoryImpl.prototype.createToHeader =function(address,tag){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createToHeader():address="+address+"tag="+address+","+tag);
    if (address == null)
    {
        console.error("HeaderFactoryImpl:createSupportedHeader(): null address arg");
        throw "HeaderFactoryImpl:createSupportedHeader(): null address arg";
    }
    var to = new To();
    to.setAddress(address);
    if (tag != null)
    {
        to.setTag(tag);
    }
    return to;
}

HeaderFactoryImpl.prototype.createUserAgentHeader =function(product){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createUserAgentHeader():product="+product);
    if (product == null)
    {
        console.error("HeaderFactoryImpl:createUserAgentHeader(): null product arg");
        throw "HeaderFactoryImpl:createUserAgentHeader(): null product arg";
    }
    var userAgent = new UserAgent();
    userAgent.setProduct(product);
    return userAgent;
}

HeaderFactoryImpl.prototype.createViaHeader =function(host,port,transport,branch){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createViaHeader():hostt="+host+", port="+port+", transport="+transport+", branch="+branch);
    if (host == null || transport == null)
    {
        console.error("HeaderFactoryImpl:createViaHeader(): null host or  transport arg");
        throw "HeaderFactoryImpl:createViaHeader(): null host or  transport arg";
    }
    var via = new Via();
    if (branch != null)
    {
        via.setBranch(branch);
    }
    if(host.indexOf(':') >= 0&& host.indexOf('[') < 0)
    {
        if(this.stripAddressScopeZones)
        {
            var zoneStart = host.indexOf('%');
            if(zoneStart != -1)
            {
                host = host.substring(0, zoneStart);
            }
        }
        host = "[" + host + "]";
    }
    via.setHost(host);
    if(port != null)
    {
        via.setPort(port);
    }
    if(transport != null)
    {
        via.setTransport(transport);
    }
    else
    {
        via.setTransport("WS");
    }
    return via;
}


HeaderFactoryImpl.prototype.createWWWAuthenticateHeader =function(scheme){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createWWWAuthenticateHeader():scheme="+scheme);
    if (scheme == null)
    {
        console.error("HeaderFactoryImpl:createWWWAuthenticateHeader(): null scheme arg");
        throw "HeaderFactoryImpl:createWWWAuthenticateHeader(): null scheme arg";
    }
    var www = new WWWAuthenticate();
    www.setScheme(scheme);
    return www;
}

HeaderFactoryImpl.prototype.createHeader =function(){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createHeader():arguments="+arguments.toString());
    if(arguments.length==1)
    {
        var headerText = arguments[0];
        var smp = new StringMsgParser();
        var sipHeader = smp.parseSIPHeader(headerText.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, ''));
        
        if(sipHeader instanceof SIPHeaderList)
        {
            if (sipHeader.size() > 1) 
            {
                console.error("HeaderFactoryImpl:createHeader(): only singleton allowed " + headerText);
                throw "HeaderFactoryImpl:createHeader():only singleton allowed " + headerText;
            }
            else if (sipHeader.size() == 0) 
            {
                var classname= sipHeader.getMyClass();
                var header=new Function('return new ' + classname)();
                return  header;
            }
            else 
            {
                return sipHeader.getFirst();
            }
        }
        else
        {
            return sipHeader;
        }
    }
    else if(arguments.length==2)
    {
        var headerName = arguments[0];
        var headerValue = arguments[1];
        if (headerName == null)
        {
            console.error("header name is null");
            console.error("HeaderFactoryImpl:createHeader(): header name is null");
            throw "HeaderFactoryImpl:createHeader(): header name is null";
        }
        var hdrText =headerName+":"+headerValue;
        return this.createHeader(hdrText);
    }
}


HeaderFactoryImpl.prototype.createHeaders =function(headers){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createHeaders():headers="+headers);
    if (headers == null)
    {
        console.error("HeaderFactoryImpl:createHeaders(): null headers arg");
        throw "HeaderFactoryImpl:createHeaders(): null headers arg";
    }
    var smp = new StringMsgParser();
    var shdr = smp.parseSIPHeader(headers);
    if (shdr instanceof SIPHeaderList)
    {
        return  shdr;
    }
    else
    {
        console.error("HeaderFactoryImpl:createHeaders():List of headers of this type is not allowed in a message", 0);
        throw "HeaderFactoryImpl:createHeaders(): list of headers of this type is not allowed in a message";
    }
}

HeaderFactoryImpl.prototype.createRouteList =function(recordRouteList){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createRouteList():recordRouteList"+recordRouteList);
    if(recordRouteList!=null)
    {
        var routeList = new RouteList();
        for(var i=recordRouteList.getHeaderList().length-1;i>=0;i--)
        {
            var rr = recordRouteList.getHeaderList()[i];
            var route = new Route();
            var address =  rr.getAddress();

            route.setAddress(address);
            route.setParameters(rr.getParameters());
            routeList.add(route);
        }
        return routeList;
    }
    else
    {
        return new RouteList();
    }
}


HeaderFactoryImpl.prototype.createRequestLine =function(requestLine){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createRequestLine():requestLine="+requestLine);
    var requestLineParser = new RequestLineParser(requestLine);
    return requestLineParser.parse();
}


HeaderFactoryImpl.prototype.createStatusLine =function(statusLine){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createStatusLine():statusLine="+statusLine);
    var statusLineParser = new StatusLineParser(statusLine);
    return statusLineParser.parse();
}

HeaderFactoryImpl.prototype.createAuthorizationHeader =function(){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createAuthorizationHeader()");
    if(arguments.length==1)
    {
        var scheme=arguments[0];
        return this.createAuthorizationHeaderargu1(scheme);
    }
    else
    {
        var response=arguments[0];
        var request=arguments[1];
        var password=arguments[2];
        var sipuri=arguments[3];
        return this.createAuthorizationHeaderargu2(response, request, password, sipuri);
    }
}

HeaderFactoryImpl.prototype.createAuthorizationHeaderargu1 =function(scheme){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createAuthorizationHeader():scheme="+scheme);
    if (scheme == null)
    {
        console.error("HeaderFactoryImpl:createAuthorizationHeaderargu1(): null scheme arg");
        throw "HeaderFactoryImpl:createAuthorizationHeaderargu1(): null scheme arg";
    }
    var auth = new Authorization();
    auth.setScheme(scheme);
    return auth;
}


HeaderFactoryImpl.prototype.createAuthorizationHeaderargu2 =function(response,request,password,sipuri){
    if(logger!=undefined) logger.debug("HeaderFactoryImpl:createAuthorizationHeaderargu2():response="+response+",request="+request);
    if(response.hasHeader("www-authenticate"))
    {
        var realm=response.getWWWAuthenticate().getRealm();
        var scheme=response.getWWWAuthenticate().getScheme();
        var nonce=response.getWWWAuthenticate().getNonce();
        var qop=response.getWWWAuthenticate().getQop();
        var authorization=new Authorization();
    }
    else if(response.hasHeader("proxy-authenticate"))
    {
        realm=response.getProxyAuthenticate().getRealm();
        scheme=response.getProxyAuthenticate().getScheme();
        nonce=response.getProxyAuthenticate().getNonce(); 
        var proxyauthorization=new ProxyAuthorization();
        qop=response.getWWWAuthenticate().getQop();
    }
    var mda=new MessageDigestAlgorithm();
    var method=response.getCSeq().getMethod();
    var user=request.getFrom().getAddress().getURI().getUser();
    var cnonce=Math.floor(Math.random()*16777215).toString(16);
    var nc="00000001"; 
    var resp=mda.calculateResponse(user,realm,password,nonce,nc,cnonce, method,sipuri,null,qop);
    
    if(response.hasHeader("www-authenticate"))
    {
        authorization.setUsername(user);
        authorization.setRealm(realm);
        authorization.setNonce(nonce);
        authorization.setCNonce(cnonce);
        authorization.setNonceCount(nc);
        authorization.setScheme(scheme);
        authorization.setResponse(resp);
        authorization.setURI(sipuri);
        authorization.setAlgorithm("MD5");
        return authorization;
    }
    else if(response.hasHeader("proxy-authenticate"))
    {
        proxyauthorization.setUsername(user);
        proxyauthorization.setRealm(realm);
        proxyauthorization.setNonce(nonce);
        authorization.setCNonce(cnonce);
        authorization.setNonceCount(nc);
        proxyauthorization.setScheme(scheme);
        proxyauthorization.setResponse(resp);
        proxyauthorization.setURI(sipuri);
        proxyauthorization.setAlgorithm("MD5");
        return proxyauthorization;
    }
}