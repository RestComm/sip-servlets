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
 *  Implementation of the JAIN-SIP StringTokenizer class.
 *  @see  gov/nist/core/StringTokenizer.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function StringTokenizer(buffer) {
    //if(logger!=undefined) logger.debug("StringTokenizer:StringTokenizer()");
    this.classname="StringTokenizer";
    this.buffer=null;
    this.bufferLen=null;
    this.ptr=null;
    this.savedPtr=null;
    if(buffer!=null)
    {
        this.buffer = buffer;
        this.bufferLen = buffer.length;
        this.ptr = 0;
    }
}

StringTokenizer.prototype.nextToken =function(){
    //if(logger!=undefined) logger.debug("StringTokenizer:nextToken()");
    var startIdx = this.ptr;
    while (this.ptr < this.bufferLen) {
        var c = this.buffer.charAt(this.ptr);
        this.ptr++;
        if (c == '\n') {
            break;
        }
    }
    return this.buffer.substring(startIdx, this.ptr);
}

StringTokenizer.prototype.hasMoreChars =function(){
    //if(logger!=undefined) logger.debug("StringTokenizer:hasMoreChars()");
    if(this.ptr < this.bufferLen&&this.buffer.charAt(this.ptr)!='\r')
    {
        return true;
    }
    else
    {
        return false;
    }
}

StringTokenizer.prototype.isHexDigit =function(ch){
    //if(logger!=undefined) logger.debug("StringTokenizer:isHexDigit():ch="+ch);
    if((ch >= "A" && ch <= "F")
        || (ch >= "a" && ch <= "f")
        || this.isDigit(ch))
        {
        return true;
    }
    else
    {
        return false;
    }
}

StringTokenizer.prototype.isAlpha =function(ch){
    //if(logger!=undefined) logger.debug("StringTokenizer:isAlpha():ch="+ch);
    if(ch.charCodeAt(0) <= 127)
    {
        if((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z'))

        {
            return true
        }
        else
        {
            return false;
        }
    }
    else
    {
        if(ch==ch.toLowerCase()||ch==ch.toUpperCase())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

StringTokenizer.prototype.isDigit =function(ch){
    //if(logger!=undefined) logger.debug("StringTokenizer:isDigit():ch="+ch);
    if(ch.charCodeAt(0) <= 127)
    {
        if(ch <= '9' && ch >= '0')

        {
            return true
        }
        else
        {
            return false;
        }
    }
    else
    {
        if(typeof ch=="number")
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

StringTokenizer.prototype.isAlphaDigit =function(ch){
    //if(logger!=undefined) logger.debug("StringTokenizer:isAlphaDigit():ch="+ch);
    if(ch.charCodeAt(0) <= 127)
    {
        if((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')||(ch <= '9' && ch >= '0'))

        {
            return true
        }
        else
        {
            return false;
        }
    }
    else
    {
        if((ch==ch.toLowerCase())||(ch==ch.toUpperCase())||(typeof ch=="number"))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}

StringTokenizer.prototype.getLine =function(){
    //if(logger!=undefined) logger.debug("StringTokenizer:getLine()");
    var startIdx = this.ptr;
    while (this.ptr < this.bufferLen && this.buffer.charAt(this.ptr) != '\n') {
        this.ptr++;
    }
    if (this.ptr < this.bufferLen && this.buffer.charAt(this.ptr) == '\n') {
        this.ptr++;
    }
    return this.buffer.substring(startIdx, this.ptr);
}

StringTokenizer.prototype.peekLine =function(){
    //if(logger!=undefined) logger.debug("StringTokenizer:peekLine()");
    var curPos = this.ptr;
    var retval = this.getLine();
    this.ptr = curPos;
    return retval;
}

StringTokenizer.prototype.lookAhead =function(k){
    //if(logger!=undefined) logger.debug("StringTokenizer:lookAhead():k="+k);
    if(k==null)
    {
        k=0;
    }
    return this.buffer.charAt(this.ptr + k);
}

StringTokenizer.prototype.getNextChar =function(){
    //if(logger!=undefined) logger.debug("StringTokenizer:getNextChar()");
    if (this.ptr >= this.bufferLen) {
        console.error("StringTokenizer:getNextChar(): end of buffer:"+this.ptr);
        throw "StringTokenizer:getNextChar(): end of buffer";
    } 
    else 
    {
        return this.buffer.charAt(this.ptr++);
    }
}

StringTokenizer.prototype.consume =function(k){
    //if(logger!=undefined) logger.debug("StringTokenizer:consume():k="+k);
    if(k==null)
    {
        this.ptr = this.savedPtr;
    }
    else
    {
        this.ptr += k;
    }
}

StringTokenizer.prototype.getLines =function(){
    //if(logger!=undefined) logger.debug("StringTokenizer:getLines()");
    var result = new Array();
    while (this.hasMoreChars()) {
        var line = this.getLine();
        result.push(line);
    }
    return result;
}

StringTokenizer.prototype.getNextToken =function(delim){
    //if(logger!=undefined) logger.debug("StringTokenizer:getNextToken():delim="+delim);
    var startIdx = this.ptr;
    while (true) {
        var la = this.lookAhead(0);
        if (la == delim) 
        {
            break;
        } 
        else if (la == '\0') 
        {
            console.error("StringTokenizer:getNextToken(): EOL reached");
            throw "StringTokenizer:getNextToken(): EOL reached";
        }
        this.consume(1);
    }
    return this.buffer.substring(startIdx, this.ptr);
}

StringTokenizer.prototype.getSDPFieldName =function(line){
    //if(logger!=undefined) logger.debug("StringTokenizer:getSDPFieldName():line="+line);
    if (line == null) {
        return null;
    }
    var fieldName = null;
    var begin = line.indexOf("=");
    fieldName = line.substring(0, begin);
    return fieldName;
}


