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
 *  Implementation of the JAIN-SIP ParametersHeader .
 *  @see  gov/nist/javax/sip/header/ParametersHeader.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ParametersHeader(hdrName, sync) {
    if(logger!=undefined) logger.debug("ParametersHeader:ParametersHeader()");
    this.classname="ParametersHeader";
    this.parameters=null;
    this.duplicates=null;
    this.headerName=null;
    if(hdrName==null&&sync==null)
    {
        this.parameters = new NameValueList();
        this.duplicates = new DuplicateNameValueList();
    }
    else if(hdrName!=null&&sync==null)
    {
        this.headerName=hdrName;
        this.parameters = new NameValueList();
        this.duplicates = new DuplicateNameValueList();
    }
    else if(hdrName!=null&&sync!=null)
    {
        this.headerName=hdrName;
        this.parameters = new NameValueList(sync);
        this.duplicates = new DuplicateNameValueList();
    }
}

ParametersHeader.prototype = new SIPHeader();
ParametersHeader.prototype.constructor=ParametersHeader;

ParametersHeader.prototype.getParameter =function(name){
    if(logger!=undefined) logger.debug("ParametersHeader:getParameter():name="+name);
    return this.parameters.getParameter(name);
}

ParametersHeader.prototype.getParameterValue =function(name){
    if(logger!=undefined) logger.debug("ParametersHeader:getParameterValue():name="+name);
    return this.parameters.getValue(name);
}

ParametersHeader.prototype.getParameterNames =function(){
    if(logger!=undefined) logger.debug("ParametersHeader:getParameterNames()");
    return this.parameters.getNames();
}

ParametersHeader.prototype.hasParameters =function(){
    if(logger!=undefined) logger.debug("ParametersHeader:hasParameters()");
    if(this.parameters != null && this.parameters.hmap.length!=0)
    {
        return true;
    }
    else
    {
        return false;
    }
}

ParametersHeader.prototype.removeParameter =function(name){
    if(logger!=undefined) logger.debug("ParametersHeader:removeParameter():name="+name);
    this.parameters.delet(name);
}
ParametersHeader.prototype.setQuotedParameter =function(name, value){
    if(logger!=undefined) logger.debug("ParametersHeader:setQuotedParameter():name="+name+",value:"+value);
    var nv = this.parameters.getNameValue(name);
    if (nv != null) 
    {
        nv.setValueAsObject(value);
        nv.setQuotedValue();
    } 
    else {
        nv = new NameValue(name, value);
        nv.setQuotedValue();
        this.parameters.set_nv(nv);
    }
}

// four fonctions set() with different auguments is integred into one fonction
ParametersHeader.prototype.setParameter =function(name, value){
    if(logger!=undefined) logger.debug("ParametersHeader:setParameter():name="+name+",value:"+value);
    if(typeof value=="object")
    {
        this.parameters.set_name_value(name,val);
    }
    else if(typeof value=="boolean")
    {
        var val = new Boolean(value);
        this.parameters.set_name_value(name,val);
    }
    else if(typeof value=="number")
    {
        var r=value-[value];
        if(r==0)
        {
            val = new Number(value);
            this.parameters.set_name_value(name,val);
        }
        else
        {
            val = new Number(value);
            var nv = this.parameters.getNameValue(name);
            if (nv != null) {
                nv.setValueAsObject(val);
            } else {
                nv = new NameValue(name, val);
                this.parameters.set_nv(nv);
            }
        }
    }
    else if(typeof value=="string")
    {
        nv = this.parameters.getNameValue(name);
        if (nv != null) {
            nv.setValueAsObject(value);
        } else {
            nv = new NameValue(name, value);
            this.parameters.set_nv(nv);
        }
    }
}

ParametersHeader.prototype.hasParameter =function(parameterName){
    if(logger!=undefined) logger.debug("ParametersHeader:hasParameter():parameterName="+parameterName);
    return this.parameters.hasNameValue(parameterName);
}

ParametersHeader.prototype.removeParameters =function(){
    if(logger!=undefined) logger.debug("ParametersHeader:removeParameters()");
    this.parameters = new NameValueList();
}

ParametersHeader.prototype.getParameters =function(){
    if(logger!=undefined) logger.debug("ParametersHeader:getParameters()");
    return this.parameters;
}
ParametersHeader.prototype.setParameter_nv =function(nameValue){
    if(logger!=undefined) logger.debug("ParametersHeader:setParameter_nv():nameValue="+nameValue);
    this.parameters.set_nv(nameValue);
}

ParametersHeader.prototype.setParameters =function(parameters){
    if(logger!=undefined) logger.debug("ParametersHeader:setParameters():parameters="+parameters);
    this.parameters = parameters;
}

ParametersHeader.prototype.getParameterAsNumber =function(parameterName){//i delete the other type of number. they are useless in javascript
    if(logger!=undefined) logger.debug("ParametersHeader:getParameterAsNumber():parameterName="+parameterName);
    if (this.getParameterValue(parameterName) != null) 
    {
        return this.getParameterValue(parameterName)-0;
    }   
    else
    {
        return -1;
    }
}
ParametersHeader.prototype.getParameterAsURI =function(parameterName){
    if(logger!=undefined) logger.debug("ParametersHeader:getParameterAsURI():parameterName="+parameterName);
    var val = this.getParameterValue(parameterName);
    if (val instanceof GenericURI)
    {
        return val;
    }
    else 
    {
        return new GenericURI(val);
    }
}

ParametersHeader.prototype.getParameterAsBoolean =function(parameterName){
    if(logger!=undefined) logger.debug("ParametersHeader:getParameterAsBoolean():parameterName="+parameterName);
    var val = this.getParameterValue(parameterName);
    if (val == null) 
    {
        return false;
    } 
    else if (val instanceof Boolean) 
    {
        return val;
    }
    else if (val instanceof String) 
    {
        var x=new Boolean(val);
        return x;
    }
    else
    {
        return false;
    }
}

ParametersHeader.prototype.getNameValue =function(parameterName){
    if(logger!=undefined) logger.debug("ParametersHeader:getNameValue():parameterName="+parameterName);
    return this.parameters.getNameValue(parameterName);
}

ParametersHeader.prototype.setMultiParameter_name_value =function(name, value){
    if(logger!=undefined) logger.debug("ParametersHeader:setMultiParameter_name_value():name="+name+",value="+value);
    var nv = new NameValue();
    nv.setName(name);
    nv.setValue(value);
    this.duplicates.set_nv(nv);
}

ParametersHeader.prototype.setMultiParameter_nv =function(nameValue){
    if(logger!=undefined) logger.debug("ParametersHeader:setMultiParameter_nv():nameValue="+nameValue);
    this.duplicates.set_nv(nameValue);
}

ParametersHeader.prototype.getMultiParameter =function(name){
    if(logger!=undefined) logger.debug("ParametersHeader:getMultiParameter():name="+name);
    return this.duplicates.getParameter(name);
}

ParametersHeader.prototype.getMultiParameters =function(){
    if(logger!=undefined) logger.debug("ParametersHeader:getMultiParameters()");
    return this.duplicates;
}

ParametersHeader.prototype.getMultiParameterValue =function(name){
    if(logger!=undefined) logger.debug("ParametersHeader:getMultiParameterValue(): name="+name);
    return this.duplicates.getValue(name);
}

ParametersHeader.prototype.getMultiParameterNames =function(){
    if(logger!=undefined) logger.debug("ParametersHeader:getMultiParameterNames()");
    return this.duplicates.getNames();
}

ParametersHeader.prototype.hasMultiParameters =function(){
    if(logger!=undefined) logger.debug("ParametersHeader:hasMultiParameters()");
    if(this.duplicates != null && this.duplicates.length!=o)
    {
        return true;
    }
    else
    {
        return false;
    }
}

ParametersHeader.prototype.removeMultiParameter =function(name){
    if(logger!=undefined) logger.debug("ParametersHeader:removeMultiParameter(): name="+name);
    this.duplicates.delet(name);
}

ParametersHeader.prototype.hasMultiParameter =function(parameterName){
    if(logger!=undefined) logger.debug("ParametersHeader:hasMultiParameter()");
    return this.duplicates.hasNameValue(parameterName);
}

ParametersHeader.prototype.removeMultiParameters =function(){
    if(logger!=undefined) logger.debug("ParametersHeader:removeMultiParameters()");
    this.duplicates = new DuplicateNameValueList();
}

ParametersHeader.prototype.equalParameters =function(other){
    if(logger!=undefined) logger.debug("ParametersHeader:equalParameters()");
    if (this==other) {
        return true;
    }
    var ary=this.getParameterNames();
    for (var i=0;i<ary.length;i++) {
        var pname = ary[i];
        var p1 = this.getParameter( pname );
        var p2 = other.getParameter( pname );
        if (p1 == null ^ p2 == null) {
            return false;
        }
        else if (p1 != null && p1.toLowerCase()!=p2){
            return false;
        }
    }
    ary=other.getParameterNames();
    for ( i=0;i<ary.length;i++) {
        pname = ary[i];
        p1 = other.getParameter( pname );
        p2 = this.getParameter( pname );
        if (p1 == null ^ p2 == null) {
            return false;
        }
        else if (p1 != null && p1.toLowerCase()!=p2 ){
            return false;
        }
    }
    return true;
}

ParametersHeader.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("ParametersHeader:encodeBody()");
    
}