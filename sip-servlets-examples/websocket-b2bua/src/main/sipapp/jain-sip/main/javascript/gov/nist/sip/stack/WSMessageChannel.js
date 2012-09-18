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
 *  Implementation of the JAIN-SIP WSMessageChannel .
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function WSMessageChannel() {
    if(logger!=undefined) logger.debug("WSMessageChannel:WSMessageChannel()");
    this.classname="WSMessageChannel"; 
    this.myParser=null;
    this.key=null;
    this.isCached=null;
    this.isRunning=null;
    this.sipStack=null;
    this.wsurl=null;
    this.infoApp=null;
    this.messageProcessor=null;
    this.alive=true;
    this.websocket=null;
    this.requestsent=null;
    if(arguments.length!=0)
    {
        var sipStack=arguments[0];
        var msgProcessor=arguments[1];
        this.sipStack = sipStack;
        this.peerProtocol = "WS";
        this.messageProcessor = msgProcessor;
        this.myAddress = this.sipStack.getHostAddress();
        this.wsurl=this.messageProcessor.getURLWS();
        this.websocket=this.createWebSocket(this.wsurl);
    }
}

WSMessageChannel.prototype.isReliable =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:isReliable()");
    return true;
}

WSMessageChannel.prototype.createWebSocket =function(wsurl){
    if(logger!=undefined) logger.debug("WSMessageChannel:createWebSocket():wsurl="+wsurl);
    this.websocket=new WebSocket(wsurl,"sip");
    var wsmc=this;
    this.websocket.onclose=function()
    {
        console.warn("WSMessageChannel:createWebSocket(): the websocket is closed, reconnecting...");
        wsmc.sipStack.sipListener.processDisconnected();
        wsmc.websocket=null;
        this.alive=false;
    }
    
    this.websocket.onopen=function()
    {
        console.info("WSMessageChannel:createWebSocket(): the websocket is opened");
        wsmc.sipStack.sipListener.processConnected();
    }
    
    this.websocket.onerror=function(error)
    {
        console.error("WSMessageChannel:createWebSocket(): websocket connection has failed:"+error);
        wsmc.sipStack.sipListener.processConnectionError(error);
    }
    
    this.websocket.onmessage=function(event)
    {
        var data=event.data;
        wsmc.myParser=new WSMsgParser(wsmc.sipStack,data);
        wsmc.myParser.parsermessage(wsmc.requestsent); 
    }
    return this.websocket;
}

WSMessageChannel.prototype.close =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:close()");
    this.alive=false;
    this.websocket.close();
    this.websocket = null;
}

WSMessageChannel.prototype.getSIPStack =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:getSIPStack()");
    return this.sipStack;
}

WSMessageChannel.prototype.getTransport =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:getTransport()");
    return "WS";
}

WSMessageChannel.prototype.getURLWS =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:getURLWS()");
    return this.wsurl;
}

WSMessageChannel.prototype.getPeerProtocol =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:getPeerProtocol()");
    return this.peerProtocol;
}

WSMessageChannel.prototype.sendMessage = function(sipMessage){
    if(logger!=undefined) logger.debug("WSMessageChannel:sendMessage()");
    if (sipMessage instanceof SIPRequest)
    {
        this.requestsent=sipMessage;
    }
    
    if(typeof sipMessage!="string")
    {
        var encodedSipMessage = sipMessage.encode();
        sipMessage=encodedSipMessage;
    }
    this.websocket.send(sipMessage);
    console.info("SIP message sent: "+sipMessage); 
}

WSMessageChannel.prototype.getKey =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:getKey()");
    if (this.key != null) {
        return this.key;
    } 
    else {
        var mc=new WSMessageChannel();
        this.key = mc.getKey(this.wsurl, "WS");
        return this.key;
    }
}

WSMessageChannel.prototype.getViaHost =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:getViaHost()");
    return this.myAddress;
}

WSMessageChannel.prototype.getViaHeader =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:getViaHeader()");
    var channelViaHeader;
    channelViaHeader = new Via();
    channelViaHeader.setTransport(this.getTransport());
    channelViaHeader.setSentBy(this.getHostPort());
    return channelViaHeader;
}

WSMessageChannel.prototype.getViaHost =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:getViaHost()");
    return this.myAddress;
}

WSMessageChannel.prototype.getViaHostPort =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:getViaHostPort()");
    var retval = new HostPort();
    retval.setHost(new Host(this.getViaHost()));
    return retval;
}

WSMessageChannel.prototype.getPeerAddress =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:getPeerAddress()");
    if (this.peerAddress != null) {
        return this.peerAddress;
    } 
    else
    {
        return getHost();
    }
}

WSMessageChannel.prototype.getHost =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:getHost()");
    return this.sipStack.getHostAddress();
}

WSMessageChannel.prototype.createBadReqRes =function(badReq/*,pe*/){
    if(logger!=undefined) logger.debug("WSMessageChannel:createBadReqRes():badReq="+badReq/*,pe*/);
    var buf = 512;
    buf=buf+"SIP/2.0 400 Bad Request";
    if (!this.copyViaHeaders(badReq, buf))
    {
        return null;
    }
    if (!this.copyHeader(this.CSeqHeader, badReq, buf))
    {
        return null;
    }
    if (!this.copyHeader(this.CallIdHeader, badReq, buf))
    {
        return null;
    }
    if (!this.copyHeader(this.FromHeader, badReq, buf))
    {
        return null;
    }
    if (!this.copyHeader(this.ToHeader, badReq, buf))
    {
        return null;
    }
    var toStart = buf.indexOf(ToHeader.NAME);
    if (toStart != -1 && buf.indexOf("tag", toStart) == -1) {
        buf=buf+";tag=badreq"
    }
    var mfi=new MessageFactoryImpl();
    var s = mfi.getDefaultServerHeader();
    if ( s != null ) {
        buf=buf+"\r\n" + s.toString();
    }
    var clength = badReq.length();
    var cth = new ContentType("message", "sipfrag");
    buf=buf+"\r\n" + cth.toString();
    var clengthHeader = new ContentLength(clength);
    buf=buf+"\r\n" + clengthHeader.toString();
    buf=buf+"\r\n\r\n" + badReq;
    return buf.toString();
}

WSMessageChannel.prototype.copyHeader =function(name,fromReq,buf){
    if(logger!=undefined) logger.debug("WSMessageChannel:copyHeader():name="+name+", fromReq="+fromReq+",buf="+buf); 
    var start = fromReq.indexOf(name);
    if (start != -1) {
        var end = fromReq.indexOf("\r\n", start);
        if (end != -1) {
            buf=buf+fromReq.substring(start - 2, end);
            return true;
        }
    }
    return false;
}

WSMessageChannel.prototype.copyViaHeaders =function(fromReq,buf){
    if(logger!=undefined) logger.debug("WSMessageChannel:copyViaHeaders(): fromReq="+fromReq+",buf="+buf);
    var start = fromReq.indexOf(this.ViaHeader);
    var found = false;
    while (start != -1) {
        var end = fromReq.indexOf("\r\n", start);
        if (end != -1) {
            buf=buf+fromReq.substring(start - 2, end);
            found = true;
            start = fromReq.indexOf(this.ViaHeader, end);
        } else {
            return false;
        }
    }
    return found;
}

WSMessageChannel.prototype.getMessageProcessor =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:getMessageProcessor()");
    return this.messageProcessor;
}

WSMessageChannel.prototype.isSecure =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:isSecure()");
    return false;
}

WSMessageChannel.prototype.getWebSocket =function(){
    if(logger!=undefined) logger.debug("WSMessageChannel:getWebSocket()");
    return this.websocket;
}