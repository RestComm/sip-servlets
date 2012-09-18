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
 *  Implementation of the JAIN-SIP Lexer .
 *  @see  gov/nist/javax/sip/parser/HeaderParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function Lexer(lexerName, buffer) {
    if(logger!=undefined) logger.debug("Lexer:Lexer(): lexerName="+lexerName);
    if(logger!=undefined) logger.debug("Lexer:Lexer(): buffer="+buffer);
    this.classname="Lexer"; 
    this.buffer = buffer;
    this.bufferLen = buffer.length;
    this.ptr = 0;
    this.currentLexer = new Array();
    this.currentLexerName = lexerName;
    this.selectLexer(lexerName);
}

Lexer.prototype = new LexerCore();
Lexer.prototype.constructor=Lexer;
Lexer.prototype.ErrorInfoHeader="Error-Info";
Lexer.prototype.AllowEventsHeader="Allow-Events";
Lexer.prototype.AuthenticationInfoHeader="Authentication-Info";
Lexer.prototype.EventHeader="Event";
Lexer.prototype.MinExpiresHeader="Min-Expires";
Lexer.prototype.RSeqHeader="RSeq";
Lexer.prototype.RAckHeader="RAck";
Lexer.prototype.ReasonHeader="Reason";
Lexer.prototype.ReplyToHeader="Reply-To";
Lexer.prototype.SubscriptionStateHeader="Subscription-State";
Lexer.prototype.TimeStampHeader="Timestamp";
Lexer.prototype.InReplyToHeader="In-Reply-To";
Lexer.prototype.MimeVersionHeader="MIME-Version";
Lexer.prototype.AlertInfoHeader="Alert-Info";
Lexer.prototype.FromHeader="From";
Lexer.prototype.ToHeader="To";
Lexer.prototype.ReferToHeader="Refer-To";
Lexer.prototype.ViaHeader="Via";
Lexer.prototype.UserAgentHeader="User-Agent";
Lexer.prototype.ServerHeader="Server";
Lexer.prototype.AcceptEncodingHeader="Accept-Encoding";
Lexer.prototype.AcceptHeader="Accept";
Lexer.prototype.AllowHeader="Allow";
Lexer.prototype.RouteHeader="Route";
Lexer.prototype.AuthorizationHeader="Authorization";
Lexer.prototype.ProxyAuthorizationHeader="Proxy-Authorization";
Lexer.prototype.RetryAfterHeader="Retry-After";
Lexer.prototype.ProxyRequireHeader="Proxy-Require";
Lexer.prototype.ContentLanguageHeader="Content-Language";
Lexer.prototype.UnsupportedHeader="Unsupported";
Lexer.prototype.SupportedHeader="Supported";
Lexer.prototype.WarningHeader="Warning";
Lexer.prototype.MaxForwardsHeader="Max-Forwards";
Lexer.prototype.DateHeader="Date";
Lexer.prototype.PriorityHeader="Priority";
Lexer.prototype.ProxyAuthenticateHeader="Proxy-Authenticate";
Lexer.prototype.ContentEncodingHeader="Content-Encoding";
Lexer.prototype.ContentLengthHeader="Content-Length";
Lexer.prototype.SubjectHeader="Subject";
Lexer.prototype.ContentTypeHeader="Content-Type";
Lexer.prototype.ContactHeader="Contact";
Lexer.prototype.CallIdHeader="Call-ID";
Lexer.prototype.RequireHeader="Require";
Lexer.prototype.ExpiresHeader="Expires";
Lexer.prototype.RecordRouteHeader="Record-Route";
Lexer.prototype.OrganizationHeader="Organization";
Lexer.prototype.CSeqHeader="CSeq";
Lexer.prototype.AcceptLanguageHeader="Accept-Language";
Lexer.prototype.WWWAuthenticateHeader="WWW-Authenticate";
Lexer.prototype.CallInfoHeader="Call-Info";
Lexer.prototype.ContentDispositionHeader="Content-Disposition";
Lexer.prototype.SIPETagHeader="SIP-ETag";
Lexer.prototype.SIPIfMatchHeader="SIP-If-Match";
Lexer.prototype.SessionExpiresHeader="Session-Expires";
Lexer.prototype.MinSEHeader="Min-SE";
Lexer.prototype.ReferredByHeader="Referred-By";
Lexer.prototype.ReplacesHeader="Replaces";
Lexer.prototype.JoinHeader="Join";
Lexer.prototype.PathHeader="Path";
Lexer.prototype.ServiceRouteHeader="Service-Route";
Lexer.prototype.PAssertedIdentityHeader="P-Asserted-Identity";
Lexer.prototype.PPreferredIdentityHeader="P-Preferred-Identity";
Lexer.prototype.PrivacyHeader="Privacy";
Lexer.prototype.PCalledPartyIDHeader="P-Called-Party-ID";
Lexer.prototype.PAssociatedURIHeader="P-Associated-URI";
Lexer.prototype.PVisitedNetworkIDHeader="P-Visited-Network-ID";
Lexer.prototype.PChargingFunctionAddressesHeader="P-Charging-Function-Addresses";
Lexer.prototype.PChargingVectorHeader="P-Charging-Vector";
Lexer.prototype.PAccessNetworkInfoHeader="P-Access-Network-Info";
Lexer.prototype.PMediaAuthorizationHeader="P-Media-Authorization";
Lexer.prototype.SecurityServerHeader="Security-Server";
Lexer.prototype.SecurityVerifyHeader="Security-Verify";
Lexer.prototype.SecurityClientHeader="Security-Client";
Lexer.prototype.PUserDatabaseHeader="P-User-Database";
Lexer.prototype.PProfileKeyHeader="P-Profile-Key";
Lexer.prototype.PServedUserHeader="P-Served-User";
Lexer.prototype.PPreferredServiceHeader="P-Preferred-Service";
Lexer.prototype.PAssertedServiceHeader="P-Asserted-Service";
Lexer.prototype.ReferencesHeader="References";


Lexer.prototype.getHeaderName =function(line){
    if(logger!=undefined) logger.debug("Lexer:getHeaderName():line="+line);
    if (line == null)
    {
        return null;
    }
    var headerName = null;
    try {
        var begin = line.indexOf(":");
        headerName = null;
        if (begin >= 1)
        {
            headerName = line.substring(0, begin).replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
        }
    } catch (ex) {
        console.error("Lexer:getHeaderName(): catched exception:"+ex);
        return null;
    }
    return headerName;
}
Lexer.prototype.getHeaderValue =function(line){
    if(logger!=undefined) logger.debug("Lexer:getHeaderValue():line="+line);
    if (line == null)
    {
        return null;
    }
    var headerValue = null;
    try {
        var begin = line.indexOf(":");
        headerValue = line.substring(begin + 1);
    } catch (ex) {
        console.error("Lexer:getHeaderValue(): catched exception:"+ex);
        return null;
    }
    return headerValue;
}
Lexer.prototype.selectLexer =function(lexerName){
    if(logger!=undefined) logger.debug("Lexer:selectLexer():lexerName="+lexerName);
    //in javascript, we can not realize thread, so i ignore the key work synchronized in the function
    var n=null;
    for(var i=0;i<this.lexerTables.length;i++)
    {
        if(this.lexerTables[i][0]==lexerName)
        {
            n=i;
        }
    }
    if(n!=null)
    {
        this.currentLexer = this.lexerTables[n][1];
    }
    this.currentLexerName = lexerName;
    if (this.currentLexer.length == 0) {
        this.addLexer(lexerName);
        if (lexerName=="method_keywordLexer") {
            this.addKeyword(TokenNames.prototype.REGISTER, TokenTypes.prototype.REGISTER);
            this.addKeyword(TokenNames.prototype.ACK, TokenTypes.prototype.ACK);
            this.addKeyword(TokenNames.prototype.OPTIONS, TokenTypes.prototype.OPTIONS);
            this.addKeyword(TokenNames.prototype.BYE, TokenTypes.prototype.BYE);
            this.addKeyword(TokenNames.prototype.INVITE, TokenTypes.prototype.INVITE);
            this.addKeyword(TokenNames.prototype.SIP.toUpperCase(), TokenTypes.prototype.SIP);
            this.addKeyword(TokenNames.prototype.SIPS.toUpperCase(), TokenTypes.prototype.SIPS);
            this.addKeyword(TokenNames.prototype.SUBSCRIBE, TokenTypes.prototype.SUBSCRIBE);
            this.addKeyword(TokenNames.prototype.NOTIFY, TokenTypes.prototype.NOTIFY);
            this.addKeyword(TokenNames.prototype.MESSAGE, TokenTypes.prototype.MESSAGE);
            this.addKeyword(TokenNames.prototype.PUBLISH, TokenTypes.prototype.PUBLISH);

        } else if (lexerName=="command_keywordLexer") {
            this.addKeyword(this.ErrorInfoHeader.toUpperCase(),
                TokenTypes.prototype.ERROR_INFO);
            this.addKeyword(this.AllowEventsHeader.toUpperCase(),
                TokenTypes.prototype.ALLOW_EVENTS);
            this.addKeyword(this.AuthenticationInfoHeader.toUpperCase(),
                TokenTypes.prototype.AUTHENTICATION_INFO);
            this.addKeyword(this.EventHeader.toUpperCase(), TokenTypes.prototype.EVENT);
            this.addKeyword(this.MinExpiresHeader.toUpperCase(),
                TokenTypes.prototype.MIN_EXPIRES);
            this.addKeyword(this.RSeqHeader.toUpperCase(), TokenTypes.prototype.RSEQ);
            this.addKeyword(this.RAckHeader.toUpperCase(), TokenTypes.prototype.RACK);
            this.addKeyword(this.ReasonHeader.toUpperCase(),
                TokenTypes.prototype.REASON);
            this.addKeyword(this.ReplyToHeader.toUpperCase(),
                TokenTypes.prototype.REPLY_TO);
            this.addKeyword(this.SubscriptionStateHeader.toUpperCase(),
                TokenTypes.prototype.SUBSCRIPTION_STATE);
            this.addKeyword(this.TimeStampHeader.toUpperCase(),
                TokenTypes.prototype.TIMESTAMP);
            this.addKeyword(this.InReplyToHeader.toUpperCase(),
                TokenTypes.prototype.IN_REPLY_TO);
            this.addKeyword(this.MimeVersionHeader.toUpperCase(),
                TokenTypes.prototype.MIME_VERSION);
            this.addKeyword(this.AlertInfoHeader.toUpperCase(),
                TokenTypes.prototype.ALERT_INFO);
            this.addKeyword(this.FromHeader.toUpperCase(), TokenTypes.prototype.FROM);
            this.addKeyword(this.ToHeader.toUpperCase(), TokenTypes.prototype.TO);
            this.addKeyword(this.ReferToHeader.toUpperCase(),
                TokenTypes.prototype.REFER_TO);
            this.addKeyword(this.ViaHeader.toUpperCase(), TokenTypes.prototype.VIA);
            this.addKeyword(this.UserAgentHeader.toUpperCase(),
                TokenTypes.prototype.USER_AGENT);
            this.addKeyword(this.ServerHeader.toUpperCase(),
                TokenTypes.prototype.SERVER);
            this.addKeyword(this.AcceptEncodingHeader.toUpperCase(),
                TokenTypes.prototype.ACCEPT_ENCODING);
            this.addKeyword(this.AcceptHeader.toUpperCase(),
                TokenTypes.prototype.ACCEPT);
            this.addKeyword(this.AllowHeader.toUpperCase(), TokenTypes.prototype.ALLOW);
            this.addKeyword(this.RouteHeader.toUpperCase(), TokenTypes.prototype.ROUTE);
            this.addKeyword(this.AuthorizationHeader.toUpperCase(),
                TokenTypes.prototype.AUTHORIZATION);
            this.addKeyword(this.ProxyAuthorizationHeader.toUpperCase(),
                TokenTypes.prototype.PROXY_AUTHORIZATION);
            this.addKeyword(this.RetryAfterHeader.toUpperCase(),
                TokenTypes.prototype.RETRY_AFTER);
            this.addKeyword(this.ProxyRequireHeader.toUpperCase(),
                TokenTypes.prototype.PROXY_REQUIRE);
            this.addKeyword(this.ContentLanguageHeader.toUpperCase(),
                TokenTypes.prototype.CONTENT_LANGUAGE);
            this.addKeyword(this.UnsupportedHeader.toUpperCase(),
                TokenTypes.prototype.UNSUPPORTED);
            this.addKeyword(this.SupportedHeader.toUpperCase(),
                TokenTypes.prototype.SUPPORTED);
            this.addKeyword(this.WarningHeader.toUpperCase(),
                TokenTypes.prototype.WARNING);
            this.addKeyword(this.MaxForwardsHeader.toUpperCase(),
                TokenTypes.prototype.MAX_FORWARDS);
            this.addKeyword(this.DateHeader.toUpperCase(), TokenTypes.prototype.DATE);
            this.addKeyword(this.PriorityHeader.toUpperCase(),
                TokenTypes.prototype.PRIORITY);
            this.addKeyword(this.ProxyAuthenticateHeader.toUpperCase(),
                TokenTypes.prototype.PROXY_AUTHENTICATE);
            this.addKeyword(this.ContentEncodingHeader.toUpperCase(),
                TokenTypes.prototype.CONTENT_ENCODING);
            this.addKeyword(this.ContentLengthHeader.toUpperCase(),
                TokenTypes.prototype.CONTENT_LENGTH);
            this.addKeyword(this.SubjectHeader.toUpperCase(),
                TokenTypes.prototype.SUBJECT);
            this.addKeyword(this.ContentTypeHeader.toUpperCase(),
                TokenTypes.prototype.CONTENT_TYPE);
            this.addKeyword(this.ContactHeader.toUpperCase(),
                TokenTypes.prototype.CONTACT);
            this.addKeyword(this.CallIdHeader.toUpperCase(),
                TokenTypes.prototype.CALL_ID);
            this.addKeyword(this.RequireHeader.toUpperCase(),
                TokenTypes.prototype.REQUIRE);
            this.addKeyword(this.ExpiresHeader.toUpperCase(),
                TokenTypes.prototype.EXPIRES);
            this.addKeyword(this.RecordRouteHeader.toUpperCase(),
                TokenTypes.prototype.RECORD_ROUTE);
            this.addKeyword(this.OrganizationHeader.toUpperCase(),
                TokenTypes.prototype.ORGANIZATION);
            this.addKeyword(this.CSeqHeader.toUpperCase(), TokenTypes.prototype.CSEQ);
            this.addKeyword(this.AcceptLanguageHeader.toUpperCase(),
                TokenTypes.prototype.ACCEPT_LANGUAGE);
            this.addKeyword(this.WWWAuthenticateHeader.toUpperCase(),
                TokenTypes.prototype.WWW_AUTHENTICATE);
            this.addKeyword(this.CallInfoHeader.toUpperCase(),
                TokenTypes.prototype.CALL_INFO);
            this.addKeyword(this.ContentDispositionHeader.toUpperCase(),
                TokenTypes.prototype.CONTENT_DISPOSITION);
            // And now the dreaded short forms....
            this.addKeyword(TokenNames.prototype.K.toUpperCase(), TokenTypes.prototype.SUPPORTED);
            this.addKeyword(TokenNames.prototype.C.toUpperCase(),
                TokenTypes.prototype.CONTENT_TYPE);
            this.addKeyword(TokenNames.prototype.E.toUpperCase(),
                TokenTypes.prototype.CONTENT_ENCODING);
            this.addKeyword(TokenNames.prototype.F.toUpperCase(), TokenTypes.prototype.FROM);
            this.addKeyword(TokenNames.prototype.I.toUpperCase(), TokenTypes.prototype.CALL_ID);
            this.addKeyword(TokenNames.prototype.M.toUpperCase(), TokenTypes.prototype.CONTACT);
            this.addKeyword(TokenNames.prototype.L.toUpperCase(),
                TokenTypes.prototype.CONTENT_LENGTH);
            this.addKeyword(TokenNames.prototype.S.toUpperCase(), TokenTypes.prototype.SUBJECT);
            this.addKeyword(TokenNames.prototype.T.toUpperCase(), TokenTypes.prototype.TO);
            this.addKeyword(TokenNames.prototype.U.toUpperCase(),
                TokenTypes.prototype.ALLOW_EVENTS); // JvB: added
            this.addKeyword(TokenNames.prototype.V.toUpperCase(), TokenTypes.prototype.VIA);
            this.addKeyword(TokenNames.prototype.R.toUpperCase(), TokenTypes.prototype.REFER_TO);
            this.addKeyword(TokenNames.prototype.O.toUpperCase(), TokenTypes.prototype.EVENT); // Bug fix by Mario Mantak
            this.addKeyword(TokenNames.prototype.X.toUpperCase(), TokenTypes.prototype.SESSIONEXPIRES_TO); // Bug fix by Jozef Saniga
                    
            // JvB: added to support RFC3903
            this.addKeyword(this.SIPETagHeader.toUpperCase(),
                TokenTypes.prototype.SIP_ETAG);
            this.addKeyword(this.SIPIfMatchHeader.toUpperCase(),
                TokenTypes.prototype.SIP_IF_MATCH);

            // pmusgrave: Add RFC4028 and ReferredBy
            this.addKeyword(this.SessionExpiresHeader.toUpperCase(),
                TokenTypes.prototype.SESSIONEXPIRES_TO);
            this.addKeyword(this.MinSEHeader.toUpperCase(),
                TokenTypes.prototype.MINSE_TO);
            this.addKeyword(this.ReferredByHeader.toUpperCase(), TokenTypes.prototype.REFERREDBY_TO);
            this.addKeyword(TokenNames.prototype.B.toUpperCase(), TokenTypes.prototype.REFERREDBY_TO); // Bug fix OrangeLabs, AUFFRET Jean-Marc


            // pmusgrave RFC3891
            this.addKeyword(this.ReplacesHeader.toUpperCase(),
                TokenTypes.prototype.REPLACES_TO);
            //jean deruelle RFC3911
            this.addKeyword(this.JoinHeader.toUpperCase(),
                TokenTypes.prototype.JOIN_TO);

            // IMS Headers
            this.addKeyword(this.PathHeader.toUpperCase(), TokenTypes.prototype.PATH);
            this.addKeyword(this.ServiceRouteHeader.toUpperCase(),
                TokenTypes.prototype.SERVICE_ROUTE);
            this.addKeyword(this.PAssertedIdentityHeader.toUpperCase(),
                TokenTypes.prototype.P_ASSERTED_IDENTITY);
            this.addKeyword(this.PPreferredIdentityHeader.toUpperCase(),
                TokenTypes.prototype.P_PREFERRED_IDENTITY);
            this.addKeyword(this.PrivacyHeader.toUpperCase(),
                TokenTypes.prototype.PRIVACY);

            // issued by Miguel Freitas
            this.addKeyword(this.PCalledPartyIDHeader.toUpperCase(),
                TokenTypes.prototype.P_CALLED_PARTY_ID);
            this.addKeyword(this.PAssociatedURIHeader.toUpperCase(),
                TokenTypes.prototype.P_ASSOCIATED_URI);
            this.addKeyword(this.PVisitedNetworkIDHeader.toUpperCase(),
                TokenTypes.prototype.P_VISITED_NETWORK_ID);
            this.addKeyword(this.PChargingFunctionAddressesHeader
                .toUpperCase(),
                TokenTypes.prototype.P_CHARGING_FUNCTION_ADDRESSES);
            this.addKeyword(this.PChargingVectorHeader.toUpperCase(),
                TokenTypes.prototype.P_VECTOR_CHARGING);
            this.addKeyword(this.PAccessNetworkInfoHeader.toUpperCase(),
                TokenTypes.prototype.P_ACCESS_NETWORK_INFO);
            this.addKeyword(this.PMediaAuthorizationHeader.toUpperCase(),
                TokenTypes.prototype.P_MEDIA_AUTHORIZATION);

            this.addKeyword(this.SecurityServerHeader.toUpperCase(),
                TokenTypes.prototype.SECURITY_SERVER);
            this.addKeyword(this.SecurityVerifyHeader.toUpperCase(),
                TokenTypes.prototype.SECURITY_VERIFY);
            this.addKeyword(this.SecurityClientHeader.toUpperCase(),
                TokenTypes.prototype.SECURITY_CLIENT);

            // added by aayush@rancore
            this.addKeyword(this.PUserDatabaseHeader.toUpperCase(),
                TokenTypes.prototype.P_USER_DATABASE);

            // added by aayush@rancore
            this.addKeyword(this.PProfileKeyHeader.toUpperCase(),
                TokenTypes.prototype.P_PROFILE_KEY);

            // added by aayush@rancore
            this.addKeyword(this.PServedUserHeader.toUpperCase(),
                TokenTypes.prototype.P_SERVED_USER);

            // added by aayush@rancore
            this.addKeyword(this.PPreferredServiceHeader.toUpperCase(),
                TokenTypes.prototype.P_PREFERRED_SERVICE);

            // added by aayush@rancore
            this.addKeyword(this.PAssertedServiceHeader.toUpperCase(),
                TokenTypes.prototype.P_ASSERTED_SERVICE);
                    
            // added References header
            this.addKeyword(this.ReferencesHeader.toUpperCase(),TokenTypes.prototype.REFERENCES);
        } else if (lexerName=="status_lineLexer") {
            this.addKeyword(TokenNames.prototype.SIP.toUpperCase(), TokenTypes.prototype.SIP);
        } else if (lexerName=="request_lineLexer") {
            this.addKeyword(TokenNames.prototype.SIP.toUpperCase(), TokenTypes.prototype.SIP);
        } else if (lexerName=="sip_urlLexer") {
            this.addKeyword(TokenNames.prototype.TEL.toUpperCase(), TokenTypes.prototype.TEL);
            this.addKeyword(TokenNames.prototype.SIP.toUpperCase(), TokenTypes.prototype.SIP);
            this.addKeyword(TokenNames.prototype.SIPS.toUpperCase(), TokenTypes.prototype.SIPS);
        }
    }
}