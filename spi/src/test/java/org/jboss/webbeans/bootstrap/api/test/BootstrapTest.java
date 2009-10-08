package org.jboss.webbeans.bootstrap.api.test;

import org.jboss.webbeans.bootstrap.api.test.MockDeployment.MockBeanDeploymentArchive;
import org.jboss.weld.bootstrap.api.Bootstrap;
import org.jboss.weld.bootstrap.api.Environments;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.Deployment;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.injection.spi.EjbInjectionServices;
import org.jboss.weld.injection.spi.JpaInjectionServices;
import org.jboss.weld.injection.spi.ResourceInjectionServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.security.spi.SecurityServices;
import org.jboss.weld.servlet.api.ServletServices;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.validation.spi.ValidationServices;
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
