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
 *  Implementation of the JAIN-SIP Contact .
 *  @see  gov/nist/javax/sip/header/Contact.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function Contact() {
    if(logger!=undefined) logger.debug("Contact:Contact()");
    this.serialVersionUID = "1677294871695706288L";
    this.classname="Contact";
    this.headerName=this.NAME;
    this.parameters = new NameValueList();
    this.duplicates = new DuplicateNameValueList();
    this.address=new AddressImpl();
    this.Q = this.Q;
    this.contactList=new ContactList();
    this.wildCardFlag=null;
}

Contact.prototype = new AddressParametersHeader();
Contact.prototype.constructor=Contact;
Contact.prototype.NAME="Contact";
Contact.prototype.ACTION="action";
Contact.prototype.PROXY="proxy";
Contact.prototype.REDIRECT="redirect";
Contact.prototype.EXPIRES="expires";
Contact.prototype.Q="q";
Contact.prototype.NAME_ADDR=1;
Contact.prototype.SEMICOLON=";";
Contact.prototype.SIP_INSTANCE="+sip.instance";
Contact.prototype.PUB_GRUU="pub-gruu";
Contact.prototype.TEMP_GRUU="temp-gruu";

Contact.prototype.setParameter =function(name,value){
    if(logger!=undefined) logger.debug("Contact:setParameter():name="+name+",value"+value);
    var nv = this.parameters.getNameValue(name);
    if (nv != null) 
    {
        nv.setValueAsObject(value);
    } 
    else 
    {
        nv = new NameValue(name, value);
        if (name.toLowerCase()=="methods")
        {
            nv.setQuotedValue();
        }
        this.parameters.set_nv(nv);
    }
}

Contact.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("Contact:encodeBody()");
    return this.encodeBodyBuffer("");
}

Contact.prototype.encodeBodyBuffer =function(buffer){
    if(logger!=undefined) logger.debug("Contact:encodeBodyBuffer():buffer="+buffer);
    if (this.wildCardFlag) {
        buffer=buffer+"*";
    }
    else {
        if (this.address.getAddressType() == this.NAME_ADDR) 
        {
            buffer=this.address.encodeBuffer(buffer);
        } 
        else 
        {
            buffer=buffer+"<";
            buffer=this.address.encodeBuffer(buffer);
            buffer=buffer+">";
        }
        if (this.parameters.hmap.length!=0) 
        {
            buffer=buffer+this.SEMICOLON;
            buffer=this.parameters.encodeBuffer(buffer);
        }
    }
    return buffer;
}

Contact.prototype.getContactList =function(){
    if(logger!=undefined) logger.debug("Contact:getContactList()");
    return this.contactList;
}

Contact.prototype.getWildCardFlag =function(){
    if(logger!=undefined) logger.debug("Contact:getWildCardFlag()");
    return this.wildCardFlag;
}

Contact.prototype.getAddress =function(){
    if(logger!=undefined) logger.debug("Contact:getAddress()");
    return this.address;
}

Contact.prototype.getContactParms =function(){
    if(logger!=undefined) logger.debug("Contact:getContactParms()");
    return this.parameters;
}

Contact.prototype.getExpires =function(){
    if(logger!=undefined) logger.debug("Contact:getExpires()");
    return this.getParameterAsNumber(this.EXPIRES);
}

Contact.prototype.setExpires =function(expiryDeltaSeconds){
    if(logger!=undefined) logger.debug("Contact:setExpires():expiryDeltaSeconds="+expiryDeltaSeconds);
    var deltaSeconds = expiryDeltaSeconds-0;
    this.parameters.set_name_value(this.EXPIRES, deltaSeconds);
}

Contact.prototype.getQValue =function(){
    if(logger!=undefined) logger.debug("Contact:getQValue()");
    return this.getParameterAsNumber(Q);
}

Contact.prototype.setContactList =function(cl){
    if(logger!=undefined) logger.debug("Contact:setContactList():cl"+cl);
    this.contactList=cl;
}

Contact.prototype.setWildCardFlag =function(w){
    if(logger!=undefined) logger.debug("Contact:setWildCardFlag():w="+w);
    this.wildCardFlag = true;
    this.address = new AddressImpl();
    this.address.setWildCardFlag();
}

Contact.prototype.setAddress =function(address){
    if(logger!=undefined) logger.debug("Contact:setAddress():address="+address);
    if (address == null)
    {
        console.error("CSeq:setAddress(): the address parameter is null");
        throw "CSeq:setAddress(): the address parameter is null";
    }
    this.address = address;
    this.wildCardFlag = false;
}

Contact.prototype.setQValue =function(qValue){
    if(logger!=undefined) logger.debug("Contact:setQValue():qValue="+qValue);
    if (qValue != -1 && (qValue < 0 || qValue > 1))
    {
        console.error("CSeq:setQValue(): the qValue is not between 0 and 1");
        throw "CSeq:setQValue(): Jthe qValue is not between 0 and 1";
    }
    var qv=new Number(qValue);
    this.parameters.set_name_value(Q, qv);
}

Contact.prototype.setWildCard =function(){
    if(logger!=undefined) logger.debug("Contact:setWildCard()");
    this.setWildCardFlag(true);
}
Contact.prototype.isWildCard =function(){
    if(logger!=undefined) logger.debug("Contact:isWildCard()");
    return this.address.isWildcard();
}

Contact.prototype.removeSipInstanceParam =function(){
    if(logger!=undefined) logger.debug("Contact:removeSipInstanceParam()");
    if (this.parameters != null)
    {
        this.parameters.delet(this.SIP_INSTANCE);
    }
}

Contact.prototype.getSipInstanceParam =function(){
    if(logger!=undefined) logger.debug("Contact:getSipInstanceParam()");
    return this.parameters.getValue(this.SIP_INSTANCE);
}

Contact.prototype.setSipInstanceParam =function(value){
    if(logger!=undefined) logger.debug("Contact:setSipInstanceParam():value="+value);
    this.parameters.set_name_value(this.SIP_INSTANCE, value);
}

Contact.prototype.removePubGruuParam =function(){
    if(logger!=undefined) logger.debug("Contact:removePubGruuParam()");
    if (this.parameters != null)
    {
        this.parameters.delet(this.PUB_GRUU);
    }
}

Contact.prototype.getPubGruuParam =function(){
    if(logger!=undefined) logger.debug("Contact:getPubGruuParam()");
    return this.parameters.getValue(this.PUB_GRUU);
}

Contact.prototype.setPubGruuParam =function(value){
    if(logger!=undefined) logger.debug("Contact:setPubGruuParam():value="+value);
    this.parameters.set_name_value(this.PUB_GRUU, value);
}

Contact.prototype.removeTempGruuParam =function(){
    if(logger!=undefined) logger.debug("Contact:removeTempGruuParam()");
    if (this.parameters != null)
    {
        this.parameters.delet(this.TEMP_GRUU);
    }
}

Contact.prototype.getTempGruuParam =function(){
    if(logger!=undefined) logger.debug("Contact:getTempGruuParam()");
    return this.parameters.getValue(this.TEMP_GRUU);
}

Contact.prototype.setTempGruuParam =function(value){
    if(logger!=undefined) logger.debug("Contact:setTempGruuParam():value="+value);
    this.parameters.set_name_value(this.TEMP_GRUU, value);
}