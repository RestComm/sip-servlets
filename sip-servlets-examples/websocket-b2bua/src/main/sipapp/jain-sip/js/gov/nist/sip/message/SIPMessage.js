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
 *  Implementation of the JAIN-SIP SIPMessage .
 *  @see  gov/nist/javax/sip/message/SIPMessage.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function SIPMessage() {
    if(logger!=undefined) logger.debug("SIPMessage:SIPMessage()");
    this.classname="SIPMessage";
    this.contentEncodingCharset = new MessageFactoryImpl().getDefaultContentEncodingCharset();
    this.nullRequest=null;
    this.unrecognizedHeaders= new Array();
    this.headers=new Array();
    this.fromHeader=new From();
    this.toHeader=new To();
    this.cSeqHeader=new CSeq();
    this.callIdHeader=new CallID();
    this.contentLengthHeader=new ContentDisposition();
    this.maxForwardsHeader=new MaxForwards();
    this.size=null;
    this.messageContent=null;
    this.nameTable=new Array();
    this.applicationData=null;
    var contentlength=new ContentLength(0)
    this.stackTransaction =null;
    this.attachHeader(contentlength, false);
    
}

SIPMessage.prototype = new MessageObject();
SIPMessage.prototype.constructor=SIPMessage;
SIPMessage.prototype.NEWLINE="\r\n";
SIPMessage.prototype.ViaHeader="Via";
SIPMessage.prototype.BRANCH_MAGIC_COOKIE_UPPER_CASE="Z9HG4BK";
SIPMessage.prototype.CANCEL="CANCEL";
SIPMessage.prototype.CONTENT_TYPE_LOWERCASE="content-type";
SIPMessage.prototype.AUTHORIZATION_LOWERCASE="authorization";
SIPMessage.prototype.PROXYAUTHORIZATION_LOWERCASE="proxy-authorization";
SIPMessage.prototype.CONTACT_LOWERCASE="contact";
SIPMessage.prototype.VIA_LOWERCASE="via";
SIPMessage.prototype.ROUTE_LOWERCASE="route";
SIPMessage.prototype.RECORDROUTE_LOWERCASE="record-route";
SIPMessage.prototype.CONTENT_DISPOSITION_LOWERCASE="content-disposition";
SIPMessage.prototype.EXPIRES_LOWERCASE="expires";

SIPMessage.prototype.isRequestHeader =function(sipHeader){
    if(logger!=undefined) logger.debug("SIPMessage:isRequestHeader():sipHeader="+sipHeader.classname);
    if(sipHeader instanceof Authorization || sipHeader instanceof MaxForwards
        || sipHeader instanceof UserAgent|| sipHeader instanceof ProxyAuthorization
        || sipHeader instanceof Route|| sipHeader instanceof RouteList || sipHeader instanceof Subject)
        {
        return true;
    }
    else
    {
        return false;
    }
}

SIPMessage.prototype.isResponseHeader =function(sipHeader){
    if(logger!=undefined) logger.debug("SIPMessage:isResponseHeader():sipHeader:"+sipHeader.classname);
    if(sipHeader instanceof WWWAuthenticate
        || sipHeader instanceof ProxyAuthenticate)
        {
        return true;
    }
    else
    {
        return false;
    }
}

SIPMessage.prototype.getMessageAsEncodedStrings =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getMessageAsEncodedStrings()");
    var retval = new Array();
    for(var i=0;i<this.headers.length;i++)
    {
        var sipHeader =  this.headers[i];
        if (sipHeader instanceof SIPHeaderList)
        {
            var shl =  sipHeader;
            for(var t=0;t<shl.getHeadersAsEncodedStrings().length;t++)
            {
                retval[retval.length]=shl.getHeadersAsEncodedStrings()[t];
            }
        } 
        else 
        {
            retval[retval.length]=sipHeader.encode();
        }
    }
    return retval;
}

SIPMessage.prototype.encodeSIPHeaders =function(){
    if(logger!=undefined) logger.debug("SIPMessage:encodeSIPHeaders()");
    var encoding = "";
    var string="";
    for(var i=0;i<this.headers.length;i++)
    {
        var siphdr = this.headers[i];
        if (!(siphdr instanceof ContentLength))
        {
            string=string+siphdr.encodeBuffer(encoding);
        }
    }
    string=(string+this.contentLengthHeader.encodeBuffer(encoding)+this.NEWLINE).toString();
    return string;
}

SIPMessage.prototype.encodeMessage =function(){
    if(logger!=undefined) logger.debug("SIPMessage:encodeMessage()");
}

SIPMessage.prototype.getDialogId =function(isServerTransaction){
    if(logger!=undefined) logger.debug("SIPMessage:getDialogId():isServerTransaction="+isServerTransaction);
}

SIPMessage.prototype.encode =function(){
    if(logger!=undefined) logger.debug("SIPMessage:encode()");
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
    if (this.messageContent != null) {
        var content = this.messageContent;
        encoding=encoding+content;
    }
    return encoding.toString();
}

SIPMessage.prototype.encodeAsBytes =function(transport){
    if(logger!=undefined) logger.debug("SIPMessage:encodeAsBytes():transport="+transport);
    if (this instanceof SIPRequest && this.isNullRequest()) 
    {
        return this.getBytes("\r\n\r\n");
    }
    var topVia = this.getHeader(this.ViaHeader);
    topVia.setTransport(transport);
    var encoding = "";
    for(var i=0;i<this.headers.length;i++)
    {
        var siphdr =  this.headers[i];
        if (!(siphdr instanceof ContentLength))
        {
            siphdr.encode(encoding);
        }
    }
    this.contentLengthHeader.encode(encoding);
    encoding=encoding+this.NEWLINE;
    var retval = null;
    var content = this.getRawContent();
    if (content != null) {
        var msgarray = null;
        msgarray = this.getBytes(encoding.toString());
        retval=msgarray.concat(content);
    } else {
        retval = this.getBytes(encoding.toString());
    }
    return retval;
}

SIPMessage.prototype.attachHeader =function(){
    if(logger!=undefined) logger.debug("SIPMessage:attachHeader()");
    if(arguments.length==1)
    {
        var h=arguments[0];
        this.attachHeaderargu1(h);
    }
    else if(arguments.length==2)
    {
        h=arguments[0];
        var replaceflag=arguments[1];
        this.attachHeaderargu2(h,replaceflag);
    }
    else if(arguments.length==3)
    {
        h=arguments[0];
        replaceflag=arguments[1];
        var top=arguments[2];
        this.attachHeaderargu3(h,replaceflag,top);
    }
}

SIPMessage.prototype.attachHeaderargu1 =function(h){
    if(logger!=undefined) logger.debug("SIPMessage:attachHeaderargu1():header:"+h.classname);
    if (h == null)
    {
        console.error("MessageFactoryImpl:attachHeaderargu1(): null header!");
        throw "MessageFactoryImpl:attachHeaderargu1(): null header!";
    }
    if (h instanceof SIPHeaderList) {
        var hl =  h;
        if (hl.hlist.length==0) {
            return;
        }
    }
    this.attachHeaderargu3(h, false, false);
}

SIPMessage.prototype.attachHeaderargu2 =function(h, replaceflag){
    if(logger!=undefined) logger.debug("SIPMessage:attachHeaderargu2():header,replaceglag:"+h.classname+","+replaceflag);
    this.attachHeaderargu3(h, replaceflag,false);
}

SIPMessage.prototype.attachHeaderargu3 =function(header, replaceFlag, top){
    if(logger!=undefined) logger.debug("SIPMessage:attachHeaderargu3():header, replaceFlag, top:"+header.classname+","+replaceFlag+","+top);
    if (header == null)
    {
        console.error("MessageFactoryImpl:attachHeaderargu3(): null header!");
        throw "MessageFactoryImpl:attachHeaderargu3(): null header!";
    }
    var h=null;
    var listmap=new ListMap();
    if(listmap.hasList(header) && !(header instanceof SIPHeaderList))
    {
        var hdrList = listmap.getList(header);
        hdrList.add(header);
        h = hdrList;
    }
    else
    {
        h = header;
    }
    var headerNameLowerCase = h.getName().toLowerCase();
    var l=null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        if(this.nameTable[i][0]==headerNameLowerCase)
        {
            l=i;
        }
    }
    
    if (replaceFlag) 
    {
        if(l!=null)
        {
            this.nameTable.splice(l,1);
        }
    } 
    else if (l!=null && !(h instanceof SIPHeaderList)) 
    {
        if (h instanceof ContentLength) {
            var cl =  h;
            this.contentLengthHeader.setContentLength(cl.getContentLength());
        }
        return;
    }
    
    var originalHeader = this.getHeader(header.getName());
    if (originalHeader != null) {
        var hn=this.headers;
        var n=0;
        for(i=0;i<this.headers.length;i++)
        {
            var next = this.headers[i];
            
            if (next!=originalHeader) {
                hn[n]=next;
                n=n+1;
            }
        }
        this.headers=hn;
    }
    if (l==null) 
    {
        var x=new Array();
        x[0]=headerNameLowerCase;
        x[1]=h;
        this.nameTable.push(x);
        this.headers.push(h);
    }
    else 
    {
        if (h instanceof SIPHeaderList) {
            var hdrlist =  this.nameTable[l][0];
            if (hdrlist != null)
            {
                hdrlist.concatenate(h, top);
            }
            else
            {
                x=new Array();
                x[0]=headerNameLowerCase;
                x[1]=h;
                this.nameTable.push(x);
            }
        } 
        else 
        {
            x=new Array();
            x[0]=headerNameLowerCase;
            x[1]=h;
            this.nameTable.push(x);
        }
    }
    if (h instanceof From) {
        this.fromHeader =  h;
    } else if (h instanceof ContentLength) {
        this.contentLengthHeader =  h;
    } else if (h instanceof To) {
        this.toHeader =  h;
    } else if (h instanceof CSeq) {
        this.cSeqHeader =  h;
    } else if (h instanceof CallID) {
        this.callIdHeader =  h;
    } else if (h instanceof MaxForwards) {
        this.maxForwardsHeader =  h;
    }
}


SIPMessage.prototype.setHeader =function(sipHeader){
    if(logger!=undefined) logger.debug("SIPMessage:setHeader():sipHeader="+sipHeader.classname);
    var header =  sipHeader;
    if (header == null)
    {
        console.error("MessageFactoryImpl:setHeader(): null header!");
        throw "MessageFactoryImpl:setHeader(): null header!";
    }
    if (header instanceof SIPHeaderList) {
        var hl =  header;
        if (hl.hlist.length==0)
        {
            return;
        }
    }
    this.removeHeader(header.getHeaderName());
    this.attachHeader(header, true, false);
}


SIPMessage.prototype.setHeaders =function(headers){
    if(logger!=undefined) logger.debug("SIPMessage:setHeaders():headers="+headers.toString());
    for(var i=0;i<headers.length;i++)
    {
        var sipHeader = headers[i];
        this.attachHeader(sipHeader, false);
    }
}

SIPMessage.prototype.removeHeader =function(){
    if(logger!=undefined) logger.debug("SIPMessage:removeHeader()");
    if(arguments.length==1)
    {
        var headerName=arguments[0];
        this.removeHeaderargu1(headerName);
    }
    else if(arguments.length==2)
    {
        headerName=arguments[0];
        var top=arguments[1];
        this.removeHeaderargu2(headerName,top);
    }
}

SIPMessage.prototype.removeHeaderargu1 =function(headerName){
    if(logger!=undefined) logger.debug("SIPMessage:removeHeaderargu1():headerName="+headerName);
    if (headerName == null)
    {
        console.error("MessageFactoryImpl:removeHeaderargu1(): null header!");
        throw "MessageFactoryImpl:removeHeaderargu1(): null header!";
    }
    var headerNameLowerCase = headerName.toLowerCase();
    var removed=null;
    var l=null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        
        if(this.nameTable[i][0]==headerNameLowerCase)
        {
            l=i;
            removed=this.nameTable[i][0];
        }
    }
    if (removed == null)
    {
        return;
    }
    else
    {
        this.nameTable.splice(l,1);
    }
    if (removed instanceof From) {
        this.fromHeader = null;
    } else if (removed instanceof To) {
        this.toHeader = null;
    } else if (removed instanceof CSeq) {
        this.cSeqHeader = null;
    } else if (removed instanceof CallID) {
        this.callIdHeader = null;
    } else if (removed instanceof MaxForwards) {
        this.maxForwardsHeader = null;
    } else if (removed instanceof ContentLength) {
        this.contentLengthHeader = null;
    }
    for(i=0;i<this.headers.length;i++)
    {
        var sipHeader = this.headers[i];
        if (sipHeader.getName().toLowerCase()==headerNameLowerCase)
        {
            l=i;
        }
    }
    this.headers.splice(l,1);
}


SIPMessage.prototype.removeHeaderargu2 =function(headerName,top){
    if(logger!=undefined) logger.debug("SIPMessage:removeHeaderargu2():headerName="+headerName+", top="+top);
    var headerNameLowerCase = headerName.toLowerCase();
    var toRemove=null;
    var l=null;
    var x=null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        if(this.nameTable[i][0]==headerNameLowerCase)
        {
            l=i;
            toRemove=this.nameTable[i][1];
        }
    }
    if (toRemove == null)
    {
        return;
    }
    if (toRemove instanceof SIPHeaderList) {
        var hdrList =  toRemove;
        if (top)
        {
            hdrList.removeFirst();
        }
        else
        {
            hdrList.removeLast();
        }
        if (hdrList.hlist.length==0) 
        {
            for(i=0;i<this.headers.length;i++)
            {
                var sipHeader = this.headers[i];
                if (sipHeader.getName().toLowerCase()==headerNameLowerCase)
                {
                    x=i;
                }
            } 
        }
        this.nameTable.splice(l,1);
        this.headers.splice(x,1);
    }      
}

SIPMessage.prototype.getTransactionId =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getTransactionId()");
    var topVia = new Via();
    if (this.getViaHeaders().hlist.length!=0) {
        topVia = this.getViaHeaders().getFirst();
    }
    if (topVia != null&& topVia.getBranch() != null
        && topVia.getBranch().toUpperCase().substr(0, 7)==
        this.BRANCH_MAGIC_COOKIE_UPPER_CASE) {
        if (this.getCSeq().getMethod()==(this.CANCEL))
        {
            return (topVia.getBranch() + ":" + this.getCSeq().getMethod()).toLowerCase();
        }
        else
        {
            return topVia.getBranch().toLowerCase();
        }
    }
    else {
        var retval = "";
        var from = this.getFrom();
        var to = this.getTo();
        if (from.hasTag())
        {
            retval=retval+from.getTag()+"-";
        }
        var cid = this.callIdHeader.getCallId();
        retval=retval+cid+"-"+this.cSeqHeader.getSequenceNumber()+"-"+this.cSeqHeader.getMethod();
        if (topVia != null) {
            retval=retval+"-"+topVia.getSentBy().encode();
            if (!topVia.getSentBy().hasPort()) {
                retval=retval+"-"+5060;
            }
        }
        if (this.getCSeq().getMethod()==this.CANCEL) {
            retval=retval+this.CANCEL;
        }
        var utils=new Utils();
        retval=retval.toString().toLowerCase().replace(":", "-").replace("@", "-")+utils.getSignature();
        return retval;
    }
}

SIPMessage.prototype.hashCode =function(){
    if(logger!=undefined) logger.debug("SIPMessage:hashCode()");
    if(this.callIdHeader == null)
    {  
        console.error("MessageFactoryImpl:hashCode(): invalid message! Cannot compute hashcode! call-id header is missing !");
        throw "MessageFactoryImpl:hashCode(): invalid message! Cannot compute hashcode! call-id header is missing !";
    }
    else
    {
        var hash = 0;
        var set=this.callIdHeader.getCallId();
        if(!(set == null || set.value == ""))  
        {  
            for (var i = 0; i < set.length; i++)  
            {  
                hash = hash * 31 + set.charCodeAt(i);  
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
SIPMessage.prototype.hasContent =function(){
    if(logger!=undefined) logger.debug("SIPMessage:hasContent()");
    return this.messageContent != null;
}

SIPMessage.prototype.getHeaders =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getHeaders()");
    if(arguments.length!=0)
    {
        var headerName=arguments[0];
        if (headerName == null)
        {
            console.error("MessageFactoryImpl:getHeaders(): headerName header!");
            throw "MessageFactoryImpl:getHeaders(): null headerName!";
        }
        var sipHeader = null;
        for(var i=0;i<this.nameTable.length;i++)
        {
            if(this.nameTable[i][0]==headerName.toLowerCase())
            {
                sipHeader=this.nameTable[i][1];
            }
        }
        if (sipHeader == null)
        {
            var siphdr=new SIPHeaderList();
            return siphdr.listIterator();
        }
        if (sipHeader instanceof SIPHeaderList) 
        {
            return sipHeader.listIterator();
        }
        else 
        {
            return new HeaderIterator(this, sipHeader);
        }
    }
    else
    {
        return this.headers;
    }

}

SIPMessage.prototype.getHeader =function(headerName){
    if(logger!=undefined) logger.debug("SIPMessage:getHeader():headerName:"+headerName);
    return this.getHeaderLowerCase(headerName.toLowerCase());
}

SIPMessage.prototype.getHeaderLowerCase =function(lowerCaseHeaderName){
    if(logger!=undefined) logger.debug("SIPMessage:getHeaderLowerCase():lowerCaseHeaderName="+lowerCaseHeaderName);
    if (lowerCaseHeaderName == null)
    {
         console.error("MessageFactoryImpl:getHeaderLowerCase(): null lowerCaseHeaderName !");
         throw "MessageFactoryImpl:getHeaderLowerCase(): null lowerCaseHeaderName!";
    }
    var sipHeader = null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        if(this.nameTable[i][0]==lowerCaseHeaderName)
        {
            sipHeader=this.nameTable[i][1];
        }
    }
    if (sipHeader instanceof SIPHeaderList)
    {
        return sipHeader.getFirst();
    }
    else
    {
        return sipHeader;
    }
}


SIPMessage.prototype.getContentTypeHeader =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getContentTypeHeader()");
    return this.getHeaderLowerCase(this.CONTENT_TYPE_LOWERCASE);
}


SIPMessage.prototype.getWWWAuthenticate =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getWWWAuthenticate()");
    return this.getHeaderLowerCase("www-authenticate");
}

SIPMessage.prototype.getProxyAuthenticate =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getproxyAuthenticate()");
    return this.getHeaderLowerCase("proxy-authenticate");
}

SIPMessage.prototype.getContentLengthHeader =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getContentLengthHeader()");
    return this.getContentLength();
}

SIPMessage.prototype.getFrom =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getFrom()");
    return this.fromHeader;
}

/*SIPMessage.prototype.getErrorInfoHeaders =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getErrorInfoHeaders()");
    
}*/

SIPMessage.prototype.getContactHeaders =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getContactHeaders()");
    return this.getSIPHeaderListLowerCase(this.CONTACT_LOWERCASE);
}

SIPMessage.prototype.getContactHeader =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getContactHeader()");
    var clist = this.getContactHeaders();
    if (clist != null) 
    {
        return clist.getFirst();
    } 
    else 
    {
        return null;
    }
}

SIPMessage.prototype.getViaHeaders =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getViaHeaders()");
    return this.getSIPHeaderListLowerCase(this.VIA_LOWERCASE);
}

SIPMessage.prototype.setVia =function(viaList){
    if(logger!=undefined) logger.debug("SIPMessage:setVia():viaList:"+viaList.classname);
    if(viaList.classname=="Via")
    {
        var vList = new ViaList();
        vList.add(viaList);
    }
    else
    {
        vList = new ViaList();
        for(var i=0;i<viaList.hlist.length;i++)
        {
            var via = viaList.hlist[i];
            vList.add(via);
        }
    }
    this.setHeader(vList);
}

SIPMessage.prototype.getTopmostVia =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getTopmostVia()");
    if (this.getViaHeaders() == null)
    {
        return null;
    }
    else
    {
        return this.getViaHeaders().getFirst();
    }
}

SIPMessage.prototype.getCSeq =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getCSeq()");
    return this.cSeqHeader;
}

SIPMessage.prototype.getAuthorization =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getAuthorization()");
    return this.getHeaderLowerCase(this.AUTHORIZATION_LOWERCASE);
}

SIPMessage.prototype.getProxyAuthorization =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getAuthorization()");
    return this.getHeaderLowerCase(this.PROXYAUTHORIZATION_LOWERCASE);
}

SIPMessage.prototype.getMaxForwards =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getMaxForwards()");
    return this.maxForwardsHeader;
}

SIPMessage.prototype.setMaxForwards =function(maxForwards){
    if(logger!=undefined) logger.debug("SIPMessage:setMaxForwards():maxForwards="+maxForwards.classname);
    this.setHeader(maxForwards);
}

SIPMessage.prototype.getRouteHeaders =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getRouteHeaders()");
    return  this.getSIPHeaderListLowerCase(this.ROUTE_LOWERCASE);
}

SIPMessage.prototype.getCallId =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getCallId()");
    return this.callIdHeader;
}

SIPMessage.prototype.setCallId =function(callId){
    if(logger!=undefined) logger.debug("SIPMessage:setCallId():callId="+callId.classname);
    if(typeof callId =="object")
    {
        this.setHeader(callId);
    }
    else if(typeof callId == "string")
    {
        if (this.callIdHeader == null) {
            this.setHeader(new CallID());
        }
        this.callIdHeader.setCallId(callId);
    }
}

SIPMessage.prototype.getRecordRouteHeaders =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getRecordRouteHeaders()");
    return this.getSIPHeaderListLowerCase(this.RECORDROUTE_LOWERCASE);
}

SIPMessage.prototype.getTo =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getTo()");
    return this.toHeader;
}

SIPMessage.prototype.setTo =function(to){
    if(logger!=undefined) logger.debug("SIPMessage:setTo():to:"+to.classname);
    this.setHeader(to);
}

SIPMessage.prototype.setFrom =function(from){
    if(logger!=undefined) logger.debug("SIPMessage:setFrom():from:"+from.classname);
    this.setHeader(from);
}
SIPMessage.prototype.getContentLength =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getContentLength()");
    return this.contentLengthHeader;
}

SIPMessage.prototype.getMessageContent =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getMessageContent()");
    return this.messageContent;
}

SIPMessage.prototype.getRawContent =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getRawContent()");
    return this.messageContent;
}

SIPMessage.prototype.setMessageContent =function(){
    if(logger!=undefined) logger.debug("SIPMessage:setMessageContent()");
    if(arguments.length==1)
    {
        var content=arguments[0];
        this.computeContentLength(content);
        this.messageContent = content;
    }
    else if(arguments.length==3)
    {
        var type=arguments[0];
        var subType=arguments[1];
        var messageContent=arguments[2];
        var ct = new ContentType(type, subType);
        this.setHeader(ct);
        this.messageContent = messageContent;
        this.computeContentLength(messageContent);
    }
    else if(arguments.length==4)
    {
        content=arguments[0];
        var strict=arguments[1];
        var computeContentLength=arguments[2];
        var givenLength=arguments[3];
        this.computeContentLength(content);
        if ((!computeContentLength)) {
            if ( (!strict && this.contentLengthHeader.getContentLength() != givenLength) 
                || this.contentLengthHeader.getContentLength() < givenLength) {
                
                console.error("MessageFactoryImpl:setMessageContent(): invalid content length "+ this.contentLengthHeader.getContentLength() + " / " + givenLength, 0);
                throw "MessageFactoryImpl:setMessageContent(): invalid content length "+ this.contentLengthHeader.getContentLength() + " / " + givenLength;
            }
        }
        this.messageContent = content;
    }
}

SIPMessage.prototype.setContent =function(content, contentTypeHeader){
    if(logger!=undefined) logger.debug("SIPMessage:setContent():content,contentTypeHeader):"+content.classname+","+contentTypeHeader.classname);
    if (content == null)
    {
         console.error("MessageFactoryImpl:setContent(): null content !", 0);
         throw "MessageFactoryImpl:setContent(): null content!";
    }
    this.setHeader(contentTypeHeader);
    this.messageContent = content;
    this.computeContentLength(content);
}

SIPMessage.prototype.getContent =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getContent()");
    if (this.messageContent != null)
    {
        return this.messageContent;
    }
    else
    {
        return null;
    }
}

SIPMessage.prototype.computeContentLength =function(content){
    if(logger!=undefined) logger.debug("SIPMessage:computeContentLength():content="+content.length);
    var length = 0;
    if (content != null) {
        if (content instanceof String) {
            length = content.length;
        }
        else if (content.constructor==Array) {
            length = content.length;
        }
        else {
            length = content.toString().length;
        }
    }
    this.contentLengthHeader.setContentLength(length);

}

SIPMessage.prototype.removeContent =function(){
    if(logger!=undefined) logger.debug("SIPMessage:removeContent()");
    this.messageContent = null;
    this.contentLengthHeader.setContentLength(0);
}

SIPMessage.prototype.getHeaderAsFormattedString =function(name){
    if(logger!=undefined) logger.debug("SIPMessage:getHeaderAsFormattedString(): name="+name);
    var lowerCaseName = name.toLowerCase();
    var l = null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        if(this.nameTable[i][0]==lowerCaseName)
        {
            l=i;
        }
    }
    if (l!=null) 
    {
        return this.nameTable[l][1].toString();
    } 
    else 
    {
        return this.getHeader(name).toString();
    }
}

SIPMessage.prototype.getSIPHeaderListLowerCase =function(lowerCaseHeaderName){
    if(logger!=undefined) logger.debug("SIPMessage:getSIPHeaderListLowerCase():lowerCaseHeaderName="+lowerCaseHeaderName);
    var l = null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        if(this.nameTable[i][0]==lowerCaseHeaderName)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        return this.nameTable[l][1];
    }
    else
    {
        return null;
    }
    
}

SIPMessage.prototype.getHeaderList =function(headerName){
    if(logger!=undefined) logger.debug("SIPMessage:getHeaderList():headerName="+headerName);
    var l=null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        if(this.nameTable[i][0]==headerName.toLowerCase())
        {
            l=i;
        }
    }
    var sipHeader = this.nameTable[l][1];
    if (sipHeader == null)
    {
        return null;
    }
    else if (sipHeader instanceof SIPHeaderList)
    {
        return sipHeader.getHeaderList();
    //return (List<SIPHeader>) (((SIPHeaderList< ? >) sipHeader).getHeaderList());
    }
    else {
        var ll = new Array();
        ll.push(sipHeader);
        return ll;
    }
}

SIPMessage.prototype.hasHeader =function(headerName){
    if(logger!=undefined) logger.debug("SIPMessage:hasHeader():headerName="+headerName);
    var l=null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        if(this.nameTable[i][0]==headerName.toLowerCase())
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

SIPMessage.prototype.hasFromTag =function(){
    if(logger!=undefined) logger.debug("SIPMessage:hasFromTag()");
    return this.fromHeader != null && this.fromHeader.getTag() != null;
}

SIPMessage.prototype.hasToTag =function(){
    if(logger!=undefined) logger.debug("SIPMessage:hasToTag()");
    return this.toHeader != null && this.toHeader.getTag() != null;
}

SIPMessage.prototype.getFromTag =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getFromTag()");
    return this.fromHeader == null ? null : this.fromHeader.getTag();
}

SIPMessage.prototype.setFromTag =function(tag){
    if(logger!=undefined) logger.debug("SIPMessage:setFromTag():tag="+tag);
    this.fromHeader.setTag(tag);
}

SIPMessage.prototype.setToTag =function(tag){
    if(logger!=undefined) logger.debug("SIPMessage:setToTag():tag="+tag);
    this.toHeader.setTag(tag);
}

SIPMessage.prototype.getToTag =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getToTag()");
    return this.toHeader == null ? null : this.toHeader.getTag(); 
}

SIPMessage.prototype.getFirstLine =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getFirstLine()");

}
SIPMessage.prototype.addHeader =function(){
    if(logger!=undefined) logger.debug("SIPMessage:addHeader():arguments="+arguments);
    if(typeof arguments[0]!="object")
    {
        var sipHeader=arguments[0];
        var hdrString = sipHeader.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '') + "\n";
        try {
            var pf=new ParserFactory();
            var parser = pf.createParser(sipHeader);
            var sh = parser.parse();
            this.attachHeader(sh, false);
        } catch (ex) {
            console.error("SIPMessage:addHeader(): catched exception:"+ex);
            this.unrecognizedHeaders.push(hdrString);
        }
    }
    else
    {
        sipHeader=arguments[0];
        sh = sipHeader;
        try {
            if ((sipHeader instanceof Via) || (sipHeader instanceof RecordRoute)){
                this.attachHeader(sh, false, true);
            } 
            else{
                this.attachHeader(sh, false, false);
            }
        } catch (ex) {
            console.error("SIPMessage:addHeader(): catched exception:"+ex);
            if (sipHeader instanceof ContentLength){
                var cl = sipHeader;
                this.contentLengthHeader.setContentLength(cl.getContentLength());
            }
        } 
    }
}

SIPMessage.prototype.addUnparsed =function(unparsed){
    if(logger!=undefined) logger.debug("SIPMessage:addUnparsed():unparsed="+unparsed);
    this.unrecognizedHeaders.push(unparsed);
}

SIPMessage.prototype.getUnrecognizedHeaders =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getUnrecognizedHeaders()");
    return this.unrecognizedHeaders
}

SIPMessage.prototype.getHeaderNames =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getHeaderNames()");
    var retval=new Array();
    for(var i=0;i<this.headers.length;i++)
    {
        var sipHeader = this.headers[i];
        var name = sipHeader.getName();
        retval.push(name);
    }
    return retval;
}


SIPMessage.prototype.getContentDisposition =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getContentDisposition()");
    return this.getHeaderLowerCase(this.CONTENT_DISPOSITION_LOWERCASE)
}

SIPMessage.prototype.getExpires =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getExpires()");
    return this.getHeaderLowerCase(this.EXPIRES_LOWERCASE);
}

SIPMessage.prototype.setExpires =function(expiresHeader){
    if(logger!=undefined) logger.debug("SIPMessage:setExpires():expiresHeader="+expiresHeader.classname);
    this.setHeader(expiresHeader);
}

SIPMessage.prototype.setContentDisposition =function(contentDispositionHeader){
    if(logger!=undefined) logger.debug("SIPMessage:setContentDisposition():contentDispositionHeader="+contentDispositionHeader);
    this.setHeader(contentDispositionHeader);
}

SIPMessage.prototype.setContentLength =function(contentLength){
    if(logger!=undefined) logger.debug("SIPMessage:setContentLength():contentLength="+contentLength);
    this.contentLengthHeader.setContentLength(contentLength.getContentLength());
}

SIPMessage.prototype.setSize =function(size){
    if(logger!=undefined) logger.debug("SIPMessage:setSize():size="+size);
    this.size = size;
}

SIPMessage.prototype.getSize =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getSize()");
    return this.size;
}
SIPMessage.prototype.addLast =function(header){
    if(logger!=undefined) logger.debug("SIPMessage:addLast(): header="+header.classname);
    if (header == null)
    {
        console.error("SIPMessage:addLast(): null header arg!");
        throw "SIPMessage:addLast(): null header arg!"
    }
    this.attachHeader(header, false, false);
}

SIPMessage.prototype.addFirst =function(header){
    if(logger!=undefined) logger.debug("SIPMessage:addFirst():header="+header.classname);
    if (header == null)
    {
        console.error("SIPMessage:addFirst(): null header arg!");
        throw "SIPMessage:addFirst(): null header arg!"
    }
    this.attachHeader(header, false, true);
}

SIPMessage.prototype.removeFirst =function(headerName){
    if(logger!=undefined) logger.debug("SIPMessage:removeFirst():headerName="+headerName);
    if (headerName == null)
    {
        console.error("SIPMessage:removeFirst(): null headerName arg!");
        throw "SIPMessage:removeFirst(): null headerName arg!"
    }
    this.removeHeader(headerName, true);
}

SIPMessage.prototype.removeLast =function(headerName){
    if(logger!=undefined) logger.debug("SIPMessage:removeLast():headerName:"+headerName);
    if (headerName == null)
    {
        console.error("SIPMessage:removeLast(): null headerName arg!");
        throw "SIPMessage:removeLast(): null headerName arg!"
    }
    this.removeHeader(headerName, false);
}

SIPMessage.prototype.setCSeq =function(cseqHeader){
    if(logger!=undefined) logger.debug("SIPMessage:setCSeq():cseqHeader="+cseqHeader.classname);
    this.setHeader(cseqHeader);
}

SIPMessage.prototype.setApplicationData =function(applicationData){
    if(logger!=undefined) logger.debug("SIPMessage:setApplicationData():applicationData:"+applicationData.classname);
    this.applicationData = applicationData;
}

SIPMessage.prototype.getApplicationData =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getApplicationData()");
    return this.applicationData;
}

SIPMessage.prototype.getMultipartMimeContent =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getMultipartMimeContent()");
    var retval = new MultipartMimeContentImpl(this.getContentTypeHeader());
    var rawContent = this.getRawContent();
    var body = new String(rawContent);
    retval.createContentList(body);
    return retval;
}

SIPMessage.prototype.getCallIdHeader =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getCallIdHeader()");
    return this.callIdHeader;
}

SIPMessage.prototype.getFromHeader =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getFromHeader()");
    return this.fromHeader;
}

SIPMessage.prototype.getToHeader =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getToHeader()");
    return this.toHeader;
}

SIPMessage.prototype.getTopmostViaHeader =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getTopmostViaHeader()");
    return this.getTopmostVia();
}

SIPMessage.prototype.getCSeqHeader =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getCSeqHeader()");
    return this.cSeqHeader;
}

SIPMessage.prototype.getCharset =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getCharset()");
    var ct = this.getContentTypeHeader();
    if (ct!=null) {
        var c = ct.getCharset();
        return c!=null ? c : this.contentEncodingCharset;
    } 
    else {
        return this.contentEncodingCharset;
    }
}

SIPMessage.prototype.isNullRequest =function(){
    if(logger!=undefined) logger.debug("SIPMessage:isNullRequest()");
    return  this.nullRequest;
}

SIPMessage.prototype.setNullRequest =function(){
    if(logger!=undefined) logger.debug("SIPMessage:()");
    this.nullRequest = true;
}

SIPMessage.prototype.setSIPVersion =function(){
    if(logger!=undefined) logger.debug("SIPMessage:setSIPVersion()");
}

SIPMessage.prototype.getSIPVersion =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getSIPVersion()");
}

SIPMessage.prototype.toString =function(){
    if(logger!=undefined) logger.debug("SIPMessage:toString()");
}

SIPMessage.prototype.getBytes =function(str){
    if(logger!=undefined) logger.debug("SIPMessage:getBytes():str="+str);
    var array=new Array();
    str=new String(str);
    for(var i=0;i<str.length;i++)
    {
        array[i]=str.charCodeAt(i);
    } 
    return array;
}

SIPMessage.prototype.getStackTransaction =function(){
    if(logger!=undefined) logger.debug("SIPMessage:getStackTransaction()");
    return this.stackTransaction;
}

SIPMessage.prototype.setStackTransaction =function(transaction){
    if(logger!=undefined) logger.debug("SIPMessage:setStackTransaction():transaction="+transaction);
    this.stackTransaction = transaction;
}

SIPMessage.prototype.addViaHeaderList =function(viaheader){
    if(logger!=undefined) logger.debug("SIPMessage:addViaHeaderList():viaheader="+viaheader);
    var hn=new Array();
    var n=0;
    for(var i=0;i<this.headers.length;i++)
    {            
        hn[n]=this.headers[i];
        n=n+1;
        if ((this.headers[i] instanceof ViaList)) {
            if(i==this.headers.length-1||!(this.headers[i] instanceof ViaList))
            {
                hn[n]=viaheader;
                n=n+1;
            }
            if(!(this.headers[i-1] instanceof ViaList)&&i!=0)
            {
                var array=new Array();
                array[0]=this.headers[i].getName().toLowerCase();
                array[1]=this.headers[i];
                this.nameTable.push(array);
            }
        }
    }
    if(n==i)
    {
        hn[n]=viaheader;
        array=new Array();
        array[0]=viaheader.getName().toLowerCase();
        array[1]=viaheader;
        this.nameTable.push(array);
    }
    this.headers=hn;
}