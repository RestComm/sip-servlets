/*
 * RHQ Management Platform
 * Copyright (C) 2005-2009 Red Hat, Inc.
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
package org.rhq.plugins.jbossas5.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.EnumSet;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;
import org.rhq.core.domain.configuration.definition.PropertyDefinition;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionList;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionMap;
import org.rhq.core.domain.configuration.definition.PropertyDefinitionSimple;
import org.rhq.core.domain.configuration.definition.PropertySimpleType;
import org.rhq.core.domain.configuration.definition.constraint.FloatRangeConstraint;
import org.rhq.core.domain.configuration.definition.constraint.IntegerRangeConstraint;
import org.rhq.core.domain.measurement.DataType;
import org.rhq.core.domain.measurement.DisplayType;
import org.rhq.core.domain.measurement.MeasurementCategory;
import org.rhq.core.domain.measurement.MeasurementDefinition;
import org.rhq.core.domain.measurement.MeasurementUnits;
import org.rhq.core.domain.operation.OperationDefinition;
import org.rhq.core.domain.resource.ResourceCategory;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.domain.util.StringUtils;

import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedObject;
import org.jboss.managed.api.ManagedOperation;
import org.jboss.managed.api.ManagedParameter;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.managed.api.annotation.ViewUse;
import org.jboss.metatype.api.types.ArrayMetaType;
import org.jboss.metatype.api.types.CollectionMetaType;
import org.jboss.metatype.api.types.CompositeMetaType;
import org.jboss.metatype.api.types.MapCompositeMetaType;
import org.jboss.metatype.api.types.MetaType;
import org.jboss.metatype.api.types.PropertiesMetaType;
import org.jboss.metatype.api.types.TableMetaType;
import org.jboss.metatype.api.types.SimpleMetaType;
import org.jboss.metatype.api.values.ArrayValue;
import org.jboss.metatype.api.values.CollectionValue;
import org.jboss.metatype.api.values.GenericValue;
import org.jboss.metatype.api.values.MetaValue;

/**
 * Utility class for converting JBAS5 Profile Service {@link ManagedComponent}s and {@link ManagedDeployment}s to RHQ
 * {@link ResourceType}s.
 *
 * @author Ian Springer
 */
public class MetadataConversionUtils {
    private static final Log LOG = LogFactory.getLog(MetadataConversionUtils.class);

    private static final String PLUGIN_NAME = "ProfileService";

    private static Map<String, PropertySimpleType> TYPE_MAP = new HashMap();
    static {
        TYPE_MAP.put(boolean.class.getName(), PropertySimpleType.BOOLEAN);
        TYPE_MAP.put(Boolean.class.getName(), PropertySimpleType.BOOLEAN);
        TYPE_MAP.put(int.class.getName(), PropertySimpleType.INTEGER);
        TYPE_MAP.put(Integer.class.getName(), PropertySimpleType.INTEGER);
        TYPE_MAP.put(long.class.getName(), PropertySimpleType.LONG);
        TYPE_MAP.put(Long.class.getName(), PropertySimpleType.LONG);
        TYPE_MAP.put(float.class.getName(), PropertySimpleType.FLOAT);
        TYPE_MAP.put(Float.class.getName(), PropertySimpleType.FLOAT);
        TYPE_MAP.put(double.class.getName(), PropertySimpleType.DOUBLE);
        TYPE_MAP.put(Double.class.getName(), PropertySimpleType.DOUBLE);
    }

    public static ResourceType convertDeploymentToResourceType(String deploymentTypeName, ManagedDeployment deployment) {
        LOG.debug("Creating ResourceType for ManagedDeployment type [" + deploymentTypeName + "]...");
        ResourceType resourceType = new ResourceType(deploymentTypeName, PLUGIN_NAME, ResourceCategory.SERVICE, null);
        Set<MeasurementDefinition> metricDefs = convertMetricPropertiesToMeasurementDefinitions(deployment);
        resourceType.setMetricDefinitions(metricDefs);
        ConfigurationDefinition resourceConfigDef = convertConfigurationPropertiesToConfigurationDefinition(deployment);
        resourceType.setResourceConfigurationDefinition(resourceConfigDef);
        return resourceType;
    }

    public static ResourceType convertComponentToResourceType(ManagedComponent component)
    {
        LOG.debug("Creating ResourceType for ManagedComponent type [" + component.getType() + "]...");
        ComponentType componentType = component.getType();
        String name;
        if (componentType.getSubtype().equals("*")) 
            name = component.getName() + " " + componentType.getType();            
        else
            name = componentType.getSubtype() + " " + componentType.getType();
        ResourceType resourceType = new ResourceType(name, PLUGIN_NAME, ResourceCategory.SERVICE, null);
        Set<OperationDefinition> opDefs = convertManagedOperationsToOperationDefinitions(component);
        for (OperationDefinition opDef : opDefs)
            resourceType.addOperationDefinition(opDef);
        Set<MeasurementDefinition> metricDefs = convertMetricPropertiesToMeasurementDefinitions(component);
        resourceType.setMetricDefinitions(metricDefs);
        ConfigurationDefinition resourceConfigDef = convertConfigurationPropertiesToConfigurationDefinition(component);
        resourceType.setResourceConfigurationDefinition(resourceConfigDef);
        return resourceType;
    }

    private static Set<OperationDefinition> convertManagedOperationsToOperationDefinitions(ManagedComponent component) {
        Set<OperationDefinition> opDefs = new TreeSet(new OperationDefinitionComparator());
        Map<String,ManagedOperation> managedOperations = new HashMap();
        for (ManagedOperation operation : component.getOperations()) {
            ManagedOperation operationWithSameName = managedOperations.get(operation.getName());
            if (operationWithSameName == null ||
                    operationWithSameName.getParameters().length < operation.getParameters().length) {
                if (operationWithSameName != null)
                    LOG.info("Found more than one ManagedOperation named '" + operation.getName()
                            + "' - converting only the one with the most parameters.");
                managedOperations.put(operation.getName(), operation);
            }
        }
        for (ManagedOperation operation : managedOperations.values()) {
            OperationDefinition opDef = convertOperationToOperationDefinition(operation);
            opDefs.add(opDef);
        }
        return opDefs;
    }

    private static OperationDefinition convertOperationToOperationDefinition(ManagedOperation operation) {
        String desc = (!operation.getName().equals(operation.getDescription())) ? operation.getDescription() : null;
        OperationDefinition opDef = new OperationDefinition(operation.getName(), null, desc);
        opDef.setDisplayName(StringUtils.deCamelCase(operation.getName()));
        ConfigurationDefinition paramsConfigDef = convertParametersToConfigurationDefinition(operation);
        opDef.setParametersConfigurationDefinition(paramsConfigDef);
        ConfigurationDefinition resultsConfigDef = convertResultToConfigurationDefinition(operation);
        opDef.setResultsConfigurationDefinition(resultsConfigDef);
        return opDef;
    }

    private static ConfigurationDefinition convertParametersToConfigurationDefinition(ManagedOperation operation)
    {
        if (operation.getParameters() == null || operation.getParameters().length == 0)
            return null;
        ConfigurationDefinition configDef = new ConfigurationDefinition(operation.getName(), null);
        for (ManagedParameter parameter : operation.getParameters()) {
            PropertyDefinition propDef = convertParameterToPropertyDefinition(parameter);
            configDef.put(propDef);
        }
        return configDef;
    }

    private static PropertyDefinition convertParameterToPropertyDefinition(ManagedParameter parameter) {
        PropertyDefinition propDef = convertMetaTypeToPropertyDefinition(parameter.getMetaType(), parameter.getName(),
                parameter.getValue());
        String desc = (!parameter.getName().equals(parameter.getDescription())) ? parameter.getDescription() : null;
        propDef.setDescription(desc);
        // TODO: Convert parameter.getLegalValues() to enum defs.
        if (propDef instanceof PropertyDefinitionSimple) {
            PropertyDefinitionSimple propDefSimple = (PropertyDefinitionSimple)propDef;
            addConstraints(propDefSimple, parameter.getMinimumValue(), parameter.getMaximumValue());
        }
        return propDef;
    }

    private static PropertyDefinitionList convertMetaTypeToPropertyDefinitionList(MetaType metaType, String name,
                                                                                  MetaValue metaValue) {
        MetaType elementType;
        MetaValue elementValue = null;
        if (metaType.isCollection()) {
            CollectionMetaType collectionMetaType = (CollectionMetaType)metaType;
            elementType = collectionMetaType.getElementType();
            if (metaValue != null) {
                CollectionValue collectionValue = (CollectionValue)metaValue;
                MetaValue[] elements = collectionValue.getElements();
                if (elements != null && elements.length != 0)
                    elementValue = elements[0];
            }
        } else if (metaType.isArray()) {
            ArrayMetaType arrayMetaType = (ArrayMetaType)metaType;
            elementType = arrayMetaType.getElementType();
            if (metaValue != null) {
                ArrayValue arrayValue = (ArrayValue)metaValue;
                if (arrayValue.getLength() != 0 && arrayValue.getValue(0) instanceof MetaValue)
                    elementValue = (MetaValue)arrayValue.getValue(0);
            }
        } else {
            throw new IllegalStateException("Unsupported MetaType: " + metaType);
        }
        PropertyDefinition elementPropDef = convertMetaTypeToPropertyDefinition(elementType, "element", elementValue);
        @SuppressWarnings({"UnnecessaryLocalVariable"})
        PropertyDefinitionList propDefList = new PropertyDefinitionList(name, null, true, elementPropDef);
        return propDefList;
    }

    private static PropertyDefinitionMap convertMetaTypeToPropertyDefinitionMap(MetaType metaType, String name,
                                                                                MetaValue metaValue) {
        PropertyDefinitionMap propDefMap = new PropertyDefinitionMap(name, null, false);
        if (metaType.isComposite()) {
            if (!(metaType instanceof MapCompositeMetaType)) {
                CompositeMetaType compositeMetaType = (CompositeMetaType)metaType;
                for (String itemName : compositeMetaType.itemSet()) {
                    MetaType itemMetaType = compositeMetaType.getType(itemName);
                    if (itemMetaType.isComposite()) {
                        // Avoid StackOverflowErrors caused by recursive CompositeMetaType metadata.
                        if (itemMetaType == compositeMetaType)
                            LOG.error("CompositeMetaType " + compositeMetaType
                                    + " contains an item whose type is a reference to the CompositeMetaType itself!");
                        else
                            LOG.error("CompositeMetaType " + compositeMetaType
                                    + " contains an item whose type is another CompositeMetaType: " + itemMetaType);
                        continue;
                    }
                    LOG.trace("Converting item with type [" + itemMetaType + "@" + System.identityHashCode(itemMetaType)
                            + "] and name [" + itemName + "]...");
                    PropertyDefinition itemPropDef = convertMetaTypeToPropertyDefinition(itemMetaType, itemName, null);
                    propDefMap.put(itemPropDef);
                    String desc = (!itemName.equals(compositeMetaType.getDescription(itemName))) ?
                            compositeMetaType.getDescription(itemName) : null;
                    itemPropDef.setDescription(desc);
                }
            }
        } else if (metaType.isGeneric()) {
            if (metaValue != null) {
                GenericValue genericValue = (GenericValue)metaValue;
                Serializable value = genericValue.getValue();
                if (value != null && value instanceof ManagedObject) {
                    ManagedObject managedObject = (ManagedObject)value;
                    for (ManagedProperty managedProp : managedObject.getProperties().values()) {
                        PropertyDefinition itemPropDef = convertManagedPropertyToPropertyDefinition(managedProp);
                        propDefMap.put(itemPropDef);
                    }
                }
            }
        } else if (metaType.isTable()) {
            TableMetaType tableMetaType = (TableMetaType)metaType;
            CompositeMetaType itemMetaType = tableMetaType.getRowType();
            for (String itemName : tableMetaType.getIndexNames()) {
                PropertyDefinition itemPropDef = convertMetaTypeToPropertyDefinition(itemMetaType, itemName, null);
                propDefMap.put(itemPropDef);
            }
        } else if (metaType instanceof PropertiesMetaType) {
            @SuppressWarnings({"UnusedDeclaration"})
            PropertiesMetaType propertiesMetaType = (PropertiesMetaType)metaType;
        }
        return propDefMap;
    }

    private static ConfigurationDefinition convertResultToConfigurationDefinition(ManagedOperation operation)
    {
        MetaType returnType = operation.getReturnType();
        if (returnType.getClassName().equals(Void.class.getName()))
            return null;        
        PropertyDefinition propDef = convertMetaTypeToPropertyDefinition(returnType, "result", null);
        ConfigurationDefinition configDef = new ConfigurationDefinition(operation.getName(), operation.getDescription());
        configDef.put(propDef);
        return configDef;
    }

    private static Set<MeasurementDefinition> convertMetricPropertiesToMeasurementDefinitions(ManagedDeployment deployment)
    {
       return convertMetricPropertiesToMeasurementDefinitions(deployment.getProperties());
    }

    private static Set<MeasurementDefinition> convertMetricPropertiesToMeasurementDefinitions(ManagedComponent component)
    {
       return convertMetricPropertiesToMeasurementDefinitions(component.getProperties());
    }

    private static Set<MeasurementDefinition> convertMetricPropertiesToMeasurementDefinitions(Map<String, ManagedProperty> props) {
        Set<MeasurementDefinition> measurementDefs = new TreeSet(new MeasurementDefinitionComparator());
        if (props == null)
            return measurementDefs;
        for(ManagedProperty prop : props.values()) {            
            if (prop.hasViewUse(ViewUse.RUNTIME) || prop.hasViewUse(ViewUse.STATISTIC)) {
                MetaType metaType = prop.getMetaType();
                if (metaType.isSimple() || metaType.isEnum())
                {
                    MeasurementDefinition measurementDef = getMeasurementDefinitionForSimpleManagedProperty(prop);
                    measurementDefs.add(measurementDef);
                }
                else if (prop.getMetaType().isComposite())
                {
                    Set<MeasurementDefinition> compositeMeasurementDefs =
                            getMeasurementDefinitionsForCompositeManagedProperty(prop);
                    measurementDefs.addAll(compositeMeasurementDefs);
                }
                else
                {
                    LOG.warn("Skipping property [" + prop + "] with unsupported type [" + metaType + "].");
                }
            }
        }
        return measurementDefs;
    }

    private static MeasurementDefinition getMeasurementDefinitionForSimpleManagedProperty(ManagedProperty prop)
    {
        MetaType metaType = prop.getMetaType();
        DataType dataType;
        if (metaType.isSimple()) {
            SimpleMetaType simpleMetaType = (SimpleMetaType)metaType;
            dataType = (prop.hasViewUse(ViewUse.STATISTIC) && MetaTypeUtils.isNumeric(simpleMetaType)) ?
                    DataType.MEASUREMENT : DataType.TRAIT;
        } else { // metaType.isEnum()
            // Enum values are always Strings.
            dataType = DataType.TRAIT;
        }        
        int defaultInterval = (dataType == DataType.MEASUREMENT) ? 60000 : 600000;
        DisplayType displayType = (dataType == DataType.MEASUREMENT) ? DisplayType.DETAIL : DisplayType.SUMMARY;
        MeasurementDefinition measurementDef = new MeasurementDefinition(prop.getName(),
                MeasurementCategory.PERFORMANCE, MeasurementUnits.NONE, dataType, true, defaultInterval, displayType);
        measurementDef.setDisplayName(StringUtils.deCamelCase(prop.getName()));
        String desc = (!prop.getName().equals(prop.getDescription())) ? prop.getDescription() : null;
        measurementDef.setDescription(desc);
        return measurementDef;
    }

    private static Set<MeasurementDefinition> getMeasurementDefinitionsForCompositeManagedProperty(ManagedProperty property) {
        Set<MeasurementDefinition> measurementDefs = new HashSet();
        CompositeMetaType compositeMetaType = (CompositeMetaType)property.getMetaType();
        Set<String> itemNames = compositeMetaType.keySet();
        for (String itemName : itemNames) {
            MetaType itemType = compositeMetaType.getType(itemName);
            if (itemType.isSimple() || itemType.isEnum()) {
                String metricName = property.getName() + "." + itemName;
                DataType dataType = (itemType.getClassName().equals(String.class.getName())) ?
                        DataType.TRAIT : DataType.MEASUREMENT;
                MeasurementDefinition measurementDef = new MeasurementDefinition(metricName,
                    MeasurementCategory.PERFORMANCE, MeasurementUnits.NONE, dataType, true, 60000,
                    DisplayType.DETAIL);
                measurementDefs.add(measurementDef);
                measurementDef.setDisplayName(StringUtils.deCamelCase(itemName));
            } else {
                LOG.warn("Composite stat property [" + property.getName() + "] contains non-simple item with type ["
                        + itemType + "] - skipping..." );
            }
         }
        return measurementDefs;
    }

    private static ConfigurationDefinition convertConfigurationPropertiesToConfigurationDefinition(String configName, Map<String, ManagedProperty> managedProps)
    {
        Set<PropertyDefinition> propDefs = new TreeSet(new PropertyDefinitionComparator());
        for(ManagedProperty prop : managedProps.values()) {
            EnumSet<ViewUse> viewUses = ManagedComponentUtils.getViewUses(prop);
            // Assume a property with no view uses is a config prop.
            if (viewUses.contains(ViewUse.CONFIGURATION) || viewUses.contains(ViewUse.RUNTIME) || viewUses.isEmpty()) {
                PropertyDefinition propDef = convertManagedPropertyToPropertyDefinition(prop);
                propDefs.add(propDef);
            }
        }
        if (propDefs.isEmpty())
            return null;
        ConfigurationDefinition configDef = new ConfigurationDefinition(configName, null);
        for (PropertyDefinition propDef : propDefs)
           configDef.getPropertyDefinitions().put(propDef.getName(), propDef);
        return configDef;
    }

    private static PropertyDefinition convertManagedPropertyToPropertyDefinition(ManagedProperty prop) {
        PropertyDefinition propDef;
        propDef = convertMetaTypeToPropertyDefinition(prop.getMetaType(), prop.getName(), prop.getValue());
        String desc = (!prop.getName().equals(prop.getDescription())) ? prop.getDescription() : null;
        propDef.setDescription(desc);
        propDef.setRequired(prop.isMandatory());
        propDef.setReadOnly(prop.isReadOnly());
        // TODO: Convert prop.getLegalValues() to enum defs.
        if (propDef instanceof PropertyDefinitionSimple) {
            PropertyDefinitionSimple propDefSimple = (PropertyDefinitionSimple)propDef;
            addConstraints(propDefSimple, prop.getMinimumValue(), prop.getMaximumValue());
        }
        return propDef;
    }

    private static PropertyDefinition convertMetaTypeToPropertyDefinition(MetaType metaType, String propName,
                                                                          MetaValue metaValue) {
        PropertyDefinition propDef;
        if (metaType.isSimple() || metaType.isEnum()) {
            PropertySimpleType propType = convertClassToPropertySimpleType(metaType.getClassName());
            propDef = new PropertyDefinitionSimple(propName, null, false, propType);
        } else if (metaType.isCollection() || metaType.isArray()) {
            propDef = convertMetaTypeToPropertyDefinitionList(metaType, propName, metaValue);
        } else if (metaType.isComposite() || metaType.isGeneric() || metaType.isTable() ||
                   metaType instanceof PropertiesMetaType) {
            LOG.trace("Converting map with type [" + metaType + "@" + System.identityHashCode(metaType) + "], name ["
                    + propName + "], and value [" + metaValue + "]...");
            propDef = convertMetaTypeToPropertyDefinitionMap(metaType, propName, metaValue);
        } else {
            throw new IllegalStateException("Unsupported MetaType: " + metaType);
        }
        propDef.setDisplayName(StringUtils.deCamelCase(propName));
        return propDef;
    }

    private static ConfigurationDefinition convertConfigurationPropertiesToConfigurationDefinition(ManagedDeployment deployment)
    {
        return convertConfigurationPropertiesToConfigurationDefinition(deployment.getName() + " resource config",
                deployment.getProperties());
    }

    private static ConfigurationDefinition convertConfigurationPropertiesToConfigurationDefinition(ManagedComponent component)
    {
        return convertConfigurationPropertiesToConfigurationDefinition(component.getName() + " resource config",
                component.getProperties());
    }

    private static PropertySimpleType convertClassToPropertySimpleType(String className) {
        PropertySimpleType simpleType = TYPE_MAP.get(className);
        return (simpleType != null) ? simpleType : PropertySimpleType.STRING;
    }

    private static void addConstraints(PropertyDefinitionSimple propDefSimple, Comparable<?> minValue,
                                       Comparable<?> maxValue) {
        if (minValue != null || maxValue != null) {
            if (propDefSimple.getType() == PropertySimpleType.INTEGER ||
                    propDefSimple.getType() == PropertySimpleType.LONG) {
                Long min = (minValue != null) ? ((Number)minValue).longValue() : null;
                Long max = (maxValue != null) ? ((Number)maxValue).longValue() : null;
                IntegerRangeConstraint constraint = new IntegerRangeConstraint(min, max);
                propDefSimple.addConstraints(constraint);
            } else if (propDefSimple.getType() == PropertySimpleType.FLOAT ||
                    propDefSimple.getType() == PropertySimpleType.DOUBLE) {
                Double min = (minValue != null) ? ((Number)minValue).doubleValue() : null;
                Double max = (maxValue != null) ? ((Number)maxValue).doubleValue() : null;
                FloatRangeConstraint constraint = new FloatRangeConstraint(min, max);
                propDefSimple.addConstraints(constraint);
            }
        }
    }

    private static class OperationDefinitionComparator implements Comparator<OperationDefinition> {
        public int compare(OperationDefinition opDef1, OperationDefinition opDef2) {
            return opDef1.getName().compareTo(opDef2.getName());
        }
    }

    private static class MeasurementDefinitionComparator implements Comparator<MeasurementDefinition> {
        public int compare(MeasurementDefinition metricDef1, MeasurementDefinition metricDef2) {
            return metricDef1.getName().compareTo(metricDef2.getName());
        }
    }

    private static class PropertyDefinitionComparator implements Comparator<PropertyDefinition> {
        public int compare(PropertyDefinition propDef1, PropertyDefinition propDef2) {
            return propDef1.getName().compareTo(propDef2.getName());
        }
    }
}
