/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
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
package org.mobicents.servlet.sip.startup.jboss;

import org.jboss.deployers.vfs.spi.structure.VFSDeploymentUnit;
import org.jboss.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.web.WebApplication;
import org.jboss.web.deployers.AbstractWarDeployer;
import org.jboss.web.deployers.AbstractWarDeployment;
import org.jboss.web.deployers.WebModule;

/**
 * Extends JBoss 5 WebModule class to provide support for Converged http/sip applications or pure sip applications
 * 
 * @see WebModule
 * @see AbstractWarDeployer
 * 
 * @author jean.deruelle@gmail.com
 * 
 */
public class ConvergedSipModule extends WebModule {

	private VFSDeploymentUnit di;
	private AbstractWarDeployer container;
	private AbstractWarDeployment deployment;

	public ConvergedSipModule(VFSDeploymentUnit di,
			AbstractWarDeployer container, AbstractWarDeployment deployment) {
		super(di, container, deployment);
		this.di = di;
		this.container = container;
		this.deployment = deployment;
		this.deployment.setDeploymentUnit(di);
	}

	public void destroy() {
		this.di = null;
		this.container = null;
		this.deployment = null;
	}

	/**
	 * Invokes the deployer start
	 */
	public synchronized void startModule() throws Exception {
		// Get the war URL
		JBossWebMetaData webMetaData = di.getAttachment(JBossWebMetaData.class);
		JBossConvergedSipMetaData sipMetaData = di.getAttachment(JBossConvergedSipMetaData.class);
		if(sipMetaData != null && (webMetaData == null || (webMetaData != null && webMetaData instanceof JBossConvergedSipMetaData))) {
			WebApplication webApp = deployment.start(di, sipMetaData);
			String warURL = di.getName();
			container.addDeployedApp(warURL, webApp);
		} else {
			super.startModule();
		}
	}
}
