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
 *  Implementation of the JAIN-SIP RouteList .
 *  @see  gov/nist/javax/sip/header/RouteList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function RouteList() {
    if(logger!=undefined) logger.debug("RouteList:RouteList()");
    this.serialVersionUID = "1L";
    this.classname="RouteList";
    this.headerName = this.NAME;
    this.myClass =  "Route";
    this.hlist=new Array();
}

RouteList.prototype = new SIPHeaderList();
RouteList.prototype.constructor=RouteList;
RouteList.prototype.NAME="Route";

RouteList.prototype.clone =function(){
    if(logger!=undefined) logger.debug("RouteList:clone()");
    
}

RouteList.prototype.encode =function(){
    if(logger!=undefined) logger.debug("RouteList:encode()");
    if (this.hlist.length==0) 
    {
        return "";
    }
    else 
    {
        return this.encodeBuffer("");
    }
}
RouteList.prototype.equals =function(){
    if(logger!=undefined) logger.debug("RouteList:equals()");
    
}
