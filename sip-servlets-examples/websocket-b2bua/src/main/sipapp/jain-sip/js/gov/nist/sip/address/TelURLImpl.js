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
 *  Implementation of the JAIN-SIP  TelURLImpl.
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *  @see  gov/nist/javax/sip/address/TelURLImpl.java  
 */


function TelURLImpl() {
    if(logger!=undefined) logger.debug("TelURLImpl:TelURLImpl()");
    this.serialVersionUID = "5873527320305915954L";
    this.classname="TelURLImpl";
    this.telephoneNumber=new TelephoneNumber();
    this.scheme = "tel";
}

TelURLImpl.prototype = new GenericURI();
TelURLImpl.prototype.constructor=TelURLImpl;

TelURLImpl.prototype.setTelephoneNumber =function(telephoneNumber){
    if(logger!=undefined) logger.debug("TelURLImpl:setTelephoneNumber():telephoneNumber:"+telephoneNumber);
    this.telephoneNumber = telephoneNumber;
}

TelURLImpl.prototype.getIsdnSubAddress =function(){
    if(logger!=undefined) logger.debug("TelURLImpl:getIsdnSubAddress()");
    return this.telephoneNumber.getIsdnSubaddress();
}

TelURLImpl.prototype.getPostDial =function(){
    if(logger!=undefined) logger.debug("TelURLImpl:getPostDial()");
    return this.telephoneNumber.getPostDial();
}

TelURLImpl.prototype.getScheme =function(){
    if(logger!=undefined) logger.debug("TelURLImpl:getScheme()");
    return this.scheme;
}

TelURLImpl.prototype.isGlobal =function(){
    if(logger!=undefined) logger.debug("TelURLImpl:isGlobal()");
    return this.telephoneNumber.isGlobal();
}

TelURLImpl.prototype.isSipURI =function(){
    if(logger!=undefined) logger.debug("TelURLImpl:isSipURI()");
    return false;
}

TelURLImpl.prototype.setGlobal =function(global){
    if(logger!=undefined) logger.debug("TelURLImpl:setGlobal():global="+global);
    this.telephoneNumber.setGlobal(global);
}

TelURLImpl.prototype.setIsdnSubAddress =function(isdnSubAddress){
    if(logger!=undefined) logger.debug("TelURLImpl:setIsdnSubAddress():isdnSubAddress="+isdnSubAddress);
    this.telephoneNumber.setIsdnSubaddress(isdnSubAddress);
}

TelURLImpl.prototype.setPostDial =function(postDial){
    if(logger!=undefined) logger.debug("TelURLImpl:setPostDial():postDial="+postDial);
    this.telephoneNumber.setPostDial(postDial);
}

TelURLImpl.prototype.setPhoneNumber =function(telephoneNumber){
    if(logger!=undefined) logger.debug("TelURLImpl:setPhoneNumber():telephoneNumber="+telephoneNumber);
    this.telephoneNumber.setPhoneNumber(telephoneNumber);
} 

TelURLImpl.prototype.getPhoneNumber =function(){
    if(logger!=undefined) logger.debug("TelURLImpl:getPhoneNumber()");
    return this.telephoneNumber.getPhoneNumber();
}

TelURLImpl.prototype.toString =function(){
    if(logger!=undefined) logger.debug("TelURLImpl:toString()");
    return this.scheme + ":" + this.telephoneNumber.encode();
}

TelURLImpl.prototype.encode =function(){
    if(logger!=undefined) logger.debug("TelURLImpl:encode()");
    return this.encodeBuffer("").toString();
}

TelURLImpl.prototype.encodeBuffer =function(buffer){
    if(logger!=undefined) logger.debug("TelURLImpl:encodeBuffer():buffer="+buffer);
    buffer=buffer+this.scheme+":";
    buffer=this.telephoneNumber.encodeBuffer(buffer);
    return buffer;
}

TelURLImpl.prototype.getParameter =function(parameterName){
    if(logger!=undefined) logger.debug("TelURLImpl:getParameter():parameterName="+parameterName);
    return this.telephoneNumber.getParameter(parameterName);
}

TelURLImpl.prototype.setParameter =function(name, value){
    if(logger!=undefined) logger.debug("TelURLImpl:setParameter():name="+name+" value="+value);
    this.telephoneNumber.setParameter(name, value);
}

TelURLImpl.prototype.getParameterNames =function(){
    if(logger!=undefined) logger.debug("TelURLImpl:getParameterNames()");
    return this.telephoneNumber.getParameterNames();
}

TelURLImpl.prototype.getParameters =function(){
    if(logger!=undefined) logger.debug("TelURLImpl:getParameters()");
    return this.telephoneNumber.getParameters();
}

TelURLImpl.prototype.removeParameter =function(name){
    if(logger!=undefined) logger.debug("TelURLImpl:removeParameter():name="+name);
    this.telephoneNumber.removeParameter(name);
}

TelURLImpl.prototype.setPhoneContext =function(phoneContext){
    if(logger!=undefined) logger.debug("TelURLImpl:setPhoneContext():phoneContext="+phoneContext);
    if (phoneContext==null) {
        this.removeParameter("phone-context");
    } 
    else 
    {
        this.setParameter("phone-context",phoneContext);
    }
}

TelURLImpl.prototype.getPhoneContext =function(){
    if(logger!=undefined) logger.debug("TelURLImpl:getPhoneContext()");
    return this.getParameter("phone-context");
}

