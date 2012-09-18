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
 *  Implementation of the JAIN-SIP ListeningPointImpl .
 *  @see  gov/nist/javax/sip/ListeningPointImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function ListeningPointImpl() {
    if(logger!=undefined) logger.debug("ListeningPointImpl:ListeningPointImpl()");
    this.classname="ListeningPointImpl";
    this.transport="ws";
    this.messageProcessor=null;
    this.sipProvider=null;
    if(arguments.length!=0)
    {
        var sipStack=arguments[0];
        this.sipStack=sipStack;
    }
}

ListeningPointImpl.prototype.makeKey =function(host,transport){
    if(logger!=undefined) logger.debug("ListeningPointImpl:makeKey():host="+host);
    if(logger!=undefined) logger.debug("ListeningPointImpl:makeKey():transport="+transport);
    var string="";
    string=(string+host+"/"+transport).toLowerCase();
    return string;
}

ListeningPointImpl.prototype.getKey =function(){
    if(logger!=undefined) logger.debug("ListeningPointImpl:getKey()");
    return this.makeKey(this.sipStack.getHostAddress(), this.transport);
}

ListeningPointImpl.prototype.getUserAgent =function(){
    if(logger!=undefined) logger.debug("ListeningPointImpl:getUserAgent()");
    return this.sipStack.getUserAgent();
}

ListeningPointImpl.prototype.setSipProvider =function(sipProviderImpl){
    if(logger!=undefined) logger.debug("ListeningPointImpl:setSipProvider():sipProviderImpl="+sipProviderImpl);
    this.sipProvider = sipProviderImpl;
}

ListeningPointImpl.prototype.removeSipProvider =function(){
    if(logger!=undefined) logger.debug("ListeningPointImpl:removeSipProvider()");
    this.sipProvider = null;
}

ListeningPointImpl.prototype.getURLWS =function(){
    if(logger!=undefined) logger.debug("ListeningPointImpl:getURLWS()");
    return this.messageProcessor.getURLWS();
}

ListeningPointImpl.prototype.getTransport =function(){
    if(logger!=undefined) logger.debug("ListeningPointImpl:getTransport()");
    return this.messageProcessor.getTransport();
}

ListeningPointImpl.prototype.getProvider =function(){
    if(logger!=undefined) logger.debug("ListeningPointImpl:getProvider()");
    return this.sipProvider;
}

ListeningPointImpl.prototype.setSentBy =function(sentBy){
    if(logger!=undefined) logger.debug("ListeningPointImpl:setSentBy():sentBy="+sentBy);
    this.messageProcessor.setSentBy(sentBy);
}

ListeningPointImpl.prototype.getSentBy =function(){
    if(logger!=undefined) logger.debug("ListeningPointImpl:getSentBy()");
    return this.messageProcessor.getSentBy();
}

ListeningPointImpl.prototype.isSentBySet =function(){
    if(logger!=undefined) logger.debug("ListeningPointImpl:isSentBySet()");
    return this.messageProcessor.isSentBySet();
}

ListeningPointImpl.prototype.getViaHeader =function(){
    if(logger!=undefined) logger.debug("ListeningPointImpl:getViaHeader()");
    return this.messageProcessor.getViaHeader();
}

ListeningPointImpl.prototype.getMessageProcessor =function(){
    if(logger!=undefined) logger.debug("ListeningPointImpl:getMessageProcessor()");
    return this.messageProcessor;
}

ListeningPointImpl.prototype.getHost =function(){
    if(logger!=undefined) logger.debug("ListeningPointImpl:getMessageProcessor()");
    return this.hostname;
}

ListeningPointImpl.prototype.createContactHeader =function(userName){
    if(logger!=undefined) logger.debug("ListeningPointImpl:createContactHeader()");
    try {
        var hostname = this.sipStack.getHostAddress();
        var sipURI = new SipUri();
        sipURI.setHost_String(hostname);
        sipURI.setUser(userName);
        sipURI.setTransportParam(this.transport);
        var contact = new Contact();
        var address = new AddressImpl();
        address.setURI(sipURI);
        contact.setAddress(address);
        return contact;
    } catch (ex) {
        console.error("ListeningPointImpl:createContactHeader(): catched exception:"+ex);
        return null;
    }
}

ListeningPointImpl.prototype.sendHeartbeat =function(infoApp){
    if(logger!=undefined) logger.debug("ListeningPointImpl:sendHeartbeat()");
    var messageChannel = this.messageProcessor.createMessageChannel(infoApp);
    var siprequest = new SIPRequest();
    siprequest.setNullRequest();
    messageChannel.sendMessage(siprequest);
}

ListeningPointImpl.prototype.createViaHeader =function(){
    if(logger!=undefined) logger.debug("ListeningPointImpl:createViaHeader()");
    return this.getViaHeader();
}

ListeningPointImpl.prototype.getPort =function(){
    if(logger!=undefined) logger.debug("ListeningPointImpl:getPort()");
    return this.messageProcessor.getPort();
}

ListeningPointImpl.prototype.getHostAddress =function(){
    if(logger!=undefined) logger.debug("ListeningPointImpl:getHostAddress()");
    return this.sipStack.getHostAddress();
}