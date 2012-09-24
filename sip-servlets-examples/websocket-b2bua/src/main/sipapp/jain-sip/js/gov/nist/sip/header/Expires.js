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
 *  Implementation of the JAIN-SIP Expires .
 *  @see  gov/nist/javax/sip/header/Expires.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Expires() {
    if(logger!=undefined) logger.debug("Expires:Expires()");
    this.serialVersionUID = "3134344915465784267L";
    this.classname="Expires";
    this.headerName=this.NAME;
    this.expires=null;
}

Expires.prototype = new SIPHeader();
Expires.prototype.constructor=Expires;
Expires.prototype.NAME="Expires";

Expires.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("Expires:encodeBody()");
    return this.encodeBodyBuffer("").toString();
}

Expires.prototype.encodeBodyBuffer =function(buffer){
    if(logger!=undefined) logger.debug("Expires:encodeBodyBuffer():buffer="+buffer);
    buffer=buffer+this.expires;
    return buffer;
}

Expires.prototype.getExpires =function(){
    if(logger!=undefined) logger.debug("Expires:getExpires()");
    return this.expires;
}

Expires.prototype.setExpires =function(expires){
    if(logger!=undefined) logger.debug("Expires:setExpires():expires="+expires);
    if (expires < 0)
    {
        console.error("Expires:setExpires(): bad argument " + expires);
        throw "Expires:setExpires(): bad argument " + expires;
    }
    this.expires = expires;
}