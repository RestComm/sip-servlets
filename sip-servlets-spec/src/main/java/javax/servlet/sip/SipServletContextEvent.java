package javax.servlet.sip;
/**
 * SIP Servlet specific context event.
 * @since 1.1 
 * @see SipServletListener
 */
public class SipServletContextEvent extends javax.servlet.ServletContextEvent{
	private SipServlet sipServlet = null;
    /**
     * Constructs a new SipServletContextEvent.
     * Parameters:context - the ServletContextservlet - the servlet, initialization of which triggered this event
     */
    public SipServletContextEvent(javax.servlet.ServletContext context, javax.servlet.sip.SipServlet servlet){
         super(context);
         this.sipServlet = servlet;
    }

    /**
     * Returns the servlet associated with the event SipServletContextEvent.
     */
    public javax.servlet.sip.SipServlet getSipServlet(){
        return sipServlet; 
    }

}
