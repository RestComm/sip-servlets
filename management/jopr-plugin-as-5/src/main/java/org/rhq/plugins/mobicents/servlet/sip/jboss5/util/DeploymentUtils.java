/*
 * RHQ Management Platform
 * Copyright (C) 2005-2008 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.plugins.mobicents.servlet.sip.jboss5.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.deployers.spi.management.deploy.DeploymentManager;
import org.jboss.deployers.spi.management.deploy.DeploymentProgress;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.util.ZipUtil;
import org.rhq.plugins.jbossas5.factory.ProfileServiceFactory;
import org.rhq.plugins.mobicents.servlet.sip.jboss5.StandaloneManagedDeploymentComponent;

/**
 * @author Ian Springer
 */
public abstract class DeploymentUtils {
	private static final Log LOG = LogFactory.getLog(DeploymentUtils.class);

    public static boolean hasCorrectExtension(File archiveFile, ResourceType resourceType)
    {
        String resourceTypeName = resourceType.getName();
        String expectedExtension;
        if (resourceTypeName.equals(StandaloneManagedDeploymentComponent.RESOURCE_TYPE_EAR)) {
            expectedExtension = "ear";
        } else if (resourceTypeName.equals(StandaloneManagedDeploymentComponent.RESOURCE_TYPE_WAR)){
            expectedExtension = "war";
        } else if (resourceTypeName.equals(StandaloneManagedDeploymentComponent.RESOURCE_TYPE_CONVERGED_WAR)){
            expectedExtension = "war";
        } else if (resourceTypeName.equals(StandaloneManagedDeploymentComponent.RESOURCE_TYPE_RAR)){
            expectedExtension = "rar";
        } else {
            expectedExtension = "jar";
        }
        String archiveName = archiveFile.getName();
        int lastPeriod = archiveName.lastIndexOf(".");
        String extension = archiveName.substring(lastPeriod + 1);
        // TODO: String compare should be case-insensitive if on Windows.
        return (lastPeriod != -1 && expectedExtension.equals(extension));
    }        

    public static DeploymentStatus deployArchive(File archiveFile, File deployDirectory, boolean deployExploded)
            throws Exception
    {
        if (deployDirectory == null)
            throw new IllegalArgumentException("Deploy directory is null.");
        DeploymentManager deploymentManager = ProfileServiceFactory.getDeploymentManager();
        String archiveFileName = archiveFile.getName();
        DeploymentProgress progress;
        if (deployExploded) {
            LOG.debug("Deploying '" + archiveFileName + "' in exploded form...");
            File tempDir = new File(deployDirectory, archiveFile.getName() + ".rej");
            ZipUtil.unzipFile(archiveFile, tempDir);
            File archiveDir = new File(deployDirectory, archiveFileName);
            URL contentURL = archiveDir.toURI().toURL();
            if (!tempDir.renameTo(archiveDir))
                throw new IOException("Failed to rename '" + tempDir + "' to '" + archiveDir + "'.");
            progress = deploymentManager.distribute(archiveFileName, contentURL, false);
        } else {
            LOG.debug("Deploying '" + archiveFileName + "' in non-exploded form...");
            URL contentURL = archiveFile.toURI().toURL();
            File deployLocation = new File(deployDirectory, archiveFileName);
            boolean copyContent = !deployLocation.equals(archiveFile);
            progress = deploymentManager.distribute(archiveFileName, contentURL, copyContent);
        }
        run(progress);

        // Now that we've distributed the deployment, we need to start it!
        String[] repositoryNames = progress.getDeploymentID().getRepositoryNames();
        progress = deploymentManager.start(repositoryNames);
        return run(progress);
    }

    public static DeploymentStatus run(DeploymentProgress progress)
            throws Exception
    {
        progress.run();
        DeploymentStatus status = progress.getDeploymentStatus();
        if (status.isFailed())
            //noinspection ThrowableResultOfMethodCallIgnored
            throw new Exception(status.getMessage(), status.getFailure());
        return status;
    }

    private DeploymentUtils()
    {
    }	    
}
