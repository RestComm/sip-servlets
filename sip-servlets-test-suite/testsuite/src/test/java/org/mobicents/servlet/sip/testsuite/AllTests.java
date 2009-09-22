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
package org.mobicents.servlet.sip.testsuite;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.mobicents.servlet.sip.testsuite.address.AddressTest;
import org.mobicents.servlet.sip.testsuite.address.SipURITest;
import org.mobicents.servlet.sip.testsuite.address.TelURLTest;
import org.mobicents.servlet.sip.testsuite.annotations.AnnotationTest;
import org.mobicents.servlet.sip.testsuite.b2bua.B2BUASipUnitTest;
import org.mobicents.servlet.sip.testsuite.b2bua.B2BUATcpUdpTest;
import org.mobicents.servlet.sip.testsuite.callcontroller.CallBlockingTest;
import org.mobicents.servlet.sip.testsuite.callcontroller.CallControllerCancelTest;
import org.mobicents.servlet.sip.testsuite.callcontroller.CallControllerJunitTest;
import org.mobicents.servlet.sip.testsuite.callcontroller.CallControllerSipUnitTest;
import org.mobicents.servlet.sip.testsuite.callcontroller.CallForwardingB2BUAJunitTest;
import org.mobicents.servlet.sip.testsuite.callcontroller.CallForwardingJunitTest;
import org.mobicents.servlet.sip.testsuite.callcontroller.CallForwardingSipUnitTest;
import org.mobicents.servlet.sip.testsuite.click2call.Click2CallBasicTest;
import org.mobicents.servlet.sip.testsuite.composition.B2BUACompositionJunitTest;
import org.mobicents.servlet.sip.testsuite.composition.LocationServiceB2BUACompositionTest;
import org.mobicents.servlet.sip.testsuite.composition.NoApplicationDeployedJunitTest;
import org.mobicents.servlet.sip.testsuite.composition.ProxyB2BUACompositionTest;
import org.mobicents.servlet.sip.testsuite.composition.SameSipSessionB2BUACompositionJunitTest;
import org.mobicents.servlet.sip.testsuite.composition.SpeedDialLocationServiceJunitTest;
import org.mobicents.servlet.sip.testsuite.concurrency.ConcurrentyControlSipSessionIsolationTest;
import org.mobicents.servlet.sip.testsuite.concurrency.CongestionControlTest;
import org.mobicents.servlet.sip.testsuite.deployment.DeploymentTest;
import org.mobicents.servlet.sip.testsuite.deployment.DistributableServletTest;
import org.mobicents.servlet.sip.testsuite.deployment.NoApplicationDeployedTest;
import org.mobicents.servlet.sip.testsuite.deployment.SameInstanceServletTest;
import org.mobicents.servlet.sip.testsuite.failover.BasicFailoverTest;
import org.mobicents.servlet.sip.testsuite.join.JoinSipServletTest;
import org.mobicents.servlet.sip.testsuite.listeners.ListenersSipServletTest;
import org.mobicents.servlet.sip.testsuite.mapping.ServletMappingSipServletTest;
import org.mobicents.servlet.sip.testsuite.proxy.ParallelProxyTelURLWithRecordRouteTest;
import org.mobicents.servlet.sip.testsuite.proxy.ParallelProxyWithRecordRouteTest;
import org.mobicents.servlet.sip.testsuite.proxy.ParallelProxyWithRecordRouteUseHostNameTest;
import org.mobicents.servlet.sip.testsuite.proxy.ProxyBranchTimeoutTest;
import org.mobicents.servlet.sip.testsuite.proxy.ProxyPrackTest;
import org.mobicents.servlet.sip.testsuite.proxy.ProxyRecordRouteReInviteTest;
import org.mobicents.servlet.sip.testsuite.proxy.SpeedDialJunitTest;
import org.mobicents.servlet.sip.testsuite.publish.PublishSipServletTest;
import org.mobicents.servlet.sip.testsuite.refer.ReferSipServletTest;
import org.mobicents.servlet.sip.testsuite.reinvite.LocationServiceReInviteSipServletTest;
import org.mobicents.servlet.sip.testsuite.reinvite.ReInviteSipServletTest;
import org.mobicents.servlet.sip.testsuite.reinvite.UACReInviteSipServletTest;
import org.mobicents.servlet.sip.testsuite.replaces.ReplacesSipServletTest;
import org.mobicents.servlet.sip.testsuite.routing.ExternalApplicationRoutingTest;
import org.mobicents.servlet.sip.testsuite.routing.ExternalRoutingServletTest;
import org.mobicents.servlet.sip.testsuite.routing.SameContainerRoutingServletTest;
import org.mobicents.servlet.sip.testsuite.security.CallForwardingB2BUAAuthTest;
import org.mobicents.servlet.sip.testsuite.security.PAssertedIdentityAuthTest;
import org.mobicents.servlet.sip.testsuite.security.ShootistSipServletAuthTest;
import org.mobicents.servlet.sip.testsuite.security.ShootmeSipServletAuthTest;
import org.mobicents.servlet.sip.testsuite.session.RequestDispatcherSipServletTest;
import org.mobicents.servlet.sip.testsuite.session.SessionHandlerSipServletTest;
import org.mobicents.servlet.sip.testsuite.session.SessionStateUACSipServletTest;
import org.mobicents.servlet.sip.testsuite.session.SessionStateUASSipServletTest;
import org.mobicents.servlet.sip.testsuite.session.SipAppSessionTerminationTest;
import org.mobicents.servlet.sip.testsuite.simple.ShootistSipServletTest;
import org.mobicents.servlet.sip.testsuite.simple.ShootistTelURLSipServletTest;
import org.mobicents.servlet.sip.testsuite.simple.ShootmeSipServletTest;
import org.mobicents.servlet.sip.testsuite.simple.ShootmeTelURLSipServletTest;
import org.mobicents.servlet.sip.testsuite.subsnotify.InDialogNotifierSipServletTest;
import org.mobicents.servlet.sip.testsuite.subsnotify.InDialogSubscriberSipServletTest;
import org.mobicents.servlet.sip.testsuite.subsnotify.NotifierSipServletTest;
import org.mobicents.servlet.sip.testsuite.subsnotify.SubscriberSipServletTest;
import org.mobicents.servlet.sip.testsuite.targeting.AppKeySipServletTest;
import org.mobicents.servlet.sip.testsuite.targeting.SessionKeyTargetingSipServletTest;
import org.mobicents.servlet.sip.testsuite.timers.TimersSipServletTest;
import org.mobicents.servlet.sip.testsuite.update.UpdateSipServletTest;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for org.mobicents.servlet.sip.testsuite");
		// $JUnit-BEGIN$
		suite.addTestSuite(ShootmeSipServletTest.class);
		suite.addTestSuite(ShootmeTelURLSipServletTest.class);
		suite.addTestSuite(ShootistSipServletTest.class);
		suite.addTestSuite(ShootistTelURLSipServletTest.class);
		suite.addTestSuite(ShootmeSipServletAuthTest.class);
		suite.addTestSuite(ShootistSipServletAuthTest.class);
		suite.addTestSuite(B2BUASipUnitTest.class);
		suite.addTestSuite(B2BUATcpUdpTest.class);
		suite.addTestSuite(CallBlockingTest.class);
		suite.addTestSuite(CallForwardingJunitTest.class);
		suite.addTestSuite(CallForwardingSipUnitTest.class);
		suite.addTestSuite(CallForwardingB2BUAJunitTest.class);
		suite.addTestSuite(CallForwardingB2BUAAuthTest.class);
		suite.addTestSuite(CallControllerSipUnitTest.class);
		suite.addTestSuite(CallControllerJunitTest.class);
		suite.addTestSuite(CallControllerCancelTest.class);
		suite.addTestSuite(B2BUACompositionJunitTest.class);
		suite.addTestSuite(ProxyB2BUACompositionTest.class);
		suite.addTestSuite(SpeedDialJunitTest.class);
		suite.addTestSuite(LocationServiceB2BUACompositionTest.class);
		suite.addTestSuite(LocationServiceReInviteSipServletTest.class);
		suite.addTestSuite(SameSipSessionB2BUACompositionJunitTest.class);
		suite.addTestSuite(ProxyRecordRouteReInviteTest.class);
		suite.addTestSuite(ParallelProxyWithRecordRouteTest.class);
		suite.addTestSuite(ParallelProxyTelURLWithRecordRouteTest.class);
		suite.addTestSuite(ParallelProxyWithRecordRouteUseHostNameTest.class);
		suite.addTestSuite(ProxyPrackTest.class);
		suite.addTestSuite(ConcurrentyControlSipSessionIsolationTest.class);
		suite.addTestSuite(CongestionControlTest.class);
		suite.addTestSuite(ProxyBranchTimeoutTest.class);
		suite.addTestSuite(PAssertedIdentityAuthTest.class);
		suite.addTestSuite(SpeedDialLocationServiceJunitTest.class);
		suite.addTestSuite(Click2CallBasicTest.class);
		suite.addTestSuite(ListenersSipServletTest.class);
		suite.addTestSuite(TimersSipServletTest.class);
		suite.addTestSuite(SipAppSessionTerminationTest.class);
		suite.addTestSuite(SessionKeyTargetingSipServletTest.class);
		suite.addTestSuite(AppKeySipServletTest.class);
		suite.addTestSuite(SessionStateUASSipServletTest.class);
		suite.addTestSuite(SessionStateUACSipServletTest.class);
		suite.addTestSuite(SessionHandlerSipServletTest.class);
		suite.addTestSuite(RequestDispatcherSipServletTest.class);
		suite.addTestSuite(AnnotationTest.class);
		suite.addTestSuite(ServletMappingSipServletTest.class);
		suite.addTestSuite(SubscriberSipServletTest.class);
		suite.addTestSuite(NotifierSipServletTest.class);
		suite.addTestSuite(InDialogNotifierSipServletTest.class);
		suite.addTestSuite(InDialogSubscriberSipServletTest.class);
		suite.addTestSuite(ReInviteSipServletTest.class);
		suite.addTestSuite(UACReInviteSipServletTest.class);
		suite.addTestSuite(UpdateSipServletTest.class);
		suite.addTestSuite(PublishSipServletTest.class);
		suite.addTestSuite(ReferSipServletTest.class);
		suite.addTestSuite(JoinSipServletTest.class);
		suite.addTestSuite(ReplacesSipServletTest.class);
		suite.addTestSuite(NoApplicationDeployedJunitTest.class);
		suite.addTestSuite(NoApplicationDeployedTest.class);
		suite.addTestSuite(ExternalRoutingServletTest.class);
		suite.addTestSuite(ExternalApplicationRoutingTest.class);
		suite.addTestSuite(SameContainerRoutingServletTest.class);
		suite.addTestSuite(DeploymentTest.class);
		suite.addTestSuite(SameInstanceServletTest.class);
		suite.addTestSuite(DistributableServletTest.class);
		suite.addTestSuite(BasicFailoverTest.class);
		suite.addTestSuite(SipURITest.class);
		suite.addTestSuite(AddressTest.class);
		suite.addTestSuite(TelURLTest.class);

		// $JUnit-END$
		return suite;
	}

}