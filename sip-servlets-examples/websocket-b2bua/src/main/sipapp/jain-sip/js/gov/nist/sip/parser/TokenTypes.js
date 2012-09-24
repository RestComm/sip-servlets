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
 *  Implementation of the JAIN-SIP TokenTypes .
 *  @see  gov/nist/javax/sip/parser/TokenTypes.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function TokenTypes() {
    if(logger!=undefined) logger.debug("TokenTypes:TokenTypes()");
    this.classname="TokenTypes"; 
}

TokenTypes.prototype.constructor=Parser;
TokenTypes.prototype.START = LexerCore.prototype.START;
// Everything under this is reserved
TokenTypes.prototype.END = LexerCore.prototype.END;
// End markder.

TokenTypes.prototype.SIP = LexerCore.prototype.START + 3;
TokenTypes.prototype.REGISTER = LexerCore.prototype.START + 4;
TokenTypes.prototype.INVITE = LexerCore.prototype.START + 5;
TokenTypes.prototype.ACK = LexerCore.prototype.START + 6;
TokenTypes.prototype.BYE = LexerCore.prototype.START + 7;
TokenTypes.prototype.OPTIONS = LexerCore.prototype.START + 8;
TokenTypes.prototype.CANCEL = LexerCore.prototype.START + 9;
TokenTypes.prototype.ERROR_INFO = LexerCore.prototype.START + 10;
TokenTypes.prototype.IN_REPLY_TO = LexerCore.prototype.START + 11;
TokenTypes.prototype.MIME_VERSION = LexerCore.prototype.START + 12;
TokenTypes.prototype.ALERT_INFO = LexerCore.prototype.START + 13;
TokenTypes.prototype.FROM = LexerCore.prototype.START + 14;
TokenTypes.prototype.TO = LexerCore.prototype.START + 15;
TokenTypes.prototype.VIA = LexerCore.prototype.START + 16;
TokenTypes.prototype.USER_AGENT = LexerCore.prototype.START + 17;
TokenTypes.prototype.SERVER = LexerCore.prototype.START + 18;
TokenTypes.prototype.ACCEPT_ENCODING = LexerCore.prototype.START + 19;
TokenTypes.prototype.ACCEPT = LexerCore.prototype.START + 20;
TokenTypes.prototype.ALLOW = LexerCore.prototype.START + 21;
TokenTypes.prototype.ROUTE = LexerCore.prototype.START + 22;
TokenTypes.prototype.AUTHORIZATION = LexerCore.prototype.START + 23;
TokenTypes.prototype.PROXY_AUTHORIZATION = LexerCore.prototype.START + 24;
TokenTypes.prototype.RETRY_AFTER = LexerCore.prototype.START + 25;
TokenTypes.prototype.PROXY_REQUIRE = LexerCore.prototype.START + 26;
TokenTypes.prototype.CONTENT_LANGUAGE = LexerCore.prototype.START + 27;
TokenTypes.prototype.UNSUPPORTED = LexerCore.prototype.START + 28;
TokenTypes.prototype.SUPPORTED = LexerCore.prototype.START + 20;
TokenTypes.prototype.WARNING = LexerCore.prototype.START + 30;
TokenTypes.prototype.MAX_FORWARDS = LexerCore.prototype.START + 31;
TokenTypes.prototype.DATE = LexerCore.prototype.START + 32;
TokenTypes.prototype.PRIORITY = LexerCore.prototype.START + 33;
TokenTypes.prototype.PROXY_AUTHENTICATE = LexerCore.prototype.START + 34;
TokenTypes.prototype.CONTENT_ENCODING = LexerCore.prototype.START + 35;
TokenTypes.prototype.CONTENT_LENGTH = LexerCore.prototype.START + 36;
TokenTypes.prototype.SUBJECT = LexerCore.prototype.START + 37;
TokenTypes.prototype.CONTENT_TYPE = LexerCore.prototype.START + 38;
TokenTypes.prototype.CONTACT = LexerCore.prototype.START + 39;
TokenTypes.prototype.CALL_ID = LexerCore.prototype.START + 40;
TokenTypes.prototype.REQUIRE = LexerCore.prototype.START + 41;
TokenTypes.prototype.EXPIRES = LexerCore.prototype.START + 42;
TokenTypes.prototype.ENCRYPTION = LexerCore.prototype.START + 43;
TokenTypes.prototype.RECORD_ROUTE = LexerCore.prototype.START + 44;
TokenTypes.prototype.ORGANIZATION = LexerCore.prototype.START + 45;
TokenTypes.prototype.CSEQ = LexerCore.prototype.START + 46;
TokenTypes.prototype.ACCEPT_LANGUAGE = LexerCore.prototype.START + 47;
TokenTypes.prototype.WWW_AUTHENTICATE = LexerCore.prototype.START + 48;
TokenTypes.prototype.RESPONSE_KEY = LexerCore.prototype.START + 49;
TokenTypes.prototype.HIDE = LexerCore.prototype.START + 50;
TokenTypes.prototype.CALL_INFO = LexerCore.prototype.START + 51;
TokenTypes.prototype.CONTENT_DISPOSITION = LexerCore.prototype.START + 52;
TokenTypes.prototype.SUBSCRIBE = LexerCore.prototype.START + 53;
TokenTypes.prototype.NOTIFY = LexerCore.prototype.START + 54;
TokenTypes.prototype.TIMESTAMP = LexerCore.prototype.START + 55;
TokenTypes.prototype.SUBSCRIPTION_STATE = LexerCore.prototype.START + 56;
TokenTypes.prototype.TEL = LexerCore.prototype.START + 57;
TokenTypes.prototype.REPLY_TO = LexerCore.prototype.START + 58;
TokenTypes.prototype.REASON = LexerCore.prototype.START + 59;
TokenTypes.prototype.RSEQ = LexerCore.prototype.START + 60;
TokenTypes.prototype.RACK = LexerCore.prototype.START + 61;
TokenTypes.prototype.MIN_EXPIRES = LexerCore.prototype.START + 62;
TokenTypes.prototype.EVENT = LexerCore.prototype.START + 63;
TokenTypes.prototype.AUTHENTICATION_INFO = LexerCore.prototype.START + 64;
TokenTypes.prototype.ALLOW_EVENTS = LexerCore.prototype.START + 65;
TokenTypes.prototype.REFER_TO = LexerCore.prototype.START + 66;

// JvB: added to support RFC3903
TokenTypes.prototype.PUBLISH = LexerCore.prototype.START + 67;
TokenTypes.prototype.SIP_ETAG = LexerCore.prototype.START + 68;
TokenTypes.prototype.SIP_IF_MATCH = LexerCore.prototype.START + 69;




TokenTypes.prototype.MESSAGE = LexerCore.prototype.START + 70;

// IMS Headers
TokenTypes.prototype.PATH = LexerCore.prototype.START + 71;
TokenTypes.prototype.SERVICE_ROUTE = LexerCore.prototype.START + 72;
TokenTypes.prototype.P_ASSERTED_IDENTITY = LexerCore.prototype.START + 73;
TokenTypes.prototype.P_PREFERRED_IDENTITY = LexerCore.prototype.START + 74;
TokenTypes.prototype.P_VISITED_NETWORK_ID = LexerCore.prototype.START + 75;
TokenTypes.prototype.P_CHARGING_FUNCTION_ADDRESSES = LexerCore.prototype.START + 76;
TokenTypes.prototype.P_VECTOR_CHARGING = LexerCore.prototype.START + 77;



// issued by Miguel Freitas - IMS headers
TokenTypes.prototype.PRIVACY = LexerCore.prototype.START + 78;
TokenTypes.prototype.P_ACCESS_NETWORK_INFO = LexerCore.prototype.START + 79;
TokenTypes.prototype.P_CALLED_PARTY_ID = LexerCore.prototype.START + 80;
TokenTypes.prototype.P_ASSOCIATED_URI = LexerCore.prototype.START + 81;
TokenTypes.prototype.P_MEDIA_AUTHORIZATION = LexerCore.prototype.START + 82;
TokenTypes.prototype.P_MEDIA_AUTHORIZATION_TOKEN = LexerCore.prototype.START + 83;


// pmusgrave - additions
TokenTypes.prototype.REFERREDBY_TO = LexerCore.prototype.START + 84;

// pmusgrave RFC4028
TokenTypes.prototype.SESSIONEXPIRES_TO = LexerCore.prototype.START + 85;
TokenTypes.prototype.MINSE_TO = LexerCore.prototype.START + 86;

// pmusgrave RFC3891
TokenTypes.prototype.REPLACES_TO = LexerCore.prototype.START + 87;

// pmusgrave sips bug fix
TokenTypes.prototype.SIPS = LexerCore.prototype.START + 88;


// issued by Miguel Freitas - SIP Security Agreement (RFC3329)
TokenTypes.prototype.SECURITY_SERVER = LexerCore.prototype.START + 89;
TokenTypes.prototype.SECURITY_CLIENT = LexerCore.prototype.START + 90;
TokenTypes.prototype.SECURITY_VERIFY = LexerCore.prototype.START + 91;

// jean deruelle RFC3911
TokenTypes.prototype.JOIN_TO = LexerCore.prototype.START + 92;

// aayush.bhatnagar: RFC 4457 support.
TokenTypes.prototype.P_USER_DATABASE = LexerCore.prototype.START + 93;
//aayush.bhatnagar: RFC 5002 support.
TokenTypes.prototype.P_PROFILE_KEY = LexerCore.prototype.START + 94;
//aayush.bhatnagar: RFC 5502 support.
TokenTypes.prototype.P_SERVED_USER = LexerCore.prototype.START + 95;
//aayush.bhatnaagr: P-Preferred-Service Header:
TokenTypes.prototype.P_PREFERRED_SERVICE = LexerCore.prototype.START + 96;
//aayush.bhatnagar: P-Asserted-Service Header:
TokenTypes.prototype.P_ASSERTED_SERVICE = LexerCore.prototype.START + 97;
//mranga - References header
TokenTypes.prototype.REFERENCES = LexerCore.prototype.START + 98;

TokenTypes.prototype.ALPHA = LexerCore.prototype.ALPHA;
TokenTypes.prototype.DIGIT = LexerCore.prototype.DIGIT;
TokenTypes.prototype.ID = LexerCore.prototype.ID;
TokenTypes.prototype.WHITESPACE = LexerCore.prototype.WHITESPACE;
TokenTypes.prototype.BACKSLASH = LexerCore.prototype.BACKSLASH;
TokenTypes.prototype.QUOTE = LexerCore.prototype.QUOTE;
TokenTypes.prototype.AT = LexerCore.prototype.AT;
TokenTypes.prototype.SP = LexerCore.prototype.SP;
TokenTypes.prototype.HT = LexerCore.prototype.HT;
TokenTypes.prototype.COLON = LexerCore.prototype.COLON;
TokenTypes.prototype.STAR = LexerCore.prototype.STAR;
TokenTypes.prototype.DOLLAR = LexerCore.prototype.DOLLAR;
TokenTypes.prototype.PLUS = LexerCore.prototype.PLUS;
TokenTypes.prototype.POUND = LexerCore.prototype.POUND;
TokenTypes.prototype.MINUS = LexerCore.prototype.MINUS;
TokenTypes.prototype.DOUBLEQUOTE = LexerCore.prototype.DOUBLEQUOTE;
TokenTypes.prototype.TILDE = LexerCore.prototype.TILDE;
TokenTypes.prototype.BACK_QUOTE = LexerCore.prototype.BACK_QUOTE;
TokenTypes.prototype.NULL = LexerCore.prototype.NULL;
TokenTypes.prototype.EQUALS =  '='.charCodeAt(0);
TokenTypes.prototype.SEMICOLON =  ';'.charCodeAt(0);
TokenTypes.prototype.SLASH =  '/'.charCodeAt(0);
TokenTypes.prototype.L_SQUARE_BRACKET =  '['.charCodeAt(0);
TokenTypes.prototype.R_SQUARE_BRACKET =  ']'.charCodeAt(0);
TokenTypes.prototype.R_CURLY =  '}'.charCodeAt(0);
TokenTypes.prototype.L_CURLY =  '{'.charCodeAt(0);
TokenTypes.prototype.HAT =  '^'.charCodeAt(0);
TokenTypes.prototype.BAR =  '|'.charCodeAt(0);
TokenTypes.prototype.DOT =  '.'.charCodeAt(0);
TokenTypes.prototype.EXCLAMATION =  '!'.charCodeAt(0);
TokenTypes.prototype.LPAREN =  '('.charCodeAt(0);
TokenTypes.prototype.RPAREN =  ')'.charCodeAt(0);
TokenTypes.prototype.GREATER_THAN =  '>'.charCodeAt(0);
TokenTypes.prototype.LESS_THAN =  '<'.charCodeAt(0);
TokenTypes.prototype.PERCENT =  '%'.charCodeAt(0);
TokenTypes.prototype.QUESTION =  '?'.charCodeAt(0);
TokenTypes.prototype.AND =  '&'.charCodeAt(0);
TokenTypes.prototype.UNDERSCORE =  '_'.charCodeAt(0);