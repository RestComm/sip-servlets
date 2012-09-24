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
 *  Implementation of the JAIN-SIP ContentType .
 *  @see  gov/nist/javax/sip/header/ContentType.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ContentType(contentType,contentSubtype) {
    if(logger!=undefined) logger.debug("ContentType:ContentType()");
    this.serialVersionUID = "8475682204373446610L";
    this.classname="ContentType";
    this.mediaRange=new MediaRange();
    this.headerName=this.NAME;
    this.parameters = new NameValueList();
    this.duplicates = new DuplicateNameValueList();
    if(contentType!=null&&contentSubtype!=null)
    {
        this.setContentType(contentType, contentSubtype);
    }
}

ContentType.prototype = new ParametersHeader();
ContentType.prototype.constructor=ContentType;
ContentType.prototype.NAME="Content-Type";
ContentType.prototype.SEMICOLON=";";

ContentType.prototype.compareMediaRange =function(media){
    if(logger!=undefined) logger.debug("ContentType:compareMediaRange():media="+media);
    var chaine1=(mediaRange.type + "/" + mediaRange.subtype).toLowerCase();
    var chaine2=media.toLowerCase();
    var c=0;
    var length;
    if(chaine1.length>=chaine2.length)
    {
        length=chaine1.length
    }
    else
    {
        length=chaine2.length;
    }
    for(var i=0;i<length;i++)
    {
        if(chaine1.charAt(i)==null)
        {}
        else if(chaine1.charAt(i)>chaine2.charAt(i))
        {
            c=c+1;
        }
        else if(chaine1.charAt(i)<chaine2.charAt(i))
        {
            c=c-1;
        }
    }
    return c;
}

ContentType.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("ContentType:encodeBody()");
    return this.encodeBodyBuffer("").toString();
}

ContentType.prototype.encodeBodyBuffer =function(buffer){
    if(logger!=undefined) logger.debug("ContentType:encodeBodyBuffer():buffer:"+buffer);
    buffer=this.mediaRange.encodeBuffer(buffer);
    if (this.hasParameters()) {
        buffer=buffer+this.SEMICOLON;
        buffer=this.parameters.encodeBuffer(buffer);
    }
    return buffer;
}

ContentType.prototype.getMediaRange =function(){
    if(logger!=undefined) logger.debug("ContentType:getMediaRange()");
    return this.mediaRange;
}

ContentType.prototype.getMediaType =function(){
    if(logger!=undefined) logger.debug("ContentType:getMediaType()");
    return this.mediaRange.type;
}

ContentType.prototype.getMediaSubType =function(){
    if(logger!=undefined) logger.debug("ContentType:getMediaSubType()");
    return this.mediaRange.subtype;
}

ContentType.prototype.getContentSubType =function(){
    if(logger!=undefined) logger.debug("ContentType:getContentSubType()");
    return this.mediaRange == null ? null : this.mediaRange.getSubtype();
}

ContentType.prototype.getContentType =function(){
    if(logger!=undefined) logger.debug("ContentType:getContentType()");
    return this.mediaRange == null ? null : this.mediaRange.getType();
}

ContentType.prototype.getCharset =function(){
    if(logger!=undefined) logger.debug("ContentType:getCharset()");
    return this.getParameter("charset");
}

ContentType.prototype.setMediaRange =function(m){
    if(logger!=undefined) logger.debug("ContentType:setMediaRange():m"+m);
    this.mediaRange = m;
}

ContentType.prototype.setContentType =function(){
    if(logger!=undefined) logger.debug("ContentType:setContentType():arguments="+arguments.toString());
    if(arguments==1)
    {
        var contentType=arguments[0];
        if (contentType == null)
        {
            console.error("ContentType:setContentType(): null argument");
            throw "ContentType:setContentType(): null argument";
        }
        if (this.mediaRange == null)
        {
            this.mediaRange = new MediaRange();
        }
        this.mediaRange.setType(contentType);
    }
    else
    {
        contentType=arguments[0];
        var contentSubType=arguments[1];
        if (this.mediaRange == null)
        {
            this.mediaRange = new MediaRange();
        }
        this.mediaRange.setType(contentType);
        this.mediaRange.setSubtype(contentSubType);
    }
}

ContentType.prototype.setContentSubType =function(contentType){
    if(logger!=undefined) logger.debug("ContentType:setContentSubType():contentType="+contentType);
    if (contentType == null)
    {
          console.error("ContentType:setContentSubType(): null contentType parameter");
            throw "ContentType:setContentSubType(): null contentType parameter";
    }
    if (this.mediaRange == null)
    {
        this.mediaRange = new MediaRange();
    }
    this.mediaRange.setSubtype(contentType);
}