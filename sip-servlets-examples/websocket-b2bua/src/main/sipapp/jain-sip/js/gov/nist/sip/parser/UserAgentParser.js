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
 *  Implementation of the JAIN-SIP UserAgentParser .
 *  @see  gov/nist/javax/sip/parser/UserAgentParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function UserAgentParser() {
    if(logger!=undefined) logger.debug("UserAgentParser:UserAgentParser()");
    this.classname="UserAgentParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var userAgent=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", userAgent);
    }
}

UserAgentParser.prototype = new HeaderParser();
UserAgentParser.prototype.constructor=UserAgentParser;

UserAgentParser.prototype.parse =function(){
    if(logger!=undefined) logger.debug("UserAgentParser:parse()");
    var userAgent = new UserAgent();
    this.headerName(TokenTypes.prototype.USER_AGENT);
    if (this.lexer.lookAhead(0) == '\n')
    {
        console.error("UserAgentParser:parse(): empty header");
        throw "UserAgentParser:parse(): empty header";
    }
    while (this.lexer.lookAhead(0) != '\n'
        && this.lexer.lookAhead(0) != '\0') {
        if (this.lexer.lookAhead(0) == '(') {
            var comment = this.lexer.comment();
            userAgent.addProductToken('(' + comment + ')');
        } else {
            this.getLexer().SPorHT();
            var product = this.lexer.byteStringNoSlash();
            if ( product == null ) {
                console.error("UserAgentParser:parse(): expected product string");
                throw "UserAgentParser:parse():expected product string";
            }
            var productSb = product;
            if (this.lexer.peekNextToken().getTokenType() == TokenTypes.prototype.SLASH) {
                this.lexer.match(TokenTypes.prototype.SLASH);
                this.getLexer().SPorHT();
                var productVersion = this.lexer.byteStringNoSlash();
                if ( productVersion == null ) {
                    console.error("UserAgentParser:parse(): expected product version");
                    throw "UserAgentParser:parse(): expected product version";
                }
                productSb=productSb+"/"+productVersion;
            }
            userAgent.addProductToken(productSb.toString());
        }
        this.lexer.SPorHT();
    }
    return userAgent;
}

