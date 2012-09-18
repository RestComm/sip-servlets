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
 *  Implementation of the JAIN-SIP address factory.
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *  @see  gov/nist/javax/sip/address/AddressFactoryImpl.java 
 *   
 */
 
function AddressFactoryImpl() {
    if(logger!=undefined) logger.debug("AddressFactoryImpl:AddressFactoryImpl()");
    this.classname="AddressFactoryImpl";
}

AddressFactoryImpl.prototype.createAddress =function(){
    if(logger!=undefined) logger.debug("AddressFactoryImpl:createAddress()");
    if(arguments.length==0)
    {
        var addressImpl=new AddressImpl();
        return addressImpl;
    }
    else if(arguments.length==1)
    {
        if(typeof arguments[0]=="string")
        {
            var address=arguments[0];
            var smp=this.createAddress_address(address);
            return smp;
        }
        else if(typeof arguments[0]=="object")
        {
            var uri=arguments[0];
            var addressImpl=this.createAddress_uri(uri);
            return addressImpl;
        }
    }
    else if(arguments.length==2)
    {
        var displayName=arguments[0];
        uri=arguments[1];
        addressImpl=this.createAddress_name_uri(displayName,uri);
        return addressImpl;
    }
    else
    {
        console.error("AddressFactoryImpl:createAddress():  too many arg");
        throw "AddressFactoryImpl:createAddress():  too many arg";
    }
}

AddressFactoryImpl.prototype.createAddress_name_uri =function(displayName,uri){
    if(logger!=undefined) logger.debug("AddressFactoryImpl:createAddress():displayName="+displayName +" uri="+uri);
    if (uri == null)
    {
        console.error("AddressFactoryImpl:createAddress_name_uri():  null uri arg");
        throw "AddressFactoryImpl:createAddress_name_uri():  null uri arg";
    }
    var addressImpl = new AddressImpl();
    if (displayName != null)
    {
        addressImpl.setDisplayName(displayName);
    }
    addressImpl.setURI(uri);
    return addressImpl;
}

AddressFactoryImpl.prototype.createSipURI =function(){
    if(logger!=undefined) logger.debug("AddressFactoryImpl:createSipURI():arguments="+arguments.toString());
    if(arguments.length==0)
    {
        console.error("AddressFactoryImpl:createSipURI(): missing uri arg");
        throw "AddressFactoryImpl:createSipURI():  missing uri arg";
    } 
    else if(arguments.length==1)
    {
        var uri=arguments[0];
        if (uri == null) {
            console.error("AddressFactoryImpl:createSipURI():  null uri arg");
            throw "AddressFactoryImpl:createSipURI():  null uri arg";
        }
        var smp = new StringMsgParser();
        var sipUri = smp.parseSIPUrl(uri);
        return sipUri;
    }
    else if(arguments.length==2)
    {
        var user=arguments[0];
        var host=arguments[1];
        sipUri=this.createSipURI_user_host(user, host);
        return sipUri;
    }      
}

AddressFactoryImpl.prototype.createSipURI_user_host =function(user, host){
    if(logger!=undefined) logger.debug("AddressFactoryImpl:createSipURI(): user="+user+" host:"+host);
    if (host == null) {
        console.error("AddressFactoryImpl:createSipURI_user_host(): null host arg");
        throw "AddressFactoryImpl:createSipURI_user_host():  null host arg";
    }
    var uriString = "sip:";
    if (user != null) {
        uriString=uriString+user+"@";
    }
    //if host is an IPv6 string we should enclose it in sq brackets
    if (host.indexOf(':') != host.lastIndexOf(':')
        && host.trim().charAt(0) != '[') {
        host = '[' + host + ']';
    }
    uriString=uriString+host;
    var smp = new StringMsgParser();
    var sipUri = smp.parseSIPUrl(uriString.toString());
    return sipUri;
}

AddressFactoryImpl.prototype.createTelURL =function(uri){
    if(logger!=undefined) logger.debug("AddressFactoryImpl:createTelURL(): uri="+uri);
    if (uri == null) {
        console.error("AddressFactoryImpl:createTelURL(): null uri arg");
        throw "AddressFactoryImpl:createTelURL():  null uri arg";
    }
    var telUrl = "tel:" + uri;
    var smp = new StringMsgParser();
    var timp = smp.parseUrl(telUrl);
    return timp;
        
}

AddressFactoryImpl.prototype.createAddress_uri =function(uri){
    if(logger!=undefined) logger.debug("AddressFactoryImpl:createAddress_uri(): uri="+uri);
    if (uri == null) {
        console.error("AddressFactoryImpl:createAddress_uri(): null uri arg");
        throw "AddressFactoryImpl:createAddress_uri():  null uri arg";
    }
    var addressImpl = new AddressImpl();
    addressImpl.setURI(uri);
    return addressImpl;
}

AddressFactoryImpl.prototype.createAddress_address =function(address){
    if(logger!=undefined) logger.debug("AddressFactoryImpl:createAddress_address(): address="+address);
    if (address == null) {
        console.error("AddressFactoryImpl:createAddress_address(): null address arg");
        throw "AddressFactoryImpl:createAddress_address():  null address arg";
    }

    if (address.equals("*")) {
        var addressImpl = new AddressImpl();
        addressImpl.setAddressType(addressImpl.wild_card);
        var uri = new SipUri();
        uri.setUser("*");
        addressImpl.setURI(uri);
        return addressImpl;
    } else {
        var smp = new gov.nist.js.parser.StringMsgParser();
        return smp.parseAddress(address);
    }
}

AddressFactoryImpl.prototype.createURI =function(uri){
    if(logger!=undefined) logger.debug("AddressFactoryImpl:createURI(): uri="+uri);
    if (uri == null) {
        console.error("AddressFactoryImpl:createURI(): null uri arg");
        throw "AddressFactoryImpl:createURI():  null uri arg";
    }
    var urlParser = new URLParser(uri);
    var scheme = urlParser.peekScheme();
    if (scheme == null) {
        console.error("AddressFactoryImpl:createURI(): bad scheme");
        throw "AddressFactoryImpl:createURI():  bad scheme"
    }
    if (scheme.toLowerCase()=="sip") {
        return urlParser.sipURL(true);
    } 
    else if (scheme.toLowerCase()=="sips") {
        return urlParser.sipURL(true);
    } 
    else if (scheme.toLowerCase()=="tel") {
        return urlParser.telURL(true);
    }
    return new GenericURI(uri);
}

