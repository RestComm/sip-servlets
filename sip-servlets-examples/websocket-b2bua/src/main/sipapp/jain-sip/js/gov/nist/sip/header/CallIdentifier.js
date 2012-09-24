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
 *  Implementation of the JAIN-SIP CallIdentifier .
 *  @see  gov/nist/javax/sip/header/CallIdentifier.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function CallIdentifier() {
    if(logger!=undefined) logger.debug("CallIdentifier");
    this.serialVersionUID = "7314773655675451377L";
    this.classname="CallIdentifier";
    this.localId=null;
    this.host=null;
    if(arguments.length==2)
    {
        var localId=arguments[0];
        var host=arguments[1];
        this.localId=localId;
        this.host=host;
    }
    else if(arguments.length==1)
    {
        var cid=arguments[0];
        this.setCallID(cid);
    }
}

CallIdentifier.prototype = new SIPObject();
CallIdentifier.prototype.constructor=CallIdentifier;
CallIdentifier.prototype.AT="@";

CallIdentifier.prototype.encode =function(){
    if(logger!=undefined) logger.debug("CallIdentifier:encode()");
    return this.encodeBuffer("").toString();
}

CallIdentifier.prototype.encodeBuffer =function(buffer){
    if(logger!=undefined) logger.debug("CallIdentifier:encodeBuffer():buffer="+buffer);
    buffer=buffer+this.localId;
    if (this.host != null) {
        buffer=buffer+this.AT+this.host;
    }
    return buffer;
}

CallIdentifier.prototype.hashCode =function(){
    if(logger!=undefined) logger.debug("CallIdentifier:hashCode():");
    if (this.localId  == null ) 
    {
        console.error("CallIdentifier:hashCode(): hash code called before id is set");
        throw "CallIdentifier:hashCode(): hash code called before id is set"
    }
    var hash = 0;
    var x=this.localId.toLowerCase();
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

CallIdentifier.prototype.getLocalId =function(){
    if(logger!=undefined) logger.debug("CallIdentifier:getLocalId()");
    return this.localId;
}

CallIdentifier.prototype.getHost =function(){
    if(logger!=undefined) logger.debug("CallIdentifier:getHost()");
    return this.host;
}

CallIdentifier.prototype.setLocalId =function(localId){
    if(logger!=undefined) logger.debug("CallIdentifier:setLocalId():localId="+localId);
    this.localId=localId;
}
CallIdentifier.prototype.setCallID =function(cid){
    if(logger!=undefined) logger.debug("CallIdentifier:setCallID():cid="+cid);
    if (cid == null)
    {
        console.error("CallIdentifier:setCallID(): cid parameter is null");
        throw "CallIdentifier:setCallID(): cid parameter is null";
    }
    var index = cid.indexOf('@');
    if (index == -1) {
        this.localId = cid;
        this.host = null;
    } 
    else 
    {
        this.localId = cid.substring(0, index);
        this.host = cid.substring(index + 1, cid.length);
        if (this.localId == null || this.host == null) 
        {
            console.error("CallIdentifier:setCallID(): CallID  must be token@token or token");
            throw "CallIdentifier:setCallID(): CallID  must be token@token or token";
            
        }
    }
}

CallIdentifier.prototype.setHost =function(host){
    if(logger!=undefined) logger.debug("CallIdentifier:setHost():host="+host);
    this.host=host;
}