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
function ProxyAuthenticate() {
    if(logger!=undefined) logger.debug("ProxyAuthenticate:ProxyAuthenticate()");
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
    if(logger!=undefined) logger.debug("ProxyAuthenticate:getURI()");
    return null
}

ProxyAuthenticate.prototype.setURI =function(uri){
    if(logger!=undefined) logger.debug("ProxyAuthenticate:setURI():uri="+uri);
    
}