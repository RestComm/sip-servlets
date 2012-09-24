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
 *  Implementation of the JAIN-SIP LexerCore class.
 *  @see  gov/nist/core/LexerCore.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *  @todo catch exception 
 */
function LexerCore() {
    if(logger!=undefined) logger.debug("LexerCore:LexerCore()");
    this.classname="LexerCore";
    this.globalSymbolTable=new Array();
    this.lexerTables=new Array();
    this.currentLexer=null;
    this.currentLexerName=null;
    this.currentMatch=new Token();
    if(arguments.length==0)
    {
        this.currentLexer = new Array();
        this.currentLexerName = "charLexer";
    }
    else
    {
        var lexerName=arguments[0];
        var buffer=arguments[1];
        this.buffer = buffer;
        this.bufferLen = buffer.length;
        this.ptr = 0;
        this.currentLexer = new Array();
        this.currentLexerName = lexerName;
    }
}

LexerCore.prototype = new StringTokenizer();
LexerCore.prototype.constructor=LexerCore;
LexerCore.prototype.START = 2048;
LexerCore.prototype.END = LexerCore.prototype.START + 2048;
LexerCore.prototype.ID = LexerCore.prototype.END - 1;
LexerCore.prototype.SAFE = LexerCore.prototype.END - 2;
LexerCore.prototype.WHITESPACE = LexerCore.prototype.END + 1;
LexerCore.prototype.DIGIT = LexerCore.prototype.END + 2;
LexerCore.prototype.ALPHA = LexerCore.prototype.END + 3;
LexerCore.prototype.BACKSLASH = "\\".charCodeAt(0);
LexerCore.prototype.QUOTE = "\'".charCodeAt(0);
LexerCore.prototype.AT = "@".charCodeAt(0);
LexerCore.prototype.SP = " ".charCodeAt(0);
LexerCore.prototype.HT = "\t".charCodeAt(0);
LexerCore.prototype.COLON = ":".charCodeAt(0);
LexerCore.prototype.STAR = "*".charCodeAt(0);
LexerCore.prototype.DOLLAR = "$".charCodeAt(0);
LexerCore.prototype.PLUS = "+".charCodeAt(0);
LexerCore.prototype.POUND = "#".charCodeAt(0);
LexerCore.prototype.MINUS = "-".charCodeAt(0);
LexerCore.prototype.DOUBLEQUOTE = "\"".charCodeAt(0);
LexerCore.prototype.TILDE = "~".charCodeAt(0);
LexerCore.prototype.BACK_QUOTE = "`".charCodeAt(0);
LexerCore.prototype.NULL = "\0".charCodeAt(0);
LexerCore.prototype.EQUALS = "=".charCodeAt(0);
LexerCore.prototype.SEMICOLON = ";".charCodeAt(0);
LexerCore.prototype.SLASH = "/".charCodeAt(0);
LexerCore.prototype.L_SQUARE_BRACKET = "[".charCodeAt(0);
LexerCore.prototype.R_SQUARE_BRACKET = "]".charCodeAt(0);
LexerCore.prototype.R_CURLY = "}".charCodeAt(0);
LexerCore.prototype.L_CURLY = "{".charCodeAt(0);
LexerCore.prototype.HAT = "^".charCodeAt(0);
LexerCore.prototype.BAR = "|".charCodeAt(0);
LexerCore.prototype.DOT = ".".charCodeAt(0);
LexerCore.prototype.EXCLAMATION = "!".charCodeAt(0);
LexerCore.prototype.LPAREN = "(".charCodeAt(0);
LexerCore.prototype.RPAREN = ")".charCodeAt(0);
LexerCore.prototype.GREATER_THAN = ">".charCodeAt(0);
LexerCore.prototype.LESS_THAN = "<".charCodeAt(0);
LexerCore.prototype.PERCENT = "%".charCodeAt(0);
LexerCore.prototype.QUESTION = "?".charCodeAt(0);
LexerCore.prototype.AND = "&".charCodeAt(0);
LexerCore.prototype.UNDERSCORE = "_".charCodeAt(0);
LexerCore.prototype.ALPHA_VALID_CHARS = String.fromCharCode(65535);
LexerCore.prototype.DIGIT_VALID_CHARS = String.fromCharCode(65534);
LexerCore.prototype.ALPHADIGIT_VALID_CHARS = String.fromCharCode(65533);

LexerCore.prototype.addKeyword =function(name, value){
    //if(logger!=undefined) logger.debug("LexerCore:addKeyword():name="+name+", value="+value);
    var val = value;
    this.currentLexer=this.put(this.currentLexer, name, val);
    var j=null
    for(var i=0;i<this.globalSymbolTable.length;i++)
    {
        if(this.globalSymbolTable[i]==val)
        {
            j=0;
        }
    }
    if(j==null)
    {
        this.globalSymbolTable=this.put(this.globalSymbolTable, val, name);
    }
}

LexerCore.prototype.lookupToken =function(value){
    //if(logger!=undefined) logger.debug("LexerCore:lookupToken():value="+value);
    var string=null;
    if (value > this.START) {
        for(var i=0;i<this.globalSymbolTable.length;i++)
        {
            if(this.globalSymbolTable[i][0]==value)
            {
                string=this.globalSymbolTable[i][1];    
            }
        }
        return string;
    } 
    else 
    {
        return value;
    }
}

LexerCore.prototype.addLexer =function(lexerName){
    //if(logger!=undefined) logger.debug("LexerCore:addLexer():lexerName="+lexerName);
    var v=null;
    for(var i=0;i<this.lexerTables.length;i++)
    {
        if(this.lexerTables[i][0]==lexerName)
        {
            v=this.lexerTables[i][1];    
        }
    }
    this.currentLexer = v;
    if (this.currentLexer == null) {
        this.currentLexer = new Array();
        this.lexerTables=this.put(this.lexerTables, lexerName, this.currentLexer);
    }
    return this.currentLexer;
}

LexerCore.prototype.selectLexer =function(lexerName){
    //if(logger!=undefined) logger.debug("LexerCore:selectLexer():lexerName="+lexerName);
    this.currentLexerName = lexerName;
}

/**
 * Peek the next id but dont move the buffer pointer forward.
 */
LexerCore.prototype.peekNextId =function(){
    //if(logger!=undefined) logger.debug("LexerCore:peekNextId()");
    var oldPtr = this.ptr;
    var retval = this.ttoken();
    this.savedPtr = this.ptr;
    this.ptr = oldPtr;
    return retval;
}

LexerCore.prototype.getNextId =function(){
    //if(logger!=undefined) logger.debug("LexerCore:getNextId()");
    return this.ttoken();
}

LexerCore.prototype.getNextToken =function(){
    //if(logger!=undefined) logger.debug("LexerCore:getNextToken()");
    if(arguments.length!=0)
    {
        var delim=arguments[0];
        var startIdx = this.ptr;
        while (true) {
            var la = this.lookAhead(0);
            if (la == delim) 
            {
                break;
            } 
            else if (la == '\0') 
            {
                console.error("LexerCore:getNextToken(): EOL reached");
                throw "LexerCore:getNextToken(): EOL reached";
            }
            this.consume(1);
        }
        return this.buffer.substring(startIdx, this.ptr);
    }
    else
    {
        return this.currentMatch;
    }
}

LexerCore.prototype.peekNextToken =function(ntokens){
    //if(logger!=undefined) logger.debug("LexerCore:peekNextToken():ntokens="+ntokens);
    if(ntokens==null)
    {
        ntokens=1;
        return this.peekNextToken(ntokens)[0];
    }
    else
    {
        var old = this.ptr;
        var retval = new Array();
        for(var i=0;i<ntokens;i++)
        {
            var tok = new Token();
            if (this.startsId()) {
                var id = this.ttoken();
                tok.tokenValue = id;
                var idUppercase = id.toUpperCase();
                var j=null
                for(var l=0;l<this.currentLexer.length;l++)
                {
                    if(this.currentLexer[l][0]==idUppercase){
                        j=l;
                    }
                }
                if (j!=null) {
                    var type = this.currentLexer[j][1];
                    tok.tokenType = type;
                } else {
                    tok.tokenType = this.ID;
                }
            } else {
                var nextChar = this.getNextChar();
                tok.tokenValue = nextChar;
                if (this.isAlpha(nextChar)) {
                    tok.tokenType = this.ALPHA;
                } else if (this.isDigit(nextChar)) {
                    tok.tokenType = this.DIGIT;
                } else {
                    tok.tokenType = nextChar;
                }
            }
            retval[i] = tok;
        }
        this.savedPtr = this.ptr;
        this.ptr = old;
        return retval;
    }
}

LexerCore.prototype.match =function(tok){
    //if(logger!=undefined) logger.debug("LexerCore:match():tok="+tok);

    if (tok > this.START && tok < this.END) {
        if (tok == this.ID) {
            if (!this.startsId()) {
                console.error("LexerCore:match(): "+ this.buffer + "\nID expected", this.ptr);
                throw "LexerCore:match(): ID expected";
            }
            var id = this.getNextId();
            this.currentMatch = new Token();
            this.currentMatch.tokenValue = id;
            this.currentMatch.tokenType = this.ID;
        } else if (tok == this.SAFE) {
            if (!this.startsSafeToken()) {
                console.error("LexerCore:match(): "+ this.buffer + "\nID expected", this.ptr);
                throw "LexerCore:match(): ID expected";
            }
            id = this.ttokenSafe();
            this.currentMatch = new Token();
            this.currentMatch.tokenValue = id;
            this.currentMatch.tokenType = this.SAFE;
        } else {
            var nexttok = this.getNextId();
            var n=null;
            for(var i=0;i<this.currentLexer.length;i++)
            {
                if(this.currentLexer[i][0]==nexttok.toUpperCase())
                {
                    n=i;     
                }
            }
            if(n==null)
            {
                var cur=null;
            }
            else
            {
                cur =  this.currentLexer[n][1];
            }
            
            if (cur == null || cur != tok) {
                console.error("LexerCore:match(): "+this.buffer + "\nUnexpected Token : " + nexttok);
                throw "LexerCore:match(): unexpected Token";
            }
            this.currentMatch = new Token();
            this.currentMatch.tokenValue = nexttok;
            this.currentMatch.tokenType = tok;
        }
    }else if (tok > this.END) {
        var next = this.lookAhead(0);
        if (tok == this.DIGIT) {
            if (!this.isDigit(next)) {
                console.error("LexerCore:match(): "+ this.buffer + "\nExpecting DIGIT", this.ptr);
                throw "LexerCore:match(): Expecting DIGIT";
            }
            this.currentMatch = new Token();
            this.currentMatch.tokenValue =next;
            this.currentMatch.tokenType = tok;
            this.consume(1);
        
        } else if (tok == this.ALPHA) {
            if (!this.isAlpha(next)) {
                console.error("LexerCore:match(): "+ this.buffer + "\nExpecting ALPHA", this.ptr);
                throw "LexerCore:match(): Expecting ALPHA";
            }
            this.currentMatch = new Token();
            this.currentMatch.tokenValue =next;
            this.currentMatch.tokenType = tok;
            this.consume(1);
        }
    }else {
        var ch =  tok;
        next = this.lookAhead(0);
        if (next == ch) {
            this.consume(1);
        } else {
            console.error("LexerCore:match(): "+ this.buffer + "\nExpecting  >>>" + ch + "<<< got >>>"+ next + "<<<");
            throw "LexerCore:match(): expecting  >>>" + ch + "<<< got >>>"+ next + "<<<";
        }
    }
    return this.currentMatch;
}

LexerCore.prototype.SPorHT =function(){
    //if(logger!=undefined) logger.debug("LexerCore:SPorHT()");
    var c = this.lookAhead(0);
    while (c == ' ' || c == '\t') {
        this.consume(1);
        c = this.lookAhead(0);
    }
}

LexerCore.prototype.isTokenChar =function(c){
    //if(logger!=undefined) logger.debug("LexerCore:():c="+c);
    if (this.isAlphaDigit(c)) {
        return true;
    } else {
        switch (c) {
            case "-":
            case ".":
            case "!":
            case "%":
            case "*":
            case "_":
            case "+":
            case "`":
            case "\'":
            case "~":
                return true;
            default:
                return false;
        }
    }
}

LexerCore.prototype.startsId =function(){
    //if(logger!=undefined) logger.debug("LexerCore:startsId()");
    var nextChar = this.lookAhead(0);
    return this.isTokenChar(nextChar);
}

LexerCore.prototype.startsSafeToken =function(){
    //if(logger!=undefined) logger.debug("LexerCore:startsSafeToken()");
    var nextChar = lookAhead(0);
    if (this.isAlphaDigit(nextChar)) {
        return true;
    } else {
        switch (nextChar) {
            case '_':
            case '+':
            case '-':
            case '!':
            case '`':
            case '\'':
            case '.':
            case '/':
            case '}':
            case '{':
            case ']':
            case '[':
            case '^':
            case '|':
            case '~':
            case '%': // bug fix by Bruno Konik, JvB copied here
            case '#':
            case '@':
            case '$':
            case ':':
            case ';':
            case '?':
            case '\"':
            case '*':
            case '=': // Issue 155 on java.net
                return true;
            default:
                return false;
        }
    }
}
LexerCore.prototype.ttoken =function(){
    //if(logger!=undefined) logger.debug("LexerCore:ttoken()");
    var startIdx = this.ptr;
    while (this.hasMoreChars()) {
        var nextChar = this.lookAhead(0);
        if (this.isTokenChar(nextChar)) {
            this.consume(1);
        } else {
            break;
        }
    }
    return this.buffer.substring(startIdx, this.ptr);
}

LexerCore.prototype.ttokenSafe =function(){
    //if(logger!=undefined) logger.debug("LexerCore:ttokenSafe()");
    var startIdx = this.ptr;
    while (this.hasMoreChars()) {
        var nextChar = this.lookAhead(0);
        if (this.isAlphaDigit(nextChar)) {
            this.consume(1);
        } else {
            var isValidChar = false;
            switch (nextChar) {
                case '_':
                case '+':
                case '-':
                case '!':
                case '`':
                case '\'':
                case '.':
                case '/':
                case '}':
                case '{':
                case ']':
                case '[':
                case '^':
                case '|':
                case '~':
                case '%': // bug fix by Bruno Konik, JvB copied here
                case '#':
                case '@':
                case '$':
                case ':':
                case ';':
                case '?':
                case '\"':
                case '*':
                    isValidChar = true;
            }
            if (isValidChar) {
                this.consume(1);
            } else {
                break;
            }
        }
    }
    return this.buffer.substring(startIdx, this.ptr);
}

LexerCore.prototype.consumeValidChars =function(validChars){
    //if(logger!=undefined) logger.debug("LexerCore:consumeValidChars():validChars="+validChars.toString());
    var validCharsLength = validChars.length;
    while (this.hasMoreChars()) {
        var nextChar = this.lookAhead(0);
        var isValid = false;
        for (var i = 0; i < validCharsLength; i++) {
            var validChar = validChars[i];
            switch (validChar) {
                case this.ALPHA_VALID_CHARS:
                    isValid = this.isAlpha(nextChar);
                    break;
                case this.DIGIT_VALID_CHARS:
                    isValid = this.isDigit(nextChar);
                    break;
                case this.ALPHADIGIT_VALID_CHARS:
                    isValid = this.isAlphaDigit(nextChar);
                    break;
                default:
                    isValid = nextChar == validChar;
            }
            if (isValid) {
                break;
            }
        }
        if (isValid) {
            this.consume(1);
        } else {
            break;
        }
    }
}

LexerCore.prototype.quotedString =function(){
    //if(logger!=undefined) logger.debug("LexerCore:quotedString()");
    var startIdx = this.ptr + 1;
    if (this.lookAhead(0) != '\"') {
        return null;
    }
    this.consume(1);
    while (true) {
        var next = this.getNextChar();
        if (next == '\"') {
            break;
        } else if (next == '\0') {
            console.error("LexerCore:quotedString(): "+ this.buffer + " :unexpected EOL",this.ptr);
            throw "LexerCore:quotedString(): unexpected EOL";
        } else if (next == '\\') {
            this.consume(1);
        }
    }
    return this.buffer.substring(startIdx, this.ptr - 1);
}

LexerCore.prototype.comment =function(){
    //if(logger!=undefined) logger.debug("LexerCore:comment()");
    var retval = "";
    if (this.lookAhead(0) != '(') {
        return null;
    }
    this.consume(1);
    while (true) {
        var next = this.getNextChar();
        if (next == ')') {
            break;
        } else if (next == '\0') {
            console.error("LexerCore:comment(): "+ this.buffer + " :unexpected EOL",this.ptr);
            throw "LexerCore:comment(): unexpected EOL";
        } else if (next == '\\') {
            retval=retval+next;
            next = this.getNextChar();
            if (next == '\0') {
                console.error("LexerCore:comment(): "+ this.buffer + " :unexpected EOL",this.ptr);
                throw "LexerCore:comment(): unexpected EOL";
            }
            retval=retval+next;
        } else {
            retval=retval+next;
        }
    }
    return retval.toString();
}

LexerCore.prototype.byteStringNoSemicolon =function(){
    //if(logger!=undefined) logger.debug("LexerCore:byteStringNoSemicolon()");
    var retval = "";
    while (true) {
        var next = this.lookAhead(0);
        if (next == '\0' || next == '\n' || next == ';' || next == ',') {
            break;
        } else {
            this.consume(1);
            retval=retval+next;
        }
    }
    return retval.toString();
}

LexerCore.prototype.byteStringNoSlash =function(){
    //if(logger!=undefined) logger.debug("LexerCore:byteStringNoSlash()");
    var retval = "";
    while (true) {
        var next = this.lookAhead(0);
        if (next == '\0' || next == '\n' || next == '/') {
            break;
        } else {
            this.consume(1);
            retval=retval+next;
        }
    }
    return retval.toString();
}

LexerCore.prototype.byteStringNoComma =function(){
    //if(logger!=undefined) logger.debug("LexerCore:byteStringNoComma()");
    var retval = "";
    while (true) {
        var next = this.lookAhead(0);
        if (next == '\n' || next == ',') {
            break;
        } else {
            this.consume(1);
            retval=retval+next;
        }
    }
    return retval.toString();
}

LexerCore.prototype.charAsString =function(){
    //if(logger!=undefined) logger.debug("LexerCore:charAsString():arguments="+nchars);
    if(typeof arguments[0]=="string")
    {
        var ch=arguments[0];
        return ch;
    }
    else if(typeof arguments[0]=="number")
    {
        var nchars=arguments[0];
        return this.buffer.substring(this.ptr, this.ptr + nchars);
    }
}

LexerCore.prototype.number =function(){
    //if(logger!=undefined) logger.debug("LexerCore:number()");
    var startIdx = this.ptr;
    if (!this.isDigit(this.lookAhead(0))) {
        console.error(this.buffer + "LexerCore:number(): Unexpected token at " + this.lookAhead(0),this.ptr);
        throw "LexerCore:number(): Unexpected token at " + this.lookAhead(0);
    }
    this.consume(1);
    while (true) {
        var next = this.lookAhead(0);
        if (this.isDigit(next)) {
            this.consume(1);
        } else {
            break;
        }
    }
    return this.buffer.substring(startIdx, this.ptr);
}

LexerCore.prototype.markInputPosition =function(){
    //if(logger!=undefined) logger.debug("LexerCore:markInputPosition()");
    return this.ptr;
}

LexerCore.prototype.rewindInputPosition =function(position){
    //if(logger!=undefined) logger.debug("LexerCore:rewindInputPosition():position="+position);
    this.ptr = position;
}

LexerCore.prototype.getRest =function(){
    //if(logger!=undefined) logger.debug("LexerCore:getRest()");
    if (this.ptr >= this.buffer.length) {
        return null;
    } else {
        return this.buffer.substring(this.ptr);
    }
}

LexerCore.prototype.getString =function(c){
    //if(logger!=undefined) logger.debug("LexerCore:getString():c:"+c);
    var retval = "";
    while (true) {
        var next = this.lookAhead(0);
        if (next == '\0') {
           console.error(this.buffer + "LexerCore:getString(): unexpected EOL",this.ptr);
           throw "LexerCore:getString(): unexpected EOL";
        } else if (next == c) {
            this.consume(1);
            break;
        } else if (next == '\\') {
            this.consume(1);
            var nextchar = this.lookAhead(0);
            if (nextchar == '\0') {
                console.error(this.buffer + "LexerCore:getString(): unexpected EOL",this.ptr);
                throw "LexerCore:getString(): unexpected EOL";
            } else {
                this.consume(1);
                retval=retval+nextchar;
            }
        } else {
            this.consume(1);
            retval=retval+next;
        }
    }
    return retval.toString();
}

LexerCore.prototype.getPtr =function(){
    //if(logger!=undefined) logger.debug("LexerCore:getPtr()");
    return this.ptr;
}

LexerCore.prototype.getBuffer =function(){
    //if(logger!=undefined) logger.debug("LexerCore:getBuffer()");
    return this.buffer;
}

LexerCore.prototype.put =function(table,name, value){
    //if(logger!=undefined) logger.debug("LexerCore:put(): table="+table.toString());
    //if(logger!=undefined) logger.debug("LexerCore:put(): name="+name);
    //if(logger!=undefined) logger.debug("LexerCore:put(): value="+value);
    var n=0;
    for(var i=0;i<table.length;i++)// loop for method put() of hashtable
    {
        var key = table[i][0];
        if (key==name) {
            n=1;
            var x=new Array();
            x[0]=key;
            x[1]=value;
            table[i]=x;
        } 
    }
    if(n==0)
    {
        x=new Array();
        x[0]=name;
        x[1]=value;
        table.push(x);
    }
    return table;
}