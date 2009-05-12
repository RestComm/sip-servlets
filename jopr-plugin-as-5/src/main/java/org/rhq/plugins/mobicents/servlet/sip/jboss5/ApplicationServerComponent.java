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

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.deployers.spi.management.KnownDeploymentTypes;
import org.jboss.deployers.spi.management.ManagementView;
import org.jboss.deployers.spi.management.deploy.DeploymentStatus;
import org.jboss.deployers.spi.management.deploy.ProgressEvent;
import org.jboss.deployers.spi.management.deploy.ProgressListener;
import org.jboss.managed.api.ComponentType;
import org.jboss.managed.api.DeploymentTemplateInfo;
import org.jboss.managed.api.ManagedComponent;
import org.jboss.managed.api.ManagedDeployment;
import org.jboss.managed.api.ManagedProperty;
import org.jboss.profileservice.spi.NoSuchDeploymentException;
import org.mc4j.ems.connection.ConnectionFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.settings.ConnectionSettings;
import org.mc4j.ems.connection.support.ConnectionProvider;
import org.mc4j.ems.connection.support.metadata.ConnectionTypeDescriptor;
import org.mc4j.ems.connection.support.metadata.InternalVMTypeDescriptor;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.Property;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.configuration.definition.ConfigurationDefinition;
import org.rhq.core.domain.configuration.definition.ConfigurationTemplate;
import org.rhq.core.domain.configuration.definition.PropertyDefinition;
import org.rhq.core.domain.content.PackageDetailsKey;
import org.rhq.core.domain.content.transfer.ResourcePackageDetails;
import org.rhq.core.domain.measurement.AvailabilityType;
import org.rhq.core.domain.measurement.DataType;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.domain.resource.CreateResourceStatus;
import org.rhq.core.domain.resource.ResourceCreationDataType;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.pluginapi.configuration.ConfigurationFacet;
import org.rhq.core.pluginapi.configuration.ConfigurationUpdateReport;
import org.rhq.core.pluginapi.inventory.CreateChildResourceFacet;
import org.rhq.core.pluginapi.inventory.CreateResourceReport;
import org.rhq.core.pluginapi.inventory.ResourceComponent;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.plugins.jbossas5.adapter.api.PropertyAdapter;
import org.rhq.plugins.jbossas5.adapter.api.PropertyAdapterFactory;
import org.rhq.plugins.jbossas5.factory.ProfileServiceFactory;
import org.rhq.plugins.jbossas5.util.ConversionUtils;
import org.rhq.plugins.jbossas5.util.DebugUtils;
import org.rhq.plugins.jbossas5.util.ManagedComponentUtils;
import org.rhq.plugins.jbossas5.util.ResourceComponentUtils;
import org.rhq.plugins.jmx.JMXDiscoveryComponent;
import org.rhq.plugins.jmx.JMXServerComponent;
import org.rhq.plugins.jmx.ObjectNameQueryUtility;
import org.rhq.plugins.mobicents.servlet.sip.jboss5.util.DeploymentUtils;
import org.rhq.plugins.mobicents.servlet.sip.jboss5.util.MainDeployer;

 /**
 * ResourceComponent for a JBoss AS, 5.1.0.CR1 or later, Server.
 *
 * @author Jason Dobies
 * @author Mark Spritzler
 * @author Ian Springer
 */
public class ApplicationServerComponent		
		extends JMXServerComponent
        implements ResourceComponent, CreateChildResourceFacet, MeasurementFacet, ConfigurationFacet, ProgressListener
{    
    static final String TEMPLATE_NAME_PROPERTY = "templateName";
    static final String RESOURCE_NAME_PROPERTY = "resourceName";
    static final String SERVER_NAME_PROPERTY = "serverName";
    public static final String JBOSS_HOME_DIR_CONFIG_PROP = "jbossHomeDir";
    public static final String NAMING_URL_CONFIG_PROP = "namingURL";
    private static final String JNP_DISABLE_DISCOVERY_JNP_INIT_PROP = "jnp.disableDiscovery";
    /**
     * This is the timeout for the initial connection to the MBeanServer that is made by {@link #start(ResourceContext)}.
     */
    private static final int JNP_TIMEOUT = 30 * 1000; // 30 seconds
    /**
     * This is the timeout for MBean attribute gets/sets and operations invoked on the remote MBeanServer.
     * NOTE: This timeout comes into play if the JBossAS instance has gone down since the original JNP connection was made.
     */
    private static final int JNP_SO_TIMEOUT = 15 * 1000; // 15 seconds

    private static final String MANAGED_PROPERTY_GROUP = "managedPropertyGroup";

    private static final Pattern METRIC_NAME_PATTERN = Pattern.compile("(.*)\\|(.*)\\|(.*)\\|(.*)");

    private final Log log = LogFactory.getLog(this.getClass());

    private ResourceContext resourceContext;
    private File deployDirectory;
    private EmsConnection connection;

    /**
     * Controls the dampening of connection error stack traces in an attempt to control spam to the log
     * file. Each time a connection error is encountered, this will be incremented. When the connection
     * is finally established, this will be reset to zero.
     */
    private int consecutiveConnectionErrors;
	private MainDeployer mainDeployer;


    public AvailabilityType getAvailability()
    {
        // TODO: Always returning UP is fine for Embedded, but we'll need to actually check avail for Enterprise.
        return AvailabilityType.UP;
    }

    public void start(ResourceContext resourceContext)
    {
        this.resourceContext = resourceContext;
    }

    public void stop()
    {
        return;
    }

    // ------------ MeasurementFacet Implementation ------------

    public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests)
    {
        for (MeasurementScheduleRequest request : requests) {
            String metricName = request.getName();
            try
            {
                // Metric names are expected to have the following syntax:
                // "<componentType>|<componentSubType>|<componentName>|<propertyName>"
                Matcher matcher = METRIC_NAME_PATTERN.matcher(metricName);
                if (!matcher.matches()) {
                    log.error("Metric name '" + metricName + "' does not match pattern '" + METRIC_NAME_PATTERN + "'.");
                    continue;
                }
                String componentCategory = matcher.group(1);
                String componentSubType = matcher.group(2);
                String componentName = matcher.group(3);
                String propertyName = matcher.group(4);
                ComponentType componentType = new ComponentType(componentCategory, componentSubType);
                ManagedComponent component;
                if (componentName.equals("*")) {
                    component = ManagedComponentUtils.getSingletonManagedComponent(componentType);
                } else {
                    component = ManagedComponentUtils.getManagedComponent(componentType, componentName);
                }
                Serializable value = ManagedComponentUtils.getSimplePropertyValue(component, propertyName);
                if (value == null) {
                    log.debug("Null value returned for metric '" + metricName + "'.");
                    continue;
                }
                if (request.getDataType() == DataType.MEASUREMENT) {
                    Number number = (Number)value;
                    report.addData(new MeasurementDataNumeric(request, number.doubleValue()));
                } else if (request.getDataType() == DataType.TRAIT) {
                    report.addData(new MeasurementDataTrait(request, value.toString()));
                }
            }
            catch (RuntimeException e)
            {
                log.error("Failed to obtain metric '" + metricName + "'.", e);
            }
        }
    }

    // ------------ ConfigurationFacet Implementation ------------

    public Configuration loadResourceConfiguration()
    {
        /* Need to determine what we consider server configuration to return. Also need to understand
           what ComponentType the profile service would use to retrieve "server" level configuration.
        */

        return null;
    }

    public void updateResourceConfiguration(ConfigurationUpdateReport configurationUpdateReport)
    {
        // See above comment on server configuration.
    }

    // CreateChildResourceFacet  --------------------------------------------

    public CreateResourceReport createResource(CreateResourceReport createResourceReport)
    {
        //ProfileServiceFactory.refreshCurrentProfileView();
        ResourceType resourceType = createResourceReport.getResourceType();
        if (resourceType.getCreationDataType() == ResourceCreationDataType.CONTENT)
            createContentBasedResource(createResourceReport, resourceType);
        else
            createConfigurationBasedResource(createResourceReport, resourceType);
        return createResourceReport;
    }

    public File getDeployDirectory()
    {
        if (this.deployDirectory == null)
            this.deployDirectory = computeDeployDirectory();
        return this.deployDirectory;
    }

    // ProgressListener  --------------------------------------------

    public void progressEvent(ProgressEvent eventInfo) {
        log.debug(eventInfo);
    }

    private void handleMiscManagedProperties(Collection<PropertyDefinition> managedPropertyGroup,
                                             Map<String, ManagedProperty> managedProperties,
                                             Configuration pluginConfiguration)
    {
        for (PropertyDefinition propertyDefinition : managedPropertyGroup)
        {
            String propertyKey = propertyDefinition.getName();
            Property property = pluginConfiguration.get(propertyKey);
            ManagedProperty managedProperty = managedProperties.get(propertyKey);
            if (managedProperty != null && property != null)
            {
                PropertyAdapter propertyAdapter = PropertyAdapterFactory.getPropertyAdapter(managedProperty.getMetaType());
                propertyAdapter.populateMetaValueFromProperty(property, managedProperty.getValue(), propertyDefinition);
            }
        }
    }

    private static String getResourceName(Configuration pluginConfig, Configuration resourceConfig)
    {
        PropertySimple resourceNameProp = pluginConfig.getSimple(RESOURCE_NAME_PROPERTY);
        if (resourceNameProp == null || resourceNameProp.getStringValue() == null)
            throw new IllegalStateException("Property [" + RESOURCE_NAME_PROPERTY
                    + "] is not defined in the default plugin configuration.");
        String resourceNamePropName = resourceNameProp.getStringValue();
        PropertySimple propToUseAsResourceName = resourceConfig.getSimple(resourceNamePropName);
        if (propToUseAsResourceName == null)
            throw new IllegalStateException("Property [" + resourceNamePropName
                    + "] is not defined in initial Resource configuration.");
        return propToUseAsResourceName.getStringValue();
    }

    private String getResourceKey(ResourceType resourceType, String resourceName)
    {
        ComponentType componentType = ConversionUtils.getComponentType(resourceType);
        if (componentType == null)
            throw new IllegalStateException("Unable to map " + resourceType + " to a ComponentType.");
        // TODO (ips): I think the key can just be the resource name.
        return componentType.getType() + ":" + componentType.getSubtype() + ":" + resourceName;
    }

    private void createConfigurationBasedResource(CreateResourceReport createResourceReport, ResourceType resourceType)
    {
        Configuration defaultPluginConfig = getDefaultPluginConfiguration(resourceType);
        Configuration resourceConfig = createResourceReport.getResourceConfiguration();
        String resourceName = getResourceName(defaultPluginConfig, resourceConfig);
        ComponentType componentType = ConversionUtils.getComponentType(resourceType);
        if (ProfileServiceFactory.isManagedComponent(resourceName, componentType)) {
            createResourceReport.setStatus(CreateResourceStatus.FAILURE);
            createResourceReport.setErrorMessage("A " + resourceType.getName() + " named '" + resourceName
                    + "' already exists.");
            return;
        }

        createResourceReport.setResourceName(resourceName);
        String resourceKey = getResourceKey(resourceType, resourceName);
        createResourceReport.setResourceKey(resourceKey);

        PropertySimple templateNameProperty = defaultPluginConfig.getSimple(TEMPLATE_NAME_PROPERTY);
        String templateName = templateNameProperty.getStringValue();

        ManagementView managementView = ProfileServiceFactory.getCurrentProfileView();
        DeploymentTemplateInfo template;
        try
        {
            template = managementView.getTemplate(templateName);
            Map<String, ManagedProperty> managedProperties = template.getProperties();
            Map<String, PropertySimple> customProps = ResourceComponentUtils.getCustomProperties(defaultPluginConfig);

            if (log.isDebugEnabled()) log.debug("BEFORE CREATE:\n" + DebugUtils.convertPropertiesToString(template));
            ConversionUtils.convertConfigurationToManagedProperties(managedProperties, resourceConfig, resourceType, customProps);
            if (log.isDebugEnabled()) log.debug("AFTER CREATE:\n" + DebugUtils.convertPropertiesToString(template));

            ConfigurationDefinition pluginConfigDef = resourceType.getPluginConfigurationDefinition();
            Collection<PropertyDefinition> managedPropertyGroup = pluginConfigDef.getPropertiesInGroup(MANAGED_PROPERTY_GROUP);
            handleMiscManagedProperties(managedPropertyGroup, managedProperties, defaultPluginConfig);
            log.debug("Applying template [" + templateName + "] to create ManagedComponent of type [" + componentType
                    + "]...");
            try
            {
                managementView.applyTemplate(resourceName, template);
                managementView.process();
                createResourceReport.setStatus(CreateResourceStatus.SUCCESS);
            }
            catch (Exception e)
            {
                log.error("Unable to apply template [" + templateName + "] to create ManagedComponent of type "
                        + componentType + ".", e);
                createResourceReport.setStatus(CreateResourceStatus.FAILURE);
                createResourceReport.setException(e);
            }
        }
        catch (NoSuchDeploymentException e)
        {
            log.error("Unable to find template [" + templateName + "].", e);
            createResourceReport.setStatus(CreateResourceStatus.FAILURE);
            createResourceReport.setException(e);
        }
        catch (Exception e)
        {
            log.error("Unable to process create request", e);
            createResourceReport.setStatus(CreateResourceStatus.FAILURE);
            createResourceReport.setException(e);
        }
    }

    private void createContentBasedResource(CreateResourceReport createResourceReport, ResourceType resourceType)
    {
        ResourcePackageDetails details = createResourceReport.getPackageDetails();
        PackageDetailsKey key = details.getKey();
        // This is the full path to a temporary file which was written by the UI layer.
        String archivePath = key.getName();

        try {
            File archiveFile = new File(archivePath);

            if (!DeploymentUtils.hasCorrectExtension(archiveFile, resourceType)) {
                createResourceReport.setStatus(CreateResourceStatus.FAILURE);
                createResourceReport.setErrorMessage("Incorrect extension specified on filename [" + archivePath + "]");
                return;
            }

            abortIfApplicationAlreadyDeployed(resourceType, archiveFile);

            Configuration deployTimeConfig = details.getDeploymentTimeConfiguration();
            @SuppressWarnings({"ConstantConditions"})
            boolean deployExploded = deployTimeConfig.getSimple("deployExploded").getBooleanValue();

            DeploymentStatus status = DeploymentUtils.deployArchive(archiveFile, getDeployDirectory(), deployExploded);

            if (status.getState() == DeploymentStatus.StateType.COMPLETED) {
                createResourceReport.setResourceName(archivePath);
                createResourceReport.setResourceKey(archivePath);
                createResourceReport.setStatus(CreateResourceStatus.SUCCESS);
            } else {
                createResourceReport.setStatus(CreateResourceStatus.FAILURE);
                createResourceReport.setErrorMessage(status.getMessage());
                //noinspection ThrowableResultOfMethodCallIgnored
                createResourceReport.setException(status.getFailure());
            }
        } catch (Throwable t) {
            log.error("Error deploying application for report: " + createResourceReport, t);
            createResourceReport.setStatus(CreateResourceStatus.FAILURE);
            createResourceReport.setException(t);            
        }
    }

    private static Configuration getDefaultPluginConfiguration(ResourceType resourceType) {
        ConfigurationTemplate pluginConfigDefaultTemplate =
                resourceType.getPluginConfigurationDefinition().getDefaultTemplate();
        return (pluginConfigDefaultTemplate != null) ?
                pluginConfigDefaultTemplate.createConfiguration() : new Configuration();
    }
    
    private void abortIfApplicationAlreadyDeployed(ResourceType resourceType, File archiveFile)
            throws Exception
    {        
        String archiveFileName = archiveFile.getName();
        KnownDeploymentTypes deploymentType = ConversionUtils.getDeploymentType(resourceType);
        String deploymentTypeString = deploymentType.getType();
        ProfileServiceFactory.refreshCurrentProfileView();
        ManagementView managementView = ProfileServiceFactory.getCurrentProfileView();
        Set<ManagedDeployment> managedDeployments = managementView.getDeploymentsForType(deploymentTypeString);
        for (ManagedDeployment managedDeployment : managedDeployments)
        {
            if (managedDeployment.getSimpleName().equals(archiveFileName))
                throw new IllegalArgumentException("An application named '" + archiveFileName + "' is already deployed.");
        }
    }

    private File computeDeployDirectory()
    {
        ManagementView managementView = ProfileServiceFactory.getCurrentProfileView();
        Set<ManagedDeployment> warDeployments;
        try
        {
            warDeployments = managementView.getDeploymentsForType(
                KnownDeploymentTypes.JavaEEWebApplication.getType());
        }
        catch (Exception e)
        {
            throw new IllegalStateException(e);
        }
        ManagedDeployment standaloneWarDeployment = null;
        for (ManagedDeployment warDeployment : warDeployments)
        {
            if (warDeployment.getParent() == null) {
                standaloneWarDeployment = warDeployment;
                break;
            }
        }
        if (standaloneWarDeployment == null)
            // This could happen if no standalone WARs, including the admin console WAR, have been fully deployed yet.
            return null;
        log.debug("Standalone WAR deployment: " + standaloneWarDeployment.getName());
        URL warUrl;
        try
        {
            warUrl = new URL(standaloneWarDeployment.getName());
        }
        catch (MalformedURLException e)
        {
            throw new IllegalStateException(e);
        }
        File warFile = new File(warUrl.getPath());
        File deployDir = warFile.getParentFile();
        log.debug(">>>>> Deploy directory: " + deployDir);
        return deployDir;
    }
    @Override
    public EmsConnection getEmsConnection() {
        EmsConnection emsConnection = null;

        try {
            emsConnection = loadConnection();
        } catch (Exception e) {
            log.error("Component attempting to access a connection that could not be loaded");
        }

        return emsConnection;
    }

    
    /**
     * This is the preferred way to use a connection from within this class; methods should not access the connection
     * property directly as it may not have been instantiated if the connection could not be made.
     *
     * <p>If the connection has already been established, return the object reference to it. If not, attempt to make
     * a live connection to the JMX server.</p>
     *
     * <p>If the connection could not be made in the {@link #start(org.rhq.core.pluginapi.inventory.ResourceContext)}
     * method, this method will effectively try to load the connection on each attempt to use it. As such, multiple
     * threads may attempt to access the connection through this means at a time. Therefore, the method has been
     * made synchronized on instances of the class.</p>
     *
     * <p>If any errors are encountered, this method will log the error, taking into account logic to prevent spamming
     * the log file. Calling methods should take care to not redundantly log the exception thrown by this method.</p>
     *
     * @return live connection to the JMX server; this will not be <code>null</code>
     *
     * @throws Exception if there are any issues at all connecting to the server
     */
    private synchronized EmsConnection loadConnection() throws Exception {
        if (this.connection == null) {
            try {
                Configuration pluginConfig = resourceContext.getPluginConfiguration();
                String jbossHomeDir = pluginConfig.getSimpleValue(JBOSS_HOME_DIR_CONFIG_PROP, null);

                ConnectionSettings connectionSettings = new ConnectionSettings();

                String connectionTypeDescriptorClass = pluginConfig.getSimple(JMXDiscoveryComponent.CONNECTION_TYPE)
                    .getStringValue();
                connectionSettings.initializeConnectionType((ConnectionTypeDescriptor) Class.forName(
                    connectionTypeDescriptorClass).newInstance());
                connectionSettings.setServerUrl(pluginConfig.getSimpleValue(NAMING_URL_CONFIG_PROP, null));
                connectionSettings.setPrincipal(pluginConfig.getSimpleValue(PRINCIPAL_CONFIG_PROP, null));
                connectionSettings.setCredentials(pluginConfig.getSimpleValue(CREDENTIALS_CONFIG_PROP, null));
                connectionSettings.setLibraryURI(jbossHomeDir);

                ConnectionFactory connectionFactory = new ConnectionFactory();
                connectionFactory.discoverServerClasses(connectionSettings);

                if (connectionSettings.getAdvancedProperties() == null) {
                    connectionSettings.setAdvancedProperties(new Properties());
                }

                connectionSettings.getAdvancedProperties().setProperty(JNP_DISABLE_DISCOVERY_JNP_INIT_PROP, "true");

                // Make sure the timeout always happens, even if the JBoss server is hung.
                connectionSettings.getAdvancedProperties().setProperty("jnp.timeout", String.valueOf(JNP_TIMEOUT));
                connectionSettings.getAdvancedProperties().setProperty("jnp.sotimeout", String.valueOf(JNP_SO_TIMEOUT));

                // Tell EMS to make copies of jar files so that the ems classloader doesn't lock
                // application files (making us unable to update them)  Bug: JBNADM-670
                // TODO GH: turn this off in the embedded case
                connectionSettings.getControlProperties().setProperty(ConnectionFactory.COPY_JARS_TO_TEMP,
                    String.valueOf(Boolean.TRUE));

                // But tell it to put them in a place that we clean up when shutting down the agent
                connectionSettings.getControlProperties().setProperty(ConnectionFactory.JAR_TEMP_DIR,
                    resourceContext.getTemporaryDirectory().getAbsolutePath());

                connectionSettings.getAdvancedProperties().setProperty(InternalVMTypeDescriptor.DEFAULT_DOMAIN_SEARCH,
                    "jboss");

                log.info("Loading JBoss connection [" + connectionSettings.getServerUrl() + "] with install path ["
                    + connectionSettings.getLibraryURI() + "]...");

                ConnectionProvider connectionProvider = connectionFactory.getConnectionProvider(connectionSettings);
                this.connection = connectionProvider.connect();

                this.connection.loadSynchronous(false); // this loads all the MBeans

                this.consecutiveConnectionErrors = 0;

                try {
                    this.mainDeployer = new MainDeployer(this.connection);
                } catch (Exception e) {
                    log.error("Unable to access MainDeployer MBean required for creation and deletion of managed "
                        + "resources - this should never happen. Cause: " + e);
                }

                if (log.isDebugEnabled())
                    log.debug("Successfully made connection to the AS instance for resource ["
                        + this.resourceContext.getResourceKey() + "]");
            } catch (Exception e) {

                // The connection will be established even in the case that the principal cannot be authenticated,
                // but the connection will not work. That failure seems to come from the call to loadSynchronous after
                // the connection is established. If we get to this point that an exception was thrown, close any
                // connection that was made and null it out so we can try to establish it again.
                if (connection != null) {
                    if (log.isDebugEnabled())
                        log.debug("Connection created but an exception was thrown. Closing the connection.", e);
                    connection.close();
                    connection = null;
                }

                // Since the connection is attempted each time it's used, failure to connect could result in log
                // file spamming. Log it once for every 10 consecutive times it's encountered.
                if (consecutiveConnectionErrors % 10 == 0) {
                    log.warn("Could not establish connection to the JBoss AS instance ["
                        + (consecutiveConnectionErrors + 1) + "] times for resource ["
                        + resourceContext.getResourceKey() + "]", e);
                }

                if (log.isDebugEnabled())
                    log.debug("Could not connect to the JBoss AS instance for resource ["
                        + resourceContext.getResourceKey() + "]", e);

                consecutiveConnectionErrors++;

                throw e;
            }
        }

        return connection;
    }

    public List<EmsBean> getWebApplicationEmsBeans(String applicationName) {    			
    	
		String pattern = "jboss.web:host=%host%,path=" + getContextPath(applicationName) + ",type=Manager";		
	    ObjectNameQueryUtility queryUtil = new ObjectNameQueryUtility(pattern);
	    return getEmsConnection().queryBeans(queryUtil.getTranslatedQuery());        	    
    }
    
    public List<EmsBean> getConvergedSipApplicationEmsBeans(String applicationName) {    			
    	
		String pattern = "jboss.web:host=%host%,path=" + getContextPath(applicationName) + ",type=SipManager";		
	    ObjectNameQueryUtility queryUtil = new ObjectNameQueryUtility(pattern);
	    return getEmsConnection().queryBeans(queryUtil.getTranslatedQuery());        	    
    }
    
    public static String getContextPath(String contextRoot) {
        return  contextRoot.equals("/")
                ? "/" : "/" + contextRoot;
    }
}

