/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.jboss.as.clustering.web.infinispan.sip;

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.Messages;
import org.jboss.metadata.web.jboss.ReplicationGranularity;

/**
 * InfinispanSipMessages
 *
 * logging id range: 10330 - 10339
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author posfai.gergely@ext.alerant.hu
 */
@MessageBundle(projectCode = "JBAS")
interface InfinispanSipMessages {
    /**
     * The messages
     */
    InfinispanSipMessages MESSAGES = Messages.getBundle(InfinispanSipMessages.class);

    /**
     * Creates an exception indicating an unexpected exception occurred while starting the group communication service
     * for the cluster represented by the {@code clusterName} parameter.
     *
     * @param clusterName the cluster the service was attempting to start on.
     *
     * @return an {@link IllegalStateException} for the error.
     */
    @Message(id = 10330, value = "Unexpected exception while starting group communication service for %s")
    IllegalStateException errorStartingGroupCommunications(@Cause Throwable cause, String clusterName);

    /**
     * Creates an exception indicating an unexpected exception occurred while starting the lock manager for the cluster
     * represented by the {@code clusterName} parameter.
     *
     * @param clusterName the cluster the service was attempting to start on.
     *
     * @return an {@link IllegalStateException} for the error.
     */
    @Message(id = 10331, value = "Unexpected exception while starting lock manager for %s")
    IllegalStateException errorStartingLockManager(@Cause Throwable cause, String clusterName);

    /**
     * A message indicating a failure to configure a sip application for distributable sessions.
     *
     * @param cacheManagerName the cache manager name.
     * @param sessionCacheName the session cache name.
     *
     * @return the message.
     */
    @Message(id = 10332, value = "Failed to configure sip application for <distributable/> sessions.  %s.%s cache requires batching=\"true\".")
    String failedToConfigureSipApp(String cacheManagerName, String sessionCacheName);

    /**
     * Creates an exception indicating a failure to store attributes for the session.
     *
     * @param cause     the cause of the error.
     * @param sessionId the session id.
     *
     * @return a {@link RuntimeException} for the error.
     */
    @Message(id = 10333, value = "Failed to load session attributes for session: %s")
    RuntimeException failedToLoadSessionAttributes(@Cause Throwable cause, String sessionId);

    @Message(id = 10334, value = "Failed to store session attributes for session: %s")
    RuntimeException failedToStoreSessionAttributes(@Cause Throwable cause, String sessionId);

    /**
     * Creates an exception indicating an attempt to put a value of the type represented by the {@code typeClassName}
     * parameter into the map.
     *
     * @param typeClassName the type class name.
     * @param map           the map the value was attempted be placed in.
     *
     * @return an {@link IllegalArgumentException} for the error.
     */
    @Message(id = 10335, value = "Attempt to put value of type %s into %s entry")
    IllegalArgumentException invalidMapValue(String typeClassName, Object map);

    /**
     * Creates an exception indicating the replication granularity is unknown.
     *
     * @param value the invalid replication granularity.
     *
     * @return an {@link IllegalArgumentException} for the error.
     */
    @Message(id = 10336, value = "Unknown replication granularity: %s")
    IllegalArgumentException unknownReplicationGranularity(ReplicationGranularity value);
}
