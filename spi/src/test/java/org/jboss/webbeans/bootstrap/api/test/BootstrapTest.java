package org.jboss.webbeans.bootstrap.api.test;

import org.jboss.webbeans.bootstrap.api.Bootstrap;
import org.jboss.webbeans.bootstrap.api.Environments;
import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.webbeans.bootstrap.api.test.MockDeployment.MockBeanDeploymentArchive;
import org.jboss.webbeans.bootstrap.spi.Deployment;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.injection.spi.EjbInjectionServices;
import org.jboss.webbeans.injection.spi.JpaInjectionServices;
import org.jboss.webbeans.injection.spi.ResourceInjectionServices;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.security.spi.SecurityServices;
import org.jboss.webbeans.servlet.api.ServletServices;
import org.jboss.webbeans.transaction.spi.TransactionServices;
import org.jboss.webbeans.validation.spi.ValidationServices;
import org.testng.annotations.Test;

public class BootstrapTest
{

   @Test(expectedExceptions = IllegalStateException.class)
   public void testMissingEjbServices()
   {
      Bootstrap bootstrap = new MockBootstrap();
      ServiceRegistry deploymentServices = new SimpleServiceRegistry();
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      deploymentServices.add(ResourceLoader.class, new MockResourceLoader());
      deploymentServices.add(TransactionServices.class, new MockTransactionServices());
      deploymentServices.add(SecurityServices.class, new MockSecurityServices());
      deploymentServices.add(ValidationServices.class, new MockValidationServices());
      deploymentServices.add(ServletServices.class, new MockServletServices());
      
      bdaServices.add(JpaInjectionServices.class, new MockJpaServices());
      bdaServices.add(ResourceInjectionServices.class, new MockResourceServices());
      bdaServices.add(EjbInjectionServices.class, new MockEjbInjectionServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE_INJECT, deployment, null);
   }
   
   @Test(expectedExceptions = IllegalStateException.class)
   public void testMissingEjbInjectionServices()
   {
      Bootstrap bootstrap = new MockBootstrap();
      ServiceRegistry deploymentServices = new SimpleServiceRegistry();
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      deploymentServices.add(ResourceLoader.class, new MockResourceLoader());
      deploymentServices.add(TransactionServices.class, new MockTransactionServices());
      deploymentServices.add(SecurityServices.class, new MockSecurityServices());
      deploymentServices.add(ValidationServices.class, new MockValidationServices());
      deploymentServices.add(ServletServices.class, new MockServletServices());
      
      bdaServices.add(JpaInjectionServices.class, new MockJpaServices());
      bdaServices.add(ResourceInjectionServices.class, new MockResourceServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE_INJECT, deployment, null);
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testMissingJpaServices()
   {
      Bootstrap bootstrap = new MockBootstrap();
      ServiceRegistry deploymentServices = new SimpleServiceRegistry();
      deploymentServices.add(ResourceLoader.class, new MockResourceLoader());
      deploymentServices.add(TransactionServices.class, new MockTransactionServices());
      deploymentServices.add(SecurityServices.class, new MockSecurityServices());
      deploymentServices.add(ValidationServices.class, new MockValidationServices());
      deploymentServices.add(ServletServices.class, new MockServletServices());
      deploymentServices.add(EjbServices.class, new MockEjbServices());
      
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      
      bdaServices.add(EjbInjectionServices.class, new MockEjbInjectionServices());
      bdaServices.add(ResourceInjectionServices.class, new MockResourceServices());
      bdaServices.add(EjbInjectionServices.class, new MockEjbInjectionServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE_INJECT, deployment, null);
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testMissingSecurityServices()
   {
      Bootstrap bootstrap = new MockBootstrap();
      ServiceRegistry deploymentServices = new SimpleServiceRegistry();
      deploymentServices.add(ResourceLoader.class, new MockResourceLoader());
      deploymentServices.add(TransactionServices.class, new MockTransactionServices());
      deploymentServices.add(ValidationServices.class, new MockValidationServices());
      deploymentServices.add(ServletServices.class, new MockServletServices());
      deploymentServices.add(EjbServices.class, new MockEjbServices());
      
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      
      bdaServices.add(JpaInjectionServices.class, new MockJpaServices());
      bdaServices.add(ResourceInjectionServices.class, new MockResourceServices());
      bdaServices.add(EjbInjectionServices.class, new MockEjbInjectionServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE_INJECT, deployment, null);
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testMissingValidationServices()
   {
      Bootstrap bootstrap = new MockBootstrap();
      ServiceRegistry deploymentServices = new SimpleServiceRegistry();
      deploymentServices.add(ResourceLoader.class, new MockResourceLoader());
      deploymentServices.add(TransactionServices.class, new MockTransactionServices());
      deploymentServices.add(SecurityServices.class, new MockSecurityServices());
      deploymentServices.add(ServletServices.class, new MockServletServices());
      deploymentServices.add(EjbServices.class, new MockEjbServices());
      
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      bdaServices.add(EjbInjectionServices.class, new MockEjbInjectionServices());
      bdaServices.add(JpaInjectionServices.class, new MockJpaServices());
      bdaServices.add(ResourceInjectionServices.class, new MockResourceServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE_INJECT, deployment, null);
   }

   @Test
   public void testEEEnv()
   {
      Bootstrap bootstrap = new MockBootstrap();
      ServiceRegistry deploymentServices = new SimpleServiceRegistry();
      deploymentServices.add(ResourceLoader.class, new MockResourceLoader());
      deploymentServices.add(TransactionServices.class, new MockTransactionServices());
      deploymentServices.add(SecurityServices.class, new MockSecurityServices());
      deploymentServices.add(ValidationServices.class, new MockValidationServices());
      deploymentServices.add(ServletServices.class, new MockServletServices());
      deploymentServices.add(EjbServices.class, new MockEjbServices());
      
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      bdaServices.add(EjbInjectionServices.class, new MockEjbInjectionServices());
      bdaServices.add(JpaInjectionServices.class, new MockJpaServices());
      bdaServices.add(ResourceInjectionServices.class, new MockResourceServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE_INJECT, deployment, null);
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testMissingTxServices()
   {
      Bootstrap bootstrap = new MockBootstrap();
      ServiceRegistry deploymentServices = new SimpleServiceRegistry();
      deploymentServices.add(ResourceLoader.class, new MockResourceLoader());
      deploymentServices.add(SecurityServices.class, new MockSecurityServices());
      deploymentServices.add(ValidationServices.class, new MockValidationServices());
      deploymentServices.add(ServletServices.class, new MockServletServices());
      deploymentServices.add(EjbServices.class, new MockEjbServices());
      
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      bdaServices.add(EjbInjectionServices.class, new MockEjbInjectionServices());
      bdaServices.add(JpaInjectionServices.class, new MockJpaServices());
      bdaServices.add(ResourceInjectionServices.class, new MockResourceServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE_INJECT, deployment, null);
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testMissingResourceServices()
   {
      Bootstrap bootstrap = new MockBootstrap();
      ServiceRegistry deploymentServices = new SimpleServiceRegistry();
      deploymentServices.add(ResourceLoader.class, new MockResourceLoader());
      deploymentServices.add(TransactionServices.class, new MockTransactionServices());
      deploymentServices.add(SecurityServices.class, new MockSecurityServices());
      deploymentServices.add(ValidationServices.class, new MockValidationServices());
      deploymentServices.add(ServletServices.class, new MockServletServices());
      deploymentServices.add(EjbServices.class, new MockEjbServices());
      
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      bdaServices.add(EjbInjectionServices.class, new MockEjbInjectionServices());
      bdaServices.add(JpaInjectionServices.class, new MockJpaServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE_INJECT, deployment, null);
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testMissingServletServices()
   {
      Bootstrap bootstrap = new MockBootstrap();
      ServiceRegistry deploymentServices = new SimpleServiceRegistry();
      deploymentServices.add(ResourceLoader.class, new MockResourceLoader());
      deploymentServices.add(TransactionServices.class, new MockTransactionServices());
      deploymentServices.add(SecurityServices.class, new MockSecurityServices());
      deploymentServices.add(ValidationServices.class, new MockValidationServices());
      deploymentServices.add(EjbServices.class, new MockEjbServices());
      
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      bdaServices.add(EjbInjectionServices.class, new MockEjbInjectionServices());
      bdaServices.add(JpaInjectionServices.class, new MockJpaServices());
      bdaServices.add(ResourceInjectionServices.class, new MockResourceServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE_INJECT, deployment, null);
   }

   @Test
   public void testSEEnv()
   {
      Bootstrap bootstrap = new MockBootstrap();
      ServiceRegistry deploymentServices = new SimpleServiceRegistry();
      deploymentServices.add(ResourceLoader.class, new MockResourceLoader());
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.SE, deployment, null);
   }

   @Test
   public void testServletEnv()
   {
      Bootstrap bootstrap = new MockBootstrap();
      ServiceRegistry deploymentServices = new SimpleServiceRegistry();
      deploymentServices.add(ResourceLoader.class, new MockResourceLoader());
      deploymentServices.add(ServletServices.class, new MockServletServices());
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.SERVLET, deployment, null);
   }

}
