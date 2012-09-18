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
 *  Implementation of the JAIN-SIP address.
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *  @see  gov/nist/javax/sip/address/AddressImpl.java 
 *   
 */
function AddressImpl() {
    if(logger!=undefined) logger.debug("AddressImpl:AddressImpl()");
    this.serialVersionUID = "429592779568617259L";
    this.classname="AddressImpl";
    this.addressType =1;
    this.displayName=null;
    this.address = new GenericURI();
}

AddressImpl.prototype.NAME_ADDR =1;
AddressImpl.prototype.ADDRESS_SPEC=2;
AddressImpl.prototype.WILD_CARD = 3;
AddressImpl.prototype.DOUBLE_QUOTE="\"";
AddressImpl.prototype.SP=" ";
AddressImpl.prototype.LESS_THAN="<";
AddressImpl.prototype.GREATER_THAN=">";

AddressImpl.prototype.match =function(other){
    if(logger!=undefined) logger.debug("AddressImpl:match():other="+other);
    if (other == null)
    {
        return true;
    }
    if (!(other instanceof AddressImpl))
    {
        return false;
    }
    else {
        var that = other;
        if (that.getMatcher() != null)
        {
            if(that.getMatcher().match(this.encode()))
            {
                return true
            }
            else
            {
                return false;
            }
        }
        else if (that.displayName != null && this.displayName == null)
        {
            return false;
        }
        else if (that.displayName == null)
        {
            if(this.address.match(that.address))
            {
                return true
            }
            else
            {
                return false;
            }
        }
        else
        {
            if(this.displayName.toLowerCase()==that.displayName
                && this.address.match(that.address))
                {
                return true
            }
            else
            {
                return false;
            }
        }
    }
}

AddressImpl.prototype.getHostPort =function(){
    if(logger!=undefined) logger.debug("AddressImpl:getHostPort()");
    if (!(this.address instanceof SipUri))
    {
        console.error("AddressImpl:getHostPort(): address is not a SipUri");
        throw "AddressImpl:getHostPort(): address is not a SipUri";
    }
    var uri = this.address;
    return uri.getHostPort();
}

AddressImpl.prototype.getPort =function(){
    if(logger!=undefined) logger.debug("AddressImpl:getPort()");
    if (!(this.address instanceof SipUri))
    {
        console.error("AddressImpl:getPort(): address is not a SipUri");
        throw "AddressImpl:getPort(): address is not a SipUri";
    }
    var uri = this.address;
    return uri.getHostPort().getPort();
}

AddressImpl.prototype.getUserAtHostPort =function(){
    if(logger!=undefined) logger.debug("AddressImpl:getUserAtHostPort()");
    if (this.address instanceof SipUri) {
        var uri = this.address;
        return uri.getUserAtHostPort();
    } 
    else
    {
        return this.address;
    }
}

AddressImpl.prototype.getHost =function(){
    if(logger!=undefined) logger.debug("AddressImpl:getHost()");
    if (!(this.address instanceof SipUri))
    {
        console.error("AddressImpl:getHost(): address is not a SipUri");
        throw "AddressImpl:getHost(): address is not a SipUri";
    }
    var uri = this.address;
    return uri.getHostPort().getHost().getHostname();
}

AddressImpl.prototype.removeParameter =function(parameterName){
    if(logger!=undefined) logger.debug("AddressImpl:removeParameter():parameterName="+parameterName);
    if (!(this.address instanceof SipUri))
    {
        console.error("AddressImpl:removeParameter(): address is not a SipUri");
        throw "AddressImpl:removeParameter(): address is not a SipUri";
    }
    var uri = this.address;
    uri.removeParameter(parameterName);
}

AddressImpl.prototype.encode =function(){
    if(logger!=undefined) logger.debug("AddressImpl:encode()");
    return this.encodeBuffer("");
}

AddressImpl.prototype.encodeBuffer =function(buffer){
    if(logger!=undefined) logger.debug("AddressImpl:encodeBuffer():buffer="+buffer);
    if (this.addressType == this.WILD_CARD) {
        buffer=buffer+"*";
    }
    else {
        if (this.displayName != null) {
            buffer=buffer+this.DOUBLE_QUOTE+this.displayName+this.DOUBLE_QUOTE+this.SP;
        }
        if (this.address != null) {
            if (this.addressType == this.NAME_ADDR || this.displayName != null)
            {
                buffer=buffer+this.LESS_THAN;
            }
            buffer=this.address.encodeBuffer(buffer);
            if (this.addressType == this.NAME_ADDR || this.displayName != null)
            {
                buffer=buffer+this.GREATER_THAN;
            }
        }
    }
    return buffer;
}

AddressImpl.prototype.getAddressType =function(){
    if(logger!=undefined) logger.debug("AddressImpl:getAddressType()");
    return this.addressType;
}

AddressImpl.prototype.setAddressType =function(atype){
    if(logger!=undefined) logger.debug("AddressImpl:setAddressType():atype="+atype);
    this.addressType = atype;
}

AddressImpl.prototype.getDisplayName =function(){
    if(logger!=undefined) logger.debug("AddressImpl:getDisplayName()");
    return this.displayName;
}

AddressImpl.prototype.setDisplayName =function(displayName){
    if(logger!=undefined) logger.debug("AddressImpl:setDisplayName():displayName="+displayName);
    this.displayName = displayName;
    this.addressType = this.NAME_ADDR;
}

AddressImpl.prototype.setAddress =function(address){
    if(logger!=undefined) logger.debug("AddressImpl:setAddress():address="+address);
    this.address = address;
}

AddressImpl.prototype.hashCode =function(){
    if(logger!=undefined) logger.debug("AddressImpl:hashCode()");
    var hash = 0;
    var x=this.address;
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

AddressImpl.prototype.equals =function(other){
    if(logger!=undefined) logger.debug("AddressImpl:equals():other="+other);
    if (this==other) {
        return true;
    }
    if (other instanceof AddressImpl) {
        var o = other;

        // Don't compare display name (?)
        if(this.getURI()== o.getURI() )
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    return false;
}

AddressImpl.prototype.hasDisplayName =function(){
    if(logger!=undefined) logger.debug("AddressImpl:hasDisplayName()");
    if(this.displayName != null)
    {
        return true;
    }
    else
    {
        return false;
    }
}

AddressImpl.prototype.removeDisplayName =function(){
    if(logger!=undefined) logger.debug("AddressImpl:removeDisplayName()");
    this.displayName = null;
}

AddressImpl.prototype.isSIPAddress =function(){
    if(logger!=undefined) logger.debug("AddressImpl:isSIPAddress()");
    if(address instanceof SipUri)
    {
        return true;
    }
    else
    {
        return false;
    }
}

AddressImpl.prototype.getURI =function(){
    if(logger!=undefined) logger.debug("AddressImpl:getURI()");
 
    return this.address;
}
AddressImpl.prototype.isWildcard =function(){
    if(logger!=undefined) logger.debug("AddressImpl:isWildcard()");
    if(this.addressType == this.WILD_CARD)
    {
        return true;
    }
    else
    {
        return false;
    }
}

AddressImpl.prototype.setURI=function(address){
    if(logger!=undefined) logger.debug("AddressImpl:setURI():address="+address);
    this.address = address;
    
}

AddressImpl.prototype.setUser=function(user){
    if(logger!=undefined) logger.debug("AddressImpl:setUser():user="+user);
    if(this.address instanceof SipUri)
    {
        this.address.setUser(user);
    }
    else
    {
        this.address = new SipUri();
        this.address.setUser(user);
    }
}

AddressImpl.prototype.setWildCardFlag=function(){
    if(logger!=undefined) logger.debug("AddressImpl:setWildCardFlag()");
    this.addressType = this.WILD_CARD;
    this.address = new SipUri();
    this.address.setUser("*");
}

