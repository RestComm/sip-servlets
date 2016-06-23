package org.mobicents.servlet.sip.startup;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static junit.framework.Assert.assertNotNull;
import junit.framework.TestCase;
import org.apache.catalina.Context;
import org.apache.tomcat.util.descriptor.DigesterFactory;
import org.apache.tomcat.util.digester.Digester;
import org.mobicents.servlet.sip.catalina.SipEntityResolver;
import org.mobicents.servlet.sip.catalina.SipRuleSet;
import org.mobicents.servlet.sip.catalina.rules.AndRule;
import org.mobicents.servlet.sip.core.descriptor.MobicentsSipServletMapping;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

public class SipContextConfigTest extends TestCase {

    public SipContextConfigTest(String testName) {
        super(testName);
    }

    private static final String AND_RULE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<sip-app>\n"
            + "    <app-name>ProxyApp</app-name>\n"
            + "    <display-name>ProxyApp</display-name>\n"
            + "	\n"
            + "\n"
            + "    <servlet>\n"
            + "        <servlet-name>ProxySipServlet</servlet-name>\n"
            + "        <display-name>ProxySipServlet</display-name>\n"
            + "        <servlet-class>\n"
            + "            com.avaya.proxy.ProxySipServlet\n"
            + "        </servlet-class>\n"
            + "        <load-on-startup>-1</load-on-startup>\n"
            + "    </servlet>\n"
            + "    \n"
            + "	<listener>\n"
            + "		<listener-class>\n"
            + "			com.avaya.proxy.ProxySipServlet\n"
            + "		</listener-class>\n"
            + "	</listener>\n"
            + "  <servlet-selection>\n"
            + "    <servlet-mapping>\n"
            + "      <servlet-name>ProxySipServlet</servlet-name>\n"
            + "      <pattern>\n"
            + "        <and>\n"
            + "          <equal>\n"
            + "            <var>request.method</var>\n"
            + "            <value>INVITE</value>\n"
            + "          </equal>\n"
            + "          <equal>\n"
            + "            <var>request.to.uri.user</var>\n"
            + "            <value>player</value>\n"
            + "          </equal>\n"
            + "        </and>\n"
            + "      </pattern>\n"
            + "    </servlet-mapping>\n"
            + "  </servlet-selection>        \n"
            + "              \n"
            + "    <context-param>\n"
            + "        <param-name>call.code</param-name>\n"
            + "        <param-value>test</param-value>\n"
            + "    </context-param>\n"
            + "    <context-param>\n"
            + "        <param-name>override.system.header.modifiable</param-name>\n"
            + "        <param-value>Modifiable</param-value>\n"
            + "    </context-param>                    \n"
            + "</sip-app>";

    public void testAddRuleParsing() throws SAXException, IOException {
        InputStream stream = new ByteArrayInputStream(AND_RULE.getBytes(StandardCharsets.UTF_8));

        Context context = new SipStandardContext();
        Digester sipDigester = DigesterFactory.newDigester(false,
                false,
                new SipRuleSet(),
                false);
        EntityResolver entityResolver = new SipEntityResolver();
        sipDigester.setValidating(false);
        sipDigester.setEntityResolver(entityResolver);
        //push the context to the digester
        sipDigester.push(context);
        sipDigester.setClassLoader(this.getClass().getClassLoader());
        //parse the sip.xml and populate the context with it
        sipDigester.resolveEntity(null, null);
        Object parse = sipDigester.parse(stream);
        assertNotNull(parse);
        assertTrue(parse instanceof SipStandardContext);
        SipStandardContext parsedCtx = (SipStandardContext) parse;

        List<MobicentsSipServletMapping> findSipServletMappings = parsedCtx.findSipServletMappings();
        assertEquals(1, findSipServletMappings.size());
        MobicentsSipServletMapping mapping = findSipServletMappings.get(0);
        assertTrue(mapping.getMatchingRule() instanceof AndRule);
    }

}
