 /*
  * Jopr Management Platform
  * Copyright (C) 2005-2009 Red Hat, Inc.
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
package org.rhq.plugins.jbossas5.adapter.api;

import org.jboss.metatype.api.types.MetaType;
import org.rhq.plugins.jbossas5.adapter.impl.measurement.SimpleMetaValueMeasurementAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.HashMap;

public class MeasurementAdapterFactory
{

    private static final Log LOG = LogFactory.getLog(MeasurementAdapterFactory.class);

    private static Map<String, String> customAdapters = new HashMap<String, String>();

    {
        // Add customAdapters to the map
        customAdapters.put("NoClasses", "NoClasses");
    }

    public static MeasurementAdapter getMeasurementPropertyAdapter(MetaType metaType)
    {
        MeasurementAdapter measurementAdapter = null;
        if (metaType.isSimple())
        {
            measurementAdapter = new SimpleMetaValueMeasurementAdapter();
        }
        else if (metaType.isGeneric())
        {
            measurementAdapter = null;
        }
        else if (metaType.isComposite())
        {
            measurementAdapter = null;
        }
        else if (metaType.isTable())
        {
            measurementAdapter = null;
        }
        else if (metaType.isCollection())
        {
            measurementAdapter = null;
        }
        else if (metaType.isArray())
        {
            measurementAdapter = null;
        }

        return measurementAdapter;
    }

    public static MeasurementAdapter getCustomMeasurementPropertyAdapter(String measurementName)
    {
        MeasurementAdapter measurementAdapter = null;
        String adapterClassName = customAdapters.get(measurementName);
        if (adapterClassName != null && !adapterClassName.equals(""))
        {
            try
            {
                measurementAdapter = (MeasurementAdapter) Class.forName(adapterClassName).newInstance();
            }
            catch (InstantiationException e)
            {
                LOG.error("Cannot instantiate adapter class called " + adapterClassName, e);
            }
            catch (IllegalAccessException e)
            {
                LOG.error("Cannot access adapter class called " + adapterClassName, e);
            }
            catch (ClassNotFoundException e)
            {
                LOG.error("Cannot find adapter class called " + adapterClassName, e);
            }
        }
        return measurementAdapter;
    }
}
