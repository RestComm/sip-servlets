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
 *  Implementation of the JAIN-SIP StringMsgParser .
 *  @see  gov/nist/javax/sip/parser/StringMsgParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function StringMsgParser(exhandler) {
    if(logger!=undefined) logger.debug("StringMsgParser:StringMsgParser()");
    this.classname="StringMsgParser"; 
    this.readBody=true;
    this.parseExceptionListener=null;
    this.rawStringMessage=null;
    this.strict=null;
    this.computeContentLengthFromMessage = false;
    this.viaCount=0;
    if(exhandler!=null)
    {
        this.parseExceptionListener = exhandler;
    }
}

StringMsgParser.prototype.SIP_VERSION_STRING="SIP/2.0";

StringMsgParser.prototype.setParseExceptionListener =function(pexhandler){
    if(logger!=undefined) logger.debug("StringMsgParser:setParseExceptionListener():pexhandler="+pexhandler);
    this.parseExceptionListener = pexhandler;
}

StringMsgParser.prototype.parseSIPMessage =function(){
    if(logger!=undefined) logger.debug("StringMsgParser:parseSIPMessage()");
    if(typeof arguments[0]=="string")
    {
        var msgString=arguments[0];
        return this.parseSIPMessagestring(msgString);
    }
    else if(typeof arguments[0]=="object")
    {
        var msgBuffer=arguments[0];
        return this.parseSIPMessagebyte(msgBuffer);
    }
}

StringMsgParser.prototype.parseSIPMessagestring =function(msgString){
    if(logger!=undefined) logger.debug("StringMsgParser:():parseSIPMessagestring: msgString="+msgString);
    if (msgString == null || msgString.length == 0) {
        return null;
    }
    this.rawStringMessage = msgString;
    var i = 0;
    try {
        while (msgString.charCodeAt(i) < 0x20) {
            i++;
        }
    } catch (ex) {
        console.error("StringMsgParser:parseSIPMessagestring(): catched exception:"+ex);
        return null;
    }
    var currentLine = null;
    var currentHeader = null;
    var isFirstLine = true;
    var message = null;
    do {
        var lineStart = i;
        try {
            var c = msgString.charAt(i);
            while (c != '\r' && c != '\n') {
                c = msgString.charAt(++i);
            }
        } catch (ex) {
            console.error("StringMsgParser:parseSIPMessagestring(): catched exception:"+ex);
            break;
        } 
        currentLine = msgString.substring(lineStart, i);
        currentLine = this.trimEndOfLine(currentLine);
        if (currentLine.length == 0) {
            if (currentHeader != null) {
                this.processHeader(currentHeader, message);
            }
        } 
        else {
            if (isFirstLine) {
                message = this.processFirstLine(currentLine);
            } 
            else {
                var firstChar = currentLine.charAt(0);
                if (firstChar == '\t' || firstChar == ' ') {
                    if (currentHeader == null) {
                        console.error("StringMsgParser:parseSIPMessagestring(): bad header continuation.");
                        throw "StringMsgParser:parseSIPMessagestring(): bad header continuation.";
                    }
                    currentHeader = currentHeader+currentLine.substring(1);
                } 
                else {
                    if (currentHeader != null) {
                        this.processHeader(currentHeader, message);
                    }
                    currentHeader = currentLine;
                }
            }
        }
        if (msgString.charAt(i) == '\r' && msgString.length > i + 1 && msgString.charAt(i + 1) == '\n') {
            i++;
        }
        i++;
        isFirstLine = false;
    } while (currentLine.length > 0);
    message.setSize(i);
    if (this.readBody && message.getContentLength() != null) {
        if (message.getContentLength().getContentLength() != 0) {
            var body = msgString.substring(i);
            message.setMessageContent(body, this.strict, this.computeContentLengthFromMessage, message.getContentLength().getContentLength());
        } 
        else if (!this.computeContentLengthFromMessage && message.getContentLength().getContentLength() == 0) {
            if (this.strict) {
                console.error("StringMsgParser:parse(): extraneous characters at the end of the message",i);
                throw "StringMsgParser:parse(): extraneous characters at the end of the message";
            }
        }
    }
    return message;
}
StringMsgParser.prototype.parseSIPMessagebyte =function(msgBuffer){
    if(logger!=undefined) logger.debug("StringMsgParser:parseSIPMessagebyte(): msgBuffer="+msgBuffer);
    if (msgBuffer == null || msgBuffer.length == 0) {
        return null;
    }
    var i = 0;
    try {
        while (msgBuffer[i].charCodeAt(0) < 0x20) {
            i++;
        }
    } catch (ex) {
        console.error("StringMsgParser:parseSIPMessagebyte(): catched exception:"+ex);
        return null;
    }
    var currentLine = "";
    var currentHeader = null;
    var isFirstLine = true;
    var message = null;
    do {
        var lineStart = i;
        try {
            while (msgBuffer[i] != '\r' && msgBuffer[i] != '\n') {
                i++;
            }
        } catch (ex) {
            console.error("StringMsgParser:parseSIPMessagebyte(): catched exception:"+ex);
            break;
        }
        var lineLength = i - lineStart;
        try {
            for(var x=0;x<lineLength;x++)
            {
                currentLine=currentLine+msgBuffer[x+lineStart];
            }
        } catch (ex) {
            console.error("StringMsgParser:parseSIPMessagebyte(): bad message encoding!");
            throw "StringMsgParser:parseSIPMessagebyte():bad message encoding!";
        }
        currentLine = this.trimEndOfLine(currentLine);
        if (currentLine.length() == 0) {
            if (currentHeader != null && message != null) {
                this.processHeader(currentHeader, message);
            }
        } else {
            if (isFirstLine) {
                message = this.processFirstLine(currentLine);
            } else {
                var firstChar = currentLine.charAt(0);
                if (firstChar == '\t' || firstChar == ' ') {
                    if (currentHeader == null) {
                        console.error("StringMsgParser:parseSIPMessagebyte(): bad header continuation");
                        throw "StringMsgParser:parseSIPMessagebyte(): bad header continuation";
                    }
                    currentHeader = currentHeader+currentLine.substring(1);
                } else {
                    if (currentHeader != null && message != null) {
                        this.processHeader(currentHeader, message);
                    }
                    currentHeader = currentLine;
                }
            }
        }
        if (msgBuffer[i] == '\r' && msgBuffer.length > i + 1 && msgBuffer[i + 1] == '\n') {
            i++;
        }
        i++;
        isFirstLine = false;
    } while (currentLine.length > 0); 
    if (message == null) {
        console.error("StringMsgParser:parseSIPMessagebyte(): bad message");
        throw "StringMsgParser:parseSIPMessagebyte(): bad message";
    }
    message.setSize(i);
    if (this.readBody && message.getContentLength() != null
        && message.getContentLength().getContentLength() != 0) {
        var bodyLength = msgBuffer.length - i;
        var body = new Array();
        var l=i;
        for(x=0;x<bodyLength;x++)
        {
            body[x]=msgBuffer[l];
            l=l+1;
        }
        message.setMessageContent(body, this.computeContentLengthFromMessage, message.getContentLength().getContentLength());
    }
    return message;
}

StringMsgParser.prototype.trimEndOfLine =function(line){
    if(logger!=undefined) logger.debug("StringMsgParser:trimEndOfLine():line="+line);
    if (line == null) {
        return line;
    }
    var i = line.length - 1;
    while (i >= 0 && line.charCodeAt(i) <= 0x20) {
        i--;
    }
    if (i == line.length - 1) {
        return line;
    }
    if (i == -1) {
        return "";
    }
    return line.substring(0, i + 1);
}


StringMsgParser.prototype.processFirstLine =function(firstLine){
    if(logger!=undefined) logger.debug("StringMsgParser:processFirstLine():firstLine="+firstLine);
    var message=null;
    var constlength=this.SIP_VERSION_STRING.length;
    var n=0;
    for(var i=0;i<constlength;i++)
    {
        if(firstLine.charAt(i)==this.SIP_VERSION_STRING.charAt(i))
        {
            n=n+1;
        }
    }
    if (n!=constlength) {
        message = new SIPRequest();
        try {
            var requestLine = new RequestLineParser(firstLine + "\n").parse();
            message.setRequestLine(requestLine);
        } catch (ex) {
            console.error("StringMsgParser:processFirstLine(): catched exception:"+ex);
            if (this.parseExceptionListener != null) {
                var rl=new RequestLine();
                this.parseExceptionListener.handleException(ex, message,
                    rl.classname, firstLine, this.rawStringMessage);
            } else {
                throw ex;
            }
        }
    } 
    else {
        message = new SIPResponse();
        try {
            var sl = new StatusLineParser(firstLine + "\n").parse();
            message.setStatusLine(sl);
        } catch (ex) {
            console.error("StringMsgParser:processFirstLine(): catched exception:"+ex);
            if (this.parseExceptionListener != null) {
                sl=new StatusLine();
                this.parseExceptionListener.handleException(ex, message,
                    sl.classname, firstLine, this.rawStringMessage);
            } else {
                throw ex;
            }
        }
    }
    return message;
}

StringMsgParser.prototype.processHeader =function(header, message){
    if(logger!=undefined) logger.debug("StringMsgParser:processHeader():header="+ header+", message="+message.classname);
    if (header == null || header.length == 0) {
        return;
    }
    var headerParser = null;
    try {
        var parserfactory=new ParserFactory();
        headerParser = parserfactory.createParser(header + "\n");
    } catch (ex) {
        console.error("StringMsgParser:processHeader(): catched exception:"+ex);
        this.parseExceptionListener.handleException(ex, message, null,
            header, this.rawStringMessage);
        return;
    }
    try {
        var sipHeader = headerParser.parse();
        if(sipHeader instanceof ViaList)
        {
            this.viaCount=this.viaCount+1;
            if(this.viaCount==1)
            {
                message.attachHeader(sipHeader, false);
            }
            else
            {
                message.addViaHeaderList(sipHeader);    
            }
        }
        else
        {
            message.attachHeader(sipHeader, false);
        }
        
    } catch (ex) {
        console.error("StringMsgParser:processHeader(): catched exception:"+ex);
        if (this.parseExceptionListener != null) {
            var lexer=new Lexer();
            var headerName = lexer.getHeaderName(header);
            var namemap=new NameMap();
            var headerClass = namemap.getClassFromName(headerName);
            if (headerClass == null) {
                headerClass = new ExtensionHeaderImpl().classname;
            }
            this.parseExceptionListener.handleException(ex, message,
                headerClass, header, this.rawStringMessage);
        }
    }
}

StringMsgParser.prototype.parseAddress =function(address){
    if(logger!=undefined) logger.debug("StringMsgParser:parseAddress():address="+address);
    var addressParser = new AddressParser(address);
    return addressParser.address(true);
}

StringMsgParser.prototype.parseHost =function(host){
    if(logger!=undefined) logger.debug("StringMsgParser:parseHost():host="+host);
    var lexer = new Lexer("charLexer", host);
    return new HostNameParser(lexer).host();
}

StringMsgParser.prototype.parseTelephoneNumber =function(telephone_number){
    if(logger!=undefined) logger.debug("StringMsgParser:parseTelephoneNumber():telephone_number="+telephone_number);
    return new URLParser(telephone_number).parseTelephoneNumber(true);
}

StringMsgParser.prototype.parseSIPUrl =function(url){
    if(logger!=undefined) logger.debug("StringMsgParser:parseSIPUrl():url:"+url);
    try {
        return new URLParser(url).sipURL(true);
    } catch (ex) {
        console.error("StringMsgParser:parseSIPUrl(): "+ url + " is not a SIP URL ");
        throw "StringMsgParser:parseSIPUrl(): "+ url + " is not a SIP URL ";
    }
}

StringMsgParser.prototype.parseUrl =function(url){
    if(logger!=undefined) logger.debug("StringMsgParser:parseUrl():url="+url);
    return new URLParser(url).parse();
}

StringMsgParser.prototype.parseSIPHeader =function(header){
    if(logger!=undefined) logger.debug("StringMsgParser:parseSIPHeader():header="+header);
    var start = 0;
    var end = header.length - 1;
    try {
        while (header.charCodeAt(start) <= 0x20) {
            start++;
        }
        while (header.charCodeAt(end) <= 0x20) {
            end--;
        }
    } catch (ex) {
        console.error("StringMsgParser:parseSIPHeader(): empty header");
        throw "StringMsgParser:parseSIPHeader(): eEmpty header";
    }
    var buffer = "";
    var i = start;
    var lineStart = start;
    var endOfLine = false;
    while (i <= end) {
        var c = header.charAt(i);
        if (c == '\r' || c == '\n') {
            if (!endOfLine) {
                buffer=buffer+header.substring(lineStart, i);
                endOfLine = true;
            }
        } else {
            if (endOfLine) {
                endOfLine = false;
                if (c == ' ' || c == '\t') {
                    buffer=buffer+' ';
                    lineStart = i + 1;
                } else {
                    lineStart = i;
                }
            }
        }
        i++;
    }
    buffer=buffer+header.substring(lineStart, i);
    buffer=buffer+'\n';
    var parserfactory=new ParserFactory();
    var hp = parserfactory.createParser(buffer.toString());
    if (hp == null) {
        console.error("StringMsgParser:parseSIPHeader(): could not create parser");
        throw "StringMsgParser:parseSIPHeader(): could not create parser";
    }
    return hp.parse();
}

StringMsgParser.prototype.parseSIPRequestLine =function(requestLine){
    if(logger!=undefined) logger.debug("StringMsgParser:parseSIPRequestLine():requestLine="+requestLine);
    requestLine += "\n";
    return new RequestLineParser(requestLine).parse();
}

StringMsgParser.prototype.parseSIPStatusLine =function(statusLine){
    if(logger!=undefined) logger.debug("StringMsgParser:parseSIPStatusLine():statusLine="+statusLine);
    statusLine += "\n";
    return new StatusLineParser(statusLine).parse();
}

StringMsgParser.prototype.setComputeContentLengthFromMessage =function(computeContentLengthFromMessage){
    if(logger!=undefined) logger.debug("StringMsgParser:setComputeContentLengthFromMessage():computeContentLengthFromMessage="+computeContentLengthFromMessage);
    this.computeContentLengthFromMessage = computeContentLengthFromMessage;
}

StringMsgParser.prototype.setStrict =function(strict){
    if(logger!=undefined) logger.debug("StringMsgParser:setStrict():strict="+strict);
    this.strict = strict;
}