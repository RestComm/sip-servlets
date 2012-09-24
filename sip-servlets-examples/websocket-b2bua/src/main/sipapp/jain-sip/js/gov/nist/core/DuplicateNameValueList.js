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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:DuplicateNameValueList()");
    this.serialVersionUID = "-5611332957903796952L";
    this.classname="DuplicateNameValueList";
    this.nameValueMap = new Array();
    this.separator=";";
}

DuplicateNameValueList.prototype.setSeparator =function(separator){
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:setSeparator():separator="+separator);
    this.separator=separator;
}

DuplicateNameValueList.prototype.encode =function(){
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:encode()");
    return this.encodeBuffer("").toString();
}

DuplicateNameValueList.prototype.encodeBuffer =function(buffer){
   // if(logger!=undefined) logger.debug("DuplicateNameValueList:encodeBuffer():buffer="+buffer);
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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:toString()");
    return this.encode();
}

DuplicateNameValueList.prototype.set_nv =function(nv){
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:set_nv():nv="+nv);
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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:set_name_value():name="+name+", value="+value);
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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:equals():otherObject="+otherObject);
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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:getValue():name="+name);
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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:getNameValue():name="+name);
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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:hasNameValue():name="+name);
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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:delet():name="+name);
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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:iterator()");
    return this.nameValueMap;
}


DuplicateNameValueList.prototype.getNames =function(){
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:getNames()");
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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:getParameter():name="+name);
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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:clear()");
    this.nameValueMap=new Array();
}

DuplicateNameValueList.prototype.isEmpty =function(){
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:isEmpty()");
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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:put():key="+key+",value="+value);
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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:remove():key="+key);
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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:size()");
    var size=0;
    for(var i=0;i<this.nameValueMap.length;i++)
    {
        size=size+this.nameValueMap[i][1].length;
    }
    return size;
}

DuplicateNameValueList.prototype.values =function(){//return a series lists of values.
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:values()");
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
    //if(logger!=undefined) logger.debug("DuplicateNameValueList:hashCode()");
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

