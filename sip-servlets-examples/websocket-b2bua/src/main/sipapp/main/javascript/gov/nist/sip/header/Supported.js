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
 *  Implementation of the JAIN-SIP Supported .
 *  @see  gov/nist/javax/sip/header/Supported.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Supported(option_tag) {
    if(logger!=undefined) logger.debug("Supported:Supported()");
    this.serialVersionUID = "-7679667592702854542L";
    this.classname="Supported";
    this.optionTag=null;
    if(option_tag!=null)
    {
        this.headerName="Supported";
        this.optionTag=option_tag;
    }
    else
    {
        this.headerName="Supported";
        this.optionTag=null;    
    }
}

Supported.prototype = new SIPHeader();
Supported.prototype.constructor=Supported;
Supported.prototype.COLON=":";
Supported.prototype.NEWLINE="\r\n";

Supported.prototype.encode =function(){
    if(logger!=undefined) logger.debug("Supported:encode()");
    var retval = this.headerName + this.COLON;
    if (this.optionTag != null)
    {
        retval = retval+this.SP + this.optionTag;
    }
    retval = retval+this.NEWLINE;
    return retval;
}

Supported.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("Supported:encodeBody()");
    return this.optionTag != null ? this.optionTag : "";
}

Supported.prototype.setOptionTag =function(optionTag){
    if(logger!=undefined) logger.debug("Supported:setOptionTag():optionTag="+optionTag);
    if (optionTag == null)
    {
        console.error("Supported:setOptionTag(): the optionTag parameter is null");
        throw "Supported:setOptionTag(): the optionTag parameter is null"; 
    }
    this.optionTag = optionTag;
}

Supported.prototype.getOptionTag =function(){
    if(logger!=undefined) logger.debug("Supported:getOptionTag()");
    return this.optionTag;
}