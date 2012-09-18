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
 *  Implementation of the JAIN-SIP AddressParametersHeader .
 *  @see  gov/nist/javax/sip/header/AddressParametersHeader.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function AddressParametersHeader(name, sync) {
    if(logger!=undefined) logger.debug("AddressParametersHeader:AddressParametersHeader(): name="+name+",sync="+sync);
    this.classname="AddressParametersHeader";
    this.address=new AddressImpl();
    this.headerName=null;
    if(name!=null&&sync==null)
    {
        this.headerName=name;
        this.parameters = new NameValueList();
        this.duplicates = new DuplicateNameValueList();
    }
    else if(name!=null&&sync!=null)
    {
        this.headerName=name;
        this.parameters = new NameValueList(sync);
        this.duplicates = new DuplicateNameValueList();
    }
}

AddressParametersHeader.prototype = new ParametersHeader();
AddressParametersHeader.prototype.constructor=AddressParametersHeader;

AddressParametersHeader.prototype.getAddress =function(){
    if(logger!=undefined) logger.debug("AddressParametersHeader:getAddress()");
    return this.address;
}

AddressParametersHeader.prototype.setAddress =function(address){
    if(logger!=undefined) logger.debug("AddressParametersHeader:setAddress():address"+address);
    this.address=address;
}

AddressParametersHeader.prototype.equals =function(other){
    if(logger!=undefined) logger.debug("AddressParametersHeader:equals():other"+other);
    if (this==other) {
        return true;
    }
    if (/*other instanceof HeaderAddress && */other instanceof ParametersHeader) {//i hava not found the class which implement headeraddress, so i delete this condition
        var o = other;
        if(this.getAddress().equals(o.getAddress())&& this.equalParameters(o))
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