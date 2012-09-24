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
 *  Implementation of the JAIN-SIP GenericObject class.
 *  @see  gov/nist/core/GenericObject.java 
 *  @author Yuemin Qin (yuemin.qin@orange.com)
 *  @author Laurent STRULLU (laurent.strullu@orange.com)
 *  @version 1.0 
 *   
 */

function GenericObject() {
    //if(logger!=undefined) logger.debug("GenericObject:GenericObject()");
    this.classname="GenericObject"; 
    this.indentation=0;
    this.stringRepresentation="";
    this.matchExpression=null;
    this.immutableClassNames =["String", "Character",
    "Boolean", "Byte", "Short", "Integer", "Long",
    "Float", "Double"]
    this.immutableClasses=new Array();
            
}

GenericObject.prototype.SEMICOLON = ";";
GenericObject.prototype.COLON = ":";
GenericObject.prototype.COMMA = ",";
GenericObject.prototype.SLASH = "/";
GenericObject.prototype.SP = " ";
GenericObject.prototype.EQUALS = "=";
GenericObject.prototype.STAR = "*";
GenericObject.prototype.NEWLINE = "\r\n";
GenericObject.prototype.RETURN = "\n";
GenericObject.prototype.LESS_THAN = "<";
GenericObject.prototype.GREATER_THAN = ">";
GenericObject.prototype.AT = "@";
GenericObject.prototype.DOT = ".";
GenericObject.prototype.QUESTION = "?";
GenericObject.prototype.POUND = "#";
GenericObject.prototype.AND = "&";
GenericObject.prototype.LPAREN = "(";
GenericObject.prototype.RPAREN = ")";
GenericObject.prototype.DOUBLE_QUOTE = "\"";
GenericObject.prototype.QUOTE = "\'";
GenericObject.prototype.HT = "\t";
GenericObject.prototype.PERCENT = "%";

GenericObject.prototype.setMatcher =function(matchExpression){
    //if(logger!=undefined) logger.debug("GenericObject:setMatcher():matchExpression="+matchExpression);
    this.matchExpression = matchExpression;
}

GenericObject.prototype.getMatcher =function(){
    //if(logger!=undefined) logger.debug("GenericObject:getMatcher()");
    return this.matchExpression;
}

GenericObject.prototype.getClassFromName =function(className){
    //if(logger!=undefined) logger.debug("GenericObject:getClassFromName():className="+className);
    function class_for_name(name) {
        return new Function('return new ' + name)();
    }
    var classfromname=class_for_name(className);
    return classfromname;
}

GenericObject.prototype.isMySubclass=function(other){
    //if(logger!=undefined) logger.debug("GenericObject:isMySubclass():other="+other);
    if((typeof other)!="object"||other instanceof Array)
    {
        return false;
    }
    else
    {
        var c=0;
        if(Object.getPrototypeOf(other).classname=="GenericObject")
        {
            return true;
        }
        else 
        {
            O=Object.getPrototypeOf(other);
            for(;O.classname!=undefined;)
            {
                if(Object.getPrototypeOf(O).classname=="GenericObject")
                {
                    c=1;
                    O=Object.getPrototypeOf(O);
                }
                else
                {
                    O=Object.getPrototypeOf(O);
                }
            }
            if(c==1)
            {
                return true;
            }
            else
            {
                return false;
            }
            
        }
    }
}

GenericObject.prototype.encode=function(){
    //if(logger!=undefined) logger.debug("GenericObject:encode()");
}

GenericObject.prototype.encode=function(buffer){
    //if(logger!=undefined) logger.debug("GenericObject:encode():buffer="+buffer);
    return buffer+this.encode();
}

GenericObject.prototype.equals=function(that){
    //if(logger!=undefined) logger.debug("GenericObject:equals():that="+that);
}

GenericObject.prototype.match=function(other){
    //if(logger!=undefined) logger.debug("GenericObject:match():other="+other);
}

GenericObject.prototype.merge=function(mergeObject){
    //if(logger!=undefined) logger.debug("GenericObject:merge():mergeObject="+mergeObject);  
}

GenericObject.prototype.clone=function(){
    //if(logger!=undefined) logger.debug("GenericObject:clone()");
    var objClone;
    if (this.constructor == Object){
        objClone = new this.constructor(); 
    }else{
        objClone = new this.constructor(this.valueOf()); 
    }
    for(var key in this){
        if ( objClone[key] != this[key] ){ 
            if ( typeof(this[key]) == 'object' ){ 
                objClone[key] = this[key].clone();
            }else{
                objClone[key] = this[key];
            }
        }
    }
    objClone.toString = this.toString;
    objClone.valueOf = this.valueOf;
    return objClone; 
}
