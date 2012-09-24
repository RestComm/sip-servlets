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
 *  Implementation of the JAIN-SIP SIPHeader .
 *  @see  gov/nist/javax/sip/header/SIPHeader.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function SIPHeader(headername) {
    if(logger!=undefined) logger.debug("SIPHeader:SIPHeader()");
    this.serialVersionUID = "7749781076218987044L";
    this.classname="SIPHeader";
    this.headerName=null;
    if(headername!=null)
    {
        this.headerName=headername;
    }
}

SIPHeader.prototype.COLON=":";
SIPHeader.prototype.SP=" ";
SIPHeader.prototype.NEWLINE="\r\n";

SIPHeader.prototype.getHeaderName =function(){
    if(logger!=undefined) logger.debug("SIPHeader:getHeaderName()");
    return this.headerName;
}

SIPHeader.prototype.getName =function(){
    if(logger!=undefined) logger.debug("SIPHeader:getName()");
    return this.headerName;
}

SIPHeader.prototype.setHeaderName =function(hdrname){
    if(logger!=undefined) logger.debug("SIPHeader:setHeaderName():hdrname="+hdrname);
    this.headerName=hdrname;
}

SIPHeader.prototype.getHeaderValue =function(){
    if(logger!=undefined) logger.debug("SIPHeader:getHeaderValue()");
    var encodedHdr = null;
    encodedHdr = this.encode();
    var buffer = encodedHdr;
    while (buffer.length > 0 && buffer.charAt(0) != ':') {
        buffer=buffer.substring(1);
    }
    if (buffer.length > 0)
    {
        buffer=buffer.substring(1);
    }
    return buffer.toString().replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
}

SIPHeader.prototype.isHeaderList =function(){
    if(logger!=undefined) logger.debug("SIPHeader:isHeaderList()");
    return false;
}

SIPHeader.prototype.encode =function(){
    if(logger!=undefined) logger.debug("SIPHeader:encode()");
    return this.encodeBuffer("").toString();
}

SIPHeader.prototype.encodeBuffer =function(buffer){
    if(logger!=undefined) logger.debug("SIPHeader:encodeBuffer():buffer="+buffer);
    buffer=buffer+this.headerName+this.COLON+this.SP;
    buffer=this.encodeBodyBuffer(buffer);
    buffer=buffer+this.NEWLINE;
    return buffer;
}

SIPHeader.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("SIPHeader:encodeBody()");
}

SIPHeader.prototype.encodeBodyBuffer =function(buffer){
    if(logger!=undefined) logger.debug("SIPHeader:encodeBodyBuffer():buffer="+buffer);
    buffer=buffer+this.encodeBody();
    return buffer;
}

SIPHeader.prototype.getValue =function(){
    if(logger!=undefined) logger.debug("SIPHeader:getValue()");
    return this.getHeaderValue();
}

SIPHeader.prototype.hashCode =function(){
    if(logger!=undefined) logger.debug("SIPHeader:hashCode()");
    var hash = 0;
    var x=this.headerName;
    if(!(x == null || x.value == ""))  
    {  
        for (var i = 0; i < x.length; i++)  
        {  
            hash = hash * 31 + x.charCodeAt(i);  
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

SIPHeader.prototype.toString =function(){
    if(logger!=undefined) logger.debug("SIPHeader:toString()");
    return this.encode();
}

