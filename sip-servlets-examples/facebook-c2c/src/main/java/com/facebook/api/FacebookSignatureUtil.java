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

import java.io.UnsupportedEncodingException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class FacebookSignatureUtil {
  private FacebookSignatureUtil() {
  }

  /**
   * Out of the passed in <code>reqParams</code>, extracts the parameters that
   * are in the FacebookParam namespace and returns them.
   * @param reqParams A map of request parameters to their values. Values
   *                  are arrays of strings, as returned by
   *                  ServletRequest.getParameterMap(). Only the first element
   *                  in a given array is significant.
   * @return a boolean indicating whether the calculated signature matched the
   *   expected signature
   */
  public static Map<String, CharSequence> extractFacebookParamsFromArray(Map<CharSequence, CharSequence[]> reqParams) {
    if (null == reqParams)
      return null;
    Map<String,CharSequence> result = new HashMap<String,CharSequence>(reqParams.size());
    for (Map.Entry<CharSequence,CharSequence[]> entry : reqParams.entrySet()) {
      String key = entry.getKey().toString();
      if (FacebookParam.isInNamespace(key)) {
        CharSequence[] value = entry.getValue();
        if (value.length > 0)
          result.put(key, value[0]);
      }
    }
    return result;
  }

  /**
   * Out of the passed in <code>reqParams</code>, extracts the parameters that
   * are in the FacebookParam namespace and returns them.
   * @param reqParams a map of request parameters to their values
   * @return a boolean indicating whether the calculated signature matched the
   *   expected signature
   */
  public static Map<String, CharSequence> extractFacebookNamespaceParams(Map<CharSequence, CharSequence> reqParams) {
    if (null == reqParams)
      return null;
    Map<String,CharSequence> result = new HashMap<String,CharSequence>(reqParams.size());
    for (Map.Entry<CharSequence,CharSequence> entry : reqParams.entrySet()) {
      String key = entry.getKey().toString();
      if (FacebookParam.isInNamespace(key))
        result.put(key, entry.getValue());
    }
    return result;
  }

  /**
   * Out of the passed in <code>reqParams</code>, extracts the parameters that
   * are known FacebookParams and returns them.
   * @param reqParams a map of request parameters to their values
   * @return a map suitable for being passed to verify signature
   */
  public static EnumMap<FacebookParam, CharSequence> extractFacebookParams(Map<CharSequence, CharSequence> reqParams) {
    if (null == reqParams)
      return null;

    EnumMap<FacebookParam, CharSequence> result =
      new EnumMap<FacebookParam, CharSequence>(FacebookParam.class);
    for (Map.Entry<CharSequence, CharSequence> entry: reqParams.entrySet()) {
      FacebookParam matchingFacebookParam = FacebookParam.get(entry.getKey().toString());
      if (null != matchingFacebookParam) {
        result.put(matchingFacebookParam, entry.getValue());
      }
    }
    return result;
  }

  /**
   * Verifies that a signature received matches the expected value.
   * Removes FacebookParam.SIGNATURE from params if present.
   * @param params a map of parameters and their values, such as one
   *   obtained from extractFacebookParams; expected to the expected signature 
   *   as the FacebookParam.SIGNATURE parameter
   * @param secret
   * @return a boolean indicating whether the calculated signature matched the
   *   expected signature
   */
  public static boolean verifySignature(EnumMap<FacebookParam, CharSequence> params, String secret) {
    if (null == params || params.isEmpty() )
      return false;
    CharSequence sigParam = params.remove(FacebookParam.SIGNATURE);
    return (null == sigParam) ? false : verifySignature(params, secret, sigParam.toString()); 
  }

  /**
   * Verifies that a signature received matches the expected value.
   * @param params a map of parameters and their values, such as one
   *   obtained from extractFacebookParams
   * @return a boolean indicating whether the calculated signature matched the
   *   expected signature
   */
  public static boolean verifySignature(EnumMap<FacebookParam, CharSequence> params, String secret,
                                        String expected) {
    assert !(null == secret || "".equals(secret));
    if (null == params || params.isEmpty() )
      return false;
    if (null == expected || "".equals(expected)) {
      return false;
    }
    params.remove(FacebookParam.SIGNATURE);
    List<String> sigParams = convertFacebookParams(params.entrySet());
    return verifySignature(sigParams, secret, expected);
  }

  /**
   * Verifies that a signature received matches the expected value.
   * Removes FacebookParam.SIGNATURE from params if present.
   * @param params a map of parameters and their values, such as one
   *   obtained from extractFacebookNamespaceParams; expected to contain the 
   *   signature as the FacebookParam.SIGNATURE parameter
   * @param secret
   * @return a boolean indicating whether the calculated signature matched the
   *   expected signature
   */
  public static boolean verifySignature(Map<String, CharSequence> params, String secret) {
    if (null == params || params.isEmpty() )
      return false;
    CharSequence sigParam = params.remove(FacebookParam.SIGNATURE.toString());
    return (null == sigParam) ? false : verifySignature(params, secret, sigParam.toString()); 
  }

  /**
   * Verifies that a signature received matches the expected value.
   * @param params a map of parameters and their values, such as one
   *   obtained from extractFacebookNamespaceParams
   * @return a boolean indicating whether the calculated signature matched the
   *   expected signature
   */
  public static boolean verifySignature(Map<String, CharSequence> params, String secret,
                                        String expected) {
    assert !(null == secret || "".equals(secret));
    if (null == params || params.isEmpty() )
      return false;
    if (null == expected || "".equals(expected)) {
      return false;
    }
    params.remove(FacebookParam.SIGNATURE.toString());
    List<String> sigParams = convert(params.entrySet());
    return verifySignature(sigParams, secret, expected);
  }


  private static boolean verifySignature(List<String> sigParams, String secret, String expected) {
    if (null == expected || "".equals(expected))
      return false;
    String signature = generateSignature(sigParams, secret);
    return expected.equals(signature);    
  }

  /**
   * Converts a Map of key-value pairs into the form expected by generateSignature
   * @param entries a collection of Map.Entry's, such as can be obtained using
   *   myMap.entrySet()
   * @return a List suitable for being passed to generateSignature
   */
  public static List<String> convert(Collection<Map.Entry<String, CharSequence>> entries) {
    List<String> result = new ArrayList<String>(entries.size());
    for (Map.Entry<String, CharSequence> entry: entries)
      result.add(FacebookParam.stripSignaturePrefix(entry.getKey()) + "=" + entry.getValue());
    return result;
  }

  /**
   * Converts a Map of key-value pairs into the form expected by generateSignature
   * @param entries a collection of Map.Entry's, such as can be obtained using
   *   myMap.entrySet()
   * @return a List suitable for being passed to generateSignature
   */
  public static List<String> convertFacebookParams(Collection<Map.Entry<FacebookParam, CharSequence>> entries) {
    List<String> result = new ArrayList<String>(entries.size());
    for (Map.Entry<FacebookParam, CharSequence> entry: entries)
      result.add(entry.getKey().getSignatureName() + "=" + entry.getValue());
    return result;
  }

  /**
   * Calculates the signature for the given set of params using the supplied secret
   * @param params Strings of the form "key=value"
   * @param secret
   * @return the signature
   */
  public static String generateSignature(List<String> params, String secret) {
    StringBuffer buffer = new StringBuffer();
    Collections.sort(params);
    for (String param : params) {
      buffer.append(param);
    }

    buffer.append(secret);
    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
      StringBuffer result = new StringBuffer();
      for (byte b : md.digest(buffer.toString().getBytes("UTF-8"))) {
        result.append(Integer.toHexString((b & 0xf0) >>> 4));
        result.append(Integer.toHexString(b & 0x0f));
      }
      return result.toString();
    } catch (java.security.NoSuchAlgorithmException ex) {
      System.err.println("MD5 does not appear to be supported" + ex);
      return "";
    } catch (UnsupportedEncodingException uee) {
      System.err.println("UTF8 encoding does not appear to be supported" + uee);
      return "";
    }
  }
}
