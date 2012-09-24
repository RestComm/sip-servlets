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
 *  Implementation of the JAIN-SIP ParserCore class.
 *  @see  gov/nist/core/ParserCore.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */

function ParserCore() {
    //if(logger!=undefined) logger.debug("ParserCore:ParserCore()");
    this.classname="ParserCore";
    this.nesting_level=null;
    this.lexer=new LexerCore();
}

ParserCore.prototype.nameValue =function(separator){
    //if(logger!=undefined) logger.debug("ParserCore:nameValue():separator="+separator);
    if(separator==null)
    {
        var nv=this.nameValue("=")
        return nv;
    }
    else
    {
        this.lexer.match(LexerCore.prototype.ID);
        var name = this.lexer.getNextToken();
        this.lexer.SPorHT();
        try {
            var quoted = false;
            var la = this.lexer.lookAhead(0);
            if (la == separator) {
                this.lexer.consume(1);
                this.lexer.SPorHT();
                var str = null;
                var isFlag = false;
                if (this.lexer.lookAhead(0) == '\"') {
                    str = this.lexer.quotedString();
                    quoted = true;
                } else {
                    this.lexer.match(LexerCore.prototype.ID);
                    var value = this.lexer.getNextToken();
                    str = value.tokenValue;
                    if (str == null) {
                        str = "";
                        isFlag = true;
                    }
                }
                var nv = new NameValue(name.tokenValue, str, isFlag);
                if (quoted) {
                    nv.setQuotedValue();
                }
                return nv;
            } else {
                nv=new NameValue(name.tokenValue, "", true);
                return nv;
            }
        } catch (ex) {    
            console.error("ParserCore:nameValue(): catched exception:"+ex);
            nv=new NameValue(name.tokenValue, null, false);
            return nv;
        }
    }
    return nv;
}