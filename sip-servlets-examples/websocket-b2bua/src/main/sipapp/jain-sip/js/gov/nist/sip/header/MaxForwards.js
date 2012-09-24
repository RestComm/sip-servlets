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
 *  Implementation of the JAIN-SIP MaxForwards .
 *  @see  gov/nist/javax/sip/header/MaxForwards.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function MaxForwards(m) {
    if(logger!=undefined) logger.debug("MaxForwards:MaxForwards()");
    this.serialVersionUID = "-3096874323347175943L";
    this.classname="MaxForwards";
    this.headerName=this.NAME;
    this.maxForwards=null;
    if(m==null)
    {
        this.headerName=this.NAME;
    }
    else
    {
        this.headerName=this.NAME;
        this.setMaxForwards( m );
    }
}

MaxForwards.prototype = new SIPHeader();
MaxForwards.prototype.constructor=MaxForwards;
MaxForwards.prototype.NAME="Max-Forwards";

MaxForwards.prototype.getMaxForwards =function(){
    if(logger!=undefined) logger.debug("MaxForwards:getMaxForwards()");
    return this.maxForwards;
}

MaxForwards.prototype.setMaxForwards =function(maxForwards){
    if(logger!=undefined) logger.debug("MaxForwards:setMaxForwards():maxForwards="+maxForwards);
    if (maxForwards < 0 || maxForwards > 255)
    { 
        console.error("MaxForwards:setMaxForwards(): bad max forwards value " + maxForwards);
        throw "MaxForwards:setMaxForwards(): bad max forwards value " + maxForwards;
    }
    this.maxForwards = maxForwards;
}

MaxForwards.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("MaxForwards:encodeBody()");
    return this.encodeBodyBuffer("").toString();
}

MaxForwards.prototype.encodeBodyBuffer =function(buffer){
    if(logger!=undefined) logger.debug("MaxForwards:encodeBodyBuffer():buffer="+buffer);
    buffer=buffer+this.maxForwards;
    return buffer;
}

MaxForwards.prototype.hasReachedZero =function(){
    if(logger!=undefined) logger.debug("MaxForwards:hasReachedZero()");
    if(this.maxForwards == 0)
    {
        return true;
    }
    else
    {
        return false;
    }
}

MaxForwards.prototype.decrementMaxForwards =function(){
    if(logger!=undefined) logger.debug("MaxForwards:decrementMaxForwards()");
    if (this.maxForwards > 0)
    {
        this.maxForwards--;
    }
    else 
    {
       console.error("MaxForwards:decrementMaxForwards(): has already reached 0!");
       throw "MaxForwards:decrementMaxForwards(): has already reached 0!";
    }
}
