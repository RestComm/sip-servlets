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
 *  Implementation of the JAIN-SIP Protocol .
 *  @see  gov/nist/javax/sip/header/Protocol.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Protocol() {
    if(logger!=undefined) logger.debug("Protocol:Protocol()");
    this.serialVersionUID = "2216758055974073280L";
    this.classname="Protocol";
    this.protocolName="SIP";
    this.protocolVersion="2.0";
    this.transport="WS";
}

Protocol.prototype = new SIPObject();
Protocol.prototype.constructor=Protocol;
Protocol.prototype.SLASH="/";

Protocol.prototype.encode =function(){
    if(logger!=undefined) logger.debug("Protocol:encode()");
    return this.encodeBuffer("").toString();
}

Protocol.prototype.encodeBuffer =function(buffer){
    if(logger!=undefined) logger.debug("Protocol:encodeBuffer():buffer="+buffer);
    buffer=buffer+this.protocolName.toUpperCase()+this.SLASH+this.protocolVersion+this.SLASH+this.transport.toUpperCase();
    return buffer;
}

Protocol.prototype.getProtocolName =function(){
    if(logger!=undefined) logger.debug("Protocol:getProtocolName()");
    return this.protocolName;
}

Protocol.prototype.getProtocolVersion =function(){
    if(logger!=undefined) logger.debug("Protocol:getProtocolVersion()");
    return this.protocolVersion;
}

Protocol.prototype.getProtocol =function(){
    if(logger!=undefined) logger.debug("Protocol:getProtocol()");
    return this.protocolName + "/" + this.protocolVersion;
}

Protocol.prototype.setProtocol =function(name_and_version){
    if(logger!=undefined) logger.debug("Protocol:setProtocol():name_and_version="+name_and_version);
    var slash = name_and_version.indexOf('/');
    if (slash>0) {
        this.protocolName = name_and_version.substring(0,slash);
        this.protocolVersion = name_and_version.substring( slash+1 );
    } 
    else 
    {
        console.error("Event:setProtocol(): missing '/' in protocol", 0 );
        throw "Event:setProtocol(): missing '/' in protocol";
    }
}

Protocol.prototype.getTransport =function(){
    if(logger!=undefined) logger.debug("Protocol:getTransport()");
    return this.transport;
}

Protocol.prototype.setProtocolName =function(pn){
    if(logger!=undefined) logger.debug("Protocol:setProtocolName():pn="+pn);
    this.protocolName=pn;
}

Protocol.prototype.setProtocolVersion =function(p){
    if(logger!=undefined) logger.debug("Protocol:setProtocolVersion():p="+p);
    this.protocolVersion=p
}

Protocol.prototype.setTransport =function(t){
    if(logger!=undefined) logger.debug("Protocol:setTransport(): t="+t);
    this.transport=t;
}
