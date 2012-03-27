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
package org.rhq.plugins.mobicents.servlet.sip.jboss5.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.operation.EmsOperation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class MainDeployer {
	private final Log log = LogFactory.getLog(this.getClass());
	public static final String MAIN_DEPLOYER = "jboss.system:service=MainDeployer";

    private EmsOperation deployOperation;
    private EmsOperation redeployOperation;
    private EmsOperation undeployOperation;

    public MainDeployer(EmsConnection connection) throws NoSuchMethodException {
        EmsBean mainDeployer = connection.getBean(MAIN_DEPLOYER);
        if (mainDeployer == null) {
            throw new IllegalStateException("MBean named [" + MAIN_DEPLOYER + "] does not exist.");
        }
        this.deployOperation = EmsUtility.getOperation(mainDeployer, "deploy", URL.class);
        this.redeployOperation = EmsUtility.getOperation(mainDeployer, "redeploy", URL.class);
        this.undeployOperation = EmsUtility.getOperation(mainDeployer, "undeploy", URL.class);
    }

    public void deploy(File file) throws DeployerException {
        log.debug("Deploying " + file + "...");
        try {
            URL url = toURL(file);
            this.deployOperation.invoke(new Object[]{url});
        }
        catch (RuntimeException e) {
            throw new DeployerException("Failed to deploy " + file, e);
        }
    }

    public void redeploy(File file) throws DeployerException {
        log.debug("Redeploying " + file + "...");
        try {
            URL url = toURL(file);
            this.redeployOperation.invoke(new Object[]{url});
        }
        catch (RuntimeException e) {
            throw new DeployerException("Failed to redeploy " + file, e);
        }
    }

    public void undeploy(File file) throws DeployerException {
        log.debug("Undeploying " + file + "...");
        try {
            URL url = toURL(file);
            this.undeployOperation.invoke(new Object[]{url});
        }
        catch (RuntimeException e) {
            throw new DeployerException("Failed to undeploy " + file, e);
        }
    }

    private static URL toURL(File file) {
        URL url;
        try {
            url = file.toURI().toURL();
        }
        catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        return url;
    }

    public class DeployerException extends Exception
    {
        DeployerException(String message) {
            super(message);
        }

        DeployerException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
