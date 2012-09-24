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
 *  Implementation of the JAIN-SIP AllowEventsList .
 *  @see  gov/nist/javax/sip/header/AllowEventsList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function AllowEventsList() {
    if(logger!=undefined) logger.debug("AllowEventsList:AllowEventsList()");
    this.serialVersionUID = "-684763195336212992L";
    this.classname="AllowEventsList";
    this.headerName = this.NAME;
    this.myClass =  "AllowEvents";
}

AllowEventsList.prototype = new SIPHeaderList();
AllowEventsList.prototype.constructor=AllowEventsList;
AllowEventsList.prototype.NAME="Allow-Events";

AllowEventsList.prototype.clone =function(){
    if(logger!=undefined) logger.debug("AllowEventsList:clone()");
    
}

AllowEventsList.prototype.getMethods =function(){
    if(logger!=undefined) logger.debug("AllowEventsList:getMethods()");
    var ae=Object.getPrototypeOf(this).hlist;
    var array=new Array();
    for(var i=0;i<ae.length;i++)
    {
        var allowEvents = ae[i];
        array[i]=allowEvents.getEventType();
    }
    return array;
}

AllowEventsList.prototype.setMethods =function(methods){
    if(logger!=undefined) logger.debug("AllowEventsList:setMethods():methods"+methods);
    for(var i=0;i<methods.length;i++)
    {
        var allowEvents = new AllowEvents();
        allowEvents.setEventType(methods[i]);
        this.add(allowEvents);
    }
}
