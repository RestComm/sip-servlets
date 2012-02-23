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

package org.mobicents.servlet.sip.arquillian.testsuite;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.mobicents.servlet.sip.testsuite");
		// $JUnit-BEGIN$

		
//		suite.addTestSuite(ShootmeSipServletTest.class);
//		suite.addTestSuite(ShootmeTelURLSipServletTest.class);
//		suite.addTestSuite(ShootistSipServletTest.class);
//		suite.addTestSuite(ShootistTelURLSipServletTest.class);
//		suite.addTestSuite(ShootmeSipServletAuthTest.class);
//		suite.addTestSuite(ShootistSipServletAuthTest.class);
//		suite.addTestSuite(B2BUASipUnitTest.class);
//		suite.addTestSuite(B2BUATcpUdpTest.class);
//		suite.addTestSuite(B2BUASessionCallbackTest.class);
//		suite.addTestSuite(CallBlockingTest.class);
//		suite.addTestSuite(CallForwardingJunitTest.class);
//		suite.addTestSuite(CallForwardingSipUnitTest.class);
//		suite.addTestSuite(CallForwardingB2BUAJunitTest.class);
//		suite.addTestSuite(CallForwardingB2BUAAuthTest.class);
//		suite.addTestSuite(CallControllerSipUnitTest.class);
//		suite.addTestSuite(CallControllerJunitTest.class);
//		suite.addTestSuite(CallControllerCancelTest.class);
//		suite.addTestSuite(B2BUACompositionJunitTest.class);
//		suite.addTestSuite(ProxyB2BUACompositionTest.class);
//		suite.addTestSuite(SpeedDialJunitTest.class);
//		suite.addTestSuite(LocationServiceB2BUACompositionTest.class);
//		suite.addTestSuite(LocationServiceReInviteSipServletTest.class);
//		suite.addTestSuite(SameSipSessionB2BUACompositionJunitTest.class);
//		suite.addTestSuite(ProxyRecordRouteReInviteTest.class);
//		suite.addTestSuite(ProxyRecordRouteUpdateTest.class);
//		suite.addTestSuite(ParallelProxyWithRecordRouteTest.class);
//		suite.addTestSuite(ParallelProxyTelURLWithRecordRouteTest.class);
//		suite.addTestSuite(ParallelProxyWithRecordRouteUseHostNameTest.class);
//		suite.addTestSuite(ProxyPrackTest.class);
//		suite.addTestSuite(ProxyURNTest.class);
//		suite.addTestSuite(ProxyTerminationTest.class);
////		suite.addTestSuite(ForkingProxyDerivedSessions.class);
//		suite.addTestSuite(ProxySipServletDownstreamProxyForkingTest.class);
//		suite.addTestSuite(ProxyNonRecordRouteTest.class);
//		suite.addTestSuite(ShootmePrackSipServletTest.class);
//		suite.addTestSuite(ShootistPrackSipServletTest.class);
//		suite.addTestSuite(CallForwardingB2BUAPrackTest.class);
//		suite.addTestSuite(ConcurrentyControlSipSessionIsolationTest.class);
//		suite.addTestSuite(ConcurrentyControlAsyncWorkSessionIsolationTest.class);
//		suite.addTestSuite(CongestionControlTest.class);
//		suite.addTestSuite(ProxyBranchTimeoutTest.class);
//		suite.addTestSuite(PAssertedIdentityAuthTest.class);
//		suite.addTestSuite(SpeedDialLocationServiceJunitTest.class);
//		suite.addTestSuite(Click2CallBasicTest.class);
//		suite.addTestSuite(ListenersSipServletTest.class);
//		suite.addTestSuite(TimersSipServletTest.class);
//		suite.addTestSuite(SipAppSessionTerminationTest.class);
//		suite.addTestSuite(SessionKeyTargetingSipServletTest.class);
//		suite.addTestSuite(AppKeySipServletTest.class);
//		suite.addTestSuite(EncodeURISipServletTest.class);
//		suite.addTestSuite(SipApplicationSessionKeyParsingTest.class);
//		suite.addTestSuite(SessionStateUASSipServletTest.class);
//		suite.addTestSuite(SessionStateUACSipServletTest.class);
//		suite.addTestSuite(SessionHandlerSipServletTest.class);
//		suite.addTestSuite(RequestDispatcherSipServletTest.class);
//		suite.addTestSuite(AnnotationTest.class);
//		suite.addTestSuite(ServletMappingSipServletTest.class);
//		suite.addTestSuite(SubscriberSipServletTest.class);
//		suite.addTestSuite(NotifierSipServletTest.class);
//		suite.addTestSuite(InDialogNotifierSipServletTest.class);
//		suite.addTestSuite(InDialogSubscriberSipServletTest.class);
//		suite.addTestSuite(ReInviteSipServletTest.class);
//		suite.addTestSuite(UACReInviteSipServletTest.class);
//		suite.addTestSuite(UpdateSipServletTest.class);
//		suite.addTestSuite(PublishSipServletTest.class);
//		suite.addTestSuite(ReferSipServletTest.class);
//		suite.addTestSuite(JoinSipServletTest.class);
//		suite.addTestSuite(ReplacesSipServletTest.class);
//		suite.addTestSuite(NoApplicationDeployedJunitTest.class);
//		suite.addTestSuite(NoApplicationDeployedTest.class);
//		suite.addTestSuite(ExternalRoutingServletTest.class);
//		suite.addTestSuite(ExternalApplicationRoutingTest.class);
//		suite.addTestSuite(SameContainerRoutingServletTest.class);
//		suite.addTestSuite(RegexApplicationRoutingTest.class);
//		suite.addTestSuite(DeploymentTest.class);
//		suite.addTestSuite(SameInstanceServletTest.class);
//		suite.addTestSuite(DistributableServletTest.class);
//		suite.addTestSuite(ShootistSipServletForkingTest.class);		
//		suite.addTestSuite(SipURITest.class);
//		suite.addTestSuite(AddressTest.class);
//		suite.addTestSuite(TelURLTest.class);

		// $JUnit-END$
		return suite;
	}

}