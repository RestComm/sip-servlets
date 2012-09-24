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
 *  Implementation of the JAIN-SIP ContentImpl .
 *  @see  gov/nist/javax/sip/message/ContentImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function ContentImpl(content,boundary) {
    if(logger!=undefined) logger.debug("ContentImpl:ContentImpl(): content="+content+", boundary="+boundary);
    this.classname="ContentImpl";
    this.content = content;
    this.boundary = boundary;
    this.contentTypeHeader=null;
    this.contentDispositionHeader=null;
}

ContentImpl.prototype.setContent =function(content){
    if(logger!=undefined) logger.debug("ContentImpl:setContent():content="+content);
    this.content = content;
}

ContentImpl.prototype.getContentTypeHeader =function(){
    if(logger!=undefined) logger.debug("ContentImpl:getContentTypeHeader()");
    return this.contentTypeHeader;
}

ContentImpl.prototype.getContent =function(){
    if(logger!=undefined) logger.debug("ContentImpl:getContent()");
    return this.content;
}

ContentImpl.prototype.toString =function(){
    if(logger!=undefined) logger.debug("ContentImpl:toString()");
    if (this.boundary == null) 
    {
        return this.content.toString();
    } 
    else 
    {
        if ( this.contentDispositionHeader != null ) 
        {
            return "--" + this.boundary + "\r\n" + this.getContentTypeHeader() + 
            this.getContentDispositionHeader().toString() + "\r\n" + this.content.toString();
        } 
        else 
        {
            return "--" + this.boundary + "\r\n" + this.getContentTypeHeader() + "\r\n" +  this.content.toString();
        }
    }
}

ContentImpl.prototype.setContentDispositionHeader =function(contentDispositionHeader){
    if(logger!=undefined) logger.debug("ContentImpl:setContentDispositionHeader():contentDispositionHeader="+contentDispositionHeader);
    this.contentDispositionHeader = contentDispositionHeader;
}

ContentImpl.prototype.getContentDispositionHeader =function(){
    if(logger!=undefined) logger.debug("ContentImpl:getContentDispositionHeader()");
    return this.contentDispositionHeader;
}

ContentImpl.prototype.setContentTypeHeader =function(contentTypeHeader){
    if(logger!=undefined) logger.debug("ContentImpl:setContentTypeHeader():contentTypeHeader="+contentTypeHeader);
    this.contentTypeHeader = contentTypeHeader;
}
