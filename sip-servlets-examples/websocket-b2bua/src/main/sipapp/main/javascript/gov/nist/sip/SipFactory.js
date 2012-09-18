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
 *  Implementation of the JAIN-SIP SipFactory .
 *  @see  gov/nist/javax/sip/SipFactory.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function SipFactory() {
    if(logger!=undefined) logger.debug("SipFactory:SipFactory()");
    this.classname="SipFactory"; 
    this.sipFactory=null;
    this.mNameSipStackMap=new Array();
}

SipFactory.prototype.getInstance =function(){
    if(logger!=undefined) logger.debug("SipFactory:getInstance()");
    if (this.sipFactory == null) 
    {
        this.sipFactory = new SipFactory();
    }
    return this.sipFactory;
}

SipFactory.prototype.resetFactory =function(){
    if(logger!=undefined) logger.debug("SipFactory:resetFactory()");
    this.mNameSipStackMap=new Array();
}

SipFactory.prototype.createSipStack =function(wsUrl,sipUserAgentName){
    if(logger!=undefined) logger.debug("SipFactory:createSipStack()");

    var sipStack = null;
    for(var i=0;i<this.mNameSipStackMap.length;i++)
    {
        if(this.mNameSipStackMap[i][0]==wsUrl)
        {
            sipStack=this.mNameSipStackMap[i][1]
        }
    }
    if (sipStack == null) {
        var array=new Array();
        sipStack=new SipStackImpl(wsUrl,sipUserAgentName);
        array[0]=wsUrl;
        array[1]=sipStack;
        this.mNameSipStackMap.push(array);
    }
    return sipStack;
}


SipFactory.prototype.createAddressFactory =function(){
    if(logger!=undefined) logger.debug("SipFactory:createAddressFactory()");
    try {
        var afi=new AddressFactoryImpl();
        return afi;
    } catch (ex) {
        console.error("SipFactory:createAddressFactory(): failed to create AddressFactory");
        throw "SipFactory:createAddressFactory(): failed to create AddressFactory";
    }
}
SipFactory.prototype.createHeaderFactory =function(){
    if(logger!=undefined) logger.debug("SipFactory:createHeaderFactory()");
    try {
        var hfi=new HeaderFactoryImpl();
        return hfi;
    } catch (ex) {
        console.error("SipFactory:createHeaderFactory(): failed to create HeaderFactory");
        throw "SipFactory:createHeaderFactory(): failed to create HeaderFactory";
    }
}
SipFactory.prototype.createMessageFactory =function(listeningpoint){
    if(logger!=undefined) logger.debug("SipFactory:createMessageFactory():listeningpoint:"+listeningpoint);
    try {
        var mfi=new MessageFactoryImpl(listeningpoint);
        return mfi;
    } catch (ex) {
        console.error("SipFactory:createMessageFactory(): failed to create MessageFactory");
        throw "SipFactory:createMessageFactory():failed to create MessageFactory";
    }
}