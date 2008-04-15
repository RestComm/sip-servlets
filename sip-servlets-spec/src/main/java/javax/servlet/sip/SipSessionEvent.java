package javax.servlet.sip;
/**
 * This is the class representing event notifications for changes to SipSessions within a SIP servlet application.
 */
public class SipSessionEvent extends java.util.EventObject{
    /**
     * Construct a session event from the given source.
     * @param source the affected SipSession object
     */
    public SipSessionEvent(javax.servlet.sip.SipSession source){
         super(source);
    }

    /**
     * Returns the session that changed.
     */
    public javax.servlet.sip.SipSession getSession(){
        return (SipSession) getSource(); 
    }

}
