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
 *  Implementation of the JAIN-SIP DefaultRouter .
 *  @see  gov/nist/javax/sip/stack/DefaultRouter.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function DefaultRouter() {
    if(logger!=undefined) logger.debug("DefaultRouter:DefaultRouter()");
    this.classname="DefaultRouter"; 
    this.sipStack=null;
    this.defaultRoute=null;
    if(arguments.length!=0)
    {
        var sipStack=arguments[0];
        var defaultRoute=arguments[1];
        this.sipStack = sipStack;
        if (defaultRoute != null) {
            this.defaultRoute =  this.sipStack.getAddressResolver().resolveAddress(new HopImpl(defaultRoute));
        }
    }
}

DefaultRouter.prototype.getNextHop =function(request){
    if(logger!=undefined) logger.debug("DefaultRouter:getNextHop():request="+request);
    var sipRequest = request;
    var requestLine = sipRequest.getRequestLine();
    if (requestLine == null) {
        return this.defaultRoute;
    }
    var requestURI = requestLine.getUri();
    if (requestURI == null)
    {
        console.error("DefaultRouter:getNextHop(): bad message: Null requestURI");
        throw "ViaParser:getNextHop():  bad message: Null requestURI";
    }
    
    var routes = sipRequest.getRouteHeaders();
    if ((routes != null) && (routes.getFirst()!=null)) {
        var route = routes.getFirst();
        var uri = route.getAddress().getURI();
        if (uri.isSipURI()) {
            var sipUri = uri;
            if (!sipUri.hasLrParam()) {
                this.fixStrictRouting(sipRequest);
            }
            var hop = this.createHop(sipUri,request);
            return hop;
        } 
        else {
            console.error("DefaultRouter:getNextHop(): first Route not a SIP URI");
            throw "DefaultRouter:getNextHop(): first Route not a SIP URI";
        }
    }
    else if (requestURI.isSipURI()
        && requestURI.getMAddrParam() != null) {
        hop = this.createHop(requestURI,request);
        return hop;
    } 
    else if (this.defaultRoute != null) {
        return this.defaultRoute;
    } 
    else if (requestURI.isSipURI()) {
        hop = this.createHop(requestURI,request);
        return hop;
    } 
    else {
        return null;
    }
}

DefaultRouter.prototype.fixStrictRouting =function(req){
    if(logger!=undefined) logger.debug("DefaultRouter:fixStrictRouting():req="+req);
    var routes = req.getRouteHeaders();
    var first = routes.getFirst();
    var firstUri = first.getAddress().getURI();
    routes.removeFirst();
    var addr = new AddressImpl();
    addr.setAddress(req.getRequestURI()); // don't clone it
    var route = new Route(addr);
    routes.add(route); // as last one
    req.setRequestURI(firstUri);
}

DefaultRouter.prototype.createHop =function(sipUri,request){
    if(logger!=undefined) logger.debug("DefaultRouter:createHop():sipUri="+sipUri+", request="+request);
    var transport = sipUri.getTransportParam();
    if (transport == null) {
        var via = request.getHeader("Via");
        transport = via.getTransport();
    }
    var port=null;
    if (sipUri.getPort() != -1) {
        port = sipUri.getPort();
    } else {
        port = 5060; // TCP or UDP
    }
    var host = sipUri.getMAddrParam() != null ? sipUri.getMAddrParam(): sipUri.getHost();
    var addressResolver = this.sipStack.getAddressResolver();
    return addressResolver.resolveAddress(new HopImpl(host, port, transport));
}

DefaultRouter.prototype.getOutboundProxy =function(){
    if(logger!=undefined) logger.debug("DefaultRouter:getOutboundProxy()");
    return this.defaultRoute;
}

DefaultRouter.prototype.getNextHops =function(request){
    if(logger!=undefined) logger.debug("DefaultRouter:getNextHops():request="+request);
    try {
        var llist = new Array();
        llist.push(this.getNextHop(request));
        return llist();
    } catch (ex) {
        console.error("DefaultRouter:getNextHops(): catched exception:"+ex);
        return null;
    }
}
