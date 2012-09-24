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
 *  Implementation of the JAIN-SIP Token class.
 *  @see  gov/nist/core/Token.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Token() {
    //if(logger!=undefined) logger.debug("Token:Token()");
    this.classname="Token";
    this.tokenValue=null;
    this.tokenType=null;
}

Token.prototype.getTokenValue =function(){
    //if(logger!=undefined) logger.debug("Token:getTokenValue()");
    return this.tokenValue;
}

Token.prototype.getTokenType =function(){
    //if(logger!=undefined) logger.debug("Token:getTokenType()");
    return this.tokenType;
}

Token.prototype.toString =function(){
    //if(logger!=undefined) logger.debug("Token:toString()");
    return "tokenValue = " + this.tokenValue + "/tokenType = " + this.tokenType;
}

