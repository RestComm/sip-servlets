package javax.servlet.sip;
/**
 * Notification that a SipApplicationSession has expired.
 * A SipApplicationSessionListener receiving this notification may attempt to extend the lifetime of the application instance corresponding to the expiring application session by invoking SipApplicationSession.setExpires(int).
 * @since: 1.1 
 */
public class SipApplicationSessionEvent extends java.util.EventObject{
    /**
     * Creates a new SipApplicationSessionEvent object.
     * @param appSession the expired application session
     */
    public SipApplicationSessionEvent(javax.servlet.sip.SipApplicationSession appSession){
    	super(appSession);
    }

    /**
     * Returns the expired session object.
     */
    public javax.servlet.sip.SipApplicationSession getApplicationSession(){
        return (SipApplicationSession)getSource(); 
    }

}
