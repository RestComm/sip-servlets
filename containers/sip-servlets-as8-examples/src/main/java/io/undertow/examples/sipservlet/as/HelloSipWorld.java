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
package io.undertow.examples.sipservlet.as;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import javax.annotation.Resource;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.sip.Address;
import javax.servlet.sip.Parameterable;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.mobicents.servlet.sip.message.SipFactoryFacade;

/**.
 * This example shows a typical UAS and reply 200 OK to any INVITE or BYE it receives
 * 
 * @author Jean Deruelle
 *
 */

public class HelloSipWorld extends SipServlet {

    /**
     * 
     */
    private static final long serialVersionUID = 5512704379502637286L;
    private static final String ASCHOST = "sip:127.0.0.1:15080";

    @Resource
    SipFactory sipFactory;

    private static Logger logger = Logger.getLogger(HelloSipWorld.class);
    private static HashMap<String, SipServletRequest> inviteRequests = new HashMap<String, SipServletRequest>();
    private static SipServletRequest sfullFirstInvite;
    private static SipServletRequest sfullSecondInvite;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        logger.info("the HelloSipWorld servlet has been started");
        super.init(servletConfig);
    }

    /**
    @Override
    protected void doInvite(SipServletRequest request) throws ServletException,
                    IOException {

            logger.info("Got request:${symbol_escape}n"
                            + request.toString());
            String fromUri = request.getFrom().getURI().toString();
            logger.info(fromUri);
           
            SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
            sipServletResponse.send();              
    }
    */

    protected void doOptions(SipServletRequest request) throws ServletException, IOException {
        logger.info("'HelloSipWorld' OPTIONS: Got request:\n" + request.getMethod());

        request.createResponse(200).send();
    }

    @Override
    protected void doInvite(SipServletRequest request) throws ServletException, IOException {
        inviteRequests.put(request.getCallId(), request);
        if (logger.isInfoEnabled()) {
            logger.info("HelloSipWorld INVITE: Got request:\n" + request.getMethod());
        }

        // 100 trying valasz csak akkor megy, ha doInvite "lass�"
        /*
         * try { Thread.sleep(2000); } catch (InterruptedException e) { // TODO Auto-generated catch block
         * e.printStackTrace(); }
         */

        // o Address.toString() o Address.getURI().setParameter(„a”,”b”); o // OCCAS + lépés: Address.setURI(uri) o
        // Address.toString() látszik-e ;a=b ?

        Address to = request.getTo();
        to.getURI().setParameter("c", "d");
        System.out.println("to address toString():" + to.toString());

        // 183 kuldese eseten:
        SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_SESSION_PROGRESS);

        if (request.getHeader("SfullTest") != null) {
            sipServletResponse.addHeader("SfullTest", request.getHeader("SfullTest"));
            sfullFirstInvite = request;

            // calltester SRTestContentType alapjan parameterkezelesre teszteles:
            Parameterable p = request.getParameterableHeader("Content-Type");

            Iterator<String> names = p.getParameterNames();
            while (names.hasNext()) {
                String name = names.next();
                System.out.println("Content-Type param: Name:" + name + ", Value:" + p.getParameter(name));

            }
            sipServletResponse.sendReliably();
        } else if (request.getHeader("CancelTest") != null) {
            sipServletResponse.addHeader("CancelTest", request.getHeader("CancelTest"));
            sipServletResponse.sendReliably();
        } else if (request.getHeader("ConcurrencyTest") != null) {
            sipServletResponse = request.createResponse(SipServletResponse.SC_MOVED_TEMPORARILY);
            sipServletResponse.addHeader("ConcurrencyTest", request.getHeader("ConcurrencyTest"));
            sipServletResponse.send();
        } else if (request.getHeader("ConcurrencyTestMDB") != null) {
            sipServletResponse = request.createResponse(SipServletResponse.SC_MOVED_TEMPORARILY);
            sipServletResponse.addHeader("ConcurrencyTestMDB", request.getHeader("ConcurrencyTestMDB"));
            sipServletResponse.send();
        } else {
            sipServletResponse = request.createResponse(SipServletResponse.SC_SESSION_PROGRESS);

            sipServletResponse.sendReliably();

        }

        /*
         * Egybol 302 kuldese eseten: SipServletResponse sipServletResponse =
         * request.createResponse(SipServletResponse.SC_MOVED_TEMPORARILY); sipServletResponse.send();
         */

    }

    @Override
    protected void doPrack(SipServletRequest request) throws ServletException, IOException {
        if (logger.isInfoEnabled()) {
            logger.info("HelloSipWorld PRACK: Got request:\n" + request.getMethod());
        }

        SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
        sipServletResponse.send();

        // sfull screnario testing
        if ("true".equals(request.getHeader("SfullTest"))) {
            SipServletRequest inviteReq = inviteRequests.get(request.getCallId()).getB2buaHelper()
                    .createRequest(inviteRequests.get(request.getCallId()));
            inviteReq.pushRoute(sipFactory.createAddress(ASCHOST));

            sfullSecondInvite = inviteReq;

            inviteReq.send();
        } else {
            SipServletRequest storedInviteReq = inviteRequests.get(request.getCallId());

            SipServletResponse sipServletResponse302 = storedInviteReq
                    .createResponse(SipServletResponse.SC_MOVED_TEMPORARILY);

            sipServletResponse302.send();
        }
    }

    @Override
    protected void doSubscribe(SipServletRequest request) throws ServletException, IOException {
        if (logger.isInfoEnabled()) {
            logger.info("HelloSipWorld SUBSCRIBE: Got request:\n" + request.getMethod());
        }

        SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_ACCEPTED);
        sipServletResponse.addHeader("Expires", request.getHeader("Expires"));
        sipServletResponse.addHeader("Event", request.getHeader("Event"));
        sipServletResponse.addHeader("Accept", request.getHeader("Accept"));
        sipServletResponse.send();

        SipServletRequest notifyRequest = request.getSession().createRequest("NOTIFY");

        Object content = "30223020A21EA01C301A06092A863A0089613A0100A70D800329080081060000000000F1";
        notifyRequest.setContent(content, request.getHeader("Accept") + ";op=71;dir=result");
        notifyRequest.addHeader("Subscription-State", "terminated;reason=timeout");

        notifyRequest.send();
    }

    @Override
    protected void doResponse(SipServletResponse resp) throws ServletException, IOException {
        if (resp.getStatus() == SipServletResponse.SC_RINGING && "true".equals(resp.getHeader("SfullTest"))) {
            SipServletRequest storedInviteReq = sfullFirstInvite;

            SipServletResponse sipServletResponse180 = storedInviteReq.createResponse(SipServletResponse.SC_RINGING);
            sipServletResponse180.addHeader("SfullTest", "true");
            sipServletResponse180.send();

        } else if (resp.getStatus() == SipServletResponse.SC_BUSY_HERE && "true".equals(resp.getHeader("SfullTest"))) {
            SipServletRequest storedInviteReq = sfullFirstInvite;
            SipServletResponse sipServletResponseBusy = storedInviteReq.createResponse(SipServletResponse.SC_BUSY_HERE);
            sipServletResponseBusy.addHeader("SfullTest", "true");
            sipServletResponseBusy.send();

        }

        else {

            super.doResponse(resp);
        }
    }

    /*
     * SipServletRequest outRequest = sipFactory.createRequest(request.getApplicationSession(), "INVITE",
     * request.getFrom().getURI(), request.getTo().getURI()); String user = ((SipURI)
     * request.getTo().getURI()).getUser(); Address calleeAddress = registeredUsersToIp.get(user); if(calleeAddress ==
     * null) { request.createResponse(SipServletResponse.SC_NOT_FOUND).send(); return; }
     * outRequest.setRequestURI(calleeAddress.getURI()); if(request.getContent() != null) {
     * outRequest.setContent(request.getContent(), request.getContentType()); } outRequest.send();
     * sessions.put(request.getSession(), outRequest.getSession()); sessions.put(outRequest.getSession(),
     * request.getSession());
     */

    /*
     * protected void doResponse(SipServletResponse response) throws ServletException, IOException {
     * 
     * response.getSession().setAttribute("lastResponse", response); SipServletRequest request = (SipServletRequest)
     * sessions.get(response.getSession()).getAttribute("lastRequest"); SipServletResponse resp =
     * request.createResponse(response.getStatus()); if(response.getContent() != null) {
     * resp.setContent(response.getContent(), response.getContentType()); } resp.send(); }
     */
    /*
     * protected void doRegister(SipServletRequest request) throws ServletException, IOException {
     * logger.info("HelloSipWorld UA REGISTERING:\n" + request.toString());
     * 
     * Address addr = request.getAddressHeader("Contact"); SipURI sipUri = (SipURI) addr.getURI();
     * //registeredUsersToIp.put(sipUri.getUser(), addr); if(logger.isInfoEnabled()) { logger.info("Address registered "
     * + addr); } SipServletResponse sipServletResponse = request.createResponse(SipServletResponse.SC_OK);
     * sipServletResponse.send(); }
     */
    /*
     * @Override protected void doBye(SipServletRequest request) throws ServletException, IOException {
     * logger.info("the HelloSipWorld has received a BYE....."); SipServletResponse sipServletResponse =
     * request.createResponse(SipServletResponse.SC_OK); sipServletResponse.send(); }
     */
}
