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
 *  Implementation of the JAIN-SIP Host class.
 *  @see  gov/nist/core/Host.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function Host(hn,addresstype) {
    if(logger!=undefined) logger.debug("Host:Host(): hn="+hn+" addresstype="+addresstype);
    this.serialVersionUID = "-7233564517978323344L";
    this.stripAddressScopeZones = false;
    this.hostname = null;
    this.addressType=null;
    this.inetAddress = null;
    this.classname="Host"; 
    if(hn==null&&addresstype==null)
    {
        this.addressType = this.HOSTNAME;
    }
    else if(addresstype==null&&hn!=null)
    {
        this.setHost(hn, this.IPV4ADDRESS);
    }
    else
    {
        this.setHost(hn, addresstype);
    }
}

Host.prototype = new GenericObject();
Host.prototype.constructor=Host;
Host.prototype.HOSTNAME = 1;
Host.prototype.IPV4ADDRESS = 2;
Host.prototype.IPV6ADDRESS = 3;

Host.prototype.encode =function(){
    //if(logger!=undefined) logger.debug("Host:encode()");
    return this.encodeBuffer("").toString();
}

Host.prototype.encodeBuffer =function(buffer){
    //if(logger!=undefined) logger.debug("Host:encodeBuffer(): buffer="+buffer);
    var encode=null;
    if (this.addressType == this.IPV6ADDRESS && !this.isIPv6Reference(this.hostname)) {
        encode=buffer+"["+this.hostname+"]";
    } else {
        encode=buffer+this.hostname;
    }
    return encode;
}

/**
     * Compare for equality of hosts.
     * Host names are compared by textual equality. No dns lookup
     * is performed.
     * @param obj Object to set
     * @return boolean
     */
Host.prototype.equals =function(obj){
    //if(logger!=undefined) logger.debug("Host:equals():obj="+obj);
    if ( obj == null ) 
    {
        return false;
    }
    if (this.classname!=obj.classname) {
        return false;
    }
    var otherHost = new Host();
    if(otherHost.hostname==this.hostname)
    {
        return true;
    }
    else
    {
        return false;
    }
}

Host.prototype.getHostname =function(){
    //if(logger!=undefined) logger.debug("Host:getHostname()");
    return this.hostname;
}

Host.prototype.getAddress =function(){
    //if(logger!=undefined) logger.debug("Host:getAddress()");
    return this.hostname;
}

Host.prototype.getIpAddress =function(){////////////////////////////////////////problem dans cette méthode
    //if(logger!=undefined) logger.debug("Host:getIpAddress()");
    var rawIpAddress = null;
    if (this.hostname == null)
        return null;
    if (this.addressType == this.HOSTNAME) {
        if (this.inetAddress == null)
            // CAN NOT BE IMPLEMENTED
        /*this.inetAddress = InetAddress.getByName(hostname);
        rawIpAddress = inetAddress.getHostAddress();*/;
    } else {
        rawIpAddress = this.hostname;
    }
    return rawIpAddress;
}


Host.prototype.setHostname =function(h){
    //if(logger!=undefined) logger.debug("Host:setHostname():h="+h);
    this.setHost(h, this.HOSTNAME);
}

Host.prototype.setHostAddress =function(address){
    //if(logger!=undefined) logger.debug("Host:setHostAddress():address="+address);
    this.setHost(address, this.IPV4ADDRESS);
}

Host.prototype.setHost =function(host,type){
    //if(logger!=undefined) logger.debug("Host:setHost():host="+host);
    //if(logger!=undefined) logger.debug("Host:setHost():type="+type);
    this.inetAddress = null;
    if(type==null)
    {
        type=2;
    }
    if (this.isIPv6Address(host))
    {
        this.addressType = this.IPV6ADDRESS;
    }
    else
    {
        this.addressType = type;
    }
    if (host != null){
        this.hostname=host.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
        if(this.addressType == this.HOSTNAME)
        {
            this.hostname = this.hostname.toLowerCase();
        }
        var zoneStart = -1;
        if(this.addressType == this.IPV6ADDRESS
            && this.stripAddressScopeZones
            && (zoneStart = this.hostname.indexOf('%'))!= -1)
            {
            this.hostname = this.hostname.substring(0, zoneStart);
        }
    }
}

Host.prototype.setAddress =function(address){
    i//f(logger!=undefined) logger.debug("Host:setAddress():address="+address);
    this.setHostAddress(address);
}

Host.prototype.isHostname =function(){
    //if(logger!=undefined) logger.debug("Host:isHostname()");
    if (this.addressType == this.HOSTNAME) {
        return true;
    } else {
        return false;
    }
}


Host.prototype.isIPAddress =function(){
    //if(logger!=undefined) logger.debug("Host:isIPAddress()");
    if (this.addressType != this.HOSTNAME) {
        return true;
    } else {
        return false;
    }
}

Host.prototype.getInetAddress =function(){
    //if(logger!=undefined) logger.debug("Host:getInetAddress()");
    if (this.hostname == null)
    {
        return null;
    }
    if (this.inetAddress != null)
    {
        return this.inetAddress;
    }
    // CAN NOT BE IMPLEMENTED
    /*this.inetAddress = InetAddress.getByName(hostname);*//////////////////////meme problem comme la méthode d'avant'
    return this.inetAddress;
}

//----- IPv6
Host.prototype.isIPv6Address =function(address){
    //if(logger!=undefined) logger.debug("Host:isIPv6Address():address="+address);
    if (address != null && address.indexOf(':') != -1) {
        return true;
    } else {
        return false;
    }
}

Host.prototype.isIPv6Reference =function(address){
    //if(logger!=undefined) logger.debug("Host:isIPv6Reference():address="+address);
    if (address.charAt(0) == '['
        && address.charAt(address.length() - 1) == ']') {
        return true;
    } else {
        return false;
    }
}

Host.prototype.hashCode =function(){
    if(logger!=undefined) logger.debug("Host:hashCode()");
    var hash = 0;
    var x=this.getHostname();
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
