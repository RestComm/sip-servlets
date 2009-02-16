package org.mobicents.sip.servlet.tooling;

import org.eclipse.wst.common.project.facet.core.IActionConfigFactory;

public class SipServletApplicationConfig {
	private String appName="";
	private String description="";
	private String mainServletName="";
	private String mainServletDesciption="";
	private String mainServletClass="";
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getMainServletName() {
		return mainServletName;
	}
	public void setMainServletName(String mainServletName) {
		this.mainServletName = mainServletName;
	}
	public String getMainServletDesciption() {
		return mainServletDesciption;
	}
	public void setMainServletDesciption(String mainServletDesciption) {
		this.mainServletDesciption = mainServletDesciption;
	}
	public String getMainServletClass() {
		return mainServletClass;
	}
	public void setMainServletClass(String mainServletClass) {
		this.mainServletClass = mainServletClass;
	}
	public static final class Factory implements IActionConfigFactory
    {
        public Object create()
        {
            return new SipServletApplicationConfig();
        }
    }

}
