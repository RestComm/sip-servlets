package javax.servlet.sip;
/**
 * A utility class providing additional support for converged HTTP/SIP applications and converged J2EE/SIP applications.
 * This class can be accessed through the ServletContext parameter named javax.servlet.sip.sessionsutil or it can be injected using the annotation @SipSessionsUtil.
 * Since: 1.1
 */
public interface SipSessionsUtil{
    /**
     * Returns the SipApplicationSession for a given applicationSessionId. The applicationSessionId String is the same as that obtained through SipApplicationSession.getId(). The method shall return the Application Session only if the queried application session belongs to the application from where this method is invoked. As an example if there exists a SIP Application with some J2EE component like a Message Driven Bean, bundled in the same application archive file (.war), then if the id of SipApplicationSession is known to the MDB it can get a reference to the SipApplicationSession object using this method. If this MDB were in a different application then it would not possible for it to access the SipApplicationSession. The method returns null in case the container does not find the SipApplicationSession instance matching the ID.
     */
    javax.servlet.sip.SipApplicationSession getApplicationSession(java.lang.String applicationSessionId);

}
