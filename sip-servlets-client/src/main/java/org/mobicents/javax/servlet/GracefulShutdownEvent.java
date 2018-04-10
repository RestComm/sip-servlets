package org.mobicents.javax.servlet;

/**
 * Base class for GracefulShutodwn events.
 * 
 * @author jaime
 */
public class GracefulShutdownEvent extends ContainerEvent {

    private final long timeToWait;

    public GracefulShutdownEvent(ContainerEventType eventType, long timeToWait) {
        super(eventType);
        this.timeToWait = timeToWait;
    }

    /**
     * 
     * @return the original TimeToWait used to invoke the graceful shutdown
     * procedure in milliseconds.
     */
    public long getTimeToWait() {
        return timeToWait;
    }

}
