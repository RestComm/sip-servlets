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
 *  Implementation of the JAIN-SIP Route .
 *  @see  gov/nist/javax/sip/header/Route.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Route(address) {
    if(logger!=undefined) logger.debug("Route:Route()");
    this.serialVersionUID = "5683577362998368846L";
    this.classname="Route";
    if(address==null)
    {
        this.headerName = this.NAME;
        this.parameters = new NameValueList();
        this.duplicates = new DuplicateNameValueList();
    }
    else
    {
        this.parameters = new NameValueList();
        this.duplicates = new DuplicateNameValueList();
        this.address = address;
    }
}

Route.prototype = new AddressParametersHeader();
Route.prototype.constructor=Route;
Route.prototype.NAME="Route";
Route.prototype.NAME_ADDR=1;
Route.prototype.SEMICOLON=";";

Route.prototype.hashCode =function(){
    if(logger!=undefined) logger.debug("Route:hashCode()");
    var hash = 0;
    var x=this.address.getHostPort().encode().toLowerCase();
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

Route.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("Route:encodeBody()");
    return this.encodeBodyBuffer("").toString();
}

Route.prototype.encodeBodyBuffer =function(buffer){
    if(logger!=undefined) logger.debug("Route:encodeBodyBuffer():buffer="+buffer);
    var addrFlag = this.address.getAddressType() == this.NAME_ADDR;
    if (!addrFlag) {
        buffer=buffer+"<";
        buffer=this.address.encodeBuffer(buffer);
        buffer=buffer+">";
    } else {
        buffer=this.address.encodeBuffer(buffer);
    }
    if (this.parameters.hmap.length!=0) {
        buffer=buffer+this.SEMICOLON;
        buffer=this.parameters.encodeBuffer(buffer);
    }
    return buffer;
}