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
 *  Implementation of the JAIN-SIP AuthenticationHeader .
 *  @see  gov/nist/javax/sip/header/AuthenticationHeader.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function AuthenticationHeader(name) {
    if(logger!=undefined) logger.debug("AuthenticationHeader:AuthenticationHeader()");
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
    if(logger!=undefined) logger.debug("AuthenticationHeader:setParameter():name="+name+",value="+value);
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
    if(logger!=undefined) logger.debug("AuthenticationHeader:setChallenge():challenge="+challenge);
    this.scheme = challenge.scheme;
    this.parameters = challenge.authParams;
}

AuthenticationHeader.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:encodeBody()");
    this.parameters.setSeparator(this.COMMA);
    return this.scheme + this.SP + this.parameters.encode();
}

AuthenticationHeader.prototype.setScheme =function(scheme){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setScheme():scheme="+scheme);
    this.scheme=scheme;
}

AuthenticationHeader.prototype.getScheme =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:getScheme()");
    return this.scheme;
}

AuthenticationHeader.prototype.setRealm =function(realm){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setRealm():realm="+realm);
    if (realm == null)
    {
        console.error("AuthenticationHeader:setRealm(): the realm parameter is null");
        throw "AuthenticationHeader:setRealm(): the realm parameter is null";
    }
    this.setParameter(this.REALM, realm);
}

AuthenticationHeader.prototype.getRealm =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:getRealm()");
    return this.getParameter(this.REALM);
}

AuthenticationHeader.prototype.setNonce =function(nonce){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setNonce():nonce="+nonce);
    if (nonce == null)
    {
        console.error("AuthenticationHeader:setNonce(): the nonce parameter is null");
        throw "AuthenticationHeader:setNonce(): the nonce parameter is null";
    }
    this.setParameter(this.NONCE, nonce);
}

AuthenticationHeader.prototype.getNonce =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:getNonce()");
    return this.getParameter(this.NONCE);
}

AuthenticationHeader.prototype.setURI =function(uri){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setURI():uri="+uri);
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
    if(logger!=undefined) logger.debug("AuthenticationHeader:getURI()");
    return this.getParameterAsURI(this.URI);
}

AuthenticationHeader.prototype.setAlgorithm =function(algorithm){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setAlgorithm():algorithm="+algorithm);
    if (algorithm == null)
    {
        console.error("AuthenticationHeader:setAlgorithm(): the algorithm parameter is null");
        throw "AuthenticationHeader:setAlgorithm(): the algorithm parameter is null";
    }
    this.setParameter(this.ALGORITHM, algorithm);
}

AuthenticationHeader.prototype.getAlgorithm =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:getAlgorithm()");
    return this.getParameter(this.ALGORITHM);
}

AuthenticationHeader.prototype.setQop =function(qop){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setQop():qop="+qop);
    if (qop == null)
    {
        console.error("AuthenticationHeader:setQop(): the qop parameter is null");
        throw "AuthenticationHeader:setQop(): the qop parameter is null";
    }
    this.setParameter(this.QOP, qop);
}

AuthenticationHeader.prototype.getQop =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:getQop()");
    return this.getParameter(this.QOP);
}

AuthenticationHeader.prototype.setOpaque =function(opaque){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setOpaque():opaque="+opaque);
    if (opaque == null)
    {
        console.error("AuthenticationHeader:setOpaque(): the opaque parameter is null");
        throw "AuthenticationHeader:setOpaque(): the opaque parameter is null";
    }
    this.setParameter(this.OPAQUE, opaque);
}

AuthenticationHeader.prototype.getOpaque =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:getOpaque()");
    return this.getParameter(this.OPAQUE);
}

AuthenticationHeader.prototype.setDomain =function(domain){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setDomain():domain:"+domain);
    if (domain == null)
    {
        console.error("AuthenticationHeader:setDomain(): the domain parameter is null");
        throw "AuthenticationHeader:setDomain(): the opaque parameter is null";
    }
    this.setParameter(this.DOMAIN, domain);
}

AuthenticationHeader.prototype.getDomain =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:getDomain()");
    return this.getParameter(this.DOMAIN);
}

AuthenticationHeader.prototype.setStale =function(stale){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setStale():stale="+stale);
    this.parameters.set_nv(new NameValue(this.STALE, new Boolean(stale)));
}

AuthenticationHeader.prototype.isStale =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:isStale()");
    return this.getParameterAsBoolean(this.STALE);
}

AuthenticationHeader.prototype.setCNonce =function(cnonce){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setCNonce():cnonce:"+cnonce);
    this.setParameter(this.CNONCE, cnonce);
}

AuthenticationHeader.prototype.getCNonce =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:getCNonce()");
    return this.getParameter(this.CNONCE);
}

AuthenticationHeader.prototype.getNonceCount =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:getNonceCount()");
    return this.getParameterAsNumber(this.NC);
}

AuthenticationHeader.prototype.setNonceCount =function(param){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setNonceCount():param:"+param);
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
    if(logger!=undefined) logger.debug("AuthenticationHeader:getResponse()");
    return this.getParameter(this.RESPONSE);
}

AuthenticationHeader.prototype.setResponse =function(response){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setResponse():response:"+response);
    if (response == null)
    {
        console.error("AuthenticationHeader:setResponse(): the domain parameter is null");
        throw "AuthenticationHeader:setResponse():  the opaque parameter is null";    
    }
    this.setParameter(this.RESPONSE, response);
}

AuthenticationHeader.prototype.getUsername =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:getUsername()");
    return this.getParameter(this.USERNAME);
}

AuthenticationHeader.prototype.setUsername =function(username){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setUsername():username:"+username);
    this.setParameter(this.USERNAME, username);
}

AuthenticationHeader.prototype.setIK =function(ik){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setIK():ik="+ik);
    if (ik == null)
    {
        console.error("AuthenticationHeader:setIK(): the auth-param IK parameter is null");
        throw "AuthenticationHeader:setIK(): the auth-param IK parameter is null";
    }
    this.setParameter(this.IK, ik);
}

AuthenticationHeader.prototype.getIK =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:getIK()");
    return this.getParameter(this.IK);
}

AuthenticationHeader.prototype.setCK =function(ck){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setCK():ck="+ck);
    if (ck == null)
    {
        console.error("AuthenticationHeader:setCK() the auth-param CK parameter is null");
        throw "AuthenticationHeader:setCK(): the auth-param CK parameter is null";
    }
    this.setParameter(this.CK, ck);
}

AuthenticationHeader.prototype.getCK =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:getCK()");
    return this.getParameter(this.CK);
}

AuthenticationHeader.prototype.setIntegrityProtected =function(integrityProtected){
    if(logger!=undefined) logger.debug("AuthenticationHeader:setIntegrityProtected():integrityProtected="+integrityProtected);
    if (integrityProtected == null)
    {
        console.error("AuthenticationHeader:setIntegrityProtected(): the integrity-protected parameter is null");
        throw "AuthenticationHeader:setIntegrityProtected(): the integrity-protected parameter is null";
    }
    this.setParameter(this.INTEGRITY_PROTECTED, integrityProtected);
}

AuthenticationHeader.prototype.getIntegrityProtected =function(){
    if(logger!=undefined) logger.debug("AuthenticationHeader:getIntegrityProtected()");
    return this.getParameter(this.INTEGRITY_PROTECTED);
}