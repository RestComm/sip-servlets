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
 *  Implementation of the JAIN-SIP TimeStamp .
 *  @see  gov/nist/javax/sip/header/TimeStamp.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function TimeStamp() {
    if(logger!=undefined) logger.debug("TimeStamp:TimeStamp()");
    this.classname="TimeStamp";
    this.serialVersionUID = "-3711322366481232720L";
    this.timeStamp = -1;
    this.delay = -1;
    this.delayFloat = -1;
    this.timeStampFloat = -1;
    this.headerName = this.TIMESTAMP;
}

TimeStamp.prototype = new SIPHeader();
TimeStamp.prototype.constructor=TimeStamp;
TimeStamp.prototype.TIMESTAMP="Timestamp";

TimeStamp.prototype.getTimeStampAsString =function(){
    if(logger!=undefined) logger.debug("TimeStamp:getTimeStampAsString()");
    if (this.timeStamp == -1 && this.timeStampFloat == -1)
    {
        return "";
    }
    else if (this.timeStamp != -1)
    {
        return this.timeStamp.toString();
    }
    else
    {
        return this.timeStampFloat.toString();
    }
}

TimeStamp.prototype.getDelayAsString =function(){
    if(logger!=undefined) logger.debug("TimeStamp:getDelayAsString()");
    if (this.delay == -1 && this.delayFloat == -1)
    {
        return "";
    }
    else if (this.delay != -1)
    {
        return this.delay.toString();
    }
    else
    {
        return this.delayFloat.toString();
    }
}

TimeStamp.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("TimeStamp:encodeBody()");
    var retval = "";
    var s1 = this.getTimeStampAsString();
    var s2 = this.getDelayAsString();
    if (s1=="" && s2=="")
    {
        return "";
    }
    if (s1!="")
    {
        retval=retval+s1;
    }
    if (s2!="")
    {
        retval=retval+" "+s2;
    }
    return retval.toString();
}

TimeStamp.prototype.hasDelay =function(){
    if(logger!=undefined) logger.debug("TimeStamp:hasDelay()");
    if(this.delay != -1)
    {
        return true;
    }
    else
    {
        return false;
    }
}

TimeStamp.prototype.removeDelay =function(){
    if(logger!=undefined) logger.debug("TimeStamp:removeDelay()");
    delay = -1;
}

TimeStamp.prototype.setTimeStamp =function(timeStamp){
    if(logger!=undefined) logger.debug("TimeStamp:setTimeStamp():timeStamp="+timeStamp);
    if (timeStamp < 0)
    {
        console.error("TimeStamp:setTimeStamp(): the timeStamp parameter is <0");
        throw "TimeStamp:setTimeStamp(): the timeStamp parameter is <0"; 
    }
    this.timeStamp = -1;
    this.timeStampFloat = timeStamp;
}

TimeStamp.prototype.getTimeStamp =function(){
    if(logger!=undefined) logger.debug("TimeStamp:getTimeStamp()");
    if(this.timeStampFloat == -1)
    {
        return this.timeStamp;
    }
    else
    {
        return this.timeStampFloat;
    }
}

TimeStamp.prototype.getDelay =function(){
    if(logger!=undefined) logger.debug("TimeStamp:getDelay()");
    if(this.delayFloat == -1)
    {
        return this.delay;
    }
    else
    {
        return this.delayFloat;
    }
}

TimeStamp.prototype.setDelay =function(delay){
    if(logger!=undefined) logger.debug("TimeStamp:setDelay():delay="+delay);
    if (delay < 0 && delay != -1)
    {
        console.error("TimeStamp:setDelay(): the delay parameter is <0");
        throw "TimeStamp:setDelay(): the delay parameter is <0"; 
    }
    this.delayFloat = delay;
    this.delay = -1;
}
TimeStamp.prototype.getTime =function(){
    if(logger!=undefined) logger.debug("TimeStamp:getTime()");
    return this.timeStamp == -1 ? this.timeStampFloat : this.timeStamp;
}

TimeStamp.prototype.getTimeDelay =function(){
    if(logger!=undefined) logger.debug("TimeStamp:getTimeDelay()");
    return this.delay == -1 ? this.delayFloat : this.delay;
}

TimeStamp.prototype.setTime =function(timeStamp){
    if(logger!=undefined) logger.debug("TimeStamp:setTime(): timeStamp="+timeStamp);
    if (this.timeStamp < -1)
    {
        console.error("TimeStamp:setTime(): illegal timestamp");
        throw "TimeStamp:setTime(): illegal timestamp"; 
    }
  
    this.timeStamp = timeStamp;
    this.timeStampFloat = -1;
}

TimeStamp.prototype.setTimeDelay =function(delay){
    if(logger!=undefined) logger.debug("TimeStamp:setTimeDelay():delay="+delay);
    if (delay < -1)
    {
        console.error("TimeStamp:setTimeDelay(): value out of range " + delay);
        throw "TimeStamp:setTimeDelay(): value out of range " + delay; 
    }
    this.delay = delay;
    this.delayFloat = -1;
}