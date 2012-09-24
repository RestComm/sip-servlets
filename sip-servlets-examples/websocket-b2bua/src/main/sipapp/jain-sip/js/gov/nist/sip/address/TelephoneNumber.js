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
 *  Implementation of the JAIN-SIP  TelephoneNumber.
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *  @see  gov/nist/javax/sip/address/TelephoneNumber.java  
 */

function TelephoneNumber() {
    if(logger!=undefined) logger.debug("TelephoneNumber:TelephoneNumber()");
    this.classname="TelephoneNumber"; 
    this.isglobal=null;
    this.phoneNumber=null;
    this.parameters=new NameValueList();
}

TelephoneNumber.prototype.POSTDIAL = "postdial";
TelephoneNumber.prototype.PHONE_CONTEXT_TAG ="context-tag";
TelephoneNumber.prototype.ISUB = "isub";
TelephoneNumber.prototype.PROVIDER_TAG = "provider-tag";
TelephoneNumber.prototype.SEMICOLON=";";

TelephoneNumber.prototype.deleteParm=function(name){
    if(logger!=undefined) logger.debug("TelephoneNumber:deleteParm()");
    this.parameters.delet(name);
}

TelephoneNumber.prototype.getPhoneNumber=function(){
    if(logger!=undefined) logger.debug("TelephoneNumber:getPhoneNumber()");
    return this.phoneNumber;
}

TelephoneNumber.prototype.getPostDial=function(){
    if(logger!=undefined) logger.debug("TelephoneNumber:getPostDial()");
    return this.parameters.getValue(this.POSTDIAL);
}

TelephoneNumber.prototype.getIsdnSubaddress=function(){
    if(logger!=undefined) logger.debug("TelephoneNumber:getIsdnSubaddress()");
    return this.parameters.getValue(this.ISUB);
}

TelephoneNumber.prototype.hasPostDial=function(){
    if(logger!=undefined) logger.debug("TelephoneNumber:hasPostDial()");
    if(this.parameters.getValue(this.POSTDIAL) != null)
    {
        return true;
    }
    else
    {
        return false;
    }
}

TelephoneNumber.prototype.hasParm=function(pname){
    if(logger!=undefined) logger.debug("TelephoneNumber:hasParm()");
    if(this.parameters.hasNameValue(pname))
    {
        return true;
    }
    else
    {
        return false;
    }
}

TelephoneNumber.prototype.hasIsdnSubaddress=function(){
    if(logger!=undefined) logger.debug("TelephoneNumber:hasIsdnSubaddress()");
    if(this.hasParm(this.ISUB))
    {
        return true;
    }
    else
    {
        return false;
    }
}

TelephoneNumber.prototype.isGlobal=function(){
    if(logger!=undefined) logger.debug("TelephoneNumber:isGlobal()");
    return this.isglobal;
}

TelephoneNumber.prototype.removePostDial=function(){
    if(logger!=undefined) logger.debug("TelephoneNumber:removePostDial()");
    this.parameters.delet(this.POSTDIAL);
}

TelephoneNumber.prototype.removeIsdnSubaddress=function(){
    if(logger!=undefined) logger.debug("TelephoneNumber:removeIsdnSubaddress()");
    this.deleteParm(this.ISUB);
}

TelephoneNumber.prototype.setParameters=function(p){
    if(logger!=undefined) logger.debug("TelephoneNumber:setParameters():p:"+p);
    this.parameters=p;
}

TelephoneNumber.prototype.setGlobal=function(g){
    if(logger!=undefined) logger.debug("TelephoneNumber:setGlobal():g:"+g);
    this.isglobal=g;
}

TelephoneNumber.prototype.setPostDial=function(p){
    if(logger!=undefined) logger.debug("TelephoneNumber:setPostDial():p:"+p);
    var nv=new NameValue(this.POSTDIAL,p);
    this.parameters.set_nv(nv);
}

TelephoneNumber.prototype.setParm=function(name, value){
    if(logger!=undefined) logger.debug("TelephoneNumber:setParm():name="+name+" value="+value);
    var nv = new NameValue(name, value);
    this.parameters.set_nv(nv);
}

TelephoneNumber.prototype.setIsdnSubaddress=function(isub){
    if(logger!=undefined) logger.debug("TelephoneNumber:setIsdnSubaddress():isub="+isub);
    this.setParm(this.ISUB, isub);
}

TelephoneNumber.prototype.setPhoneNumber=function(num){
    if(logger!=undefined) logger.debug("TelephoneNumber:setPhoneNumber():num="+num);
    this.phoneNumber=num;
}

TelephoneNumber.prototype.encode=function(){
    if(logger!=undefined) logger.debug("TelephoneNumber:encode()");
    return this.encodeBuffer("").toString();
}

TelephoneNumber.prototype.encodeBuffer=function(buffer){
    if(logger!=undefined) logger.debug("TelephoneNumber:encodeBuffer():buffer="+buffer);
    if (this.isglobal)
    {
        buffer=buffer+"+";
    }
    buffer=buffer+this.phoneNumber;
    if (this.parameters.hmap.length!=0) {
        buffer=buffer+this.SEMICOLON;
        buffer=this.parameters.encodeBuffer(buffer);
    }
    return buffer;
}

TelephoneNumber.prototype.getParameter=function(name){
    if(logger!=undefined) logger.debug("TelephoneNumber:getParameter():name="+name);
    var val = this.parameters.getValue(name);
    if (val == null)
    {
        return null;
    }
    if (val instanceof GenericObject)
    {
        return val.encode();
    }
    else
    {
        return val.toString();
    }
}

TelephoneNumber.prototype.getParameterNames=function(){
    if(logger!=undefined) logger.debug("TelephoneNumber:getParameterNames()");
    return this.parameters.getNames();
}

TelephoneNumber.prototype.removeParameter=function(parameter){
    if(logger!=undefined) logger.debug("TelephoneNumber:removeParameter():parameter:"+parameter);
    this.parameters.delet(parameter);
}

TelephoneNumber.prototype.setParameter=function(name, value){
    if(logger!=undefined) logger.debug("TelephoneNumber:setParameter():name="+name+" value="+value);
    var nv = new NameValue(name, value);
    this.parameters.set_nv(nv);
}

TelephoneNumber.prototype.getParameters=function(){
    if(logger!=undefined) logger.debug("TelephoneNumber:getParameters()");
    return this.parameters;
}

