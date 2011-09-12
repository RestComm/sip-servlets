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

package javax.servlet.sip;

import java.io.IOException;

/**
 * Provides an abstract class to be subclassed to create a SIP servlet.
 * This class receives incoming messages through the service method. This method calls doRequest or doResponse for incoming requests and responses, respectively. These two methods in turn dispatch on request method or status code to one of the following methods: doInvite - for SIP INVITE requests doAck - for SIP ACK requests doOptions - for SIP OPTIONS requests doBye - for SIP BYE requests doCancel - for SIP CANCEL requests doRegister - for SIP REGISTER requests doSubscribe - for SIP SUBSCRIBE requests doNotify - for SIP NOTIFY requests doMessage - for SIP MESSAGE requests doInfo - for SIP INFO requests doPrack - for SIP PRACK requests doUpdate - for SIP UPDATE requests doRefer - for SIP REFER requests doPublish - for SIP PUBLISH requests doProvisionalResponse - for SIP 1xx informational responses doSuccessResponse - for SIP 2xx responses doRedirectResponse - for SIP 3xx responses doErrorResponse - for SIP 4xx, 5xx, and 6xx responses
 * The default implementation of doAck, doCancel and all the response handling methods are empty. All other request handling methods reject the request with a 500 error response.
 * Subclasses of SipServlet will usually override one or more of these methods.
 * See Also:RFC 2976, The SIP INFO Method, RFC 3262, Reliability of Provisional Responses in the Session Initiation Protocol (SIP), RFC 3265, Session Initiation Protocol (SIP)-Specific Event Notification, SIP Extensions for Instant Messaging, Serialized Form
 */
public abstract class SipServlet extends javax.servlet.GenericServlet{
    /**
     * The string "javax.servlet.sip.outboundInterfaces". This is the name of the ServletContext attribute whose value is a list of SipURI objects which represent the available outbound interfaces for sending SIP requests. On a multihomed machine, a specific outbound interface can be selected for send requests by calling the the @{link SipSession.setOutboundInterface} with an available interface address chosen from this list.
     * Since: 1.1 See Also:Constant Field Values
     */
    public static final java.lang.String OUTBOUND_INTERFACES="javax.servlet.sip.outboundInterfaces";

    /**
     * The string "javax.servlet.sip.SipFactory". This is the name of the ServletContext attribute whose value is an instance of the SipFactory interface.
     * See Also:SipFactory, Constant Field Values
     */
    public static final java.lang.String SIP_FACTORY="javax.servlet.sip.SipFactory";

    /**
     * The string "javax.servlet.sip.Sessions". This is the name of the ServletContext attribute whose value is the @{link Sessions} utility class providing support for converged SIP/HTTP applications.
     * Since: 1.1 See Also:Constant Field Values
     */
    public static final java.lang.String SIP_SESSIONS_UTIL="javax.servlet.sip.SipSessionsUtil";

    /**
     * The string "javax.servlet.sip.supported". This is the name of the ServletContext attribute whose value is a List containing the names of SIP extensions supported by the container.
     * See Also:Constant Field Values
     */
    public static final java.lang.String SUPPORTED="javax.servlet.sip.supported";
    
    /**
     * The string "javax.servlet.sip.supportedRfcs". 
     * This is the name of the ServletContext attribute whose value is a List 
     * containing the RFC numbers represented as Strings of SIP RFCs supported by the container. 
     * For e.g., if the container supports RFC 3261, RFC 3262 and RFC 3265, 
     * the List associated with this attribute should contain the Strings "3261", "3262" and "3265".
     */
    public static final java.lang.String SUPPORTED_RFCs="javax.servlet.sip.supportedRfcs";

    /**
     * @deprecated in favor of using the "javax.servlet.sip.supported" attribute
     * The string "javax.servlet.sip.100rel". This is the name of the ServletContext attribute whose value suggests whether the container supports the 100rel extension i.e. RFC 3262. 
     */
    public static final java.lang.String PRACK_SUPPORTED="javax.servlet.sip.100rel";
    
    /**
     * The string "javax.servlet.sip.TimerService". This is the name of the ServletContext attribute whose value is an instance of the TimerService interface.
     * See Also:TimerService, Constant Field Values
     */
    public static final java.lang.String TIMER_SERVICE="javax.servlet.sip.TimerService";

    public SipServlet(){
         
    }

    /**
     * Invoked by the server (via the service method) to handle incoming ACK requests.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doAck(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
        
    }

    /**
     * Invoked by the server to handle intermediate final responses only if this Servlet behaves as a proxy.
     * The default implementation is empty and must be overridden by subclasses to handle intermediate final responses received on a ProxyBranch. 
     * @param resp the response object 
     * @throws javax.servlet.ServletException if an exception occurs that interferes with the servlet's normal operation 
     * @throws java.io.IOException if an input or output exception occurs
     */
    protected void doBranchResponse(SipServletResponse resp) throws javax.servlet.ServletException, java.io.IOException {
    	
    }
    
    /**
     * Invoked by the server (via the service method) to handle incoming BYE requests.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doBye(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
    	if (req.isInitial())
			notHandled(req);
    }

    /**
     * Invoked by the server (via the service method) to handle incoming CANCEL requests.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doCancel(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
       
    }

    /**
     * Invoked by the server (via the doResponse method) to handle incoming 4xx - 6xx class responses.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doErrorResponse(javax.servlet.sip.SipServletResponse resp) throws javax.servlet.ServletException, java.io.IOException{
        
    }

    /**
     * Invoked by the server (via the service method) to handle incoming INFO requests.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doInfo(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
    	if (req.isInitial()) 
			notHandled(req);
    }

    /**
     * Invoked by the server (via the service method) to handle incoming INVITE requests.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doInvite(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
    	if (req.isInitial())
			notHandled(req);
    }

    /**
     * Invoked by the server (via the service method) to handle incoming MESSAGE requests.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doMessage(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
    	if (req.isInitial())
			notImplemented(req);
    }

    /**
     * Invoked by the server (via the service method) to handle incoming NOTIFY requests.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doNotify(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
    	if (req.isInitial())
			notImplemented(req);
    }

    /**
     * Invoked by the server (via the service method) to handle incoming OPTIONS requests.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doOptions(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
    	if (req.isInitial())
			notHandled(req);
    }

    /**
     * Invoked by the server (via the service method) to handle incoming PRACK requests.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doPrack(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
    	if (req.isInitial())
			notHandled(req);
    }

    /**
     * Invoked by the server (via the doResponse method) to handle incoming 1xx class responses.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doProvisionalResponse(javax.servlet.sip.SipServletResponse resp) throws javax.servlet.ServletException, java.io.IOException{
       
    }

    /**
     * Invoked by the server (via the service method) to handle incoming PUBLISH requests.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doPublish(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
    	if (req.isInitial())
			notImplemented(req);
    }

    /**
     * Invoked by the server to notify the servlet of incoming 3xx class responses.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doRedirectResponse(javax.servlet.sip.SipServletResponse resp) throws javax.servlet.ServletException, java.io.IOException{
        
    }

    /**
     * Invoked by the server (via the service method) to handle incoming REFER requests.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doRefer(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
    	if (req.isInitial())
			notHandled(req);
    }

    /**
     * Invoked by the server (via the service method) to handle incoming REGISTER requests.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doRegister(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
    	if (req.isInitial())
			notHandled(req);
    }

    /**
     * Invoked to handle incoming requests. This method dispatched requests to one of the doXxx methods where Xxx is the SIP method used in the request. Servlets will not usually need to override this method.
     */
    protected void doRequest(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
    	String m = req.getMethod();
		if ("INVITE".equals(m))
			doInvite(req);
		else if ("ACK".equals(m))
			doAck(req);
		else if ("OPTIONS".equals(m))
			doOptions(req);
		else if ("BYE".equals(m))
			doBye(req);
		else if ("CANCEL".equals(m))
			doCancel(req);
		else if ("REGISTER".equals(m))
			doRegister(req);
		else if ("SUBSCRIBE".equals(m))
			doSubscribe(req);
		else if ("NOTIFY".equals(m))
			doNotify(req);
		else if ("MESSAGE".equals(m))
			doMessage(req);
		else if ("INFO".equals(m))
			doInfo(req);
		else if ("REFER".equals(m))
			doRefer(req);
		else if ("PUBLISH".equals(m))
			doPublish(req);
		else if ("UPDATE".equals(m))
			doUpdate(req);
		else if ("PRACK".equals(m))
			doPrack(req);
		else if (req.isInitial())
			notHandled(req);
    }

    /**
     * Invoked to handle incoming responses. This method dispatched responses to one of the
     * ,
     * ,
     * . Servlets will not usually need to override this method.
     */
    protected void doResponse(javax.servlet.sip.SipServletResponse resp) throws javax.servlet.ServletException, java.io.IOException{
    	int status = resp.getStatus();
		if (status < 200) {
			doProvisionalResponse(resp);
		} else {			
			if (status < 300) {
				doSuccessResponse(resp);
			} else if (status < 400) {
				doRedirectResponse(resp);
			} else {	
				doErrorResponse(resp);
			}				
		}
    }

    /**
     * Invoked by the server (via the service method) to handle incoming SUBSCRIBE requests.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doSubscribe(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
    	if (req.isInitial())
			notImplemented(req);
    }

    /**
     * Invoked by the server (via the doResponse method) to handle incoming 2xx class responses.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doSuccessResponse(javax.servlet.sip.SipServletResponse resp) throws javax.servlet.ServletException, java.io.IOException{
        
    }

    /**
     * Invoked by the server (via the service method) to handle incoming UPDATE requests.
     * The default implementation is empty and must be overridden by subclasses to do something useful.
     */
    protected void doUpdate(javax.servlet.sip.SipServletRequest req) throws javax.servlet.ServletException, java.io.IOException{
        
    }

    /**
     * Writes the specified message to a servlet log file. See {link ServletContext#log(String)}.
     */
    public void log(java.lang.String message){
    	getServletContext().log(message);
    }

    /**
     * Writes an explanatory message and a stack trace for a given Throwable exception to the servlet log file. See ServletContext.log(String, Throwable).
     */
    public void log(java.lang.String message, java.lang.Throwable t){
    	getServletContext().log(message,t);
    }

    /**
     * Invoked to handle incoming SIP messages: requests or responses. Exactly one of the arguments is null: if the event is a request the response argument is null, and vice versa, if the event is a response the request argument is null.
     * This method dispatched to doRequest() or doResponse() as appropriate. Servlets will not usually need to override this method.
     */
    public void service(javax.servlet.ServletRequest req, javax.servlet.ServletResponse resp) throws javax.servlet.ServletException, java.io.IOException{
    	if (req != null) {
			doRequest((SipServletRequest) req);
		} else {
			SipServletResponse response = (SipServletResponse)resp;
			if(response.isBranchResponse()) {
				doBranchResponse(response);
			} else {
				doResponse(response);
			}
		}
    }

    /**
     * 
     * @param req
     * @throws IOException
     */
    private void notHandled(SipServletRequest req) throws IOException {
		SipServletResponse resp = req.createResponse(500,
				"Request not handled by app");
		resp.send();
	}
    
    /**
     * 
     * @param req
     * @throws IOException
     */
    private void notImplemented(SipServletRequest req) throws IOException {
		SipServletResponse resp = req.createResponse(501,
				"Request not implemented");
		resp.send();
	}
}
