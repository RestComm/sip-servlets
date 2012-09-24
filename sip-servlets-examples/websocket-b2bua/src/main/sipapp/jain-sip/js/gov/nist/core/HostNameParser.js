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
 *  Implementation of the JAIN-SIP HostNameParser class.
 *  @see  gov/nist/core/HostNameParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function HostNameParser() {
    //if(logger!=undefined) logger.debug("HostNameParser:HostNameParser()");
    this.classname="HostNameParser"; 
    this.Lexer=null;
    this.stripAddressScopeZones = false;
    if(typeof arguments[0]=="string")
    {
        var hname=arguments[0];
        this.lexer = new LexerCore("charLexer", hname);
    }
    else if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("charLexer");
    }
}

HostNameParser.prototype = new ParserCore();
HostNameParser.prototype.constructor=HostNameParser;
HostNameParser.prototype.VALID_DOMAIN_LABEL_CHAR=[LexerCore.prototype.ALPHADIGIT_VALID_CHARS, '-', '.'];

HostNameParser.prototype.consumeDomainLabel =function(){
    //if(logger!=undefined) logger.debug("Parser:consumeDomainLabel()");
    this.lexer.consumeValidChars(this.VALID_DOMAIN_LABEL_CHAR);
}

//ipv6 is not used
HostNameParser.prototype.ipv6Reference =function(){
    //if(logger!=undefined) logger.debug("Parser:ipv6Reference()");   
}

HostNameParser.prototype.host =function(){
    //if(logger!=undefined) logger.debug("Parser:host()");
    var hostname;
    //IPv6 referene
    if (this.lexer.lookAhead(0) == '[') {
        hostname = this.ipv6Reference();
    }
    //IPv6 address (i.e. missing square brackets)
    /*else if( isIPv6Address(lexer.getRest()) )
            {
                var startPtr = lexer.getPtr();
                lexer.consumeValidChars([LexerCore.ALPHADIGIT_VALID_CHARS, ':']);
                hostname
                    = new StringBuffer("[").append(
                        lexer.getBuffer().substring(startPtr, lexer.getPtr()))
                        .append("]").toString();
            }*///ignore the parte of ipv6.
    //IPv4 address or hostname
    else {
        var startPtr = this.lexer.getPtr();
        this.consumeDomainLabel();
        hostname = this.lexer.getBuffer().substring(startPtr, this.lexer.getPtr());
    }
    if (hostname.length == 0)
    {
        console.error("Parser:host():"+ this.lexer.getBuffer() + " missing host name",this.lexer.getPtr());
        throw  "Parser:host(): Missing host name";   
    }
    else
    {
        return new Host(hostname);
    }
   
}


HostNameParser.prototype.isIPv6Address =function(){
    //if(logger!=undefined) logger.debug("Parser:isIPv6Address()");   
}

HostNameParser.prototype.hostPort =function(allowWS){
    //if(logger!=undefined) logger.debug("Parser:hostPort():allowWS="+allowWS);
    var host = this.host();
    var hp = new HostPort();
    hp.setHost(host);
    if (allowWS) {
        this.lexer.SPorHT();
    } 
    if (this.lexer.hasMoreChars()) {
        var la = this.lexer.lookAhead(0);
        switch (la)
        {
            case ':':
                this.lexer.consume(1);
                if (allowWS) {
                    this.lexer.SPorHT();
                } // white space before port number should be accepted
                try {
                    var port = this.lexer.number();
                    hp.setPort(port);
                } catch (nfe) {
                     console.error("Parser:hostPort():"+ this.lexer.getBuffer() + " : error parsing port ",this.lexer.getPtr());
                     throw  "Parser:hostPort(): error parsing port";   
                }
                break;
            case ',':
            case ';':   // OK, can appear in URIs (parameters)
            case '?':   // same, header parameters
            case '>':   // OK, can appear in headers
            case ' ':   // OK, allow whitespace
            case '\t':
            case '\r':
            case '\n':
            case '/':   // e.g. http://[::1]/xyz.html
                break;
            case '%':
                if(this.stripAddressScopeZones){
                    break;//OK,allow IPv6 address scope zone
                }
            default:
                if (!allowWS) {
                    console.error("Parser:hostPort(): "+ this.lexer.getBuffer() +" illegal character in hostname:" + this.lexer.lookAhead(0),this.lexer.getPtr() );
                    throw  "Parser:hostPort(): error parsing port"; 
                }
        }
    }
    return hp;
}