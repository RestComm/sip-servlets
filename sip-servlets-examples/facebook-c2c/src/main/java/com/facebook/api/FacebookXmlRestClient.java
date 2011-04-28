/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

/**
 * A FacebookRestClient that uses the XML result format. This means 
 * results from calls to the Facebook API are returned as XML and 
 * transformed into instances of {@link org.w3c.dom.Document}.
 */
public class FacebookXmlRestClient extends FacebookRestClient<Document> {

  public FacebookXmlRestClient(String apiKey, String secret) {
    this(SERVER_URL, apiKey, secret, null);
  }

  public FacebookXmlRestClient(String apiKey, String secret, String sessionKey) {
    this(SERVER_URL, apiKey, secret, sessionKey);
  }

  public FacebookXmlRestClient(String serverAddr, String apiKey, String secret,
                            String sessionKey) throws MalformedURLException {
    this(new URL(serverAddr), apiKey, secret, sessionKey);
  }

  public FacebookXmlRestClient(URL serverUrl, String apiKey, String secret,
                            String sessionKey) {
    super(serverUrl, apiKey, secret, sessionKey);
  }

  /**
   * The response format in which results to FacebookMethod calls are returned
   * @return the format: either XML, JSON, or null (API default)
   */
  public String getResponseFormat() {
    return "xml";
  }

  /**
   * Extracts a String from a T consisting entirely of a String.
   * @param result
   * @return the String
   */
  public String extractString(Document d) {
    return d.getFirstChild().getTextContent();
  }

  /**
   * Call this function to retrieve the session information after your user has
   * logged in.
   * @param authToken the token returned by auth_createToken or passed back to your callback_url.
   */
  public String auth_getSession(String authToken) throws FacebookException,
                                                         IOException {
    if (null != this._sessionKey) {
      return this._sessionKey;
    }
    Document d =
      this.callMethod(FacebookMethod.AUTH_GET_SESSION, 
                      new Pair<String, CharSequence>("auth_token", authToken.toString()));
    this._sessionKey =
        d.getElementsByTagName("session_key").item(0).getFirstChild().getTextContent();
    this._userId = Integer.parseInt(d.getElementsByTagName("uid").item(0).getFirstChild().getTextContent());
    if (this._isDesktop) {
      this._sessionSecret =
          d.getElementsByTagName("secret").item(0).getFirstChild().getTextContent();
    }
    return this._sessionKey;
  }

  protected Document parseCallResult(InputStream data, IFacebookMethod method) throws FacebookException, IOException {
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = builder.parse(data);
      doc.normalizeDocument();
      stripEmptyTextNodes(doc);

      if (isDebug()) {
        FacebookXmlRestClient.printDom(doc, method.methodName() + "| ");
      }
      NodeList errors = doc.getElementsByTagName(ERROR_TAG);
      if (errors.getLength() > 0) {
        int errorCode =
          Integer.parseInt(errors.item(0).getFirstChild().getFirstChild().getTextContent());
        String message = errors.item(0).getFirstChild().getNextSibling().getTextContent();
        throw new FacebookException(errorCode, message);
      }
      return doc;
    } catch (ParserConfigurationException ex) {
      System.err.println("huh?" + ex);
    } catch (SAXException ex) {
      throw new IOException("error parsing xml");
    }
    return null;
  }

  /**
   * Extracts a URL from a document that consists of a URL only.
   * @param doc
   * @return the URL
   */
  protected URL extractURL(Document doc) throws IOException {
    String url = doc.getFirstChild().getTextContent();
    return (null == url || "".equals(url)) ? null : new URL(url);
  }

  /**
   * Extracts an Integer from a document that consists of an Integer only.
   * @param doc
   * @return the Integer
   */
  protected int extractInt(Document doc) {
    return Integer.parseInt(doc.getFirstChild().getTextContent());
  }

  /**
   * Extracts a Long from a document that consists of a Long only.
   * @param doc
   * @return the Long
   */
  protected Long extractLong(Document doc) {
    return Long.parseLong(doc.getFirstChild().getTextContent());
  }

  /**
   * Hack...since DOM reads newlines as textnodes we want to strip out those
   * nodes to make it easier to use the tree.
   */
  private static void stripEmptyTextNodes(Node n) {
    NodeList children = n.getChildNodes();
    int length = children.getLength();
    for (int i = 0; i < length; i++) {
      Node c = children.item(i);
      if (!c.hasChildNodes() && c.getNodeType() == Node.TEXT_NODE &&
        c.getTextContent().trim().length() == 0) {
        n.removeChild(c);
        i--;
        length--;
        children = n.getChildNodes();
      } else {
        stripEmptyTextNodes(c);
      }
    }
  }
  
  /**
   * Prints out the DOM tree.
   */
  public static void printDom(Node n, String prefix) {
    String outString = prefix;
    if (n.getNodeType() == Node.TEXT_NODE) {
      outString += "'" + n.getTextContent().trim() + "'";
    } else {
      outString += n.getNodeName();
    }
    System.out.println(outString);
    NodeList children = n.getChildNodes();
    int length = children.getLength();
    for (int i = 0; i < length; i++) {
      FacebookXmlRestClient.printDom(children.item(i), prefix + "  ");
    }
  }
}
