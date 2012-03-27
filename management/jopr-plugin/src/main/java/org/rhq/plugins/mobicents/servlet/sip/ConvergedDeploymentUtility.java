/**
 * 
 */
package org.rhq.plugins.mobicents.servlet.sip;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mc4j.ems.connection.EmsConnection;
import org.mc4j.ems.connection.bean.EmsBean;
import org.mc4j.ems.connection.bean.operation.EmsOperation;
import org.rhq.plugins.jbossas.util.WarDeploymentInformation;
import org.rhq.plugins.jmx.ObjectNameQueryUtility;

/**
 * Accesses the MainDeployer mbean to find the deployment files behind services.
 *
 * Original file copied over from jopr jboss as plugin trunk rev 26. 
 * getVHosts method has been modified to fetch SipManager instead of Manager
 * 
 * @author Greg Hinkle
 * @author <A HREF="mailto:jean.deruelle@gmail.com">Jean Deruelle</A>
 */
public class ConvergedDeploymentUtility {
	private static Log log = LogFactory.getLog(ConvergedDeploymentUtility.class);
	/**
     * The object name of the JBoss main deployer MBean.
     */
    protected static final String MAIN_DEPLOYER = "jboss.system:service=MainDeployer";

    /**
     * The name of the main deployer operation that is to be used to get the list of the modules are deployed - this is
     * the Main Deployer operation name for JBossAS 4.x.
     */
    private static final String LIST_DEPLOYED_MODULES_OP_NAME = "listDeployedModules";

    /**
     * The name of the main deployer operation that is to be used to get the list of the modules are deployed - this is
     * the Main Deployer operation name for JBossAS 3.x.
     */
    private static final String LIST_DEPLOYED_OP_NAME = "listDeployed";
	
	/**
     * Retrieves all the discovery information for a War resources. We are retrieving all the information
     * so that there is only ever one call to the MBeanServer to get the deployed mbeans, therefore saving
     * some performance if it did this for each and every war resource one at a time.
     *
     * @param connection EmsConnection to get the mbean information
     * @param objectNames Name of the main jboss.management mbeans for a collection of wars.
     * @return map holds all the war deployment information for the objects passed in the objectNames collection
     */
    public static Map<String, List<WarDeploymentInformation>> getConvergedWarDeploymentInformation(EmsConnection connection,
        List<String> objectNames) {
        // We need a list of informations, as one jsr77 deployment can end up in multiple web apps in different vhosts
        HashMap<String, List<WarDeploymentInformation>> retDeploymentInformationMap = new HashMap<String, List<WarDeploymentInformation>>();

        // will contain information on all deployments
        Collection deployed;
        try {
            deployed = getDeploymentInformations(connection);
        }
        catch (Exception e) {
            return null;
        }

        String separator = System.getProperty("file.separator");
        boolean isOnWin = separator.equals("\\");
        
        // Loop through the deployments, get the name information and compare it to the collection
        // of strings passed into this method
        //        Interpreter i = new Interpreter();
        for (Object sdi : deployed) {
            try {
                ObjectName jbossWebObjectName = getFieldValue(sdi, "deployedObject", javax.management.ObjectName.class);
                if (jbossWebObjectName != null) {
                    String shortName = getFieldValue(sdi, "shortName", String.class);

                    for (String objectNameString : objectNames) {
                        String nameFromObject = getMBeanNameAttribute(objectNameString);

                        if (shortName.equals(nameFromObject)) {

                            String jbossWebmBeanName = jbossWebObjectName.getCanonicalName();
                            String contextRoot = getMBeanNameAttribute(jbossWebmBeanName);
                            int lastSlashIndex = contextRoot.lastIndexOf("/");
                            if (lastSlashIndex > 0) {
                                int lastIndex = contextRoot.length() - 1;
                                // If it ends in a slash, it's the root context (context root -> "/"). Otherwise, the
                                // context root will be everything after the last slash (e.g. "jmx-console").
                                contextRoot = (lastSlashIndex == lastIndex) ? "/" : contextRoot.substring(lastSlashIndex + 1);
                            }

                            String file = getFieldValue(sdi, "url", URL.class).toString();
                            if (file.startsWith("file:/")) {
                                if (isOnWin) {
                                   file = file.substring(6);
                                   // listDeployed() always delivers / as path separator, so we need to correct this.
                                   File tmp = new File(file);
                                   file = tmp.getCanonicalPath();
                                }
                                else
                                   file = file.substring(5);
                            }

                            /*
                             * We now have a valid deployment. Go back to the MBeanServer and get *all*
                             * applicable virtual hosts for this web app. Return deployment info for all 
                             * of them.
                             * Most of the time this will only be one virtual host called 'localhost', as
                             * this is the default if no vhost is set.
                             */
                            List<EmsBean> vhosts = getVHosts(contextRoot, connection);
                            List<WarDeploymentInformation> infos = new ArrayList<WarDeploymentInformation>(vhosts
                                .size());

                            for (EmsBean vhost : vhosts) {
                                WarDeploymentInformation deploymentInformation = new WarDeploymentInformation();
                                String vhostname = vhost.getBeanName().getKeyProperty("host");
                                deploymentInformation.setVHost(vhostname);
                                deploymentInformation.setFileName(file);
                                deploymentInformation.setContextRoot(contextRoot);
                                // jboss.web:J2EEApplication=none,J2EEServer=none,j2eeType=WebModule,name=//bsd.de/test
                                // TODO to we have a better tool to exchange a value of a key-value pair in an ObjectName ?
                                int index = jbossWebmBeanName.indexOf("name=");
                                jbossWebmBeanName = jbossWebmBeanName.substring(0, index + 5);
                                jbossWebmBeanName += "//" + vhostname +ConvergedWarDiscoveryHelper.getContextPath(contextRoot);

                                deploymentInformation.setJbossWebModuleMBeanObjectName(jbossWebmBeanName);
                                infos.add(deploymentInformation);
                            }
                            retDeploymentInformationMap.put(objectNameString, infos);
                        }
                    }
                }
            } catch (Exception evalError) {
                log.warn("Failed to determine if a deployment contains our mbean", evalError);
            }
        }
        return retDeploymentInformationMap;
    }

	
	/**
     * Retrieves the virtual host MBeans for the webapp with the specified context root.
     * VHost MBeans have the pattern "jboss.web:host=*,path=/<ctx_root>,type=Manager".
     *
     * @param contextRoot the context root
     * 
     * @return the list of VHost MBeans for this webapp
     */
    public static List<EmsBean> getVHosts(String contextRoot, EmsConnection emsConnection) {
        String pattern = "jboss.web:host=%host%,path=" + ConvergedWarDiscoveryHelper.getContextPath(contextRoot)
                + ",type=SipManager";
        ObjectNameQueryUtility queryUtil = new ObjectNameQueryUtility(pattern);
        String translatedQuery = queryUtil.getTranslatedQuery();
        List<EmsBean> mBeans = emsConnection.queryBeans(translatedQuery);
        
        pattern = "jboss.web:host=%host%,path=" + ConvergedWarDiscoveryHelper.getContextPath(contextRoot)
        + ",type=ClusterSipManager";
        queryUtil = new ObjectNameQueryUtility(pattern);
        translatedQuery = queryUtil.getTranslatedQuery();
        List<EmsBean> haMBeans = emsConnection.queryBeans(translatedQuery);
                
        List<EmsBean> allBeans = new ArrayList<EmsBean>(mBeans);
        allBeans.addAll(haMBeans);
        
        return allBeans;
    }
    
    private static Collection getDeploymentInformations(EmsConnection connection) throws Exception {
        Collection deploymentInfos = null;
        EmsOperation operation = null;
        try {
            operation = getListDeployedOperation(connection);
            if (operation == null) {
                throw new UnsupportedOperationException(
                    "This JBossAS instance is unsupported; its MainDeployer MBean doesn't have a listDeployedModules or listDeployed operation.");
            }
            deploymentInfos = (Collection) operation.invoke(new Object[0]);
        } catch (RuntimeException re) {
            // Make a last ditch effort in case the call to listDeployedModules() failed due to
            // https://jira.jboss.org/jira/browse/JBAS-5983.
            if (operation != null && operation.getName().equals(LIST_DEPLOYED_MODULES_OP_NAME)) {
                EmsBean mainDeployerMBean = connection.getBean(MAIN_DEPLOYER);
                operation = mainDeployerMBean.getOperation(LIST_DEPLOYED_OP_NAME);
                try {
                    deploymentInfos = (Collection) operation.invoke(new Object[0]);
                }
                catch (RuntimeException re2) {
                    deploymentInfos = null;
                }
            }
            if (deploymentInfos == null) {
                log.warn("Cannot determine deployed modules - cause: " + re);
                throw new Exception(re);
            }
        }
        return deploymentInfos;
    }
    
    private static String getMBeanNameAttribute(String objectBeanName) {
        Object[] splitName = objectBeanName.split("name=");
        String retNameAttribute = "";
        if (splitName.length > 1) {
            String afterName = (String) splitName[1];
            String[] removeAnythingAfter = afterName.split(",");
            if (removeAnythingAfter.length > 0) {
                retNameAttribute = removeAnythingAfter[0];
            } else {
                retNameAttribute = afterName;
            }
        }
        return retNameAttribute;
    }

    private static <T> T getFieldValue(Object target, String name, Class<T> T) {

        if (target == null)
            return null;

        Field field;
        T ret;
        try {
            field = target.getClass().getField(name);
            ret = (T) field.get(target);
            if (T == ObjectName.class && ret != null) {
                ret = (T) new ObjectName(ret.toString());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return ret;
    }
    
    protected static EmsOperation getListDeployedOperation(EmsConnection connection) {
        EmsOperation retOperation;
        EmsBean bean = connection.getBean(MAIN_DEPLOYER);

        // first try the new operation name, used by JBossAS 3.2.8 and 4.x.
        retOperation = bean.getOperation(LIST_DEPLOYED_MODULES_OP_NAME);

        // if that doesn't exist, we are probably connected to a JBossAS 3.2.7 or earlier version
        if (retOperation == null) {
            retOperation = bean.getOperation(LIST_DEPLOYED_OP_NAME);
        }

        // if we still did not manage to find the operation name, let the caller handle this error condition
        return retOperation;
    }
}
