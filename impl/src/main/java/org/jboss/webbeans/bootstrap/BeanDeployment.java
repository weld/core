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

import org.jboss.webbeans.BeanManagerImpl;
import org.jboss.webbeans.bean.builtin.DefaultValidatorBean;
import org.jboss.webbeans.bean.builtin.DefaultValidatorFactoryBean;
import org.jboss.webbeans.bean.builtin.InjectionPointBean;
import org.jboss.webbeans.bean.builtin.ManagerBean;
import org.jboss.webbeans.bean.builtin.PrincipalBean;
import org.jboss.webbeans.bean.builtin.UserTransactionBean;
import org.jboss.webbeans.bean.builtin.facade.EventBean;
import org.jboss.webbeans.bean.builtin.facade.InstanceBean;
import org.jboss.webbeans.bootstrap.api.Environment;
import org.jboss.webbeans.bootstrap.api.Environments;
import org.jboss.webbeans.bootstrap.api.ServiceRegistry;
import org.jboss.webbeans.bootstrap.api.helpers.SimpleServiceRegistry;
import org.jboss.webbeans.bootstrap.spi.BeanDeploymentArchive;
import org.jboss.webbeans.conversation.ConversationImpl;
import org.jboss.webbeans.conversation.JavaSEConversationTerminator;
import org.jboss.webbeans.conversation.NumericConversationIdGenerator;
import org.jboss.webbeans.conversation.ServletConversationManager;
import org.jboss.webbeans.ejb.EjbDescriptorCache;
import org.jboss.webbeans.ejb.spi.EjbServices;
import org.jboss.webbeans.log.Log;
import org.jboss.webbeans.log.Logging;
import org.jboss.webbeans.resources.spi.ResourceLoader;
import org.jboss.webbeans.security.spi.SecurityServices;
import org.jboss.webbeans.servlet.HttpSessionManager;
import org.jboss.webbeans.transaction.spi.TransactionServices;
import org.jboss.webbeans.validation.spi.ValidationServices;
import org.jboss.webbeans.xml.BeansXmlParser;

/**
 * @author pmuir
 *
 */
public class BeanDeployment
{

   private static final Log log = Logging.getLog(BeanDeployment.class);
   
   private final BeanDeploymentArchive beanDeploymentArchive;
   private final BeanManagerImpl beanManager;
   private final BeanDeployer beanDeployer;
   
   public BeanDeployment(BeanDeploymentArchive beanDeploymentArchive, BeanManagerImpl deploymentManager)
   {
      this.beanDeploymentArchive = beanDeploymentArchive;
      ServiceRegistry services = new SimpleServiceRegistry();
      services.addAll(deploymentManager.getServices().entrySet());
      services.addAll(beanDeploymentArchive.getServices().entrySet());
      this.beanManager = BeanManagerImpl.newManager(deploymentManager, services);
      EjbDescriptorCache ejbDescriptors = new EjbDescriptorCache();
      if (beanManager.getServices().contains(EjbServices.class))
      {
         // Must populate EJB cache first, as we need it to detect whether a
         // bean is an EJB!
         ejbDescriptors.addAll(beanDeploymentArchive.getEjbs());
      }
      
      beanDeployer = new BeanDeployer(beanManager, deploymentManager, ejbDescriptors);
      
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
      log.debug("Enabled policies for "  + this + ": " + beanManager.getEnabledPolicyClasses() + " " + beanManager.getEnabledPolicyStereotypes());
      log.debug("Enabled decorator types for "  + beanManager + ": " + beanManager.getEnabledDecoratorClasses());
      log.debug("Enabled interceptor types for "  + beanManager + ": " + beanManager.getEnabledInterceptorClasses());
   }
   
   public void deployBeans(Environment environment)
   {
      beanDeployer.addClasses(beanDeploymentArchive.getBeanClasses());
      beanDeployer.getEnvironment().addBean(new ManagerBean(beanManager));
      beanDeployer.getEnvironment().addBean(new InjectionPointBean(beanManager));
      beanDeployer.getEnvironment().addBean(new EventBean(beanManager));
      beanDeployer.getEnvironment().addBean(new InstanceBean(beanManager));
      if (!environment.equals(Environments.SE))
      {
         beanDeployer.addClass(ConversationImpl.class);
         beanDeployer.addClass(ServletConversationManager.class);
         beanDeployer.addClass(JavaSEConversationTerminator.class);
         beanDeployer.addClass(NumericConversationIdGenerator.class);
         beanDeployer.addClass(HttpSessionManager.class);
      }
      if (beanManager.getServices().contains(TransactionServices.class))
      {
         beanDeployer.getEnvironment().addBean(new UserTransactionBean(beanManager));
      }
      if (beanManager.getServices().contains(SecurityServices.class))
      {
         beanDeployer.getEnvironment().addBean(new PrincipalBean(beanManager));
      }
      if (beanManager.getServices().contains(ValidationServices.class))
      {
         beanDeployer.getEnvironment().addBean(new DefaultValidatorBean(beanManager));
         beanDeployer.getEnvironment().addBean(new DefaultValidatorFactoryBean(beanManager));
      }
      beanDeployer.createBeans().deploy();
   }

}
