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
    if(logger!=undefined) logger.debug("Authority:Authority()");
    this.classname="Authority"; 
    this.serialVersionUID = "-3570349777347017894L";
    this.userInfo =null;
    this.hostPort=new HostPort();
}

Authority.prototype.AT="@";

Authority.prototype.encode=function(){
    if(logger!=undefined) logger.debug("Authority:encode()");
    return this.encodeBuffer("");
}

Authority.prototype.encodeBuffer=function(buffer){
    if(logger!=undefined) logger.debug("Authority:encodeBuffer():buffer="+buffer);
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
    if(logger!=undefined) logger.debug("Authority:equals():other="+other);
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
    if(logger!=undefined) logger.debug("Authority:getHostPort()");
    return this.hostPort;
}

Authority.prototype.getUserInfo=function(){
    if(logger!=undefined) logger.debug("Authority:getUserInfo()");
    return this.userInfo;
}

Authority.prototype.getPassword=function(){
    if(logger!=undefined) logger.debug("Authority:getPassword()");
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
    if(logger!=undefined) logger.debug("Authority:getUser()");
    return this.userInfo != null ? this.userInfo.user : null;
}

Authority.prototype.getHost=function(){
    if(logger!=undefined) logger.debug("Authority:getHost()");
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
    if(logger!=undefined) logger.debug("Authority:getPort()");
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
    if(logger!=undefined) logger.debug("Authority:removePort()");
    if (this.hostPort != null)
    {
        this.hostPort.removePort();
    }
}

Authority.prototype.setPassword=function(passwd){
    if(logger!=undefined) logger.debug("Authority:setPassword():passwd="+passwd);
    if (this.userInfo == null)
    {
        this.userInfo = new UserInfo();
    }
    this.userInfo.setPassword(passwd);
}

Authority.prototype.setUser=function(user){
    if(logger!=undefined) logger.debug("Authority:setUser():user="+user);
    if (this.userInfo == null)
    {
        this.userInfo = new UserInfo();
    }
    this.userInfo.setUser(user);
}

Authority.prototype.setHost=function(host){
    if(logger!=undefined) logger.debug("Authority:setHost():host:"+host);
    if (this.hostPort == null)
    {
        this.hostPort = new HostPort();
    }
    this.hostPort.setHost(host);
}

Authority.prototype.setPort=function(port){
    if(logger!=undefined) logger.debug("Authority:setPort():port:"+port);
    if (this.hostPort == null)
    {
        this.hostPort = new HostPort();
    }
    this.hostPort.setPort(port);
}

Authority.prototype.setHostPort=function(h){
    if(logger!=undefined) logger.debug("Authority:setHostPort():h:"+h);
    this.hostPort=h;
}

Authority.prototype.setUserInfo=function(u){
    if(logger!=undefined) logger.debug("Authority:setUserInfo()");
    this.userInfo=u;
}

Authority.prototype.removeUserInfo=function(){
    if(logger!=undefined) logger.debug("Authority:removeUserInfo()");
    this.userInfo=null;
}

Authority.prototype.hashCode=function(){
    if(logger!=undefined) logger.debug("Authority:hashCode()");
    if ( this.hostPort == null ) 
    {
        console.error("Authority:hashCode(): null hostPort cannot compute hashcode");
        throw "Authority:hashCode(): null hostPort cannot compute hashcode";
    }
    return this.hostPort.encode().hashCode();
}
