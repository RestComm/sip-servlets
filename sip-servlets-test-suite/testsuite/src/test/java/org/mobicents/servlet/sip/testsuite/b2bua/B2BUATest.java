/*
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
package org.mobicents.servlet.sip.testsuite.b2bua;

import org.mobicents.servlet.sip.SipServletTestCase;

public class B2BUATest extends SipServletTestCase {
	
	private B2BUATestCase testCase;
	
	public B2BUATest(String name) {
		super(name);
	}
	
	@Override
	public void deployApplication() {
		assertTrue(tomcat.deployContext(
				projectHome + "/sip-servlets-test-suite/applications/b2bua-sip-servlet/src/main/sipapp",
				"sip-test-context", "sip-test"));
	}

	@Override
	protected String getDarConfigurationFile() {
		return "file:///" + projectHome + "/sip-servlets-test-suite/testsuite/src/test/resources/" +
				"org/mobicents/servlet/sip/testsuite/b2bua/b2bua-sip-servlet-dar.properties";
	}
	
	public void init() throws Exception {
	
		this.testCase = new B2BUATestCase();
		this.testCase.setUp();
		
	}
	
	public void testSendInvite()throws Exception {
		init();
		testCase.doTest();
	}
	
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		this.testCase.tearDown();
		
	}
	
	

}
