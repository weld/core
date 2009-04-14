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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;

import org.jboss.webbeans.BeanValidator;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.ee.AbstractJavaEEResourceBean;
import org.jboss.webbeans.bean.standard.EventBean;
import org.jboss.webbeans.bean.standard.InjectionPointBean;
import org.jboss.webbeans.bean.standard.InstanceBean;
import org.jboss.webbeans.bean.standard.ManagerBean;
import org.jboss.webbeans.bootstrap.api.Bootstrap;
import org.jboss.webbeans.bootstrap.api.Environments;
import org.jboss.webbeans.bootstrap.api.helpers.AbstractBootstrap;
import org.jboss.webbeans.bootstrap.api.helpers.ServiceRegistries;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.context.ApplicationContext;
import org.jboss.webbeans.context.ConversationContext;
import org.jboss.webbeans.context.DependentContext;
import org.jboss.webbeans.context.RequestContext;
import org.jboss.webbeans.context.SessionContext;
import org.jboss.webbeans.context.api.BeanStore;
import org.jboss.webbeans.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.webbeans.conversation.ConversationImpl;
import org.jboss.webbeans.conversation.JavaSEConversationTerminator;
import org.jboss.webbeans.conversation.NumericConversationIdGenerator;
import org.jboss.webbeans.conversation.ServletConversationManager;
import org.jboss.webbeans.ejb.EJBApiAbstraction;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.introspector.AnnotatedClass;
import org.jboss.webbeans.jsf.JSFApiAbstraction;
import org.jboss.webbeans.literal.DeployedLiteral;
import org.jboss.webbeans.literal.InitializedLiteral;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.metadata.MetaDataCache;
import org.jboss.webbeans.persistence.DefaultEntityDiscovery;
import org.jboss.webbeans.persistence.PersistenceApiAbstraction;
import org.jboss.webbeans.persistence.spi.EntityDiscovery;
import org.jboss.webbeans.persistence.spi.JpaServices;
import org.jboss.webbeans.resources.ClassTransformer;
import org.jboss.webbeans.resources.DefaultResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceServices;
import org.jboss.webbeans.servlet.HttpSessionManager;
import org.jboss.webbeans.servlet.ServletApiAbstraction;
import org.jboss.webbeans.transaction.spi.TransactionServices;
import org.jboss.webbeans.xml.XmlEnvironment;
import org.jboss.webbeans.xml.XmlParser;

/**
 * Common bootstrapping functionality that is run at application startup and
 * detects and register beans
 * 
 * @author Pete Muir
 */
public class WebBeansBootstrap extends AbstractBootstrap implements Bootstrap
{
  
   // The log provider
   private static Log log = Logging.getLog(WebBeansBootstrap.class);

   // The Web Beans manager
   private ManagerImpl manager;
   public WebBeansBootstrap()
   {
      // initialize default services
      getServices().add(ResourceLoader.class, new DefaultResourceLoader());
      getServices().add(EntityDiscovery.class, new DefaultEntityDiscovery(getServices()));
   }

   public void initialize()
   {
      verify();
      if (!getServices().contains(TransactionServices.class))
      {
         log.info("Transactional services not available.  Transactional observers will be invoked synchronously.");
      }
      if (!getServices().contains(EjbServices.class))
      {
         log.info("EJB services not available. Session beans will be simple beans, injection into non-contextual EJBs, injection of @EJB in simple beans, injection of Java EE resources and JMS resources will not be available.");
      }
      if (!getServices().contains(JpaServices.class))
      {
         log.info("JPA services not available. Injection of @PersistenceContext will not occur. Entity beans will be discovered as simple beans.");
      }
      if (!getServices().contains(ResourceServices.class))
      {
         log.info("@Resource injection not available.");
      }
      addImplementationServices();
      this.manager = ManagerImpl.newRootManager(ServiceRegistries.unmodifiableServiceRegistry(getServices()));
      CurrentManager.setRootManager(manager);
      initializeContexts();
   }
   
   private void addImplementationServices()
   {
      ResourceLoader resourceLoader = getServices().get(ResourceLoader.class);
      getServices().add(EJBApiAbstraction.class, new EJBApiAbstraction(resourceLoader));
      getServices().add(JSFApiAbstraction.class, new JSFApiAbstraction(resourceLoader));
      getServices().add(PersistenceApiAbstraction.class, new PersistenceApiAbstraction(resourceLoader));
      getServices().add(ServletApiAbstraction.class, new ServletApiAbstraction(resourceLoader));
      // Temporary workaround to provide context for building annotated class
      // TODO expose AnnotatedClass on SPI and allow container to provide impl of this via ResourceLoader
      getServices().add(ClassTransformer.class, new ClassTransformer());
      getServices().add(MetaDataCache.class, new MetaDataCache(getServices().get(ClassTransformer.class)));
   }
   
   public ManagerImpl getManager()
   {
      return manager;
   }

   /**
    * Register the bean with the getManager(), including any standard (built in)
    * beans
    * 
    * @param classes The classes to register as Web Beans
    */
   protected void registerBeans(Iterable<Class<?>> classes, Collection<AnnotatedClass<?>> xmlClasses, Iterable<AbstractJavaEEResourceBean<?>> resourceBeans, EjbDescriptorCache ejbDescriptors)
   {
      BeanDeployer beanDeployer = new BeanDeployer(manager, ejbDescriptors);
      beanDeployer.addClasses(classes);
      beanDeployer.addClasses(xmlClasses);
      beanDeployer.addBean(ManagerBean.of(manager));
      beanDeployer.addBean(InjectionPointBean.of(manager));
      beanDeployer.addBean(EventBean.of(manager));
      beanDeployer.addBean(InstanceBean.of(manager));
      if (!getEnvironment().equals(Environments.SE))
      {
         beanDeployer.addClass(ConversationImpl.class);
         beanDeployer.addClass(ServletConversationManager.class);
         beanDeployer.addClass(JavaSEConversationTerminator.class);
         beanDeployer.addClass(NumericConversationIdGenerator.class);
         beanDeployer.addClass(HttpSessionManager.class);
      }
      beanDeployer.addBeans(resourceBeans);
      beanDeployer.createBeans().deploy();
   }
   
   public void boot()
   {
      synchronized (this)
      {
         log.info("Starting Web Beans RI " + getVersion());
         if (manager == null)
         {
            throw new IllegalStateException("Manager has not been initialized");
         }
         if (getApplicationContext() == null)
         {
            throw new IllegalStateException("No application context BeanStore set");
         }
         beginApplication(getApplicationContext());
         BeanStore requestBeanStore = new ConcurrentHashMapBeanStore();
         beginDeploy(requestBeanStore);
         EjbDescriptorCache ejbDescriptors = new EjbDescriptorCache();
         if (getServices().contains(EjbServices.class))
         {
            // Must populate EJB cache first, as we need it to detect whether a
            // bean is an EJB!
            ejbDescriptors.addAll(getServices().get(EjbServices.class).discoverEjbs());
         }
         
         XmlEnvironment xmlEnvironmentImpl = new XmlEnvironment(getServices(), ejbDescriptors, manager);
         XmlParser parser = new XmlParser(xmlEnvironmentImpl);
         parser.parse();
          
         List<Class<? extends Annotation>> enabledDeploymentTypes = xmlEnvironmentImpl.getEnabledDeploymentTypes();
         if (enabledDeploymentTypes.size() > 0)
         {
            manager.setEnabledDeploymentTypes(enabledDeploymentTypes);
         }
         log.info("Deployment types: " + manager.getEnabledDeploymentTypes());
         registerBeans(getServices().get(WebBeanDiscovery.class).discoverWebBeanClasses(), xmlEnvironmentImpl.getClasses(), xmlEnvironmentImpl.getResourceBeans(), ejbDescriptors);
         manager.fireEvent(manager, new InitializedLiteral());
         log.info("Web Beans initialized. Validating beans.");
         manager.getResolver().resolveInjectionPoints();
         new BeanValidator(manager).validate();
         manager.fireEvent(manager, new DeployedLiteral());
         endDeploy(requestBeanStore);
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
      manager.addContext(DependentContext.create());
      manager.addContext(RequestContext.create());
      manager.addContext(SessionContext.create());
      manager.addContext(ApplicationContext.create());
      manager.addContext(ConversationContext.create());
   }

   protected void beginApplication(BeanStore applicationBeanStore)
   {
      log.trace("Starting application");
      ApplicationContext.instance().setBeanStore(applicationBeanStore);
      ApplicationContext.instance().setActive(true);

   }

   protected void beginDeploy(BeanStore requestBeanStore)
   {
      RequestContext.instance().setBeanStore(requestBeanStore);
      RequestContext.instance().setActive(true);
   }

   protected void endDeploy(BeanStore requestBeanStore)
   {
      RequestContext.instance().setBeanStore(null);
      RequestContext.instance().setActive(false);
   }
   
   public void shutdown()
   {
      manager.shutdown();
   }

}
