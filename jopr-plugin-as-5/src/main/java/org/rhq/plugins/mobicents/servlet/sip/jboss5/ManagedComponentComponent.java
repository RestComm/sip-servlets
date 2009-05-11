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
package org.rhq.plugins.mobicents.servlet.sip.jboss5;

 import java.util.Map;
 import java.util.Set;

 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;

 import org.jboss.deployers.spi.management.ManagementView;
 import org.jboss.managed.api.ComponentType;
 import org.jboss.managed.api.ManagedComponent;
 import org.jboss.managed.api.ManagedOperation;
 import org.jboss.managed.api.ManagedProperty;
 import org.jboss.managed.api.RunState;
 import org.jboss.metatype.api.values.CompositeValue;
 import org.jboss.metatype.api.values.MetaValue;
 import org.jboss.metatype.api.values.SimpleValue;

 import org.rhq.core.domain.configuration.Configuration;
 import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;
 import org.rhq.core.domain.measurement.AvailabilityType;
 import org.rhq.core.domain.measurement.DataType;
 import org.rhq.core.domain.measurement.MeasurementDataNumeric;
 import org.rhq.core.domain.measurement.MeasurementDataTrait;
 import org.rhq.core.domain.measurement.MeasurementReport;
 import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
 import org.rhq.core.domain.operation.OperationDefinition;
 import org.rhq.core.domain.resource.ResourceType;
 import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
 import org.rhq.core.pluginapi.inventory.DeleteResourceFacet;
 import org.rhq.core.pluginapi.inventory.ResourceComponent;
 import org.rhq.core.pluginapi.inventory.ResourceContext;
 import org.rhq.core.pluginapi.measurement.MeasurementFacet;
 import org.rhq.core.pluginapi.operation.OperationFacet;
 import org.rhq.core.pluginapi.operation.OperationResult;
 import org.rhq.plugins.jbossas5.factory.ProfileServiceFactory;
 import org.rhq.plugins.jbossas5.util.ConversionUtils;
 import org.rhq.plugins.jbossas5.util.ResourceTypeUtils;
 import org.jetbrains.annotations.NotNull;

 /**
 * Service ResourceComponent for all {@link ManagedComponent}s in a Profile.
 *
 * @author Ian Springer
 * @author Jason Dobies
 * @author Mark Spritzler
 */
public class ManagedComponentComponent extends AbstractManagedComponent
        implements ResourceComponent, ConfigurationFacet, DeleteResourceFacet, OperationFacet, MeasurementFacet
{
    public static final String COMPONENT_TYPE_PROPERTY = "componentType";
    public static final String COMPONENT_SUBTYPE_PROPERTY = "componentSubtype";
    public static final String COMPONENT_NAME_PROPERTY = "componentName";

    private final Log log = LogFactory.getLog(this.getClass());

    private String componentName;
    private ComponentType componentType;    

    // ResourceComponent Implementation  --------------------------------------------

    public AvailabilityType getAvailability()
    {
        RunState runState = getManagedComponent().getRunState();
        return (runState == RunState.RUNNING) ? AvailabilityType.UP :
                AvailabilityType.DOWN;
    }

    public void start(ResourceContext resourceContext) throws Exception {
        super.start(resourceContext);
        this.componentType = ConversionUtils.getComponentType(getResourceContext().getResourceType());
        Configuration pluginConfig = resourceContext.getPluginConfiguration();
        this.componentName = pluginConfig.getSimple(COMPONENT_NAME_PROPERTY).getStringValue();
        log.trace("Started ResourceComponent for " + getResourceDescription() + ", managing " + this.componentType
                + " component '" + this.componentName + "'.");
    }

    public void stop()
    {
        return;
    }

    // DeleteResourceFacet Implementation  --------------------------------------------

    public void deleteResource() throws Exception
    {
        throw new UnsupportedOperationException("Deletion of " + getResourceContext().getResourceType().getName()
                + " Resources is not currently supported.");
        /*
        ManagedComponent managedComponent = getManagedComponent();
        log.debug("Removing " + getResourceDescription() + " with component " + toString(managedComponent) + "...");
        ManagementView managementView = ProfileServiceFactory.getCurrentProfileView();
        managementView.removeComponent(managedComponent);
        managementView.reload();
        */
    }

    // OperationFacet Implementation  --------------------------------------------

    public OperationResult invokeOperation(String name, Configuration parameters) throws Exception
    {
        OperationDefinition operationDefinition = getOperationDefinition(name);
        ManagedOperation managedOperation = getManagedOperation(operationDefinition);
        // Convert parameters into MetaValue array.
        MetaValue[] parameterMetaValues = ConversionUtils.convertOperationsParametersToMetaValues(managedOperation, 
                parameters, operationDefinition);
        // invoke() takes a varargs, so we need to pass an empty array, rather than null.
        MetaValue resultMetaValue = managedOperation.invoke(parameterMetaValues);
        OperationResult result = new OperationResult();
        // Convert result MetaValue to corresponding Property type.
        ConversionUtils.convertManagedOperationResults(managedOperation, resultMetaValue, result.getComplexResults(),
                operationDefinition);
        return result;
    }

    // MeasurementFacet Implementation  --------------------------------------------

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> metrics) throws Exception
    {
        ManagedComponent managedComponent = getManagedComponent();
        for (MeasurementScheduleRequest request : metrics)
        {
            try {
                if (request.getName().equals("runState")) {
                    String runState = managedComponent.getRunState().name();
                    report.addData(new MeasurementDataTrait(request, runState));
                } else {
                    SimpleValue simpleValue = getSimpleValue(managedComponent, request);
                    if (simpleValue != null)
                        addSimpleValueToMeasurementReport(report, request, simpleValue);
                }
            } catch (Exception e) {
                log.error("Failed to collect metric for " + request, e);
            }
        }
    }

    // ------------ AbstractManagedComponent implementation -------------

    protected Map<String, ManagedProperty> getManagedProperties() {
        return getManagedComponent().getProperties();
    }

    protected Log getLog() {
        return this.log;
    }

    protected void updateComponent() throws Exception {
        ManagedComponent managedComponent = getManagedComponent();
        log.trace("Updating " + getResourceDescription() + " with component " + toString(managedComponent) + "...");
        ManagementView managementView = ProfileServiceFactory.getCurrentProfileView();
        managementView.updateComponent(managedComponent);
        managementView.reload();
    }

    // ------------------------------------------------------------------------------

    private SimpleValue getSimpleValue(ManagedComponent managedComponent, MeasurementScheduleRequest request) {
        String metricName = request.getName();
        int dotIndex = metricName.indexOf('.');
        String metricPropName = (dotIndex == -1) ? metricName : metricName.substring(0, dotIndex);
        ManagedProperty metricProp = managedComponent.getProperty(metricPropName);
        SimpleValue simpleValue;
        if (dotIndex == -1)
            simpleValue = (SimpleValue)metricProp.getValue();
        else {
            CompositeValue compositeValue = (CompositeValue)metricProp.getValue();
            String key = metricName.substring(dotIndex + 1);
            simpleValue = (SimpleValue)compositeValue.get(key);
        }
        if (simpleValue == null)
            log.debug("Profile service returned null value for metric [" + request.getName() + "].");
        return simpleValue;
    }

    private void addSimpleValueToMeasurementReport(MeasurementReport report, MeasurementScheduleRequest request, SimpleValue simpleValue) {
        DataType dataType = request.getDataType();
        switch (dataType) {
            case MEASUREMENT:
                try {
                    MeasurementDataNumeric dataNumeric = new MeasurementDataNumeric(request,
                            Double.valueOf(simpleValue.getValue().toString()));
                    report.addData(dataNumeric);
                }
                catch (NumberFormatException e) {
                    log.error("Profile service did not return a numeric value as expected for metric ["
                            + request.getName() + "] - value returned was " + simpleValue + ".", e);
                }
                break;
            case TRAIT:
                MeasurementDataTrait dataTrait = new MeasurementDataTrait(request,
                        String.valueOf(simpleValue.getValue()));
                report.addData(dataTrait);
                break;
            default:
                throw new IllegalStateException("Unsupported measurement data type: " + dataType);
        }
    }

    private ManagedComponent getManagedComponent()
    {
        ManagedComponent managedComponent;
        try {
            //ProfileServiceFactory.refreshCurrentProfileView();
            ManagementView managementView = ProfileServiceFactory.getCurrentProfileView();
            managedComponent = ProfileServiceFactory.getManagedComponent(managementView, this.componentType,
                    this.componentName);
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load [" + this.componentType + "] ManagedComponent ["
                    + this.componentName + "].", e);
        }
        if (managedComponent == null)
            throw new IllegalStateException("Failed to find [" + this.componentType + "] ManagedComponent named ["
                    + this.componentName + "].");
        log.trace("Retrieved " + toString(managedComponent) + ".");
        return managedComponent;
    }

    @NotNull
    private OperationDefinition getOperationDefinition(String operationName) {
        ResourceType resourceType = getResourceContext().getResourceType();
        OperationDefinition operationDefinition = ResourceTypeUtils.getOperationDefinition(resourceType, operationName);
        if (operationDefinition == null)
            throw new IllegalStateException("Operation named '" + operationName + "' is not defined for Resource type '"
                    + resourceType.getName() + "' in the '" + resourceType.getPlugin() + "' plugin's descriptor.");
        return operationDefinition;
    }

    @NotNull
    private ManagedOperation getManagedOperation(OperationDefinition operationDefinition)
    {
        ManagedComponent managedComponent = getManagedComponent();
        Set<ManagedOperation> operations = managedComponent.getOperations();
        for (ManagedOperation operation : operations)
        {
            ConfigurationDefinition paramsConfigDef = operationDefinition.getParametersConfigurationDefinition();
            int paramCount = (paramsConfigDef != null) ? paramsConfigDef.getPropertyDefinitions().size() : 0;
            if (operation.getName().equals(operationDefinition.getName()) &&
               (operation.getParameters().length == paramCount))
                return operation;
        }
        throw new IllegalStateException("ManagedOperation named '" + operationDefinition.getName()
                + "' not found on ManagedComponent [" + getManagedComponent() + "].");
    }

    private static String toString(ManagedComponent managedComponent) {
        Map<String, ManagedProperty> properties = managedComponent.getProperties();
        return managedComponent.getClass().getSimpleName() + "@" + System.identityHashCode(managedComponent) + "["
                + "type=" + managedComponent.getType() + ", name=" + managedComponent.getName()
                + ", properties=" + properties.getClass().getSimpleName() + "@" + System.identityHashCode(properties)
                + "]";
    }
}
