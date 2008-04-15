package javax.servlet.sip;

/**
 * Routing regions used in the application selection process.
 * @since 1.1
 *
 */
public enum SipApplicationRoutingRegionType{
	/**
	 * The NEUTRAL region contains applications that do not service a specific subscriber.
	 */
	NEUTRAL, 
	/**
	 * The ORIGINATING region contains applications that service the caller.
	 */
	ORIGINATING, 
	/**
	 * The TERMINATING region contains applications that service the callee.
	 */
	TERMINATING;
}
