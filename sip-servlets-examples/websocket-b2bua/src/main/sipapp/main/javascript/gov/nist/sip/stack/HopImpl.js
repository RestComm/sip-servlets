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
 *  Implementation of the JAIN-SIP HopImpl .
 *  @see  gov/nist/javax/sip/stack/HopImpl.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function HopImpl() {
    if(logger!=undefined) logger.debug("HopImpl:HopImpl()");
    this.classname="HopImpl";
    this.host=null;
    this.port=null;
    this.transport="WS";
    this.defaultRoute=null; // This is generated from the proxy addr
    this.uriRoute=null;
    this.wsurl=null;
    if(arguments.length==1)
    {
        var hop=arguments[0];
        if(logger!=undefined) logger.debug("HopImpl:HopImpl(): hop="+hop);
        if (hop == null)
        {
            console.error("HopImpl:HopImpl(): null arg!");
            throw "HopImpl:HopImpl(): null arg!";
        }
        var brack = hop.indexOf(']');
        var colon = hop.indexOf(':',brack);
        var slash = hop.indexOf('/',colon);
        if (colon>0) {
            this.host = hop.substring(0,colon);
            var portstr;
            if (slash>0) {
                portstr = hop.substring(colon+1,slash);
                this.transport = hop.substring(slash+1);
            } else {
                portstr = hop.substring(colon+1);
            }
            this.port = portstr-0;
        } else {
            if (slash>0) {
                this.host = hop.substring(0,slash);
                this.transport = hop.substring(slash+1);
                this.port = 8080;
            } else {
                this.host = hop;
                this.port = 8080;
            }
        }
        if (this.host == null || this.host.length == 0)
        {
            console.error("HopImpl:HopImpl(): no host!");
            throw "HopImpl:HopImpl(): no host!";
        }
        this.host = this.host.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
        this.transport = this.transport.replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
        if ((brack>0) && this.host.charAt(0)!='[') {
            console.error("HopImpl:HopImpl(): bad IPv6 reference spec");
            throw "HopImpl:HopImpl(): bad IPv6 reference spec";
        }
        if ((this.transport.toLowerCase()!="tcp") && (this.transport.toLowerCase()!= "ws") ) {
            console.error("HopImpl:HopImpl(): bad transport string " + this.transport);
            throw "HopImpl:HopImpl(): bad transport string " + this.transport;
        }
    }
    else if(arguments.length==3)
    {
        var hostName = arguments[0];
        var portNumber = arguments[1];
        var trans = arguments[2];
        if(logger!=undefined) logger.debug("HopImpl:HopImpl(): hostName="+hostName);
        if(logger!=undefined) logger.debug("HopImpl:HopImpl(): portNumber="+portNumber);
        if(logger!=undefined) logger.debug("HopImpl:HopImpl(): trans="+trans);
        this.host = hostName;
        if(this.host.indexOf(":") >= 0)
        {
            if(this.host.indexOf("[") < 0)
            {
                this.host = "[" + this.host + "]";
            }
        }
        this.port = portNumber;
        this.transport = trans;
    }
}

HopImpl.prototype.toString =function(){
    if(logger!=undefined) logger.debug("HopImpl:toString()");
    return this.host + ":" +this.port + "/" + this.transport;
}

HopImpl.prototype.getHost =function(){
    if(logger!=undefined) logger.debug("HopImpl:getHost()");
    return this.host;
}

HopImpl.prototype.getPort =function(){
    if(logger!=undefined) logger.debug("HopImpl:getPort()");
    return this.port;
}

HopImpl.prototype.getTransport =function(){
    if(logger!=undefined) logger.debug("HopImpl:getTransport()");
    return this.transport;
}

HopImpl.prototype.getURLWS =function(){
    if(logger!=undefined) logger.debug("HopImpl:getURLWS()");
    return this.wsurl;
}

HopImpl.prototype.isURIRoute =function(){
    if(logger!=undefined) logger.debug("HopImpl:isURIRoute()");
    return this.uriRoute;
}

HopImpl.prototype.setURIRouteFlag =function(){
    if(logger!=undefined) logger.debug("HopImpl:setURIRouteFlag()");
    this.uriRoute=true;
}

HopImpl.prototype.setURLWS =function(wsurl){
    if(logger!=undefined) logger.debug("HopImpl:setURLWS():wsurl="+wsurl);
    this.wsurl=wsurl;
}
