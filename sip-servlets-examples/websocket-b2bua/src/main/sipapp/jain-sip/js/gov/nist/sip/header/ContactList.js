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
 *  Implementation of the JAIN-SIP ContactList .
 *  @see  gov/nist/javax/sip/header/ContactList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ContactList() {
    if(logger!=undefined) logger.debug("ContactList:ContactList()");
    this.serialVersionUID = "1224806837758986814L";
    this.classname="ContactList";
    this.headerName = this.NAME;
    this.myClass =  "Contact";
    this.hlist=new Array();
}

ContactList.prototype = new SIPHeaderList();
ContactList.prototype.constructor=ContactList;
ContactList.prototype.NAME="Contact";

ContactList.prototype.clone =function(){
    if(logger!=undefined) logger.debug("ContactList:clone()");
}