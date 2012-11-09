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
 *  Implementation of the JAIN-SIP GenericObject class.
 *  @see  gov/nist/core/GenericObject.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function GenericObject() {
    this.classname="GenericObject"; 
    this.indentation=0;
    this.stringRepresentation="";
    this.matchExpression=null;
    this.immutableClassNames =["String", "Character",
    "Boolean", "Byte", "Short", "Integer", "Long",
    "Float", "Double"]
    this.immutableClasses=new Array();
            
}

GenericObject.prototype.SEMICOLON = ";";
GenericObject.prototype.COLON = ":";
GenericObject.prototype.COMMA = ",";
GenericObject.prototype.SLASH = "/";
GenericObject.prototype.SP = " ";
GenericObject.prototype.EQUALS = "=";
GenericObject.prototype.STAR = "*";
GenericObject.prototype.NEWLINE = "\r\n";
GenericObject.prototype.RETURN = "\n";
GenericObject.prototype.LESS_THAN = "<";
GenericObject.prototype.GREATER_THAN = ">";
GenericObject.prototype.AT = "@";
GenericObject.prototype.DOT = ".";
GenericObject.prototype.QUESTION = "?";
GenericObject.prototype.POUND = "#";
GenericObject.prototype.AND = "&";
GenericObject.prototype.LPAREN = "(";
GenericObject.prototype.RPAREN = ")";
GenericObject.prototype.DOUBLE_QUOTE = "\"";
GenericObject.prototype.QUOTE = "\'";
GenericObject.prototype.HT = "\t";
GenericObject.prototype.PERCENT = "%";

GenericObject.prototype.setMatcher =function(matchExpression){
    this.matchExpression = matchExpression;
}

GenericObject.prototype.getMatcher =function(){
    return this.matchExpression;
}

GenericObject.prototype.getClassFromName =function(className){
    function class_for_name(name) {
        return new Function('return new ' + name)();
    }
    var classfromname=class_for_name(className);
    return classfromname;
}

GenericObject.prototype.isMySubclass=function(other){
    if((typeof other)!="object"||other instanceof Array)
    {
        return false;
    }
    else
    {
        var c=0;
        if(Object.getPrototypeOf(other).classname=="GenericObject")
        {
            return true;
        }
        else 
        {
            O=Object.getPrototypeOf(other);
            for(;O.classname!=undefined;)
            {
                if(Object.getPrototypeOf(O).classname=="GenericObject")
                {
                    c=1;
                    O=Object.getPrototypeOf(O);
                }
                else
                {
                    O=Object.getPrototypeOf(O);
                }
            }
            if(c==1)
            {
                return true;
            }
            else
            {
                return false;
            }
            
        }
    }
}

GenericObject.prototype.encode=function(){
}

GenericObject.prototype.encode=function(buffer){
    return buffer+this.encode();
}

GenericObject.prototype.equals=function(that){
}

GenericObject.prototype.match=function(other){
}

GenericObject.prototype.merge=function(mergeObject){
}

GenericObject.prototype.clone=function(){
    var objClone;
    if (this.constructor == Object){
        objClone = new this.constructor(); 
    }else{
        objClone = new this.constructor(this.valueOf()); 
    }
    for(var key in this){
        if ( objClone[key] != this[key] ){ 
            if ( typeof(this[key]) == 'object' ){ 
                objClone[key] = this[key].clone();
            }else{
                objClone[key] = this[key];
            }
        }
    }
    objClone.toString = this.toString;
    objClone.valueOf = this.valueOf;
    return objClone; 
}
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
 *  Implementation of the JAIN-SIP GenericObjectList class.
 *  @see  gov/nist/core/GenericObjectList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function GenericObjectList() {
    this.classname="GenericObjectList"; 
}

GenericObjectList.prototype.isMySubclass=function(other){
    if((typeof other)!="object"||other instanceof Array)
    {
        return false;
    }
    else
    {
        var c=0;
        if(Object.getPrototypeOf(other).classname=="GenericObjectList")
        {
            return true;
        }
        else 
        {
            O=Object.getPrototypeOf(other);
            for(;O.classname!=undefined;)
            {
                if(Object.getPrototypeOf(O).classname=="GenericObjectList")
                {
                    c=1;
                    O=Object.getPrototypeOf(O);
                }
                else
                {
                    O=Object.getPrototypeOf(O);
                }
            }
            if(c==1)
            {
                return true;
            }
            else
            {
                return false;
            }
            
        }
    }
}
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
    this.separator=sep;
}

NameValue.prototype.setQuotedValue=function(){
    this.isQuotedString=true;
    this.quotes=this.DOUBLE_QUOTE;
}

NameValue.prototype.isValueQuoted=function(){
    return this.isQuotedString;
}

NameValue.prototype.getName=function(){
    return this.name;
}

NameValue.prototype.getValueAsObject=function(){
    return this.isFlagParameter ? "" : this.value; 
}

NameValue.prototype.setName=function(n){
    this.name=n;
}

NameValue.prototype.setValueAsObject=function(v){
    this.value=v;
}

NameValue.prototype.encode=function(){
    return this.encodeBuffer("").toString();
}

NameValue.prototype.encodeBuffer=function(buffer){
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
    return this.name;
}

NameValue.prototype.getValue=function(){
    return  this.value == null ? null : this.value.toString();
}

NameValue.prototype.setValue=function(value){
    var retval = this.value == null ? null : value;
    this.value = value;
    return retval;
}

NameValue.prototype.hashCode=function(){
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
 *  Implementation of the JAIN-SIP NameValueList class.
 *  @see  gov/nist/core/NameValueList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function NameValueList(sync) {
    this.classname="NameValueList"; 
    this.serialVersionUID = "-6998271876574260243L";
    this.hmap = new Array();
    this.separator=";";

}

NameValueList.prototype.setSeparator =function(separator){
    this.separator=separator;
}

NameValueList.prototype.encode =function(){
    return this.encodeBuffer("").toString();
}

NameValueList.prototype.encodeBuffer =function(buffer){
    if (this.hmap.length!=0) 
    {
        for(var i=0;i<this.hmap.length;i++)
        {
            var obj = this.hmap[i][1];
            if (obj instanceof GenericObject) {
                var gobj = obj;
                buffer=gobj.encodeBuffer(buffer);
            } else {
                buffer=buffer+obj.toString();
            }
            if (i!=this.hmap.length-1)
            {
                buffer=buffer+this.separator;
            }
        }
    }
    return buffer;
}

NameValueList.prototype.toString =function(){
    return this.encode();
}

NameValueList.prototype.set_nv =function(nv){
    var n=0;
    for(var i=0;i<this.hmap.length;i++)// loop for method put() of hashtable
    {
        var key = this.hmap[i][0];
        if (key==nv.getName().toLowerCase()) {
            n=1;
            var x=new Array();
            x[0]=key;
            x[1]=nv;
            this.hmap[i]=x;
        } 
    }
    if(n==0)
    {
        var c=this.hmap.length;
        x=new Array();
        x[0]=nv.getName().toLowerCase();
        x[1]=nv;
        this.hmap[c]=x;
    }
}


NameValueList.prototype.set_name_value =function(name,value){
    var nv=new NameValue(name,value);
    name=name.toLowerCase();
    value=nv;
    var n=0;
    for(var i=0;i<this.hmap.length;i++)// loop for method put() of hashtable
    {
        var key = this.hmap[i][0];
        if (key==name.toLowerCase()) {
            n=1;
            var x=new Array();
            x[0]=key;
            x[1]=value;
            this.hmap[i]=x;
        } 
    }
    if(n==0)
    {
        var c=this.hmap.length;
        x=new Array();
        x[0]=name.toLowerCase();
        x[1]=value;
        this.hmap[c]=x;
    }
}


NameValueList.prototype.equals =function(otherObject){
    if ( otherObject == null ) {
        return false;
    }
    if (otherObject.classname!=this.classname){
        return false;
    }
    var other = otherObject;
    if (this.hmap.length!=other.hmap.length) {
        return false;
    }
    var key=new Array();
    var c=0;
    for (var i=0;i<this.hmap.length;i++)
    {
        key[c]=this.hmap[i][0];
        c++;
    }
    for (i=0;i<key.length;i++)
    {
        k = key[i];
        var nv1 = new NameValue();
        var nv2 = new NameValue();
        nv1 = this.getNameValue(k);
        nv2 = other.getNameValue(k);
        if (nv2 == null)
        {
            return false;
        }
        else if (!nv2.equals(nv1))
        {
            return false;
        }
    }
    return true;
}


NameValueList.prototype.getValue =function(name){
    var nv = new NameValue(); 
    nv=this.getNameValue(name.toLowerCase());
    if (nv != null)
    {
        return nv.getValueAsObject();
    }
    else
    {
        return null;
    }
}


NameValueList.prototype.getNameValue =function(name){
    var nv=null;
    for (var i=0;i<this.hmap.length;i++)
    {
        if(name.toLowerCase()==this.hmap[i][0])
        {
            nv=this.hmap[i][1];
        }
    }
    return nv;
}

NameValueList.prototype.hasNameValue =function(name){
    var c=0;
    for (var i=0;i<this.hmap.length;i++)
    {
        if(name.toLowerCase()==this.hmap[i][0])
        {
            c=1;
        }
    }
    if(c==1)
    {
        return true
    }
    else
    {
        return false
    }
}

NameValueList.prototype.delet=function(name){
    var c=0;
    var n=0;
    for (var i=0;i<this.hmap.length;i++)
    {
        if(name.toLowerCase()==this.hmap[i][0])
        {
            c=1;
            n=i;
        }
    }
    if(c==1)
    {
        this.hmap.splice(n,1);
        return true
    }
    else
    {
        return false
    }
}

NameValueList.prototype.size=function(){
    return this.hmap.length;
}

NameValueList.prototype.isEmpty=function(){
    if(this.hmap.length==0)
    {
        return true;
    }
    else
    {
        return false
    }
}


NameValueList.prototype.iterator=function(){
    return this.hmap;//here, i consider that we can use array to replace the itertor
}

NameValueList.prototype.getNames=function(){
    var key=new Array();
    var c=0;
    for (i=0;i<this.hmap.length;i++)
    {    
        key[c]=this.hmap[i][0];
        c++;
    }
    if(key.length!=0)
    {
        return key;
    }
    else
    {
        return null;
    }
}

NameValueList.prototype.getParameter=function(name){
    var val = this.getValue(name);
    if (val == null) {
        return null;
    }
    if (val instanceof GenericObject) {
        return val.encode();
    } else {
        return val.toString();
    }
}

NameValueList.prototype.clear=function(){
    this.hmap=null;
}

NameValueList.prototype.containsKey=function(key){
    var c=0;
    for (var i=0;i<this.hmap.length;i++)
    {
        if(this.hmap[i][0]==key.toString().toLowerCase())
        {
            c=1;
        }
    }
    if(c==1)
    {
        return true
    }
    else
    {
        return false
    }
}

NameValueList.prototype.containsValue=function(value){
    var c=0;
    for (var i=0;i<this.hmap.length;i++)
    {
        if(this.hmap[i][1]==value)
        {
            c=1;
        }
    }
    if(c==1)
    {
        return true
    }
    else
    {
        return false
    }
}

NameValueList.prototype.entrySet=function(){
    return this.hmap;
}

NameValueList.prototype.get=function(key){
    var nv = new NameValue(); 
    nv=this.getNameValue(key.toString().toLowerCase());
    if (nv != null)
    {
        return nv.getValueAsObject();
    }
    else
    {
        return null;
    }
}

NameValueList.prototype.keySet=function(){
    var key=new Array();
    var c=0;
    for (i=0;i<this.hmap.length;i++)
    {
        key[c]=this.hmap[i][0];
        c++;
    }
    return key;
}

NameValueList.prototype.put=function(name,nameValue){
    var nv=new NameValue();
    var n=0;
    for(var i=0;i<this.hmap.length;i++)// loop for method put() of hashtable
    {
        var key = this.hmap[i][0];
        if (key==name) {
            n=1;
            nv=this.hmap[i][1];
            var x=new Array();
            x[0]=name;
            x[1]=nameValue;
            this.hmap[i]=x;
        } 
    }
    if(n!=0)
    {
        return nv;
    }
    else
    {
        return null;
    }
}

NameValueList.prototype.putAll=function(map){
    for(var i=0;i<map.length;i++)// loop for method put() of hashtable
    {
        this.put(map[i][0], map[i][1]);
    }
}

NameValueList.prototype.remove=function(key){
    var c=0;
    var n=0;
    var nv=new NameValue();
    for (var i=0;i<this.hmap.length;i++)
    {
        if(key.toString().toLowerCase()==this.hmap[i][0])
        {
            c=1;
            n=i;
        }
    }
    if(c==1)
    {
        nv=this.hmap[n][1];
        this.hmap.splice(n,1);
        return nv;
    }
    else
    {
        return null;
    }
}

NameValueList.prototype.values=function(){
    var values=new Array();
    var c=0;
    for (i=0;i<this.hmap.length;i++)
    {
        values[c]=this.hmap[i][1];
        c++;
    }
    return values;
}

NameValueList.prototype.hashCode=function(){
    var hash = 0;
    var x=this.keySet();
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
 *  Implementation of the JAIN-SIP DuplicateNameValueList class.
 *  @see  gov/nist/core/DuplicateNameValueList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function DuplicateNameValueList() {
    this.serialVersionUID = "-5611332957903796952L";
    this.classname="DuplicateNameValueList";
    this.nameValueMap = new Array();
    this.separator=";";
}

DuplicateNameValueList.prototype.setSeparator =function(separator){
    this.separator=separator;
}

DuplicateNameValueList.prototype.encode =function(){
    return this.encodeBuffer("").toString();
}

DuplicateNameValueList.prototype.encodeBuffer =function(buffer){
    if (this.nameValueMap.length!=0) 
    {
        for(var i=0;i<this.nameValueMap.length;i++)
        {
            var obj = this.nameValueMap[i][1];
            for(var t=0;t<this.nameValueMap[i][1].length;t++)
            {
                obj=this.nameValueMap[i][1][t];
                if (obj instanceof GenericObject) {
                    var gobj = obj;
                    buffer=gobj.encodeBuffer(buffer);
                } else {
                    buffer=buffer+obj.toString();
                }
                if (i!=this.nameValueMap.length-1)
                {
                    buffer=buffer+this.separator;
                }
            }
        }
    }
    return buffer;
}

DuplicateNameValueList.prototype.toString =function(){
    return this.encode();
}

DuplicateNameValueList.prototype.set_nv =function(nv){
    var keylist=null;
    var keyex=0;
    for(var i=0;i<this.nameValueMap.length;i++)// loop for method put() of hashmap
    {
        var key = this.nameValueMap[i][0];
        if (key==nv.getName().toLowerCase()) {
            keylist=this.nameValueMap[i][1];
            var kll=keylist.length;
            keyex=1;
            var lo=i;
        } 
    }
    if(keylist==null)
    {
        if(keyex==1)//cas: key exists but the value is null
        {
            keylist = new Array();
            keylist[0]=nv;
            this.nameValueMap[lo][1]=keylist;
        }
        else//the key does not exist
        {
            var c=this.nameValueMap.length;
            keylist = new Array();
            var x=new Array();
            x[0]=nv.getName().toLowerCase();
            x[1]=keylist;
            this.nameValueMap[c]=x;
            kll=0;
            lo=c;
        }
    }
    keylist[kll]=nv;
    this.nameValueMap[lo][1]=keylist;
}

DuplicateNameValueList.prototype.set_name_value =function(name, value){
    var nv=new NameValue(name,value);
    var keylist=null;
    var keyex=0;
    for(var i=0;i<this.nameValueMap.length;i++)// loop for method put() of hashmap
    {
        var key = this.nameValueMap[i][0];
        if (key==nv.getName().toLowerCase()) {
            keylist=this.nameValueMap[i][1];
            var kll=keylist.length;
            keyex=1;
            var lo=i;
        } 
    }
    if(keylist==null)
    {
        if(keyex==1)
        {
            keylist = new Array();
            keylist[0]=nv;
            this.nameValueMap[lo][1]=keylist;
        }
        else
        {
            var c=this.nameValueMap.length;
            keylist = new Array();
            var x=new Array();
            x[0]=nv.getName().toLowerCase();
            x[1]=keylist;
            this.nameValueMap[c]=x;
            kll=0;
            lo=c;
        }
    }  
    keylist[kll]=nv;
    this.nameValueMap[lo][1]=keylist;
    
}

DuplicateNameValueList.prototype.equals =function(otherObject){
    if ( otherObject == null ) 
    {
        return false;
    }
    if (otherObject.classname==this.classname) 
    {
        return false;
    }
    var other =otherObject;

    if (this.nameValueMap.length != other.nameValueMap.length) 
    {
        return false;
    }
    for(var i;i<this.nameValueMap.length;i++)
    {
        var key=this.nameValueMap[i][0];
        var nv1 = this.getNameValue(key);
        var nv2 = other.nameValueMap.get(key);
        if (nv2 == null)
        {
            return false;
        }
        else if (nv2!=nv1)
        {
            return false;
        }
    }
    return true;
}

DuplicateNameValueList.prototype.getValue =function(name){
    var nv = this.getNameValue(name.toLowerCase());
    if (nv != null)
    {
        return nv;
    }
    else
    {
        return null;
    }
}

DuplicateNameValueList.prototype.getNameValue =function(name){
    var keylist=new Array();
    for(var i=0;i<this.nameValueMap.length;i++)// loop for method put() of hashmap
    {
        var key = this.nameValueMap[i][0];
        if (key==name) {
            keylist=this.nameValueMap[i][1];
        } 
    }
    return keylist;
}

DuplicateNameValueList.prototype.hasNameValue =function(name){
    var ex=0;
    for(var i=0;i<this.nameValueMap.length;i++)// loop for method put() of hashmap
    {
        var key = this.nameValueMap[i][0];
        if (key==name.toLowerCase()) {
            ex=1;
        } 
    }
    if(ex==1)
    {
        return true;
    }
    else
    {
        return false;
    }
}

DuplicateNameValueList.prototype.delet =function(name){
    var lcName = name.toLowerCase();
    var ex=0;
    var lo=0
    for(var i=0;i<this.nameValueMap.length;i++)// loop for method put() of hashmap
    {
        var key = this.nameValueMap[i][0];
        if (key==lcName) {
            ex=1;
            lo=i;
        } 
    }
    if(ex==1)
    {
        this.nameValueMap.splice(lo,1);
        return true;
    }
    else
    {
        return false;
    }
}

DuplicateNameValueList.prototype.iterator =function(){
    return this.nameValueMap;
}


DuplicateNameValueList.prototype.getNames =function(){
    var key=new Array();
    var c=0;
    for (var i=0;i<this.nameValueMap.length;i++)
    {
        key[c]=this.nameValueMap[i][0];
        c++;
    }
    if(key.length!=0)
    {
        return key;
    }
    else
    {
        return null;
    }
}

DuplicateNameValueList.prototype.getParameter =function(name){
    var val = this.getValue(name);
    
    if (val == null)
    {
        return null;
    }
    var string="";
    for(var i=0;i<val.length;i++)
    {
        if (val[i] instanceof GenericObject)
        {
            string=string+val[i].encode();
                    
        }
        else
        {
            string=string+val[i].toString();
        }
        if(i!=val.length-1)
        {
            string=string+";  ";
        }
    }
    return string;
}

DuplicateNameValueList.prototype.clear =function(){
    this.nameValueMap=new Array();
}

DuplicateNameValueList.prototype.isEmpty =function(){
    if(this.nameValueMap.length!=0)
    {
        return true;
    }
    else
    {
        return false; 
    }
}

DuplicateNameValueList.prototype.put =function(key, value){
    var nv=new NameValue();
    var n=0;
    for(var i=0;i<this.nameValueMap.length;i++)// loop for method put() of hashtable
    {
        var k = this.nameValueMap[i][0];
        if (k==key) {
            n=1;
            nv=this.nameValueMap[i][1];
            var x=new Array();
            x[0]=key;
            x[1]=value;
            this.nameValueMap[i]=x;
        } 
    }
    if(n!=0)
    {
        return nv;
    }
    else
    {
        return null;
    }
}

DuplicateNameValueList.prototype.remove =function(key){
    var c=0;
    var n=0;
    var nv=new NameValue();
    for (var i=0;i< this.nameValueMap.length;i++)
    {
        if(key.toString().toLowerCase()== this.nameValueMap[i][0])
        {
            c=1;
            n=i;
        }
    }
    if(c==1)
    {
        nv= this.nameValueMap[n][1];
        this.nameValueMap.splice(n,1);
        return nv;
    }
    else
    {
        return null;
    }
}

DuplicateNameValueList.prototype.size =function(){
    var size=0;
    for(var i=0;i<this.nameValueMap.length;i++)
    {
        size=size+this.nameValueMap[i][1].length;
    }
    return size;
}

DuplicateNameValueList.prototype.values =function(){//return a series lists of values.
    var values=new Array();
    var c=0;
    for (var i=0;i<this.nameValueMap.length;i++)
    {
        values[c]=this.nameValueMap[i][1];
        c++;
    }
    return values;
}

DuplicateNameValueList.prototype.hashCode =function(){
    var hash = 0;
    var key=new Array();
    var c=0
    for (var i=0;i<this.nameValueMap.length;i++)
    {
        key[c]=this.nameValueMap[i][0];
        c++;
    }
    var x=key;
    if(!(x == null || x.value == ""))  
    {  
        for (i = 0; i < x.length; i++)  
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
 *  Implementation of the JAIN-SIP HostPort class.
 *  @see  gov/nist/core/HostPort.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function HostPort() {
    this.classname="HostPort"; 
    this.serialVersionUID = "-7103412227431884523L";
    this.port = -1;
    this.host=new Host();
}

HostPort.prototype = new GenericObject();
HostPort.prototype.constructor=HostPort;
HostPort.prototype.COLON=":";

HostPort.prototype.encode =function(){
    return this.encodeBuffer("");
}
HostPort.prototype.encodeBuffer =function(buffer){
    buffer=this.host.encodeBuffer(buffer);
    if (this.port != -1)
    {
        buffer=buffer+this.COLON+this.port;  
    }
    return buffer;
}
HostPort.prototype.equals =function(other){
    if (other == null) {
        return false;
    }
    if (this.classname==other.classname) {
        return false;
    }
    var that =  other;
    if(this.port == that.port && this.host.equals(that.host))
    {
        return true;
    }
    else
    {
        return false;
    }
}

HostPort.prototype.getHost =function(){
    return this.host;
}

HostPort.prototype.getPort =function(){
    return this.port;
}

HostPort.prototype.hasPort =function(){
    if(this.port!=-1)
    {
        return true;
    }
    else
    {
        return false;
    }
}

HostPort.prototype.removePort =function(){
    this.port=-1;
}

HostPort.prototype.setHost =function(h){
    this.host=h;
}

HostPort.prototype.setPort =function(p){
    this.port=p;
}

HostPort.prototype.getInetAddress =function(){
    if (this.host == null)
    {
        return null;
    }
    else
    {
        return this.host.getInetAddress();
    }
}

HostPort.prototype.merge =function(mergeObject){
    var go=new GenericObject();
    go.merge (mergeObject);
    if (this.port == -1)
    {
        this.port = mergeObject.port;
    }
}


HostPort.prototype.toString =function(){
    return this.encode();
}

HostPort.prototype.hashCode =function(){
    return this.host.hashCode()+this.port;
}/*
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
    return this.encodeBuffer("").toString();
}

Host.prototype.encodeBuffer =function(buffer){
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
    return this.hostname;
}

Host.prototype.getAddress =function(){
    return this.hostname;
}

Host.prototype.getIpAddress =function(){////////////////////////////////////////problem dans cette méthode
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
    this.setHost(h, this.HOSTNAME);
}

Host.prototype.setHostAddress =function(address){
    this.setHost(address, this.IPV4ADDRESS);
}

Host.prototype.setHost =function(host,type){
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
    this.setHostAddress(address);
}

Host.prototype.isHostname =function(){
    if (this.addressType == this.HOSTNAME) {
        return true;
    } else {
        return false;
    }
}


Host.prototype.isIPAddress =function(){
    if (this.addressType != this.HOSTNAME) {
        return true;
    } else {
        return false;
    }
}

Host.prototype.getInetAddress =function(){
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
    if (address != null && address.indexOf(':') != -1) {
        return true;
    } else {
        return false;
    }
}

Host.prototype.isIPv6Reference =function(address){
    if (address.charAt(0) == '['
        && address.charAt(address.length() - 1) == ']') {
        return true;
    } else {
        return false;
    }
}

Host.prototype.hashCode =function(){
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
 *  Implementation of the JAIN-SIP Token class.
 *  @see  gov/nist/core/Token.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Token() {
    this.classname="Token";
    this.tokenValue=null;
    this.tokenType=null;
}

Token.prototype.getTokenValue =function(){
    return this.tokenValue;
}

Token.prototype.getTokenType =function(){
    return this.tokenType;
}

Token.prototype.toString =function(){
    return "tokenValue = " + this.tokenValue + "/tokenType = " + this.tokenType;
}

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
 *  Implementation of the JAIN-SIP StringTokenizer class.
 *  @see  gov/nist/core/StringTokenizer.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function StringTokenizer(buffer) {
    this.classname="StringTokenizer";
    this.buffer=null;
    this.bufferLen=null;
    this.ptr=null;
    this.savedPtr=null;
    if(buffer!=null)
    {
        this.buffer = buffer;
        this.bufferLen = buffer.length;
        this.ptr = 0;
    }
}

StringTokenizer.prototype.nextToken =function(){
    var startIdx = this.ptr;
    while (this.ptr < this.bufferLen) {
        var c = this.buffer.charAt(this.ptr);
        this.ptr++;
        if (c == '\n') {
            break;
        }
    }
    return this.buffer.substring(startIdx, this.ptr);
}

StringTokenizer.prototype.hasMoreChars =function(){
    if(this.ptr < this.bufferLen&&this.buffer.charAt(this.ptr)!='\r')
    {
        return true;
    }
    else
    {
        return false;
    }
}

StringTokenizer.prototype.isHexDigit =function(ch){
    if((ch >= "A" && ch <= "F")
        || (ch >= "a" && ch <= "f")
        || this.isDigit(ch))
        {
        return true;
    }
    else
    {
        return false;
    }
}

StringTokenizer.prototype.isAlpha =function(ch){
    if(ch.charCodeAt(0) <= 127)
    {
        if((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))

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
        if(ch==ch.toLowerCase()||ch==ch.toUpperCase())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

StringTokenizer.prototype.isDigit =function(ch){
    if(ch.charCodeAt(0) <= 127)
    {
        if(ch <= '9' && ch >= '0')

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
        if(typeof ch=="number")
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

StringTokenizer.prototype.isAlphaDigit =function(ch){
    if(ch.charCodeAt(0) <= 127)
    {
        if((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')||(ch <= '9' && ch >= '0'))

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
        if((ch==ch.toLowerCase())||(ch==ch.toUpperCase())||(typeof ch=="number"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

StringTokenizer.prototype.getLine =function(){
    var startIdx = this.ptr;
    while (this.ptr < this.bufferLen && this.buffer.charAt(this.ptr) != '\n') {
        this.ptr++;
    }
    if (this.ptr < this.bufferLen && this.buffer.charAt(this.ptr) == '\n') {
        this.ptr++;
    }
    return this.buffer.substring(startIdx, this.ptr);
}

StringTokenizer.prototype.peekLine =function(){
    var curPos = this.ptr;
    var retval = this.getLine();
    this.ptr = curPos;
    return retval;
}

StringTokenizer.prototype.lookAhead =function(k){
    if(k==null)
    {
        k=0;
    }
    return this.buffer.charAt(this.ptr + k);
}

StringTokenizer.prototype.getNextChar =function(){
    if (this.ptr >= this.bufferLen) {
        console.error("StringTokenizer:getNextChar(): end of buffer:"+this.ptr);
        throw "StringTokenizer:getNextChar(): end of buffer";
    } 
    else 
    {
        return this.buffer.charAt(this.ptr++);
    }
}

StringTokenizer.prototype.consume =function(k){
    if(k==null)
    {
        this.ptr = this.savedPtr;
    }
    else
    {
        this.ptr += k;
    }
}

StringTokenizer.prototype.getLines =function(){
    var result = new Array();
    while (this.hasMoreChars()) {
        var line = this.getLine();
        result.push(line);
    }
    return result;
}

StringTokenizer.prototype.getNextToken =function(delim){
    var startIdx = this.ptr;
    while (true) {
        var la = this.lookAhead(0);
        if (la == delim) 
        {
            break;
        } 
        else if (la == '\0') 
        {
            console.error("StringTokenizer:getNextToken(): EOL reached");
            throw "StringTokenizer:getNextToken(): EOL reached";
        }
        this.consume(1);
    }
    return this.buffer.substring(startIdx, this.ptr);
}

StringTokenizer.prototype.getSDPFieldName =function(line){
    if (line == null) {
        return null;
    }
    var fieldName = null;
    var begin = line.indexOf("=");
    fieldName = line.substring(0, begin);
    return fieldName;
}


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
 *  Implementation of the JAIN-SIP LexerCore class.
 *  @see  gov/nist/core/LexerCore.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *  @todo catch exception 
 */
function LexerCore() {
    this.classname="LexerCore";
    this.globalSymbolTable=new Array();
    this.lexerTables=new Array();
    this.currentLexer=null;
    this.currentLexerName=null;
    this.currentMatch=new Token();
    if(arguments.length==0)
    {
        this.currentLexer = new Array();
        this.currentLexerName = "charLexer";
    }
    else
    {
        var lexerName=arguments[0];
        var buffer=arguments[1];
        this.buffer = buffer;
        this.bufferLen = buffer.length;
        this.ptr = 0;
        this.currentLexer = new Array();
        this.currentLexerName = lexerName;
    }
}

LexerCore.prototype = new StringTokenizer();
LexerCore.prototype.constructor=LexerCore;
LexerCore.prototype.START = 2048;
LexerCore.prototype.END = LexerCore.prototype.START + 2048;
LexerCore.prototype.ID = LexerCore.prototype.END - 1;
LexerCore.prototype.SAFE = LexerCore.prototype.END - 2;
LexerCore.prototype.WHITESPACE = LexerCore.prototype.END + 1;
LexerCore.prototype.DIGIT = LexerCore.prototype.END + 2;
LexerCore.prototype.ALPHA = LexerCore.prototype.END + 3;
LexerCore.prototype.BACKSLASH = "\\".charCodeAt(0);
LexerCore.prototype.QUOTE = "\'".charCodeAt(0);
LexerCore.prototype.AT = "@".charCodeAt(0);
LexerCore.prototype.SP = " ".charCodeAt(0);
LexerCore.prototype.HT = "\t".charCodeAt(0);
LexerCore.prototype.COLON = ":".charCodeAt(0);
LexerCore.prototype.STAR = "*".charCodeAt(0);
LexerCore.prototype.DOLLAR = "$".charCodeAt(0);
LexerCore.prototype.PLUS = "+".charCodeAt(0);
LexerCore.prototype.POUND = "#".charCodeAt(0);
LexerCore.prototype.MINUS = "-".charCodeAt(0);
LexerCore.prototype.DOUBLEQUOTE = "\"".charCodeAt(0);
LexerCore.prototype.TILDE = "~".charCodeAt(0);
LexerCore.prototype.BACK_QUOTE = "`".charCodeAt(0);
LexerCore.prototype.NULL = "\0".charCodeAt(0);
LexerCore.prototype.EQUALS = "=".charCodeAt(0);
LexerCore.prototype.SEMICOLON = ";".charCodeAt(0);
LexerCore.prototype.SLASH = "/".charCodeAt(0);
LexerCore.prototype.L_SQUARE_BRACKET = "[".charCodeAt(0);
LexerCore.prototype.R_SQUARE_BRACKET = "]".charCodeAt(0);
LexerCore.prototype.R_CURLY = "}".charCodeAt(0);
LexerCore.prototype.L_CURLY = "{".charCodeAt(0);
LexerCore.prototype.HAT = "^".charCodeAt(0);
LexerCore.prototype.BAR = "|".charCodeAt(0);
LexerCore.prototype.DOT = ".".charCodeAt(0);
LexerCore.prototype.EXCLAMATION = "!".charCodeAt(0);
LexerCore.prototype.LPAREN = "(".charCodeAt(0);
LexerCore.prototype.RPAREN = ")".charCodeAt(0);
LexerCore.prototype.GREATER_THAN = ">".charCodeAt(0);
LexerCore.prototype.LESS_THAN = "<".charCodeAt(0);
LexerCore.prototype.PERCENT = "%".charCodeAt(0);
LexerCore.prototype.QUESTION = "?".charCodeAt(0);
LexerCore.prototype.AND = "&".charCodeAt(0);
LexerCore.prototype.UNDERSCORE = "_".charCodeAt(0);
LexerCore.prototype.ALPHA_VALID_CHARS = String.fromCharCode(65535);
LexerCore.prototype.DIGIT_VALID_CHARS = String.fromCharCode(65534);
LexerCore.prototype.ALPHADIGIT_VALID_CHARS = String.fromCharCode(65533);

LexerCore.prototype.addKeyword =function(name, value){
    var val = value;
    this.currentLexer=this.put(this.currentLexer, name, val);
    var j=null
    for(var i=0;i<this.globalSymbolTable.length;i++)
    {
        if(this.globalSymbolTable[i]==val)
        {
            j=0;
        }
    }
    if(j==null)
    {
        this.globalSymbolTable=this.put(this.globalSymbolTable, val, name);
    }
}

LexerCore.prototype.lookupToken =function(value){
    var string=null;
    if (value > this.START) {
        for(var i=0;i<this.globalSymbolTable.length;i++)
        {
            if(this.globalSymbolTable[i][0]==value)
            {
                string=this.globalSymbolTable[i][1];    
            }
        }
        return string;
    } 
    else 
    {
        return value;
    }
}

LexerCore.prototype.addLexer =function(lexerName){
    var v=null;
    for(var i=0;i<this.lexerTables.length;i++)
    {
        if(this.lexerTables[i][0]==lexerName)
        {
            v=this.lexerTables[i][1];    
        }
    }
    this.currentLexer = v;
    if (this.currentLexer == null) {
        this.currentLexer = new Array();
        this.lexerTables=this.put(this.lexerTables, lexerName, this.currentLexer);
    }
    return this.currentLexer;
}

LexerCore.prototype.selectLexer =function(lexerName){
    this.currentLexerName = lexerName;
}

/**
 * Peek the next id but dont move the buffer pointer forward.
 */
LexerCore.prototype.peekNextId =function(){
    var oldPtr = this.ptr;
    var retval = this.ttoken();
    this.savedPtr = this.ptr;
    this.ptr = oldPtr;
    return retval;
}

LexerCore.prototype.getNextId =function(){
    return this.ttoken();
}

LexerCore.prototype.getNextToken =function(){
    if(arguments.length!=0)
    {
        var delim=arguments[0];
        var startIdx = this.ptr;
        while (true) {
            var la = this.lookAhead(0);
            if (la == delim) 
            {
                break;
            } 
            else if (la == '\0') 
            {
                console.error("LexerCore:getNextToken(): EOL reached");
                throw "LexerCore:getNextToken(): EOL reached";
            }
            this.consume(1);
        }
        return this.buffer.substring(startIdx, this.ptr);
    }
    else
    {
        return this.currentMatch;
    }
}

LexerCore.prototype.peekNextToken =function(ntokens){
    if(ntokens==null)
    {
        ntokens=1;
        return this.peekNextToken(ntokens)[0];
    }
    else
    {
        var old = this.ptr;
        var retval = new Array();
        for(var i=0;i<ntokens;i++)
        {
            var tok = new Token();
            if (this.startsId()) {
                var id = this.ttoken();
                tok.tokenValue = id;
                var idUppercase = id.toUpperCase();
                var j=null
                for(var l=0;l<this.currentLexer.length;l++)
                {
                    if(this.currentLexer[l][0]==idUppercase){
                        j=l;
                    }
                }
                if (j!=null) {
                    var type = this.currentLexer[j][1];
                    tok.tokenType = type;
                } else {
                    tok.tokenType = this.ID;
                }
            } else {
                var nextChar = this.getNextChar();
                tok.tokenValue = nextChar;
                if (this.isAlpha(nextChar)) {
                    tok.tokenType = this.ALPHA;
                } else if (this.isDigit(nextChar)) {
                    tok.tokenType = this.DIGIT;
                } else {
                    tok.tokenType = nextChar;
                }
            }
            retval[i] = tok;
        }
        this.savedPtr = this.ptr;
        this.ptr = old;
        return retval;
    }
}

LexerCore.prototype.match =function(tok){

    if (tok > this.START && tok < this.END) {
        if (tok == this.ID) {
            if (!this.startsId()) {
                console.error("LexerCore:match(): "+ this.buffer + "\nID expected", this.ptr);
                throw "LexerCore:match(): ID expected";
            }
            var id = this.getNextId();
            this.currentMatch = new Token();
            this.currentMatch.tokenValue = id;
            this.currentMatch.tokenType = this.ID;
        } else if (tok == this.SAFE) {
            if (!this.startsSafeToken()) {
                console.error("LexerCore:match(): "+ this.buffer + "\nID expected", this.ptr);
                throw "LexerCore:match(): ID expected";
            }
            id = this.ttokenSafe();
            this.currentMatch = new Token();
            this.currentMatch.tokenValue = id;
            this.currentMatch.tokenType = this.SAFE;
        } else {
            var nexttok = this.getNextId();
            var n=null;
            for(var i=0;i<this.currentLexer.length;i++)
            {
                if(this.currentLexer[i][0]==nexttok.toUpperCase())
                {
                    n=i;     
                }
            }
            if(n==null)
            {
                var cur=null;
            }
            else
            {
                cur =  this.currentLexer[n][1];
            }
            
            if (cur == null || cur != tok) {
                console.error("LexerCore:match(): "+this.buffer + "\nUnexpected Token : " + nexttok);
                throw "LexerCore:match(): unexpected Token";
            }
            this.currentMatch = new Token();
            this.currentMatch.tokenValue = nexttok;
            this.currentMatch.tokenType = tok;
        }
    }else if (tok > this.END) {
        var next = this.lookAhead(0);
        if (tok == this.DIGIT) {
            if (!this.isDigit(next)) {
                console.error("LexerCore:match(): "+ this.buffer + "\nExpecting DIGIT", this.ptr);
                throw "LexerCore:match(): Expecting DIGIT";
            }
            this.currentMatch = new Token();
            this.currentMatch.tokenValue =next;
            this.currentMatch.tokenType = tok;
            this.consume(1);
        
        } else if (tok == this.ALPHA) {
            if (!this.isAlpha(next)) {
                console.error("LexerCore:match(): "+ this.buffer + "\nExpecting ALPHA", this.ptr);
                throw "LexerCore:match(): Expecting ALPHA";
            }
            this.currentMatch = new Token();
            this.currentMatch.tokenValue =next;
            this.currentMatch.tokenType = tok;
            this.consume(1);
        }
    }else {
        var ch =  tok;
        next = this.lookAhead(0);
        if (next == ch) {
            this.consume(1);
        } else {
            console.error("LexerCore:match(): "+ this.buffer + "\nExpecting  >>>" + ch + "<<< got >>>"+ next + "<<<");
            throw "LexerCore:match(): expecting  >>>" + ch + "<<< got >>>"+ next + "<<<";
        }
    }
    return this.currentMatch;
}

LexerCore.prototype.SPorHT =function(){
    var c = this.lookAhead(0);
    while (c == ' ' || c == '\t') {
        this.consume(1);
        c = this.lookAhead(0);
    }
}

LexerCore.prototype.isTokenChar =function(c){
    if (this.isAlphaDigit(c)) {
        return true;
    } else {
        switch (c) {
            case "-":
            case ".":
            case "!":
            case "%":
            case "*":
            case "_":
            case "+":
            case "`":
            case "\'":
            case "~":
                return true;
            default:
                return false;
        }
    }
}

LexerCore.prototype.startsId =function(){
    var nextChar = this.lookAhead(0);
    return this.isTokenChar(nextChar);
}

LexerCore.prototype.startsSafeToken =function(){
    var nextChar = lookAhead(0);
    if (this.isAlphaDigit(nextChar)) {
        return true;
    } else {
        switch (nextChar) {
            case '_':
            case '+':
            case '-':
            case '!':
            case '`':
            case '\'':
            case '.':
            case '/':
            case '}':
            case '{':
            case ']':
            case '[':
            case '^':
            case '|':
            case '~':
            case '%': // bug fix by Bruno Konik, JvB copied here
            case '#':
            case '@':
            case '$':
            case ':':
            case ';':
            case '?':
            case '\"':
            case '*':
            case '=': // Issue 155 on java.net
                return true;
            default:
                return false;
        }
    }
}
LexerCore.prototype.ttoken =function(){
    var startIdx = this.ptr;
    while (this.hasMoreChars()) {
        var nextChar = this.lookAhead(0);
        if (this.isTokenChar(nextChar)) {
            this.consume(1);
        } else {
            break;
        }
    }
    return this.buffer.substring(startIdx, this.ptr);
}

LexerCore.prototype.ttokenSafe =function(){
    var startIdx = this.ptr;
    while (this.hasMoreChars()) {
        var nextChar = this.lookAhead(0);
        if (this.isAlphaDigit(nextChar)) {
            this.consume(1);
        } else {
            var isValidChar = false;
            switch (nextChar) {
                case '_':
                case '+':
                case '-':
                case '!':
                case '`':
                case '\'':
                case '.':
                case '/':
                case '}':
                case '{':
                case ']':
                case '[':
                case '^':
                case '|':
                case '~':
                case '%': // bug fix by Bruno Konik, JvB copied here
                case '#':
                case '@':
                case '$':
                case ':':
                case ';':
                case '?':
                case '\"':
                case '*':
                    isValidChar = true;
            }
            if (isValidChar) {
                this.consume(1);
            } else {
                break;
            }
        }
    }
    return this.buffer.substring(startIdx, this.ptr);
}

LexerCore.prototype.consumeValidChars =function(validChars){
    var validCharsLength = validChars.length;
    while (this.hasMoreChars()) {
        var nextChar = this.lookAhead(0);
        var isValid = false;
        for (var i = 0; i < validCharsLength; i++) {
            var validChar = validChars[i];
            switch (validChar) {
                case this.ALPHA_VALID_CHARS:
                    isValid = this.isAlpha(nextChar);
                    break;
                case this.DIGIT_VALID_CHARS:
                    isValid = this.isDigit(nextChar);
                    break;
                case this.ALPHADIGIT_VALID_CHARS:
                    isValid = this.isAlphaDigit(nextChar);
                    break;
                default:
                    isValid = nextChar == validChar;
            }
            if (isValid) {
                break;
            }
        }
        if (isValid) {
            this.consume(1);
        } else {
            break;
        }
    }
}

LexerCore.prototype.quotedString =function(){
    var startIdx = this.ptr + 1;
    if (this.lookAhead(0) != '\"') {
        return null;
    }
    this.consume(1);
    while (true) {
        var next = this.getNextChar();
        if (next == '\"') {
            break;
        } else if (next == '\0') {
            console.error("LexerCore:quotedString(): "+ this.buffer + " :unexpected EOL",this.ptr);
            throw "LexerCore:quotedString(): unexpected EOL";
        } else if (next == '\\') {
            this.consume(1);
        }
    }
    return this.buffer.substring(startIdx, this.ptr - 1);
}

LexerCore.prototype.comment =function(){
    var retval = "";
    if (this.lookAhead(0) != '(') {
        return null;
    }
    this.consume(1);
    while (true) {
        var next = this.getNextChar();
        if (next == ')') {
            break;
        } else if (next == '\0') {
            console.error("LexerCore:comment(): "+ this.buffer + " :unexpected EOL",this.ptr);
            throw "LexerCore:comment(): unexpected EOL";
        } else if (next == '\\') {
            retval=retval+next;
            next = this.getNextChar();
            if (next == '\0') {
                console.error("LexerCore:comment(): "+ this.buffer + " :unexpected EOL",this.ptr);
                throw "LexerCore:comment(): unexpected EOL";
            }
            retval=retval+next;
        } else {
            retval=retval+next;
        }
    }
    return retval.toString();
}

LexerCore.prototype.byteStringNoSemicolon =function(){
    var retval = "";
    while (true) {
        var next = this.lookAhead(0);
        if (next == '\0' || next == '\n' || next == ';' || next == ',') {
            break;
        } else {
            this.consume(1);
            retval=retval+next;
        }
    }
    return retval.toString();
}

LexerCore.prototype.byteStringNoWhiteSpace =function(){
    var retval = "";
    while (true) {
        var next = this.lookAhead(0);
        if (next == '\0' || next == '\n' || next == ' ') {
            break;
        } else {
            this.consume(1);
            retval=retval+next;
        }
    }
    return retval.toString();
}

LexerCore.prototype.byteStringNoSlash =function(){
    var retval = "";
    while (true) {
        var next = this.lookAhead(0);
        if (next == '\0' || next == '\n' || next == '/') {
            break;
        } else {
            this.consume(1);
            retval=retval+next;
        }
    }
    return retval.toString();
}

LexerCore.prototype.byteStringNoComma =function(){
    var retval = "";
    while (true) {
        var next = this.lookAhead(0);
        if (next == '\n' || next == ',') {
            break;
        } else {
            this.consume(1);
            retval=retval+next;
        }
    }
    return retval.toString();
}

LexerCore.prototype.charAsString =function(){
    if(typeof arguments[0]=="string")
    {
        var ch=arguments[0];
        return ch;
    }
    else if(typeof arguments[0]=="number")
    {
        var nchars=arguments[0];
        return this.buffer.substring(this.ptr, this.ptr + nchars);
    }
}

LexerCore.prototype.number =function(){
    var startIdx = this.ptr;
    if (!this.isDigit(this.lookAhead(0))) {
        console.error(this.buffer + "LexerCore:number(): Unexpected token at " + this.lookAhead(0),this.ptr);
        throw "LexerCore:number(): Unexpected token at " + this.lookAhead(0);
    }
    this.consume(1);
    while (true) {
        var next = this.lookAhead(0);
        if (this.isDigit(next)) {
            this.consume(1);
        } else {
            break;
        }
    }
    return this.buffer.substring(startIdx, this.ptr);
}

LexerCore.prototype.markInputPosition =function(){
    return this.ptr;
}

LexerCore.prototype.rewindInputPosition =function(position){
    this.ptr = position;
}

LexerCore.prototype.getRest =function(){
    if (this.ptr >= this.buffer.length) {
        return null;
    } else {
        return this.buffer.substring(this.ptr);
    }
}

LexerCore.prototype.getString =function(c){
    var retval = "";
    while (true) {
        var next = this.lookAhead(0);
        if (next == '\0') {
           console.error(this.buffer + "LexerCore:getString(): unexpected EOL",this.ptr);
           throw "LexerCore:getString(): unexpected EOL";
        } else if (next == c) {
            this.consume(1);
            break;
        } else if (next == '\\') {
            this.consume(1);
            var nextchar = this.lookAhead(0);
            if (nextchar == '\0') {
                console.error(this.buffer + "LexerCore:getString(): unexpected EOL",this.ptr);
                throw "LexerCore:getString(): unexpected EOL";
            } else {
                this.consume(1);
                retval=retval+nextchar;
            }
        } else {
            this.consume(1);
            retval=retval+next;
        }
    }
    return retval.toString();
}

LexerCore.prototype.getPtr =function(){
    return this.ptr;
}

LexerCore.prototype.getBuffer =function(){
    return this.buffer;
}

LexerCore.prototype.put =function(table,name, value){
    var n=0;
    for(var i=0;i<table.length;i++)// loop for method put() of hashtable
    {
        var key = table[i][0];
        if (key==name) {
            n=1;
            var x=new Array();
            x[0]=key;
            x[1]=value;
            table[i]=x;
        } 
    }
    if(n==0)
    {
        x=new Array();
        x[0]=name;
        x[1]=value;
        table.push(x);
    }
    return table;
}/*
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
 *  Implementation of the JAIN-SIP ParserCore class.
 *  @see  gov/nist/core/ParserCore.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function ParserCore() {
    this.classname="ParserCore";
    this.nesting_level=null;
    this.lexer=new LexerCore();
}

ParserCore.prototype.nameValue =function(separator){
    if(separator==null)
    {
        var nv=this.nameValue("=")
        return nv;
    }
    else
    {
        this.lexer.match(LexerCore.prototype.ID);
        var name = this.lexer.getNextToken();
        this.lexer.SPorHT();
        try {
            var quoted = false;
            var la = this.lexer.lookAhead(0);
            if (la == separator) {
                this.lexer.consume(1);
                this.lexer.SPorHT();
                var str = null;
                var isFlag = false;
                if (this.lexer.lookAhead(0) == '\"') {
                    str = this.lexer.quotedString();
                    quoted = true;
                } else {
                    this.lexer.match(LexerCore.prototype.ID);
                    var value = this.lexer.getNextToken();
                    str = value.tokenValue;
                    if (str == null) {
                        str = "";
                        isFlag = true;
                    }
                }
                var nv = new NameValue(name.tokenValue, str, isFlag);
                if (quoted) {
                    nv.setQuotedValue();
                }
                return nv;
            } else {
                nv=new NameValue(name.tokenValue, "", true);
                return nv;
            }
        } catch (ex) {    
            console.error("ParserCore:nameValue(): catched exception:"+ex);
            nv=new NameValue(name.tokenValue, null, false);
            return nv;
        }
    }
    return nv;
}/*
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
 *  Implementation of the JAIN-SIP HostNameParser class.
 *  @see  gov/nist/core/HostNameParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function HostNameParser() {
    this.classname="HostNameParser"; 
    this.Lexer=null;
    this.stripAddressScopeZones = false;
    if(typeof arguments[0]=="string")
    {
        var hname=arguments[0];
        this.lexer = new LexerCore("charLexer", hname);
    }
    else if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("charLexer");
    }
}

HostNameParser.prototype = new ParserCore();
HostNameParser.prototype.constructor=HostNameParser;
HostNameParser.prototype.VALID_DOMAIN_LABEL_CHAR=[LexerCore.prototype.ALPHADIGIT_VALID_CHARS, '-', '.'];

HostNameParser.prototype.consumeDomainLabel =function(){
    this.lexer.consumeValidChars(this.VALID_DOMAIN_LABEL_CHAR);
}

//ipv6 is not used
HostNameParser.prototype.ipv6Reference =function(){
}

HostNameParser.prototype.host =function(){
    var hostname;
    //IPv6 referene
    if (this.lexer.lookAhead(0) == '[') {
        hostname = this.ipv6Reference();
    }
    //IPv6 address (i.e. missing square brackets)
    /*else if( isIPv6Address(lexer.getRest()) )
            {
                var startPtr = lexer.getPtr();
                lexer.consumeValidChars([LexerCore.ALPHADIGIT_VALID_CHARS, ':']);
                hostname
                    = new StringBuffer("[").append(
                        lexer.getBuffer().substring(startPtr, lexer.getPtr()))
                        .append("]").toString();
            }*///ignore the parte of ipv6.
    //IPv4 address or hostname
    else {
        var startPtr = this.lexer.getPtr();
        this.consumeDomainLabel();
        hostname = this.lexer.getBuffer().substring(startPtr, this.lexer.getPtr());
    }
    if (hostname.length == 0)
    {
        console.error("Parser:host():"+ this.lexer.getBuffer() + " missing host name",this.lexer.getPtr());
        throw  "Parser:host(): Missing host name";   
    }
    else
    {
        return new Host(hostname);
    }
   
}


HostNameParser.prototype.isIPv6Address =function(){
}

HostNameParser.prototype.hostPort =function(allowWS){
    var host = this.host();
    var hp = new HostPort();
    hp.setHost(host);
    if (allowWS) {
        this.lexer.SPorHT();
    } 
    if (this.lexer.hasMoreChars()) {
        var la = this.lexer.lookAhead(0);
        switch (la)
        {
            case ':':
                this.lexer.consume(1);
                if (allowWS) {
                    this.lexer.SPorHT();
                } // white space before port number should be accepted
                try {
                    var port = this.lexer.number();
                    hp.setPort(port);
                } catch (nfe) {
                     console.error("Parser:hostPort():"+ this.lexer.getBuffer() + " : error parsing port ",this.lexer.getPtr());
                     throw  "Parser:hostPort(): error parsing port";   
                }
                break;
            case ',':
            case ';':   // OK, can appear in URIs (parameters)
            case '?':   // same, header parameters
            case '>':   // OK, can appear in headers
            case ' ':   // OK, allow whitespace
            case '\t':
            case '\r':
            case '\n':
            case '/':   // e.g. http://[::1]/xyz.html
                break;
            case '%':
                if(this.stripAddressScopeZones){
                    break;//OK,allow IPv6 address scope zone
                }
            default:
                if (!allowWS) {
                    console.error("Parser:hostPort(): "+ this.lexer.getBuffer() +" illegal character in hostname:" + this.lexer.lookAhead(0),this.lexer.getPtr() );
                    throw  "Parser:hostPort(): error parsing port"; 
                }
        }
    }
    return hp;
}/*
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
 * A JavaScript implementation (a bit modified by Orange)of the RSA Data Security, Inc. MD5 Message
 * Digest Algorithm, as defined in RFC 1321.
 * Version 2.2 Copyright (C) Paul Johnston 1999 - 2009
 * Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet
 * Distributed under the BSD License
 * See http://pajhome.org.uk/crypt/md5 for more info.
 */

function MessageDigestAlgorithm() {
    this.classname="MessageDigestAlgorithm"; 
    this.toHex=['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'];
    this.hexcase=0;
}

MessageDigestAlgorithm.prototype.calculateResponse =function(username_value,realm_value,
    passwd,nonce_value,nc_value,cnonce_value,method,digest_uri_value,entity_body,qop_value){
    var A1 = null;
    A1 = username_value + ":" + realm_value + ":" + passwd;
    var A2 = null;
    if (qop_value == null || qop_value.length == 0|| qop_value=="auth") 
    {
        A2 = method + ":" + digest_uri_value;
    } 
    else 
    {
        if (entity_body == null)
        {
            entity_body = "";
        }
        A2 = method + ":" + digest_uri_value + ":" + this.H(entity_body);
    }
    var request_digest = null;
    if (cnonce_value != null && qop_value != null && nc_value != null
        && (qop_value=="auth" || qop_value=="auth-int"))
        {
        request_digest = this.KD(this.H(A1), nonce_value + ":" + nc_value + ":" + cnonce_value + ":"
            + qop_value + ":" + this.H(A2));

    } 
    else {
        request_digest = this.KD(this.H(A1), nonce_value + ":" + this.H(A2));
    }
    return request_digest;
}

MessageDigestAlgorithm.prototype.H =function(data){
    return this.md5(data);
}
MessageDigestAlgorithm.prototype.KD =function(secret,data){
    return this.H(secret + ":" + data);
}

MessageDigestAlgorithm.prototype.toHexString =function(b){
    var pos = 0;
    var chaine="";
    var c = new Array();
    for (var i = 0; i < b.length; i++) {
        c[pos++] = this.toHex[(b[i] >> 4) & 0x0F];
        chaine=chaine+c[pos];
        c[pos++] = this.toHex[b[i] & 0x0f];
        chaine=chaine+c[pos];
    }
    return chaine;
}
MessageDigestAlgorithm.prototype.md5 =function(chaine){
    return this.hex_md5(chaine);    
}

MessageDigestAlgorithm.prototype.hex_md5=function(s)    {
    return this.rstr2hex(this.rstr_md5(this.str2rstr_utf8(s)));
}

MessageDigestAlgorithm.prototype.rstr_md5=function(s)
{
    return this.binl2rstr(this.binl_md5(this.rstr2binl(s), s.length * 8));
}

MessageDigestAlgorithm.prototype.rstr2hex=function(input)
{
    var hex_tab = this.hexcase ? "0123456789ABCDEF" : "0123456789abcdef";
    var output = "";
    var x;
    for(var i = 0; i < input.length; i++)
    {
        x = input.charCodeAt(i);
        output += hex_tab.charAt((x >>> 4) & 0x0F)
        +  hex_tab.charAt( x        & 0x0F);
    }
    return output;
}

MessageDigestAlgorithm.prototype.str2rstr_utf8=function(input)
{
    var output = "";
    var i = -1;
    var x, y;

    while(++i < input.length)
    {
        x = input.charCodeAt(i);
        y = i + 1 < input.length ? input.charCodeAt(i + 1) : 0;
        if(0xD800 <= x && x <= 0xDBFF && 0xDC00 <= y && y <= 0xDFFF)
        {
            x = 0x10000 + ((x & 0x03FF) << 10) + (y & 0x03FF);
            i++;
        }

        if(x <= 0x7F)
            output += String.fromCharCode(x);
        else if(x <= 0x7FF)
            output += String.fromCharCode(0xC0 | ((x >>> 6 ) & 0x1F),
                0x80 | ( x         & 0x3F));
        else if(x <= 0xFFFF)
            output += String.fromCharCode(0xE0 | ((x >>> 12) & 0x0F),
                0x80 | ((x >>> 6 ) & 0x3F),
                0x80 | ( x         & 0x3F));
        else if(x <= 0x1FFFFF)
            output += String.fromCharCode(0xF0 | ((x >>> 18) & 0x07),
                0x80 | ((x >>> 12) & 0x3F),
                0x80 | ((x >>> 6 ) & 0x3F),
                0x80 | ( x         & 0x3F));
    }
    return output;
}

MessageDigestAlgorithm.prototype.rstr2binl=function(input)
{
    var output = Array(input.length >> 2);
    for(var i = 0; i < output.length; i++)
        output[i] = 0;
    for(i = 0; i < input.length * 8; i += 8)
        output[i>>5] |= (input.charCodeAt(i / 8) & 0xFF) << (i%32);
    return output;
}

MessageDigestAlgorithm.prototype.binl2rstr=function(input)
{
    var output = "";
    for(var i = 0; i < input.length * 32; i += 8)
        output += String.fromCharCode((input[i>>5] >>> (i % 32)) & 0xFF);
    return output;
}

MessageDigestAlgorithm.prototype.binl_md5=function(x, len)
{
    x[len >> 5] |= 0x80 << ((len) % 32);
    x[(((len + 64) >>> 9) << 4) + 14] = len;

    var a =  1732584193;
    var b = -271733879;
    var c = -1732584194;
    var d =  271733878;

    for(var i = 0; i < x.length; i += 16)
    {
        var olda = a;
        var oldb = b;
        var oldc = c;
        var oldd = d;

        a = this.md5_ff(a, b, c, d, x[i+ 0], 7 , -680876936);
        d = this.md5_ff(d, a, b, c, x[i+ 1], 12, -389564586);
        c = this.md5_ff(c, d, a, b, x[i+ 2], 17,  606105819);
        b = this.md5_ff(b, c, d, a, x[i+ 3], 22, -1044525330);
        a = this.md5_ff(a, b, c, d, x[i+ 4], 7 , -176418897);
        d = this.md5_ff(d, a, b, c, x[i+ 5], 12,  1200080426);
        c = this.md5_ff(c, d, a, b, x[i+ 6], 17, -1473231341);
        b = this.md5_ff(b, c, d, a, x[i+ 7], 22, -45705983);
        a = this.md5_ff(a, b, c, d, x[i+ 8], 7 ,  1770035416);
        d = this.md5_ff(d, a, b, c, x[i+ 9], 12, -1958414417);
        c = this.md5_ff(c, d, a, b, x[i+10], 17, -42063);
        b = this.md5_ff(b, c, d, a, x[i+11], 22, -1990404162);
        a = this.md5_ff(a, b, c, d, x[i+12], 7 ,  1804603682);
        d = this.md5_ff(d, a, b, c, x[i+13], 12, -40341101);
        c = this.md5_ff(c, d, a, b, x[i+14], 17, -1502002290);
        b = this.md5_ff(b, c, d, a, x[i+15], 22,  1236535329);

        a = this.md5_gg(a, b, c, d, x[i+ 1], 5 , -165796510);
        d = this.md5_gg(d, a, b, c, x[i+ 6], 9 , -1069501632);
        c = this.md5_gg(c, d, a, b, x[i+11], 14,  643717713);
        b = this.md5_gg(b, c, d, a, x[i+ 0], 20, -373897302);
        a = this.md5_gg(a, b, c, d, x[i+ 5], 5 , -701558691);
        d = this.md5_gg(d, a, b, c, x[i+10], 9 ,  38016083);
        c = this.md5_gg(c, d, a, b, x[i+15], 14, -660478335);
        b = this.md5_gg(b, c, d, a, x[i+ 4], 20, -405537848);
        a = this.md5_gg(a, b, c, d, x[i+ 9], 5 ,  568446438);
        d = this.md5_gg(d, a, b, c, x[i+14], 9 , -1019803690);
        c = this.md5_gg(c, d, a, b, x[i+ 3], 14, -187363961);
        b = this.md5_gg(b, c, d, a, x[i+ 8], 20,  1163531501);
        a = this.md5_gg(a, b, c, d, x[i+13], 5 , -1444681467);
        d = this.md5_gg(d, a, b, c, x[i+ 2], 9 , -51403784);
        c = this.md5_gg(c, d, a, b, x[i+ 7], 14,  1735328473);
        b = this.md5_gg(b, c, d, a, x[i+12], 20, -1926607734);

        a = this.md5_hh(a, b, c, d, x[i+ 5], 4 , -378558);
        d = this.md5_hh(d, a, b, c, x[i+ 8], 11, -2022574463);
        c = this.md5_hh(c, d, a, b, x[i+11], 16,  1839030562);
        b = this.md5_hh(b, c, d, a, x[i+14], 23, -35309556);
        a = this.md5_hh(a, b, c, d, x[i+ 1], 4 , -1530992060);
        d = this.md5_hh(d, a, b, c, x[i+ 4], 11,  1272893353);
        c = this.md5_hh(c, d, a, b, x[i+ 7], 16, -155497632);
        b = this.md5_hh(b, c, d, a, x[i+10], 23, -1094730640);
        a = this.md5_hh(a, b, c, d, x[i+13], 4 ,  681279174);
        d = this.md5_hh(d, a, b, c, x[i+ 0], 11, -358537222);
        c = this.md5_hh(c, d, a, b, x[i+ 3], 16, -722521979);
        b = this.md5_hh(b, c, d, a, x[i+ 6], 23,  76029189);
        a = this.md5_hh(a, b, c, d, x[i+ 9], 4 , -640364487);
        d = this.md5_hh(d, a, b, c, x[i+12], 11, -421815835);
        c = this.md5_hh(c, d, a, b, x[i+15], 16,  530742520);
        b = this.md5_hh(b, c, d, a, x[i+ 2], 23, -995338651);

        a = this.md5_ii(a, b, c, d, x[i+ 0], 6 , -198630844);
        d = this.md5_ii(d, a, b, c, x[i+ 7], 10,  1126891415);
        c = this.md5_ii(c, d, a, b, x[i+14], 15, -1416354905);
        b = this.md5_ii(b, c, d, a, x[i+ 5], 21, -57434055);
        a = this.md5_ii(a, b, c, d, x[i+12], 6 ,  1700485571);
        d = this.md5_ii(d, a, b, c, x[i+ 3], 10, -1894986606);
        c = this.md5_ii(c, d, a, b, x[i+10], 15, -1051523);
        b = this.md5_ii(b, c, d, a, x[i+ 1], 21, -2054922799);
        a = this.md5_ii(a, b, c, d, x[i+ 8], 6 ,  1873313359);
        d = this.md5_ii(d, a, b, c, x[i+15], 10, -30611744);
        c = this.md5_ii(c, d, a, b, x[i+ 6], 15, -1560198380);
        b = this.md5_ii(b, c, d, a, x[i+13], 21,  1309151649);
        a = this.md5_ii(a, b, c, d, x[i+ 4], 6 , -145523070);
        d = this.md5_ii(d, a, b, c, x[i+11], 10, -1120210379);
        c = this.md5_ii(c, d, a, b, x[i+ 2], 15,  718787259);
        b = this.md5_ii(b, c, d, a, x[i+ 9], 21, -343485551);

        a = this.safe_add(a, olda);
        b = this.safe_add(b, oldb);
        c = this.safe_add(c, oldc);
        d = this.safe_add(d, oldd);
    }
    return Array(a, b, c, d);
}

MessageDigestAlgorithm.prototype.md5_cmn=function(q, a, b, x, s, t)
{
    return this.safe_add(this.bit_rol(this.safe_add(this.safe_add(a, q), this.safe_add(x, t)), s),b);
}

MessageDigestAlgorithm.prototype.md5_ff=function(a, b, c, d, x, s, t)
{
    return this.md5_cmn((b & c) | ((~b) & d), a, b, x, s, t);
}

MessageDigestAlgorithm.prototype.md5_gg=function(a, b, c, d, x, s, t)
{
    return this.md5_cmn((b & d) | (c & (~d)), a, b, x, s, t);
}

MessageDigestAlgorithm.prototype.md5_hh=function(a, b, c, d, x, s, t)
{
    return this.md5_cmn(b ^ c ^ d, a, b, x, s, t);
}

MessageDigestAlgorithm.prototype.md5_ii=function(a, b, c, d, x, s, t)
{
    return this.md5_cmn(c ^ (b | (~d)), a, b, x, s, t);
}

MessageDigestAlgorithm.prototype.safe_add=function(x, y)
{
    var lsw = (x & 0xFFFF) + (y & 0xFFFF);
    var msw = (x >> 16) + (y >> 16) + (lsw >> 16);
    return (msw << 16) | (lsw & 0xFFFF);
}
MessageDigestAlgorithm.prototype.bit_rol=function(num, cnt)
{
    return (num << cnt) | (num >>> (32 - cnt));
}/*
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
 *  Implementation of the JAIN-SIP  GenericURI.
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *  @see  gov/nist/javax/sip/address/GenericURI.java  
 */


function GenericURI(uriString) {
    this.classname="GenericURI"; 
    this.serialVersionUID = "3237685256878068790L";
    this.uriString=null;
    this.scheme=null;
    if(uriString!=null)
    {
        this.uriString = uriString;
        var i = uriString.indexOf(":");
        this.scheme = uriString.substring(0, i);
    }
}

GenericURI.prototype.SIP="sip";
GenericURI.prototype.SIPS="sips";
GenericURI.prototype.POSTDIAL = "postdial";
GenericURI.prototype.PHONE_CONTEXT_TAG ="context-tag";
GenericURI.prototype.ISUB = "isub";
GenericURI.prototype.PROVIDER_TAG = "provider-tag";
GenericURI.prototype.TEL="tel";

GenericURI.prototype.encode=function(){
    return this.uriString;
}

GenericURI.prototype.encodeBuffer=function(buffer){
    buffer=buffer+this.uriString;
    return buffer;
}

GenericURI.prototype.toString=function(){
    return this.encode();
}

GenericURI.prototype.getScheme=function(){
    return this.scheme;
}

GenericURI.prototype.isSipURI=function(){
    if(this instanceof SipUri)
    {
        return true;
    }
    else
    {
        return false;
    }
}

GenericURI.prototype.equals=function(that){
    if (this==that) {
        return true;
    }
    else if (that instanceof URI) {
        var o = that;
        // This is not sufficient for equality; revert to String equality...
        // return this.getScheme().equalsIgnoreCase( o.getScheme() )
        if(this.toString().toLowerCase()==o.toString().toLowerCase())
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

GenericURI.prototype.hashCode=function(){
    var hash = 0;
    var x=this.toString();
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
 *  Implementation of the JAIN-SIP  UserInfo.
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *  @see  gov/nist/javax/sip/address/UserInfo.java  
 */
function UserInfo() {
    this.serialVersionUID = "7268593273924256144L";
    this.user =null;
    this.password=null;
    this.userType = null;
    this.classname="UserInfo"; 
}

UserInfo.prototype.TELEPHONE_SUBSCRIBER =1;
UserInfo.prototype.USER=2;
UserInfo.prototype.COLON=":";
UserInfo.prototype.POUND="#";
UserInfo.prototype.SEMICOLON=";"

UserInfo.prototype.equals =function(obj){
    if (obj.classname!="UserInfo") {
        return false;
    }
    var other = new UserInfo();
    other=obj;
    if (this.userType != other.userType) {
        return false;
    }
    if (this.user!=other.user) {
        return false;
    }
    if (this.password != null && other.password == null) {
        return false;
    } 
    if (other.password != null && this.password == null){
        return false;
    }
    return this.password == other.password;  
}

UserInfo.prototype.encode =function(){
    return this.encodeBuffer("").toString();
}

UserInfo.prototype.encodeBuffer =function(buffer){
    var encode=null;
    if(this.password!=null)
    {
        encode=buffer+this.user+this.COLON+this.password;
    }
    else
    {
        encode=buffer+this.user;
    }
    
    return encode;
}

UserInfo.prototype.clearPassword =function(){
    this.password=null;
}

UserInfo.prototype.getUserType =function(){
    return this.userType;
}

UserInfo.prototype.getUser =function(){
    return this.user;
}

UserInfo.prototype.setUser =function(user){
    this.user=user;
    if (user != null
        && (user.indexOf(this.POUND) >= 0 || user.indexOf(this.SEMICOLON) >= 0)) {
        this.setUserType(this.TELEPHONE_SUBSCRIBER);
    } else {
        this.setUserType(this.USER);
    }
}

UserInfo.prototype.getPassword =function(){
    return this.password;
}

UserInfo.prototype.setPassword =function(p){
    this.password=p;
}

UserInfo.prototype.setUserType =function(type){
    if (type != this.USER && type != this.TELEPHONE_SUBSCRIBER) {
        console.error("UserInfo:setUserType(): parameter not in range");
        throw "UserInfo:setUserType(): parameter not in range";
    }
    this.userType = type;
}
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
 *  Implementation of the JAIN-SIP  Authority.
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *  @see  gov/nist/javax/sip/address/Authority.java  
 */

function Authority() {
    this.classname="Authority"; 
    this.serialVersionUID = "-3570349777347017894L";
    this.userInfo =null;
    this.hostPort=new HostPort();
}

Authority.prototype.AT="@";

Authority.prototype.encode=function(){
    return this.encodeBuffer("");
}

Authority.prototype.encodeBuffer=function(buffer){
    if (this.userInfo != null) {
        buffer=this.userInfo.encodeBuffer(buffer);
        buffer=buffer+this.AT;
        buffer=this.hostPort.encodeBuffer(buffer);
    } else {
        buffer=this.hostPort.encodeBuffer(buffer);
    }
    return buffer;
}

Authority.prototype.equals=function(other){
    if (other == null) {
        return false;
    }
    if (other.classname != this.classname) {
        return false;
    }
    var otherAuth = other;
    if (this.hostPort!=otherAuth.hostPort) {
        return false;
    }
    if (this.userInfo != null && otherAuth.userInfo != null) {
        if (this.userInfo!=otherAuth.userInfo) {
            return false;
        }
    }
    return true;
}

Authority.prototype.getHostPort=function(){
    return this.hostPort;
}

Authority.prototype.getUserInfo=function(){
    return this.userInfo;
}

Authority.prototype.getPassword=function(){
    if (this.userInfo == null)
    {
        return null;
    }
    else
    {
        return this.userInfo.password;
    }    
}

Authority.prototype.getUser=function(){
    return this.userInfo != null ? this.userInfo.user : null;
}

Authority.prototype.getHost=function(){
    if (this.hostPort == null)
    {
        return null;
    }
    else
    {
        return this.hostPort.getHost();
    }
}

Authority.prototype.getPort=function(){
    if (this.hostPort == null)
    {
        return -1;
    }
    else
    {
        return this.hostPort.getPort();
    }
}

Authority.prototype.removePort=function(){
    if (this.hostPort != null)
    {
        this.hostPort.removePort();
    }
}

Authority.prototype.setPassword=function(passwd){
    if (this.userInfo == null)
    {
        this.userInfo = new UserInfo();
    }
    this.userInfo.setPassword(passwd);
}

Authority.prototype.setUser=function(user){
    if (this.userInfo == null)
    {
        this.userInfo = new UserInfo();
    }
    this.userInfo.setUser(user);
}

Authority.prototype.setHost=function(host){
    if (this.hostPort == null)
    {
        this.hostPort = new HostPort();
    }
    this.hostPort.setHost(host);
}

Authority.prototype.setPort=function(port){
    if (this.hostPort == null)
    {
        this.hostPort = new HostPort();
    }
    this.hostPort.setPort(port);
}

Authority.prototype.setHostPort=function(h){
    this.hostPort=h;
}

Authority.prototype.setUserInfo=function(u){
    this.userInfo=u;
}

Authority.prototype.removeUserInfo=function(){
    this.userInfo=null;
}

Authority.prototype.hashCode=function(){
    if ( this.hostPort == null ) 
    {
        console.error("Authority:hashCode(): null hostPort cannot compute hashcode");
        throw "Authority:hashCode(): null hostPort cannot compute hashcode";
    }
    return this.hostPort.encode().hashCode();
}
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
    this.serialVersionUID = "5873527320305915954L";
    this.classname="TelURLImpl";
    this.telephoneNumber=new TelephoneNumber();
    this.scheme = "tel";
}

TelURLImpl.prototype = new GenericURI();
TelURLImpl.prototype.constructor=TelURLImpl;

TelURLImpl.prototype.setTelephoneNumber =function(telephoneNumber){
    this.telephoneNumber = telephoneNumber;
}

TelURLImpl.prototype.getIsdnSubAddress =function(){
    return this.telephoneNumber.getIsdnSubaddress();
}

TelURLImpl.prototype.getPostDial =function(){
    return this.telephoneNumber.getPostDial();
}

TelURLImpl.prototype.getScheme =function(){
    return this.scheme;
}

TelURLImpl.prototype.isGlobal =function(){
    return this.telephoneNumber.isGlobal();
}

TelURLImpl.prototype.isSipURI =function(){
    return false;
}

TelURLImpl.prototype.setGlobal =function(global){
    this.telephoneNumber.setGlobal(global);
}

TelURLImpl.prototype.setIsdnSubAddress =function(isdnSubAddress){
    this.telephoneNumber.setIsdnSubaddress(isdnSubAddress);
}

TelURLImpl.prototype.setPostDial =function(postDial){
    this.telephoneNumber.setPostDial(postDial);
}

TelURLImpl.prototype.setPhoneNumber =function(telephoneNumber){
    this.telephoneNumber.setPhoneNumber(telephoneNumber);
} 

TelURLImpl.prototype.getPhoneNumber =function(){
    return this.telephoneNumber.getPhoneNumber();
}

TelURLImpl.prototype.toString =function(){
    return this.scheme + ":" + this.telephoneNumber.encode();
}

TelURLImpl.prototype.encode =function(){
    return this.encodeBuffer("").toString();
}

TelURLImpl.prototype.encodeBuffer =function(buffer){
    buffer=buffer+this.scheme+":";
    buffer=this.telephoneNumber.encodeBuffer(buffer);
    return buffer;
}

TelURLImpl.prototype.getParameter =function(parameterName){
    return this.telephoneNumber.getParameter(parameterName);
}

TelURLImpl.prototype.setParameter =function(name, value){
    this.telephoneNumber.setParameter(name, value);
}

TelURLImpl.prototype.getParameterNames =function(){
    return this.telephoneNumber.getParameterNames();
}

TelURLImpl.prototype.getParameters =function(){
    return this.telephoneNumber.getParameters();
}

TelURLImpl.prototype.removeParameter =function(name){
    this.telephoneNumber.removeParameter(name);
}

TelURLImpl.prototype.setPhoneContext =function(phoneContext){
    if (phoneContext==null) {
        this.removeParameter("phone-context");
    } 
    else 
    {
        this.setParameter("phone-context",phoneContext);
    }
}

TelURLImpl.prototype.getPhoneContext =function(){
    return this.getParameter("phone-context");
}

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
    this.parameters.delet(name);
}

TelephoneNumber.prototype.getPhoneNumber=function(){
    return this.phoneNumber;
}

TelephoneNumber.prototype.getPostDial=function(){
    return this.parameters.getValue(this.POSTDIAL);
}

TelephoneNumber.prototype.getIsdnSubaddress=function(){
    return this.parameters.getValue(this.ISUB);
}

TelephoneNumber.prototype.hasPostDial=function(){
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
    return this.isglobal;
}

TelephoneNumber.prototype.removePostDial=function(){
    this.parameters.delet(this.POSTDIAL);
}

TelephoneNumber.prototype.removeIsdnSubaddress=function(){
    this.deleteParm(this.ISUB);
}

TelephoneNumber.prototype.setParameters=function(p){
    this.parameters=p;
}

TelephoneNumber.prototype.setGlobal=function(g){
    this.isglobal=g;
}

TelephoneNumber.prototype.setPostDial=function(p){
    var nv=new NameValue(this.POSTDIAL,p);
    this.parameters.set_nv(nv);
}

TelephoneNumber.prototype.setParm=function(name, value){
    var nv = new NameValue(name, value);
    this.parameters.set_nv(nv);
}

TelephoneNumber.prototype.setIsdnSubaddress=function(isub){
    this.setParm(this.ISUB, isub);
}

TelephoneNumber.prototype.setPhoneNumber=function(num){
    this.phoneNumber=num;
}

TelephoneNumber.prototype.encode=function(){
    return this.encodeBuffer("").toString();
}

TelephoneNumber.prototype.encodeBuffer=function(buffer){
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
    return this.parameters.getNames();
}

TelephoneNumber.prototype.removeParameter=function(parameter){
    this.parameters.delet(parameter);
}

TelephoneNumber.prototype.setParameter=function(name, value){
    var nv = new NameValue(name, value);
    this.parameters.set_nv(nv);
}

TelephoneNumber.prototype.getParameters=function(){
    return this.parameters;
}

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
 *  Implementation of the JAIN-SIP  SipUri.
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *  @see  gov/nist/javax/sip/address/SipUri.java  
 */

function SipUri() {
    this.serialVersionUID = "7749781076218987044L";
    this.classname="SipUri";
    this.authority=new Authority();
    this.uriParms=new NameValueList();
    this.qheaders=new NameValueList;
    this.telephoneSubscriber=new TelephoneNumber();
    this.scheme = "sip";
    this.qheaders.setSeparator("&");
}

SipUri.prototype = new GenericURI();
SipUri.prototype.constructor=SipUri;
SipUri.prototype.SIP="sip";
SipUri.prototype.SIPS="sips";
SipUri.prototype.COLON=":";
SipUri.prototype.SEMICOLON=";";
SipUri.prototype.QUESTION="?";
SipUri.prototype.AT="@";
SipUri.prototype.METHOD="method";
SipUri.prototype.USER = "user";
SipUri.prototype.PHONE="phone";
SipUri.prototype.TTL="ttl";
SipUri.prototype.MADDR="maddr";
SipUri.prototype.TRANSPORT="transport";
SipUri.prototype.LR="lr";
SipUri.prototype.GRUU="gr";

/** Constructor given the scheme.
 * The scheme must be either Sip or Sips
 */
SipUri.prototype.setScheme =function(scheme){
    if (scheme.toLowerCase()!= this.SIP
        &&scheme.toLowerCase()!=  this.SIPS)
        {
        console.error("SipUri:setScheme(): bad scheme " + scheme);
        throw "SipUri:setScheme(): bad scheme " + scheme;
    }
    this.scheme = scheme.toLowerCase();
}

SipUri.prototype.getScheme =function(){
    return this.scheme;
}

SipUri.prototype.clearUriParms =function(){
    this.uriParms = new NameValueList();
}

SipUri.prototype.clearPassword =function(){
    if (this.authority != null) {
        var userInfo = this.authority.getUserInfo();
        if (userInfo != null)
        {
            userInfo.clearPassword();
        }
    }
}
SipUri.prototype.getAuthority =function(){
    return this.authority;
}

SipUri.prototype.clearQheaders =function(){
    this.qheaders = new NameValueList();
}

SipUri.prototype.equals =function(that){
    
    // Shortcut for same object
    if (that==this) {
        return true;
    }
    
    if (that instanceof SipUri) {
        var a = this;
        var b = that;
        
        // A SIP and SIPS URI are never equivalent
        if ( a.isSecure() ^ b.isSecure() ) 
        {
            return false;
        }
        // For two URIs to be equal, the user, password, host, and port
        // components must match; comparison of userinfo is case-sensitive
        if (a.getUser()==null ^ b.getUser()==null) 
        {
            return false;
        }
        if (a.getUserPassword()==null ^ b.getUserPassword()==null) 
        {
            return false;
        }
        if (a.getUser()!=null && (a.getUser()!=b.getUser())) 
        {
            return false;
        }
        if (a.getUserPassword()!=null && (a.getUserPassword()!=b.getUserPassword())) 
        {
            return false;
        }
        if (a.getHost() == null ^ b.getHost() == null) 
        {
            return false;
        }
        if (a.getHost() != null && !a.getHost().equalsIgnoreCase(b.getHost())) 
        {
            return false;
        }
        if (a.getPort() != b.getPort()) 
        {
            return false;
        }
        // URI parameters
        var array =a.getParameterNames();
        for (var i=0;i<array.length;i++) {
            var pname = array[i];
            var p1 = a.getParameter(pname);
            var p2 = b.getParameter(pname);
            // those present in both must match (case-insensitive)
            if (p1!=null && p2!=null && (p1!=p2)) {
                return false;
            }
        }
        // transport, user, ttl or method must match when present in either
        if (a.getTransportParam()==null ^ b.getTransportParam()==null) {
            return false;
        }
        if (a.getUserParam()==null ^ b.getUserParam()==null) {
            return false;
        }
        if (a.getTTLParam()==-1 ^ b.getTTLParam()==-1) {
            return false;
        }
        if (a.getMethodParam()==null ^ b.getMethodParam()==null) {
            return false;
        }
        if (a.getMAddrParam()==null ^ b.getMAddrParam()==null) {
            return false;
        }
        
        var arraya=a.getHeaderNames();
        var arrayb=b.getHeaderNames();
        // Headers: must match according to their definition.
        if(arraya[0] && !arrayb[0]) 
        {
            return false;
        }
        if(!arraya[0] && arrayb[0]) 
        {
            return false;
        }
        if(arraya[0] && arrayb[0]) {
            var sf=new SipFactory();
            var headerFactory = null;
            headerFactory = sf.createHeaderFactory();
            for (i=0;i<arraya.length;i++) {
                var hname = arraya[i];
                var h1 = a.getHeader(hname);
                var h2 = b.getHeader(hname);
                if(h1 == null && h2 != null) {
                    return false;
                }
                if(h2 == null && h1 != null) {
                    return false;
                }
                // The following check should not be needed but we add it for findbugs.
                if(h1 == null && h2 == null) {
                    continue;
                }
                var header1 = headerFactory.createHeader(hname, RFC2396UrlDecoder.decode(h1));
                var header2 = headerFactory.createHeader(hname, RFC2396UrlDecoder.decode(h2));
                if (header1!=header2) {
                    return false;
                }
            /////////////////////////////////////////////////////////////////////////////// var header1 = headerFactory.createHeader(hname, RFC2396UrlDecoder.decode(h1));
            /////////////////////////////////////////////////////////////////////////////// var header2 = headerFactory.createHeader(hname, RFC2396UrlDecoder.decode(h2));
            // those present in both must match according to the equals method of the corresponding header
            ////////////////////////////////////////////////////////////////////////////////if (!header1.equals(header2)) return false;
            }
        }
        // Finally, we can conclude that they are indeed equal
        return true;
    }
    return false;
}

SipUri.prototype.encode =function(){
    return this.encodeBuffer("");
}

SipUri.prototype.encodeBuffer =function(buffer){
    buffer=buffer+this.scheme+this.COLON;
    if (this.authority != null)
    {
        buffer=this.authority.encodeBuffer(buffer);
    }
    if (this.uriParms.hmap.length!=0) 
    {
        buffer=buffer+this.SEMICOLON;
        buffer=this.uriParms.encodeBuffer(buffer);
    }
    if (this.qheaders.hmap.length!=0) 
    {
        buffer=buffer+this.QUESTION;
        buffer=this.qheaders.encodeBuffer(buffer);
    }
    return buffer;
}

SipUri.prototype.encodeWithoutScheme =function(){
    var buffer="";
    if (this.authority != null)
    {
        buffer=this.authority.encodeBuffer(buffer);
    }
    if (this.uriParms.hmap.length!=0) 
    {
        buffer=buffer+this.SEMICOLON;
        buffer=this.uriParms.encodeBuffer(buffer);
    }
    if (this.qheaders.hmap.length!=0) 
    {
        buffer=buffer+this.QUESTION;
        buffer=this.qheaders.encodeBuffer(buffer);
    }
    return buffer;
}

SipUri.prototype.toString =function(){
    return this.encode();
}

SipUri.prototype.getUserAtHost =function(){
    var user = "";
    if (this.authority.getUserInfo() != null)
    {
        user = this.authority.getUserInfo().getUser();
    }
    var host = this.authority.getHost().encode();
    var s = null;
    if (user=="") {
        s = null;
    } else {
        s = ":"+this.AT;
    }
    s=s+host;
    return s;
}

SipUri.prototype.getUserAtHostPort =function(){
    var user = "";
    if (this.authority.getUserInfo() != null)
    {
        user = this.authority.getUserInfo().getUser();
    }
    
    var host = this.authority.getHost().encode();
    var port = this.authority.getPort();
    // If port not set assign the default.
    var s = null;
    if (user=="") {
    {
        s = null;
    }
    } 
    else {
    {
        s = ":"+this.AT;
    }
    }
    if (port != -1) {
    {
        s=s+host+this.COLON+port;
        return s;
    }
    } 
    else
    {
        s=s+host;
        return s;
    }
}

SipUri.prototype.getParm =function(parmname){
    var obj = this.uriParms.getValue(parmname);
    return obj;
}

SipUri.prototype.getMethod =function(){
    return this.getParm(this.METHOD);
}

SipUri.prototype.getParameters =function(){
    return this.uriParms;
}

SipUri.prototype.removeParameters=function(){
    this.uriParms = new NameValueList();
}

SipUri.prototype.getQheaders =function(){
    return this.qheaders;
}

SipUri.prototype.getUserType =function(){
    return this.uriParms.getValue(this.USER);
}

SipUri.prototype.getUserPassword =function(){
    if (this.authority == null)
    {
        return null;
    }
    return this.authority.getPassword();
}

SipUri.prototype.setUserPassword =function(password){
    if (this.authority == null)
    {
        this.authority = new Authority();
    }
    this.authority.setPassword(password);
}

SipUri.prototype.getTelephoneSubscriber =function(){
    if (this.telephoneSubscriber == null) {
        
        this.telephoneSubscriber = new TelephoneNumber();
    }
    return this.telephoneSubscriber;
}

SipUri.prototype.getHostPort =function(){
    if (this.authority == null ||  this.authority.getHost() == null )
    {
        return null;
    }
    else 
    {
        return  this.authority.getHostPort();
    }
}

SipUri.prototype.getPort =function(){
    var hp = this.getHostPort();
    if (hp == null)
    {
        return -1;
    }
    return hp.getPort();
}

SipUri.prototype.getHost =function(){
    if ( this.authority == null) {
        return null;
    }
    else if (this.authority.getHost() == null ) {
        return null;
    }
    else {
        return this.authority.getHost().encode();
    }
}

SipUri.prototype.isUserTelephoneSubscriber =function(){
    var usrtype = this.uriParms.getValue(this.USER);
    if (usrtype == null)
    {
        return false;
    }
    if(usrtype.toLowerCase()==this.PHONE)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SipUri.prototype.removeTTL =function(){
    if (this.uriParms != null)
    {
        this.uriParms.delet(this.TTL);
    }
}

SipUri.prototype.removeMAddr =function(){
    if (this.uriParms != null)
    {
        this.uriParms.delet(this.MADDR);
    }
}

SipUri.prototype.removeTransport =function(){
    if (this.uriParms != null)
    {
        this.uriParms.delet(this.TRANSPORT);
    }
}

SipUri.prototype.removeHeader =function(name){
    if (this.uriParms != null)
    {
        this.uriParms.delet(name);
    }
}

SipUri.prototype.removeHeaders =function(){
    this.qheaders = new NameValueList();
}

SipUri.prototype.removeUserType =function(){
    if (this.uriParms != null)
    {
        this.uriParms.delet(this.USER);
    }
}

SipUri.prototype.removePort =function(){
    this.authority.removePort();
}

SipUri.prototype.removeMethod =function(){
    if (this.uriParms != null)
    {
        this.uriParms.delet(this.METHOD);
    }
}

SipUri.prototype.setUser =function(uname){
    if (this.authority == null) 
    {
        this.authority = new Authority();
    }
    this.authority.setUser(uname);
}

SipUri.prototype.removeUser =function(){
    this.authority.removeUserInfo();
}

SipUri.prototype.setDefaultParm=function(name, value){
    if (this.uriParms.getValue(name) == null) 
    {
        var nv = new NameValue(name, value);
        this.uriParms.set_nv(nv);
    }
}

SipUri.prototype.setAuthority =function(authority){
    this.authority = authority;
}

SipUri.prototype.setHost_Host =function(h){
    if (this.authority == null)
    {
        this.authority = new Authority();
    }
    this.authority.setHost(h);
}

SipUri.prototype.setUriParms =function(parms){
    this.uriParms = parms;
}

SipUri.prototype.setUriParm =function(name, value){
    var nv = new NameValue(name, value);
    this.uriParms.set_nv(nv);
}

SipUri.prototype.setQheaders =function(parms){
    this.qheaders = parms;
}

SipUri.prototype.setMAddr =function(mAddr){
    var nameValue = this.uriParms.getNameValue(this.MADDR);
    var host = new Host();
    host.setAddress(mAddr);
    if (nameValue != null)
    {
        nameValue.setValueAsObject(host);
    }
    else {
        nameValue = new NameValue(this.MADDR, host);
        this.uriParms.set_nv(nameValue);
    }
}

SipUri.prototype.setUserParam =function(usertype){
    this.uriParms.set_name_value(this.USER, usertype);
}

SipUri.prototype.setMethod =function(method){
    this.uriParms.set_name_value(this.METHOD, method);
}

SipUri.prototype.setIsdnSubAddress =function(isdnSubAddress){
    if (this.telephoneSubscriber == null)
    {
        this.telephoneSubscriber = new TelephoneNumber();
    }
    this.telephoneSubscriber.setIsdnSubaddress(isdnSubAddress);
}

SipUri.prototype.setTelephoneSubscriber =function(tel){
    this.telephoneSubscriber = tel;
}

SipUri.prototype.setPort =function(p){
    if (this.authority == null)
    {
        this.authority = new Authority();
    }
    this.authority.setPort(p);
}

SipUri.prototype.hasParameter =function(name){
    if(this.uriParms.getValue(name) != null)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SipUri.prototype.setQHeader =function(nameValue){
    this.qheaders.set_nv(nameValue);
}
SipUri.prototype.setUriParameter =function(nameValue){
    this.uriParms.set_nv(nameValue);
}

SipUri.prototype.hasTransport =function(){
    if(this.hasParameter(this.TRANSPORT))
    {
        return true;
    }
    else
    {
        return false;
    }

}

SipUri.prototype.removeParameter =function(name){
    this.uriParms.delet(name);
}

SipUri.prototype.setHostPort =function(hostPort){
    if (this.authority == null) {
        this.authority = new Authority();
    }
    this.authority.setHostPort(hostPort);
}

SipUri.prototype.getHeader =function(name){
    return this.qheaders.getValue(name) != null
    ? this.qheaders.getValue(name)
    : null;
}

SipUri.prototype.getHeaderNames=function(){
    return this.qheaders.getNames();
}

SipUri.prototype.getLrParam=function(){
    var haslr = this.hasParameter(this.LR);
    return haslr ? "true" : null;
}

SipUri.prototype.getMAddrParam=function(){
    var maddr = this.uriParms.getNameValue(this.MADDR);
    if (maddr == null)
    {
        return null;
    }
    //var host = maddr.getValueAsObject().encode();//here we should add the method encode, else host is an object not type of String.
    var host = maddr.getValueAsObject();
    if(typeof host=="object")
    {
        host=host.encode();
    }
    return host;
}

SipUri.prototype.getMethodParam=function(){
    return this.getParameter(this.METHOD);
}

SipUri.prototype.getParameter=function(name){
    var val = this.uriParms.getValue(name);
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
        return val;
    }
}

SipUri.prototype.getParameterNames=function(){
    return this.uriParms.getNames();
}

SipUri.prototype.getTTLParam=function(){
    var ttl = this.uriParms.getValue("ttl");
    if (ttl != null)
    {
        ttl=ttl-0
        return ttl;
    }
    else
    {
        return -1;
    }
}

SipUri.prototype.getTransportParam=function(){
    if (this.uriParms != null) 
    {
        return this.uriParms.getValue(this.TRANSPORT);
    } 
    else
    {
        return null;
    }
}

SipUri.prototype.getUser=function(){
    return this.authority.getUser();
}

SipUri.prototype.isSecure=function(){
    if(this.getScheme().toLowerCase()==this.SIPS)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SipUri.prototype.isSipURI=function(){
    return true;
}

SipUri.prototype.setHeader=function(name, value){
    var nv = new NameValue(name, value);
    this.qheaders.set_nv(nv);
}

//in order to make difference between two setHost(), i have named them setHost_String et setHost_Host
SipUri.prototype.setHost_String=function(host){
    var h = new Host(host);
    this.setHost_Host(h);
}

SipUri.prototype.setLrParam=function(){
    this.uriParms.set_name_value("lr",null);
}

SipUri.prototype.setMAddrParam=function(maddr){
    if (maddr == null)
    {
        console.error("SipUri:setMAddrParam(): bad maddr");
        throw "SipUri:setMAddrParam(): bad maddr";
    }
    this.setParameter("maddr", maddr);
}

SipUri.prototype.setMethodParam=function(method){
    this.setParameter("method", method);
}

SipUri.prototype.setParameter=function(name, value){
    if (name.toLowerCase()=="ttl") {
        value=value-0;
    }
    this.uriParms.set_name_value(name,value);
}

SipUri.prototype.setSecure=function(secure){
    if (secure)
    {
        this.scheme = this.SIPS;
    }
    else
    {
        this.scheme = this.SIP;
    }   
}

SipUri.prototype.setTTLParam=function(ttl){
    if (ttl <= 0)
    {
        console.error("SipUri:setTTLParam(): Bad ttl value");
        throw "SipUri:setTTLParam(): Bad ttl value";
    }
    
    if (this.uriParms != null) {
        var nv = new NameValue("ttl", ttl.valueOf());
        this.uriParms.set_nv(nv);
    }
}

SipUri.prototype.setTransportParam=function(transport){
    if (transport == null)
    {
        console.error("SipUri:setTransportParam():  null transport arg");
        throw "SipUri:setTransportParam(): null transport arg";
    }
    
    if (transport=="UDP" == 0
        || transport=="TLS" == 0
        || transport=="TCP" == 0
        || transport=="SCTP" == 0
        || transport=="WS" == 0) {
        var nv = new NameValue(this.TRANSPORT, transport.toLowerCase());
        this.uriParms.set_nv(nv);
    } 
    else
    {
        console.error("SipUri:setTransportParam():  bad transport " + transport);
        throw "SipUri:setTransportParam(): bad transport " + transport;
    }
}

SipUri.prototype.getUserParam=function(){
    return this.getParameter("user");
}

SipUri.prototype.hasLrParam=function(){
    if(this.uriParms.getNameValue("lr") != null)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SipUri.prototype.hasGrParam=function(){
    if(this.uriParms.getNameValue(this.GRUU) != null)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SipUri.prototype.setGrParam=function(value){
    this.uriParms.set_name_value(this.GRUU, value);
}

SipUri.prototype.getGrParam=function(){
    return this.uriParms.getValue(this.GRUU);
}

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
    if (!(this.address instanceof SipUri))
    {
        console.error("AddressImpl:getHostPort(): address is not a SipUri");
        throw "AddressImpl:getHostPort(): address is not a SipUri";
    }
    var uri = this.address;
    return uri.getHostPort();
}

AddressImpl.prototype.getPort =function(){
    if (!(this.address instanceof SipUri))
    {
        console.error("AddressImpl:getPort(): address is not a SipUri");
        throw "AddressImpl:getPort(): address is not a SipUri";
    }
    var uri = this.address;
    return uri.getHostPort().getPort();
}

AddressImpl.prototype.getUserAtHostPort =function(){
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
    if (!(this.address instanceof SipUri))
    {
        console.error("AddressImpl:getHost(): address is not a SipUri");
        throw "AddressImpl:getHost(): address is not a SipUri";
    }
    var uri = this.address;
    return uri.getHostPort().getHost().getHostname();
}

AddressImpl.prototype.removeParameter =function(parameterName){
    if (!(this.address instanceof SipUri))
    {
        console.error("AddressImpl:removeParameter(): address is not a SipUri");
        throw "AddressImpl:removeParameter(): address is not a SipUri";
    }
    var uri = this.address;
    uri.removeParameter(parameterName);
}

AddressImpl.prototype.encode =function(){
    return this.encodeBuffer("");
}

AddressImpl.prototype.encodeBuffer =function(buffer){
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
    return this.addressType;
}

AddressImpl.prototype.setAddressType =function(atype){
    this.addressType = atype;
}

AddressImpl.prototype.getDisplayName =function(){
    return this.displayName;
}

AddressImpl.prototype.setDisplayName =function(displayName){
    this.displayName = displayName;
    this.addressType = this.NAME_ADDR;
}

AddressImpl.prototype.setAddress =function(address){
    this.address = address;
}

AddressImpl.prototype.hashCode =function(){
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
    this.displayName = null;
}

AddressImpl.prototype.isSIPAddress =function(){
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
 
    return this.address;
}
AddressImpl.prototype.isWildcard =function(){
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
    this.address = address;
    
}

AddressImpl.prototype.setUser=function(user){
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
    this.addressType = this.WILD_CARD;
    this.address = new SipUri();
    this.address.setUser("*");
}

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
 *  Implementation of the JAIN-SIP address factory.
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *  @see  gov/nist/javax/sip/address/AddressFactoryImpl.java 
 *   
 */
 
function AddressFactoryImpl() {
    this.classname="AddressFactoryImpl";
}

AddressFactoryImpl.prototype.createAddress =function(){
    if(arguments.length==0)
    {
        var addressImpl=new AddressImpl();
        return addressImpl;
    }
    else if(arguments.length==1)
    {
        if(typeof arguments[0]=="string")
        {
            var address=arguments[0];
            var smp=this.createAddress_address(address);
            return smp;
        }
        else if(typeof arguments[0]=="object")
        {
            var uri=arguments[0];
            var addressImpl=this.createAddress_uri(uri);
            return addressImpl;
        }
    }
    else if(arguments.length==2)
    {
        var displayName=arguments[0];
        uri=arguments[1];
        addressImpl=this.createAddress_name_uri(displayName,uri);
        return addressImpl;
    }
    else
    {
        console.error("AddressFactoryImpl:createAddress():  too many arg");
        throw "AddressFactoryImpl:createAddress():  too many arg";
    }
}

AddressFactoryImpl.prototype.createAddress_name_uri =function(displayName,uri){
    if (uri == null)
    {
        console.error("AddressFactoryImpl:createAddress_name_uri():  null uri arg");
        throw "AddressFactoryImpl:createAddress_name_uri():  null uri arg";
    }
    var addressImpl = new AddressImpl();
    if (displayName != null)
    {
        addressImpl.setDisplayName(displayName);
    }
    addressImpl.setURI(uri);
    return addressImpl;
}

AddressFactoryImpl.prototype.createSipURI =function(){
    if(arguments.length==0)
    {
        console.error("AddressFactoryImpl:createSipURI(): missing uri arg");
        throw "AddressFactoryImpl:createSipURI():  missing uri arg";
    } 
    else if(arguments.length==1)
    {
        var uri=arguments[0];
        if (uri == null) {
            console.error("AddressFactoryImpl:createSipURI():  null uri arg");
            throw "AddressFactoryImpl:createSipURI():  null uri arg";
        }
        var smp = new StringMsgParser();
        var sipUri = smp.parseSIPUrl(uri);
        return sipUri;
    }
    else if(arguments.length==2)
    {
        var user=arguments[0];
        var host=arguments[1];
        sipUri=this.createSipURI_user_host(user, host);
        return sipUri;
    }      
}

AddressFactoryImpl.prototype.createSipURI_user_host =function(user, host){
    if (host == null) {
        console.error("AddressFactoryImpl:createSipURI_user_host(): null host arg");
        throw "AddressFactoryImpl:createSipURI_user_host():  null host arg";
    }
    var uriString = "sip:";
    if (user != null) {
        uriString=uriString+user+"@";
    }
    //if host is an IPv6 string we should enclose it in sq brackets
    if (host.indexOf(':') != host.lastIndexOf(':')
        && host.trim().charAt(0) != '[') {
        host = '[' + host + ']';
    }
    uriString=uriString+host;
    var smp = new StringMsgParser();
    var sipUri = smp.parseSIPUrl(uriString.toString());
    return sipUri;
}

AddressFactoryImpl.prototype.createTelURL =function(uri){
    if (uri == null) {
        console.error("AddressFactoryImpl:createTelURL(): null uri arg");
        throw "AddressFactoryImpl:createTelURL():  null uri arg";
    }
    var telUrl = "tel:" + uri;
    var smp = new StringMsgParser();
    var timp = smp.parseUrl(telUrl);
    return timp;
        
}

AddressFactoryImpl.prototype.createAddress_uri =function(uri){
    if (uri == null) {
        console.error("AddressFactoryImpl:createAddress_uri(): null uri arg");
        throw "AddressFactoryImpl:createAddress_uri():  null uri arg";
    }
    var addressImpl = new AddressImpl();
    addressImpl.setURI(uri);
    return addressImpl;
}

AddressFactoryImpl.prototype.createAddress_address =function(address){
    if (address == null) {
        console.error("AddressFactoryImpl:createAddress_address(): null address arg");
        throw "AddressFactoryImpl:createAddress_address():  null address arg";
    }

    if (address.equals("*")) {
        var addressImpl = new AddressImpl();
        addressImpl.setAddressType(addressImpl.wild_card);
        var uri = new SipUri();
        uri.setUser("*");
        addressImpl.setURI(uri);
        return addressImpl;
    } else {
        var smp = new gov.nist.js.parser.StringMsgParser();
        return smp.parseAddress(address);
    }
}

AddressFactoryImpl.prototype.createURI =function(uri){
    if (uri == null) {
        console.error("AddressFactoryImpl:createURI(): null uri arg");
        throw "AddressFactoryImpl:createURI():  null uri arg";
    }
    var urlParser = new URLParser(uri);
    var scheme = urlParser.peekScheme();
    if (scheme == null) {
        console.error("AddressFactoryImpl:createURI(): bad scheme");
        throw "AddressFactoryImpl:createURI():  bad scheme"
    }
    if (scheme.toLowerCase()=="sip") {
        return urlParser.sipURL(true);
    } 
    else if (scheme.toLowerCase()=="sips") {
        return urlParser.sipURL(true);
    } 
    else if (scheme.toLowerCase()=="tel") {
        return urlParser.telURL(true);
    }
    return new GenericURI(uri);
}

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
 *  Implementation of the JAIN-SIP SIPObject .
 *  @see  gov/nist/javax/sip/header/SIPObject.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function SIPObject() {
    this.classname="SIPObject"; 
    //GenericObject.call(this);
}
SIPObject.prototype = new GenericObject();
SIPObject.prototype.constructor=SIPObject;

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
 *  Implementation of the JAIN-SIP SIPHeader .
 *  @see  gov/nist/javax/sip/header/SIPHeader.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function SIPHeader(headername) {
    this.serialVersionUID = "7749781076218987044L";
    this.classname="SIPHeader";
    this.headerName=null;
    if(headername!=null)
    {
        this.headerName=headername;
    }
}

SIPHeader.prototype.COLON=":";
SIPHeader.prototype.SP=" ";
SIPHeader.prototype.NEWLINE="\r\n";

SIPHeader.prototype.getHeaderName =function(){
    return this.headerName;
}

SIPHeader.prototype.getName =function(){
    return this.headerName;
}

SIPHeader.prototype.setHeaderName =function(hdrname){
    this.headerName=hdrname;
}

SIPHeader.prototype.getHeaderValue =function(){
    var encodedHdr = null;
    encodedHdr = this.encode();
    var buffer = encodedHdr;
    while (buffer.length > 0 && buffer.charAt(0) != ':') {
        buffer=buffer.substring(1);
    }
    if (buffer.length > 0)
    {
        buffer=buffer.substring(1);
    }
    return buffer.toString().replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
}

SIPHeader.prototype.isHeaderList =function(){
    return false;
}

SIPHeader.prototype.encode =function(){
    return this.encodeBuffer("").toString();
}

SIPHeader.prototype.encodeBuffer =function(buffer){
    buffer=buffer+this.headerName+this.COLON+this.SP;
    buffer=this.encodeBodyBuffer(buffer);
    buffer=buffer+this.NEWLINE;
    return buffer;
}

SIPHeader.prototype.encodeBody =function(){
}

SIPHeader.prototype.encodeBodyBuffer =function(buffer){
    buffer=buffer+this.encodeBody();
    return buffer;
}

SIPHeader.prototype.getValue =function(){
    return this.getHeaderValue();
}

SIPHeader.prototype.hashCode =function(){
    var hash = 0;
    var x=this.headerName;
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

SIPHeader.prototype.toString =function(){
    return this.encode();
}

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
 *  Implementation of the JAIN-SIP SIPHeaderList .
 *  @see  gov/nist/javax/sip/header/SIPHeaderList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function SIPHeaderList() {
    this.classname="SIPHeaderList";
    this.prettyEncode = false;
    this.hlist=new Array();
    this.myClass=null;
    if(arguments.length!=0)
    {
        var objclass=arguments[0];
        var hname=arguments[1];
        this.headerName = hname;
        this.myClass =objclass;
    }
}

SIPHeaderList.prototype = new SIPHeader();
SIPHeaderList.prototype.constructor=SIPHeaderList;
SIPHeaderList.prototype.NEWLINE="\r\n";
SIPHeaderList.prototype.WWW_AUTHENTICATE="WWW-Authenticate";
SIPHeaderList.prototype.PROXY_AUTHENTICATE="Proxy-Authenticate";
SIPHeaderList.prototype.AUTHORIZATION="Authorization";
SIPHeaderList.prototype.PROXY_AUTHORIZATION="Proxy-Authorization";
SIPHeaderList.prototype.ROUTE="Route";
SIPHeaderList.prototype.RECORD_ROUTE="Record-Route";
SIPHeaderList.prototype.VIA="Via";
SIPHeaderList.prototype.COLON=":";
SIPHeaderList.prototype.SP=" ";
SIPHeaderList.prototype.COMMA=",";
SIPHeaderList.prototype.SEMICOLON=";";

SIPHeaderList.prototype.getName =function(){
    return this.headerName;
}

SIPHeaderList.prototype.add =function(){
    if(arguments.length==1)
    {
        var objectToAdd=arguments[0];
        var l=null;
        for(var i=0;i<this.hlist.length;i++)
        {
            if(objectToAdd==this.hlist[i])
            {
                l=i;
            }
        }
        if(l==null)
        {
            this.hlist.push(objectToAdd);
        }
    }
    else if(typeof arguments[0]=="object")
    {
        var sipheader=arguments[0];
        var top=arguments[1];
        if (top)
        {
            this.addFirst(sipheader);
        }
        else
        {
            this.add(sipheader);
        }
    }
    else if(typeof arguments[0]=="number")
    {
        var index=arguments[0];
        var sipHeader=arguments[1];
        if(this.hlist[index]!=null)
        {
            var x=this.hlist.slice(index);
            this.hlist[index]=sipHeader;
            for(i=0;i<x.length;i++)
            {
                var n=index+i+1;
                this.hlist[n]=x[i];
            }
        }
        else
        {
            this.hlist[index]=sipHeader;
        }
    }
}

SIPHeaderList.prototype.addFirst =function(obj){
    this.hlist.unshift(obj); 
}

SIPHeaderList.prototype.concatenate =function(other, topFlag){
    if (!topFlag) 
    {
        this.addAll(other);
    } 
    else 
    {
        this.addAll(0, other);
    }
}

SIPHeaderList.prototype.encode =function(){
    return this.encodeBuffer("").toString();
}

SIPHeaderList.prototype.encodeBuffer =function(buffer){
    if (this.hlist.length==0) 
    {
        buffer=buffer+this.headerName+":"+this.NEWLINE;
    }
    else {
        if (this.headerName==this.WWW_AUTHENTICATE
            || this.headerName==this.PROXY_AUTHENTICATE
            || this.headerName==this.AUTHORIZATION
            || this.headerName==this.PROXY_AUTHORIZATION
            || (this.prettyEncode &&
                (this.headerName==this.VIA || this.headerName==this.ROUTE || this.headerName==this.RECORD_ROUTE)) // Less confusing to read
            || this.classname == "ExtensionHeaderList" ) 
            {
            for(var i=0;i<this.hlist.length;i++)
            {
                var sipheader = this.hlist[i];
                buffer=sipheader.encodeBuffer(buffer);
            }
        } 
        else 
        {
            buffer=buffer+this.headerName+this.COLON+this.SP;
            buffer=this.encodeBodyBuffer(buffer);
            buffer=buffer+this.NEWLINE;
        }
    }
    return buffer;
}

SIPHeaderList.prototype.getHeadersAsEncodedStrings =function(){
    var retval = new Array();
    for(var i=0;i<this.hlist.length;i++)
    {
        var sipheader = this.hlist[i];
        retval[i]=sipheader.toString();
    }
    return retval;
}

SIPHeaderList.prototype.getFirst =function(){
    if (this.hlist == null || this.hlist.length==0)
    {
        return null;
    }
    else
    {
        return  this.hlist[0];
    }
}

SIPHeaderList.prototype.getLast =function(){
    if (this.hlist == null || this.hlist.length==0)
    {
        return null;
    }
    var length=this.hlist.length;
    return this.hlist[length-1];
}

SIPHeaderList.prototype.getMyClass =function(){
    return  this.myClass;
/*here it return the name of the class not an object. the reason
     *i have explained on the top
     **/
}

SIPHeaderList.prototype.isEmpty =function(){
    if(this.hlist==0)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPHeaderList.prototype.listIterator =function(){
    if(arguments.length==0)
    {
        return this.hlist;
    }
    else
    {
        var position=arguments[0];
        var r=this.hlist;
        return r.slice(position);
    }
}

SIPHeaderList.prototype.getHeaderList =function(){
    return this.hlist;
}

SIPHeaderList.prototype.removeFirst =function(){
    if (this.hlist.length != 0)
    {
        this.hlist.splice(0,1);
    }
}

SIPHeaderList.prototype.removeLast =function(){
    if (this.hlist.length != 0)
    {
        var length=this.hlist.length-1;
        this.hlist.splice(length,1);
    }
}

SIPHeaderList.prototype.remove =function(obj){
    if(typeof obj=="number")
    {
        var x=this.hlist[obj]
        this.hlist.splice(obj,1);
        return x;
    }
    else if(typeof obj=="object")
    {
        if (this.hlist.length == 0)
        {
            return false;
        }
        else
        {
            var l=null;
            for(var i=0;i<this.hlist.length;i++)
            {
                if(this.hlist[i]==obj)
                {
                    l=i;
                }
            }
            if(l!=null)
            {
                this.hlist.splice(l,1);
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    return false;
}
    
SIPHeaderList.prototype.setMyClass =function(cl){//here cl is the name of the class not an object.
    this.myClass = cl;
}

SIPHeaderList.prototype.toArray =function(){
    return this.hlist;
}
SIPHeaderList.prototype.indexOf =function(gobj){
    var l=null;
    for(var i=0;i<this.hlist.length;i++)
    {
        if(this.hlist[i].classname.toLowerCase()==gobj.toLowerCase())
        {
            l=i;
        }
    }
    if(l!=null)
    {
        return l;
    }
    else
    {
        return -1;
    }
}

SIPHeaderList.prototype.size =function(){
    return this.hlist.length;
}

SIPHeaderList.prototype.isHeaderList =function(){
    return true;
}

SIPHeaderList.prototype.encodeBody =function(){
    return this.encodeBodyBuffer("").toString();
}

SIPHeaderList.prototype.encodeBodyBuffer =function(buffer){
    for(var i=0;i<this.hlist.length;i++)
    {
        var sipHeader = this.hlist[i];
        if ( sipHeader.classname=="SIPHeaderList"  ) 
        {
            console.error("SIPHeaderList:encodeBodyBuffer(): unexpected circularity in SipHeaderList");
            throw "SIPHeaderList:encodeBodyBuffer(): unexpected circularity in SipHeaderList";
        }
        buffer=sipHeader.encodeBodyBuffer(buffer);
        if (i!=this.hlist.length-1) 
        {
            if (this.headerName!="Privacy")
            {
                buffer=buffer+this.COMMA;
            }
            else
            {
                buffer=buffer+this.SEMICOLON;
            }
        }
    }
    return buffer;
}

SIPHeaderList.prototype.addAll =function(){
    if(arguments.length==1)
    {
        
        var collection=arguments[0];
        var length=this.hlist.length;
        for(var i=0;i<collection.length;i++)
        {
            var n=i+length;
            this.hlist[n]=collection[i];
        }
        if(this.hlist.length!=length)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    else if(arguments.length==2)
    {
        var index=arguments[0];
        collection=arguments[1];
        if(this.hlist[index]!=null)
        {
            var x=this.hlist.slice(index);
            length=this.hlist.length;
            for(i=0;i<collection.length;i++)
            {
                n=i+index;
                this.hlist[n]=collection[i];
            }
            for(i=0;i<x.length;i++)
            {
                n=index+i+collection.length;
                this.hlist[n]=x[i];
            }
            if(this.hlist.length!=length)
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
            index=arguments[0];
            collection=arguments[1];
            length=this.hlist.length;
            for(i=0;i<collection.length;i++)
            {
                n=i+index;
                this.hlist[n]=collection[i];
            }
            if(this.hlist.length!=length)
            {
                return true;
            }
            else
            {
                return false;
            }
        }
    }
    return false;
}

SIPHeaderList.prototype.containsAll =function(collection){
    var c=0;
    for(var i=0;i<collection.length;i++)
    {
        for(var n=0;n<this.hlist.length;n++)
        {
            if(this.hlist[n].classname==collection[i].classname)
            {
                c=c+1;
            }
        }
    }
    if(c==collection.length)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPHeaderList.prototype.clear =function(){
    this.hlist=new Array();
}

SIPHeaderList.prototype.contains =function(header){
    var c=0;
    for(var n=0;n<this.hlist.length;n++)
    {
        if(this.hlist[n].classname==header)
        {
            c=1;
        }
    }
    if(c==1)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPHeaderList.prototype.get =function(index){
    return this.hlist[index];
}

SIPHeaderList.prototype.iterator =function(){
    return this.hlist;
}

SIPHeaderList.prototype.lastIndexOf =function(obj){
    var c=null;
    for(var n=0;n<this.hlist.length;n++)
    {
        if(this.hlist[n].classname==obj)
        {
            c=n;
        }
    }
    if(c==null)
    {
        return c;
    }
    else
    {
        return -1;
    }
}

SIPHeaderList.prototype.removeAll =function(collection){
    var na=new Array();
    var c=0;
    var l=0;
    for(var i=0;i<this.hlist.length;i++)
    {
        for(var n=0;n<collection.length;n++)
        {
            if((this.hlist[i].classname!=collection[n].classname)&&(collection[n].classname!=null))
            {
                l=l+1;
            }
        }
        if(l==collection.length)
        {
            na[c]=this.hlist[i];
            c=c+1;
        }
        l=0;
    }
    if(this.hlist.length!=na.length)
    {
        this.hlist=na;
        return true;
    }
    else
    {
        return false;
    }
}

SIPHeaderList.prototype.retainAll =function(collection){
    var na=new Array();
    var c=0;
    for(var i=0;i<this.hlist.length;i++)
    {
        for(var n=0;n<collection.length;n++)
        {
            if((this.hlist[i].classname==collection[n].classname))
            {
                na[c]=this.hlist[i];
                c=c+1;
            }
        }
    }
    if(this.hlist.length!=na.length)
    {
        this.hlist=na;
        return true;
    }
    else
    {
        return false;
    }
}

SIPHeaderList.prototype.subList =function(index1, index2){
    return this.hlist.slice(index1,index2);
}

SIPHeaderList.prototype.hashCode =function(){
    var hash = 0;
    var set=this.headerName;
    if(!(set == null || set.value == ""))  
    {  
        for (var i = 0; i < set.length; i++)  
        {  
            hash = hash * 31 + set.charCodeAt(i);  
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

SIPHeaderList.prototype.set =function(position, sipHeader){
    var x=this.hlist[position];
    this.hlist[position]=sipHeader;
    return x;
}
SIPHeaderList.prototype.setPrettyEncode =function(flag){
    this.prettyEncode=flag;
}/*
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
    return this.parameters.getParameter(name);
}

ParametersHeader.prototype.getParameterValue =function(name){
    return this.parameters.getValue(name);
}

ParametersHeader.prototype.getParameterNames =function(){
    return this.parameters.getNames();
}

ParametersHeader.prototype.hasParameters =function(){
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
    this.parameters.delet(name);
}
ParametersHeader.prototype.setQuotedParameter =function(name, value){
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
    return this.parameters.hasNameValue(parameterName);
}

ParametersHeader.prototype.removeParameters =function(){
    this.parameters = new NameValueList();
}

ParametersHeader.prototype.getParameters =function(){
    return this.parameters;
}
ParametersHeader.prototype.setParameter_nv =function(nameValue){
    this.parameters.set_nv(nameValue);
}

ParametersHeader.prototype.setParameters =function(parameters){
    this.parameters = parameters;
}

ParametersHeader.prototype.getParameterAsNumber =function(parameterName){//i delete the other type of number. they are useless in javascript
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
    return this.parameters.getNameValue(parameterName);
}

ParametersHeader.prototype.setMultiParameter_name_value =function(name, value){
    var nv = new NameValue();
    nv.setName(name);
    nv.setValue(value);
    this.duplicates.set_nv(nv);
}

ParametersHeader.prototype.setMultiParameter_nv =function(nameValue){
    this.duplicates.set_nv(nameValue);
}

ParametersHeader.prototype.getMultiParameter =function(name){
    return this.duplicates.getParameter(name);
}

ParametersHeader.prototype.getMultiParameters =function(){
    return this.duplicates;
}

ParametersHeader.prototype.getMultiParameterValue =function(name){
    return this.duplicates.getValue(name);
}

ParametersHeader.prototype.getMultiParameterNames =function(){
    return this.duplicates.getNames();
}

ParametersHeader.prototype.hasMultiParameters =function(){
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
    this.duplicates.delet(name);
}

ParametersHeader.prototype.hasMultiParameter =function(parameterName){
    return this.duplicates.hasNameValue(parameterName);
}

ParametersHeader.prototype.removeMultiParameters =function(){
    this.duplicates = new DuplicateNameValueList();
}

ParametersHeader.prototype.equalParameters =function(other){
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
    
}/*
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
 *  Implementation of the JAIN-SIP RequestLine .
 *  @see  gov/nist/javax/sip/header/RequestLine.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function RequestLine(requestURI, method) {
    this.serialVersionUID = "-3286426172326043129L";
    this.classname="RequestLine";
    this.uri=new GenericURI();
    this.method=null;
    this.sipVersion= "SIP/2.0";
    if(requestURI!=null&&method!=null)
    {
        this.uri=requestURI;
        this.method=method;
    }
}

RequestLine.prototype = new SIPObject();
RequestLine.prototype.constructor=RequestLine;
RequestLine.prototype.SP=" ";
RequestLine.prototype.NEWLINE="\r\n";

RequestLine.prototype.encode =function(){
    return this.encodeBuffer("").toString();
}

RequestLine.prototype.encodeBuffer =function(buffer){
    if (this.method != null) {
        buffer=buffer+this.method+this.SP;
    }
    if (this.uri != null) {
        buffer=this.uri.encodeBuffer(buffer);
        buffer=buffer+this.SP;
    }
    buffer=buffer+this.sipVersion+this.NEWLINE;
    return buffer;
}

RequestLine.prototype.getUri =function(){
    return this.uri;
}

RequestLine.prototype.getMethod =function(){
    return this.method;
}

RequestLine.prototype.getSipVersion =function(){
    return this.sipVersion;
}

RequestLine.prototype.setUri =function(uri){
    return this.uri=uri;
}

RequestLine.prototype.setMethod =function(method){
    this.method=method
}

RequestLine.prototype.setSipVersion =function(version){
    this.sipVersion=version
}

RequestLine.prototype.getVersionMajor =function(){
    if (this.sipVersion == null)
        return null;
    var major = null;
    var slash = false;
    for (var i = 0; i < this.sipVersion.length; i++) {
        if (this.sipVersion.charAt(i) == '.')
        {
            break;
        }
        if (slash) 
        {
            if (major == null)
            {
                major = "" + this.sipVersion.charAt(i);
            }
            else
            {
                major += this.sipVersion.charAt(i);
            }
        }
        if (this.sipVersion.charAt(i) == '/')
        {
            slash = true;
        }
    }
    return major;
}

RequestLine.prototype.getVersionMinor =function(){
    if (this.sipVersion == null)
        return null;
    var minor = null;
    var dot = false;
    for (var i = 0; i < this.sipVersion.length; i++) {
        if (this.sipVersion.charAt(i) == '.')
        {
            break;
        }
        if (dot) 
        {
            if (minor == null)
            {
                minor = "" + this.sipVersion.charAt(i);
            }
            else
            {
                minor += this.sipVersion.charAt(i);
            }
        }
        if (this.sipVersion.charAt(i) == '/')
        {
            dot = true;
        }
    }
    return minor;
}/*
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
 *  Implementation of the JAIN-SIP UserAgent .
 *  @see  gov/nist/javax/sip/header/UserAgent.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function UserAgent() {
    this.serialVersionUID = "4561239179796364295L";
    this.classname="UserAgent";
    this.productTokens=new Array();
    this.headerName=this.NAME;
}

UserAgent.prototype = new SIPHeader();
UserAgent.prototype.constructor=UserAgent;
UserAgent.prototype.NAME="User-Agent";

UserAgent.prototype.encodeProduct =function(){
    var tokens = "";
    for(var i=0;i<this.productTokens.length;i++)
    {
        tokens=tokens+this.productTokens[i];
    }
    return tokens.toString();
}

UserAgent.prototype.addProductToken =function(pt){
    var x=this.productTokens.length;
    this.productTokens[x]=pt;
}

UserAgent.prototype.encodeBody =function(){
    return this.encodeProduct();
}

UserAgent.prototype.getProduct =function(){
    if (this.productTokens == null || this.productTokens.length==0)
    {
        return null;
    }
    else
    {
        return this.productTokens;
    }
}

UserAgent.prototype.setProduct =function(product){
    if (product == null)
    {
        console.error("UserAgent:setProduct(): the product parameter is null");
        throw "UserAgent:setProduct(): the product parameter is null"; 
    }
    this.productTokens = product;
}
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
 *  Implementation of the JAIN-SIP ContentLength .
 *  @see  gov/nist/javax/sip/header/ContentLength.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ContentLength(length) {
    this.serialVersionUID = "1187190542411037027L";
    this.classname="ContentLength";
    this.headerName=this.NAME;
    this.contentLength=null;
    if(length==null)
    {
        this.headerName=this.NAME;
    }
    else
    {
        this.headerName=this.NAME;
        this.contentLength=length;
    }
}

ContentLength.prototype = new SIPHeader();
ContentLength.prototype.constructor=ContentLength;
ContentLength.prototype.NAME="Content-Length";

ContentLength.prototype.getContentLength =function(){
    var x=this.contentLength-0;
    return x;
}

ContentLength.prototype.setContentLength =function(contentLength){
    if (contentLength < 0)
    {
        console.error("ContentLength:setContentLength(): the contentLength parameter is < 0");
        throw "ContentLength:setContentLength(): the contentLength parameter is < 0";
}
    this.contentLength = contentLength;
}

ContentLength.prototype.encodeBody =function(){
    return this.encodeBodyBuffer("").toString();
}

ContentLength.prototype.encodeBodyBuffer =function(buffer){
    if (this.contentLength == null)
    {
        buffer=buffer+"0";
    }
    else
    {
        buffer=buffer+this.contentLength.toString();
    }
    return buffer;
}

ContentLength.prototype.match =function(other){
    if (other instanceof ContentLength)
    {
        return true;
    }
    else
    {
        return false;
    }
}/*
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
 *  Implementation of the JAIN-SIP ExtensionHeaderImpl .
 *  @see  gov/nist/javax/sip/header/ExtensionHeaderImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function ExtensionHeaderImpl(headerName) {
    this.classname="ExtensionHeaderImpl"; 
    this.serialVersionUID = "-8693922839612081849L";
    this.value=null;
    if(headerName!=null)
    {
        this.headerName = headerName;
    }
}

ExtensionHeaderImpl.prototype = new SIPHeader();
ExtensionHeaderImpl.prototype.constructor=ExtensionHeaderImpl;
ExtensionHeaderImpl.prototype.COLON=":";
ExtensionHeaderImpl.prototype.SP=" ";
ExtensionHeaderImpl.prototype.NEWLINE="\r\n";

ExtensionHeaderImpl.prototype.setName =function(headerName){
    this.headerName = headerName;
}

ExtensionHeaderImpl.prototype.setValue =function(value){
    this.value = value;
}

ExtensionHeaderImpl.prototype.getHeaderValue =function(){
    if (this.value != null) {
        return this.value;
    } 
    else {
        var encodedHdr = null;
        try {
            encodedHdr = this.encode();
        } catch (ex) {
            console.error("ExtensionHeaderImpl:getHeaderValue(): catched exception:"+ex);
            return null;
        }
        var buffer = encodedHdr;
        var x=0;
        var chaine=buffer.substring(x);
        while (chaine.length > 0 && buffer.charAt(0) != ':') {
            x=x+1;
            chaine=buffer.substring(x);
        }
        x=x+1;
        chaine=buffer.substring(x);
        this.value = chaine.toString().replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
        return this.value;
    }
}

ExtensionHeaderImpl.prototype.encode =function(){
    var encode="";
    encode=encode+this.headerName+this.COLON+this.SP+this.value+this.NEWLINE;
    return encode;
}

ExtensionHeaderImpl.prototype.encodeBody =function(){
    return this.getHeaderValue();
}/*
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
 *  Implementation of the JAIN-SIP Server .
 *  @see  gov/nist/javax/sip/header/Server.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Server(to) {
    this.serialVersionUID = "-3587764149383342973L";
    this.classname="Server";
    this.productTokens = new Array();
    this.headerName=this.NAME;
}

Server.prototype = new SIPHeader();
Server.prototype.constructor=Server;
Server.prototype.NAME="Server";

Server.prototype.encodeProduct =function(){
    var tokens = "";
    for(var i=0;i<this.productTokens.length;i++)
    {
        tokens=tokens+this.productTokens[i];
        if (i!=this.productTokens.length-1)
        {
            tokens=tokens+"/";
        }
        else
        {
            break;
        }
    }
    return tokens.toString();
}

Server.prototype.addProductToken =function(pt){
    this.productTokens.push(pt);
}

Server.prototype.encodeBody =function(){
    return this.encodeProduct();
}

Server.prototype.getProduct =function(){
    if (this.productTokens == null || this.productTokens.length==0)
    {
        return null;
    }
    else
    {
        return this.productTokens;
    }
}

Server.prototype.setProduct =function(product){
    if (product == null)
    {
        console.error("Server:setProduct(): the product parameter is null");
        throw "Server:setProduct(): the product parameter is null";
    }
    this.productTokens = product;
}/*
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
    return this.address;
}

AddressParametersHeader.prototype.setAddress =function(address){
    this.address=address;
}

AddressParametersHeader.prototype.equals =function(other){
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
}/*
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
 *  Implementation of the JAIN-SIP From .
 *  @see  gov/nist/javax/sip/header/ExtensionHeaderImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function From(to) {
    this.serialVersionUID = "-6312727234330643892L";
    this.classname="From";
    this.parameters=null;
    this.duplicates=null;
    this.address = new AddressImpl();
    if(to==null)
    {
        this.headerName=this.NAME;
        this.parameters = new NameValueList();
        this.duplicates = new DuplicateNameValueList();
    }
    else
    {
        this.headerName=this.NAME;
        this.address = to.address;
        this.parameters = to.parameters;
    }
}

From.prototype = new AddressParametersHeader();
From.prototype.constructor=From;
From.prototype.NAME="From";
From.prototype.ADDRESS_SPEC = 2;
From.prototype.LESS_THAN="<";
From.prototype.GREATER_THAN=">";
From.prototype.SEMICOLON=";";
From.prototype.TAG="tag";

From.prototype.encodeBody =function(){
    return this.encodeBodyBuffer("").toString();
}

From.prototype.encodeBodyBuffer =function(buffer){
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

From.prototype.getHostPort =function(){
    return this.address.getHostPort();
}

From.prototype.getDisplayName =function(){
    return this.address.getDisplayName();
}

From.prototype.getTag =function(){
    if (this.parameters == null)
    {
        return null;
    }
    return this.getParameter(this.TAG);
}

From.prototype.hasTag =function(){
    return this.hasParameter(this.TAG);
}

From.prototype.removeTag =function(){
    this.parameters.delet(this.TAG);
}

From.prototype.setAddress =function(address){
    this.address=address;
}

From.prototype.setTag =function(t){
    var parser=new Parser();
    parser.checkToken(t);
    this.setParameter(this.TAG, t);
}

From.prototype.getUserAtHostPort =function(){
    return this.address.getUserAtHostPort();
}

From.prototype.equals =function(other){
    if((other instanceof From) && Object.getPrototypeOf(this).equals(other))
    {
        return true;
    }
    else
    {
        return false;
    }
}

From.prototype.getAddress=function(){
    return this.address;
}/*
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
 *  Implementation of the JAIN-SIP To .
 *  @see  gov/nist/javax/sip/header/To.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function To(from) {
    this.serialVersionUID = "-4057413800584586316L";
    this.classname="To";
    if(from==null)
    {
        this.headerName=this.NAME;
        this.parameters = new NameValueList();
        this.duplicates = new DuplicateNameValueList();
    }
    else
    {
        this.headerName=this.NAME;
        this.parameters = new NameValueList();
        this.duplicates = new DuplicateNameValueList();
        this.setAddress(from.address);
        this.setParameters(from.parameters);
    }
}

To.prototype = new AddressParametersHeader();
To.prototype.constructor=To;
To.prototype.NAME="To";
To.prototype.ADDRESS_SPEC = 2;
To.prototype.LESS_THAN="<";
To.prototype.GREATER_THAN=">";
To.prototype.SEMICOLON=";";
To.prototype.TAG="tag";
To.prototype.COLON=":";
To.prototype.SP=" ";
To.prototype.NEWLINE="\r\n";

To.prototype.encode =function(){
    return this.headerName + this.COLON + this.SP + this.encodeBody() + this.NEWLINE;
}

To.prototype.encodeBody =function(){
    return this.encodeBodyBuffer("").toString();
}

To.prototype.encodeBodyBuffer =function(buffer){
    if (this.address != null) {
        if (this.address.getAddressType() == this.ADDRESS_SPEC) {
            buffer=buffer+this.LESS_THAN;
        }
        buffer=this.address.encodeBuffer(buffer);
        if (this.address.getAddressType() == this.ADDRESS_SPEC) {
            buffer=buffer+this.GREATER_THAN;
        }
        if (!this.parameters.isEmpty()) {
            buffer=buffer+this.SEMICOLON
            buffer=this.parameters.encodeBuffer(buffer);
        }
    }
    return buffer;
}

To.prototype.getHostPort =function(){
    if (this.address == null)
    {
        return null;
    }
    return this.address.getHostPort();
}

To.prototype.getDisplayName =function(){
    if (this.address == null)
    {
        return null;
    }
    return this.address.getDisplayName();
}

To.prototype.getTag =function(){
    if (this.parameters == null)
    {
        return null;
    }
    return this.getParameter(this.TAG);
}

To.prototype.hasTag =function(){
    if (this.parameters == null)
    {
        return false;
    }
    return this.hasParameter(this.TAG);
}

To.prototype.removeTag =function(){
    if (this.parameters != null)
    {
        this.parameters.delet(this.TAG);
    }
}

To.prototype.setTag =function(t){
    var parser=new Parser();
    parser.checkToken(t);
    this.setParameter(this.TAG, t);
}

To.prototype.getUserAtHostPort =function(){
    if (this.address == null)
    {
        return null;
    }
    return this.address.getUserAtHostPort();
}/*
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
 *  Implementation of the JAIN-SIP Reason .
 *  @see  gov/nist/javax/sip/header/Reason.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Reason() {
    this.serialVersionUID = "-8903376965568297388L";
    this.classname="Reason";
    this.headerName=this.NAME;
    this.parameters = new NameValueList();
    this.duplicates = new DuplicateNameValueList();
    this.protocol=null;
}

Reason.prototype = new ParametersHeader();
Reason.prototype.constructor=Reason;
Reason.prototype.NAME= "Reason";
Reason.prototype.TEXT = "text";
Reason.prototype.CAUSE = "cause";
Reason.prototype.SEMICOLON = ";";

Reason.prototype.getCause =function(){
    return this.getParameterAsNumber(this.CAUSE);
}

Reason.prototype.setCause =function(cause){
    this.parameters.set_name_value("cause", cause);
}

Reason.prototype.setProtocol =function(protocol){
    this.protocol = protocol;
}

Reason.prototype.getProtocol =function(){
    return this.protocol;
}

Reason.prototype.setText =function(text){
    if ( text.charAt(0) != '"' ) {
        var utils=new Utils();
        text = utils.getQuotedString(text);
    }
    this.parameters.set_name_value("text", text);
}

Reason.prototype.getText =function(){
    return this.parameters.getParameter("text");
}

Reason.prototype.getName =function(){
    return this.NAME;
}

Reason.prototype.encodeBody =function(){
    var s = "";
    s=s+this.protocol;
    if (this.parameters != null && this.parameters.hmap.length!=0)
    {
        s=s+this.SEMICOLON+this.parameters.encode()
    }
    return s.toString();
}/*
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
 *  Implementation of the JAIN-SIP ReasonList .
 *  @see  gov/nist/javax/sip/header/ReasonList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ReasonList() {
    this.serialVersionUID = "7459989997463160670L";
    this.classname="ReasonList";
    this.headerName = this.NAME;
    this.myClass = "Reason";
}

ReasonList.prototype = new SIPHeaderList();
ReasonList.prototype.constructor=ReasonList;
ReasonList.prototype.NAME="Reason";

ReasonList.prototype.clone =function(){
}

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
 *  Implementation of the JAIN-SIP Protocol .
 *  @see  gov/nist/javax/sip/header/Protocol.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Protocol() {
    this.serialVersionUID = "2216758055974073280L";
    this.classname="Protocol";
    this.protocolName="SIP";
    this.protocolVersion="2.0";
    this.transport="WS";
}

Protocol.prototype = new SIPObject();
Protocol.prototype.constructor=Protocol;
Protocol.prototype.SLASH="/";

Protocol.prototype.encode =function(){
    return this.encodeBuffer("").toString();
}

Protocol.prototype.encodeBuffer =function(buffer){
    buffer=buffer+this.protocolName.toUpperCase()+this.SLASH+this.protocolVersion+this.SLASH+this.transport.toUpperCase();
    return buffer;
}

Protocol.prototype.getProtocolName =function(){
    return this.protocolName;
}

Protocol.prototype.getProtocolVersion =function(){
    return this.protocolVersion;
}

Protocol.prototype.getProtocol =function(){
    return this.protocolName + "/" + this.protocolVersion;
}

Protocol.prototype.setProtocol =function(name_and_version){
    var slash = name_and_version.indexOf('/');
    if (slash>0) {
        this.protocolName = name_and_version.substring(0,slash);
        this.protocolVersion = name_and_version.substring( slash+1 );
    } 
    else 
    {
        console.error("Event:setProtocol(): missing '/' in protocol", 0 );
        throw "Event:setProtocol(): missing '/' in protocol";
    }
}

Protocol.prototype.getTransport =function(){
    return this.transport;
}

Protocol.prototype.setProtocolName =function(pn){
    this.protocolName=pn;
}

Protocol.prototype.setProtocolVersion =function(p){
    this.protocolVersion=p
}

Protocol.prototype.setTransport =function(t){
    this.transport=t;
}
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
 *  Implementation of the JAIN-SIP Via .
 *  @see  gov/nist/javax/sip/header/Via.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Via() {
    this.serialVersionUID = "5281728373401351378L";
    this.classname="Via";
    this.sentProtocol=new Protocol();
    this.sentBy=new HostPort();
    this.comment=null;
    this.rPortFlag = true;
    this.headerName=this.NAME;
    this.parameters = new NameValueList();
    this.duplicates = new DuplicateNameValueList();
}

Via.prototype = new ParametersHeader();
Via.prototype.constructor=Via;
Via.prototype.NAME="Via";
Via.prototype.BRANCH="branch";
Via.prototype.RECEIVED="received";
Via.prototype.MADDR="maddr";
Via.prototype.TTL="ttl";
Via.prototype.RPORT="rport";
Via.prototype.SP=" ";
Via.prototype.SEMICOLON=";";
Via.prototype.LPAREN="(";
Via.prototype.RPAREN=")";


Via.prototype.getProtocolVersion =function(){
    if (this.sentProtocol == null)
    {
        return null;
    }
    else
    {
        return this.sentProtocol.getProtocolVersion();
    }
}

Via.prototype.getSentProtocol =function(){
    return this.sentProtocol;
}

Via.prototype.getSentBy =function(){
    return this.sentBy;
}

Via.prototype.getHop =function(){
    var hop = new HopImpl(this.sentBy.getHost().getHostname());
    return hop;
}

Via.prototype.getViaParms =function(){
    return this.parameters;
}
Via.prototype.hasPort =function(){
    return this.getSentBy().hasPort();
}

Via.prototype.hasComment =function(){
    if(this.comment != null)
    {
        return true;
    }
    else
    {
        return false;
    }
}

Via.prototype.removePort =function(){
    this.sentBy.removePort();
}

Via.prototype.removeComment =function(){
    this.comment = null;
}

Via.prototype.setProtocolVersion =function(){
    if (this.sentProtocol == null)
    {
        this.sentProtocol = new Protocol();
    }
    this.sentProtocol.setProtocolVersion(this.protocolVersion);
}

Via.prototype.setHost =function(host){
    if(typeof host=="object")
    {
        if (this.sentBy == null) {
            this.sentBy = new HostPort();
        }
        this.sentBy.setHost(host);
    }
    else if(typeof host=="string")
    {
        if (this.sentBy == null)

        {
            this.sentBy = new HostPort();
        }
        var h = new Host(host);
        this.sentBy.setHost(h);
    }
    
}

Via.prototype.setSentProtocol =function(s){
    this.sentProtocol = s;
}

Via.prototype.setSentBy =function(s){
    this.sentBy = s;
}

Via.prototype.encodeBody =function(){
    return this.encodeBodyBuffer("").toString();
}

Via.prototype.encodeBodyBuffer =function(buffer){
    buffer=this.sentProtocol.encodeBuffer(buffer);
    buffer=buffer+this.SP;   
    buffer=this.sentBy.encodeBuffer(buffer);
    if (this.parameters.length!=0) {
        buffer=buffer+this.SEMICOLON;
        buffer=this.parameters.encodeBuffer(buffer);
    }
    if (this.comment != null) {
        buffer=buffer+this.SP+this.LPAREN+this.comment+this.RPAREN;
    }
    if (this.rPortFlag)
    {
        buffer=buffer+";rport"
    }
    return buffer;
}

Via.prototype.getHost =function(){
    if (this.sentBy == null)
        return null;
    else {
        var host = this.sentBy.getHost();
        if (host == null)
        {
            return null;
        }
        else
        {
            return host.getHostname();
        }
    }
}

Via.prototype.setPort =function(port){
    if ( port!=-1 && (port<1 || port>65535)) {
        console.error("Via:setPort(): port value out of range -1, [1..65535]");
        throw "Via:setPort(): port value out of range -1, [1..65535]"; 
    }
    if (this.sentBy == null)
    {
        this.sentBy = new HostPort();
    }
    this.sentBy.setPort(port);
}

Via.prototype.setRPort =function(){
    this.rPortFlag = true;
}

Via.prototype.getPort =function(){
    if (this.sentBy == null)
    {
        return -1;
    }
    return this.sentBy.getPort();
}

Via.prototype.getRPort =function(){
    var strRport = this.getParameter(this.RPORT);
    if (strRport != null && ! strRport.equals(""))
    {
        var x=strRport-0;
        return x;
    }
    else
    {
        return -1;
    }
}

Via.prototype.getTransport =function(){
    if (this.sentProtocol == null)
    {
        return null;
    }
    return this.sentProtocol.getTransport();
}

Via.prototype.setTransport =function(transport){
    if (transport == null)
    {
        console.error("Via:setTransport(): the transport parameter is null");
        throw "Via:setTransport(): the transport parameter is null"; 
    }
    if (this.sentProtocol == null)
    {
        this.sentProtocol = new Protocol();
    }
    this.sentProtocol.setTransport(transport);
}

Via.prototype.getProtocol =function(){
    if (this.sentProtocol == null)
    {
        return null;
    }
    return this.sentProtocol.getProtocol();
}

Via.prototype.setProtocol =function(protocol){
    if (protocol == null)
    {
        console.error("Via:setProtocol(): the transport parameter is null");
        throw "Via:setProtocol(): the protocol parameter is null"; 
    }
    if (this.sentProtocol == null)
    {
        this.sentProtocol = new Protocol();
    }
    this.sentProtocol.setProtocol(protocol);
}

Via.prototype.getTTL =function(){
    var ttl = this.getParameterAsNumber(this.TTL);
    return ttl;
}

Via.prototype.setTTL =function(ttl){
    if (ttl < 0 && ttl != -1)
    {
        console.error("Via:setTTL():  the ttl parameter is < 0");
        throw "Via:setTTL(): the ttl parameter is < 0"; 
    }
    ttl=ttl-0;
    this.setParameter_nv(new NameValue(this.TTL, ttl));
}

Via.prototype.getMAddr =function(){
    return this.getParameter(this.MADDR);
}

Via.prototype.setMAddr =function(mAddr){
    if (mAddr == null)
    {
        console.error("Via:setMAddr():  the mAddr parameter is < 0");
        throw "Via:setMAddr(): the mAddr parameter is < 0"; 
    }
    var host = new Host();
    host.setAddress(mAddr);
    var nameValue = new NameValue(this.MADDR, host);
    this.setParameter_nv(nameValue);
}

Via.prototype.getReceived =function(){
    return this.getParameter(this.RECEIVED);
}

Via.prototype.setReceived =function(received){
    if (received == null)
    {
       console.error("Via:setReceived():  the received parameter is < 0");
       throw "Via:setReceived(): the received parameter is < 0"; 
    }
    this.setParameter(this.RECEIVED, received);
}

Via.prototype.getBranch =function(){
    return this.getParameter(this.BRANCH);
}

Via.prototype.setBranch =function(branch){
    if (branch == null || branch.length==0)
    {
       console.error("Via:setBranch():  branch parameter is null or length 0");
       throw "Via:setBranch(): branch parameter is null or length 0"; 
    }
    this.setParameter(this.BRANCH, branch);
}

Via.prototype.getSentByField =function(){
    if(this.sentBy != null)
    {
        return this.sentBy.encode();
    }
    return null;
}
Via.prototype.getSentProtocolField =function(){
    if(this.sentProtocol != null)
    {
        return this.sentProtocol.encode();
    }
    return null;
}/*
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
 *  Implementation of the JAIN-SIP Contact .
 *  @see  gov/nist/javax/sip/header/Contact.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Contact() {
    this.serialVersionUID = "1677294871695706288L";
    this.classname="Contact";
    this.headerName=this.NAME;
    this.parameters = new NameValueList();
    this.duplicates = new DuplicateNameValueList();
    this.address=new AddressImpl();
    this.Q = this.Q;
    this.contactList=new ContactList();
    this.wildCardFlag=null;
}

Contact.prototype = new AddressParametersHeader();
Contact.prototype.constructor=Contact;
Contact.prototype.NAME="Contact";
Contact.prototype.ACTION="action";
Contact.prototype.PROXY="proxy";
Contact.prototype.REDIRECT="redirect";
Contact.prototype.EXPIRES="expires";
Contact.prototype.Q="q";
Contact.prototype.NAME_ADDR=1;
Contact.prototype.SEMICOLON=";";
Contact.prototype.SIP_INSTANCE="+sip.instance";
Contact.prototype.PUB_GRUU="pub-gruu";
Contact.prototype.TEMP_GRUU="temp-gruu";

Contact.prototype.setParameter =function(name,value){
    var nv = this.parameters.getNameValue(name);
    if (nv != null) 
    {
        nv.setValueAsObject(value);
    } 
    else 
    {
        nv = new NameValue(name, value);
        if (name.toLowerCase()=="methods")
        {
            nv.setQuotedValue();
        }
        this.parameters.set_nv(nv);
    }
}

Contact.prototype.encodeBody =function(){
    return this.encodeBodyBuffer("");
}

Contact.prototype.encodeBodyBuffer =function(buffer){
    if (this.wildCardFlag) {
        buffer=buffer+"*";
    }
    else {
        if (this.address.getAddressType() == this.NAME_ADDR) 
        {
            buffer=this.address.encodeBuffer(buffer);
        } 
        else 
        {
            buffer=buffer+"<";
            buffer=this.address.encodeBuffer(buffer);
            buffer=buffer+">";
        }
        if (this.parameters.hmap.length!=0) 
        {
            buffer=buffer+this.SEMICOLON;
            buffer=this.parameters.encodeBuffer(buffer);
        }
    }
    return buffer;
}

Contact.prototype.getContactList =function(){
    return this.contactList;
}

Contact.prototype.getWildCardFlag =function(){
    return this.wildCardFlag;
}

Contact.prototype.getAddress =function(){
    return this.address;
}

Contact.prototype.getContactParms =function(){
    return this.parameters;
}

Contact.prototype.getExpires =function(){
    return this.getParameterAsNumber(this.EXPIRES);
}

Contact.prototype.setExpires =function(expiryDeltaSeconds){
    var deltaSeconds = expiryDeltaSeconds-0;
    this.parameters.set_name_value(this.EXPIRES, deltaSeconds);
}

Contact.prototype.getQValue =function(){
    return this.getParameterAsNumber(Q);
}

Contact.prototype.setContactList =function(cl){
    this.contactList=cl;
}

Contact.prototype.setWildCardFlag =function(w){
    this.wildCardFlag = true;
    this.address = new AddressImpl();
    this.address.setWildCardFlag();
}

Contact.prototype.setAddress =function(address){
    if (address == null)
    {
        console.error("CSeq:setAddress(): the address parameter is null");
        throw "CSeq:setAddress(): the address parameter is null";
    }
    this.address = address;
    this.wildCardFlag = false;
}

Contact.prototype.setQValue =function(qValue){
    if (qValue != -1 && (qValue < 0 || qValue > 1))
    {
        console.error("CSeq:setQValue(): the qValue is not between 0 and 1");
        throw "CSeq:setQValue(): Jthe qValue is not between 0 and 1";
    }
    var qv=new Number(qValue);
    this.parameters.set_name_value(Q, qv);
}

Contact.prototype.setWildCard =function(){
    this.setWildCardFlag(true);
}
Contact.prototype.isWildCard =function(){
    return this.address.isWildcard();
}

Contact.prototype.removeSipInstanceParam =function(){
    if (this.parameters != null)
    {
        this.parameters.delet(this.SIP_INSTANCE);
    }
}

Contact.prototype.getSipInstanceParam =function(){
    return this.parameters.getValue(this.SIP_INSTANCE);
}

Contact.prototype.setSipInstanceParam =function(value){
    this.parameters.set_name_value(this.SIP_INSTANCE, value);
}

Contact.prototype.removePubGruuParam =function(){
    if (this.parameters != null)
    {
        this.parameters.delet(this.PUB_GRUU);
    }
}

Contact.prototype.getPubGruuParam =function(){
    return this.parameters.getValue(this.PUB_GRUU);
}

Contact.prototype.setPubGruuParam =function(value){
    this.parameters.set_name_value(this.PUB_GRUU, value);
}

Contact.prototype.removeTempGruuParam =function(){
    if (this.parameters != null)
    {
        this.parameters.delet(this.TEMP_GRUU);
    }
}

Contact.prototype.getTempGruuParam =function(){
    return this.parameters.getValue(this.TEMP_GRUU);
}

Contact.prototype.setTempGruuParam =function(value){
    this.parameters.set_name_value(this.TEMP_GRUU, value);
}/*
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
 *  Implementation of the JAIN-SIP MediaRange .
 *  @see  gov/nist/javax/sip/header/MediaRange.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function MediaRange() {
    this.serialVersionUID = "-6297125815438079210L";
    this.classname="MediaRange";
    this.type=null;
    this.subtype=null;
}

MediaRange.prototype = new SIPObject();
MediaRange.prototype.constructor=MediaRange;
MediaRange.prototype.SLASH="/"

MediaRange.prototype.getType =function(){
    return this.type;
}

MediaRange.prototype.getSubtype =function(){
    return this.subtype;
}

MediaRange.prototype.setType =function(t){
    this.type=t;
}

MediaRange.prototype.setSubtype =function(s){
    this.subtype=s;
}

MediaRange.prototype.encode =function(){
    return this.encodeBuffer("").toString();
}

MediaRange.prototype.encodeBuffer =function(buffer){
    buffer=buffer+this.type+this.SLASH+this.subtype;
    return buffer;
}/*
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
 *  Implementation of the JAIN-SIP AuthenticationHeader .
 *  @see  gov/nist/javax/sip/header/AuthenticationHeader.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function AuthenticationHeader(name) {
    this.classname="AuthenticationHeader";
    this.headerName=null;
    this.scheme=null;
    if(name==null)
    {
        this.parameters = new NameValueList();
        this.duplicates = new DuplicateNameValueList();
        this.parameters.setSeparator(this.COMMA);
    }
    else
    {
        this.headerName=name;
        this.parameters = new NameValueList();
        this.duplicates = new DuplicateNameValueList();
        this.parameters.setSeparator(this.COMMA);
        this.scheme = this.DIGEST;
    }
}

AuthenticationHeader.prototype = new ParametersHeader();
AuthenticationHeader.prototype.constructor=AuthenticationHeader;
AuthenticationHeader.prototype.DOMAIN = "domain";
AuthenticationHeader.prototype.REALM = "realm";
AuthenticationHeader.prototype.OPAQUE = "opaque";
AuthenticationHeader.prototype.ALGORITHM = "algorithm";
AuthenticationHeader.prototype.QOP = "qop";
AuthenticationHeader.prototype.STALE = "stale";
AuthenticationHeader.prototype.SIGNATURE = "signature";
AuthenticationHeader.prototype.RESPONSE = "response";
AuthenticationHeader.prototype.SIGNED_BY = "signed-by";
AuthenticationHeader.prototype.NC = "nc";
AuthenticationHeader.prototype.URI = "uri";
AuthenticationHeader.prototype.USERNAME = "username";
AuthenticationHeader.prototype.CNONCE = "cnonce";
AuthenticationHeader.prototype.NONCE = "nonce";
AuthenticationHeader.prototype.IK = "ik";
AuthenticationHeader.prototype.CK = "ck";
AuthenticationHeader.prototype.INTEGRITY_PROTECTED = "integrity-protected";
AuthenticationHeader.prototype.COMMA = ",";
AuthenticationHeader.prototype.DIGEST = "Digest";
AuthenticationHeader.prototype.DOUBLE_QUOTE = "\"";
AuthenticationHeader.prototype.SP = " ";
//AuthenticationHeader.prototype.CK = "ck";

AuthenticationHeader.prototype.setParameter =function(name,value){
    var nv = this.parameters.getNameValue(name.toLowerCase());
    if (nv == null) 
    {
        nv = new NameValue(name, value);
        if (name.toLowerCase()==this.QOP
            || name.toLowerCase()==this.REALM
            || name.toLowerCase()==this.CNONCE
            || name.toLowerCase()==this.NONCE
            || name.toLowerCase()==this.USERNAME
            || name.toLowerCase()==this.DOMAIN
            || name.toLowerCase()==this.OPAQUE
            || name.toLowerCase()==this.NEXT_NONCE
            || name.toLowerCase()==this.URI
            || name.toLowerCase()==this.RESPONSE 
            ||name.toLowerCase()==this.IK
            || name.toLowerCase()==this.CK
            || name.toLowerCase()==this.INTEGRITY_PROTECTED)
            {
            if (((this instanceof Authorization) || (this instanceof ProxyAuthorization))
                && name.toLowerCase()==this.QOP) {
            // NOP, QOP not quoted in authorization headers
            } 
            else 
            {
                nv.setQuotedValue();
            }
            if (value == null)
            {
               console.error("AuthenticationHeader:setParameter(): null value");
               throw "AuthenticationHeader:setParameter(): null value";  
            }
            if (value.charAt(0)==this.DOUBLE_QUOTE)
            {
                console.error("AuthenticationHeader:setParameter(): unexpected DOUBLE_QUOTE");
                throw "AuthenticationHeader:setParameter(): unexpected DOUBLE_QUOTE";  
            }
        }
        this.parameters.set_nv(nv);
    }
    else
    {
        nv.setValueAsObject(value);
    }

}

AuthenticationHeader.prototype.setChallenge =function(challenge){
    this.scheme = challenge.scheme;
    this.parameters = challenge.authParams;
}

AuthenticationHeader.prototype.encodeBody =function(){
    this.parameters.setSeparator(this.COMMA);
    return this.scheme + this.SP + this.parameters.encode();
}

AuthenticationHeader.prototype.setScheme =function(scheme){
    this.scheme=scheme;
}

AuthenticationHeader.prototype.getScheme =function(){
    return this.scheme;
}

AuthenticationHeader.prototype.setRealm =function(realm){
    if (realm == null)
    {
        console.error("AuthenticationHeader:setRealm(): the realm parameter is null");
        throw "AuthenticationHeader:setRealm(): the realm parameter is null";
    }
    this.setParameter(this.REALM, realm);
}

AuthenticationHeader.prototype.getRealm =function(){
    return this.getParameter(this.REALM);
}

AuthenticationHeader.prototype.setNonce =function(nonce){
    if (nonce == null)
    {
        console.error("AuthenticationHeader:setNonce(): the nonce parameter is null");
        throw "AuthenticationHeader:setNonce(): the nonce parameter is null";
    }
    this.setParameter(this.NONCE, nonce);
}

AuthenticationHeader.prototype.getNonce =function(){
    return this.getParameter(this.NONCE);
}

AuthenticationHeader.prototype.setURI =function(uri){
    if (uri != null) 
    {
        var nv = new NameValue(this.URI, uri);
        nv.setQuotedValue();
        this.parameters.set_nv(nv);
    } 
    else 
    {
        console.error("AuthenticationHeader:setNonce(): the uri parameter is null");
        throw "AuthenticationHeader:setURI(): the uri parameter is null";
    }
}

AuthenticationHeader.prototype.getURI =function(){
    return this.getParameterAsURI(this.URI);
}

AuthenticationHeader.prototype.setAlgorithm =function(algorithm){
    if (algorithm == null)
    {
        console.error("AuthenticationHeader:setAlgorithm(): the algorithm parameter is null");
        throw "AuthenticationHeader:setAlgorithm(): the algorithm parameter is null";
    }
    this.setParameter(this.ALGORITHM, algorithm);
}

AuthenticationHeader.prototype.getAlgorithm =function(){
    return this.getParameter(this.ALGORITHM);
}

AuthenticationHeader.prototype.setQop =function(qop){
    if (qop == null)
    {
        console.error("AuthenticationHeader:setQop(): the qop parameter is null");
        throw "AuthenticationHeader:setQop(): the qop parameter is null";
    }
    this.setParameter(this.QOP, qop);
}

AuthenticationHeader.prototype.getQop =function(){
    return this.getParameter(this.QOP);
}

AuthenticationHeader.prototype.setOpaque =function(opaque){
    if (opaque == null)
    {
        console.error("AuthenticationHeader:setOpaque(): the opaque parameter is null");
        throw "AuthenticationHeader:setOpaque(): the opaque parameter is null";
    }
    this.setParameter(this.OPAQUE, opaque);
}

AuthenticationHeader.prototype.getOpaque =function(){
    return this.getParameter(this.OPAQUE);
}

AuthenticationHeader.prototype.setDomain =function(domain){
    if (domain == null)
    {
        console.error("AuthenticationHeader:setDomain(): the domain parameter is null");
        throw "AuthenticationHeader:setDomain(): the opaque parameter is null";
    }
    this.setParameter(this.DOMAIN, domain);
}

AuthenticationHeader.prototype.getDomain =function(){
    return this.getParameter(this.DOMAIN);
}

AuthenticationHeader.prototype.setStale =function(stale){
    this.parameters.set_nv(new NameValue(this.STALE, new Boolean(stale)));
}

AuthenticationHeader.prototype.isStale =function(){
    return this.getParameterAsBoolean(this.STALE);
}

AuthenticationHeader.prototype.setCNonce =function(cnonce){
    this.setParameter(this.CNONCE, cnonce);
}

AuthenticationHeader.prototype.getCNonce =function(){
    return this.getParameter(this.CNONCE);
}

AuthenticationHeader.prototype.getNonceCount =function(){
    return this.getParameterAsNumber(this.NC);
}

AuthenticationHeader.prototype.setNonceCount =function(param){
    if (param < 0)
    {
        console.error("AuthenticationHeader:setNonceCount(): bad parameter");
        throw "AuthenticationHeader:setNonceCount(): bad parameter"; 
    }
    var nc = new String(param);
    var base = "00000000";
    nc = base.substring(0, 8 - nc.length) + nc;
    this.setParameter(this.NC, nc);
}

AuthenticationHeader.prototype.getResponse =function(){
    return this.getParameter(this.RESPONSE);
}

AuthenticationHeader.prototype.setResponse =function(response){
    if (response == null)
    {
        console.error("AuthenticationHeader:setResponse(): the domain parameter is null");
        throw "AuthenticationHeader:setResponse():  the opaque parameter is null";    
    }
    this.setParameter(this.RESPONSE, response);
}

AuthenticationHeader.prototype.getUsername =function(){
    return this.getParameter(this.USERNAME);
}

AuthenticationHeader.prototype.setUsername =function(username){
    this.setParameter(this.USERNAME, username);
}

AuthenticationHeader.prototype.setIK =function(ik){
    if (ik == null)
    {
        console.error("AuthenticationHeader:setIK(): the auth-param IK parameter is null");
        throw "AuthenticationHeader:setIK(): the auth-param IK parameter is null";
    }
    this.setParameter(this.IK, ik);
}

AuthenticationHeader.prototype.getIK =function(){
    return this.getParameter(this.IK);
}

AuthenticationHeader.prototype.setCK =function(ck){
    if (ck == null)
    {
        console.error("AuthenticationHeader:setCK() the auth-param CK parameter is null");
        throw "AuthenticationHeader:setCK(): the auth-param CK parameter is null";
    }
    this.setParameter(this.CK, ck);
}

AuthenticationHeader.prototype.getCK =function(){
    return this.getParameter(this.CK);
}

AuthenticationHeader.prototype.setIntegrityProtected =function(integrityProtected){
    if (integrityProtected == null)
    {
        console.error("AuthenticationHeader:setIntegrityProtected(): the integrity-protected parameter is null");
        throw "AuthenticationHeader:setIntegrityProtected(): the integrity-protected parameter is null";
    }
    this.setParameter(this.INTEGRITY_PROTECTED, integrityProtected);
}

AuthenticationHeader.prototype.getIntegrityProtected =function(){
    return this.getParameter(this.INTEGRITY_PROTECTED);
}/*
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
 *  Implementation of the JAIN-SIP WWWAuthenticate .
 *  @see  gov/nist/javax/sip/header/WWWAuthenticate.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function WWWAuthenticate() {
    this.serialVersionUID = "115378648697363486L";
    this.classname="WWWAuthenticate";
    this.headerName=this.NAME;
    this.parameters = new NameValueList();
    this.duplicates = new DuplicateNameValueList();
    this.parameters.setSeparator(this.COMMA);
    this.scheme = this.DIGEST;
}

WWWAuthenticate.prototype = new AuthenticationHeader();
WWWAuthenticate.prototype.constructor=WWWAuthenticate;
WWWAuthenticate.prototype.NAME="WWW-Authenticate";
WWWAuthenticate.prototype.COMMA = ",";
WWWAuthenticate.prototype.DIGEST = "Digest";

WWWAuthenticate.prototype.getURI =function(){
    return null
}

WWWAuthenticate.prototype.setURI =function(uri){
}/*
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
    return this.encodeBodyBuffer("").toString();
}

Route.prototype.encodeBodyBuffer =function(buffer){
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
}/*
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
 *  Implementation of the JAIN-SIP ProxyAuthenticate .
 *  @see  gov/nist/javax/sip/header/ProxyAuthenticate.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ProxyAuthenticate() {
    this.serialVersionUID = "-3826145955463251116L";
    this.classname="ProxyAuthenticate";
    this.headerName=this.PROXY_AUTHENTICATE;
    this.parameters = new NameValueList();
    this.duplicates = new DuplicateNameValueList();
    this.parameters.setSeparator(this.COMMA);
    this.scheme = this.DIGEST;
}

ProxyAuthenticate.prototype = new AuthenticationHeader();
ProxyAuthenticate.prototype.constructor=ProxyAuthenticate;
ProxyAuthenticate.prototype.PROXY_AUTHENTICATE="Proxy-Authenticate";
ProxyAuthenticate.prototype.COMMA = ",";
ProxyAuthenticate.prototype.DIGEST = "Digest";

ProxyAuthenticate.prototype.getURI =function(){
    return null
}

ProxyAuthenticate.prototype.setURI =function(uri){
    
}/*
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
 *  Implementation of the JAIN-SIP ProxyAuthorization .
 *  @see  gov/nist/javax/sip/header/ProxyAuthorization.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ProxyAuthorization() {
    this.serialVersionUID = "-6374966905199799098L";
    this.classname="ProxyAuthorization";
    this.scheme=null;
    this.headerName=this.PROXY_AUTHORIZATION;
    this.parameters = new NameValueList();
    this.duplicates = new DuplicateNameValueList();
    this.parameters.setSeparator(this.COMMA);
    this.scheme = this.DIGEST;
}

ProxyAuthorization.prototype = new AuthenticationHeader();
ProxyAuthorization.prototype.constructor=ProxyAuthorization;
ProxyAuthorization.prototype.PROXY_AUTHORIZATION="Proxy-Authorization";
ProxyAuthorization.prototype.COMMA = ",";
ProxyAuthorization.prototype.DIGEST = "Digest";
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
 *  Implementation of the JAIN-SIP StatusLine .
 *  @see  gov/nist/javax/sip/header/StatusLine.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function StatusLine() {
    this.serialVersionUID = "-4738092215519950414L";
    this.classname="StatusLine";
    this.matchStatusClass=null;
    this.sipVersion=this.SIP_VERSION_STRING;
    this.statusCode=null;
    this.reasonPhrase=null;
}

StatusLine.prototype = new SIPObject();
StatusLine.prototype.constructor=StatusLine;
StatusLine.prototype.SIP_VERSION_STRING="SIP/2.0";
StatusLine.prototype.SP=" ";
StatusLine.prototype.NEWLINE="\r\n";

StatusLine.prototype.match =function(){
    
}
StatusLine.prototype.setMatchStatusClass =function(flag){
    this.matchStatusClass=flag;
}

StatusLine.prototype.encode =function(){
    var encoding = this.SIP_VERSION_STRING + this.SP + this.statusCode;
    if (this.reasonPhrase != null)
    {
        encoding =encoding+this.SP + this.reasonPhrase;
    }
    encoding =encoding+ this.NEWLINE;
    return encoding;
}

StatusLine.prototype.getSipVersion =function(){
    return this.sipVersion;
}

StatusLine.prototype.getStatusCode =function(){
    return this.statusCode;
}

StatusLine.prototype.getReasonPhrase =function(){
    return this.reasonPhrase;
}

StatusLine.prototype.setSipVersion =function(s){
    this.sipVersion=s;
}

StatusLine.prototype.setStatusCode =function(statusCode){
    this.statusCode=statusCode;
}

StatusLine.prototype.setReasonPhrase =function(reasonPhrase){
    this.reasonPhrase=reasonPhrase;
}

StatusLine.prototype.getVersionMajor =function(){
    if (this.sipVersion == null)
    {
        return null;
    }
    var major = null;
    var slash = false;
    for (var i = 0; i < this.sipVersion.length; i++) {
        if (this.sipVersion.charAt(i) == ".")
        {
            slash = false;
        }
        if (slash) 
        {
            if (major == null)
                major = "" + this.sipVersion.charAt(i);
            else
                major =major+ this.sipVersion.charAt(i);
        }
        if (this.sipVersion.charAt(i) == "/")
        {
            slash = true;
        }
    }
    return major;
}

StatusLine.prototype.getVersionMinor =function(){
    if (this.sipVersion == null)
        return null;
    var minor = null;
    var dot = false;
    for (var i = 0; i < this.sipVersion.length; i++) {
        if (dot) 
        {
            if (minor == null)
            {
                minor = "" + this.sipVersion.charAt(i);
            }
            else
            {
                minor =minor + this.sipVersion.charAt(i);
            }
        }
        if (this.sipVersion.charAt(i) == '.')
        {
            dot = true;
        }
    }
    return minor;
}/*
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
 *  Implementation of the JAIN-SIP Authorization .
 *  @see  gov/nist/javax/sip/header/Authorization.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Authorization() {
    this.serialVersionUID = "-8897770321892281348L";
    this.classname="Authorization";
    this.headerName=this.NAME;
    this.parameters = new NameValueList();
    this.duplicates = new DuplicateNameValueList();
    this.parameters.setSeparator(this.COMMA);
    this.scheme = this.DIGEST;
}

Authorization.prototype = new AuthenticationHeader();
Authorization.prototype.constructor=Authorization;
Authorization.prototype.NAME = "Authorization";
Authorization.prototype.COMMA = ",";
Authorization.prototype.DIGEST = "Digest";
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
 *  Implementation of the JAIN-SIP Allow .
 *  @see  gov/nist/javax/sip/header/Allow.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function Allow (m) {
    this.serialVersionUID = "-3105079479020693930L";
    this.classname="Allow"; 
    this.method=null;
    if(m==null)
    {
        this.headerName=this.ALLOW;
    }
    else if(arguments.length==1)
    {
        this.headerName=this.ALLOW;
        this.method=m;
    }
}

Allow.prototype = new SIPHeader();
Allow.prototype.constructor=Allow;
Allow.prototype.ALLOW="Allow";

Allow.prototype.getMethod =function(){
    return this.method;
}

Allow.prototype.setMethod =function(method){
    if (method == null)
    {
        console.error("Allow:setMethod(): JAIN-SIP Exception, the method parameter is null.");
        throw "Allow:setMethod(): the method parameter is null.";
    }
    this.method = method;
}

Allow.prototype.encodeBody =function(){
    return this.method;
}
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
    return this.encodeBodyBuffer("").toString();
}

RecordRoute.prototype.encodeBodyBuffer =function(buffer){
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
 *  Implementation of the JAIN-SIP MaxForwards .
 *  @see  gov/nist/javax/sip/header/MaxForwards.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function MaxForwards(m) {
    this.serialVersionUID = "-3096874323347175943L";
    this.classname="MaxForwards";
    this.headerName=this.NAME;
    this.maxForwards=null;
    if(m==null)
    {
        this.headerName=this.NAME;
    }
    else
    {
        this.headerName=this.NAME;
        this.setMaxForwards( m );
    }
}

MaxForwards.prototype = new SIPHeader();
MaxForwards.prototype.constructor=MaxForwards;
MaxForwards.prototype.NAME="Max-Forwards";

MaxForwards.prototype.getMaxForwards =function(){
    return this.maxForwards;
}

MaxForwards.prototype.setMaxForwards =function(maxForwards){
    if (maxForwards < 0 || maxForwards > 255)
    { 
        console.error("MaxForwards:setMaxForwards(): bad max forwards value " + maxForwards);
        throw "MaxForwards:setMaxForwards(): bad max forwards value " + maxForwards;
    }
    this.maxForwards = maxForwards;
}

MaxForwards.prototype.encodeBody =function(){
    return this.encodeBodyBuffer("").toString();
}

MaxForwards.prototype.encodeBodyBuffer =function(buffer){
    buffer=buffer+this.maxForwards;
    return buffer;
}

MaxForwards.prototype.hasReachedZero =function(){
    if(this.maxForwards == 0)
    {
        return true;
    }
    else
    {
        return false;
    }
}

MaxForwards.prototype.decrementMaxForwards =function(){
    if (this.maxForwards > 0)
    {
        this.maxForwards--;
    }
    else 
    {
       console.error("MaxForwards:decrementMaxForwards(): has already reached 0!");
       throw "MaxForwards:decrementMaxForwards(): has already reached 0!";
    }
}
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
 *  Implementation of the JAIN-SIP ContentType .
 *  @see  gov/nist/javax/sip/header/ContentType.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ContentType(contentType,contentSubtype) {
    this.serialVersionUID = "8475682204373446610L";
    this.classname="ContentType";
    this.mediaRange=new MediaRange();
    this.headerName=this.NAME;
    this.parameters = new NameValueList();
    this.duplicates = new DuplicateNameValueList();
    if(contentType!=null&&contentSubtype!=null)
    {
        this.setContentType(contentType, contentSubtype);
    }
}

ContentType.prototype = new ParametersHeader();
ContentType.prototype.constructor=ContentType;
ContentType.prototype.NAME="Content-Type";
ContentType.prototype.SEMICOLON=";";

ContentType.prototype.compareMediaRange =function(media){
    var chaine1=(mediaRange.type + "/" + mediaRange.subtype).toLowerCase();
    var chaine2=media.toLowerCase();
    var c=0;
    var length;
    if(chaine1.length>=chaine2.length)
    {
        length=chaine1.length
    }
    else
    {
        length=chaine2.length;
    }
    for(var i=0;i<length;i++)
    {
        if(chaine1.charAt(i)==null)
        {}
        else if(chaine1.charAt(i)>chaine2.charAt(i))
        {
            c=c+1;
        }
        else if(chaine1.charAt(i)<chaine2.charAt(i))
        {
            c=c-1;
        }
    }
    return c;
}

ContentType.prototype.encodeBody =function(){
    return this.encodeBodyBuffer("").toString();
}

ContentType.prototype.encodeBodyBuffer =function(buffer){
    buffer=this.mediaRange.encodeBuffer(buffer);
    if (this.hasParameters()) {
        buffer=buffer+this.SEMICOLON;
        buffer=this.parameters.encodeBuffer(buffer);
    }
    return buffer;
}

ContentType.prototype.getMediaRange =function(){
    return this.mediaRange;
}

ContentType.prototype.getMediaType =function(){
    return this.mediaRange.type;
}

ContentType.prototype.getMediaSubType =function(){
    return this.mediaRange.subtype;
}

ContentType.prototype.getContentSubType =function(){
    return this.mediaRange == null ? null : this.mediaRange.getSubtype();
}

ContentType.prototype.getContentType =function(){
    return this.mediaRange == null ? null : this.mediaRange.getType();
}

ContentType.prototype.getCharset =function(){
    return this.getParameter("charset");
}

ContentType.prototype.setMediaRange =function(m){
    this.mediaRange = m;
}

ContentType.prototype.setContentType =function(){
    if(arguments==1)
    {
        var contentType=arguments[0];
        if (contentType == null)
        {
            console.error("ContentType:setContentType(): null argument");
            throw "ContentType:setContentType(): null argument";
        }
        if (this.mediaRange == null)
        {
            this.mediaRange = new MediaRange();
        }
        this.mediaRange.setType(contentType);
    }
    else
    {
        contentType=arguments[0];
        var contentSubType=arguments[1];
        if (this.mediaRange == null)
        {
            this.mediaRange = new MediaRange();
        }
        this.mediaRange.setType(contentType);
        this.mediaRange.setSubtype(contentSubType);
    }
}

ContentType.prototype.setContentSubType =function(contentType){
    if (contentType == null)
    {
          console.error("ContentType:setContentSubType(): null contentType parameter");
            throw "ContentType:setContentSubType(): null contentType parameter";
    }
    if (this.mediaRange == null)
    {
        this.mediaRange = new MediaRange();
    }
    this.mediaRange.setSubtype(contentType);
}/*
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
 *  Implementation of the JAIN-SIP TimeStamp .
 *  @see  gov/nist/javax/sip/header/TimeStamp.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function TimeStamp() {
    this.classname="TimeStamp";
    this.serialVersionUID = "-3711322366481232720L";
    this.timeStamp = -1;
    this.delay = -1;
    this.delayFloat = -1;
    this.timeStampFloat = -1;
    this.headerName = this.TIMESTAMP;
}

TimeStamp.prototype = new SIPHeader();
TimeStamp.prototype.constructor=TimeStamp;
TimeStamp.prototype.TIMESTAMP="Timestamp";

TimeStamp.prototype.getTimeStampAsString =function(){
    if (this.timeStamp == -1 && this.timeStampFloat == -1)
    {
        return "";
    }
    else if (this.timeStamp != -1)
    {
        return this.timeStamp.toString();
    }
    else
    {
        return this.timeStampFloat.toString();
    }
}

TimeStamp.prototype.getDelayAsString =function(){
    if (this.delay == -1 && this.delayFloat == -1)
    {
        return "";
    }
    else if (this.delay != -1)
    {
        return this.delay.toString();
    }
    else
    {
        return this.delayFloat.toString();
    }
}

TimeStamp.prototype.encodeBody =function(){
    var retval = "";
    var s1 = this.getTimeStampAsString();
    var s2 = this.getDelayAsString();
    if (s1=="" && s2=="")
    {
        return "";
    }
    if (s1!="")
    {
        retval=retval+s1;
    }
    if (s2!="")
    {
        retval=retval+" "+s2;
    }
    return retval.toString();
}

TimeStamp.prototype.hasDelay =function(){
    if(this.delay != -1)
    {
        return true;
    }
    else
    {
        return false;
    }
}

TimeStamp.prototype.removeDelay =function(){
    delay = -1;
}

TimeStamp.prototype.setTimeStamp =function(timeStamp){
    if (timeStamp < 0)
    {
        console.error("TimeStamp:setTimeStamp(): the timeStamp parameter is <0");
        throw "TimeStamp:setTimeStamp(): the timeStamp parameter is <0"; 
    }
    this.timeStamp = -1;
    this.timeStampFloat = timeStamp;
}

TimeStamp.prototype.getTimeStamp =function(){
    if(this.timeStampFloat == -1)
    {
        return this.timeStamp;
    }
    else
    {
        return this.timeStampFloat;
    }
}

TimeStamp.prototype.getDelay =function(){
    if(this.delayFloat == -1)
    {
        return this.delay;
    }
    else
    {
        return this.delayFloat;
    }
}

TimeStamp.prototype.setDelay =function(delay){
    if (delay < 0 && delay != -1)
    {
        console.error("TimeStamp:setDelay(): the delay parameter is <0");
        throw "TimeStamp:setDelay(): the delay parameter is <0"; 
    }
    this.delayFloat = delay;
    this.delay = -1;
}
TimeStamp.prototype.getTime =function(){
    return this.timeStamp == -1 ? this.timeStampFloat : this.timeStamp;
}

TimeStamp.prototype.getTimeDelay =function(){
    return this.delay == -1 ? this.delayFloat : this.delay;
}

TimeStamp.prototype.setTime =function(timeStamp){
    if (this.timeStamp < -1)
    {
        console.error("TimeStamp:setTime(): illegal timestamp");
        throw "TimeStamp:setTime(): illegal timestamp"; 
    }
  
    this.timeStamp = timeStamp;
    this.timeStampFloat = -1;
}

TimeStamp.prototype.setTimeDelay =function(delay){
    if (delay < -1)
    {
        console.error("TimeStamp:setTimeDelay(): value out of range " + delay);
        throw "TimeStamp:setTimeDelay(): value out of range " + delay; 
    }
    this.delay = delay;
    this.delayFloat = -1;
}/*
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
 *  Implementation of the JAIN-SIP ContentLength .
 *  @see  gov/nist/javax/sip/header/ContentLength.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ContentLength(length) {
    this.serialVersionUID = "1187190542411037027L";
    this.classname="ContentLength";
    this.headerName=this.NAME;
    this.contentLength=null;
    if(length==null)
    {
        this.headerName=this.NAME;
    }
    else
    {
        this.headerName=this.NAME;
        this.contentLength=length;
    }
}

ContentLength.prototype = new SIPHeader();
ContentLength.prototype.constructor=ContentLength;
ContentLength.prototype.NAME="Content-Length";

ContentLength.prototype.getContentLength =function(){
    var x=this.contentLength-0;
    return x;
}

ContentLength.prototype.setContentLength =function(contentLength){
    if (contentLength < 0)
    {
        console.error("ContentLength:setContentLength(): the contentLength parameter is < 0");
        throw "ContentLength:setContentLength(): the contentLength parameter is < 0";
}
    this.contentLength = contentLength;
}

ContentLength.prototype.encodeBody =function(){
    return this.encodeBodyBuffer("").toString();
}

ContentLength.prototype.encodeBodyBuffer =function(buffer){
    if (this.contentLength == null)
    {
        buffer=buffer+"0";
    }
    else
    {
        buffer=buffer+this.contentLength.toString();
    }
    return buffer;
}

ContentLength.prototype.match =function(other){
    if (other instanceof ContentLength)
    {
        return true;
    }
    else
    {
        return false;
    }
}/*
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
 *  Implementation of the JAIN-SIP ContentDisposition .
 *  @see  gov/nist/javax/sip/header/ContentDisposition.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ContentDisposition (m) {
    this.serialVersionUID = "835596496276127003L";
    this.classname="ContentDisposition"; 
    this.dispositionType=null;
    this.headerName=this.NAME;
    this.parameters = new NameValueList();
    this.duplicates = new DuplicateNameValueList();
}

ContentDisposition.prototype = new ParametersHeader();
ContentDisposition.prototype.constructor=ContentDisposition;
ContentDisposition.prototype.NAME="Content-Disposition";
ContentDisposition.prototype.SEMICOLON=";";

ContentDisposition.prototype.encodeBody =function(){
    var encoding = this.dispositionType;
    if (this.parameters.hmap.length!=0) {
        encoding=encoding+this.SEMICOLON+this.parameters.encode();
    }
    return encoding.toString();
}

ContentDisposition.prototype.setDispositionType =function(dispositionType){
    if (dispositionType == null)
    {
        console.error("ContentDisposition:setDispositionType(): the dispositionType parameter is null");
        throw "ContentDisposition:setDispositionType(): the dispositionType parameter is null";
    }
    this.dispositionType = dispositionType;
}

ContentDisposition.prototype.getDispositionType =function(){
    return this.dispositionType;
}

ContentDisposition.prototype.getHandling =function(){
    return this.getParameter("handling");
}

ContentDisposition.prototype.setHandling =function(handling){
    if (handling == null)
    {
        console.error("ContentDisposition:setHandling(): the handling parameter is null");
        throw "ContentDisposition:setHandling(): the handling parameter is null";
    }
    this.setParameter("handling", handling);
}

ContentDisposition.prototype.getContentDisposition =function(){
    return this.encodeBody();
}/*
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
    return this.encodeBuffer("").toString();
}

CallIdentifier.prototype.encodeBuffer =function(buffer){
    buffer=buffer+this.localId;
    if (this.host != null) {
        buffer=buffer+this.AT+this.host;
    }
    return buffer;
}

CallIdentifier.prototype.hashCode =function(){
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
    return this.localId;
}

CallIdentifier.prototype.getHost =function(){
    return this.host;
}

CallIdentifier.prototype.setLocalId =function(localId){
    this.localId=localId;
}
CallIdentifier.prototype.setCallID =function(cid){
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
    this.host=host;
}/*
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
 *  Implementation of the JAIN-SIP CallID .
 *  @see  gov/nist/javax/sip/header/CallID.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function CallID() {
    this.serialVersionUID = "-6463630258703731156L;";
    this.classname="CallID";
    this.headerName=this.NAME;
    this.callIdentifier=new CallIdentifier();
    if(arguments.length!=0)
    {
        var callId=arguments[0];
        this.callIdentifier = new CallIdentifier(callId);
    }
}

CallID.prototype = new SIPHeader();
CallID.prototype.constructor=CallID;
CallID.prototype.NAME="Call-ID";

CallID.prototype.encodeBody =function(){
    return this.encodeBodyBuffer("").toString();
}

CallID.prototype.encodeBodyBuffer =function(buffer){
    if (this.callIdentifier != null)
    {
        buffer=this.callIdentifier.encodeBuffer(buffer);
    }
    return buffer;
}

CallID.prototype.getCallId =function(){
    return this.encodeBody();
}

CallID.prototype.getCallIdentifer =function(){
    return this.callIdentifier;
}

CallID.prototype.setCallId =function(cid){
    this.callIdentifier = new CallIdentifier(cid);
}

CallID.prototype.setCallIdentifier =function(cid){
    this.callIdentifier=cid;
}/*
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
 *  Implementation of the JAIN-SIP AuthorizationList .
 *  @see  gov/nist/javax/sip/header/AuthorizationList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function CSeq() {
    this.serialVersionUID = "-5405798080040422910L";
    this.classname="CSeq";
    this.method=null;
    this.seqno=null;
    if(arguments.length==0)
    {
        this.headerName=this.CSEQ;
    }
    else
    {
        var seqno=arguments[0];
        var method=arguments[1];
        this.headerName=this.CSEQ;
        this.seqno = seqno;
        var siprequest=new SIPRequest();
        this.method = siprequest.getCannonicalName(method);
    }
}

CSeq.prototype = new SIPHeader();
CSeq.prototype.constructor=CSeq;
CSeq.prototype.CSEQ="CSeq";
CSeq.prototype.COLON=":";
CSeq.prototype.SP=" ";
CSeq.prototype.NEWLINE="\r\n";


CSeq.prototype.encode =function(){
    return this.headerName+this.COLON+this.SP+this.encodeBody()+this.NEWLINE;
}

CSeq.prototype.encodeBody =function(){
    return this.encodeBodyBuffer("").toString();
}

CSeq.prototype.encodeBodyBuffer =function(buffer){
    buffer=buffer+this.seqno+this.SP+this.method.toUpperCase();
    return buffer;
}

CSeq.prototype.getMethod =function(){
   
   return this.method;
}

CSeq.prototype.setSeqNumber =function(sequenceNumber){
    if (sequenceNumber < 0 )
    {
       console.error("CSeq:setSeqNumber(): the sequence number parameter is < 0 : " + sequenceNumber);
       throw "CSeq:setSeqNumber(): the sequence number parameter is < 0 : " + sequenceNumber;
    }
    else if ( sequenceNumber > 2147483647)
    {
       console.error("CSeq:setSeqNumber(): the sequence number parameter is too large : " + sequenceNumber);
       throw "CSeq:setSeqNumber(): the sequence number parameter is too large : " + sequenceNumber;
    }
    this.seqno = sequenceNumber;
}

CSeq.prototype.setSequenceNumber =function(sequenceNumber){
    this.setSeqNumber(sequenceNumber);
}

CSeq.prototype.setMethod =function(method){
    if (method == null)
    {
        console.error("CSeq:setMethod(): the method parameter is null");
         throw "CSeq:setMethod(): the meth parameter is null";
    }
    var siprequest=new SIPRequest();
    this.method = siprequest.getCannonicalName(method);
}

CSeq.prototype.getSequenceNumber =function(){
    if (this.seqno == null)
    {
        return 0;
    }
    else
    {
        return this.seqno;
    }
}
CSeq.prototype.getSeqNumber =function(){
    return this.seqno;
}/*
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
 *  Implementation of the JAIN-SIP Supported .
 *  @see  gov/nist/javax/sip/header/Supported.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Supported(option_tag) {
    this.serialVersionUID = "-7679667592702854542L";
    this.classname="Supported";
    this.optionTag=null;
    if(option_tag!=null)
    {
        this.headerName="Supported";
        this.optionTag=option_tag;
    }
    else
    {
        this.headerName="Supported";
        this.optionTag=null;    
    }
}

Supported.prototype = new SIPHeader();
Supported.prototype.constructor=Supported;
Supported.prototype.COLON=":";
Supported.prototype.NEWLINE="\r\n";

Supported.prototype.encode =function(){
    var retval = this.headerName + this.COLON;
    if (this.optionTag != null)
    {
        retval = retval+this.SP + this.optionTag;
    }
    retval = retval+this.NEWLINE;
    return retval;
}

Supported.prototype.encodeBody =function(){
    return this.optionTag != null ? this.optionTag : "";
}

Supported.prototype.setOptionTag =function(optionTag){
    if (optionTag == null)
    {
        console.error("Supported:setOptionTag(): the optionTag parameter is null");
        throw "Supported:setOptionTag(): the optionTag parameter is null"; 
    }
    this.optionTag = optionTag;
}

Supported.prototype.getOptionTag =function(){
    return this.optionTag;
}/*
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
 *  Implementation of the JAIN-SIP Expires .
 *  @see  gov/nist/javax/sip/header/Expires.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Expires() {
    this.serialVersionUID = "3134344915465784267L";
    this.classname="Expires";
    this.headerName=this.NAME;
    this.expires=null;
}

Expires.prototype = new SIPHeader();
Expires.prototype.constructor=Expires;
Expires.prototype.NAME="Expires";

Expires.prototype.encodeBody =function(){
    return this.encodeBodyBuffer("").toString();
}

Expires.prototype.encodeBodyBuffer =function(buffer){
    buffer=buffer+this.expires;
    return buffer;
}

Expires.prototype.getExpires =function(){
    return this.expires;
}

Expires.prototype.setExpires =function(expires){
    if (expires < 0)
    {
        console.error("Expires:setExpires(): bad argument " + expires);
        throw "Expires:setExpires(): bad argument " + expires;
    }
    this.expires = expires;
}/*
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
 *  Implementation of the JAIN-SIP ContactList .
 *  @see  gov/nist/javax/sip/header/ContactList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ContactList() {
    this.serialVersionUID = "1224806837758986814L";
    this.classname="ContactList";
    this.headerName = this.NAME;
    this.myClass =  "Contact";
    this.hlist=new Array();
}

ContactList.prototype = new SIPHeaderList();
ContactList.prototype.constructor=ContactList;
ContactList.prototype.NAME="Contact";

ContactList.prototype.clone =function(){
}/*
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
 *  Implementation of the JAIN-SIP ViaList .
 *  @see  gov/nist/javax/sip/header/ViaList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ViaList() {
    this.serialVersionUID = "3899679374556152313L";
    this.classname="ViaList";
    this.headerName = this.NAME;
    this.myClass =  "Via";
    this.hlist=new Array();
}

ViaList.prototype = new SIPHeaderList();
ViaList.prototype.constructor=ViaList;
ViaList.prototype.NAME="Via";

ViaList.prototype.clone =function(){
}

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
 *  Implementation of the JAIN-SIP WWWAuthenticateList .
 *  @see  gov/nist/javax/sip/header/WWWAuthenticateList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function WWWAuthenticateList() {
    this.serialVersionUID = "-6978902284285501346L";
    this.classname="WWWAuthenticateList";
    this.headerName = this.NAME;
    this.myClass =  "WWWAuthenticate";
    this.hlist=new Array();
}

WWWAuthenticateList.prototype = new SIPHeaderList();
WWWAuthenticateList.prototype.constructor=WWWAuthenticateList;
WWWAuthenticateList.prototype.NAME="WWW-Authenticate";

WWWAuthenticateList.prototype.clone =function(){
}

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
 *  Implementation of the JAIN-SIP RouteList .
 *  @see  gov/nist/javax/sip/header/RouteList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function RouteList() {
    this.serialVersionUID = "1L";
    this.classname="RouteList";
    this.headerName = this.NAME;
    this.myClass =  "Route";
    this.hlist=new Array();
}

RouteList.prototype = new SIPHeaderList();
RouteList.prototype.constructor=RouteList;
RouteList.prototype.NAME="Route";

RouteList.prototype.clone =function(){
    
}

RouteList.prototype.encode =function(){
    if (this.hlist.length==0) 
    {
        return "";
    }
    else 
    {
        return this.encodeBuffer("");
    }
}
RouteList.prototype.equals =function(){
    
}
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
 *  Implementation of the JAIN-SIP ProxyAuthenticate .
 *  @see  gov/nist/javax/sip/header/ProxyAuthenticate.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ProxyAuthenticateList() {
    this.serialVersionUID = "1L";
    this.classname="ProxyAuthenticateList";
    this.headerName = this.NAME;
    this.myClass =  "ProxyAuthenticate";
    this.hlist=new Array();
}

ProxyAuthenticateList.prototype = new SIPHeaderList();
ProxyAuthenticateList.prototype.constructor=ProxyAuthenticateList;
ProxyAuthenticateList.prototype.NAME="Proxy-Authenticate";

ProxyAuthenticateList.prototype.clone =function(){
}

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
 *  Implementation of the JAIN-SIP ProxyAuthorizationList .
 *  @see  gov/nist/javax/sip/header/ProxyAuthorizationList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ProxyAuthorizationList() {
    this.serialVersionUID = "-1L";
    this.classname="ProxyAuthorizationList";
    this.headerName = this.NAME;
    this.myClass =  "ProxyAuthorization";
}

ProxyAuthorizationList.prototype = new SIPHeaderList();
ProxyAuthorizationList.prototype.constructor=ProxyAuthorizationList;
ProxyAuthorizationList.prototype.NAME="Proxy-Authorization";

ProxyAuthorizationList.prototype.clone =function(){
    
}

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
 *  Implementation of the JAIN-SIP AuthorizationList .
 *  @see  gov/nist/javax/sip/header/AuthorizationList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function AuthorizationList() {
    this.serialVersionUID = "1L";
    this.classname="AuthorizationList";
    this.headerName = this.NAME;
    this.myClass =  "Authorization";
}

AuthorizationList.prototype = new SIPHeaderList();
AuthorizationList.prototype.constructor=AuthorizationList;
AuthorizationList.prototype.NAME="Authorization";

AuthorizationList.prototype.clone =function(){
    
}

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
 *  Implementation of the JAIN-SIP AllowList .
 *  @see  gov/nist/javax/sip/header/AllowList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function AllowList() {
    this.serialVersionUID = "-4699795429662562358L";
    this.classname="AllowList";
    this.headerName = this.NAME;
    this.myClass =  "Allow";
    this.hlist=new Array();
}

AllowList.prototype = new SIPHeaderList();
AllowList.prototype.constructor=AllowList;
AllowList.prototype.NAME="Allow";

AllowList.prototype.clone =function(){
    
}

AllowList.prototype.getMethods =function(){
    var array=new Array();
    for(var i=0;i<this.hlist.length;i++)
    {
        var a=this.hlist[i];
        array[i]=a.getMethod();
    }
    return array;
}

AllowList.prototype.setMethods =function(methods){
    var array=new Array();
    for(var i=0;i<methods.length;i++)
    {
        var allow=new Allow();
        allow.setMethod(methods[i]);
        this.add(allow);
    }
    return array;
}
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
 *  Implementation of the JAIN-SIP RecordRouteList .
 *  @see  gov/nist/javax/sip/header/RecordRouteList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function RecordRouteList() {
    this.serialVersionUID = "1724940469426766691L";
    this.classname="RecordRouteList";
    this.headerName = this.NAME;
    this.myClass =  "RecordRoute";
    this.hlist=new Array();
}

RecordRouteList.prototype = new SIPHeaderList();
RecordRouteList.prototype.constructor=RecordRouteList;
RecordRouteList.prototype.NAME="Record-Route";

RecordRouteList.prototype.clone =function(){
    
}

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
 *  Implementation of the JAIN-SIP SupportedList .
 *  @see  gov/nist/javax/sip/header/SupportedList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function SupportedList() {
    this.serialVersionUID = "-4539299544895602367L";
    this.classname="SupportedList";
    this.headerName = this.NAME;
    this.myClass =  "Supported";
}

SupportedList.prototype = new SIPHeaderList();
SupportedList.prototype.constructor=SupportedList;
SupportedList.prototype.NAME="Supported";

SupportedList.prototype.clone =function(){
}

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
 *  Implementation of the JAIN-SIP HeaderFactoryImpl .
 *  @see  gov/nist/javax/sip/header/HeaderFactoryImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function HeaderFactoryImpl() {
    this.classname="HeaderFactoryImpl";
    this.stripAddressScopeZones = false;
}

HeaderFactoryImpl.prototype = new SIPHeaderList();
HeaderFactoryImpl.prototype.constructor=HeaderFactoryImpl;
HeaderFactoryImpl.prototype.NAME="Authorization";

HeaderFactoryImpl.prototype.setPrettyEncoding =function(flag){
    var sipheaderlist=new SIPHeaderList();
    sipheaderlist.setPrettyEncode(flag);
}

HeaderFactoryImpl.prototype.createAllowEventsHeader =function(eventType){
    if (eventType == null)
    {
        console.error("HeaderFactoryImpl:createAllowEventsHeader(): null arg eventType");
        throw "HeaderFactoryImpl:createAllowEventsHeader(): null arg eventType";
    }
    var allowEvents = new AllowEvents();
    allowEvents.setEventType(eventType);
    return allowEvents;
}

HeaderFactoryImpl.prototype.createAllowHeader =function(method){
    if (method == null)
    {
        console.error("HeaderFactoryImpl:createAllowHeader(): null arg method");
        throw "HeaderFactoryImpl:createAllowHeader(): null arg method"; 
    }
    var allow = new Allow();
    allow.setMethod(method);
    return allow;
}

HeaderFactoryImpl.prototype.createCSeqHeader =function(sequenceNumber,method){
    if (sequenceNumber < 0)
    {
        console.error("HeaderFactoryImpl:createCSeqHeader(): bad arg " + sequenceNumber);
        throw "HeaderFactoryImpl:createCSeqHeader(): bad arg " + sequenceNumber;
    }
    if (method == null)
    {
        console.error("HeaderFactoryImpl:createCSeqHeader(): null arg method");
        throw "HeaderFactoryImpl:createCSeqHeader(): null arg method";      
    }
    var cseq = new CSeq();
    cseq.setMethod(method);
    cseq.setSeqNumber(sequenceNumber);
    return cseq;
}

HeaderFactoryImpl.prototype.createCallIdHeader =function(callId){
    if (callId == null)
    {
        var chain=new Date();
        callId=new String(chain.getTime());
    }
    var c = new CallID();
    c.setCallId(callId);
    return c;
}

HeaderFactoryImpl.prototype.createContactHeader =function(){
    if(arguments.length==0)
    {
        var contact = new Contact();
        contact.setWildCardFlag(true);
        contact.setExpires(0);
        return contact;
    }
    else
    {
        var address=arguments[0];
        if (address == null)
        {
            console.error("HeaderFactoryImpl:createContactHeader(): null arg address");
            throw "HeaderFactoryImpl:createContactHeader(): null arg address";      
        }
        contact = new Contact();
        contact.setAddress(address);
        return contact;
    }  
}

HeaderFactoryImpl.prototype.createContentDispositionHeader =function(contentDisposition){
    if (contentDisposition < 0)
    {
        console.error("HeaderFactoryImpl:createContentDispositionHeader(): null arg contentDisposition");
        throw "HeaderFactoryImpl:createContentDispositionHeader(): null arg contentDisposition";
    }
    var c = new ContentDisposition();
    c.setDispositionType(contentDisposition);
    return c;
}

HeaderFactoryImpl.prototype.createContentLengthHeader =function(contentLength){
    if (contentLength < 0)
    {
        console.error("HeaderFactoryImpl:createContentLengthHeader(): bad contentLength");
        throw "HeaderFactoryImpl:createContentLengthHeader(): bad contentLength"; 
    }
    var c = new ContentLength();
    c.setContentLength(contentLength);
    return c;
}

HeaderFactoryImpl.prototype.createContentTypeHeader =function(contentType,contentSubType){
    if (contentType == null || contentSubType == null)
    {
        console.error("HeaderFactoryImpl:createContentTypeHeader(): null contentType or subType");
        throw "HeaderFactoryImpl:createContentTypeHeader(): null contentType or subType"; 
    }
    var c = new ContentType();
    c.setContentType(contentType);
    c.setContentSubType(contentSubType);
    return c;
}

HeaderFactoryImpl.prototype.createEventHeader =function(eventType){
    if (eventType == null)
    {
        console.error("HeaderFactoryImpl:createEventHeader(): null eventType");
        throw "HeaderFactoryImpl:createEventHeader(): null eventType"; 
    }
    var event = new Event();
    event.setEventType(eventType);
    return event;
}

HeaderFactoryImpl.prototype.createExpiresHeader =function(expires){
    if (expires < 0)
    {
        console.error("HeaderFactoryImpl:createExpiresHeader(): bad value " + expires);
        throw "HeaderFactoryImpl:createExpiresHeader(): bad value " + expires;
    }
    var e = new Expires();
    e.setExpires(expires);
    return e;
}


HeaderFactoryImpl.prototype.createExtensionHeader =function(name,value){
    if (name == null)
    {
        console.error("HeaderFactoryImpl:createExtensionHeader(): bad name");
        throw "HeaderFactoryImpl:createExtensionHeader(): bad name";
    }
    var ext = new ExtensionHeaderImpl();
    ext.setName(name);
    ext.setValue(value);
    return ext;
}

HeaderFactoryImpl.prototype.createFromHeader =function(address,tag){
    if (address == null)
    {
        console.error("HeaderFactoryImpl:createFromHeader(): null address arg");
        throw "HeaderFactoryImpl:createFromHeader(): null address arg";
    }
    var from = new From();
    from.setAddress(address);
    if (tag != null)
    {
        from.setTag(tag);
    }
    return from;
}

HeaderFactoryImpl.prototype.createMaxForwardsHeader =function(maxForwards){
    if (maxForwards < 0 || maxForwards > 255)
    {
        console.error("HeaderFactoryImpl:createMaxForwardsHeader(): bad maxForwards arg " + maxForwards);
        throw "HeaderFactoryImpl:createMaxForwardsHeader(): bad maxForwards arg " + maxForwards;
    }
    var m = new MaxForwards();
    m.setMaxForwards(maxForwards);
    return m;
}

HeaderFactoryImpl.prototype.createProxyAuthenticateHeader =function(scheme){
    if (scheme == null)
    {
        console.error("HeaderFactoryImpl:createProxyAuthenticateHeader(): null scheme arg");
        throw "HeaderFactoryImpl:createProxyAuthenticateHeader(): null scheme arg";
    }
    var p = new ProxyAuthenticate();
    p.setScheme(scheme);
    return p;
}


HeaderFactoryImpl.prototype.createProxyAuthorizationHeader =function(scheme){
    if (scheme == null)
    {
        console.error("HeaderFactoryImpl:createProxyAuthorizationHeader(): null scheme arg");
        throw "HeaderFactoryImpl:createProxyAuthorizationHeader():null scheme arg";      
    }
    var p = new ProxyAuthorization();
    p.setScheme(scheme);
    return p;
}

HeaderFactoryImpl.prototype.createReasonHeader =function(protocol,cause,text){
    if (protocol == null)
    {
        console.error("HeaderFactoryImpl:createReasonHeader(): null protocol arg");
        throw "HeaderFactoryImpl:createReasonHeader(): null protocol arg";    
    }
    if (cause < 0)
    {
        console.error("HeaderFactoryImpl:createReasonHeader(): bad cause");
        throw "HeaderFactoryImpl:createReasonHeader():bad cause";   
    }
    var reason = new Reason();
    reason.setProtocol(protocol);
    reason.setCause(cause);
    reason.setText(text);
    return reason;
}

HeaderFactoryImpl.prototype.createRecordRouteHeader =function(address){
    if ( address == null) 
    {
        console.error("HeaderFactoryImpl:createRecordRouteHeader(): null address arg");
        throw "HeaderFactoryImpl:createRecordRouteHeader(): null address arg";  
    }
    var recordRoute = new RecordRoute();
    recordRoute.setAddress(address);
    return recordRoute;
}

HeaderFactoryImpl.prototype.createRouteHeader =function(address){
    if (address == null)
    {
        console.error("HeaderFactoryImpl:createRouteHeader(): null address arg");
        throw "HeaderFactoryImpl:createRouteHeader(): null address arg";
    }
    var route = new Route();
    route.setAddress(address);
    return route;
}

HeaderFactoryImpl.prototype.createSubjectHeader =function(subject){
    if (subject == null)
    {
        console.error("HeaderFactoryImpl:createSubjectHeader(): null subject arg");
        throw "HeaderFactoryImpl:createSubjectHeader():  null subject arg";
    }
    var s = new Subject();
    s.setSubject(subject);
    return s;
}

HeaderFactoryImpl.prototype.createSupportedHeader =function(optionTag){
    if (optionTag == null)
    {
        console.error("HeaderFactoryImpl:createSupportedHeader(): null optionTag arg");
        throw "HeaderFactoryImpl:createSupportedHeader(): null optionTag arg";
    }
    var supported = new Supported();
    supported.setOptionTag(optionTag);
    return supported;
}

HeaderFactoryImpl.prototype.createTimeStampHeader =function(timeStamp){
    if (timeStamp < 0)
    {
        console.error("HeaderFactoryImpl:createTimeStampHeader(): illegal timeStamp");
        throw "HeaderFactoryImpl:createTimeStampHeader(): illegal timeStamp";
    }
    var t = new TimeStamp();
    t.setTimeStamp(timeStamp);
    return t;
}

HeaderFactoryImpl.prototype.createToHeader =function(address,tag){
    if (address == null)
    {
        console.error("HeaderFactoryImpl:createSupportedHeader(): null address arg");
        throw "HeaderFactoryImpl:createSupportedHeader(): null address arg";
    }
    var to = new To();
    to.setAddress(address);
    if (tag != null)
    {
        to.setTag(tag);
    }
    return to;
}

HeaderFactoryImpl.prototype.createUserAgentHeader =function(product){
    if (product == null)
    {
        console.error("HeaderFactoryImpl:createUserAgentHeader(): null product arg");
        throw "HeaderFactoryImpl:createUserAgentHeader(): null product arg";
    }
    var userAgent = new UserAgent();
    userAgent.setProduct(product);
    return userAgent;
}

HeaderFactoryImpl.prototype.createViaHeader =function(host,port,transport,branch){
    if (host == null || transport == null)
    {
        console.error("HeaderFactoryImpl:createViaHeader(): null host or  transport arg");
        throw "HeaderFactoryImpl:createViaHeader(): null host or  transport arg";
    }
    var via = new Via();
    if (branch != null)
    {
        via.setBranch(branch);
    }
    if(host.indexOf(':') >= 0&& host.indexOf('[') < 0)
    {
        if(this.stripAddressScopeZones)
        {
            var zoneStart = host.indexOf('%');
            if(zoneStart != -1)
            {
                host = host.substring(0, zoneStart);
            }
        }
        host = "[" + host + "]";
    }
    via.setHost(host);
    if(port != null)
    {
        via.setPort(port);
    }
    if(transport != null)
    {
        via.setTransport(transport);
    }
    else
    {
        via.setTransport("WS");
    }
    return via;
}


HeaderFactoryImpl.prototype.createWWWAuthenticateHeader =function(scheme){
    if (scheme == null)
    {
        console.error("HeaderFactoryImpl:createWWWAuthenticateHeader(): null scheme arg");
        throw "HeaderFactoryImpl:createWWWAuthenticateHeader(): null scheme arg";
    }
    var www = new WWWAuthenticate();
    www.setScheme(scheme);
    return www;
}

HeaderFactoryImpl.prototype.createHeader =function(){
    if(arguments.length==1)
    {
        var headerText = arguments[0];
        var smp = new StringMsgParser();
        var sipHeader = smp.parseSIPHeader(headerText.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, ''));
        
        if(sipHeader instanceof SIPHeaderList)
        {
            if (sipHeader.size() > 1) 
            {
                console.error("HeaderFactoryImpl:createHeader(): only singleton allowed " + headerText);
                throw "HeaderFactoryImpl:createHeader():only singleton allowed " + headerText;
            }
            else if (sipHeader.size() == 0) 
            {
                var classname= sipHeader.getMyClass();
                var header=new Function('return new ' + classname)();
                return  header;
            }
            else 
            {
                return sipHeader.getFirst();
            }
        }
        else
        {
            return sipHeader;
        }
    }
    else if(arguments.length==2)
    {
        var headerName = arguments[0];
        var headerValue = arguments[1];
        if (headerName == null)
        {
            console.error("header name is null");
            console.error("HeaderFactoryImpl:createHeader(): header name is null");
            throw "HeaderFactoryImpl:createHeader(): header name is null";
        }
        var hdrText =headerName+":"+headerValue;
        return this.createHeader(hdrText);
    }
}


HeaderFactoryImpl.prototype.createHeaders =function(headers){
    if (headers == null)
    {
        console.error("HeaderFactoryImpl:createHeaders(): null headers arg");
        throw "HeaderFactoryImpl:createHeaders(): null headers arg";
    }
    var smp = new StringMsgParser();
    var shdr = smp.parseSIPHeader(headers);
    if (shdr instanceof SIPHeaderList)
    {
        return  shdr;
    }
    else
    {
        console.error("HeaderFactoryImpl:createHeaders():List of headers of this type is not allowed in a message", 0);
        throw "HeaderFactoryImpl:createHeaders(): list of headers of this type is not allowed in a message";
    }
}

HeaderFactoryImpl.prototype.createRouteList =function(recordRouteList){
    if(recordRouteList!=null)
    {
        var routeList = new RouteList();
        for(var i=recordRouteList.getHeaderList().length-1;i>=0;i--)
        {
            var rr = recordRouteList.getHeaderList()[i];
            var route = new Route();
            var address =  rr.getAddress();

            route.setAddress(address);
            route.setParameters(rr.getParameters());
            routeList.add(route);
        }
        return routeList;
    }
    else
    {
        return new RouteList();
    }
}


HeaderFactoryImpl.prototype.createRequestLine =function(requestLine){
    var requestLineParser = new RequestLineParser(requestLine);
    return requestLineParser.parse();
}


HeaderFactoryImpl.prototype.createStatusLine =function(statusLine){
    var statusLineParser = new StatusLineParser(statusLine);
    return statusLineParser.parse();
}

HeaderFactoryImpl.prototype.createAuthorizationHeader =function(){
    if(arguments.length==1)
    {
        var scheme=arguments[0];
        return this.createAuthorizationHeaderargu1(scheme);
    }
    else
    {
        var response=arguments[0];
        var request=arguments[1];
        var sipPassword=arguments[2];
        var sipLogin=arguments[3];
        var sipDomainUri=arguments[4];
        return this.createAuthorizationHeaderargu2(response, request, sipPassword, sipLogin);
    }
}

HeaderFactoryImpl.prototype.createAuthorizationHeaderargu1 =function(scheme){
    if (scheme == null)
    {
        console.error("HeaderFactoryImpl:createAuthorizationHeaderargu1(): null scheme arg");
        throw "HeaderFactoryImpl:createAuthorizationHeaderargu1(): null scheme arg";
    }
    var auth = new Authorization();
    auth.setScheme(scheme);
    return auth;
}


HeaderFactoryImpl.prototype.createAuthorizationHeaderargu2 =function(response,request,sipPassword,sipLogin){
    if(response.hasHeader("www-authenticate"))
    {
        var realm=response.getWWWAuthenticate().getRealm();
        var scheme=response.getWWWAuthenticate().getScheme();
        var nonce=response.getWWWAuthenticate().getNonce();
        var qop=response.getWWWAuthenticate().getQop();
        var authorization=new Authorization();
    }
    else if(response.hasHeader("proxy-authenticate"))
    {
        realm=response.getProxyAuthenticate().getRealm();
        scheme=response.getProxyAuthenticate().getScheme();
        nonce=response.getProxyAuthenticate().getNonce(); 
        var proxyauthorization=new ProxyAuthorization();
        qop=response.getWWWAuthenticate().getQop();
    }
    var mda=new MessageDigestAlgorithm();
    var method=response.getCSeq().getMethod();

    var cnonce=Math.floor(Math.random()*16777215).toString(16);
    var nc="00000001"; 
    var resp=mda.calculateResponse(sipLogin,realm,sipPassword,nonce,nc,cnonce, method,request.getRequestURI(),null,qop);
    
    if(response.hasHeader("www-authenticate"))
    {
        authorization.setUsername(sipLogin);
        authorization.setRealm(realm);
        authorization.setNonce(nonce);
        authorization.setCNonce(cnonce);
        authorization.setNonceCount(nc);
        authorization.setScheme(scheme);
        authorization.setResponse(resp);
        authorization.setURI(request.getRequestURI());
        authorization.setAlgorithm("MD5");
        authorization.setQop(qop);
        return authorization;
    }
    else if(response.hasHeader("proxy-authenticate"))
    {
        proxyauthorization.setUsername(sipLogin);
        proxyauthorization.setRealm(realm);
        proxyauthorization.setNonce(nonce);
        authorization.setCNonce(cnonce);
        authorization.setNonceCount(nc);
        proxyauthorization.setScheme(scheme);
        proxyauthorization.setResponse(resp);
        proxyauthorization.setURI(request.getRequestURI());
        proxyauthorization.setAlgorithm("MD5");
        return proxyauthorization;
    }
}/*
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
 *  Implementation of the JAIN-SIP Parser .
 *  @see  gov/nist/javax/sip/parser/Parser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function Parser() {
    this.classname="Parser"; 
}

Parser.prototype = new ParserCore();
Parser.prototype.constructor=Parser;
Parser.prototype.INVITE=LexerCore.prototype.START+5;
Parser.prototype.ACK=LexerCore.prototype.START+6;
Parser.prototype.OPTIONS=LexerCore.prototype.START+8;
Parser.prototype.BYE=LexerCore.prototype.START+7;
Parser.prototype.REGISTER=LexerCore.prototype.START+4;
Parser.prototype.CANCEL=LexerCore.prototype.START+9;
Parser.prototype.SUBSCRIBE=LexerCore.prototype.START+53;
Parser.prototype.NOTIFY=LexerCore.prototype.START+54;
Parser.prototype.PUBLISH=LexerCore.prototype.START+67;
Parser.prototype.MESSAGE=LexerCore.prototype.START+70;
Parser.prototype.ID=LexerCore.prototype.ID;
Parser.prototype.SIP=LexerCore.prototype.START+3;

Parser.prototype.createParseException =function(){
}

Parser.prototype.getLexer =function(){
    return this.lexer;
}

Parser.prototype.sipVersion =function(){
    var tok = this.lexer.match(this.SIP);
    if (tok.getTokenValue().toUpperCase()!="SIP") {
        this.createParseException("Expecting SIP");
    }
    this.lexer.match('/');
    tok = this.lexer.match(this.ID);
    if (tok.getTokenValue()!="2.0") {
        this.createParseException("Expecting SIP/2.0");
    }
    return "SIP/2.0";
    
}
Parser.prototype.method =function(){
    var tokens = this.lexer.peekNextToken(1);
    var token =  tokens[0];
    if (token.getTokenType() == this.INVITE
        || token.getTokenType() == this.ACK
        || token.getTokenType() == this.OPTIONS
        || token.getTokenType() == this.BYE
        || token.getTokenType() == this.REGISTER
        || token.getTokenType() == this.CANCEL
        || token.getTokenType() == this.SUBSCRIBE
        || token.getTokenType() == this.NOTIFY
        || token.getTokenType() == this.PUBLISH
        || token.getTokenType() == this.MESSAGE
        || token.getTokenType() == this.ID) {
        this.lexer.consume();
        return token.getTokenValue();
    } else {
        console.error("Parser:method(): invalid Method");
        throw "Parser:method(): invalid Method";
    }
   
}
Parser.prototype.checkToken =function(token){
    if (token == null || token.length == 0) {
        console.error("Parser:checkToken(): null or empty token");
        throw "Parser:method(): null or empty token";
    } else {
        for (var i = 0; i < token.length; ++i) {
            var lc=new LexerCore();
            if (!lc.isTokenChar(token.charAt(i))) {
                console.error("Parser:checkToken(): invalid character(s) in string (not allowed in 'token')",i);
                throw "Parser:method(): invalid character(s) in string (not allowed in 'token')";
            }
        }
    }
}/*
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
 *  Implementation of the JAIN-SIP Lexer .
 *  @see  gov/nist/javax/sip/parser/HeaderParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function Lexer(lexerName, buffer) {
    this.classname="Lexer"; 
    this.buffer = buffer;
    this.bufferLen = buffer.length;
    this.ptr = 0;
    this.currentLexer = new Array();
    this.currentLexerName = lexerName;
    this.selectLexer(lexerName);
}

Lexer.prototype = new LexerCore();
Lexer.prototype.constructor=Lexer;
Lexer.prototype.ErrorInfoHeader="Error-Info";
Lexer.prototype.AllowEventsHeader="Allow-Events";
Lexer.prototype.AuthenticationInfoHeader="Authentication-Info";
Lexer.prototype.EventHeader="Event";
Lexer.prototype.MinExpiresHeader="Min-Expires";
Lexer.prototype.RSeqHeader="RSeq";
Lexer.prototype.RAckHeader="RAck";
Lexer.prototype.ReasonHeader="Reason";
Lexer.prototype.ReplyToHeader="Reply-To";
Lexer.prototype.SubscriptionStateHeader="Subscription-State";
Lexer.prototype.TimeStampHeader="Timestamp";
Lexer.prototype.InReplyToHeader="In-Reply-To";
Lexer.prototype.MimeVersionHeader="MIME-Version";
Lexer.prototype.AlertInfoHeader="Alert-Info";
Lexer.prototype.FromHeader="From";
Lexer.prototype.ToHeader="To";
Lexer.prototype.ReferToHeader="Refer-To";
Lexer.prototype.ViaHeader="Via";
Lexer.prototype.UserAgentHeader="User-Agent";
Lexer.prototype.ServerHeader="Server";
Lexer.prototype.AcceptEncodingHeader="Accept-Encoding";
Lexer.prototype.AcceptHeader="Accept";
Lexer.prototype.AllowHeader="Allow";
Lexer.prototype.RouteHeader="Route";
Lexer.prototype.AuthorizationHeader="Authorization";
Lexer.prototype.ProxyAuthorizationHeader="Proxy-Authorization";
Lexer.prototype.RetryAfterHeader="Retry-After";
Lexer.prototype.ProxyRequireHeader="Proxy-Require";
Lexer.prototype.ContentLanguageHeader="Content-Language";
Lexer.prototype.UnsupportedHeader="Unsupported";
Lexer.prototype.SupportedHeader="Supported";
Lexer.prototype.WarningHeader="Warning";
Lexer.prototype.MaxForwardsHeader="Max-Forwards";
Lexer.prototype.DateHeader="Date";
Lexer.prototype.PriorityHeader="Priority";
Lexer.prototype.ProxyAuthenticateHeader="Proxy-Authenticate";
Lexer.prototype.ContentEncodingHeader="Content-Encoding";
Lexer.prototype.ContentLengthHeader="Content-Length";
Lexer.prototype.SubjectHeader="Subject";
Lexer.prototype.ContentTypeHeader="Content-Type";
Lexer.prototype.ContactHeader="Contact";
Lexer.prototype.CallIdHeader="Call-ID";
Lexer.prototype.RequireHeader="Require";
Lexer.prototype.ExpiresHeader="Expires";
Lexer.prototype.RecordRouteHeader="Record-Route";
Lexer.prototype.OrganizationHeader="Organization";
Lexer.prototype.CSeqHeader="CSeq";
Lexer.prototype.AcceptLanguageHeader="Accept-Language";
Lexer.prototype.WWWAuthenticateHeader="WWW-Authenticate";
Lexer.prototype.CallInfoHeader="Call-Info";
Lexer.prototype.ContentDispositionHeader="Content-Disposition";
Lexer.prototype.SIPETagHeader="SIP-ETag";
Lexer.prototype.SIPIfMatchHeader="SIP-If-Match";
Lexer.prototype.SessionExpiresHeader="Session-Expires";
Lexer.prototype.MinSEHeader="Min-SE";
Lexer.prototype.ReferredByHeader="Referred-By";
Lexer.prototype.ReplacesHeader="Replaces";
Lexer.prototype.JoinHeader="Join";
Lexer.prototype.PathHeader="Path";
Lexer.prototype.ServiceRouteHeader="Service-Route";
Lexer.prototype.PAssertedIdentityHeader="P-Asserted-Identity";
Lexer.prototype.PPreferredIdentityHeader="P-Preferred-Identity";
Lexer.prototype.PrivacyHeader="Privacy";
Lexer.prototype.PCalledPartyIDHeader="P-Called-Party-ID";
Lexer.prototype.PAssociatedURIHeader="P-Associated-URI";
Lexer.prototype.PVisitedNetworkIDHeader="P-Visited-Network-ID";
Lexer.prototype.PChargingFunctionAddressesHeader="P-Charging-Function-Addresses";
Lexer.prototype.PChargingVectorHeader="P-Charging-Vector";
Lexer.prototype.PAccessNetworkInfoHeader="P-Access-Network-Info";
Lexer.prototype.PMediaAuthorizationHeader="P-Media-Authorization";
Lexer.prototype.SecurityServerHeader="Security-Server";
Lexer.prototype.SecurityVerifyHeader="Security-Verify";
Lexer.prototype.SecurityClientHeader="Security-Client";
Lexer.prototype.PUserDatabaseHeader="P-User-Database";
Lexer.prototype.PProfileKeyHeader="P-Profile-Key";
Lexer.prototype.PServedUserHeader="P-Served-User";
Lexer.prototype.PPreferredServiceHeader="P-Preferred-Service";
Lexer.prototype.PAssertedServiceHeader="P-Asserted-Service";
Lexer.prototype.ReferencesHeader="References";
Lexer.prototype.AcceptContact="Accept-Contact";


Lexer.prototype.getHeaderName =function(line){
    if (line == null)
    {
        return null;
    }
    var headerName = null;
    try {
        var begin = line.indexOf(":");
        headerName = null;
        if (begin >= 1)
        {
            headerName = line.substring(0, begin).replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
        }
    } catch (ex) {
        console.error("Lexer:getHeaderName(): catched exception:"+ex);
        return null;
    }
    return headerName;
}
Lexer.prototype.getHeaderValue =function(line){
    if (line == null)
    {
        return null;
    }
    var headerValue = null;
    try {
        var begin = line.indexOf(":");
        headerValue = line.substring(begin + 1);
    } catch (ex) {
        console.error("Lexer:getHeaderValue(): catched exception:"+ex);
        return null;
    }
    return headerValue;
}
Lexer.prototype.selectLexer =function(lexerName){
    //in javascript, we can not realize thread, so i ignore the key work synchronized in the function
    var n=null;
    for(var i=0;i<this.lexerTables.length;i++)
    {
        if(this.lexerTables[i][0]==lexerName)
        {
            n=i;
        }
    }
    if(n!=null)
    {
        this.currentLexer = this.lexerTables[n][1];
    }
    this.currentLexerName = lexerName;
    if (this.currentLexer.length == 0) {
        this.addLexer(lexerName);
        if (lexerName=="method_keywordLexer") {
            this.addKeyword(TokenNames.prototype.REGISTER, TokenTypes.prototype.REGISTER);
            this.addKeyword(TokenNames.prototype.ACK, TokenTypes.prototype.ACK);
            this.addKeyword(TokenNames.prototype.OPTIONS, TokenTypes.prototype.OPTIONS);
            this.addKeyword(TokenNames.prototype.BYE, TokenTypes.prototype.BYE);
            this.addKeyword(TokenNames.prototype.INVITE, TokenTypes.prototype.INVITE);
            this.addKeyword(TokenNames.prototype.SIP.toUpperCase(), TokenTypes.prototype.SIP);
            this.addKeyword(TokenNames.prototype.SIPS.toUpperCase(), TokenTypes.prototype.SIPS);
            this.addKeyword(TokenNames.prototype.SUBSCRIBE, TokenTypes.prototype.SUBSCRIBE);
            this.addKeyword(TokenNames.prototype.NOTIFY, TokenTypes.prototype.NOTIFY);
            this.addKeyword(TokenNames.prototype.MESSAGE, TokenTypes.prototype.MESSAGE);
            this.addKeyword(TokenNames.prototype.PUBLISH, TokenTypes.prototype.PUBLISH);

        } else if (lexerName=="command_keywordLexer") {
            this.addKeyword(this.ErrorInfoHeader.toUpperCase(),
                TokenTypes.prototype.ERROR_INFO);
            this.addKeyword(this.AllowEventsHeader.toUpperCase(),
                TokenTypes.prototype.ALLOW_EVENTS);
            this.addKeyword(this.AuthenticationInfoHeader.toUpperCase(),
                TokenTypes.prototype.AUTHENTICATION_INFO);
            this.addKeyword(this.EventHeader.toUpperCase(), TokenTypes.prototype.EVENT);
            this.addKeyword(this.MinExpiresHeader.toUpperCase(),
                TokenTypes.prototype.MIN_EXPIRES);
            this.addKeyword(this.RSeqHeader.toUpperCase(), TokenTypes.prototype.RSEQ);
            this.addKeyword(this.RAckHeader.toUpperCase(), TokenTypes.prototype.RACK);
            this.addKeyword(this.ReasonHeader.toUpperCase(),
                TokenTypes.prototype.REASON);
            this.addKeyword(this.ReplyToHeader.toUpperCase(),
                TokenTypes.prototype.REPLY_TO);
            this.addKeyword(this.SubscriptionStateHeader.toUpperCase(),
                TokenTypes.prototype.SUBSCRIPTION_STATE);
            this.addKeyword(this.TimeStampHeader.toUpperCase(),
                TokenTypes.prototype.TIMESTAMP);
            this.addKeyword(this.InReplyToHeader.toUpperCase(),
                TokenTypes.prototype.IN_REPLY_TO);
            this.addKeyword(this.MimeVersionHeader.toUpperCase(),
                TokenTypes.prototype.MIME_VERSION);
            this.addKeyword(this.AlertInfoHeader.toUpperCase(),
                TokenTypes.prototype.ALERT_INFO);
            this.addKeyword(this.FromHeader.toUpperCase(), TokenTypes.prototype.FROM);
            this.addKeyword(this.ToHeader.toUpperCase(), TokenTypes.prototype.TO);
            this.addKeyword(this.ReferToHeader.toUpperCase(),
                TokenTypes.prototype.REFER_TO);
            this.addKeyword(this.ViaHeader.toUpperCase(), TokenTypes.prototype.VIA);
            this.addKeyword(this.UserAgentHeader.toUpperCase(),
                TokenTypes.prototype.USER_AGENT);
            this.addKeyword(this.ServerHeader.toUpperCase(),
                TokenTypes.prototype.SERVER);
            this.addKeyword(this.AcceptEncodingHeader.toUpperCase(),
                TokenTypes.prototype.ACCEPT_ENCODING);
            this.addKeyword(this.AcceptHeader.toUpperCase(),
                TokenTypes.prototype.ACCEPT);
            this.addKeyword(this.AllowHeader.toUpperCase(), TokenTypes.prototype.ALLOW);
            this.addKeyword(this.RouteHeader.toUpperCase(), TokenTypes.prototype.ROUTE);
            this.addKeyword(this.AuthorizationHeader.toUpperCase(),
                TokenTypes.prototype.AUTHORIZATION);
            this.addKeyword(this.ProxyAuthorizationHeader.toUpperCase(),
                TokenTypes.prototype.PROXY_AUTHORIZATION);
            this.addKeyword(this.RetryAfterHeader.toUpperCase(),
                TokenTypes.prototype.RETRY_AFTER);
            this.addKeyword(this.ProxyRequireHeader.toUpperCase(),
                TokenTypes.prototype.PROXY_REQUIRE);
            this.addKeyword(this.ContentLanguageHeader.toUpperCase(),
                TokenTypes.prototype.CONTENT_LANGUAGE);
            this.addKeyword(this.UnsupportedHeader.toUpperCase(),
                TokenTypes.prototype.UNSUPPORTED);
            this.addKeyword(this.SupportedHeader.toUpperCase(),
                TokenTypes.prototype.SUPPORTED);
            this.addKeyword(this.WarningHeader.toUpperCase(),
                TokenTypes.prototype.WARNING);
            this.addKeyword(this.MaxForwardsHeader.toUpperCase(),
                TokenTypes.prototype.MAX_FORWARDS);
            this.addKeyword(this.DateHeader.toUpperCase(), TokenTypes.prototype.DATE);
            this.addKeyword(this.PriorityHeader.toUpperCase(),
                TokenTypes.prototype.PRIORITY);
            this.addKeyword(this.ProxyAuthenticateHeader.toUpperCase(),
                TokenTypes.prototype.PROXY_AUTHENTICATE);
            this.addKeyword(this.ContentEncodingHeader.toUpperCase(),
                TokenTypes.prototype.CONTENT_ENCODING);
            this.addKeyword(this.ContentLengthHeader.toUpperCase(),
                TokenTypes.prototype.CONTENT_LENGTH);
            this.addKeyword(this.SubjectHeader.toUpperCase(),
                TokenTypes.prototype.SUBJECT);
            this.addKeyword(this.ContentTypeHeader.toUpperCase(),
                TokenTypes.prototype.CONTENT_TYPE);
            this.addKeyword(this.ContactHeader.toUpperCase(),
                TokenTypes.prototype.CONTACT);
            this.addKeyword(this.CallIdHeader.toUpperCase(),
                TokenTypes.prototype.CALL_ID);
            this.addKeyword(this.RequireHeader.toUpperCase(),
                TokenTypes.prototype.REQUIRE);
            this.addKeyword(this.ExpiresHeader.toUpperCase(),
                TokenTypes.prototype.EXPIRES);
            this.addKeyword(this.RecordRouteHeader.toUpperCase(),
                TokenTypes.prototype.RECORD_ROUTE);
            this.addKeyword(this.OrganizationHeader.toUpperCase(),
                TokenTypes.prototype.ORGANIZATION);
            this.addKeyword(this.CSeqHeader.toUpperCase(), TokenTypes.prototype.CSEQ);
            this.addKeyword(this.AcceptLanguageHeader.toUpperCase(),
                TokenTypes.prototype.ACCEPT_LANGUAGE);
            this.addKeyword(this.WWWAuthenticateHeader.toUpperCase(),
                TokenTypes.prototype.WWW_AUTHENTICATE);
            this.addKeyword(this.CallInfoHeader.toUpperCase(),
                TokenTypes.prototype.CALL_INFO);
            this.addKeyword(this.ContentDispositionHeader.toUpperCase(),
                TokenTypes.prototype.CONTENT_DISPOSITION);
            // And now the dreaded short forms....
            this.addKeyword(TokenNames.prototype.K.toUpperCase(), TokenTypes.prototype.SUPPORTED);
            this.addKeyword(TokenNames.prototype.C.toUpperCase(),
                TokenTypes.prototype.CONTENT_TYPE);
            this.addKeyword(TokenNames.prototype.E.toUpperCase(),
                TokenTypes.prototype.CONTENT_ENCODING);
            this.addKeyword(TokenNames.prototype.F.toUpperCase(), TokenTypes.prototype.FROM);
            this.addKeyword(TokenNames.prototype.I.toUpperCase(), TokenTypes.prototype.CALL_ID);
            this.addKeyword(TokenNames.prototype.M.toUpperCase(), TokenTypes.prototype.CONTACT);
            this.addKeyword(TokenNames.prototype.L.toUpperCase(),
                TokenTypes.prototype.CONTENT_LENGTH);
            this.addKeyword(TokenNames.prototype.S.toUpperCase(), TokenTypes.prototype.SUBJECT);
            this.addKeyword(TokenNames.prototype.T.toUpperCase(), TokenTypes.prototype.TO);
            this.addKeyword(TokenNames.prototype.U.toUpperCase(),
                TokenTypes.prototype.ALLOW_EVENTS); // JvB: added
            this.addKeyword(TokenNames.prototype.V.toUpperCase(), TokenTypes.prototype.VIA);
            this.addKeyword(TokenNames.prototype.R.toUpperCase(), TokenTypes.prototype.REFER_TO);
            this.addKeyword(TokenNames.prototype.O.toUpperCase(), TokenTypes.prototype.EVENT); // Bug fix by Mario Mantak
            this.addKeyword(TokenNames.prototype.X.toUpperCase(), TokenTypes.prototype.SESSIONEXPIRES_TO); // Bug fix by Jozef Saniga
                    
            // JvB: added to support RFC3903
            this.addKeyword(this.SIPETagHeader.toUpperCase(),
                TokenTypes.prototype.SIP_ETAG);
            this.addKeyword(this.SIPIfMatchHeader.toUpperCase(),
                TokenTypes.prototype.SIP_IF_MATCH);

            // pmusgrave: Add RFC4028 and ReferredBy
            this.addKeyword(this.SessionExpiresHeader.toUpperCase(),
                TokenTypes.prototype.SESSIONEXPIRES_TO);
            this.addKeyword(this.MinSEHeader.toUpperCase(),
                TokenTypes.prototype.MINSE_TO);
            this.addKeyword(this.ReferredByHeader.toUpperCase(), TokenTypes.prototype.REFERREDBY_TO);
            this.addKeyword(TokenNames.prototype.B.toUpperCase(), TokenTypes.prototype.REFERREDBY_TO); // Bug fix OrangeLabs, AUFFRET Jean-Marc


            // pmusgrave RFC3891
            this.addKeyword(this.ReplacesHeader.toUpperCase(),
                TokenTypes.prototype.REPLACES_TO);
            //jean deruelle RFC3911
            this.addKeyword(this.JoinHeader.toUpperCase(),
                TokenTypes.prototype.JOIN_TO);

            // IMS Headers
            this.addKeyword(this.PathHeader.toUpperCase(), TokenTypes.prototype.PATH);
            this.addKeyword(this.ServiceRouteHeader.toUpperCase(),
                TokenTypes.prototype.SERVICE_ROUTE);
            this.addKeyword(this.PAssertedIdentityHeader.toUpperCase(),
                TokenTypes.prototype.P_ASSERTED_IDENTITY);
            this.addKeyword(this.PPreferredIdentityHeader.toUpperCase(),
                TokenTypes.prototype.P_PREFERRED_IDENTITY);
            this.addKeyword(this.PrivacyHeader.toUpperCase(),
                TokenTypes.prototype.PRIVACY);

            // issued by Miguel Freitas
            this.addKeyword(this.PCalledPartyIDHeader.toUpperCase(),
                TokenTypes.prototype.P_CALLED_PARTY_ID);
            this.addKeyword(this.PAssociatedURIHeader.toUpperCase(),
                TokenTypes.prototype.P_ASSOCIATED_URI);
            this.addKeyword(this.PVisitedNetworkIDHeader.toUpperCase(),
                TokenTypes.prototype.P_VISITED_NETWORK_ID);
            this.addKeyword(this.PChargingFunctionAddressesHeader
                .toUpperCase(),
                TokenTypes.prototype.P_CHARGING_FUNCTION_ADDRESSES);
            this.addKeyword(this.PChargingVectorHeader.toUpperCase(),
                TokenTypes.prototype.P_VECTOR_CHARGING);
            this.addKeyword(this.PAccessNetworkInfoHeader.toUpperCase(),
                TokenTypes.prototype.P_ACCESS_NETWORK_INFO);
            this.addKeyword(this.PMediaAuthorizationHeader.toUpperCase(),
                TokenTypes.prototype.P_MEDIA_AUTHORIZATION);

            this.addKeyword(this.SecurityServerHeader.toUpperCase(),
                TokenTypes.prototype.SECURITY_SERVER);
            this.addKeyword(this.SecurityVerifyHeader.toUpperCase(),
                TokenTypes.prototype.SECURITY_VERIFY);
            this.addKeyword(this.SecurityClientHeader.toUpperCase(),
                TokenTypes.prototype.SECURITY_CLIENT);

            // added by aayush@rancore
            this.addKeyword(this.PUserDatabaseHeader.toUpperCase(),
                TokenTypes.prototype.P_USER_DATABASE);

            // added by aayush@rancore
            this.addKeyword(this.PProfileKeyHeader.toUpperCase(),
                TokenTypes.prototype.P_PROFILE_KEY);

            // added by aayush@rancore
            this.addKeyword(this.PServedUserHeader.toUpperCase(),
                TokenTypes.prototype.P_SERVED_USER);

            // added by aayush@rancore
            this.addKeyword(this.PPreferredServiceHeader.toUpperCase(),
                TokenTypes.prototype.P_PREFERRED_SERVICE);

            // added by aayush@rancore
            this.addKeyword(this.PAssertedServiceHeader.toUpperCase(),
                TokenTypes.prototype.P_ASSERTED_SERVICE);
                    
            // added References header
            this.addKeyword(this.ReferencesHeader.toUpperCase(),TokenTypes.prototype.REFERENCES);
                        
                        // added Accept-Contact header
                        this.addKeyword(this.AcceptContact.toUpperCase(),TokenTypes.prototype.ACCEPT_CONTACT);
                        
        } else if (lexerName=="status_lineLexer") {
            this.addKeyword(TokenNames.prototype.SIP.toUpperCase(), TokenTypes.prototype.SIP);
        } else if (lexerName=="request_lineLexer") {
            this.addKeyword(TokenNames.prototype.SIP.toUpperCase(), TokenTypes.prototype.SIP);
        } else if (lexerName=="sip_urlLexer") {
            this.addKeyword(TokenNames.prototype.TEL.toUpperCase(), TokenTypes.prototype.TEL);
            this.addKeyword(TokenNames.prototype.SIP.toUpperCase(), TokenTypes.prototype.SIP);
            this.addKeyword(TokenNames.prototype.SIPS.toUpperCase(), TokenTypes.prototype.SIPS);
        }
    }
}/*
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
 *  Implementation of the JAIN-SIP HeaderParser .
 *  @see  gov/nist/javax/sip/parser/HeaderParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function HeaderParser() {
    this.classname="HeaderParser"; 
    if(typeof arguments[0]=="string")
    {
        var header=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", header);
    }
    else if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
}

HeaderParser.prototype = new Parser();
HeaderParser.prototype.constructor=HeaderParser;

HeaderParser.prototype.wkday =function(){
    var tok = this.lexer.ttoken();
    var id = tok.toLowerCase();
    if (TokenNames.prototype.MON==id.toLowerCase()) {
        return 2;//Calendar.MONDAY;
    } else if (TokenNames.prototype.TUE.toLowerCase()==id.toLowerCase()) {
        return 3;//calendar.TUESDAY;
    } else if (TokenNames.prototype.WED.toLowerCase()==id.toLowerCase()) {
        return 4;//calendar.WEDNESDAY;
    } else if (TokenNames.prototype.THU.toLowerCase()==id.toLowerCase()) {
        return 5;//calendar.THURSDAY;
    } else if (TokenNames.prototype.FRI.toLowerCase()==id.toLowerCase()) {
        return 6;//calendar.FRIDAY;
    } else if (TokenNames.prototype.SAT.toLowerCase()==id.toLowerCase()) {
        return 7;//calendar.SATURDAY;
    } else if (TokenNames.prototype.SUN.toLowerCase()==id.toLowerCase()) {
        return 1;//calendar.SUNDAY;
    } else {
        console.error("HeaderParser:wkday(): bad wkday");
        throw "HeaderParser:wkday(): bad wkday";
    }
}

HeaderParser.prototype.date =function(){
    try {
        var retval = new Date();
        var s1 = this.lexer.number();
        var day = s1+0;
        if (day <= 0 || day > 31) {
           console.error("HeaderParser:date(): bad day");
           throw "HeaderParser:date():  bad day";
        }
        retval.set(5, day);
        this.lexer.match(' ');
        var month = this.lexer.ttoken().toLowerCase();
        if (month.equals("jan")) {
            retval.setMonth( 0);//Calendar.MONTH=2
        } else if (month=="feb") {
            retval.setMonth(1);
        } else if (month=="mar") {
            retval.setMonth(2);
        } else if (month=="apr") {
            retval.setMonth(3);
        } else if (month=="may") {
            retval.setMonth(4);
        } else if (month=="jun") {
            retval.setMonth(5);
        } else if (month=="jul") {
            retval.setMonth(6);
        } else if (month=="aug") {
            retval.setMonth(7);
        } else if (month=="sep") {
            retval.setMonth(8);
        } else if (month=="oct") {
            retval.setMonth(9);
        } else if (month=="nov") {
            retval.setMonth(10);
        } else if (month=="dec") {
            retval.setMonth(11);
        }
        this.lexer.match(' ');
        var s2 = this.lexer.number();
        var yr = s2;
        retval.setYear(yr);
        return retval;
    } catch (ex) {
        console.error("HeaderParser:date(): bad date field");
        throw "HeaderParser:date(): bad date field";
    }
}

HeaderParser.prototype.time =function(calendar){
    try {
        var s = this.lexer.number();
        var hour = s;
        calendar.setHours(hour);
        this.lexer.match(':');
        s = this.lexer.number();
        var min = s;
        calendar.setMinutes(min);
        this.lexer.match(':');
        s = this.lexer.number();
        var sec = s;
        calendar.setSeconds(sec);
    } catch (ex) {
        console.error("HeaderParser:time(): error processing time ");
        throw "HeaderParser:time():error processing time ";
    }
}


HeaderParser.prototype.parse =function(){
    var name = this.lexer.getNextToken(':');
    this.lexer.consume(1);
    var body = this.lexer.getLine().replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
    var retval = new ExtensionHeaderImpl(name);
    retval.setValue(body);
    return retval;
}
HeaderParser.prototype.headerName =function(tok){
    this.lexer.match(tok);
    this.lexer.SPorHT();
    this.lexer.match(':');
    this.lexer.SPorHT();
}/*
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
 *  Implementation of the JAIN-SIP ParametersParser .
 *  @see  gov/nist/javax/sip/parser/ParametersParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function ParametersParser() {
    this.classname="ParametersParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var buffer=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", buffer);
    }
}

ParametersParser.prototype = new HeaderParser();
ParametersParser.prototype.constructor=ParametersParser;

ParametersParser.prototype.parse =function(parametersHeader){
    this.lexer.SPorHT();
    while (this.lexer.lookAhead(0) == ';') {
        this.lexer.consume(1);
        this.lexer.SPorHT();
        var nv = this.nameValue();
        parametersHeader.setParameter_nv(nv);
        this.lexer.SPorHT();
    }
}
ParametersParser.prototype.parseNameValueList =function(parametersHeader){
    parametersHeader.removeParameters();
    while (true) {
        this.lexer.SPorHT();
        var nv = nameValue();
      
        parametersHeader.setParameter(nv.getName(), nv.getValueAsObject());
        this.lexer.SPorHT();
        if (this.lexer.lookAhead(0) != ';')  {
            break;
        }
        else {
            this.lexer.consume(1);
        }
    }
}/*
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
 *  Implementation of the JAIN-SIP TokenTypes .
 *  @see  gov/nist/javax/sip/parser/TokenTypes.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function TokenTypes() {
    this.classname="TokenTypes"; 
}

TokenTypes.prototype.constructor=Parser;
TokenTypes.prototype.START = LexerCore.prototype.START;
// Everything under this is reserved
TokenTypes.prototype.END = LexerCore.prototype.END;
// End markder.

TokenTypes.prototype.SIP = LexerCore.prototype.START + 3;
TokenTypes.prototype.REGISTER = LexerCore.prototype.START + 4;
TokenTypes.prototype.INVITE = LexerCore.prototype.START + 5;
TokenTypes.prototype.ACK = LexerCore.prototype.START + 6;
TokenTypes.prototype.BYE = LexerCore.prototype.START + 7;
TokenTypes.prototype.OPTIONS = LexerCore.prototype.START + 8;
TokenTypes.prototype.CANCEL = LexerCore.prototype.START + 9;
TokenTypes.prototype.ERROR_INFO = LexerCore.prototype.START + 10;
TokenTypes.prototype.IN_REPLY_TO = LexerCore.prototype.START + 11;
TokenTypes.prototype.MIME_VERSION = LexerCore.prototype.START + 12;
TokenTypes.prototype.ALERT_INFO = LexerCore.prototype.START + 13;
TokenTypes.prototype.FROM = LexerCore.prototype.START + 14;
TokenTypes.prototype.TO = LexerCore.prototype.START + 15;
TokenTypes.prototype.VIA = LexerCore.prototype.START + 16;
TokenTypes.prototype.USER_AGENT = LexerCore.prototype.START + 17;
TokenTypes.prototype.SERVER = LexerCore.prototype.START + 18;
TokenTypes.prototype.ACCEPT_ENCODING = LexerCore.prototype.START + 19;
TokenTypes.prototype.ACCEPT = LexerCore.prototype.START + 20;
TokenTypes.prototype.ALLOW = LexerCore.prototype.START + 21;
TokenTypes.prototype.ROUTE = LexerCore.prototype.START + 22;
TokenTypes.prototype.AUTHORIZATION = LexerCore.prototype.START + 23;
TokenTypes.prototype.PROXY_AUTHORIZATION = LexerCore.prototype.START + 24;
TokenTypes.prototype.RETRY_AFTER = LexerCore.prototype.START + 25;
TokenTypes.prototype.PROXY_REQUIRE = LexerCore.prototype.START + 26;
TokenTypes.prototype.CONTENT_LANGUAGE = LexerCore.prototype.START + 27;
TokenTypes.prototype.UNSUPPORTED = LexerCore.prototype.START + 28;
TokenTypes.prototype.SUPPORTED = LexerCore.prototype.START + 20;
TokenTypes.prototype.WARNING = LexerCore.prototype.START + 30;
TokenTypes.prototype.MAX_FORWARDS = LexerCore.prototype.START + 31;
TokenTypes.prototype.DATE = LexerCore.prototype.START + 32;
TokenTypes.prototype.PRIORITY = LexerCore.prototype.START + 33;
TokenTypes.prototype.PROXY_AUTHENTICATE = LexerCore.prototype.START + 34;
TokenTypes.prototype.CONTENT_ENCODING = LexerCore.prototype.START + 35;
TokenTypes.prototype.CONTENT_LENGTH = LexerCore.prototype.START + 36;
TokenTypes.prototype.SUBJECT = LexerCore.prototype.START + 37;
TokenTypes.prototype.CONTENT_TYPE = LexerCore.prototype.START + 38;
TokenTypes.prototype.CONTACT = LexerCore.prototype.START + 39;
TokenTypes.prototype.CALL_ID = LexerCore.prototype.START + 40;
TokenTypes.prototype.REQUIRE = LexerCore.prototype.START + 41;
TokenTypes.prototype.EXPIRES = LexerCore.prototype.START + 42;
TokenTypes.prototype.ENCRYPTION = LexerCore.prototype.START + 43;
TokenTypes.prototype.RECORD_ROUTE = LexerCore.prototype.START + 44;
TokenTypes.prototype.ORGANIZATION = LexerCore.prototype.START + 45;
TokenTypes.prototype.CSEQ = LexerCore.prototype.START + 46;
TokenTypes.prototype.ACCEPT_LANGUAGE = LexerCore.prototype.START + 47;
TokenTypes.prototype.WWW_AUTHENTICATE = LexerCore.prototype.START + 48;
TokenTypes.prototype.RESPONSE_KEY = LexerCore.prototype.START + 49;
TokenTypes.prototype.HIDE = LexerCore.prototype.START + 50;
TokenTypes.prototype.CALL_INFO = LexerCore.prototype.START + 51;
TokenTypes.prototype.CONTENT_DISPOSITION = LexerCore.prototype.START + 52;
TokenTypes.prototype.SUBSCRIBE = LexerCore.prototype.START + 53;
TokenTypes.prototype.NOTIFY = LexerCore.prototype.START + 54;
TokenTypes.prototype.TIMESTAMP = LexerCore.prototype.START + 55;
TokenTypes.prototype.SUBSCRIPTION_STATE = LexerCore.prototype.START + 56;
TokenTypes.prototype.TEL = LexerCore.prototype.START + 57;
TokenTypes.prototype.REPLY_TO = LexerCore.prototype.START + 58;
TokenTypes.prototype.REASON = LexerCore.prototype.START + 59;
TokenTypes.prototype.RSEQ = LexerCore.prototype.START + 60;
TokenTypes.prototype.RACK = LexerCore.prototype.START + 61;
TokenTypes.prototype.MIN_EXPIRES = LexerCore.prototype.START + 62;
TokenTypes.prototype.EVENT = LexerCore.prototype.START + 63;
TokenTypes.prototype.AUTHENTICATION_INFO = LexerCore.prototype.START + 64;
TokenTypes.prototype.ALLOW_EVENTS = LexerCore.prototype.START + 65;
TokenTypes.prototype.REFER_TO = LexerCore.prototype.START + 66;

// JvB: added to support RFC3903
TokenTypes.prototype.PUBLISH = LexerCore.prototype.START + 67;
TokenTypes.prototype.SIP_ETAG = LexerCore.prototype.START + 68;
TokenTypes.prototype.SIP_IF_MATCH = LexerCore.prototype.START + 69;




TokenTypes.prototype.MESSAGE = LexerCore.prototype.START + 70;

// IMS Headers
TokenTypes.prototype.PATH = LexerCore.prototype.START + 71;
TokenTypes.prototype.SERVICE_ROUTE = LexerCore.prototype.START + 72;
TokenTypes.prototype.P_ASSERTED_IDENTITY = LexerCore.prototype.START + 73;
TokenTypes.prototype.P_PREFERRED_IDENTITY = LexerCore.prototype.START + 74;
TokenTypes.prototype.P_VISITED_NETWORK_ID = LexerCore.prototype.START + 75;
TokenTypes.prototype.P_CHARGING_FUNCTION_ADDRESSES = LexerCore.prototype.START + 76;
TokenTypes.prototype.P_VECTOR_CHARGING = LexerCore.prototype.START + 77;



// issued by Miguel Freitas - IMS headers
TokenTypes.prototype.PRIVACY = LexerCore.prototype.START + 78;
TokenTypes.prototype.P_ACCESS_NETWORK_INFO = LexerCore.prototype.START + 79;
TokenTypes.prototype.P_CALLED_PARTY_ID = LexerCore.prototype.START + 80;
TokenTypes.prototype.P_ASSOCIATED_URI = LexerCore.prototype.START + 81;
TokenTypes.prototype.P_MEDIA_AUTHORIZATION = LexerCore.prototype.START + 82;
TokenTypes.prototype.P_MEDIA_AUTHORIZATION_TOKEN = LexerCore.prototype.START + 83;


// pmusgrave - additions
TokenTypes.prototype.REFERREDBY_TO = LexerCore.prototype.START + 84;

// pmusgrave RFC4028
TokenTypes.prototype.SESSIONEXPIRES_TO = LexerCore.prototype.START + 85;
TokenTypes.prototype.MINSE_TO = LexerCore.prototype.START + 86;

// pmusgrave RFC3891
TokenTypes.prototype.REPLACES_TO = LexerCore.prototype.START + 87;

// pmusgrave sips bug fix
TokenTypes.prototype.SIPS = LexerCore.prototype.START + 88;


// issued by Miguel Freitas - SIP Security Agreement (RFC3329)
TokenTypes.prototype.SECURITY_SERVER = LexerCore.prototype.START + 89;
TokenTypes.prototype.SECURITY_CLIENT = LexerCore.prototype.START + 90;
TokenTypes.prototype.SECURITY_VERIFY = LexerCore.prototype.START + 91;

// jean deruelle RFC3911
TokenTypes.prototype.JOIN_TO = LexerCore.prototype.START + 92;

// aayush.bhatnagar: RFC 4457 support.
TokenTypes.prototype.P_USER_DATABASE = LexerCore.prototype.START + 93;
//aayush.bhatnagar: RFC 5002 support.
TokenTypes.prototype.P_PROFILE_KEY = LexerCore.prototype.START + 94;
//aayush.bhatnagar: RFC 5502 support.
TokenTypes.prototype.P_SERVED_USER = LexerCore.prototype.START + 95;
//aayush.bhatnaagr: P-Preferred-Service Header:
TokenTypes.prototype.P_PREFERRED_SERVICE = LexerCore.prototype.START + 96;
//aayush.bhatnagar: P-Asserted-Service Header:
TokenTypes.prototype.P_ASSERTED_SERVICE = LexerCore.prototype.START + 97;
//mranga - References header
TokenTypes.prototype.REFERENCES = LexerCore.prototype.START + 98;

TokenTypes.prototype.ACCEPT_CONTACT = LexerCore.prototype.START + 99;

TokenTypes.prototype.ALPHA = LexerCore.prototype.ALPHA;
TokenTypes.prototype.DIGIT = LexerCore.prototype.DIGIT;
TokenTypes.prototype.ID = LexerCore.prototype.ID;
TokenTypes.prototype.WHITESPACE = LexerCore.prototype.WHITESPACE;
TokenTypes.prototype.BACKSLASH = LexerCore.prototype.BACKSLASH;
TokenTypes.prototype.QUOTE = LexerCore.prototype.QUOTE;
TokenTypes.prototype.AT = LexerCore.prototype.AT;
TokenTypes.prototype.SP = LexerCore.prototype.SP;
TokenTypes.prototype.HT = LexerCore.prototype.HT;
TokenTypes.prototype.COLON = LexerCore.prototype.COLON;
TokenTypes.prototype.STAR = LexerCore.prototype.STAR;
TokenTypes.prototype.DOLLAR = LexerCore.prototype.DOLLAR;
TokenTypes.prototype.PLUS = LexerCore.prototype.PLUS;
TokenTypes.prototype.POUND = LexerCore.prototype.POUND;
TokenTypes.prototype.MINUS = LexerCore.prototype.MINUS;
TokenTypes.prototype.DOUBLEQUOTE = LexerCore.prototype.DOUBLEQUOTE;
TokenTypes.prototype.TILDE = LexerCore.prototype.TILDE;
TokenTypes.prototype.BACK_QUOTE = LexerCore.prototype.BACK_QUOTE;
TokenTypes.prototype.NULL = LexerCore.prototype.NULL;
TokenTypes.prototype.EQUALS =  '='.charCodeAt(0);
TokenTypes.prototype.SEMICOLON =  ';'.charCodeAt(0);
TokenTypes.prototype.SLASH =  '/'.charCodeAt(0);
TokenTypes.prototype.L_SQUARE_BRACKET =  '['.charCodeAt(0);
TokenTypes.prototype.R_SQUARE_BRACKET =  ']'.charCodeAt(0);
TokenTypes.prototype.R_CURLY =  '}'.charCodeAt(0);
TokenTypes.prototype.L_CURLY =  '{'.charCodeAt(0);
TokenTypes.prototype.HAT =  '^'.charCodeAt(0);
TokenTypes.prototype.BAR =  '|'.charCodeAt(0);
TokenTypes.prototype.DOT =  '.'.charCodeAt(0);
TokenTypes.prototype.EXCLAMATION =  '!'.charCodeAt(0);
TokenTypes.prototype.LPAREN =  '('.charCodeAt(0);
TokenTypes.prototype.RPAREN =  ')'.charCodeAt(0);
TokenTypes.prototype.GREATER_THAN =  '>'.charCodeAt(0);
TokenTypes.prototype.LESS_THAN =  '<'.charCodeAt(0);
TokenTypes.prototype.PERCENT =  '%'.charCodeAt(0);
TokenTypes.prototype.QUESTION =  '?'.charCodeAt(0);
TokenTypes.prototype.AND =  '&'.charCodeAt(0);
TokenTypes.prototype.UNDERSCORE =  '_'.charCodeAt(0);/*
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
 *  Implementation of the JAIN-SIP TokenNames .
 *  @see  gov/nist/javax/sip/parser/TokenNames.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function TokenNames() {
    this.classname="TokenNames";
}

TokenNames.prototype.INVITE = "INVITE";
TokenNames.prototype.ACK = "ACK";
TokenNames.prototype.BYE = "BYE";
TokenNames.prototype.SUBSCRIBE = "SUBSCRIBE";
TokenNames.prototype.NOTIFY = "NOTIFY";
TokenNames.prototype.OPTIONS = "OPTIONS";
TokenNames.prototype.REGISTER = "REGISTER";
TokenNames.prototype.MESSAGE = "MESSAGE";
TokenNames.prototype.PUBLISH = "PUBLISH";

TokenNames.prototype.SIP = "sip";
TokenNames.prototype.SIPS = "sips";
TokenNames.prototype.TEL = "tel";
TokenNames.prototype.GMT = "GMT";
TokenNames.prototype.MON = "Mon";
TokenNames.prototype.TUE = "Tue";
TokenNames.prototype.WED = "Wed";
TokenNames.prototype.THU = "Thu";
TokenNames.prototype.FRI = "Fri";
TokenNames.prototype.SAT = "Sat";
TokenNames.prototype.SUN = "Sun";
TokenNames.prototype.JAN = "Jan";
TokenNames.prototype.FEB = "Feb";
TokenNames.prototype.MAR = "Mar";
TokenNames.prototype.APR = "Apr";
TokenNames.prototype.MAY = "May";
TokenNames.prototype.JUN = "Jun";
TokenNames.prototype.JUL = "Jul";
TokenNames.prototype.AUG = "Aug";
TokenNames.prototype.SEP = "Sep";
TokenNames.prototype.OCT = "Oct";
TokenNames.prototype.NOV = "Nov";
TokenNames.prototype.DEC = "Dec";
TokenNames.prototype.K = "K";
TokenNames.prototype.C = "C";
TokenNames.prototype.E = "E";
TokenNames.prototype.F = "F";
TokenNames.prototype.I = "I";
TokenNames.prototype.M = "M";
TokenNames.prototype.L = "L";
TokenNames.prototype.S = "S";
TokenNames.prototype.T = "T";
TokenNames.prototype.U = "U";// JvB: added
TokenNames.prototype.V = "V";
TokenNames.prototype.R = "R";
TokenNames.prototype.O = "O";
TokenNames.prototype.X = "X"; //Jozef Saniga added
TokenNames.prototype.B = "B";

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
 *  Implementation of the JAIN-SIP StringMsgParser .
 *  @see  gov/nist/javax/sip/parser/StringMsgParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function StringMsgParser(exhandler) {
    this.classname="StringMsgParser"; 
    this.readBody=true;
    this.parseExceptionListener=null;
    this.rawStringMessage=null;
    this.strict=null;
    this.computeContentLengthFromMessage = false;
    this.viaCount=0;
    if(exhandler!=null)
    {
        this.parseExceptionListener = exhandler;
    }
}

StringMsgParser.prototype.SIP_VERSION_STRING="SIP/2.0";

StringMsgParser.prototype.setParseExceptionListener =function(pexhandler){
    this.parseExceptionListener = pexhandler;
}

StringMsgParser.prototype.parseSIPMessage =function(){
    if(typeof arguments[0]=="string")
    {
        var msgString=arguments[0];
        return this.parseSIPMessagestring(msgString);
    }
    else if(typeof arguments[0]=="object")
    {
        var msgBuffer=arguments[0];
        return this.parseSIPMessagebyte(msgBuffer);
    }
}

StringMsgParser.prototype.parseSIPMessagestring =function(msgString){
    if (msgString == null || msgString.length == 0) {
        return null;
    }
    this.rawStringMessage = msgString;
    var i = 0;
    try {
        while (msgString.charCodeAt(i) < 0x20) {
            i++;
        }
    } catch (ex) {
        console.error("StringMsgParser:parseSIPMessagestring(): catched exception:"+ex);
        return null;
    }
    var currentLine = null;
    var currentHeader = null;
    var isFirstLine = true;
    var message = null;
    do {
        var lineStart = i;
        try {
            var c = msgString.charAt(i);
            while (c != '\r' && c != '\n') {
                c = msgString.charAt(++i);
            }
        } catch (ex) {
            console.error("StringMsgParser:parseSIPMessagestring(): catched exception:"+ex);
            break;
        } 
        currentLine = msgString.substring(lineStart, i);
        currentLine = this.trimEndOfLine(currentLine);
        if (currentLine.length == 0) {
            if (currentHeader != null) {
                this.processHeader(currentHeader, message);
            }
        } 
        else {
            if (isFirstLine) {
                message = this.processFirstLine(currentLine);
            } 
            else {
                var firstChar = currentLine.charAt(0);
                if (firstChar == '\t' || firstChar == ' ') {
                    if (currentHeader == null) {
                        console.error("StringMsgParser:parseSIPMessagestring(): bad header continuation.");
                        throw "StringMsgParser:parseSIPMessagestring(): bad header continuation.";
                    }
                    currentHeader = currentHeader+currentLine.substring(1);
                } 
                else {
                    if (currentHeader != null) {
                        this.processHeader(currentHeader, message);
                    }
                    currentHeader = currentLine;
                }
            }
        }
        if (msgString.charAt(i) == '\r' && msgString.length > i + 1 && msgString.charAt(i + 1) == '\n') {
            i++;
        }
        i++;
        isFirstLine = false;
    } while (currentLine.length > 0);
    message.setSize(i);
    if (this.readBody && message.getContentLength() != null) {
        if (message.getContentLength().getContentLength() != 0) {
            var body = msgString.substring(i);
            message.setMessageContent(body, this.strict, this.computeContentLengthFromMessage, message.getContentLength().getContentLength());
        } 
        else if (!this.computeContentLengthFromMessage && message.getContentLength().getContentLength() == 0) {
            if (this.strict) {
                console.error("StringMsgParser:parse(): extraneous characters at the end of the message",i);
                throw "StringMsgParser:parse(): extraneous characters at the end of the message";
            }
        }
    }
    return message;
}
StringMsgParser.prototype.parseSIPMessagebyte =function(msgBuffer){
    if (msgBuffer == null || msgBuffer.length == 0) {
        return null;
    }
    var i = 0;
    try {
        while (msgBuffer[i].charCodeAt(0) < 0x20) {
            i++;
        }
    } catch (ex) {
        console.error("StringMsgParser:parseSIPMessagebyte(): catched exception:"+ex);
        return null;
    }
    var currentLine = "";
    var currentHeader = null;
    var isFirstLine = true;
    var message = null;
    do {
        var lineStart = i;
        try {
            while (msgBuffer[i] != '\r' && msgBuffer[i] != '\n') {
                i++;
            }
        } catch (ex) {
            console.error("StringMsgParser:parseSIPMessagebyte(): catched exception:"+ex);
            break;
        }
        var lineLength = i - lineStart;
        try {
            for(var x=0;x<lineLength;x++)
            {
                currentLine=currentLine+msgBuffer[x+lineStart];
            }
        } catch (ex) {
            console.error("StringMsgParser:parseSIPMessagebyte(): bad message encoding!");
            throw "StringMsgParser:parseSIPMessagebyte():bad message encoding!";
        }
        currentLine = this.trimEndOfLine(currentLine);
        if (currentLine.length() == 0) {
            if (currentHeader != null && message != null) {
                this.processHeader(currentHeader, message);
            }
        } else {
            if (isFirstLine) {
                message = this.processFirstLine(currentLine);
            } else {
                var firstChar = currentLine.charAt(0);
                if (firstChar == '\t' || firstChar == ' ') {
                    if (currentHeader == null) {
                        console.error("StringMsgParser:parseSIPMessagebyte(): bad header continuation");
                        throw "StringMsgParser:parseSIPMessagebyte(): bad header continuation";
                    }
                    currentHeader = currentHeader+currentLine.substring(1);
                } else {
                    if (currentHeader != null && message != null) {
                        this.processHeader(currentHeader, message);
                    }
                    currentHeader = currentLine;
                }
            }
        }
        if (msgBuffer[i] == '\r' && msgBuffer.length > i + 1 && msgBuffer[i + 1] == '\n') {
            i++;
        }
        i++;
        isFirstLine = false;
    } while (currentLine.length > 0); 
    if (message == null) {
        console.error("StringMsgParser:parseSIPMessagebyte(): bad message");
        throw "StringMsgParser:parseSIPMessagebyte(): bad message";
    }
    message.setSize(i);
    if (this.readBody && message.getContentLength() != null
        && message.getContentLength().getContentLength() != 0) {
        var bodyLength = msgBuffer.length - i;
        var body = new Array();
        var l=i;
        for(x=0;x<bodyLength;x++)
        {
            body[x]=msgBuffer[l];
            l=l+1;
        }
        message.setMessageContent(body, this.computeContentLengthFromMessage, message.getContentLength().getContentLength());
    }
    return message;
}

StringMsgParser.prototype.trimEndOfLine =function(line){
    if (line == null) {
        return line;
    }
    var i = line.length - 1;
    while (i >= 0 && line.charCodeAt(i) <= 0x20) {
        i--;
    }
    if (i == line.length - 1) {
        return line;
    }
    if (i == -1) {
        return "";
    }
    return line.substring(0, i + 1);
}


StringMsgParser.prototype.processFirstLine =function(firstLine){
    var message=null;
    var constlength=this.SIP_VERSION_STRING.length;
    var n=0;
    for(var i=0;i<constlength;i++)
    {
        if(firstLine.charAt(i)==this.SIP_VERSION_STRING.charAt(i))
        {
            n=n+1;
        }
    }
    if (n!=constlength) {
        message = new SIPRequest();
        try {
            var requestLine = new RequestLineParser(firstLine + "\n").parse();
            message.setRequestLine(requestLine);
        } catch (ex) {
            console.error("StringMsgParser:processFirstLine(): catched exception:"+ex);
            if (this.parseExceptionListener != null) {
                var rl=new RequestLine();
                this.parseExceptionListener.handleException(ex, message,
                    rl.classname, firstLine, this.rawStringMessage);
            } else {
                throw ex;
            }
        }
    } 
    else {
        message = new SIPResponse();
        try {
            var sl = new StatusLineParser(firstLine + "\n").parse();
            message.setStatusLine(sl);
        } catch (ex) {
            console.error("StringMsgParser:processFirstLine(): catched exception:"+ex);
            if (this.parseExceptionListener != null) {
                sl=new StatusLine();
                this.parseExceptionListener.handleException(ex, message,
                    sl.classname, firstLine, this.rawStringMessage);
            } else {
                throw ex;
            }
        }
    }
    return message;
}

StringMsgParser.prototype.processHeader =function(header, message){
    if (header == null || header.length == 0) {
        return;
    }
    var headerParser = null;
    try {
        var parserfactory=new ParserFactory();
        headerParser = parserfactory.createParser(header + "\n");
    } catch (ex) {
        console.error("StringMsgParser:processHeader(): catched exception:"+ex);
        this.parseExceptionListener.handleException(ex, message, null,
            header, this.rawStringMessage);
        return;
    }
    try {
        var sipHeader = headerParser.parse();
        if(sipHeader instanceof ViaList)
        {
            this.viaCount=this.viaCount+1;
            if(this.viaCount==1)
            {
                message.attachHeader(sipHeader, false);
            }
            else
            {
                message.addViaHeaderList(sipHeader);    
            }
        }
        else
        {
            message.attachHeader(sipHeader, false);
        }
        
    } catch (ex) {
        console.error("StringMsgParser:processHeader(): catched exception:"+ex);
        if (this.parseExceptionListener != null) {
            var lexer=new Lexer();
            var headerName = lexer.getHeaderName(header);
            var namemap=new NameMap();
            var headerClass = namemap.getClassFromName(headerName);
            if (headerClass == null) {
                headerClass = new ExtensionHeaderImpl().classname;
            }
            this.parseExceptionListener.handleException(ex, message,
                headerClass, header, this.rawStringMessage);
        }
    }
}

StringMsgParser.prototype.parseAddress =function(address){
    var addressParser = new AddressParser(address);
    return addressParser.address(true);
}

StringMsgParser.prototype.parseHost =function(host){
    var lexer = new Lexer("charLexer", host);
    return new HostNameParser(lexer).host();
}

StringMsgParser.prototype.parseTelephoneNumber =function(telephone_number){
    return new URLParser(telephone_number).parseTelephoneNumber(true);
}

StringMsgParser.prototype.parseSIPUrl =function(url){
    try {
        return new URLParser(url).sipURL(true);
    } catch (ex) {
        console.error("StringMsgParser:parseSIPUrl(): "+ url + " is not a SIP URL ");
        throw "StringMsgParser:parseSIPUrl(): "+ url + " is not a SIP URL ";
    }
}

StringMsgParser.prototype.parseUrl =function(url){
    return new URLParser(url).parse();
}

StringMsgParser.prototype.parseSIPHeader =function(header){
    var start = 0;
    var end = header.length - 1;
    try {
        while (header.charCodeAt(start) <= 0x20) {
            start++;
        }
        while (header.charCodeAt(end) <= 0x20) {
            end--;
        }
    } catch (ex) {
        console.error("StringMsgParser:parseSIPHeader(): empty header");
        throw "StringMsgParser:parseSIPHeader(): eEmpty header";
    }
    var buffer = "";
    var i = start;
    var lineStart = start;
    var endOfLine = false;
    while (i <= end) {
        var c = header.charAt(i);
        if (c == '\r' || c == '\n') {
            if (!endOfLine) {
                buffer=buffer+header.substring(lineStart, i);
                endOfLine = true;
            }
        } else {
            if (endOfLine) {
                endOfLine = false;
                if (c == ' ' || c == '\t') {
                    buffer=buffer+' ';
                    lineStart = i + 1;
                } else {
                    lineStart = i;
                }
            }
        }
        i++;
    }
    buffer=buffer+header.substring(lineStart, i);
    buffer=buffer+'\n';
    var parserfactory=new ParserFactory();
    var hp = parserfactory.createParser(buffer.toString());
    if (hp == null) {
        console.error("StringMsgParser:parseSIPHeader(): could not create parser");
        throw "StringMsgParser:parseSIPHeader(): could not create parser";
    }
    return hp.parse();
}

StringMsgParser.prototype.parseSIPRequestLine =function(requestLine){
    requestLine += "\n";
    return new RequestLineParser(requestLine).parse();
}

StringMsgParser.prototype.parseSIPStatusLine =function(statusLine){
    statusLine += "\n";
    return new StatusLineParser(statusLine).parse();
}

StringMsgParser.prototype.setComputeContentLengthFromMessage =function(computeContentLengthFromMessage){
    this.computeContentLengthFromMessage = computeContentLengthFromMessage;
}

StringMsgParser.prototype.setStrict =function(strict){
    this.strict = strict;
}/*
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
 *  Implementation of the JAIN-SIP AddressParametersParser .
 *  @see  gov/nist/javax/sip/parser/AddressParametersParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function AddressParametersParser() {
    this.classname="AddressParametersParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var buffer=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", buffer);
    }
}

AddressParametersParser.prototype = new ParametersParser();
AddressParametersParser.prototype.constructor=AddressParametersParser;

AddressParametersParser.prototype.parse =function(addressParametersHeader){
    try {
        var addressParser = new AddressParser(this.getLexer());
        var addr = addressParser.address(false);
        addressParametersHeader.setAddress(addr);
        this.lexer.SPorHT();
        var la = this.lexer.lookAhead(0);
        if (this.lexer.hasMoreChars() && la != '\0' && la != '\n' && this.lexer.startsId()) {
            ParametersParser.prototype.parseNameValueList.call(this,addressParametersHeader);
        } else {
            ParametersParser.prototype.parse.call(this,addressParametersHeader);
        }
    } catch (ex) {
        console.error("AddressParametersParser:parse(): address Error");
        throw ex;
    }
}/*
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
 *  Implementation of the JAIN-SIP ChallengeParser .
 *  @see  gov/nist/javax/sip/parser/ChallengeParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function ChallengeParser() {
    this.classname="ChallengeParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var challenge=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", challenge);
    }
}

ChallengeParser.prototype = new HeaderParser();
ChallengeParser.prototype.constructor=ChallengeParser;

ChallengeParser.prototype.parse =function(header){
    this.lexer.SPorHT();
    this.lexer.match(TokenTypes.prototype.ID);
    var type = this.lexer.getNextToken();
    this.lexer.SPorHT();
    header.setScheme(type.getTokenValue());
    while (this.lexer.lookAhead(0) != "\n") {
        this.parseParameter(header);
        this.lexer.SPorHT();
        var la = this.lexer.lookAhead(0);
        if (la == "\n" || la == "'\0'")
        {
            break;
        }
        this.lexer.match(',');
        this.lexer.SPorHT();
    }
}
ChallengeParser.prototype.parseParameter =function(header){
    var nv = this.nameValue('=');
    header.setParameter_nv(nv);
}
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
 *  Implementation of the JAIN-SIP URLParser .
 *  @see  gov/nist/javax/sip/parser/URLParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function URLParser() {
    this.classname="URLParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("sip_urlLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var url=arguments[0];
        this.lexer = new Lexer("sip_urlLexer", url);
    }
}

URLParser.prototype = new Parser();
URLParser.prototype.constructor=URLParser;
URLParser.prototype.PLUS=TokenTypes.prototype.PLUS;
URLParser.prototype.SEMICOLON=TokenTypes.prototype.SEMICOLON;

URLParser.prototype.isMark =function(next){
    switch (next) {
        case '-':
        case '_':
        case '.':
        case '!':
        case '~':
        case '*':
        case '\'':
        case '(':
        case ')':
            return true;
        default:
            return false;
    }
}

URLParser.prototype.isUnreserved =function(next){
    var lexer=new Lexer("","");
    if(lexer.isAlphaDigit(next) || this.isMark(next))
    {
        return true;
    }
    else
    {
        return false;
    }
}

URLParser.prototype.isReservedNoSlash =function(next){
    switch (next) {
        case ';':
        case '?':
        case ':':
        case '@':
        case '&':
        case '+':
        case '$':
        case ',':
            return true;
        default:
            return false;
    }
}

URLParser.prototype.isUserUnreserved =function(la){
    switch (la) {
        case '&':
        case '?':
        case '+':
        case '$':
        case '#':
        case '/':
        case ',':
        case ';':
        case '=':
            return true;
        default:
            return false;
    }
}

URLParser.prototype.unreserved =function(){
    var next = this.lexer.lookAhead(0);
    if (this.isUnreserved(next)) {
        this.lexer.consume(1);
        return next;
    } else {
        console.error("URLParser:unreserved(): unreserved");
        throw "URLParser:unreserved(): unreserved";
    }
}

URLParser.prototype.paramNameOrValue =function(){
    var startIdx = this.lexer.getPtr();
    while (this.lexer.hasMoreChars()) {
        var next = this.lexer.lookAhead(0);
        var isValidChar = false;
        switch (next) {
            case '[':
            case ']':// JvB: fixed this one
            case '/':
            case ':':
            case '&':
            case '+':
            case '$':
                isValidChar = true;
        }
        if (isValidChar || this.isUnreserved(next)) {
            this.lexer.consume(1);
        } else if (this.isEscaped()) {
            this.lexer.consume(3);
        } else {
            break;
        }
    }
    return this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
}

URLParser.prototype.uriParam =function(){
    var pvalue = "";
    var pname = this.paramNameOrValue();
    var next = this.lexer.lookAhead(0);
    var isFlagParam = true;
    if (next == '=') {
        this.lexer.consume(1);
        pvalue = this.paramNameOrValue();
        isFlagParam = false;
    }
    if (pname.length == 0&& (pvalue == null|| pvalue.length == 0)) {
        return null;
    } else {
        return new NameValue(pname, pvalue, isFlagParam);
    }
   
}

URLParser.prototype.isReserved =function(next){
    switch (next) {
        case ';':
        case '/':
        case '?':
        case ':':
        case '=': // Bug fix by Bruno Konik
        case '@':
        case '&':
        case '+':
        case '$':
        case ',':
            return true;
        default:
            return false;
    }
}

URLParser.prototype.reserved =function(){
    var next = this.lexer.lookAhead(0);
    if (this.isReserved(next)) {
        this.lexer.consume(1);
        var encode="";
        encode=(encode+next).toString();
        return encode;
    } else {
        console.error("URLParser:reserved(): reserved");
        throw "URLParser:reserved(): reserved";
    }
}

URLParser.prototype.isEscaped =function(){
    try {
        var lexer=new Lexer("","");
        if(this.lexer.lookAhead(0) == '%'
            && lexer.isHexDigit(this.lexer.lookAhead(1))
            && lexer.isHexDigit(this.lexer.lookAhead(2)))
            {
            return true;
        }
        else
        {
            return false;
        }
    } catch (ex) {
        console.error("URLParser:isEscaped(): catched exception:"+ex);
        return false;
    }
}

URLParser.prototype.escaped =function(){
    var lexer=new Lexer("","");
    var retval = "";
    var next = this.lexer.lookAhead(0);
    var next1 = this.lexer.lookAhead(1);
    var next2 = this.lexer.lookAhead(2);
    if (next == '%'
        && lexer.isHexDigit(next1)
        && lexer.isHexDigit(next2)) {
        this.lexer.consume(3);
        retval=retval+next+next1+next2;
    } else {
        console.error("URLParser:escaped(): escaped");
        throw "URLParser:escaped(): escaped";
    }
    return retval.toString();  
}

URLParser.prototype.mark =function(){
    var next = this.lexer.lookAhead(0);
    if (this.isMark(next)) {
        this.lexer.consume(1);
        return next;
    } else {
        console.error("URLParser:mark(): marked");
        throw "URLParser:mark(): marked";
    } 
}

URLParser.prototype.uric =function(){
    try {
        var lexer=new Lexer("","");
        var la = this.lexer.lookAhead(0);
        if (this.isUnreserved(la)) {
            this.lexer.consume(1);
            return lexer.charAsString(la);
        } else if (this.isReserved(la)) {
            this.lexer.consume(1);
            return lexer.charAsString(la);
        } else if (this.isEscaped()) {
            var retval = this.lexer.charAsString(3);
            this.lexer.consume(3);
            return retval;
        } else {
            return null;
        }
    } catch (ex) {
        console.error("URLParser:uric(): catched exception:"+ex);
        return null;
    }
}

URLParser.prototype.uricNoSlash =function(){
    try {
        var lexer=new Lexer("","");
        var la = this.lexer.lookAhead(0);
        if (this.isEscaped()) {
            var retval = this.lexer.charAsString(3);
            this.lexer.consume(3);
            return retval;
        } else if (this.isUnreserved(la)) {
            this.lexer.consume(1);
            return lexer.charAsString(la);
        } else if (this.isReservedNoSlash(la)) {
            this.lexer.consume(1);
            return lexer.charAsString(la);
        } else {
            return null;
        }
    } catch (ex) {
        console.error("URLParser:uricNoSlash(): catched exception:"+ex);
        return null;
    }
}

URLParser.prototype.uricString =function(){
    var retval = "";
    while (true) {
        var next = this.uric();
        if (next == null) {
            var la = this.lexer.lookAhead(0);
            if (la == '[') {
                var hnp = new HostNameParser(this.getLexer());
                var hp = hnp.hostPort(false);
                retval=retval+hp.toString();
                continue;
            }
            break;
        }
        retval=retval+next;
    }
    return retval.toString();
}

URLParser.prototype.uriReference =function(inBrackets){
    var retval = null;
    var tokens = this.lexer.peekNextToken(2);
    var t1 = tokens[0];
    var t2 = tokens[1];
    if (t1.getTokenType() == TokenTypes.prototype.SIP || t1.getTokenType() == TokenTypes.prototype.SIPS) {
        if (t2.getTokenType() == ':') {
            retval = this.sipURL(inBrackets);
        } else {
            console.error("URLParser:uriReference(): expecting \':\'");
            throw "URLParser:uriReference(): expecting \':\'";
        }
    } else if (t1.getTokenType() == TokenTypes.prototype.TEL) {
        if (t2.getTokenType() == ':') {
            retval = this.telURL(inBrackets);
        } else {
            console.error("URLParser:uriReference(): expecting \':\'");
            throw "URLParser:uriReference(): expecting \':\'";
        }
    } else {
        var urlString = this.uricString();
        try {
            retval = new GenericURI(urlString);
        } catch (ex) {
            console.error("URLParser:uriReference(): "+ex);
            throw "URLParser:uriReference(): "+ ex;
        }
    }
    return retval;
}

URLParser.prototype.base_phone_number =function(){
    var s = "";
    var lexer=new Lexer("","");
    var lc = 0;
    while (this.lexer.hasMoreChars()) {
        var w = this.lexer.lookAhead(0);
        if (lexer.isDigit(w)|| w == '-'|| w == '.'|| w == '('|| w == ')') {
            this.lexer.consume(1);
            s=s+w;
            lc++;
        } else if (lc > 0) {
            break;
        } else {
            console.error("URLParser:base_phone_number(): unexpected"+w);
            throw "URLParser:base_phone_number(): unexpected"+w;
        }
    }
    return s.toString();
}

URLParser.prototype.local_number =function(){
    var s = "";
    var lexer=new Lexer("","");
    var lc = 0;
    while (this.lexer.hasMoreChars()) {
        var la = this.lexer.lookAhead(0);
        if (la == '*'|| la == '#'|| la == '-'|| la == '.'|| la == '('| la == ')'
            || lexer.isHexDigit(la)) {
            this.lexer.consume(1);
            s=s+la
            lc++;
        } else if (lc > 0) {
            break;
        } else {
            console.error("URLParser:local_number(): unexpected"+la);
            throw "URLParser:local_number(): unexpected"+la;
        }
    }
    return s.toString();
}

URLParser.prototype.parseTelephoneNumber =function(inBrackets){
    var tn;
    this.lexer.selectLexer("charLexer");
    var lexer=new Lexer("","");
    var c = this.lexer.lookAhead(0);
    if (c == '+') {
        tn = this.global_phone_number(inBrackets);
    } else if (lexer.isHexDigit(c)|| c == '#'|| c == '*'|| c == '-'|| c == '.'
        || c == '('
        || c == ')') {
        tn = this.local_phone_number(inBrackets);
    } else {
        console.error("URLParser:parseTelephoneNumber(): unexpected char " + c);
        throw "URLParser:parseTelephoneNumber(): unexpected char " + c;
    }
    return tn;
}


URLParser.prototype.global_phone_number =function(inBrackets){
    var tn = new TelephoneNumber();
    tn.setGlobal(true);
    var nv = null;
    this.lexer.match('+');
    var b = this.base_phone_number();
    tn.setPhoneNumber(b);
    if (this.lexer.hasMoreChars()) {
        var tok = this.lexer.lookAhead(0);
        if (tok == ';' && inBrackets) {
            this.lexer.consume(1);
            nv = tel_parameters();
            tn.setParameters(nv);
        }
    }
    return tn;
}

URLParser.prototype.local_phone_number =function(inBrackets){
    var tn = new TelephoneNumber();
    tn.setGlobal(false);
    var nv = null;
    var b = null;
    b = this.local_number();
    tn.setPhoneNumber(b);
    if (this.lexer.hasMoreChars()) {
        var tok = this.lexer.peekNextToken();
        switch (tok.getTokenType()) {
            case this.SEMICOLON: {
                if (inBrackets) {
                    this.lexer.consume(1);
                    nv = this.tel_parameters();
                    tn.setParameters(nv);
                }
                break;
            }
            default: {
                break;
            }
        }
    }
    return tn;
}

URLParser.prototype.tel_parameters =function(){
    var nvList = new NameValueList();
    var nv;
    while (true) {
        var pname = this.paramNameOrValue();
        if (pname.toLowerCase()==("phone-context").toLowerCase()) {
            nv = this.phone_context();
        } else {
            if (this.lexer.lookAhead(0) == '=') {
                this.lexer.consume(1);
                var value = this.paramNameOrValue();
                nv = new NameValue(pname, value, false);
            } else {
                nv = new NameValue(pname, "", true);
            }
        }
        nvList.set(nv);
        if (this.lexer.lookAhead(0) == ';') {
            this.lexer.consume(1);
        } else {
            return nvList;
        }
    }
}

URLParser.prototype.phone_context =function(){
    this.lexer.match('=');
    var la = this.lexer.lookAhead(0);
    var value=null;
    if (la == '+') {// global-number-digits
        this.lexer.consume(1);// skip '+'
        value = "+" + this.base_phone_number();
    } else if (Lexer.isAlphaDigit(la)) {
        var t = this.lexer.match(Lexer.prototype.ID);// more broad than allowed
        value = t.getTokenValue();
    } else {
        console.error("URLParser:phone_context(): invalid phone-context:" + la);
        throw "URLParser:phone_context(): invalid phone-context:" + la;
    }
    return new NameValue("phone-context", value, false);
}


URLParser.prototype.telURL =function(inBrackets){
    this.lexer.match(TokenTypes.prototype.TEL);
    this.lexer.match(':');
    var tn = this.parseTelephoneNumber(inBrackets);
    var telUrl = new TelURLImpl();
    telUrl.setTelephoneNumber(tn);
    return telUrl;
}

URLParser.prototype.sipURL =function(inBrackets){
    var retval = new SipUri();
    // pmusgrave - handle sips case
    var nextToken = this.lexer.peekNextToken();
    var sipOrSips = TokenTypes.prototype.SIP;
    var scheme = TokenNames.prototype.SIP;
    if (nextToken.getTokenType() == TokenTypes.prototype.SIPS) {
        sipOrSips = TokenTypes.prototype.SIPS;
        scheme = TokenNames.prototype.SIPS;
    }
    this.lexer.match(sipOrSips);
    this.lexer.match(':');
    retval.setScheme(scheme);
    var startOfUser = this.lexer.markInputPosition();
    var userOrHost = this.user();// Note: user may contain ';', host may not...
    var passOrPort = null;
    // name:password or host:port
    if (this.lexer.lookAhead() == ':') {
        this.lexer.consume(1);
        passOrPort = this.password();
    }
    // name@hostPort
    if (this.lexer.lookAhead() == '@') {
        this.lexer.consume(1);
        retval.setUser(userOrHost);
        if (passOrPort != null) {
            retval.setUserPassword(passOrPort);
        }
    } else {
        // then userOrHost was a host, backtrack just in case a ';' was eaten...
        this.lexer.rewindInputPosition(startOfUser);
    }
    var hnp = new HostNameParser(this.getLexer());
    var hp = hnp.hostPort(false);
    retval.setHostPort(hp);
    this.lexer.selectLexer("charLexer");
    while (this.lexer.hasMoreChars()) {
        // If the URI is not enclosed in brackets, parameters belong to header
        if (this.lexer.lookAhead(0) != ';' || !inBrackets) {
            break;
        }
        this.lexer.consume(1);
        var parms = this.uriParam();
        if (parms != null) {
            retval.setUriParameter(parms);
        }
    }
    if (this.lexer.hasMoreChars() && this.lexer.lookAhead(0) == '?') {
        this.lexer.consume(1);
        while (this.lexer.hasMoreChars()) {
            parms = this.qheader();
            retval.setQHeader(parms);
            if (this.lexer.hasMoreChars() && this.lexer.lookAhead(0) != '&') {
                break;
            } else {
                this.lexer.consume(1);
            }
        }
    }
        
    return retval;
}

URLParser.prototype.peekScheme =function(){
    var tokens = this.lexer.peekNextToken(1);
    if (tokens.length == 0) {
        return null;
    }
    var scheme = tokens[0].getTokenValue();
    return scheme;
}

URLParser.prototype.qheader =function(){
    var startIdx = this.lexer.ptr;
    while (true) {
        var la = this.lexer.lookAhead(0);
        if (la == '=') {
            break;
        } else if (la == '\0') {
            console.error("URLParser:qheader(): EOL reached");
            throw "URLParser:qheader(): EOL reached";
        }
        this.lexer.consume(1);
    }
    var name=this.lexer.getBuffer().substring(startIdx, this.lexer.ptr);
    this.lexer.consume(1);
    var value = this.hvalue();
    return new NameValue(name, value, false);
}

URLParser.prototype.hvalue =function(){
    var retval = "";
    while (this.lexer.hasMoreChars()) {
        var la = this.lexer.lookAhead(0);
        // Look for a character that can terminate a URL.
        var isValidChar = false;
        switch (la) {
            case '+':
            case '?':
            case ':':
            case '[':
            case ']':
            case '/':
            case '$':
            case '_':
            case '-':
            case '"':
            case '!':
            case '~':
            case '*':
            case '.':
            case '(':
            case ')':
                isValidChar = true;
        }
        var lexer=new Lexer("","");
        if (isValidChar || lexer.isAlphaDigit(la)) {
            this.lexer.consume(1);
            retval=retval+la;
        } else if (la == '%') {
            retval=retval+this.escaped();
        } else {
            break;
        }
    }
    return retval.toString();
}

URLParser.prototype.urlString =function(){
    var retval = "";
    this.lexer.selectLexer("charLexer");
    while (this.lexer.hasMoreChars()) {
        var la = this.lexer.lookAhead(0);
        if (la == ' '|| la == '\t'|| la == '\n'|| la == '>'|| la == '<') {
            break;
        }
        this.lexer.consume(0);
        retval=retval+la;
    }
    return retval.toString();
}

URLParser.prototype.user =function(){
    var startIdx = this.lexer.getPtr();
    while (this.lexer.hasMoreChars()) {
        var la = this.lexer.lookAhead(0);
        if (this.isUnreserved(la) || this.isUserUnreserved(la)) {
            this.lexer.consume(1);
        } else if (this.isEscaped()) {
            this.lexer.consume(3);
        } else {
            break;
        }
    }
    return this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
}


URLParser.prototype.password =function(){
    var startIdx = this.lexer.getPtr();
    while (true) {
        var la = this.lexer.lookAhead(0);
        var isValidChar = false;
        switch (la) {
            case '&':
            case '=':
            case '+':
            case '$':
            case ',':
                isValidChar = true;
        }
        if (isValidChar || this.isUnreserved(la)) {
            this.lexer.consume(1);
        } else if (this.isEscaped()) {
            this.lexer.consume(3); 
        } else {
            break;
        }

    }
    return this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
}

URLParser.prototype.parse =function(){
    return this.uriReference(true);
}/*
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
 *  Implementation of the JAIN-SIP AddressParser .
 *  @see  gov/nist/javax/sip/parser/AddressParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function AddressParser() {
    this.classname="AddressParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var address=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", address);
    }
}

AddressParser.prototype = new Parser();
AddressParser.prototype.constructor=AddressParser;
AddressParser.prototype.NAME_ADDR=1;
AddressParser.prototype.ADDRESS_SPEC=2;

AddressParser.prototype.nameAddr =function(){
    if (this.lexer.lookAhead(0) == '<') {
        this.lexer.consume(1);
        this.lexer.selectLexer("sip_urlLexer");
        this.lexer.SPorHT();
        var uriParser = new URLParser(this.lexer);
        var uri = uriParser.uriReference( true );
        var retval = new AddressImpl();
        retval.setAddressType(this.NAME_ADDR);
        retval.setURI(uri);
        this.lexer.SPorHT();
        this.lexer.match('>');
        return retval;
    } else {
        var addr = new AddressImpl();
        addr.setAddressType(this.NAME_ADDR);
        var name = null;
        if (this.lexer.lookAhead(0) == '\"') {
            name = this.lexer.quotedString();
            this.lexer.SPorHT();
        } 
        else
        {
            name = this.lexer.getNextToken('<');
        }
        addr.setDisplayName(name.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, ''));
        
        this.lexer.match('<');
        
        this.lexer.SPorHT();
        uriParser = new URLParser(this.lexer);
        uri = uriParser.uriReference( true );
        retval = new AddressImpl();
        addr.setAddressType(this.NAME_ADDR);
        addr.setURI(uri);
        this.lexer.SPorHT();
        this.lexer.match('>');
        return addr;
    }  
}

AddressParser.prototype.address =function(inclParams){
    var retval = null;
    var k = 0;
    while (this.lexer.hasMoreChars()) {
        var la = this.lexer.lookAhead(k);
        if (la == '<'|| la == '\"'|| la == ':'|| la == '/')
        {
            break;
        }
        else if (la == '\0')
        {
           console.error("AddressParser:address(): unexpected EOL");
           throw "AddressParser:parse(): unexpected EOL";
        }
        else
        {
            k++;
        }
    }
    la = this.lexer.lookAhead(k);
    if (la == '<' || la == '\"') {
        retval = this.nameAddr();
    } else if (la == ':' || la == '/') {
        retval = new AddressImpl();
        var uriParser = new URLParser(this.lexer);
        var uri = uriParser.uriReference( inclParams );
        retval.setAddressType(this.ADDRESS_SPEC);
        retval.setURI(uri);
    } else {
        console.error("AddressParser:address(): bad address spec");
        throw "AddressParser:parse(): bad address spec";
    }
    return retval;
}
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
 *  Implementation of the JAIN-SIP ToParser .
 *  @see  gov/nist/javax/sip/parser/ToParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function ToParser() {
    this.classname="ToParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var to=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", to);
    }
}

ToParser.prototype = new AddressParametersParser();
ToParser.prototype.constructor=ToParser;

ToParser.prototype.parse =function(){
    this.headerName(TokenTypes.prototype.TO);
    var to = new To();
    AddressParametersParser.prototype.parse.call(this,to);
    this.lexer.match('\n');        
    return to;
}

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
 *  Implementation of the JAIN-SIP FromParser .
 *  @see  gov/nist/javax/sip/parser/FromParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function FromParser() {
    this.classname="FromParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var from=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", from);
    }
}

FromParser.prototype = new AddressParametersParser();
FromParser.prototype.constructor=FromParser;

FromParser.prototype.parse =function(){
    var from = new From();
    this.lexer.match(TokenTypes.prototype.FROM);
    this.lexer.SPorHT();
    this.lexer.match(':');
    this.lexer.SPorHT();
    AddressParametersParser.prototype.parse.call(this,from);
    this.lexer.match('\n');
    return from;
}

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
 *  Implementation of the JAIN-SIP CSeqParser .
 *  @see  gov/nist/javax/sip/parser/CSeqParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function CSeqParser() {
    this.classname="CSeqParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var buffer=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", buffer);
        
    }
}

CSeqParser.prototype = new HeaderParser();
CSeqParser.prototype.constructor=CSeqParser;

CSeqParser.prototype.parse =function(){
    var c = new CSeq();
    this.lexer.match(TokenTypes.prototype.CSEQ);
    this.lexer.SPorHT();
    this.lexer.match(':');
    this.lexer.SPorHT();
    var number = this.lexer.number();
    c.setSeqNumber(number);
    this.lexer.SPorHT();
    var siprequest=new SIPRequest();
    var m = siprequest.getCannonicalName(this.method());
    c.setMethod(m);
    this.lexer.SPorHT();
    this.lexer.match('\n');
    return c;
}

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
 *  Implementation of the JAIN-SIP ViaParser .
 *  @see  gov/nist/javax/sip/parser/ViaParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function ViaParser() {
    this.classname="ViaParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var via=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", via);
    }
}

ViaParser.prototype = new HeaderParser();
ViaParser.prototype.constructor=ViaParser;
ViaParser.prototype.RECEIVED="received";
ViaParser.prototype.BRANCH="branch";

ViaParser.prototype.parseVia =function(v){
    this.lexer.match(TokenTypes.prototype.ID);
    var protocolName = this.lexer.getNextToken();
    this.lexer.SPorHT();
    this.lexer.match('/');
    this.lexer.SPorHT();
    this.lexer.match(TokenTypes.prototype.ID);
    this.lexer.SPorHT();
    var protocolVersion = this.lexer.getNextToken();
    this.lexer.SPorHT();
    this.lexer.match('/');
    this.lexer.SPorHT();
    this.lexer.match(TokenTypes.prototype.ID);
    this.lexer.SPorHT();
    var transport = this.lexer.getNextToken();
    this.lexer.SPorHT();
    var protocol = new Protocol();
    protocol.setProtocolName(protocolName.getTokenValue());
    protocol.setProtocolVersion(protocolVersion.getTokenValue());
    if(transport.getTokenValue()=="WS")
    {
        protocol.setTransport("WS");
    }
    else
    {
        protocol.setTransport(transport.getTokenValue());
    }
    v.setSentProtocol(protocol);
    var hnp = new HostNameParser(this.getLexer());
    var hostPort = hnp.hostPort( true );
    v.setSentBy(hostPort);
    this.lexer.SPorHT();
    while (this.lexer.lookAhead(0) == ';') {
        this.lexer.consume(1);
        this.lexer.SPorHT();
        var nameValue = this.nameValue();
        var name = nameValue.getName();
        if (name==this.BRANCH) {
            var branchId = nameValue.getValueAsObject();
            if (branchId == null)
            {
                console.error("ViaParser:parseVia(): null branch Id", this.lexer.getPtr());
                throw "ViaParser:parseVia(): null branch Id"+ this.lexer.getPtr();
            }
        }
        v.setParameter_nv(nameValue);
        this.lexer.SPorHT();
    }
    if (this.lexer.lookAhead(0) == '(') {
        this.lexer.selectLexer("charLexer");
        this.lexer.consume(1);
        var comment = "";
        while (true) {
            var ch = this.lexer.lookAhead(0);
            if (ch == ')') {
                this.lexer.consume(1);
                break;
            } else if (ch == '\\') {
                // Escaped character
                var tok = this.lexer.getNextToken();
                comment=comment+tok.getTokenValue();
                this.lexer.consume(1);
                tok = this.lexer.getNextToken();
                comment=comment+tok.getTokenValue();
                this.lexer.consume(1);
            } else if (ch == '\n') {
                break;
            } else {
                comment=comment+ch;
                this.lexer.consume(1);
            }
        }
        v.setComment(comment.toString());
    }
}

ViaParser.prototype.nameValue =function(){
    this.lexer.match(LexerCore.prototype.ID);
    var name = this.lexer.getNextToken();
    this.lexer.SPorHT();
    try {
        var quoted = false;
        var la = this.lexer.lookAhead(0);
        if (la == '=') {
            this.lexer.consume(1);
            this.lexer.SPorHT();
            var str = null;
            if (name.getTokenValue().toLowerCase()==this.RECEIVED.toLowerCase()) {
                str = this.lexer.byteStringNoSemicolon();
            } else {
                if (this.lexer.lookAhead(0) == '\"') {
                    str = this.lexer.quotedString();
                    quoted = true;
                } else {
                    this.lexer.match(LexerCore.prototype.ID);
                    var value = this.lexer.getNextToken();
                    str = value.getTokenValue();
                }
            }
            var nv = new NameValue(name.getTokenValue().toLowerCase(), str);
            if (quoted)
            {
                nv.setQuotedValue();
            }
            return nv;
        } else {
            return new NameValue(name.getTokenValue().toLowerCase(), null);
        }
    } catch (ex) {
        console.error("ViaParser:nameValue(): catched exception:"+ex);
        return new NameValue(name.getTokenValue(), null);
    }
}

ViaParser.prototype.parse =function(){
    var viaList = new ViaList();
    this.lexer.match(TokenTypes.prototype.VIA);
    this.lexer.SPorHT(); // ignore blanks
    this.lexer.match(':'); // expect a colon.
    this.lexer.SPorHT(); // ingore blanks.
    while (true) {
        var v = new Via();
        this.parseVia(v);
        viaList.add(v);
        this.lexer.SPorHT(); // eat whitespace.
        if (this.lexer.lookAhead(0) == ',') {
            this.lexer.consume(1); // Consume the comma
            this.lexer.SPorHT(); // Ignore space after.
        }
        if (this.lexer.lookAhead(0) == '\n')
        {
            break;
        }
    }
    this.lexer.match('\n');
    return viaList;
}

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
 *  Implementation of the JAIN-SIP ContactParser .
 *  @see  gov/nist/javax/sip/parser/ContactParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function ContactParser() {
    this.classname="ContactParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var contact=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", contact);
    }
}

ContactParser.prototype = new AddressParametersParser();
ContactParser.prototype.constructor=ContactParser;

ContactParser.prototype.parse =function(){
    this.headerName(TokenTypes.prototype.CONTACT);
    var retval = new ContactList();
    while (true) {
        var contact = new Contact();
        if (this.lexer.lookAhead(0) == '*') {
            var next = this.lexer.lookAhead(1);
            if (next == ' ' || next == '\t' || next == '\r' || next == '\n') {
                this.lexer.match('*');
                contact.setWildCardFlag(true);
            } else {
                AddressParametersParser.prototype.parse.call(this,contact);
            }
        } else {
            AddressParametersParser.prototype.parse.call(this,contact);
        }
        retval.add(contact);
        this.lexer.SPorHT();
        var la = this.lexer.lookAhead(0);
        if (la == ',') {
            this.lexer.match(',');
            this.lexer.SPorHT();
        } 
        else if (la == '\n' || la == '\0')
        {
            break;
        }
        else
        {
           console.error("ContactParser:address(): unexpected char");
           throw "ContactParser:parse(): unexpected char";
        }
    }
    return retval;
}

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
 *  Implementation of the JAIN-SIP ContentTypeParser .
 *  @see  gov/nist/javax/sip/parser/ContentTypeParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function ContentTypeParser() {
    this.classname="ContentTypeParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var contentType=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", contentType);
    }
}

ContentTypeParser.prototype = new ParametersParser();
ContentTypeParser.prototype.constructor=ContentTypeParser;

ContentTypeParser.prototype.parse =function(){
    var contentType = new ContentType();
    this.headerName(TokenTypes.prototype.CONTENT_TYPE);
    this.lexer.match(TokenTypes.prototype.ID);
    var type = this.lexer.getNextToken();
    this.lexer.SPorHT();
    contentType.setContentType(type.getTokenValue());
    this.lexer.match('/');
    this.lexer.match(TokenTypes.prototype.ID);
    var subType = this.lexer.getNextToken();
    this.lexer.SPorHT();
    contentType.setContentSubType(subType.getTokenValue());
    ParametersParser.prototype.parse.call(this,contentType);
    this.lexer.match('\n');
    return contentType;
}

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
 *  Implementation of the JAIN-SIP ContentLengthParser .
 *  @see  gov/nist/javax/sip/parser/ContentLengthParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function ContentLengthParser() {
    this.classname="ContentLengthParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var contentLength=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", contentLength);
    }
}

ContentLengthParser.prototype = new HeaderParser();
ContentLengthParser.prototype.constructor=ContentLengthParser;

ContentLengthParser.prototype.parse =function(){
    var contentLength = new ContentLength();
    this.headerName(TokenTypes.prototype.CONTENT_LENGTH);
    var number = this.lexer.number();
    contentLength.setContentLength(number);
    this.lexer.SPorHT();
    this.lexer.match('\n');
    return contentLength;
}

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
 *  Implementation of the JAIN-SIP AuthorizationParser .
 *  @see  gov/nist/javax/sip/parser/AuthorizationParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function AuthorizationParser() {
    this.classname="AuthorizationParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var authorization=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", authorization);
    }
}

AuthorizationParser.prototype = new ChallengeParser();
AuthorizationParser.prototype.constructor=AuthorizationParser;

AuthorizationParser.prototype.parse =function(){
    this.headerName(TokenTypes.prototype.AUTHORIZATION);
    var auth = new Authorization();
    ChallengeParser.prototype.parse.call(this,auth);//used to call the method of challengeparser
    return auth;
}

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
 *  Implementation of the JAIN-SIP WWWAuthenticateParser .
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function WWWAuthenticateParser() {
    this.classname="WWWAuthenticateParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var wwwAuthenticate=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", wwwAuthenticate);
    }
}

WWWAuthenticateParser.prototype = new ChallengeParser();
WWWAuthenticateParser.prototype.constructor=WWWAuthenticateParser;

WWWAuthenticateParser.prototype.parse =function(){
    this.headerName(TokenTypes.prototype.WWW_AUTHENTICATE);
    var wwwAuthenticate = new WWWAuthenticate();
    ChallengeParser.prototype.parse.call(this,wwwAuthenticate);
    return wwwAuthenticate;
}


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
 *  Implementation of the JAIN-SIP CallIDParser .
 *  @see  gov/nist/javax/sip/parser/CallIDParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function CallIDParser() {
    this.classname="CallIDParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var buffer=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", buffer);
    }
}

CallIDParser.prototype = new HeaderParser();
CallIDParser.prototype.constructor=CallIDParser;

CallIDParser.prototype.parse =function(){
    this.lexer.match(TokenTypes.prototype.CALL_ID);
    this.lexer.SPorHT();
    this.lexer.match(':');
    this.lexer.SPorHT();
    var callID = new CallID();
    this.lexer.SPorHT();
    var rest = this.lexer.getRest();
    callID.setCallId(rest.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, ''));
    return callID;
}

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
 *  Implementation of the JAIN-SIP RouteParser .
 *  @see  gov/nist/javax/sip/parser/RouteParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function RouteParser() {
    this.classname="RouteParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var route=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", route);
    }
}

RouteParser.prototype = new AddressParametersParser();
RouteParser.prototype.constructor=RouteParser;

RouteParser.prototype.parse =function(){
    var routeList = new RouteList();
    this.lexer.match(TokenTypes.prototype.ROUTE);
    this.lexer.SPorHT();
    this.lexer.match(':');
    this.lexer.SPorHT();
    while (true) {
        var route = new Route();
        AddressParametersParser.prototype.parse.call(this,route);
        routeList.add(route);
        this.lexer.SPorHT();
        var la = this.lexer.lookAhead(0);
        if (la == ',') {
            this.lexer.match(',');
            this.lexer.SPorHT();
        } 
        else if (la == '\n')
        {
            break;
        }
        else
        {
            console.error("RouteParser:parse(): unexpected char");
            throw "RouteParser:parse(): unexpected char";
        }
    }
    return routeList;
}

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
 *  Implementation of the JAIN-SIP RecordRouteParser .
 *  @see  gov/nist/javax/sip/parser/RecordRouteParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function RecordRouteParser() {
    this.classname="RecordRouteParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var recordRoute=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", recordRoute);
    }
}

RecordRouteParser.prototype = new AddressParametersParser();
RecordRouteParser.prototype.constructor=RecordRouteParser;

RecordRouteParser.prototype.parse =function(){
    var recordRouteList = new RecordRouteList();
    this.lexer.match(TokenTypes.prototype.RECORD_ROUTE);
    this.lexer.SPorHT();
    this.lexer.match(':');
    this.lexer.SPorHT();
    
    while (true) {
        var recordRoute = new RecordRoute();
        AddressParametersParser.prototype.parse.call(this,recordRoute);
        recordRouteList.add(recordRoute);
        this.lexer.SPorHT();
        var la = this.lexer.lookAhead(0);
        if (la == ',') {
            this.lexer.match(',');
            this.lexer.SPorHT();
        } 
        else if (la == '\n')
        {
            break;
        }
        else
        {
            console.error("RecordRouteParser:parse(): unexpected char");
            throw "RecordRouteParser:parse(): unexpected char";
        }
    }
    return recordRouteList;
}

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
 *  Implementation of the JAIN-SIP ProxyAuthenticateParser .
 *  @see  gov/nist/javax/sip/parser/ProxyAuthenticateParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function ProxyAuthenticateParser() {
    this.classname="ProxyAuthenticateParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var proxyAuthenticate=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", proxyAuthenticate);
    }
}

ProxyAuthenticateParser.prototype = new ChallengeParser();
ProxyAuthenticateParser.prototype.constructor=ProxyAuthenticateParser;

ProxyAuthenticateParser.prototype.parse =function(){
    this.headerName(TokenTypes.prototype.PROXY_AUTHENTICATE);
    var proxyAuth = new ProxyAuthenticate();
    ChallengeParser.prototype.parse.call(this,proxyAuth);
    return proxyAuth;
}

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
 *  Implementation of the JAIN-SIP ProxyAuthorizationParser .
 *  @see  gov/nist/javax/sip/parser/ProxyAuthorizationParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function ProxyAuthorizationParser() {
    this.classname="ProxyAuthorizationParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var proxyAuthorization=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", proxyAuthorization);
    }
}

ProxyAuthorizationParser.prototype = new ChallengeParser();
ProxyAuthorizationParser.prototype.constructor=ProxyAuthorizationParser;

ProxyAuthorizationParser.prototype.parse =function(){
    this.headerName(TokenTypes.prototype.PROXY_AUTHORIZATION);
    var proxyAuth = new ProxyAuthorization();
    ChallengeParser.prototype.parse.call(this,proxyAuth);
    return proxyAuth;
}

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
 *  Implementation of the JAIN-SIP TimeStampParser .
 *  @see  gov/nist/javax/sip/parser/TimeStampParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function TimeStampParser() {
    this.classname="TimeStampParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var timeStamp=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", timeStamp);
    }
}

TimeStampParser.prototype = new HeaderParser();
TimeStampParser.prototype.constructor=TimeStampParser;
TimeStampParser.prototype.TIMESTAMP="Timestamp";

TimeStampParser.prototype.parse =function(){
    var timeStamp = new TimeStamp();
    this.headerName(TokenTypes.prototype.TIMESTAMP);
    timeStamp.setHeaderName(this.TIMESTAMP);
    this.lexer.SPorHT();
    var firstNumber = this.lexer.number();
    if (this.lexer.lookAhead(0) == '.') {
        this.lexer.match('.');
        var secondNumber = this.lexer.number();

        var s = firstNumber + "." + secondNumber;
        var ts = s;
        timeStamp.setTimeStamp(ts);
    } else {
        ts = firstNumber;
        timeStamp.setTime(ts);
    }

    this.lexer.SPorHT();
    if (this.lexer.lookAhead(0) != '\n') 
    {
        firstNumber = this.lexer.number();
    }
    if (this.lexer.lookAhead(0) == '.') {
        this.lexer.match('.');
        secondNumber = this.lexer.number();
        s = firstNumber + "." + secondNumber;
        ts = s;
        timeStamp.setDelay(ts);
    } else {
        ts = firstNumber;
        timeStamp.setDelay(ts);
    }
    return timeStamp;
}


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
 *  Implementation of the JAIN-SIP UserAgentParser .
 *  @see  gov/nist/javax/sip/parser/UserAgentParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function UserAgentParser() {
    this.classname="UserAgentParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var userAgent=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", userAgent);
    }
}

UserAgentParser.prototype = new HeaderParser();
UserAgentParser.prototype.constructor=UserAgentParser;

UserAgentParser.prototype.parse =function(){
    var userAgent = new UserAgent();
    this.headerName(TokenTypes.prototype.USER_AGENT);
    if (this.lexer.lookAhead(0) == '\n')
    {
        console.error("UserAgentParser:parse(): empty header");
        throw "UserAgentParser:parse(): empty header";
    }

    while (this.lexer.lookAhead(0) != '\n'
        && this.lexer.lookAhead(0) != '\0') {
        if (this.lexer.lookAhead(0) == '(') {
            var comment = this.lexer.comment();
            userAgent.addProductToken('(' + comment + ')');
        } else {
            this.getLexer().SPorHT();
            var product = this.lexer.byteStringNoSlash();
            if ( product == null ) {
                console.error("UserAgentParser:parse(): expected product string");
                throw "UserAgentParser:parse():expected product string";
            }
            var productSb = product;
            if (this.lexer.peekNextToken().getTokenValue() == '/') {
                this.lexer.match('/');
                this.getLexer().SPorHT();
                var productVersion = this.lexer.byteStringNoWhiteSpace();
                if ( productVersion == null ) {
                    console.error("UserAgentParser:parse(): expected product version");
                    throw "UserAgentParser:parse(): expected product version";
                }
                productSb=productSb+"/"+productVersion;
            }
            userAgent.addProductToken(productSb.toString());
        }
        this.lexer.SPorHT();
    }
    return userAgent;
}


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
 *  Implementation of the JAIN-SIP SupportedParser .
 *  @see  gov/nist/javax/sip/parser/SupportedParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function SupportedParser() {
    this.classname="SupportedParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var supported=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", supported);
    }
}

SupportedParser.prototype = new HeaderParser();
SupportedParser.prototype.constructor=SupportedParser;
SupportedParser.prototype.SUPPORTED="Supported";

SupportedParser.prototype.parse =function(){
    var supportedList = new SupportedList();
    this.headerName(TokenTypes.prototype.SUPPORTED);
    while (this.lexer.lookAhead(0) != '\n') {
        this.lexer.SPorHT();
        var supported = new Supported();
        supported.setHeaderName(this.SUPPORTED);
        this.lexer.match(TokenTypes.prototype.ID);
        var token = this.lexer.getNextToken();
        supported.setOptionTag(token.getTokenValue());
        this.lexer.SPorHT();
        supportedList.add(supported);
        while (this.lexer.lookAhead(0) == ',') {
            this.lexer.match(',');
            this.lexer.SPorHT();
            supported = new Supported();
            this.lexer.match(TokenTypes.prototype.ID);
            token = this.lexer.getNextToken();
            supported.setOptionTag(token.getTokenValue());
            this.lexer.SPorHT();
            supportedList.add(supported);
        }

    }
    return supportedList;
}


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
 *  Implementation of the JAIN-SIP ServerParser .
 *  @see  gov/nist/javax/sip/parser/ServerParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function ServerParser() {
    this.classname="ServerParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var server=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", server);
    }
}

ServerParser.prototype = new HeaderParser();
ServerParser.prototype.constructor=ServerParser;

ServerParser.prototype.parse =function(){
    var server = new Server();
    this.headerName(TokenTypes.prototype.SERVER);
    if (this.lexer.lookAhead(0) == '\n')
    {
       console.error("ServerParser:parse(): empty header");
       throw "ServerParser:parse():  empty header";
    }
    while (this.lexer.lookAhead(0) != '\n'
        && this.lexer.lookAhead(0) != '\0') {
        if (this.lexer.lookAhead(0) == '(') {
            var comment = this.lexer.comment();
            server.addProductToken('(' + comment + ')');
        } else {
            var tok;
            var marker = 0;
            try {
                marker = this.lexer.markInputPosition();
                tok = this.lexer.getString('/');
                if (tok.charAt(tok.length() - 1) == '\n')
                {
                    tok = tok.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
                }
                server.addProductToken(tok);
            } catch (ex) {
                this.lexer.rewindInputPosition(marker);
                tok = this.lexer.getRest().replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
                server.addProductToken(tok);
                break;
            }
        }
    }
    return server;
}

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
 *  Implementation of the JAIN-SIP SubjectParser .
 *  @see  gov/nist/javax/sip/parser/SubjectParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function SubjectParser() {
    this.classname="SubjectParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var subject=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", subject);
    }
}

SubjectParser.prototype = new HeaderParser();
SubjectParser.prototype.constructor=SubjectParser;

SubjectParser.prototype.parse =function(){
    var subject = new Subject();
    this.headerName(TokenTypes.prototype.SUBJECT);
    this.lexer.SPorHT();
    var s = this.lexer.getRest();
    subject.setSubject(s.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, ''));
    return subject;
}

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
 *  Implementation of the JAIN-SIP MaxForwardsParser .
 *  @see  gov/nist/javax/sip/parser/MaxForwardsParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function MaxForwardsParser() {
    this.classname="MaxForwardsParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var contentLength=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", contentLength);
    }
}

MaxForwardsParser.prototype = new HeaderParser();
MaxForwardsParser.prototype.constructor=MaxForwardsParser;

MaxForwardsParser.prototype.parse =function(){
    var contentLength = new MaxForwards();
    this.headerName(TokenTypes.prototype.MAX_FORWARDS);
    var number = this.lexer.number();
    contentLength.setMaxForwards(number);
    this.lexer.SPorHT();
    this.lexer.match("\n");
    return contentLength;
}

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
 *  Implementation of the JAIN-SIP ReasonParser .
 *  @see  gov/nist/javax/sip/parser/ReasonParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function ReasonParser() {
    this.classname="ReasonParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var reason=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", reason);
    }
}

ReasonParser.prototype = new ParametersParser();
ReasonParser.prototype.constructor=ReasonParser;

ReasonParser.prototype.parse =function(){
    var reasonList = new ReasonList();
    this.headerName(TokenTypes.prototype.REASON);
    this.lexer.SPorHT();
    while (this.lexer.lookAhead(0) != '\n') {
        var reason = new Reason();
        this.lexer.match(TokenTypes.prototype.ID);
        var token = this.lexer.getNextToken();
        var value = token.getTokenValue();
        reason.setProtocol(value);
        ParametersParser.prototype.parse.call(this,reason);
        reasonList.add(reason);
        if (this.lexer.lookAhead(0) == ',') {
            this.lexer.match(',');
            this.lexer.SPorHT();
        } 
        else
        {
            this.lexer.SPorHT();
        }
        return reasonList;
    }
}

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
 *  Implementation of the JAIN-SIP RequestLineParser .
 *  @see  gov/nist/javax/sip/parser/RequestLineParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function RequestLineParser() {
    this.classname="RequestLineParser"; 
    if(typeof arguments[0]=="string")
    {
        var requestLine=arguments[0];
        this.lexer = new Lexer("method_keywordLexer", requestLine);
    }
    else if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("method_keywordLexer");
    }
}

RequestLineParser.prototype = new Parser();
RequestLineParser.prototype.constructor=RequestLineParser;

RequestLineParser.prototype.parse =function(){
    var retval = new RequestLine();
    var m = this.method();
    this.lexer.SPorHT();
    retval.setMethod(m);
    this.lexer.selectLexer("sip_urlLexer");
    var urlParser = new URLParser(this.getLexer());
    var url = urlParser.uriReference(true);
    this.lexer.SPorHT();
    retval.setUri(url);
    this.lexer.selectLexer("request_lineLexer");
    var v = this.sipVersion();
    retval.setSipVersion(v);
    this.lexer.SPorHT();
    this.lexer.match('\n');
    return retval;
}/*
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
 *  Implementation of the JAIN-SIP ExpiresParser .
 *  @see  gov/nist/javax/sip/parser/ExpiresParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function ExpiresParser() {
    this.classname="ExpiresParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var text=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", text);
    }
}

ExpiresParser.prototype = new HeaderParser();
ExpiresParser.prototype.constructor=ExpiresParser;

ExpiresParser.prototype.parse =function(){
    var expires = new Expires();
    this.lexer.match(TokenTypes.prototype.EXPIRES);
    this.lexer.SPorHT();
    this.lexer.match(':');
    this.lexer.SPorHT();
    var nextId = this.lexer.getNextId();
    this.lexer.match('\n');
    var delta = nextId+0;
    expires.setExpires(delta);
    return expires;
}

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
 *  Implementation of the JAIN-SIP EventParser .
 *  @see  gov/nist/javax/sip/parser/EventParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function EventParser() {
    this.classname="EventParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var event=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", event);
    }
}

EventParser.prototype = new ParametersParser();
EventParser.prototype.constructor=EventParser;

EventParser.prototype.parse =function(){
    this.headerName(TokenTypes.prototype.EVENT);
    this.lexer.SPorHT();
    var event = new Event();
    this.lexer.match(TokenTypes.prototype.ID);
    var token = this.lexer.getNextToken();
    var value = token.getTokenValue();
    event.setEventType(value);
    ParametersParser.prototype.parse.call(this,event);
    this.lexer.SPorHT();
    this.lexer.match('\n');
    return event;
}

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
 *  Implementation of the JAIN-SIP StatusLineParser .
 *  @see  gov/nist/javax/sip/parser/StatusLineParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function StatusLineParser() {
    this.classname="StatusLineParser"; 
    if(typeof arguments[0]=="string")
    {
        var statusLine=arguments[0];
        this.lexer = new Lexer("status_lineLexer", statusLine);
    }
    else if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("status_lineLexer");
    }
}

StatusLineParser.prototype = new Parser();
StatusLineParser.prototype.constructor=StatusLineParser;

StatusLineParser.prototype.statusCode =function(){
    var scode = this.lexer.number();
    var retval = scode;
    return retval;
}
StatusLineParser.prototype.reasonPhrase =function(){
    return this.lexer.getRest().replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
}
StatusLineParser.prototype.parse =function(){
    var retval = new StatusLine();
    var version = this.sipVersion();
    retval.setSipVersion(version);
    this.lexer.SPorHT();
    var scode = this.statusCode();
    retval.setStatusCode(scode);
    this.lexer.SPorHT();
    var rp = this.reasonPhrase();
    retval.setReasonPhrase(rp);
    this.lexer.SPorHT();
    return retval;
}/*
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
 *  Implementation of the JAIN-SIP ContentDispositionParser .
 *  @see  gov/nist/javax/sip/parser/ContentDispositionParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function ContentDispositionParser() {
    this.classname="ContentDispositionParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var contentDisposition=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", contentDisposition);
    }
}

ContentDispositionParser.prototype = new ParametersParser();
ContentDispositionParser.prototype.constructor=ContentDispositionParser;
ContentDispositionParser.prototype.CONTENT_DISPOSITION="Content-Disposition";

ContentDispositionParser.prototype.parse =function(){
    this.headerName(TokenTypes.prototype.CONTENT_DISPOSITION);
    
    var cd = new ContentDisposition();
    cd.setHeaderName(this.CONTENT_DISPOSITION);
    
    this.lexer.SPorHT();
    this.lexer.match(TokenTypes.prototype.ID);

    var token = this.lexer.getNextToken();
    cd.setDispositionType(token.getTokenValue());
    this.lexer.SPorHT();
    ParametersParser.prototype.parse.call(this,cd);

    this.lexer.SPorHT();
    this.lexer.match('\n');

    return cd;
}


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
 *  Implementation of the JAIN-SIP AllowParser .
 *  @see  gov/nist/javax/sip/parser/AllowParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function AllowParser() {
    this.classname="AllowParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var buffer=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", buffer);
    }
}

AllowParser.prototype = new HeaderParser();
AllowParser.prototype.constructor=AllowParser;
AllowParser.prototype.ALLOW="Allow";

AllowParser.prototype.parse =function(){
    var list = new AllowList();
    this.headerName(TokenTypes.prototype.ALLOW);
    var allow = new Allow();
    allow.setHeaderName(this.ALLOW);
    this.lexer.SPorHT();
    this.lexer.match(TokenTypes.prototype.ID);
    var token = this.lexer.getNextToken();
    allow.setMethod(token.getTokenValue());
    list.add(allow);
    this.lexer.SPorHT();
    while (this.lexer.lookAhead(0) == ',') {
        this.lexer.match(',');
        this.lexer.SPorHT();
        allow = new Allow();
        this.lexer.match(TokenTypes.prototype.ID);
        token = this.lexer.getNextToken();
        allow.setMethod(token.getTokenValue());
        list.add(allow);
        this.lexer.SPorHT();
    }
    this.lexer.SPorHT();
    this.lexer.match('\n');
    return list;
}/*
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
 *  Implementation of the JAIN-SIP AllowEventsParser .
 *  @see  gov/nist/javax/sip/parser/AllowEventsParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function AllowEventsParser() {
    this.classname="AllowEventsParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var allowEvents=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", allowEvents);
    }
}

AllowEventsParser.prototype = new HeaderParser();
AllowEventsParser.prototype.constructor=AllowEventsParser;
AllowEventsParser.prototype.ALLOW_EVENTS="Allow-Events";

AllowEventsParser.prototype.parse =function(){
    var list = new AllowEventsList();
    this.headerName(TokenTypes.prototype.ALLOW_EVENTS);
    var allowEvents = new AllowEvents();
    allowEvents.setHeaderName(this.ALLOW_EVENTS);
    this.lexer.SPorHT();
    this.lexer.match(TokenTypes.prototype.ID);
    var token = this.lexer.getNextToken();
    allowEvents.setEventType(token.getTokenValue());
    list.add(allowEvents);
    this.lexer.SPorHT();
    while (this.lexer.lookAhead(0) == ',') {
        this.lexer.match(',');
        this.lexer.SPorHT();
        allowEvents = new AllowEvents();
        this.lexer.match(TokenTypes.prototype.ID);
        token = this.lexer.getNextToken();
        allowEvents.setEventType(token.getTokenValue());
        list.add(allowEvents);
        this.lexer.SPorHT();
    }
    this.lexer.SPorHT();
    this.lexer.match('\n');
    return list;
}

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
 *  Implementation of the JAIN-SIP ParserFactory .
 *  @see  gov/nist/javax/sip/parser/ParserFactory.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function ParserFactory() {
    this.classname="ParserFactory"; 
    this.parserTable=new Array();
    this.constructorArgs=null;
    //i use type String to replace the type of class in Java and creat the object when it is in need.
    this.parserConstructorCache=new Array();
    this.put(this.parserTable,"t",new ToParser().classname);
    this.put(this.parserTable,"To".toLowerCase(), new ToParser().classname);
    this.put(this.parserTable,"From".toLowerCase(),new FromParser().classname);
    this.put(this.parserTable,"f",new FromParser().classname);
    this.put(this.parserTable,"CSeq".toLowerCase(),new CSeqParser().classname);
    this.put(this.parserTable,"Via".toLowerCase(),new ViaParser().classname);
    this.put(this.parserTable,"v",new ViaParser().classname);
    this.put(this.parserTable,"Contact".toLowerCase(),new ContactParser().classname);
    this.put(this.parserTable,"m",new ContactParser().classname);
    this.put(this.parserTable,"Content-Type".toLowerCase(),new ContentTypeParser().classname);
    this.put(this.parserTable,"c",new ContentTypeParser().classname);
    this.put(this.parserTable,"Content-Length".toLowerCase(),new ContentLengthParser().classname);
    this.put(this.parserTable,"l",new ContentLengthParser().classname);
    this.put(this.parserTable,"Authorization".toLowerCase(),new AuthorizationParser().classname);
    this.put(this.parserTable,"WWW-Authenticate".toLowerCase(),new WWWAuthenticateParser().classname);
    this.put(this.parserTable,"Call-ID".toLowerCase(),new CallIDParser().classname);
    this.put(this.parserTable,"i",new CallIDParser().classname);
    this.put(this.parserTable,"Route".toLowerCase(),new RouteParser().classname);
    this.put(this.parserTable,"Record-Route".toLowerCase(),new RecordRouteParser().classname);
    this.put(this.parserTable,"Proxy-Authorization".toLowerCase(),new ProxyAuthorizationParser().classname);
    this.put(this.parserTable,"Proxy-Authenticate".toLowerCase(),new ProxyAuthenticateParser().classname);
    this.put(this.parserTable,"Timestamp".toLowerCase(),new TimeStampParser().classname);
    this.put(this.parserTable,"User-Agent".toLowerCase(),new UserAgentParser().classname);
    this.put(this.parserTable,"Supported".toLowerCase(),new SupportedParser().classname);
    this.put(this.parserTable,"k",new SupportedParser().classname);
    this.put(this.parserTable,"Server".toLowerCase(),new ServerParser().classname);
    this.put(this.parserTable,"Subject".toLowerCase(),new SubjectParser().classname);
    this.put(this.parserTable,"s",new SubjectParser().classname); 
    this.put(this.parserTable,"Max-Forwards".toLowerCase(),new MaxForwardsParser().classname);
    this.put(this.parserTable,"Reason".toLowerCase(),new ReasonParser().classname);
    this.put(this.parserTable,"Expires".toLowerCase(),new ExpiresParser().classname);
    this.put(this.parserTable,"Event".toLowerCase(),new EventParser().classname);
    this.put(this.parserTable,"o",new EventParser().classname);
    this.put(this.parserTable,"Content-Disposition".toLowerCase(),new ContentDispositionParser().classname);
    this.put(this.parserTable,"Allow".toLowerCase(),new AllowParser().classname);
    this.put(this.parserTable,"Allow-Events".toLowerCase(),new AllowEventsParser().classname);
    this.put(this.parserTable,"u",new AllowEventsParser().classname);
}

ParserFactory.prototype.createParser =function(line){
    var lexer=new Lexer("","");
    var headerName = lexer.getHeaderName(line);
    var headerValue = lexer.getHeaderValue(line);
    if (headerName == null || headerValue == null) {
        console.error("ParserFactory:createParser(): the header name or value is null");
        throw "ParserFactory:createParser(): the header name or value is null";
    }
    var parserClass = null;
        var lowercaseHeadervalue=headerName.toLowerCase();
    for(var i=0;i<this.parserTable.length;i++)
    {
        if(this.parserTable[i][0]==lowercaseHeadervalue)
        {
            parserClass=this.parserTable[i][1];
        }
    }
    
    if (parserClass != null) {
        var cons = null;
        for(i=0;i<this.parserConstructorCache.length;i++)
        {
            if(this.parserConstructorCache[i][0]==parserClass)
            {
                parserClass=this.parserConstructorCache[i][1];
            }
        }
        if (cons == null) {
            cons = new Function('return new ' + parserClass)();
            this.put(this.parserConstructorCache, parserClass, cons);
        }
        var args = line;
        var retval =  new window[parserClass](args);
        return retval;
    } else {
        return new HeaderParser(line);
    }
}

ParserFactory.prototype.put =function(table,name, value){
    var n=0;
    for(var i=0;i<table.length;i++)// loop for method put() of hashtable
    {
        var key = table[i][0];
        if (key==name) {
            n=1;
            var x=new Array();
            x[0]=key;
            x[1]=value;
            table[i]=x;
        } 
    }
    if(n==0)
    {
        x=new Array();
        x[0]=name;
        x[1]=value;
        table.push(x);
    }
    return table;
}
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
 *  Implementation of the JAIN-SIP WSMsgParser .
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function WSMsgParser(sipstack,data) {
    this.classname="WSMsgParser"; 
    this.data=data;
    this.peerProtocol=null;
    this.messageProcessor = null;
    this.sipStack=sipstack;
}

WSMsgParser.prototype.RPORT="rport";
WSMsgParser.prototype.RECEIVED="received";

WSMsgParser.prototype.parsermessage =function(requestsent){
    var smp = new StringMsgParser();
    var sipMessage = smp.parseSIPMessage(this.data);
    var cl =  sipMessage.getContentLength();
    var contentLength = 0;
    if (cl != null) {
        contentLength = cl.getContentLength();
    }
    else {
        contentLength = 0;
    }
    if (contentLength == 0) {
        sipMessage.removeContent();
    } 
    console.info("SIP message received: "+sipMessage.encode());
    this.processMessage(sipMessage,requestsent);
}

WSMsgParser.prototype.processMessage =function(sipMessage,requestSent){
    if (sipMessage.getFrom() == null
        ||  sipMessage.getTo() == null || sipMessage.getCallId() == null
        || sipMessage.getCSeq() == null || sipMessage.getViaHeaders() == null) {
        return;
    }
    var channel=this.getSIPStack().getChannel();
    if (sipMessage instanceof SIPRequest) {
        this.peerProtocol = "WS";
        var sipRequest =  sipMessage;
        var sipServerRequest = this.sipStack.newSIPServerRequest(sipRequest, channel);
        if (sipServerRequest != null) 
        {
            sipServerRequest.processRequest(sipRequest,channel);
        }//i delete all parts of logger 
    } 
    else {
        var sipResponse = sipMessage;
        try {
            sipResponse.checkHeaders();
        } catch (ex) {
            console.error("WSMsgParser:processMessage(): catched exception:"+ex);
            return;
        }
        var sipServerResponse = this.sipStack.newSIPServerResponse(sipResponse, channel);
        if (sipServerResponse != null) {
            if (sipServerResponse instanceof SIPClientTransaction
                && !sipServerResponse.checkFromTag(sipResponse)) 
                {
                return;
            }
            sipServerResponse.processResponse(sipResponse, channel);
        } 
    }
}


WSMsgParser.prototype.getSIPStack =function(){
    return this.sipStack;
}/*
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
 *  Implementation of the JAIN-SIP MessageObject .
 *  @see  gov/nist/javax/sip/message/MessageObject.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function MessageObject() {
    this.classname="MessageObject";
}

MessageObject.prototype = new GenericObject();
MessageObject.prototype.constructor=MessageObject;

MessageObject.prototype.encode =function(){
}/*
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
 *  Implementation of the JAIN-SIP ListMap .
 *  @see  gov/nist/javax/sip/message/ListMap.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function ListMap() {
    this.classname="ListMap";
    this.headerListTable=new Array();
    this.initialized=null;
    this.initializeListMap();
}

ListMap.prototype.put =function(hashtable, class1, class2){
    var n=0;
    for(var i=0;i<hashtable.length;i++)// loop for method put() of hashtable
    {
        var key = hashtable[i][0];
        if (key.classname==class1) {
            n=1;
            var x=new Array();
            x[0]=class1;
            x[1]=class2;
            hashtable[i]=x;
        } 
    }
    if(n==0)
    {
        var c=hashtable.length;
        x=new Array();
        x[0]=class1;
        x[1]=class2;
        hashtable[c]=x;
    }
}

ListMap.prototype.initializeListMap =function(){
    this.put(this.headerListTable, "Contact", "ContactList");
    this.put(this.headerListTable, "Via", "ViaList");
    this.put(this.headerListTable, "WWW-Authenticate", "WWWAuthenticateList");
    this.put(this.headerListTable, "Route", "RouteList");
    this.put(this.headerListTable, "Proxy-Authenticate", "ProxyAuthenticateList");
    //this.put(this.headerListTable, "ProxyAuthorization", "ProxyAuthorizationList");
    //this.put(this.headerListTable, "Authorization", "AuthorizationList");
    this.put(this.headerListTable, "Allow", "AllowList");
    this.put(this.headerListTable, "RecordRoute", "RecordRouteList");
    this.put(this.headerListTable, "Supported", "SupportedList");
    this.initialized = true;
}

ListMap.prototype.hasList =function(){
    if (!this.initialized)
    {
        initializeListMap();
    }
    var sipHeader=arguments[0];
    if(typeof sipHeader=="object")
    {
        if (sipHeader instanceof SIPHeaderList)
        {
            return false;
        }
        else
        {
            var headerClass = sipHeader.classname;
            var listClass =null;
        }
    }
    else
    {
        headerClass = sipHeader;
        listClass =null;
    }
    for(var i=0;i<this.headerListTable.length;i++)
    {
        if(this.headerListTable[i][0]==headerClass)
        {
            listClass=this.headerListTable[i][1];
        }
    }
    if(listClass != null)
    {
        return true;
    }
    else
    {
        return false;
    }
    
}

ListMap.prototype.getListClass =function(sipHdrClass){
    if (!this.initialized)
    {
        initializeListMap();
    }
    var list=null;
    for(var i=0;i<this.headerListTable.length;i++)
    {
        if(this.headerListTable[i][0]==sipHdrClass)
        {
            list=this.headerListTable[i][1];
        }
    }
    return list;
}

ListMap.prototype.getList =function(sipHeader){
    if (!this.initialized)
    {
        initializeListMap();
    }
    var headerClass = sipHeader.headerName;
    var listClass =null;
    for(var i=0;i<this.headerListTable.length;i++)
    {
        if(this.headerListTable[i][0]==headerClass)
        {
            listClass=this.headerListTable[i][1];
        }
    }
    if(listClass!=null)
    {
        var shl =  new Function('return new ' + listClass)();
        shl.setHeaderName(sipHeader.getName());
        return shl;   
    }
    else
    {
        return null;
    }   
}
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
 *  Implementation of the JAIN-SIP SIPMessage .
 *  @see  gov/nist/javax/sip/message/SIPMessage.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function SIPMessage() {
    this.classname="SIPMessage";
    this.contentEncodingCharset = new MessageFactoryImpl().getDefaultContentEncodingCharset();
    this.nullRequest=null;
    this.unrecognizedHeaders= new Array();
    this.headers=new Array();
    this.fromHeader=new From();
    this.toHeader=new To();
    this.cSeqHeader=new CSeq();
    this.callIdHeader=new CallID();
    this.contentLengthHeader=new ContentDisposition();
    this.maxForwardsHeader=new MaxForwards();
    this.size=null;
    this.messageContent=null;
    this.nameTable=new Array();
    this.applicationData=null;
    var contentlength=new ContentLength(0)
    this.stackTransaction =null;
    this.attachHeader(contentlength, false);
    
}

SIPMessage.prototype = new MessageObject();
SIPMessage.prototype.constructor=SIPMessage;
SIPMessage.prototype.NEWLINE="\r\n";
SIPMessage.prototype.ViaHeader="Via";
SIPMessage.prototype.BRANCH_MAGIC_COOKIE_UPPER_CASE="Z9HG4BK";
SIPMessage.prototype.CANCEL="CANCEL";
SIPMessage.prototype.CONTENT_TYPE_LOWERCASE="content-type";
SIPMessage.prototype.AUTHORIZATION_LOWERCASE="authorization";
SIPMessage.prototype.PROXYAUTHORIZATION_LOWERCASE="proxy-authorization";
SIPMessage.prototype.CONTACT_LOWERCASE="contact";
SIPMessage.prototype.VIA_LOWERCASE="via";
SIPMessage.prototype.ROUTE_LOWERCASE="route";
SIPMessage.prototype.RECORDROUTE_LOWERCASE="record-route";
SIPMessage.prototype.CONTENT_DISPOSITION_LOWERCASE="content-disposition";
SIPMessage.prototype.EXPIRES_LOWERCASE="expires";

SIPMessage.prototype.isRequestHeader =function(sipHeader){
    if(sipHeader instanceof Authorization || sipHeader instanceof MaxForwards
        || sipHeader instanceof UserAgent|| sipHeader instanceof ProxyAuthorization
        || sipHeader instanceof Route|| sipHeader instanceof RouteList || sipHeader instanceof Subject)
        {
        return true;
    }
    else
    {
        return false;
    }
}

SIPMessage.prototype.isResponseHeader =function(sipHeader){
    if(sipHeader instanceof WWWAuthenticate
        || sipHeader instanceof ProxyAuthenticate)
        {
        return true;
    }
    else
    {
        return false;
    }
}

SIPMessage.prototype.getMessageAsEncodedStrings =function(){
    var retval = new Array();
    for(var i=0;i<this.headers.length;i++)
    {
        var sipHeader =  this.headers[i];
        if (sipHeader instanceof SIPHeaderList)
        {
            var shl =  sipHeader;
            for(var t=0;t<shl.getHeadersAsEncodedStrings().length;t++)
            {
                retval[retval.length]=shl.getHeadersAsEncodedStrings()[t];
            }
        } 
        else 
        {
            retval[retval.length]=sipHeader.encode();
        }
    }
    return retval;
}

SIPMessage.prototype.encodeSIPHeaders =function(){
    var encoding = "";
    var string="";
    for(var i=0;i<this.headers.length;i++)
    {
        var siphdr = this.headers[i];
        if (!(siphdr instanceof ContentLength))
        {
            string=string+siphdr.encodeBuffer(encoding);
        }
    }
    string=(string+this.contentLengthHeader.encodeBuffer(encoding)+this.NEWLINE).toString();
    return string;
}

SIPMessage.prototype.encodeMessage =function(){
}

SIPMessage.prototype.getDialogId =function(isServerTransaction){
}

SIPMessage.prototype.encode =function(){
    var encoding = "";
    for(var i=0;i<this.headers.length;i++)
    {
        var siphdr = this.headers[i];
        if (!(siphdr instanceof ContentLength))
        {
            encoding=encoding+siphdr.encode();
        }
    }
    for(i=0;i<this.unrecognizedHeaders.length;i++)
    {
        var unrecognized=this.unrecognizedHeaders[i];
        encoding=encoding+unrecognized+this.NEWLINE;
    }
    encoding=encoding+this.contentLengthHeader.encode()+this.NEWLINE;
    if (this.messageContent != null) {
        var content = this.messageContent;
        encoding=encoding+content;
    }
    return encoding.toString();
}

SIPMessage.prototype.encodeAsBytes =function(transport){
    if (this instanceof SIPRequest && this.isNullRequest()) 
    {
        return this.getBytes("\r\n\r\n");
    }
    var topVia = this.getHeader(this.ViaHeader);
    topVia.setTransport(transport);
    var encoding = "";
    for(var i=0;i<this.headers.length;i++)
    {
        var siphdr =  this.headers[i];
        if (!(siphdr instanceof ContentLength))
        {
            siphdr.encode(encoding);
        }
    }
    this.contentLengthHeader.encode(encoding);
    encoding=encoding+this.NEWLINE;
    var retval = null;
    var content = this.getRawContent();
    if (content != null) {
        var msgarray = null;
        msgarray = this.getBytes(encoding.toString());
        retval=msgarray.concat(content);
    } else {
        retval = this.getBytes(encoding.toString());
    }
    return retval;
}

SIPMessage.prototype.attachHeader =function(){
    if(arguments.length==1)
    {
        var h=arguments[0];
        this.attachHeaderargu1(h);
    }
    else if(arguments.length==2)
    {
        h=arguments[0];
        var replaceflag=arguments[1];
        this.attachHeaderargu2(h,replaceflag);
    }
    else if(arguments.length==3)
    {
        h=arguments[0];
        replaceflag=arguments[1];
        var top=arguments[2];
        this.attachHeaderargu3(h,replaceflag,top);
    }
}

SIPMessage.prototype.attachHeaderargu1 =function(h){
    if (h == null)
    {
        console.error("MessageFactoryImpl:attachHeaderargu1(): null header!");
        throw "MessageFactoryImpl:attachHeaderargu1(): null header!";
    }
    if (h instanceof SIPHeaderList) {
        var hl =  h;
        if (hl.hlist.length==0) {
            return;
        }
    }
    this.attachHeaderargu3(h, false, false);
}

SIPMessage.prototype.attachHeaderargu2 =function(h, replaceflag){
    this.attachHeaderargu3(h, replaceflag,false);
}

SIPMessage.prototype.attachHeaderargu3 =function(header, replaceFlag, top){
    if (header == null)
    {
        console.error("MessageFactoryImpl:attachHeaderargu3(): null header!");
        throw "MessageFactoryImpl:attachHeaderargu3(): null header!";
    }
    var h=null;
    var listmap=new ListMap();
    if(listmap.hasList(header) && !(header instanceof SIPHeaderList))
    {
        var hdrList = listmap.getList(header);
        hdrList.add(header);
        h = hdrList;
    }
    else
    {
        h = header;
    }
    var headerNameLowerCase = h.getName().toLowerCase();
    var l=null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        if(this.nameTable[i][0]==headerNameLowerCase)
        {
            l=i;
        }
    }
    
    if (replaceFlag) 
    {
        if(l!=null)
        {
            this.nameTable.splice(l,1);
        }
    } 
    else if (l!=null && !(h instanceof SIPHeaderList)) 
    {
        if (h instanceof ContentLength) {
            var cl =  h;
            this.contentLengthHeader.setContentLength(cl.getContentLength());
        }
        return;
    }
    
    var originalHeader = this.getHeader(header.getName());
    if (originalHeader != null) {
        var hn=this.headers;
        var n=0;
        for(i=0;i<this.headers.length;i++)
        {
            var next = this.headers[i];
            
            if (next!=originalHeader) {
                hn[n]=next;
                n=n+1;
            }
        }
        this.headers=hn;
    }
    if (l==null) 
    {
        var x=new Array();
        x[0]=headerNameLowerCase;
        x[1]=h;
        this.nameTable.push(x);
        this.headers.push(h);
    }
    else 
    {
        if (h instanceof SIPHeaderList) {
            var hdrlist =  this.nameTable[l][0];
            if (hdrlist != null)
            {
                hdrlist.concatenate(h, top);
            }
            else
            {
                x=new Array();
                x[0]=headerNameLowerCase;
                x[1]=h;
                this.nameTable.push(x);
            }
        } 
        else 
        {
            x=new Array();
            x[0]=headerNameLowerCase;
            x[1]=h;
            this.nameTable.push(x);
        }
    }
    if (h instanceof From) {
        this.fromHeader =  h;
    } else if (h instanceof ContentLength) {
        this.contentLengthHeader =  h;
    } else if (h instanceof To) {
        this.toHeader =  h;
    } else if (h instanceof CSeq) {
        this.cSeqHeader =  h;
    } else if (h instanceof CallID) {
        this.callIdHeader =  h;
    } else if (h instanceof MaxForwards) {
        this.maxForwardsHeader =  h;
    }
}


SIPMessage.prototype.setHeader =function(sipHeader){
    var header =  sipHeader;
    if (header == null)
    {
        console.error("MessageFactoryImpl:setHeader(): null header!");
        throw "MessageFactoryImpl:setHeader(): null header!";
    }
    if (header instanceof SIPHeaderList) {
        var hl =  header;
        if (hl.hlist.length==0)
        {
            return;
        }
    }
    this.removeHeader(header.getHeaderName());
    this.attachHeader(header, true, false);
}


SIPMessage.prototype.setHeaders =function(headers){
    for(var i=0;i<headers.length;i++)
    {
        var sipHeader = headers[i];
        this.attachHeader(sipHeader, false);
    }
}

SIPMessage.prototype.removeHeader =function(){
    if(arguments.length==1)
    {
        var headerName=arguments[0];
        this.removeHeaderargu1(headerName);
    }
    else if(arguments.length==2)
    {
        headerName=arguments[0];
        var top=arguments[1];
        this.removeHeaderargu2(headerName,top);
    }
}

SIPMessage.prototype.removeHeaderargu1 =function(headerName){
    if (headerName == null)
    {
        console.error("MessageFactoryImpl:removeHeaderargu1(): null header!");
        throw "MessageFactoryImpl:removeHeaderargu1(): null header!";
    }
    var headerNameLowerCase = headerName.toLowerCase();
    var removed=null;
    var l=null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        
        if(this.nameTable[i][0]==headerNameLowerCase)
        {
            l=i;
            removed=this.nameTable[i][0];
        }
    }
    if (removed == null)
    {
        return;
    }
    else
    {
        this.nameTable.splice(l,1);
    }
    if (removed instanceof From) {
        this.fromHeader = null;
    } else if (removed instanceof To) {
        this.toHeader = null;
    } else if (removed instanceof CSeq) {
        this.cSeqHeader = null;
    } else if (removed instanceof CallID) {
        this.callIdHeader = null;
    } else if (removed instanceof MaxForwards) {
        this.maxForwardsHeader = null;
    } else if (removed instanceof ContentLength) {
        this.contentLengthHeader = null;
    }
    for(i=0;i<this.headers.length;i++)
    {
        var sipHeader = this.headers[i];
        if (sipHeader.getName().toLowerCase()==headerNameLowerCase)
        {
            l=i;
        }
    }
    this.headers.splice(l,1);
}


SIPMessage.prototype.removeHeaderargu2 =function(headerName,top){
    var headerNameLowerCase = headerName.toLowerCase();
    var toRemove=null;
    var l=null;
    var x=null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        if(this.nameTable[i][0]==headerNameLowerCase)
        {
            l=i;
            toRemove=this.nameTable[i][1];
        }
    }
    if (toRemove == null)
    {
        return;
    }
    if (toRemove instanceof SIPHeaderList) {
        var hdrList =  toRemove;
        if (top)
        {
            hdrList.removeFirst();
        }
        else
        {
            hdrList.removeLast();
        }
        if (hdrList.hlist.length==0) 
        {
            for(i=0;i<this.headers.length;i++)
            {
                var sipHeader = this.headers[i];
                if (sipHeader.getName().toLowerCase()==headerNameLowerCase)
                {
                    x=i;
                }
            } 
        }
        this.nameTable.splice(l,1);
        this.headers.splice(x,1);
    }      
}

SIPMessage.prototype.getTransactionId =function(){
    var topVia = new Via();
    if (this.getViaHeaders().hlist.length!=0) {
        topVia = this.getViaHeaders().getFirst();
    }
    if (topVia != null&& topVia.getBranch() != null
        && topVia.getBranch().toUpperCase().substr(0, 7)==
        this.BRANCH_MAGIC_COOKIE_UPPER_CASE) {
        if (this.getCSeq().getMethod()==(this.CANCEL))
        {
            return (topVia.getBranch() + ":" + this.getCSeq().getMethod()).toLowerCase();
        }
        else
        {
            return topVia.getBranch().toLowerCase();
        }
    }
    else {
        var retval = "";
        var from = this.getFrom();
        var to = this.getTo();
        if (from.hasTag())
        {
            retval=retval+from.getTag()+"-";
        }
        var cid = this.callIdHeader.getCallId();
        retval=retval+cid+"-"+this.cSeqHeader.getSequenceNumber()+"-"+this.cSeqHeader.getMethod();
        if (topVia != null) {
            retval=retval+"-"+topVia.getSentBy().encode();
            if (!topVia.getSentBy().hasPort()) {
                retval=retval+"-"+5060;
            }
        }
        if (this.getCSeq().getMethod()==this.CANCEL) {
            retval=retval+this.CANCEL;
        }
        var utils=new Utils();
        retval=retval.toString().toLowerCase().replace(":", "-").replace("@", "-")+utils.getSignature();
        return retval;
    }
}

SIPMessage.prototype.hashCode =function(){
    if(this.callIdHeader == null)
    {  
        console.error("MessageFactoryImpl:hashCode(): invalid message! Cannot compute hashcode! call-id header is missing !");
        throw "MessageFactoryImpl:hashCode(): invalid message! Cannot compute hashcode! call-id header is missing !";
    }
    else
    {
        var hash = 0;
        var set=this.callIdHeader.getCallId();
        if(!(set == null || set.value == ""))  
        {  
            for (var i = 0; i < set.length; i++)  
            {  
                hash = hash * 31 + set.charCodeAt(i);  
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
}
SIPMessage.prototype.hasContent =function(){
    return this.messageContent != null;
}

SIPMessage.prototype.getHeaders =function(){
    if(arguments.length!=0)
    {
        var headerName=arguments[0];
        if (headerName == null)
        {
            console.error("MessageFactoryImpl:getHeaders(): headerName header!");
            throw "MessageFactoryImpl:getHeaders(): null headerName!";
        }
        var sipHeader = null;
        for(var i=0;i<this.nameTable.length;i++)
        {
            if(this.nameTable[i][0]==headerName.toLowerCase())
            {
                sipHeader=this.nameTable[i][1];
            }
        }
        if (sipHeader == null)
        {
            var siphdr=new SIPHeaderList();
            return siphdr.listIterator();
        }
        if (sipHeader instanceof SIPHeaderList) 
        {
            return sipHeader.listIterator();
        }
        else 
        {
            return new HeaderIterator(this, sipHeader);
        }
    }
    else
    {
        return this.headers;
    }

}

SIPMessage.prototype.getHeader =function(headerName){
    return this.getHeaderLowerCase(headerName.toLowerCase());
}

SIPMessage.prototype.getHeaderLowerCase =function(lowerCaseHeaderName){
    if (lowerCaseHeaderName == null)
    {
         console.error("MessageFactoryImpl:getHeaderLowerCase(): null lowerCaseHeaderName !");
         throw "MessageFactoryImpl:getHeaderLowerCase(): null lowerCaseHeaderName!";
    }
    var sipHeader = null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        if(this.nameTable[i][0]==lowerCaseHeaderName)
        {
            sipHeader=this.nameTable[i][1];
        }
    }
    if (sipHeader instanceof SIPHeaderList)
    {
        return sipHeader.getFirst();
    }
    else
    {
        return sipHeader;
    }
}


SIPMessage.prototype.getContentTypeHeader =function(){
    return this.getHeaderLowerCase(this.CONTENT_TYPE_LOWERCASE);
}


SIPMessage.prototype.getWWWAuthenticate =function(){
    return this.getHeaderLowerCase("www-authenticate");
}

SIPMessage.prototype.getProxyAuthenticate =function(){
    return this.getHeaderLowerCase("proxy-authenticate");
}

SIPMessage.prototype.getContentLengthHeader =function(){
    return this.getContentLength();
}

SIPMessage.prototype.getFrom =function(){
    return this.fromHeader;
}

/*SIPMessage.prototype.getErrorInfoHeaders =function(){
    
}*/

SIPMessage.prototype.getContactHeaders =function(){
    return this.getSIPHeaderListLowerCase(this.CONTACT_LOWERCASE);
}

SIPMessage.prototype.getContactHeader =function(){
    var clist = this.getContactHeaders();
    if (clist != null) 
    {
        return clist.getFirst();
    } 
    else 
    {
        return null;
    }
}

SIPMessage.prototype.getViaHeaders =function(){
    return this.getSIPHeaderListLowerCase(this.VIA_LOWERCASE);
}

SIPMessage.prototype.setVia =function(viaList){
    if(viaList.classname=="Via")
    {
        var vList = new ViaList();
        vList.add(viaList);
    }
    else
    {
        vList = new ViaList();
        for(var i=0;i<viaList.hlist.length;i++)
        {
            var via = viaList.hlist[i];
            vList.add(via);
        }
    }
    this.setHeader(vList);
}

SIPMessage.prototype.getTopmostVia =function(){
    if (this.getViaHeaders() == null)
    {
        return null;
    }
    else
    {
        return this.getViaHeaders().getFirst();
    }
}

SIPMessage.prototype.getCSeq =function(){
    return this.cSeqHeader;
}

SIPMessage.prototype.getAuthorization =function(){
    return this.getHeaderLowerCase(this.AUTHORIZATION_LOWERCASE);
}

SIPMessage.prototype.getProxyAuthorization =function(){
    return this.getHeaderLowerCase(this.PROXYAUTHORIZATION_LOWERCASE);
}

SIPMessage.prototype.getMaxForwards =function(){
    return this.maxForwardsHeader;
}

SIPMessage.prototype.setMaxForwards =function(maxForwards){
    this.setHeader(maxForwards);
}

SIPMessage.prototype.getRouteHeaders =function(){
    return  this.getSIPHeaderListLowerCase(this.ROUTE_LOWERCASE);
}

SIPMessage.prototype.getCallId =function(){
    return this.callIdHeader;
}

SIPMessage.prototype.setCallId =function(callId){
    if(typeof callId =="object")
    {
        this.setHeader(callId);
    }
    else if(typeof callId == "string")
    {
        if (this.callIdHeader == null) {
            this.setHeader(new CallID());
        }
        this.callIdHeader.setCallId(callId);
    }
}

SIPMessage.prototype.getRecordRouteHeaders =function(){
    return this.getSIPHeaderListLowerCase(this.RECORDROUTE_LOWERCASE);
}

SIPMessage.prototype.getTo =function(){
    return this.toHeader;
}

SIPMessage.prototype.setTo =function(to){
    this.setHeader(to);
}

SIPMessage.prototype.setFrom =function(from){
    this.setHeader(from);
}
SIPMessage.prototype.getContentLength =function(){
    return this.contentLengthHeader;
}

SIPMessage.prototype.getMessageContent =function(){
    return this.messageContent;
}

SIPMessage.prototype.getRawContent =function(){
    return this.messageContent;
}

SIPMessage.prototype.setMessageContent =function(){
    if(arguments.length==1)
    {
        var content=arguments[0];
        this.computeContentLength(content);
        this.messageContent = content;
    }
    else if(arguments.length==3)
    {
        var type=arguments[0];
        var subType=arguments[1];
        var messageContent=arguments[2];
        var ct = new ContentType(type, subType);
        this.setHeader(ct);
        this.messageContent = messageContent;
        this.computeContentLength(messageContent);
    }
    else if(arguments.length==4)
    {
        content=arguments[0];
        var strict=arguments[1];
        var computeContentLength=arguments[2];
        var givenLength=arguments[3];
        this.computeContentLength(content);
        if ((!computeContentLength)) {
            if ( (!strict && this.contentLengthHeader.getContentLength() != givenLength) 
                || this.contentLengthHeader.getContentLength() < givenLength) {
                
                console.error("MessageFactoryImpl:setMessageContent(): invalid content length "+ this.contentLengthHeader.getContentLength() + " / " + givenLength, 0);
                throw "MessageFactoryImpl:setMessageContent(): invalid content length "+ this.contentLengthHeader.getContentLength() + " / " + givenLength;
            }
        }
        this.messageContent = content;
    }
}

SIPMessage.prototype.setContent =function(content, contentTypeHeader){
    if (content == null)
    {
         console.error("MessageFactoryImpl:setContent(): null content !", 0);
         throw "MessageFactoryImpl:setContent(): null content!";
    }
    this.setHeader(contentTypeHeader);
    this.messageContent = content;
    this.computeContentLength(content);
}

SIPMessage.prototype.getContent =function(){
    if (this.messageContent != null)
    {
        return this.messageContent;
    }
    else
    {
        return null;
    }
}

SIPMessage.prototype.computeContentLength =function(content){
    var length = 0;
    if (content != null) {
        if (content instanceof String) {
            length = content.length;
        }
        else if (content.constructor==Array) {
            length = content.length;
        }
        else {
            length = content.toString().length;
        }
    }
    this.contentLengthHeader.setContentLength(length);

}

SIPMessage.prototype.removeContent =function(){
    this.messageContent = null;
    this.contentLengthHeader.setContentLength(0);
}

SIPMessage.prototype.getHeaderAsFormattedString =function(name){
    var lowerCaseName = name.toLowerCase();
    var l = null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        if(this.nameTable[i][0]==lowerCaseName)
        {
            l=i;
        }
    }
    if (l!=null) 
    {
        return this.nameTable[l][1].toString();
    } 
    else 
    {
        return this.getHeader(name).toString();
    }
}

SIPMessage.prototype.getSIPHeaderListLowerCase =function(lowerCaseHeaderName){
    var l = null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        if(this.nameTable[i][0]==lowerCaseHeaderName)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        return this.nameTable[l][1];
    }
    else
    {
        return null;
    }
    
}

SIPMessage.prototype.getHeaderList =function(headerName){
    var l=null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        if(this.nameTable[i][0]==headerName.toLowerCase())
        {
            l=i;
        }
    }
    var sipHeader = this.nameTable[l][1];
    if (sipHeader == null)
    {
        return null;
    }
    else if (sipHeader instanceof SIPHeaderList)
    {
        return sipHeader.getHeaderList();
    //return (List<SIPHeader>) (((SIPHeaderList< ? >) sipHeader).getHeaderList());
    }
    else {
        var ll = new Array();
        ll.push(sipHeader);
        return ll;
    }
}

SIPMessage.prototype.hasHeader =function(headerName){
    var l=null;
    for(var i=0;i<this.nameTable.length;i++)
    {
        if(this.nameTable[i][0]==headerName.toLowerCase())
        {
            l=i;
        }
    }
    if(l!=null)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPMessage.prototype.hasFromTag =function(){
    return this.fromHeader != null && this.fromHeader.getTag() != null;
}

SIPMessage.prototype.hasToTag =function(){
    return this.toHeader != null && this.toHeader.getTag() != null;
}

SIPMessage.prototype.getFromTag =function(){
    return this.fromHeader == null ? null : this.fromHeader.getTag();
}

SIPMessage.prototype.setFromTag =function(tag){
    this.fromHeader.setTag(tag);
}

SIPMessage.prototype.setToTag =function(tag){
    this.toHeader.setTag(tag);
}

SIPMessage.prototype.getToTag =function(){
    return this.toHeader == null ? null : this.toHeader.getTag(); 
}

SIPMessage.prototype.getFirstLine =function(){

}
SIPMessage.prototype.addHeader =function(){
    if(typeof arguments[0]!="object")
    {
        var sipHeader=arguments[0];
        var hdrString = sipHeader.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '') + "\n";
        try {
            var pf=new ParserFactory();
            var parser = pf.createParser(sipHeader);
            var sh = parser.parse();
            this.attachHeader(sh, false);
        } catch (ex) {
            console.error("SIPMessage:addHeader(): catched exception:"+ex);
            this.unrecognizedHeaders.push(hdrString);
        }
    }
    else
    {
        sipHeader=arguments[0];
        sh = sipHeader;
        try {
            if ((sipHeader instanceof Via) || (sipHeader instanceof RecordRoute)){
                this.attachHeader(sh, false, true);
            } 
            else{
                this.attachHeader(sh, false, false);
            }
        } catch (ex) {
            console.error("SIPMessage:addHeader(): catched exception:"+ex);
            if (sipHeader instanceof ContentLength){
                var cl = sipHeader;
                this.contentLengthHeader.setContentLength(cl.getContentLength());
            }
        } 
    }
}

SIPMessage.prototype.addUnparsed =function(unparsed){
    this.unrecognizedHeaders.push(unparsed);
}

SIPMessage.prototype.getUnrecognizedHeaders =function(){
    return this.unrecognizedHeaders
}

SIPMessage.prototype.getHeaderNames =function(){
    var retval=new Array();
    for(var i=0;i<this.headers.length;i++)
    {
        var sipHeader = this.headers[i];
        var name = sipHeader.getName();
        retval.push(name);
    }
    return retval;
}


SIPMessage.prototype.getContentDisposition =function(){
    return this.getHeaderLowerCase(this.CONTENT_DISPOSITION_LOWERCASE)
}

SIPMessage.prototype.getExpires =function(){
    return this.getHeaderLowerCase(this.EXPIRES_LOWERCASE);
}

SIPMessage.prototype.setExpires =function(expiresHeader){
    this.setHeader(expiresHeader);
}

SIPMessage.prototype.setContentDisposition =function(contentDispositionHeader){
    this.setHeader(contentDispositionHeader);
}

SIPMessage.prototype.setContentLength =function(contentLength){
    this.contentLengthHeader.setContentLength(contentLength.getContentLength());
}

SIPMessage.prototype.setSize =function(size){
    this.size = size;
}

SIPMessage.prototype.getSize =function(){
    return this.size;
}
SIPMessage.prototype.addLast =function(header){
    if (header == null)
    {
        console.error("SIPMessage:addLast(): null header arg!");
        throw "SIPMessage:addLast(): null header arg!"
    }
    this.attachHeader(header, false, false);
}

SIPMessage.prototype.addFirst =function(header){
    if (header == null)
    {
        console.error("SIPMessage:addFirst(): null header arg!");
        throw "SIPMessage:addFirst(): null header arg!"
    }
    this.attachHeader(header, false, true);
}

SIPMessage.prototype.removeFirst =function(headerName){
    if (headerName == null)
    {
        console.error("SIPMessage:removeFirst(): null headerName arg!");
        throw "SIPMessage:removeFirst(): null headerName arg!"
    }
    this.removeHeader(headerName, true);
}

SIPMessage.prototype.removeLast =function(headerName){
    if (headerName == null)
    {
        console.error("SIPMessage:removeLast(): null headerName arg!");
        throw "SIPMessage:removeLast(): null headerName arg!"
    }
    this.removeHeader(headerName, false);
}

SIPMessage.prototype.setCSeq =function(cseqHeader){
    this.setHeader(cseqHeader);
}

SIPMessage.prototype.setApplicationData =function(applicationData){
    this.applicationData = applicationData;
}

SIPMessage.prototype.getApplicationData =function(){
    return this.applicationData;
}

SIPMessage.prototype.getMultipartMimeContent =function(){
    var retval = new MultipartMimeContentImpl(this.getContentTypeHeader());
    var rawContent = this.getRawContent();
    var body = new String(rawContent);
    retval.createContentList(body);
    return retval;
}

SIPMessage.prototype.getCallIdHeader =function(){
    return this.callIdHeader;
}

SIPMessage.prototype.getFromHeader =function(){
    return this.fromHeader;
}

SIPMessage.prototype.getToHeader =function(){
    return this.toHeader;
}

SIPMessage.prototype.getTopmostViaHeader =function(){
    return this.getTopmostVia();
}

SIPMessage.prototype.getCSeqHeader =function(){
    return this.cSeqHeader;
}

SIPMessage.prototype.getCharset =function(){
    var ct = this.getContentTypeHeader();
    if (ct!=null) {
        var c = ct.getCharset();
        return c!=null ? c : this.contentEncodingCharset;
    } 
    else {
        return this.contentEncodingCharset;
    }
}

SIPMessage.prototype.isNullRequest =function(){
    return  this.nullRequest;
}

SIPMessage.prototype.setNullRequest =function(){
    this.nullRequest = true;
}

SIPMessage.prototype.setSIPVersion =function(){
}

SIPMessage.prototype.getSIPVersion =function(){
}

SIPMessage.prototype.toString =function(){
}

SIPMessage.prototype.getBytes =function(str){
    var array=new Array();
    str=new String(str);
    for(var i=0;i<str.length;i++)
    {
        array[i]=str.charCodeAt(i);
    } 
    return array;
}

SIPMessage.prototype.getStackTransaction =function(){
    return this.stackTransaction;
}

SIPMessage.prototype.setStackTransaction =function(transaction){
    this.stackTransaction = transaction;
}

SIPMessage.prototype.addViaHeaderList =function(viaheader){
    var hn=new Array();
    var n=0;
    for(var i=0;i<this.headers.length;i++)
    {            
        hn[n]=this.headers[i];
        n=n+1;
        if ((this.headers[i] instanceof ViaList)) {
            if(i==this.headers.length-1||!(this.headers[i] instanceof ViaList))
            {
                hn[n]=viaheader;
                n=n+1;
            }
            if(!(this.headers[i-1] instanceof ViaList)&&i!=0)
            {
                var array=new Array();
                array[0]=this.headers[i].getName().toLowerCase();
                array[1]=this.headers[i];
                this.nameTable.push(array);
            }
        }
    }
    if(n==i)
    {
        hn[n]=viaheader;
        array=new Array();
        array[0]=viaheader.getName().toLowerCase();
        array[1]=viaheader;
        this.nameTable.push(array);
    }
    this.headers=hn;
}/*
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
 *  Implementation of the JAIN-SIP MessageFactoryImpl .
 *  @see  gov/nist/javax/sip/message/MessageFactoryImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function MessageFactoryImpl() {
    this.classname="MessageFactoryImpl";
    this.testing = false;
    this.strict  = true;
    this.defaultContentEncodingCharset = "UTF-8"
    this.userAgent=null;
    this.server=null;
    if(arguments.length!=0)
    {
        this.listeningpoint=arguments[0];
        this.viaheader=this.listeningpoint.getViaHeader();
    }
}

MessageFactoryImpl.prototype.CONTENTTYPEHEADER="Content-Type";

MessageFactoryImpl.prototype.setStrict =function(){
    
}

MessageFactoryImpl.prototype.setTest =function(){
    
}

MessageFactoryImpl.prototype.createRequest =function(){
    if(arguments.length==1)
    {
        var requestString = arguments[0];
        return this.createRequestPrototype5(requestString);
    }
    else if(arguments.length==7)
    {
        var requestURI = arguments[0];
        var method = arguments[1];
        var callId = arguments[2];
        var cSeq = arguments[3];
        var from = arguments[4];
        var to = arguments[5];
        var maxForwards = arguments[6];
        var via=this.viaheader;
        return this.createRequestPrototype3(requestURI, method, callId, cSeq, from, to, via, maxForwards);
    }
    else if(arguments.length==9)
    {
        if(arguments[7].classname!="ContentType")
        {
            requestURI = arguments[0];
            method = arguments[1];
            callId = arguments[2];
            cSeq = arguments[3];
            from = arguments[4];
            to = arguments[5];
            via=this.viaheader;
            maxForwards = arguments[6];
            var content = arguments[7];
            var contentType = arguments[8];
            return this.createRequestPrototype2(requestURI, method, callId, cSeq, from, to, via, maxForwards, content, contentType);
        }
        else if(arguments[8].constructor!=Array)
        {
            requestURI = arguments[0];
            method = arguments[1];
            callId = arguments[2];
            cSeq = arguments[3];
            from = arguments[4];
            to = arguments[5];
            via=this.viaheader;
            maxForwards = arguments[6];
            contentType = arguments[7];
            content = arguments[8];
            return this.createRequestPrototype4(requestURI, method, callId, cSeq, from, to, via, maxForwards, contentType, content);
        }
        else if(arguments[8].constructor==Array)
        {
            requestURI = arguments[0];
            method = arguments[1];
            callId = arguments[2];
            cSeq = arguments[3];
            from = arguments[4];
            to = arguments[5];
            via=this.viaheader;
            maxForwards = arguments[6];
            contentType = arguments[7];
            content = arguments[8];
            return this.createRequestPrototype1(requestURI, method, callId, cSeq, from, to, via, maxForwards, contentType, content);
        }
    }
}


MessageFactoryImpl.prototype.createRequestPrototype1 =function(requestURI,method,callId,cSeq,from,to,via,maxForwards,contentType,content){
    
    if (requestURI == null || method == null || callId == null
        || cSeq == null || from == null || to == null || via == null
        || maxForwards == null || content == null
        || contentType == null)
        {
        console.error("MessageFactoryImpl:createRequestPrototype1(): some parameters are missing, unable to create the request");
        throw "MessageFactoryImpl:createRequestPrototype1(): some parameters are missing, unable to create the request";
    }
    
    var sipRequest = new SIPRequest();
    sipRequest.setRequestURI(requestURI);
    sipRequest.setMethod(method);
    sipRequest.setCallId(callId);
    sipRequest.setCSeq(cSeq);
    sipRequest.setFrom(from);
    sipRequest.setTo(to);
    sipRequest.setVia(via);
    sipRequest.setMaxForwards(maxForwards);
    sipRequest.setContent(content, contentType);
    if (this.userAgent != null ) {
        sipRequest.setHeader(this.userAgent);
    }
    return sipRequest;
}


MessageFactoryImpl.prototype.createRequestPrototype2 =function(requestURI,method,callId,cSeq,from,to,via,maxForwards,content,contentType){
    
    if (requestURI == null || method == null || callId == null
        || cSeq == null || from == null || to == null || via == null
        || maxForwards == null || content == null
        || contentType == null)
        {
        console.error("MessageFactoryImpl:createRequestPrototype2(): some parameters are missing, unable to create the request");
        throw "MessageFactoryImpl:createRequestPrototype2(): some parameters are missing, unable to create the request";
    }

    var sipRequest = new SIPRequest();
    sipRequest.setRequestURI(requestURI);
    sipRequest.setMethod(method);
    sipRequest.setCallId(callId);
    sipRequest.setCSeq(cSeq);
    sipRequest.setFrom(from);
    sipRequest.setTo(to);
    sipRequest.setVia(via);
    sipRequest.setMaxForwards(maxForwards);
    sipRequest.setHeader(contentType);
    sipRequest.setMessageContent(content);
    if (this.userAgent != null ) {
        sipRequest.setHeader(this.userAgent);
    }
    return sipRequest;
}

MessageFactoryImpl.prototype.createRequestPrototype3 =function(requestURI,method,callId,cSeq,from,to,via,maxForwards){
    
    if (requestURI == null || method == null || callId == null
        || cSeq == null || from == null || to == null || via == null
        || maxForwards == null)
        {
        console.error("MessageFactoryImpl:createRequestPrototype3(): some parameters are missing, unable to create the request");
        throw "MessageFactoryImpl:createRequestPrototype3(): some parameters are missing, unable to create the request";
    }
    var sipRequest = new SIPRequest();
    sipRequest.setRequestURI(requestURI);
    sipRequest.setMethod(method);
    sipRequest.setCallId(callId);
    sipRequest.setCSeq(cSeq);
    sipRequest.setFrom(from);
    sipRequest.setTo(to);
    sipRequest.setVia(via);
    sipRequest.setMaxForwards(maxForwards);
    if (this.userAgent != null) {
        sipRequest.setHeader(this.userAgent);
    }
    return sipRequest;
}

MessageFactoryImpl.prototype.createRequestPrototype4 =function(requestURI,method,callId,cSeq,from,to,via,maxForwards,contentType,content){
    
    if (requestURI == null || method == null || callId == null
        || cSeq == null || from == null || to == null || via == null
        || maxForwards == null || content == null
        || contentType == null)
        {
        console.error("MessageFactoryImpl:createRequestPrototype4(): some parameters are missing, unable to create the request");
        throw "MessageFactoryImpl:createRequestPrototype4(): some parameters are missing, unable to create the request";
    }
    
    var sipRequest = new SIPRequest();
    sipRequest.setRequestURI(requestURI);
    sipRequest.setMethod(method);
    sipRequest.setCallId(callId);
    sipRequest.setCSeq(cSeq);
    sipRequest.setFrom(from);
    sipRequest.setTo(to);
    sipRequest.setVia(via);
    sipRequest.setMaxForwards(maxForwards);
    sipRequest.setContent(content, contentType);
    if (this.userAgent != null) {
        sipRequest.setHeader(this.userAgent);
    }
    return sipRequest;
}


MessageFactoryImpl.prototype.createRequestPrototype5 =function(requestString){
    if (requestString == null || requestString.equals("")) {
        var retval = new SIPRequest();
        retval.setNullRequest();
        return retval;
    }
    var smp = new StringMsgParser();
    smp.setStrict(this.strict);
    var sipMessage = smp.parseSIPMessage(requestString);
    if (!(sipMessage instanceof SIPRequest))
    {
        console.error("MessageFactoryImpl:createRequestPrototype5(): parsing error");
        throw "MessageFactoryImpl:createRequestPrototype5(): parsing error";
    }
    return  sipMessage;
}


MessageFactoryImpl.prototype.createResponse =function(){
    if(arguments.length==1)
    {
        var responseString=arguments[0];
        return this.createReponsePrototype9(responseString);
    }
    else if(arguments.length==2)
    {
        var statusCode=arguments[0];
        var request=arguments[1];
        return this.createReponsePrototype6(statusCode, request);
    }
    else if(arguments.length==4)
    {
        if(arguments[3].constructor!=Array)
        {
            statusCode=arguments[0];
            request=arguments[1];
            var contentType=arguments[2];
            var content=arguments[3];
            return this.createReponsePrototype4(statusCode, request, contentType, content);
        }
        else if(arguments[3].constructor==Array)
        {
            statusCode=arguments[0];
            request=arguments[1];
            contentType=arguments[2];
            content=arguments[3];
            return this.createReponsePrototype5(statusCode, request, contentType, content);
        }
    }
    else if(arguments.length==7)
    {
        statusCode=arguments[0];
        var callId=arguments[1];
        var cSeq=arguments[2];
        var from=arguments[3];
        var to=arguments[4];
        var via=arguments[5];
        var maxForwards=arguments[6];
        return this.createReponsePrototype3(statusCode, callId, cSeq, from, to, via, maxForwards)
    }
    else if(arguments.length==9)
    {
        if(arguments[8].classname=="ContentType")
        {
            if(arguments[7].constructor!=Array)
            {
                statusCode=arguments[0];
                callId=arguments[1];
                cSeq=arguments[2];
                from=arguments[3];
                to=arguments[4];
                via=arguments[5];
                maxForwards=arguments[6];
                content=arguments[7];
                contentType=arguments[8];
                return this.createReponsePrototype1(statusCode, callId, cSeq, from, to, via, maxForwards, content, contentType)
            }
            else if(arguments[7].constructor==Array)
            {
                statusCode=arguments[0];
                callId=arguments[1];
                cSeq=arguments[2];
                from=arguments[3];
                to=arguments[4];
                via=arguments[5];
                maxForwards=arguments[6];
                content=arguments[7];
                contentType=arguments[8];
                return this.createReponsePrototype2(statusCode, callId, cSeq, from, to, via, maxForwards, content, contentType)
            }
        }
        else if(arguments[8].classname!="ContentType")
        {
            if(arguments[7].constructor!=Array)
            {
                statusCode=arguments[0];
                callId=arguments[1];
                cSeq=arguments[2];
                from=arguments[3];
                to=arguments[4];
                via=arguments[5];
                maxForwards=arguments[6];
                contentType=arguments[7];
                content=arguments[8];
                return this.createReponsePrototype7(statusCode, callId, cSeq, from, to, via, maxForwards, contentType, content)
            }
            else if(arguments[7].constructor==Array)
            {
                statusCode=arguments[0];
                callId=arguments[1];
                cSeq=arguments[2];
                from=arguments[3];
                to=arguments[4];
                via=arguments[5];
                maxForwards=arguments[6];
                contentType=arguments[7];
                content=arguments[8];
                return this.createReponsePrototype8(statusCode, callId, cSeq, from, to, via, maxForwards, contentType, content)
            }
        }
    }
}

MessageFactoryImpl.prototype.createReponsePrototype1 =function(statusCode, callId, cSeq, from, to, via, maxForwards, content, contentType){
    
    if (callId == null || cSeq == null || from == null || to == null
        || via == null || maxForwards == null || content == null
        || contentType == null)
        {
        console.error("MessageFactoryImpl:createReponsePrototype1(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype1(): some parameters are missing, unable to create the response";
    }
    
    var sipResponse = new SIPResponse();
    var statusLine = new StatusLine();
    statusLine.setStatusCode(statusCode);
    var reasonPhrase = sipResponse.getReasonPhrase(statusCode);
    //if (reasonPhrase == null)
    //  throw new ParseException(statusCode + " Unkown  ", 0);
    statusLine.setReasonPhrase(reasonPhrase);
    sipResponse.setStatusLine(statusLine);
    sipResponse.setCallId(callId);
    sipResponse.setCSeq(cSeq);
    sipResponse.setFrom(from);
    sipResponse.setTo(to);
    sipResponse.setVia(via);
    sipResponse.setMaxForwards(maxForwards);
    sipResponse.setContent(content, contentType);
    if (this.userAgent != null) {
        sipResponse.setHeader(this.userAgent);
    }
    return sipResponse;
}

MessageFactoryImpl.prototype.createReponsePrototype2 =function(statusCode, callId, cSeq, from, to, via, maxForwards, content, contentType){
    if (callId == null || cSeq == null || from == null || to == null
        || via == null || maxForwards == null || content == null
        || contentType == null)
        {
        console.error("MessageFactoryImpl:createReponsePrototype2(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype2(): some parameters are missing, unable to create the response";
    }
    //i don't know why there is no reason phrase in this function
    var sipResponse = new SIPResponse();
    sipResponse.setStatusCode(statusCode);
    sipResponse.setCallId(callId);
    sipResponse.setCSeq(cSeq);
    sipResponse.setFrom(from);
    sipResponse.setTo(to);
    sipResponse.setVia(via);
    sipResponse.setMaxForwards(maxForwards);
    sipResponse.setHeader(contentType);
    sipResponse.setMessageContent(content);
    if (this.userAgent != null) {
        sipResponse.setHeader(this.userAgent);
    }
    return sipResponse;
}

MessageFactoryImpl.prototype.createReponsePrototype3 =function(statusCode,callId,cSeq,from,to,via,maxForwards){
    if (callId == null || cSeq == null || from == null || to == null
        || via == null || maxForwards == null)
        {
        console.error("MessageFactoryImpl:createReponsePrototype3(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype3(): some parameters are missing, unable to create the response";
    }
    var sipResponse = new SIPResponse();
    sipResponse.setStatusCode(statusCode);
    sipResponse.setCallId(callId);
    sipResponse.setCSeq(cSeq);
    sipResponse.setFrom(from);
    sipResponse.setTo(to);
    sipResponse.setVia(via);
    sipResponse.setMaxForwards(maxForwards);
    if (this.userAgent != null) {
        sipResponse.setHeader(this.userAgent);
    }
    return sipResponse;
}


MessageFactoryImpl.prototype.createReponsePrototype4 =function(statusCode,request,contentType,content){
    if (request == null || content == null || contentType == null)
    {
        console.error("MessageFactoryImpl:createReponsePrototype4(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype4(): some parameters are missing, unable to create the response";
    }
    var sipRequest =  request;
    var sipResponse = sipRequest.createResponse(statusCode);
    sipResponse.setContent(content, contentType);
    if (this.server != null) {
        sipResponse.setHeader(this.server);
    }
    return sipResponse;
}


MessageFactoryImpl.prototype.createReponsePrototype5 =function(statusCode,request,contentType,content){
    if (request == null || content == null || contentType == null)
    {
        console.error("MessageFactoryImpl:createReponsePrototype5(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype5(): some parameters are missing, unable to create the response";
    }
    var sipRequest =  request;
    var sipResponse = sipRequest.createResponse(statusCode);
    sipResponse.setHeader(contentType);
    sipResponse.setMessageContent(content);
    if (this.server != null) {
        sipResponse.setHeader(this.server);
    }
    return sipResponse;
}


MessageFactoryImpl.prototype.createReponsePrototype6 =function(statusCode,request){
    if (request == null || content == null || contentType == null)
    {
        console.error("MessageFactoryImpl:createReponsePrototype6(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype6(): ome parameters are missing, unable to create the response";
    }
    var sipRequest = request;
    var sipResponse = sipRequest.createResponse(statusCode);
    sipResponse.removeContent();
    sipResponse.removeHeader(this.CONTENTTYPEHEADER);
    if (this.server != null) {
        sipResponse.setHeader(this.server);
    }
    return sipResponse;
}


MessageFactoryImpl.prototype.createReponsePrototype7 =function(statusCode, callId, cSeq, from, to, via, maxForwards, contentType, content){
    if (callId == null || cSeq == null || from == null || to == null
        || via == null || maxForwards == null || content == null
        || contentType == null)
       {
        console.error("MessageFactoryImpl:createReponsePrototype7(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype7(): some parameters are missing, unable to create the response";
    }
    var sipResponse = new SIPResponse();
    var statusLine = new StatusLine();
    statusLine.setStatusCode(statusCode);
    var reason = sipResponse.getReasonPhrase(statusCode);
    if (reason == null)
    {
        console.error("MessageFactoryImpl:createReponsePrototype7(): unknown statusCode "+ statusCode);
        throw "MessageFactoryImpl:createReponsePrototype7(): unknown statusCode "+ statusCode;
    }
    statusLine.setReasonPhrase(reason);
    sipResponse.setStatusLine(statusLine);
    sipResponse.setCallId(callId);
    sipResponse.setCSeq(cSeq);
    sipResponse.setFrom(from);
    sipResponse.setTo(to);
    sipResponse.setVia(via);
    sipResponse.setContent(content, contentType);
    if (this.userAgent != null) {
        sipResponse.setHeader(this.userAgent);
    }
    return sipResponse;
}


MessageFactoryImpl.prototype.createReponsePrototype8 =function(statusCode, callId, cSeq, from, to, via, maxForwards, contentType, content){

    if (callId == null || cSeq == null || from == null || to == null
        || via == null || maxForwards == null || content == null
        || contentType == null)
        {
         console.error("MessageFactoryImpl:createReponsePrototype8(): some parameters are missing, unable to create the response");
        throw "MessageFactoryImpl:createReponsePrototype8(): some parameters are missing, unable to create the response";
    }
    var sipResponse = new SIPResponse();
    var statusLine = new StatusLine();
    statusLine.setStatusCode(statusCode);
    var reason = sipResponse.getReasonPhrase(statusCode);
    if (reason == null)
    {
        console.error("MessageFactoryImpl:createReponsePrototype8(): unknown statusCode "+ statusCode);
        throw "MessageFactoryImpl:createReponsePrototype8(): unknown statusCode "+ statusCode;
    }
    statusLine.setReasonPhrase(reason);
    sipResponse.setStatusLine(statusLine);
    sipResponse.setCallId(callId);
    sipResponse.setCSeq(cSeq);
    sipResponse.setFrom(from);
    sipResponse.setTo(to);
    sipResponse.setVia(via);
    sipResponse.setContent(content, contentType);
    if (this.userAgent != null) {
        sipResponse.setHeader(this.userAgent);
    }
    return sipResponse;
}

MessageFactoryImpl.prototype.createReponsePrototype9 =function(responseString){
    if (responseString == null)
    {
        return new SIPResponse();
    }
    var smp = new StringMsgParser();
    var sipMessage = smp.parseSIPMessage(responseString);
    if (!(sipMessage instanceof SIPResponse))
    {
        console.error("MessageFactoryImpl:createReponsePrototype9(): parse error");
        throw "MessageFactoryImpl:createReponsePrototype9(): parse error";
    }
    return sipMessage;
}

MessageFactoryImpl.prototype.setDefaultUserAgentHeader =function(userAgent){
    this.userAgent = userAgent;
}

MessageFactoryImpl.prototype.setDefaultServerHeader =function(server){
    this.server = server;
}

MessageFactoryImpl.prototype.getDefaultUserAgentHeader =function(){
    return this.userAgent;
}

MessageFactoryImpl.prototype.getDefaultServerHeader =function(){
    return this.server;
}

MessageFactoryImpl.prototype.setDefaultContentEncodingCharset =function(charset){
    if (charset == null ) {
        throw new NullPointerException ("Null argument!");
    }
    this.defaultContentEncodingCharset = charset;
}

MessageFactoryImpl.prototype.getDefaultContentEncodingCharset =function(){
    return this.defaultContentEncodingCharset;
}

MessageFactoryImpl.prototype.createMultipartMimeContent =function(multipartMimeCth,contentType,
    contentSubtype,contentBody){
    var boundary = multipartMimeCth.getParameter("boundary");
    var retval = new MultipartMimeContentImpl(multipartMimeCth);
    for (var i = 0 ;  i < contentType.length; i++ ) {
        var cth = new ContentType(contentType[i],contentSubtype[i]);
        var contentImpl  = new ContentImpl(contentBody[i],boundary);
        contentImpl.setContentTypeHeader(cth);
        retval.add(contentImpl);
    }
    return retval;
}

MessageFactoryImpl.prototype.getBytes =function(str){
    var array=new Array();
    str=new String(str);
    for(var i=0;i<str.length;i++)
    {
        array[i]=str.charCodeAt(i);
    }
    return array;
}

MessageFactoryImpl.prototype.addHeader =function(sipmessage,header){
    sipmessage.addHeader(header);
    return sipmessage;
}

MessageFactoryImpl.prototype.setNewViaHeader =function(sipmessage){
    this.viaheader=this.listeningpoint.getViaHeader();
    if(sipmessage instanceof SIPRequest)
    {
        var newmessage = new SIPRequest();
        newmessage.setMethod(sipmessage.getMethod());
        newmessage.setRequestURI(sipmessage.getRequestURI());
    }
    else
    {
        newmessage = new SIPResponse();
    }
    
    var headerlist=sipmessage.getHeaders();
    for(var i=0;i<headerlist.length;i++)
    {
        newmessage.addHeader(headerlist[i]);
    }
    
    if(sipmessage.getCSeq()!=null && sipmessage.getCallId()!=null && sipmessage.getViaHeaders()!=null
        && sipmessage.getFrom()!=null && sipmessage.getTo()!=null && sipmessage.getMaxForwards()!=null)
        {
        newmessage.setCSeq(sipmessage.getCSeq());
        newmessage.setCallId(sipmessage.getCallId());
        newmessage.setVia(this.viaheader);
        newmessage.setFrom(sipmessage.getFrom());
        newmessage.setTo(sipmessage.getTo());
        newmessage.setMaxForwards(sipmessage.getMaxForwards());
        if(sipmessage.getContent()!=null)
        {
            var content=sipmessage.getContent();
            var contentType=sipmessage.getContentTypeHeader();
            newmessage.setContent(content, contentType);
        }
    }
    
    return newmessage;
}/*
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
 *  Implementation of the JAIN-SIP SIPRequest .
 *  @see  gov/nist/javax/sip/message/SIPRequest.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function SIPRequest() {
    this.classname="SIPRequest";
    this.serialVersionUID = "3360720013577322927L";
    this.transactionPointer=null;
    this.requestLine = new RequestLine();
    this.messageChannel=null;
    this.inviteTransaction=null;
    this.targetRefreshMethods = new Array();
    this.nameTable = new Array();
    this.unrecognizedHeaders= new Array();
    this.headers=new Array();
    this.attachHeader(new ContentLength(0), false);
    
    this.targetRefreshMethods.push(this.INVITE);
    this.targetRefreshMethods.push(this.UPDATE);
    this.targetRefreshMethods.push(this.SUBSCRIBE);
    this.targetRefreshMethods.push(this.NOTIFY);
    this.targetRefreshMethods.push(this.REFER);
    
    this.putName(this.INVITE);
    this.putName(this.BYE);
    this.putName(this.CANCEL);
    this.putName(this.ACK);
    this.putName(this.PRACK);
    this.putName(this.INFO);
    this.putName(this.MESSAGE);
    this.putName(this.NOTIFY);
    this.putName(this.OPTIONS);
    this.putName(this.PRACK);
    this.putName(this.PUBLISH);
    this.putName(this.REFER);
    this.putName(this.REGISTER);
    this.putName(this.SUBSCRIBE);
    this.putName(this.UPDATE);
    
}

SIPRequest.prototype = new SIPMessage();
SIPRequest.prototype.constructor=SIPRequest;
SIPRequest.prototype.DEFAULT_USER="ip";
SIPRequest.prototype.DEFAULT_TRANSPORT="tcp";
SIPRequest.prototype.INVITE="INVITE";
SIPRequest.prototype.BYE="BYE";
SIPRequest.prototype.CANCEL="CANCEL";
SIPRequest.prototype.ACK="ACK";
SIPRequest.prototype.PRACK="PRACK";
SIPRequest.prototype.INFO="INFO";
SIPRequest.prototype.MESSAGE="MESSAGE";
SIPRequest.prototype.NOTIFY="NOTIFY";
SIPRequest.prototype.OPTIONS="OPTIONS";
SIPRequest.prototype.PUBLISH="PUBLISH";
SIPRequest.prototype.REFER="REFER";
SIPRequest.prototype.REGISTER="REGISTER";
SIPRequest.prototype.SUBSCRIBE="SUBSCRIBE";
SIPRequest.prototype.UPDATE="UPDATE";
SIPRequest.prototype.CSeqHeader="CSeq";
SIPRequest.prototype.ToHeader="To";
SIPRequest.prototype.CallIdHeader="Call-ID";
SIPRequest.prototype.FromHeader="From";
SIPRequest.prototype.ViaHeader="Via";
SIPRequest.prototype.MaxForwardsHeader="Max-Forwards";
SIPRequest.prototype.EventHeader="Event";
SIPRequest.prototype.ContactHeader="Contact";
SIPRequest.prototype.COLON=":";
//SIPRequest.prototype.UPDATE="UPDATE";

SIPRequest.prototype.putName =function(name){
    var array=new Array();
    array[0]=name;
    array[1]=name;
    this.nameTable.push(array);
}

SIPRequest.prototype.isTargetRefresh =function(ucaseMethod){
    var x=null;
    for(var i=0;i<this.targetRefreshMethods;i++)
    {
        if(this.targetRefreshMethods[i]==ucaseMethod)
        {
            x=1;
        }
    }
    if(x!=null)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPRequest.prototype.isDialogCreating =function(ucaseMethod){
    var siptranstack=new SIPTransactionStack();
    return siptranstack.isDialogCreated(ucaseMethod);
}

SIPRequest.prototype.getCannonicalName =function(method){
    var x=null;
    for(var i=0;i<this.nameTable;i++)
    {
        if(this.nameTable[i][0]==method)
        {
            x=i;
        }
    }
    if(x!=null)
    {
        return this.nameTable[x][1];
    }
    else
    {
        return method;
    }
}

SIPRequest.prototype.getRequestLine =function(){
    return this.requestLine;
}

SIPRequest.prototype.setRequestLine =function(requestLine){
    this.requestLine = requestLine;
}

SIPRequest.prototype.checkHeaders =function(){
    var prefix = "Missing a required header : ";

    if (this.getCSeq() == null) {
        console.error("SIPRequest:checkHeaders(): "+prefix + this.CSeqHeader);
        throw "SIPRequest:checkHeaders(): "+prefix + this.CSeqHeader;
    }
    if (this.getTo() == null) {
        console.error("SIPRequest:checkHeaders(): "+prefix + this.ToHeader);
        throw "SIPRequest:checkHeaders(): "+prefix + this.ToHeader;
    }
    if (this.callIdHeader == null || this.callIdHeader.getCallId() == null
        || this.callIdHeader.getCallId()=="") {
        console.error("SIPRequest:checkHeaders(): "+prefix + this.CallIdHeader);
        throw "SIPRequest:checkHeaders(): "+prefix + this.CallIdHeader;
    }
    if (this.getFrom() == null) {
        console.error("SIPRequest:checkHeaders(): "+prefix + this.FromHeader);
        throw "SIPRequest:checkHeaders(): "+prefix + this.FromHeader;
    }
    if (this.getViaHeaders() == null) {
        console.error("SIPRequest:checkHeaders(): "+prefix + this.ViaHeader);
        throw "SIPRequest:checkHeaders(): "+prefix + this.ViaHeader;
    }
    if (this.getMaxForwards() == null) {
        console.error("SIPRequest:checkHeaders(): "+prefix + this.MaxForwardsHeader);
        throw "SIPRequest:checkHeaders(): "+prefix + this.MaxForwardsHeader;
    }
    if (this.getTopmostVia() == null){
        console.error("SIPRequest:checkHeaders(): no via header in request!");
        throw "SIPRequest:checkHeaders():  no via header in request!";
    }
    
    if (this.getMethod()==this.NOTIFY) {
        /*if (getHeader(SubscriptionStateHeader.NAME) == null){
            console.error(prefix + SubscriptionStateHeader.NAME, 0);
        }*/
        if (getHeader(this.EventHeader) == null){
            console.error("SIPRequest:checkHeaders(): "+prefix + this.EventHeader);
            throw "SIPRequest:checkHeaders(): "+prefix + this.EventHeader;
        }
    } else if (this.getMethod()==this.PUBLISH) {
        if (this.getHeader(this.EventHeader) == null)
            console.error("SIPRequest:checkHeaders(): "+prefix + this.EventHeader);
            throw "SIPRequest:checkHeaders(): "+prefix + this.EventHeader;
    }
    if (this.requestLine.getMethod()==this.INVITE
        || this.requestLine.getMethod()==this.SUBSCRIBE
        || this.requestLine.getMethod()==this.REFER) {
        if (this.getContactHeader() == null) {
            // Make sure this is not a target refresh. If this is a target
            // refresh its ok not to have a contact header. Otherwise
            // contact header is mandatory.
            if (this.getToTag() == null){
                console.error("SIPRequest:checkHeaders(): "+prefix + this.ContactHeader);
                throw "SIPRequest:checkHeaders(): "+prefix + this.ContactHeader;
            }
        }
        if (this.requestLine.getUri() instanceof SipUri) {
            var scheme = (this.requestLine.getUri()).getScheme();
            if ("sips"==scheme.toLowerCase()) {
                var sipUri = this.getContactHeader().getAddress().getURI();
                if (sipUri.getScheme()!="sips") {
                    console.error("SIPRequest:checkHeaders(): scheme for contact should be sips:" + sipUri);
                    throw "SIPRequest:checkHeaders(): scheme for contact should be sips:" + sipUri;
                }
            }
        }
    }
    
    /*
         * Contact header is mandatory for a SIP INVITE request.
         */
    if (this.getContactHeader() == null&& (this.getMethod()==this.INVITE
        || this.getMethod()==this.REFER || this.getMethod()==this.SUBSCRIBE)) {
        console.error("SIPRequest:checkHeaders(): Contact Header is Mandatory for a SIP INVITE");
        throw "SIPRequest:checkHeaders(): Scheme for contact should be sips:" + sipUri;
    }

    if (this.requestLine != null && this.requestLine.getMethod() != null
        && this.getCSeq().getMethod() != null
        && this.requestLine.getMethod()!= this.getCSeq().getMethod()) {
        console.error("SIPRequest:checkHeaders(): CSEQ method mismatch with  Request-Line");
        throw "SIPRequest:checkHeaders(): CSEQ method mismatch with  Request-Line";
    }
}


SIPRequest.prototype.setDefaults =function(){
    if (this.requestLine == null)
    {
        return;
    }
    var method = this.requestLine.getMethod();
    if (method == null)
    {
        return;
    }
    var u = this.requestLine.getUri();
    if (u == null)
    {
        return;
    }
    if (method == this.REGISTER || method == this.INVITE) {
        if (u instanceof SipUri) {
            var sipUri = u;
            sipUri.setUserParam(this.DEFAULT_USER);
            sipUri.setTransportParam(this.DEFAULT_TRANSPORT);
        }
    }
}

SIPRequest.prototype.setRequestLineDefaults =function(){
    var method = this.requestLine.getMethod();
    if (method == null) {
        var cseq =  this.getCSeq();
        if (cseq != null) {
            method = this.getCannonicalName(cseq.getMethod());
            this.requestLine.setMethod(method);
        }
    }
}

SIPRequest.prototype.getRequestURI =function(){
    if (this.requestLine == null)
    {
        return null;
    }
    else
    {
        return  this.requestLine.getUri();
    }
}

SIPRequest.prototype.setRequestURI =function(uri){
    if ( uri == null ) {
        console.error("SIPRequest:setRequestURI(): null request URI");
        throw "SIPRequest:setRequestURI(): null request URI";
    }
    if (this.requestLine == null) {
        this.requestLine = new RequestLine();
    }
    this.requestLine.setUri(uri);
    this.nullRequest = false;
}

SIPRequest.prototype.setMethod =function(method){
    if (method == null)
    {
        console.error("SIPRequest:setMethod(): null method");
        throw "SIPRequest:setMethod(): null method";
    }
    if (this.requestLine == null) {
        this.requestLine = new RequestLine();
    }
    var meth = this.getCannonicalName(method);
    this.requestLine.setMethod(meth);

    if (this.cSeqHeader != null) {
        this.cSeqHeader.setMethod(meth);
    }
}

SIPRequest.prototype.getMethod =function(){
    if (this.requestLine == null)
    {
        return null;
    }
    else
    {
        return this.requestLine.getMethod();
    }
}

SIPRequest.prototype.encode =function(){
    var retval=null;
    if (this.requestLine != null) {
        this.setRequestLineDefaults();
        retval = this.requestLine.encode() + this.superencode();
    } else if (this.isNullRequest()) {
        retval = "\r\n\r\n";
    } else {       
        retval = this.superencode();
    }
    return retval;
}

SIPRequest.prototype.encodeMessage =function(){
    var retval;
    if (this.requestLine != null) 
    {
        this.setRequestLineDefaults();
        retval = this.requestLine.encode() + this.encodeSIPHeaders();
    } 
    else if (this.isNullRequest()) 
    {
        retval = "\r\n\r\n";
    } 
    else
    {
        retval = this.encodeSIPHeaders();
    }
    return retval;
}

SIPRequest.prototype.toString =function(){
    return this.encode();
}

SIPRequest.prototype.superencode =function(){
    var encoding = "";
    for(var i=0;i<this.headers.length;i++)
    {
        var siphdr = this.headers[i];
        if (!(siphdr instanceof ContentLength))
        {
            encoding=encoding+siphdr.encode();
        }
    }
    for(i=0;i<this.unrecognizedHeaders.length;i++)
    {
        var unrecognized=this.unrecognizedHeaders[i];
        encoding=encoding+unrecognized+this.NEWLINE;
    }
    encoding=encoding+this.contentLengthHeader.encode()+this.NEWLINE;
    if (this.messageContentObject != null) 
    {
        var mbody = this.getContent().toString();
        encoding=encoding+mbody;
    }
    else if (this.messageContent != null || this.messageContentBytes != null) {
        var content = "";
        if (this.messageContent != null)
        {
            content = this.messageContent;
        }
        else 
        {
            for(i=0;i<this.messageContentBytes.length;i++)
            {
                content=content+this.messageContentBytes[i];
            }
        }
        encoding=encoding+content;
    }
    return encoding.toString();
}

SIPRequest.prototype.getMessageAsEncodedStrings =function(){
    var retval = Object.getPrototypeOf(this).getMessageAsEncodedStrings();
    if (this.requestLine != null) {
        this.setRequestLineDefaults();
        retval.addFirst(this.requestLine.encode());
    }
    return retval;
}

SIPRequest.prototype.getDialogId =function(){
    if(arguments.length==1)
    {
        var isServer=arguments[0];
        return this.getDialogIdargu1(isServer);
    }
    else if(arguments.length==2)
    {
        isServer=arguments[0];
        var toTag=arguments[1];
        return this.getDialogIdargu2(isServer, toTag);
    }
}

SIPRequest.prototype.getDialogIdargu1 =function(isServer){
    var cid = this.getCallId();
    var retval = cid.getCallId();
    var from = this.getFrom();
    var to = this.getTo();
    if (!isServer) {
        if (from.getTag() != null) {
            retval=retval+this.COLON+from.getTag();
        }
        if (to.getTag() != null) {
            retval=retval+this.COLON+to.getTag();
        }
    } 
    else {
        if (to.getTag() != null) {
            retval=retval+this.COLON+to.getTag();
        }
        if (from.getTag() != null) {
            retval=retval+this.COLON+from.getTag();
        }
    }
    return retval.toString().toLowerCase();
}

SIPRequest.prototype.getDialogIdargu2 =function(isServer, toTag){
    var from =  this.getFrom();
    var cid =  this.getCallId();
    var retval = cid.getCallId();
    if (!isServer) {
        if (from.getTag() != null) {
            retval=retval+this.COLON+from.getTag();
        }
        if (toTag != null) {
            retval=retval+this.COLON+toTag;
        }
    } else {
        if (toTag != null) {
            retval=retval+this.COLON+toTag;
        }
        if (from.getTag() != null) {
            retval=retval+this.COLON+from.getTag();
        }
    }
    return retval.toString().toLowerCase();
}

SIPRequest.prototype.createResponse =function(){
    if(arguments.length==1)
    {
        var statusCode=arguments[0];
        return this.createResponseargu1(statusCode);
    }
    else if(arguments.length==2)
    {
        statusCode=arguments[0];
        var reasonPhrase=arguments[1];
        return this.createResponseargu2(statusCode, reasonPhrase);
    }
    else if(arguments.length==3)
    {
        statusCode=arguments[0];
        reasonPhrase=arguments[1];
        var requestsent=arguments[2];
        var response=this.createResponseargu2(statusCode, reasonPhrase);
        return this.createResponseargu3(response,requestsent);
    }
}

SIPRequest.prototype.createResponseargu1 =function(statusCode){
    var sipresponse=new SIPResponse();
    var reasonPhrase = sipresponse.getReasonPhrase(statusCode);
    return this.createResponseargu2(statusCode, reasonPhrase);
}

SIPRequest.prototype.createResponseargu2 =function(statusCode,reasonPhrase){
    var newResponse= new SIPResponse();
    var headerIterator=null;
    var nextHeader=null;
    try {
        newResponse.setStatusCode(statusCode);
    } catch (ex) {
        console.error("SIPRequest:createResponseargu2(): bad code " + statusCode);
        throw "SIPRequest:createResponseargu2(): bad code " + statusCode;
    }
    if (reasonPhrase != null)
    {
        newResponse.setReasonPhrase(reasonPhrase);
    }
    else
    {
        var sipresponse=new SIPResponse();
        newResponse.setReasonPhrase(sipresponse.getReasonPhrase(statusCode));
    }
    headerIterator = this.getHeaders();
    for(var i=0;i<headerIterator.length;i++)
    {
        nextHeader = headerIterator[i];
        if(nextHeader instanceof To)
        {
            if(!nextHeader.hasTag())
            {
                var date=new Date();
                var tag=date.getTime();
                nextHeader.setTag(tag);
            }
        }
        if(nextHeader instanceof ViaList)
        {
            newResponse.addViaHeaderList(nextHeader);
        }
        else if(!(nextHeader instanceof ContactList) && !(nextHeader instanceof UserAgent))
        {
            if(newResponse.getStatusCode()==200)
            {
                newResponse.attachHeader(nextHeader, false);
            }
            else if(!(nextHeader instanceof ContentLength)&&!(nextHeader instanceof ContentType))
            {
                newResponse.attachHeader(nextHeader, false);
            }
        }
    }
    if(this.getMessageContent()!=null&&newResponse.getStatusCode()==200)
    {
        newResponse.setMessageContent(this.getMessageContent());
    }
    var mfimpl=new MessageFactoryImpl();
    if (mfimpl.getDefaultServerHeader() != null) {
        newResponse.setHeader(mfimpl.getDefaultServerHeader());
    }
    if (newResponse.getStatusCode() == 100) {
        newResponse.getTo().removeParameter("tag");
    }
    if(newResponse.getStatusCode()==480&&newResponse.getHeaders("recordroute")!=null)
    {
        newResponse.removeHeader("record-route");
    }
    var server = mfimpl.getDefaultServerHeader();
    if (server != null) {
        newResponse.setHeader(server);
    }
    return newResponse;
}

SIPRequest.prototype.createResponseargu3 =function(response,requestsent){
    response.addContactUseragent(requestsent);
    return response;
}

SIPRequest.prototype.mustCopyRR =function(code){
    if ( code>100 && code<300 ) {
        return this.isDialogCreating( this.getMethod() ) && this.getToTag() == null;
    } 
    else {
        return false;
    }
}

SIPRequest.prototype.createCancelRequest =function(){
    if (this.getMethod()!=this.INVITE)
    {
        console.error("SIPRequest:createCancelRequest(): Attempt to create CANCEL for " + this.getMethod());
        throw "SIPRequest:createCancelRequest(): Attempt to create CANCEL for " + this.getMethod();
    }
    var cancel = new SIPRequest();
    cancel.setRequestLine( this.requestLine);
    cancel.setMethod(this.CANCEL);
    cancel.setHeader(this.callIdHeader);
    cancel.setHeader(this.toHeader);
    cancel.setHeader(this.cSeqHeader);
    cancel.getCSeq().setMethod(this.CANCEL);
    cancel.setHeader( this.fromHeader);
    cancel.addFirst( this.getTopmostVia());
    cancel.setHeader( this.maxForwardsHeader);
    if (this.getRouteHeaders() != null) {
        cancel.setHeader(this.getRouteHeaders());
    }
    var mfimpl=new MessageFactoryImpl();
    if (mfimpl.getDefaultUserAgentHeader() != null) {
        cancel.setHeader(mfimpl.getDefaultUserAgentHeader());
    }
    return cancel;
}

SIPRequest.prototype.createAckRequest =function()
{
    if(arguments.length==1)
    {
        var responseToHeader=arguments[0];
        return this.createAckRequest_argu1(responseToHeader);
    }
    else
    {
        return this.createACKRequest_argu0();
    }
}
SIPRequest.prototype.createAckRequest_argu1 =function(responseToHeader){
    var newRequest = new SIPRequest();
    var nextHeader =null;
    newRequest.setRequestLine(this.requestLine);
    newRequest.setMethod(this.ACK);
    var headerIterator = this.getHeaders();
    for(var i=0;i<headerIterator.length;i++)
    {
        nextHeader=headerIterator[i];
        if (nextHeader instanceof RouteList) {
            continue;
        } else if (nextHeader instanceof ProxyAuthorization) {
            continue;
        } else if (nextHeader instanceof ContentLength) {
            // Adding content is responsibility of user.
            nextHeader.setContentLength(0);
        } else if (nextHeader instanceof ContentType) {
            continue;
        }else if (nextHeader instanceof CSeq) {
            // The CSeq header field in the
            // ACK MUST contain the same value for the
            // sequence number as was present in the
            // original request, but the method parameter
            // MUST be equal to "ACK".
            var cseq = nextHeader;
            cseq.setMethod(this.ACK);
            nextHeader = cseq;
        } else if (nextHeader instanceof To) {
            if (responseToHeader != null) {
                nextHeader = responseToHeader;
            } else {
                nextHeader = nextHeader;
            }
        } else if (nextHeader instanceof ContactList || nextHeader instanceof Expires) {
            continue;
        } else if (nextHeader instanceof ViaList) {
            nextHeader =  nextHeader.getFirst();
        } else {
            nextHeader = nextHeader;
        }
        newRequest.attachHeader(nextHeader, false);
    }
    var mfimpl=new MessageFactoryImpl();
    if (mfimpl.getDefaultUserAgentHeader() != null) {
        newRequest.setHeader(mfimpl.getDefaultUserAgentHeader());
    }
    return newRequest;
}
SIPRequest.prototype.createErrorAck =function(responseToHeader){
    var newRequest = new SIPRequest();
    var newrequestline=new RequestLine();
    newrequestline.setMethod(this.ACK);
    newrequestline.setUri(this.requestLine.getUri());
    var newCSeq=new CSeq();
    newCSeq.setMethod(this.ACK);
    newCSeq.setSeqNumber(this.cSeqHeader.getSequenceNumber());
    newRequest.setRequestLine(newrequestline);
    newRequest.setMethod(this.ACK);
    newRequest.setHeader(this.callIdHeader);
    newRequest.setHeader(this.maxForwardsHeader);
    newRequest.setHeader(this.fromHeader);
    newRequest.setHeader(responseToHeader);
    newRequest.addFirst(this.getTopmostVia());
    newRequest.setHeader(newCSeq);
    if (this.getRouteHeaders() != null) {
        newRequest.setHeader(this.getRouteHeaders());
    }
    var mfimpl=new MessageFactoryImpl();
    if (mfimpl.getDefaultUserAgentHeader() != null) {
        newRequest.setHeader(mfimpl.getDefaultUserAgentHeader());
    }
    return newRequest;
}
SIPRequest.prototype.createSIPRequest =function(requestLine, switchHeaders){
    var newRequest = new SIPRequest();
    newRequest.requestLine = requestLine;
    var headerIterator = this.getHeaders();
    var f=null;
    var t=null;
    for(var i=0;i<headerIterator.length;i++)
    {
        var nextHeader = headerIterator[i];
        if (nextHeader instanceof CSeq) {
            var newCseq = nextHeader;
            nextHeader = newCseq;
            newCseq.setMethod(requestLine.getMethod());
        } else if (nextHeader.classname=="ViaList") {
            var via = nextHeader.getFirst();
            //via.removeParameter("branch");
            nextHeader = via;
        } else if (nextHeader instanceof To) {
            var to =  nextHeader;
            if (switchHeaders) {
                nextHeader = new From(to);
            //nextHeader.removeTag();
            } else {
                nextHeader = to;
            //nextHeader.removeTag();
            }
        } else if (nextHeader instanceof From) {
            var from =  nextHeader;
            if (switchHeaders) {
                nextHeader = new To(from);
            //nextHeader.removeTag();
            } else {
                nextHeader = from;
            //nextHeader.removeTag();
            }
        } else if (nextHeader instanceof ContentLength) {
            var cl = nextHeader;
            cl.setContentLength(0);
            nextHeader = cl;
        }
        else if (!(nextHeader instanceof CallID) && !(nextHeader instanceof MaxForwards)
            && !(nextHeader instanceof Expires)&& !(nextHeader instanceof UserAgent)
            &&!(nextHeader instanceof AllowList)&&!(nextHeader instanceof ContactList)) {
            continue;
        }
        newRequest.attachHeader(nextHeader, false);
    }
    var mfimpl=new MessageFactoryImpl();
    if (mfimpl.getDefaultUserAgentHeader() != null) {
        newRequest.setHeader(mfimpl.getDefaultUserAgentHeader());
    }
    return newRequest;
}


SIPRequest.prototype.createBYERequest =function(switchHeaders){
    var requestLine =  this.requestLine;
    this.requestLine.setMethod("BYE");
    return this.createSIPRequest(requestLine, switchHeaders);
}

SIPRequest.prototype.createACKRequest_argu0 =function(){
    var requestLine =  this.requestLine;
    this.requestLine.setMethod(this.ACK);
    return this.createSIPRequest(requestLine, false);
}

SIPRequest.prototype.getViaHost =function(){
    var via = this.getViaHeaders().getFirst();
    return via.getHost();
}

SIPRequest.prototype.getViaPort =function(){
    var via = this.getViaHeaders().getFirst();
    if (via.hasPort())
    {
        return via.getPort();
    }
    else
    {
        return 5060;
    }
}

SIPRequest.prototype.getFirstLine =function(){
    if (this.requestLine == null)
    {
        return null;
    }
    else
    {
        return this.requestLine.encode();
    }
}

SIPRequest.prototype.setSIPVersion =function(sipVersion){
    if (sipVersion == null || (sipVersion.toLowerCase()!=("SIP/2.0").toLowerCase()))
    {
        console.error("SIPRequest:setSIPVersion(): bad sipVersion", 0);
        throw "SIPRequest:setSIPVersion(): bad sipVersion";
    }
    this.requestLine.setSipVersion(sipVersion);
}

SIPRequest.prototype.getSIPVersion =function(){
    return this.requestLine.getSipVersion();
}

SIPRequest.prototype.getTransaction =function(){
    return this.transactionPointer;
}

SIPRequest.prototype.setTransaction =function(transaction){
    this.transactionPointer = transaction;
}

SIPRequest.prototype.getMessageChannel =function(){
    return this.messageChannel;
}

SIPRequest.prototype.setMessageChannel =function(messageChannel){
    this.messageChannel=messageChannel;
}

SIPRequest.prototype.getMergeId =function(){
    /*
         * generate an identifier from the From tag, call-ID, and CSeq
         */
    var fromTag = this.getFromTag();
    var cseq = this.cSeqHeader.toString();
    var callId = this.callIdHeader.getCallId();
    /* NOTE : The RFC does NOT specify you need to include a Request URI 
         * This is added here for the case of Back to Back User Agents.
         */
    var requestUri = this.getRequestURI().toString();

    if (fromTag != null) {
        var buffer="";
        buffer=(buffer+requestUri+":"+fromTag+":"+cseq+":"+callId).toString();
        return buffer;
    } 
    else
    {
        return null;
    }
}

SIPRequest.prototype.setInviteTransaction =function(inviteTransaction){
    this.inviteTransaction=inviteTransaction;
}

SIPRequest.prototype.getInviteTransaction =function(){
    return this.inviteTransaction;
}

SIPRequest.prototype.fortest =function(){
    var retval = this.requestLine.encode();
    for(var i=0;i<this.headers.length;i++)
    {
        var siphdr = this.headers[i];
        if (!(siphdr instanceof ContentLength))
        {
            var headertemp=siphdr.encode();
           
            retval=retval+headertemp;
        }
    }
    return retval;
}
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
 *  Implementation of the JAIN-SIP SIPResponse .
 *  @see  gov/nist/javax/sip/message/SIPResponse.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function SIPResponse() {
    this.classname="SIPResponse";
    this.statusLine=new StatusLine();
    this.nameTable = new Array();
    this.unrecognizedHeaders= new Array();
    this.headers=new Array();
    this.attachHeader(new ContentLength(0), false);
}

SIPResponse.prototype = new SIPMessage();
SIPResponse.prototype.constructor=SIPResponse;
SIPResponse.prototype.TRYING=100;
SIPResponse.prototype.RINGING=180;
SIPResponse.prototype.CALL_IS_BEING_FORWARDED=181;
SIPResponse.prototype.QUEUED=182;
SIPResponse.prototype.SESSION_PROGRESS=183;
SIPResponse.prototype.OK=200;
SIPResponse.prototype.ACCEPTED=202;
SIPResponse.prototype.MULTIPLE_CHOICES=300;
SIPResponse.prototype.MOVED_PERMANENTLY=301;
SIPResponse.prototype.MOVED_TEMPORARILY=302;
SIPResponse.prototype.USE_PROXY=305;
SIPResponse.prototype.ALTERNATIVE_SERVICE=380;
SIPResponse.prototype.BAD_REQUEST=400;
SIPResponse.prototype.UNAUTHORIZED=401;
SIPResponse.prototype.PAYMENT_REQUIRED=402;
SIPResponse.prototype.FORBIDDEN=403;
SIPResponse.prototype.NOT_FOUND=404;
SIPResponse.prototype.METHOD_NOT_ALLOWED=405;
SIPResponse.prototype.NOT_ACCEPTABLE=406;
SIPResponse.prototype.PROXY_AUTHENTICATION_REQUIRED=407;
SIPResponse.prototype.REQUEST_TIMEOUT=408;
SIPResponse.prototype.GONE=410;
SIPResponse.prototype.TEMPORARILY_UNAVAILABLE=480;
SIPResponse.prototype.REQUEST_ENTITY_TOO_LARGE=413;
SIPResponse.prototype.REQUEST_URI_TOO_LONG=414;
SIPResponse.prototype.UNSUPPORTED_MEDIA_TYPE=415;
SIPResponse.prototype.UNSUPPORTED_URI_SCHEME=416;
SIPResponse.prototype.BAD_EXTENSION=420;
SIPResponse.prototype.EXTENSION_REQUIRED=421;
SIPResponse.prototype.INTERVAL_TOO_BRIEF=423;
SIPResponse.prototype.CALL_OR_TRANSACTION_DOES_NOT_EXIST=481;
SIPResponse.prototype.LOOP_DETECTED=482;
SIPResponse.prototype.TOO_MANY_HOPS=483;
SIPResponse.prototype.ADDRESS_INCOMPLETE=484;
SIPResponse.prototype.AMBIGUOUS=485;
SIPResponse.prototype.BUSY_HERE=486;
SIPResponse.prototype.REQUEST_TERMINATED=487;
SIPResponse.prototype.NOT_ACCEPTABLE_HERE=488;
SIPResponse.prototype.BAD_EVENT=489;
SIPResponse.prototype.REQUEST_PENDING=491;
SIPResponse.prototype.SERVER_INTERNAL_ERROR=500;
SIPResponse.prototype.UNDECIPHERABLE=493;
SIPResponse.prototype.NOT_IMPLEMENTED=501;
SIPResponse.prototype.BAD_GATEWAY=502;
SIPResponse.prototype.SERVICE_UNAVAILABLE=503;
SIPResponse.prototype.SERVER_TIMEOUT=504;
SIPResponse.prototype.VERSION_NOT_SUPPORTED=505;
SIPResponse.prototype.MESSAGE_TOO_LARGE=513;
SIPResponse.prototype.BUSY_EVERYWHERE=600;
SIPResponse.prototype.DECLINE=603;
SIPResponse.prototype.DOES_NOT_EXIST_ANYWHERE=604;
SIPResponse.prototype.SESSION_NOT_ACCEPTABLE=606;
SIPResponse.prototype.CONDITIONAL_REQUEST_FAILED=412;
SIPResponse.prototype.CSeq="CSeq";
SIPResponse.prototype.To="To";
SIPResponse.prototype.From="From";
SIPResponse.prototype.Via="Via";
SIPResponse.prototype.CallID="Call-ID";
SIPResponse.prototype.COLON=":";
SIPResponse.prototype.CANCEL="CANCEL";
SIPResponse.prototype.ACK="ACK";


SIPResponse.prototype.getReasonPhrase=function()
{
    if(arguments.length==1)
    {
        var rc=arguments[0];
        return this.getReasonPhrase_argu1(rc);
    }
    else
    {
        return this.getReasonPhrase_argu0();
    }
}

SIPResponse.prototype.getReasonPhrase_argu1 =function(rc){
    var retval = null;
    switch (rc) {
        case this.TRYING :
            retval = "Trying";
            break;

        case this.RINGING :
            retval = "Ringing";
            break;

        case this.CALL_IS_BEING_FORWARDED :
            retval = "Call is being forwarded";
            break;

        case this.QUEUED :
            retval = "Queued";
            break;

        case this.SESSION_PROGRESS :
            retval = "Session progress";
            break;

        case this.OK :
            retval = "OK";
            break;

        case this.ACCEPTED :
            retval = "Accepted";
            break;

        case this.MULTIPLE_CHOICES :
            retval = "Multiple choices";
            break;

        case this.MOVED_PERMANENTLY :
            retval = "Moved permanently";
            break;

        case this.MOVED_TEMPORARILY :
            retval = "Moved Temporarily";
            break;

        case this.USE_PROXY :
            retval = "Use proxy";
            break;

        case this.ALTERNATIVE_SERVICE :
            retval = "Alternative service";
            break;

        case this.BAD_REQUEST :
            retval = "Bad request";
            break;

        case this.UNAUTHORIZED :
            retval = "Unauthorized";
            break;

        case this.PAYMENT_REQUIRED :
            retval = "Payment required";
            break;

        case this.FORBIDDEN :
            retval = "Forbidden";
            break;

        case this.NOT_FOUND :
            retval = "Not found";
            break;

        case this.METHOD_NOT_ALLOWED :
            retval = "Method not allowed";
            break;

        case this.NOT_ACCEPTABLE :
            retval = "Not acceptable";
            break;

        case this.PROXY_AUTHENTICATION_REQUIRED :
            retval = "Proxy Authentication required";
            break;

        case this.REQUEST_TIMEOUT :
            retval = "Request timeout";
            break;

        case this.GONE :
            retval = "Gone";
            break;

        case this.TEMPORARILY_UNAVAILABLE :
            retval = "Temporarily Unavailable";
            break;

        case this.REQUEST_ENTITY_TOO_LARGE :
            retval = "Request entity too large";
            break;

        case this.REQUEST_URI_TOO_LONG :
            retval = "Request-URI too large";
            break;

        case this.UNSUPPORTED_MEDIA_TYPE :
            retval = "Unsupported media type";
            break;

        case this.UNSUPPORTED_URI_SCHEME :
            retval = "Unsupported URI Scheme";
            break;

        case this.BAD_EXTENSION :
            retval = "Bad extension";
            break;

        case this.EXTENSION_REQUIRED :
            retval = "Etension Required";
            break;

        case this.INTERVAL_TOO_BRIEF :
            retval = "Interval too brief";
            break;

        case this.CALL_OR_TRANSACTION_DOES_NOT_EXIST :
            retval = "Call leg/Transaction does not exist";
            break;

        case this.LOOP_DETECTED :
            retval = "Loop detected";
            break;

        case this.TOO_MANY_HOPS :
            retval = "Too many hops";
            break;

        case this.ADDRESS_INCOMPLETE :
            retval = "Address incomplete";
            break;

        case this.AMBIGUOUS :
            retval = "Ambiguous";
            break;

        case this.BUSY_HERE :
            retval = "Busy here";
            break;

        case this.REQUEST_TERMINATED :
            retval = "Request Terminated";
            break;

        //Issue 168, Typo fix reported by fre on the retval
        case this.NOT_ACCEPTABLE_HERE :
            retval = "Not Acceptable here";
            break;

        case this.BAD_EVENT :
            retval = "Bad Event";
            break;

        case this.REQUEST_PENDING :
            retval = "Request Pending";
            break;

        case this.SERVER_INTERNAL_ERROR :
            retval = "Server Internal Error";
            break;

        case this.UNDECIPHERABLE :
            retval = "Undecipherable";
            break;

        case this.NOT_IMPLEMENTED :
            retval = "Not implemented";
            break;

        case this.BAD_GATEWAY :
            retval = "Bad gateway";
            break;

        case this.SERVICE_UNAVAILABLE :
            retval = "Service unavailable";
            break;

        case this.SERVER_TIMEOUT :
            retval = "Gateway timeout";
            break;

        case this.VERSION_NOT_SUPPORTED :
            retval = "SIP version not supported";
            break;

        case this.MESSAGE_TOO_LARGE :
            retval = "Message Too Large";
            break;

        case this.BUSY_EVERYWHERE :
            retval = "Busy everywhere";
            break;

        case this.DECLINE :
            retval = "Decline";
            break;

        case this.DOES_NOT_EXIST_ANYWHERE :
            retval = "Does not exist anywhere";
            break;

        case this.SESSION_NOT_ACCEPTABLE :
            retval = "Session Not acceptable";
            break;

        case this.CONDITIONAL_REQUEST_FAILED:
            retval = "Conditional request failed";
            break;

        default :
            retval = "Unknown Status";

    }
    return retval;
}

SIPResponse.prototype.setStatusCode =function(statusCode){
    if (statusCode < 100 || statusCode > 699)
    {
        console.error("SIPResponse:setStatusCode(): bad status code");
        throw "SIPResponse:setStatusCode(): bad status code";
    }
    if (this.statusLine == null)
    {
        this.statusLine = new StatusLine();
    }
    this.statusLine.setStatusCode(statusCode);
}

SIPResponse.prototype.getStatusLine =function(){
    return this.statusLine;
}

SIPResponse.prototype.getStatusCode =function(){
    return this.statusLine.getStatusCode();
}

SIPResponse.prototype.setReasonPhrase =function(reasonPhrase){
    if (reasonPhrase == null)
    {
        console.error("SIPResponse:setReasonPhrase(): bad reason phrase");
        throw "SIPResponse:setReasonPhrase(): bad reason phrase";
    }
    if (this.statusLine == null)
    {
        this.statusLine = new StatusLine();
    }
    this.statusLine.setReasonPhrase(reasonPhrase);
}

SIPResponse.prototype.getReasonPhrase_argu0 =function(){
    if (this.statusLine == null || this.statusLine.getReasonPhrase() == null)
    {
        return "";
    }
    else
    {
        return this.statusLine.getReasonPhrase();
    }
}

SIPResponse.prototype.isFinalResponse =function(rc){
    if(rc==null)
    {
        rc=this.statusLine.getStatusCode();
    }
    if(rc >= 200 && rc < 700)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPResponse.prototype.setStatusLine =function(sl){
    this.statusLine = sl;
}

SIPResponse.prototype.checkHeaders =function(){
    
    if (this.getCSeq() == null) {
        console.error("SIPResponse:checkHeaders(): "+ this.CSeq+ " is missing ", 0);
        throw "SIPResponse:checkHeaders(): "+ this.CSeq+ " is missing ";
    }
    if (this.getTo() == null) {
        console.error("SIPResponse:checkHeaders(): "+ this.To+ " is missing ", 0);
        throw "SIPResponse:checkHeaders(): "+ this.To+ " is missing ";
    }
    if (this.getFrom() == null) {
        console.error("SIPResponse:checkHeaders(): "+ this.From+ " is missing ", 0);
        throw "SIPResponse:checkHeaders(): "+ this.From+ " is missing ";
    }
    if (this.getViaHeaders() == null) {
        console.error("SIPResponse:checkHeaders(): "+ this.Via+ " is missing ", 0);
        throw "SIPResponse:checkHeaders(): "+ this.Via+ " is missing ";
    }
    if (this.getCallId() == null) {
       console.error("SIPResponse:checkHeaders(): "+ this.CallID+ " is missing ", 0);
        throw "SIPResponse:checkHeaders(): "+ this.CallID+ " is missing ";
    }
    if (this.getStatusCode() > 699) {
        console.error("SIPResponse:checkHeaders(): unknown error code!" + this.getStatusCode(), 0);
        throw "SIPResponse:checkHeaders(): unknown error code!" + this.getStatusCode();
    }
}

SIPResponse.prototype.encode =function(){
    var retval;
    if (this.statusLine != null)
    {
        retval = this.statusLine.encode() + this.superencode();
    }
    else
    {
        retval = this.superencode();
    }
    return retval ;
}

SIPResponse.prototype.encodeMessage =function(){
    var retval;
    if (this.statusLine != null)
    {
        retval = this.statusLine.encode() + this.encodeSIPHeaders();
    }
    else
    {
        retval = this.encodeSIPHeaders();
    }
    return retval ;
}

SIPResponse.prototype.superencode =function(){
    var encoding = "";
    for(var i=0;i<this.headers.length;i++)
    {
        var siphdr = this.headers[i];
        if (!(siphdr instanceof ContentLength))
        {
            encoding=encoding+siphdr.encode();
        }
    }
    for(i=0;i<this.unrecognizedHeaders.length;i++)
    {
        var unrecognized=this.unrecognizedHeaders[i];
        encoding=encoding+unrecognized+this.NEWLINE;
    }
    encoding=encoding+this.contentLengthHeader.encode()+this.NEWLINE;
    if (this.messageContentObject != null) 
    {
        var mbody = this.getContent().toString();
        encoding=encoding+mbody;
    }
    else if (this.messageContent != null || this.messageContentBytes != null) {
        var content = "";
        if (this.messageContent != null)
        {
            content = this.messageContent;
        }
        else 
        {
            for(i=0;i<this.messageContentBytes.length;i++)
            {
                content=content+this.messageContentBytes[i];
            }
        }
        encoding=encoding+content;
    }
    return encoding.toString();
}

SIPResponse.prototype.getMessageAsEncodedStrings =function(){
    var retval = Object.getPrototypeOf(this).getMessageAsEncodedStrings();
    if (this.statusLine != null)
    {
        var x=new Array();
        x[0]=this.statusLine.encode();
        for(var i=0;i<this.retval.length;i++)
        {
            x[i+1]=this.retval[i];
        }
        retval=x;   
    }
    return retval;
}

SIPResponse.prototype.encodeAsBytes =function(transport){
    var slbytes = new Array();
    if (this.statusLine != null) {
        slbytes = this.getBytes(this.statusLine.encode());
    }
    var superbytes = Object.getPrototypeOf(this).encodeAsBytes(transport);
    var retval = new Array();
    retval=slbytes.concat(superbytes);
    return retval;
}

SIPResponse.prototype.getDialogId =function(){
    if(arguments.length==1)
    {
        var isServer = arguments[0];
        return this.getDialogIdargu1(isServer);
    }
    else if(arguments.length==2)
    {
        isServer = arguments[0];
        var toTag = arguments[1];
        return this.getDialogIdargu2(isServer, toTag);
    }
}
SIPResponse.prototype.getDialogIdargu1 =function(isServer){
    var cid = this.getCallId();
    var retval = cid.getCallId();
    var from = this.getFrom();
    var to = this.getTo();
    if (!isServer) {
        if (from.getTag() != null) {
            retval=retval+this.COLON+from.getTag();
        }
        if (to.getTag() != null) {
            retval=retval+this.COLON+to.getTag();
        }
    } else {
        if (to.getTag() != null) {
            retval=retval+this.COLON+to.getTag();
        }
        if (from.getTag() != null) {
            retval=retval+this.COLON+from.getTag();
        }
    }
    return retval.toString().toLowerCase();
}

SIPResponse.prototype.getDialogIdargu2 =function(isServer, toTag){
    var from =  this.getFrom();
    var cid =  this.getCallId();
    var retval = cid.getCallId();
    if (!isServer) {
        if (from.getTag() != null) {
            retval=retval+this.COLON+from.getTag();
        }
        if (toTag != null) {
            retval=retval+this.COLON+toTag;
        }
    } else {
        if (toTag != null) {
            retval=retval+this.COLON+toTag;
        }
        if (from.getTag() != null) {
            retval=retval+this.COLON+from.getTag();
        }
    }
    return retval.toString().toLowerCase();
}

SIPResponse.prototype.setBranch =function(via, method){
    var branch;
    if (method==this.ACK) {
        if (this.statusLine.getStatusCode() >= 300 ) {
            branch = this.getTopmostVia().getBranch();   // non-2xx ACK uses same branch
        } 
        else {
            var utils=new Utils();
            branch = utils.getInstance().generateBranchId();    // 2xx ACK gets new branch
        }
    } 
    else if (method==this.CANCEL) {
        branch = this.getTopmostVia().getBranch();   // CANCEL uses same branch
    } 
    else {
        return;
    }
    via.setBranch( branch );
}

SIPResponse.prototype.getFirstLine =function(){
    if (this.statusLine == null)
    {
        return null;
    }
    else
    {
        return this.statusLine.encode();
    }
}

SIPResponse.prototype.setSIPVersion =function(sipVersion){
    this.statusLine.setSipVersion(sipVersion);
}

SIPResponse.prototype.getSIPVersion =function(){
    return this.statusLine.getSipVersion();
}

SIPResponse.prototype.toString =function(){
    if (this.statusLine == null) {
        return  "";
    }
    else {
        return this.statusLine.encode() + Object.getPrototypeOf(this).encode();
    }
}

SIPResponse.prototype.createRequest =function(requestURI, via, cseq, from, to){
    var newRequest = new SIPRequest();
    var method = cseq.getMethod();
    var callid=this.getCallId();
    newRequest.setMethod(method);
    newRequest.setRequestURI(requestURI);
    this.setBranch(via, method);
    newRequest.setHeader(via);
    newRequest.setHeader(from);
    newRequest.setHeader(to);
    newRequest.setHeader(cseq);
    newRequest.setHeader(callid);
    newRequest.attachHeader(new MaxForwards(70), false);
    var headerIterator = newRequest.getHeaders();
    for(var i=0;i<headerIterator.length;i++)
    {
        var nextHeader = headerIterator[i];
        if (this.isResponseHeader(nextHeader)
            || nextHeader instanceof ViaList
            || nextHeader instanceof CSeq
            || nextHeader instanceof ContentType
            || nextHeader instanceof ContentLength
            || nextHeader instanceof RecordRouteList
            //|| nextHeader instanceof RequireList
            || nextHeader instanceof ContactList    // JvB: added
            || nextHeader instanceof ContentLength
            //|| nextHeader instanceof ServerHeader
            || nextHeader instanceof Reason
            //|| nextHeader instanceof SessionExpires
            || nextHeader instanceof ReasonList) {
            continue;
        }
        if (nextHeader instanceof To)
        {
            nextHeader =  to;
        }
        else if (nextHeader instanceof From)
        {
            nextHeader =  from;
        }
        newRequest.attachHeader(nextHeader, false);    
    }
    // JvB: all requests need a Max-Forwards
    var mfimpl=new MessageFactoryImpl();   
    if (mfimpl.getDefaultUserAgentHeader() != null ) {
        newRequest.setHeader(mfimpl.getDefaultUserAgentHeader());
    }
    return newRequest;
}

SIPResponse.prototype.getBytes =function(str){
    var array=new Array();
    str=new String(str);
    for(var i=0;i<str.length;i++)
    {
        array[i]=str.charCodeAt(i);
    } 
    return array;
}

SIPResponse.prototype.addContactUseragent =function(requestsent){
    var contact=requestsent.getContactHeader();
    var useragent=requestsent.getHeader("User-Agent");
    this.addHeader(contact);
    this.addHeader(useragent);
}

SIPResponse.prototype.fortest =function(){
    var retval = this.statusLine.encode();
    for(var i=0;i<this.headers.length;i++)
    {
        var siphdr = this.headers[i];
        if (!(siphdr instanceof ContentLength))
        {
            var headertemp=siphdr.encode();
           
            retval=retval+headertemp;
        }
    }
    return retval;
}/*
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
 *  Implementation of the JAIN-SIP HopImpl .
 *  @see  gov/nist/javax/sip/stack/HopImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function HopImpl() {
    this.classname="HopImpl";
    this.host=null;
    this.port=null;
    this.transport="WS";
    this.defaultRoute=null; // This is generated from the proxy addr
    this.uriRoute=null;
    this.wsurl=null;
    if(arguments.length==1)
    {
        var hop=arguments[0];
        if (hop == null)
        {
            console.error("HopImpl:HopImpl(): null arg!");
            throw "HopImpl:HopImpl(): null arg!";
        }
        var brack = hop.indexOf(']');
        var colon = hop.indexOf(':',brack);
        var slash = hop.indexOf('/',colon);
        if (colon>0) {
            this.host = hop.substring(0,colon);
            var portstr;
            if (slash>0) {
                portstr = hop.substring(colon+1,slash);
                this.transport = hop.substring(slash+1);
            } else {
                portstr = hop.substring(colon+1);
            }
            this.port = portstr-0;
        } else {
            if (slash>0) {
                this.host = hop.substring(0,slash);
                this.transport = hop.substring(slash+1);
                this.port = 8080;
            } else {
                this.host = hop;
                this.port = 8080;
            }
        }
        if (this.host == null || this.host.length == 0)
        {
            console.error("HopImpl:HopImpl(): no host!");
            throw "HopImpl:HopImpl(): no host!";
        }
        this.host = this.host.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
        this.transport = this.transport.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
        if ((brack>0) && this.host.charAt(0)!='[') {
            console.error("HopImpl:HopImpl(): bad IPv6 reference spec");
            throw "HopImpl:HopImpl(): bad IPv6 reference spec";
        }
        if ((this.transport.toLowerCase()!="tcp") && (this.transport.toLowerCase()!= "ws") ) {
            console.error("HopImpl:HopImpl(): bad transport string " + this.transport);
            throw "HopImpl:HopImpl(): bad transport string " + this.transport;
        }
    }
    else if(arguments.length==3)
    {
        var hostName = arguments[0];
        var portNumber = arguments[1];
        var trans = arguments[2];
        this.host = hostName;
        if(this.host.indexOf(":") >= 0)
        {
            if(this.host.indexOf("[") < 0)
            {
                this.host = "[" + this.host + "]";
            }
        }
        this.port = portNumber;
        this.transport = trans;
    }
}

HopImpl.prototype.toString =function(){
    return this.host + ":" +this.port + "/" + this.transport;
}

HopImpl.prototype.getHost =function(){
    return this.host;
}

HopImpl.prototype.getPort =function(){
    return this.port;
}

HopImpl.prototype.getTransport =function(){
    return this.transport;
}

HopImpl.prototype.getURLWS =function(){
    return this.wsurl;
}

HopImpl.prototype.isURIRoute =function(){
    return this.uriRoute;
}

HopImpl.prototype.setURIRouteFlag =function(){
    this.uriRoute=true;
}

HopImpl.prototype.setURLWS =function(wsurl){
    this.wsurl=wsurl;
}
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
 *  Implementation of the JAIN-SIP SIPTransactionStack .
 *  @see  gov/nist/javax/sip/stack/SIPTransactionStack.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function SIPTransactionStack() {
    this.classname="SIPTransactionStack"; 
    
    this.messageProcessors=new Array();
    this.sipMessageFactory=null;
    this.activeClientTransactionCount = 0;
    this.mergeTable=new Array();
    this.defaultRouter=null;
    this.needsLogging=null;
    this.stackName=null;
    this.router=null;
    this.maxConnections=-1;
    this.useRouterForAll=null;
    this.readTimeout= -1;
    this.outboundProxy=null;
    this.routerPath=null;
    this.isAutomaticDialogSupportEnabled=null;
    this.forkedEvents=new Array();
    this.generateTimeStampHeader=null;
    this.cancelClientTransactionChecked = true;
    this.remoteTagReassignmentAllowed = true;
    this.logStackTraceOnMessageSend = true;
    this.stackDoesCongestionControl = true;
    this.checkBranchId=false;
    this.isAutomaticDialogErrorHandlingEnabled = true;
    this.isDialogTerminatedEventDeliveredForNullDialog = false;
    this.serverTransactionTable=new Array();
    this.clientTransactionTable=new Array();
    this.terminatedServerTransactionsPendingAck=new Array();
    this.forkedClientTransactionTable=new Array();
    this.dialogCreatingMethods=new Array();
    this.dialogTable=new Array();
    this.earlyDialogTable=new Array();
    this.pendingTransactions=new Array();
    this.unlimitedServerTransactionTableSize = true;
    this.unlimitedClientTransactionTableSize = true;
    this.serverTransactionTableHighwaterMark = 5000;
    this.serverTransactionTableLowaterMark = 4000;
    this.clientTransactionTableHiwaterMark = 1000;
    this.clientTransactionTableLowaterMark = 800;
    this.rfc2543Supported=true;
    this.timer=0;
    this.maxForkTime=0;
    this.toExit=false;
    this.isBackToBackUserAgent = false;
    this.maxListenerResponseTime=-1;
    this.non2XXAckPassedToListener=null;
    this.maxMessageSize=null;
    this.addressResolver = new DefaultAddressResolver();
    this.stackAddress =null;
    this.dialogCreatingMethods.push("REFER");
    this.dialogCreatingMethods.push("INVITE");
    this.dialogCreatingMethods.push("SUBSCRIBE");
    this.dialogCreatingMethods.push("REGISTER");
}

SIPTransactionStack.prototype.BASE_TIMER_INTERVAL=500;
SIPTransactionStack.prototype.CONNECTION_LINGER_TIME=8;
SIPTransactionStack.prototype.BRANCH_MAGIC_COOKIE_LOWER_CASE="z9hg4bk";
SIPTransactionStack.prototype.TRYING=100;
SIPTransactionStack.prototype.RINGING=180;

SIPTransactionStack.prototype.reInit =function(){
    this.messageProcessors = new Array();
    this.pendingTransactions = new Array();
    this.clientTransactionTable = new Array();
    this.serverTransactionTable = new Array();
    this.mergeTable = new Array();
    this.dialogTable = new Array();
    this.earlyDialogTable = new Array();
    this.terminatedServerTransactionsPendingAck = new Array();
    this.forkedClientTransactionTable = new Array();
    this.timer = null;
    this.activeClientTransactionCount=0;
}

SIPTransactionStack.prototype.addExtensionMethod =function(extensionMethod){
    if (extensionMethod!="NOTIFY") {
        var l=null;
        for(var i=0;i<this.dialogCreatingMethods.length;i++)
        {
            if(this.dialogCreatingMethods[i]==extensionMethod.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '').toUpperCase())
            {
                l=i;
            }
        }
        if(l==null)
        {
            this.dialogCreatingMethods.push(extensionMethod.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '').toUpperCase());
        }
    }
}

SIPTransactionStack.prototype.removeDialog =function(){
    if(typeof arguments[0]=="objet")
    {
        var dialog=arguments[0];
        this.removeDialogobjet(dialog);
    }
    else if(typeof arguments[0]=="string")
    {
        var dialogId=arguments[0];
        this.removeDialogstring(dialogId);
    }
}

SIPTransactionStack.prototype.removeDialogstring =function(dialogId){
    var l=null;
    for(var i=0;i<this.dialogTable.length;i++)
    {
        if(this.dialogTable[i][0]==dialogId)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        this.dialogTable.splice(l,1);
    }    
}

SIPTransactionStack.prototype.removeDialogobjet =function(dialog){
    var id = dialog.getDialogId();
    var earlyId = dialog.getEarlyDialogId();
    if (earlyId != null) {
        var l=null;
        for(var i=0;i<this.earlyDialogTable.length;i++)
        {
            if(this.earlyDialogTable[i][0]==earlyId)
            {
                l=i;
            }
        }
        this.earlyDialogTable.splice(l,1);
        for(i=0;i<this.dialogTable.length;i++)
        {
            if(this.dialogTable[i][0]==earlyId)
            {
                l=i;
            }
        }
        if(l!=null)
        {
            this.dialogTable.splice(l,1);
        } 
    }
    if (id != null) {
        var old = null;
        for(i=0;i<this.dialogTable.length;i++)
        {
            if(this.dialogTable[i][0]==id)
            {
                old = this.dialogTable[i][1];
            }
        }
        if (old == dialog) {
            for(i=0;i<this.dialogTable.length;i++)
            {
                if(this.dialogTable[i][0]==id)
                {
                    l=i;
                }
            }
            if(l!=null)
            {
                this.dialogTable.splice(l,1);
            } 
        }
        if (!dialog.testAndSetIsDialogTerminatedEventDelivered()) {
            var event = new DialogTerminatedEvent(dialog.getSipProvider(),dialog);
            dialog.getSipProvider().handleEvent(event, null);
        }
    }
}

SIPTransactionStack.prototype.findSubscribeTransaction =function(notifyMessage,listeningPoint){
    var retval = null;
    var thisToTag = notifyMessage.getTo().getTag();
    if (thisToTag == null) {
        return retval;
    }
    var eventHdr = notifyMessage.getHeader("Event");
    if (eventHdr == null) {
        return retval;
    }
    for(var i=0;i<this.clientTransactionTable.length;i++)
    {
        var ct = this.clientTransactionTable[i][1];
        if (ct.getMethod()!="SUBSCRIBE") {
            continue;
        }
        var fromTag = ct.from.getTag();
        var hisEvent = ct.event;
        if (hisEvent == null) {
            continue;
        }
        if (fromTag.toLowerCase()==thisToTag.toLowerCase()
            && hisEvent != null
            && eventHdr.match(hisEvent)
            && notifyMessage.getCallId().getCallId().toLowerCase()==ct.callId.getCallId().toLowerCase()) {
            retval = ct;
            return retval;
        }
    }
    return retval;
}

SIPTransactionStack.prototype.removeTransactionPendingAck =function(serverTransaction){
    var branchId = serverTransaction.getRequest().getTopmostVia().getBranch();
    var l=null;
    for(var i=0;i<this.terminatedServerTransactionsPendingAck.length;i++)
    {
        if(this.terminatedServerTransactionsPendingAck[i][0]==branchId)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        var r=true;
    }
    else
    {
        r=false;
    }
    if (branchId != null && r) {
        l=null;
        for(i=0;i<this.terminatedServerTransactionsPendingAck.length;i++)
        {
            if(this.terminatedServerTransactionsPendingAck[i][0]==branchId)
            {
                l=i;
            }
        }
        if(l!=null)
        {
            this.terminatedServerTransactionsPendingAck.splice(l,1);
        }
        return true;
    } else {
        return false;
    }

}

SIPTransactionStack.prototype.removeTransactionHash =function(sipTransaction){
    var sipRequest = sipTransaction.getOriginalRequest();
    if (sipRequest == null) {
        return;
    }
    if (sipTransaction instanceof SIPClientTransaction) {
        var key = sipTransaction.getTransactionId();
        var l=null;
        for(var i=0;i<this.clientTransactionTable.length;i++)
        {
            if(this.clientTransactionTable[i][0]==key)
            {
                l=i;
            }
        }
        this.clientTransactionTable.splice(l,1);
    } 
    else if (sipTransaction instanceof SIPServerTransaction) {
        key = sipTransaction.getTransactionId();
        l=null;
        for(i=0;i<this.serverTransactionTable.length;i++)
        {
            if(this.serverTransactionTable[i][0]==key)
            {
                l=i;
            }
        }
        this.serverTransactionTable.splice(l,1);
    }
}

SIPTransactionStack.prototype.removePendingTransaction =function(tr){
    var l=null;
    for(var i=0;i<this.pendingTransactions.length;i++)
    {
        if(this.pendingTransactions[i][0]==tr.getTransactionId())
        {
            l=i;
        }
    }
    if(l!=null)
    {
        this.pendingTransactions.splice(l,1);
    }
}

SIPTransactionStack.prototype.isAlive =function(){
    if(!this.toExit)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransactionStack.prototype.getTimer =function(){
    return this.timer;
}

SIPTransactionStack.prototype.findCancelTransaction =function(cancelRequest,isServer){
    if (isServer) {
        for(var i=0;i<this.serverTransactionTable.length;i++)
        {
            var transaction = this.serverTransactionTable[i][1];
            var sipServerTransaction = transaction;
            if (sipServerTransaction.doesCancelMatchTransaction(cancelRequest)) {
                return sipServerTransaction;
            }
        }
    } 
    else {
        for(i=0;i<this.clientTransactionTable.length;i++)
        {
            transaction = this.clientTransactionTable[i][1];
            var sipClientTransaction = transaction;
            if (sipClientTransaction.doesCancelMatchTransaction(cancelRequest)) {
                return sipClientTransaction;
            }
        }
    }
    return null;
}

SIPTransactionStack.prototype.getDialog =function(dialogId){
    var l=null;
    for(var i=0;i<this.dialogTable.length;i++)
    {
        if(this.dialogTable[i][0]==dialogId)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        var sipDialog = this.dialogTable[l][1];
        return sipDialog;
    }
    else
    {
        return null;
    }
}

SIPTransactionStack.prototype.isDialogCreated =function(method){
    var l=null;
    for(var i=0;i<this.dialogCreatingMethods.length;i++)
    {
        if(this.dialogCreatingMethods[i]==method)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        return true;
    }
    else
    {
        return false
    }
}

SIPTransactionStack.prototype.isRfc2543Supported =function(){
    return this.rfc2543Supported;
}

SIPTransactionStack.prototype.createDialog =function(){
    if(arguments.length==1)
    {
        var transaction=arguments[0];
        return this.createDialogargu1(transaction);
    }
    else if(arguments.length==2)
    {
        if(arguments[0].classname=="SipProviderImpl")
        {
            var sipProvider=arguments[0];
            sipResponse=arguments[1];
            return new SIPDialog(sipProvider,sipResponse); 
        }
        else
        {
            transaction=arguments[0];
            var sipResponse=arguments[1];
            return this.createDialogargu2(transaction, sipResponse);
        }
    }
}

SIPTransactionStack.prototype.createDialogargu1 =function(transaction){
    var retval = null;
    if (transaction instanceof SIPClientTransaction) {
        var dialogId = transaction.getRequest().getDialogId(false);
        var l=null;
        for(var i=0;i<this.earlyDialogTable.length;i++)
        {
            if(this.earlyDialogTable[i][0]==dialogId)
            {
                l=i;
            }
        }
        if (l != null) {
            var dialog = this.earlyDialogTable[l][1];
            if (dialog.getState() == null || dialog.getState() == "EARLY") {
                retval = dialog;
            } 
            else {
                retval = new SIPDialog(transaction);
                this.earlyDialogTable[l][1]=retval;
            }
        } 
        else 
        {
            retval = new SIPDialog(transaction);
            var array=new Array();
            array[0]=dialogId;
            array[1]=retval;
            this.earlyDialogTable.push(array);
        }
    } 
    else {
        retval = new SIPDialog(transaction);
    }
    return retval;
}

SIPTransactionStack.prototype.createDialogargu2 =function(transaction,sipResponse){
    var dialogId = transaction.getRequest().getDialogId(false);
    var retval = null;
    var l=null;
    for(var i=0;i<this.earlyDialogTable.length;i++)
    {
        if(this.earlyDialogTable[i][0]==dialogId)
        {
            l=i;
        }
    }
    if (l != null) {
        retval = this.earlyDialogTable[l][1];
        if (sipResponse.isFinalResponse()) {
            this.earlyDialogTable.splice(l,1);
        }
    } 
    else {
        retval = new SIPDialog(transaction, sipResponse);
    }
    return retval;
}

SIPTransactionStack.prototype.createRawMessageChannel =function(){
    var newChannel = null;
    //var l=null;
    for(var i=0;i<this.messageProcessors.length && newChannel == null;i++)
    {
        var processor = this.messageProcessors[i];
        if (processor.getURLWS()==this.wsurl) {
            newChannel = processor.createMessageChannel();
        }
    }
    return newChannel;
}

SIPTransactionStack.prototype.isNon2XXAckPassedToListener =function(){
    if(this.non2XXAckPassedToListener)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransactionStack.prototype.isTransactionPendingAck =function(serverTransaction){
    var branchId = serverTransaction.getRequest().getTopmostVia().getBranch();
    var l=null;
    for(var i=0;i<this.terminatedServerTransactionsPendingAck.length;i++)
    {
        if(this.terminatedServerTransactionsPendingAck[i][0]==branchId)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransactionStack.prototype.setNon2XXAckPassedToListener =function(passToListener){
    this.non2XXAckPassedToListener = passToListener;
}

SIPTransactionStack.prototype.addForkedClientTransaction =function(clientTransaction){
    var l=null;
    for(var i=0;i<this.forkedClientTransactionTable.length;i++)
    {
        if(this.forkedClientTransactionTable[i][0]==clientTransaction.getTransactionId())
        {
            l=i;
            this.forkedClientTransactionTable[i][1]=clientTransaction;
        }
    }
    if(l==null)
    {
        var array=new Array();
        array[0]=clientTransaction.getTransactionId();
        array[1]=clientTransaction;
        this.forkedClientTransactionTable.push(array);
    }
}

SIPTransactionStack.prototype.getForkedTransaction =function(transactionId){
    var l=null;
    for(var i=0;i<this.forkedClientTransactionTable.length;i++)
    {
        if(this.forkedClientTransactionTable[i][0]==transactionId)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        return this.forkedClientTransactionTable[l][1];
    }
    else
    {
        return null;
    }
}

SIPTransactionStack.prototype.addTransactionPendingAck =function(serverTransaction){
    var branchId = serverTransaction.getRequest().getTopmostVia().getBranch();
    if (branchId != null) {
        var l=null;
        for(var i=0;i<this.terminatedServerTransactionsPendingAck.length;i++)
        {
            if(this.forkedClientTransactionTable[i][0]==branchId)
            {
                l=i;
                this.forkedClientTransactionTable[i][1]=serverTransaction;
            }
        }
        if(l==null)
        {
            var array=new Array();
            array[0]=branchId;
            array[1]=serverTransaction;
            this.forkedClientTransactionTable.push(array);
        }
    }
}

SIPTransactionStack.prototype.findTransactionPendingAck =function(ackMessage){
    var l=null;
    for(var i=0;i<this.terminatedServerTransactionsPendingAck.length;i++)
    {
        if(this.terminatedServerTransactionsPendingAck[i][0]==ackMessage.getTopmostVia().getBranch())
        {
            l=i;
        }
    }
    if(l==null)
    {
        return null;
    }
    else
    {
        return this.terminatedServerTransactionsPendingAck[l][1];
    }
}

SIPTransactionStack.prototype.putDialog =function(dialog){
    var dialogId = dialog.getDialogId();
    var l=null;
    for(var i=0;i<this.dialogTable.length;i++)
    {
        if(this.dialogTable[i][0]==dialogId)
        {
            l=i;
        }
    }
    if (l!=null) {
        return;
    }
    dialog.setStack(this);
    var array=new Array()
    array[0]=dialogId;
    array[1]=dialog;
    this.dialogTable.push(array);
}

SIPTransactionStack.prototype.findPendingTransaction =function(requestReceived){
    var l=null;
    for(var i=0;i<this.pendingTransactions.length;i++)
    {
        if(this.dialogTable[i][0]==requestReceived.getTransactionId())
        {
            l=i;
        }
    }
    if(l==null)
    {
        return null;
    }
    else
    {
        return this.pendingTransactions[l][1];
    }
}

SIPTransactionStack.prototype.putPendingTransaction =function(tr){
    var l=null;
    for(var i=0;i<this.pendingTransactions.length;i++)
    {
        if(this.pendingTransactions[i][0]==tr.getTransactionId())
        {
            l=i;
            this.pendingTransactions[i][1]=tr;
        }
    }
    if(l==null)
    {
        var array=new Array();
        array[0]=tr.getTransactionId();
        array[1]=tr;
        this.pendingTransactions.push(array);
    }
}

SIPTransactionStack.prototype.removePendingTransaction =function(tr){
    var l=null;
    for(var i=0;i<this.pendingTransactions.length;i++)
    {
        if(this.pendingTransactions[i][0]==tr.getTransactionId())
        {
            l=i;
        }
    }
    this.pendingTransactions.splice(l,1);
}

SIPTransactionStack.prototype.getServerTransactionTableSize =function(){
    return this.serverTransactionTable.length;
}

SIPTransactionStack.prototype.getClientTransactionTableSize =function(){
    return this.clientTransactionTable.length;
}

SIPTransactionStack.prototype.findTransaction =function(sipMessage,isServer){
    var retval = null;
    if (isServer) {
        var via = sipMessage.getTopmostVia();
        if (via.getBranch() != null) {
            var key = sipMessage.getTransactionId();
            for(var i=0;i<this.serverTransactionTable.length;i++)
            {
                if(this.serverTransactionTable[i][0]==key)
                {
                    retval=this.serverTransactionTable[i][1];
                }
            }
            if (key.substring(0,7).toLowerCase()=="z9hg4bk") {
                return retval;
            }
        }
        for(i=0;i<this.serverTransactionTable.length;i++)
        {
            var sipServerTransaction = this.serverTransactionTable[i][1];
            if (sipServerTransaction.isMessagePartOfTransaction(sipMessage)) {
                retval = sipServerTransaction;
                return retval;
            }
        }
    } else {
        via = sipMessage.getTopmostVia();
        if (via.getBranch() != null) {
            key = sipMessage.getTransactionId();
            for(i=0;i<this.clientTransactionTable.length;i++)
            {
                if(this.clientTransactionTable[i][0]==key)
                {
                    retval=this.clientTransactionTable[i][1];
                }
            }
            if (key.substring(0,7).toLowerCase()=="z9hg4bk") {
                return retval;
            }
        }
        for(i=0;i<this.serverTransactionTable.length;i++)
        {
            var clientTransaction = this.clientTransactionTable[i][1];
            if (clientTransaction.isMessagePartOfTransaction(sipMessage)) {
                retval = clientTransaction;
                return retval;
            }
        }
    }
    return retval;
}

SIPTransactionStack.prototype.removeFromMergeTable =function(tr){
    var key = tr.getRequest().getMergeId();
    var l=null
    if (key != null) {
        for(var i=0;i<this.mergeTable.length;i++)
        {
            if(this.mergeTable[i][0]==key)
            {
                l=i;
            }
        }
        if(l!=null)
        {
            this.mergeTable.splice(l,1);
        }
    }   
}

SIPTransactionStack.prototype.putInMergeTable =function(sipTransaction,sipRequest){
    var mergeKey = sipRequest.getMergeId();
    var l=null;
    if (mergeKey != null) {
        for(var i=0;i<this.mergeTable.length;i++)
        {
            if(this.mergeTable[i][0]==mergeKey)
            {
                this.mergeTable[i][1]=sipTransaction;
                l=i
            }
        }
        if(l==null)
        {
            var array=new Array()
            array[0]=mergeKey;
            array[1]=sipTransaction;
            this.mergeTable.push(array);
        }
    }
}

SIPTransactionStack.prototype.addTransactionHash =function(sipTransaction){
    var sipRequest = sipTransaction.getOriginalRequest();
    if (sipTransaction instanceof SIPClientTransaction) {
        this.activeClientTransactionCount++;
        var l=null;
        var key = sipRequest.getTransactionId();
        for(var i=0;i<this.clientTransactionTable.length;i++)
        {
            if(this.clientTransactionTable[i][0]==key)
            {
                l=i;
                this.clientTransactionTable[i][1]=sipTransaction;
            }
        }
        if(l==null)
        {
            var array=new Array();
            array[0]=key;
            array[1]=sipTransaction;
            this.clientTransactionTable.push(array);
        }
    } else {
        l=null;
        key = sipRequest.getTransactionId();
        for(i=0;i<this.serverTransactionTable.length;i++)
        {
            if(this.serverTransactionTable[i][0]==key)
            {
                l=i;
                this.serverTransactionTable[i][1]=sipTransaction;
            }
        }
        if(l==null)
        {
            array=new Array();
            array[0]=key;
            array[1]=sipTransaction;
            this.serverTransactionTable.push(array);
        }
    }
}

SIPTransactionStack.prototype.setMessageFactory =function(messageFactory){
    this.sipMessageFactory = messageFactory; 
}

SIPTransactionStack.prototype.checkBranchIdFunction =function(){
    return this.checkBranchId;    
}
SIPTransactionStack.prototype.addMessageProcessor =function(newMessageProcessor){
    var l=null
    for(var i=0;i<this.messageProcessors.length;i++)
    {
        if(this.messageProcessors[i]==newMessageProcessor)
        {
            l=i;
        }
    }
    if(l==null)
    {
        this.messageProcessors.push(newMessageProcessor);
    }
}

SIPTransactionStack.prototype.findMergedTransaction =function(sipRequest){
    if (sipRequest.getMethod()!="INVITE") {
        return null;
    }
    var mergeId = sipRequest.getMergeId();
    var mergedTransaction = null;  
    for(var i=0;i<this.mergeTable.length;i++)
    {
        if(this.mergeTable[i][0]==mergeId)
        {
            mergedTransaction = this.mergeTable[i][1];
        }
    }
    if (mergeId == null) {
        return null;
    } 
    else if (mergedTransaction != null && !mergedTransaction.isMessagePartOfTransaction(sipRequest)) {
        return mergedTransaction;
    } 
    else {
        for (i=0;i<this.dialogTable.length;i++) {
            var dialog=this.dialogTable[i][1];
            var sipDialog = dialog;
            if (sipDialog.getFirstTransaction() != null
                && sipDialog.getFirstTransaction() instanceof SIPServerTransaction) {
                var serverTransaction = sipDialog.getFirstTransaction();
                var transactionRequest = sipDialog.getFirstTransaction().getOriginalRequest();
                if ((!serverTransaction.isMessagePartOfTransaction(sipRequest))
                    && sipRequest.getMergeId()==transactionRequest.getMergeId()) {
                    return sipDialog.getFirstTransaction();
                }
            }
        }
        return null;
    }
}

SIPTransactionStack.prototype.mapTransaction =function(transaction){
    if (transaction.isMapped) {
        return;
    }
    this.addTransactionHash(transaction);
    transaction.isMapped = true;      
}

SIPTransactionStack.prototype.createTransaction =function(request,mc,nextHop){
    var returnChannel=null;
    if (mc == null) {
        return null;
    }
    returnChannel = this.createClientTransaction(request, mc);
    returnChannel.setViaPort(nextHop.getPort());
    returnChannel.setViaHost(nextHop.getHost());
    this.addTransactionHash(returnChannel);
    return returnChannel;       
}

SIPTransactionStack.prototype.createClientTransaction =function(sipRequest,encapsulatedMessageChannel){
    var ct = new SIPClientTransaction(this, encapsulatedMessageChannel);
    ct.setOriginalRequest(sipRequest);
    return ct;       
}

SIPTransactionStack.prototype.addTransaction =function(transaction){
    if(transaction instanceof SIPServerTransaction)
    {
        transaction.map();
    }
    this.addTransactionHash(transaction);     
}

SIPTransactionStack.prototype.transactionErrorEvent =function(transactionErrorEvent){
    var transaction = transactionErrorEvent.getSource();
    if (transactionErrorEvent.getErrorID() == 2) {
        transaction.setState("TERMINATED");
        if (transaction instanceof SIPServerTransaction) {
            transaction.collectionTime = 0;
        }
        transaction.disableTimeoutTimer();
    }       
}

SIPTransactionStack.prototype.dialogErrorEvent =function(dialogErrorEvent){
    var sipDialog = dialogErrorEvent.getSource();
    if (sipDialog != null) {
        sipDialog.delet();
    }     
}

SIPTransactionStack.prototype.stopStack =function(){
    if (this.timer != null) {
        clearTimeout(this.timer)
    }
    this.timer = null;
    this.pendingTransactions=new Array();
    this.toExit = true;
    var processorList = this.getMessageProcessors();
    for (var processorIndex = 0; processorIndex < processorList.length; processorIndex++) {
        this.removeMessageProcessor(processorList[processorIndex]);
        this.clientTransactionTable=new Array();
        this.serverTransactionTable=new Array();
        this.dialogTable=new Array();
    }
}

SIPTransactionStack.prototype.getMaxMessageSize =function(){
    return this.maxMessageSize;
}
SIPTransactionStack.prototype.getNextHop =function(sipRequest){
    if (this.useRouterForAll) {
        if (this.router != null) {
            return this.router.getNextHop(sipRequest);
        } 
        else {
            return null;
        }
    } 
    else {
        if (sipRequest.getRequestURI().isSipURI() || sipRequest.getRouteHeaders() != null) {
            return this.defaultRouter.getNextHop(sipRequest);
        } 
        else if (this.router != null) {
            return this.router.getNextHop(sipRequest);
        } 
        else {
            return null;
        }
    }   
}

SIPTransactionStack.prototype.setStackName =function(stackName){
    this.stackName = stackName;
}

SIPTransactionStack.prototype.setHostAddress =function(stackAddress){
    if (stackAddress.indexOf(':') != stackAddress.lastIndexOf(':')
        && stackAddress.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '').charAt(0) != '[') {
        this.stackAddress = '[' + stackAddress + ']';
    } else {
        this.stackAddress = stackAddress;
    }
    this.stackInetAddress = stackAddress;   
}

SIPTransactionStack.prototype.getHostAddress =function(){
    return this.stackAddress;  
}

SIPTransactionStack.prototype.setRouter =function(router){
    this.router = router;
}

SIPTransactionStack.prototype.getRouter =function(){
    if(arguments.length==0)
    {
        this.getRouterargu0();
    }
    else
    {
        var request=arguments[0];
        this.getRouterargu1(request);
    }
}

SIPTransactionStack.prototype.getRouterargu0 =function(){
    return this.router;
}

SIPTransactionStack.prototype.getRouterargu1 =function(request){
    if (request.getRequestLine() == null) {
        return this.defaultRouter;
    } 
    else if (this.useRouterForAll) {
        return this.router;
    }
    else {
        if (request.getRequestURI().getScheme()=="sip"
            || request.getRequestURI().getScheme()=="sips") {
            return this.defaultRouter;
        } else {
            if (this.router != null) {
                return this.router;
            } else {
                return this.defaultRouter;
            }
        }
    }       
}

SIPTransactionStack.prototype.removeMessageProcessor =function(oldMessageProcessor){
    var l=null;
    for(var i=0;i<this.messageProcessors.lengt;i++)
    {
        if (this.messageProcessors[i]==oldMessageProcessor) {
            l=i;
        }
    }
    if (l!=null) {
        this.messageProcessors.splice(l,1);
        oldMessageProcessor.stop();
    }
}

SIPTransactionStack.prototype.getMessageProcessors =function(){
    return this.messageProcessors;
}
SIPTransactionStack.prototype.isEventForked =function(ename){
    var l=null;
    for(var i=0;i<this.forkedEvents.length;i++)
    {
        if(this.forkedEvents[i]==ename)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransactionStack.prototype.getActiveClientTransactionCount =function(){
    return this.activeClientTransactionCount;    
}

SIPTransactionStack.prototype.isCancelClientTransactionChecked =function(){
    return this.cancelClientTransactionChecked; 
}

SIPTransactionStack.prototype.isRemoteTagReassignmentAllowed =function(){
    return this.remoteTagReassignmentAllowed;
}

SIPTransactionStack.prototype.getDialogs =function(){
    if(arguments.length==0)
    {
        return this.getDialogsargu0();
    }
    else
    {
        var state=arguments[0];
        return this.getDialogsargu1(state);
    }
}

SIPTransactionStack.prototype.getDialogsargu0 =function(){
    var dialogs = new Array();
    for(var i=0;i<this.dialogTable.length;i++)
    {
        var l=null;
        for(var x=0;x<dialogs.legnth;x++)
        {
            if(dialogs[x]==this.dialogTable[i][1])
            {
                l=i;
            }
        }
        if(l==null)
        {
            dialogs.push(this.dialogTable[i][1]);
        }
    }
    for(i=0;i<this.earlyDialogTable.length;i++)
    {
        l=null;
        for(x=0;x<dialogs.legnth;x++)
        {
            if(dialogs[x]==this.earlyDialogTable[i][1])
            {
                l=i;
            }
        }
        if(l==null)
        {
            dialogs.push(this.earlyDialogTable[i][1]);
        }
    }
    return dialogs;
}

SIPTransactionStack.prototype.getDialogsargu1 =function(state){
    var matchingDialogs = new Array();
    if ("EARLY"==state) {
        for(var i=0;i<this.earlyDialogTable.length;i++)
        {
            var l=null;
            for(var x=0;x<matchingDialogs.legnth;x++)
            {
                if(matchingDialogs[x]==this.earlyDialogTable[i][1])
                {
                    l=i;
                }
            }
            if(l==null)
            {
                matchingDialogs.push(this.earlyDialogTable[i][1]);
            }
        }
    }
    else {
        for(i=0;i<this.dialogTable.length;i++)
        {
            var dialog=this.dialogTable[i][1];
            if (dialog.getState() != null && dialog.getState()==state) {
                l=null;
                for(x=0;x<matchingDialogs.legnth;x++)
                {
                    if(matchingDialogs[x]==dialog)
                    {
                        l=i;
                    }
                }
                if(l==null)
                {
                    matchingDialogs.push(dialog);
                }
            }
        }
    }
    return matchingDialogs;     
}

SIPTransactionStack.prototype.setTimer =function(timer){
    this.timer = timer;
}

SIPTransactionStack.prototype.setDeliverDialogTerminatedEventForNullDialog =function(){
    this.isDialogTerminatedEventDeliveredForNullDialog = true;    
}
SIPTransactionStack.prototype.getAddressResolver =function(){
    return this.addressResolver;      
}/*
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
 *  Implementation of the JAIN-SIP SIPTransactionErrorEvent .
 *  @see  gov/nist/javax/sip/stack/SIPTransactionErrorEvent.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function SIPTransactionErrorEvent(sourceTransaction,transactionErrorID) {
    this.classname="SIPTransactionErrorEvent"; 
    this.serialVersionUID = "-2713188471978065031L";
    this.source=sourceTransaction;
    this.errorID=transactionErrorID;
}

SIPTransactionErrorEvent.prototype.TIMEOUT_ERROR = 1;
SIPTransactionErrorEvent.prototype.TRANSPORT_ERROR = 64;
SIPTransactionErrorEvent.prototype.TIMEOUT_RETRANSMIT = 64;

SIPTransactionErrorEvent.prototype.getErrorID =function(){
    return this.errorID;
}

SIPTransactionErrorEvent.prototype.getSource =function(){
    return this.source;
}/*
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
 *  Implementation of the JAIN-SIP DefaultRouter .
 *  @see  gov/nist/javax/sip/stack/DefaultRouter.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function DefaultRouter() {
    this.classname="DefaultRouter"; 
    this.sipStack=null;
    this.defaultRoute=null;
    if(arguments.length!=0)
    {
        var sipStack=arguments[0];
        var defaultRoute=arguments[1];
        this.sipStack = sipStack;
        if (defaultRoute != null) {
            this.defaultRoute =  this.sipStack.getAddressResolver().resolveAddress(new HopImpl(defaultRoute));
        }
    }
}

DefaultRouter.prototype.getNextHop =function(request){
    var sipRequest = request;
    var requestLine = sipRequest.getRequestLine();
    if (requestLine == null) {
        return this.defaultRoute;
    }
    var requestURI = requestLine.getUri();
    if (requestURI == null)
    {
        console.error("DefaultRouter:getNextHop(): bad message: Null requestURI");
        throw "ViaParser:getNextHop():  bad message: Null requestURI";
    }
    
    var routes = sipRequest.getRouteHeaders();
    if ((routes != null) && (routes.getFirst()!=null)) {
        var route = routes.getFirst();
        var uri = route.getAddress().getURI();
        if (uri.isSipURI()) {
            var sipUri = uri;
            if (!sipUri.hasLrParam()) {
                this.fixStrictRouting(sipRequest);
            }
            var hop = this.createHop(sipUri,request);
            return hop;
        } 
        else {
            console.error("DefaultRouter:getNextHop(): first Route not a SIP URI");
            throw "DefaultRouter:getNextHop(): first Route not a SIP URI";
        }
    }
    else if (requestURI.isSipURI()
        && requestURI.getMAddrParam() != null) {
        hop = this.createHop(requestURI,request);
        return hop;
    } 
    else if (this.defaultRoute != null) {
        return this.defaultRoute;
    } 
    else if (requestURI.isSipURI()) {
        hop = this.createHop(requestURI,request);
        return hop;
    } 
    else {
        return null;
    }
}

DefaultRouter.prototype.fixStrictRouting =function(req){
    var routes = req.getRouteHeaders();
    var first = routes.getFirst();
    var firstUri = first.getAddress().getURI();
    routes.removeFirst();
    var addr = new AddressImpl();
    addr.setAddress(req.getRequestURI()); // don't clone it
    var route = new Route(addr);
    routes.add(route); // as last one
    req.setRequestURI(firstUri);
}

DefaultRouter.prototype.createHop =function(sipUri,request){
    var transport = sipUri.getTransportParam();
    if (transport == null) {
        var via = request.getHeader("Via");
        transport = via.getTransport();
    }
    var port=null;
    if (sipUri.getPort() != -1) {
        port = sipUri.getPort();
    } else {
        port = 5060; // TCP or UDP
    }
    var host = sipUri.getMAddrParam() != null ? sipUri.getMAddrParam(): sipUri.getHost();
    var addressResolver = this.sipStack.getAddressResolver();
    return addressResolver.resolveAddress(new HopImpl(host, port, transport));
}

DefaultRouter.prototype.getOutboundProxy =function(){
    return this.defaultRoute;
}

DefaultRouter.prototype.getNextHops =function(request){
    try {
        var llist = new Array();
        llist.push(this.getNextHop(request));
        return llist();
    } catch (ex) {
        console.error("DefaultRouter:getNextHops(): catched exception:"+ex);
        return null;
    }
}
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
 *  Implementation of the JAIN-SIP WSMessageChannel .
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function WSMessageChannel() {
    this.classname="WSMessageChannel"; 
    this.myParser=null;
    this.key=null;
    this.isCached=null;
    this.isRunning=null;
    this.sipStack=null;
    this.wsurl=null;
    this.infoApp=null;
    this.messageProcessor=null;
    this.alive=true;
    this.websocket=null;
    this.requestsent=null;
    if(arguments.length!=0)
    {
        var sipStack=arguments[0];
        var msgProcessor=arguments[1];
        this.sipStack = sipStack;
        this.peerProtocol = "WS";
        this.messageProcessor = msgProcessor;
        this.myAddress = this.sipStack.getHostAddress();
        this.wsurl=this.messageProcessor.getURLWS();
        this.websocket=this.createWebSocket(this.wsurl);
    }
}

WSMessageChannel.prototype.isReliable =function(){
    return true;
}

WSMessageChannel.prototype.createWebSocket =function(wsurl){
    this.websocket=new WebSocket(wsurl,"sip");
    var wsmc=this;
    this.websocket.onclose=function()
    {
        console.warn("WSMessageChannel:createWebSocket(): the websocket is closed, reconnecting...");
        wsmc.sipStack.sipListener.processDisconnected();
        wsmc.websocket=null;
        this.alive=false;
    }
    
    this.websocket.onopen=function()
    {
        console.info("WSMessageChannel:createWebSocket(): the websocket is opened");
        wsmc.sipStack.sipListener.processConnected();
    }
    
    this.websocket.onerror=function(error)
    {
        console.error("WSMessageChannel:createWebSocket(): websocket connection has failed:"+error);
        wsmc.sipStack.sipListener.processConnectionError(error);
    }
    
    this.websocket.onmessage=function(event)
    {
        var data=event.data;
        wsmc.myParser=new WSMsgParser(wsmc.sipStack,data);
        wsmc.myParser.parsermessage(wsmc.requestsent); 
    }
    return this.websocket;
}

WSMessageChannel.prototype.close =function(){
    this.alive=false;
    this.websocket.close();
    this.websocket = null;
}

WSMessageChannel.prototype.getSIPStack =function(){
    return this.sipStack;
}

WSMessageChannel.prototype.getTransport =function(){
    return "WS";
}

WSMessageChannel.prototype.getURLWS =function(){
    return this.wsurl;
}

WSMessageChannel.prototype.getPeerProtocol =function(){
    return this.peerProtocol;
}

WSMessageChannel.prototype.sendMessage = function(sipMessage){
    if (sipMessage instanceof SIPRequest)
    {
        this.requestsent=sipMessage;
    }
    
    if(typeof sipMessage!="string")
    {
        var encodedSipMessage = sipMessage.encode();
        sipMessage=encodedSipMessage;
    }
    this.websocket.send(sipMessage);
    console.info("SIP message sent: "+sipMessage); 
}

WSMessageChannel.prototype.getKey =function(){
    if (this.key != null) {
        return this.key;
    } 
    else {
        var mc=new WSMessageChannel();
        this.key = mc.getKey(this.wsurl, "WS");
        return this.key;
    }
}

WSMessageChannel.prototype.getViaHost =function(){
    return this.myAddress;
}

WSMessageChannel.prototype.getViaHeader =function(){
    var channelViaHeader;
    channelViaHeader = new Via();
    channelViaHeader.setTransport(this.getTransport());
    channelViaHeader.setSentBy(this.getHostPort());
    return channelViaHeader;
}

WSMessageChannel.prototype.getViaHost =function(){
    return this.myAddress;
}

WSMessageChannel.prototype.getViaHostPort =function(){
    var retval = new HostPort();
    retval.setHost(new Host(this.getViaHost()));
    return retval;
}

WSMessageChannel.prototype.getPeerAddress =function(){
    if (this.peerAddress != null) {
        return this.peerAddress;
    } 
    else
    {
        return getHost();
    }
}

WSMessageChannel.prototype.getHost =function(){
    return this.sipStack.getHostAddress();
}

WSMessageChannel.prototype.createBadReqRes =function(badReq/*,pe*/){
    var buf = 512;
    buf=buf+"SIP/2.0 400 Bad Request";
    if (!this.copyViaHeaders(badReq, buf))
    {
        return null;
    }
    if (!this.copyHeader(this.CSeqHeader, badReq, buf))
    {
        return null;
    }
    if (!this.copyHeader(this.CallIdHeader, badReq, buf))
    {
        return null;
    }
    if (!this.copyHeader(this.FromHeader, badReq, buf))
    {
        return null;
    }
    if (!this.copyHeader(this.ToHeader, badReq, buf))
    {
        return null;
    }
    var toStart = buf.indexOf(ToHeader.NAME);
    if (toStart != -1 && buf.indexOf("tag", toStart) == -1) {
        buf=buf+";tag=badreq"
    }
    var mfi=new MessageFactoryImpl();
    var s = mfi.getDefaultServerHeader();
    if ( s != null ) {
        buf=buf+"\r\n" + s.toString();
    }
    var clength = badReq.length();
    var cth = new ContentType("message", "sipfrag");
    buf=buf+"\r\n" + cth.toString();
    var clengthHeader = new ContentLength(clength);
    buf=buf+"\r\n" + clengthHeader.toString();
    buf=buf+"\r\n\r\n" + badReq;
    return buf.toString();
}

WSMessageChannel.prototype.copyHeader =function(name,fromReq,buf){
    var start = fromReq.indexOf(name);
    if (start != -1) {
        var end = fromReq.indexOf("\r\n", start);
        if (end != -1) {
            buf=buf+fromReq.substring(start - 2, end);
            return true;
        }
    }
    return false;
}

WSMessageChannel.prototype.copyViaHeaders =function(fromReq,buf){
    var start = fromReq.indexOf(this.ViaHeader);
    var found = false;
    while (start != -1) {
        var end = fromReq.indexOf("\r\n", start);
        if (end != -1) {
            buf=buf+fromReq.substring(start - 2, end);
            found = true;
            start = fromReq.indexOf(this.ViaHeader, end);
        } else {
            return false;
        }
    }
    return found;
}

WSMessageChannel.prototype.getMessageProcessor =function(){
    return this.messageProcessor;
}

WSMessageChannel.prototype.isSecure =function(){
    return false;
}

WSMessageChannel.prototype.getWebSocket =function(){
    return this.websocket;
}/*
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
 *  Implementation of the JAIN-SIP WSMessageProcessor .
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function WSMessageProcessor() {
    this.classname="WSMessageProcessor"; 
    this.nConnections=null;
    this.isRunning=null;
    this.wsMessageChannels=new Array(); 
    this.incomingwsMessageChannels=null;
    this.websocket=null;
    this.useCount=0;
    if(arguments.length!=0)
    {
        var sipStack=arguments[0];
        this.sipStack = sipStack;
        this.wsurl=this.sipStack.getUrlWs();
    }
    this.sentByHostPort=new HostPort();
    this.sentByHostPort.setHost(new Host(this.sipStack.getHostAddress()));
    this.transport = "WS";
    this.sentBy=null;
    this.sentBySet=null;
}

WSMessageProcessor.prototype.start =function(){
    return this.run();
}

WSMessageProcessor.prototype.run =function(){
    this.incomingwsMessageChannels = new WSMessageChannel(this);
    return this.incomingwsMessageChannels;
}

WSMessageProcessor.prototype.getTransport =function(){
    return "WS";
}

WSMessageProcessor.prototype.getIncomingwsMessageChannels =function(){
    return this.incomingwsMessageChannels;
}

WSMessageProcessor.prototype.getSIPStack =function(){
    return this.sipStack;
}

WSMessageProcessor.prototype.getURLWS =function(){
    return this.wsurl;
}

WSMessageProcessor.prototype.getInfoApp =function(){
    return this.infoApp;
}

WSMessageProcessor.prototype.stop =function(){
    this.isRunning = false;
    this.incomingwsMessageChannels.close();
}

WSMessageProcessor.prototype.createMessageChannel =function(){
    this.incomingwsMessageChannels = new WSMessageChannel(this.sipStack,this);
    return this.incomingwsMessageChannels;
}

WSMessageProcessor.prototype.getMaximumMessageSize =function(){
    return 0x7fffffff;
}

WSMessageProcessor.prototype.getViaHeader =function(){
    var via = new Via();
    if (this.sentByHostPort != null) {
        via.setSentBy(this.sentByHostPort);
        via.setTransport(this.getTransport());
    } else {
        var host = new Host();
        host.setHostname(this.sipStack.getHostAddress());
        via.setHost(host);
        via.setTransport(this.getTransport());
    }
    var random=new Date();
    var viabranch="z9hG4bK"+new String(random.getTime());
    via.setBranch(viabranch);
    return via;
}

WSMessageProcessor.prototype.inUse =function(){
    if(this.useCount != 0)
    {
        return true;
    }
    else
    {
        return false;
    }
}

WSMessageProcessor.prototype.getDefaultTargetPort =function(){
    return 5060;
}

WSMessageProcessor.prototype.isSecure =function(){
    return false;
}

WSMessageProcessor.prototype.getListeningPoint =function(){
    return this.listeningPoint;
}

WSMessageProcessor.prototype.setListeningPoint =function(lp){
    this.listeningPoint = lp;
}

WSMessageProcessor.prototype.initialize =function(infoApp,transactionStack){
    this.sipStack = transactionStack;
    this.infoApp=infoApp;
}

WSMessageProcessor.prototype.setSentBy =function(sentBy){
    var ind = sentBy.indexOf(":");
    if (ind == -1) {
        this.sentByHostPort = new HostPort();
        this.sentByHostPort.setHost(new Host(sentBy));
    } else {
        this.sentByHostPort = new HostPort();
        this.sentByHostPort.setHost(new Host(sentBy.substring(0, ind)));
        var portStr = sentBy.substring(ind + 1);
        try {
            var port = portStr;
            this.sentByHostPort.setPort(port);
        } catch (ex) {
            console.error("WSMessageProcessor:setSentBy(): bad format encountered at ", ind);
            throw "WSMessageProcessor:setSentBy(): bad format encountered at "+ ind;
        }
    }
    this.sentBySet = true;
    this.sentBy = sentBy;
}

WSMessageProcessor.prototype.getSentBy =function(){
    if (this.sentBy == null && this.sentByHostPort != null) {
        this.sentBy = this.sentByHostPort.toString();
    }
    return this.sentBy;
}

WSMessageProcessor.prototype.getPort =function(){
    return this.port;
}

WSMessageProcessor.prototype.getDefaultPort =function(){
    var retval=5060
    return retval;
}/*
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
 *  Implementation of the JAIN-SIP SIPDialog .
 *  @see  gov/nist/javax/sip/stack/SIPDialog.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function SIPDialog() {
    this.classname="SIPDialog"; 
    
    this.serialVersionUID = "-1429794423085204069L";
    this.dialogTerminatedEventDelivered=null; 
    this.stackTrace=null;
    this.method=null;
    this.isAssigned=null;
    this.reInviteFlag=null;
    this.applicationData=null; 
    this.originalRequest=null;
    this.lastResponse=null;
    this.firstTransaction=null;
    this.lastTransaction=null;
    this.dialogId=null;
    this.earlyDialogId=null;
    this.localSequenceNumber=0;
    this.remoteSequenceNumber=-1;
    this.myTag=null;
    this.hisTag=null;
    this.routeList=new RouteList();
    this.sipStack=null;
    this.dialogState=this.NULL_STATE;
    this.ackSeen=false;
    this.lastAckSent=null;
    this.lastAckReceived=null;
    this.ackProcessed=false;
    this.timerTask=null;
    this.nextSeqno=null;
    this.retransmissionTicksLeft=null;
    this.prevRetransmissionTicks=null;
    this.originalLocalSequenceNumber=null;
    this.ackLine=null;
    this.auditTag = 0;
    this.localParty=null;
    this.remoteParty=null;
    this.callIdHeader=null;
    this.serverTransactionFlag=null;
    this.sipProvider=null;
    this.terminateOnBye=true;
    this.byeSent=null; // Flag set when BYE is sent, to disallow new
    this.remoteTarget=null;
    this.eventHeader=null; // for Subscribe notify
    this.lastInviteOkReceived=null;
    this.reInviteWaitTime = 100;
    this.dialogDeleteTask=null;
    this.timerdelete=null;
    this.dialogDeleteIfNoAckSentTask=null;
    this.isAcknowledged=null;
    this.highestSequenceNumberAcknowledged = -1;
    this.isBackToBackUserAgent=null;
    this.sequenceNumberValidation = true;
    this.eventListeners=new Array();
    this.firstTransactionSecure=null;
    this.firstTransactionSeen=null;
    this.firstTransactionMethod=null;
    this.firstTransactionId=null;
    this.firstTransactionIsServerTransaction=null;
    this.firstTransactionPort = 5060;
    this.contactHeader=null;
    this.timer=null;
    this.inviteTransaction=null;
    if(arguments[0].classname=="SipProviderImpl")
    {
        this.terminateOnBye = true;
        this.routeList = new RouteList();
        this.dialogState = this.NULL_STATE; // not yet initialized.
        this.localSequenceNumber = 0;
        this.remoteSequenceNumber = -1;
        this.sipProvider = arguments[0];  
        if(arguments.length==2)
        {
            sipResponse=arguments[1];
            this.sipStack = this.sipProvider.getSipStack();
            this.setLastResponse(null, sipResponse);
            this.localSequenceNumber = sipResponse.getCSeq().getSeqNumber();
            this.originalLocalSequenceNumber = this.localSequenceNumber;
            this.myTag = sipResponse.getFrom().getTag();
            this.hisTag = sipResponse.getTo().getTag();
            this.localParty = sipResponse.getFrom().getAddress();
            this.remoteParty = sipResponse.getTo().getAddress();
            this.method = sipResponse.getCSeq().getMethod();
            this.callIdHeader = sipResponse.getCallId();
            this.serverTransactionFlag = false;
            this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;
            this.addEventListener(this.sipStack); 
        }
    }
    else if(arguments[0].classname!="SipProviderImpl")
    {
        var transaction=arguments[0];
        this.sipProvider = transaction.getSipProvider();
        this.firstTransactionSeen = false;
        this.terminateOnBye = true;
        this.routeList = new RouteList();
        this.dialogState = this.NULL_STATE; // not yet initialized.
        this.localSequenceNumber = 0;
        this.remoteSequenceNumber = -1;
        var sipRequest = transaction.getRequest();
        this.callIdHeader = sipRequest.getCallId();
        this.earlyDialogId = sipRequest.getDialogId(false);
        if (transaction == null) {
            console.error("SIPDialog:SIPDialog(): null transcation argument");
            throw "SIPDialog:SIPDialog(): null transcation argument";
        }
        this.sipStack = transaction.sipStack;
        if (this.sipProvider == null) {
            console.error("SIPDialog:SIPDialog(): null sip provider argument");
            throw "SIPDialog:SIPDialog(): null sip provider argument";
        }
        this.addTransaction(transaction);
        if(arguments.length==2)
        {
            var sipResponse=arguments[1];
            if (sipResponse == null) {
                console.error("SIPDialog:SIPDialog(): null sip response argument");
                throw "SIPDialog:SIPDialog(): null sip response argument";
            }
            this.setLastResponse(transaction, sipResponse);
            this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;      
        }
        this.isBackToBackUserAgent = this.sipStack.isBackToBackUserAgent;
        this.addEventListener(this.sipStack);
    }
}

SIPDialog.prototype.NULL_STATE=-1;
SIPDialog.prototype.EARLY_STATE=0;
SIPDialog.prototype.CONFIRMED_STATE=1;
SIPDialog.prototype.TERMINATED_STATE=2;
SIPDialog.prototype.DIALOG_LINGER_TIME=8;
SIPDialog.prototype.TimeStampHeader="Timestamp";
SIPDialog.prototype.TIMER_H=64;
SIPDialog.prototype.TIMER_J=64;
SIPDialog.prototype.BASE_TIMER_INTERVAL=500;

var timer=this.timer;
var sipdialog=null;
var variabletransaction=null;
var variabledialog=null;
var variablereinvite=null;
function lingerTimerDialog(){
    var dialog = sipdialog;
    if (this.eventListeners != null) {
        this.eventListeners.clear();
    }
    this.timerTaskLock = null;
    dialog.sipStack.removeDialog(dialog);
}
function DialogDeleteTask(){
    sipdialog.dialogDeleteTask=null;
}
function DialogTimerTask(){
    this.transaction=null;
    if(variabletransaction!=null)
    {
        this.transaction = variabletransaction;
    }
    var dialog = sipdialog;
    var transaction = sipdialog.transaction;
    if ((!dialog.ackSeen) && (transaction != null)) {
        var response = transaction.getLastResponse();
        if (response.getStatusCode() == 200) {
            transaction.fireTimer();
        }
    }
    if (dialog.isAckSeen() || dialog.dialogState == 2) {
        sipdialog.transaction = null;
        clearTimeout(sipdialog.timer);
    }
}

function DialogDeleteIfNoAckSentTask(seqno){
    this.seqno = seqno;
    var dialog = sipdialog;
    if (dialog.highestSequenceNumberAcknowledged < seqno) {
        dialog.dialogDeleteIfNoAckSentTask = null;
        if (!dialog.isBackToBackUserAgent) {
            if (dialog.sipProvider.getSipListener() instanceof SipListener) {
                this.raiseErrorEvent(SIPDialogErrorEvent.DIALOG_ACK_NOT_SENT_TIMEOUT);
            } else {
                this.delet();
            }
        } else {
            if (dialog.sipProvider.getSipListener() instanceof SipListener) {
                this.raiseErrorEvent(SIPDialogErrorEvent.DIALOG_ACK_NOT_SENT_TIMEOUT);
            } 
            else {
                try {
                    var byeRequest = dialog.createRequest("BYE");
                    var mfi=new MessageFactoryImpl();
                    if (mfi.getDefaultUserAgentHeader() != null) {
                        byeRequest.addHeader(mfi.getDefaultUserAgentHeader());
                    }
                    var reasonHeader = new Reason();
                    reasonHeader.setProtocol("SIP");
                    reasonHeader.setCause(1025);
                    reasonHeader.setText("Timed out waiting to send ACK");
                    byeRequest.addHeader(reasonHeader);
                    var byeCtx = dialog.getSipProvider().getNewClientTransaction(byeRequest);
                    dialog.sendRequest(byeCtx);
                    return;
                } catch (ex) {
                    console.error("SIPDialog:DialogDeleteIfNoAckSentTask(): catched exception:"+ex);
                    dialog.delet();
                }
            }
        }
    }
}

SIPDialog.prototype.ackReceived =function(sipRequest){
    if (this.ackSeen) {
        return;
    }
    var tr = this.getInviteTransaction();
    if (tr != null) {
        if (tr.getCSeq() == sipRequest.getCSeq().getSeqNumber()) {
            this.ackSeen = true;
            this.setLastAckReceived(sipRequest);
            this.setRemoteTag(sipRequest.getFromTag());
            this.setLocalTag(sipRequest.getToTag());
            this.setDialogId(sipRequest.getDialogId(true));
            this.addRoute(sipRequest)
            this.setState(this.CONFIRMED_STATE);
            this.sipStack.putDialog(this);
        }
    }
}

SIPDialog.prototype.isTerminatedOnBye =function(){
    if(this.terminateOnBye)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPDialog.prototype.setState =function(state){
    this.dialogState = state;
    if (state == this.TERMINATED_STATE) {
        if (this.sipStack.getTimer() != null) { 
            this.timer=this.sipStack.getTimer();
            sipdialog=this;
            this.timer=setTimeout("lingerTimerDialog()", this.DIALOG_LINGER_TIME * 1000);
        }
    }
}

SIPDialog.prototype.getLocalTag =function(){
    return this.myTag;
}

SIPDialog.prototype.isAckSeen =function(){
    return this.ackSeen;
}

SIPDialog.prototype.getLastAckSent =function(){
    return this.lastAckSent;
}

SIPDialog.prototype.resendAck =function(){
    if (this.getLastAckSent() != null) {
        if (this.getLastAckSent().getHeader(this.TimeStampHeader) != null
            && this.sipStack.generateTimeStampHeader) {
            var ts = new TimeStamp();
            var d=new Date();
            ts.setTimeStamp(d.getTime());
            this.getLastAckSent().setHeader(ts);
        }
        this.sendAck(this.getLastAckSent(), false);
    }
}

SIPDialog.prototype.getMethod =function(){
    return this.method;
}

SIPDialog.prototype.isBackToBackUserAgent =function(){
    return this.isBackToBackUserAgent;
}

SIPDialog.prototype.getState =function(){
    var x=null;
    if (this.dialogState == this.NULL_STATE) {
        x=null; // not yet initialized
    }
    if(this.dialogState==0)
    {
        x="EARLY";
    }
    else if(this.dialogState==1)
    {
        x="CONFIRMED";
    }
    else if(this.dialogState==2)
    {
        x="TERMINATED";
    }
    return x;
}

SIPDialog.prototype.delet =function(){
    this.setState(this.TERMINATED_STATE);
}

SIPDialog.prototype.isTerminatedOnBye =function(){
    return this.terminateOnBye;
}

SIPDialog.prototype.getLastResponse =function(){
    return this.lastResponse;
}

SIPDialog.prototype.setLastResponse =function(transaction,sipResponse){
    this.callIdHeader = sipResponse.getCallId();
    var statusCode = sipResponse.getStatusCode();
    if (statusCode == 100) {
        return;
    }
    this.lastResponse = sipResponse;
    this.setAssigned();
    if (this.getState() == "TERMINATED") {
        if (sipResponse.getCSeq().getMethod()=="INVITE" && statusCode == 200) {
            this.lastInviteOkReceived = Math.max(sipResponse.getCSeq().getSeqNumber(),
                this.lastInviteOkReceived);
        }
        return;
    }
    var cseqMethod = sipResponse.getCSeq().getMethod();
    if (transaction == null || transaction instanceof SIPClientTransaction) {
        if (this.sipStack.isDialogCreated(cseqMethod)) {
            if (this.getState() == null && (100 <= statusCode && statusCode <= 199)) {
                this.setState(this.EARLY_STATE);
                if ((sipResponse.getToTag() != null || this.sipStack.rfc2543Supported)
                    && this.getRemoteTag() == null) {
                    this.setRemoteTag(sipResponse.getToTag());
                    this.setDialogId(sipResponse.getDialogId(false));
                    this.sipStack.putDialog(this);
                    this.addRoute(sipResponse);
                }
            } else if (this.getState() != null && this.getState()=="EARLY" && 100 <= statusCode && statusCode <= 199) {
                if (cseqMethod==this.getMethod() && transaction != null
                    && (sipResponse.getToTag() != null || this.sipStack.rfc2543Supported)) {
                    this.setRemoteTag(sipResponse.getToTag());
                    this.setDialogId(sipResponse.getDialogId(false));
                    this.sipStack.putDialog(this);
                    this.addRoute(sipResponse);
                }
            } else if (200 <= statusCode && statusCode <= 299) {
                if (cseqMethod==this.getMethod()
                    && (sipResponse.getToTag() != null || this.sipStack.rfc2543Supported)
                    && this.getState() != "CONFIRMED") {
                    this.setRemoteTag(sipResponse.getToTag());
                    this.setDialogId(sipResponse.getDialogId(false));
                    this.sipStack.putDialog(this);
                    this.addRoute(sipResponse);
                    this.setState(this.CONFIRMED_STATE);
                }
                if (cseqMethod=="INVITE") {
                    this.lastInviteOkReceived = Math.max(sipResponse.getCSeq().getSeqNumber(),
                        this.lastInviteOkReceived);
                }
            } 
            else if (statusCode >= 300&& statusCode <= 699
                && (this.getState() == null || 
                    (cseqMethod==this.getMethod() && this.getState()== "EARLY"))) {
                this.setState(this.TERMINATED_STATE);
            }
            if (this.getState() != "CONFIRMED" && this.getState() != "TERMINATED") {
                if (this.originalRequest != null) {
                    var rrList = this.originalRequest.getRecordRouteHeaders();
                    if (rrList != null) {
                        for(var i=rrList.length;i>=0;i--)
                        {
                            var rr = rrList[i];
                            var route = this.routeList.getFirst();
                            if (route != null && rr.getAddress()==route.getAddress()) {
                                this.routeList.removeFirst();
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        } else if (cseqMethod=="NOTIFY"
            && (this.getMethod()=="SUBSCRIBE" || this.getMethod()=="REFER") &&
            sipResponse.getStatusCode() / 100 == 2
            && this.getState() == null) {

            this.setDialogId(sipResponse.getDialogId(true));
            this.sipStack.putDialog(this);
            this.setState(this.CONFIRMED_STATE);
        } else if (cseqMethod=="BYE" && statusCode / 100 == 2
            && this.isTerminatedOnBye()) {
            this.setState(this.TERMINATED_STATE);
        }
    } 
    else {
        if (cseqMethod=="BYE" && statusCode / 100 == 2
            && this.isTerminatedOnBye()) {
            this.setState(this.TERMINATED_STATE);
        } else {
            var doPutDialog = false;
            if (this.getLocalTag() == null && sipResponse.getTo().getTag() != null
                && this.sipStack.isDialogCreated(cseqMethod) && cseqMethod==this.getMethod()) {
                this.setLocalTag(sipResponse.getTo().getTag());
                doPutDialog = true;
            }
            if (statusCode / 100 != 2) {
                if (statusCode / 100 == 1) {
                    if (doPutDialog) {
                        this.setState(this.EARLY_STATE);
                        this.setDialogId(sipResponse.getDialogId(true));
                        this.sipStack.putDialog(this);
                    }
                } 
                else {
                    if (!this.isReInvite() && this.getState() != this.CONFIRMED) {
                        this.setState(SIPDialog.TERMINATED_STATE);
                    }
                }
            } else {
                if (this.dialogState <= this.EARLY_STATE && 
                    (cseqMethod=="INVITE"|| cseqMethod=="SUBSCRIBE" || cseqMethod=="REFER")) {
                    this.setState(this.CONFIRMED_STATE);
                }
                if (doPutDialog) {
                    this.setDialogId(sipResponse.getDialogId(true));
                    this.sipStack.putDialog(this);
                }
            }
        }
    }
}

SIPDialog.prototype.getDialogId =function(){
    if (this.dialogId == null && this.lastResponse != null) {
        this.dialogId = this.lastResponse.getDialogId(this.isServer());
    }
    return this.dialogId;
}

SIPDialog.prototype.isAssignedFunction =function(){
    return this.isAssigned;
}

SIPDialog.prototype.setResponseTags =function(sipResponse){
    if (this.getLocalTag() != null || this.getRemoteTag() != null) {
        return;
    }
    var responseFromTag = sipResponse.getFromTag();
    if (responseFromTag != null) {
        if (responseFromTag==this.getLocalTag()) {
            sipResponse.setToTag(this.getRemoteTag());
        } 
        else if (responseFromTag==this.getRemoteTag()) {
            sipResponse.setToTag(this.getLocalTag());
        }
    } 
}

SIPDialog.prototype.getSipProvider =function(){
    return this.sipProvider;
}

SIPDialog.prototype.sendAck =function(request){
    var ackRequest = request;
    if (!ackRequest.getMethod()=="ACK") {
        console.error("SIPDialog:sendAck(): bad request method -- should be ACK");
        throw "SIPDialog:sendAck(): bad request method -- should be ACK";
    }
    if (this.getState() == null || this.getState() == "EARLY") {
        console.error("SIPDialog:sendAck(): bad dialog state " + this.getState())
        throw "SIPDialog:sendAck(): bad dialog state " + this.getState();
    }
    
    if (this.getCallId().getCallId()!=request.getCallId().getCallId()) {
        console.error("SIPDialog:sendAck(): bad call ID in request");
        throw "SIPDialog:sendAck(): bad call ID in request";
    }
    try {
        if (this.getLocalTag() != null) {
            ackRequest.getFrom().setTag(this.getLocalTag());
        }
        if (this.getRemoteTag() != null) {
            ackRequest.getTo().setTag(this.getRemoteTag());
        }
    } catch (ex) {
        console.error("SIPDialog:sendAck(): catched exception:"+ex);
        throw "SIPDialog:sendAck(): catched exception:"+ex;
    }
    
    var hop = this.sipStack.getNextHop(ackRequest);
    if (hop == null) {
        console.error("SIPDialog:sendAck(): no route!");
        throw "SIPDialog:sendAck(): no route!";
    }
    var lp = this.sipProvider.getListeningPoint();
    if (lp == null) {
        console.error("SIPDialog:sendAck(): no listening point for this provider registered at " + hop);
        throw "SIPDialog:sendAck(): no listening point for this provider registered at " + hop;
    }
    var messageChannel = lp.getMessageProcessor().getIncomingwsMessageChannels();
    this.setLastAckSent(ackRequest);
    messageChannel.sendMessage(ackRequest);
    this.isAcknowledged = true;
    this.highestSequenceNumberAcknowledged = Math.max(this.highestSequenceNumberAcknowledged,
        ackRequest.getCSeq().getSeqNumber());
    if (this.dialogDeleteTask != null) {
    //this.dialogDeleteTask.cancel();
    //this.dialogDeleteTask = null;
    }
    this.ackSeen = true;  
    var encodedSipMessage = request.encode();
    console.info("SIP message sent: "+encodedSipMessage); 
}

SIPDialog.prototype.getRemoteTag =function(){
    return this.hisTag;
}

SIPDialog.prototype.isServer =function(){
    if (this.firstTransactionSeen == false) {
        return this.serverTransactionFlag;
    } 
    else {
        return this.firstTransactionIsServerTransaction;
    }
}

SIPDialog.prototype.addTransaction =function(transaction){
    var sipRequest = transaction.getOriginalRequest();
    if (this.firstTransactionSeen && this.firstTransactionId!=(transaction.getBranchId())
        && transaction.getMethod()==this.firstTransactionMethod) {
        this.reInviteFlag = true;
    }
    
    if (this.firstTransactionSeen == false) {
        this.storeFirstTransactionInfo(this, transaction);
        if (sipRequest.getMethod()=="SUBSCRIBE") {
            this.eventHeader = sipRequest.getHeader("Event");
        }
        this.setLocalParty(sipRequest);
        this.setRemoteParty(sipRequest);
        this.setCallId(sipRequest);
        if (this.originalRequest == null) {
            this.originalRequest = sipRequest;
        }
        if (this.method == null) {
            this.method = sipRequest.getMethod();
        }
        if (transaction instanceof SIPServerTransaction) {
            this.hisTag = sipRequest.getFrom().getTag();
        } else {
            this.setLocalSequenceNumber(sipRequest.getCSeq().getSeqNumber());
            this.originalLocalSequenceNumber = this.localSequenceNumber;
            this.myTag = sipRequest.getFrom().getTag();
        }
    } 
    else if (transaction.getMethod()==this.firstTransactionMethod
        && this.firstTransactionIsServerTransaction != transaction.isServerTransaction()) {
        this.storeFirstTransactionInfo(this, transaction);
        this.setLocalParty(sipRequest);
        this.setRemoteParty(sipRequest);
        this.setCallId(sipRequest);
        this.originalRequest = sipRequest;
        this.method = sipRequest.getMethod();
    }
    if (transaction instanceof SIPServerTransaction) {
        this.setRemoteSequenceNumber(sipRequest.getCSeq().getSeqNumber());
    }
    this.lastTransaction = transaction;
}

SIPDialog.prototype.storeFirstTransactionInfo =function(dialog,transaction){
    dialog.firstTransaction = transaction;
    dialog.firstTransactionSeen = true;
    dialog.firstTransactionIsServerTransaction = transaction.isServerTransaction();
    dialog.firstTransactionSecure = true;
    //dialog.firstTransactionPort = transaction.getPort();
    dialog.firstTransactionId = transaction.getBranchId();
    dialog.firstTransactionMethod = transaction.getMethod();
    if (dialog.isServer()) {
        var st = transaction;
        var response = st.getLastResponse();
        dialog.contactHeader = response != null ? response.getContactHeader() : null;
    } else {
        var ct = transaction;
        if (ct != null) {
            var sipRequest = ct.getOriginalRequest();
            dialog.contactHeader = sipRequest.getContactHeader();
        }
    }
}

SIPDialog.prototype.setLocalParty =function(sipMessage){
    if (!this.isServer()) {
        this.localParty = sipMessage.getFrom().getAddress();
    } else {
        this.localParty = sipMessage.getTo().getAddress();
    }
}

SIPDialog.prototype.setRemoteParty =function(sipMessage){
    if (!this.isServer()) {
        this.remoteParty = sipMessage.getTo().getAddress();
    } else {
        this.remoteParty = sipMessage.getFrom().getAddress();
    }
}

SIPDialog.prototype.setCallId =function(sipRequest){
    this.callIdHeader = sipRequest.getCallId();
}

SIPDialog.prototype.setLocalSequenceNumber =function(lCseq){
    this.localSequenceNumber = lCseq;
}

SIPDialog.prototype.getLocalParty =function(){
    return this.localParty;
}

SIPDialog.prototype.getRemoteParty =function(){
    return this.remoteParty;
}

SIPDialog.prototype.addEventListener =function(newListener){
    var l=null;
    for(var i=0;i<this.eventListeners;i++)
    {
        if(this.eventListeners[i]==newListener)
        {
            l=i;
        }
    }
    if(l==null)
    {
        this.eventListeners.push(newListener);
    }
}

SIPDialog.prototype.setAssigned =function(){
    this.isAssigned = true;
}

SIPDialog.prototype.testAndSetIsDialogTerminatedEventDelivered =function(){
    var retval = this.dialogTerminatedEventDelivered;
    this.dialogTerminatedEventDelivered = true;
    return retval;
}

SIPDialog.prototype.getFirstTransaction =function(){
    return this.firstTransaction;
}

SIPDialog.prototype.isClientDialog =function(){
    var transaction = this.getFirstTransaction();
    if(transaction instanceof SIPClientTransaction)
    {
        return true
    }
    else
    {
        return false;
    }
}

SIPDialog.prototype.addRoute =function(){
    if(arguments[0] instanceof SIPResponse)
    {
        var sipResponse=arguments[0];
        this.addRouteResponse(sipResponse);
    }
    else if(arguments[0] instanceof SIPRequest)
    {
        var sipRequest=arguments[0];
        this.addRouteRequest(sipRequest);
    }
    else
    {
        var recordRouteList=arguments[0];
        this.addRouteList(recordRouteList);
    }
}

SIPDialog.prototype.addRouteResponse =function(sipResponse){
    if (sipResponse.getStatusCode() == 100) {
        return;
    } 
    else if (this.dialogState == 2) {
        return;
    } 
    else if (this.dialogState == 1) {
        if (200<=sipResponse.getStatusCode() && sipResponse.getStatusCode()<=299 && !this.isServer()) {
            var contactList = sipResponse.getContactHeaders();
            var request=new SIPRequest();
            if (contactList != null && request.isTargetRefresh(sipResponse.getCSeq().getMethod())) {
                this.setRemoteTarget(contactList.getFirst());
            }
        }
        return;
    }
    if (!this.isServer()) {
        if (this.getState() != "CONFIRMED"&& this.getState() != "TERMINATED") {
            var rrlist = sipResponse.getRecordRouteHeaders();
            if (rrlist != null) {
                this.addRoute(rrlist);
            } 
            else {
                this.routeList = new RouteList();
            }
        }
        //        contactList = sipResponse.getContactHeaders();
        if (contactList != null) {
            this.setRemoteTarget(contactList.getFirst());
        }
    }
}

SIPDialog.prototype.addRouteRequest =function(sipRequest){
    var siprequest=new SIPRequest();
    if (this.dialogState == "CONFIRMED"&& siprequest.isTargetRefresh(sipRequest.getMethod())) {
        this.doTargetRefresh(sipRequest);
    }
    if (this.dialogState == "CONFIRMED" || this.dialogState == "TERMINATED") {
        return;
    }
    if (sipRequest.getToTag() != null) {
        return;
    }
    var rrlist = sipRequest.getRecordRouteHeaders();
    if (rrlist != null) {
        this.addRoute(rrlist);
    } 
    else {
        this.routeList = new RouteList();
    }
    var contactList = sipRequest.getContactHeaders();
    if (contactList != null) {
        this.setRemoteTarget(contactList.getFirst());
    }
}

SIPDialog.prototype.addRouteList =function(recordRouteList){
    if (this.isClientDialog()) {
        this.routeList = new RouteList();
        for(var i=recordRouteList.getHeaderList().length-1;i>=0;i--)
        {
            var rr = recordRouteList.getHeaderList()[i];
            var route = new Route();
            var address =  rr.getAddress();

            route.setAddress(address);
            route.setParameters(rr.getParameters());
            this.routeList.add(route);
        }
    } 
    else {
        this.routeList = new RouteList();
        for(i=0;i<recordRouteList.length;i--)
        {
            rr = recordRouteList[i];
            route = new Route();
            address =  rr.getAddress();

            route.setAddress(address);
            route.setParameters(rr.getParameters());

            this.routeList.add(route);
        }
    }
}

SIPDialog.prototype.setRemoteTarget =function(contact){
    this.remoteTarget = contact.getAddress();
}

SIPDialog.prototype.getRouteList =function(){
    return this.routeList;
}

SIPDialog.prototype.setRouteList =function(routeList){
    this.routeList = routeList;
}

SIPDialog.prototype.setStack =function(sipStack){
    this.sipStack = sipStack;
}

SIPDialog.prototype.getStack =function(){
    return this.sipStack;
}

SIPDialog.prototype.removeEventListener =function(oldListener){
    var l=null;
    for(var i=0;i<this.eventListeners.length;i++)
    {
        if(this.eventListeners[i]==oldListener)
        {
            l=i;
        }
    }
    this.eventListeners.splice(l,1);
}

SIPDialog.prototype.setApplicationData =function(applicationData){
    this.applicationData = applicationData;
}

SIPDialog.prototype.getApplicationData =function(){
    return this.applicationData;
}

SIPDialog.prototype.requestConsumed =function(){
    this.nextSeqno = this.getRemoteSeqNumber() + 1;
}

SIPDialog.prototype.isRequestConsumable =function(dialogRequest){
    if (dialogRequest.getMethod()=="ACK") {
        console.error("SIPDialog:isRequestConsumable(): Illegal method");
        throw "SIPDialog:isRequestConsumable(): Illegal method";
    }
    if (!this.isSequnceNumberValidation()) {
        return true;
    }
    if(this.remoteSequenceNumber <= dialogRequest.getCSeq().getSeqNumber())
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPDialog.prototype.doDeferredDelete =function(){
    if (this.sipStack.getTimer() == null) {
        this.setState(this.TERMINATED_STATE);
    } else {
        this.timerdelete=this.sipStack.getTimer();
        sipdialog=this;
        this.timerdelete=setTimeout(function(){
            sipdialog.dialogDeleteTask = new DialogDeleteTask();
        },this.TIMER_H * this.BASE_TIMER_INTERVAL);
    }
}

SIPDialog.prototype.isAckSent =function(cseqNo){
    if (this.getLastTransaction() == null) {
        return true;
    }
    if (this.getLastTransaction() instanceof SIPClientTransaction) {
        if (this.getLastAckSent() == null) {
            return false;
        } 
        else {
            return cseqNo <= this.getLastAckSent().getCSeq().getSeqNumber();
        }
    }
    else {
        return true;
    }
}

SIPDialog.prototype.getRouteSet =function(){
    if (this.routeList == null) {
        this.routeList=new Array();
        return this.routeList;
    } 
    else {
        return this.getRouteList();
    }
}

SIPDialog.prototype.setDialogId =function(dialogId){
    this.dialogId = dialogId;
}

SIPDialog.prototype.createFromNOTIFY =function(subscribeTx,notifyST){
    var d = new SIPDialog(notifyST);
    d.serverTransactionFlag = false;
    d.lastTransaction = subscribeTx;
    storeFirstTransactionInfo(d, subscribeTx);
    d.terminateOnBye = false;
    d.localSequenceNumber = subscribeTx.getCSeq();
    var not = notifyST.getRequest();
    d.remoteSequenceNumber = not.getCSeq().getSeqNumber();
    d.setDialogId(not.getDialogId(true));
    d.setLocalTag(not.getToTag());
    d.setRemoteTag(not.getFromTag());
    d.setLastResponse(subscribeTx, subscribeTx.getLastResponse());
    d.localParty = not.getTo().getAddress();
    d.remoteParty = not.getFrom().getAddress();
    d.addRoute(not);
    d.setState(this.CONFIRMED_STATE); 
    return d;
}

SIPDialog.prototype.isReInvite =function(){
    return this.reInviteFlag;
}

SIPDialog.prototype.setRemoteTag =function(hisTag){
    if (this.hisTag != null && hisTag != null && hisTag!=this.hisTag) {
        if (this.getState() != "EARLY") {
            return;
        } 
        else if (this.sipStack.isRemoteTagReassignmentAllowed()) {
            var removed = false;
            if (this.sipStack.getDialog(this.dialogId) == this) {
                this.sipStack.removeDialog(this.dialogId);
                removed = true;
            }
            this.dialogId = null;
            this.hisTag = hisTag;
            if (removed) {
                this.sipStack.putDialog(this);
            }
        }
    }
    else {
        if (hisTag != null) {
            this.hisTag = hisTag;
        } 
    }
}

SIPDialog.prototype.getLastTransaction =function(){
    return this.lastTransaction;
}

SIPDialog.prototype.getInviteTransaction =function(){
    return this.inviteTransaction;
}

SIPDialog.prototype.setInviteTransaction =function(transaction){
    this.inviteTransaction=transaction;
}

SIPDialog.prototype.setLocalSequenceNumber =function(lCseq){
    if (lCseq <= this.localSequenceNumber) {
        console.error("SIPDialog:setLocalSequenceNumber(): sequence number should not decrease !");
        throw "SIPDialog:setLocalSequenceNumber(): sequence number should not decrease !";
    }
    this.localSequenceNumber = lCseq;
}

SIPDialog.prototype.setRemoteSequenceNumber =function(rCseq){
    this.remoteSequenceNumber = rCseq;
}

SIPDialog.prototype.incrementLocalSequenceNumber =function(){
    ++this.localSequenceNumber;
}
SIPDialog.prototype.getOriginalLocalSequenceNumber =function(){
    return this.originalLocalSequenceNumber;
}
SIPDialog.prototype.getLocalSeqNumber =function(){
    return this.localSequenceNumber;
}
SIPDialog.prototype.getRemoteSeqNumber =function(){
    return this.remoteSequenceNumber;
}
SIPDialog.prototype.setLocalTag =function(mytag){
    this.myTag = mytag;
}
SIPDialog.prototype.getCallId =function(){
    return this.callIdHeader;
}
SIPDialog.prototype.getRemoteTarget =function(){
    return this.remoteTarget;
}
SIPDialog.prototype.isSecure =function(){
    return this.firstTransactionSecure;
}
SIPDialog.prototype.createRequest =function(){
    if(arguments.length==1)
    {
        var method=arguments[0];
        return this.createRequestargu1(method);
    }
    else
    {
        method=arguments[0];
        var sipResponse=arguments[1];
        return this.createRequestargu2(method, sipResponse);
    }
}
SIPDialog.prototype.createRequestargu1 =function(method){
    if (method=="ACK") {
        console.error("SIPDialog:createRequestargu1(): invalid method specified for createRequest:" + method);
        throw "SIPDialog:createRequestargu1(): invalid method specified for createRequest:" + method;
    }
    if (this.lastResponse != null) {
        return this.createRequest(method, this.lastResponse);
    } 
    else {
        console.error("SIPDialog:createRequestargu1(): dialog not yet established -- no response!");
        throw "SIPDialog:createRequestargu1(): dialog not yet established -- no response!";
    }
}

SIPDialog.prototype.createRequestargu2 =function(method,sipResponse){
    if (method == null || sipResponse == null) {
        console.error("SIPDialog:createRequestargu2(): null argument");
        throw "SIPDialog:createRequestargu2(): null argument";
    }
    if (method=="CANCEL") {
        console.error("SIPDialog:createRequestargu2(): invalid request");
        throw "SIPDialog:createRequestargu2(): invalid request";
    }
    if (this.getState() == null
        || (this.getState() == "TERMINATED" && method.toUpperCase()!="BYE")
        || (this.isServer() && this.getState() == "EARLY" && method.toUpperCase()=="BYE")) {
        console.error("SIPDialog:createRequestargu2(): dialog  " + getDialogId()+" not yet established or terminated " + this.getState());
        throw "SIPDialog:createRequestargu2(): dialog  " + getDialogId()+" not yet established or terminated " + this.getState();
    }
    var sipUri = null;
    if (this.getRemoteTarget() != null) {
        sipUri = this.getRemoteTarget().getURI();
    } 
    else {
        sipUri = this.getRemoteParty().getURI();
        sipUri.clearUriParms();
    }
    if(this.isServer())
    {
        var contactHeader=this.getInviteTransaction().getOriginalRequest().getContactHeader();
        sipUri=contactHeader.getAddress().getURI();
    }
    else
    {
        sipUri=sipResponse.getContactHeader().getAddress().getURI();
    }
    var cseq = new CSeq();
    cseq.setMethod(method);
    //this.getLocalSeqNumber()+1
    cseq.setSeqNumber(this.getLocalSeqNumber()+1);
    if (method=="SUBSCRIBE") {
        if (this.eventHeader != null) {
            sipRequest.addHeader(this.eventHeader);
        }
    }
    var lp = this.sipProvider.getListeningPoint();
    if (lp == null) {
        
        console.error("SIPDialog:createRequestargu2(): cannot find listening point for transport " + sipResponse.getTopmostVia().getTransport());
        throw "SIPDialog:createRequestargu2(): cannot find listening point for transport " + sipResponse.getTopmostVia().getTransport();
    }
    var via = lp.getViaHeader();
    var from = new From();
    from.setAddress(this.localParty);
    var to = new To();
    to.setAddress(this.remoteParty);
    if (this.getLocalTag() != null) {
        from.setTag(this.getLocalTag());
    } else {
        from.removeTag();
    }
    if (this.getRemoteTag() != null) {
        to.setTag(this.getRemoteTag());
    } else {
        to.removeTag();
    }
    var sipRequest = sipResponse.createRequest(sipUri, via, cseq, from, to);
    
    var siprq=new SIPRequest();
    if (siprq.isTargetRefresh(method)) {
        var contactHeader = this.sipProvider.getListeningPoint().createContactHeader();
        contactHeader.getAddress().getURI().setSecure(this.isSecure());
        sipRequest.setHeader(contactHeader);
    }
    this.updateRequest(sipRequest);
    return sipRequest;
}

SIPDialog.prototype.sendRequest =function(clientTransactionId){
    var dialogRequest =  clientTransactionId.getOriginalRequest();
    if (clientTransactionId == null) {
        console.error("SIPDialog:sendRequest(): null parameter");
        throw "SIPDialog:sendRequest(): null parameter";
    }
    
    if (dialogRequest.getMethod()=="ACK" || dialogRequest.getMethod()=="CANCEL") {
        console.error("SIPDialog:sendRequest(): bad request method. " + dialogRequest.getMethod());
       throw "SIPDialog:sendRequest(): bad request method. " + dialogRequest.getMethod();
    }
    
    if (this.byeSent && this.isTerminatedOnBye() && dialogRequest.getMethod()!="BYE") {
        console.error("SIPDialog:sendRequest(): cannot send request; BYE already sent");
        throw "SIPDialog:sendRequest(): cannot send request; BYE already sent";
    }
    if (dialogRequest.getTopmostVia() == null) {
        var via =  clientTransactionId.getOutgoingViaHeader();
        dialogRequest.addHeader(via);
    }
    
    if (this.getCallId().getCallId().toLowerCase()!=dialogRequest.getCallId().getCallId().toLowerCase()) {    
        console.error("SIPDialog:sendRequest(): bad call ID in request");
        throw "SIPDialog:sendRequest(): bad call ID in request";
    }
    clientTransactionId.setDialog(this, this.dialogId);
    this.addTransaction(clientTransactionId);
    clientTransactionId.isMapped = true;
    var from = dialogRequest.getFrom();
    var to = dialogRequest.getTo();
    if (this.getLocalTag() != null && from.getTag() != null && from.getTag()!=this.getLocalTag()) {
        console.error("SIPDialog:sendRequest(): from tag mismatch expecting  " + this.getLocalTag());
        throw "SIPDialog:sendRequest(): from tag mismatch expecting  " + this.getLocalTag();
    }
    
    if (this.getLocalTag() == null && dialogRequest.getMethod()=="NOTIFY") {
        if (this.getMethod()!="SUBSCRIBE") {
            console.error("SIPDialog:sendRequest(): trying to send NOTIFY without SUBSCRIBE Dialog!");
            throw "SIPDialog:sendRequest(): trying to send NOTIFY without SUBSCRIBE Dialog!";
        }
        this.setLocalTag(from.getTag());
    }
    if (this.getLocalTag() != null) {
        from.setTag(this.getLocalTag());
    }
    if (this.getRemoteTag() != null) {
        to.setTag(this.getRemoteTag());
    }
    var messageChannel = this.sipStack.getChannel();
    if (messageChannel == null) {
        var outboundProxy = this.sipStack.getRouter(dialogRequest).getOutboundProxy();
        if (outboundProxy == null) {   
            console.error("SIPDialog:sendRequest(): no route found!");
            throw "SIPDialog:sendRequest(): no route found!"; 
        }
        messageChannel = this.sipStack.createRawMessageChannel(this.getSipProvider().
            getListeningPoint(outboundProxy.getTransport()).getHostAddress(),this.firstTransactionPort, outboundProxy);
        if (messageChannel != null) {
            clientTransactionId.setEncapsulatedChannel(messageChannel);
        }
    } 
    else {
        clientTransactionId.setEncapsulatedChannel(messageChannel);
    }
    if (messageChannel != null) {
        messageChannel.useCount++;
    }
    this.localSequenceNumber++;
    dialogRequest.getCSeq().setSeqNumber(this.getLocalSeqNumber());
    try {
        clientTransactionId.sendMessage(dialogRequest);
        if (dialogRequest.getMethod()=="BYE") {
            this.byeSent = true;
            if (this.isTerminatedOnBye()) {
                this.setState(this.TERMINATED_STATE);
            }
        }
    } catch (ex) {  
        console.error("SIPDialog:sendRequest():  catched execption, error sending message",ex);
        throw "SIPDialog:sendRequest():  catched execption, error sending message";
    }
}

SIPDialog.prototype.startTimer =function(transaction){
    if (this.timerTask != null && this.timerTask.transaction == transaction) {
        return;
    }
    this.ackSeen = false;
    if (this.timerTask != null) {
        this.timerTask.transaction = transaction;
    } else {
        variabletransaction=transaction;
        this.timer=this.sipStack.getTimer();
        sipdialog=this;
        this.timer=setTimeout(function(){
            sipdialog.timer=setInterval(function(){
                sipdialog.timerTask = new DialogTimerTask(variabletransaction);
            }, sipdialog.BASE_TIMER_INTERVAL);
        }, this.BASE_TIMER_INTERVAL);
    } 
}

SIPDialog.prototype.stopTimer =function(){
    if (this.timerTask != null) {
        clearTimeout(this.timer);
        this.timerTask = null;
    }
}

SIPDialog.prototype.updateRequest =function(sipRequest){
    var rl = this.getRouteList();
    if (!rl.isEmpty()) {
        sipRequest.setHeader(rl);
    } else {
        sipRequest.removeHeader("Route");
    }
    var mfi=new MessageFactoryImpl();
    if (mfi.getDefaultUserAgentHeader() != null) {
        sipRequest.setHeader(mfi.getDefaultUserAgentHeader());
    }
}

SIPDialog.prototype.createAck =function(cseqno){
    if (this.method!="INVITE") {
        console.error("SIPDialog:createAck(): dialog was not created with an INVITE" + this.method);
        throw "SIPDialog:createAck(): dialog was not created with an INVITE" + this.method;
    }
    if (cseqno <= 0) {
        console.error("SIPDialog:createAck(): bad cseq <= 0");
        throw "SIPDialog:createAck(): bad cseq <= 0";
    }
    if (this.remoteTarget == null) {
        console.error("SIPDialog:createAck(): cannot create ACK - no remote Target!");
         throw "SIPDialog:createAck(): cannot create ACK - no remote Target!";
    }
    if (this.lastInviteOkReceived < cseqno) {
        console.error("SIPDialog:createAck(): dialog not yet established -- no OK response!");
         throw "SIPDialog:createAck(): dialog not yet established -- no OK response!";
    }
    
    try {
        var uri4transport = null;
        if (this.routeList != null && !this.routeList.isEmpty()) {
            var r = this.routeList.getFirst();
            uri4transport = r.getAddress().getURI();
        } else {
            uri4transport = this.remoteTarget.getURI();
        }
        var transport = uri4transport.getTransportParam();
        if (transport == null) {
            transport = "WS";
        }
        var lp = this.sipProvider.getListeningPoint(transport);
        if (lp == null) {
            console.error("SIPDialog:createAck(): cannot create ACK - no ListeningPoint for transport towards next hop found:"+ transport);
            throw "SIPDialog:createAck(): cannot create ACK - no ListeningPoint for transport towards next hop found:"+ transport;
        }
        var sipRequest = new SIPRequest();
        sipRequest.setMethod("ACK");
        sipRequest.setRequestURI(getRemoteTarget().getURI());
        sipRequest.setCallId(this.callIdHeader);
        sipRequest.setCSeq(new CSeq(cseqno, "ACK"));
        var vias = new Array();
        var via = this.lastResponse.getTopmostVia();
        via.removeParameters();
        if (this.originalRequest != null && this.originalRequest.getTopmostVia() != null) {
            var originalRequestParameters = this.originalRequest.getTopmostVia().getParameters();
            if (originalRequestParameters != null && originalRequestParameters.size() > 0) {
                via.setParameters(originalRequestParameters.clone());
            }
        }
        var utils=new Utils();
        via.setBranch(utils.generateBranchId()); // new branch
        vias.add(via);
        sipRequest.setVia(vias);
        var from = new From();
        from.setAddress(this.localParty);
        from.setTag(this.myTag);
        sipRequest.setFrom(from);
        var to = new To();
        to.setAddress(this.remoteParty);
        if (this.hisTag != null) {
            to.setTag(this.hisTag);
        }
        sipRequest.setTo(to);
        sipRequest.setMaxForwards(new MaxForwards(70));

        if (this.originalRequest != null) {
            var authorization = this.originalRequest.getAuthorization();
            if (authorization != null) {
                sipRequest.setHeader(authorization);
            }
        }
        this.updateRequest(sipRequest);
        return sipRequest;
    } catch (ex) {
        console.error("SIPDialog:createAck(): catched unexpected exception ", ex);
        throw "SIPDialog:createAck(): catched unexpected exception";
    }
}

SIPDialog.prototype.setSipProvider =function(sipProvider){
    this.sipProvider = sipProvider;
}

SIPDialog.prototype.doTargetRefresh =function(sipMessage){
    var contactList = sipMessage.getContactHeaders();
    if (contactList != null) {
        var contact = contactList.getFirst();
        this.setRemoteTarget(contact);
    }
}

SIPDialog.prototype.createReliableProvisionalResponse =function(statusCode){
    if (!(this.firstTransactionIsServerTransaction)) {
        console.error("SIPDialog:createReliableProvisionalResponse(): not a Server Dialog!");
        throw "SIPDialog:createReliableProvisionalResponse(): not a Server Dialog!";
    }
    if (statusCode <= 100 || statusCode > 199) {
        console.error("SIPDialog:createReliableProvisionalResponse(): bad status code ");
        throw "SIPDialog:createReliableProvisionalResponse(): bad status code ";
    }
    var request = this.originalRequest;
    if (request.getMethod()!="INVITE") {
        console.error("SIPDialog:createReliableProvisionalResponse(): bad method");
        throw "SIPDialog:createReliableProvisionalResponse(): bad method";
    }
    var list = request.getHeaders("Supported");
    if (list == null&&!optionPresent(list, "100rel")) {
        list = request.getHeaders("Require");
        if (list == null&&!optionPresent(list, "100rel")) {
            console.error("SIPDialog:createReliableProvisionalResponse(): no Supported/Require 100rel header in the request");
            throw "SIPDialog:createReliableProvisionalResponse(): no Supported/Require 100rel header in the request";
        }
    }
    var response = request.createResponse(statusCode);
    var require = new Require();
    require.setOptionTag("100rel");
    response.addHeader(require);
    var rseq = new RSeq();
    rseq.setSeqNumber("1L");
    var rrl = request.getRecordRouteHeaders();
    if (rrl != null) {
        var rrlclone = rrl;
        response.setHeader(rrlclone);
    }
    return response;
}

SIPDialog.prototype.sendReliableProvisionalResponse =function(relResponse){
    if (!this.isServer()) {
        console.error("SIPDialog:sendReliableProvisionalResponse(): not a Server Dialog!");
        throw "SIPDialog:sendReliableProvisionalResponse(): not a Server Dialog!";
    }
    var sipResponse = relResponse;
    if (relResponse.getStatusCode() == 100) {
        console.error("SIPDialog:sendReliableProvisionalResponse(): cannot send 100 as a reliable provisional response");
        throw "SIPDialog:sendReliableProvisionalResponse(): cannot send 100 as a reliable provisional response";
    }
    if (relResponse.getStatusCode() / 100 > 2) {
        console.error("SIPDialog:sendReliableProvisionalResponse(): response code is not a 1xx response - should be in the range 101 to 199 ");
        throw "SIPDialog:sendReliableProvisionalResponse(): response code is not a 1xx response - should be in the range 101 to 199 ";
    }
    if (sipResponse.getToTag() == null) {
        console.error("SIPDialog:sendReliableProvisionalResponse(): badly formatted response -- To tag mandatory for Reliable Provisional Response");
        throw "SIPDialog:sendReliableProvisionalResponse(): badly formatted response -- To tag mandatory for Reliable Provisional Response";
    }
    var requireList = relResponse.getHeaders("Require");
    var found = false;
    if (requireList != null) {
        for(var i=0;i<requireList.length && !found;i++)
        {
            var rh = requireList[i];
            if (rh.getOptionTag().toLowerCase()=="100rel") {
                found = true;
            }
        }
    }
    if (!found) {
        var require = new Require("100rel");
        relResponse.addHeader(require);

    }
    var serverTransaction = this.getFirstTransaction();
    this.setLastResponse(serverTransaction, sipResponse);
    this.setDialogId(sipResponse.getDialogId(true));
    serverTransaction.sendReliableProvisionalResponse(relResponse);
}

SIPDialog.prototype.terminateOnBye =function(terminateFlag){
    this.terminateOnBye = terminateFlag;
}

SIPDialog.prototype.getMyContactHeader =function(){
    return this.contactHeader;
}

SIPDialog.prototype.handleAck =function(){
    return true;
}

SIPDialog.prototype.setEarlyDialogId =function(earlyDialogId){
    this.earlyDialogId = earlyDialogId;
}

SIPDialog.prototype.getEarlyDialogId =function(){
    return this.earlyDialogId;
}

SIPDialog.prototype.optionPresent =function(l,option){
    for(var i=0;i<l.length;i++)
    {
        var opt =  l[i];
        if (opt != null && option.toLowerCase()==opt.getOptionTag().toLowerCase()) {
            return true;
        }
    }
    return false;
}

SIPDialog.prototype.setLastAckReceived =function(lastAckReceived){
    this.lastAckReceived = lastAckReceived;
}

SIPDialog.prototype.getLastAckReceived =function(){
    return this.lastAckReceived;
}

SIPDialog.prototype.setLastAckSent =function(lastAckSent){
    this.lastAckSent = lastAckSent;
}

SIPDialog.prototype.isAtleastOneAckSent =function(){
    return this.isAcknowledged;
}

SIPDialog.prototype.isBackToBackUserAgent =function(){
    return this.isBackToBackUserAgent;
}

SIPDialog.prototype.doDeferredDeleteIfNoAckSent =function(seqno){
    if (this.sipStack.getTimer() == null) {
        this.setState(this.TERMINATED_STATE);
    } 
    else if (this.dialogDeleteIfNoAckSentTask == null) {
        variabledialog=seqno;
        var timer=this.sipStack.getTimer();
        sipdialog=this;
        timer=setTimeout(function(){
            sipdialog.dialogDeleteIfNoAckSentTask = new DialogDeleteIfNoAckSentTask(variabledialog);
        },this.TIMER_J* this.BASE_TIMER_INTERVAL);
    }
}

SIPDialog.prototype.setBackToBackUserAgent =function(){
    this.isBackToBackUserAgent = true;
}

SIPDialog.prototype.getEventHeader =function(){
    return eventHeader;
}

SIPDialog.prototype.setEventHeader =function(eventHeader){
    this.eventHeader = eventHeader;
}

SIPDialog.prototype.setServerTransactionFlag =function(serverTransactionFlag){
    this.serverTransactionFlag = serverTransactionFlag;
}

SIPDialog.prototype.setReInviteFlag =function(reInviteFlag){
    this.reInviteFlag = reInviteFlag
}

SIPDialog.prototype.isSequnceNumberValidation =function(){
    return this.sequenceNumberValidation;
}

SIPDialog.prototype.disableSequenceNumberValidation =function(){
    this.sequenceNumberValidation = false;
}

SIPDialog.prototype.raiseErrorEvent =function(dialogTimeoutError){
    var nextListener=null;
    var newErrorEvent = new SIPDialogErrorEvent(this, dialogTimeoutError);
    for(var i=0;i<this.eventListeners.length;i++)
    {
        nextListener = this.eventListeners[i];
        nextListener.dialogErrorEvent(newErrorEvent);
    }
    this.eventListeners=new Array();
    if (dialogTimeoutError != SIPDialogErrorEvent.DIALOG_ACK_NOT_SENT_TIMEOUT
        && dialogTimeoutError != SIPDialogErrorEvent.DIALOG_ACK_NOT_RECEIVED_TIMEOUT
        && dialogTimeoutError != SIPDialogErrorEvent.DIALOG_REINVITE_TIMEOUT) {
        this.delet();
    }
    this.stopTimer();
}

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
 *  Implementation of the JAIN-SIP SIPDialogErrorEvent .
 *  @see  gov/nist/javax/sip/stack/SIPDialogErrorEvent.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function SIPDialogErrorEvent(sourceDialog,dialogErrorID) {
    this.classname="SIPDialogErrorEvent"; 
    this.errorID=dialogErrorID;
    this.source = sourceDialog;
}

SIPDialogErrorEvent.prototype.DIALOG_ACK_NOT_RECEIVED_TIMEOUT=1;
SIPDialogErrorEvent.prototype.DIALOG_ACK_NOT_SENT_TIMEOUT=2;
SIPDialogErrorEvent.prototype.DIALOG_REINVITE_TIMEOUT=3;

SIPDialogErrorEvent.prototype.getErrorID =function(){
    return this.errorID;
}

SIPDialogErrorEvent.prototype.getSource =function(){
    return this.source;
}
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
 *  Implementation of the JAIN-SIP SIPDialogEventListener .
 *  @see  gov/nist/javax/sip/stack/SIPDialogEventListener.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function SIPDialogEventListener(dialogErrorEvent) {
    this.classname="SIPDialogEventListener"; 
}

SIPDialogEventListener.prototype.dialogErrorEvent =function(){
}

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
 *  Implementation of the JAIN-SIP SIPTransaction .
 *  @see  gov/nist/javax/sip/stack/SIPTransaction.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
var siptransaction;
function SIPTransaction(newParentStack,newEncapsulatedChannel) {
    this.classname="SIPTransaction"; 
    this.toListener=null;
    this.applicationData=null;
    this.lastResponse=null;
    this.isMapped=null;
    this.transactionId=null;
    this.auditTag = 0;
    this.sipStack=newParentStack;
    this.originalRequest=null;
    this.encapsulatedChannel=newEncapsulatedChannel;
    if(arguments.length==0)
    {
        this.wsurl=null;
    }
    else
    {
        this.wsurl=this.encapsulatedChannel.wsurl;
        /*if (this.isReliable()) {            
            this.encapsulatedChannel.useCount++;
        }*/
        this.disableTimeoutTimer();
        this.addEventListener(newParentStack);
    }
    this.eventListeners = new Array();
    this.transactionTimerStarted = false;
    this.branch=null;
    this.method=null;
    this.cSeq=null;
    this.currentState=null;
    this.timeoutTimerTicksLeft=null;
    this.from=null;
    this.to=null;
    this.event=null;
    this.callId=null;
    this.collectionTime=null;
    this.toTag=null;
    this.fromTag=null;
    this.terminatedEventDelivered=null;
}

SIPTransaction.prototype = new WSMessageChannel();
SIPTransaction.prototype.constructor=SIPTransaction;
SIPTransaction.prototype.BASE_TIMER_INTERVAL = 500;
SIPTransaction.prototype.T4 = 5000 / SIPTransaction.prototype.BASE_TIMER_INTERVAL;
SIPTransaction.prototype.T2 = 4000 / SIPTransaction.prototype.BASE_TIMER_INTERVAL;
SIPTransaction.prototype.TIMER_I = SIPTransaction.prototype.T4;
SIPTransaction.prototype.TIMER_K = SIPTransaction.prototype.T4;
SIPTransaction.prototype.TIMER_D = 32000 / SIPTransaction.prototype.BASE_TIMER_INTERVAL;
SIPTransaction.prototype.T1 = 1;
SIPTransaction.prototype.TIMER_A = 1;
SIPTransaction.prototype.TIMER_B = 64;
SIPTransaction.prototype.TIMER_J = 64;
SIPTransaction.prototype.TIMER_F = 64;
SIPTransaction.prototype.TIMER_H = 64;
SIPTransaction.prototype.INITIAL_STATE=null;
SIPTransaction.prototype.TRYING_STATE = "TRYING";
SIPTransaction.prototype.CALLING_STATE = "CALLING";
SIPTransaction.prototype.PROCEEDING_STATE = "PROCEEDING";
SIPTransaction.prototype.COMPLETED_STATE = "COMPLETED";
SIPTransaction.prototype.CONFIRMED_STATE = "CONFIRMED";
SIPTransaction.prototype.TERMINATED_STATE = "TERMINATED";
SIPTransaction.prototype.MAXIMUM_RETRANSMISSION_TICK_COUNT = 8;
SIPTransaction.prototype.TIMEOUT_RETRANSMIT = 3;
SIPTransaction.prototype.CONNECTION_LINGER_TIME=8;

function lingerTimer() {
    var transaction = siptransaction;
    var sipStack = transaction.getSIPStack();
    if (transaction instanceof SIPClientTransaction) {
        sipStack.removeTransaction(transaction);
    } 
    else if (transaction instanceof ServerTransaction) {
        sipStack.removeTransaction(transaction);
    }
}

SIPTransaction.prototype.getBranchId =function(){
    return this.branch;
}

SIPTransaction.prototype.setOriginalRequest =function(newOriginalRequest){
    var newBranch= null;
    if (this.originalRequest != null
        && (this.originalRequest.getTransactionId()!=newOriginalRequest.getTransactionId())) {
        this.sipStack.removeTransactionHash(this);
    }
    this.originalRequest = newOriginalRequest;
    this.method = newOriginalRequest.getMethod();
    this.from =  newOriginalRequest.getFrom();
    this.to =  newOriginalRequest.getTo();
    this.toTag = this.to.getTag();
    this.fromTag = this.from.getTag();
    this.callId =  newOriginalRequest.getCallId();
    this.cSeq = newOriginalRequest.getCSeq().getSeqNumber();
    this.event =  newOriginalRequest.getHeader("Event");
    this.transactionId = newOriginalRequest.getTransactionId();
    this.originalRequest.setTransaction(this);
    newBranch = newOriginalRequest.getViaHeaders().getFirst().getBranch();
    if (newBranch != null) {
        this.setBranch(newBranch);
    } else {
        this.setBranch(newOriginalRequest.getTransactionId());
    }
}

SIPTransaction.prototype.getOriginalRequest =function(){
    return this.originalRequest;
}

SIPTransaction.prototype.getRequest =function(){
    return this.originalRequest;
}

SIPTransaction.prototype.isInviteTransaction =function(){
    if(this.getMethod()=="INVITE")
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransaction.prototype.isCancelTransaction =function(){
    if(this.getMethod()=="CANCEL")
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransaction.prototype.isByeTransaction =function(){
    if(this.getMethod()=="BYE")
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransaction.prototype.getMessageChannel =function(){
    return this.encapsulatedChannel;
}

SIPTransaction.prototype.setBranch =function(newBranch){
    this.branch = newBranch;
}
SIPTransaction.prototype.getBranch =function(){
    if (this.branch == null) {
        this.branch = this.getOriginalRequest().getTopmostVia().getBranch();
    }
    return this.branch;
}

SIPTransaction.prototype.getMethod =function(){
    return this.method;
}

SIPTransaction.prototype.getCSeq =function(){
    return this.cSeq;
}

SIPTransaction.prototype.setState =function(newState){
    if (this.currentState == "COMPLETED") {
        if (newState != "TERMINATED" && newState != "CONFIRMED")
            newState = "COMPLETED";
    }
    if (this.currentState == "CONFIRMED") {
        if (newState != "TERMINATED")
        {
            newState = "CONFIRMED";
        }
    }
    if (this.currentState != "TERMINATED")
    {
        this.currentState = newState;
    }
    else
    {
        newState = this.currentState;
    }
}

SIPTransaction.prototype.getState =function(){
    return this.currentState;
}

SIPTransaction.prototype.enableTimeoutTimer =function(tickCount){
    this.timeoutTimerTicksLeft = tickCount;
}

SIPTransaction.prototype.disableTimeoutTimer =function(){
    this.timeoutTimerTicksLeft = -1;
}

SIPTransaction.prototype.fireTimer =function(){
    if (this.timeoutTimerTicksLeft != -1) {
        if (--this.timeoutTimerTicksLeft == 0) {
            this.fireTimeoutTimer();
        }
    }
}

SIPTransaction.prototype.isTerminated =function(){
    if(this.getState() == "TERMINATED")
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransaction.prototype.getURLWS =function(){
    return this.encapsulatedChannel.getURLWS();
}

SIPTransaction.prototype.getKey =function(){
    return this.encapsulatedChannel.getKey();
}

SIPTransaction.prototype.getSIPStack =function(){
    return this.sipStack;
}

SIPTransaction.prototype.getTransport =function(){
    return this.encapsulatedChannel.getTransport();
}

SIPTransaction.prototype.isReliable =function(){
    return true;
}

SIPTransaction.prototype.getViaHeader =function(){
    var channelViaHeader = WSMessageChannel.prototype.getViaHeader.call(this);
    channelViaHeader.setBranch(this.branch);
    return channelViaHeader;
}

SIPTransaction.prototype.sendMessage=function(messageToSend){
    this.encapsulatedChannel.sendMessage(messageToSend);
//this.startTransactionTimer();
}

SIPTransaction.prototype.addEventListener =function(newListener){
    var l=null;
    for(var i=0;i<this.eventListeners.length;i++)
    {
        if(this.eventListeners[i]==newListener)
        {
            l=i;
        }
    }
    if(l==null)
    {
        this.eventListeners.push(newListener);
    }
}

SIPTransaction.prototype.removeEventListener =function(oldListener){
    var l=null;
    for(var i=0;i<this.eventListeners.length;i++)
    {
        if(this.eventListeners[i]==oldListener)
        {
            l=i;
        }
    }
    this.eventListeners.splice(l,1);
}

SIPTransaction.prototype.raiseErrorEvent =function(errorEventID){
    var nextListener=null;
    var newErrorEvent = new SIPTransactionErrorEvent(this, errorEventID);
    for(var i=0;i<this.eventListeners.length;i++)
    {
        nextListener = this.eventListeners[i];
        nextListener.transactionErrorEvent(newErrorEvent);
    }
    if (errorEventID != this.TIMEOUT_RETRANSMIT) {
        this.eventListeners=new Array();
        this.setState("TERMINATED");
        if (this instanceof SIPServerTransaction && this.isByeTransaction() && this.getDialog() != null)
        {
            this.getDialog().setState("TERMINATED");
        }
    }
}

SIPTransaction.prototype.isServerTransaction =function(){
    if(this instanceof SIPServerTransaction)
    {
        return true;
    }
    else
    {
        return false;
    }
}

SIPTransaction.prototype.getDialog =function(){
}

SIPTransaction.prototype.setDialog =function(sipDialog,dialogId){
}

SIPTransaction.prototype.getViaHost =function(){
    return this.getViaHeader().getHost();
}

SIPTransaction.prototype.getLastResponse =function(){
    return this.lastResponse;
}

SIPTransaction.prototype.getResponse =function(){
    return this.lastResponse;
}

SIPTransaction.prototype.getTransactionId =function(){
    return this.transactionId;
}

SIPTransaction.prototype.hashCode =function(){
    if (this.transactionId == null)
    {
        return -1;
    }
    else
    { 
        var hash = 0;
        var x=this.transactionId;
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
}

SIPTransaction.prototype.getViaPort =function(){
    return this.getViaHeader().getPort();
}

SIPTransaction.prototype.getPort =function(){
    return this.encapsulatedChannel.getPort();
}

SIPTransaction.prototype.doesCancelMatchTransaction =function(requestToTest){
    var viaHeaders;
    var topViaHeader;
    var messageBranch;
    var transactionMatches = false;
    if (this.getOriginalRequest() == null || this.getOriginalRequest().getMethod()=="CANCEL")
    {
        return false;
    }
    viaHeaders = requestToTest.getViaHeaders();
    if (viaHeaders != null) {
        topViaHeader = viaHeaders.getFirst();
        messageBranch = topViaHeader.getBranch();
        if (messageBranch != null) {
            if(messageBranch.toLowerCase().substring(0,7)!="z9hg4bk")
            {
                messageBranch = null;
            }
        }
        if (messageBranch != null && this.getBranch() != null) {
            if (this.getBranch().toLowerCase()==messageBranch.toLowerCase()
                && topViaHeader.getSentBy()==
                this.getOriginalRequest().getViaHeaders().getFirst().getSentBy()) {
                transactionMatches = true;
            }
        } else {
            if (this.getOriginalRequest().getRequestURI()==
                requestToTest.getRequestURI()
                && this.getOriginalRequest().getTo()==
                requestToTest.getTo()
                && this.getOriginalRequest().getFrom()==
                requestToTest.getFrom()
                && this.getOriginalRequest().getCallId().getCallId()==
                requestToTest.getCallId().getCallId()
                && this.getOriginalRequest().getCSeq().getSeqNumber() == requestToTest.getCSeq().getSeqNumber()
                && topViaHeader==this.getOriginalRequest().getViaHeaders().getFirst()) {
                transactionMatches = true;
            }
        }
    }
    if (transactionMatches) {
        this.setPassToListener();
    }
    return transactionMatches;
}

SIPTransaction.prototype.close =function(){
    this.encapsulatedChannel.close();
}

SIPTransaction.prototype.isSecure =function(){
    return this.encapsulatedChannel.isSecure();
}

SIPTransaction.prototype.getMessageProcessor =function(){
    return this.encapsulatedChannel.getMessageProcessor();
}

SIPTransaction.prototype.setApplicationData =function(applicationData){
    this.applicationData = applicationData;
}

SIPTransaction.prototype.getURLWS =function(){
    return this.wsurl;
}

SIPTransaction.prototype.getApplicationData =function(){
    return this.applicationData;
}

SIPTransaction.prototype.setEncapsulatedChannel =function(messageChannel){
    this.encapsulatedChannel = messageChannel;
}

SIPTransaction.prototype.getSipProvider =function(){
    return this.getMessageProcessor().getListeningPoint().getProvider();
}

SIPTransaction.prototype.raiseIOExceptionEvent =function(){
    this.setState("TERMINATED");
}

SIPTransaction.prototype.passToListener =function(){
    return this.toListener;
}

SIPTransaction.prototype.setPassToListener =function(){
    this.toListener = true;
}

SIPTransaction.prototype.testAndSetTransactionTerminatedEvent =function(){
    var retval=!this.terminatedEventDelivered;
    this.terminatedEventDelivered = true;
    return retval;
}

SIPTransaction.prototype.startTransactionTimer =function(){
    
}

SIPTransaction.prototype.isMessagePartOfTransaction =function(){
    
}

SIPTransaction.prototype.fireTimeoutTimer =function(){
    
}/*
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
 *  Implementation of the JAIN-SIP SIPClientTransaction .
 *  @see  gov/nist/javax/sip/stack/SIPClientTransaction.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function SIPClientTransaction(newSIPStack,newChannelToUse) {
    this.classname="SIPClientTransaction"; 
    this.encapsulatedChannel=newChannelToUse;
    this.wsurl=this.encapsulatedChannel.wsurl;
    /*if (this.isReliable()) {            
        this.encapsulatedChannel.useCount++;
    }*/
    this.disableTimeoutTimer();
    this.sipStack=newSIPStack;
    this.infoApp=newSIPStack.infoApp;
    this.addEventListener(newSIPStack);
    this.originalRequest=null;
    this.eventListeners = new Array();
    
    var utils=new Utils();
    this.setBranch(utils.generateBranchId());
    this.notifyOnRetransmit = false;
    this.timeoutIfStillInCallingState = false;
    this.setEncapsulatedChannel(newChannelToUse);
    this.messageProcessor = newChannelToUse.messageProcessor;
    this.sipDialogs=new Array();
    this.lastRequest=null;
    this.viaPort=null;
    this.viaHost=null;
    this.respondTo=null;
    this.defaultDialog=null;
    this.nextHop=null;
    this.callingStateTimeoutCount=null;
    this.timer=null;
    this.oldmessage=null;
}

SIPClientTransaction.prototype = new SIPTransaction();
SIPClientTransaction.prototype.constructor=SIPClientTransaction;
SIPClientTransaction.prototype.BRANCH_MAGIC_COOKIE_LOWER_CASE="z9hg4bk";
SIPClientTransaction.prototype.MAXIMUM_RETRANSMISSION_TICK_COUNT=8;
SIPClientTransaction.prototype.COMPLETED="COMPLETED";
SIPClientTransaction.prototype.PROCEEDING="PROCEEDING";
SIPClientTransaction.prototype.CALLING="CALLING";
SIPClientTransaction.prototype.TERMINATED="TERMINATED";
SIPClientTransaction.prototype.ACK="ACK";
SIPClientTransaction.prototype.INVITE="INVITE";
SIPClientTransaction.prototype.TRYING="TRYING";
SIPClientTransaction.prototype.CANCEL="CANCEL";
SIPClientTransaction.prototype.BYE="BYE";
SIPClientTransaction.prototype.SUBSCRIBE="SUBSCRIBE";
SIPClientTransaction.prototype.NOTIFY="NOTIFY";
SIPClientTransaction.prototype.TIMER_B=64;
SIPClientTransaction.prototype.TIMER_D=SIPTransaction.prototype.TIMER_D;
SIPClientTransaction.prototype.TIMER_F=64;
SIPClientTransaction.prototype.TIMER_K=SIPTransaction.prototype.T4;
SIPClientTransaction.prototype.TIMER_J=64;
SIPClientTransaction.prototype.TimeStampHeader="Timestamp";
SIPClientTransaction.prototype.RouteHeader="Route";
SIPClientTransaction.prototype.RETRANSMIT="RETRANSMIT";
SIPClientTransaction.prototype.TRANSPORT_ERROR=2;
SIPClientTransaction.prototype.TIMEOUT_ERROR=1;
SIPClientTransaction.prototype.EARLY="EARLY";
SIPClientTransaction.prototype.CONNECTION_LINGER_TIME=8;
SIPClientTransaction.prototype.BASE_TIMER_INTERVAL=500;

SIPClientTransaction.prototype.setResponseInterface =function(newRespondTo){
    this.respondTo = newRespondTo;
}

SIPClientTransaction.prototype.getRequestChannel =function(){
    return this;
}

SIPClientTransaction.prototype.isMessagePartOfTransaction =function(messageToTest){
    var viaHeaders = messageToTest.getViaHeaders();
    var transactionMatches= false;
    var messageBranch =  viaHeaders.getFirst().getBranch();
    if(this.getBranch() != null && messageBranch != null
        && this.getBranch().toLowerCase().substring(0,7)==(this.BRANCH_MAGIC_COOKIE_LOWER_CASE)
        && messageBranch.toLowerCase().substring(0,7)==(this.BRANCH_MAGIC_COOKIE_LOWER_CASE))
        {
        var rfc3261Compliant = true;
    }
    else
    {
        rfc3261Compliant = false;
    }
    if (this.COMPLETED == this.getState()) {
        if (rfc3261Compliant) {
            if(this.getBranch()().toLowerCase()==viaHeaders.getFirst().getBranch().toLowerCase()
                && this.getMethod()==messageToTest.getCSeq().getMethod())
                {
                transactionMatches = true;
            }
            else
            {
                transactionMatches=false;
            }
        } 
        else {
            if(this.getBranch()==messageToTest.getTransactionId())
            {
                transactionMatches = true;
            }
            else
            {
                transactionMatches=false;
            }
        }
    } 
    else if (!this.isTerminated()) {
        if (rfc3261Compliant) {
            if (viaHeaders != null) {
                if (this.getBranch().toLowerCase()==viaHeaders.getFirst().getBranch().toLowerCase()) {
                    if(this.getOriginalRequest().getCSeq().getMethod()==messageToTest.getCSeq().getMethod())
                    {
                        transactionMatches = true;
                    }
                    else
                    {
                        transactionMatches=false;
                    }
                }
            }
        } 
        else {
            if (this.getBranch() != null) {
                if(this.getBranch().toLowerCase()==messageToTest.getTransactionId().toLowerCase())
                {
                    transactionMatches = true;
                }
                else
                {
                    transactionMatches=false;
                }
            } else {
                if(this.getOriginalRequest().getTransactionId().toLowerCase()==messageToTest.getTransactionId().toLowerCase())
                {
                    transactionMatches = true;
                }
                else
                {
                    transactionMatches=false;
                }
            }
        }
    }
    return transactionMatches;
}

SIPClientTransaction.prototype.sendMessage =function(messageToSend){
    var transactionRequest = messageToSend;
    var topVia =  transactionRequest.getViaHeaders().getFirst();
    topVia.setBranch(this.getBranch());
    if (this.PROCEEDING == this.getState()|| this.CALLING == this.getState()) {
        if (transactionRequest.getMethod()==this.ACK) {
            if (this.isReliable()) {
                this.setState(this.TERMINATED);
            } else {
                this.setState(this.COMPLETED);
            }
            SIPTransaction.prototype.sendMessage.call(this,transactionRequest);
            return;
        }
    }
    try {
        this.lastRequest = transactionRequest;
        if (this.getState() == null) {
            this.setOriginalRequest(transactionRequest);
            if (transactionRequest.getMethod()==this.INVITE) {
                this.setState(this.CALLING);
            } 
            else if (transactionRequest.getMethod()==this.ACK) {
                this.setState(this.TERMINATED);
            } 
            else {
                this.setState(this.TRYING);
            }
            if (this.isInviteTransaction()) {
                this.enableTimeoutTimer(this.TIMER_B);
            } 
            else {
                this.enableTimeoutTimer(this.TIMER_F);
            }
        }
        SIPTransaction.prototype.sendMessage.call(this,transactionRequest);  
    } catch (ex) {
        console.error("SIPClientTransaction:sendMessage(): catched exception:"+ex);
        this.setState(this.TERMINATED);
    }
    this.isMapped = true;
    this.startTransactionTimer();
}

SIPClientTransaction.prototype.processResponse =function(){
    if(arguments.length==2)
    {
        var sipResponse=arguments[0];
        var incomingChannel=arguments[1];
        this.processResponseargu2(sipResponse, incomingChannel);
    }
    else
    {
        var transactionResponse=arguments[0];
        var sourceChannel=arguments[1];
        var dialog=arguments[2];
        this.processResponseargu3(transactionResponse, sourceChannel, dialog);
    }
}

SIPClientTransaction.prototype.processResponseargu2 =function(sipResponse,incomingChannel){
    var dialog = null;
    var method = sipResponse.getCSeq().getMethod();
    var dialogId = sipResponse.getDialogId(false);
    if (method==this.CANCEL && this.lastRequest != null) {
        var ict = this.lastRequest.getInviteTransaction();
        if (ict != null) {
            dialog = ict.defaultDialog;
        }
    } else {
        dialog = this.getDialog(dialogId);
    }
    if (dialog == null) {
        var code = sipResponse.getStatusCode();
        if ((code > 100 && code < 300)
            && (sipResponse.getToTag() != null || this.sipStack.isRfc2543Supported())
            && this.sipStack.isDialogCreated(method)) {
            if (this.defaultDialog != null) {
                if (sipResponse.getFromTag() != null) {
                    var dialogResponse = this.defaultDialog.getLastResponse();
                    var defaultDialogId = this.defaultDialog.getDialogId();
                    if (dialogResponse == null|| method==this.SUBSCRIBE && defaultDialogId==dialogId
                        && dialogResponse.getCSeq().getMethod()==this.NOTIFY) {
                        this.defaultDialog.setLastResponse(this, sipResponse);
                        dialog = this.defaultDialog;
                    } else {
                        dialog = this.sipStack.getDialog(dialogId);
                        if (dialog == null) {
                            if (this.defaultDialog.isAssignedFunction()) {
                                dialog = this.sipStack.createDialog(this, sipResponse);
                            }
                        }
                    }
                    if ( dialog != null ) {
                        this.setDialog(dialog, dialog.getDialogId());
                    } 
                } else {
                    console.error("SIPClientTransaction:processResponseargu2(): response without from-tag");
                    throw "SIPClientTransaction:processResponseargu2(): response without from-tag";
                }
            } else {
                if (this.sipStack.isAutomaticDialogSupportEnabled) {
                    dialog = this.sipStack.createDialog(this, sipResponse);
                    this.setDialog(dialog, dialog.getDialogId());
                }
            }
        } else {
            dialog = this.defaultDialog;
        }
    } else {    
        dialog.setLastResponse(this, sipResponse);
    }
    this.processResponse(sipResponse, incomingChannel, dialog);
}

SIPClientTransaction.prototype.processResponseargu3 =function(transactionResponse,sourceChannel,dialog){

    if (this.getState() == null)
    {
        return;
    }
    if ((this.COMPLETED == this.getState() || this.TERMINATED == this.getState())
        && transactionResponse.getStatusCode() / 100 == 1) {
        return;
    }
    this.lastResponse = transactionResponse;
    try {
        if (this.isInviteTransaction())
        {
            this.inviteClientTransaction(transactionResponse, sourceChannel, dialog);
        }
        else
        {
            this.nonInviteClientTransaction(transactionResponse, sourceChannel, dialog);
        }   
    } catch (ex) {
        console.error("SIPClientTransaction:processResponseargu3(): catched exception:"+ex);
        this.setState(this.TERMINATED);
    }
}

SIPClientTransaction.prototype.nonInviteClientTransaction =function(transactionResponse,sourceChannel,sipDialog){

    var statusCode = transactionResponse.getStatusCode();
    if (this.TRYING == this.getState()) {
        if (100 <= statusCode && statusCode <= 199) {
            this.setState(this.PROCEEDING);
            this.enableTimeoutTimer(this.TIMER_F);
            if (this.respondTo != null) {
                this.respondTo.processResponse(transactionResponse, this, sipDialog);
            }
        } 
        else if (200 <= statusCode && statusCode <= 699) {
            if (!this.isReliable()) {
                this.setState(this.COMPLETED);
                this.enableTimeoutTimer(this.TIMER_K);
            }
            else {
                this.setState(this.TERMINATED);
                this.sipStack.removeTransaction(this);
                clearTimeout(this.timer);
            }
            if (this.respondTo != null) {
                this.respondTo.processResponse(transactionResponse, this, sipDialog);
            } 
        }
    }
    else if (this.PROCEEDING == this.getState()) {
        if (100 <= statusCode && statusCode <= 199) {
            if (this.respondTo != null) {
                this.respondTo.processResponse(transactionResponse, this, sipDialog);
            } 
        } else if (200 <= statusCode && statusCode <= 699) {
            if (this.respondTo != null) {
                this.respondTo.processResponse(transactionResponse, this, sipDialog);
            } 
            this.disableTimeoutTimer();
            if (!this.isReliable()) {
                this.setState(this.COMPLETED);
                this.enableTimeoutTimer(this.TIMER_K);
            } else {
                this.setState(this.TERMINATED);
            }
        }
    } 
}

SIPClientTransaction.prototype.inviteClientTransaction =function(transactionResponse,sourceChannel,dialog){
    var statusCode = transactionResponse.getStatusCode();
    if (this.TERMINATED == this.getState()) {
        var ackAlreadySent = false;
        if (dialog != null && dialog.isAckSeen() && dialog.getLastAckSent() != null) {
            if (dialog.getLastAckSent().getCSeq().getSeqNumber() == transactionResponse.getCSeq().getSeqNumber()
                && transactionResponse.getFromTag()==dialog.getLastAckSent().getFromTag()) {
                ackAlreadySent = true;
            }
        }
        if (dialog!= null && !ackAlreadySent
            && transactionResponse.getCSeq().getMethod()==dialog.getMethod()) {
            dialog.resendAck();
        }
        this.sipStack.removeTransaction(this);
        clearTimeout(this.timer);
        return;
    }
    else if (this.CALLING == this.getState()) {
        if (200 <= statusCode && statusCode <= 299) {
            this.disableTimeoutTimer();
            this.setState(this.TERMINATED);
            if (this.respondTo != null)
            {
                this.respondTo.processResponse(transactionResponse, this, dialog);
            }
        } 
        else if (100 <= statusCode && statusCode <= 199) {
            this.disableTimeoutTimer();
            this.setState(this.PROCEEDING);
            if (this.respondTo != null)
            {
                this.respondTo.processResponse(transactionResponse, this, dialog);
            }
        } 
        else if (300 <= statusCode && statusCode <= 699) {
            this.sendMessage(this.createErrorAck());
            if (this.respondTo != null) 
            {    
                this.respondTo.processResponse(transactionResponse, this, dialog);
            } 
            if (!this.isReliable()) {
                this.setState(this.COMPLETED);
                this.enableTimeoutTimer(this.TIMER_D);
            } 
            else {
                this.setState(this.TERMINATED);
            }
        }
    }
    else if (this.PROCEEDING == this.getState()) {
        if (100 <= statusCode && statusCode <= 199) 
        {
            if (this.respondTo != null) {
                this.respondTo.processResponse(transactionResponse, this, dialog);
            } 
        } 
        else if (200 <= statusCode && statusCode <= 299) 
        {
            this.setState(this.TERMINATED);
            this.sipStack.removeTransaction(this);
            clearTimeout(this.timer);
            if (this.respondTo != null) {
                this.respondTo.processResponse(transactionResponse, this, dialog);
            } 
        } 
        else if (300 <= statusCode && statusCode <= 699) 
        {
            this.sendMessage(this.createErrorAck());
            if (!this.isReliable()) {
                this.setState(this.COMPLETED);
                this.enableTimeoutTimer(this.TIMER_D);
            } 
            else 
            {
                this.setState(this.TERMINATED);
                this.sipStack.removeTransaction(this);
                clearTimeout(this.timer);
            }
            if (this.respondTo != null)
            {
                this.respondTo.processResponse(transactionResponse, this, dialog);
            }
        }
    }
    else if (this.COMPLETED == this.getState()) {
        this.setState(this.TERMINATED);
        this.sipStack.removeTransaction(this);
        clearTimeout(this.timer);
        if (300 <= statusCode && statusCode <= 699) {
            this.sendMessage(this.createErrorAck());
        }
    }
}

SIPClientTransaction.prototype.sendRequest =function(){
    var sipRequest = this.getOriginalRequest();
    if (this.getState() != null)
    {
        console.error("SIPClientTransaction:sendRequest(): request already sent");
        throw "SIPClientTransaction:sendRequest(): request already sent";
    }
    try {
        sipRequest.checkHeaders();
    } catch (ex) {
        console.error("SIPClientTransaction:sendRequest(): "+ ex);
        throw "SIPClientTransaction:sendRequest(): "+ex;
    }
    try {
        if (this.getOriginalRequest().getMethod()==this.CANCEL
            && this.sipStack.isCancelClientTransactionChecked()) {
            var ct = this.sipStack.findCancelTransaction(this.getOriginalRequest(), false);
            if (ct == null) {
                console.error("SIPClientTransaction:sendRequest(): could not find original tx to cancel. RFC 3261 9.1");
                throw "SIPClientTransaction:sendRequest(): could not find original tx to cancel. RFC 3261 9.1";
            } 
            else if (ct.getState() == null) {
                console.error("SIPClientTransaction:sendRequest(): state is null no provisional response yet -- cannot cancel RFC 3261 9.1");
                throw "SIPClientTransaction:sendRequest(): state is null no provisional response yet -- cannot cancel RFC 3261 9.1";
            } 
            else if (ct.getMethod()!=this.INVITE) {
                console.error("SIPClientTransaction:sendRequest(): cannot cancel non-invite requests RFC 3261 9.1");
                throw "SIPClientTransaction:sendRequest(): cannot cancel non-invite requests RFC 3261 9.1";
            }
        } 
        else if (this.getOriginalRequest().getMethod()==this.BYE
            ||this.getOriginalRequest().getMethod()==this.NOTIFY) {
            var dialog = this.sipStack.getDialog(this.getOriginalRequest().getDialogId(false));
            if (this.getSipProvider().isAutomaticDialogSupportEnabled() && dialog != null) {
                console.error("SIPClientTransaction:sendRequest(): Dialog is present and AutomaticDialogSupport is enabled for the provider -- Send the Request using the Dialog.sendRequest(transaction)");
                throw "SIPClientTransaction:sendRequest(): Dialog is present and AutomaticDialogSupport is enabled for the provider -- Send the Request using the Dialog.sendRequest(transaction)";
            }
        }
        if (this.getOriginalRequest().getMethod()==this.INVITE) {
            dialog = this.getDefaultDialog();
        }
        this.isMapped = true;
        this.sendMessage(sipRequest);
    } catch (ex) {
        this.setState(this.TERMINATED);
        console.error("SIPClientTransaction:sendRequest(): catched exception:"+ ex);
        throw "SIPClientTransaction:sendRequest(): catched exception:"+ ex;
    }
}

SIPClientTransaction.prototype.fireTimeoutTimer =function(){
    clearTimeout(this.timer);
    var dialog = this.getDialog();
    if (this.CALLING == this.getState()|| this.TRYING == this.getState()
        || this.PROCEEDING == this.getState()) {
        if (dialog != null&& (dialog.getState() == null || dialog.getState() == this.EARLY)) {
            if (this.getSIPStack().isDialogCreated(this.getOriginalRequest().getMethod())) {
                dialog.delet();
            }
        } 
        else if (dialog != null) {
            if (this.getOriginalRequest().getMethod().toLowerCase()==this.BYE.toLowerCase()
                && dialog.isTerminatedOnBye()) {
                dialog.delet();
            }
        }
    }
    if (this.COMPLETED != this.getState()) {
        this.raiseErrorEvent(this.TIMEOUT_ERROR);
        if (this.getOriginalRequest().getMethod().toLowerCase()==this.CANCEL.toLowerCase()) {
            var inviteTx = this.getOriginalRequest().getInviteTransaction();
            if (inviteTx != null&& inviteTx.getDialog() != null
                && (inviteTx.getState() == this.CALLING || inviteTx.getState() == this.PROCEEDING)) 
                {
                inviteTx.setState(this.TERMINATED);
            }
        }
    } 
    else {
        this.setState(this.TERMINATED);
    }
}

SIPClientTransaction.prototype.createCancel =function(){
    var originalRequest = this.getOriginalRequest();
    if (originalRequest == null)
    {
        console.error("SIPClientTransaction:createCancel(): bad state " + this.getState());
        throw "SIPClientTransaction:createCancel(): bad state " + this.getState();
    }
    if (originalRequest.getMethod()!=this.INVITE)
    {
        console.error("SIPClientTransaction:createCancel(): only INIVTE may be cancelled");
        throw "SIPClientTransaction:createCancel(): only INIVTE may be cancelled";
    }
    if (originalRequest.getMethod().toLowerCase()==this.ACK.toLowerCase())
    {
        console.error("SIPClientTransaction:createCancel(): cannot Cancel ACK!");
        throw "SIPClientTransaction:createCancel(): cannot Cancel ACK!";
    }
    else {
        var cancelRequest = originalRequest.createCancelRequest();
        cancelRequest.setInviteTransaction(this);
        return cancelRequest;
    }
}

SIPClientTransaction.prototype.createAck =function(){
    var originalRequest = this.getOriginalRequest();
    if (originalRequest == null)
    {
        console.error("SIPClientTransaction:createAck(): bad state " + getState());
        throw "SIPClientTransaction:createAck(): bad state " + getState();
    }
    
    if (this.getMethod().toLowerCase()==this.ACK.toLowerCase()) {
        console.error("SIPClientTransaction:createAck(): cannot ACK an ACK!");
        throw "SIPClientTransaction:createAck(): cannot ACK an ACK!";
    } else if (this.lastResponse == null) {
        console.error("SIPClientTransaction:createAck(): bad Transaction state");
        throw "SIPClientTransaction:createAck(): bad Transaction state";
    } else if (this.lastResponse.getStatusCode() < 200) {
        console.error("SIPClientTransaction:createAck() : cannot ACK a provisional response!");
        throw "SIPClientTransaction:createAck(): cannot ACK a provisional response!";
    }
    var ackRequest = originalRequest.createAckRequest(this.lastResponse.getTo());
    var recordRouteList = this.lastResponse.getRecordRouteHeaders();
    if (recordRouteList == null) {
        if (this.lastResponse.getContactHeaders() != null
            && this.lastResponse.getStatusCode() / 100 != 3) {
            var contact = this.lastResponse.getContactHeaders().getFirst();
            var uri =  contact.getAddress().getURI();
            ackRequest.setRequestURI(uri);
        }
        return ackRequest;
    }
    ackRequest.removeHeader(this.RouteHeader);
    var routeList = new RouteList();
    for(var i=recordRouteList.length-1;i>=0;i--)
    {
        var rr =  recordRouteList[i];
        var route = new Route();
        route.setAddressrr.getAddress();
        route.setParameters(rr.getParameters());
        routeList.add(route);
    }
    contact = null;
    if (this.lastResponse.getContactHeaders() != null) {
        contact = this.lastResponse.getContactHeaders().getFirst();
    }
    if (!routeList.getFirst().getAddress().getURI().hasLrParam()) {
        route = null;
        if (contact != null) {
            route = new Route();
            route.setAddress(contact.getAddress());
        }
        var firstRoute = routeList.getFirst();
        routeList.removeFirst();
        uri = firstRoute.getAddress().getURI();
        ackRequest.setRequestURI(uri);
        if (route != null)
            routeList.add(route);
        ackRequest.addHeader(routeList);
    } 
    else {
        if (contact != null) {
            uri =  contact.getAddress().getURI();
            ackRequest.setRequestURI(uri);
            ackRequest.addHeader(routeList);
        }
    }
    return ackRequest;
}

SIPClientTransaction.prototype.createErrorAck =function(){
    var originalRequest = this.getOriginalRequest();
    if (originalRequest == null)
    {
        console.error("SIPClientTransaction:createErrorAck(): bad state " + getState());
        throw "SIPClientTransaction:createErrorAck(): bad state " + getState();
    }
    if (this.getMethod()!=this.INVITE) 
    {
        console.error("SIPClientTransaction:createErrorAck(): can only ACK an INVITE!");
        throw "SIPClientTransaction:createErrorAck(): can only ACK an INVITE!";
    } 
    else if (this.lastResponse == null) 
    {
        console.error("SIPClientTransaction:createErrorAck(): bad Transaction state");
        throw "SIPClientTransaction:createErrorAck():  bad Transaction state";
    } 
    else if (this.lastResponse.getStatusCode() < 200) 
    {
        console.error("SIPClientTransaction:createErrorAck(): cannot ACK a provisional response!");
        throw "SIPClientTransaction:createErrorAck(): cannot ACK a provisional response!";
    }
    return originalRequest.createErrorAck(this.lastResponse.getTo());
}

SIPClientTransaction.prototype.setViaPort =function(port){
    this.viaPort = port;
}

SIPClientTransaction.prototype.setViaHost =function(host){
    this.viaHost = host;
}

SIPClientTransaction.prototype.getViaPort =function(){
    return this.viaPort;
}

SIPClientTransaction.prototype.getViaHost =function(){
    return this.viaHost;
}

SIPClientTransaction.prototype.getOutgoingViaHeader =function(){
    return this.getMessageProcessor().getViaHeader();
}

SIPClientTransaction.prototype.clearState =function(){
    
}

SIPClientTransaction.prototype.setState =function(newState){
    if (newState == this.TERMINATED && this.isReliable()) {
        this.collectionTime = this.TIMER_J;
    }
    /*if (SIPTransaction.prototype.getState.call(this) != this.COMPLETED
        && (newState == this.COMPLETED || newState == this.TERMINATED)) {
        this.sipStack.decrementActiveClientTransactionCount();
    }*/
    SIPTransaction.prototype.setState.call(this,newState);
}

SIPClientTransaction.prototype.startTransactionTimer =function(){
    if (this.transactionTimerStarted==false) {
        this.transactionTimerStarted=true;
        if (this.sipStack.getTimer() != null ) {
            var transaction=this;
            this.timer=setInterval(function(){
                if(transaction.isTerminated())
                {
                    var sipStack=transaction.getSIPStack();
                    sipStack.removeTransaction(transaction);
                }
                else
                {
                    transaction.fireTimer();
                }
            },this.BASE_TIMER_INTERVAL);
        }
    }
}

SIPClientTransaction.prototype.terminate =function(){
    this.setState(this.TERMINATED);
}

SIPClientTransaction.prototype.checkFromTag =function(sipResponse){
    var originalFromTag = this.getRequest().getFromTag();
    if (this.defaultDialog != null) {
        if (originalFromTag == null ^ sipResponse.getFrom().getTag() == null) {
            return false;
        }
        if (originalFromTag.toLowerCase()!=sipResponse.getFrom().getTag().toLowerCase()
            && originalFromTag != null) {
            return false;
        }
    }
    return true;
}

SIPClientTransaction.prototype.getDialog =function(){
    if(arguments.length==0)
    {
        return this.getDialogargu0();
    }
    else if(arguments.length==1)
    {
        var dialogId=arguments[0];
        return this.getDialogargu1(dialogId);
    }
}

SIPClientTransaction.prototype.getDialogargu0 =function(){
    var retval = null;
    if (this.lastResponse != null && this.lastResponse.getFromTag() != null
        && this.lastResponse.getToTag() != null
        && this.lastResponse.getStatusCode() != 100) {
        var dialogId = this.lastResponse.getDialogId(false);
        retval = this.getDialog(dialogId);
    }
    if (retval == null) {
        retval = this.defaultDialog;
    }
    return retval;
}

SIPClientTransaction.prototype.getDialogargu1 =function(dialogId){
    var retval=null;
    for(var i=0;i<this.sipDialogs.length;i++)
    {
        if(this.sipDialogs[i][0]==dialogId)
        {
            retval = this.sipDialogs[i][1];
        }
    }
    return retval;
}

SIPClientTransaction.prototype.setDialog =function(sipDialog,dialogId){
    if (sipDialog == null) {
        console.error("SIPClientTransaction:setDialog(): bad dialog argument");
        throw "SIPClientTransaction:setDialog(): bad dialog argument";
    }
    if (this.defaultDialog == null) {
        this.defaultDialog = sipDialog;
    }
    if (dialogId != null && sipDialog.getDialogId() != null) {
        var l=null
        for(var i=0;i<this.sipDialogs.length;i++)
        {
            if(this.sipDialogs[i][0]==dialogId)
            {
                l=i;
            }
        }
        if(l!=null)
        {
            this.sipDialogs[l][1]=sipDialog;
        }
        else
        {
            var array=new Array();
            array[0]=dialogId;
            array[1]=sipDialog;
            this.sipDialogs.push(array);
        }
    }
}

SIPClientTransaction.prototype.getDefaultDialog =function(){
    return this.defaultDialog;
}

SIPClientTransaction.prototype.setNextHop =function(hop){
    this.nextHop = hop;
}

SIPClientTransaction.prototype.getNextHop =function(){
    return this.nextHop;
}

SIPClientTransaction.prototype.alertIfStillInCallingStateBy =function(count){
    this.timeoutIfStillInCallingState = true;
    this.callingStateTimeoutCount = count;
}/*
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
 *  Implementation of the JAIN-SIP SIPServerTransaction .
 *  @see  gov/nist/javax/sip/stack/SIPServerTransaction.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *  
 */
var sipservertransaction=this;
function SIPServerTransaction(sipStack,newChannelToUse) {
    this.classname="SIPServerTransaction"; 
    this.auditTag = 0;
    this.sipStack=sipStack;
    this.originalRequest=null;
    this.encapsulatedChannel=newChannelToUse;
    this.wsurl=this.encapsulatedChannel.wsurl;
    this.disableTimeoutTimer();
    this.eventListeners = new Array();
    this.addEventListener(sipStack);
    this.timert=null;
    this.timer=null;
    
    if (sipStack.maxListenerResponseTime != -1) {
        this.timer=sipStack.getTimer();
        this.timer=setTimeout(this.listenerExecutionMaxTimer(), sipStack.maxListenerResponseTime * 1000);
    }
    
    this.rseqNumber =  (Math.random() * 1000);
    this.requestOf=null;
    this.dialog=null;
    this.pendingReliableResponse=null;
    this.provisionalResponseTask=null;
    this.isAckSeen=null;
    this.pendingSubscribeTransaction=null;
    this.inviteTransaction=null;
    sipservertransaction=this;
}

SIPServerTransaction.prototype = new SIPTransaction();
SIPServerTransaction.prototype.constructor=SIPServerTransaction;
SIPServerTransaction.prototype.TERMINATED_STATE=2;
SIPServerTransaction.prototype.TIMEOUT_ERROR=1;
SIPServerTransaction.prototype.RECEIVED="received";
SIPServerTransaction.prototype.INVITE="INVITE";
SIPServerTransaction.prototype.CANCEL="CANCEL";
SIPServerTransaction.prototype.BRANCH="branch";
SIPServerTransaction.prototype.BRANCH_MAGIC_COOKIE_LOWER_CASE="z9hg4bk";
SIPServerTransaction.prototype.TIMER_H=64;
SIPServerTransaction.prototype.TIMER_J=64;
SIPServerTransaction.prototype.TIMER_I=SIPTransaction.prototype.TIMER_I;
SIPServerTransaction.prototype.CONNECTION_LINGER_TIME=8;
SIPServerTransaction.prototype.BASE_TIMER_INTERVAL=500;
SIPServerTransaction.prototype.ExpiresHeader="Expires";
SIPServerTransaction.prototype.ContactHeader="Contact";

function listenerExecutionMaxTimer(){
    var serverTransaction = sipservertransaction;
    if (serverTransaction.getState() == null) {
        serverTransaction.terminate();
        var sipStack = serverTransaction.getSIPStack();
        sipStack.removePendingTransaction(serverTransaction);
        sipStack.removeTransaction(serverTransaction);
    }
}

SIPServerTransaction.prototype.setRequestInterface =function(newRequestOf){
    this.requestOf = newRequestOf;
}

SIPServerTransaction.prototype.getResponseChannel =function(){
    return this;
}

SIPServerTransaction.prototype.isMessagePartOfTransaction =function(messageToTest){
    var transactionMatches = false;
    var method = messageToTest.getCSeq().getMethod();
    if (method==this.INVITE || !this.isTerminated()) {
        var viaHeaders = messageToTest.getViaHeaders();
        if (viaHeaders != null) {
            var topViaHeader = viaHeaders.getFirst();
            var messageBranch = topViaHeader.getBranch();
            if (messageBranch != null) {
                if (messageBranch.toLowerCase().substring(0,7)!=this.BRANCH_MAGIC_COOKIE_LOWER_CASE) {
                    messageBranch = null;
                }
            }
            if (messageBranch != null && this.getBranch() != null) {
                if (method==this.CANCEL) {
                    if(this.getMethod()==this.CANCEL
                        && this.getBranch().toLowerCase()==messageBranch.toLowerCase()
                        && topViaHeader.getSentBy()==
                        this.getOriginalRequest().getViaHeaders().getFirst().getSentBy())
                        {
                        transactionMatches=true;
                    }
                    else
                    {
                        transactionMatches=false;
                    }
                } 
                else {
                    if(this.getBranch().toLowerCase()==messageBranch.toLowerCase()
                        && topViaHeader.getSentBy()==
                        this.getOriginalRequest().getViaHeaders().getFirst().getSentBy())
                        {
                        transactionMatches=true;
                    }
                    else
                    {
                        transactionMatches=false;
                    }
                }
            } 
            else {
                var originalFromTag = SIPTransaction.prototype.fromTag;
                var thisFromTag = messageToTest.getFrom().getTag();
                if(originalFromTag == null || thisFromTag == null)
                {
                    var skipFrom=true;
                }
                else
                {
                    skipFrom=false;
                }
                var originalToTag = SIPTransaction.prototype.toTag;
                var thisToTag = messageToTest.getTo().getTag();
                if(originalToTag == null || thisToTag == null)
                {
                    var skipTo=true;
                }
                else
                {
                    skipTo=false;
                }
                if(messageToTest instanceof SIPResponse)
                {
                    var isResponse=true;
                }
                else
                {
                    isResponse=false;
                }
                if (messageToTest.getCSeq().getMethod().toLowerCase()==this.CANCEL.toLowerCase()
                    && getOriginalRequest().getCSeq().getMethod().toLowerCase()!=this.CANCEL.toLowerCase()) {
                    transactionMatches = false;
                } 
                else if ((isResponse || this.getOriginalRequest().getRequestURI()==
                    messageToTest.getRequestURI())
                && (skipFrom || originalFromTag != null 
                    && originalFromTag.toLowerCase()==thisFromTag.toLowerCase())
                && (skipTo || originalToTag != null 
                    && originalToTag.toLowerCase()==thisToTag.toLowerCase())
                && this.getOriginalRequest().getCallId().getCallId().toLowerCase()==
                    messageToTest.getCallId().getCallId().toLowerCase()
                    && this.getOriginalRequest().getCSeq().getSeqNumber() == messageToTest
                    .getCSeq().getSeqNumber()
                    && ((messageToTest.getCSeq().getMethod()!=(this.CANCEL)) || this.getOriginalRequest()
                        .getMethod()==messageToTest.getCSeq().getMethod())
                    && topViaHeader==getOriginalRequest().getViaHeaders().getFirst()) {
                    transactionMatches = true;
                }
            }
        }
    }
    return transactionMatches;
}

SIPServerTransaction.prototype.isTransactionMapped =function(){
    return this.isMapped;
}

SIPServerTransaction.prototype.processRequest =function(transactionRequest,sourceChannel){
    var toTu = false;
    if (this.getRealState() == null) {
        this.setOriginalRequest(transactionRequest);
        this.setState("TRYING");
        toTu = true;
        this.setPassToListener();
    }
    else if (this.isInviteTransaction() && "COMPLETED" == this.getRealState()
        && transactionRequest.getMethod()=="ACK") {
        this.setState("CONFIRMED");
        if (!this.isReliable()) {
            this.enableTimeoutTimer(this.TIMER_I);
        } 
        else {
            this.setState("TERMINATED");
        }
        if (this.sipStack.isNon2XXAckPassedToListener()) {
            this.requestOf.processRequest(transactionRequest, this);
        } 
        return;
    }
    else if (transactionRequest.getMethod()==this.getOriginalRequest().getMethod()) {
        if ("PROCEEDING" == this.getRealState()|| "COMPLETED" == this.getRealState()) {
            if (this.lastResponse != null) {
                SIPTransaction.prototype.sendMessage.call(this,this.lastResponse);
            }
        } else if (transactionRequest.getMethod()=="ACK") {
            if (this.requestOf != null)
            {
                this.requestOf.processRequest(transactionRequest, this);
            }
        }
        return;
    }
    if ("COMPLETED" != this.getRealState()
        && "TERMINATED" != this.getRealState() && this.requestOf != null) {
        if (this.getOriginalRequest().getMethod()==transactionRequest.getMethod()) {
            if (toTu) {
                this.requestOf.processRequest(transactionRequest, this);
            } 
        } 
        else {
            if (this.requestOf != null) {
                this.requestOf.processRequest(transactionRequest, this);
            } 
        }
    }
    else {
        if (this.getSIPStack().isDialogCreated(this.getOriginalRequest().getMethod())
            && this.getRealState() == "TERMINATED"
            && transactionRequest.getMethod()=="ACK"
            && this.requestOf != null) {
            var thisDialog = this.dialog;
            if (thisDialog == null || !thisDialog.ackProcessed) {
                if (thisDialog != null) {
                    thisDialog.ackReceived(transactionRequest);
                    thisDialog.ackProcessed = true;
                }
                this.requestOf.processRequest(transactionRequest, this);
            } 
        } 
        else if (transactionRequest.getMethod()=="CANCEL") {
            this.sendMessage(transactionRequest.createResponse("OK"));
        }
    }
}

SIPServerTransaction.prototype.sendMessage =function(messageToSend){
    var transactionResponse = messageToSend;
    var statusCode = transactionResponse.getStatusCode();
    if (this.getOriginalRequest().getTopmostVia().getBranch() != null) {
        transactionResponse.getTopmostVia().setBranch(this.getBranch());
    } 
    else {
        transactionResponse.getTopmostVia().removeParameter(ParameterNames.BRANCH);
    }
    if (!this.getOriginalRequest().getTopmostVia().hasPort()) {
        transactionResponse.getTopmostVia().removePort();
    }
    if (!transactionResponse.getCSeq().getMethod()==this.getOriginalRequest().getMethod()) {
        this.sendResponseSRT(transactionResponse);
        return;
    }
    if (this.getRealState() == "TRYING") {
        if (statusCode <=199 && statusCode>=100) {
            this.setState("PROCEEDING");
        } 
        else if (200 <= statusCode && statusCode <= 699) {
            if (!this.isInviteTransaction()) {
                if (!this.isReliable()) {
                    this.setState("COMPLETED");
                    this.enableTimeoutTimer(this.TIMER_J);
                } 
                else {
                    this.setState("TERMINATED");
                }
            } 
            else {
                if (statusCode <=299 && statusCode>=200) {
                    this.disableTimeoutTimer();
                    this.collectionTime = this.TIMER_J;
                    this.setState("TERMINATED");
                } 
                else {
                    this.setState("COMPLETED");
                    this.enableTimeoutTimer(this.TIMER_H);
                }
            }
        }
    }
    else if (this.getRealState() == "PROCEEDING") {
        if (this.isInviteTransaction()) {
            if (statusCode <=299 && statusCode>=100) {
                this.disableTimeoutTimer();
                this.collectionTime = this.TIMER_J;
                this.setState("TERMINATED");
            } 
            else if (300 <= statusCode && statusCode <= 699) {
                this.setState("COMPLETED");
                this.enableTimeoutTimer(this.TIMER_H);
            }
        }
        else if (200 <= statusCode && statusCode <= 699) {
            this.setState("COMPLETED");
            if (!this.isReliable()) {
                this.enableTimeoutTimer(this.TIMER_J);
            } 
            else {
                this.setState("TERMINATED");
            }
        }
    }
    else if ("COMPLETED" == this.getRealState()) {
        return;
    }
    try {
        this.lastResponse = transactionResponse;
        this.sendResponseSRT(transactionResponse);
    } catch (ex) {
        this.setState("TERMINATED");
        this.collectionTime = 0;
        console.error("SIPServerTransaction:sendMessage(): catched exception:"+ex);
    }
}

SIPServerTransaction.prototype.getViaHost =function(){
    return this.getMessageChannel().getViaHost();
}

SIPServerTransaction.prototype.getViaPort =function(){
    return this.getMessageChannel().getViaPort();
}

SIPServerTransaction.prototype.fireTimeoutTimer =function(){
    clearTimeout(this.timer);
    if (this.getMethod()=="INVITE" && this.sipStack.removeTransactionPendingAck(this)) {
        return;
    }
    var dialog = this.dialog;
    if (this.getSIPStack().isDialogCreated(this.getOriginalRequest().getMethod())
        && ("CALLING" == this.getRealState() || "TRYING" == this.getRealState())) {
        dialog.setState(this.TERMINATED_STATE);
    } 
    else if (this.getOriginalRequest().getMethod()=="BYE") {
        if (dialog != null && dialog.isTerminatedOnBye()) {
            dialog.setState(this.TERMINATED_STATE);
        }
    }
    if ("COMPLETED" == this.getRealState() && this.isInviteTransaction()) {
        this.raiseErrorEvent(this.TIMEOUT_ERROR);
        this.setState("TERMINATED");
        this.sipStack.removeTransaction(this);
    } 
    else if ("COMPLETED" == this.getRealState() && !this.isInviteTransaction()) {
        this.setState("TERMINATED");
        this.sipStack.removeTransaction(this);
    } 
    else if ("CONFIRMED" == this.getRealState() && this.isInviteTransaction()) {
        this.setState("TERMINATED");
        this.sipStack.removeTransaction(this);
    } 
    else if (!isInviteTransaction()
        && ("COMPLETED" == this.getRealState() || "CONFIRMED" == this.getRealState())) {
        this.setState("TERMINATED");
    } 
    else if (isInviteTransaction() && "TERMINATED" == this.getRealState()) {
        this.raiseErrorEvent(this.TIMEOUT_ERROR);
        if (dialog != null) {
            dialog.setState(this.TERMINATED_STATE);
        }
    }
}

SIPServerTransaction.prototype.getLastResponse =function(){
    return this.lastResponse;
}

SIPServerTransaction.prototype.sendResponseSRT =function(transactionResponse){
    this.getMessageChannel().sendMessage(transactionResponse);
    this.startTransactionTimer();
}

SIPServerTransaction.prototype.sendResponse =function(response){
    var sipResponse = response;
    var dialog = this.dialog;
    if (response == null) {
        console.error("SIPServerTransaction:sendResponse(): null response argument");
        throw "SIPServerTransaction:sendResponse(): null response argument";
    }
    try {
        sipResponse.checkHeaders();
    } catch (ex) {
        console.error("SIPServerTransaction:sendMessage(): catched exception:"+ex);
        throw "SIPServerTransaction:sendResponse(): catched exception:"+ex;
    }
    
   
    if (sipResponse.getCSeq().getMethod()!=this.getMethod()) {
        console.error("SIPServerTransaction:sendResponse(): CSeq method does not match Request method of request that created the tx.");
        throw "SIPServerTransaction:sendResponse(): CSeq method does not match Request method of request that created the tx.";
    }
    
    if (this.getMethod()==("SUBSCRIBE") && response.getStatusCode() / 100 == 2) {
        if (response.getHeader(this.ExpiresHeader) == null) {
            console.error("SIPServerTransaction:sendResponse(): Expires header is mandatory in 2xx response of SUBSCRIBE");
            throw "SIPServerTransaction:sendResponse(): Expires header is mandatory in 2xx response of SUBSCRIBE";
        } else {
            var requestExpires = this.getOriginalRequest().getExpires();
            var responseExpires = response.getExpires();
            if (requestExpires != null
                && responseExpires.getExpires() > requestExpires.getExpires()) {
                console.error("SIPServerTransaction:sendResponse(): response Expires time exceeds request Expires time : See RFC 3265 3.1.1");
                throw "SIPServerTransaction:sendResponse():Response Expires time exceeds request Expires time : See RFC 3265 3.1.1";
            }
        }
    }
    
    if (sipResponse.getStatusCode() == 200
        && sipResponse.getCSeq().getMethod()=="INVITE"
        && sipResponse.getHeader(this.ContactHeader) == null) {
        console.error("SIPServerTransaction:sendResponse(): Contact Header is mandatory for the OK to the INVITE");
        throw "SIPServerTransaction:sendResponse(): Contact Header is mandatory for the OK to the INVITE";
    }
    
    if (!this.isMessagePartOfTransaction(response)) {
        console.error("SIPServerTransaction:sendResponse(): response does not belong to this transaction.");
        throw "SIPServerTransaction:sendResponse(): response does not belong to this transaction.";
    }
    
    try {
        if (dialog != null) {
            if (sipResponse.getStatusCode() / 100 == 2
                && this.sipStack.isDialogCreated(sipResponse.getCSeq().getMethod())) {
                if (dialog.getLocalTag() == null && sipResponse.getTo().getTag() == null) {
                    var utils=new Utils();
                    sipResponse.getTo().setTag(utils.generateTag());
                } 
                else if (dialog.getLocalTag() != null && sipResponse.getToTag() == null) {
                    sipResponse.setToTag(dialog.getLocalTag());
                } 
                else if (dialog.getLocalTag() != null && sipResponse.getToTag() != null
                    && dialog.getLocalTag()!=sipResponse.getToTag()) {
                    console.error("SIPServerTransaction:sendResponse(): tag mismatch dialogTag is "   + dialog.getLocalTag() + " responseTag is "+ sipResponse.getToTag());
                    throw "SIPServerTransaction:sendResponse(): tag mismatch dialogTag is "   + dialog.getLocalTag() + " responseTag is "+ sipResponse.getToTag();
                }
            }
            if (sipResponse.getCallId().getCallId()!=dialog.getCallId().getCallId()) {
                console.error("SIPServerTransaction:sendResponse(): dialog mismatch!");
                throw "SIPServerTransaction:sendResponse(): dialog mismatch!";
            }
        }
        
        var fromTag = this.getRequest().getFrom().getTag();
        if (fromTag != null && sipResponse.getFromTag() != null
            && sipResponse.getFromTag()!=fromTag) {
            console.error("SIPServerTransaction:sendResponse(): from tag of request does not match response from tag");
            throw "SIPServerTransaction:sendResponse(): from tag of request does not match response from tag";
        } 
        else if (fromTag != null) {
            sipResponse.getFrom().setTag(fromTag);
        } 
        if (dialog != null && response.getStatusCode() != 100) {
            dialog.setResponseTags(sipResponse);
            var oldState = dialog.getState();
            dialog.setLastResponse(this, response);
            if (oldState == null && dialog.getState() == "TERMINATED") {
                var event = new DialogTerminatedEvent(dialog.getSipProvider(), dialog);
                dialog.getSipProvider().handleEvent(event, this);
            }
        }
        this.sendMessage(response);
    } catch (ex) {
        this.setState("TERMINATED");
        this.raiseErrorEvent(this.TRANSPORT_ERROR);
        console.error("SIPServerTransaction:sendMessage(): catched exception:"+ex);
    }
}

SIPServerTransaction.prototype.getRealState =function(){
    return SIPTransaction.prototype.getState.call(this);
}

SIPServerTransaction.prototype.getState =function(){
    if (this.isInviteTransaction() && "TRYING" == SIPTransaction.prototype.getState.call(this)) {
        return "PROCEEDING";
    } 
    else {
        return SIPTransaction.prototype.getState.call(this);
    }
}

SIPServerTransaction.prototype.setState =function(newState){
    if (newState == "TERMINATED" && this.isReliable()) {
        this.collectionTime = this.TIMER_J;
    }
    SIPTransaction.prototype.setState.call(this,newState);
}

SIPServerTransaction.prototype.startTransactionTimer =function(){
    if(this.transactionTimerStarted==false)
    {
        this.transactionTimerStarted=true;
    }
    if (this.transactionTimerStarted) {
        if (this.sipStack.getTimer() != null) {
            this.timer = this.sipStack.getTimer();
            var transaction=this;
            this.timer=setInterval(function(){
                if(!transaction.isTerminated()){
                    transaction.fireTimer();
                }
            }, this.BASE_TIMER_INTERVAL);
        }
    }
}

SIPServerTransaction.prototype.getDialog =function(){
    return this.dialog;
}

SIPServerTransaction.prototype.setDialog =function(sipDialog,dialogId){
    this.dialog = sipDialog;
    if (dialogId != null) {
        this.dialog.setAssigned();
    }
}

SIPServerTransaction.prototype.terminate =function(){
    this.setState("TERMINATED");
}

SIPServerTransaction.prototype.setAckSeen =function(){
    this.isAckSeen = true;
}

SIPServerTransaction.prototype.ackSeen =function(){
    return this.isAckSeen;
}

SIPServerTransaction.prototype.setMapped =function(b){
    this.isMapped = true;
}

SIPServerTransaction.prototype.setPendingSubscribe =function(pendingSubscribeClientTx){
    this.pendingSubscribeTransaction = pendingSubscribeClientTx;
}

SIPServerTransaction.prototype.setInviteTransaction =function(st){
    this.inviteTransaction = st;
}

SIPServerTransaction.prototype.getCanceledInviteTransaction =function(){
    return this.inviteTransaction;
}

SIPServerTransaction.prototype.scheduleAckRemoval =function(){
    if (this.getMethod() == null || this.getMethod()!="ACK") {
        console.error("SIPServerTransaction:scheduleAckRemoval():  method is null[" + (this.getMethod() == null)+ "] or method is not ACK[" + this.getMethod() + "]");
        throw "SIPServerTransaction:scheduleAckRemoval():  method is null[" + (this.getMethod() == null)+ "] or method is not ACK[" + this.getMethod() + "]";
    }
    this.startTransactionTimer();
}

SIPServerTransaction.prototype.map =function(){
    var realState = this.getRealState();
    if (realState == null || realState == "TRYING") {
        this.isMapped = true;
    }
    this.sipStack.removePendingTransaction(this);
}/*
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
 *  Implementation of the JAIN-SIP Utils .
 *  @see  gov/nist/javax/sip/Utils.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Utils() {
    this.classname="Utils";
    //this.digester=null; 
    //there is no class MessageDigest, so i use the function digeste(MP5) to replace this object. 
    this.rand=Math.random();
    this.counter = 0;
    this.callIDCounter=null;
    this.toHex = ["0", "1", "2", "3", "4", "5", "6","7", "8", "9", "a", "b", "c", "d", "e", "f" ];
    this.signature= this.toHexString(this.getBytes(Math.round(this.rand*1000).toString()));
    this.instance = "Utils";
}

Utils.prototype.BRANCH_MAGIC_COOKIE="z9hG4bK";

Utils.prototype.getInstance =function(){
    return new Function('return new ' + this.instance)();
}

Utils.prototype.toHexString =function(b){
    var c = "";
    for (var i = 0; i < b.length; i++) {
        c=c+this.toHex[(b[i] >> 4) & 0x0F];
        c=c+this.toHex[b[i] & 0x0f];
    }
    return c;
}

Utils.prototype.getQuotedString =function(str){
    return '"' + str.replace( "\"", "\\\"" ) + '"';
}

Utils.prototype.reduceString =function(input){
    var newString = input.toLowerCase();
    var len = newString.length();
    var retval = "";
    for (var i = 0; i < len; i++) {
        if (newString.charAt(i) == ' ' || newString.charAt(i) == '\t')
        {
            continue;
        }
        else
        {
            retval = retval+newString.charAt(i);
        }
    }
    return retval;
}

Utils.prototype.generateCallIdentifier =function(address){
    var date = new Date().getTime() + this.callIDCounter+Math.round(this.rand*100000000000000000000);
    var x=new String(this.getBytes(date.toString()))
    var cid = this.digest(x);
    var cidString = cid;
    return cidString + "@" + address;
}

Utils.prototype.generateTag =function(){
    var x=Math.round(this.rand*10000000000);
    return x.toString(16);
}

Utils.prototype.generateBranchId =function(){
    var date=new Date().getTime();
    var num = Math.round(this.rand*100000000000000000000)+ this.counter+date;
    var x = new String(this.getBytes(num.toString()));
    var bid= this.digest(x);
    // prepend with a magic cookie to indicate we are bis09 compatible.
    return this.BRANCH_MAGIC_COOKIE + bid + this.signature;
}

Utils.prototype.responseBelongsToUs =function(response){
    var topmostVia = response.getTopmostVia();
    var branch = topmostVia.getBranch();
    var x=branch.length-1;
    var j=0;
    for(var i=this.signature.length-1;i>=0;i--)
    {
        if(this.signature[i]==branch[x])
        {
            j=j+1;
            x=x-1;
        }
    }
    if(branch != null && j==this.signature.length)
    {
        return true;
    }
    else
    {
        return false;
    }
}


Utils.prototype.getSignature =function(){
    return this.signature;
}

Utils.prototype.randomString= function(stringLength) {
        var chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXTZabcdefghiklmnopqrstuvwxyz";
        var randomString = '';
        for (var i=0; i<stringLength; i++) {
                var rnum = Math.floor(Math.random() * chars.length);
                randomString += chars.substring(rnum,rnum+1);
        }
        return randomString;
}

Utils.prototype.getBytes =function(str){
    var array=new Array();
    str=new String(str);
    for(var i=0;i<str.length;i++)
    {
        array[i]=str.charCodeAt(i);
    }
    return array;
}

Utils.prototype.digest =function(string){
    
    function RotateLeft(lValue, iShiftBits) {
        return (lValue<<iShiftBits) | (lValue>>>(32-iShiftBits));
    }
    
    function AddUnsigned(lX,lY) {
        var lX4,lY4,lX8,lY8,lResult;
        lX8 = (lX & 0x80000000);
        lY8 = (lY & 0x80000000);
        lX4 = (lX & 0x40000000);
        lY4 = (lY & 0x40000000);
        lResult = (lX & 0x3FFFFFFF)+(lY & 0x3FFFFFFF);
        if (lX4 & lY4) {
            return (lResult ^ 0x80000000 ^ lX8 ^ lY8);
        }
        if (lX4 | lY4) {
            if (lResult & 0x40000000) {
                return (lResult ^ 0xC0000000 ^ lX8 ^ lY8);
            } else {
                return (lResult ^ 0x40000000 ^ lX8 ^ lY8);
            }
        } else {
            return (lResult ^ lX8 ^ lY8);
        }
    }
    
    function F(x,y,z) {
        return (x & y) | ((~x) & z);
    }
    function G(x,y,z) {
        return (x & z) | (y & (~z));
    }
    function H(x,y,z) {
        return (x ^ y ^ z);
    }
    function I(x,y,z) {
        return (y ^ (x | (~z)));
    }
    
    function FF(a,b,c,d,x,s,ac) {
        a = AddUnsigned(a, AddUnsigned(AddUnsigned(F(b, c, d), x), ac));
        return AddUnsigned(RotateLeft(a, s), b);
    };
    
    function GG(a,b,c,d,x,s,ac) {
        a = AddUnsigned(a, AddUnsigned(AddUnsigned(G(b, c, d), x), ac));
        return AddUnsigned(RotateLeft(a, s), b);
    };
    
    function HH(a,b,c,d,x,s,ac) {
        a = AddUnsigned(a, AddUnsigned(AddUnsigned(H(b, c, d), x), ac));
        return AddUnsigned(RotateLeft(a, s), b);
    };
    
    function II(a,b,c,d,x,s,ac) {
        a = AddUnsigned(a, AddUnsigned(AddUnsigned(I(b, c, d), x), ac));
        return AddUnsigned(RotateLeft(a, s), b);
    };
    
    function ConvertToWordArray(string) {
        var lWordCount;
        var lMessageLength = string.length;
        var lNumberOfWords_temp1=lMessageLength + 8;
        var lNumberOfWords_temp2=(lNumberOfWords_temp1-(lNumberOfWords_temp1 % 64))/64;
        var lNumberOfWords = (lNumberOfWords_temp2+1)*16;
        var lWordArray=Array(lNumberOfWords-1);
        var lBytePosition = 0;
        var lByteCount = 0;
        while ( lByteCount < lMessageLength ) {
            lWordCount = (lByteCount-(lByteCount % 4))/4;
            lBytePosition = (lByteCount % 4)*8;
            lWordArray[lWordCount] = (lWordArray[lWordCount] | (string.charCodeAt(lByteCount)<<lBytePosition));
            lByteCount++;
        }
        lWordCount = (lByteCount-(lByteCount % 4))/4;
        lBytePosition = (lByteCount % 4)*8;
        lWordArray[lWordCount] = lWordArray[lWordCount] | (0x80<<lBytePosition);
        lWordArray[lNumberOfWords-2] = lMessageLength<<3;
        lWordArray[lNumberOfWords-1] = lMessageLength>>>29;
        return lWordArray;
    };
    
    function WordToHex(lValue) {
        var WordToHexValue="",WordToHexValue_temp="",lByte,lCount;
        for (lCount = 0;lCount<=3;lCount++) {
            lByte = (lValue>>>(lCount*8)) & 255;
            WordToHexValue_temp = "0" + lByte.toString(16);
            WordToHexValue = WordToHexValue + WordToHexValue_temp.substr(WordToHexValue_temp.length-2,2);
        }
        return WordToHexValue;
    };
    
    function Utf8Encode(string) {
        string = string.replace(/\r\n/g,"\n");
        var utftext = "";
        for (var n = 0; n < string.length; n++) {
            
            var c = string.charCodeAt(n);
            
            if (c < 128) {
                utftext += String.fromCharCode(c);
            }
            else if((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            }
            else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }
        
        }
        
        return utftext;
    };
    
    var x=Array();
    var k,AA,BB,CC,DD,a,b,c,d;
    var S11=7, S12=12, S13=17, S14=22;
    var S21=5, S22=9 , S23=14, S24=20;
    var S31=4, S32=11, S33=16, S34=23;
    var S41=6, S42=10, S43=15, S44=21;
    
    string = Utf8Encode(string);
    
    x = ConvertToWordArray(string);
    
    a = 0x67452301;
    b = 0xEFCDAB89;
    c = 0x98BADCFE;
    d = 0x10325476;
    
    for (k=0;k<x.length;k+=16) {
        AA=a;
        BB=b;
        CC=c;
        DD=d;
        a=FF(a,b,c,d,x[k+0], S11,0xD76AA478);
        d=FF(d,a,b,c,x[k+1], S12,0xE8C7B756);
        c=FF(c,d,a,b,x[k+2], S13,0x242070DB);
        b=FF(b,c,d,a,x[k+3], S14,0xC1BDCEEE);
        a=FF(a,b,c,d,x[k+4], S11,0xF57C0FAF);
        d=FF(d,a,b,c,x[k+5], S12,0x4787C62A);
        c=FF(c,d,a,b,x[k+6], S13,0xA8304613);
        b=FF(b,c,d,a,x[k+7], S14,0xFD469501);
        a=FF(a,b,c,d,x[k+8], S11,0x698098D8);
        d=FF(d,a,b,c,x[k+9], S12,0x8B44F7AF);
        c=FF(c,d,a,b,x[k+10],S13,0xFFFF5BB1);
        b=FF(b,c,d,a,x[k+11],S14,0x895CD7BE);
        a=FF(a,b,c,d,x[k+12],S11,0x6B901122);
        d=FF(d,a,b,c,x[k+13],S12,0xFD987193);
        c=FF(c,d,a,b,x[k+14],S13,0xA679438E);
        b=FF(b,c,d,a,x[k+15],S14,0x49B40821);
        a=GG(a,b,c,d,x[k+1], S21,0xF61E2562);
        d=GG(d,a,b,c,x[k+6], S22,0xC040B340);
        c=GG(c,d,a,b,x[k+11],S23,0x265E5A51);
        b=GG(b,c,d,a,x[k+0], S24,0xE9B6C7AA);
        a=GG(a,b,c,d,x[k+5], S21,0xD62F105D);
        d=GG(d,a,b,c,x[k+10],S22,0x2441453);
        c=GG(c,d,a,b,x[k+15],S23,0xD8A1E681);
        b=GG(b,c,d,a,x[k+4], S24,0xE7D3FBC8);
        a=GG(a,b,c,d,x[k+9], S21,0x21E1CDE6);
        d=GG(d,a,b,c,x[k+14],S22,0xC33707D6);
        c=GG(c,d,a,b,x[k+3], S23,0xF4D50D87);
        b=GG(b,c,d,a,x[k+8], S24,0x455A14ED);
        a=GG(a,b,c,d,x[k+13],S21,0xA9E3E905);
        d=GG(d,a,b,c,x[k+2], S22,0xFCEFA3F8);
        c=GG(c,d,a,b,x[k+7], S23,0x676F02D9);
        b=GG(b,c,d,a,x[k+12],S24,0x8D2A4C8A);
        a=HH(a,b,c,d,x[k+5], S31,0xFFFA3942);
        d=HH(d,a,b,c,x[k+8], S32,0x8771F681);
        c=HH(c,d,a,b,x[k+11],S33,0x6D9D6122);
        b=HH(b,c,d,a,x[k+14],S34,0xFDE5380C);
        a=HH(a,b,c,d,x[k+1], S31,0xA4BEEA44);
        d=HH(d,a,b,c,x[k+4], S32,0x4BDECFA9);
        c=HH(c,d,a,b,x[k+7], S33,0xF6BB4B60);
        b=HH(b,c,d,a,x[k+10],S34,0xBEBFBC70);
        a=HH(a,b,c,d,x[k+13],S31,0x289B7EC6);
        d=HH(d,a,b,c,x[k+0], S32,0xEAA127FA);
        c=HH(c,d,a,b,x[k+3], S33,0xD4EF3085);
        b=HH(b,c,d,a,x[k+6], S34,0x4881D05);
        a=HH(a,b,c,d,x[k+9], S31,0xD9D4D039);
        d=HH(d,a,b,c,x[k+12],S32,0xE6DB99E5);
        c=HH(c,d,a,b,x[k+15],S33,0x1FA27CF8);
        b=HH(b,c,d,a,x[k+2], S34,0xC4AC5665);
        a=II(a,b,c,d,x[k+0], S41,0xF4292244);
        d=II(d,a,b,c,x[k+7], S42,0x432AFF97);
        c=II(c,d,a,b,x[k+14],S43,0xAB9423A7);
        b=II(b,c,d,a,x[k+5], S44,0xFC93A039);
        a=II(a,b,c,d,x[k+12],S41,0x655B59C3);
        d=II(d,a,b,c,x[k+3], S42,0x8F0CCC92);
        c=II(c,d,a,b,x[k+10],S43,0xFFEFF47D);
        b=II(b,c,d,a,x[k+1], S44,0x85845DD1);
        a=II(a,b,c,d,x[k+8], S41,0x6FA87E4F);
        d=II(d,a,b,c,x[k+15],S42,0xFE2CE6E0);
        c=II(c,d,a,b,x[k+6], S43,0xA3014314);
        b=II(b,c,d,a,x[k+13],S44,0x4E0811A1);
        a=II(a,b,c,d,x[k+4], S41,0xF7537E82);
        d=II(d,a,b,c,x[k+11],S42,0xBD3AF235);
        c=II(c,d,a,b,x[k+2], S43,0x2AD7D2BB);
        b=II(b,c,d,a,x[k+9], S44,0xEB86D391);
        a=AddUnsigned(a,AA);
        b=AddUnsigned(b,BB);
        c=AddUnsigned(c,CC);
        d=AddUnsigned(d,DD);
    }
    
    var temp = WordToHex(a)+WordToHex(b)+WordToHex(c)+WordToHex(d);
    
    return temp.toLowerCase();
}
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
 *  Implementation of the JAIN-SIP EventWrapper .
 *  @see  gov/nist/javax/sip/EventWrapper.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function EventWrapper(sipEvent,transaction) {
    this.classname="EventWrapper";
    this.sipEvent= sipEvent;
    this.transaction= transaction;
}


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
 *  Implementation of the JAIN-SIP EventScanner .
 *  @see  gov/nist/javax/sip/EventScanner.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function EventScanner(sipStackImpl) {
    this.classname="EventScanner";
    this.isStopped=null;
    this.refCount=null;
    this.pendingEvents = new Array();
    this.eventMutex = new Array();
    this.eventMutex[0]=0;
    this.sipStack=sipStackImpl;
}

EventScanner.prototype.BRANCH_MAGIC_COOKIE="z9hG4bK";

EventScanner.prototype.incrementRefcount =function(){
    
}

EventScanner.prototype.addEvent =function(eventWrapper){
    this.pendingEvents.push(eventWrapper);
//this.eventMutex.notify();
}

EventScanner.prototype.stop =function(){
    if (this.refCount > 0)
    {
        this.refCount--;
    }
    if (this.refCount == 0) {
        this.isStopped = true;
    //this.eventMutex.notify();
    }
}

EventScanner.prototype.forceStop =function(){
    this.isStopped = true;
    this.refCount = 0;
//this.eventMutex.notify();
}

EventScanner.prototype.deliverEvent =function(eventWrapper){
    var sipEvent = eventWrapper.sipEvent;
    var sipListener = this.sipStack.getSipListener();
    if (sipEvent instanceof RequestEvent) {
        var sipRequest = sipEvent.getRequest();
        var tx = this.sipStack.findTransaction(sipRequest, true);
        if (tx != null && !tx.passToListener()) {
            return;
        }
        else if (this.sipStack.findPendingTransaction(sipRequest) != null) {
            return;
        }
        else {
            var st = eventWrapper.transaction;
            this.sipStack.putPendingTransaction(st);
        }
        sipRequest.setTransaction(eventWrapper.transaction);
        if (sipListener != null)
        {
            sipListener.processRequest(sipEvent);
        }
        if (eventWrapper.transaction != null) {
            var dialog = eventWrapper.transaction.getDialog();
            if (dialog != null)
            {
                dialog.requestConsumed();
            }
        }
        if (eventWrapper.transaction != null)
        {
            this.sipStack.removePendingTransaction(eventWrapper.transaction);
        }
        if (eventWrapper.transaction.getOriginalRequest().getMethod()=="ACK") {

            eventWrapper.transaction.setState("TERMINATED");
        }
    }
    else if (sipEvent instanceof ResponseEvent) {
        var responseEvent = sipEvent;
        var sipResponse = responseEvent.getResponse();
        var sipDialog = responseEvent.getDialog();
        if (sipListener != null) {
            tx = eventWrapper.transaction;
            if (tx != null) {
                tx.setPassToListener();
            }
            sipListener.processResponse(sipEvent);//for the level application
        }
        if ((sipDialog != null && (sipDialog.getState() == null || sipDialog.getState()!="TERMINATED"))
            && (sipResponse.getStatusCode() == 481 || sipResponse.getStatusCode() == 408)) {
            sipDialog.doDeferredDelete();
        }
        if (sipResponse.getCSeq().getMethod()=="INVITE"&& sipDialog != null
            && sipResponse.getStatusCode() == 200) {
            sipDialog.doDeferredDeleteIfNoAckSent(sipResponse.getCSeq().getSeqNumber());
        }
        var ct = eventWrapper.transaction;
        if (ct != null && "COMPLETED" == ct.getState() && ct.getOriginalRequest() != null
            && ct.getOriginalRequest().getMethod()!="INVITE") {
            ct.clearState();
        }
    } else if (sipEvent instanceof TimeoutEvent) {
        if (sipListener != null)
        {
            sipListener.processTimeout(sipEvent);//the application level will process the infomation
        }
    } else if (sipEvent instanceof DialogTimeoutEvent) {
        if (sipListener != null && sipListener instanceof SipListenerExt) {
            sipListener.processDialogTimeout(sipEvent);  //the application level will process the infomation                  
        }

    } else if (sipEvent instanceof TransactionTerminatedEvent) {
        if (sipListener != null)
        {
            sipListener.processTransactionTerminated(sipEvent);//the application level will process the infomation
        }
    } else if (sipEvent instanceof DialogTerminatedEvent) {
        if (sipListener != null)
        {
            sipListener.processDialogTerminated(sipEvent);//the application level will process the infomation
        }
    }
}

EventScanner.prototype.run =function(){
    while (true) {
        var eventWrapper = null;
        var eventsToDeliver;
        while (this.pendingEvents.length==0) {
            if (this.isStopped) {
                return;
            }
            try {
            //setTimeout();
            //eventMutex.wait(threadHandle.getPingIntervalInMillisecs());
            } catch (ex) {
                console.error("EventScanner:run(): catched exception:"+ex);
                return;
            }
        }
        eventsToDeliver = this.pendingEvents;
        this.pendingEvents = new Array();
        for(var i=0;i<eventsToDeliver.length;i++)
        {
            eventWrapper = eventsToDeliver[i];
            this.deliverEvent(eventWrapper);
        }
    } 
}

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
 *  Implementation of the JAIN-SIP DialogTerminatedEvent .
 *  @see  gov/nist/javax/sip/DialogTerminatedEvent.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function DialogTerminatedEvent(source,dialog) {
    this.classname="DialogTerminatedEvent";
    this.mDialog=dialog;
    this.source=source;
}

DialogTerminatedEvent.prototype.getDialog =function(){
    return this.mDialog;
}

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
 *  Implementation of the JAIN-SIP DialogTimeoutEvent .
 *  @see  gov/nist/javax/sip/DialogTimeoutEvent.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function DialogTimeoutEvent(source,dialog,reason) {
    this.classname="DialogTimeoutEvent";
    this.serialVersionUID = "-2514000059989311925L";
    this.source=source;
    this.m_dialog = dialog;
    this.m_reason = reason;
}

DialogTimeoutEvent.prototype.AckNotReceived="AckNotReceived";
DialogTimeoutEvent.prototype.AckNotSent="AckNotSent";
DialogTimeoutEvent.prototype.ReInviteTimeout="ReInviteTimeout";

DialogTimeoutEvent.prototype.getDialog =function(){
    return this.m_dialog;
}
DialogTimeoutEvent.prototype.getReason =function(){
    return this.m_reason;
}
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
 *  Implementation of the JAIN-SIP TransactionTerminatedEvent .
 *  @see  gov/nist/javax/sip/TransactionTerminatedEvent.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function TransactionTerminatedEvent() {
    this.classname="TransactionTerminatedEvent";
    this.mTimeout=null;
    this.mIsServerTransaction=null;
    this.mServerTransaction=null;
    this.mClientTransaction=null;
    this.source=null;
    if(arguments[1] instanceof SIPServerTransaction)
    {
        var source=arguments[0];
        var serverTransaction=arguments[1];
        this.source=source;
        this.mServerTransaction = serverTransaction;
        this.mIsServerTransaction = true;
    }
    else if(arguments[1] instanceof SIPClientTransaction)
    {
        source=arguments[0];
        var clientTransaction=arguments[1];
        this.source=source;
        this.mClientTransaction = clientTransaction;
        this.mIsServerTransaction = false;
    }
}

TransactionTerminatedEvent.prototype.isServerTransaction =function(){
    return this.mIsServerTransaction;
}

TransactionTerminatedEvent.prototype.getClientTransaction =function(){
    return this.mClientTransaction;
}

TransactionTerminatedEvent.prototype.getServerTransaction =function(){
    return this.mServerTransaction;
}
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
 *  Implementation of the JAIN-SIP RequestEvent .
 *  @see  gov/nist/javax/sip/RequestEvent.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function RequestEvent(source,serverTransaction,dialog,request) {
    this.classname="RequestEvent";
    this.mDialog = dialog;
    this.mRequest = request;
    this.mServerTransaction = serverTransaction;
    this.source = source;
}

RequestEvent.prototype.getDialog =function(){
    return this.mDialog;
}

RequestEvent.prototype.getRequest =function(){
    return this.mRequest;
}

RequestEvent.prototype.getServerTransaction =function(){
    return this.mServerTransaction;
}

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
 *  Implementation of the JAIN-SIP TimeoutEvent .
 *  @see  gov/nist/javax/sip/TimeoutEvent.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function TimeoutEvent() {
    this.classname="TimeoutEvent";
    this.mTimeout=null;
    this.mIsServerTransaction=null;
    this.mServerTransaction=null;
    this.mClientTransaction=null;
    this.source=null;
    if(arguments[1] instanceof SIPServerTransaction)
    {
        var source=arguments[0];
        var serverTransaction=arguments[1];
        var timeout=arguments[2];
        this.source=source;
        this.mTimeout = timeout;
        this.mServerTransaction = serverTransaction;
        this.mIsServerTransaction = true;
    }
    else if(arguments[1] instanceof SIPClientTransaction)
    {
        source=arguments[0];
        var clientTransaction=arguments[1];
        timeout=arguments[2];
        this.source=source;
        this.mTimeout = timeout;
        this.mClientTransaction = clientTransaction;
        this.mIsServerTransaction = false;
    }
}

TimeoutEvent.prototype = new TransactionTerminatedEvent();
TimeoutEvent.prototype.constructor=TimeoutEvent;

TimeoutEvent.prototype.getTimeout =function(){
    return this.mTimeout;
}

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
 *  Implementation of the JAIN-SIP TransactionTerminatedEvent .
 *  @see  gov/nist/javax/sip/TransactionTerminatedEvent.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function TransactionTerminatedEvent() {
    this.classname="TransactionTerminatedEvent";
    this.mTimeout=null;
    this.mIsServerTransaction=null;
    this.mServerTransaction=null;
    this.mClientTransaction=null;
    this.source=null;
    if(arguments[1] instanceof SIPServerTransaction)
    {
        var source=arguments[0];
        var serverTransaction=arguments[1];
        this.source=source;
        this.mServerTransaction = serverTransaction;
        this.mIsServerTransaction = true;
    }
    else if(arguments[1] instanceof SIPClientTransaction)
    {
        source=arguments[0];
        var clientTransaction=arguments[1];
        this.source=source;
        this.mClientTransaction = clientTransaction;
        this.mIsServerTransaction = false;
    }
}

TransactionTerminatedEvent.prototype.isServerTransaction =function(){
    return this.mIsServerTransaction;
}

TransactionTerminatedEvent.prototype.getClientTransaction =function(){
    return this.mClientTransaction;
}

TransactionTerminatedEvent.prototype.getServerTransaction =function(){
    return this.mServerTransaction;
}
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
 *  Implementation of the JAIN-SIP DefaultAddressResolver .
 *  @see  gov/nist/javax/sip/DefaultAddressResolver.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function DefaultAddressResolver() {
    this.classname="DefaultAddressResolver"; 
}

DefaultAddressResolver.prototype.resolveAddress =function(inputAddress){
    if  (inputAddress.getPort()  != -1)
    {
        return inputAddress;
    }
    else 
    {
        var mp=new MessageProcessor();
        return new HopImpl(inputAddress.getHost(),mp.getDefaultPort(),inputAddress.getTransport());
    }
}
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
 *  Implementation of the JAIN-SIP ResponseEvent .
 *  @see  gov/nist/javax/sip/ResponseEvent.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ResponseEvent(source,serverTransaction,dialog,response) {
    this.classname="ResponseEvent";
    this.mDialog = dialog;
    this.mResponse = response;
    this.mServerTransaction = serverTransaction;
    this.source = source;
}

ResponseEvent.prototype.getDialog =function(){
    return this.mDialog;
}
ResponseEvent.prototype.getResponse =function(){
    return this.mResponse;
}
ResponseEvent.prototype.getClientTransaction =function(){
    return this.mClientTransaction;
}
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
 *  Implementation of the JAIN-SIP ResponseEventExt .
 *  @see  gov/nist/javax/sip/ResponseEventExt.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ResponseEventExt(source,clientTransaction,dialog,response) {
    this.classname="ResponseEventExt";
    this.mDialog = dialog;
    this.mResponse = response;
    this.mServerTransaction = clientTransaction;
    this.source = source;
    this.m_originalTransaction = clientTransaction;
}

ResponseEventExt.prototype = new ResponseEvent();
ResponseEventExt.prototype.constructor=ResponseEventExt;

ResponseEventExt.prototype.isForkedResponse =function(){
    return this.mServerTransaction == null && this.m_originalTransaction != null;
}

ResponseEventExt.prototype.setOriginalTransaction =function(originalTransaction){
    this.m_originalTransaction = originalTransaction;
}

ResponseEventExt.prototype.getOriginalTransaction =function(){
    return this.m_originalTransaction;
}
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
 *  Implementation of the JAIN-SIP DialogFilter .
 *  @see  gov/nist/javax/sip/DialogFilter.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function DialogFilter(sipStack) {
    this.classname="DialogFilter"; 
    this.sipStack=sipStack;
    this.transactionChannel=null;
    this.listeningPoint=null;
}

DialogFilter.prototype.processResponse =function(){
    if(arguments.length==2)
    {
        var sipResponse=arguments[0];
        var incomingChannel=arguments[1];
        this.processResponseargu2(sipResponse, incomingChannel);
    }
    else 
    {
        var response=arguments[0];
        var incomingMessageChannel=arguments[1];
        var dialog=arguments[2];
        this.processResponseargu3(response, incomingMessageChannel, dialog);
    }
}

DialogFilter.prototype.processResponseargu2 =function(sipResponse,incomingChannel){
    var dialogID = sipResponse.getDialogId(false);
    var sipDialog = this.sipStack.getDialog(dialogID);
    var method = sipResponse.getCSeq().getMethod();
    var utils=new Utils();
    if (this.sipStack.checkBranchId && !utils.responseBelongsToUs(sipResponse)) {
        return;
    }
    if (this.listeningPoint == null) {
        return;
    }
    var sipProvider = this.listeningPoint.getProvider();
    if (sipProvider == null) {
        return;
    }
    if (sipProvider.getSipListener() == null) {
        return;
    }
    var transaction = this.transactionChannel;
    if (sipDialog == null && transaction != null) {
        sipDialog = transaction.getDialog(dialogID);
        if (sipDialog != null && sipDialog.getState() == "TERMINATED")
            sipDialog = null;
    }
    if (this.transactionChannel != null) {
        var originalFrom = this.transactionChannel.getRequest().getFromTag();
        if (originalFrom == null ^ sipResponse.getFrom().getTag() == null) {
            return;
        }
        if (originalFrom != null
            && originalFrom.toLowerCase()!=sipResponse.getFrom().getTag().toLowerCase()) {
            return;
        }
    }
    if (this.sipStack.isDialogCreated(method) && sipResponse.getStatusCode() != 100
        && sipResponse.getFrom().getTag() != null && sipResponse.getTo().getTag() != null
        && sipDialog == null) {
        if (sipProvider.isAutomaticDialogSupportEnabled()) {
            if (this.transactionChannel != null) {
                if (sipDialog == null) {
                    sipDialog = this.sipStack.createDialog(this.transactionChannel, sipResponse);
                    this.transactionChannel.setDialog(sipDialog, sipResponse.getDialogId(false));
                }
            } else {
                sipDialog = this.sipStack.createDialog(sipProvider, sipResponse);
            }
        }
    } else {
        if (sipDialog != null && transaction == null
            && sipDialog.getState() != "TERMINATED") {
            if (sipDialog.getState() == "TERMINATED" && 
                (sipResponse.getStatusCode()>= 200||sipResponse.getStatusCode()<=299)) {
                if ((sipResponse.getStatusCode()>= 200||sipResponse.getStatusCode()<=299)
                    && sipResponse.getCSeq().getMethod()=="INVITE") {
                    var ackRequest = sipDialog.createAck(sipResponse.getCSeq().getSeqNumber());
                    sipDialog.sendAck(ackRequest);
                }
                return;
            } 
            else {
                var ackAlreadySent = false;
                if (sipDialog.isAckSeen() && sipDialog.getLastAckSent() != null) {
                    if (sipDialog.getLastAckSent().getCSeq().getSeqNumber() == sipResponse.getCSeq().getSeqNumber()
                        && sipResponse.getDialogId(false)==sipDialog.getLastAckSent().getDialogId(false)) {
                        ackAlreadySent = true;
                    }
                }
                if (ackAlreadySent && sipResponse.getCSeq().getMethod()==sipDialog.getMethod()) {
                    sipDialog.resendAck();
                    return;
                }
            }
        }

    }
    if (sipDialog != null && sipResponse.getStatusCode() != 100
        && sipResponse.getTo().getTag() != null) {
        sipDialog.setLastResponse(transaction, sipResponse);
    }
    var responseEvent = new ResponseEventExt(sipProvider,transaction,sipDialog,sipResponse);
    if (sipResponse.getCSeq().getMethod()=="INVITE") {
        var originalTx = this.sipStack.getForkedTransaction(sipResponse.getTransactionId());
        responseEvent.setOriginalTransaction(originalTx);
    }
    sipProvider.handleEvent(responseEvent, transaction);
}

DialogFilter.prototype.processResponseargu3 =function(response,incomingMessageChannel,dialog){
    if (this.listeningPoint == null) {
        return;
    }
    var utils=new Utils();
    if (this.sipStack.checkBranchId && !utils.responseBelongsToUs(response)) {
        return;
    }
    var sipProvider = this.listeningPoint.getProvider();
    if (sipProvider == null) {
        return;
    }
    if (sipProvider.getSipListener() == null) {
        return;
    }
    var transaction = this.transactionChannel;
    if (transaction == null) {
        if (dialog != null) {
            if (response.getStatusCode()<200||response.getStatusCode()>299) {
                return;
            } else if (dialog.getState() == "TERMINATED") {
                return;
            } else {
                var ackAlreadySent = false;
                if (dialog.isAckSeen() && dialog.getLastAckSent() != null) {
                    if (dialog.getLastAckSent().getCSeq().getSeqNumber() == response.getCSeq().getSeqNumber()) {
                        ackAlreadySent = true;
                    }
                }
                if (ackAlreadySent
                    && response.getCSeq().getMethod()==dialog.getMethod()) {
                    dialog.resendAck();
                    return;
                }
            }
        }
        var sipEvent = new ResponseEventExt(sipProvider,transaction,dialog,response);
        if (response.getCSeqHeader().getMethod()=="INVITE") {
            var forked = this.sipStack.getForkedTransaction(response.getTransactionId());
            sipEvent.setOriginalTransaction(forked);
        }
        sipProvider.handleEvent(sipEvent, transaction);
        return;
    }
    var responseEvent = null;
    responseEvent = new ResponseEventExt(sipProvider,transaction,dialog,response);
    if (response.getCSeqHeader().getMethod()=="INVITE") {
        responseEvent.setOriginalTransaction(transaction);
    }
    if (dialog != null && response.getStatusCode() != 100) {
        dialog.setLastResponse(transaction, response);
        transaction.setDialog(dialog, dialog.getDialogId());
    }
    sipProvider.handleEvent(responseEvent, transaction);
}

DialogFilter.prototype.getSipStack =function(){
    return this.sipStack;
}

DialogFilter.prototype.sendBadRequestResponse =function(sipRequest,transaction,reasonPhrase){
    var sipResponse = sipRequest.createResponse(400);
    if (reasonPhrase != null)
    {
        sipResponse.setReasonPhrase(reasonPhrase);
    }
    var mfi=new MessageFactoryImpl();
    var serverHeader = mfi.getDefaultServerHeader();
    if (serverHeader != null) {
        sipResponse.setHeader(serverHeader);
    }
    if (sipRequest.getMethod()=="INVITE") {
        this.sipStack.addTransactionPendingAck(transaction);
    }
    transaction.sendResponse(sipResponse);
}


DialogFilter.prototype.sendCallOrTransactionDoesNotExistResponse =function(sipRequest,transaction){
    var sipResponse = sipRequest.createResponse(481);
    var mfi=new MessageFactoryImpl();
    var serverHeader = mfi.getDefaultServerHeader();
    if (serverHeader != null) {
        sipResponse.setHeader(serverHeader);
    }
    if (sipRequest.getMethod()=="INVITE") {
        this.sipStack.addTransactionPendingAck(transaction);
    }
    transaction.sendResponse(sipResponse);
}

DialogFilter.prototype.sendLoopDetectedResponse =function(sipRequest,transaction){
    var sipResponse = sipRequest.createResponse(482);
    var mfi=new MessageFactoryImpl();
    var serverHeader = mfi.getDefaultServerHeader();
    if (serverHeader != null) {
        sipResponse.setHeader(serverHeader);
    }
    this.sipStack.addTransactionPendingAck(transaction);
    transaction.sendResponse(sipResponse);
}

DialogFilter.prototype.processRequest =function(sipRequest,incomingMessageChannel){
    if (this.listeningPoint == null) {
        return;
    }
    var sipStack = this.transactionChannel.getSIPStack();
    var sipProvider = this.listeningPoint.getProvider();
    if (sipProvider == null) {
        return;
    }
    var transaction = this.transactionChannel;
    var dialogId = sipRequest.getDialogId(true);
    var dialog = sipStack.getDialog(dialogId);
    if (dialog != null && sipProvider != dialog.getSipProvider()) {
        var contact = dialog.getMyContactHeader();
        if (contact != null) {
            var contactUri = contact.getAddress().getURI();
            var ipAddress = contactUri.getHost();
            var contactPort = contactUri.getPort();
            if (contactPort == -1) {
                contactPort = 5060;
            }
            if (ipAddress != null
                && (ipAddress!=this.listeningPoint.getHostAddress() || contactPort != this.listeningPoint.getPort())) {
                dialog = null;
            }
        }
    }
    if (sipProvider.isAutomaticDialogSupportEnabled()
        && sipProvider.isDialogErrorsAutomaticallyHandled()
        && sipRequest.getToTag() == null) {
        var sipServerTransaction = sipStack.findMergedTransaction(sipRequest);
        if (sipServerTransaction != null) {
            this.sendLoopDetectedResponse(sipRequest, transaction);
            return;
        }
    }
    if (sipRequest.getMethod()=="ACK") {
        if (dialog == null) {
            var ackTransaction = sipStack.findTransactionPendingAck(sipRequest);
            if (ackTransaction != null) {
                ackTransaction.setAckSeen();
                sipStack.removeTransaction(ackTransaction);
                sipStack.removeTransactionPendingAck(ackTransaction);
                return;
            }
        } 
        else {
            if (!dialog.handleAck(transaction)) {
                ackTransaction = sipStack.findTransactionPendingAck(sipRequest);
                if (ackTransaction != null) {
                    ackTransaction.setAckSeen();
                    sipStack.removeTransaction(ackTransaction);
                    sipStack.removeTransactionPendingAck(ackTransaction);
                }
                return;
            } 
            else {
                transaction.passToListener();
                dialog.addTransaction(transaction);
                dialog.addRoute(sipRequest);
                transaction.setDialog(dialog, dialogId);
                if (sipRequest.getMethod()=="INVITE"
                    && sipProvider.isDialogErrorsAutomaticallyHandled()) {
                    sipStack.putInMergeTable(transaction, sipRequest);
                }
                if (sipStack.deliverTerminatedEventForAck) {
                    sipStack.addTransaction(transaction);
                    transaction.scheduleAckRemoval();
                } else {
                    transaction.setMapped(true);
                }
            }
        }
    }
    else if (sipRequest.getMethod()=="BYE") {
        if (dialog != null && !dialog.isRequestConsumable(sipRequest)) {
            if (dialog.getRemoteSeqNumber() >= sipRequest.getCSeq().getSeqNumber()
                && transaction.getState() == "TRYING") {
                this.sendServerInternalErrorResponse(sipRequest, transaction);
            }
            if (transaction != null)
            {
                sipStack.removeTransaction(transaction);
            }
            return;
        } 
        else if (dialog == null && sipProvider.isAutomaticDialogSupportEnabled()) {
            var response = sipRequest.createResponse(481);
            response.setReasonPhrase("Dialog Not Found");
            transaction.sendResponse(response);
            if (transaction != null) {
                sipStack.removeTransaction(transaction);
                transaction = null;
            }
            return;
        }
        if (transaction != null && dialog != null) {
            if (sipProvider == dialog.getSipProvider()) {
                sipStack.addTransaction(transaction);
                dialog.addTransaction(transaction);
                transaction.setDialog(dialog, dialogId);
            }
        }
    } 
    else if (sipRequest.getMethod()=="CANCEL") {
        var st = sipStack.findCancelTransaction(sipRequest, true);
        if (sipRequest.getMethod()=="CANCEL") {
            if (st != null && st.getState() == "TERMINATED") {
                transaction.sendResponse(sipRequest.createResponse("OK"));
                return;
            }
        }
        if (transaction != null && st != null && st.getDialog() != null) {
            transaction.setDialog(st.getDialog(), dialogId);
            dialog = st.getDialog();
        } 
        else if (st == null && sipProvider.isAutomaticDialogSupportEnabled()
            && transaction != null) {
            response = sipRequest.createResponse(481);
            sipProvider.sendResponse(response);
            if (transaction != null) {
                sipStack.removeTransaction(transaction);
            }
            return;
        }
        if (st != null) {
            if (transaction != null) {
                sipStack.addTransaction(transaction);
                transaction.setPassToListener();
                transaction.setInviteTransaction(st);
            }
        }
    }
    else if (sipRequest.getMethod()=="INVITE") {
        var lastTransaction = dialog == null ? null : dialog.getInviteTransaction();
        if (dialog != null && transaction != null && lastTransaction != null
            && sipRequest.getCSeq().getSeqNumber() > dialog.getRemoteSeqNumber()
            && lastTransaction instanceof SIPServerTransaction
            && sipProvider.isDialogErrorsAutomaticallyHandled()
            && dialog.isSequnceNumberValidation()
            && lastTransaction.isInviteTransaction()
            && lastTransaction.getState() != "COMPLETED"
            && lastTransaction.getState() != "TERMINATED"
            && lastTransaction.getState() != "CONFIRMED") {
            this.sendServerInternalErrorResponse(sipRequest, transaction);
            return;
        }
        lastTransaction = (dialog == null ? null : dialog.getLastTransaction());
        if (dialog != null
            && sipProvider.isDialogErrorsAutomaticallyHandled()
            && lastTransaction != null
            && lastTransaction.isInviteTransaction()
            && lastTransaction instanceof SIPClientTransaction
            && lastTransaction.getLastResponse() != null
            && lastTransaction.getLastResponse().getStatusCode() == 200
            && !dialog.isAckSent(lastTransaction.getLastResponse().getCSeq()
                .getSeqNumber())) {
            this.sendRequestPendingResponse(sipRequest, transaction);
            return;
        }
        if (dialog != null && lastTransaction != null
            && sipProvider.isDialogErrorsAutomaticallyHandled()
            && lastTransaction.isInviteTransaction()
            && lastTransaction instanceof ServerTransaction && !dialog.isAckSeen()) {
            this.sendRequestPendingResponse(sipRequest, transaction);
            return;
        }
    }
    if (dialog != null && transaction != null && sipRequest.getMethod()!="BYE"
        && sipRequest.getMethod()!="CANCEL"
        && sipRequest.getMethod()!="ACK") {
        if (!dialog.isRequestConsumable(sipRequest)) {
            if (dialog.getRemoteSeqNumber() >= sipRequest.getCSeq().getSeqNumber()
                && sipProvider.isDialogErrorsAutomaticallyHandled()
                && (transaction.getState() == "TRYING" || transaction.getState() == "PROCEEDING")) {
                this.sendServerInternalErrorResponse(sipRequest, transaction);
            }
            return;
        }
        try {
            if (sipProvider == dialog.getSipProvider()) {
                sipStack.addTransaction(transaction);
                dialog.addTransaction(transaction);
                dialog.addRoute(sipRequest);
                transaction.setDialog(dialog, dialogId);
            }
        } catch (ex) {
            console.error("DialogFilter:processRequest(): catched exception:"+ex);
            sipStack.removeTransaction(transaction);
            return;
        }

    }
    var sipEvent;
    if (transaction != null) {
        sipEvent = new RequestEvent(sipProvider, transaction, dialog, sipRequest);
    } 
    else {
        sipEvent = new RequestEvent(sipProvider, null, dialog, sipRequest);
    }
    sipProvider.handleEvent(sipEvent, transaction);
}

DialogFilter.prototype.getProcessingInfo =function(){
    return null;
}
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
 *  Implementation of the JAIN-SIP ListeningPointImpl .
 *  @see  gov/nist/javax/sip/ListeningPointImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ListeningPointImpl() {
    this.classname="ListeningPointImpl";
    this.transport="ws";
    this.messageProcessor=null;
    this.sipProvider=null;
    if(arguments.length!=0)
    {
        var sipStack=arguments[0];
        this.sipStack=sipStack;
    }
}

ListeningPointImpl.prototype.makeKey =function(host,transport){
    var string="";
    string=(string+host+"/"+transport).toLowerCase();
    return string;
}

ListeningPointImpl.prototype.getKey =function(){
    return this.makeKey(this.sipStack.getHostAddress(), this.transport);
}

ListeningPointImpl.prototype.getUserAgent =function(){
    return this.sipStack.getUserAgent();
}

ListeningPointImpl.prototype.setSipProvider =function(sipProviderImpl){
    this.sipProvider = sipProviderImpl;
}

ListeningPointImpl.prototype.removeSipProvider =function(){
    this.sipProvider = null;
}

ListeningPointImpl.prototype.getURLWS =function(){
    return this.messageProcessor.getURLWS();
}

ListeningPointImpl.prototype.getTransport =function(){
    return this.messageProcessor.getTransport();
}

ListeningPointImpl.prototype.getProvider =function(){
    return this.sipProvider;
}

ListeningPointImpl.prototype.setSentBy =function(sentBy){
    this.messageProcessor.setSentBy(sentBy);
}

ListeningPointImpl.prototype.getSentBy =function(){
    return this.messageProcessor.getSentBy();
}

ListeningPointImpl.prototype.isSentBySet =function(){
    return this.messageProcessor.isSentBySet();
}

ListeningPointImpl.prototype.getViaHeader =function(){
    return this.messageProcessor.getViaHeader();
}

ListeningPointImpl.prototype.getMessageProcessor =function(){
    return this.messageProcessor;
}

ListeningPointImpl.prototype.getHost =function(){
    return this.hostname;
}

ListeningPointImpl.prototype.createContactHeader =function(userName){
    try {
        var hostname = this.sipStack.getHostAddress();
        var sipURI = new SipUri();
        sipURI.setHost_String(hostname);
        sipURI.setUser(userName);
        sipURI.setTransportParam(this.transport);
        var contact = new Contact();
        var address = new AddressImpl();
        address.setURI(sipURI);
        contact.setAddress(address);
        return contact;
    } catch (ex) {
        console.error("ListeningPointImpl:createContactHeader(): catched exception:"+ex);
        return null;
    }
}

ListeningPointImpl.prototype.sendHeartbeat =function(infoApp){
    var messageChannel = this.messageProcessor.createMessageChannel(infoApp);
    var siprequest = new SIPRequest();
    siprequest.setNullRequest();
    messageChannel.sendMessage(siprequest);
}

ListeningPointImpl.prototype.createViaHeader =function(){
    return this.getViaHeader();
}

ListeningPointImpl.prototype.getPort =function(){
    return this.messageProcessor.getPort();
}

ListeningPointImpl.prototype.getHostAddress =function(){
    return this.sipStack.getHostAddress();
}/*
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
 *  Implementation of the JAIN-SIP NistSipMessageFactoryImpl .
 *  @see  gov/nist/javax/sip/NistSipMessageFactoryImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function NistSipMessageFactoryImpl(sipStack) {
    this.classname="NistSipMessageFactoryImpl"; 
    this.sipStack=sipStack;
}

NistSipMessageFactoryImpl.prototype.newSIPServerRequest =function(sipRequest,messageChannel){
    if (messageChannel == null || sipRequest == null) {
        console.error("NistSipMessageFactoryImpl:newSIPServerRequest(): null Arg!");
        throw "NistSipMessageFactoryImpl:newSIPServerRequest(): null Arg!";
    }
    var theStack = messageChannel.getSIPStack();
    var retval = new DialogFilter(theStack);
    if (messageChannel instanceof SIPTransaction) {
        retval.transactionChannel = messageChannel;
    }
    retval.listeningPoint = messageChannel.getMessageProcessor().getListeningPoint();
    if (retval.listeningPoint == null)
    {
        return null;
    }
    return retval;
}

NistSipMessageFactoryImpl.prototype.newSIPServerResponse =function(sipResponse,messageChannel){
    var theStack = messageChannel.getSIPStack();
    var tr = theStack.findTransaction(sipResponse, false);
    if (tr != null) {
        if (tr.getState() == null) {
            return null;
        } 
        else if ("COMPLETED" == tr.getState()
            && sipResponse.getStatusCode() / 100 == 1) {
            return null;
        }
    }
    var retval = new DialogFilter(this.sipStack);
    retval.transactionChannel = tr;
    retval.listeningPoint = messageChannel.getMessageProcessor().getListeningPoint();
    return retval;
}/*
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
 *  Implementation of the JAIN-SIP SipProviderImpl .
 *  @see  gov/nist/javax/sip/SipProviderImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function SipProviderImpl() {
    this.classname="SipProviderImpl"; 
    this.sipListener=null;
    this.sipStack=null;
    this.listeningPoints=new Array();
    this.eventScanner=null;
    this.address=null;
    this.port=null;
    this.automaticDialogSupportEnabled=null; 
    this.dialogErrorsAutomaticallyHandled = true;
    if(arguments.length!=0)
    {
        var sipStack=arguments[0];
        this.sipStack=sipStack;
        this.eventScanner = sipStack.getEventScanner(); // for quick access.
        this.eventScanner.incrementRefcount();
        this.listeningPoints = new Array();
        this.automaticDialogSupportEnabled = this.sipStack.isAutomaticDialogSupportEnabledFunction();
        this.dialogErrorsAutomaticallyHandled = this.sipStack.isAutomaticDialogErrorHandlingEnabledFunction();
    }
}

SipProviderImpl.prototype.getListeningPoint =function(){
    if (this.listeningPoints.length > 0)
    {
        return this.listeningPoints[0][1];
    }
    else
    {
        return null;
    }
}

SipProviderImpl.prototype.isAutomaticDialogSupportEnabled =function(){
    return this.automaticDialogSupportEnabled;
}
SipProviderImpl.prototype.handleEvent =function(sipEvent,transaction){
    var eventWrapper = new EventWrapper(sipEvent, transaction);
    if (!this.sipStack.reEntrantListener) 
    {
        this.eventScanner.addEvent(eventWrapper);
    } 
    else 
    {
        this.eventScanner.deliverEvent(eventWrapper);
    }
}

SipProviderImpl.prototype.addSipListener =function(sipListener){
    if (this.sipStack.sipListener == null) {
        this.sipStack.sipListener = sipListener;
    }
    else if (this.sipStack.sipListener != sipListener) {
        console.error("SipProviderImpl:addSipListener(): stack already has a listener. Only one listener per stack allowed");
        throw "SipProviderImpl:addSipListener(): stack already has a listener. Only one listener per stack allowed";
    }
    this.sipListener = sipListener;
}

SipProviderImpl.prototype.setListeningPoint =function(listeningPoint){
    if (listeningPoint == null)
    {
        console.error("SipProviderImpl:setListeningPoint(): null listeningPoint argument");
        throw "SipProviderImpl:setListeningPoint(): null listeningPoint argument ";
    }
    var lp = listeningPoint;
    lp.sipProvider = this;
    var transport = lp.getTransport().toUpperCase();
    this.address = listeningPoint.getHostAddress();
    this.port = listeningPoint.getPort();
    this.listeningPoints=new Array();
    var array=new Array();
    array[0]=transport;
    array[1]=listeningPoint;
    this.listeningPoints.push(array);
}

SipProviderImpl.prototype.getSipListener =function(){
    return this.sipListener;
}

SipProviderImpl.prototype.stop =function(){
    for(var i=0;i<this.listeningPoints.length;i++)
    {
        var listeningPoint = this.listeningPoints[i][1];
        listeningPoint.removeSipProvider();
    }
    this.eventScanner.stop();
}

SipProviderImpl.prototype.getNewCallId =function(){
    var utils=new Utils();
    var callId = utils.generateCallIdentifier(this.getListeningPoint().getHostAddress());
    var callid = new CallID();
    callid.setCallId(callId);
    return callid;
}

SipProviderImpl.prototype.getNewClientTransaction =function(request){
    if (request == null)
    {
        console.error("SipProviderImpl:getNewClientTransaction(): null request argument");
        throw "SipProviderImpl:getNewClientTransaction(): null request argument ";
    }
    if (!this.sipStack.isAlive())
    {
        console.error("SipProviderImpl:getNewClientTransaction(): stack is stopped");
        throw "SipProviderImpl:getNewClientTransaction(): stack is stopped";
    }
    var sipRequest = request;
    if (sipRequest.getTransaction() != null)
    {
        console.error("SipProviderImpl:getNewClientTransaction(): transaction already assigned to request");
        throw "SipProviderImpl:getNewClientTransaction(): transaction already assigned to request";
    }
    if (sipRequest.getMethod()=="ACK") {
        console.error("SipProviderImpl:getNewClientTransaction(): cannot create client transaction for  ACK");
        throw "SipProviderImpl:getNewClientTransaction(): cannot create client transaction for  ACK";
    }
    if (sipRequest.getTopmostVia() == null) {
        var lp = this.getListeningPoint();//i ust tcp for the test. in the end we should change it to ws
        var via = lp.getViaHeader();
        request.setHeader(via);
    }
    try {
        sipRequest.checkHeaders();
    } catch (ex) {
        console.error("SipProviderImpl:getNewClientTransaction(): catched exception: "+ ex);
        throw "SipProviderImpl:getNewClientTransaction(): catched exception: "+ ex;
    }
    var bool=null;
    if(sipRequest.getTopmostVia().getBranch().substring(0, 7)=="z9hG4bK")
    {
        bool=true;
    }
    else
    {
        bool=false;
    }
    if (sipRequest.getTopmostVia().getBranch() != null
        && bool && this.sipStack.findTransaction(request, false) != null) {
        console.error("SipProviderImpl:getNewClientTransaction(): transaction already exists!");
        throw "SipProviderImpl:getNewClientTransaction(): transaction already exists!";
    }
    if (request.getMethod().toUpperCase()=="CANCEL") {
        var ct = this.sipStack.findCancelTransaction(request, false);
        if (ct != null) {
            var retval = this.sipStack.createClientTransaction(request, ct.getMessageChannel());
            retval.addEventListener(this);
            this.sipStack.addTransaction(retval);
            if (ct.getDialog() != null) {
                retval.setDialog(ct.getDialog(), sipRequest.getDialogId(false));
            }
            return retval;
        }
    }
    var hop = null;
    hop = this.sipStack.getNextHop(request);
    if (hop == null)
    {
        console.error("SipProviderImpl:getNewClientTransaction(): cannot resolve next hop -- transaction unavailable");
        throw "SipProviderImpl:getNewClientTransaction(): cannot resolve next hop -- transaction unavailable";
    }
    var transport = hop.getTransport();
    //var listeningPoint = this.getListeningPoint();
    var dialogId = sipRequest.getDialogId(false);
    var dialog = this.sipStack.getDialog(dialogId);
    if (dialog != null && dialog.getState() == "TERMINATED") {
        this.sipStack.removeDialog(dialog);
    }
    var branchId = null;
    bool=null;
    if(sipRequest.getTopmostVia().getBranch().substring(0, 7)!="z9hG4bK")
    {
        bool=true;
    }
    else
    {
        bool=false;
    }
    if (sipRequest.getTopmostVia().getBranch() == null
        || bool || this.sipStack.checkBranchIdFunction() ) {
        var utils=new Utils();
        branchId = utils.generateBranchId();
        sipRequest.getTopmostVia().setBranch(branchId);
    }
    var topmostVia = sipRequest.getTopmostVia();
    if(topmostVia.getTransport() == null)
    {
        topmostVia.setTransport(transport);
    }
    branchId = sipRequest.getTopmostVia().getBranch();
    ct = this.sipStack.createTransaction(sipRequest,this.sipStack.getChannel(),hop);
    if (ct == null)
    {
        console.error("SipProviderImpl:getNewClientTransaction(): cannot create transaction");
        throw "SipProviderImpl:getNewClientTransaction(): cannot create transaction";
    }
    ct.setNextHop(hop);
    ct.setOriginalRequest(sipRequest);
    ct.setBranch(branchId);
    if (this.sipStack.isDialogCreated(request.getMethod())) {
        if (dialog != null)
        {
            ct.setDialog(dialog, sipRequest.getDialogId(false));
        }
        else if (this.isAutomaticDialogSupportEnabled()) {
            var sipDialog = this.sipStack.createDialog(ct);
            ct.setDialog(sipDialog, sipRequest.getDialogId(false));
        }
    } 
    else {
        if (dialog != null) {
            ct.setDialog(dialog, sipRequest.getDialogId(false));
        }
    }
    ct.addEventListener(this);
    return ct;
}

SipProviderImpl.prototype.getNewServerTransaction =function(request){
    if (!this.sipStack.isAlive())
    {
        console.error("SipProviderImpl:getNewServerTransaction(): stack is stopped");
        throw "SipProviderImpl:getNewServerTransaction(): stack is stopped";
    }
    var transaction = null;
    var sipRequest = request;
    try {
        sipRequest.checkHeaders();
    } catch (ex) {
        console.error("SipProviderImpl:getNewServerTransaction(): catched exception:"+ex);
        throw "SipProviderImpl:getNewServerTransaction(): catched exception:"+ex;
    }
    if ( request.getMethod()=="ACK") {
        console.error("SipProviderImpl:getNewServerTransaction(): cannot create Server transaction for  ACK");
        throw "SipProviderImpl:getNewServerTransaction(): cannot Server client transaction for  ACK";
    }
    if (sipRequest.getMethod()=="NOTIFY"
        && sipRequest.getFromTag() != null && sipRequest.getToTag() == null) {
        var ct = this.sipStack.findSubscribeTransaction(sipRequest, this.getListeningPoint());
        if (ct == null && !this.sipStack.deliverUnsolicitedNotify) {
            console.error("SipProviderImpl:getNewServerTransaction(): cannot find matching Subscription (and gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY not set)");
            throw "SipProviderImpl:getNewServerTransaction(): cannot find matching Subscription (and gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY not set)"; 
        }
    }
    if (this.sipStack.isDialogCreated(sipRequest.getMethod())) {
        if (this.sipStack.findTransaction(request, true) != null)
        {
            console.error("SipProviderImpl:getNewServerTransaction(): server transaction already exists!");
            throw "SipProviderImpl:getNewServerTransaction(): server transaction already exists!)"; 
        }
        transaction =  request.getTransaction();
        if (transaction == null)
        {
            console.error("SipProviderImpl:getNewServerTransaction(): transaction not available");
            throw "SipProviderImpl:getNewServerTransaction(): transaction not available"; 
        }
        if (transaction.getOriginalRequest() == null)
        {
            transaction.setOriginalRequest(sipRequest);
        }
        try {
            this.sipStack.addTransaction(transaction);
        } catch (ex) {
            console.error("SipProviderImpl:getNewServerTransaction(): catched exception:"+ex);
            throw "SipProviderImpl:getNewServerTransaction(): catched exception:"+ex; 
        }
        transaction.addEventListener(this);
        if (this.isAutomaticDialogSupportEnabled()) {
            var dialogId = sipRequest.getDialogId(true);
            var dialog = this.sipStack.getDialog(dialogId);
            if (dialog == null) {
                dialog = this.sipStack.createDialog(transaction);
            }
            transaction.setDialog(dialog, sipRequest.getDialogId(true));
            if (sipRequest.getMethod()=="INVITE" && this.isDialogErrorsAutomaticallyHandled()) {
                this.sipStack.putInMergeTable(transaction, sipRequest);
            }
            if (dialog.getRemoteTag() != null && dialog.getLocalTag() != null) {
                this.sipStack.putDialog(dialog);
            }
            dialog.setInviteTransaction(transaction);
            dialog.setState(dialog.EARLY_STATE);
        }
    }
    else {
        if (this.isAutomaticDialogSupportEnabled()) {
            transaction = this.sipStack.findTransaction(request, true);
            if (transaction != null)
            {
                console.error("SipProviderImpl:getNewServerTransaction(): transaction exists!");
                throw "SipProviderImpl:getNewServerTransaction(): transaction exists!"; 
            }
            transaction = request.getTransaction();
            if (transaction == null)
            {
                console.error("SipProviderImpl:getNewServerTransaction(): transaction not available");
                throw "SipProviderImpl:getNewServerTransaction():transaction not available"; 
            }
            if (transaction.getOriginalRequest() == null)
            {
                transaction.setOriginalRequest(sipRequest);
            }
            try {
                this.sipStack.addTransaction(transaction);
            } catch (ex) {
                console.error("SipProviderImpl:getNewServerTransaction(): catched exception:"+ex);
                throw "SipProviderImpl:getNewServerTransaction():  catched exception:"+ex; 
            }
            dialogId = sipRequest.getDialogId(true);
            dialog = this.sipStack.getDialog(dialogId);
            if (dialog != null) {
                dialog.addTransaction(transaction);
                dialog.addRoute(sipRequest);
                transaction.setDialog(dialog, sipRequest.getDialogId(true));
            }
        } 
        else {
            transaction = this.sipStack.findTransaction(request, true);
            if (transaction != null)
            {
                console.error("SipProviderImpl:getNewServerTransaction(): transaction exists!");
                throw "SipProviderImpl:getNewServerTransaction(): transaction exists!"; 
            }
            transaction =  request.getTransaction();
            if (transaction != null) {
                if (transaction.getOriginalRequest() == null)
                    transaction.setOriginalRequest(sipRequest);
                this.sipStack.mapTransaction(transaction);
                dialogId = sipRequest.getDialogId(true);
                dialog = this.sipStack.getDialog(dialogId);
                if (dialog != null) {
                    dialog.addTransaction(transaction);
                    dialog.addRoute(sipRequest);
                    transaction.setDialog(dialog, sipRequest.getDialogId(true));
                }
                return transaction;
            } 
            else {
                var mc = sipRequest.getMessageChannel();
                transaction = this.sipStack.createServerTransaction(mc);
                if (transaction == null)
                {
                    console.error("SipProviderImpl:getNewServerTransaction(): transaction unavailable -- too many servrer transactions");
                    throw "SipProviderImpl:getNewServerTransaction(): transaction unavailable -- too many servrer transactions"; 
                }
                transaction.setOriginalRequest(sipRequest);
                this.sipStack.mapTransaction(transaction);
                dialogId = sipRequest.getDialogId(true);
                dialog = this.sipStack.getDialog(dialogId);
                if (dialog != null) {
                    dialog.addTransaction(transaction);
                    dialog.addRoute(sipRequest);
                    transaction.setDialog(dialog, sipRequest.getDialogId(true));
                }
                return transaction;
            }
        }
    } 
    return transaction;
}

SipProviderImpl.prototype.getSipStack =function(){
    return this.sipStack;
}

SipProviderImpl.prototype.removeSipListener =function(sipListener){
    if (sipListener == this.getSipListener()) {
        this.sipListener = null;
    }
    var found = false;
    var list=this.sipStack.getSipProviders();
    for(var i=0;i<list.length;i++)
    {
        var nextProvider = list[i];
        if (nextProvider.getSipListener() != null)
        {
            found = true;
        }
    }
    if (!found) {
        this.sipStack.sipListener = null;
    }
}

SipProviderImpl.prototype.sendRequest =function(request){
    if (!this.sipStack.isAlive())
    {
        console.error("SipProviderImpl:sendRequest(): stack is stopped");
        throw "SipProviderImpl:sendRequest(): stack is stopped";
    }
    var hop = this.sipStack.getRouter(request).getNextHop(request);
    if (hop == null)
    {
        console.error("SipProviderImpl:sendRequest(): could not determine next hop!");
        throw "SipProviderImpl:sendRequest():  could not determine next hop!";
    }
    var sipRequest = request;
    if ((!sipRequest.isNullRequest()) && sipRequest.getTopmostVia() == null)
    {
        console.error("SipProviderImpl:sendRequest(): invalid SipRequest -- no via header!");
        throw "SipProviderImpl:sendRequest(): invalid SipRequest -- no via header!";
    }
    if (!sipRequest.isNullRequest()) {
        var via = sipRequest.getTopmostVia();
        var branch = via.getBranch();
        if (branch == null || branch.length() == 0) {
            via.setBranch(sipRequest.getTransactionId());
        }
    }
    var messageChannel = null;
    var bool=null;
    for(var i=0;i<this.listeningPoints.length;i++)
    {
        if(hop.getTransport().toUpperCase()==this.listeningPoints[i][0])
        {
            bool=i;
        }
    }
    if(bool!=null)
    {
        bool=true;
    }
    else
    {
        bool=false;
    }
    if (bool)
    {
        messageChannel = this.sipStack.createRawMessageChannel();
    }
    if (messageChannel != null) {
        messageChannel.sendMessage(sipRequest);
    } else {
        console.error("SipProviderImpl:sendRequest(): could not create a message channel for "+ hop.toString());
        throw "SipProviderImpl:sendRequest(): could not create a message channel for "+ hop.toString();
    }
}

SipProviderImpl.prototype.sendResponse =function(response){
    if (!this.sipStack.isAlive())
    {
       console.error("SipProviderImpl:sendResponse(): stack is stopped");
       throw "SipProviderImpl:sendResponse(): stack is stopped";
    }
    var sipResponse = response;
    var via = sipResponse.getTopmostVia();
    if (via == null)
    {
        console.error("SipProviderImpl:sendResponse(): no via header in response!");
        throw "SipProviderImpl:sendResponse(): no via header in response!";
    }
    var st = this.sipStack.findTransaction(response, true);
    if ( st != null   && st.getState() != "TERMINATED" && this.isAutomaticDialogSupportEnabled()) {
        console.error("SipProviderImpl:sendResponse(): transaction exists -- cannot send response statelessly");
        throw "SipProviderImpl:sendResponse(): transaction exists -- cannot send response statelessly";
    }
    try {
        var listeningPoint = this.getListeningPoint();
        if (listeningPoint == null)
        {
           console.error("SipProviderImpl:sendResponse(): whoopsa daisy! no listening point found for transport "+transport);
           throw "SipProviderImpl:sendResponse(): whoopsa daisy! no listening point found for transport "+transport;
        }
        var messageChannel = this.sipStack.getChannel();
        messageChannel.sendMessage(sipResponse);
    } catch (ex) {
        console.error("SipProviderImpl:sendResponse(): catched exception: "+ ex);
        throw "SipProviderImpl:sendResponse(): catched exception:"+ ex;
    }
}

SipProviderImpl.prototype.getNewDialog =function(transaction){
    if (transaction == null)
    {
        console.error("SipProviderImpl:sendResponse(): null transaction!");
        throw "SipProviderImpl:sendResponse():  null transaction!";
    }

    if (!this.sipStack.isAlive())
    {
       console.error("SipProviderImpl:sendResponse(): stack is stopped");
       throw "SipProviderImpl:sendResponse(): stack is stopped";
    }

    if (this.isAutomaticDialogSupportEnabled())
    {
       console.error("SipProviderImpl:sendResponse(): error - AUTOMATIC_DIALOG_SUPPORT is on");
       throw "SipProviderImpl:sendResponse(): error - AUTOMATIC_DIALOG_SUPPORT is on";
    }

    if (!this.sipStack.isDialogCreated(transaction.getRequest().getMethod()))
    {
        console.error("SipProviderImpl:sendResponse(): dialog cannot be created for this method "+ transaction.getRequest().getMethod());
        throw "SipProviderImpl:sendResponse(): dialog cannot be created for this method "+ transaction.getRequest().getMethod();
    }
    
    var dialog = null;
    var sipTransaction = transaction;
    if (transaction instanceof SIPServerTransaction) {
        var st = transaction;
        var response = st.getLastResponse();
        if (response != null) {
            if (response.getStatusCode() != 100)
            {
                console.error("SipProviderImpl:sendResponse(): cannot set dialog after response has been sent");
                throw "SipProviderImpl:sendResponse(): cannot set dialog after response has been sent";
            }
        }
        var sipRequest = transaction.getRequest();
        var dialogId = sipRequest.getDialogId(true);
        dialog = this.sipStack.getDialog(dialogId);
        if (dialog == null) {
            dialog = this.sipStack.createDialog(transaction);
            dialog.addTransaction(sipTransaction);
            dialog.addRoute(sipRequest);
            sipTransaction.setDialog(dialog, null);
        } else {
            sipTransaction.setDialog(dialog, sipRequest.getDialogId(true));
        }
        if (sipRequest.getMethod()=="INVITE" && this.isDialogErrorsAutomaticallyHandled()) {
            this.sipStack.putInMergeTable(st, sipRequest);
        }
    }
    else {
        var sipClientTx = transaction;
        response = sipClientTx.getLastResponse();
        if (response == null) {
            var request = sipClientTx.getRequest();
            dialogId = request.getDialogId(false);
            dialog = this.sipStack.getDialog(dialogId);
            if (dialog != null) {
                console.error("SipProviderImpl:sendResponse(): dialog already exists!");
                throw "SipProviderImpl:sendResponse(): dialog already exists!";
            } 
            else {
                dialog = this.sipStack.createDialog(sipTransaction);
            }
            sipClientTx.setDialog(dialog, null);
        } 
        else {
            console.error("SipProviderImpl:sendResponse(): cannot call this method after response is received!");
            throw "SipProviderImpl:sendResponse(): cannot call this method after response is received!";
        }
    }
    dialog.addEventListener(this);
    return dialog;
}



SipProviderImpl.prototype.transactionErrorEvent =function(transactionErrorEvent){
    var transaction = transactionErrorEvent.getSource();
    if (transactionErrorEvent.getErrorID() == 2) {
        var errorObject = transactionErrorEvent.getSource();
        var timeout = "TRANSACTION";
        var ev = null;

        if (errorObject instanceof SIPServerTransaction) {
            ev = new TimeoutEvent(this, errorObject,timeout);
        } else {
            var clientTx = errorObject;
            var hop = clientTx.getNextHop();
            ev = new TimeoutEvent(this,errorObject,timeout);
        }
        this.handleEvent(ev,errorObject);
    }
    else if (transactionErrorEvent.getErrorID() == 1) {
        errorObject = transactionErrorEvent.getSource();
        timeout = "TRANSACTION";
        ev = null;
        if (errorObject instanceof SIPServerTransaction) {
            ev = new TimeoutEvent(this, errorObject, timeout);
        }
        else {
            clientTx = errorObject;
            hop = clientTx.getNextHop();
            ev = new TimeoutEvent(this,errorObject,timeout);
        }
        this.handleEvent(ev,errorObject);
    }
    else if (transactionErrorEvent.getErrorID() == 3) {
        errorObject = transactionErrorEvent.getSource();
        var tx = errorObject;
        timeout = Timeout.RETRANSMIT;
        ev = null;
        if (errorObject instanceof SIPServerTransaction) {
            ev = new TimeoutEvent(this, errorObject,timeout);
        } else {
            ev = new TimeoutEvent(this, errorObject, timeout);
        }
        this.handleEvent(ev, errorObject);
    }
}


SipProviderImpl.prototype.dialogErrorEvent =function(dialogErrorEvent){
    var sipDialog = dialogErrorEvent.getSource();
    var reason = "AckNotReceived";
    if (dialogErrorEvent.getErrorID() == 2) {
        reason= "AckNotSent";
    } 
    else if (dialogErrorEvent.getErrorID() == 3) {
        reason = "ReInviteTimeout";
    }
    var ev = new DialogTimeoutEvent(this, sipDialog, reason);
    this.handleEvent(ev, null);
}


SipProviderImpl.prototype.getListeningPoints =function(){
    var retval = new Array();
    for(var i=0;i<this.listeningPoints.length;i++)
    {
        retval[i]=this.listeningPoints[i][1];
    }
    return retval;
}

SipProviderImpl.prototype.addListeningPoint =function(listeningPoint){
    var lp = listeningPoint;
    if (lp.sipProvider != null && lp.sipProvider != this)
    {
        console.error("SipProviderImpl:addListeningPoint(): listening point assigned to another provider");
        throw "SipProviderImpl:addListeningPoint(): listening point assigned to another provider";
    }
    var transport = lp.getTransport().toUpperCase();
    if (this.listeningPoints.length==0) {
        this.address = listeningPoint.getHostAddress();
        this.port = listeningPoint.getPort();
    } 
    else {
        if ((this.address!=listeningPoint.getHostAddress())
            || this.port != listeningPoint.getPort())
            {
            console.error("SipProviderImpl:addListeningPoint(): provider already has different IP Address associated");
            throw "SipProviderImpl:addListeningPoint(): provider already has different IP Address associated";          
        }
    }
    var bool=null;
    var l=null
    for(var i=0;i<this.listeningPoints.length;i++)
    {
        if(transport==this.listeningPoints[i][0])
        {
            bool=i;
            l=i;
        }
    }
    if(bool!=null)
    {
        bool=true;
    }
    else
    {
        bool=false;
    }
    if(l!=null)
    {
        var value=this.listeningPoints[l][1]
    }
    if (bool && value != listeningPoint)
    {
        console.error("SipProviderImpl:addListeningPoint(): listening point already assigned for transport!");
        throw "SipProviderImpl:addListeningPoint(): listening point already assigned for transport!";       
    }
    lp.sipProvider = this;
    var array=new Array();
    array[0]=transport;
    array[1]=lp;
    this.listeningPoints.push(array);
}


SipProviderImpl.prototype.removeListeningPoint =function(listeningPoint){
    var lp = listeningPoint;
    /*if (lp.messageProcessor.inUse())
    {
        console.error("Object is in use");
    }*/
    var l=null;
    for(var i=0;i<this.listeningPoints.length;i++)
    {
        if(lp.getTransport().toUpperCase()==this.listeningPoints[i][0])
        {
            l=i;
        }
    }
    this.listeningPoints.splice(l,1);
}


SipProviderImpl.prototype.removeListeningPoints =function(){
    for(var i=0;i<this.listeningPoints.length;i++)
    {
        var lp = this.listeningPoints[i][1];
        lp.messageProcessor.stop();
    }
    this.listeningPoints=new Array();
}


SipProviderImpl.prototype.setAutomaticDialogSupportEnabled =function(automaticDialogSupportEnabled){
    this.automaticDialogSupportEnabled = automaticDialogSupportEnabled;
    if ( this.automaticDialogSupportEnabled ) {
        this.dialogErrorsAutomaticallyHandled = true;
    }
}


SipProviderImpl.prototype.setDialogErrorsAutomaticallyHandled =function(){
    this.dialogErrorsAutomaticallyHandled = true;
}

SipProviderImpl.prototype.isDialogErrorsAutomaticallyHandled =function(){
    return this.dialogErrorsAutomaticallyHandled;
}
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
 *  Implementation of the JAIN-SIP SipStackImpl .
 *  @see  gov/nist/javax/sip/SipStackImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function SipStackImpl() {
    this.classname="SipStackImpl"; 
    this.stackName=null;
    this.serverTransactionTable=new Array();
    this.clientTransactionTable=new Array();
    this.dialogCreatingMethods=new Array();
    this.earlyDialogTable=new Array();
    this.isBackToBackUserAgent = false;
    this.eventScanner=new EventScanner(this);
    this.listeningPoints=new Array();
    this.sipProviders=new Array();
    this.sipListener=null;
    this.setNon2XXAckPassedToListener(false);
    this.isAutomaticDialogSupportEnabled = true;
    this.isAutomaticDialogErrorHandlingEnabled = true;
    this.messageChannel=null;
    this.userAgentName=null;
    this.lastTransaction=null;
    this.reEntrantListener=true;
    
    this.dialogCreatingMethods.push("REFER");
    this.dialogCreatingMethods.push("INVITE");
    this.dialogCreatingMethods.push("SUBSCRIBE");
    
    if(arguments.length!=0)
    {
        this.wsurl=arguments[0];
        var utils=new Utils();
        this.setHostAddress(utils.randomString(12)+".invalid");       
        this.userAgentName=arguments[1];     
        this.sipMessageFactory = new NistSipMessageFactoryImpl(this);      
        this.defaultRouter = new DefaultRouter(this, this.stackAddress);
    }
}

SipStackImpl.prototype = new SIPTransactionStack();
SipStackImpl.prototype.constructor=SipStackImpl;
SipStackImpl.prototype.MAX_DATAGRAM_SIZE=8 * 1024;

SipStackImpl.prototype.isAutomaticDialogSupportEnabledFunction =function(){
    return this.isAutomaticDialogSupportEnabled;
}

SipStackImpl.prototype.isAutomaticDialogErrorHandlingEnabledFunction =function(){
    return this.isAutomaticDialogErrorHandlingEnabled;
}

SipStackImpl.prototype.getEventScanner =function(){
    return this.eventScanner;
}

SipStackImpl.prototype.getSipListener =function(){
    return this.sipListener;
}

SipStackImpl.prototype.createSipProvider =function(listeningPoint){
    if (listeningPoint == null) {
        console.error("SipProviderImpl:createSipProvider(): null listeningPoint argument");
        throw "SipProviderImpl:createSipProvider(): null listeningPoint argument";
    }
    
    var listeningPointImpl = listeningPoint;
    if (listeningPointImpl.sipProvider != null) {
        console.error("SipProviderImpl:createSipProvider(): provider already attached!");
        throw "SipProviderImpl:createSipProvider(): provider already attached!";
    }
    
    var provider = new SipProviderImpl(this);
    provider.setListeningPoint(listeningPointImpl);
    listeningPointImpl.sipProvider = provider;
    var l=null;
    for(var i=0;i<this.sipProviders.length;i++)
    {
        if(this.sipProviders[i]==provider)
        {
            l=i;
        }
    }
    if(l==null)
    {
        this.sipProviders.push(provider);
    }
    return provider;
}

SipStackImpl.prototype.createListeningPoint =function(){
    if (!this.isAlive()) {
        this.toExit = false;
        this.reInitialize();
    }
    var transport="ws";
    var lp=new ListeningPointImpl(this);
    lp.host=this.stackAddress;
    var key = lp.makeKey(this.stackAddress, transport);
    var lip=null;
    for(var i=0;i<this.listeningPoints.length;i++)
    {
        if(this.listeningPoints[i]==key)
        {
            lip=this.listeningPoints[i][1];
        }
    }
    if (lip != null) {
        return lip;
    }
    else {
        var messageProcessor = this.createMessageProcessor();
        lip = new ListeningPointImpl(this);
        lip.messageProcessor = messageProcessor;
        messageProcessor.setListeningPoint(lip);
        var array=new Array();
        array[0]=key;
        array[1]=lip;
        this.listeningPoints.push(array);
        this.messageChannel=messageProcessor.createMessageChannel();
        return lip;
    }
}

SipStackImpl.prototype.createMessageProcessor =function(){
    var wsMessageProcessor = new WSMessageProcessor(this);
    this.addMessageProcessor(wsMessageProcessor);
    return wsMessageProcessor;
}

SipStackImpl.prototype.reInitialize =function(){
    this.reInit();
    this.eventScanner = new EventScanner(this);
    this.listeningPoints = new Array();
    this.sipProviders = new Array();
    this.sipListener = null;
}

SipStackImpl.prototype.getUrlWs =function(){
    return this.wsurl;
}

SipStackImpl.prototype.getUserAgent =function(){
    return this.userAgentName;
}


SipStackImpl.prototype.deleteListeningPoint =function(listeningPoint){
    if (listeningPoint == null) {
        console.error("SipProviderImpl:deleteListeningPoint(): null listeningPoint arg");
        throw "SipProviderImpl:deleteListeningPoint(): null listeningPoint arg";
    }
    var lip = listeningPoint;
    this.removeMessageProcessor(lip.messageProcessor);
    var key = lip.getKey();
    var l=null;
    for(var i=0;i<this.listeningPoints.length;i++)
    {
        if(this.listeningPoints[i][0]==key)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        this.listeningPoints.splice(l,1);
    }
}


SipStackImpl.prototype.deleteSipProvider =function(sipProvider){
    if (sipProvider == null) {
        console.error("SipProviderImpl:deleteSipProvider(): null provider arg");
        throw "SipProviderImpl:deleteSipProvider(): null provider arg";
    }
    var sipProviderImpl = sipProvider;
    if (sipProviderImpl.getSipListener() != null) {
        console.error("SipProviderImpl:deleteSipProvider(): sipProvider still has an associated SipListener!");
        throw "SipProviderImpl:deleteSipProvider(): sipProvider still has an associated SipListener!";
    }
    sipProviderImpl.removeListeningPoints();
    sipProviderImpl.stop();
    var l=null;
    for(var i=0;i<this.sipProviders.length;i++)
    {
        if(this.sipProviders[i]==sipProvider)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        this.sipProviders.splice(l,1);
    }
    if (this.sipProviders.length==0) {
        this.stopStack();
    }
}

SipStackImpl.prototype.getListeningPoints =function(){
    return this.listeningPoints;
}

SipStackImpl.prototype.getSipProviders =function(){
    if(this.sipProviders.length==1)
    {
        return this.sipProviders[0];
    }
    else
    {
        return this.sipProviders;
    }
}

SipStackImpl.prototype.getStackName =function(){
    return this.stackName;
}

SipStackImpl.prototype.finalize =function(){
    this.stopStack();
}

SipStackImpl.prototype.stop =function(){
    this.stopStack();
    this.sipProviders = new Array();
    this.listeningPoints = new Array();
    if (this.eventScanner != null) {
        this.eventScanner.forceStop();
    }
    this.eventScanner = null;
}

SipStackImpl.prototype.start =function(){
    if (this.eventScanner == null) {
        this.eventScanner = new EventScanner(this);
    }
}

SipStackImpl.prototype.getSipListener =function(){
    return this.sipListener;
}

SipStackImpl.prototype.setEnabledCipherSuites =function(newCipherSuites){
    this.cipherSuites = newCipherSuites;
}

SipStackImpl.prototype.getEnabledCipherSuites =function(){
    return this.cipherSuites;
}

SipStackImpl.prototype.setEnabledProtocols =function(newProtocols){
    this.enabledProtocols = newProtocols;
}

SipStackImpl.prototype.getEnabledProtocols =function(){
    return this.enabledProtocols;
}

SipStackImpl.prototype.setIsBackToBackUserAgent =function(flag){
    this.isBackToBackUserAgent = flag;
}

SipStackImpl.prototype.isBackToBackUserAgent =function(){
    return this.isBackToBackUserAgent;
}

SipStackImpl.prototype.isAutomaticDialogErrorHandlingEnabled =function(){
    return this.isAutomaticDialogErrorHandlingEnabled;
}

SipStackImpl.prototype.getChannel =function(){
    return this.messageChannel;
}

SipStackImpl.prototype.newSIPServerRequest =function(requestReceived,requestMessageChannel){
    var nextTransaction=null;
    var currentTransaction=null;
    var key = requestReceived.getTransactionId();
    requestReceived.setMessageChannel(requestMessageChannel); 
    var l=null;
    for(var i=0;i<this.serverTransactionTable.length;i++)
    {
        if(this.serverTransactionTable[i][0]==key)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        currentTransaction=this.serverTransactionTable[l][1]; 
    }
    if (currentTransaction == null|| !currentTransaction.isMessagePartOfTransaction(requestReceived)) {
        currentTransaction = null;
        var length=this.BRANCH_MAGIC_COOKIE_LOWER_CASE.length;
        var chaine=key.toLowerCase().substr(0, length-1);
        if (chaine!=this.BRANCH_MAGIC_COOKIE_LOWER_CASE) {
            for(i=0;i<this.serverTransactionTable.length&& currentTransaction == null;i++)
            {
                nextTransaction=this.serverTransactionTable[i][1];
                if (nextTransaction.isMessagePartOfTransaction(requestReceived)) {
                    currentTransaction = nextTransaction;
                }
            }
        }
        if (currentTransaction == null) {
            currentTransaction = this.findPendingTransaction(requestReceived);
            if (currentTransaction != null) {
                requestReceived.setTransaction(currentTransaction);
                if (currentTransaction != null) {
                    return currentTransaction;
                } else {
                    return null;
                }

            }
            currentTransaction = this.createServerTransaction(requestMessageChannel);
            currentTransaction.setOriginalRequest(requestReceived);
            requestReceived.setTransaction(currentTransaction);
            if(requestReceived.getMethod()!="ACK")
            {
                currentTransaction=this.getSipProviders().getNewServerTransaction(requestReceived);
            }
        }
    }
    if (currentTransaction != null) {
        currentTransaction.setRequestInterface(this.sipMessageFactory.newSIPServerRequest(
            requestReceived, currentTransaction));
    }
    if(requestReceived.getMethod()=="ACK")
    {
        currentTransaction=this.lastTransaction;
    }
    else
    {
        this.lastTransaction=currentTransaction;
    }
    return currentTransaction;
}


SipStackImpl.prototype.newSIPServerResponse =function(responseReceived,responseMessageChannel){
    var nextTransaction=null;
    var currentTransaction=null;
    var key = responseReceived.getTransactionId();
    var l=null;
    for(var i=0;i<this.clientTransactionTable.length;i++)
    {
        if(this.clientTransactionTable[i][0]==key)
        {
            l=i;
        }
    }
    if(l!=null)
    {
        currentTransaction=this.clientTransactionTable[l][1];
    }
    var length=this.BRANCH_MAGIC_COOKIE_LOWER_CASE.length;
    var chaine=key.toLowerCase().substr(0, length-1);
    if (currentTransaction == null
        || (!currentTransaction.isMessagePartOfTransaction(responseReceived) 
            && chaine!=this.BRANCH_MAGIC_COOKIE_LOWER_CASE)) {
        for(i=0;i<this.clientTransactionTable.length&& currentTransaction == null;i++)
        {
            nextTransaction=this.serverTransactionTable[i][1];
            if (nextTransaction.isMessagePartOfTransaction(responseReceived)) {
                currentTransaction = nextTransaction;
            }
        }
        if (currentTransaction == null) {
            return this.sipMessageFactory.newSIPServerResponse(responseReceived,
                responseMessageChannel);
        }
    }
    var sri = this.sipMessageFactory.newSIPServerResponse(responseReceived, currentTransaction);
    if (sri != null) {
        currentTransaction.setResponseInterface(sri);
    }
    else {
        return null;
    }
    return currentTransaction;
}

SipStackImpl.prototype.createServerTransaction =function(encapsulatedMessageChannel){
    return new SIPServerTransaction(this, encapsulatedMessageChannel);
}

SipStackImpl.prototype.removeTransaction =function(sipTransaction){
    if (sipTransaction instanceof SIPServerTransaction) {
        var key = sipTransaction.getTransactionId();
        var removed=null;
        var l=null;
        for(var i=0;i<this.serverTransactionTable.length;i++)
        {
            if(key==this.serverTransactionTable[i])
            {
                l=i
                removed=this.serverTransactionTable[i][1];
            }
        }
        this.serverTransactionTable.splice(l,1);
        var method = sipTransaction.getMethod();
        this.removePendingTransaction(sipTransaction);
        this.removeTransactionPendingAck(sipTransaction);
        if (method.toUpperCase()=="INVITE") {
            this.removeFromMergeTable(sipTransaction);
        }
        var sipProvider = sipTransaction.getSipProvider();
        if (removed != null && sipTransaction.testAndSetTransactionTerminatedEvent()) {
            var event = new TransactionTerminatedEvent(sipProvider,sipTransaction);
            sipProvider.handleEvent(event, sipTransaction);
        }
    } 
    else {
        key = sipTransaction.getTransactionId();
        var l=null;
        for(var i=0;i<this.clientTransactionTable.length;i++)
        {
            if(this.clientTransactionTable[i][0]==key)
            {
                l=i;
            }
        }
        if(l!=null)
        {
            removed = this.clientTransactionTable[l][1];
            this.clientTransactionTable.splice(l,1);
        }
        if (removed != null && sipTransaction.testAndSetTransactionTerminatedEvent()) {
            sipProvider = sipTransaction.getSipProvider();
            event = new TransactionTerminatedEvent(sipProvider,sipTransaction);
            sipProvider.handleEvent(event, sipTransaction);
        }
    }
}/*
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
 *  Implementation of the JAIN-SIP SipListener .
 *  @see  gov/nist/javax/sip/SipListener.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function SipListener() {
}

SipListener.prototype.processDialogTerminated =function(dialogTerminatedEvent){
}

SipListener.prototype.processIOException =function(exceptionEvent){ 
}

SipListener.prototype.processRequest =function(requestEvent){ 
}

SipListener.prototype.processResponse =function(responseEvent){
}

SipListener.prototype.processTimeout =function(timeoutEvent){
}

SipListener.prototype.processTransactionTerminated =function(transactionTerminatedEvent){
}

SipListener.prototype.processConnected =function(){   
}

SipListener.prototype.processDisconnected =function(){   
}

SipListener.prototype.processConnectionError =function(){   
}/*
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
 *  Implementation of the JAIN-SIP SipFactory .
 *  @see  gov/nist/javax/sip/SipFactory.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function SipFactory() {
    this.classname="SipFactory"; 
    this.sipFactory=null;
    this.mNameSipStackMap=new Array();
}

SipFactory.prototype.getInstance =function(){
    if (this.sipFactory == null) 
    {
        this.sipFactory = new SipFactory();
    }
    return this.sipFactory;
}

SipFactory.prototype.resetFactory =function(){
    this.mNameSipStackMap=new Array();
}

SipFactory.prototype.createSipStack =function(wsUrl,sipUserAgentName){

    var sipStack = null;
    for(var i=0;i<this.mNameSipStackMap.length;i++)
    {
        if(this.mNameSipStackMap[i][0]==wsUrl)
        {
            sipStack=this.mNameSipStackMap[i][1]
        }
    }
    if (sipStack == null) {
        var array=new Array();
        sipStack=new SipStackImpl(wsUrl,sipUserAgentName);
        array[0]=wsUrl;
        array[1]=sipStack;
        this.mNameSipStackMap.push(array);
    }
    return sipStack;
}


SipFactory.prototype.createAddressFactory =function(){
    try {
        var afi=new AddressFactoryImpl();
        return afi;
    } catch (ex) {
        console.error("SipFactory:createAddressFactory(): failed to create AddressFactory");
        throw "SipFactory:createAddressFactory(): failed to create AddressFactory";
    }
}
SipFactory.prototype.createHeaderFactory =function(){
    try {
        var hfi=new HeaderFactoryImpl();
        return hfi;
    } catch (ex) {
        console.error("SipFactory:createHeaderFactory(): failed to create HeaderFactory");
        throw "SipFactory:createHeaderFactory(): failed to create HeaderFactory";
    }
}
SipFactory.prototype.createMessageFactory =function(listeningpoint){
    try {
        var mfi=new MessageFactoryImpl(listeningpoint);
        return mfi;
    } catch (ex) {
        console.error("SipFactory:createMessageFactory(): failed to create MessageFactory");
        throw "SipFactory:createMessageFactory():failed to create MessageFactory";
    }
}
