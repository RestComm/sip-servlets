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
 *  Implementation of the JAIN-SIP NameValue class.
 *  @see  gov/nist/core/NameValue.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function NameValue(n,v,isFlag) {
    //if(logger!=undefined) logger.debug("NameValue:NameValue()");
    this.classname="NameValue"; 
    this.serialVersionUID = "-1857729012596437950L";
    this.isQuotedString = null;
    this.isFlagParameter= isFlag;
    this.separator = this.EQUALS;
    this.quotes= "";
    this.name = n;
    this.value= v;
    if(n==null)
    {
        this.name=null;
    }
    if(v==null)
    {
        this.value=null;
    }
    if(isFlag==null)
    {
        this.isFlagParameter=false;
    }
}

NameValue.prototype = new GenericObject();
NameValue.prototype.constructor=NameValue;
NameValue.prototype.EQUALS="=";
NameValue.prototype.DOUBLE_QUOTE="\"";

NameValue.prototype.setSeparator=function(sep){
    //if(logger!=undefined) logger.debug("NameValue:setSeparator():sep="+sep);
    this.separator=sep;
}

NameValue.prototype.setQuotedValue=function(){
    //if(logger!=undefined) logger.debug("NameValue:setQuotedValue()");
    this.isQuotedString=true;
    this.quotes=this.DOUBLE_QUOTE;
}

NameValue.prototype.isValueQuoted=function(){
    //if(logger!=undefined) logger.debug("NameValue:isValueQuoted()");
    return this.isQuotedString;
}

NameValue.prototype.getName=function(){
    //if(logger!=undefined) logger.debug("NameValue:getName()");
    return this.name;
}

NameValue.prototype.getValueAsObject=function(){
    //if(logger!=undefined) logger.debug("NameValue:getValueAsObject()");
    return this.isFlagParameter ? "" : this.value; 
}

NameValue.prototype.setName=function(n){
    //if(logger!=undefined) logger.debug("NameValue:setName()");
    this.name=n;
}

NameValue.prototype.setValueAsObject=function(v){
    //if(logger!=undefined) logger.debug("NameValue:setValueAsObject():v="+v);
    this.value=v;
}

NameValue.prototype.encode=function(){
    //if(logger!=undefined) logger.debug("NameValue:encode()");
    return this.encodeBuffer("").toString();
}

NameValue.prototype.encodeBuffer=function(buffer){
    //if(logger!=undefined) logger.debug("NameValue:encodeBuffer():buffer="+buffer);
    var go=new GenericObject();
    var gol=new GenericObjectList();
   
    if (this.name != null && this.value != null && !this.isFlagParameter) {
        
        if (go.isMySubclass(this.value)) {
            buffer=buffer+this.name+this.separator+this.quotes;
            buffer=this.value.encodeBuffer(buffer);
            buffer=buffer+this.quotes;
            return buffer;
        } else if (gol.isMySubclass(this.value)) {
            buffer=buffer+this.name+this.separator+this.value.encode();
            return buffer;
        } else if (this.value.toString().length == 0) {
            // opaque="" bug fix - pmusgrave
            /*
             * if
             * (name.toString().equals(gov.nist.javax.sip.header.ParameterNames.OPAQUE))
             * return name + separator + quotes + quotes; else return name;
             */
            if (this.isQuotedString) {
                buffer=buffer+this.name+this.separator+this.quotes+this.quotes;
                return buffer;
            } else {
                if(this.name=="lr")//when it is lr, we don't need "="
                {
                    buffer=buffer+this.name;
                    return buffer;
                }
                else
                {
                    buffer=buffer+this.name+this.separator;
                    //buffer.append(name).append(separator); // JvB: fix, case: "sip:host?subject="
                    return buffer;
                }
                
            }
        } else {
            buffer=buffer+this.name+this.separator+this.quotes+this.value.toString()+this.quotes;
            return buffer;
        }
    } else if (this.name == null && this.value != null) {
        
        if (go.isMySubclass(this.value)) {
            this.value.encodeBuffer(buffer);
            return buffer;
        } else if (gol.isMySubclass(this.value)) {
            buffer=buffer+this.value.encode();
            return buffer;
        } else {
            buffer=buffer+this.quotes+this.value.toString()+this.quotes;
            return buffer;
        }
    } else if (this.name != null && (this.value == null || this.isFlagParameter)) {
        buffer=buffer+this.name;
        return buffer;
    } else {
        return buffer;
    }
}

NameValue.prototype.equals=function(other){
    //if(logger!=undefined) logger.debug("NameValue:equals():other="+other);
    if (other == null ) return false;
    if (other.classname!=this.classname)
    {
        return false;
    }
    var that = other;
    if (this == that)
    {
        return true;
    }
    if (this.name == null && that.name != null || this.name != null
        && that.name == null)
        {
        return false;
    }
    if (this.name != null && that.name != null
        && this.name.compareToIgnoreCase(that.name) != 0)
        {
        return false;
    }
    if (this.value != null && that.value == null || this.value == null
        && that.value != null)
        {
        return false;
    }
    if (this.value == that.value)
    {
        return true;
    }
    if (this.value instanceof String) {
        // Quoted string comparisions are case sensitive.
        if (this.isQuotedString)
        {
            if(this.value==that.value)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        var val = this.value;
        var val1 = that.value;
        if(val.toLowerCase()==val1.toLowerCase())
        {
            return true;
        }
        else
        {
            return false;
        }
    } 
    else
    {
        if(this.value==that.value)
        {
            return true;
        }
        else
        {
            return false;
        }   
    }
}

NameValue.prototype.getKey=function(){
    //if(logger!=undefined) logger.debug("NameValue:getKey()");
    return this.name;
}

NameValue.prototype.getValue=function(){
    //if(logger!=undefined) logger.debug("NameValue:getValue()");
    return  this.value == null ? null : this.value.toString();
}

NameValue.prototype.setValue=function(value){
    //if(logger!=undefined) logger.debug("NameValue:setValue():value="+value);
    var retval = this.value == null ? null : value;
    this.value = value;
    return retval;
}

NameValue.prototype.hashCode=function(){
    //if(logger!=undefined) logger.debug("NameValue:hashCode()");
    var hash = 0;
    var x=this.encode().toLowerCase();
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

