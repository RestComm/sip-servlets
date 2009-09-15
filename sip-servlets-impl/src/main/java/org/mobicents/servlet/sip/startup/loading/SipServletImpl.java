/*
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.servlet.sip.startup.loading;

import javax.management.Notification;
import javax.management.ObjectName;
import javax.servlet.ServletException;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.tomcat.util.modeler.Registry;

/**
 * Sip implementation of the <b>Wrapper</b> interface that represents
 * an individual servlet definition. This class inherits from the StandardWrapper Tomcat Class.
 * No child Containers are allowed, and the parent Container must be a Context.
 *
 * @author Jean Deruelle
 */
public class SipServletImpl extends StandardWrapper {
	private static final long serialVersionUID = 1L;
	/**
     * The descriptive information string for this implementation.
     */
    protected static final String info =
        "org.mobicents.servlet.sip.startup.loading.SipServletImpl/1.0";
    
    static final String[] DEFAULT_SIP_SERVLET_METHODS = new String[] {
        "INVITE", "ACK", "BYE", "CANCEL", "INFO", "MESSAGE", "SUBSCRIBE", "NOTIFY",
        "OPTIONS", "PRACK", "PUBLISH", "REFER", "REGISTER", "UPDATE", 
        "SUCCESS_RESPONSE", "ERROR_RESPONSE", "BRANCH_RESPONSE", "REDIRECT_RESPONSE", "PROVISIONAL_RESPONSE" };
    
	private String icon;
	private String servletName;
	private String displayName;
	private String description;
	
	public SipServletImpl() {
		super();
	}
	
	@Override
	public void load() throws ServletException {
		super.load();
	}

	/**
	 * @return the icon
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * @param icon the icon to set
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * @return the servletName
	 */
	public String getServletName() {
		return servletName;
	}

	/**
	 * @param servletName the servletName to set
	 */
	public void setServletName(String servletName) {
		this.servletName = servletName;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	//copied over from super class changing the JMX name being registered j2eeType is now SipServlet instead of Servlet
	protected void registerJMX(StandardContext ctx) {

        String parentName = ctx.getName();
        parentName = ("".equals(parentName)) ? "/" : parentName;

        String hostName = ctx.getParent().getName();
        hostName = (hostName==null) ? "DEFAULT" : hostName;

        String domain = ctx.getDomain();

        String webMod= "//" + hostName + parentName;
        String onameStr = domain + ":j2eeType=SipServlet,name=" + getName() +
                          ",WebModule=" + webMod + ",J2EEApplication=" +
                          ctx.getJ2EEApplication() + ",J2EEServer=" +
                          ctx.getJ2EEServer();
        try {
            oname=new ObjectName(onameStr);
            controller=oname;
            Registry.getRegistry(null, null)
                .registerComponent(this, oname, null );
            
            // Send j2ee.object.created notification 
            if (this.getObjectName() != null) {
                Notification notification = new Notification(
                                                "j2ee.object.created", 
                                                this.getObjectName(), 
                                                sequenceNumber++);
                broadcaster.sendNotification(notification);
            }
        } catch( Exception ex ) {
            log.info("Error registering servlet with jmx " + this);
        }
    }
	
	 /**
     * Return descriptive information about this Container implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return (info);
    }
    
    /**
     * Gets the names of the methods supported by the underlying servlet.
     *
     * This is the same set of methods included in the Allow response header
     * in response to an OPTIONS request method processed by the underlying
     * servlet.
     *
     * @return Array of names of the methods supported by the underlying
     * servlet
     */
    public String[] getServletMethods() throws ServletException {
        return DEFAULT_SIP_SERVLET_METHODS;
    }

}
