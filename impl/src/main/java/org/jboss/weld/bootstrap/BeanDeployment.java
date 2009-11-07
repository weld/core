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
package org.jboss.weld.bootstrap;

import static org.jboss.weld.logging.Category.BOOTSTRAP;
import static org.jboss.weld.logging.LoggerFactory.loggerFactory;
import static org.jboss.weld.logging.messages.BootstrapMessage.ENABLED_DECORATORS;
import static org.jboss.weld.logging.messages.BootstrapMessage.ENABLED_INTERCEPTORS;
import static org.jboss.weld.logging.messages.BootstrapMessage.ENABLED_POLICIES;

import java.util.List;

import javax.enterprise.inject.spi.Bean;

import org.jboss.weld.BeanManagerImpl;
import org.jboss.weld.bean.RIBean;
import org.jboss.weld.bean.builtin.InjectionPointBean;
import org.jboss.weld.bean.builtin.ManagerBean;
import org.jboss.weld.bean.builtin.facade.EventBean;
import org.jboss.weld.bean.builtin.facade.InstanceBean;
import org.jboss.weld.bean.ee.DefaultValidatorBean;
import org.jboss.weld.bean.ee.DefaultValidatorFactoryBean;
import org.jboss.weld.bean.ee.PrincipalBean;
import org.jboss.weld.bean.ee.UserTransactionBean;
import org.jboss.weld.bootstrap.api.Environment;
import org.jboss.weld.bootstrap.api.ServiceRegistry;
import org.jboss.weld.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.weld.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.weld.ejb.EjbDescriptors;
import org.jboss.weld.ejb.spi.EjbServices;
import org.jboss.weld.resources.spi.ResourceLoader;
import org.jboss.weld.security.spi.SecurityServices;
import org.jboss.weld.transaction.spi.TransactionServices;
import org.jboss.weld.validation.spi.ValidationServices;
import org.jboss.weld.xml.BeansXmlParser;
import org.slf4j.cal10n.LocLogger;

/**
 * @author pmuir
 *
 */
public class BeanDeployment
{

   private static final LocLogger log = loggerFactory().getLogger(BOOTSTRAP);
   
   private final BeanDeploymentArchive beanDeploymentArchive;
   private final BeanManagerImpl beanManager;
   private final BeanDeployer beanDeployer;
   
   public BeanDeployment(BeanDeploymentArchive beanDeploymentArchive, BeanManagerImpl deploymentManager, ServiceRegistry deploymentServices)
   {
      this.beanDeploymentArchive = beanDeploymentArchive;
      EjbDescriptors ejbDescriptors = new EjbDescriptors();
      beanDeploymentArchive.getServices().add(EjbDescriptors.class, ejbDescriptors);
      ServiceRegistry services = new SimpleServiceRegistry();
      services.addAll(deploymentServices.entrySet());
      services.addAll(beanDeploymentArchive.getServices().entrySet());
      this.beanManager = BeanManagerImpl.newManager(deploymentManager, beanDeploymentArchive.getId(), services);
      if (beanManager.getServices().contains(EjbServices.class))
      {
         // Must populate EJB cache first, as we need it to detect whether a
         // bean is an EJB!
         ejbDescriptors.addAll(beanDeploymentArchive.getEjbs());
      }
      beanDeployer = new BeanDeployer(beanManager, ejbDescriptors);
      
      // Must at the Manager bean straight away, as it can be injected during startup!
      beanManager.addBean(new ManagerBean(beanManager));
      
      parseBeansXml();
   }
   
   public BeanManagerImpl getBeanManager()
   {
      return beanManager;
   }
   
   public BeanDeployer getBeanDeployer()
   {
      return beanDeployer;
   }
   
   public BeanDeploymentArchive getBeanDeploymentArchive()
   {
      return beanDeploymentArchive;
   }
   
   
   private void parseBeansXml()
   {
      BeansXmlParser parser = new BeansXmlParser(beanManager.getServices().get(ResourceLoader.class), getBeanDeploymentArchive().getBeansXml());
      parser.parse();
      
      if (parser.getEnabledPolicyClasses() != null)
      {
         beanManager.setEnabledPolicyClasses(parser.getEnabledPolicyClasses());
      }
      if (parser.getEnabledPolicyStereotypes() != null)
      {
         beanManager.setEnabledPolicyStereotypes(parser.getEnabledPolicyStereotypes());
      }
      if (parser.getEnabledDecoratorClasses() != null)
      {
         beanManager.setEnabledDecoratorClasses(parser.getEnabledDecoratorClasses());
      }
      if (parser.getEnabledInterceptorClasses() != null)
      {
         beanManager.setEnabledInterceptorClasses(parser.getEnabledInterceptorClasses());
      }
      log.debug(ENABLED_POLICIES, this.beanManager, beanManager.getEnabledPolicyClasses(), beanManager.getEnabledPolicyStereotypes());
      log.debug(ENABLED_DECORATORS, this.beanManager, beanManager.getEnabledDecoratorClasses());
      log.debug(ENABLED_INTERCEPTORS, this.beanManager, beanManager.getEnabledInterceptorClasses());
   }
   
   // TODO Move class stuff into startContainer phase
   // TODO read EJB descriptors after reading classes
   public void deployBeans(Environment environment)
   {
      beanDeployer.addClasses(beanDeploymentArchive.getBeanClasses());
      beanDeployer.getEnvironment().addBuiltInBean(new InjectionPointBean(beanManager));
      beanDeployer.getEnvironment().addBuiltInBean(new EventBean(beanManager));
      beanDeployer.getEnvironment().addBuiltInBean(new InstanceBean(beanManager));
      if (beanManager.getServices().contains(TransactionServices.class))
      {
         beanDeployer.getEnvironment().addBuiltInBean(new UserTransactionBean(beanManager));
      }
      if (beanManager.getServices().contains(SecurityServices.class))
      {
         beanDeployer.getEnvironment().addBuiltInBean(new PrincipalBean(beanManager));
      }
      if (beanManager.getServices().contains(ValidationServices.class))
      {
         beanDeployer.getEnvironment().addBuiltInBean(new DefaultValidatorBean(beanManager));
         beanDeployer.getEnvironment().addBuiltInBean(new DefaultValidatorFactoryBean(beanManager));
      }
      beanDeployer.createBeans().deploy();
   }

   public void afterBeanDiscovery(Environment environment)
   {
      doAfterBeanDiscovery(beanManager.getBeans());
      doAfterBeanDiscovery(beanManager.getDecorators());
      doAfterBeanDiscovery(beanManager.getInterceptors());
   }

   private void doAfterBeanDiscovery(List<? extends Bean> beanList)
   {
      for (Bean<?> bean : beanList)
      {
         if (bean instanceof RIBean)
         {
            ((RIBean) bean).initializeAfterBeanDiscovery();
         }
      }
   }
}
