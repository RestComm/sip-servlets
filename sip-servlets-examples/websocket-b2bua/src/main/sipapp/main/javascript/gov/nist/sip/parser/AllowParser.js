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
 *  Implementation of the JAIN-SIP AllowParser .
 *  @see  gov/nist/javax/sip/parser/AllowParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function AllowParser() {
    if(logger!=undefined) logger.debug("AllowParser:AllowParser()");
    this.classname="AllowParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var buffer=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", buffer);
    }
}

AllowParser.prototype = new HeaderParser();
AllowParser.prototype.constructor=AllowParser;
AllowParser.prototype.ALLOW="Allow";

AllowParser.prototype.parse =function(){
    if(logger!=undefined) logger.debug("AllowParser:parse()");
    var list = new AllowList();
    this.headerName(TokenTypes.prototype.ALLOW);
    var allow = new Allow();
    allow.setHeaderName(this.ALLOW);
    this.lexer.SPorHT();
    this.lexer.match(TokenTypes.prototype.ID);
    var token = this.lexer.getNextToken();
    allow.setMethod(token.getTokenValue());
    list.add(allow);
    this.lexer.SPorHT();
    while (this.lexer.lookAhead(0) == ',') {
        this.lexer.match(',');
        this.lexer.SPorHT();
        allow = new Allow();
        this.lexer.match(TokenTypes.prototype.ID);
        token = this.lexer.getNextToken();
        allow.setMethod(token.getTokenValue());
        list.add(allow);
        this.lexer.SPorHT();
    }
    this.lexer.SPorHT();
    this.lexer.match('\n');
    return list;
}