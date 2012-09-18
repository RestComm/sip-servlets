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
 *  Implementation of the JAIN-SIP Subject .
 *  @see  gov/nist/javax/sip/header/Subject.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Subject() {
    if(logger!=undefined) logger.debug("Subject:Subject()");
    this.serialVersionUID = "-6479220126758862528L";
    this.classname="Subject";
    this.headerName=this.SUBJECT;
    this.subject=null;
}

Subject.prototype = new SIPHeader();
Subject.prototype.constructor=Subject;
Subject.prototype.SUBJECT="Subject";

Subject.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("Subject:encodeBody()");
    if (this.subject != null) {
        return this.subject;
    } else {
        return "";
    }
}

Subject.prototype.setSubject =function(subject){
    if(logger!=undefined) logger.debug("Subject:setSubject():subject="+subject);
    if (subject == null)
    {
        console.error("Subject:setSubject(): null arg subject");
        throw "Subject:setSubject(): null arg subject"; 
    }
    this.subject = subject;
}

Subject.prototype.getSubject =function(){
    if(logger!=undefined) logger.debug("Subject:getSubject()");
    return this.subject;
}