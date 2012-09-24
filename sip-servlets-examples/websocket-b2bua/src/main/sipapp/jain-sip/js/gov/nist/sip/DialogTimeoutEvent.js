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
 *  Implementation of the JAIN-SIP DialogTimeoutEvent .
 *  @see  gov/nist/javax/sip/DialogTimeoutEvent.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function DialogTimeoutEvent(source,dialog,reason) {
    if(logger!=undefined) logger.debug("DialogTimeoutEvent:DialogTimeoutEvent(): source="+source);
    if(logger!=undefined) logger.debug("DialogTimeoutEvent:DialogTimeoutEvent(): dialog="+dialog);
    if(logger!=undefined) logger.debug("DialogTimeoutEvent:DialogTimeoutEvent(): reason="+reason);
    this.classname="DialogTimeoutEvent";
    this.serialVersionUID = "-2514000059989311925L";
    this.source=source;
    this.m_dialog = dialog;
    this.m_reason = reason;
}

DialogTimeoutEvent.prototype.AckNotReceived="AckNotReceived";
DialogTimeoutEvent.prototype.AckNotSent="AckNotSent";
DialogTimeoutEvent.prototype.ReInviteTimeout="ReInviteTimeout";

DialogTimeoutEvent.prototype.getDialog =function(){
    if(logger!=undefined) logger.debug("DialogTimeoutEvent:getDialog()");
    return this.m_dialog;
}
DialogTimeoutEvent.prototype.getReason =function(){
    if(logger!=undefined) logger.debug("DialogTimeoutEvent:getReason()");
    return this.m_reason;
}
