/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mobicents.servlet.sip.address;

import junit.framework.TestCase;

/**
 *
 * @author jim
 */
public class RFC2396UrlDecoderTest extends TestCase {
    
    public RFC2396UrlDecoderTest(String testName) {
        super(testName);
    }

    public void testNoUTF8() {
        String decoded = RFC2396UrlDecoder.decode("93684108_c0aa74a5_bbc53370_867090d7");
        assertEquals("93684108_c0aa74a5_bbc53370_867090d7", decoded);
    }
    public void testUTF8() {
        String url = 
               "https%3A%2F%2Fmywebsite%2Fdocs%2Fenglish%2Fsite%2Fmybook.do" +
               "%3Frequest_type%3D%26type%3Dprivate";
        String decoded = RFC2396UrlDecoder.decode(url);
        assertEquals("https://mywebsite/docs/english/site/mybook.do" +
               "?request_type=&type=private", decoded);
    }
    
}
