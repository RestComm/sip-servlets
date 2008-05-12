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

import org.json.simple.JSONObject;

public class MarketplaceListing {
  private String _category;
  private String _subCategory;
  private String _title;
  private String _description;
  private Map<CharSequence,CharSequence> _extraAttributes = null;
  
  public MarketplaceListing(String category, String subCategory, String title, String description) {
    this(category, subCategory, title, description, null);
  }

  public MarketplaceListing(String category, String subCategory, String title, String description, Map<CharSequence,CharSequence> extraAttributes) {
    setCategory(category);
    setSubCategory(subCategory);
    setTitle(title);
    setDescription(description);
    if (null != extraAttributes) {
      this._extraAttributes = extraAttributes;
    }
  }

  public void putAttribute(CharSequence attr, CharSequence value) {
    if (null == this._extraAttributes) {
      this._extraAttributes = new HashMap<CharSequence, CharSequence>();
    }
    this._extraAttributes.put(attr, value);
  }

  public void removeAttribute(CharSequence attr) {
    if (null == this._extraAttributes) {
      return;
    }
    this._extraAttributes.remove(attr);
  }

  public void setCategory(String category) {
    this._category = category;
  }

  public String getCategory() {
    return _category;
  }

  public void setSubCategory(String subCategory) {
    this._subCategory = subCategory;
  }

  public String getSubCategory() {
    return _subCategory;
  }

  public void setTitle(String title) {
    this._title = title;
  }

  public String getTitle() {
    return _title;
  }

  public void setDescription(String description) {
    this._description = description;
  }

  public String getDescription() {
    return _description;
  }
  
  /**
   * Return a JSON representation of this object
   * @return JSONObject
   */
  public JSONObject jsonify() {
    JSONObject ret = new JSONObject();
    ret.put("title", getTitle());
    ret.put("category", getCategory());
    ret.put("subcategory", getSubCategory());
    ret.put("description", getDescription());
    if (null != this._extraAttributes && !this._extraAttributes.isEmpty()) {
      ret.putAll(this._extraAttributes);
    }
    return ret;
  }
}
