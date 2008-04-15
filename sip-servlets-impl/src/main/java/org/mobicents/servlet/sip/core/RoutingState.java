package org.mobicents.servlet.sip.core;

/**
 * Enum holding the different states a request can be while being routed.
 *  
 * @author Jean Deruelle
 *
 */
public enum RoutingState {
	// initial request being routed
	INITIAL,	
	// subsequent request being routed
	SUBSEQUENT,
	// merged request being routed
	MERGED,
	//final response sent for this request, stop routing
	FINAL_RESPONSE_SENT,
	// request has been proxied, stop routing
	PROXIED,
	//request has been relayed, stop routing
	RELAYED, 
	//request has been canncelled, stop routing
	CANCELLED;
}
