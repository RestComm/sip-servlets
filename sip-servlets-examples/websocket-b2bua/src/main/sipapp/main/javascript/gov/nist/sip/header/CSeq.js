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
 *  Implementation of the JAIN-SIP AuthorizationList .
 *  @see  gov/nist/javax/sip/header/AuthorizationList.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 */
function CSeq() {
    if(logger!=undefined) logger.debug("CSeq:CSeq()");
    this.serialVersionUID = "-5405798080040422910L";
    this.classname="CSeq";
    this.method=null;
    this.seqno=null;
    if(arguments.length==0)
    {
        this.headerName=this.CSEQ;
    }
    else
    {
        var seqno=arguments[0];
        var method=arguments[1];
        this.headerName=this.CSEQ;
        this.seqno = seqno;
        var siprequest=new SIPRequest();
        this.method = siprequest.getCannonicalName(method);
    }
}

CSeq.prototype = new SIPHeader();
CSeq.prototype.constructor=CSeq;
CSeq.prototype.CSEQ="CSeq";
CSeq.prototype.COLON=":";
CSeq.prototype.SP=" ";
CSeq.prototype.NEWLINE="\r\n";


CSeq.prototype.encode =function(){
    if(logger!=undefined) logger.debug("CSeq:encode()");
    return this.headerName+this.COLON+this.SP+this.encodeBody()+this.NEWLINE;
}

CSeq.prototype.encodeBody =function(){
    if(logger!=undefined) logger.debug("CSeq:encodeBody()");
    return this.encodeBodyBuffer("").toString();
}

CSeq.prototype.encodeBodyBuffer =function(buffer){
    if(logger!=undefined) logger.debug("CSeq:encodeBodyBuffer():buffer="+buffer);
    buffer=buffer+this.seqno+this.SP+this.method.toUpperCase();
    return buffer;
}

CSeq.prototype.getMethod =function(){
    if(logger!=undefined) logger.debug("CSeq:getMethod()");
   
   return this.method;
}

CSeq.prototype.setSeqNumber =function(sequenceNumber){
    if(logger!=undefined) logger.debug("CSeq:setSeqNumber():sequenceNumber="+sequenceNumber);
    if (sequenceNumber < 0 )
    {
       console.error("CSeq:setSeqNumber(): the sequence number parameter is < 0 : " + sequenceNumber);
       throw "CSeq:setSeqNumber(): the sequence number parameter is < 0 : " + sequenceNumber;
    }
    else if ( sequenceNumber > 2147483647)
    {
       console.error("CSeq:setSeqNumber(): the sequence number parameter is too large : " + sequenceNumber);
       throw "CSeq:setSeqNumber(): the sequence number parameter is too large : " + sequenceNumber;
    }
    this.seqno = sequenceNumber;
}

CSeq.prototype.setSequenceNumber =function(sequenceNumber){
    if(logger!=undefined) logger.debug("CSeq:setSequenceNumber():sequenceNumber:"+sequenceNumber);
    this.setSeqNumber(sequenceNumber);
}

CSeq.prototype.setMethod =function(method){
    if(logger!=undefined) logger.debug("CSeq:setMethod(): method="+method);
    if (method == null)
    {
        console.error("CSeq:setMethod(): the method parameter is null");
         throw "CSeq:setMethod(): the meth parameter is null";
    }
    var siprequest=new SIPRequest();
    this.method = siprequest.getCannonicalName(method);
}

CSeq.prototype.getSequenceNumber =function(){
    if(logger!=undefined) logger.debug("CSeq:getSequenceNumber()");
    if (this.seqno == null)
    {
        return 0;
    }
    else
    {
        return this.seqno;
    }
}
CSeq.prototype.getSeqNumber =function(){
    if(logger!=undefined) logger.debug("CSeq:getSeqNumber()");
    return this.seqno;
}