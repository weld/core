package org.jboss.webbeans.bootstrap.api.test;

import org.jboss.webbeans.bootstrap.api.Bootstrap;
import org.jboss.webbeans.bootstrap.api.Environments;
import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.webbeans.bootstrap.api.test.MockDeployment.MockBeanDeploymentArchive;
import org.jboss.webbeans.bootstrap.spi.Deployment;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.jsf.spi.JSFServices;
import org.jboss.webbeans.persistence.spi.JpaServices;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.resources.spi.ResourceServices;
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
      deploymentServices.add(JSFServices.class, new MockJSFServices());
      
      bdaServices.add(JpaServices.class, new MockJpaServices());
      bdaServices.add(ResourceServices.class, new MockResourceServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE, deployment, null);
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
      deploymentServices.add(JSFServices.class, new MockJSFServices());
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      
      bdaServices.add(EjbServices.class, new MockEjbServices());
      bdaServices.add(ResourceServices.class, new MockResourceServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE, deployment, null);
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
      deploymentServices.add(JSFServices.class, new MockJSFServices());
      
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      bdaServices.add(EjbServices.class, new MockEjbServices());
      bdaServices.add(JpaServices.class, new MockJpaServices());
      bdaServices.add(ResourceServices.class, new MockResourceServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE, deployment, null);
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
      deploymentServices.add(JSFServices.class, new MockJSFServices());
      
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      bdaServices.add(EjbServices.class, new MockEjbServices());
      bdaServices.add(JpaServices.class, new MockJpaServices());
      bdaServices.add(ResourceServices.class, new MockResourceServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE, deployment, null);
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
      deploymentServices.add(JSFServices.class, new MockJSFServices());
      
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      bdaServices.add(EjbServices.class, new MockEjbServices());
      bdaServices.add(JpaServices.class, new MockJpaServices());
      bdaServices.add(ResourceServices.class, new MockResourceServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE, deployment, null);
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
      deploymentServices.add(JSFServices.class, new MockJSFServices());
      
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      bdaServices.add(EjbServices.class, new MockEjbServices());
      bdaServices.add(JpaServices.class, new MockJpaServices());
      bdaServices.add(ResourceServices.class, new MockResourceServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE, deployment, null);
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
      deploymentServices.add(JSFServices.class, new MockJSFServices());
      
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      bdaServices.add(EjbServices.class, new MockEjbServices());
      bdaServices.add(JpaServices.class, new MockJpaServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE, deployment, null);
   }

   @Test(expectedExceptions = IllegalStateException.class)
   public void testMissingJSFServices()
   {
      Bootstrap bootstrap = new MockBootstrap();
      ServiceRegistry deploymentServices = new SimpleServiceRegistry();
      deploymentServices.add(ResourceLoader.class, new MockResourceLoader());
      deploymentServices.add(TransactionServices.class, new MockTransactionServices());
      deploymentServices.add(SecurityServices.class, new MockSecurityServices());
      deploymentServices.add(ValidationServices.class, new MockValidationServices());
      deploymentServices.add(ServletServices.class, new MockServletServices());
      
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      bdaServices.add(EjbServices.class, new MockEjbServices());
      bdaServices.add(JpaServices.class, new MockJpaServices());
      bdaServices.add(ResourceServices.class, new MockResourceServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE, deployment, null);
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
      deploymentServices.add(JSFServices.class, new MockJSFServices());
      
      ServiceRegistry bdaServices = new SimpleServiceRegistry();
      bdaServices.add(EjbServices.class, new MockEjbServices());
      bdaServices.add(JpaServices.class, new MockJpaServices());
      bdaServices.add(ResourceServices.class, new MockResourceServices());
      
      Deployment deployment = new MockDeployment(deploymentServices, new MockBeanDeploymentArchive(bdaServices));
      bootstrap.startContainer(Environments.EE, deployment, null);
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
