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
 *  Implementation of the JAIN-SIP Allow .
 *  @see  gov/nist/javax/sip/header/Allow.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function Allow (m) {
    if(logger!=undefined) logger.debug("Allow:Allow(): m="+m);
    this.serialVersionUID = "-3105079479020693930L";
    this.classname="Allow"; 
    this.method=null;
    if(m==null)
    {
        this.headerName=this.ALLOW;
    }
    else if(arguments.length==1)
    {
        this.headerName=this.ALLOW;
        this.method=m;
    }
}

Allow.prototype = new SIPHeader();
Allow.prototype.constructor=Allow;
Allow.prototype.ALLOW="Allow";

Allow.prototype.getMethod =function(){
    if(logger!=undefined) logger.debug("Allow:getMethod()");
    return this.method;
}

Allow.prototype.setMethod =function(method){
    if(logger!=undefined) logger.debug("Allow:setMethod():method="+method);
    if (method == null)
    {
        console.error("Allow:setMethod(): JAIN-SIP Exception, the method parameter is null.");
        throw "Allow:setMethod(): the method parameter is null.";
    }
    this.method = method;
}

Allow.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("Allow:encodeBody()");
    return this.method;
}
