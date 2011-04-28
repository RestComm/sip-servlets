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

public interface RoClientListener {

  /**
   * Callback method for successful request for credit units.
   * 
   * @param amount the amount of granted units
   * @param finalUnits true if these are the last units
   * @throws Exception
   */
  public abstract void creditGranted(long amount, boolean finalUnits) throws Exception;

  /**
   * Callback method for unsuccessful request for credit units.
   * 
   * @param failureCode the code specifying why it failed
   * @throws Exception
   */
  public abstract void creditDenied(int failureCode) throws Exception;

  /**
   * Callback method for end of credit. Service should be terminated.
   * 
   * @throws Exception
   */
  public abstract void creditTerminated() throws Exception;

}
