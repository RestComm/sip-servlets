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
 *  Implementation of the JAIN-SIP Server .
 *  @see  gov/nist/javax/sip/header/Server.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Server(to) {
    if(logger!=undefined) logger.debug("Server:Server(): to="+to);
    this.serialVersionUID = "-3587764149383342973L";
    this.classname="Server";
    this.productTokens = new Array();
    this.headerName=this.NAME;
}

Server.prototype = new SIPHeader();
Server.prototype.constructor=Server;
Server.prototype.NAME="Server";

Server.prototype.encodeProduct =function(){
    if(logger!=undefined) logger.debug("Server:encodeProduct()");
    var tokens = "";
    for(var i=0;i<this.productTokens.length;i++)
    {
        tokens=tokens+this.productTokens[i];
        if (i!=this.productTokens.length-1)
        {
            tokens=tokens+"/";
        }
        else
        {
            break;
        }
    }
    return tokens.toString();
}

Server.prototype.addProductToken =function(pt){
    if(logger!=undefined) logger.debug("Server:addProductToken():pt="+pt);
    this.productTokens.push(pt);
}

Server.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("Server:encodeBody()");
    return this.encodeProduct();
}

Server.prototype.getProduct =function(){
    if(logger!=undefined) logger.debug("Server:getProduct()");
    if (this.productTokens == null || this.productTokens.length==0)
    {
        return null;
    }
    else
    {
        return this.productTokens;
    }
}

Server.prototype.setProduct =function(product){
    if(logger!=undefined) logger.debug("Server:setProduct():product="+product.toString());
    if (product == null)
    {
        console.error("Server:setProduct(): the product parameter is null");
        throw "Server:setProduct(): the product parameter is null";
    }
    this.productTokens = product;
}