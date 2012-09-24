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
    if(logger!=undefined) logger.debug("SIPHeaderList:SIPHeaderList()");
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
    if(logger!=undefined) logger.debug("SIPHeaderList:getName()");
    return this.headerName;
}

SIPHeaderList.prototype.add =function(){
    if(logger!=undefined) logger.debug("SIPHeaderList:add()");
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
    if(logger!=undefined) logger.debug("SIPHeaderList:addFirst():obj="+obj);
    this.hlist.unshift(obj); 
}

SIPHeaderList.prototype.concatenate =function(other, topFlag){
    if(logger!=undefined) logger.debug("SIPHeaderList:concatenate():other="+other+",topFlag="+topFlag);
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
    if(logger!=undefined) logger.debug("SIPHeaderList:encode()");
    return this.encodeBuffer("").toString();
}

SIPHeaderList.prototype.encodeBuffer =function(buffer){
    if(logger!=undefined) logger.debug("SIPHeaderList:encodeBuffer():buffer="+buffer);
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
    if(logger!=undefined) logger.debug("SIPHeaderList:getHeadersAsEncodedStrings()");
    var retval = new Array();
    for(var i=0;i<this.hlist.length;i++)
    {
        var sipheader = this.hlist[i];
        retval[i]=sipheader.toString();
    }
    return retval;
}

SIPHeaderList.prototype.getFirst =function(){
    if(logger!=undefined) logger.debug("SIPHeaderList:getFirst()");
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
    if(logger!=undefined) logger.debug("SIPHeaderList:getLast()");
    if (this.hlist == null || this.hlist.length==0)
    {
        return null;
    }
    var length=this.hlist.length;
    return this.hlist[length-1];
}

SIPHeaderList.prototype.getMyClass =function(){
    if(logger!=undefined) logger.debug("SIPHeaderList:getMyClass()");
    return  this.myClass;
/*here it return the name of the class not an object. the reason
     *i have explained on the top
     **/
}

SIPHeaderList.prototype.isEmpty =function(){
    if(logger!=undefined) logger.debug("SIPHeaderList:isEmpty()");
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
    if(logger!=undefined) logger.debug("SIPHeaderList:listIterator()");
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
    if(logger!=undefined) logger.debug("SIPHeaderList:getHeaderList()");
    return this.hlist;
}

SIPHeaderList.prototype.removeFirst =function(){
    if(logger!=undefined) logger.debug("SIPHeaderList:removeFirst()");
    if (this.hlist.length != 0)
    {
        this.hlist.splice(0,1);
    }
}

SIPHeaderList.prototype.removeLast =function(){
    if(logger!=undefined) logger.debug("SIPHeaderList:removeLast()");
    if (this.hlist.length != 0)
    {
        var length=this.hlist.length-1;
        this.hlist.splice(length,1);
    }
}

SIPHeaderList.prototype.remove =function(obj){
    if(logger!=undefined) logger.debug("SIPHeaderList:remove():obj="+obj);
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
    if(logger!=undefined) logger.debug("SIPHeaderList:setMyClass():cl="+cl);
    this.myClass = cl;
}

SIPHeaderList.prototype.toArray =function(){
    if(logger!=undefined) logger.debug("SIPHeaderList:toArray()");
    return this.hlist;
}
SIPHeaderList.prototype.indexOf =function(gobj){
    if(logger!=undefined) logger.debug("SIPHeaderList:indexOf():gobj="+gobj);
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
    if(logger!=undefined) logger.debug("SIPHeaderList:size()");
    return this.hlist.length;
}

SIPHeaderList.prototype.isHeaderList =function(){
    if(logger!=undefined) logger.debug("SIPHeaderList:isHeaderList()");
    return true;
}

SIPHeaderList.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("SIPHeaderList:encodeBody()");
    return this.encodeBodyBuffer("").toString();
}

SIPHeaderList.prototype.encodeBodyBuffer =function(buffer){
    if(logger!=undefined) logger.debug("SIPHeaderList:encodeBodyBuffer():buffer="+buffer);
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
    if(logger!=undefined) logger.debug("SIPHeaderList:addAll():arguments="+arguments.toString());
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
    if(logger!=undefined) logger.debug("SIPHeaderList:containsAll():collection="+collection);
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
    if(logger!=undefined) logger.debug("SIPHeaderList:clear()");
    this.hlist=new Array();
}

SIPHeaderList.prototype.contains =function(header){
    if(logger!=undefined) logger.debug("SIPHeaderList:contains():header="+header);
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
    if(logger!=undefined) logger.debug("SIPHeaderList:get():index="+index);
    return this.hlist[index];
}

SIPHeaderList.prototype.iterator =function(){
    if(logger!=undefined) logger.debug("SIPHeaderList:iterator()");
    return this.hlist;
}

SIPHeaderList.prototype.lastIndexOf =function(obj){
    if(logger!=undefined) logger.debug("SIPHeaderList:lastIndexOf():obj="+obj);
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
    if(logger!=undefined) logger.debug("SIPHeaderList:removeAll():collection="+collection);
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
    if(logger!=undefined) logger.debug("SIPHeaderList:retainAll():collection="+collection);
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
    if(logger!=undefined) logger.debug("SIPHeaderList:subList():index1="+index1+", index2:"+index2);
    return this.hlist.slice(index1,index2);
}

SIPHeaderList.prototype.hashCode =function(){
    if(logger!=undefined) logger.debug("SIPHeaderList:hashCode()");
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
    if(logger!=undefined) logger.debug("SIPHeaderList:set():position="+position+", sipHeader:"+sipHeader);
    var x=this.hlist[position];
    this.hlist[position]=sipHeader;
    return x;
}
SIPHeaderList.prototype.setPrettyEncode =function(flag){
    if(logger!=undefined) logger.debug("SIPHeaderList:setPrettyEncode():flag="+flag);
    this.prettyEncode=flag;
}