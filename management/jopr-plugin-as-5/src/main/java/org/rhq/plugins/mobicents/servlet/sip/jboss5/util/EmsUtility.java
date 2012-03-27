/*
 * Jopr Management Platform
 * Copyright (C) 2005-2008 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License, version 2, as
 * published by the Free Software Foundation, and/or the GNU Lesser
 * General Public License, version 2.1, also as published by the Free
 * Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License and the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * and the GNU Lesser General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.rhq.plugins.mobicents.servlet.sip.jboss5.util;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.operation.EmsOperation;
import org.mc4j.ems.connection.bean.parameter.EmsParameter;

/**
 * @author Ian Springer
 */
public class EmsUtility {
    /**
     * Retrieves an MBean operation from an EMS MBean proxy which matches the specified name and parameter types.
     * The method is modeled after {@link java.lang.Class#getMethod(String, Class[])}.
     *
     * @param mbean an EMS MBean proxy
     * @param name the name of the operation
     * @param parameterTypes the list of parameter types
     *
     * @return the <code>EmsOperation</code> object that matches the specified
     *         <code>name</code> and <code>parameterTypes</code>
     *
     * @throws NoSuchMethodException if a matching operation is not found
     */
    public static EmsOperation getOperation(EmsBean mbean, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        if (mbean == null || name == null || parameterTypes == null)
            throw new IllegalArgumentException("All parameters must be non-null.");

        String[] parameterTypeNames = new String[parameterTypes.length];
        int i = 0;
        for (Class<?> paramType : parameterTypes) {
            parameterTypeNames[i] = paramType.getName();
        }

        return getOperation(mbean, name, parameterTypeNames);
    }

    public static EmsOperation getOperation(EmsBean mbean, String name, String... parameterTypeNames) throws NoSuchMethodException {
        if (mbean == null || name == null || parameterTypeNames == null)
            throw new IllegalArgumentException("All parameters must be non-null.");
        SortedSet<EmsOperation> operations = mbean.getOperations();

        operationsLoop: for (EmsOperation operation : operations) {
            List<EmsParameter> operationParameters = operation.getParameters();

            if (operationParameters.size() != parameterTypeNames.length || !operation.getName().equals(name))
                // Different name or number of params than what we are looking for - move on...
                continue;
            // At this point, name and number of params match. Now compare the parameter types...
            for (int i = 0; i < operationParameters.size(); i++) {
                EmsParameter operationParameter = operationParameters.get(i);
                if (!operationParameter.getType().equals(parameterTypeNames[i]))
                    // One of the params doesn't match - move on...
                    continue operationsLoop;
            }
            // If we made it here, we have a match.
            return operation;
        }
        // If we made it here, we failed to find a match.
        throw new NoSuchMethodException("Operation named [" + name + "] with parameters [" + Arrays.asList(parameterTypeNames) + "] not found on MBean [" + mbean.getBeanName() + "].");
    }

}
