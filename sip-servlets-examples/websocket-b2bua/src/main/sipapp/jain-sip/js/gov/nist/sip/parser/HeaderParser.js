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
 *  Implementation of the JAIN-SIP HeaderParser .
 *  @see  gov/nist/javax/sip/parser/HeaderParser.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */
function HeaderParser() {
    if(logger!=undefined) logger.debug("HeaderParser:HeaderParser()");
    this.classname="HeaderParser"; 
    if(typeof arguments[0]=="string")
    {
        var header=arguments[0];
        this.lexer = new Lexer("command_keywordLexer", header);
    }
    else if(typeof arguments[0]=="object")
    {
        var lexer=arguments[0];
        this.lexer = lexer;
        this.lexer.selectLexer("command_keywordLexer");
    }
}

HeaderParser.prototype = new Parser();
HeaderParser.prototype.constructor=HeaderParser;

HeaderParser.prototype.wkday =function(){
    if(logger!=undefined) logger.debug("HeaderParser:wkday()");
    var tok = this.lexer.ttoken();
    var id = tok.toLowerCase();
    if (TokenNames.prototype.MON==id.toLowerCase()) {
        return 2;//Calendar.MONDAY;
    } else if (TokenNames.prototype.TUE.toLowerCase()==id.toLowerCase()) {
        return 3;//calendar.TUESDAY;
    } else if (TokenNames.prototype.WED.toLowerCase()==id.toLowerCase()) {
        return 4;//calendar.WEDNESDAY;
    } else if (TokenNames.prototype.THU.toLowerCase()==id.toLowerCase()) {
        return 5;//calendar.THURSDAY;
    } else if (TokenNames.prototype.FRI.toLowerCase()==id.toLowerCase()) {
        return 6;//calendar.FRIDAY;
    } else if (TokenNames.prototype.SAT.toLowerCase()==id.toLowerCase()) {
        return 7;//calendar.SATURDAY;
    } else if (TokenNames.prototype.SUN.toLowerCase()==id.toLowerCase()) {
        return 1;//calendar.SUNDAY;
    } else {
        console.error("HeaderParser:wkday(): bad wkday");
        throw "HeaderParser:wkday(): bad wkday";
    }
}

HeaderParser.prototype.date =function(){
    if(logger!=undefined) logger.debug("HeaderParser:date()");
    try {
        var retval = new Date();
        var s1 = this.lexer.number();
        var day = s1+0;
        if (day <= 0 || day > 31) {
           console.error("HeaderParser:date(): bad day");
           throw "HeaderParser:date():  bad day";
        }
        retval.set(5, day);
        this.lexer.match(' ');
        var month = this.lexer.ttoken().toLowerCase();
        if (month.equals("jan")) {
            retval.setMonth( 0);//Calendar.MONTH=2
        } else if (month=="feb") {
            retval.setMonth(1);
        } else if (month=="mar") {
            retval.setMonth(2);
        } else if (month=="apr") {
            retval.setMonth(3);
        } else if (month=="may") {
            retval.setMonth(4);
        } else if (month=="jun") {
            retval.setMonth(5);
        } else if (month=="jul") {
            retval.setMonth(6);
        } else if (month=="aug") {
            retval.setMonth(7);
        } else if (month=="sep") {
            retval.setMonth(8);
        } else if (month=="oct") {
            retval.setMonth(9);
        } else if (month=="nov") {
            retval.setMonth(10);
        } else if (month=="dec") {
            retval.setMonth(11);
        }
        this.lexer.match(' ');
        var s2 = this.lexer.number();
        var yr = s2;
        retval.setYear(yr);
        return retval;
    } catch (ex) {
        console.error("HeaderParser:date(): bad date field");
        throw "HeaderParser:date(): bad date field";
    }
}

HeaderParser.prototype.time =function(calendar){
    if(logger!=undefined) logger.debug("HeaderParser:time():calendar="+calendar);
    try {
        var s = this.lexer.number();
        var hour = s;
        calendar.setHours(hour);
        this.lexer.match(':');
        s = this.lexer.number();
        var min = s;
        calendar.setMinutes(min);
        this.lexer.match(':');
        s = this.lexer.number();
        var sec = s;
        calendar.setSeconds(sec);
    } catch (ex) {
        console.error("HeaderParser:time(): error processing time ");
        throw "HeaderParser:time():error processing time ";
    }
}


HeaderParser.prototype.parse =function(){
    if(logger!=undefined) logger.debug("HeaderParser:parse()");
    var name = this.lexer.getNextToken(':');
    this.lexer.consume(1);
    var body = this.lexer.getLine().replace(/^(\s|\xA0)+|(\s|\xA0)+$/g, '');
    var retval = new ExtensionHeaderImpl(name);
    retval.setValue(body);
    return retval;
}
HeaderParser.prototype.headerName =function(tok){
    if(logger!=undefined) logger.debug("HeaderParser:headerName():tok="+tok);
    this.lexer.match(tok);
    this.lexer.SPorHT();
    this.lexer.match(':');
    this.lexer.SPorHT();
}