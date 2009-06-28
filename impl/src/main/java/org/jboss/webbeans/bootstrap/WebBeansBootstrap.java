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

import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.DefinitionException;
import org.jboss.webbeans.DeploymentException;
import org.jboss.webbeans.Validator;
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
import org.jboss.webbeans.context.ContextLifecycle;
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
import org.jboss.webbeans.jsf.JsfApiAbstraction;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.messaging.spi.JmsServices;
import org.jboss.webbeans.metadata.TypeStore;
import org.jboss.webbeans.metadata.cache.MetaAnnotationStore;
import org.jboss.webbeans.persistence.PersistenceApiAbstraction;
import org.jboss.webbeans.persistence.spi.JpaServices;
import org.jboss.webbeans.resources.ClassTransformer;
import org.jboss.webbeans.resources.DefaultResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceServices;
import org.jboss.webbeans.servlet.HttpSessionManager;
import org.jboss.webbeans.servlet.ServletApiAbstraction;
import org.jboss.webbeans.transaction.spi.TransactionServices;
import org.jboss.webbeans.util.serviceProvider.ServiceLoader;
import org.jboss.webbeans.ws.spi.WebServices;
import org.jboss.webbeans.xml.BeansXmlParser;

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
   
   static
   {
	   log.info("Web Beans " + getVersion());
   }

   // The Web Beans manager
   private BeanManagerImpl manager;
   
   public WebBeansBootstrap()
   {
      // initialize default services
      getServices().add(ResourceLoader.class, new DefaultResourceLoader());
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
         log.info("EJB services not available. Session beans will be simple beans, CDI-style injection into non-contextual EJBs, injection of remote EJBs and injection of @EJB in simple beans will not be available");
      }
      if (!getServices().contains(JmsServices.class))
      {
         log.info("JMS services not available. JMS resources will not be available.");
      }
      if (!getServices().contains(JpaServices.class))
      {
         log.info("JPA services not available. Injection of @PersistenceContext will not occur. Entity beans will be discovered as simple beans.");
      }
      if (!getServices().contains(ResourceServices.class))
      {
         log.info("@Resource injection not available.");
      }
      if (!getServices().contains(WebServices.class))
      {
         log.info("WebService reference injection not available.");
      }
      addImplementationServices();
      createContexts();
      this.manager = BeanManagerImpl.newRootManager(ServiceRegistries.unmodifiableServiceRegistry(getServices()));
      CurrentManager.setRootManager(manager);
      initializeContexts();
   }
   
   private void addImplementationServices()
   {
      ResourceLoader resourceLoader = getServices().get(ResourceLoader.class);
      getServices().add(EJBApiAbstraction.class, new EJBApiAbstraction(resourceLoader));
      getServices().add(JsfApiAbstraction.class, new JsfApiAbstraction(resourceLoader));
      getServices().add(PersistenceApiAbstraction.class, new PersistenceApiAbstraction(resourceLoader));
      getServices().add(ServletApiAbstraction.class, new ServletApiAbstraction(resourceLoader));
      // Temporary workaround to provide context for building annotated class
      // TODO expose AnnotatedClass on SPI and allow container to provide impl of this via ResourceLoader
      getServices().add(Validator.class, new Validator());
      getServices().add(TypeStore.class, new TypeStore());
      getServices().add(ClassTransformer.class, new ClassTransformer(getServices().get(TypeStore.class)));
      getServices().add(MetaAnnotationStore.class, new MetaAnnotationStore(getServices().get(ClassTransformer.class)));
   }
   
   public BeanManagerImpl getManager()
   {
      return manager;
   }

   /**
    * Register the bean with the getManager(), including any standard (built in)
    * beans
    * 
    * @param classes The classes to register as Web Beans
    */
   protected void registerBeans(Iterable<Class<?>> classes, BeanDeployer beanDeployer)
   {
      beanDeployer.addClasses(classes);
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
      beanDeployer.createBeans().deploy();
   }
   
   private void registerExtensionBeans(Iterable<Extension> instances, AbstractBeanDeployer beanDeployer)
   {
      
   }
   
   public void boot()
   {
      synchronized (this)
      {
         if (manager == null)
         {
            throw new IllegalStateException("Manager has not been initialized");
         }
         if (getApplicationContext() == null)
         {
            throw new IllegalStateException("No application context BeanStore set");
         }
         
         parseBeansXml();
         
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
         
         // TODO Should use a separate event manager for sending bootstrap events
         ExtensionBeanDeployer extensionBeanDeployer = new ExtensionBeanDeployer(manager);
         extensionBeanDeployer.addExtensions(ServiceLoader.load(Extension.class));
         extensionBeanDeployer.createBeans().deploy();
         
         BeanDeployer beanDeployer = new BeanDeployer(manager, ejbDescriptors);
         
         fireBeforeBeanDiscoveryEvent(beanDeployer);
         registerBeans(getServices().get(WebBeanDiscovery.class).discoverWebBeanClasses(), beanDeployer);
         fireAfterBeanDiscoveryEvent();
         log.debug("Web Beans initialized. Validating beans.");
         getServices().get(Validator.class).validateDeployment(manager, beanDeployer.getBeanDeployerEnvironment());
         // TODO I don't really think this is needed anymore, as we validate all points
         manager.getResolver().resolveInjectionPoints();
         fireAfterDeploymentValidationEvent();
         endDeploy(requestBeanStore);
      }
   }
   
   private void parseBeansXml()
   {
      BeansXmlParser parser = new BeansXmlParser(getServices().get(ResourceLoader.class), getServices().get(WebBeanDiscovery.class).discoverWebBeansXml());
      parser.parse();
      
      if (parser.getEnabledDeploymentTypes() != null)
      {
         manager.setEnabledDeploymentTypes(parser.getEnabledDeploymentTypes());
      }
      if (parser.getEnabledDecoratorClasses() != null)
      {
         manager.setEnabledDecoratorClasses(parser.getEnabledDecoratorClasses());
      }
      if (parser.getEnabledInterceptorClasses() != null)
      {
         manager.setEnabledInterceptorClasses(parser.getEnabledInterceptorClasses());
      }
      log.debug("Enabled deployment types: " + manager.getEnabledDeploymentTypes());
      log.debug("Enabled decorator types: " + manager.getEnabledDecoratorClasses());
      log.debug("Enabled interceptor types: " + manager.getEnabledInterceptorClasses());
   }

   private void fireBeforeBeanDiscoveryEvent(BeanDeployer beanDeployer)
   {
      BeforeBeanDiscoveryImpl event = new BeforeBeanDiscoveryImpl(getManager(), beanDeployer);
      try
      {
         getManager().fireEvent(event);
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
         getManager().fireEvent(event);
      }
      catch (Exception e)
      {
         throw new DeploymentException(e);
      }
   }
   
   private void fireAfterBeanDiscoveryEvent()
   {
      AfterBeanDiscoveryImpl event = new AfterBeanDiscoveryImpl(getManager());
      try
      {
         manager.fireEvent(event);
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
         manager.fireEvent(event);
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
      manager.addContext(DependentContext.instance());
      manager.addContext(RequestContext.instance());
      manager.addContext(ConversationContext.instance());
      manager.addContext(SessionContext.instance());
      manager.addContext(ApplicationContext.instance());
   }
   
   protected void createContexts()
   {
      getServices().add(ContextLifecycle.class, new ContextLifecycle());
      getServices().add(DependentContext.class, new DependentContext());
      getServices().add(RequestContext.class, new RequestContext());
      getServices().add(ConversationContext.class, new ConversationContext());
      getServices().add(SessionContext.class, new SessionContext());
      getServices().add(ApplicationContext.class, new ApplicationContext());
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
      try
      {
         fireBeforeShutdownEvent();
      }
      finally
      {
         manager.shutdown();
      }
   }

}
