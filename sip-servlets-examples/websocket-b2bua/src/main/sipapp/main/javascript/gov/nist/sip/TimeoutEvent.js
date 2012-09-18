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
 *  Implementation of the JAIN-SIP TimeoutEvent .
 *  @see  gov/nist/javax/sip/TimeoutEvent.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function TimeoutEvent() {
    if(logger!=undefined) logger.debug("TimeoutEvent:TimeoutEvent()");
    this.classname="TimeoutEvent";
    this.mTimeout=null;
    this.mIsServerTransaction=null;
    this.mServerTransaction=null;
    this.mClientTransaction=null;
    this.source=null;
    if(arguments[1] instanceof SIPServerTransaction)
    {
        var source=arguments[0];
        var serverTransaction=arguments[1];
        var timeout=arguments[2];
        this.source=source;
        this.mTimeout = timeout;
        this.mServerTransaction = serverTransaction;
        this.mIsServerTransaction = true;
    }
    else if(arguments[1] instanceof SIPClientTransaction)
    {
        source=arguments[0];
        var clientTransaction=arguments[1];
        timeout=arguments[2];
        this.source=source;
        this.mTimeout = timeout;
        this.mClientTransaction = clientTransaction;
        this.mIsServerTransaction = false;
    }
}

TimeoutEvent.prototype = new TransactionTerminatedEvent();
TimeoutEvent.prototype.constructor=TimeoutEvent;

TimeoutEvent.prototype.getTimeout =function(){
    if(logger!=undefined) logger.debug("TimeoutEvent:getTimeout()");
    return this.mTimeout;
}

