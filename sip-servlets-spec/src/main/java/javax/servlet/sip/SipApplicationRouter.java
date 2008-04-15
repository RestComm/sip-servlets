package javax.servlet.sip;

import java.util.List;

/**
 * This interface class specifies the API between the container and the application router.
 * Since: 1.1
 */
public interface SipApplicationRouter{
    /**
     * Container notifies application router that new applications are deployed.
     */
    void applicationDeployed(List<String> newlyDeployedApplicationNames);

    /**
     * Container notifies application router that some applications are undeployed.
     */
    void applicationUndeployed(List<String> undeployedApplicationNames);

    /**
     * Container calls this method when it finishes using this application router.
     */
    void destroy();

    /**
     * This method is called by the container when a servlet sends or proxies an initial SipServletRequest. The application router returns a set of information. See @{link SipApplicationRouterInfo} for details.
     */
    javax.servlet.sip.SipApplicationRouterInfo getNextApplication(javax.servlet.sip.SipServletRequest initialRequest, javax.servlet.sip.SipApplicationRoutingRegion region, javax.servlet.sip.SipApplicationRoutingDirective directive, java.io.Serializable stateInfo);

    /**
     * Initializes the SipApplicationRouter.
     */
    void init();

    /**
     * Container calls this method to initialize the application router, with applications that are currently deployed.
     */
    void init(List<String> deployedApplicationNames);

}
