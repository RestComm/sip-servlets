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
 *  Implementation of the JAIN-SIP ListMap .
 *  @see  gov/nist/javax/sip/message/ListMap.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function ListMap() {
    if(logger!=undefined) logger.debug("ListMap:ListMap()");
    this.classname="ListMap";
    this.headerListTable=new Array();
    this.initialized=null;
    this.initializeListMap();
}

ListMap.prototype.put =function(hashtable, class1, class2){
    if(logger!=undefined) logger.debug("ListMap:put():class1="+class1+", class2:"+class2);
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
    if(logger!=undefined) logger.debug("ListMap:initializeListMap()");
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
    if(logger!=undefined) logger.debug("ListMap:hasList():sipHeader:"+arguments[0]);
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
    if(logger!=undefined) logger.debug("ListMap:getListClass():sipHdrClass="+sipHdrClass);
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
    if(logger!=undefined) logger.debug("ListMap:getList():sipHeader:"+sipHeader.classname);
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
