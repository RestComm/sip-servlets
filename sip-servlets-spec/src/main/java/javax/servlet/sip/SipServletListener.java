package javax.servlet.sip;
/**
 * Containers are required to invoke init() on the servlets before the servlets are ready for service. The servlet can only be used after succesful initialization. Since SIP is a peer-to-peer protocol and some servlets may act as UACs, the container is required to let the servlet know when it is succesfully initialized by invoking SipServletListener.
 * Since: 1.1 See Also:SipServletContextEvent
 */
public interface SipServletListener{
    /**
     * Notification that the servlet was succesfully initialized
     */
    void servletInitialized(javax.servlet.sip.SipServletContextEvent ce);

}
