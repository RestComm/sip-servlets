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
 *  Implementation of the JAIN-SIP UserAgent .
 *  @see  gov/nist/javax/sip/header/UserAgent.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function UserAgent() {
    if(logger!=undefined) logger.debug("UserAgent:UserAgent()");
    this.serialVersionUID = "4561239179796364295L";
    this.classname="UserAgent";
    this.productTokens=new Array();
    this.headerName=this.NAME;
}

UserAgent.prototype = new SIPHeader();
UserAgent.prototype.constructor=UserAgent;
UserAgent.prototype.NAME="User-Agent";

UserAgent.prototype.encodeProduct =function(){
    if(logger!=undefined) logger.debug("UserAgent_encodeProduct");
    var tokens = "";
    for(var i=0;i<this.productTokens.length;i++)
    {
        tokens=tokens+this.productTokens[i];
    }
    return tokens.toString();
}

UserAgent.prototype.addProductToken =function(pt){
    if(logger!=undefined) logger.debug("UserAgent_addProductToken");
    var x=this.productTokens.length;
    this.productTokens[x]=pt;
}

UserAgent.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("UserAgent_encodeBody");
    return this.encodeProduct();
}

UserAgent.prototype.getProduct =function(){
    if(logger!=undefined) logger.debug("UserAgent_getProduct");
    if (this.productTokens == null || this.productTokens.length==0)
    {
        return null;
    }
    else
    {
        return this.productTokens;
    }
}

UserAgent.prototype.setProduct =function(product){
    if(logger!=undefined) logger.debug("UserAgent_setProduct");
    if (product == null)
    {
        console.error("UserAgent:setProduct(): the product parameter is null");
        throw "UserAgent:setProduct(): the product parameter is null"; 
    }
    this.productTokens = product;
}
