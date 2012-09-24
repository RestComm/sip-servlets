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
    //if(logger!=undefined) logger.debug("NameValueList");
    this.classname="NameValueList"; 
    this.serialVersionUID = "-6998271876574260243L";
    this.hmap = new Array();
    this.separator=";";

}

NameValueList.prototype.setSeparator =function(separator){
    //if(logger!=undefined) logger.debug("NameValueList:setSeparator():separator="+separator);
    this.separator=separator;
}

NameValueList.prototype.encode =function(){
    //if(logger!=undefined) logger.debug("NameValueList:encode()");
    return this.encodeBuffer("").toString();
}

NameValueList.prototype.encodeBuffer =function(buffer){
    //if(logger!=undefined) logger.debug("NameValueList:encodeBuffer():buffer="+buffer);
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
    //if(logger!=undefined) logger.debug("NameValueList:toString()");
    return this.encode();
}

NameValueList.prototype.set_nv =function(nv){
    //if(logger!=undefined) logger.debug("NameValueList:set_nv():nv="+nv);
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
    //if(logger!=undefined) logger.debug("NameValueList:set_name_value():name="+name+", value="+value);
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
    //if(logger!=undefined) logger.debug("NameValueList:equals():otherObject="+otherObject);
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
    //if(logger!=undefined) logger.debug("NameValueList:getValue():name="+name);
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
    //if(logger!=undefined) logger.debug("NameValueList:getNameValue():name="+name);
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
    //if(logger!=undefined) logger.debug("NameValueList:hasNameValue():name="+name);
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
    //if(logger!=undefined) logger.debug("NameValueList:delet():name="+name);
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
    //if(logger!=undefined) logger.debug("NameValueList:size()");
    return this.hmap.length;
}

NameValueList.prototype.isEmpty=function(){
    //if(logger!=undefined) logger.debug("NameValueList:isEmpty()");
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
    //if(logger!=undefined) logger.debug("NameValueList:iterator()");
    return this.hmap;//here, i consider that we can use array to replace the itertor
}

NameValueList.prototype.getNames=function(){
    //if(logger!=undefined) logger.debug("NameValueList:getNames()");
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
    //if(logger!=undefined) logger.debug("NameValueList:getParameter():name="+name);
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
    //if(logger!=undefined) logger.debug("NameValueList:clear()");
    this.hmap=null;
}

NameValueList.prototype.containsKey=function(key){
    //if(logger!=undefined) logger.debug("NameValueList:containsKey():key="+key);
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
    //if(logger!=undefined) logger.debug("NameValueList:containsValue():value="+value);
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
    //if(logger!=undefined) logger.debug("NameValueList:entrySet()");
    return this.hmap;
}

NameValueList.prototype.get=function(key){
    //if(logger!=undefined) logger.debug("NameValueList:get():key="+key);
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
    //if(logger!=undefined) logger.debug("NameValueList:keySet()");
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
    //if(logger!=undefined) logger.debug("NameValueList:put()name="+name+",nameValue="+nameValue);
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
    //if(logger!=undefined) logger.debug("NameValueList:putAll():map="+map);
    for(var i=0;i<map.length;i++)// loop for method put() of hashtable
    {
        this.put(map[i][0], map[i][1]);
    }
}

NameValueList.prototype.remove=function(key){
    //if(logger!=undefined) logger.debug("NameValueList:remove():key="+key);
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
    //if(logger!=undefined) logger.debug("NameValueList:values()");
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
    //if(logger!=undefined) logger.debug("NameValueList:hashCode()");
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
    
    
    