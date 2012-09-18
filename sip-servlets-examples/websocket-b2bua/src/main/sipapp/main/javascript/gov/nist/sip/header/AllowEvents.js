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
 *  Implementation of the JAIN-SIP AllowEvents .
 *  @see  gov/nist/javax/sip/header/AllowEvents.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function AllowEvents(m) {
    if(logger!=undefined) logger.debug("AllowEvents:AllowEvents()");
    this.serialVersionUID = "5281728373401351378L";
    this.classname="AllowEvents";
    this.eventType=null;
    if(m==null)
    {
        this.headerName=this.ALLOW_EVENTS;
    }
    else
    {
        this.headerName=this.ALLOW_EVENTS;
        this.eventType=m;
    }
}

AllowEvents.prototype = new SIPHeader();
AllowEvents.prototype.constructor=AllowEvents;
AllowEvents.prototype.ALLOW_EVENTS="Allow-Events";

AllowEvents.prototype.setEventType =function(eventType){
    if(logger!=undefined) logger.debug("AllowEvents:setEventType():eventType="+eventType);
    if (eventType == null)
    {
        console.error("AllowEvents:setEventType(): the eventType parameter is null");
        throw "AllowEvents:setEventType(): the eventType parameter is null";      
    }
    this.eventType = eventType;
}


AllowEvents.prototype.getEventType =function(){
    if(logger!=undefined) logger.debug("AllowEvents:getEventType()");
    return this.eventType;
}

AllowEvents.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("AllowEvents:encodeBody()");
    return this.eventType;
}