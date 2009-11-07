/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.bootstrap;

import static org.jboss.weld.logging.Category.BOOTSTRAP;
import static org.jboss.weld.logging.Category.VERSION;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BootstrapMessage.JTA_UNAVAILABLE;
import static org.jboss.weld.logging.messages.BootstrapMessage.VALIDATING_BEANS;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
 

import javax.enterprise.inject.spi.Extension;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.Container;
import org.jboss.weld.ContextualStoreImpl;
import org.jboss.weld.Validator;
import org.jboss.weld.Container.Status;
import org.jboss.weld.bean.builtin.ManagerBean;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.Lifecycle;
import org.jboss.weld.bootstrap.api.Service;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.ServiceRegistries;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.events.AfterBeanDiscoveryImpl;
import org.jboss.weld.bootstrap.events.AfterDeploymentValidationImpl;
import org.jboss.weld.bootstrap.events.BeforeBeanDiscoveryImpl;
import org.jboss.weld.bootstrap.events.BeforeShutdownImpl;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.context.AbstractApplicationContext;
import org.jboss.weld.context.ApplicationContext;
import org.jboss.weld.context.ContextLifecycle;
import org.jboss.weld.context.ConversationContext;
import org.jboss.weld.context.DependentContext;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.SessionContext;
import org.jboss.weld.context.SingletonContext;
import org.jboss.weld.context.api.BeanStore;
import org.jboss.weld.conversation.ConversationImpl;
import org.jboss.weld.conversation.NumericConversationIdGenerator;
import org.jboss.weld.conversation.ServletConversationManager;
import org.jboss.weld.ejb.EJBApiAbstraction;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.spi.EjbDescriptor;
import org.jboss.weld.jsf.JsfApiAbstraction;
import org.jboss.weld.logging.messages.VersionMessage;
import org.jboss.weld.metadata.TypeStore;
import org.jboss.weld.metadata.cache.MetaAnnotationStore;
import org.jboss.weld.persistence.PersistenceApiAbstraction;
import org.jboss.weld.resources.ClassTransformer;
import org.jboss.weld.resources.DefaultResourceLoader;
import org.jboss.weld.resources.SingleThreadScheduledExecutorServiceFactory;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.resources.spi.ScheduledExecutorServiceFactory;
import org.jboss.weld.serialization.spi.ContextualStore;
import org.jboss.weld.servlet.HttpSessionManager;
import org.jboss.weld.servlet.ServletApiAbstraction;
import org.jboss.weld.servlet.api.ServletServices;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.util.Names;
import org.jboss.weld.util.collections.Arrays2;
import org.jboss.weld.util.serviceProvider.DefaultServiceLoaderFactory;
import org.jboss.weld.util.serviceProvider.ServiceLoaderFactory;
import org.jboss.weld.ws.WSApiAbstraction;
import org.slf4j.cal10n.LocLogger;

/**
 * Common bootstrapping functionality that is run at application startup and
 * detects and register beans
 * 
 * @author Pete Muir
 */
public class WeldBootstrap implements Bootstrap
{
   
   private static final LocLogger log = loggerFactory().getLogger(BOOTSTRAP);
   
   /**
    * 
    * A Deployment visitor which can find the transitive closure of Bean 
    * Deployment Archives
    * 
    * @author pmuir
    *
    */
   private static class DeploymentVisitor
   {
      
      private final BeanManagerImpl deploymentManager;
      private final Environment environment;
      private final Deployment deployment;
      private final Map<BeanDeploymentArchive, BeanDeployment> managerAwareBeanDeploymentArchives;
      private final BeanDeploymentArchive implementationBeanDeploymentArchive;
      
      public DeploymentVisitor(BeanManagerImpl deploymentManager, Environment environment, final Deployment deployment)
      {
         this.deploymentManager = deploymentManager;
         this.environment = environment;
         this.deployment = deployment;
         this.managerAwareBeanDeploymentArchives = new ConcurrentHashMap<BeanDeploymentArchive, BeanDeployment>();
         this.implementationBeanDeploymentArchive = new BeanDeploymentArchive()
         {
            
            private  final ServiceRegistry serviceRegistry = new SimpleServiceRegistry();
            private final Set<Class<?>> beanClasses = (deployment.getServices().contains(ServletServices.class)) 
                    ? Arrays2.<Class<?>>asSet(ConversationImpl.class, ServletConversationManager.class, NumericConversationIdGenerator.class, HttpSessionManager.class)
                    : Collections.EMPTY_SET;
            
            public ServiceRegistry getServices()
            {
               return serviceRegistry;
            }
            
            public String getId()
            {
               return "weld";
            }
            
            public Collection<EjbDescriptor<?>> getEjbs()
            {
               return Collections.emptySet();
            }
            
            public Collection<URL> getBeansXml()
            {
               return Collections.emptySet();
            }
            
            public Collection<BeanDeploymentArchive> getBeanDeploymentArchives()
            {
               return Collections.emptySet();
            }
            
            public Collection<Class<?>> getBeanClasses()
            {
               return beanClasses;
            }
         };
      }
      
      public Map<BeanDeploymentArchive, BeanDeployment> visit()
      {
         // Add the impl beans
         visit(implementationBeanDeploymentArchive, managerAwareBeanDeploymentArchives, new HashSet<BeanDeploymentArchive>(), false);
         
         for (BeanDeploymentArchive archvive : deployment.getBeanDeploymentArchives())
         {
            visit(archvive, managerAwareBeanDeploymentArchives, new HashSet<BeanDeploymentArchive>(), true);
         }
         return managerAwareBeanDeploymentArchives;
      }
      
      private BeanDeployment visit(BeanDeploymentArchive beanDeploymentArchive, Map<BeanDeploymentArchive, BeanDeployment> managerAwareBeanDeploymentArchives, Set<BeanDeploymentArchive> seenBeanDeploymentArchives, boolean validate)
      {
         // Check that the required services are specified
         if (validate)
         {
            verifyServices(beanDeploymentArchive.getServices(), environment.getRequiredBeanDeploymentArchiveServices());
         }
         
         // Check the id is not null
         if (beanDeploymentArchive.getId() == null)
         {
            throw new IllegalArgumentException("BeanDeploymentArchive must not be null " + beanDeploymentArchive);
         }
         
         BeanDeployment parent = managerAwareBeanDeploymentArchives.get(beanDeploymentArchive);
         if (parent == null)
         {
            // Create the BeanDeployment and attach
            parent = new BeanDeployment(beanDeploymentArchive, deploymentManager, deployment.getServices());
            managerAwareBeanDeploymentArchives.put(beanDeploymentArchive, parent);
         }
         seenBeanDeploymentArchives.add(beanDeploymentArchive);
         for (BeanDeploymentArchive archive : beanDeploymentArchive.getBeanDeploymentArchives())
         {
            // Cut any circularties
            if (!seenBeanDeploymentArchives.contains(archive))
            {
               BeanDeployment child = visit(archive, managerAwareBeanDeploymentArchives, seenBeanDeploymentArchives, validate);
               parent.getBeanManager().addAccessibleBeanManager(child.getBeanManager());
            }
         }
         // Make the implementation beans accessible
         parent.getBeanManager().addAccessibleBeanManager(managerAwareBeanDeploymentArchives.get(implementationBeanDeploymentArchive).getBeanManager());
         return parent;
      }
      
   }
   
   static
   {
      loggerFactory().getLogger(VERSION).info(VersionMessage.VERSION, Names.version(WeldBootstrap.class.getPackage()));
   }

   // The Bean manager
   private BeanManagerImpl deploymentManager;
   private Map<BeanDeploymentArchive, BeanDeployment> beanDeployments;
   private Environment environment;
   private Deployment deployment;
   private ExtensionBeanDeployerEnvironment extensionDeployerEnvironment;
   private DeploymentVisitor deploymentVisitor;
 
   public Bootstrap startContainer(Environment environment, Deployment deployment, BeanStore applicationBeanStore)
   {
      synchronized (this)
      {
         if (deployment == null)
         {
            throw new IllegalArgumentException("Must start the container with a deployment");
         }
         if (!deployment.getServices().contains(ResourceLoader.class))
         {
            deployment.getServices().add(ResourceLoader.class, new DefaultResourceLoader());
         }
         if (!deployment.getServices().contains(ScheduledExecutorServiceFactory.class))
         {
            deployment.getServices().add(ScheduledExecutorServiceFactory.class, new SingleThreadScheduledExecutorServiceFactory());
         }
         
         verifyServices(deployment.getServices(), environment.getRequiredDeploymentServices());
         
         if (!deployment.getServices().contains(TransactionServices.class))
         {
            log.info(JTA_UNAVAILABLE);
         }
         // TODO Reinstate if we can find a good way to detect.
//         if (!deployment.getServices().contains(EjbServices.class))
//         {
//            log.info("EJB services not available. Session beans will be simple beans, CDI-style injection into non-contextual EJBs, injection of remote EJBs and injection of @EJB in simple beans will not be available");
//         }
//         if (!deployment.getServices().contains(JpaInjectionServices.class))
//         {
//            log.info("JPA services not available. Injection of @PersistenceContext will not occur. Entity beans will be discovered as simple beans.");
//         }
//         if (!deployment.getServices().contains(ResourceInjectionServices.class))
//         {
//            log.info("@Resource injection not available.");
//         }
         if (applicationBeanStore == null)
         {
            throw new IllegalStateException("No application context BeanStore set");
         }
         
         this.deployment = deployment;
         ServiceRegistry implementationServices = getImplementationServices(deployment.getServices().get(ResourceLoader.class));
         
         deployment.getServices().addAll(implementationServices.entrySet());
         
         ServiceRegistry deploymentServices = new SimpleServiceRegistry();
         deploymentServices.add(ClassTransformer.class, implementationServices.get(ClassTransformer.class));
         deploymentServices.add(MetaAnnotationStore.class, implementationServices.get(MetaAnnotationStore.class));
         deploymentServices.add(TypeStore.class, implementationServices.get(TypeStore.class));
         
         this.environment = environment;
         this.deploymentManager = BeanManagerImpl.newRootManager("deployment", deploymentServices);
         
         Container.initialize(deploymentManager, ServiceRegistries.unmodifiableServiceRegistry(deployment.getServices()));
         Container.instance().setStatus(Status.STARTING);
         
         createContexts();
         initializeContexts();
         // Start the application context
         Container.instance().deploymentServices().get(ContextLifecycle.class).beginApplication(applicationBeanStore);
         this.extensionDeployerEnvironment = new ExtensionBeanDeployerEnvironment(EjbDescriptors.EMPTY, deploymentManager);
         this.deploymentVisitor = new DeploymentVisitor(deploymentManager, environment, deployment);
         
         // Read the deployment structure, this will be the physical structure as caused by the presence of beans.xml
         beanDeployments = deploymentVisitor.visit();
         
         return this;
      }
   }
   
   private ServiceRegistry getImplementationServices(ResourceLoader resourceLoader)
   {
      ServiceRegistry services = new SimpleServiceRegistry();
      services.add(EJBApiAbstraction.class, new EJBApiAbstraction(resourceLoader));
      services.add(JsfApiAbstraction.class, new JsfApiAbstraction(resourceLoader));
      services.add(PersistenceApiAbstraction.class, new PersistenceApiAbstraction(resourceLoader));
      services.add(WSApiAbstraction.class, new WSApiAbstraction(resourceLoader));
      services.add(ServletApiAbstraction.class, new ServletApiAbstraction(resourceLoader));
      // Temporary workaround to provide context for building annotated class
      // TODO expose AnnotatedClass on SPI and allow container to provide impl of this via ResourceLoader
      services.add(Validator.class, new Validator());
      services.add(TypeStore.class, new TypeStore());
      services.add(ClassTransformer.class, new ClassTransformer(services.get(TypeStore.class)));
      services.add(MetaAnnotationStore.class, new MetaAnnotationStore(services.get(ClassTransformer.class)));
      services.add(ContextualStore.class, new ContextualStoreImpl());
      services.add(ServiceLoaderFactory.class, new DefaultServiceLoaderFactory());
      return services;
   }
   
   public BeanManagerImpl getManager(BeanDeploymentArchive beanDeploymentArchive)
   {
      if (beanDeployments.containsKey(beanDeploymentArchive))
      {
         return beanDeployments.get(beanDeploymentArchive).getBeanManager();
      }
      else
      {
         return null;
      }
   }
   
   public Bootstrap startInitialization()
   {
      synchronized (this)
      {
         if (deploymentManager == null)
         {
            throw new IllegalStateException("Manager has not been initialized");
         }
         
         ExtensionBeanDeployer extensionBeanDeployer = new ExtensionBeanDeployer(deploymentManager, deployment, beanDeployments);
         extensionBeanDeployer.addExtensions(deployment.getServices().get(ServiceLoaderFactory.class).load(Extension.class));
         extensionBeanDeployer.deployBeans();
         
         // Add the Deployment BeanManager Bean to the Deployment BeanManager
         deploymentManager.addBean(new ManagerBean(deploymentManager));
         
         // Re-Read the deployment structure, this will be the physical structure, and will add in BDAs for any extensions outside a physical BDA
         beanDeployments = deploymentVisitor.visit();
         
         BeforeBeanDiscoveryImpl.fire(deploymentManager, deployment, beanDeployments);
         
         // Re-Read the deployment structure, this will be the physical structure, extensions and any classes added using addAnnotatedType outside the physical BDA
         beanDeployments = deploymentVisitor.visit();
         
      }
      return this;
   }
   
   public Bootstrap deployBeans()
   {
      synchronized (this)
      {
         // TODO keep a list of new bdas, add them all in, and deploy beans for them, then merge into existing
         for (Entry<BeanDeploymentArchive, BeanDeployment> entry : beanDeployments.entrySet())
         {
            entry.getValue().deployBeans(environment);
         }
         AfterBeanDiscoveryImpl.fire(deploymentManager, deployment, beanDeployments);
         for (Entry<BeanDeploymentArchive, BeanDeployment> entry : beanDeployments.entrySet())
         {
            entry.getValue().afterBeanDiscovery(environment);
         }
         // Re-read the deployment structure, this will be the physical structure, extensions, classes, and any beans added using addBean outside the physical structure
         beanDeployments = deploymentVisitor.visit();
         Container.instance().putBeanDeployments(beanDeployments);
         Container.instance().setStatus(Status.INITIALIZED);
      }
      return this;
   }
   
   public Bootstrap validateBeans()
   {
      synchronized (this)
      {
         log.debug(VALIDATING_BEANS);
         for (Entry<BeanDeploymentArchive, BeanDeployment> entry : beanDeployments.entrySet())
         {
            deployment.getServices().get(Validator.class).validateDeployment(entry.getValue().getBeanManager(), entry.getValue().getBeanDeployer().getEnvironment());
         }
         AfterDeploymentValidationImpl.fire(deploymentManager, beanDeployments);
      }
      return this;
   }

   public Bootstrap endInitialization()
   {
      // TODO rebuild the manager accessibility graph if the bdas have changed
      synchronized (this)
      {
         // Register the managers so external requests can handle them
         Container.instance().setStatus(Status.VALIDATED);
      }
      return this;
   }
   
   protected void initializeContexts()
   {
      Lifecycle lifecycle = deployment.getServices().get(ContextLifecycle.class);
      deploymentManager.addContext(lifecycle.getDependentContext());
      deploymentManager.addContext(lifecycle.getRequestContext());
      deploymentManager.addContext(lifecycle.getConversationContext());
      deploymentManager.addContext(lifecycle.getSessionContext());
      deploymentManager.addContext(lifecycle.getApplicationContext());
      deploymentManager.addContext(lifecycle.getSingletonContext());
   }
   
   protected void createContexts()
   {
      AbstractApplicationContext applicationContext = new ApplicationContext();
      AbstractApplicationContext singletonContext = new SingletonContext();
      SessionContext sessionContext = new SessionContext();
      ConversationContext conversationContext = new ConversationContext();
      RequestContext requestContext = new RequestContext();
      DependentContext dependentContext = new DependentContext();
      
      deployment.getServices().add(ContextLifecycle.class, new ContextLifecycle(applicationContext, singletonContext, sessionContext, conversationContext, requestContext, dependentContext));
   }
   
   public void shutdown()
   {
      try
      {
         BeforeShutdownImpl.fire(deploymentManager);
      }
      finally
      {
         Container.instance().setStatus(Status.SHUTDOWN);
         Container.instance().deploymentServices().get(ContextLifecycle.class).endApplication();
      }
   }
   
   protected static void verifyServices(ServiceRegistry services, Set<Class<? extends Service>> requiredServices) 
   {
      for (Class<? extends Service> serviceType : requiredServices)
      {
         if (!services.contains(serviceType))
         {
            throw new IllegalStateException("Required service " + serviceType.getName() + " has not been specified");
         }
      }
   }

}
