package javax.servlet.sip;
/**
 * Events of this type are either sent to an object that implements SipSessionBindingListener when it is bound or unbound from a session, or to a SipSessionAttributeListener that has been configured in the deployment descriptor when any attribute is bound, unbound or replaced in a session.
 * The session binds the object by a call to SipSession.setAttribute and unbinds the object by a call to SipSession.removeAttribute.
 * @see SipSession, SipSessionBindingListener, SipSessionAttributeListener
 */
public class SipSessionBindingEvent extends java.util.EventObject{
	private String name;
    /**
     * Constructs an event that notifies an object that it has been bound to or unbound from a session. To receive the event, the object must implement
     * .
     * @param session the session to which the object is bound or unboundname - the name with which the object is bound or unbound
     */
    public SipSessionBindingEvent(javax.servlet.sip.SipSession session, java.lang.String name){
         super(session);
         this.name = name;
    }

    /**
     * Returns the name with which the object is bound to or unbound from the session.
     */
    public java.lang.String getName(){
        return name; 
    }

    /**
     * Returns the session to or from which the object is bound or unbound.
     */
    public javax.servlet.sip.SipSession getSession(){
        return (SipSession)getSource(); 
    }

}
