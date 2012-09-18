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
 *  Implementation of the JAIN-SIP CallID .
 *  @see  gov/nist/javax/sip/header/CallID.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function CallID() {
    if(logger!=undefined) logger.debug("CallID:CallID()");
    this.serialVersionUID = "-6463630258703731156L;";
    this.classname="CallID";
    this.headerName=this.NAME;
    this.callIdentifier=new CallIdentifier();
    if(arguments.length!=0)
    {
        var callId=arguments[0];
        this.callIdentifier = new CallIdentifier(callId);
    }
}

CallID.prototype = new SIPHeader();
CallID.prototype.constructor=CallID;
CallID.prototype.NAME="Call-ID";

CallID.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("CallID:encodeBody()");
    return this.encodeBodyBuffer("").toString();
}

CallID.prototype.encodeBodyBuffer =function(buffer){
    if(logger!=undefined) logger.debug("CallID:encodeBodyBuffer():buffer="+buffer);
    if (this.callIdentifier != null)
    {
        buffer=this.callIdentifier.encodeBuffer(buffer);
    }
    return buffer;
}

CallID.prototype.getCallId =function(){
    if(logger!=undefined) logger.debug("CallID:getCallId()");
    return this.encodeBody();
}

CallID.prototype.getCallIdentifer =function(){
    if(logger!=undefined) logger.debug("CallID:getCallIdentifer()");
    return this.callIdentifier;
}

CallID.prototype.setCallId =function(cid){
    if(logger!=undefined) logger.debug("CallID:setCallId():cid="+cid);
    this.callIdentifier = new CallIdentifier(cid);
}

CallID.prototype.setCallIdentifier =function(cid){
    if(logger!=undefined) logger.debug("CallID:setCallIdentifier():cid="+cid);
    this.callIdentifier=cid;
}