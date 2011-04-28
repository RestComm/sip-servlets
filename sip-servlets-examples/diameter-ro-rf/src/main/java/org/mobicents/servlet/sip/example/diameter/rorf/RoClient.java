/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.mobicents.servlet.sip.example.diameter.rorf;

public interface RoClient {

  /**
   * Reserve some initial units to make sure the call should proceed.
   * No units are consumed in this stage, only reserved.
   * 
   * @param userId the user id.
   * @param serviceContextId the service-id.
   * @throws Exception
   */
  public abstract void reserveInitialUnits(String userId, String serviceContextId) throws Exception;

  /**
   * Starts the actual charging process, consuming the initially reserved units.
   * Updates are automatically handled at the end of each period.
   * 
   * @param userId the user id.
   * @param serviceContextId the service-id.
   * @throws Exception
   */
  public abstract void startCharging(String userId, String serviceContextId) throws Exception;

  /**
   * Stops the charging process, by returning the non-used units.
   * 
   * @param userId the user id.
   * @param serviceContextId the service-id.
   * @throws Exception
   */
  public abstract void stopCharging(String userId, String serviceContextId) throws Exception;

}