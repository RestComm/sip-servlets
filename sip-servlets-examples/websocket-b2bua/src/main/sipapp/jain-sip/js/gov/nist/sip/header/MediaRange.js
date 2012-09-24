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
 *  Implementation of the JAIN-SIP MediaRange .
 *  @see  gov/nist/javax/sip/header/MediaRange.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function MediaRange() {
    if(logger!=undefined) logger.debug("MediaRange:MediaRange()");
    this.serialVersionUID = "-6297125815438079210L";
    this.classname="MediaRange";
    this.type=null;
    this.subtype=null;
}

MediaRange.prototype = new SIPObject();
MediaRange.prototype.constructor=MediaRange;
MediaRange.prototype.SLASH="/"

MediaRange.prototype.getType =function(){
    if(logger!=undefined) logger.debug("MediaRange:getType()");
    return this.type;
}

MediaRange.prototype.getSubtype =function(){
    if(logger!=undefined) logger.debug("MediaRange:getSubtype()");
    return this.subtype;
}

MediaRange.prototype.setType =function(t){
    if(logger!=undefined) logger.debug("MediaRange:setType():t="+t);
    this.type=t;
}

MediaRange.prototype.setSubtype =function(s){
    if(logger!=undefined) logger.debug("MediaRange:setSubtype():s="+s);
    this.subtype=s;
}

MediaRange.prototype.encode =function(){
    if(logger!=undefined) logger.debug("MediaRange:encode()");
    return this.encodeBuffer("").toString();
}

MediaRange.prototype.encodeBuffer =function(buffer){
    if(logger!=undefined) logger.debug("MediaRange:encodeBuffer():buffer="+buffer);
    buffer=buffer+this.type+this.SLASH+this.subtype;
    return buffer;
}