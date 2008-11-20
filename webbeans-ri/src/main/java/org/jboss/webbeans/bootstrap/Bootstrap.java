package org.jboss.webbeans.bootstrap;

import static org.jboss.webbeans.util.BeanFactory.createEnterpriseBean;
import static org.jboss.webbeans.util.BeanFactory.createProducerMethodBean;
import static org.jboss.webbeans.util.BeanFactory.createSimpleBean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.jboss.webbeans.ManagerImpl;
import org.jboss.webbeans.bean.AbstractBean;
import org.jboss.webbeans.bean.AbstractClassBean;
import org.jboss.webbeans.bean.ProducerMethodBean;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.introspector.AnnotatedMethod;
import org.jboss.webbeans.log.LogProvider;
import org.jboss.webbeans.log.Logging;

public class Bootstrap
{
   
   public static String WEB_BEAN_DISCOVERY_PROPERTY_NAME = "org.jboss.webbeans.bootstrap.webBeanDiscovery";
   
   private static LogProvider log = Logging.getLogProvider(Bootstrap.class);
   
   private ManagerImpl manager;
   
   public Bootstrap()
   {
      this(new ManagerImpl());
   }
   
   protected Bootstrap(ManagerImpl manager)
   {
      this.manager = manager;
   }
   
   /**
    * Register any beans defined by the provided classes with the manager
    */
   public void registerBeans(Class<?>...classes)
   {
      registerBeans(new HashSet<Class<?>>(Arrays.asList(classes)));
   }
   
   public void registerBeans(Iterable<Class<?>> classes)
   {
      Set<AbstractBean<?, ?>> beans = createBeans(classes);
      manager.setBeans(beans);
      manager.getResolver().resolveInjectionPoints();
   }
   
   /**
    * Discover any beans defined by the provided classes
    * 
    * Beans discovered are not registered with the manager
    */
   public Set<AbstractBean<?, ?>> createBeans(Class<?>... classes)
   {
      return createBeans(new HashSet<Class<?>>(Arrays.asList(classes)));
   }
   
   public Set<AbstractBean<?, ?>> createBeans(Iterable<Class<?>> classes)
   {
      Set<AbstractBean<?, ?>> beans = new HashSet<AbstractBean<?, ?>>();
      for (Class<?> clazz : classes)
      {
         AbstractClassBean<?> bean;
         if (manager.getMetaDataCache().getEjbMetaData(clazz).isEjb())
         {
            bean = createEnterpriseBean(clazz, manager);
         }
         else
         {
            bean = createSimpleBean(clazz, manager);
         }
         beans.add(bean);
         manager.getResolver().addInjectionPoints(bean.getInjectionPoints());
         for (AnnotatedMethod<Object> producerMethod : bean.getProducerMethods())
         {
            ProducerMethodBean<?> producerMethodBean = createProducerMethodBean(producerMethod.getType(), producerMethod, manager, bean);
            beans.add(producerMethodBean);
            manager.getResolver().addInjectionPoints(producerMethodBean.getInjectionPoints());
         }
         log.info("Web Bean: " + bean);
      }
      return beans;
   }

   public void boot(WebBeanDiscovery webBeanDiscovery)
   {
      log.info("Starting Web Beans RI " + getVersion());
      if (webBeanDiscovery == null)
      {
         throw new IllegalStateException("No WebBeanDiscovery provider found, you need to implement the org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery interface, and tell the RI to use it by specifying -D" + Bootstrap.WEB_BEAN_DISCOVERY_PROPERTY_NAME + "=<classname>");
      }
      registerBeans(webBeanDiscovery.discoverWebBeanClasses());
   }
   
   public static String getVersion()
   {
      Package pkg = Bootstrap.class.getPackage();
      return pkg != null ? pkg.getImplementationVersion() : null;      
   }
   
   
   public static Set<Class<? extends WebBeanDiscovery>> getWebBeanDiscoveryClasses()
   {
      Set<Class<? extends WebBeanDiscovery>> webBeanDiscoveryClasses = new HashSet<Class<? extends WebBeanDiscovery>>();
      for (String className : new DeploymentProperties(Thread.currentThread().getContextClassLoader()).getPropertyValues(WEB_BEAN_DISCOVERY_PROPERTY_NAME))
      {
         Class<WebBeanDiscovery> webBeanDiscoveryClass = null;
         try
         {
            webBeanDiscoveryClasses.add((Class<WebBeanDiscovery>) Class.forName(className));
         }
         catch (ClassNotFoundException e) 
         {
            log.debug("Unable to load WebBeanDiscovery provider " + className, e);
         }
         catch (NoClassDefFoundError e) {
            log.warn("Unable to load WebBeanDiscovery provider " + className + " due classDependencyProblem", e);
         }
      }
      return webBeanDiscoveryClasses;
   }
   
   
   
}
