package javax.servlet.sip;
/**
 * This listener interface can be implemented in order to get notifications of changes to the attribute lists of application sessions.
 * Since: 1.1
 */
public interface SipApplicationSessionAttributeListener extends java.util.EventListener{
    /**
     * Notification that an attribute has been added to an application session. Called after the attribute is added.
     */
    void attributeAdded(javax.servlet.sip.SipApplicationSessionBindingEvent ev);

    /**
     * Notification that an attribute has been removed from an application session. Called after the attribute is removed.
     */
    void attributeRemoved(javax.servlet.sip.SipApplicationSessionBindingEvent ev);

    /**
     * Notification that an attribute has been replaced in an application session. Called after the attribute is replaced.
     */
    void attributeReplaced(javax.servlet.sip.SipApplicationSessionBindingEvent ev);

}
