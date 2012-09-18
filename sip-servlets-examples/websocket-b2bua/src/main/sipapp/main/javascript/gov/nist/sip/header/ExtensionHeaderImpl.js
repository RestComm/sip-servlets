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
 *  Implementation of the JAIN-SIP ExtensionHeaderImpl .
 *  @see  gov/nist/javax/sip/header/ExtensionHeaderImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function ExtensionHeaderImpl(headerName) {
    if(logger!=undefined) logger.debug("ExtensionHeaderImpl:ExtensionHeaderImpl(): headerName="+headerName);
    this.classname="ExtensionHeaderImpl"; 
    this.serialVersionUID = "-8693922839612081849L";
    this.value=null;
    if(headerName!=null)
    {
        this.headerName = headerName;
    }
}

ExtensionHeaderImpl.prototype = new SIPHeader();
ExtensionHeaderImpl.prototype.constructor=ExtensionHeaderImpl;
ExtensionHeaderImpl.prototype.COLON=":";
ExtensionHeaderImpl.prototype.SP=" ";
ExtensionHeaderImpl.prototype.NEWLINE="\r\n";

ExtensionHeaderImpl.prototype.setName =function(headerName){
    if(logger!=undefined) logger.debug("ExtensionHeaderImpl:setName():headerName="+headerName);
    this.headerName = headerName;
}

ExtensionHeaderImpl.prototype.setValue =function(value){
    if(logger!=undefined) logger.debug("ExtensionHeaderImpl:setValue():value="+value);
    this.value = value;
}

ExtensionHeaderImpl.prototype.getHeaderValue =function(){
    if(logger!=undefined) logger.debug("ExtensionHeaderImpl:getHeaderValue()");
    if (this.value != null) {
        return this.value;
    } 
    else {
        var encodedHdr = null;
        try {
            encodedHdr = this.encode();
        } catch (ex) {
            console.error("ExtensionHeaderImpl:getHeaderValue(): catched exception:"+ex);
            return null;
        }
        var buffer = encodedHdr;
        var x=0;
        var chaine=buffer.substring(x);
        while (chaine.length > 0 && buffer.charAt(0) != ':') {
            x=x+1;
            chaine=buffer.substring(x);
        }
        x=x+1;
        chaine=buffer.substring(x);
        this.value = chaine.toString().replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
        return this.value;
    }
}

ExtensionHeaderImpl.prototype.encode =function(){
    if(logger!=undefined) logger.debug("ExtensionHeaderImpl:encode()");
    var encode="";
    encode=encode+this.headerName+this.COLON+this.SP+this.value+this.NEWLINE;
    return encode;
}

ExtensionHeaderImpl.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("ExtensionHeaderImpl:encodeBody()");
    return this.getHeaderValue();
}