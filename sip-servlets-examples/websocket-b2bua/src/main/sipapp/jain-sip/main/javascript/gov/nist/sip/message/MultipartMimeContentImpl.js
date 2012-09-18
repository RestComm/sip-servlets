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
 *  Implementation of the JAIN-SIP MultipartMimeContentImpl .
 *  @see  gov/nist/javax/sip/message/MultipartMimeContentImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function MultipartMimeContentImpl(contentTypeHeader) {
    if(logger!=undefined) logger.debug("MultipartMimeContentImpl");
    this.classname="MultipartMimeContentImpl";
    this.contentList = new Array();
    this.multipartMimeContentTypeHeader=contentTypeHeader;
    this.boundary=contentTypeHeader.getParameter(this.BOUNDARY);
}

MultipartMimeContentImpl.prototype.BOUNDARY="boundary";

MultipartMimeContentImpl.prototype.add =function(content){
    if(logger!=undefined) logger.debug("MultipartMimeContentImpl:add():content="+content);
    var length=this.contentList.length;
    this.contentList.push(content);
    if(length!=this.contentList.length)
    {
        return true;
    }
    else
    {
        return false;
    }
}

MultipartMimeContentImpl.prototype.getContentTypeHeader =function(){
    if(logger!=undefined) logger.debug("MultipartMimeContentImpl:getContentTypeHeader()");
    return this.multipartMimeContentTypeHeader;
}

MultipartMimeContentImpl.prototype.toString =function(){
    if(logger!=undefined) logger.debug("MultipartMimeContentImpl:toString()");
    var stringBuffer = "";
    for (var i=0;i<this.contentList.length;i++) {
        stringBuffer=stringBuffer+this.contentList[i].toString();
    }
    return stringBuffer.toString();
}

MultipartMimeContentImpl.prototype.createContentList =function(body){
    if(logger!=undefined) logger.debug("MultipartMimeContentImpl:createContentList():body="+body);
    var headerFactory = new HeaderFactoryImpl();
    var delimiter = this.getContentTypeHeader().getParameter(this.BOUNDARY);
    if (delimiter == null) 
    {
        this.contentList = new Array();
        var content = new ContentImpl(body, delimiter);
        content.setContentTypeHeader(this.getContentTypeHeader());
        this.contentList.push(content);
        return;
    }
    var fragments = body.split("--" + delimiter + "\r\n");
    for(var i=0;i<fragments.length;i++)
    {
        var nextPart=fragments[i];
        if (nextPart == null) {
            return;
        }
        var strbuf = nextPart;
        while (strbuf.length() > 0&& (strbuf.charAt(0) == '\r' || strbuf.charAt(0) == '\n'))
        {
            strbuf=strbuf.substring(1);
        }
        if (strbuf.length() == 0)
        {
            continue;
        }
        nextPart = strbuf.toString();
        var position = nextPart.indexOf("\r\n\r\n");
        var off = 4;
        if (position == -1) {
            position = nextPart.indexOf("\n");
            off = 2;
        }
        if (position == -1)
        {
            console.error("MessageFactoryImpl:createContentList(): no content type header found in " + nextPart);
            throw "MessageFactoryImpl:createContentList(): no content type header found in " + nextPart;
        }
        var rest = nextPart.substring(position + off);
        if (rest == null)
        {
            console.error("MessageFactoryImpl:createContentList(): no content [" + nextPart + "]");
            throw "MessageFactoryImpl:createContentList(): no content [" + nextPart + "]";
        }

        var headers = nextPart.substring(0, position);
        content = new ContentImpl(rest, this.boundary);
        var headerArray = headers.split("\r\n");
        for(var t=0;t<headerArray.length;t++)
        {
            var hdr=headerArray[t];
            var header = headerFactory.createHeader(hdr);
            if (header instanceof ContentTypeHeader) 
            {
                content.setContentTypeHeader(header);
            } 
            else if (header instanceof ContentDispositionHeader) 
            {
                content.setContentDispositionHeader(header);
            } 
            else 
            {
                console.error("MessageFactoryImpl:createContentList(): unexpected header type " + header.getName());
                throw "MessageFactoryImpl:createContentList(): unexpected header type " + header.getName();
            }
            this.contentList.push(content);   
        }
    }
}

MultipartMimeContentImpl.prototype.getContentByType =function(contentType, contentSubtype){
    if(logger!=undefined) logger.debug("MultipartMimeContentImpl:getContentByType(): contentType="+contentType+",contentSubtype="+contentSubtype);
    var retval = null;
    if (contentList == null)
    {
        return null;
    }
    for (var i=0;i<this.contentList.length;i++) {
        var content=this.contentList[i];
        if (content.getContentTypeHeader().getContentType().toLowerCase()==contentType
            && content.getContentTypeHeader().getContentSubType().toLowerCase()==contentSubtype)
            {
            retval = content;
            break;
        }
    }
    return retval;
}

MultipartMimeContentImpl.prototype.addContent =function(content){
    if(logger!=undefined) logger.debug("MultipartMimeContentImpl:addContent()");
    this.add(content);
}

MultipartMimeContentImpl.prototype.getContents =function(){
    if(logger!=undefined) logger.debug("MultipartMimeContentImpl:getContents()");
    return this.contentList;
}

MultipartMimeContentImpl.prototype.getContentCount =function(){
    if(logger!=undefined) logger.debug("MultipartMimeContentImpl:getContentCount()");
    return this.contentList.length;
}
