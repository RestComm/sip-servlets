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
 *  Implementation of the JAIN-SIP Event .
 *  @see  gov/nist/javax/sip/header/Event.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Event() {
    if(logger!=undefined) logger.debug("Event:Event()");
    this.serialVersionUID = "-6458387810431874841L";
    this.classname="Event";
    this.eventType=null;
    this.headerName=this.EVENT;
    this.parameters = new NameValueList();
    this.duplicates = new DuplicateNameValueList();
}

Event.prototype = new ParametersHeader();
Event.prototype.constructor=Event;
Event.prototype.EVENT="Event";
Event.prototype.ID="id";
Event.prototype.SEMICOLON=";";


Event.prototype.setEventType =function(eventType){
    if(logger!=undefined) logger.debug("Event:setEventType():eventType="+eventType);
    if (eventType == null)
    {
        console.error("Event:setEventType(): the eventType is null");
        throw "Event:setEventType():  the eventType is null";
    }
    this.eventType = eventType;
}

Event.prototype.getEventType =function(){
    if(logger!=undefined) logger.debug("Event:getEventType()");
    return this.eventType;
}

Event.prototype.setEventId =function(eventId){
    if(logger!=undefined) logger.debug("Event:setEventId():eventId="+eventId);
    if (eventId == null)
    {
        console.error("Event:setEventId(): the eventId is null");
        throw "Event:setEventId(): the eventId is null";
    }
    this.setParameter(this.ID, eventId);
}

Event.prototype.getEventId =function(){
    if(logger!=undefined) logger.debug("Event:getEventId()");
    return this.getParameter(this.ID);
}

Event.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("Event:encodeBody()");
    return this.encodeBodyBuffer("").toString();
}

Event.prototype.encodeBodyBuffer =function(buffer){
    if(logger!=undefined) logger.debug("Event:encodeBodyBuffer():buffer="+buffer);
    if (this.eventType != null)
    {
        buffer=buffer+this.eventType;
    }

    if (this.parameters.hmap.length!=0) {
        buffer=buffer+this.SEMICOLON;
        buffer=this.parameters.encodeBuffer(buffer);
    }
    return buffer;
}

Event.prototype.match =function(matchTarget){
    if(logger!=undefined) logger.debug("Event:match():matchTarget="+matchTarget);
    if (matchTarget.eventType == null && this.eventType != null)
    {
        return false;
    }
    else if (matchTarget.eventType != null && this.eventType == null)
    {
        return false;
    }
    else if (this.eventType == null && matchTarget.eventType == null)
    {
        return false;
    }
    else if (getEventId() == null && matchTarget.getEventId() != null)
    {
        return false;
    }
    else if (getEventId() != null && matchTarget.getEventId() == null)
    {
        return false;
    }
    if(matchTarget.eventType.toLowerCase()==this.eventType
        && ((this.getEventId() == matchTarget.getEventId())
            || this.getEventId().toLowerCase()==matchTarget.getEventId()))
            {
        return true
    }
    else
    {
        return false
    }
}