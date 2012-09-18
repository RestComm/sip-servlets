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
 *  Implementation of the JAIN-SIP RequestLineParser .
 *  @see  gov/nist/javax/sip/parser/RequestLineParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function RequestLineParser() {
    if(logger!=undefined) logger.debug("RequestLineParser:RequestLineParser()");
    this.classname="RequestLineParser"; 
    if(typeof arguments[0]=="string")
    {
        var requestLine=arguments[0];
        this.lexer = new Lexer("method_keywordLexer", requestLine);
    }
    else if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("method_keywordLexer");
    }
}

RequestLineParser.prototype = new Parser();
RequestLineParser.prototype.constructor=RequestLineParser;

RequestLineParser.prototype.parse =function(){
    if(logger!=undefined) logger.debug("RequestLineParser:parse()");
    var retval = new RequestLine();
    var m = this.method();
    this.lexer.SPorHT();
    retval.setMethod(m);
    this.lexer.selectLexer("sip_urlLexer");
    var urlParser = new URLParser(this.getLexer());
    var url = urlParser.uriReference(true);
    this.lexer.SPorHT();
    retval.setUri(url);
    this.lexer.selectLexer("request_lineLexer");
    var v = this.sipVersion();
    retval.setSipVersion(v);
    this.lexer.SPorHT();
    this.lexer.match('\n');
    return retval;
}