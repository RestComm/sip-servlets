package org.mobicents.servlet.sip.example;

import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * 
 * @author amit bhayani
 *
 */
public interface RtspRequest extends HttpRequest {
        public String getHost();
        
        public int getPort();

        public String debug();


}