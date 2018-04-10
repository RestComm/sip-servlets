package org.mobicents.javax.servlet;


/**
 * During graceful shutdown period, the container will notify the service logic
 * the procedure has been started.
 * 
 * 
 * Notification is only one-time till the different conditions are met to finally
 * stop the container.
 * 
 *
 */
public class GracefulShutdownStartedEvent extends GracefulShutdownEvent {

    public GracefulShutdownStartedEvent(long timeToWait) {
        super(ContainerEventType.GRACEFUL_SHUTDOWN_STARTED, timeToWait);
    }

}
