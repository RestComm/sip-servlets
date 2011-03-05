package org.mobicents.servlet.sip.example;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.jboss.netty.handler.codec.http.DefaultHttpMessage;
import org.jboss.netty.handler.codec.http.HttpMethod;

public class DefaultRtspRequest extends DefaultHttpMessage implements
                RtspRequest {

        private final RtspMethod method;
        private final String uri;
        private final String host;
        private final int port;

        private final static String COLON = ":";
        private final static String SP = " ";
        private final static String CRLF = "\r\n";

        public DefaultRtspRequest(RtspVersion rtspVersion, RtspMethod method,
                        String uri) throws URISyntaxException {
                super(rtspVersion);
                if (method == null) {
                        throw new NullPointerException("method");
                }
                if (uri == null) {
                        throw new NullPointerException("uri");
                }

                URI objUri = new URI(uri);
                String scheme = objUri.getScheme() == null ? "rtsp" : objUri
                                .getScheme();
                host = objUri.getHost() == null ? "localhost" : objUri.getHost();
                port = objUri.getPort() == -1 ? 5050 : objUri.getPort();

                if (!scheme.equalsIgnoreCase("rtsp")) {
                        throw new UnsupportedOperationException("Only rtsp is supported");
                }

                this.method = method;
                this.uri = uri;
        }

        public HttpMethod getMethod() {
                return method;
        }

        public String getUri() {
                return uri;
        }

        @Override
        public String toString() {
                return getMethod().toString() + ' ' + getUri() + ' '
                                + getProtocolVersion().getText();
        }

        public String debug() {
                StringBuffer buffer = new StringBuffer();
                buffer.append(this.toString());
                buffer.append(CRLF);
                Set<String> headers = this.getHeaderNames();

                for (String header : headers) {
                        List<String> values = this.getHeaders(header);
                        for (String value : values) {

                                buffer.append(header);
                                buffer.append(COLON);
                                buffer.append(SP);
                                buffer.append(value);
                                buffer.append(CRLF);
                        }
                }

                return buffer.toString();
        }

        public String getHost() {
                return host;
        }

        public int getPort() {
                return port;
        }

		public void setMethod(HttpMethod arg0) {
			throw new IllegalStateException();

		}

		public void setUri(String arg0) {
			throw new IllegalStateException();
		}

}