package org.mobicents.servlet.sip.router;

import java.util.Properties;
import junit.framework.TestCase;

public class DefaultApplicationRouterTest extends TestCase {

    public DefaultApplicationRouterTest(String testName) {
        super(testName);
    }

    public void testInitWithProperties() {
        DefaultApplicationRouter router = new DefaultApplicationRouter();
        Properties properties = new Properties();
        properties
                .setProperty("ALL",
                        "(\"PlayMyBand\",\"DAR:From\",\"NEUTRAL\",\"\",\"NO_ROUTE\",\"0\",\"HEADER_FROM=.*sip:.*@sip-servlets\\.com\")");
        router.init(properties);
    }

}
