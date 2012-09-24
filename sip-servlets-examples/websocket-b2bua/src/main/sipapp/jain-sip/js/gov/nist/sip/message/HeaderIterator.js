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
 *  Implementation of the JAIN-SIP HeaderIterator .
 *  @see  gov/nist/javax/sip/message/HeaderIterator.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function HeaderIterator(sipMessage, sipHeader) {
    if(logger!=undefined) logger.debug("HeaderIterator:HeaderIterator()");
    this.classname="HeaderIterator";
    this.toRemove=null;
    this.index=null;
    this.sipMessage=sipMessage;
    this.sipHeader=sipHeader;
}

HeaderIterator.prototype.next =function(){
    if(logger!=undefined) logger.debug("HeaderIterator:next()");
    if (this.sipHeader == null || this.index == 1)
    {
        console.error("HeaderIterator:next(): ");
        throw "HeaderIterator:next(): ";
    }
    this.toRemove = true;
    this.index = 1;
    return this.sipHeader;
}

HeaderIterator.prototype.previous =function(){
    if(logger!=undefined) logger.debug("HeaderIterator:previous()");
    if (this.sipHeader == null || this.index == 0)
    {
        console.error("HeaderIterator:previous(): ");
        throw "HeaderIterator:previous(): ";
    }
    this.toRemove = true;
    this.index = 0;
    return this.sipHeader;
}

HeaderIterator.prototype.nextIndex =function(){
    if(logger!=undefined) logger.debug("HeaderIterator:nextIndex()");
    return 1;
}

HeaderIterator.prototype.previousIndex =function(){
    if(logger!=undefined) logger.debug("HeaderIterator:previousIndex()");
    return this.index == 0 ? -1 : 0;
}

HeaderIterator.prototype.set =function(){
    if(logger!=undefined) logger.debug("HeaderIterator:set()");
    console.error("HeaderIterator:add(): unsupported operation");
    throw "HeaderIterator:add(): unsupported operation";
}

HeaderIterator.prototype.add =function(){
    if(logger!=undefined) logger.debug("HeaderIterator:add()");
    console.error("HeaderIterator:add(): unsupported operation");
    throw "HeaderIterator:add(): unsupported operationn";
}

HeaderIterator.prototype.remove =function(){
    if(logger!=undefined) logger.debug("HeaderIterator:remove()");
    if (this.sipHeader == null)
    {
        console.error("HeaderIterator:remove(): illegal state");
        throw "HeaderIterator:remove(): illegal state";
    }
    if (this.toRemove) {
        this.sipHeader = null;
        this.sipMessage.removeHeader(this.sipHeader.getName());
    } else {
        console.error("HeaderIterator:remove(): illegal state");
        throw "HeaderIterator:remove(): illegal state";
    }
}

HeaderIterator.prototype.hasNext =function(){
    if(logger!=undefined) logger.debug("HeaderIterator:hasNext()");
    if(this.index == 0)
    {
        return true;
    }
    else
    {
        return false;
    }
}

HeaderIterator.prototype.hasPrevious =function(){
    if(logger!=undefined) logger.debug("HeaderIterator:hasPrevious()");
    if(this.index == 1)
    {
        return true;
    }
    else
    {
        return false;
    }
}