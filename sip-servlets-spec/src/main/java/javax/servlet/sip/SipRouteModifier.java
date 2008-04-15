package javax.servlet.sip;

/**
 * Route modifiers as returned by the Application Router, used to interpret the returned route from the router.
 * @since 1.1
 *
 */
public enum SipRouteModifier{
	/**
	 * Indicates to the container to clear any popped route so far, such that SipServletRequest.getPoppedRoute() should now return null.
	 */
	CLEAR_ROUTE,
	/**
	 * Indicates that SipApplicationRouterInfo.getRoute() does not contain any valid route.
	 */	
	NO_ROUTE,
	/**
	 * Indicates that the route returned by SipApplicationRouterInfo.getRoute() is a valid route.
	 */
	ROUTE;
}
