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
 *  Implementation of the JAIN-SIP Challenge .
 *  @see  gov/nist/javax/sip/header/Challenge.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Challenge() {
    if(logger!=undefined) logger.debug("Challenge:Challenge()");
    this.serialVersionUID = "5944455875924336L";
    this.classname="Challenge";
    this.authParams=new NameValueList();
    this.scheme=null;
    this.authParams.setSeparator(this.COMMA);
}

Challenge.prototype = new SIPObject();
Challenge.prototype.constructor=Challenge;
Challenge.prototype.DOMAIN = "domain";
Challenge.prototype.REALM = "realm";
Challenge.prototype.OPAQUE = "opaque";
Challenge.prototype.ALGORITHM = "algorithm";
Challenge.prototype.QOP = "qop";
Challenge.prototype.STALE = "stale";
Challenge.prototype.SIGNATURE = "signature";
Challenge.prototype.RESPONSE = "response";
Challenge.prototype.SIGNED_BY = "signed-by";
Challenge.prototype.URI = "uri";
Challenge.prototype.COMMA = ",";
Challenge.prototype.SP = " ";
Challenge.prototype.DOUBLE_QUOTE = "\"";

Challenge.prototype.encode =function(){
    if(logger!=undefined) logger.debug("Challenge:encode()");
    var buffer=(this.scheme+this.SP+this.authParams.encode()).toString();
    return buffer;
}

Challenge.prototype.getScheme =function(){
    if(logger!=undefined) logger.debug("Challenge:getScheme()");
    return this.scheme;
}

Challenge.prototype.getAuthParams =function(){
    if(logger!=undefined) logger.debug("Challenge:getAuthParams()");
    return this.authParams;
}

Challenge.prototype.getDomain =function(){
    if(logger!=undefined) logger.debug("Challenge:getDomain()");
    return this.authParams.getValue(this.DOMAIN);
}

Challenge.prototype.getURI =function(){
    if(logger!=undefined) logger.debug("Challenge:getURI()");
    return this.authParams.getValue(this.URI);
}

Challenge.prototype.getOpaque =function(){
    if(logger!=undefined) logger.debug("Challenge:getOpaque()");
    return this.authParams.getValue(this.OPAQUE);
}

Challenge.prototype.getQOP =function(){
    if(logger!=undefined) logger.debug("Challenge:getQOP()");
    return this.authParams.getValue(this.QOP);
}

Challenge.prototype.getAlgorithm =function(){
    if(logger!=undefined) logger.debug("Challenge:getAlgorithm()");
    return this.authParams.getValue(this.ALGORITHM);
}

Challenge.prototype.getStale =function(){
    if(logger!=undefined) logger.debug("Challenge:getStale()");
    return this.authParams.getValue(this.STALE);
}

Challenge.prototype.getSignature =function(){
    if(logger!=undefined) logger.debug("Challenge:getSignature()");
    return this.authParams.getValue(this.SIGNATURE);
}

Challenge.prototype.getSignedBy =function(){
    if(logger!=undefined) logger.debug("Challenge:getSignedBy()");
    return this.authParams.getValue(this.SIGNED_BY);
}

Challenge.prototype.getResponse =function(){
    if(logger!=undefined) logger.debug("Challenge:getResponse()");
    return this.authParams.getValue(this.RESPONSE);
}

Challenge.prototype.getRealm =function(){
    if(logger!=undefined) logger.debug("Challenge:getRealm()");
    return this.authParams.getValue(this.REALM);
}
Challenge.prototype.getParameter =function(name){
    if(logger!=undefined) logger.debug("Challenge:getParameter():name="+name);
    return this.authParams.getValue(name);
}

Challenge.prototype.hasParameter =function(name){
    if(logger!=undefined) logger.debug("Challenge:hasParameter():name="+name);
    if(this.authParams.getNameValue(name) != null)
    {
        return true;
    }
    else
    {
        return false;
    }
}

Challenge.prototype.hasParameters =function(){
    if(logger!=undefined) logger.debug("Challenge:hasParameters()");
    if(this.authParams.length != 0 )
    {
        return true;
    }
    else
    {
        return false;
    }
}

Challenge.prototype.removeParameter =function(name){
    if(logger!=undefined) logger.debug("Challenge:removeParameter():name="+name);
    return this.authParams.delet(name);
}

Challenge.prototype.removeParameters =function(){
    if(logger!=undefined) logger.debug("Challenge:removeParameters()");
    this.authParams = new NameValueList();
}

Challenge.prototype.setParameter =function(nv){
    if(logger!=undefined) logger.debug("Challenge:setParameter():nv="+nv);
    this.authParams.set_nv(nv);
}

Challenge.prototype.setScheme =function(s){
    if(logger!=undefined) logger.debug("Challenge:setScheme():s="+s);
    this.scheme=s;
}

Challenge.prototype.setAuthParams =function(a){
    if(logger!=undefined) logger.debug("Challenge:setAuthParams():a="+a);
    this.authParams=a;
}