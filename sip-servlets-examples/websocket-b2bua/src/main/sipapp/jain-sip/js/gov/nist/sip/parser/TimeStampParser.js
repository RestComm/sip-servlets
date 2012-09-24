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
 *  Implementation of the JAIN-SIP TimeStampParser .
 *  @see  gov/nist/javax/sip/parser/TimeStampParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function TimeStampParser() {
    if(logger!=undefined) logger.debug("TimeStampParser:TimeStampParser()");
    this.classname="TimeStampParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var timeStamp=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", timeStamp);
    }
}

TimeStampParser.prototype = new HeaderParser();
TimeStampParser.prototype.constructor=TimeStampParser;
TimeStampParser.prototype.TIMESTAMP="Timestamp";

TimeStampParser.prototype.parse =function(){
    if(logger!=undefined) logger.debug("TimeStampParser:parse()");
    var timeStamp = new TimeStamp();
    this.headerName(TokenTypes.prototype.TIMESTAMP);
    timeStamp.setHeaderName(this.TIMESTAMP);
    this.lexer.SPorHT();
    var firstNumber = this.lexer.number();
    if (this.lexer.lookAhead(0) == '.') {
        this.lexer.match('.');
        var secondNumber = this.lexer.number();

        var s = firstNumber + "." + secondNumber;
        var ts = s;
        timeStamp.setTimeStamp(ts);
    } else {
        ts = firstNumber;
        timeStamp.setTime(ts);
    }

    this.lexer.SPorHT();
    if (this.lexer.lookAhead(0) != '\n') 
    {
        firstNumber = this.lexer.number();
    }
    if (this.lexer.lookAhead(0) == '.') {
        this.lexer.match('.');
        secondNumber = this.lexer.number();
        s = firstNumber + "." + secondNumber;
        ts = s;
        timeStamp.setDelay(ts);
    } else {
        ts = firstNumber;
        timeStamp.setDelay(ts);
    }
    return timeStamp;
}


