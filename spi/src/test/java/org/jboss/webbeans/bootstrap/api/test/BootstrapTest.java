package org.jboss.webbeans.bootstrap.api.test;

import org.jboss.webbeans.bootstrap.api.Environments;
import org.jboss.webbeans.bootstrap.api.helpers.AbstractBootstrap;
import org.jboss.webbeans.bootstrap.spi.Deployment;
import org.jboss.webbeans.context.api.helpers.ConcurrentHashMapBeanStore;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.persistence.spi.JpaServices;
import org.jboss.webbeans.persistence.spi.helpers.JSFServices;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceServices;
import org.jboss.webbeans.security.spi.SecurityServices;
import org.jboss.webbeans.servlet.api.ServletServices;
import org.jboss.webbeans.transaction.spi.TransactionServices;
import org.jboss.webbeans.validation.spi.ValidationServices;
import org.testng.annotations.Test;

public class BootstrapTest
{
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingDeployment()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.SE);
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.startContainer();
   }
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingEjbServices()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(Deployment.class, new MockDeployment());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.getServices().add(SecurityServices.class, new MockSecurityServices());
      bootstrap.getServices().add(ValidationServices.class, new MockValidationServices());
      bootstrap.getServices().add(ServletServices.class, new MockServletServices());
      bootstrap.getServices().add(JSFServices.class, new MockJSFServices());
      bootstrap.startContainer();
   }
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingJpaServices()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(Deployment.class, new MockDeployment());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.getServices().add(SecurityServices.class, new MockSecurityServices());
      bootstrap.getServices().add(ValidationServices.class, new MockValidationServices());
      bootstrap.getServices().add(ServletServices.class, new MockServletServices());
      bootstrap.getServices().add(JSFServices.class, new MockJSFServices());
      bootstrap.startContainer();
   }
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingSecurityServices()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(Deployment.class, new MockDeployment());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.getServices().add(ValidationServices.class, new MockValidationServices());
      bootstrap.getServices().add(ServletServices.class, new MockServletServices());
      bootstrap.getServices().add(JSFServices.class, new MockJSFServices());
      bootstrap.startContainer();
   }
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingValidationServices()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(Deployment.class, new MockDeployment());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.getServices().add(SecurityServices.class, new MockSecurityServices());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(ServletServices.class, new MockServletServices());
      bootstrap.getServices().add(JSFServices.class, new MockJSFServices());
      bootstrap.startContainer();
   }
   
   @Test
   public void testEEEnv()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(Deployment.class, new MockDeployment());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.getServices().add(SecurityServices.class, new MockSecurityServices());
      bootstrap.getServices().add(ValidationServices.class, new MockValidationServices());
      bootstrap.getServices().add(ServletServices.class, new MockServletServices());
      bootstrap.getServices().add(JSFServices.class, new MockJSFServices());
      bootstrap.startContainer();
   }
   
   @Test
   public void testEEWebProfileEnv()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE_WEB_PROFILE);
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(Deployment.class, new MockDeployment());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.getServices().add(SecurityServices.class, new MockSecurityServices());
      bootstrap.getServices().add(ValidationServices.class, new MockValidationServices());
      bootstrap.getServices().add(ServletServices.class, new MockServletServices());
      bootstrap.getServices().add(JSFServices.class, new MockJSFServices());
      bootstrap.startContainer();
   }
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingTxServices()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices()); 
      bootstrap.getServices().add(Deployment.class, new MockDeployment());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.getServices().add(SecurityServices.class, new MockSecurityServices());
      bootstrap.getServices().add(ValidationServices.class, new MockValidationServices());
      bootstrap.getServices().add(ServletServices.class, new MockServletServices());
      bootstrap.getServices().add(JSFServices.class, new MockJSFServices());
      bootstrap.startContainer();
   }
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingResourceServices()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices()); 
      bootstrap.getServices().add(Deployment.class, new MockDeployment());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(SecurityServices.class, new MockSecurityServices());
      bootstrap.getServices().add(ValidationServices.class, new MockValidationServices());
      bootstrap.getServices().add(ServletServices.class, new MockServletServices());
      bootstrap.getServices().add(JSFServices.class, new MockJSFServices());
      bootstrap.startContainer();
   }
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingJSFServices()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices()); 
      bootstrap.getServices().add(Deployment.class, new MockDeployment());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(SecurityServices.class, new MockSecurityServices());
      bootstrap.getServices().add(ValidationServices.class, new MockValidationServices());
      bootstrap.getServices().add(ServletServices.class, new MockServletServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.startContainer();
   }
   
   @Test(expectedExceptions=IllegalStateException.class)
   public void testMissingServletServices()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.EE);
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(EjbServices.class, new MockEjbServices()); 
      bootstrap.getServices().add(Deployment.class, new MockDeployment());
      bootstrap.getServices().add(JpaServices.class, new MockJpaServices());
      bootstrap.getServices().add(TransactionServices.class, new MockTransactionServices());
      bootstrap.getServices().add(SecurityServices.class, new MockSecurityServices());
      bootstrap.getServices().add(ValidationServices.class, new MockValidationServices());
      bootstrap.getServices().add(ResourceServices.class, new MockResourceServices());
      bootstrap.getServices().add(JSFServices.class, new MockJSFServices());
      bootstrap.startContainer();
   }
   
   @Test
   public void testSEEnv()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.SE);
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.getServices().add(Deployment.class, new MockDeployment());      
      bootstrap.startContainer();
   }
   
   @Test
   public void testServletEnv()
   {
      AbstractBootstrap bootstrap = new MockBootstrap();
      bootstrap.setEnvironment(Environments.SERVLET);
      bootstrap.getServices().add(ResourceLoader.class, new MockResourceLoader());
      bootstrap.setApplicationContext(new ConcurrentHashMapBeanStore());
      bootstrap.getServices().add(Deployment.class, new MockDeployment());
      bootstrap.getServices().add(ServletServices.class, new MockServletServices());
      bootstrap.startContainer();
   }
   
}
