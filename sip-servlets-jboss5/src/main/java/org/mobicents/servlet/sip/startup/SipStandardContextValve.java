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
package org.mobicents.servlet.sip.startup;

import java.io.IOException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.catalina.Container;
import org.apache.catalina.Globals;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.Constants;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.util.StringManager;
import org.apache.log4j.Logger;
import org.apache.tomcat.util.buf.MessageBytes;
import org.mobicents.servlet.sip.core.session.MobicentsSipApplicationSession;
import org.mobicents.servlet.sip.core.session.SessionManagerUtil;
import org.mobicents.servlet.sip.core.session.SipApplicationSessionKey;
import org.mobicents.servlet.sip.core.session.SipManager;


/**
 * Valve that implements the default basic behavior for the
 * <code>SipStandardContext</code> container implementation.
 * <p>
 * <b>USAGE CONSTRAINT</b>:  This implementation is likely to be useful only
 * when processing HTTP requests.
 *
 * The code was copy pasted from tomcat 5.5.20 source and some specific code was added on invoke 
 * method to store the htpp session in the threadlocal of the sipfactoryfacade and check if the request
 * has an application key associated with it
 * 
 * @author Jean Deruelle
 *
 */
final class SipStandardContextValve extends org.apache.catalina.valves.ValveBase {
	
	// ----------------------------------------------------- Instance Variables


    /**
     * The descriptive information related to this implementation.
     */
    private static final String info =
        "org.apache.catalina.core.StandardContextValve/1.0";


    /**
     * The string manager for this package.
     */
    private static final StringManager sm =
        StringManager.getManager(Constants.Package);


    private static transient Logger logger = Logger.getLogger(SipStandardContextValve.class);

    
    private SipStandardContext context = null;
    

    // ------------------------------------------------------------- Properties


    /**
     * Return descriptive information about this Valve implementation.
     */
    public String getInfo() {

        return (info);

    }


    // --------------------------------------------------------- Public Methods


    /**
     * Cast to a StandardContext right away, as it will be needed later.
     * 
     * @see org.apache.catalina.Contained#setContainer(org.apache.catalina.Container)
     */
    public void setContainer(Container container) {
        super.setContainer(container);
        context = (SipStandardContext) container;
    }

    
    /**
     * Select the appropriate child Wrapper to process this request,
     * based on the specified request URI.  If no matching Wrapper can
     * be found, return an appropriate HTTP error.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     * @param valveContext Valve context used to forward to the next Valve
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    public final void invoke(Request request, Response response)
        throws IOException, ServletException {

        // Disallow any direct access to resources under WEB-INF or META-INF
        MessageBytes requestPathMB = request.getRequestPathMB();
        if ((requestPathMB.startsWithIgnoreCase("/META-INF/", 0))
            || (requestPathMB.equalsIgnoreCase("/META-INF"))
            || (requestPathMB.startsWithIgnoreCase("/WEB-INF/", 0))
            || (requestPathMB.equalsIgnoreCase("/WEB-INF"))) {
            String requestURI = request.getDecodedRequestURI();
            notFound(requestURI, response);
            return;
        }

        // Wait if we are reloading
        while (context.getPaused()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                ;
            }
        }

        // Select the Wrapper to be used for this Request
        Wrapper wrapper = request.getWrapper();
        if (wrapper == null) {
            String requestURI = request.getDecodedRequestURI();
            notFound(requestURI, response);
            return;
        }

        // Normal request processing
        Object instances[] = context.getApplicationEventListeners();

        ServletRequestEvent event = null;

        if ((instances != null) 
                && (instances.length > 0)) {
            event = new ServletRequestEvent
                (((StandardContext) container).getServletContext(), 
                 request.getRequest());
            // create pre-service event
            for (int i = 0; i < instances.length; i++) {
                if (instances[i] == null)
                    continue;
                if (!(instances[i] instanceof ServletRequestListener))
                    continue;
                ServletRequestListener listener =
                    (ServletRequestListener) instances[i];
                try {
                    listener.requestInitialized(event);
                } catch (Throwable t) {
                    container.getLogger().error(sm.getString("requestListenerValve.requestInit",
                                     instances[i].getClass().getName()), t);
                    ServletRequest sreq = request.getRequest();
                    sreq.setAttribute(Globals.EXCEPTION_ATTR,t);
                    return;
                }
            }
        }
        context.enterSipApp(null, null, context.getSipManager(), false, false);        
    	//the line below was replaced by the whole bunch of code because getting the parameter from the request is causing
    	//JRuby-Rails persistence to fail, go figure...
//			String sipApplicationKey = request.getParameter(MobicentsSipApplicationSession.SIP_APPLICATION_KEY_PARAM_NAME);
    	
    	String sipApplicationKey = null;
    	String queryString = request.getQueryString();
    	if(queryString != null) {
        	int indexOfSipAppKey = queryString.indexOf(MobicentsSipApplicationSession.SIP_APPLICATION_KEY_PARAM_NAME);
        		        	
        	if(indexOfSipAppKey != -1) {
        		// +1 to remove the = sign also
        		String sipAppKeyParam = queryString.substring(indexOfSipAppKey + MobicentsSipApplicationSession.SIP_APPLICATION_KEY_PARAM_NAME.length() + 1);
        		int indexOfPoundSign = sipAppKeyParam.indexOf("&");
        		if(indexOfPoundSign != -1) {
        			sipAppKeyParam = sipAppKeyParam.substring(0, indexOfPoundSign);
        		} 
        		sipApplicationKey = sipAppKeyParam;
        	}
    	}
    	
		if(sipApplicationKey != null && sipApplicationKey.length() > 0) {
			try {
				SipApplicationSessionKey sipApplicationSessionKey = 
					SessionManagerUtil.parseSipApplicationSessionKey(sipApplicationKey);
				MobicentsSipApplicationSession sipApplicationSessionImpl = 
					((SipManager)context.getManager()).getSipApplicationSession(sipApplicationSessionKey, false);
				sipApplicationSessionImpl.addHttpSession(request.getSession());
			} catch (ParseException pe) {
				logger.error("Unexpected exception while parsing the sip application session key" + sipApplicationKey, pe);
			}
		} else {
			// Fix for Issue 882 : HTTP requests to a SIP application always create an HTTP session, even for static resources
			// Don't create an http session if not already created
			final HttpSession httpSession = request.getSession(false);
			if(httpSession != null) {
				context.getSipFactoryFacade().storeHttpSession(httpSession);
			}
		}
		
        wrapper.getPipeline().getFirst().invoke(request, response);
        context.exitSipApp(null, null);
        
        if ((instances !=null ) &&
                (instances.length > 0)) {
            // create post-service event
            for (int i = 0; i < instances.length; i++) {
                if (instances[i] == null)
                    continue;
                if (!(instances[i] instanceof ServletRequestListener))
                    continue;
                ServletRequestListener listener =
                    (ServletRequestListener) instances[i];
                try {
                    listener.requestDestroyed(event);
                } catch (Throwable t) {
                    container.getLogger().error(sm.getString("requestListenerValve.requestDestroy",
                                     instances[i].getClass().getName()), t);
                    ServletRequest sreq = request.getRequest();
                    sreq.setAttribute(Globals.EXCEPTION_ATTR,t);
                }
            }
        }
                
    }


//    /**
//     * Select the appropriate child Wrapper to process this request,
//     * based on the specified request URI.  If no matching Wrapper can
//     * be found, return an appropriate HTTP error.
//     *
//     * @param request Request to be processed
//     * @param response Response to be produced
//     * @param valveContext Valve context used to forward to the next Valve
//     *
//     * @exception IOException if an input/output error occurred
//     * @exception ServletException if a servlet error occurred
//     */
//    public final void event(Request request, Response response, CometEvent event)
//        throws IOException, ServletException {
//
//        // Select the Wrapper to be used for this Request
//        Wrapper wrapper = request.getWrapper();
//
//        // Normal request processing
//        // FIXME: This could be an addition to the core API too
//        /*
//        Object instances[] = context.getApplicationEventListeners();
//
//        ServletRequestEvent event = null;
//
//        if ((instances != null) 
//                && (instances.length > 0)) {
//            event = new ServletRequestEvent
//                (((StandardContext) container).getServletContext(), 
//                 request.getRequest());
//            // create pre-service event
//            for (int i = 0; i < instances.length; i++) {
//                if (instances[i] == null)
//                    continue;
//                if (!(instances[i] instanceof ServletRequestListener))
//                    continue;
//                ServletRequestListener listener =
//                    (ServletRequestListener) instances[i];
//                try {
//                    listener.requestInitialized(event);
//                } catch (Throwable t) {
//                    container.getLogger().error(sm.getString("requestListenerValve.requestInit",
//                                     instances[i].getClass().getName()), t);
//                    ServletRequest sreq = request.getRequest();
//                    sreq.setAttribute(Globals.EXCEPTION_ATTR,t);
//                    return;
//                }
//            }
//        }
//        */
//
//        wrapper.getPipeline().getFirst().event(request, response, event);
//
//        /*
//        if ((instances !=null ) &&
//                (instances.length > 0)) {
//            // create post-service event
//            for (int i = 0; i < instances.length; i++) {
//                if (instances[i] == null)
//                    continue;
//                if (!(instances[i] instanceof ServletRequestListener))
//                    continue;
//                ServletRequestListener listener =
//                    (ServletRequestListener) instances[i];
//                try {
//                    listener.requestDestroyed(event);
//                } catch (Throwable t) {
//                    container.getLogger().error(sm.getString("requestListenerValve.requestDestroy",
//                                     instances[i].getClass().getName()), t);
//                    ServletRequest sreq = request.getRequest();
//                    sreq.setAttribute(Globals.EXCEPTION_ATTR,t);
//                }
//            }
//        }
//        */
//      
//    }


    // -------------------------------------------------------- Private Methods


    /**
     * Report a "not found" error for the specified resource.  FIXME:  We
     * should really be using the error reporting settings for this web
     * application, but currently that code runs at the wrapper level rather
     * than the context level.
     *
     * @param requestURI The request URI for the requested resource
     * @param response The response we are creating
     */
    private void notFound(String requestURI, HttpServletResponse response) {

        try {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, requestURI);
        } catch (IllegalStateException e) {
            ;
        } catch (IOException e) {
            ;
        }

    }
}
