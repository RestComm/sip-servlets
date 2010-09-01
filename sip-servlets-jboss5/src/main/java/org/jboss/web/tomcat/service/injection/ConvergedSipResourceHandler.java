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
package org.jboss.web.tomcat.service.injection;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.servlet.sip.SipFactory;
import javax.servlet.sip.SipSessionsUtil;
import javax.transaction.UserTransaction;
import javax.xml.ws.WebServiceContext;

import org.jboss.ejb3.Container;
import org.jboss.injection.EnvEntryEncInjector;
import org.jboss.injection.InjectionContainer;
import org.jboss.injection.InjectionUtil;
import org.jboss.injection.Injector;
import org.jboss.injection.InjectorFactory;
import org.jboss.injection.JndiPropertyInjector;
import org.jboss.injection.LinkRefEncInjector;
import org.jboss.injection.SipFactoryPropertyInjector;
import org.jboss.injection.SipSessionsUtilPropertyInjector;
import org.jboss.injection.SipTimerServicePropertyInjector;
import org.jboss.injection.TimerServicePropertyInjector;
import org.jboss.injection.UserTransactionPropertyInjector;
import org.jboss.injection.ValueEncInjector;
import org.jboss.injection.lang.reflect.BeanProperty;
import org.jboss.injection.lang.reflect.FieldBeanProperty;
import org.jboss.logging.Logger;
import org.jboss.metadata.javaee.spec.EnvironmentEntryMetaData;
import org.jboss.metadata.javaee.spec.MessageDestinationReferenceMetaData;
import org.jboss.metadata.javaee.spec.RemoteEnvironment;
import org.jboss.metadata.javaee.spec.ResourceEnvironmentReferenceMetaData;
import org.jboss.metadata.javaee.spec.ResourceReferenceMetaData;
import org.jboss.reflect.plugins.ValueConvertor;
import org.jboss.web.tomcat.service.TomcatConvergedSipInjectionContainer;
import org.mobicents.servlet.sip.startup.SipContext;
import org.omg.CORBA.ORB;

/**
 * InjectionHandler for sip components. Allows to inject SipFactory, SipSessionsUtil and TimerService from @Resource annotation field 
 * 
 * @author jean.deruelle@gmail.com
 *
 */
public class ConvergedSipResourceHandler<X extends RemoteEnvironment> extends WebResourceHandler<X> {

	private static final Logger log = Logger.getLogger(ConvergedSipResourceHandler.class);
	
	private boolean checkEncInjectors;

    public ConvergedSipResourceHandler()
    {
       this(true);
    }
    
    public ConvergedSipResourceHandler(boolean checkEncInjectors)
    {
       this.checkEncInjectors = checkEncInjectors;
    }
    
	
	public void handleFieldAnnotations(Field field, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
    {
       Resource ref = container.getAnnotation(Resource.class, field);
       if (ref == null) return;

       log.trace("field " + field + " has @Resource");
       
       handlePropertyAnnotation(ref, new FieldBeanProperty(field), container, injectors);
    }

	
	private static void createURLInjector(String encName, String mappedName, InjectionContainer container)
	{
	      assert encName.length() > 0 : "encName is empty";
	      assert mappedName.length() > 0 : "mappedName is empty";

	      // Create a URL from the mappedName
	      try
	      {
	         URL url = new URL(mappedName.trim());
	         container.getEncInjectors().put(encName, new ValueEncInjector(encName, url, "@Resource"));
	      }
	      catch (MalformedURLException e)
	      {
	         throw new RuntimeException("failed to create url injector for: " + encName, e);
	      }
	}
	
    private void handlePropertyAnnotation(Resource ref, BeanProperty property, InjectionContainer container, Map<AccessibleObject, Injector> injectors)
    {
       assert ref != null;
       assert property != null;
       assert container != null;
       assert injectors != null;
       
       String encName = ref.name();
       if (encName == null || encName.equals(""))
       {
          //encName = InjectionUtil.getEncName(field);
          encName = property.getDeclaringClass().getName() + "/" + property.getName();
       }
       if (!encName.startsWith("env/"))
       {
          encName = "env/" + encName;
       }

       AccessibleObject accObj = property.getAccessibleObject();
       
       Class<?> type = property.getType();
       if (!ref.type().equals(Object.class))
       {
          type = ref.type();
       }
       
       if (type.equals(UserTransaction.class))
       {
          injectors.put(accObj, new UserTransactionPropertyInjector(property, container));
       }
       else if (type.equals(TimerService.class))
       {
          injectors.put(accObj, new TimerServicePropertyInjector(property, (Container) container)); // only EJBs
       }
       //Injects a Sip Factory
       else if (type.equals(SipFactory.class))
       {          
    	   injectors.put(accObj, new SipFactoryPropertyInjector(property, container));
       }
       //Injects a SipSessionsUtil
       else if (type.equals(SipSessionsUtil.class))
       {
    	   injectors.put(accObj, new SipSessionsUtilPropertyInjector(property, container));          
       }
       //Injects a Timer Service
       else if (type.equals(javax.servlet.sip.TimerService.class))
       {
    	   injectors.put(accObj, new SipTimerServicePropertyInjector(property, container));          
       }
       else if(type.equals(URL.class) && ref.mappedName() != null && ref.mappedName().length() > 0)
       {
          createURLInjector(encName, ref.mappedName(), container);
          injectors.put(accObj, new JndiPropertyInjector(property, encName, container.getEnc()));
       }
       else if (type.equals(String.class)
               || type.equals(Character.class)
               || type.equals(Byte.class)
               || type.equals(Short.class)
               || type.equals(Integer.class)
               || type.equals(Long.class)
               || type.equals(Boolean.class)
               || type.equals(Double.class)
               || type.equals(Float.class)
               || type.isPrimitive()
               )
       {
          // don't add an injector if no XML <env-entry is present as there will be no value to inject
          if (container.getEncInjectors().containsKey(encName))
          {
             injectors.put(accObj, new JndiPropertyInjector(property, encName, container.getEnc()));
          }
          else if (ref.mappedName() != null && ref.mappedName().length() > 0)
          {
             // Use the mappedName as the string value
             String s = ref.mappedName().trim();
             try
             {
                Object value = ValueConvertor.convertValue(type, s);
                container.getEncInjectors().put(encName, new ValueEncInjector(encName, value, "@Resource"));
                injectors.put(accObj, new JndiPropertyInjector(property, encName, container.getEnc()));
             }
             catch(Throwable t)
             {
                throw new RuntimeException("Failed to convert: "+ref.mappedName()+" to type:"+type, t);
             }
          }
          else
          {
             log.warn("Not injecting " + property.getName() + ", no matching enc injector " + encName + " found");
          }
       }
       else
       {
          if (checkEncInjectors && !container.getEncInjectors().containsKey(encName))
          {
             String mappedName = ref.mappedName();
             if (mappedName == null || mappedName.equals(""))
             {
                // TODO: is this a nice trick?
//                if(ConnectionFactory.class.isAssignableFrom(type))
//                {
//                   // neat little trick
//                   mappedName = "java:/ConnectionFactory";
//                }
//                else
                if(ORB.class.isAssignableFrom(type))
                   mappedName = "java:comp/ORB";
                else
                   throw new RuntimeException("You did not specify a @Resource.mappedName() on " + accObj + " and there is no binding for enc name " + encName + " in XML");
             }
             container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, mappedName, "@Resource"));
          }
          injectors.put(accObj, new JndiPropertyInjector(property, encName, container.getEnc()));
       }      
    }
    
    public void loadXml(X xml, InjectionContainer container)
    {
       if (xml == null)
          return;
       if (xml.getMessageDestinationReferences() != null)
          loadXmlMessageDestinationRefs(container, xml.getMessageDestinationReferences());
       if (xml.getResourceEnvironmentReferences() != null)
          loadXmlResourceEnvRefs(container, xml.getResourceEnvironmentReferences());
       if (xml.getResourceReferences() != null)
          loadXmlResourceRefs(container, xml.getResourceReferences());
       if (xml.getEnvironmentEntries() != null)
          loadEnvEntry(container, xml.getEnvironmentEntries());
    }

    private static void loadEnvEntry(InjectionContainer container, Collection<EnvironmentEntryMetaData> envEntries)
    {
       for (EnvironmentEntryMetaData envEntry : envEntries)
       {
          String encName = "env/" + envEntry.getEnvEntryName();
          // 16.4.1.3: If the env-entry-value is not specified, no value will be injected and it
          // will not be initialized into the naming context.
          if (envEntry.getValue() == null)
          {
             log.debug("ignoring env-entry " + envEntry);
             continue;
          }
          InjectionUtil.injectionTarget(encName, envEntry, container, container.getEncInjections());
          if (container.getEncInjectors().containsKey(encName))
             continue;
          log.trace("adding env-entry injector " + encName);
          container.getEncInjectors().put(encName,
                new EnvEntryEncInjector(encName, envEntry.getType(), envEntry.getValue()));
       }
    }

    private static void loadXmlResourceRefs(InjectionContainer container, Collection<ResourceReferenceMetaData> refs)
    {
       for (ResourceReferenceMetaData envRef : refs)
       {
          String encName = "env/" + envRef.getResourceRefName();
          if (container.getEncInjectors().containsKey(encName))
             continue;

          String mappedName = envRef.getMappedName();
          if(mappedName == null || mappedName.length() == 0)
             mappedName = envRef.getResolvedJndiName();
          if (mappedName == null || mappedName.length() == 0)
          {
             if (envRef.getResUrl() != null)
             {
                try
                {
                   container.getEncInjectors().put(encName, new ValueEncInjector(encName, new URL(envRef.getResUrl().trim()), "<resource-ref>"));
                }
                catch (MalformedURLException e)
                {
                   throw new RuntimeException(e);
                }
             }
             else if (UserTransaction.class.getName().equals(envRef.getType()))
             {
                final InjectionContainer ic = container;
                InjectorFactory<?> factory = new InjectorFactory<UserTransactionPropertyInjector>()
                {
                   public UserTransactionPropertyInjector create(BeanProperty property)
                   {
                      return new UserTransactionPropertyInjector(property, ic);
                   }
                };
                if(envRef.getInjectionTargets() != null)
                {
                   TomcatInjectionUtils.createInjectors(container.getEncInjections(), container.getClassloader(), factory, envRef.getInjectionTargets());
                   continue;
                }
                else
                {
                   encName = "java:comp/UserTransaction";
                }
             }
             else if (ORB.class.getName().equals(envRef.getType()))
             {
                encName = "java:comp/ORB";
             }            
             else
             {
                throw new RuntimeException("mapped-name is required for " + envRef.getResourceRefName() + " of deployment " + container.getIdentifier());
             }
          }
          else if(URL.class.getName().equals(envRef.getType()) && ! mappedName.startsWith("java:") )
          {
             createURLInjector(encName, mappedName, container);
             InjectionUtil.injectionTarget(encName, envRef, container, container.getEncInjections());
             continue;
          }
          else if(envRef.getResUrl() != null)
          {
             try
             {
                container.getEncInjectors().put(encName, new ValueEncInjector(encName, new URL(envRef.getResUrl().trim()), "<resource-ref>"));
             }
             catch (MalformedURLException e)
             {
                throw new RuntimeException(e);
             }
          }
          else
          {
             container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, mappedName, "<resource-ref>"));
          }
          InjectionUtil.injectionTarget(encName, envRef, container, container.getEncInjections());
       }
    }

    private static void loadXmlResourceEnvRefs(InjectionContainer container, Collection<ResourceEnvironmentReferenceMetaData> refs)
    {
       for (ResourceEnvironmentReferenceMetaData envRef : refs)
       {
          String resTypeName = envRef.getType();
          String mappedName = envRef.getMappedName();
          if(mappedName == null || mappedName.length() == 0)
             mappedName = envRef.getResolvedJndiName();
          try
          {
             if(resTypeName != null)
             {
            	Class<?> resType = Class.forName(resTypeName, false, container.getClassloader());
                if(TimerService.class.isAssignableFrom(resType))
                {
                   log.warn("Ignoring invalid TimerService resource-env-ref");
                   continue;
                }
                else if(SessionContext.class.isAssignableFrom(resType))
                {
                   log.warn("Ignoring invalid SessionContext resource-env-ref");
                   continue;
                }
                else if (resType.equals(UserTransaction.class))
                {
                   final InjectionContainer ic = container;
                   InjectorFactory<?> factory = new InjectorFactory<UserTransactionPropertyInjector>()
                   {
                      public UserTransactionPropertyInjector create(BeanProperty property)
                      {
                         return new UserTransactionPropertyInjector(property, ic);
                      }
                   };
                   if(envRef.getInjectionTargets() != null)
                   {
                      TomcatInjectionUtils.createInjectors(container.getEncInjections(), container.getClassloader(), factory, envRef.getInjectionTargets());
                      continue;
                   }
                   else
                   {
                      mappedName = "java:comp/UserTransaction";
                   }
                }
                //Injects a Sip Factory
                else if (resType.equals(SipFactory.class))
                {
                   final InjectionContainer ic = container;
                   InjectorFactory<?> factory = new InjectorFactory<SipFactoryPropertyInjector>()
                   {
                      public SipFactoryPropertyInjector create(BeanProperty property)
                      {
                         return new SipFactoryPropertyInjector(property, ic);
                      }
                   };
                   if(envRef.getInjectionTargets() != null)
                   {
                      TomcatInjectionUtils.createInjectors(container.getEncInjections(), container.getClassloader(), factory, envRef.getInjectionTargets());
                      continue;
                   }
                   else
                   {
                      mappedName = "java:comp/env/sip/"+ ((SipContext)((TomcatConvergedSipInjectionContainer)container).getCatalinaContext()).getApplicationName() +"/SipFactory";
                   }
                }
                //Injects a SipSessionsUtil
                else if (resType.equals(SipSessionsUtil.class))
                {
                   final InjectionContainer ic = container;
                   InjectorFactory<?> factory = new InjectorFactory<SipSessionsUtilPropertyInjector>()
                   {
                      public SipSessionsUtilPropertyInjector create(BeanProperty property)
                      {
                         return new SipSessionsUtilPropertyInjector(property, ic);
                      }
                   };
                   if(envRef.getInjectionTargets() != null)
                   {
                      TomcatInjectionUtils.createInjectors(container.getEncInjections(), container.getClassloader(), factory, envRef.getInjectionTargets());
                      continue;
                   }
                   else
                   {
                      mappedName = "java:comp/env/sip/"+ ((SipContext)((TomcatConvergedSipInjectionContainer)container).getCatalinaContext()).getApplicationName() +"/SipSessionsUtil";
                   }
                }
                //Injects a Timer Service
                else if (resType.equals(javax.servlet.sip.TimerService.class))
                {
                   final InjectionContainer ic = container;
                   InjectorFactory<?> factory = new InjectorFactory<SipTimerServicePropertyInjector>()
                   {
                      public SipTimerServicePropertyInjector create(BeanProperty property)
                      {
                         return new SipTimerServicePropertyInjector(property, ic);
                      }
                   };
                   if(envRef.getInjectionTargets() != null)
                   {
                      TomcatInjectionUtils.createInjectors(container.getEncInjections(), container.getClassloader(), factory, envRef.getInjectionTargets());
                      continue;
                   }
                   else
                   {
                      mappedName = "java:comp/env/sip/"+ ((SipContext)((TomcatConvergedSipInjectionContainer)container).getCatalinaContext()).getApplicationName() +"/TimerService";
                   }
                }
                else if (resType.equals(ORB.class))
                {
                   mappedName = "java:comp/ORB";
                   continue;
                }
                else if(WebServiceContext.class.getName().equals(envRef.getType()))                      
                {     
                   // JBAS-5359
                   InjectorFactory<?> factory = new InjectorFactory<WebServiceContextPropertyInjector>()
                   {                       public WebServiceContextPropertyInjector create(BeanProperty property)          
                      {
                         return new WebServiceContextPropertyInjector(property);
                      }
                   }; 
                   if(envRef.getInjectionTargets() != null)
                   {
                      TomcatInjectionUtils.createInjectors(container.getEncInjections(), container.getClassloader(), factory, envRef.getInjectionTargets());
                      continue; 
                   }  
                }
             }
          }
          catch(ClassNotFoundException e)
          {
        	  throw new EJBException(e);
          }
          
          String encName = "env/" + envRef.getResourceEnvRefName();
          if (container.getEncInjectors().containsKey(encName))
             continue;
          if (mappedName == null || mappedName.equals(""))
          {
             throw new RuntimeException("mapped-name is required for " + envRef.getResourceEnvRefName() + " of deployment " + container.getIdentifier());
          }
          container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, envRef.getMappedName(), "<resource-ref>"));
          InjectionUtil.injectionTarget(encName, envRef, container, container.getEncInjections());
       }
    }

    private static void loadXmlMessageDestinationRefs(InjectionContainer container, Collection<MessageDestinationReferenceMetaData> refs)
    {
       for (MessageDestinationReferenceMetaData envRef : refs)
       {
          String encName = "env/" + envRef.getMessageDestinationRefName();
          if (container.getEncInjectors().containsKey(encName)) continue;
          String jndiName = envRef.getMappedName();
          if (jndiName == null || jndiName.equals(""))
          {
             jndiName = envRef.getResolvedJndiName();
             if (jndiName == null || jndiName.equals(""))
                throw new RuntimeException("mapped-name is required for " + envRef.getMessageDestinationRefName() + " of deployment " + container.getIdentifier());
          }
          container.getEncInjectors().put(encName, new LinkRefEncInjector(encName, jndiName, "<message-destination-ref>"));
          InjectionUtil.injectionTarget(encName, envRef, container, container.getEncInjections());
       }
    }
}
