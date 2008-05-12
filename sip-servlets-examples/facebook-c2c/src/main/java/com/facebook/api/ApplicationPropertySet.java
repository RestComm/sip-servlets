/*
  +---------------------------------------------------------------------------+
  | Facebook Development Platform Java Client                                 |
  +---------------------------------------------------------------------------+
  | Copyright (c) 2007-2008 Facebook, Inc.                                    |
  | All rights reserved.                                                      |
  |                                                                           |
  | Redistribution and use in source and binary forms, with or without        |
  | modification, are permitted provided that the following conditions        |
  | are met:                                                                  |
  |                                                                           |
  | 1. Redistributions of source code must retain the above copyright         |
  |    notice, this list of conditions and the following disclaimer.          |
  | 2. Redistributions in binary form must reproduce the above copyright      |
  |    notice, this list of conditions and the following disclaimer in the    |
  |    documentation and/or other materials provided with the distribution.   |
  |                                                                           |
  | THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR      |
  | IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES |
  | OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.   |
  | IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,          |
  | INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT  |
  | NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, |
  | DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY     |
  | THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT       |
  | (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF  |
  | THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.         |
  +---------------------------------------------------------------------------+
  | For help with this library, contact developers-help@facebook.com          |
  +---------------------------------------------------------------------------+
*/
package com.facebook.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;


/**
 * This utility represents a set of application properties for use in 
 * the facebook.admin_setApplicatoinProperty API call.
 */
public class ApplicationPropertySet {
  private Map<ApplicationProperty,Boolean>      _attributesBool = null;
  private Map<ApplicationProperty,CharSequence> _attributesString = null;
  
  public ApplicationPropertySet() {
  }

  /**
   * Initialize the ApplicationPropertySet mapping from its JSON representation
   * @param jsonString JSON representing the application properties 
   * @throws ClassCastException if the JSON-encoded mapping doesn't conform to
   *         expectations
   */
  public ApplicationPropertySet(String jsonString) throws ClassCastException {
    try {
      JSONObject mappings = (JSONObject) JSONValue.parse(jsonString);
      Set<Map.Entry> entries = mappings.entrySet();
      for (Map.Entry entry : entries) {
        String propName = (String) entry.getKey();
        ApplicationProperty prop = ApplicationProperty.getProperty(propName);
        if (null == prop) {
          throw new IllegalArgumentException("Unrecognized application property name: " + propName);
        }
        Object value = entry.getValue();
        if (prop.isBooleanProperty()) {
          if (null == value) {
            this.setBoolProperty(prop, Boolean.FALSE);
          } else if (value instanceof Boolean) {
            this.setBoolProperty(prop, (Boolean) value);
          } else if (value instanceof Number) {
            this.setBoolProperty(prop, !(new Long(0)).equals(value));
          } else if (value instanceof String) {
            this.setBoolProperty(prop, !"".equals(value));
          }
        } else if (prop.isStringProperty()) {
          this.setStringProperty(prop, (String) entry.getValue());
        }
      }
    } catch (ClassCastException cce) {
      // hmmm ...
    }
  }

  public void setBoolProperty(ApplicationProperty prop, boolean value) {
    if (null == prop || !prop.isBooleanProperty()) {
      throw new IllegalArgumentException("Boolean property expected");
    }
    
    if (null == this._attributesBool) {
      this._attributesBool = new HashMap<ApplicationProperty,Boolean>();
    }
    this._attributesBool.put(prop, value);
  }

  public Boolean getBoolProperty(ApplicationProperty prop) {
    if (null == prop || !prop.isBooleanProperty()) {
      throw new IllegalArgumentException("Boolean property expected");
    }
    
    return (null == this._attributesBool) ? null : this._attributesBool.get(prop);
  }

  public void setStringProperty(ApplicationProperty prop, CharSequence value) {
    if (null == prop || !prop.isStringProperty()) {
      throw new IllegalArgumentException("String property expected");
    }
    
    if (null == this._attributesString) {
      this._attributesString = new HashMap<ApplicationProperty,CharSequence>();
    }
    this._attributesString.put(prop, value);
  }

  public CharSequence getStringProperty(ApplicationProperty prop) {
    if (null == prop || !prop.isStringProperty()) {
      throw new IllegalArgumentException("String property expected");
    }
    
    return (null == this._attributesString) ? null : this._attributesString.get(prop);
  }

  public void removeProperty(ApplicationProperty prop) {
    if (prop.isBooleanProperty()) {
      this._attributesBool.remove(prop);
    }
    else if (prop.isStringProperty()) {
      this._attributesString.remove(prop);
    }
  }

  public boolean isEmpty() {
    return (null == this._attributesString || this._attributesString.isEmpty())
      && (null == this._attributesBool || this._attributesBool.isEmpty());
  }

  /**
   * Return a JSON representation of this property set object
   * @return JSONObject
   */
  public JSONObject jsonify() {
    JSONObject ret = new JSONObject();
    if (null != this._attributesString) {
      for (Map.Entry<ApplicationProperty,CharSequence> entry: this._attributesString.entrySet()) {
        ret.put(entry.getKey().propertyName(), entry.getValue().toString());
      }
    }
    if (null != this._attributesBool) {
      for (Map.Entry<ApplicationProperty,Boolean> entry: this._attributesBool.entrySet()) {
        ret.put(entry.getKey().propertyName(), entry.getValue());
      }
    }
    return ret;
  }
  
  /**
   * Return a JSON string representation of this object
   * @return a JSON string
   */
  public String toJsonString() {
    return this.jsonify().toString();
  }  
}
