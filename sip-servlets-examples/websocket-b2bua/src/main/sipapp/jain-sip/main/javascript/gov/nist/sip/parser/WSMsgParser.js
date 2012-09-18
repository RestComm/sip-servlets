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
 *  Implementation of the JAIN-SIP WSMsgParser .
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function WSMsgParser(sipstack,data) {
    if(logger!=undefined) logger.debug("WSMsgParser:WSMsgParser()");
    this.classname="WSMsgParser"; 
    this.data=data;
    this.peerProtocol=null;
    this.messageProcessor = null;
    this.sipStack=sipstack;
}

WSMsgParser.prototype.RPORT="rport";
WSMsgParser.prototype.RECEIVED="received";

WSMsgParser.prototype.parsermessage =function(requestsent){
    if(logger!=undefined) logger.debug("WSMsgParser:parsermessage():requestsent="+requestsent);
    var smp = new StringMsgParser();
    var sipMessage = smp.parseSIPMessage(this.data);
    var cl =  sipMessage.getContentLength();
    var contentLength = 0;
    if (cl != null) {
        contentLength = cl.getContentLength();
    }
    else {
        contentLength = 0;
    }
    if (contentLength == 0) {
        sipMessage.removeContent();
    } 
    console.info("SIP message received: "+sipMessage.encode());
    this.processMessage(sipMessage,requestsent);
}

WSMsgParser.prototype.processMessage =function(sipMessage,requestSent){
    if(logger!=undefined) logger.debug("WSMsgParser:processMessage():sipMessage="+sipMessage+", requestsent:"+requestSent);
    if (sipMessage.getFrom() == null
        ||  sipMessage.getTo() == null || sipMessage.getCallId() == null
        || sipMessage.getCSeq() == null || sipMessage.getViaHeaders() == null) {
        return;
    }
    var channel=this.getSIPStack().getChannel();
    if (sipMessage instanceof SIPRequest) {
        this.peerProtocol = "WS";
        var sipRequest =  sipMessage;
        var sipServerRequest = this.sipStack.newSIPServerRequest(sipRequest, channel);
        if (sipServerRequest != null) 
        {
            sipServerRequest.processRequest(sipRequest,channel);
        }//i delete all parts of logger 
    } 
    else {
        var sipResponse = sipMessage;
        try {
            sipResponse.checkHeaders();
        } catch (ex) {
            console.error("WSMsgParser:processMessage(): catched exception:"+ex);
            return;
        }
        var sipServerResponse = this.sipStack.newSIPServerResponse(sipResponse, channel);
        if (sipServerResponse != null) {
            if (sipServerResponse instanceof SIPClientTransaction
                && !sipServerResponse.checkFromTag(sipResponse)) 
                {
                return;
            }
            sipServerResponse.processResponse(sipResponse, channel);
        } 
    }
}


WSMsgParser.prototype.getSIPStack =function(){
    if(logger!=undefined) logger.debug("WSMsgParser:getSIPStack()");
    return this.sipStack;
}