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
    if(logger!=undefined) logger.debug("SipUri");
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
    if(logger!=undefined) logger.debug("SipUri:setScheme():scheme="+scheme);
    if (scheme.toLowerCase()!= this.SIP
        &&scheme.toLowerCase()!=  this.SIPS)
        {
        console.error("SipUri:setScheme(): bad scheme " + scheme);
        throw "SipUri:setScheme(): bad scheme " + scheme;
    }
    this.scheme = scheme.toLowerCase();
}

SipUri.prototype.getScheme =function(){
    if(logger!=undefined) logger.debug("SipUri:getScheme()");
    return this.scheme;
}

SipUri.prototype.clearUriParms =function(){
    if(logger!=undefined) logger.debug("SipUri:clearUriParms()");
    this.uriParms = new NameValueList();
}

SipUri.prototype.clearPassword =function(){
    if(logger!=undefined) logger.debug("SipUri:clearPassword()");
    if (this.authority != null) {
        var userInfo = this.authority.getUserInfo();
        if (userInfo != null)
        {
            userInfo.clearPassword();
        }
    }
}
SipUri.prototype.getAuthority =function(){
    if(logger!=undefined) logger.debug("SipUri:getAuthority()");
    return this.authority;
}

SipUri.prototype.clearQheaders =function(){
    if(logger!=undefined) logger.debug("SipUri:clearQheaders()");
    this.qheaders = new NameValueList();
}

SipUri.prototype.equals =function(that){
    if(logger!=undefined) logger.debug("SipUri:equals():that="+that);
    
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
    if(logger!=undefined) logger.debug("SipUri:encode()");
    return this.encodeBuffer("");
}

SipUri.prototype.encodeBuffer =function(buffer){
    if(logger!=undefined) logger.debug("SipUri:encodeBuffer():buffer="+buffer);
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
    if(logger!=undefined) logger.debug("SipUri:encodeWithoutScheme()");
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
    if(logger!=undefined) logger.debug("SipUri:toString()");
    return this.encode();
}

SipUri.prototype.getUserAtHost =function(){
    if(logger!=undefined) logger.debug("SipUri:getUserAtHost()");
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
    if(logger!=undefined) logger.debug("SipUri:getUserAtHostPort()");
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
    if(logger!=undefined) logger.debug("SipUri:getParm():parmname="+parmname);
    var obj = this.uriParms.getValue(parmname);
    return obj;
}

SipUri.prototype.getMethod =function(){
    if(logger!=undefined) logger.debug("SipUri:getMethod()");
    return this.getParm(this.METHOD);
}

SipUri.prototype.getParameters =function(){
    if(logger!=undefined) logger.debug("SipUri:getParameters()");
    return this.uriParms;
}

SipUri.prototype.removeParameters=function(){
    if(logger!=undefined) logger.debug("SipUri:removeParameters()");
    this.uriParms = new NameValueList();
}

SipUri.prototype.getQheaders =function(){
    if(logger!=undefined) logger.debug("SipUri:getQheaders()");
    return this.qheaders;
}

SipUri.prototype.getUserType =function(){
    if(logger!=undefined) logger.debug("SipUri:getUserType()");
    return this.uriParms.getValue(this.USER);
}

SipUri.prototype.getUserPassword =function(){
    if(logger!=undefined) logger.debug("SipUri:getUserPassword()");
    if (this.authority == null)
    {
        return null;
    }
    return this.authority.getPassword();
}

SipUri.prototype.setUserPassword =function(password){
    if(logger!=undefined) logger.debug("SipUri:setUserPassword():password="+password);
    if (this.authority == null)
    {
        this.authority = new Authority();
    }
    this.authority.setPassword(password);
}

SipUri.prototype.getTelephoneSubscriber =function(){
    if(logger!=undefined) logger.debug("SipUri:getTelephoneSubscriber()");
    if (this.telephoneSubscriber == null) {
        
        this.telephoneSubscriber = new TelephoneNumber();
    }
    return this.telephoneSubscriber;
}

SipUri.prototype.getHostPort =function(){
    if(logger!=undefined) logger.debug("SipUri:getHostPort()");
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
    if(logger!=undefined) logger.debug("SipUri:getPort()");
    var hp = this.getHostPort();
    if (hp == null)
    {
        return -1;
    }
    return hp.getPort();
}

SipUri.prototype.getHost =function(){
    if(logger!=undefined) logger.debug("SipUri:getHost()");
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
    if(logger!=undefined) logger.debug("SipUri:isUserTelephoneSubscriber()");
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
    if(logger!=undefined) logger.debug("SipUri:removeTTL()");
    if (this.uriParms != null)
    {
        this.uriParms.delet(this.TTL);
    }
}

SipUri.prototype.removeMAddr =function(){
    if(logger!=undefined) logger.debug("SipUri:removeMAddr()");
    if (this.uriParms != null)
    {
        this.uriParms.delet(this.MADDR);
    }
}

SipUri.prototype.removeTransport =function(){
    if(logger!=undefined) logger.debug("SipUri:removeTransport()");
    if (this.uriParms != null)
    {
        this.uriParms.delet(this.TRANSPORT);
    }
}

SipUri.prototype.removeHeader =function(name){
    if(logger!=undefined) logger.debug("SipUri:removeHeader():name="+name);
    if (this.uriParms != null)
    {
        this.uriParms.delet(name);
    }
}

SipUri.prototype.removeHeaders =function(){
    if(logger!=undefined) logger.debug("SipUri:removeHeaders()");
    this.qheaders = new NameValueList();
}

SipUri.prototype.removeUserType =function(){
    if(logger!=undefined) logger.debug("SipUri:removeUserType()");
    if (this.uriParms != null)
    {
        this.uriParms.delet(this.USER);
    }
}

SipUri.prototype.removePort =function(){
    if(logger!=undefined) logger.debug("SipUri:removePort()");
    this.authority.removePort();
}

SipUri.prototype.removeMethod =function(){
    if(logger!=undefined) logger.debug("SipUri:removeMethod()");
    if (this.uriParms != null)
    {
        this.uriParms.delet(this.METHOD);
    }
}

SipUri.prototype.setUser =function(uname){
    if(logger!=undefined) logger.debug("SipUri:setUser():uname:"+uname);
    if (this.authority == null) 
    {
        this.authority = new Authority();
    }
    this.authority.setUser(uname);
}

SipUri.prototype.removeUser =function(){
    if(logger!=undefined) logger.debug("SipUri:removeUser()");
    this.authority.removeUserInfo();
}

SipUri.prototype.setDefaultParm=function(name, value){
    if(logger!=undefined) logger.debug("SipUri:setDefaultParm():name, value:"+name+","+value);
    if (this.uriParms.getValue(name) == null) 
    {
        var nv = new NameValue(name, value);
        this.uriParms.set_nv(nv);
    }
}

SipUri.prototype.setAuthority =function(authority){
    if(logger!=undefined) logger.debug("SipUri:setAuthority():authority:"+authority);
    this.authority = authority;
}

SipUri.prototype.setHost_Host =function(h){
    if(logger!=undefined) logger.debug("SipUri:setHost_Host():h="+h);
    if (this.authority == null)
    {
        this.authority = new Authority();
    }
    this.authority.setHost(h);
}

SipUri.prototype.setUriParms =function(parms){
    if(logger!=undefined) logger.debug("SipUri:setUriParms():parms="+parms);
    this.uriParms = parms;
}

SipUri.prototype.setUriParm =function(name, value){
    if(logger!=undefined) logger.debug("SipUri:setUriParm():name="+name+" value="+value);
    var nv = new NameValue(name, value);
    this.uriParms.set_nv(nv);
}

SipUri.prototype.setQheaders =function(parms){
    if(logger!=undefined) logger.debug("SipUri:setQheaders():parms="+parms);
    this.qheaders = parms;
}

SipUri.prototype.setMAddr =function(mAddr){
    if(logger!=undefined) logger.debug("SipUri:setMAddr():mAddr="+mAddr);
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
    if(logger!=undefined) logger.debug("SipUri:setUserParam():usertype="+usertype);
    this.uriParms.set_name_value(this.USER, usertype);
}

SipUri.prototype.setMethod =function(method){
    if(logger!=undefined) logger.debug("SipUri:setMethod():method="+method);
    this.uriParms.set_name_value(this.METHOD, method);
}

SipUri.prototype.setIsdnSubAddress =function(isdnSubAddress){
    if(logger!=undefined) logger.debug("SipUri:setIsdnSubAddress():isdnSubAddress="+isdnSubAddress);
    if (this.telephoneSubscriber == null)
    {
        this.telephoneSubscriber = new TelephoneNumber();
    }
    this.telephoneSubscriber.setIsdnSubaddress(isdnSubAddress);
}

SipUri.prototype.setTelephoneSubscriber =function(tel){
    if(logger!=undefined) logger.debug("SipUri:setTelephoneSubscriber():tel="+tel);
    this.telephoneSubscriber = tel;
}

SipUri.prototype.setPort =function(p){
    if(logger!=undefined) logger.debug("SipUri:setPort():p="+p);
    if (this.authority == null)
    {
        this.authority = new Authority();
    }
    this.authority.setPort(p);
}

SipUri.prototype.hasParameter =function(name){
    if(logger!=undefined) logger.debug("SipUri:hasParameter():name="+name);
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
    if(logger!=undefined) logger.debug("SipUri:setQHeader():nameValue="+nameValue);
    this.qheaders.set_nv(nameValue);
}
SipUri.prototype.setUriParameter =function(nameValue){
    if(logger!=undefined) logger.debug("SipUri:setUriParameter():nameValue="+nameValue);
    this.uriParms.set_nv(nameValue);
}

SipUri.prototype.hasTransport =function(){
    if(logger!=undefined) logger.debug("SipUri:hasTransport()");
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
    if(logger!=undefined) logger.debug("SipUri:removeParameter():name="+name);
    this.uriParms.delet(name);
}

SipUri.prototype.setHostPort =function(hostPort){
    if(logger!=undefined) logger.debug("SipUri:setHostPort():hostPort="+hostPort);
    if (this.authority == null) {
        this.authority = new Authority();
    }
    this.authority.setHostPort(hostPort);
}

SipUri.prototype.getHeader =function(name){
    if(logger!=undefined) logger.debug("SipUri:getHeader():name="+name);
    return this.qheaders.getValue(name) != null
    ? this.qheaders.getValue(name)
    : null;
}

SipUri.prototype.getHeaderNames=function(){
    if(logger!=undefined) logger.debug("SipUri:getHeaderNames()");
    return this.qheaders.getNames();
}

SipUri.prototype.getLrParam=function(){
    if(logger!=undefined) logger.debug("SipUri:getLrParam()");
    var haslr = this.hasParameter(this.LR);
    return haslr ? "true" : null;
}

SipUri.prototype.getMAddrParam=function(){
    if(logger!=undefined) logger.debug("SipUri:getMAddrParam()");
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
    if(logger!=undefined) logger.debug("SipUri:getMethodParam()");
    return this.getParameter(this.METHOD);
}

SipUri.prototype.getParameter=function(name){
    if(logger!=undefined) logger.debug("SipUri:getParameter():name="+name);
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
    if(logger!=undefined) logger.debug("SipUri:getParameterNames()");
    return this.uriParms.getNames();
}

SipUri.prototype.getTTLParam=function(){
    if(logger!=undefined) logger.debug("SipUri:getTTLParam()");
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
    if(logger!=undefined) logger.debug("SipUri:getTransportParam()");
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
    if(logger!=undefined) logger.debug("SipUri:getUser()");
    return this.authority.getUser();
}

SipUri.prototype.isSecure=function(){
    if(logger!=undefined) logger.debug("SipUri:isSecure()"); 
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
    if(logger!=undefined) logger.debug("SipUri:isSipURI()");
    return true;
}

SipUri.prototype.setHeader=function(name, value){
    if(logger!=undefined) logger.debug("SipUri:setHeader():name="+name+" value="+value);
    var nv = new NameValue(name, value);
    this.qheaders.set_nv(nv);
}

//in order to make difference between two setHost(), i have named them setHost_String et setHost_Host
SipUri.prototype.setHost_String=function(host){
    if(logger!=undefined) logger.debug("SipUri:setHost_String():host="+host);
    var h = new Host(host);
    this.setHost_Host(h);
}

SipUri.prototype.setLrParam=function(){
    if(logger!=undefined) logger.debug("SipUri:setLrParam()");
    this.uriParms.set_name_value("lr",null);
}

SipUri.prototype.setMAddrParam=function(maddr){
    if(logger!=undefined) logger.debug("SipUri:setMAddrParam():maddr="+maddr);
    if (maddr == null)
    {
        console.error("SipUri:setMAddrParam(): bad maddr");
        throw "SipUri:setMAddrParam(): bad maddr";
    }
    this.setParameter("maddr", maddr);
}

SipUri.prototype.setMethodParam=function(method){
    if(logger!=undefined) logger.debug("SipUri:setMethodParam():method="+method);
    this.setParameter("method", method);
}

SipUri.prototype.setParameter=function(name, value){
    if(logger!=undefined) logger.debug("SipUri:setParameter():name="+name+" value="+value);
    if (name.toLowerCase()=="ttl") {
        value=value-0;
    }
    this.uriParms.set_name_value(name,value);
}

SipUri.prototype.setSecure=function(secure){
    if(logger!=undefined) logger.debug("SipUri:setSecure():secure="+secure);
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
    if(logger!=undefined) logger.debug("SipUri:setTTLParam():ttl="+ttl);
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
    if(logger!=undefined) logger.debug("SipUri:setTransportParam():transport="+transport);
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
    if(logger!=undefined) logger.debug("SipUri:getUserParam()");
    return this.getParameter("user");
}

SipUri.prototype.hasLrParam=function(){
    if(logger!=undefined) logger.debug("SipUri:hasLrParam()");
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
    if(logger!=undefined) logger.debug("SipUri:hasGrParam()");
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
    if(logger!=undefined) logger.debug("SipUri:setGrParam():value="+value);
    this.uriParms.set_name_value(this.GRUU, value);
}

SipUri.prototype.getGrParam=function(){
    if(logger!=undefined) logger.debug("SipUri:getGrParam()");
    return this.uriParms.getValue(this.GRUU);
}

