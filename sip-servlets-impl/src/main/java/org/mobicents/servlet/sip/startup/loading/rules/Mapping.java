package org.mobicents.servlet.sip.startup.loading.rules;

import java.io.StringReader;

import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.NodeCreateRule;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import org.mobicents.servlet.sip.startup.SipStandardService;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;

public class Mapping {
	private static final Logger logger = Logger.getLogger(Mapping.class);
	/**
	 * From JSR: amounts to 
	 * method == "RULETEST1" &&
     * (r-uri.scheme == "sips" || r-uri.scheme = "sip") &&
     * r-uri.user == "rule-1" &&
     * r-uri.host subdomain-of "example.com" &&
     * r-uri.host == "host17.example.com" &&
     * !(r-uri.tel != null) &&
     * r-uri.param.foo != null &&
     * r-uri.port == 5000
	 */
	public static final String TEST = 
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		+ "<sip-app>"
	    + "<servlet-mapping>"
	    + "<servlet-name>rule-1</servlet-name>"
	    + "<pattern>"
	    + "<and>"
	    + "<equal>"
	    + "<var>request.method</var>"
        + "<value>RULETEST1</value>"
        + "</equal>"
        + "<or>"
        + "<equal>"
        + "<var>request.uri.scheme</var>"
        + "<value>sips</value>"
        + "</equal>"
        + "<equal>"
        + "<var>request.uri.scheme</var>"
        + "<value>sip</value>"
        + "</equal>"
        + "</or>"
        + "<equal>"
        + "<var>request.uri.user</var>"
        + "<value>rule-1</value>"
        + "</equal>"
        + "<subdomain-of>"
        + "<var>request.uri.host</var>"
        + "<value>example.com</value>"
        + "</subdomain-of>"
        + "<equal>"
        + "<var>request.uri.host</var>"
        + "<value>host17.example.com</value>"
        + "</equal>"
        + "<not>"
        + "<exists>"
        + "<var>request.uri.tel</var>"
        + "</exists>"
        + "</not>"
        + "<exists>"
        + "<var>request.uri.param.foo</var>"
        + "</exists>"
        + "<equal>"
        + "<var>request.uri.port</var>"
        + "<value>5072</value>"
        + "</equal>"
        + "</and>"
        + "</pattern>" 
        + "</servlet-mapping>"
        + "</sip-app>";
        

	public static void main(String[] args) throws Exception {
		
		BasicConfigurator.configure();
		
		Digester digester = new Digester();
        digester.setValidating(false);
        digester.addRule("sip-app/servlet-mapping/pattern",
                new TestRule());
        
       digester.parse(new StringReader(TEST));
	}
	
	static class TestRule extends NodeCreateRule {
		
		public TestRule() throws Exception {	
		}

		@Override
		public void begin(String arg0, String arg1, Attributes arg2)
				throws Exception {
			// TODO Auto-generated method stub
			super.begin(arg0, arg1, arg2);
		}
		
		@Override
		public void end(String namespace, String name) throws Exception {
			 Element e = (Element) super.digester.pop();
			   Node pattern = (Node) e;

			   NodeList list = pattern.getChildNodes();
			   
			   try {
				   MatchingRule rule = MatchingRuleParser.buildRule((Element) list.item(0));
				   if (rule != null) {
					   logger.debug(rule.getExpression());
				   }
			   } catch (Throwable t) {
				   t.printStackTrace();
			   }
		}
	}
}
