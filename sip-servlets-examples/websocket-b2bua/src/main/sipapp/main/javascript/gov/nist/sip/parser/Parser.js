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
 *  Implementation of the JAIN-SIP Parser .
 *  @see  gov/nist/javax/sip/parser/Parser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function Parser() {
    if(logger!=undefined) logger.debug("Parser:Parser()");
    this.classname="Parser"; 
}

Parser.prototype = new ParserCore();
Parser.prototype.constructor=Parser;
Parser.prototype.INVITE=LexerCore.prototype.START+5;
Parser.prototype.ACK=LexerCore.prototype.START+6;
Parser.prototype.OPTIONS=LexerCore.prototype.START+8;
Parser.prototype.BYE=LexerCore.prototype.START+7;
Parser.prototype.REGISTER=LexerCore.prototype.START+4;
Parser.prototype.CANCEL=LexerCore.prototype.START+9;
Parser.prototype.SUBSCRIBE=LexerCore.prototype.START+53;
Parser.prototype.NOTIFY=LexerCore.prototype.START+54;
Parser.prototype.PUBLISH=LexerCore.prototype.START+67;
Parser.prototype.MESSAGE=LexerCore.prototype.START+70;
Parser.prototype.ID=LexerCore.prototype.ID;
Parser.prototype.SIP=LexerCore.prototype.START+3;

Parser.prototype.createParseException =function(){
    if(logger!=undefined) logger.debug("Parser:createParseException()"); 
}

Parser.prototype.getLexer =function(){
    if(logger!=undefined) logger.debug("Parser:getLexer()");
    return this.lexer;
}

Parser.prototype.sipVersion =function(){
    if(logger!=undefined) logger.debug("Parser:sipVersion()");
    var tok = this.lexer.match(this.SIP);
    if (tok.getTokenValue().toUpperCase()!="SIP") {
        this.createParseException("Expecting SIP");
    }
    this.lexer.match('/');
    tok = this.lexer.match(this.ID);
    if (tok.getTokenValue()!="2.0") {
        this.createParseException("Expecting SIP/2.0");
    }
    return "SIP/2.0";
    
}
Parser.prototype.method =function(){
    if(logger!=undefined) logger.debug("Parser:method()");
    var tokens = this.lexer.peekNextToken(1);
    var token =  tokens[0];
    if (token.getTokenType() == this.INVITE
        || token.getTokenType() == this.ACK
        || token.getTokenType() == this.OPTIONS
        || token.getTokenType() == this.BYE
        || token.getTokenType() == this.REGISTER
        || token.getTokenType() == this.CANCEL
        || token.getTokenType() == this.SUBSCRIBE
        || token.getTokenType() == this.NOTIFY
        || token.getTokenType() == this.PUBLISH
        || token.getTokenType() == this.MESSAGE
        || token.getTokenType() == this.ID) {
        this.lexer.consume();
        return token.getTokenValue();
    } else {
        console.error("Parser:method(): invalid Method");
        throw "Parser:method(): invalid Method";
    }
   
}
Parser.prototype.checkToken =function(token){
    if(logger!=undefined) logger.debug("Parser:checkToken():token="+token);
    if (token == null || token.length == 0) {
        console.error("Parser:checkToken(): null or empty token");
        throw "Parser:method(): null or empty token";
    } else {
        for (var i = 0; i < token.length; ++i) {
            var lc=new LexerCore();
            if (!lc.isTokenChar(token.charAt(i))) {
                console.error("Parser:checkToken(): invalid character(s) in string (not allowed in 'token')",i);
                throw "Parser:method(): invalid character(s) in string (not allowed in 'token')";
            }
        }
    }
}