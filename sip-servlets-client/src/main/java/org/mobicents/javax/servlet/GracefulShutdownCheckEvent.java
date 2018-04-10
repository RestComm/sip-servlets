package org.mobicents.javax.servlet;

/**
 * During graceful shutdown period, the container will periodically query the
 * service logic if a premature shutdown is possible.
 *
 * The service logic may implement specific service criteria to coordinate with
 * container. The event will be only notified when the container identifies no
 * more active sessions are handled for the particular application.
 *
 *
 * The logic may indicate premature shutdown is not allowed by inserting an
 * attribute in the ServletContext "org.mobicents.servlet.sip.PREVENT_PREMATURE_SHUTDOWN".
 * If no attribute is put, then the container will shutdown the container right after
 * the event notification.
 * 
 * The frequency at which the container will notify the service logic maybe 
 * configured from server configuration. Check documentation on how to configure
 * "graceful-interval". By default this frequency is 30000 milliseconds.
 */
public class GracefulShutdownCheckEvent extends GracefulShutdownEvent {

    private final long elapsedTime;

    public GracefulShutdownCheckEvent(long elapsedTime, long timeToWait) {
        super(ContainerEventType.GRACEFUL_SHUTDOWN_CHECK, timeToWait);
        this.elapsedTime = elapsedTime;
    }

    /**
     * 
     * @return elapsed time in milliseconds from the original time when graceful
     * shutdown procedure was invoked.
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

}
