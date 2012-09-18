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
 *  Implementation of the JAIN-SIP ReasonParser .
 *  @see  gov/nist/javax/sip/parser/ReasonParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function ReasonParser() {
    if(logger!=undefined) logger.debug("ReasonParser:ReasonParser()");
    this.classname="ReasonParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var reason=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", reason);
    }
}

ReasonParser.prototype = new ParametersParser();
ReasonParser.prototype.constructor=ReasonParser;

ReasonParser.prototype.parse =function(){
    if(logger!=undefined) logger.debug("ReasonParser:parse()");
    var reasonList = new ReasonList();
    this.headerName(TokenTypes.prototype.REASON);
    this.lexer.SPorHT();
    while (this.lexer.lookAhead(0) != '\n') {
        var reason = new Reason();
        this.lexer.match(TokenTypes.prototype.ID);
        var token = this.lexer.getNextToken();
        var value = token.getTokenValue();
        reason.setProtocol(value);
        ParametersParser.prototype.parse.call(this,reason);
        reasonList.add(reason);
        if (this.lexer.lookAhead(0) == ',') {
            this.lexer.match(',');
            this.lexer.SPorHT();
        } 
        else
        {
            this.lexer.SPorHT();
        }
        return reasonList;
    }
}

