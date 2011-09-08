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

package org.mobicents.servlet.sip.catalina.rules;

import java.io.StringReader;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.digester.Digester;
import org.apache.tomcat.util.digester.NodeCreateRule;
import org.mobicents.servlet.sip.core.descriptor.MatchingRule;
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
