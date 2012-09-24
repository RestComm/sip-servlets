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
 *  Implementation of the JAIN-SIP RecordRoute .
 *  @see  gov/nist/javax/sip/header/RecordRoute.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function RecordRoute(address) {
    if(logger!=undefined) logger.debug("RecordRoute:RecordRoute()");
    this.serialVersionUID = "2388023364181727205L";
    this.classname="RecordRoute";
    if(address==null)
    {
        this.headerName=this.RECORD_ROUTE;
        this.address=new AddressImpl();
        this.parameters = new NameValueList();
        this.duplicates = new DuplicateNameValueList();
    }
    else
    {
        this.headerName=this.NAME;
        this.address=address;
        this.parameters = new NameValueList();
        this.duplicates = new DuplicateNameValueList();
    }
}

RecordRoute.prototype = new AddressParametersHeader();
RecordRoute.prototype.constructor=RecordRoute;
RecordRoute.prototype.NAME="Record-Route";
RecordRoute.prototype.RECORD_ROUTE="Record-Route";
RecordRoute.prototype.ADDRESS_SPEC = 2;
RecordRoute.prototype.LESS_THAN="<";
RecordRoute.prototype.GREATER_THAN=">";
RecordRoute.prototype.SEMICOLON=";";

RecordRoute.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("RecordRoute:encodeBody()");
    return this.encodeBodyBuffer("").toString();
}

RecordRoute.prototype.encodeBodyBuffer =function(buffer){
    if(logger!=undefined) logger.debug("RecordRoute:encodeBodyBuffer():buffer="+buffer);
    if (this.address.getAddressType() == this.ADDRESS_SPEC) {
        buffer=buffer+this.LESS_THAN;
    }
    buffer=this.address.encodeBuffer(buffer);
    if (this.address.getAddressType() == this.ADDRESS_SPEC) {
        buffer=buffer+this.GREATER_THAN;
    }

    if (this.parameters.hmap.length!=0) {
        buffer=buffer+this.SEMICOLON;
        buffer=this.parameters.encodeBuffer(buffer);
    }
    return buffer;
}
