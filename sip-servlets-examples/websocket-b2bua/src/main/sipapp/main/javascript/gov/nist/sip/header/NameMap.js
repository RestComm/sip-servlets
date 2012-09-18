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
 *  Implementation of the JAIN-SIP NameMap .
 *  @see  gov/nist/javax/sip/header/NameMap.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function NameMap() {
    if(logger!=undefined) logger.debug("NameMap:NameMap()");
    this.classname="NameMap"; 
    this.nameMap=new Array();
    this.initializeNameMap();
}

NameMap.prototype.putNameMap =function(headerName, className){
    if(logger!=undefined) logger.debug("NameMap:putNameMap():headerName="+headerName+",className="+className);
    var l=null
    for(var i=0;i<this.nameMap.length;i++)
    {
        if(this.nameMap[i][0]==headerName.toLowerCase())
        {
            l=i
        }
    }
    if(l==null)
    {
        var array=new Array();
        array[0]=headerName.toLowerCase();
        array[1]=className
        this.nameMap.push(array);
    }
    else
    {
        this.nameMap[l][0]=headerName.toLowerCase();
        this.nameMap[l][1]=className;
    }
}

NameMap.prototype.getClassFromName =function(headerName){
    if(logger!=undefined) logger.debug("NameMap:getClassFromName()");
    var className=null;
    for(var i=0;i<this.nameMap.length;i++)
    {
        if(this.nameMap[i][0]==headerName.toLowerCase())
        {
            className = this.nameMap[i][1];
        }
    }
    if (className == null)
        return null;
    else {
        return new Function('return new ' + className)();
    }
}

NameMap.prototype.addExtensionHeader =function(headerName, className){
    if(logger!=undefined) logger.debug("NameMap:addExtensionHeader():headerName="+headerName+",className="+className);
    var l=null
    for(var i=0;i<this.nameMap.length;i++)
    {
        if(this.nameMap[i][0]==headerName.toLowerCase())
        {
            l=i
        }
    }
    if(l==null)
    {
        var array=new Array();
        array[0]=headerName.toLowerCase();
        array[1]=className
        this.nameMap.push(array);
    }
    else
    {
        this.nameMap[l][0]=headerName.toLowerCase();
        this.nameMap[l][1]=className;
    }
}

NameMap.prototype.initializeNameMap =function(){
    if(logger!=undefined) logger.debug("NameMap:initializeNameMap()");
    this.nameMap = new Array();
    //this.putNameMap(MinExpires.NAME, MinExpires); // 1
    //this.putNameMap(ErrorInfo.NAME, ErrorInfo); // 2
    //this.putNameMap(MimeVersion.NAME, MimeVersion); // 3
    //this.putNameMap(InReplyTo.NAME, InReplyTo); // 4
    this.putNameMap(new Allow().headerName, new Allow().classname); // 5
    //this.putNameMap(ContentLanguage.NAME, ContentLanguage); // 6
    //this.putNameMap(CALL_INFO, CallInfo); //7
    this.putNameMap(new CSeq().headerName, new CSeq().classname); //8
    //this.putNameMap(ALERT_INFO, AlertInf); //9
    //this.putNameMap(ACCEPT_ENCODING, AcceptEncoding); //10
    //this.putNameMap(ACCEPT, Accept); //11
    //this.putNameMap(ACCEPT_LANGUAGE, AcceptLanguag); //12
    this.putNameMap(new RecordRoute().headerName, new RecordRoute().classname); //13
    this.putNameMap(new TimeStamp().headerName, new TimeStamp().classname); //14
    this.putNameMap(new To().headerName, new To().classname); //15
    this.putNameMap(new Via().headerName, new Via().classname); //16
    this.putNameMap(new From().headerName, new From().classname); //17
    this.putNameMap(new CallID().headerName, new CallID().classname); //18
    this.putNameMap(new Authorization().headerName, new Authorization().classname); //19
    //this.putNameMap(PROXY_AUTHENTICATE, ProxyAuthenticate().classname); //20
    this.putNameMap(new Server().headerName, new Server().classname); //21
    //this.putNameMap(UNSUPPORTED, Unsupported); //22
    //this.putNameMap(RETRY_AFTER, RetryAfter); //23
    this.putNameMap(new ContentType().headerName, new ContentType().classname); //24
    //this.putNameMap(CONTENT_ENCODING, ContentEncoding); //25
    this.putNameMap(new ContentLength().headerName, new ContentLength().classname); //26
    this.putNameMap(new Route().headerName, new Route().classname); //27
    this.putNameMap(new Contact().headerName, new Contact().classname); //28
    this.putNameMap(new WWWAuthenticate().headerName, new WWWAuthenticate().classname); //29
    this.putNameMap(new MaxForwards().headerName, new MaxForwards().classname); //30
    //this.putNameMap(ORGANIZATION, Organization); //31
    this.putNameMap(new ProxyAuthorization().headerName, new ProxyAuthorization().classname); //32
    //this.putNameMap(PROXY_REQUIRE, ProxyRequire); //33
    //this.putNameMap(REQUIRE, Require); //34
    this.putNameMap(new ContentDisposition().headerName, new ContentDisposition().classname); //35
    this.putNameMap(new Subject().headerName, new Subject().classname); //36
    this.putNameMap(new UserAgent().headerName, new UserAgent().classname); //37
    //this.putNameMap(WARNING, Warning); //38
    //this.putNameMap(PRIORITY, Priority); //39
    //this.putNameMap(DATE, SIPDateHeader); //40
    this.putNameMap(new Expires().headerName, new Expires().classname); //41
    this.putNameMap(new Supported().headerName, new Supported().classname); //42
    //this.putNameMap(REPLY_TO, ReplyTo); // 43
    //this.putNameMap(SUBSCRIPTION_STATE, SubscriptionState); //44 
    this.putNameMap(new Event().headerName, new Event().classname); //45
    this.putNameMap(new AllowEvents().headerName, new AllowEvents().classname); //46

    // pmusgrave - extensions
    this.putNameMap("Referred-By", "ReferredBy");
    this.putNameMap("Session-Expires", "SessionExpires");
    this.putNameMap("Min-SE", "MinSE");
    this.putNameMap("Replaces", "Replaces");
    // jean deruelle
    this.putNameMap("Join", "Join");

// IMS Specific headers.
// this.putNameMap(PAccessNetworkInfoHeader.NAME, PAccessNetworkInfo.class);
//   this.putNameMap(PAssertedIdentityHeader.NAME, PAssertedIdentity.class);
//   this.putNameMap(PAssociatedURIHeader.NAME, PAssociatedURI.class);
//  this.putNameMap(PCalledPartyIDHeader.NAME, PCalledPartyID.class);
//   this.putNameMap(PChargingFunctionAddressesHeader.NAME,  PChargingFunctionAddresses.class);
//   this.putNameMap(PChargingVectorHeader.NAME,PChargingVector.class);
//   this.putNameMap(PMediaAuthorizationHeader.NAME,PMediaAuthorization.class);
//   this.putNameMap(Path.NAME, Path.class);
//   this.putNameMap(PPreferredIdentity.NAME, PPreferredIdentity.class);
//  this.putNameMap(Privacy.NAME,Privacy.class);
//   this.putNameMap(ServiceRoute.NAME, ServiceRoute.class);
//   this.putNameMap(PVisitedNetworkID.NAME, PVisitedNetworkID.class);*/
}