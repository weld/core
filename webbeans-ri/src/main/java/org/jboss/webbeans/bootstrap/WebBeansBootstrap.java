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



import org.jboss.webbeans.BeanValidator;
import org.jboss.webbeans.CurrentManager;
import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.standard.InjectionPointBean;
import org.jboss.webbeans.bean.standard.ManagerBean;
import org.jboss.webbeans.bootstrap.spi.EjbDiscovery;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.ejb.spi.EjbResolver;
import org.jboss.webbeans.literal.DeployedLiteral;
import org.jboss.webbeans.literal.InitializedLiteral;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.resources.spi.NamingContext;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.transaction.Transaction;

/**
 * Common bootstrapping functionality that is run at application startup and
 * detects and register beans
 * 
 * @author Pete Muir
 */
public abstract class WebBeansBootstrap
{
  
   // The log provider
   private static LogProvider log = Logging.getLogProvider(WebBeansBootstrap.class);

   // The Web Beans manager
   private ManagerImpl manager;

   protected void initManager(NamingContext namingContext, EjbResolver ejbResolver, ResourceLoader resourceLoader)
   {
      this.manager = new ManagerImpl(namingContext, ejbResolver, resourceLoader);
      manager.getNaming().bind(ManagerImpl.JNDI_KEY, getManager());
      CurrentManager.setRootManager(manager);
   }

   public ManagerImpl getManager()
   {
      return manager;
   }

   protected abstract WebBeanDiscovery getWebBeanDiscovery();
   
   protected abstract EjbDiscovery getEjbDiscovery();

   public abstract ResourceLoader getResourceLoader();

   protected void validateBootstrap()
   {
      if (getManager() == null)
      {
         throw new IllegalStateException("getManager() is not set on bootstrap");
      }
      if (getWebBeanDiscovery() == null)
      {
         throw new IllegalStateException("WebBeanDiscovery plugin not set on bootstrap");
      }
      if (getResourceLoader() == null)
      {
         throw new IllegalStateException("ResourceLoader plugin not set on bootstrap");
      }
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
      beanDeployer.deploy();
   }


   /**
    * Starts the boot process.
    * 
    * Discovers the beans and registers them with the getManager(). Also
    * resolves the injection points.
    * 
    * @param webBeanDiscovery The discovery implementation
    */
   public void boot()
   {
      synchronized (this)
      {
         log.info("Starting Web Beans RI " + getVersion());
         validateBootstrap();
         // Must populate EJB cache first, as we need it to detect whether a
         // bean is an EJB!
         getManager().getEjbDescriptorCache().addAll(getEjbDiscovery().discoverEjbs());
         registerBeans(getWebBeanDiscovery().discoverWebBeanClasses());
         getManager().fireEvent(getManager(), new InitializedLiteral());
         log.info("Web Beans initialized. Validating beans.");
         getManager().getResolver().resolveInjectionPoints();
         new BeanValidator(manager).validate();
         getManager().fireEvent(getManager(), new DeployedLiteral());
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

}
