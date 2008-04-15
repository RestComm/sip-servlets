package javax.servlet.sip;
/**
 * Events of this type are either sent to an object that implements SipApplicationSessionBindingListener when it is bound or unbound from an application session, or to a SipApplicationSessionAttributeListener that has been configured in the deployment descriptor when any attribute is bound, unbound or replaced in an application session.
 * The session binds the object by a call to SipApplicationSession.setAttribute(String, Object) and unbinds the object by a call to SipApplicationSession.removeAttribute(String).
 * @since: 1.1 
 * @see SipApplicationSession, SipApplicationSessionBindingListener, SipApplicationSessionAttributeListener
 */
public class SipApplicationSessionBindingEvent extends java.util.EventObject{
	private String name= null;
    /**
     * Constructs an event that notifies an object that it has been bound to or unbound from an application session. 
     * To receive the event, the object must implement SipApplicationSessionBindingListener.
     * @param session - the application ession to which the object is bound or unboundname - the name with which the object is bound or unbound
     */
    public SipApplicationSessionBindingEvent(javax.servlet.sip.SipApplicationSession session, java.lang.String name){
         super(session);
         this.name = name;
    }

    /**
     * Returns the application session to or from which the object is bound or unbound.
     */
    public javax.servlet.sip.SipApplicationSession getApplicationSession(){
        return (SipApplicationSession)getSource(); 
    }

    /**
     * Returns the name with which the object is bound to or unbound from the application session.
     */
    public java.lang.String getName(){
        return name;
    }

}
