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
    if(logger!=undefined) logger.debug("Via:Via()");
    this.serialVersionUID = "5281728373401351378L";
    this.classname="Via";
    this.sentProtocol=new Protocol();
    this.sentBy=new HostPort();
    this.comment=null;
    this.rPortFlag = false;
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
    if(logger!=undefined) logger.debug("Via:getProtocolVersion()");
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
    if(logger!=undefined) logger.debug("Via:getSentProtocol()");
    return this.sentProtocol;
}

Via.prototype.getSentBy =function(){
    if(logger!=undefined) logger.debug("Via:getSentBy()");
    return this.sentBy;
}

Via.prototype.getHop =function(){
    if(logger!=undefined) logger.debug("Via:getHop()");
    var hop = new HopImpl(this.sentBy.getHost().getHostname());
    return hop;
}

Via.prototype.getViaParms =function(){
    if(logger!=undefined) logger.debug("Via:getViaParms()");
    return this.parameters;
}
Via.prototype.hasPort =function(){
    if(logger!=undefined) logger.debug("Via:hasPort()");
    return this.getSentBy().hasPort();
}

Via.prototype.hasComment =function(){
    if(logger!=undefined) logger.debug("Via:hasComment()");
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
    if(logger!=undefined) logger.debug("Via:removePort()");
    this.sentBy.removePort();
}

Via.prototype.removeComment =function(){
    if(logger!=undefined) logger.debug("Via:removeComment()");
    this.comment = null;
}

Via.prototype.setProtocolVersion =function(){
    if(logger!=undefined) logger.debug("Via:setProtocolVersion()");
    if (this.sentProtocol == null)
    {
        this.sentProtocol = new Protocol();
    }
    this.sentProtocol.setProtocolVersion(this.protocolVersion);
}

Via.prototype.setHost =function(host){
    if(logger!=undefined) logger.debug("Via:setHost():host="+host);
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
    if(logger!=undefined) logger.debug("Via:setSentProtocol():s="+s);
    this.sentProtocol = s;
}

Via.prototype.setSentBy =function(s){
    if(logger!=undefined) logger.debug("Via:setSentBy():s="+s);
    this.sentBy = s;
}

Via.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("Via:encodeBody()");
    return this.encodeBodyBuffer("").toString();
}

Via.prototype.encodeBodyBuffer =function(buffer){
    if(logger!=undefined) logger.debug("Via:encodeBodyBuffer():buffer="+buffer);
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
    if(logger!=undefined) logger.debug("Via:getHost()");
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
    if(logger!=undefined) logger.debug("Via:setPort():port="+port);
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
    if(logger!=undefined) logger.debug("Via:setRPort()");
    this.rPortFlag = true;
}

Via.prototype.getPort =function(){
    if(logger!=undefined) logger.debug("Via:getPort()");
    if (this.sentBy == null)
    {
        return -1;
    }
    return this.sentBy.getPort();
}

Via.prototype.getRPort =function(){
    if(logger!=undefined) logger.debug("Via:getRPort()");
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
    if(logger!=undefined) logger.debug("Via:getTransport()");
    if (this.sentProtocol == null)
    {
        return null;
    }
    return this.sentProtocol.getTransport();
}

Via.prototype.setTransport =function(transport){
    if(logger!=undefined) logger.debug("Via:setTransport():transport="+transport);
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
    if(logger!=undefined) logger.debug("Via:getProtocol()");
    if (this.sentProtocol == null)
    {
        return null;
    }
    return this.sentProtocol.getProtocol();
}

Via.prototype.setProtocol =function(protocol){
    if(logger!=undefined) logger.debug("Via:setProtocol():protocol="+protocol);
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
    if(logger!=undefined) logger.debug("Via:getTTL()");
    var ttl = this.getParameterAsNumber(this.TTL);
    return ttl;
}

Via.prototype.setTTL =function(ttl){
    if(logger!=undefined) logger.debug("Via:setTTL():ttl="+ttl);
    if (ttl < 0 && ttl != -1)
    {
        console.error("Via:setTTL():  the ttl parameter is < 0");
        throw "Via:setTTL(): the ttl parameter is < 0"; 
    }
    ttl=ttl-0;
    this.setParameter_nv(new NameValue(this.TTL, ttl));
}

Via.prototype.getMAddr =function(){
    if(logger!=undefined) logger.debug("Via:getMAddr()");
    return this.getParameter(this.MADDR);
}

Via.prototype.setMAddr =function(mAddr){
    if(logger!=undefined) logger.debug("Via:setMAddr():mAddr="+mAddr);
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
    if(logger!=undefined) logger.debug("Via:getReceived()");
    return this.getParameter(this.RECEIVED);
}

Via.prototype.setReceived =function(received){
    if(logger!=undefined) logger.debug("Via:setReceived():received="+received);
    if (received == null)
    {
       console.error("Via:setReceived():  the received parameter is < 0");
       throw "Via:setReceived(): the received parameter is < 0"; 
    }
    this.setParameter(this.RECEIVED, received);
}

Via.prototype.getBranch =function(){
    if(logger!=undefined) logger.debug("Via:getBranch()");
    return this.getParameter(this.BRANCH);
}

Via.prototype.setBranch =function(branch){
    if(logger!=undefined) logger.debug("Via:setBranch():branch:"+branch);
    if (branch == null || branch.length==0)
    {
       console.error("Via:setBranch():  branch parameter is null or length 0");
       throw "Via:setBranch(): branch parameter is null or length 0"; 
    }
    this.setParameter(this.BRANCH, branch);
}

Via.prototype.getSentByField =function(){
    if(logger!=undefined) logger.debug("Via:getSentByField()");
    if(this.sentBy != null)
    {
        return this.sentBy.encode();
    }
    return null;
}
Via.prototype.getSentProtocolField =function(){
    if(logger!=undefined) logger.debug("Via:getSentProtocolField()");
    if(this.sentProtocol != null)
    {
        return this.sentProtocol.encode();
    }
    return null;
}