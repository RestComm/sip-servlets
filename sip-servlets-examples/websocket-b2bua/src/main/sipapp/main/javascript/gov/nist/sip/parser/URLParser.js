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
 *  Implementation of the JAIN-SIP URLParser .
 *  @see  gov/nist/javax/sip/parser/URLParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function URLParser() {
    if(logger!=undefined) logger.debug("URLParser:URLParser()");
    this.classname="URLParser"; 
    if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("sip_urlLexer");
    }
    else if(typeof arguments[0]=="string")
    {
        var url=arguments[0];
        this.lexer = new Lexer("sip_urlLexer", url);
    }
}

URLParser.prototype = new Parser();
URLParser.prototype.constructor=URLParser;
URLParser.prototype.PLUS=TokenTypes.prototype.PLUS;
URLParser.prototype.SEMICOLON=TokenTypes.prototype.SEMICOLON;

URLParser.prototype.isMark =function(next){
    if(logger!=undefined) logger.debug("URLParser:isMark():next="+next);
    switch (next) {
        case '-':
        case '_':
        case '.':
        case '!':
        case '~':
        case '*':
        case '\'':
        case '(':
        case ')':
            return true;
        default:
            return false;
    }
}

URLParser.prototype.isUnreserved =function(next){
    if(logger!=undefined) logger.debug("URLParser:isUnreserved():next="+next);
    var lexer=new Lexer("","");
    if(lexer.isAlphaDigit(next) || this.isMark(next))
    {
        return true;
    }
    else
    {
        return false;
    }
}

URLParser.prototype.isReservedNoSlash =function(next){
    if(logger!=undefined) logger.debug("URLParser:isReservedNoSlash():next="+next);
    switch (next) {
        case ';':
        case '?':
        case ':':
        case '@':
        case '&':
        case '+':
        case '$':
        case ',':
            return true;
        default:
            return false;
    }
}

URLParser.prototype.isUserUnreserved =function(la){
    if(logger!=undefined) logger.debug("URLParser:():la="+la);
    switch (la) {
        case '&':
        case '?':
        case '+':
        case '$':
        case '#':
        case '/':
        case ',':
        case ';':
        case '=':
            return true;
        default:
            return false;
    }
}

URLParser.prototype.unreserved =function(){
    if(logger!=undefined) logger.debug("URLParser:unreserved()");
    var next = this.lexer.lookAhead(0);
    if (this.isUnreserved(next)) {
        this.lexer.consume(1);
        return next;
    } else {
        console.error("URLParser:unreserved(): unreserved");
        throw "URLParser:unreserved(): unreserved";
    }
}

URLParser.prototype.paramNameOrValue =function(){
    if(logger!=undefined) logger.debug("URLParser:paramNameOrValue()");
    var startIdx = this.lexer.getPtr();
    while (this.lexer.hasMoreChars()) {
        var next = this.lexer.lookAhead(0);
        var isValidChar = false;
        switch (next) {
            case '[':
            case ']':// JvB: fixed this one
            case '/':
            case ':':
            case '&':
            case '+':
            case '$':
                isValidChar = true;
        }
        if (isValidChar || this.isUnreserved(next)) {
            this.lexer.consume(1);
        } else if (this.isEscaped()) {
            this.lexer.consume(3);
        } else {
            break;
        }
    }
    return this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
}

URLParser.prototype.uriParam =function(){
    if(logger!=undefined) logger.debug("URLParser:uriParam()");
    var pvalue = "";
    var pname = this.paramNameOrValue();
    var next = this.lexer.lookAhead(0);
    var isFlagParam = true;
    if (next == '=') {
        this.lexer.consume(1);
        pvalue = this.paramNameOrValue();
        isFlagParam = false;
    }
    if (pname.length == 0&& (pvalue == null|| pvalue.length == 0)) {
        return null;
    } else {
        return new NameValue(pname, pvalue, isFlagParam);
    }
   
}

URLParser.prototype.isReserved =function(next){
    if(logger!=undefined) logger.debug("URLParser:isReserved():next="+next);
    switch (next) {
        case ';':
        case '/':
        case '?':
        case ':':
        case '=': // Bug fix by Bruno Konik
        case '@':
        case '&':
        case '+':
        case '$':
        case ',':
            return true;
        default:
            return false;
    }
}

URLParser.prototype.reserved =function(){
    if(logger!=undefined) logger.debug("URLParser:reserved()");
    var next = this.lexer.lookAhead(0);
    if (this.isReserved(next)) {
        this.lexer.consume(1);
        var encode="";
        encode=(encode+next).toString();
        return encode;
    } else {
        console.error("URLParser:reserved(): reserved");
        throw "URLParser:reserved(): reserved";
    }
}

URLParser.prototype.isEscaped =function(){
    if(logger!=undefined) logger.debug("URLParser:isEscaped()");
    try {
        var lexer=new Lexer("","");
        if(this.lexer.lookAhead(0) == '%'
            && lexer.isHexDigit(this.lexer.lookAhead(1))
            && lexer.isHexDigit(this.lexer.lookAhead(2)))
            {
            return true;
        }
        else
        {
            return false;
        }
    } catch (ex) {
        console.error("URLParser:isEscaped(): catched exception:"+ex);
        return false;
    }
}

URLParser.prototype.escaped =function(){
    if(logger!=undefined) logger.debug("URLParser:escaped()");
    var lexer=new Lexer("","");
    var retval = "";
    var next = this.lexer.lookAhead(0);
    var next1 = this.lexer.lookAhead(1);
    var next2 = this.lexer.lookAhead(2);
    if (next == '%'
        && lexer.isHexDigit(next1)
        && lexer.isHexDigit(next2)) {
        this.lexer.consume(3);
        retval=retval+next+next1+next2;
    } else {
        console.error("URLParser:escaped(): escaped");
        throw "URLParser:escaped(): escaped";
    }
    return retval.toString();  
}

URLParser.prototype.mark =function(){
    if(logger!=undefined) logger.debug("URLParser:mark()");
    var next = this.lexer.lookAhead(0);
    if (this.isMark(next)) {
        this.lexer.consume(1);
        return next;
    } else {
        console.error("URLParser:mark(): marked");
        throw "URLParser:mark(): marked";
    } 
}

URLParser.prototype.uric =function(){
    if(logger!=undefined) logger.debug("URLParser:uric()");
    try {
        var lexer=new Lexer("","");
        var la = this.lexer.lookAhead(0);
        if (this.isUnreserved(la)) {
            this.lexer.consume(1);
            return lexer.charAsString(la);
        } else if (this.isReserved(la)) {
            this.lexer.consume(1);
            return lexer.charAsString(la);
        } else if (this.isEscaped()) {
            var retval = this.lexer.charAsString(3);
            this.lexer.consume(3);
            return retval;
        } else {
            return null;
        }
    } catch (ex) {
        console.error("URLParser:uric(): catched exception:"+ex);
        return null;
    }
}

URLParser.prototype.uricNoSlash =function(){
    if(logger!=undefined) logger.debug("URLParser:uricNoSlash()");
    try {
        var lexer=new Lexer("","");
        var la = this.lexer.lookAhead(0);
        if (this.isEscaped()) {
            var retval = this.lexer.charAsString(3);
            this.lexer.consume(3);
            return retval;
        } else if (this.isUnreserved(la)) {
            this.lexer.consume(1);
            return lexer.charAsString(la);
        } else if (this.isReservedNoSlash(la)) {
            this.lexer.consume(1);
            return lexer.charAsString(la);
        } else {
            return null;
        }
    } catch (ex) {
        console.error("URLParser:uricNoSlash(): catched exception:"+ex);
        return null;
    }
}

URLParser.prototype.uricString =function(){
    if(logger!=undefined) logger.debug("URLParser:uricString()");
    var retval = "";
    while (true) {
        var next = this.uric();
        if (next == null) {
            var la = this.lexer.lookAhead(0);
            if (la == '[') {
                var hnp = new HostNameParser(this.getLexer());
                var hp = hnp.hostPort(false);
                retval=retval+hp.toString();
                continue;
            }
            break;
        }
        retval=retval+next;
    }
    return retval.toString();
}

URLParser.prototype.uriReference =function(inBrackets){
    if(logger!=undefined) logger.debug("URLParser:uriReference():inBrackets:"+inBrackets);
    var retval = null;
    var tokens = this.lexer.peekNextToken(2);
    var t1 = tokens[0];
    var t2 = tokens[1];
    if (t1.getTokenType() == TokenTypes.prototype.SIP || t1.getTokenType() == TokenTypes.prototype.SIPS) {
        if (t2.getTokenType() == ':') {
            retval = this.sipURL(inBrackets);
        } else {
            console.error("URLParser:uriReference(): expecting \':\'");
            throw "URLParser:uriReference(): expecting \':\'";
        }
    } else if (t1.getTokenType() == TokenTypes.prototype.TEL) {
        if (t2.getTokenType() == ':') {
            retval = this.telURL(inBrackets);
        } else {
            console.error("URLParser:uriReference(): expecting \':\'");
            throw "URLParser:uriReference(): expecting \':\'";
        }
    } else {
        var urlString = this.uricString();
        try {
            retval = new GenericURI(urlString);
        } catch (ex) {
            console.error("URLParser:uriReference(): "+ex);
            throw "URLParser:uriReference(): "+ ex;
        }
    }
    return retval;
}

URLParser.prototype.base_phone_number =function(){
    if(logger!=undefined) logger.debug("URLParser:base_phone_number()");
    var s = "";
    var lexer=new Lexer("","");
    var lc = 0;
    while (this.lexer.hasMoreChars()) {
        var w = this.lexer.lookAhead(0);
        if (lexer.isDigit(w)|| w == '-'|| w == '.'|| w == '('|| w == ')') {
            this.lexer.consume(1);
            s=s+w;
            lc++;
        } else if (lc > 0) {
            break;
        } else {
            console.error("URLParser:base_phone_number(): unexpected"+w);
            throw "URLParser:base_phone_number(): unexpected"+w;
        }
    }
    return s.toString();
}

URLParser.prototype.local_number =function(){
    if(logger!=undefined) logger.debug("URLParser:local_number()");
    var s = "";
    var lexer=new Lexer("","");
    var lc = 0;
    while (this.lexer.hasMoreChars()) {
        var la = this.lexer.lookAhead(0);
        if (la == '*'|| la == '#'|| la == '-'|| la == '.'|| la == '('| la == ')'
            || lexer.isHexDigit(la)) {
            this.lexer.consume(1);
            s=s+la
            lc++;
        } else if (lc > 0) {
            break;
        } else {
            console.error("URLParser:local_number(): unexpected"+la);
            throw "URLParser:local_number(): unexpected"+la;
        }
    }
    return s.toString();
}

URLParser.prototype.parseTelephoneNumber =function(inBrackets){
    if(logger!=undefined) logger.debug("URLParser:parseTelephoneNumber():inBrackets="+inBrackets);
    var tn;
    this.lexer.selectLexer("charLexer");
    var lexer=new Lexer("","");
    var c = this.lexer.lookAhead(0);
    if (c == '+') {
        tn = this.global_phone_number(inBrackets);
    } else if (lexer.isHexDigit(c)|| c == '#'|| c == '*'|| c == '-'|| c == '.'
        || c == '('
        || c == ')') {
        tn = this.local_phone_number(inBrackets);
    } else {
        console.error("URLParser:parseTelephoneNumber(): unexpected char " + c);
        throw "URLParser:parseTelephoneNumber(): unexpected char " + c;
    }
    return tn;
}


URLParser.prototype.global_phone_number =function(inBrackets){
    if(logger!=undefined) logger.debug("URLParser:global_phone_number():inBrackets="+inBrackets);
    var tn = new TelephoneNumber();
    tn.setGlobal(true);
    var nv = null;
    this.lexer.match(this.PLUS);
    var b = this.base_phone_number();
    tn.setPhoneNumber(b);
    if (this.lexer.hasMoreChars()) {
        var tok = this.lexer.lookAhead(0);
        if (tok == ';' && inBrackets) {
            this.lexer.consume(1);
            nv = tel_parameters();
            tn.setParameters(nv);
        }
    }
    return tn;
}

URLParser.prototype.local_phone_number =function(inBrackets){
    if(logger!=undefined) logger.debug("URLParser:local_phone_number():inBrackets:"+inBrackets);
    var tn = new TelephoneNumber();
    tn.setGlobal(false);
    var nv = null;
    var b = null;
    b = this.local_number();
    tn.setPhoneNumber(b);
    if (this.lexer.hasMoreChars()) {
        var tok = this.lexer.peekNextToken();
        switch (tok.getTokenType()) {
            case this.SEMICOLON: {
                if (inBrackets) {
                    this.lexer.consume(1);
                    nv = this.tel_parameters();
                    tn.setParameters(nv);
                }
                break;
            }
            default: {
                break;
            }
        }
    }
    return tn;
}

URLParser.prototype.tel_parameters =function(){
    if(logger!=undefined) logger.debug("URLParser:tel_parameters()");
    var nvList = new NameValueList();
    var nv;
    while (true) {
        var pname = this.paramNameOrValue();
        if (pname.toLowerCase()==("phone-context").toLowerCase()) {
            nv = this.phone_context();
        } else {
            if (this.lexer.lookAhead(0) == '=') {
                this.lexer.consume(1);
                var value = this.paramNameOrValue();
                nv = new NameValue(pname, value, false);
            } else {
                nv = new NameValue(pname, "", true);
            }
        }
        nvList.set(nv);
        if (this.lexer.lookAhead(0) == ';') {
            this.lexer.consume(1);
        } else {
            return nvList;
        }
    }
}

URLParser.prototype.phone_context =function(){
    if(logger!=undefined) logger.debug("URLParser:phone_context()");
    this.lexer.match('=');
    var la = this.lexer.lookAhead(0);
    var value=null;
    if (la == '+') {// global-number-digits
        this.lexer.consume(1);// skip '+'
        value = "+" + this.base_phone_number();
    } else if (Lexer.isAlphaDigit(la)) {
        var t = this.lexer.match(Lexer.prototype.ID);// more broad than allowed
        value = t.getTokenValue();
    } else {
        console.error("URLParser:phone_context(): invalid phone-context:" + la);
        throw "URLParser:phone_context(): invalid phone-context:" + la;
    }
    return new NameValue("phone-context", value, false);
}


URLParser.prototype.telURL =function(inBrackets){
    if(logger!=undefined) logger.debug("URLParser:telURL():inBrackets="+inBrackets);
    this.lexer.match(TokenTypes.prototype.TEL);
    this.lexer.match(':');
    var tn = this.parseTelephoneNumber(inBrackets);
    var telUrl = new TelURLImpl();
    telUrl.setTelephoneNumber(tn);
    return telUrl;
}

URLParser.prototype.sipURL =function(inBrackets){
    if(logger!=undefined) logger.debug("URLParser:sipURL():inBrackets="+inBrackets);
    var retval = new SipUri();
    // pmusgrave - handle sips case
    var nextToken = this.lexer.peekNextToken();
    var sipOrSips = TokenTypes.prototype.SIP;
    var scheme = TokenNames.prototype.SIP;
    if (nextToken.getTokenType() == TokenTypes.prototype.SIPS) {
        sipOrSips = TokenTypes.prototype.SIPS;
        scheme = TokenNames.prototype.SIPS;
    }
    this.lexer.match(sipOrSips);
    this.lexer.match(':');
    retval.setScheme(scheme);
    var startOfUser = this.lexer.markInputPosition();
    var userOrHost = this.user();// Note: user may contain ';', host may not...
    var passOrPort = null;
    // name:password or host:port
    if (this.lexer.lookAhead() == ':') {
        this.lexer.consume(1);
        passOrPort = this.password();
    }
    // name@hostPort
    if (this.lexer.lookAhead() == '@') {
        this.lexer.consume(1);
        retval.setUser(userOrHost);
        if (passOrPort != null) {
            retval.setUserPassword(passOrPort);
        }
    } else {
        // then userOrHost was a host, backtrack just in case a ';' was eaten...
        this.lexer.rewindInputPosition(startOfUser);
    }
    var hnp = new HostNameParser(this.getLexer());
    var hp = hnp.hostPort(false);
    retval.setHostPort(hp);
    this.lexer.selectLexer("charLexer");
    while (this.lexer.hasMoreChars()) {
        // If the URI is not enclosed in brackets, parameters belong to header
        if (this.lexer.lookAhead(0) != ';' || !inBrackets) {
            break;
        }
        this.lexer.consume(1);
        var parms = this.uriParam();
        if (parms != null) {
            retval.setUriParameter(parms);
        }
    }
    if (this.lexer.hasMoreChars() && this.lexer.lookAhead(0) == '?') {
        this.lexer.consume(1);
        while (this.lexer.hasMoreChars()) {
            parms = this.qheader();
            retval.setQHeader(parms);
            if (this.lexer.hasMoreChars() && this.lexer.lookAhead(0) != '&') {
                break;
            } else {
                this.lexer.consume(1);
            }
        }
    }
        
    return retval;
}

URLParser.prototype.peekScheme =function(){
    if(logger!=undefined) logger.debug("URLParser:peekScheme()");
    var tokens = this.lexer.peekNextToken(1);
    if (tokens.length == 0) {
        return null;
    }
    var scheme = tokens[0].getTokenValue();
    return scheme;
}

URLParser.prototype.qheader =function(){
    if(logger!=undefined) logger.debug("URLParser:qheader()");
    var startIdx = this.lexer.ptr;
    while (true) {
        var la = this.lexer.lookAhead(0);
        if (la == '=') {
            break;
        } else if (la == '\0') {
            console.error("URLParser:qheader(): EOL reached");
            throw "URLParser:qheader(): EOL reached";
        }
        this.lexer.consume(1);
    }
    var name=this.lexer.getBuffer().substring(startIdx, this.lexer.ptr);
    this.lexer.consume(1);
    var value = this.hvalue();
    return new NameValue(name, value, false);
}

URLParser.prototype.hvalue =function(){
    if(logger!=undefined) logger.debug("URLParser:hvalue()");
    var retval = "";
    while (this.lexer.hasMoreChars()) {
        var la = this.lexer.lookAhead(0);
        // Look for a character that can terminate a URL.
        var isValidChar = false;
        switch (la) {
            case '+':
            case '?':
            case ':':
            case '[':
            case ']':
            case '/':
            case '$':
            case '_':
            case '-':
            case '"':
            case '!':
            case '~':
            case '*':
            case '.':
            case '(':
            case ')':
                isValidChar = true;
        }
        var lexer=new Lexer("","");
        if (isValidChar || lexer.isAlphaDigit(la)) {
            this.lexer.consume(1);
            retval=retval+la;
        } else if (la == '%') {
            retval=retval+this.escaped();
        } else {
            break;
        }
    }
    return retval.toString();
}

URLParser.prototype.urlString =function(){
    if(logger!=undefined) logger.debug("URLParser:urlString()");
    var retval = "";
    this.lexer.selectLexer("charLexer");
    while (this.lexer.hasMoreChars()) {
        var la = this.lexer.lookAhead(0);
        if (la == ' '|| la == '\t'|| la == '\n'|| la == '>'|| la == '<') {
            break;
        }
        this.lexer.consume(0);
        retval=retval+la;
    }
    return retval.toString();
}

URLParser.prototype.user =function(){
    if(logger!=undefined) logger.debug("URLParser:user()");
    var startIdx = this.lexer.getPtr();
    while (this.lexer.hasMoreChars()) {
        var la = this.lexer.lookAhead(0);
        if (this.isUnreserved(la) || this.isUserUnreserved(la)) {
            this.lexer.consume(1);
        } else if (this.isEscaped()) {
            this.lexer.consume(3);
        } else {
            break;
        }
    }
    return this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
}


URLParser.prototype.password =function(){
    if(logger!=undefined) logger.debug("URLParser:password()");
    var startIdx = this.lexer.getPtr();
    while (true) {
        var la = this.lexer.lookAhead(0);
        var isValidChar = false;
        switch (la) {
            case '&':
            case '=':
            case '+':
            case '$':
            case ',':
                isValidChar = true;
        }
        if (isValidChar || this.isUnreserved(la)) {
            this.lexer.consume(1);
        } else if (this.isEscaped()) {
            this.lexer.consume(3); 
        } else {
            break;
        }

    }
    return this.lexer.getBuffer().substring(startIdx, this.lexer.getPtr());
}

URLParser.prototype.parse =function(){
    if(logger!=undefined) logger.debug("URLParser:parse()");
    return this.uriReference(true);
}