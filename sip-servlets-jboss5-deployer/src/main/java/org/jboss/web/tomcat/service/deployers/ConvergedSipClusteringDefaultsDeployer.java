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
package org.jboss.web.tomcat.service.deployers;

import org.jboss.deployers.spi.DeploymentException;
import org.jboss.deployers.structure.spi.DeploymentUnit;
import org.jboss.metadata.sip.jboss.JBossConvergedSipMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.jboss.PassivationConfig;
import org.jboss.metadata.web.jboss.ReplicationConfig;
import org.jboss.metadata.web.jboss.ReplicationGranularity;

/**
 * @author jean.deruelle@gmail.com
 * 
 */
public class ConvergedSipClusteringDefaultsDeployer extends
		ClusteringDefaultsDeployer {

	/**
	 * Injects the configured default property values into any
	 * {@link JBossWebMetaData} attached to <code>unit</code> if the relevant
	 * property isn't already configured.
	 */
	public void deploy(DeploymentUnit unit) throws DeploymentException {
		super.deploy(unit);
		JBossConvergedSipMetaData convergedMetaData = (JBossConvergedSipMetaData) unit.getAttachment(JBossConvergedSipMetaData.class);
		if (convergedMetaData != null && convergedMetaData.getDistributable() != null) {
			if (convergedMetaData.getDistributable() != null) {
				addReplicationConfigDefaults(convergedMetaData);

				addPassivationConfigDefaults(convergedMetaData);
			}
		}
	}

	/**
	 * Inject default values in {@link PassivationConfig}
	 * 
	 * @param metaData
	 */
	private void addPassivationConfigDefaults(JBossWebMetaData metaData) {
		PassivationConfig passCfg = metaData.getPassivationConfig();
		if (passCfg == null) {
			passCfg = new PassivationConfig();
			metaData.setPassivationConfig(passCfg);
		}

		if (passCfg.getUseSessionPassivation() == null)
			passCfg.setUseSessionPassivation(Boolean
					.valueOf(this.isUseSessionPassivation()));
		if (passCfg.getPassivationMinIdleTime() == null)
			passCfg.setPassivationMinIdleTime(new Integer(
					this.getPassivationMinIdleTime()));
		if (passCfg.getPassivationMinIdleTime() == null)
			passCfg.setPassivationMaxIdleTime(new Integer(
					this.getPassivationMaxIdleTime()));
	}

	/**
	 * Inject default values in {@link ReplicationConfig}
	 * 
	 * @param metaData
	 */
	private void addReplicationConfigDefaults(JBossWebMetaData metaData) {
		ReplicationConfig repCfg = metaData.getReplicationConfig();
		if (repCfg == null) {
			repCfg = new ReplicationConfig();
			metaData.setReplicationConfig(repCfg);
		}

		if (repCfg.getUseJK() == null && isUseJK() != null)
			repCfg.setUseJK(this.isUseJK());
		if (repCfg.getSnapshotMode() == null)
			repCfg.setSnapshotMode(this.getSnapshotMode());
		if (repCfg.getSnapshotInterval() == null)
			repCfg.setSnapshotInterval(new Integer(this.getSnapshotInterval()));
		if (repCfg.getReplicationGranularity() == null)
			repCfg.setReplicationGranularity(this.getReplicationGranularity());
		if (repCfg.getReplicationTrigger() == null)
			repCfg.setReplicationTrigger(this.getReplicationTrigger());
		if (repCfg.getReplicationFieldBatchMode() == null)
			repCfg.setReplicationFieldBatchMode(Boolean
					.valueOf(this.isReplicationFieldBatchMode()));

		if (repCfg.getCacheName() == null) {
			String cacheConfig = ReplicationGranularity.FIELD == repCfg
					.getReplicationGranularity() ? getFieldGranularityCacheName()
					: getCacheName();
			repCfg.setCacheName(cacheConfig);
		}

		if (repCfg.getMaxUnreplicatedInterval() == null) {
			repCfg.setMaxUnreplicatedInterval(new Integer(
					getMaxUnreplicatedInterval()));
		}
	}

}
