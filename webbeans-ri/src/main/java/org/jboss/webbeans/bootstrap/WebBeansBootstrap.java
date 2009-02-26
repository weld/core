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
import java.util.List;

import org.jboss.webbeans.BeanValidator;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.standard.InjectionPointBean;
import org.jboss.webbeans.bean.standard.ManagerBean;
import org.jboss.webbeans.bootstrap.api.Bootstrap;
import org.jboss.webbeans.bootstrap.api.helpers.AbstractBootstrap;
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
import org.jboss.webbeans.literal.DeployedLiteral;
import org.jboss.webbeans.literal.InitializedLiteral;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.resources.DefaultNamingContext;
import org.jboss.webbeans.resources.DefaultResourceLoader;
import org.jboss.webbeans.servlet.HttpSessionManager;
import org.jboss.webbeans.transaction.Transaction;

/**
 * Common bootstrapping functionality that is run at application startup and
 * detects and register beans
 * 
 * @author Pete Muir
 */
public class WebBeansBootstrap extends AbstractBootstrap implements Bootstrap
{
  
   // The log provider
   private static LogProvider log = Logging.getLogProvider(WebBeansBootstrap.class);

   // The Web Beans manager
   private ManagerImpl manager;
   public WebBeansBootstrap()
   {
      setResourceLoader(new DefaultResourceLoader());
      setNamingContext(new DefaultNamingContext());
   }

   public void initialize()
   {
      if (getResourceLoader() == null)
      {
         throw new IllegalStateException("ResourceLoader not set");
      }
      if (getNamingContext() == null)
      {
         throw new IllegalStateException("NamingContext is not set");
      }
      if (getEjbResolver() == null)
      {
         throw new IllegalStateException("EjbResolver is not set");
      }
      this.manager = new ManagerImpl(getNamingContext(), getEjbResolver(), getResourceLoader());
      getManager().getNaming().bind(ManagerImpl.JNDI_KEY, getManager());
      CurrentManager.setRootManager(manager);
      initializeContexts();
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
   protected void registerBeans(Iterable<Class<?>> classes)
   {
      BeanDeployer beanDeployer = new BeanDeployer(manager);
      beanDeployer.addClasses(classes);
      beanDeployer.addBean(ManagerBean.of(manager));
      beanDeployer.addBean(InjectionPointBean.of(manager));
      beanDeployer.addClass(Transaction.class);
      beanDeployer.addClass(ConversationImpl.class);
      beanDeployer.addClass(ServletConversationManager.class);
      beanDeployer.addClass(JavaSEConversationTerminator.class);
      beanDeployer.addClass(NumericConversationIdGenerator.class);
      beanDeployer.addClass(HttpSessionManager.class);
      beanDeployer.deploy();
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
         if (getWebBeanDiscovery() == null)
         {
            throw new IllegalStateException("WebBeanDiscovery not set");
         }
         if (getEjbDiscovery() == null)
         {
            throw new IllegalStateException("EjbDiscovery is not set");
         }
         if (getResourceLoader() == null)
         {
            throw new IllegalStateException("ResourceLoader not set");
         }
         if (getApplicationContext() == null)
         {
            throw new IllegalStateException("No application context BeanStore set");
         }
         beginApplication(getApplicationContext());
         BeanStore requestBeanStore = new ConcurrentHashMapBeanStore();
         beginDeploy(requestBeanStore);
         // Must populate EJB cache first, as we need it to detect whether a
         // bean is an EJB!
         manager.getEjbDescriptorCache().addAll(getEjbDiscovery().discoverEjbs());
         BeansXmlParser parser = new BeansXmlParser(getResourceLoader(), getWebBeanDiscovery().discoverWebBeansXml());
         parser.parse();
         List<Class<? extends Annotation>> enabledDeploymentTypes = parser.getEnabledDeploymentTypes();
         if (enabledDeploymentTypes != null)
         {
            manager.setEnabledDeploymentTypes(enabledDeploymentTypes);
         }
         log.info("Deployment types: " + manager.getEnabledDeploymentTypes());
         registerBeans(getWebBeanDiscovery().discoverWebBeanClasses());
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
      ApplicationContext.INSTANCE.setBeanStore(applicationBeanStore);
      ApplicationContext.INSTANCE.setActive(true);

   }

   protected void beginDeploy(BeanStore requestBeanStore)
   {
      RequestContext.INSTANCE.setBeanStore(requestBeanStore);
      RequestContext.INSTANCE.setActive(true);
   }

   protected void endDeploy(BeanStore requestBeanStore)
   {
      RequestContext.INSTANCE.setBeanStore(null);
      RequestContext.INSTANCE.setActive(false);
   }

   protected void endApplication(BeanStore applicationBeanStore)
   {
      log.trace("Ending application");
      ApplicationContext.INSTANCE.destroy();
      ApplicationContext.INSTANCE.setActive(false);
      ApplicationContext.INSTANCE.setBeanStore(null);
   }

}
