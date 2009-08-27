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
package org.jboss.webbeans.bootstrap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.ContextualIdStore;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.DeploymentException;
import org.jboss.webbeans.Validator;
import org.jboss.webbeans.bean.builtin.ManagerBean;
import org.jboss.webbeans.bootstrap.api.Bootstrap;
import org.jboss.webbeans.bootstrap.api.Environment;
import org.jboss.webbeans.bootstrap.api.Service;
import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.bootstrap.api.helpers.ServiceRegistries;
import org.jboss.webbeans.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.webbeans.bootstrap.spi.Deployment;
import org.jboss.webbeans.context.ApplicationContext;
import org.jboss.webbeans.context.ContextLifecycle;
import org.jboss.webbeans.context.ConversationContext;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.RequestContext;
import org.jboss.webbeans.context.SessionContext;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.ejb.EJBApiAbstraction;
import org.jboss.webbeans.jsf.JsfApiAbstraction;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.metadata.TypeStore;
import org.jboss.webbeans.metadata.cache.MetaAnnotationStore;
import org.jboss.webbeans.persistence.PersistenceApiAbstraction;
import org.jboss.webbeans.resources.ClassTransformer;
import org.jboss.webbeans.resources.DefaultResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.servlet.ServletApiAbstraction;
import org.jboss.webbeans.transaction.spi.TransactionServices;
import org.jboss.webbeans.util.serviceProvider.ServiceLoader;

/**
 * Common bootstrapping functionality that is run at application startup and
 * detects and register beans
 * 
 * @author Pete Muir
 */
public class WebBeansBootstrap implements Bootstrap
{
   
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
      
      public DeploymentVisitor(BeanManagerImpl deploymentManager, Environment environment, Deployment deployment)
      {
         this.deploymentManager = deploymentManager;
         this.environment = environment;
         this.deployment = deployment;
      }
      
      public Map<BeanDeploymentArchive, BeanDeployment> visit()
      {
         Set<BeanDeploymentArchive> seenBeanDeploymentArchives = new HashSet<BeanDeploymentArchive>();
         Map<BeanDeploymentArchive, BeanDeployment> managerAwareBeanDeploymentArchives = new HashMap<BeanDeploymentArchive, BeanDeployment>();
         for (BeanDeploymentArchive archvive : deployment.getBeanDeploymentArchives())
         {
            visit(archvive, managerAwareBeanDeploymentArchives, seenBeanDeploymentArchives);
         }
         return managerAwareBeanDeploymentArchives;
      }
      
      private BeanDeployment visit(BeanDeploymentArchive beanDeploymentArchive, Map<BeanDeploymentArchive, BeanDeployment> managerAwareBeanDeploymentArchives, Set<BeanDeploymentArchive> seenBeanDeploymentArchives)
      {
         // Check that the required services are specified
         verifyServices(beanDeploymentArchive.getServices(), environment.getRequiredBeanDeploymentArchiveServices());
         
         // Create the BeanDeployment and attach
         BeanDeployment parent = new BeanDeployment(beanDeploymentArchive, deploymentManager);
         managerAwareBeanDeploymentArchives.put(beanDeploymentArchive, parent);
         seenBeanDeploymentArchives.add(beanDeploymentArchive);
         for (BeanDeploymentArchive archive : beanDeploymentArchive.getBeanDeploymentArchives())
         {
            // Cut any circularties
            if (!seenBeanDeploymentArchives.contains(archive))
            {
               BeanDeployment child = visit(archive, managerAwareBeanDeploymentArchives, seenBeanDeploymentArchives);
               parent.getBeanManager().addAccessibleBeanManager(child.getBeanManager());
            }
         }
         return parent;
      }
      
   }
  
   // The log provider
   private static Log log = Logging.getLog(WebBeansBootstrap.class);
   
   static
   {
	   log.info("Web Beans " + getVersion());
   }

   // The Web Beans manager
   private BeanManagerImpl deploymentManager;
   private Map<BeanDeploymentArchive, BeanDeployment> beanDeployments;
   private Environment environment;
   private Deployment deployment;
   
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
         
         verifyServices(deployment.getServices(), environment.getRequiredDeploymentServices());
         
         if (!deployment.getServices().contains(TransactionServices.class))
         {
            log.info("Transactional services not available. Injection of @Current UserTransaction not available. Transactional observers will be invoked synchronously.");
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
         addImplementationServices(deployment.getServices());
         
         
         this.environment = environment;
         this.deploymentManager = BeanManagerImpl.newRootManager(ServiceRegistries.unmodifiableServiceRegistry(deployment.getServices()));
         CurrentManager.setRootManager(deploymentManager);
         
         createContexts();
         initializeContexts();
         // Start the application context
         beginApplication(applicationBeanStore);
         
         DeploymentVisitor deploymentVisitor = new DeploymentVisitor(deploymentManager, environment, deployment);
         beanDeployments = deploymentVisitor.visit();
         
         return this;
      }
   }
   
   private void addImplementationServices(ServiceRegistry services)
   {
      ResourceLoader resourceLoader = services.get(ResourceLoader.class);
      services.add(EJBApiAbstraction.class, new EJBApiAbstraction(resourceLoader));
      services.add(JsfApiAbstraction.class, new JsfApiAbstraction(resourceLoader));
      services.add(PersistenceApiAbstraction.class, new PersistenceApiAbstraction(resourceLoader));
      services.add(ServletApiAbstraction.class, new ServletApiAbstraction(resourceLoader));
      // Temporary workaround to provide context for building annotated class
      // TODO expose AnnotatedClass on SPI and allow container to provide impl of this via ResourceLoader
      services.add(Validator.class, new Validator());
      services.add(TypeStore.class, new TypeStore());
      services.add(ClassTransformer.class, new ClassTransformer(services.get(TypeStore.class)));
      services.add(MetaAnnotationStore.class, new MetaAnnotationStore(services.get(ClassTransformer.class)));
      services.add(ContextualIdStore.class, new ContextualIdStore());
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
         
         ExtensionBeanDeployer extensionBeanDeployer = new ExtensionBeanDeployer(deploymentManager);
         extensionBeanDeployer.addExtensions(ServiceLoader.load(Extension.class));
         extensionBeanDeployer.createBeans().deploy();
         
         // Add the Deployment BeanManager Bean to the Deployment BeanManager
         deploymentManager.addBean(new ManagerBean(deploymentManager));
         
         fireBeforeBeanDiscoveryEvent();
      }
      return this;
   }
   
   public Bootstrap deployBeans()
   {
      synchronized (this)
      {
         for (Entry<BeanDeploymentArchive, BeanDeployment> entry : beanDeployments.entrySet())
         {
            entry.getValue().deployBeans(environment);
         }
         fireAfterBeanDiscoveryEvent();
         log.debug("Web Beans initialized. Validating beans.");
      }
      return this;
   }
   
   public Bootstrap validateBeans()
   {
      synchronized (this)
      {
         for (Entry<BeanDeploymentArchive, BeanDeployment> entry : beanDeployments.entrySet())
         {
            deploymentManager.getServices().get(Validator.class).validateDeployment(entry.getValue().getBeanManager(), entry.getValue().getBeanDeployer().getEnvironment());
         }
         fireAfterDeploymentValidationEvent();
      }
      return this;
   }

   public Bootstrap endInitialization()
   {
      synchronized (this)
      {
         // Register the managers so external requests can handle them
         CurrentManager.setBeanDeploymentArchives(beanDeployments);
      }
      return this;
   }

   private void fireBeforeBeanDiscoveryEvent()
   {
      BeforeBeanDiscovery event = new BeforeBeanDiscoveryImpl(deploymentManager, deployment, beanDeployments);
      try
      {
         deploymentManager.fireEvent(event);
      }
      catch (Exception e)
      {
         throw new DefinitionException(e);
      }
   }
   
   private void fireBeforeShutdownEvent()
   {
      BeforeShutdown event = new BeforeShutdownImpl();
      try
      {
         deploymentManager.fireEvent(event);
      }
      catch (Exception e)
      {
         throw new DeploymentException(e);
      }
   }
   
   private void fireAfterBeanDiscoveryEvent()
   {
      AfterBeanDiscoveryImpl event = new AfterBeanDiscoveryImpl(deploymentManager, deployment, beanDeployments);
      try
      {
         deploymentManager.fireEvent(event);
      }
      catch (Exception e)
      {
         event.addDefinitionError(e);
      }
      
      if (event.getDefinitionErrors().size() > 0)
      {
         // FIXME communicate all the captured definition errors in this exception
         throw new DefinitionException(event.getDefinitionErrors().get(0));
      }
   }
   
   private void fireAfterDeploymentValidationEvent()
   {
      AfterDeploymentValidationImpl event = new AfterDeploymentValidationImpl();
      
      try
      {
         deploymentManager.fireEvent(event);
      }
      catch (Exception e)
      {
         event.addDeploymentProblem(e);
      }
      
      if (event.getDeploymentProblems().size() > 0)
      {
         // FIXME communicate all the captured deployment problems in this exception
         throw new DeploymentException(event.getDeploymentProblems().get(0));
      }
   }

   /**
    * Gets version information
    * 
    * @return The implementation version from the Bootstrap class package.
    */
   public static String getVersion()
   {
      Package pkg = WebBeansBootstrap.class.getPackage();
      return pkg != null ? pkg.getImplementationVersion() : null;
   }
   
   protected void initializeContexts()
   {
      deploymentManager.addContext(deploymentManager.getServices().get(DependentContext.class));
      deploymentManager.addContext(deploymentManager.getServices().get(RequestContext.class));
      deploymentManager.addContext(deploymentManager.getServices().get(ConversationContext.class));
      deploymentManager.addContext(deploymentManager.getServices().get(SessionContext.class));
      deploymentManager.addContext(deploymentManager.getServices().get(ApplicationContext.class));
   }
   
   protected void createContexts()
   {
      deployment.getServices().add(ContextLifecycle.class, new ContextLifecycle());
      deployment.getServices().add(DependentContext.class, new DependentContext());
      deployment.getServices().add(RequestContext.class, new RequestContext());
      deployment.getServices().add(ConversationContext.class, new ConversationContext());
      deployment.getServices().add(SessionContext.class, new SessionContext());
      deployment.getServices().add(ApplicationContext.class, new ApplicationContext());
   }

   protected void beginApplication(BeanStore applicationBeanStore)
   {
      log.trace("Starting application");
      ApplicationContext applicationContext = deploymentManager.getServices().get(ApplicationContext.class);
      applicationContext.setBeanStore(applicationBeanStore);
      applicationContext.setActive(true);

   }
   
   public void shutdown()
   {
      try
      {
         fireBeforeShutdownEvent();
      }
      finally
      {
         deploymentManager.shutdown();
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
