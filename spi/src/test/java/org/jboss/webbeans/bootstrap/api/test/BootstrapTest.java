package org.jboss.webbeans.bootstrap.api.test;

import org.jboss.webbeans.bootstrap.api.Environments;
import org.jboss.webbeans.bootstrap.api.helpers.AbstractBootstrap;
import org.jboss.webbeans.bootstrap.spi.WebBeanDiscovery;
import org.jboss.webbeans.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.messaging.spi.JmsServices;
import org.jboss.webbeans.persistence.spi.JpaServices;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceServices;
import org.jboss.webbeans.transaction.spi.TransactionServices;
import org.jboss.webbeans.ws.spi.WebServices;
import org.testng.annotations.Test;

public class BootstrapTest
{
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingWBDiscovery()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.SE);
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.initialize();
   }
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingEjbServices()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(WebBeanDiscovery.class, new MockWebBeanDiscovery());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.getServices().add(WebServices.class, new MockWebServices());
      bootstrap.getServices().add(JmsServices.class, new MockJmsServices());
      bootstrap.initialize();
   }
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingJpaServices()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(WebBeanDiscovery.class, new MockWebBeanDiscovery());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.getServices().add(WebServices.class, new MockWebServices());
      bootstrap.getServices().add(JmsServices.class, new MockJmsServices());
      bootstrap.initialize();
   }
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingWebServices()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(WebBeanDiscovery.class, new MockWebBeanDiscovery());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(JmsServices.class, new MockJmsServices());
      bootstrap.initialize();
   }
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingJmsServices()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(WebBeanDiscovery.class, new MockWebBeanDiscovery());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(WebServices.class, new MockWebServices());
      bootstrap.initialize();
   }
   
   
   @Test
   public void testEEEnv()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(WebBeanDiscovery.class, new MockWebBeanDiscovery());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.getServices().add(WebServices.class, new MockWebServices());
      bootstrap.getServices().add(JmsServices.class, new MockJmsServices());
      bootstrap.initialize();
   }
   
   @Test
   public void testEEWebProfileEnv()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE_WEB_PROFILE);
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(WebBeanDiscovery.class, new MockWebBeanDiscovery());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.initialize();
   }
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingTxServices()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices()); 
      bootstrap.getServices().add(WebBeanDiscovery.class, new MockWebBeanDiscovery());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.initialize();
   }
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingResourceServices()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices()); 
      bootstrap.getServices().add(WebBeanDiscovery.class, new MockWebBeanDiscovery());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.initialize();
   }
   
   @Test
   public void testSEEnv()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.SE);
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.getServices().add(WebBeanDiscovery.class, new MockWebBeanDiscovery());      
      bootstrap.initialize();
   }
   
   @Test
   public void testServletEnv()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.SERVLET);
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(WebBeanDiscovery.class, new MockWebBeanDiscovery());      
      bootstrap.initialize();
   }
   
}
