package org.mobicents.servlet.sip.router;

import junit.framework.Assert;
import junit.framework.TestCase;

public class DefaultSipApplicationRouterInfoTest extends TestCase {

    public DefaultSipApplicationRouterInfoTest(String testName) {
        super(testName);
    }

    public void testGetHeaderPatternMap_NoHeader() {
        String optionalParams = "DIRECTION=1 REGEX=From:.*sip:.*@sip-servlets\\.com";
        DefaultSipApplicationRouterInfo info = new DefaultSipApplicationRouterInfo(null, null, null, null, null, 0,
                optionalParams);
        Assert.assertEquals(0, info.getHeaderPatternMap().size());
    }

    public void testGetHeaderPatternMap_OneHeader() {
        String optionalParams = "DIRECTION=1 REGEX=From:.*sip:.*@sip-servlets\\.com HEADER_TO=.*sip:.*@sip-servlets";
        DefaultSipApplicationRouterInfo info = new DefaultSipApplicationRouterInfo(null, null, null, null, null, 0,
                optionalParams);
        Assert.assertEquals(1, info.getHeaderPatternMap().size());
        Assert.assertNotNull(info.getHeaderPatternMap().get("TO"));
    }

    public void testGetHeaderPatternMap_MoreThenOneHeader() {
        String optionalParams = "DIRECTION=1 HEADER_TO=.*sip:.*@sip-servlets REGEX=From:.*sip:.*@sip-servlets\\.com HEADER_VIA=.*sip:.*@sip-servlets";
        DefaultSipApplicationRouterInfo info = new DefaultSipApplicationRouterInfo(null, null, null, null, null, 0,
                optionalParams);
        Assert.assertEquals(2, info.getHeaderPatternMap().size());
    }

}
